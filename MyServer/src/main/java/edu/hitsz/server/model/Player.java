package edu.hitsz.server.model;

public class Player {
    private String id;
    private String nickname;
    private String password;

    public Player(String id, String nickname, String password) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
    }

    public String getId() { return id; }
    public String getNickname() { return nickname; }
    public String getPassword() { return password; }

    public String toJson() {
        return "{\"id\":\"" + id + "\",\"nickname\":\"" + nickname + "\"}";
    }
}
