package com.vs.izzdin;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public  class RpcController implements BankRettungsService.Iface{

    public static List<geldBetrag> ueberweisungen = new ArrayList<>();
    @Override
    public double ausleihen(geldBetrag geld) throws TException {
        if(UDPSocketClient.gesamtWert < 0){
            System.out.println("Info: Ich bin nicht in der Lage Geld zu geben");
        }else if(UDPSocketClient.gesamtWert > 0){
            double currentTotal  = UDPSocketClient.gesamtWert;
            double diff = currentTotal - geld.geld;
            if(diff < 0){
                double betrag = currentTotal + diff;
                System.out.println("Info: Leider kann ich so viel nicht dir ausleihen");
                System.out.println("Ich kann nur "+ betrag + " dir ausleihen");
                return betrag;
            }
            return geld.geld;
        }
        return 0.0;
    }

    @Override
    public double ueberweisen(geldBetrag geld) throws TException {
        ueberweisungen.add(geld);
        UDPSocketClient.gesamtWert = UDPSocketClient.gesamtWert - geld.geld;
        return geld.geld;
    }

    @Override
    public boolean stornieren(geldBetrag geld) {
        int id = geld.getTransaktionsnummer();

        if (id >= 0){
            for(int i = 0; i < ueberweisungen.size(); i++) {
                geldBetrag stornierteUeberweisung = ueberweisungen.get(i);
                if(id == stornierteUeberweisung.getTransaktionsnummer()){
                    ueberweisungen.remove(i);
                    System.out.println(stornierteUeberweisung.getGeld() + " " +
                                       stornierteUeberweisung.getVerwendungszweck() + " " +
                                    stornierteUeberweisung.getTransaktionsnummer()+
                            " - Diese Transaktion wurde storniert.");
                }else {
                    System.out.println(stornierteUeberweisung.getGeld() + " " +
                            stornierteUeberweisung.getVerwendungszweck() + " " +
                            stornierteUeberweisung.getTransaktionsnummer()+ " " +
                            " - Diese Transaktion wurde abgebrochen.");
                }
            }
            return true;
        } else {
            System.out.println("Stornierung bzw. Vorgang Abbruch fehlgeschlagen");
            return false;
        }
    }



}
