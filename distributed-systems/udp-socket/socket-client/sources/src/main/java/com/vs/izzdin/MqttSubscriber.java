package com.vs.izzdin;

import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber {
    String broker = "tcp://mosquitto:1883";
    String topic = "hda/vs";
    PayloadHandler payloadHandler;


    public MqttSubscriber(String broker, String topic){
        System.out.println("Broker= "+ broker + "Topic= " + topic);
        this.broker = broker;
        this.topic = topic;
    }
    public void run(){
        try{
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
            System.out.println("Ausgabe dder ID von Client Subscriber: " + client.getClientId());

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
               String message = new String(mqttMessage.getPayload());
               System.out.println("Message received: "+ message);
               payloadHandler = new PayloadHandler(message);

               Runnable simple = new Runnable() {
                   @Override
                   public void run() {
                       try {
                           payloadHandler.onReciveMessage();
                       } catch (TException e) {
                           throw new RuntimeException(e);
                       }
                   }
               };
               Thread.sleep(1000);
               new Thread(simple).start();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            //connect to the MQTT broker
            client.connect();
            System.out.println("Connected to "+ broker);

            //subsribe to a topic
            client.subscribe(topic);

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
