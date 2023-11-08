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
package com.vs.izzdin.configuration;

import com.vs.izzdin.UDPSocketServer;

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
public class serverMessage {

    public static final int PORT = 6543;

    public static final int BUFFER_SIZE = 256;
    public static final String MESSAGE = "Hallo World";
    /*----------------------------------------------*/
    private static serverMessage instance;
    private int port = PORT;
    private int bufferSize = BUFFER_SIZE;
    private String message = MESSAGE;
    String[] kurzel = {"AAPL", "AMZN", "MSFT", "TSLA" }; //Apple, Tesla, Microsoft, Amazon
    int aktuellWert = -1;
    int richtung = 1;
    public static serverMessage getInstance() {
        if (instance == null)
            instance = new serverMessage();
        return instance;
    }

    public static int aktienAnzahl() {
        Random rd = new Random();
        int zufall = (rd.nextInt(1500) + 1);
        return zufall;
    }

    public String getMessage() {
        int zufall  = new Random().nextInt(3) ;
        int aktienNum = aktienAnzahl();

        String boerse ="Börse";
       if(!UDPSocketServer.boerseName.isEmpty()) {
            boerse = UDPSocketServer.boerseName;
        }


        if(aktuellWert < 0) {
            aktuellWert = new Random().nextInt(301) +200;
        } else{
            if(aktuellWert >= 500){
                richtung = -1;
            } else if (aktuellWert <=200) {
                richtung = 1;
            }
            aktuellWert += richtung * 5;
        }

        message = aktienNum + "\n" +
                kurzel[zufall] + "\n" +
                aktuellWert + "\n"+
                boerse + "\n";
        return this.message;

    }

    public int getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(String bufferSize) {
        this.bufferSize = Integer.parseInt(bufferSize);
    }

    /**
     * A private constructor to avoid
     * instantiation.
     */
    private serverMessage() {
    }
}
