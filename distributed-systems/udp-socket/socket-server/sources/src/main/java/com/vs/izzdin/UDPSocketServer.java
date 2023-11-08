/*
 Copyright (c) 2018, Michael Bredel, H-DA
 ALL RIGHTS RESERVED.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Neither the name of the H-DA and Michael Bredel
 nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written
 permission.
 */
package com.vs.izzdin;

import com.vs.izzdin.configuration.serverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;


/**
 * The actual socket server that creates
 * a UDP socket and waits for incoming
 * datagram.
 *
 * @author Michael Bredel
 */
public class UDPSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPSocketServer.class);

    private byte[] buf; //recive
    private byte[] buffHealth;
    private byte[] buffSend;

    private boolean running = true;
    public static String boerseName= "";

    private Map <String, Long> clients = new HashMap<>();

    public UDPSocketServer() {
        // Initialize the UDP buffer.
        buf = new byte[serverMessage.getInstance().getBufferSize()];
        buffHealth = new byte[serverMessage.getInstance().getBufferSize()];
        buffSend = new byte[serverMessage.getInstance().getBufferSize()];
    }

    public void run() {
        String bankNachricht = "";
        // Create the UDP datagram socket.
        try (DatagramSocket udpSocket = new DatagramSocket(serverMessage.getInstance().getPort())) {
            LOGGER.info("Started the UDP socket server at port {} with buffer size {}.",
                    serverMessage.getInstance().getPort(),
                    buf.length);
            // Receive packets continuously.
            while (running) {


                // Create the datagram packet structure that contains the received datagram information.

                //todo:hier Healchcheck
                //buf = (CliParameters.getInstance().getMessageZwei()).getBytes();
                //DatagramPacket udpPaket2 = new DatagramPacket(buf ,buf.length, Integer.parseInt(CliParameters.getInstance().getMessage()));
                DatagramPacket udpPaket = new DatagramPacket(buffHealth, buffHealth.length);

                // Receive message
                udpSocket.setSoTimeout(4000);
                udpSocket.receive(udpPaket);


                //Print some packet data.
                printPacketData(udpPaket);

                InetAddress adresse = udpPaket.getAddress();
                int port = udpPaket.getPort();

                clientAnzahlAusgeben(adresse , port);


                udpPaket.setPort(port);
                udpPaket.setAddress(adresse);

                System.out.println("Answer to " + adresse + ": " + port);

                bankNachricht = new String(udpPaket.getData(), 0, udpPaket.getLength());
                System.out.println("Nachricht von Bank: " + bankNachricht);


                if (!bankNachricht.isEmpty()) {
                    LOGGER.info(bankNachricht + "Bank ist betriebsbereit",
                            serverMessage.getInstance().getPort(),
                            buffHealth.length);
                }


                //Send message
                /**
                 * wenn Client sich bei Server meldet, dann der Server kennt seine Informationen(Adresse,Port, PaketData, etc.. ) Nach Empfang(recive),
                 * weil die DatagramPacket-Teile beim Verschicken mit Daten gefüllt werden müssen, somit kann der Server auf diese zugreifen
                 * und anschließend mittels dieser Informationen zurück antworten.
                 */

                //für Docker Compose
                boerseName = System.getenv("CONTAINER_NAME");
                if(!boerseName.isEmpty()){
                    System.out.println("Ich bin: "+ boerseName);
                }

                String daten= serverMessage.getInstance().getMessage();
                buffSend = daten.getBytes();
                DatagramPacket udpPacketOK= new DatagramPacket(buffSend, buffSend.length);
                udpPacketOK.setAddress(udpPaket.getAddress()); //die Adresse, die udpPaketHealth von Client empfangen hat, erneurt setzen, damit da ankommt
                udpPacketOK.setPort(udpPaket.getPort()); //der Port, den udpPaketHealth von Client empfangen hat, erneurt setzen
                udpSocket.send(udpPacketOK);




            }
        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            System.out.println(bankNachricht + " nicht Betriebstbereit");
            run();
        } catch (SocketException e) {
            LOGGER.error("Could not start the UDP socket server.\n{}", e);
        } catch (IOException e) {
            LOGGER.error("Could not receive packet.\n{}", e);
        }


    }
    private void clientAnzahlAusgeben(InetAddress adresse, int port) {
        String clientskey = adresse.getHostAddress() + ":" + port;
        //füge die Cleint in Map zu, falls es nicht im Map schon vorhanden ist
        if(!clients.containsKey(clientskey)){
            clients.put(clientskey , (long) System.currentTimeMillis());
        }

        clients.put(clientskey, (long) System.currentTimeMillis()); // aktualisiere den Zeitstempel


        System.out.println(clientskey);
        System.out.println("Aktive Banks: " + clients.size());

        long actueltime = System.currentTimeMillis();

        List<String> inactiveClients = new ArrayList<>();
        for(Map.Entry<String, Long> entry : clients.entrySet()){
            String clientkey = entry.getKey();
            long lastUpdatedTime = entry.getValue();
           long diff = actueltime - lastUpdatedTime;
            if(diff > 5000){
                inactiveClients.add(clientkey);
            }
        }

        for(String clientkey : inactiveClients){
            System.out.println("Das wurde geloescht: " + clientkey);
            clients.remove(clientkey);
       }

    }

    public static void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Stop the UDP socket server.
     */
    @SuppressWarnings("unused")
    public void stop() {
        this.running = false;
    }

    /**
     * Extracts some data of a given datagram packet
     * and prints the result to standard out.
     *
     * @param udpPacket The datagram packet to extract and print.
     */
    private void printPacketData(DatagramPacket udpPacket) {
        // Get IP address and port.
        InetAddress address = udpPacket.getAddress();
        int port = udpPacket.getPort();
        // Get packet length.
        int length = udpPacket.getLength();
        // Get the payload from the buffer. Mind the buffer size and the packet length!
        byte[] playload = Arrays.copyOfRange(udpPacket.getData(), 0, length);
        // Print the packet information.
        System.out.println("Received a packet: IP:Port: " + address + ":" + port + ", length: " + length + ", payload: " + new String(playload));
    }
}