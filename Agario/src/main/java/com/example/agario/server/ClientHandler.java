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

            // Recevoir le joueur initial
            player = (Player) ois.readObject();
            System.out.println("Joueur connecté: " + player.getName());

            // Enregistrer le joueur et le flux
            synchronized (GameServer.sharedGame) {
                GameServer.sharedGame.addPlayer(player);
            }
            GameServer.clientOutputStreams.add(oos);

            // Envoyer l'état initial
            oos.writeObject(GameServer.sharedGame);
            oos.flush();

            // Boucle de réception des mises à jour
            while (true) {
                Player updatedPlayer = (Player) ois.readObject();
                synchronized (GameServer.sharedGame) {
                    GameServer.sharedGame.updatePlayer(updatedPlayer);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur client: " + e.getMessage());
        } finally {
            try {
                // Nettoyage
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