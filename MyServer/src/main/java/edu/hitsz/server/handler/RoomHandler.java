package edu.hitsz.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.hitsz.server.model.Room;
import edu.hitsz.server.store.RoomStore;

import java.io.IOException;

import static edu.hitsz.server.handler.AccountHandler.*;

public class RoomHandler implements HttpHandler {
    private final RoomStore roomStore = RoomStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.endsWith("/create") && "POST".equals(method)) {
                handleCreate(exchange);
            } else if (path.endsWith("/join") && "POST".equals(method)) {
                handleJoin(exchange);
            } else if (path.endsWith("/info") && "GET".equals(method)) {
                handleInfo(exchange);
            } else if (path.endsWith("/ready") && "POST".equals(method)) {
                handleReady(exchange);
            } else if (path.endsWith("/start") && "POST".equals(method)) {
                handleStart(exchange);
            } else if (path.endsWith("/return") && "POST".equals(method)) {
                handleReturn(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String difficulty = extractJsonString(body, "difficulty");
        if (playerId == null || difficulty == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing playerId or difficulty\"}");
            return;
        }
        Room room = roomStore.createRoom(playerId, difficulty);
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
    }

    private void handleJoin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        if (playerId == null || roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing playerId or roomId\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        if (room.isFull()) {
            sendResponse(exchange, 409, "{\"error\":\"Room is full\"}");
            return;
        }
        room.setGuestId(playerId);
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
    }

    private void handleInfo(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String roomId = extractQueryParam(query, "roomId");
        if (roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing roomId\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
    }

    private void handleReady(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        if (playerId == null || roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        if (room.isHost(playerId)) room.setHostReady(true);
        else if (room.isGuest(playerId)) room.setGuestReady(true);
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
    }

    private void handleStart(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        if (playerId == null || roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        if (!room.isHost(playerId)) {
            sendResponse(exchange, 403, "{\"error\":\"Only host can start\"}");
            return;
        }
        if (!room.isHostReady() || !room.isGuestReady()) {
            sendResponse(exchange, 400, "{\"error\":\"Both players must be ready\"}");
            return;
        }
        room.setStatus(Room.Status.PLAYING);
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
    }

    private void handleReturn(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        if (playerId == null || roomId == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }
        Room room = roomStore.getRoom(roomId);
        if (room == null) {
            sendResponse(exchange, 404, "{\"error\":\"Room not found\"}");
            return;
        }
        if (!room.isHost(playerId) && !room.isGuest(playerId)) {
            sendResponse(exchange, 403, "{\"error\":\"Player not in this room\"}");
            return;
        }
        if (room.getStatus() != Room.Status.FINISHED) {
            sendResponse(exchange, 400, "{\"error\":\"Game is not finished\"}");
            return;
        }
        room.resetForNewGame();
        sendResponse(exchange, 200, "{\"success\":true,\"room\":" + room.toJson() + "}");
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
