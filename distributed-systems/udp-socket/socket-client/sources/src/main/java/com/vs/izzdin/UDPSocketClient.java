
package com.vs.izzdin;

import com.vs.izzdin.configuration.clientMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

//import static org.apache.commons.lang.CharSetUtils.count;
//import static sun.net.www.http.HttpClient.New;

public class UDPSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPSocketClient.class);

    private InetAddress address;



    public ArrayList<Double> rtt_buff = new ArrayList<>();
    public ArrayList<Long> t1_buff = new ArrayList<>();
    public ArrayList<Long> t2_buff = new ArrayList<>();
    public ArrayList<String> packetInhalt_buff = new ArrayList<>();
    public ArrayList<Integer> packetLost_buff = new ArrayList<>();
    
    public static double gesamtWert = 0.0 ;

    public ArrayList<String> datenbuffer = new ArrayList<>();

    int bestaetigungCount = 0;
    int gesendetCount = 0;
    int anzahlpaketverluste = 0;
    long t1 = 0;
    long t2 = 0;
    int counter = 0;
    private DatagramSocket udpSocket;
    String[] kurzel = {"AAPL", "AMZN", "MSFT", "TSLA" }; //Apple, Amazon, Microsoft, Tesla
    int[] KurzelWerte ={375 ,310 , 300 , 280};//Apple, Amazon, Microsoft, Tesla
    public UDPSocketClient() {
        // Try to set the destination host address.
        try {
            udpSocket = new DatagramSocket();
            address = InetAddress.getByName(clientMessage.getInstance().getDestination()); //"127.19.0.2"
            //address = InetAddress.getByName("172.19.0.1");
            // clientSocket = new Socket(address,8080);

        } catch (UnknownHostException e) {
            LOGGER.error("Can not parse the destination host address.\n{}", e.getMessage());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * Demo UDP-Packet:
    * byte[] bufXBeliebig = new byte[250];
    * Bei der Paketsendung muss man neuen buffer anlegen, weil wir neuen Packet mit neuem Inhalt schicken wollen
    *
    */
    public void sendMsg(String msg) throws IOException {


        // Create the UDP datagram socket.
        try  {

            LOGGER.info("Started the UDP socket that connects to {}.", address.getHostAddress());

            // Convert the message into a byte-array.
            byte[] buf = msg.getBytes();
            // Create a new UDP packet with the byte-array as payload.
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, clientMessage.getInstance().getPort());

            // Send the data.
            t1 = System.currentTimeMillis();
            udpSocket.send(packet);


            gesendetCount++;


            byte[] bufhealth = new byte[200];
            DatagramPacket packet2 = new DatagramPacket(bufhealth, bufhealth.length);

            udpSocket.setSoTimeout(600); //auf Antwort von Server warten, falls er Stromausfall hat oder irgendwie abgesturzt wurde

            udpSocket.receive(packet2);
            t2 = System.currentTimeMillis();


            bestaetigungCount++;

            udopdateChceck();

            String BoerseNachricht = new String(packet2.getData(), 0, packet2.getLength());
            Scanner scan = new Scanner(BoerseNachricht);
            int aktienNum =  Integer.parseInt(scan.nextLine());
            String kurzell =  scan.nextLine();
            int aktuellerWert = Integer.parseInt(scan.nextLine());
            String boerseName = scan.nextLine();
            System.out.print("Nachricht von "+ boerseName + ": ");
            System.out.println(aktienNum + " Aktien von " + kurzell + " für " + aktuellerWert+ " € verkauft");

            GewinnVerlust(aktienNum, kurzell , aktuellerWert);

           


            anzahlpaketverluste = gesendetCount - bestaetigungCount;

            double umlaufzeit = t2 - t1;
            rtt_buff.add(umlaufzeit);
            t1_buff.add(t1);
            t2_buff.add(t2);
            packetInhalt_buff.add(kurzell);
            packetLost_buff.add(anzahlpaketverluste);

            System.out.println("Datenverluste: " + anzahlpaketverluste);
            System.out.println("RTT-UDP : " + umlaufzeit + " ms");
            String UDPDaten = String.format("%-10s | %-20s | %-20s | %-20s | %s",umlaufzeit +" ms", t1,t2,kurzell,anzahlpaketverluste);
            System.out.println(UDPDaten);

            infoSpeichern();
            System.out.println("----------------------");




            ClientHandlerTCP tcpClient = new ClientHandlerTCP();
            tcpClient.sendRTTData((float) umlaufzeit);
            tcpClient.sendMsgGET(String.valueOf(gesamtWert));

           /* int rand = new Random().nextInt(3) +1;
            String minus = "-";
            if (counter < 2) {
                int ablegen = rand * 1000;
                tcpClient.sendMsgPOST("Einzahlen", String.valueOf(ablegen));
                gesamatWert += ablegen;
                counter += rand;
            } else if (counter == 2) {
                int abheben = rand * 1000;
                minus = minus.concat(String.valueOf(abheben));
                tcpClient.sendMsgPOST("Auszahlen", minus);
                gesamatWert -= abheben;
                counter += rand;
           } else {
                tcpClient.sendMsgGET(String.valueOf(gesamatWert));
                counter -= rand;
            }*/

           // LOGGER.info("Message sent with payload: {}", msg);
        } catch (SocketException e) {
            LOGGER.error("Could not start the UDP socket server.\n{}", e.getLocalizedMessage());

        } catch (IOException e) {
            LOGGER.error("Could not send data.\n{}", e);
        }


    }


    private void udopdateChceck() {
        System.out.print("Die aktuelle Datenwerte: ");
        for( int i = 0; i < kurzel.length ; i++){
            System.out.print(kurzel[i] + "= " + KurzelWerte[i] + ", ");
        }
        System.out.println();
    }
  

    private void GewinnVerlust(int aktienNum, String kurzell, int aktuellerWert) {

        double ursprungWert = 0.0;
        int index = -1;
        for(int i = 0; i < kurzel.length ; i++) {
            if (kurzell.equals(kurzel[i])) {
                ursprungWert = KurzelWerte[i];
                index = i;
            }
        }
        if(index >= 0){
            KurzelWerte[index] = aktuellerWert;
        }

        double portofilioWert = aktienNum * aktuellerWert;
        double ursprungWertErgeb= aktienNum * ursprungWert;
        double diff =  portofilioWert - ursprungWertErgeb ;

        //diff = Math.round(diff * 100.0) / 100.0;

        //gesamatWert += portofilioWert;

        if(diff > 0){
            System.out.println("Das Portofolio hat Gewinn von " + diff);
            gesamtWert += diff;
        } else if(diff == 0 ){
            System.out.println("Du hast weder Gewinn noch Verlust");
        }else {
            gesamtWert += diff;
            System.out.println("Das Portofolie hat Verlust von " + diff  );
        }


    }

    private void infoSpeichern() {
        String fileName = "RTT.txt";
        String encoding = "UTF-8";
        try {
            PrintWriter writer = new PrintWriter(fileName, encoding);


           String header  = String.format("%-10s | %-20s | %-20s | %-20s | %s", "RTT",
                   "Send-Time" , "Recive-Time", "Message" , "Packet-lost");
           writer.println(header);


            for (int i = 0; i < rtt_buff.size(); i++) {
            String data = String.format("%-10s | %-20s | %-20s | %-20s | %s",
                    rtt_buff.get(i) +" ms",
                    t1_buff.get(i),
                    t2_buff.get(i),
                    packetInhalt_buff.get(i),
                    packetLost_buff.get(i));
            writer.println(data);
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


    }

    }

