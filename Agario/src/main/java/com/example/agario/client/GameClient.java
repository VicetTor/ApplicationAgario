package com.example.agario.client;


import com.example.agario.models.ConnectionResult;
import com.example.agario.models.Player;

import java.io.*;
import java.net.*;
import java.util.List;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;



    public static ConnectionResult playOnLine(Player player) {
        try {

            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(player);
            oos.flush();

            List<Player> existingPlayers = (List<Player>) ois.readObject();

            return new ConnectionResult(socket, oos, ois);
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