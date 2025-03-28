package com.example.agario.models;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Object for the connection with interaction server-client
 */
public class ConnectionResult {
    public final Socket socket;
    public final ObjectOutputStream oos;
    public final ObjectInputStream ois;

    /**
     * Constructor
     *
     * @param socket socket of communication
     * @param oos OutputStream
     * @param ois InputStream
     */
    public ConnectionResult(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.socket = socket;
        this.oos = oos;
        this.ois = ois;
    }
}
