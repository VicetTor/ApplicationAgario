package com.example.agario.models;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionResult {
    public final Socket socket;
    public final ObjectOutputStream oos;
    public final ObjectInputStream ois;

    public ConnectionResult(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.socket = socket;
        this.oos = oos;
        this.ois = ois;
    }
}
