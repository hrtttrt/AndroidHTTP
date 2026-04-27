package edu.hitsz.server.store;

import edu.hitsz.server.model.LeaderboardEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardStore {
    private static final Path DATA_DIR = Paths.get("server-data");
    private static final Path DATA_FILE = DATA_DIR.resolve("leaderboard.txt");
    private static final LeaderboardStore INSTANCE = new LeaderboardStore();

    private final CopyOnWriteArrayList<LeaderboardEntry> entries = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private LeaderboardStore() {
        loadFromFile();
    }

    public static LeaderboardStore getInstance() {
        return INSTANCE;
    }

    public synchronized LeaderboardEntry addScore(String playerId, String nickname, int score, String difficulty) {
        int id = nextId.getAndIncrement();
        LeaderboardEntry entry = new LeaderboardEntry(id, playerId, nickname, score, difficulty);
        entries.add(entry);
        saveToFile();
        return entry;
    }

    public List<LeaderboardEntry> getScoresByDifficulty(String difficulty) {
        List<LeaderboardEntry> result = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            if (difficulty.equals(entry.getDifficulty())) {
                result.add(entry);
            }
        }
        result.sort(Comparator.comparingInt(LeaderboardEntry::getScore).reversed()
                .thenComparingInt(LeaderboardEntry::getId));
        return result;
    }

    public synchronized boolean deleteScore(int id) {
        boolean removed = entries.removeIf(entry -> entry.getId() == id);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    private void loadFromFile() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            int maxId = 0;
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\t", -1);
                if (parts.length != 5) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(decode(parts[0]));
                    String playerId = decode(parts[1]);
                    String nickname = decode(parts[2]);
                    int score = Integer.parseInt(decode(parts[3]));
                    String difficulty = decode(parts[4]);
                    entries.add(new LeaderboardEntry(id, playerId, nickname, score, difficulty));
                    if (id > maxId) {
                        maxId = id;
                    }
                } catch (IllegalArgumentException | NumberFormatException ignored) {
                }
            }
            nextId.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load leaderboard data", e);
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(DATA_DIR);
            StringBuilder content = new StringBuilder();
            List<LeaderboardEntry> sortedEntries = new ArrayList<>(entries);
            sortedEntries.sort(Comparator.comparingInt(LeaderboardEntry::getId));
            for (LeaderboardEntry entry : sortedEntries) {
                content.append(encode(String.valueOf(entry.getId())))
                        .append('\t')
                        .append(encode(entry.getPlayerId()))
                        .append('\t')
                        .append(encode(entry.getNickname()))
                        .append('\t')
                        .append(encode(String.valueOf(entry.getScore())))
                        .append('\t')
                        .append(encode(entry.getDifficulty()))
                        .append(System.lineSeparator());
            }
            Path tempFile = DATA_DIR.resolve("leaderboard.txt.tmp");
            Files.writeString(tempFile, content.toString(), StandardCharsets.UTF_8);
            replaceDataFile(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save leaderboard data", e);
        }
    }

    private void replaceDataFile(Path tempFile) throws IOException {
        try {
            Files.move(tempFile, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
