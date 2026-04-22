package edu.hitsz.server;

import com.sun.net.httpserver.HttpServer;
import edu.hitsz.server.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Account API
        server.createContext("/api/account/", new AccountHandler());

        // Room API
        server.createContext("/api/room/", new RoomHandler());

        // Game state API
        server.createContext("/api/game/", new GameStateHandler());

        // Quick message API (shares /api/game/ prefix but separate handler)
        // We need to handle routing within GameStateHandler or use more specific paths
        // Let's use a combined approach with a dispatcher

        server.createContext("/api/msg/", new QuickMsgHandler());

        server.start();
        System.out.println("========================================");
        System.out.println("  AircraftWar Server started on port " + PORT);
        System.out.println("  http://localhost:" + PORT);
        System.out.println("========================================");
        System.out.println("API endpoints:");
        System.out.println("  POST /api/account/register");
        System.out.println("  POST /api/account/login");
        System.out.println("  POST /api/room/create");
        System.out.println("  POST /api/room/join");
        System.out.println("  GET  /api/room/info?roomId=xxx");
        System.out.println("  POST /api/room/ready");
        System.out.println("  POST /api/room/start");
        System.out.println("  POST /api/game/update");
        System.out.println("  GET  /api/game/state?playerId=x&roomId=x");
        System.out.println("  POST /api/msg/message");
        System.out.println("  GET  /api/msg/messages?playerId=x&roomId=x");
        System.out.println("========================================");
    }
}
