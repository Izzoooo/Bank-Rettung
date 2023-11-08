package com.vs.izzdin;

import org.apache.thrift.TException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PayloadHandler {
public PayloadHandler(){}
   private String message;
   public static String ruckAntwort="";
   private static double geld = 0.0;
    private static int transaktionsnummer = 0;
    private static String bankNamee = "";
    private static double ergebnisVonAusleihen = 0.0;
  public static int zustimmungCommit = 0;
   public static int zustimmungAbort = 0;
    

  public void aktualisiere(String value){
      ruckAntwort = value;
      System.out.println(value);
    }

    public PayloadHandler(String message){
        this.message = message;
    }
    public void onReciveMessage() throws TException {
        System.out.println("Empfangene Nachricht: " + message);

        RpcController rpc = new RpcController();
        int randTransaktionsnummer = 8;


        if(isDouble(message)){
            ergebnisVonAusleihen = 0.0;
            geldBetrag geldTransaktion = new geldBetrag();

            geld= Double.parseDouble(message);
            transaktionsnummer =  randTransaktionsnummer;
            bankNamee = TCPSocketServer.bankName;

            geldTransaktion.setGeld(geld);
            geldTransaktion.setTransaktionsnummer(transaktionsnummer);
            geldTransaktion.setVerwendungszweck(bankNamee);


            ergebnisVonAusleihen = rpc.ausleihen(geldTransaktion);
            if ( ergebnisVonAusleihen == 0.0) {
                System.out.println("No money");
                aktualisiere("Abort");
                zustimmungAbort++;
                //zaehlerSpeichern(bankNamee, "Abort");
            

            } else {
                System.out.println("Jaaaa, ich kann helfen");
                aktualisiere("Commit");
                zustimmungCommit++;
                //zaehlerSpeichern(bankNamee, "Commit");
            
            }

        }
        else{

            geldBetrag geldTransaktuionsKopie = new geldBetrag();
            geldTransaktuionsKopie.setGeld(ergebnisVonAusleihen);
            geldTransaktuionsKopie.setTransaktionsnummer(transaktionsnummer);
           geldTransaktuionsKopie.setVerwendungszweck(bankNamee);
            if(message.equals("Commit")){
                System.out.println(message);
                System.out.println(rpc.ueberweisen(geldTransaktuionsKopie));
            }else if(message.equals("Abort")){
                System.out.println(message);
                System.out.println(rpc.stornieren(geldTransaktuionsKopie));
            }else {
                System.out.println("Unerwarteter Sting: "+ message);
            }
         
        // Daten zurücksetzen, um alles aktuell zu halten.
        zustimmungCommit = 0;
         zustimmungAbort = 0;
         ergebnisVonAusleihen = 0.0;
         transaktionsnummer = 0;
         bankNamee = "";
        }
    }


    private boolean isDouble(String message) {
        try {
            Double.parseDouble(message);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
    private void zaehlerSpeichern(String bank, String zustand) {
        String path = System.getenv("FILE_PATH2");
    
    
        try {
            FileWriter fileWriter = new FileWriter(path, true); // Der zweite Parameter "true" aktiviert den Append-Modus
            PrintWriter writer = new PrintWriter(fileWriter);
            String data = bank +" "+ zustand;
            writer.println(data);
            writer.close();
        } catch (IOException e) {
            System.out.println("Ein Fehler ist aufgetreten");
            e.printStackTrace();
        }
    }
    
    
}
