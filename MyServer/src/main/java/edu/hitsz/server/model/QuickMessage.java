package edu.hitsz.server.model;

public class QuickMessage {
    private String fromPlayerId;
    private String message;
    private long timestamp;

    public QuickMessage(String fromPlayerId, String message) {
        this.fromPlayerId = fromPlayerId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getFromPlayerId() { return fromPlayerId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    public String toJson() {
        return "{\"from\":\"" + fromPlayerId + "\",\"message\":\"" + message +
               "\",\"timestamp\":" + timestamp + "}";
    }
}
