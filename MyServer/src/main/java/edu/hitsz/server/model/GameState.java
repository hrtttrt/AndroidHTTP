package edu.hitsz.server.model;

public class GameState {
    private int score;
    private int hp;
    private float x;
    private float y;
    private boolean alive;
    private long timestamp;

    public GameState() {
        this.alive = true;
        this.hp = 1000;
        this.timestamp = System.currentTimeMillis();
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String toJson() {
        return "{\"score\":" + score + ",\"hp\":" + hp +
               ",\"x\":" + x + ",\"y\":" + y +
               ",\"alive\":" + alive + ",\"timestamp\":" + timestamp + "}";
    }
}
