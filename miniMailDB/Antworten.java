
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Authenticator;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class Antworten extends JDialog {

    private static final long serialVersionUID = 8627640580889368029L;
    // für die Eingabefelder
    private JTextField empfaenger, betreff;
    private JTextArea inhalt;
    private JButton sendenButton, abbrechenButton;
    private String senderTest, betreffZeile, inhaltText;

    // Konstruktor
    public Antworten(JFrame parent, boolean modal, String sender, String betreff, String inhalt) {
        super(parent, modal);
        senderTest = sender;
        betreffZeile = betreff;
        inhaltText = inhalt;
        initGui();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    // Erstellung und Konfiguration der Benutzeroberfläche
    private void initGui() {
        setLayout(new BorderLayout());

        JPanel oben = new JPanel(new GridLayout(0, 2));

        oben.add(new JLabel("Empfänger:"));

        empfaenger = new JTextField(senderTest);
        oben.add(empfaenger);
        oben.add(new JLabel("Betreff:"));
        betreff = new JTextField("AW: " + betreffZeile);
        oben.add(betreff);
        add(oben, BorderLayout.NORTH);

        inhalt = new JTextArea("----- Text der ursprünglichen Nachricht ----\n" + inhaltText);
        inhalt.setLineWrap(true);
        inhalt.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(inhalt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);

        JPanel unten = new JPanel();
        sendenButton = new JButton("Senden");
        sendenButton.addActionListener(new NeuListener());
        abbrechenButton = new JButton("Abbrechen");
        abbrechenButton.addActionListener(new NeuListener());
        unten.add(sendenButton);
        unten.add(abbrechenButton);
        add(unten, BorderLayout.SOUTH);

        setSize(600, 300);
        setVisible(true);
    }

    private void senden() {
        Session session = verbindungHerstellen();
        if (session != null) {
            dispose();
            nachrichtVerschicken(session);
            nachrichtSpeichern();
        } else {
            JOptionPane.showMessageDialog(this, "Verbindung konnte nicht hergestellt werden.", "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // verbindung aufbauen
    private static Session verbindungHerstellen() {
        String konfigurationsdatei = "config.txt";
        Properties eigenschaften = new Properties();

        try (InputStream input = new FileInputStream(konfigurationsdatei)) {
            eigenschaften.load(input);
            String benutzername = eigenschaften.getProperty("Benutzername");
            String kennwort = eigenschaften.getProperty("Kennwort");
            String server = "mail.gmx.net";
            int port = 587;

            eigenschaften.put("mail.mime.charset", "UTF-8");
            eigenschaften.put("mail.smtps.charset", "UTF-8");
            eigenschaften.put("mail.smtp.auth", "true");
            eigenschaften.put("mail.smtp.starttls.enable", "true");
            eigenschaften.put("mail.smtp.host", server);
            eigenschaften.put("mail.smtp.port", String.valueOf(port));

            Session session = Session.getInstance(eigenschaften, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(benutzername, kennwort);
                }
            });

            return session;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void nachrichtVerschicken(Session session) {
        String absender = "emailName@gmx.ch";

        try {
            MimeMessage nachricht = new MimeMessage(session);
            nachricht.setFrom(new InternetAddress(absender));
            nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
            nachricht.setSubject(betreff.getText());

            String originalText = inhalt.getText();
            String neuerText = "----- Text der ursprünglichen Nachricht ----\n" + originalText;

            nachricht.setText(neuerText);
            Transport.send(nachricht);

            JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");
            dispose();
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtSpeichern() {
        try {
            Connection verbindung = MiniDBTools.oeffnenDB("jdbc:derby:emailDB");
            PreparedStatement prepState = verbindung
                    .prepareStatement("INSERT INTO gesendet (empfaenger, betreff, inhalt) VALUES (?,?,?)");
            prepState.setString(1, empfaenger.getText());
            prepState.setString(2, betreff.getText());
            prepState.setString(3, inhalt.getText());
            prepState.executeUpdate();
            verbindung.commit();

            prepState.close();
            verbindung.close();
            MiniDBTools.schliessenDB("jdbc:derby:emailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    class NeuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == sendenButton) {
                senden();
            } else if (e.getSource() == abbrechenButton) {
                dispose();
            }
        }
    }
}

// immernoch 98 Fehlermeldungen ...