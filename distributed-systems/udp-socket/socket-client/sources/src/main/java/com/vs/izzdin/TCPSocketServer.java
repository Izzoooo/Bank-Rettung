package com.vs.izzdin;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPSocketServer {

    private static int PORT = 3142;
    private ServerSocket tcpServerSocket;
    private boolean running = true;
    private Socket connectionSocket;
    ArrayList<String> msgGet = new ArrayList<>();

    double betragAuszahlen = 0.0;
    double betragEinzahlen = 0.0;
    public  static String  bankName = "";


    public TCPSocketServer() throws IOException {
        String portDocker= System.getenv("PORT_NUMBER");
        if(!portDocker.isEmpty()) {
            PORT = Integer.parseInt(portDocker);
            tcpServerSocket = new ServerSocket(PORT);
        }
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

        // Read request line
        String requestLine = rd.readLine();
        if (requestLine == null) {
            System.out.println("Input Error");
            return;
        }
        String convertLineToSplit = requestLine.replaceAll("[^a-zA-Z0-9.-]", " ");
        System.out.println("Request information: " + convertLineToSplit);

        // Parse request line to determine request method
        String[] parts = convertLineToSplit.split("   ");
        String postOrGet = parts[0];
        System.out.println("Request: " + postOrGet);

        if (postOrGet.trim().split("\\s+")[0].equalsIgnoreCase("POST")) { //statt so :if (postOrGet.equals("POST  TCPSocketServer.java HTTP 1.1"))
            // Read and parse headers
            int contentLength = 0;
            String headerLine;
            while ((headerLine = rd.readLine()) != null && !headerLine.isEmpty()) {
                if (headerLine.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(headerLine.substring("Content-Length:".length()).trim());
                }
            }

            // Read request body
            char[] requestBody = new char[contentLength];
            int numRead = 0;
            while (numRead < contentLength) {
                int count = rd.read(requestBody, numRead, contentLength - numRead);
                if (count < 0) {
                    throw new IOException("Unexpected end of input");
                }
                numRead += count;
            }
            String request = new String(requestBody);

            // Parse request parameters
            String[] payloadParts = request.split("&");
            String betrag = payloadParts[0].split("=")[1];
            String transaktionsArt = payloadParts[1].split("=")[1];
            System.out.println("Amount: " + betrag);
            System.out.println("Transaction: " + transaktionsArt);



            if(transaktionsArt.equals("auszahlen")){
                 betragAuszahlen = Double.parseDouble(betrag);
                 UDPSocketClient.gesamtWert -= betragAuszahlen;
            }else {
                 betragEinzahlen = Double.parseDouble(betrag);
                 UDPSocketClient.gesamtWert += betragEinzahlen;
            }



            // Write response
            wr = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
            StringBuilder postBuilder = new StringBuilder();
            postBuilder.append("HTTP/1.1 200 OK\r\n");
            postBuilder.append("Content-Type: text/plain\r\n");
            postBuilder.append("\r\n");
            postBuilder.append("Received POST request with Amount=" + betrag + " and Transaction=" + transaktionsArt);
            postBuilder.append("Transaktion abgeschlossen!!!!!");
            if (!connectionSocket.isClosed() && connectionSocket.isConnected()) {
                wr.write(postBuilder.toString());
            } else {
                System.out.println("Write failed");
            }
            wr.flush();

        } else if (postOrGet.equals(" G E T") || postOrGet.equals("GET")) {
               String part22 = parts[1];
                System.out.println(part22);
                msgGet.add(part22);
            String gw= String.valueOf(UDPSocketClient.gesamtWert);
                try (OutputStream clientOutput = connectionSocket.getOutputStream()) {
                    clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                    clientOutput.write("\r\n".getBytes());
                    clientOutput.write(("" + generateKundenHtmlData() + "\r\n").getBytes());
                    clientOutput.write(("<span style=\"font-size: 30px; color: red;\">" + bankName + "</span>\r\n").getBytes());
                    clientOutput.write((": <span style=\"font-size: 30px; color: blue;\">" + gw + "</span>\r\n").getBytes());
                    clientOutput.write(("" + generateHtmlData() + "\r\n").getBytes());
                    clientOutput.write("\r\n\r\n".getBytes());
                    clientOutput.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

            private String generateKundenHtmlData () {
                String data = "";
                try {
                    //für IntelliJ
                     //System.out.println("Aktuelles Verzeichnis:"+  System.getProperty("user.dir"));
                    //File file = new File("sources\\src\\main\\java\\com\\vs\\izzdin\\index.html");


                    //für Docker Compose
                    String enviroment = System.getenv("FILE_PATH");
                    File file = new File(enviroment);
                    bankName = System.getenv("CONTAINER_NAME");
                    if(!bankName.isEmpty()){
                        System.out.println("Ich bin: "+ bankName);
                    }
                    if(file.isFile() && file.exists()) {
                        System.out.println(file.getClass().getName() + " -> " + file.getName() + " wurde gefunden");
                    }
                    Scanner scanner = new Scanner(file);
                    while (scanner.hasNextLine()) {
                        data += scanner.nextLine();
                    }
                    scanner.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return data;
            }


            private String generateHtmlData () {
                String data = "<html>"
                        + "<head>" +
                        "<title>Bankkontozustand</title> " +
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
                data += "<h2>Bankkontozustand</h2>";
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
