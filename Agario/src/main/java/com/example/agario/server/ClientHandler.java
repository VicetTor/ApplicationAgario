package com.example.agario.server;

import com.example.agario.models.GameStateSnapshot;
import com.example.agario.models.PlayerInput;
import com.example.agario.models.Player;

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

            // Étape 1: Recevoir le nom du joueur
            String playerName = (String) ois.readObject();

            // Étape 2: Créer un nouveau joueur
            player = new Player(
                    Math.random() * GameServer.sharedGame.getxMax(),
                    Math.random() * GameServer.sharedGame.getyMax(),
                    playerName
            );

            // Étape 3: Ajouter le joueur au jeu
            synchronized (GameServer.sharedGame) {
                GameServer.sharedGame.addPlayer(player);
            }

            // Étape 4: Ajouter le flux de sortie
            GameServer.clientOutputStreams.add(oos);

            // Envoyer l'état initial
            oos.writeObject(new GameStateSnapshot(GameServer.sharedGame));
            oos.flush();

            // Étape 5: Boucle principale
            while (true) {
                PlayerInput input = (PlayerInput) ois.readObject();
                System.out.printf("Received input from %s: dx=%.2f dy=%.2f%n",
                        player.getName(), input.dirX, input.dirY);

                synchronized (GameServer.sharedGame) {
                    // Calculate actual movement (consider speed and mass)
                    double speed = player.getSpeed() / (1 + player.getMass()/100); // Mass slows movement
                    double moveX = input.dirX * speed;
                    double moveY = input.dirY * speed;

                    player.updatePosition(
                            moveX,
                            moveY,
                            GameServer.sharedGame.getxMax(),
                            GameServer.sharedGame.getyMax()
                    );

                    System.out.printf("Updated %s to (%.1f,%.1f)%n",
                            player.getName(), player.getPosX(), player.getPosY());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur client: " + e.getMessage());
        } finally {
            try {
                if (player != null) {
                    synchronized (GameServer.sharedGame) {
                        GameServer.sharedGame.getPlayers().removeIf(p ->
                                p.getName().equals(player.getName()));
                    }
                }
                if (oos != null) {
                    GameServer.clientOutputStreams.remove(oos);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}