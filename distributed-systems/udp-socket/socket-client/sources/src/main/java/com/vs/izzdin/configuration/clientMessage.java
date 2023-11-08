
package com.vs.izzdin.configuration;

import com.vs.izzdin.TCPSocketServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A container class that contains all the
 * CLI parameters. It is implemented using
 * the Singleton pattern (GoF) to make sure
 * we only have one object of this class.
 *
 * @author Michael Bredel
 */
public class clientMessage {
    public static final int PORT = 6543;
    public static final String MESSAGE = "Bank";
    public static final String DST_HOST = "localhost";

    /*------------------------------------------------*/
    private static clientMessage instance;
    private int port = PORT;
    private String message = MESSAGE;
    private String destination = DST_HOST;
    String[] kurzel = {"AAPL", "AMZN", "MSFT", "TSLA" }; //Apple, Tesla, Microsoft, Amazon
    public static clientMessage getInstance() {
        if (instance == null)
            instance = new clientMessage();
        return instance;
    }


    public int getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    public String getMessage() throws IOException {
        //TCPSocketServer tcpp = new TCPSocketServer(); //das wäre falsch(denn somit erzeugst du ein Instanz) -> kommt "Adress already in use", als Lösungsalternative, kann man
        // 1. "bankName" bei TCPSocketServer als static definieren und einfach hier aufrufen
        // 2. In TCPSocketServer getMethode von bankName erstellen und hier direkt aufrufen
        // 3.....etc...
        if(!TCPSocketServer.bankName.isEmpty()) {
            message = TCPSocketServer.bankName;
        }
        return this.message;
    }

/*
    public void setMessage(List<String> args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
            sb.append(" ");
        }
        this.message = sb.toString().trim();
    }*/

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    private clientMessage() {
    }
}
