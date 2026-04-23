package edu.hitsz.server.store;

import edu.hitsz.server.model.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStore {
    private static final Path DATA_DIR = Paths.get("server-data");
    private static final Path DATA_FILE = DATA_DIR.resolve("accounts.txt");
    private static final PlayerStore INSTANCE = new PlayerStore();
    private final ConcurrentHashMap<String, Player> playersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Player> playersByNickname = new ConcurrentHashMap<>();

    private PlayerStore() {
        loadFromFile();
    }

    public static PlayerStore getInstance() { return INSTANCE; }

    public synchronized Player register(String nickname, String password) {
        if (playersByNickname.containsKey(nickname)) return null;
        String id = generatePlayerId();
        Player p = new Player(id, nickname, password);
        playersById.put(id, p);
        playersByNickname.put(nickname, p);
        saveToFile();
        return p;
    }

    public Player login(String nickname, String password) {
        Player p = playersByNickname.get(nickname);
        if (p != null && p.getPassword().equals(password)) return p;
        return null;
    }

    public Player getById(String id) { return playersById.get(id); }

    private void loadFromFile() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\t", -1);
                if (parts.length != 3) {
                    continue;
                }
                try {
                    String id = decode(parts[0]);
                    String nickname = decode(parts[1]);
                    String password = decode(parts[2]);
                    if (id.isBlank() || nickname.isBlank()) {
                        continue;
                    }
                    Player player = new Player(id, nickname, password);
                    playersById.put(id, player);
                    playersByNickname.put(nickname, player);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load account data", e);
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(DATA_DIR);
            StringBuilder content = new StringBuilder();
            for (Player player : playersById.values()) {
                content.append(encode(player.getId()))
                        .append('\t')
                        .append(encode(player.getNickname()))
                        .append('\t')
                        .append(encode(player.getPassword()))
                        .append(System.lineSeparator());
            }
            Path tempFile = DATA_DIR.resolve("accounts.txt.tmp");
            Files.writeString(tempFile, content.toString(), StandardCharsets.UTF_8);
            replaceDataFile(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save account data", e);
        }
    }

    private void replaceDataFile(Path tempFile) throws IOException {
        try {
            Files.move(tempFile, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String generatePlayerId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8);
        } while (playersById.containsKey(id));
        return id;
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
