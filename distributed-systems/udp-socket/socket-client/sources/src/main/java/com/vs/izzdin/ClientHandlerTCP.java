package com.vs.izzdin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;



public class ClientHandlerTCP {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandlerTCP.class);
    /**
     * The UDP port the client connects to.
     */
    private static int PORTT = 3142;

    private long startTime;
    private long endtime;
    public ArrayList<Float> rtt_Tcp = new ArrayList<>();
    public ArrayList<Long> t1_buffTCP = new ArrayList<>();
    public ArrayList<Long> t2_buffTCP = new ArrayList<>();
    public ArrayList<Float> diff_tcp_udp = new ArrayList<>();


    float time_udp = 0.0F;


    /**
     * The TCP client socket used to send data.
     */
    private Socket clientSocket;
    /**
     * The IP address the client connects to.
     */
    private InetAddress address;

    /**
     * Default constructor that creates, i.e., opens
     * the socket.
     *
     * @throws IOException In case the socket cannot be created.
     */

    public ClientHandlerTCP() throws IOException {
        address = InetAddress.getByName("localhost"); // statt localhost tcp-server for docker-compose (falls tcp-server sich docker-cpmpose befindet)
        clientSocket = new Socket(address, PORTT);
        LOGGER.info("Started the TCP socket that connects to " + address.getHostAddress());
    }

    /**
     * Method that transmits a String message
     * via the UDP socket.
     */
    public void sendRTTData(float rttUDP){
        try {
            time_udp = rttUDP;
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
    }
    public void sendMsgGET(String ergebnis) {
        // Send the data.

        try {
            startTime = System.currentTimeMillis();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeChars("GET " + ergebnis + "\n");
            LOGGER.debug("Message sent with payload: " + ergebnis);
            outToServer.flush();

            // Ack empfangen bestaetigen
            AckEmpfang();

              //auf Ack warten
            AckAbchecken();
            if (!AckEmpfang()) {
                AckAbchecken();
            }

            endtime = System.currentTimeMillis();

            float timee = endtime - startTime;
            float Differenz_TCP_UDP = timee - time_udp;
            rtt_Tcp.add(timee);
            t1_buffTCP.add(startTime);
            t2_buffTCP.add(endtime);
            diff_tcp_udp.add(Differenz_TCP_UDP);

            infoSpeichernTCP();

            String TCPDaten = String.format("%-10s | %-20s | %-20s | %s",timee +" ms",startTime,endtime,Differenz_TCP_UDP);
            System.out.println(TCPDaten);

            System.out.println("RTT-TCP: " + timee);
            System.out.println( Differenz_TCP_UDP + " ms -> SO viel ist UDP schneller als TCP");



        } catch (IOException e) {
            LOGGER.error("Could not send data.\n" + e.getLocalizedMessage());
        }
    }

    public void sendMsgPOST(String Befehl , String wert) {

        try {
            String payload = Befehl + " " + wert;
            String host = clientSocket.getInetAddress().getHostName();
            String contentType = "text/plain";
            int contentLength = payload.getBytes().length;

            // Send HTTP POST request
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes("POST / HTTP/1.1\r\n");
            outToServer.writeBytes("payload: " + payload + "\r\n");
            outToServer.writeBytes("Host: " + host + "\r\n");
            outToServer.writeBytes("Content-Type: " + contentType + "\r\n");
            outToServer.writeBytes("Content-Length: " + contentLength + "\r\n");
            outToServer.writeBytes("\r\n");
            outToServer.flush();

            LOGGER.debug("Message sent with payload: " + payload);

        } catch (IOException e) {
            LOGGER.error("Could not send data.\n" + e.getLocalizedMessage());
        }
    }

    private void AckAbchecken() throws IOException {
        if (!AckEmpfang()) {
            wait(500);
            AckEmpfang();
        }
    }

    private boolean AckEmpfang() throws IOException {
        InputStream in = clientSocket.getInputStream();
        //byte[] ackData = new byte[4];
        //int bytesRead = in.read(ackData);
        //String ackMessage = new String(ackData, 1, bytesRead);
        String ackMessage = String.valueOf(in.read());
        if (ackMessage.equals("Ack")) {
            return true;
        }
        return false;
    }


    public void infoSpeichernTCP() {
        //todo: RTT Ergebnisse in einer Datei speichern
        String fileName = "RTT-TCP.txt";
        String encoding = "UTF-8";
        try {
            PrintWriter writer = new PrintWriter(fileName, encoding);


            String header  = String.format("%-10s | %-20s | %-20s | %s", "RTT",
                    "Send-Time" , "Recive-Time" , "UDP ist so viel schneller");
            writer.println(header);


            for (int i = 0; i < rtt_Tcp.size(); i++) {
                String data = String.format("%-10s | %-20s | %-20s | %s",
                        rtt_Tcp.get(i) +" ms",
                        t1_buffTCP.get(i),
                        t2_buffTCP.get(i),
                        diff_tcp_udp.get(0));
                writer.println(data);
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


    }

    public static void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
        }

    }

}