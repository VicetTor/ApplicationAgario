package com.example.agario.server;

import com.example.agario.models.ChatMessage;
import com.example.agario.models.GameStateSnapshot;
import com.example.agario.models.PlayerInput;
import com.example.agario.models.Player;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Player player;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // 1. Lire d'abord le nom du joueur (String)
            String playerName = (String) ois.readObject();
            System.out.println("Nouveau joueur connecté : " + playerName);

            // 2. Créer le joueur et l'ajouter au jeu
            player = new Player(
                    Math.random() * GameServer.sharedGame.getxMax(),
                    Math.random() * GameServer.sharedGame.getyMax(),
                    playerName
            );

            synchronized (GameServer.sharedGame) {
                GameServer.sharedGame.addPlayer(player);
            }


            // 3. Envoyer l'état initial (GameStateSnapshot)
            oos.writeObject(new GameStateSnapshot(GameServer.sharedGame));
            oos.flush();

            // 4. Boucle principale : Lire uniquement PlayerInput
            while (true) {
                Object input = ois.readObject();
                System.out.println("Objet reçu de type: " +
                        (input != null ? input.getClass() : "null")); // Debug

                if (input instanceof PlayerInput) {
                    PlayerInput playerInput = (PlayerInput) input;
                    // Handle player movement as before
                    synchronized (GameServer.sharedGame) {
                        double speed = playerInput.speed;


                        double moveX = playerInput.dirX;
                        double moveY = playerInput.dirY;

                        player.setSpeedy(speed);
                        player.updatePosition(
                                moveX,
                                moveY,
                                GameServer.sharedGame.getxMax(),
                                GameServer.sharedGame.getyMax()
                        );



                        oos.writeObject(new GameStateSnapshot(GameServer.sharedGame));
                        oos.reset();
                        oos.flush();
                    }
                }
                else if (input instanceof ChatMessage) {
                    // Diffuser le message à tous les clients
                    ChatMessage chatMessage = (ChatMessage) input;
                    System.out.println("Message chat reçu de " + chatMessage.getSender() + ": " + chatMessage.getMessage());
                    GameServer.broadcastChatMessage(chatMessage);
                }

            }
        } catch (Exception e) {
            System.err.println("Erreur client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage
            try {
                if (player != null) {
                    synchronized (GameServer.sharedGame) {
                        GameServer.sharedGame.getPlayers().removeIf(p ->
                                p.getName().equals(player.getName()));
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}