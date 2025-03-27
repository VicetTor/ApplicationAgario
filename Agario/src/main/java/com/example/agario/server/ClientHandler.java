package com.example.agario.server;


import com.example.agario.models.Player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import static com.example.agario.server.GameServer.clientWriters;


public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());

            // Recevoir le joueur du client
            Player player = (Player) ois.readObject();
            GameServer.players.put(player.getName(), player);

            // Envoyer la liste des joueurs existants
            oos.writeObject(new ArrayList<>(GameServer.players.values()));
            oos.flush();

            synchronized (GameServer.clientWriters) {
                GameServer.clientWriters.add(new PrintWriter(socket.getOutputStream(), true));
                if (clientWriters.isEmpty()) {
                    System.out.println("No clients are connected.");
                }
            }

            // Boucle principale pour recevoir les mises à jour du client
            while (true) {
                Player updatedPlayer = (Player) ois.readObject();
                if (updatedPlayer == null) {
                    break; // Déconnexion
                }
                GameServer.players.put(updatedPlayer.getName(), updatedPlayer);
            }

        } catch (IOException | ClassNotFoundException e) {
            // Gérer la déconnexion
            String playerName = GameServer.players.entrySet().stream()
                    .map(Map.Entry::getKey)
                    .findFirst().orElse("");

            GameServer.players.remove(playerName);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
