package edu.hitsz.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.hitsz.server.model.QuickMessage;
import edu.hitsz.server.model.Room;
import edu.hitsz.server.store.RoomStore;

import java.io.IOException;
import java.util.List;

import static edu.hitsz.server.handler.AccountHandler.*;

public class QuickMsgHandler implements HttpHandler {
    private final RoomStore roomStore = RoomStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.endsWith("/message") && "POST".equals(method)) {
                handleSend(exchange);
            } else if (path.endsWith("/messages") && "GET".equals(method)) {
                handleGet(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleSend(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        String message = extractJsonString(body, "message");
        if (playerId == null || roomId == null || message == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        String opponentId = room.getOpponentId(playerId);
        if (opponentId != null) {
            room.addMessageFor(opponentId, new QuickMessage(playerId, message));
        }
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String playerId = extractQueryParam(query, "playerId");
        String roomId = extractQueryParam(query, "roomId");
        if (playerId == null || roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        List<QuickMessage> msgs = room.pollMessagesFor(playerId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < msgs.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(msgs.get(i).toJson());
        }
        sb.append("]");
        sendResponse(exchange, 200, "{\"messages\":" + sb.toString() + "}");
    }

    private String extractQueryParam(String query, String key) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }
}
