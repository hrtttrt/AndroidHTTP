package edu.hitsz.server.model;

public class LeaderboardEntry {
    private final int id;
    private final String playerId;
    private final String nickname;
    private final int score;
    private final String difficulty;

    public LeaderboardEntry(int id, String playerId, String nickname, int score, String difficulty) {
        this.id = id;
        this.playerId = playerId;
        this.nickname = nickname;
        this.score = score;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String toJson() {
        return "{\"id\":" + id +
                ",\"playerId\":\"" + escape(playerId) + "\"" +
                ",\"nickname\":\"" + escape(nickname) + "\"" +
                ",\"score\":" + score +
                ",\"difficulty\":\"" + escape(difficulty) + "\"}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
