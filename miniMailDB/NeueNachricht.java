
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class NeueNachricht extends JDialog {
    private static final long serialVersionUID = -5496318621928815910L;
    // für die Eingabefelder
    private JTextField empfaenger, betreff;
    private JTextArea inhalt;
    private JButton senden, abbrechen;

    // die innere Klasse für den ActionListener
    class NeuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("senden"))
                senden();
            else if (e.getActionCommand().equals("abbrechen"))
                dispose();
        }
    }

    // Konstruktor
    public NeueNachricht(JFrame parent, boolean modal) {
        super(parent, modal);
        setTitle("Neue Nachricht");
        initGui();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    // Erstellung und Konfiguration der Benutzeroberfläche
    private void initGui() {
        setLayout(new BorderLayout());

        JPanel oben = new JPanel();
        oben.setLayout(new GridLayout(0, 2));
        oben.add(new JLabel("Empfänger:"));
        empfaenger = new JTextField();
        oben.add(empfaenger);
        oben.add(new JLabel("Betreff:"));
        betreff = new JTextField();
        oben.add(betreff);
        add(oben, BorderLayout.NORTH);
        inhalt = new JTextArea();
        inhalt.setLineWrap(true);
        inhalt.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(inhalt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);

        JPanel unten = new JPanel();
        senden = new JButton("Senden");
        senden.setActionCommand("senden");
        abbrechen = new JButton("Abbrechen");
        abbrechen.setActionCommand("abbrechen");

        NeuListener listener = new NeuListener();
        senden.addActionListener(listener);
        abbrechen.addActionListener(listener);
        unten.add(senden);
        unten.add(abbrechen);
        add(unten, BorderLayout.SOUTH);
        setSize(600, 300);
        setVisible(true);
    }

    private void senden() { // Verbindung herstellen und eine Sitzung erhalten
        Session sitzung = verbindungHerstellen();
        if (sitzung != null) {
            nachrichtVerschicken(sitzung);// send
            nachrichtSpeichern();// save
            // close
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Verbindung konnte nicht hergestellt werden.", "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // verbindung herstellen
    private static Session verbindungHerstellen() {
        String konfigurationsdatei = "config.txt";
        Properties eigenschaften = new Properties();

        try (InputStream input = new FileInputStream(konfigurationsdatei)) {
            eigenschaften.load(input);
            String benutzername = eigenschaften.getProperty("Benutzername");
            String kennwort = eigenschaften.getProperty("Kennwort");
            // Serveradresse aus den Eigenschaften extrahieren
            String server = "mail.gmx.net";
            int port = 587;

            eigenschaften.put("mail.smtp.auth", "true");
            eigenschaften.put("mail.smtp.starttls.enable", "true");
            eigenschaften.put("mail.smtp.host", server);
            eigenschaften.put("mail.smtp.port", String.valueOf(port));

            eigenschaften.put("mail.smtp.charset", "UTF-8");
            eigenschaften.put("mail.mime.charset", "UTF-8");

            Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(benutzername, kennwort);
                }
            });

            return sitzung;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void nachrichtVerschicken(Session sitzung) {
        String absender = "emailName@gmx.ch";

        try {
            // Eine neue MimeMessage erstellen
            MimeMessage nachricht = new MimeMessage(sitzung);
            // Absender der Nachricht festlegen nachricht.setFrom(new
            // InternetAddress(absender));
            // Betreff der Nachricht festlegen
            nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
            nachricht.setSubject(betreff.getText());
            // Originaltext der Nachricht abrufen
            String originalText = inhalt.getText();

            // Überprüfen, ob der Originaltext mit "WG:" beginnt
            if (originalText.startsWith("WG:")) {
                nachricht.setText("----- Text der ursprünglichen Nachricht ----\n" + originalText);
            } else if (originalText.startsWith("AW:")) {
                nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(absender));
                String neuerBetreff = "AW: " + betreff.getText();
                nachricht.setSubject(neuerBetreff);
                String neuerText = "----- Text der ursprünglichen Nachricht ----\n" + originalText + "\n\n";
                neuerText += inhalt.getText();
                nachricht.setText(neuerText);
                Transport.send(nachricht);
                JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");
                dispose();

            }
            // Falls ein Fehler auftritt, wird eine entsprechende Fehlermeldung angezeigt
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    // save Msg
    private void nachrichtSpeichern() {
        Connection verbindung;
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
            PreparedStatement prepState;
            prepState = verbindung.prepareStatement(
                    "insert into gesendet (empfaenger, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, empfaenger.getText());
            prepState.setString(2, betreff.getText());
            prepState.setString(3, inhalt.getText());
            prepState.executeUpdate();
            verbindung.commit();

            prepState.close();
            verbindung.close();
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }
}
