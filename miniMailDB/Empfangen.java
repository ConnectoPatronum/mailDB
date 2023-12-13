
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import jakarta.mail.Multipart;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

public class Empfangen extends JFrame {
    // automatisch über Eclipse ergänzt
    private static final long serialVersionUID = 2905894464790399040L;

    // für die Tabelle
    private JTable tabelle;
    private DefaultTableModel modell;
    private JButton forward, answer;
    private JToolBar leiste;

    // Listener für die Buttons "Weiterleiten" und "Antworten"
    class MeinListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("forward")) {
                // Wenn keine Nachricht ausgewählt ist, passiert nichts
                if (tabelle.getSelectedRow() == -1)
                    return;

                // Betreff und Inhalt aus der ausgewählten Zeile erhalten
                int zeile = tabelle.getSelectedRow();
                String betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
                String inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();

                // Weiterleiten aufrufen
                weiterleiten(true, betreff, inhalt);
            }
            if (e.getActionCommand().equals("answer")) {
                // Wenn keine Nachricht ausgewählt ist, passiert nichts
                if (tabelle.getSelectedRow() == -1)
                    return;

                // Sender, Betreff und Inhalt aus der ausgewählten Zeile erhalten
                int zeile = tabelle.getSelectedRow();
                String sender = tabelle.getModel().getValueAt(zeile, 1).toString();
                String betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
                String inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();

                // Antworten aufrufen
                antworten(sender, betreff, inhalt);
            }
        }
    }

    // Methode zur Erstellung der Symbolleiste
    private JToolBar symbolleiste() {
        JToolBar leiste = new JToolBar();
        // Weiterleiten-Button
        forward = new JButton("Weiterleiten");
        forward.setIcon(new ImageIcon("icons/mail-forward.gif"));
        forward.setActionCommand("forward");
        forward.setToolTipText("Leitet eine E-Mail weiter");

        // Antworten-Button
        answer = new JButton("Antworten");
        answer.setIcon(new ImageIcon("icons/mail-reply.gif"));
        answer.setActionCommand("answer");
        answer.setToolTipText("Eine E-Mail beantworten");

        // Listener hinzufügen
        MeinListener listener = new MeinListener();
        forward.addActionListener(listener);
        answer.addActionListener(listener);

        // Buttons zur Symbolleiste hinzufügen
        leiste.add(forward);
        leiste.add(answer);

        return leiste;
    }

    // Konstruktor
    Empfangen() {
        super();
        setTitle("E-Mail empfangen");
        setLayout(new BorderLayout());
        setVisible(true);
        setSize(700, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        leiste = symbolleiste();
        add(leiste, BorderLayout.NORTH);
        tabelleErstellen();
        tabelleAktualisieren();

        addWindowListener(new MeinWindowAdapter());
    }

    // Methode zum Aktualisieren der Tabelle

    private void tabelleAktualisieren() {
        // für den Datenbankzugriff
        Connection verbindung;
        ResultSet ergebnisMenge;

        // für die Spalten
        String sender, betreff, inhalt, ID;
        // die Inhalte löschen
        modell.setRowCount(0);

        try {
            // Verbindung herstellen und Ergebnismenge beschaffen
            verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
            ergebnisMenge = MiniDBTools.liefereErgebnis(verbindung, "SELECT * FROM empfangen");
            // die Einträge in die Tabelle schreiben
            while (ergebnisMenge.next()) {
                ID = ergebnisMenge.getString("iNummer");
                sender = ergebnisMenge.getString("sender");
                betreff = ergebnisMenge.getString("betreff");
                // den Inhalt vom CLOB beschaffen und in einen String umbauen
                Clob clob;
                clob = ergebnisMenge.getClob("inhalt");
                inhalt = clob.getSubString(1, (int) clob.length());

                // die Zeile zum Modell hinzufügen
                // dazu benutzen wir ein Array vom Typ Object
                modell.addRow(new Object[] { ID, sender, betreff, inhalt });
            }
            // die Verbindungen wieder schließen und trennen
            ergebnisMenge.close();
            verbindung.close();
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
        }
    }

    // Methode zum Erstellen der Tabelle für eingehende E-Mails
    private void tabelleErstellen() {
        // für die Spaltenbezeichner
        String[] spaltenNamen = { "ID", "Sender", "Betreff", "Text" };
        // ein neues Standardmodell erstellen
        modell = new DefaultTableModel();
        // die Spaltennamen setzen
        modell.setColumnIdentifiers(spaltenNamen);
        // die Tabelle erzeugen
        tabelle = new JTable();
        // und mit dem Modell verbinden
        tabelle.setModel(modell);
        // wir haben keinen Editor, können die Tabelle also nicht bearbeiten
        tabelle.setDefaultEditor(Object.class, null);
        // es sollen immer alle Spalten angepasst werden
        tabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // und die volle Größe genutzt werden
        tabelle.setFillsViewportHeight(true);
        // die Tabelle setzen wir in ein Scrollpane
        JScrollPane scroll = new JScrollPane(tabelle);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);
        // einen Maus-Listener ergänzen
        tabelle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // war es ein Doppelklick?
                if (e.getClickCount() == 2) {
                    // die Zeile beschaffen
                    int zeile = tabelle.getSelectedRow();
                    // die Daten beschaffen
                    String sender, betreff, inhalt, ID;
                    ID = tabelle.getModel().getValueAt(zeile, 0).toString();
                    sender = tabelle.getModel().getValueAt(zeile, 1).toString();
                    betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
                    inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
                    // und anzeigen
                    // übergeben wird der Frame der äußeren Klasse
                    new Anzeige(Empfangen.this, true, ID, sender, betreff, inhalt);
                }
            }
        });
    }

    class MeinWindowAdapter extends WindowAdapter {
        // für das Öffnen des Fensters
        @Override
        public void windowOpened(WindowEvent e) {
            // die Methode nachrichtenEmpfangen() aufrufen
            nachrichtenEmpfangen();
        }
    }

    private void nachrichtenEmpfangen() {
        nachrichtenAbholen();
        // nach dem Empfangen lassen wir die Anzeige aktualisieren
        tabelleAktualisieren();
    }

    // E-Mails von einem POP3-Server abzurufen und sie zu verarbeiten
    private void nachrichtenAbholen() {
        try {
            // Konfigurationsdatei laden
            Properties konfiguration = new Properties();

            FileInputStream konfigurationsDatei = new FileInputStream("config.txt");
            konfiguration.load(konfigurationsDatei);
            konfigurationsDatei.close();

            // Zugangsdaten aus der Konfigurationsdatei lesen
            String benutzername = konfiguration.getProperty("Benutzername");
            String kennwort = konfiguration.getProperty("Kennwort");

            String server = "pop.gmx.net";

            // Eigenschaften setzen
            Properties eigenschaften = new Properties();

            eigenschaften.put("mail.mime.charset", "UTF-8");
            eigenschaften.put("mail.smtps.charset", "UTF-8");
            eigenschaften.put("mail.store.protocol", "pop3");
            eigenschaften.put("mail.pop3.host", server);
            eigenschaften.put("mail.pop3.port", "995");
            eigenschaften.put("mail.pop3.starttls.enable", "true");
            Session sitzung = Session.getDefaultInstance(eigenschaften);

            // Store-Objekt über die Sitzung erzeugen und verbinden
            try (Store store = sitzung.getStore("pop3s")) {
                store.connect(server, benutzername, kennwort);
                Folder posteingang = store.getFolder("INBOX");
                posteingang.open(Folder.READ_WRITE);

                Message[] nachrichten = posteingang.getMessages();

                if (nachrichten.length != 0) {
                    JOptionPane.showMessageDialog(null,
                            "Es gibt " + posteingang.getUnreadMessageCount() + " neue Nachrichten.");
                    for (Message nachricht : nachrichten)
                        nachrichtVerarbeiten(nachricht);
                } else
                    JOptionPane.showMessageDialog(null, "Es gibt keine neuen Nachrichten.");

                posteingang.close(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problem beim Laden der Konfigurationsdatei: \n" + e.toString());
        }
    }

    // In diesem Teil des Codes wird der Fall abgedeckt, wenn die Nachricht den Typ
    // "multipart/mixed" hat und aus mehreren Teilen besteht.
    private void nachrichtVerarbeiten(Message nachricht) {
        try {
            // Ist es einfacher Text?
            if (nachricht.isMimeType("text/plain")) {
                // Den ersten Sender beschaffen
                String sender = nachricht.getFrom()[0].toString();
                // Den Betreff
                String betreff = nachricht.getSubject();
                // Den Inhalt
                String inhalt = nachricht.getContent().toString();
                // Und die Nachricht speichern
                nachrichtSpeichern(sender, betreff, inhalt);
                // Und zum Löschen markieren
                nachricht.setFlag(Flags.Flag.DELETED, true);
            }
            // Ist es multipart/mixed?
            else if (nachricht.isMimeType("multipart/mixed")) {
                Multipart multipart = (Multipart) nachricht.getContent();
                int numParts = multipart.getCount();

                for (int i = 0; i < numParts; i++) {
                    BodyPart part = multipart.getBodyPart(i);

                    if (part.isMimeType("text/plain")) {
                        // Verarbeite Textteil
                        String sender = nachricht.getFrom()[0].toString();
                        String betreff = nachricht.getSubject();
                        String inhalt = part.getContent().toString();
                        nachrichtSpeichern(sender, betreff, inhalt);
                    } else {
                        // je nach Bedarf.........................
                    }
                }

                // Nachricht zum Löschen markieren
                nachricht.setFlag(Flags.Flag.DELETED, true);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Der Typ der Nachricht " + nachricht.getContentType() + " kann nicht verarbeitet werden.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
        }
    }

    // speichern die Nachricht
    private void nachrichtSpeichern(String sender, String betreff, String inhalt) {
        // für die Verbindung
        Connection verbindung;
        // die Datenbank öffnen
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
            // einen Eintrag in der Tabelle empfangen anlegen
            // über ein vorbereitetes Statement
            PreparedStatement prepState;
            prepState = verbindung.prepareStatement("insert into empfangen (sender, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, sender);
            prepState.setString(2, betreff);
            prepState.setString(3, inhalt);
            // das Statement ausführen
            prepState.executeUpdate();
            verbindung.commit();

            // Verbindung schließen
            prepState.close();
            verbindung.close();
            // und die Datenbank schließen
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
        }
    }

    // die Dialoge anzeigen und erstellen bswp. beim Antworten
    private void weiterleiten(boolean modal, String betreff, String inhalt) {
        new Weiterleiten(this, modal, betreff, inhalt).setVisible(true);
    }

    private void antworten(String sender, String betreff, String inhalt) {
        SwingUtilities.invokeLater(() -> {
            Antworten antwortenDialog = new Antworten(this, true, sender, betreff, inhalt);
            antwortenDialog.setVisible(true);
        });
    }

}
