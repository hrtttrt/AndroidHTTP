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

        server.createContext("/api/account/", new AccountHandler());
        server.createContext("/api/room/", new RoomHandler());
        server.createContext("/api/game/", new GameStateHandler());
        server.createContext("/api/msg/", new QuickMsgHandler());

        server.start();
    }
}
