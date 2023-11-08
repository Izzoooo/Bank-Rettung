package com.vs.izzdin;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.management.remote.JMXServerErrorException;
import java.awt.*;

public class MqttPublisher {
    String brokerAdress;
    String topic;
    MqttClient client;

    public MqttPublisher(String broker, String topic){
        this.brokerAdress = broker;
        this.topic=topic;
    }
    public void init()throws MqttException {

       MqttConnectOptions mqttConnectOpts=  new MqttConnectOptions();
       mqttConnectOpts.setCleanSession(true);

       try{
           client = new MqttClient(brokerAdress, MqttClient.generateClientId());
           System.out.println("Client-ID von Publischer:"+ client.getClientId());

           //connect to the MQTT broker using the connection options
           try{
               client.connect(mqttConnectOpts);
               System.out.println("Connectes to" + brokerAdress);

           } catch (MqttException e) {
               throw new RuntimeException(e);
           }
       } catch (MqttException e) {
           System.out.println("Error: " + e.getMessage());
           client.disconnect();
       }

    }
    public void sendMessage(String messageString) throws MqttException {
        MqttMessage message = new MqttMessage(messageString.getBytes());
        message.setQos(2); // ": EXactly once

        //publisher Message
        client.publish(topic, message);

        //Exit the app explicitly
        //System.exit(0);
    }

}
