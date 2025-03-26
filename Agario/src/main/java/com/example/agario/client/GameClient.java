package com.example.agario.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static Socket playOnLine() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            // Créer OUTPUT en premier côté client
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            // Test de connexion
            out.writeObject("CONNECTION_TEST");
            out.flush();
            return socket;
        } catch (IOException e) {
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