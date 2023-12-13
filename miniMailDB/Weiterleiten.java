
import jakarta.mail.*;
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

public class Weiterleiten extends JDialog {

    private static final long serialVersionUID = 2514022432736759969L;
    // für die Eingabefelder
    private JTextField empfaenger, betreff;
    private JTextArea inhalt;
    private JButton ok, abbrechen;
    private String betreffZeile, inhaltText;

    class NeuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("senden"))
                senden();
            if (e.getActionCommand().equals("abbrechen"))
                dispose();
        }
    }

    // Konstruktor
    public Weiterleiten(JFrame parent, boolean modal, String betreff, String inhalt) {
        super(parent, modal);
        betreffZeile = betreff;
        inhaltText = inhalt;
        initGui();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    // Erstellung und Konfiguration der Benutzeroberfläche
    private void initGui() {
        setLayout(new BorderLayout());
        JPanel oben = new JPanel();
        oben.setLayout(new GridLayout(0, 2));
        oben.add(new JLabel("Empfaenger:"));
        empfaenger = new JTextField();
        oben.add(empfaenger);
        oben.add(new JLabel("Betreff:"));
        betreff = new JTextField("WG: " + betreffZeile);
        oben.add(betreff);
        add(oben, BorderLayout.NORTH);
        inhalt = new JTextArea("----- Text der ursprünglichen Nachricht ----\n" + inhaltText);
        inhalt.setLineWrap(true);
        inhalt.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(inhalt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);
        JPanel unten = new JPanel();
        ok = new JButton("Senden");
        ok.setActionCommand("senden");
        abbrechen = new JButton("Abbrechen");
        abbrechen.setActionCommand("abbrechen");
        NeuListener listener = new NeuListener();
        ok.addActionListener(listener);
        abbrechen.addActionListener(listener);
        unten.add(ok);
        unten.add(abbrechen);
        add(unten, BorderLayout.SOUTH);
        setSize(600, 300);
        setVisible(true);
    }

    private void senden() {
        Session sitzung = verbindungHerstellen();
        nachrichtVerschicken(sitzung);
        nachrichtSpeichern();
    }

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
    } // send

    private void nachrichtVerschicken(Session sitzung) {
        String absender = "emailName@gmx.ch";

        try { // get Mime Msg
            MimeMessage nachricht = new MimeMessage(sitzung);

            nachricht.setFrom(new InternetAddress(absender));
            nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
            nachricht.setSubject(betreff.getText());
            // check originale
            String originalText = inhalt.getText();
            String neuerText = "----- Text der ursprünglichen Nachricht ----\n" + originalText;

            nachricht.setText(neuerText);
            Transport.send(nachricht);

            JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");
            // close
            dispose();
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtSpeichern() {
        Connection verbindung;
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:emailDB");

        try {
            PreparedStatement prepState;
            prepState = verbindung
                    .prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?,?,?)");
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
}
