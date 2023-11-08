package com.vs.izzdin;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        TCPSocketServer tcpSocketServer = null;
        try {
            tcpSocketServer = new TCPSocketServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // Run the TCP socket server.
        tcpSocketServer.run();
    }
}
