package edu.hitsz.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.hitsz.server.model.Player;
import edu.hitsz.server.store.PlayerStore;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AccountHandler implements HttpHandler {
    private final PlayerStore playerStore = PlayerStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (!"POST".equals(method)) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);

        if (path.endsWith("/register")) {
            handleRegister(exchange, body);
        } else if (path.endsWith("/login")) {
            handleLogin(exchange, body);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleRegister(HttpExchange exchange, String body) throws IOException {
        String nickname = extractJsonString(body, "nickname");
        String password = extractJsonString(body, "password");
        if (nickname == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing nickname or password\"}");
            return;
        }
        Player p = playerStore.register(nickname, password);
        if (p == null) {
            sendResponse(exchange, 409, "{\"error\":\"Nickname already exists\"}");
            return;
        }
        sendResponse(exchange, 200, "{\"success\":true,\"player\":" + p.toJson() + "}");
    }

    private void handleLogin(HttpExchange exchange, String body) throws IOException {
        String nickname = extractJsonString(body, "nickname");
        String password = extractJsonString(body, "password");
        if (nickname == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing nickname or password\"}");
            return;
        }
        Player p = playerStore.login(nickname, password);
        if (p == null) {
            sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            return;
        }
        sendResponse(exchange, 200, "{\"success\":true,\"player\":" + p.toJson() + "}");
    }

    static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    static void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    static String extractJsonInt(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) end++;
        if (end == start) return null;
        return json.substring(start, end);
    }

    static String extractJsonBoolean(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        if (json.startsWith("true", start)) return "true";
        if (json.startsWith("false", start)) return "false";
        return null;
    }
}
