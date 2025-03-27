package com.example.agario.server;

import com.example.agario.models.Game;
import com.example.agario.models.GameState;
import com.example.agario.models.Player;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;
import com.example.agario.server.ClientHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8080;
    static final Map<String, Player> players = new ConcurrentHashMap<>();
    static final Set<ObjectOutputStream> clientOutputStreams = Collections.synchronizedSet(new HashSet<>());
    static GameState sharedGame = GameState.getInstance(new QuadTree(0, new Dimension(0, 0, 10000, 10000)), "server");

    public static void main(String[] args) {
        System.out.println("Le serveur est en marche...");
        ExecutorService pool = Executors.newCachedThreadPool();
        sharedGame.createRandomPellets(1000);

        // Thread de mise à jour du jeu
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50); // ~20 updates par seconde
                    broadcastGameState();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion acceptée");
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcastGameState() {
        synchronized (sharedGame) {
            List<ObjectOutputStream> toRemove = new ArrayList<>();

            synchronized (clientOutputStreams) {
                for (ObjectOutputStream oos : clientOutputStreams) {
                    try {
                        oos.writeObject(sharedGame);
                        oos.reset();
                        oos.flush();
                    } catch (IOException e) {
                        System.err.println("Erreur d'envoi, déconnexion client");
                        toRemove.add(oos);
                    }
                }

                // Nettoyer les flux déconnectés
                clientOutputStreams.removeAll(toRemove);
            }
        }
    }
}