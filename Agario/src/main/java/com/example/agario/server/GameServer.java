package com.example.agario.server;

import com.example.agario.models.*;
import com.example.agario.models.utils.Dimension;
import com.example.agario.models.utils.QuadTree;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
    static final Set<ObjectOutputStream> clientOutputStreams = Collections.newSetFromMap(new ConcurrentHashMap<>());
    static GameState sharedGame = GameState.getInstance(new QuadTree(0, new Dimension(0, 0, 10000, 10000)), "server");


    private static final ConcurrentLinkedQueue<ChatMessage> chatHistory = new ConcurrentLinkedQueue<>();
    private static final int MAX_CHAT_HISTORY = 100;

    public static void main(String[] args) {
        System.out.println("Le serveur est en marche...");
        ExecutorService pool = Executors.newCachedThreadPool();
        sharedGame.createRandomPellets(1000);

        // Thread de mise à jour du jeu
        ScheduledExecutorService gameLoop = Executors.newSingleThreadScheduledExecutor();
        gameLoop.scheduleAtFixedRate(() -> {
            try {
                updateGameState();
                broadcastGameState();
                broadcastChatMessage(new ChatMessage("de","azda"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS

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

    public static synchronized void broadcastChatMessage(ChatMessage message) {
        // Debug
        System.out.println("Envoi du message : " + message.getSender() + " -> " + message.getMessage());

        chatHistory.add(message);
        while (chatHistory.size() > MAX_CHAT_HISTORY) {
            chatHistory.poll();
        }

        List<ObjectOutputStream> toRemove = new ArrayList<>();
        synchronized (clientOutputStreams) {
            for (ObjectOutputStream oos : clientOutputStreams) {
                try {
                    oos.writeObject(message);
                    oos.reset();
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    toRemove.add(oos);
                }
            }
            clientOutputStreams.removeAll(toRemove);
        }
    }


    public static synchronized void updateGameState() {
        handleCollisions();

        // Debug
        System.out.println("Nombre de joueurs: " + sharedGame.getPlayers().size());
        sharedGame.getPlayers().forEach(p ->
                System.out.printf("%s à (%.1f, %.1f)\n", p.getName(), p.getPosX(), p.getPosY())
        );

        if (sharedGame.getQuadTree().getAllPellets().size() < 500) {
            sharedGame.createRandomPellets(100);
        }
    }

    public static synchronized void broadcastGameState() {
        List<ObjectOutputStream> toRemove = new ArrayList<>();
        GameStateSnapshot snapshot = new GameStateSnapshot(sharedGame);

        synchronized (clientOutputStreams) {
            for (ObjectOutputStream oos : clientOutputStreams) {
                try {
                    oos.writeObject(snapshot);
                    // Send chat history to new clients
                    if (snapshot.isInitialSnapshot()) {

                        System.out.println("oç-odroooo-oyoodrooootyodirooooyoroooooooooo");
                        for (ChatMessage message : chatHistory) {
                            oos.writeObject(message);
                        }
                    }
                    oos.reset();
                    oos.flush();
                } catch (IOException e) {
                    toRemove.add(oos);
                }
            }
            clientOutputStreams.removeAll(toRemove);
        }
    }

    private static void handleCollisions() {
        List<Entity> allEntities = new ArrayList<>(sharedGame.getAllEntities());
        for (int i = 0; i < allEntities.size(); i++) {
            Entity entity = allEntities.get(i);
            if (entity instanceof MovableEntity) {
                MovableEntity mover = (MovableEntity) entity;
                for (int j = 0; j < allEntities.size(); j++) {
                    if (i == j) continue;
                    Entity other = allEntities.get(j);
                    if (isColliding(mover, other) && mover.getMass() > other.getMass() * 1.33) {
                        handleCollision(mover, other);
                    }
                }
            }
        }
    }


    private static void handleCollision(MovableEntity eater, Entity eaten) {
        // Augmenter la masse du mangeur
        eater.setMass(eater.getMass() + eaten.getMass());


        // Supprimer l'entité mangée
        if (eaten instanceof Player) {
            sharedGame.getPlayers().remove(eaten);
        } else if (eaten instanceof Pellet) {
            sharedGame.getQuadTree().removeNode(eaten, sharedGame.getQuadTree());
        }
    }

    private static boolean isColliding(Entity a, Entity b) {
        double dx = a.getPosX() - b.getPosX();
        double dy = a.getPosY() - b.getPosY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (a.getRadius() + b.getRadius());
    }
}