package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static Server.GameServer.clientWriters;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Method to handle client communication ( retieve client command )
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);


            synchronized (clientWriters) {
                clientWriters.add(out);
            }

            out.println("WELCOME");

            try{
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Message reçu du client : " + message);
                    out.flush();
                }
            }catch (IOException e){

            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clientWriters) {
                clientWriters.remove(out);
                // Check if there are no more clients connected
                if (clientWriters.isEmpty()) {
                    System.out.println("No clients are connected.");
                }
            }
        }
    }
}
