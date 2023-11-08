package com.vs.izzdin;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TCPSocketServer {

    private static int PORT = 3142;
    private ServerSocket tcpServerSocket;
    private boolean running = true;
    private Socket connectionSocket;
    ArrayList<String> msgGet = new ArrayList<>();

    int dbCounter = 0;


    public TCPSocketServer() throws IOException {
        tcpServerSocket = new ServerSocket(PORT);
        System.out.println("Started the TCP socket server at port " + PORT);
        System.out.println("TCP Server running...");
    }

    public void run() {
        while (running) {
            connectionSocket = null;
            try {
                // Open a connection socket, once a client connects to the server socket.
                connectionSocket = tcpServerSocket.accept();
                // Get the continuous input stream from the connection socket.
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                //Ack send füt RTT-TCP auf der andere Seite berechnen zu könenn
                OutputStream out = connectionSocket.getOutputStream();
                out.write("Ack".getBytes());
                out.flush();


                // Case distinction HTTP-POST or HTTP-GET
                handlePostOrGet(inFromClient);


            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                if (connectionSocket != null) {
                    try {
                        connectionSocket.close();
                        System.out.println("Connection socket closed");
                    } catch (IOException e) {
                        // Do nothing.s
                    }
                }
            }
        }
    }


    public void handlePostOrGet(BufferedReader rd) throws IOException, SQLException {

        BufferedWriter wr;
        String line = rd.readLine();

        if (line == null) {
            System.out.println("Input Error");
            return;
        }

        String convertedLineToSplit = line.replaceAll("[^a-zA-Z0-9]", " ");
        System.out.println("Request information: " + convertedLineToSplit);

        String[] parts = convertedLineToSplit.split("   ");
        String postOrGet = parts[0];
        System.out.println("Request: " + postOrGet);


        if (postOrGet.equals(" P O S T") || postOrGet.equals("POST")) {
            try {
                StringBuilder requestBuilder = new StringBuilder();
                String postline;
                while ((postline = rd.readLine()) != null && !postline.equals("")) {
                    requestBuilder.append(postline).append("\r\n");
                }

                String request = requestBuilder.toString();

                //zerteilen der Anfrage in Header und Payload
                int payloadStrartIndex = request.indexOf("\r\n\r\n") + 4; //Da die Payload nach dem Trennzeichen beginnt, wird zu dieser Position 4 addiert (die Länge des Trennzeichens), um den Startindex der Payload zu erhalten.
                String payload = request.substring(payloadStrartIndex);
                System.out.println("Payload:\n" + payload);


                String[] partsss = payload.split(": ")[1].split(" ");
                String first = partsss[0];
                String second = partsss[1];
                System.out.println("First: " + first);
                System.out.println("Second: " + second);

                   /*  Database db = new Database();
                    //  db.connection();
                    // db.createTabelBeliebig(); // Im Docker mit Datnebank verbinden: nach jedem "docker compose down" (Somit wird alles gelöscht-> z.B. die Relationen, etc..)
                    // soll die Tabelle auf IntelliJ also hier einmal erstellt, dann können wir auskomentieren, da Tebelle schon wxistieren wird.

                    dbCounter++;
                    if(dbCounter == 1){
                        db.connection();
                        db.loeschen();
                    }
                    db.connection();
                    db.insertDataBeliebig(first, second);

                    db.connection();
                    db.DatenAusgebenBeliebig();

*/


                wr = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

                StringBuilder postBuilder = new StringBuilder();
                postBuilder.append("HTTP/1.1 200 OK\r\n");
                postBuilder.append("Content-Type: text/plain\r\n");
                postBuilder.append("\r\n");
                if (!connectionSocket.isClosed() && connectionSocket.isConnected()) {
                    wr.write(String.valueOf(postBuilder.toString().getBytes()));
                } else {
                    System.out.println("Write failed");
                }

                wr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (rd != null) {
                    try {
                        rd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        } else if (postOrGet.equals(" G E T") || postOrGet.equals("GET")) {
            String part22 = parts[1];
            System.out.println(part22);
            msgGet.add(part22);
            try (OutputStream clientOutput = connectionSocket.getOutputStream()) {
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                clientOutput.write("\r\n".getBytes());
                clientOutput.write(("" + generateHtmlData() + "\r\n").getBytes());
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String generateHtmlData() {
        String data = "";
        try {
            File file = new File("C:\\Users\\izzdi\\VSMoore2023\\Team-Mi4-Y-E\\Praktikum1VS\\distributed-systems\\udp-socket\\tcp-server\\src\\main\\java\\com\\vs\\izzdin\\indexPost.html");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                data += scanner.nextLine();
            }
            scanner.close();

            data = data.replace("{TABLE_CONTENT}", generateTable()); // Hier wird die Tabelle eingefügt

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private String generateTable() {
        String table = "<h2>Praktikum 2</h2>";
        table += "<table>";
        table += "<tr>";
        table += "<th>Data:</th>";
        table += "</tr>";
        for (int i = 0; i < msgGet.size(); i++) {
            table += "<tr><td>" + msgGet.get(i) + "</td></tr>";
        }
        table += "</table>";
        return table;
    }


    private String generateHtmlData2() {
        String data = "<html>"
                + "<head>" +
                "<title>Praktikum 2</title> " +
                "<style> " +
                "table {" +
                "font-family: arial, sans-serif;" +
                "border-collapse: collapse;" +
                "width: 100%;" +
                "}" +
                "td, th {" +
                "border: 1px solid #dddddd;" +
                "text-align: left;" +
                "padding: 8px;" +
                "}" +
                "tr:nth-child(even) {" +
                "background-color: #dddddd;" +
                "}" +
                "h2 {" +
                "text-align: center;" +
                "}" +
                "</style>" +
                "</head>"
                + "<body>";
        data += "<h2>Praktikum 2</h2>";
        data += "<table>";
        data += "<tr>";
        data += "<th>Data:</th>";
        data += "</tr>";
        for (int i = 0; i < msgGet.size(); i++) {
            data += "<td> " + msgGet.get(i) + '\n' + " </td>";
        }
        data += "</table>";
        data += "</body>" + "</html>";
        return data;
    }
}