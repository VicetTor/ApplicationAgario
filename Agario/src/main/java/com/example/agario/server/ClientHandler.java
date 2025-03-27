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

            // Recevoir le joueur du client
            player = (Player) ois.readObject();
            GameServer.players.put(player.getName(), player);
            GameServer.clientOutputStreams.add(oos);

            // Envoyer l'état initial du jeu
            oos.writeObject(new ArrayList<>(GameServer.players.values()));
            oos.flush();

            // Boucle principale pour recevoir les mises à jour du client
            while (true) {
                Player updatedPlayer = (Player) ois.readObject();
                if (updatedPlayer == null) break;

                // Mettre à jour le joueur dans la liste partagée
                synchronized (GameServer.players) {
                    GameServer.players.put(updatedPlayer.getName(), updatedPlayer);
                }

                // Diffuser l'état mis à jour à tous les clients
                GameServer.broadcastGameState();
            }

        } catch (IOException | ClassNotFoundException e) {
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