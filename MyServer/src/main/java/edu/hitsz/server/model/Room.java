package edu.hitsz.server.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    public enum Status { WAITING, READY, PLAYING, FINISHED }

    private String roomId;
    private String hostId;
    private String guestId;
    private String difficulty;
    private Status status;
    private boolean hostReady;
    private boolean guestReady;
    private GameState hostState;
    private GameState guestState;
    private final List<QuickMessage> hostMessages = new CopyOnWriteArrayList<>();
    private final List<QuickMessage> guestMessages = new CopyOnWriteArrayList<>();

    public Room(String roomId, String hostId, String difficulty) {
        this.roomId = roomId;
        this.hostId = hostId;
        this.difficulty = difficulty;
        this.status = Status.WAITING;
        this.hostReady = false;
        this.guestReady = false;
        this.hostState = new GameState();
        this.guestState = new GameState();
    }

    public String getRoomId() { return roomId; }
    public String getHostId() { return hostId; }
    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }
    public String getDifficulty() { return difficulty; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isHostReady() { return hostReady; }
    public void setHostReady(boolean hostReady) { this.hostReady = hostReady; }
    public boolean isGuestReady() { return guestReady; }
    public void setGuestReady(boolean guestReady) { this.guestReady = guestReady; }
    public GameState getHostState() { return hostState; }
    public GameState getGuestState() { return guestState; }

    public boolean isHost(String playerId) { return hostId.equals(playerId); }
    public boolean isGuest(String playerId) { return playerId.equals(guestId); }
    public boolean isFull() { return guestId != null; }

    public GameState getPlayerState(String playerId) {
        return isHost(playerId) ? hostState : guestState;
    }
    public GameState getOpponentState(String playerId) {
        return isHost(playerId) ? guestState : hostState;
    }

    public void addMessageForHost(QuickMessage msg) { hostMessages.add(msg); }
    public void addMessageForGuest(QuickMessage msg) { guestMessages.add(msg); }

    public List<QuickMessage> pollMessagesForHost() {
        List<QuickMessage> msgs = new java.util.ArrayList<>(hostMessages);
        hostMessages.clear();
        return msgs;
    }
    public List<QuickMessage> pollMessagesForGuest() {
        List<QuickMessage> msgs = new java.util.ArrayList<>(guestMessages);
        guestMessages.clear();
        return msgs;
    }
    public List<QuickMessage> pollMessagesFor(String playerId) {
        return isHost(playerId) ? pollMessagesForHost() : pollMessagesForGuest();
    }
    public void addMessageFor(String targetPlayerId, QuickMessage msg) {
        if (isHost(targetPlayerId)) hostMessages.add(msg);
        else guestMessages.add(msg);
    }

    public String getOpponentId(String playerId) {
        return isHost(playerId) ? guestId : hostId;
    }

    public void resetForNewGame() {
        this.status = Status.WAITING;
        this.hostReady = false;
        this.guestReady = false;
        this.hostState = new GameState();
        this.guestState = new GameState();
        this.hostMessages.clear();
        this.guestMessages.clear();
    }

    public String toJson() {
        return "{\"roomId\":\"" + roomId + "\",\"hostId\":\"" + hostId +
               "\",\"guestId\":" + (guestId != null ? "\"" + guestId + "\"" : "null") +
               ",\"difficulty\":\"" + difficulty + "\",\"status\":\"" + status.name() +
               "\",\"hostReady\":" + hostReady + ",\"guestReady\":" + guestReady + "}";
    }
}
