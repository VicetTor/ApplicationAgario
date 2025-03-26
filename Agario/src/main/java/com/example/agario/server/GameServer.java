package com.example.agario.server;

import com.example.agario.models.*;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 8080;
    private static final int TICK_RATE = 30;
    static final Dimension WORLD_SIZE = new Dimension(0, 0, 10000, 10000);
    private static final int MAX_PELLETS = 1000;
    private static final int MIN_PELLETS = 500;

    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final QuadTree quadTree = new QuadTree(0, WORLD_SIZE);
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("Serveur Agar.io démarré sur le port " + PORT);

        // Initialisation du QuadTree avec une profondeur maximale
        quadTree.setMAX_DEPTH(6);

        // Génération des pellets initiaux
        generateInitialPellets(MAX_PELLETS);

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Boucle de jeu principale
            ScheduledExecutorService gameLoop = Executors.newSingleThreadScheduledExecutor();
            gameLoop.scheduleAtFixedRate(GameServer::gameTick, 0, 1000/TICK_RATE, TimeUnit.MILLISECONDS);

            // Acceptation des connexions clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur: " + e.getMessage());
        }
    }

    private static void generateInitialPellets(int count) {
        for (int i = 0; i < count; i++) {
            Pellet pellet = new Pellet(
                    random.nextDouble() * WORLD_SIZE.getWidth(),
                    random.nextDouble() * WORLD_SIZE.getHeight()
            );
            quadTree.insertNode(pellet);
        }
    }

    private static void gameTick() {
        synchronized (clients) {
            // 1. Mise à jour des positions
            updatePlayersPositions();

            // 2. Gestion des collisions
            handleCollisions();

            // 3. Respawn des pellets si nécessaire
            maintainPellets();

            // 4. Envoi de l'état du jeu
            broadcastGameState();
        }
    }

    private static void updatePlayersPositions() {
        for (ClientHandler client : clients) {
            client.updatePlayerPosition();
        }
    }

    private static void handleCollisions() {
        List<Entity> nearbyEntities = new ArrayList<>();

        for (ClientHandler client : clients) {
            Player player = client.getPlayer();
            nearbyEntities.clear();

            // Utilisation du QuadTree pour trouver les entités proches
            Dimension searchArea = new Dimension(
                    player.getPosX() - player.getRadius() * 2,
                    player.getPosY() - player.getRadius() * 2,
                    player.getPosX() + player.getRadius() * 2,
                    player.getPosY() + player.getRadius() * 2
            );

            quadTree.DFSChunk(quadTree, searchArea, nearbyEntities);

            // Vérification des collisions
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Pellet && checkCollision(player, entity)) {
                    handlePelletCollision(player, (Pellet) entity);
                }
            }
        }
    }

    private static boolean checkCollision(Entity e1, Entity e2) {
        double dx = e1.getPosX() - e2.getPosX();
        double dy = e1.getPosY() - e2.getPosY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (e1.getRadius() + e2.getRadius());
    }

    private static void handlePelletCollision(Player player, Pellet pellet) {
        player.setMass(player.getMass() + pellet.getMass());
        quadTree.removeNode(pellet, quadTree);
    }

    private static void maintainPellets() {
        // Comptage des pellets actuels
        List<Entity> allPellets = new ArrayList<>();
        quadTree.DFSChunk(quadTree, WORLD_SIZE, allPellets);

        // Regénération si nécessaire
        if (allPellets.size() < MIN_PELLETS) {
            generateInitialPellets(MAX_PELLETS - allPellets.size());
        }
    }

    private static void broadcastGameState() {
        GameState gameState = new GameState();

        // Récupération de tous les joueurs
        List<Player> players = new ArrayList<>();
        for (ClientHandler client : clients) {
            players.add(client.getPlayer());
        }
        gameState.setPlayers(players);

        // Récupération de tous les pellets
        List<Entity> pellets = new ArrayList<>();
        quadTree.DFSChunk(quadTree, WORLD_SIZE, pellets);
        gameState.setPellets(pellets);

        // Envoi à tous les clients
        for (ClientHandler client : clients) {
            client.sendGameState(gameState);
        }
    }

    static void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
            quadTree.removeNode(client.getPlayer(), quadTree);
        }
    }
}