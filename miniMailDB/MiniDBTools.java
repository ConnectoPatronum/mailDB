
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JLabel;

public class MiniDBTools {

	private static JLabel statusLabel;

	// Methode zur Erstellung der Leiste für den Dialog
	public static JToolBar createToolbar() {
		JToolBar toolbar = new JToolBar();
		statusLabel = new JLabel();
		toolbar.add(statusLabel);
		return toolbar;
	}

	// Methode zum Aktualisieren der Anzeige in der Leiste
	private static void updateStatusLabel(int currentRecord, int totalRecords) {
		statusLabel.setText("Datensatz " + currentRecord + " von " + totalRecords);
	}

	// Methode zum Öffnen der Verbindung zur Datenbank
	public static Connection oeffnenDB(String arg) {
		Connection verbindung = null;
		try {
			verbindung = DriverManager.getConnection(arg);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
		}
		return verbindung;
	}

	// Methode zum Abrufen der Ergebnismenge
	public static ResultSet liefereErgebnis(Connection verbindung, String sqlAnweisung) {
		ResultSet ergebnisMenge = null;
		try {
			Statement state = verbindung.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ergebnisMenge = state.executeQuery(sqlAnweisung);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
		}
		return ergebnisMenge;
	}

	// Methode zum Herunterfahren des Datenbanksystems
	public static void schliessenDB(String protokoll) {
		boolean erfolg = false;
		try {
			DriverManager.getConnection(protokoll + "adressenDB; shutdown=true");
		} catch (SQLException e) {
			erfolg = true;
		}
		if (!erfolg) {
			JOptionPane.showMessageDialog(null, "Das DBMS konnte nicht heruntergefahren werden.");
		}
	}

	// Methode zum Navigieren zum nächsten Datensatz
	public static void naechsterDatensatz(ResultSet ergebnisMenge, int currentRecord) {
		try {
			if (ergebnisMenge.next()) {
				currentRecord++;
				updateStatusLabel(currentRecord, ergebnisMenge.getRow());
				// Daten anzeigen oder verarbeiten
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
		}
	}

	// Methode zum Löschen des aktuellen Datensatzes
	public static void loescheDatensatz(ResultSet ergebnisMenge, int currentRecord) {
		try {
			ergebnisMenge.deleteRow();
			updateStatusLabel(currentRecord, ergebnisMenge.getRow());
			// Weitere Aktionen nach dem Löschen des Datensatzes
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString());
		}
	}
}
