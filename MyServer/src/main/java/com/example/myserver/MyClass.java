package com.example.myserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MyClass {
    private static final String USER_DATA =
            "{\n" +
                    "  \"userId\": \"1001\",\n" +
                    "  \"userName\": \"张三\",\n" +
                    "  \"age\": 20,\n" +
                    "  \"major\": \"计算机科学与技术\"\n" +
                    "}";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 10);
        server.createContext("/api/user/info", new UserInfoHandler());
        server.start();
        System.out.println("HTTP 服务器启动成功：http://localhost:8080/api/user/info");
    }

    static class UserInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            String query = exchange.getRequestURI().getQuery();
            String responseData = USER_DATA;
            if (query != null && query.contains("userId=")) {
                String userId = query.split("=")[1];
                responseData = responseData.replace("1001", userId);
            }

            byte[] responseBytes = responseData.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }
    }
}
