package com.example.agario.client;


import com.example.agario.models.Player;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;

    public static Socket playOnLine(Player player) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Envoyer le joueur au serveur
            oos.writeObject(player);
            oos.flush();

            // Recevoir la liste des joueurs existants
            List<Player> existingPlayers = (List<Player>) ois.readObject();

            return socket;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            return null;
        }
    }



    public static void closeConnection(Socket socket) {

        if (socket != null) {

            try {

                socket.close();

                System.out.println("Connexion fermée");

            } catch (IOException e) {

                System.err.println("Erreur lors de la fermeture du socket: " + e.getMessage());

            }

        }

    }

}