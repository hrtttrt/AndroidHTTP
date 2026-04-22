package edu.hitsz.server.store;

import edu.hitsz.server.model.Room;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

public class RoomStore {
    private static final RoomStore INSTANCE = new RoomStore();
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private RoomStore() {}
    public static RoomStore getInstance() { return INSTANCE; }

    public Room createRoom(String hostId, String difficulty) {
        String roomId = String.format("%06d", random.nextInt(1000000));
        while (rooms.containsKey(roomId)) {
            roomId = String.format("%06d", random.nextInt(1000000));
        }
        Room room = new Room(roomId, hostId, difficulty);
        rooms.put(roomId, room);
        return room;
    }

    public Room getRoom(String roomId) { return rooms.get(roomId); }

    public void removeRoom(String roomId) { rooms.remove(roomId); }
}
