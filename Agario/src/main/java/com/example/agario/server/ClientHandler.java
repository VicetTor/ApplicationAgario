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

                synchronized (GameServer.sharedGame) {
                    player.setDirX(input.dirX);
                    player.setDirY(input.dirY);
                    player.updatePosition(
                            input.dirX * player.getSpeed(),
                            input.dirY * player.getSpeed(),
                            GameServer.sharedGame.getxMax(),
                            GameServer.sharedGame.getyMax()
                    );
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