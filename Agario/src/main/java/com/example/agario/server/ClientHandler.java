package com.example.agario.server;

import com.example.agario.client.controllers.input.PlayerInput;
import com.example.agario.models.GameState;
import com.example.agario.models.InitGameData;
import com.example.agario.models.Player;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Player player;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.player = new Player(
                Math.random() * GameServer.WORLD_SIZE.getWidth(),
                Math.random() * GameServer.WORLD_SIZE.getHeight(),
                "Player" + socket.getPort()
        );
        // Créer d'abord le flux d'entrée
        this.input = new ObjectInputStream(socket.getInputStream());

        // Puis le flux de sortie
        this.output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
    }

    @Override
    public void run() {
        try {
            // 1. Envoyer d'abord les données d'initialisation
            InitGameData initData = new InitGameData(
                    player.getId(),
                    GameServer.WORLD_SIZE.getWidth(),
                    GameServer.WORLD_SIZE.getHeight()
            );
            output.writeObject(initData);
            output.flush();

            // 2. Ensuite seulement écouter les inputs
            while (true) {
                Object received = input.readObject();
                if (received instanceof PlayerInput) {
                    PlayerInput input = (PlayerInput) received;
                }
            }
        } catch (Exception e) {
            System.out.println("Client déconnecté: " + player.getName());
        } finally {
            GameServer.removeClient(this);
            closeResources();
        }
    }

    private void sendInitialData() throws IOException {
        output.writeObject(new InitGameData(
                player.getId(),
                GameServer.WORLD_SIZE.getWidth(),
                GameServer.WORLD_SIZE.getHeight()
        ));
        output.flush();
    }

    private void updatePlayerTarget(PlayerInput input) {
        player.setSpeed(
                input.getTargetX() - player.getPosX(),
                input.getTargetY() - player.getPosY(),
                GameServer.WORLD_SIZE.getWidth(),
                GameServer.WORLD_SIZE.getHeight()
        );
    }

    public void updatePlayerPosition() {
        player.updatePosition(
                player.getSpeed() * player.getDirX(),
                player.getSpeed() * player.getDirY(),
                GameServer.WORLD_SIZE.getWidth(),
                GameServer.WORLD_SIZE.getHeight()
        );
    }

    public void sendGameState(GameState gameState) {
        try {
            output.writeObject(gameState);
            output.reset();
            output.flush();
        } catch (IOException e) {
            System.err.println("Erreur d'envoi au client: " + e.getMessage());
        }
    }

    public Player getPlayer() {
        return player;
    }

    private void closeResources() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur de fermeture des ressources: " + e.getMessage());
        }
    }
}