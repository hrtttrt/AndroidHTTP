package edu.hitsz.server.store;

import edu.hitsz.server.model.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class PlayerStore {
    private static final PlayerStore INSTANCE = new PlayerStore();
    private final ConcurrentHashMap<String, Player> playersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Player> playersByNickname = new ConcurrentHashMap<>();

    private PlayerStore() {}
    public static PlayerStore getInstance() { return INSTANCE; }

    public Player register(String nickname, String password) {
        if (playersByNickname.containsKey(nickname)) return null;
        String id = UUID.randomUUID().toString().substring(0, 8);
        Player p = new Player(id, nickname, password);
        playersById.put(id, p);
        playersByNickname.put(nickname, p);
        return p;
    }

    public Player login(String nickname, String password) {
        Player p = playersByNickname.get(nickname);
        if (p != null && p.getPassword().equals(password)) return p;
        return null;
    }

    public Player getById(String id) { return playersById.get(id); }
}
