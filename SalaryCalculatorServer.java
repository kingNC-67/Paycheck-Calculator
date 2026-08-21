import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaryCalculatorServer {
    private static final double TAX_RATE = 0.15;
    private static final Pattern HOURS_PATTERN = Pattern.compile("\"hours\"\\s*:\\s*(-?(?:\\d+(?:\\.\\d*)?|\\.\\d+))");
    private static final Pattern RATE_PATTERN = Pattern.compile("\"hourlyRate\"\\s*:\\s*(-?(?:\\d+(?:\\.\\d*)?|\\.\\d+))");
    private static final Path PUBLIC_DIRECTORY = Paths.get("public").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        int port = getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api/calculate", new CalculateHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Salary Calculator is running on port " + port);
    }

    private static int getPort() {
        String configuredPort = System.getenv("PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return 5000;
        }
        try {
            return Integer.parseInt(configuredPort);
        } catch (NumberFormatException exception) {
            return 5000;
        }
    }

    private static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Double hours = readNumber(requestBody, HOURS_PATTERN);
            Double hourlyRate = readNumber(requestBody, RATE_PATTERN);

            if (hours == null || hourlyRate == null
                    || !Double.isFinite(hours) || !Double.isFinite(hourlyRate)
                    || hours < 0 || hours > 168 || hourlyRate < 0 || hourlyRate > 1_000_000) {
                sendJson(exchange, 400,
                        "{\"error\":\"Enter valid values: 0–168 hours and a non-negative hourly rate.\"}");
                return;
            }

            double grossPay = hours * hourlyRate;
            double taxes = grossPay * TAX_RATE;
            double netPay = grossPay - taxes;
            String response = String.format(Locale.US,
                    "{\"hours\":%.2f,\"hourlyRate\":%.2f,\"grossPay\":%.2f,\"taxes\":%.2f,\"netPay\":%.2f}",
                    hours, hourlyRate, grossPay, taxes, netPay);
            sendJson(exchange, 200, response);
        }
    }

    private static Double readNumber(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed.");
                return;
            }

            String requestedPath = exchange.getRequestURI().getPath();
            if (requestedPath == null || requestedPath.equals("/")) {
                requestedPath = "/index.html";
            }

            Path file = PUBLIC_DIRECTORY.resolve(requestedPath.substring(1)).normalize();
            if (!file.startsWith(PUBLIC_DIRECTORY) || !Files.isRegularFile(file)) {
                sendText(exchange, 404, "Not found.");
                return;
            }

            byte[] content = Files.readAllBytes(file);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType(file));
            headers.set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(content);
            }
        }

        private static String contentType(Path file) {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".css")) return "text/css; charset=utf-8";
            if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
            return "application/octet-stream";
        }

        private static void sendText(HttpExchange exchange, int statusCode, String body) throws IOException {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        }
    }
}