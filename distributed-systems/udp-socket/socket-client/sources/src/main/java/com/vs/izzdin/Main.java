
package com.vs.izzdin;

import com.vs.izzdin.configuration.clientMessage;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;


/**
 * The main class that contains the
 * main method that starts the client.
 *
 * @author Michael Bredel
 */
public class Main {

    /**
     * The logger.
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Sets the command-line options with values in environment variables.
     * This can be used to ease the configuration of the server running
     * in Docker compose.
     */
    private static void parseOptionsFromEnv() {
        try {
            clientMessage.getInstance().setDestination(System.getenv("DESTINATION"));
        } catch (NullPointerException e) {
            LOGGER.debug("Environment variable \"DESTINATION\" does not exist");
        }
    }


    //todo:RPC -> Client
    private static volatile TTransport transport;
    private static volatile BankRettungsService.Client client;


    //todo:RPC -> Server
    public static BankRettungsService.Processor processor;
    public static RpcController handler;

    public static Double rpcRuckgabWert;



    public static void main(String[] args) throws IOException, TException, InterruptedException {
        // Parse environemnt variables.
        parseOptionsFromEnv();

        System.out.println("\u001B[33mUDP\u001B[0m");
       Thread udpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                UDPSocketClient udpSocketClient = new UDPSocketClient();
                for (int counter = 0; counter < 5; ++counter) {
                    try {
                        udpSocketClient.sendMsg(clientMessage.getInstance().getMessage());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(1000); // Wait for 1 second before sending the next message.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        udpThread.start(); // Start the UDP client thread.

        System.out.println("\u001B[33mTCP\u001B[0m");
        // Create a new thread for the TCP server socket transaction.
        Thread tcpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TCPSocketServer tcpSocketServer = null;
                try {
                    tcpSocketServer = new TCPSocketServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                tcpSocketServer.run();
            }
        });
        tcpThread.start(); // Start the TCP server thread.

        System.out.println("-----------------------------------------------------------------------------------------------------");
        System.out.println("\u001B[33mRPC\u001B[0m");

        Thread rpcServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startRpcServer();
            }
        });

        rpcServerThread.start();

        Thread.sleep(1000);

        // Create a new thread for the RPC client.
        Thread rpcClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        long timeRpcStart = 0;
                        long timeRpcEnd = 0;
                        timeRpcStart = System.currentTimeMillis();
                        connectToRpcServer();
                        datenHandlung();
                        timeRpcEnd = System.currentTimeMillis();
                        long rttRpc = timeRpcEnd - timeRpcStart;
                        System.out.print("RPC=> ");
                        rttAusgabe(rttRpc, timeRpcStart, timeRpcEnd, rpcRuckgabWert);
                        ueberweisungsliste();
                    } catch (TTransportException e) {
                        throw new RuntimeException(e);
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }

                }
        });

            rpcClientThread.start();

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("\u001B[33mMQTT\u001B[0m");



        Thread mqttStepOne = new Thread(new Runnable() {
            @Override
            public void run() {
                long timeMQTTstart = 0;
                long  timeMQTTend = 0;
                double geldGebrauch = 0.0;
                timeMQTTstart= System.currentTimeMillis();
                 if(UDPSocketClient.gesamtWert < 0) {
                    MqttPublisher mqttPublisherOne = new MqttPublisher("tcp://mosquitto:1883", "Request");
                
                    geldGebrauch = UDPSocketClient.gesamtWert/3; //insgesamt haben wir vier Banken, wenn eine Bank broke ist, dann die andere drei helfen diese eine faule mental verschobene Ratte die sogenannte Broke-Bank 
                    String messageOne = String.valueOf(geldGebrauch * -1.0);
                    try {
                        mqttPublisherOne.init();
                        mqttPublisherOne.sendMessage(messageOne);

                    } catch (MqttException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
                    MqttSubscriber mqttSubscriberOne = new MqttSubscriber("tcp://mosquitto:1883", "Request");
                    mqttSubscriberOne.run();
                timeMQTTend = System.currentTimeMillis();
                long rttMqtt = timeMQTTend - timeMQTTstart;
                System.out.print("MQTT=> ");
                rttAusgabe(rttMqtt , timeMQTTstart, timeMQTTend, geldGebrauch);
            }

           // }
        }) ;
        

            mqttStepOne.start();



        Thread.sleep(4000);//sonst "PayloadHandler.ruckAntwort" wird nicht aktualisiert, daher muss darauf gewartet werden.

        Thread  mqttStepTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                MqttPublisher mqttPublisherTwo = new MqttPublisher("tcp://mosquitto:1883", "Ack");



                String messageTwo = "";



                if (!PayloadHandler.ruckAntwort.isEmpty()) {
                    /*

                    String path = System.getenv("FILE_PATH2");
                    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if(line.contains("Abort")) {
                                messageTwo = "Abort";
                            }else{
                                messageTwo = "Commit";
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
*/

                    //messageTwo = PayloadHandler.ruckAntwort;
                   // System.out.println("Commit-Anzahl => "+ PayloadHandler.zustimmungCommit );
                    //System.out.println("Abort-Anzahl => "+ PayloadHandler.zustimmungAbort );
                    if(PayloadHandler.zustimmungCommit == 3){
                        messageTwo = "Commit";
                    }else if(PayloadHandler.zustimmungAbort >= 1){
                       messageTwo = "Abort";
                    }
                }else{
                    messageTwo = "Noch kein Ack";
                }
                try {
                    mqttPublisherTwo.init();
                    mqttPublisherTwo.sendMessage(messageTwo);
                } catch (MqttException e) {
                    throw new RuntimeException(e.getMessage());
                }
                

                MqttSubscriber mqttSubsriberTwo = new MqttSubscriber("tcp://mosquitto:1883", "Ack");
                mqttSubsriberTwo.run();


            }

        });

            mqttStepTwo.start();




    }

    private static void rttAusgabe(long rtt, long timeRpcStart, long timeRpcEnd, Double rpcRuckgabWert) {
        String data = String.format("%-10s | %-20s | %-20s | %-20s" ,
                rtt +" ms",
                timeRpcStart,
                timeRpcEnd,
                rpcRuckgabWert+ " €");
        System.out.println(data);
    }

    private static void ueberweisungsliste() {
        System.out.println("Die Überweisungsliste:");
        for(geldBetrag g : RpcController.ueberweisungen){
            System.out.println(g);
        }
    }

    private static void datenHandlung() throws TException {
        geldBetrag g1 = new geldBetrag();
        g1.setGeld(500.0);
        //g1.setVerwendungszweck(TCPSocketServer.bankName); //für docker Compose
        g1.setVerwendungszweck("AarealBank");
        g1.setTransaktionsnummer(1);

        double randomValue = 100.0 + (1500.0 - 100.0) * new Random().nextDouble();
        double randomV = Math.round(randomValue * 100.0) /100.0;
        geldBetrag g2 = new geldBetrag();
        g2.setGeld(randomV);
        //g2.setVerwendungszweck(TCPSocketServer.bankName);//für docker Compose
        g2.setVerwendungszweck("Sparkasse");
        g2.setTransaktionsnummer(2);

         double ueberweisen1  = client.ueberweisen(g1);
         double ueberweisen2 = client.ueberweisen(g2);
        System.out.println("Ich hab "+ ueberweisen1 + " € ueberwiesen");
        System.out.println("Ich hab "+ ueberweisen2 + " € ueberwiesen");

        rpcRuckgabWert = ueberweisen2;
        client.stornieren(g1); //Probe

        if(UDPSocketClient.gesamtWert < 0){
            double ausleihenBetrag = UDPSocketClient.gesamtWert;
            geldBetrag g3 = new geldBetrag();
            g2.setGeld(ausleihenBetrag * -1.0);
            g2.setVerwendungszweck(TCPSocketServer.bankName);//für docker Compose
            //g2.setVerwendungszweck("Sparkasse");
            g2.setTransaktionsnummer(3);

           System.out.println("Ich will "+  g3.getGeld()  +" ausleihen");
            System.out.println("Ich will "+ client.ausleihen(g3) + " ausleihen");
        }
/*
        double ueberweisungsbetrag = client.ueberweisen(g1);
        if(ueberweisungsbetrag != 0 && UDPSocketClient.gesamtWert <0 ){ //ueberweisungsbetrag != 0 =>das bedeutet es ist mindestens einmal aufgerufen
            client.stornieren(g1);
        }

*/
    }



    private static void connectToRpcServer() throws TTransportException {
        transport = new TSocket("localhost", 9090);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        client = new BankRettungsService.Client(protocol);
        System.out.println("starting RPC Client");
    }


    private static void startRpcServer() {
        try {
            handler = new RpcController();
            processor = new BankRettungsService.Processor(handler);

            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));


            System.out.print("Starting the Simple Server...");
                server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}










