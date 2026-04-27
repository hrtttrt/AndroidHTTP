package edu.hitsz.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.hitsz.server.model.LeaderboardEntry;
import edu.hitsz.server.model.Player;
import edu.hitsz.server.model.Room;
import edu.hitsz.server.store.LeaderboardStore;
import edu.hitsz.server.store.PlayerStore;
import edu.hitsz.server.store.RoomStore;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static edu.hitsz.server.handler.AccountHandler.extractJsonInt;
import static edu.hitsz.server.handler.AccountHandler.extractJsonString;
import static edu.hitsz.server.handler.AccountHandler.readBody;
import static edu.hitsz.server.handler.AccountHandler.sendResponse;

public class LeaderboardHandler implements HttpHandler {
    private final LeaderboardStore leaderboardStore = LeaderboardStore.getInstance();
    private final PlayerStore playerStore = PlayerStore.getInstance();
    private final RoomStore roomStore = RoomStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.endsWith("/list") && "GET".equals(method)) {
                handleList(exchange);
            } else if (path.endsWith("/submit") && "POST".equals(method)) {
                handleSubmit(exchange);
            } else if (path.endsWith("/delete") && "POST".equals(method)) {
                handleDelete(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String difficulty = extractQueryParam(query, "difficulty");
        if (difficulty == null || difficulty.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Missing difficulty\"}");
            return;
        }
        List<LeaderboardEntry> entries = leaderboardStore.getScoresByDifficulty(difficulty);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"entries\":[");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(entries.get(i).toJson());
        }
        sb.append("]}");
        sendResponse(exchange, 200, sb.toString());
    }

    private void handleSubmit(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String playerId = extractJsonString(body, "playerId");
        String roomId = extractJsonString(body, "roomId");
        String difficulty = extractJsonString(body, "difficulty");
        String scoreStr = extractJsonInt(body, "score");
        if (playerId == null || roomId == null || difficulty == null || scoreStr == null) {
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
        if (room.hasSubmittedLeaderboard(playerId)) {
            sendResponse(exchange, 200, "{\"success\":true,\"duplicate\":true}");
            return;
        }

        Player player = playerStore.getById(playerId);
        if (player == null) {
            sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            return;
        }

        LeaderboardEntry entry = leaderboardStore.addScore(
                playerId,
                player.getNickname(),
                Integer.parseInt(scoreStr),
                difficulty
        );
        room.markLeaderboardSubmitted(playerId);
        sendResponse(exchange, 200, "{\"success\":true,\"entry\":" + entry.toJson() + "}");
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String idStr = extractJsonInt(body, "id");
        if (idStr == null) {
            sendResponse(exchange, 400, "{\"error\":\"Missing id\"}");
            return;
        }
        boolean deleted = leaderboardStore.deleteScore(Integer.parseInt(idStr));
        if (!deleted) {
            sendResponse(exchange, 404, "{\"error\":\"Record not found\"}");
            return;
        }
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private String extractQueryParam(String query, String key) {
        if (query == null) {
            return null;
        }
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
