# `Distributed Systems`

## Analyse der Aktienperformance: Gewinne und Verluste im Fokus
```
Die Banken senden zunächst eine Nachricht (z.B. "Bank Sparkasse") per UDP an die Börse, und die Börse empfängt das Paket.
Anschließend sendet die Börse eine Nachricht an dieselbe Bank, die die Abkürzung des Wertpapiers, die Anzahl der Aktien und den aktuellen Wert der gekauften Aktien enthält.
Die Bank empfängt diese Informationen von der Börse und vergleicht sie mit den ursprünglichen Werten der Aktien. Dadurch kann die Bank feststellen, ob ein Gewinn oder Verlust entsteht.
Anschließend aktualisiert sie die Aktienwerte. Sowohl die Bank als auch die Börse warten jeweils vier Sekunden aufeinander.
Darüber hinaus behandelt die Bank auch Paketverluste, indem sie die Differenz zwischen dem empfangenen und dem gesendeten Paket-Counter berechnet.    
Auf der Börsenseite entsprechen die Abkürzungen der Wertpapiere denen in der Bank. Sowohl die Anzahl der Aktien als auch die Werte werden zufällig generiert,
jedoch innerhalb vernünftiger Rahmenbedingungen. Es wird darauf geachtet, dass die Werte nicht stark voneinander abweichen.
```
## RTT (Round Trip Time) UDP
``` 
Die Paketumlaufzeit wird nur auf der Bankenseite berechnet, indem die lokale Zeit der Paketsendung (send()) und die lokale Zeit des Paketempfangs (receive()) erfasst werden.
Durch die Berechnung der Differenz zwischen den beiden lokalen Zeiten wird die Paketumlaufzeit (RTT) ermittelt.
```

## `In der Bank intialisierte und ursprüngliche Werte`
| ***Name***      | Kurzel | Werte  |
|-----------------|--------|--------|
| ***Apple***     | AAPL   | 375 €  |
| ***Amazon***    | AMZN   | 310 €  |
| ***Microsoft*** | MSFT   | 300 €  |
| ***Tesla***     | TSLA   | 280 €  |

###### Bemerkung:
- ping is a tool using the icmp protocol. icmp sits on layer 3, the network layer, of the OSImodel.
- ping is much faster then UDP.


## HTTP 
``` 
Wir haben die Kommunikation zur Datenübertragung auf TCP umgestellt, um HTTP-Methoden wie POST und GET nutzen zu können.
Daher haben wir die Ergebnisse einer UDP-Kommunikation intern an einen anderen Ort, nämlich den ClientHandlerTCP, übertragen,
um sie dort sinnvoll weiterverarbeiten zu können. Die empfangenen Daten werden dann in der TCPSocketServer-Datei auf der Bankseite verarbeitet und mithilfe von selbst implementierten HTTP GET- und POST-Anfragen in einem selbst erstellten Webbrowser ausgegeben.
Zusätzlich zu dieser internen Kommunikation ist es in unserem Programm möglich, Daten über das Postman-Tool oder unsere eigens erstellte Webseite als POST-Anfragen zu senden.
Diese Daten haben Einfluss auf das Portfolio der Banken, da Kunden sowohl Einzahlungen/Überweisungen (+) als auch Auszahlungen/Abhebungen (-) vornehmen können.   
```

## RTT (Round Trip Time) TCP
```
Bei TCP soll die Paketumlaufzeit auf der Bankseite, ähnlich wie bei UDP, vom Zeitpunkt des Sendens bis zum Zeitpunkt des Empfangens berechnet werden.
Das Senden erfolgt mit OutputStream und das Empfangen, beispielsweise des Acks, mit InputStream. Sobald das Acknowledgment empfangen wird, wird die Differenz zwischen beiden Ereignissen berechnet.
Es ist zu erwarten, dass aufgrund des TCP-Transportmechanismus längere Wartezeiten auf das Acknowledgment auftreten können. 
Daher wäre es sinnvoll, die Implementierung so zu gestalten, dass angemessen auf lange Paketumlaufzeiten reagiert und diese korrekt interpretiert werden.```
```
## Wer ist schneller ***TCP*** oder ***UDP***
```
Laut der Test-Ergebnisse der jeweiligen RTT unserer Implementation ist UDP deutlich schneller als TCP,
da UDP einen geringeren Overhead als TCP hat.Es wird nähmlich keine Bestätigungen, Flusskontrolle oder Verbindungsaufbau benötigt.
UDP sendet einfach die Datenpakete ohne zusätzliche Kontrollmechanismen.

```

## RPC
```
Nach Ausführung der Thrift-Datei, die drei Funktionen enthält, wurden automatisch drei Java-Klassen (InvalidOperation, BankRettungsService und Geldbetrag) als Schnittstellen generiert. Diese wurden dem Socket-Client (Bank) hinzugefügt.
Die drei Funktionen der Thrift-Datei wurden in der Klasse RpcController auf der Bank-Seite implementiert. Dadurch ermöglichen wir den Zugriff bzw. den Aufruf dieser Funktionen von der Bank aus mithilfe von TTransport und TProtocol.
Alle drei Funktionen haben einen Parameter vom Typ "geldbetrag", der als Struktur in der Thrift-Datei definiert ist und die Eigenschaften Geld, Transaktionsnummer und Verwendungszweck enthält.
In der Funktion "ausleihen(geldBetrag geld)" wird überprüft, ob die Bank in der Lage ist, den gesamten geforderten Betrag, einen Teil davon oder nichts zu überweisen.
In der zweiten Funktion namens "ueberweisen(geldbetrag geld)" wird tatsächlich der gesamte Geldbetrag überwiesen und der überwiesene Betrag sicherheitshalber in einem Container gespeichert,
ohne weitere Fragen zu stellen. Dadurch wird das Portfolio der Bank entsprechend beeinflusst.
Die letzte Funktion namens "stornieren(geldbetrag geld)" sucht die übermittelte Transaktionsnummer in dem Container.
Wenn die Transaktionsnummer vorhanden ist, wird der Eintrag aus dem Container gelöscht und storniert. Andernfalls wird der Vorgang abgebrochen.
```

## MQTT & 2PC
```
Wir haben uns für MQTT (Message Queuing Telemetry Transport) als Message-oriented Middleware (MoM) zur Nachrichtenübermittlung zwischen den Banken entschieden. Für die Verbindung mit dem Broker haben wir die Software Mosquitto verwendet,
die wir über die Datei docker-compose.yml heruntergeladen haben. Die Banken können über die Topics "Request" und "Ack" Nachrichten veröffentlichen und abonnieren. Dabei müssen das Topic und der Broker der jeweiligen Bank übereinstimmen,
um die Nachrichten an den entsprechenden Worker weiterzuleiten. Mit anderen Worten sind die Banken verpflichtet, sich beim Broker zu registrieren.
Folgende Schritte wurden für das implementierte 2PC (Two-Phase Commit) durchgeführt:
    1. Sobald eine Bank einen negativen Kontostand erreicht, sendet sie den benötigten Betrag als Nachricht über MQTT mit dem Topic "Request".
    2. Die anderen Banken abonnieren den Broker und prüfen, ob sie helfen können. Dies kann über den Aufruf der Funktion "ausleihen(geldbetrag geld)" festgestellt werden.
       Falls die Hilfe akzeptiert wird, erhöht sich der Commit Counter um eins, andernfalls der Abort Counter.
    3. Mit dem Topic "Ack" wird letztendlich entweder "Commit" oder "Abort" gesendet. Dies geschieht jedoch nur, wenn alle Banken "Commit" signalisieren.
     Falls eine Bank einen "Abort" signalisiert, wird anstelle von "Commit" die Nachricht mit "Abort" befüllt und gesendet.
    4. Wenn ein "Commit" empfangen wird, wird die Methode "ueberweisen(geldbetrag geld)" aufgerufen.
    5. Wenn ein "Abort" empfangen wird, wird die Methode "stornieren(geldbetrag geld)" aufgerufen.

```
## Docker Compose
|         | Bank           |  
|---------|----------------|
| ***1*** | Sparkasse_Bank |
| ***2*** | Volksbank      |
| ***3*** | Aareal_Bank    |
| ***4*** | Commerz_Bank   |

|         | Börse               |
|---------|---------------------|
| ***1*** | Boerse_XETRA_DEU    |
| ***2*** | Boerse_NYSE_America |
```
In der Docker Compose-Datei haben wir zwei Börsen mit unterschiedlichen UDP-Kommunikationsports und vier Banken mit unterschiedlichen TCP-Kommunikationsports (HTTP) definiert.
Für die MQTT-Kommunikation steht in der Docker Compose-Datei die Mosquitto-Software zur Verfügung.
```
### Programm Ausführen 
```
    1. docker compose build
    2. doker compose up
```