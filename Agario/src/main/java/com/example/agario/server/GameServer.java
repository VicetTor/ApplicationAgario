package com.example.agario.server;


import com.example.agario.models.Player;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 8080; // Changer le port pour correspondre au client
    static final Map<String, Player> players = new ConcurrentHashMap<>();
    static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    static QuadTree quadTree = new QuadTree(0, new Dimension(0, 0, 10000, 10000));

    public static void main(String[] args) {
        System.out.println("Le serveur est en marche...");
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            new Thread(GameServer::updateGameState).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion acceptée");
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateGameState() {
        while (true) {
            // Mettre à jour la position de tous les joueurs et gérer les collisions
            synchronized (players) {
                // Vérifier les collisions entre joueurs
                List<Player> playerList = new ArrayList<>(players.values());
                for (int i = 0; i < playerList.size(); i++) {
                    System.out.println(playerList.get(i).getPosX());
                    for (int j = i + 1; j < playerList.size(); j++) {
                        Player p1 = playerList.get(i);
                        Player p2 = playerList.get(j);

                        double dx = p1.getPosX() - p2.getPosX();
                        double dy = p1.getPosY() - p2.getPosY();
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        if (distance < p1.getRadius() + p2.getRadius()) {
                            // Gérer la collision (un joueur mange l'autre)
                            if (p1.getMass() > p2.getMass() * 1.2) {
                                p1.setMass(p1.getMass() + p2.getMass());
                                players.remove(p2.getName());
                            } else if (p2.getMass() > p1.getMass() * 1.2) {
                                p2.setMass(p2.getMass() + p1.getMass());
                                players.remove(p1.getName());
                            }
                        }
                    }
                }
            }

            // Envoyer l'état du jeu à tous les clients
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    try {
                        writer.println(players.values());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(33); // ~30 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
