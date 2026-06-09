package com.smart_hire.desktop;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

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

public class SmartHireDesktopApp extends Application {
    private static final double WINDOW_WIDTH = 1480;
    private static final double WINDOW_HEIGHT = 920;
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

    private final String explicitFrontendUrl = System.getProperty(
            "smarthire.frontend.url",
            System.getenv("SMARTHIRE_FRONTEND_URL")
    );
    private final String apiBaseUrl = System.getProperty(
            "smarthire.api.base.url",
            System.getenv().getOrDefault("SMARTHIRE_API_BASE_URL", DEFAULT_API_BASE_URL)
    );

    private Label statusLabel;
    private Label loadingSubtitleLabel;
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
            frontendUrl = explicitFrontendUrl != null ? explicitFrontendUrl : "http://127.0.0.1:4173";
        }

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webView.setContextMenuEnabled(false);
        webEngine.setJavaScriptEnabled(true);

        StackPane webCard = buildWebCard(webView, webEngine);
        BorderPane shell = new BorderPane();
        shell.setPadding(new Insets(10));
        shell.setTop(buildHeader(stage, webEngine));
        shell.setCenter(webCard);
        shell.setStyle("-fx-background-color: linear-gradient(to bottom right, #07111f, #0d1a28 45%, #14263b 100%);");

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(WINDOW_WIDTH, Math.max(1080, bounds.getWidth() * 0.92));
        double initialHeight = Math.min(WINDOW_HEIGHT, Math.max(720, bounds.getHeight() * 0.92));

        Scene scene = new Scene(shell, initialWidth, initialHeight);
        stage.setTitle("SmartHire Desktop");
        stage.setMinWidth(Math.min(1100, bounds.getWidth() * 0.78));
        stage.setMinHeight(Math.min(760, bounds.getHeight() * 0.78));
        stage.setMaxWidth(bounds.getWidth());
        stage.setMaxHeight(bounds.getHeight());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        stage.setMaximized(true);
        webView.setZoom(1.0);
        startFrontendLoad(webEngine);
    }

    @Override
    public void stop() {
        if (embeddedFrontendServer != null) {
            embeddedFrontendServer.stop(0);
        }
    }

    private Region buildHeader(Stage stage, WebEngine webEngine) {
        StackPane header = new StackPane();
        header.setPrefHeight(92);

        Canvas art = new Canvas();
        art.widthProperty().bind(header.widthProperty());
        art.heightProperty().bind(header.heightProperty());
        art.widthProperty().addListener((obs, oldVal, newVal) -> drawHeader(art));
        art.heightProperty().addListener((obs, oldVal, newVal) -> drawHeader(art));

        VBox titleBox = new VBox(6);
        Label title = new Label("SmartHire Desktop");
        title.setTextFill(Color.web("#eff6ff"));
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));

        Label subtitle = new Label("Mevcut React akisi korunur, JavaFX kabugu ozel cizimlerle deneyimi masaustune tasir.");
        subtitle.setTextFill(Color.web("#c7d2e2"));
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(640);
        subtitle.setFont(Font.font("Segoe UI", 12));
        titleBox.getChildren().addAll(title, subtitle);

        VBox statusBox = new VBox(8);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusLabel = buildBadge("Baglaniyor");
        Label urlLabel = new Label(frontendUrl);
        urlLabel.setTextFill(Color.web("#dbeafe"));
        urlLabel.setFont(Font.font("Consolas", 12));

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button refreshButton = chromeButton("Yenile");
        refreshButton.setOnAction(event -> startFrontendLoad(webEngine));

        Button browserButton = chromeButton("Tarayicida Ac");
        browserButton.setOnAction(event -> getHostServices().showDocument(frontendUrl));

        Button minimizeButton = chromeButton("_");
        minimizeButton.setOnAction(event -> stage.setIconified(true));

        Button closeButton = chromeButton("X");
        closeButton.setOnAction(event -> Platform.exit());

        actions.getChildren().addAll(refreshButton, browserButton, minimizeButton, closeButton);
        statusBox.getChildren().addAll(statusLabel, urlLabel, actions);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(16, 18, 12, 18));
        content.setLeft(titleBox);
        content.setRight(statusBox);

        header.getChildren().addAll(art, content);
        return header;
    }

    private StackPane buildWebCard(WebView webView, WebEngine webEngine) {
        StackPane card = new StackPane();
        card.setPadding(new Insets(0));

        Pane backdrop = new Pane();
        backdrop.setStyle("-fx-background-color: rgba(9, 16, 27, 0.74); -fx-background-radius: 18; -fx-border-color: rgba(145, 185, 255, 0.18); -fx-border-radius: 18;");

        Canvas graphicsLayer = new Canvas();
        graphicsLayer.widthProperty().bind(card.widthProperty());
        graphicsLayer.heightProperty().bind(card.heightProperty());
        graphicsLayer.setMouseTransparent(true);
        graphicsLayer.widthProperty().addListener((obs, oldVal, newVal) -> drawBackdrop(graphicsLayer));
        graphicsLayer.heightProperty().addListener((obs, oldVal, newVal) -> drawBackdrop(graphicsLayer));

        StackPane webHolder = new StackPane(webView);
        webHolder.setPadding(new Insets(0));
        webHolder.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 18;");
        webView.prefWidthProperty().bind(webHolder.widthProperty());
        webView.prefHeightProperty().bind(webHolder.heightProperty());
        webView.setMinWidth(0);
        webView.setMinHeight(0);

        StackPane overlay = buildLoadingOverlay(webEngine);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        card.getChildren().addAll(backdrop, graphicsLayer, webHolder, overlay);
        return card;
    }

    private StackPane buildLoadingOverlay(WebEngine webEngine) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(5, 10, 18, 0.72);");

        Canvas pulse = new Canvas(420, 240);
        pulse.setEffect(new GaussianBlur(18));
        Timeline pulseTimeline = new Timeline(new KeyFrame(Duration.millis(42), event -> drawPulse(pulse)));
        pulseTimeline.setCycleCount(Animation.INDEFINITE);
        pulseTimeline.play();

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(84, 84);
        progressIndicator.setStyle("-fx-progress-color: #7dd3fc;");

        Label loadingTitle = new Label("SmartHire hazirlaniyor");
        loadingTitle.setTextFill(Color.WHITE);
        loadingTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        loadingSubtitleLabel = new Label("Frontend yukleniyor.");
        loadingSubtitleLabel.setTextFill(Color.web("#c8d4e5"));
        loadingSubtitleLabel.setFont(Font.font("Segoe UI", 13));
        loadingSubtitleLabel.setWrapText(true);
        loadingSubtitleLabel.setMaxWidth(460);

        retryButton = chromeButton("Tekrar Dene");
        retryButton.setVisible(false);
        retryButton.setManaged(false);
        retryButton.setOnAction(event -> startFrontendLoad(webEngine));

        VBox content = new VBox(16, progressIndicator, loadingTitle, loadingSubtitleLabel, retryButton);
        content.setAlignment(Pos.CENTER);

        overlay.getChildren().addAll(pulse, content);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING || newState == Worker.State.SCHEDULED) {
                updateStatus("Baglaniyor");
                updateOverlayMessage("Arayuz desktop icinden yukleniyor.");
                retryButton.setVisible(false);
                retryButton.setManaged(false);
            } else if (newState == Worker.State.SUCCEEDED) {
                updateStatus("Hazir");
            } else if (newState == Worker.State.FAILED) {
                showFrontendUnavailable(webEngine, "Arayuz sayfasi yuklenemedi.");
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
                updateStatus("Baglanti Hatasi");
                updateOverlayMessage(newEx.getMessage());
            }
        });

        return overlay;
    }

    private void handleWebError(WebErrorEvent event) {
        if (event != null && event.getMessage() != null && !event.getMessage().isBlank()) {
            updateStatus("Web Hatasi");
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
                        showFrontendUnavailable(webEngine, "Frontend ulasildi ancak beklenmeyen HTTP durumu dondu: " + statusCode);
                    }
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> showFrontendUnavailable(
                            webEngine,
                            "Desktop icindeki frontend servisi veya API proxy acilamadi."
                    ));
                    return null;
                });
    }

    private void showFrontendUnavailable(WebEngine webEngine, String message) {
        updateStatus("Baglanti Hatasi");
        updateOverlayMessage(message + " Hedef adres: " + frontendUrl);
        retryButton.setVisible(true);
        retryButton.setManaged(true);
        webEngine.loadContent(buildUnavailablePage(message));
    }

    private void updateOverlayMessage(String message) {
        if (loadingSubtitleLabel != null) {
            loadingSubtitleLabel.setText(message);
        }
    }

    private String buildUnavailablePage(String message) {
        String safeMessage = escapeHtml(message);
        String safeUrl = escapeHtml(frontendUrl);
        return """
                <!doctype html>
                <html lang="tr">
                <head>
                  <meta charset="utf-8" />
                  <title>SmartHire Desktop</title>
                  <style>
                    body {
                      margin: 0;
                      min-height: 100vh;
                      display: grid;
                      place-items: center;
                      background:
                        radial-gradient(circle at top right, rgba(56,189,248,0.18), transparent 24%),
                        linear-gradient(180deg, #07111f, #0b1626);
                      color: #eef8ff;
                      font-family: "Segoe UI", sans-serif;
                    }
                    .panel {
                      width: min(760px, calc(100vw - 48px));
                      padding: 32px;
                      border-radius: 28px;
                      border: 1px solid rgba(148,184,216,0.18);
                      background: rgba(8,18,31,0.84);
                      box-shadow: 0 24px 60px rgba(0,0,0,0.35);
                    }
                    .kicker {
                      color: #5eead4;
                      text-transform: uppercase;
                      letter-spacing: 0.16em;
                      font-size: 12px;
                      margin-bottom: 12px;
                    }
                    h1 { margin: 0 0 12px; font-size: 34px; }
                    p { color: #9db3ca; line-height: 1.6; }
                    code {
                      display: inline-block;
                      margin-top: 8px;
                      padding: 10px 14px;
                      border-radius: 12px;
                      background: rgba(255,255,255,0.05);
                      color: #dffaf5;
                    }
                    .steps {
                      margin-top: 18px;
                      padding-left: 18px;
                      color: #d6e4f0;
                    }
                  </style>
                </head>
                <body>
                  <div class="panel">
                    <div class="kicker">Desktop Frontend Error</div>
                    <h1>Desktop kabugu acildi ama arayuz gelmedi.</h1>
                    <p>%s</p>
                    <p>Beklenen adres:</p>
                    <code>%s</code>
                    <ol class="steps">
                      <li><code>cd c:\\Users\\anisa\\SmartHireAI\\frontend</code></li>
                      <li><code>npm run build</code></li>
                      <li>Backend kullanimi icin `dispatcher` servisinin acik oldugundan emin ol.</li>
                    </ol>
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
        if (explicitFrontendUrl != null && !explicitFrontendUrl.isBlank()) {
            return explicitFrontendUrl;
        }

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

    private Label buildBadge(String text) {
        Label badge = new Label(text);
        badge.setTextFill(Color.web("#ecfeff"));
        badge.setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-border-color: rgba(125,211,252,0.34); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 8 14;");
        badge.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        return badge;
    }

    private Button chromeButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        button.setTextFill(Color.web("#eff6ff"));
        button.setStyle("-fx-background-color: rgba(21, 39, 62, 0.88); -fx-border-color: rgba(147, 197, 253, 0.24); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 9 16; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: rgba(35, 58, 89, 0.98); -fx-border-color: rgba(125, 211, 252, 0.55); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 9 16; -fx-cursor: hand;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: rgba(21, 39, 62, 0.88); -fx-border-color: rgba(147, 197, 253, 0.24); -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 9 16; -fx-cursor: hand;"));
        return button;
    }

    private void updateStatus(String text) {
        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(text);
        String style = switch (text) {
            case "Hazir" -> "-fx-background-color: rgba(34,197,94,0.18); -fx-border-color: rgba(74,222,128,0.36);";
            case "Baglanti Hatasi", "Web Hatasi" -> "-fx-background-color: rgba(239,68,68,0.18); -fx-border-color: rgba(248,113,113,0.42);";
            default -> "-fx-background-color: rgba(56,189,248,0.18); -fx-border-color: rgba(125,211,252,0.34);";
        };
        statusLabel.setStyle(style + " -fx-border-radius: 999; -fx-background-radius: 999; -fx-padding: 8 14;");
    }

    private void drawHeader(Canvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setFill(new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#0f1f35")),
                new javafx.scene.paint.Stop(0.55, Color.web("#112842")),
                new javafx.scene.paint.Stop(1, Color.web("#17395c"))
        ));
        gc.fillRoundRect(0, 0, width, height, 36, 36);

        gc.setStroke(Color.web("#7dd3fc", 0.28));
        gc.setLineWidth(2.2);
        gc.beginPath();
        gc.moveTo(0, height * 0.72);
        gc.bezierCurveTo(width * 0.22, height * 0.44, width * 0.46, height * 0.96, width * 0.78, height * 0.48);
        gc.bezierCurveTo(width * 0.88, height * 0.34, width * 0.96, height * 0.52, width, height * 0.28);
        gc.stroke();

        gc.setFill(Color.web("#38bdf8", 0.15));
        gc.fillOval(width - 220, -40, 240, 180);
        gc.setFill(Color.web("#22c55e", 0.10));
        gc.fillOval(width - 360, 18, 180, 120);
        gc.setFill(Color.web("#f59e0b", 0.08));
        gc.fillOval(42, 28, 140, 92);
    }

    private void drawBackdrop(Canvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setFill(Color.web("#07111f", 0.72));
        gc.fillRoundRect(0, 0, width, height, 32, 32);

        gc.setStroke(Color.web("#60a5fa", 0.12));
        gc.setLineWidth(1);
        for (double x = 24; x < width; x += 34) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double y = 24; y < height; y += 34) {
            gc.strokeLine(0, y, width, y);
        }

        gc.setFill(Color.web("#38bdf8", 0.08));
        gc.fillOval(width - 300, 36, 210, 160);
        gc.setFill(Color.web("#22c55e", 0.07));
        gc.fillOval(36, height - 180, 230, 150);
    }

    private void drawPulse(Canvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double seconds = System.currentTimeMillis() / 1000.0;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double radiusA = 140 + Math.sin(seconds * 1.6) * 22;
        double radiusB = 112 + Math.cos(seconds * 1.2) * 18;

        gc.setFill(Color.web("#38bdf8", 0.20));
        gc.fillOval(width * 0.5 - radiusA * 0.7, height * 0.54 - radiusA * 0.45, radiusA, radiusA * 0.8);

        gc.setFill(Color.web("#22c55e", 0.14));
        gc.fillOval(width * 0.5 - radiusB * 0.4, height * 0.5 - radiusB * 0.5, radiusB, radiusB);

        gc.setFill(Color.web("#f59e0b", 0.12));
        gc.fillOval(width * 0.5 - 40, height * 0.5 - 36, 80, 72);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
