package com.smart_hire.mobile;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class SmartHireMobileApp extends Application {
    private static final double WINDOW_WIDTH = 430;
    private static final double WINDOW_HEIGHT = 880;
    private static final String DEFAULT_API_BASE_URL = "http://localhost:8080";
    private static final Set<String> BLOCKED_REQUEST_HEADERS = Set.of(
            "host",
            "content-length",
            "connection",
            "upgrade",
            "expect",
            "http2-settings",
            "transfer-encoding"
    );

    private final String apiBaseUrl = System.getProperty(
            "smarthire.api.base.url",
            System.getenv().getOrDefault("SMARTHIRE_API_BASE_URL", DEFAULT_API_BASE_URL)
    );

    private Label statusBadge;
    private Label statusText;
    private Button retryButton;
    private String frontendUrl;
    private HttpServer embeddedFrontendServer;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.of(3, ChronoUnit.SECONDS))
            .build();

    @Override
    public void start(Stage stage) {
        try {
            frontendUrl = resolveFrontendUrl();
        } catch (IOException exception) {
            frontendUrl = "http://127.0.0.1:4173";
        }

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.setPageFill(Color.web("#04111d"));
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        BorderPane shell = new BorderPane();
        shell.setTop(buildHeader(webEngine));
        shell.setCenter(buildContent(webView, webEngine));
        shell.setStyle("-fx-background-color: linear-gradient(to bottom, #04111d, #0a1f32 52%, #12334d 100%);");

        Scene scene = new Scene(shell, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("SmartHire Mobile");
        stage.setMinWidth(360);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();

        startFrontendLoad(webEngine);
    }

    @Override
    public void stop() {
        if (embeddedFrontendServer != null) {
            embeddedFrontendServer.stop(0);
        }
    }

    private Region buildHeader(WebEngine webEngine) {
        VBox wrapper = new VBox(10);
        wrapper.setPadding(new Insets(18, 18, 12, 18));

        Label title = new Label("SmartHire Mobile");
        title.setTextFill(Color.web("#f8fbff"));
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));

        Label subtitle = new Label("Ayni SmartHire akisi, mobil odakli bir JavaFX ve Gluon kabugu icinde.");
        subtitle.setTextFill(Color.web("#d2dfeb"));
        subtitle.setWrapText(true);
        subtitle.setFont(Font.font("Segoe UI", 12));

        statusBadge = new Label("Baglaniyor");
        statusBadge.setTextFill(Color.web("#eff6ff"));
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        statusBadge.setStyle("-fx-background-color: rgba(56,189,248,0.18); -fx-border-color: rgba(125,211,252,0.34); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 6 12;");

        Button refreshButton = pillButton("Yenile");
        refreshButton.setOnAction(event -> startFrontendLoad(webEngine));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox badgeRow = new HBox(10, statusBadge, spacer, refreshButton);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        wrapper.getChildren().addAll(title, subtitle, badgeRow);
        return wrapper;
    }

    private StackPane buildContent(WebView webView, WebEngine webEngine) {
        StackPane root = new StackPane();
        root.setPadding(new Insets(0, 14, 14, 14));

        StackPane webCard = new StackPane(webView);
        webCard.setStyle("-fx-background-color: rgba(8,18,31,0.88); -fx-background-radius: 24; -fx-border-color: rgba(148,184,216,0.16); -fx-border-radius: 24;");
        webView.setZoom(0.94);

        StackPane overlay = buildLoadingOverlay(webEngine);

        root.getChildren().addAll(webCard, overlay);
        return root;
    }

    private StackPane buildLoadingOverlay(WebEngine webEngine) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(4, 17, 29, 0.72);");

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(78, 78);
        indicator.setStyle("-fx-progress-color: #38bdf8;");

        Label title = new Label("Mobil arayuz yukleniyor");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        statusText = new Label("Frontend hazirlaniyor.");
        statusText.setTextFill(Color.web("#d4deea"));
        statusText.setWrapText(true);
        statusText.setMaxWidth(280);
        statusText.setFont(Font.font("Segoe UI", 12));

        retryButton = pillButton("Tekrar Dene");
        retryButton.setVisible(false);
        retryButton.setManaged(false);
        retryButton.setOnAction(event -> startFrontendLoad(webEngine));

        VBox content = new VBox(16, indicator, title, statusText, retryButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING || newState == Worker.State.SCHEDULED) {
                updateStatus("Baglaniyor");
                updateOverlayMessage("SmartHire mobil kabugu web arayuzune baglaniyor.");
                retryButton.setVisible(false);
                retryButton.setManaged(false);
            } else if (newState == Worker.State.SUCCEEDED) {
                updateStatus("Hazir");
            } else if (newState == Worker.State.FAILED) {
                showFrontendUnavailable(webEngine, "Mobil arayuz yuklenemedi.");
            }
        });

        overlay.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> webEngine.getLoadWorker().getState() != Worker.State.SUCCEEDED,
                        webEngine.getLoadWorker().stateProperty()
                )
        );
        overlay.managedProperty().bind(overlay.visibleProperty());

        webEngine.setOnError(this::handleWebError);
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                updateStatus("Hata");
                updateOverlayMessage(newEx.getMessage());
            }
        });

        overlay.getChildren().add(content);
        return overlay;
    }

    private void handleWebError(WebErrorEvent event) {
        if (event != null && event.getMessage() != null && !event.getMessage().isBlank()) {
            updateStatus("Hata");
            updateOverlayMessage(event.getMessage());
        }
    }

    private void startFrontendLoad(WebEngine webEngine) {
        updateStatus("Baglaniyor");
        updateOverlayMessage("Frontend baglantisi kontrol ediliyor: " + frontendUrl);
        retryButton.setVisible(false);
        retryButton.setManaged(false);

        HttpRequest request = HttpRequest.newBuilder(URI.create(frontendUrl))
                .timeout(java.time.Duration.of(3, ChronoUnit.SECONDS))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> Platform.runLater(() -> {
                    int statusCode = response.statusCode();
                    if (statusCode >= 200 && statusCode < 400) {
                        webEngine.load(frontendUrl);
                    } else {
                        showFrontendUnavailable(webEngine, "Frontend beklenmeyen HTTP durumu dondurdu: " + statusCode);
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> showFrontendUnavailable(
                            webEngine,
                            "Frontend servisine veya mobil API proxy katmanina ulasilamadi."
                    ));
                    return null;
                });
    }

    private void showFrontendUnavailable(WebEngine webEngine, String message) {
        updateStatus("Hata");
        updateOverlayMessage(message + " Hedef adres: " + frontendUrl);
        retryButton.setVisible(true);
        retryButton.setManaged(true);
        webEngine.loadContent(buildUnavailablePage(message));
    }

    private void updateOverlayMessage(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }

    private void updateStatus(String text) {
        if (statusBadge == null) {
            return;
        }

        statusBadge.setText(text);
        String style = switch (text) {
            case "Hazir" -> "-fx-background-color: rgba(34,197,94,0.18); -fx-border-color: rgba(74,222,128,0.36);";
            case "Hata" -> "-fx-background-color: rgba(239,68,68,0.18); -fx-border-color: rgba(248,113,113,0.42);";
            default -> "-fx-background-color: rgba(56,189,248,0.18); -fx-border-color: rgba(125,211,252,0.34);";
        };
        statusBadge.setStyle(style + " -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 6 12;");
    }

    private Button pillButton(String text) {
        Button button = new Button(text);
        button.setTextFill(Color.web("#eff6ff"));
        button.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        button.setStyle("-fx-background-color: rgba(15, 44, 70, 0.96); -fx-border-color: rgba(125, 211, 252, 0.32); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 8 14;");
        return button;
    }

    private String buildUnavailablePage(String message) {
        String safeMessage = escapeHtml(message);
        String safeUrl = escapeHtml(frontendUrl);
        return """
                <!doctype html>
                <html lang="tr">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>SmartHire Mobile</title>
                  <style>
                    body {
                      margin: 0;
                      min-height: 100vh;
                      display: grid;
                      place-items: center;
                      padding: 24px;
                      background:
                        radial-gradient(circle at top, rgba(56,189,248,0.18), transparent 28%),
                        linear-gradient(180deg, #04111d, #0d2337);
                      color: #eef8ff;
                      font-family: "Segoe UI", sans-serif;
                    }
                    .panel {
                      width: min(100%, 420px);
                      padding: 24px;
                      border-radius: 24px;
                      background: rgba(8,18,31,0.88);
                      border: 1px solid rgba(148,184,216,0.18);
                      box-shadow: 0 18px 40px rgba(0,0,0,0.28);
                    }
                    .eyebrow {
                      color: #5eead4;
                      letter-spacing: 0.12em;
                      text-transform: uppercase;
                      font-size: 11px;
                      margin-bottom: 10px;
                    }
                    h1 { margin: 0 0 10px; font-size: 28px; }
                    p { color: #c3d3e4; line-height: 1.6; }
                    code {
                      display: block;
                      margin-top: 8px;
                      padding: 10px 12px;
                      border-radius: 14px;
                      background: rgba(255,255,255,0.05);
                      overflow-wrap: anywhere;
                    }
                  </style>
                </head>
                <body>
                  <div class="panel">
                    <div class="eyebrow">Mobile Frontend Error</div>
                    <h1>Mobil kabuk acildi ama arayuz gelmedi.</h1>
                    <p>%s</p>
                    <p>Beklenen adres:</p>
                    <code>%s</code>
                  </div>
                </body>
                </html>
                """.formatted(safeMessage, safeUrl);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String resolveFrontendUrl() throws IOException {
        Path distDir = locateFrontendDist();
        embeddedFrontendServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        embeddedFrontendServer.createContext("/api", new ApiProxyHandler());
        embeddedFrontendServer.createContext("/", new StaticFrontendHandler(distDir));
        embeddedFrontendServer.setExecutor(Executors.newCachedThreadPool());
        embeddedFrontendServer.start();
        return "http://127.0.0.1:" + embeddedFrontendServer.getAddress().getPort();
    }

    private Path locateFrontendDist() throws IOException {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> candidates = List.of(
                cwd.resolve("../frontend/dist").normalize(),
                cwd.resolve("frontend/dist").normalize(),
                Path.of("c:/Users/anisa/SmartHireAI/frontend/dist").normalize()
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate.resolve("index.html"))) {
                return candidate;
            }
        }

        throw new IOException("frontend/dist/index.html bulunamadi");
    }

    private final class StaticFrontendHandler implements HttpHandler {
        private final Path distDir;

        private StaticFrontendHandler(Path distDir) {
            this.distDir = distDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Path target = resolvePath(exchange.getRequestURI().getPath());
            if (target == null || !Files.exists(target) || Files.isDirectory(target)) {
                target = distDir.resolve("index.html");
            }

            byte[] bytes = Files.readAllBytes(target);
            exchange.getResponseHeaders().set("Content-Type", detectContentType(target));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        }

        private Path resolvePath(String requestPath) {
            String sanitized = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
            if (sanitized.isBlank()) {
                return distDir.resolve("index.html");
            }

            Path candidate = distDir.resolve(sanitized).normalize();
            if (!candidate.startsWith(distDir)) {
                return null;
            }
            return candidate;
        }

        private String detectContentType(Path target) throws IOException {
            String probed = Files.probeContentType(target);
            if (probed != null) {
                return probed;
            }

            String name = target.getFileName().toString();
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (name.endsWith(".css")) return "text/css; charset=utf-8";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".json")) return "application/json; charset=utf-8";
            return "application/octet-stream";
        }
    }

    private final class ApiProxyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                byte[] requestBody = readRequestBody(exchange);
                HttpRequest.BodyPublisher bodyPublisher = requestBody.length == 0
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofByteArray(requestBody);

                URI targetUri = URI.create(apiBaseUrl + exchange.getRequestURI());
                HttpRequest.Builder builder = HttpRequest.newBuilder(targetUri)
                        .timeout(java.time.Duration.of(30, ChronoUnit.SECONDS))
                        .method(exchange.getRequestMethod(), bodyPublisher);

                for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
                    String headerName = entry.getKey();
                    if (BLOCKED_REQUEST_HEADERS.contains(headerName.toLowerCase())) {
                        continue;
                    }
                    for (String value : entry.getValue()) {
                        builder.header(headerName, value);
                    }
                }

                HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
                Headers responseHeaders = exchange.getResponseHeaders();
                response.headers().map().forEach((name, values) -> {
                    if (!"transfer-encoding".equalsIgnoreCase(name)) {
                        responseHeaders.put(name, values);
                    }
                });

                byte[] responseBody = response.body();
                exchange.sendResponseHeaders(response.statusCode(), responseBody.length);
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(responseBody);
                }
            } catch (Exception exception) {
                byte[] body = ("API proxy error: " + exception.getMessage()).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(502, body.length);
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(body);
                }
            }
        }

        private byte[] readRequestBody(HttpExchange exchange) throws IOException {
            try (InputStream inputStream = exchange.getRequestBody()) {
                return inputStream.readAllBytes();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
