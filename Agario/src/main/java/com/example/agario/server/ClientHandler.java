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
                PlayerInput input = (PlayerInput) ois.readObject(); // Maintenant sécurisé
                System.out.printf("Input reçu de %s: (%.2f, %.2f)%n",
                        player.getName(), input.dirX, input.dirY);

                // Traitement du mouvement...
                synchronized (GameServer.sharedGame) {
                    double speed = player.getSpeed() / (1 + player.getMass() / 100);
                    player.updatePosition(
                            input.dirX * speed,
                            input.dirY * speed,
                            GameServer.sharedGame.getxMax(),
                            GameServer.sharedGame.getyMax()
                    );
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