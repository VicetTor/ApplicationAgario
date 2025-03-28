package com.example.agario.client;


import com.example.agario.models.ConnectionResult;
import com.example.agario.models.Player;

import java.io.*;
import java.net.*;
import java.util.List;

public class GameClient {
    private static final String SERVER_ADDRESS = "10.42.17.83";
    private static final int SERVER_PORT = 12345;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;



    public static ConnectionResult playOnLine() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected");
            return new ConnectionResult(socket, oos, ois);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
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

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            System.out.println("je suis connecté");
          //  return new ConnectionResult(socket, oos, ois);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
         //   return null;
        }
    }
}