# mailDB
MiniMail - E-Mail-Anwendung

Beschreibung

MiniMail ist eine Java-Anwendung, die es ermöglicht, E-Mails von einem POP3-Server abzurufen, anzuzeigen, darauf zu antworten und sie weiterzuleiten. Die Anwendung bietet eine benutzerfreundliche Oberfläche zum Verwalten von E-Mails und speichert empfangene E-Mails in einer Apache Derby-Datenbank.

Funktionalitäten

E-Mail-Abruf: MiniMail ermöglicht das Abrufen von E-Mails von einem POP3-Server.
Anzeige von E-Mails: Empfangene E-Mails werden in einer Tabelle mit den Spalten "ID", "Sender", "Betreff" und "Text" angezeigt.
Antworten auf E-Mails: Nutzer können auf E-Mails antworten und dabei den Sender, Betreff und Text bearbeiten.
Weiterleiten von E-Mails: Die Anwendung ermöglicht das Weiterleiten von E-Mails mit angepasstem Betreff und Text.
Speichern von E-Mails: Empfangene E-Mails werden in einer lokalen Apache Derby-Datenbank gespeichert.

Klassenübersicht

Empfangen: Erstellt die Benutzeroberfläche und ermöglicht das Anzeigen, Abrufen und Verwalten von E-Mails.
Weiterleiten: Klasse zur Erstellung eines Dialogs für das Weiterleiten von E-Mails.
Antworten: Klasse zur Erstellung eines Dialogs für das Antworten auf E-Mails.
Anzeige: Klasse zur Detailansicht einer einzelnen E-Mail.
MiniDBTools: Hilfsklasse für die Datenbankverbindung und -verwaltung.

Anforderungen

Java Development Kit (JDK) installiert
Apache Derby-Datenbank installiert und konfiguriert
Konfigurationsdatei config.txt für die POP3-Serververbindung

Verwendung

Starten der Anwendung: Führen Sie die Klasse Empfangen aus.
E-Mail-Abruf: Die Anwendung ruft automatisch E-Mails von einem konfigurierten POP3-Server ab.
Anzeigen von E-Mails: Die empfangenen E-Mails werden in einer Tabelle angezeigt. Ein Doppelklick auf eine E-Mail öffnet eine Detailansicht.
Antworten auf E-Mails: Klicken Sie auf "Antworten", um auf eine ausgewählte E-Mail zu antworten.
Weiterleiten von E-Mails: Klicken Sie auf "Weiterleiten", um eine E-Mail weiterzuleiten und einen neuen Empfänger, Betreff und Text anzugeben.

Datenbankkonfiguration

1.Apache Derby-Datenbank anlegen: Legen Sie eine neue Apache Derby-Datenbank an, um die E-Mails zu speichern.

Beispiel:

sql CREATE TABLE empfangen (
    ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    sender VARCHAR(100),
    betreff VARCHAR(255),
    inhalt CLOB,
    PRIMARY KEY (ID)
);

2.Konfigurationsdatei config.txt für die Datenbank: Konfigurieren Sie config.txt, um auf die erstellte Datenbank zuzugreifen.

Beispiel config.txt: Benutzername=meinbenutzername
Kennwort=meinkennwort

leider habe ich noch 98 fehlermeldungen...xD aber sie funktioniert


