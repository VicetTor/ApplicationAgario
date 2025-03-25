package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer  {
    private static final int PORT = 650;
    static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());



    //create a new server with the indicate PORT and wait for a connexion
    public static void main(String[] args) {
        System.out.println("Le serveur est en marche...");
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            new Thread(GameServer::updateGameState).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion acceptée");
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Method to periodically (for 30fps) update the game state and send messages to clients
    //Put the game logic here
    private static void updateGameState() {
        while (true) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println("yoo");
                }
            }
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
