package com.smart_hire.dispatcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DispatcherGatewayIntegrationTest {

    private static final StubDownstreamServer STUB_SERVER = StubDownstreamServer.start();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("dispatcher.services.auth-url", STUB_SERVER::baseUrl);
        registry.add("dispatcher.services.document-url", STUB_SERVER::baseUrl);
        registry.add("dispatcher.services.job-url", STUB_SERVER::baseUrl);
        registry.add("dispatcher.services.application-url", STUB_SERVER::baseUrl);
        registry.add("dispatcher.services.analysis-url", STUB_SERVER::baseUrl);
    }

    @AfterEach
    void tearDown() {
        STUB_SERVER.reset();
    }

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldForwardPublicRegisterRequestToAuthService() throws Exception {
        STUB_SERVER.stub("POST", "/api/auth/register", 201, "", Map.of("Content-Type", "application/json"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                        {
                          "username": "recruiter1",
                          "email": "recruiter1@smart-hire.dev",
                          "password": "secret123"
                        }
                        """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(STUB_SERVER.recordedRequests()).hasSize(1);
        assertThat(STUB_SERVER.recordedRequests().getFirst().path()).isEqualTo("/api/auth/register");
    }

    @Test
    void shouldForwardPublicLoginRequestToAuthService() throws Exception {
        STUB_SERVER.stub(
                "POST",
                "/api/auth/login",
                200,
                """
                {"token":"jwt-token"}
                """,
                Map.of("Content-Type", "application/json")
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                        {
                          "username": "recruiter1",
                          "password": "secret123"
                        }
                        """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"token":"jwt-token"}
                        """));

        assertThat(STUB_SERVER.recordedRequests()).hasSize(1);
        assertThat(STUB_SERVER.recordedRequests().getFirst().path()).isEqualTo("/api/auth/login");
    }

    @Test
    void shouldForwardPublicJobsListingRequestToJobService() throws Exception {
        STUB_SERVER.stub(
                "GET",
                "/api/jobs",
                200,
                """
                [{"id":101,"title":"Platform Engineer"}]
                """,
                Map.of("Content-Type", "application/json")
        );

        mockMvc.perform(get("/api/jobs")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":101,"title":"Platform Engineer"}]
                        """));

        assertThat(STUB_SERVER.recordedRequests()).hasSize(1);
        assertThat(STUB_SERVER.recordedRequests().getFirst().path()).isEqualTo("/api/jobs");
    }

    @Test
    void shouldRejectProtectedDocumentRequestWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/documents/doc-77")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        assertThat(STUB_SERVER.recordedRequests()).isEmpty();
    }

    private record RecordedRequest(String method, String path, String body, HttpHeaders headers) {
    }

    private record StubResponse(int status, String body, Map<String, String> headers) {
    }

    private static final class StubDownstreamServer implements HttpHandler {

        private final HttpServer server;
        private final List<RecordedRequest> recordedRequests = new CopyOnWriteArrayList<>();
        private final Map<String, StubResponse> responses = new ConcurrentHashMap<>();

        private StubDownstreamServer(HttpServer server) {
            this.server = server;
        }

        static StubDownstreamServer start() {
            try {
                HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
                StubDownstreamServer stubDownstreamServer = new StubDownstreamServer(httpServer);
                httpServer.createContext("/", stubDownstreamServer);
                httpServer.start();
                return stubDownstreamServer;
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to start stub downstream server", exception);
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String body = readBody(exchange.getRequestBody());

            HttpHeaders headers = new HttpHeaders();
            exchange.getRequestHeaders().forEach(headers::put);
            recordedRequests.add(new RecordedRequest(method, path, body, headers));

            StubResponse response = responses.getOrDefault(method + " " + path, new StubResponse(404, "", Map.of()));
            response.headers().forEach((headerName, headerValue) -> exchange.getResponseHeaders().add(headerName, headerValue));

            byte[] payload = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.status(), payload.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(payload);
            }
        }

        void stub(String method, String path, int status, String body, Map<String, String> headers) {
            responses.put(method + " " + path, new StubResponse(status, body, headers));
        }

        void reset() {
            recordedRequests.clear();
            responses.clear();
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        List<RecordedRequest> recordedRequests() {
            return recordedRequests;
        }

        private String readBody(InputStream inputStream) throws IOException {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
