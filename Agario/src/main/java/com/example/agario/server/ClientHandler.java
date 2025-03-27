package com.example.agario.server;


import com.example.agario.models.Player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;




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

            // Ajouter le flux de sortie à la liste
            GameServer.clientOutputStreams.add(oos);

            // Recevoir le joueur du client
            Player player = (Player) ois.readObject();
            synchronized (GameServer.sharedGame) {
                GameServer.sharedGame.addPlayer(player);
            }

            // Boucle principale
            while (true) {
                Player updatedPlayer = (Player) ois.readObject();
                synchronized (GameServer.sharedGame) {
                    // Mettre à jour le joueur dans le jeu partagé
                    GameServer.sharedGame.updatePlayer(updatedPlayer);
                }
            }

        } catch (IOException |
                 ClassNotFoundException e) {
            System.err.println("Déconnexion: " + e.getMessage());
        } finally {
            try {
                if (player != null) {
                    GameServer.players.remove(player.getName());
                    GameServer.clientOutputStreams.remove(oos);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}