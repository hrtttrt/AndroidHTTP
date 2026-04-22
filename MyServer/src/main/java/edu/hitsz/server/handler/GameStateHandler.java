package edu.hitsz.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.hitsz.server.model.GameState;
import edu.hitsz.server.model.Room;
import edu.hitsz.server.store.RoomStore;

import java.io.IOException;

import static edu.hitsz.server.handler.AccountHandler.*;

public class GameStateHandler implements HttpHandler {
    private final RoomStore roomStore = RoomStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.endsWith("/update") && "POST".equals(method)) {
                handleUpdate(exchange);
            } else if (path.endsWith("/state") && "GET".equals(method)) {
                handleGetState(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
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

        GameState state = room.getPlayerState(playerId);
        String scoreStr = extractJsonInt(body, "score");
        String hpStr = extractJsonInt(body, "hp");
        String xStr = extractJsonInt(body, "x");
        String yStr = extractJsonInt(body, "y");
        String aliveStr = extractJsonBoolean(body, "alive");

        if (scoreStr != null) state.setScore(Integer.parseInt(scoreStr));
        if (hpStr != null) state.setHp(Integer.parseInt(hpStr));
        if (xStr != null) state.setX(Float.parseFloat(xStr));
        if (yStr != null) state.setY(Float.parseFloat(yStr));
        if (aliveStr != null) state.setAlive(Boolean.parseBoolean(aliveStr));
        state.setTimestamp(System.currentTimeMillis());

        // Check if both dead -> game finished
        boolean bothDead = !room.getHostState().isAlive() && !room.getGuestState().isAlive();
        if (bothDead) {
            room.setStatus(Room.Status.FINISHED);
        }

        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private void handleGetState(HttpExchange exchange) throws IOException {
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
        GameState opponent = room.getOpponentState(playerId);
        GameState mine = room.getPlayerState(playerId);
        String status = room.getStatus().name();
        sendResponse(exchange, 200, "{\"opponent\":" + opponent.toJson() +
                ",\"mine\":" + mine.toJson() +
                ",\"roomStatus\":\"" + status + "\"}");
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
