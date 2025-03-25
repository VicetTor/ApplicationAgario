package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 650;



    //create a new connexion to the given server
    public static void main(String[] args) {



        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté au serveur !");
            ExecutorService pool = Executors.newCachedThreadPool();


            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Serveur: " + serverMessage);
                    }

                } catch (IOException e) {
                    System.err.println("Connexion perdue.");
                }
            }).start();

            String userCommand;
            while ((userCommand = userInput.readLine()) != null) {
                System.out.println("Client envoie : " + userCommand);
                out.println(userCommand);
                out.flush();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

