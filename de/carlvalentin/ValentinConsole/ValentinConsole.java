package de.carlvalentin.ValentinConsole;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.Interface.UI.*;
import de.carlvalentin.Protocol.*;
import de.carlvalentin.Protocol.UI.*;
import de.carlvalentin.Scripting.UI.*;
import de.carlvalentin.Updater.ValentinUpdater;

import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung um im Carl
 * Valentin Printer Language-Format (CVPL) mit einem Device kommunizieren zu
 * k&ouml;nnen.
 */
public class ValentinConsole extends JFrame {

	private static ValentinConsole vc;

	/**
	 * Ausgabe von Fehlermeldungen in Form von Dialogen
	 */
	private CVErrorMessage lk_cErrorMessage = null;

	/**
	 * Ausgabe von Fehlermeldungen in Logdatei
	 */
	private CVLogging lk_cErrorFile = null;

	/**
	 * Ausgabe von Statusmeldungen auf Statuszeile
	 */
	private CVStatusLine lk_cStatusMessage = null;

	private CVUINetwork lk_cTCPNetworkUI;

	/**
	 * Verwaltet die Verbindungen zum Drucker
	 */
	private CVConnectionManager lk_cConnectionManager = null;

	/**
	 * Datei zum Speichern der akutellen Konfiguration
	 */
	private CVConfigFile lk_cConfigFile = null;

	// Datei zum speichern der favorisierten Kommandos
	private CVCommandFile lk_cCommandFile = null;

	/**
	 * Einstellung Start-/Stopzeichen CVPL
	 */
	private CVSohEtb lk_cSohEtb = null;
	/**
	 * Toekn zum Speichern der gewaehlten Zeichen in Konfigurationsdatei
	 */
	private final String lk_szConfigTokenSohEtb = "ValentinConsoleSettingsSohEtb";

	/**
	 * Token zum Speichern der gewaehlten Schnittstelle in Konfigurationsdatei
	 */
	private final String lk_szConfigTokenInterface = "ValentinConsoleSettingsInterface";

	/**
	 * Token zum Speichern des gewaehlten Dateipfades in Konfigurationsdatei
	 */
	private final String lk_szConfigTokenPath = "ValentinConsoleSettingsPath";

	/**
	 * Token zum Speichern des Dateipfades zuletzt ge�ffneter Dateien in
	 * Konfigurationsdatei
	 */
	private final String lk_szConfigTokenFilePathRecent0 = "ValentinConsoleSettingsFilePathRecent0";
	private final String lk_szConfigTokenFilePathRecent1 = "ValentinConsoleSettingsFilePathRecent1";
	private final String lk_szConfigTokenFilePathRecent2 = "ValentinConsoleSettingsFilePathRecent2";
	private final String lk_szConfigTokenFilePathRecent3 = "ValentinConsoleSettingsFilePathRecent3";
	private final String lk_szConfigTokenFilePathRecent4 = "ValentinConsoleSettingsFilePathRecent4";

	/**
	 * Token zum Speichern des Dateinamens zuletzt ge�ffneter Dateien in
	 * Konfigurationsdatei
	 */
	private final String lk_szConfigTokenFileNameRecent0 = "ValentinConsoleSettingsFileNameRecent0";
	private final String lk_szConfigTokenFileNameRecent1 = "ValentinConsoleSettingsFileNameRecent1";
	private final String lk_szConfigTokenFileNameRecent2 = "ValentinConsoleSettingsFileNameRecent2";
	private final String lk_szConfigTokenFileNameRecent3 = "ValentinConsoleSettingsFileNameRecent3";
	private final String lk_szConfigTokenFileNameRecent4 = "ValentinConsoleSettingsFileNameRecent4";

	/**
	 * Token zum Speichern zuletzt verwendeter Dateipfade in Konfigurationsdatei
	 */
	String lk_szConfigTokenPathRecent0 = "ValentinConsoleSettingsPathRecent0";
	String lk_szConfigTokenPathRecent1 = "ValentinConsoleSettingsPathRecent1";
	String lk_szConfigTokenPathRecent2 = "ValentinConsoleSettingsPathRecent2";
	String lk_szConfigTokenPathRecent3 = "ValentinConsoleSettingsPathRecent3";
	String lk_szConfigTokenPathRecent4 = "ValentinConsoleSettingsPathRecent4";

	/**
	 * Grafische Oberflaeche fuer Einstellung Start-/Stopzeichen CVPL
	 */
	private CVUISohEtb lk_cUISohEtb = null;

	/**
	 * Konsole fuer Benutzereingaben
	 */
	private CVConsole lk_cConsoleInput = null;
	/**
	 * Titel der Konsole fuer Benutzereingaben
	 */
	private String lk_sConsoleInputTitle;

	/**
	 * Gibt Inhalt der Konsole hexadezimal aus
	 */
	private CVTextDisplay lk_cConsoleInputHEX = null;
	/**
	 * Titel der hexadezimalen Ausgabe der Konsolendaten
	 */
	private String lk_sConsoleInputHEXTitle;

	/**
	 * Grafische Oberflaeche fuer Skriptverarbeitung
	 */
	private CVUIBeanShell lk_cBeanShellScriptingUI = null;
	/**
	 * Titel der grafischen Oberflaeche fuer Skriptverarbeitung
	 */
	private String lk_sBeanShellScriptingTitle;

	private javax.swing.JPanel jPanelMain = null;
	private JTabbedPane jTabbedPaneMain = null;
	private javax.swing.JScrollPane jScrollPaneConsole = null;

	/**
	 * Zentrale Knopfleiste zur Einstellung der Verbindung/Schnittstelle
	 */
	private JPanel jPanelConnectBar = null;
	private JLabel jLabelConnectBarInterface = null;
	private JComboBox jComboBoxConnectBarChooseInterface = null;
	private JButton jButtonConnectBarConfigInterface = null;
	private JLabel jLabelConnectBarEncoding = null;
	private JComboBox jComboBoxConnectBarChooseEncoding = null;
	private JButton jButtonConnectBarConnect = null;
	private JButton jButtonConnectBarDisconnect = null;

	/**
	 * Zentrale Men&uumlbar
	 */
	private JMenuBar jMenuBarMain = null;
	/**
	 * Info Men&uuml
	 */
	private JMenu jMenuInfo = null;
	/**
	 * Men&uuml zur Arbeit mit Dateien
	 */
	private JMenu jMenuFile = null;
	private JMenuItem jMenuItemUpdate = null;
	private JMenu jMenuFileSubmenuRecentFile = null;
	private JMenu jMenuFileSubmenuRecentPath = null;
	private JMenuItem jMenuItemTransmitFile = null;
	private JMenuItem jMenuItemOpenRecent0 = null;
	private JMenuItem jMenuItemOpenRecent1 = null;
	private JMenuItem jMenuItemOpenRecent2 = null;
	private JMenuItem jMenuItemOpenRecent3 = null;
	private JMenuItem jMenuItemOpenRecent4 = null;
	private JMenuItem jMenuItemOpenRecentPath0 = null;
	private JMenuItem jMenuItemOpenRecentPath1 = null;
	private JMenuItem jMenuItemOpenRecentPath2 = null;
	private JMenuItem jMenuItemOpenRecentPath3 = null;
	private JMenuItem jMenuItemOpenRecentPath4 = null;
	private JMenuItem jMenuItemInfo = null;
	private JMenuItem jMenuItemLog = null;
	/**
	 * Men&uuml zur Konfiguration der verschiedenen Schnittstellen
	 */
	private JMenu jMenuInterface = null;
	private JMenuItem jMenuItemConfigureNetworkTCP = null;
	private JMenuItem jMenuItemConfigureNetworkUDP = null;
	private JMenuItem jMenuItemConfigureRS232 = null;
	private JMenuItem jMenuItemConfigureParallel = null;
	/**
	 * Men&uuml zur Beeinflussung der Konsole
	 */
	private JMenu jMenuConsole = null;
	private JMenuItem jMenuItemClearConsole = null;

	// Item fuer Favoriten Commands
	private JMenu jMenuCommandSelection = null;
	// Item fuer Scripte
	private JMenu jMenuScriptSelection = null;

	private boolean bAutoReconnectRunning = false;

	/**
	 * main-Funktion des Programms
	 *
	 */
	public static void main(String[] args) {
		int i = 0;
		String sArg;
		String sScript = null;
		String sIP = null;
		boolean bRunScript = false;
		int iPort = 0;

		// ----------------------------------------------------------------------
		// Classpath anpassen
		// ----------------------------------------------------------------------
		System.setProperty("java.class.path", "Veto.jar;" + ".\\nrjavaserial-5.2.1.jar;"
		        + ".\\bsh-2.0b4.jar;"
				+ ".\\lava3-core.jar;" + ".\\lava3-printf.jar" + ".\\rsyntaxtextarea-2.6.0.jar");

		while (i < args.length && args[i].startsWith("-")) {
			sArg = args[i++];

			if (sArg.equals("-h")) {
				System.out.println("-sSERVER:    Device IP-Address");
				System.out.println("-pPORT:      Printer Port-Number");
				System.out.println("-aSCRIPT");
				System.out.println("-lSCRIPT");
				System.exit(0);
			}

			if (sArg.startsWith("-s")) {
				sIP = sArg.substring(2);
			}

			if (sArg.startsWith("-p")) {
				iPort = Integer.parseInt(sArg.substring(2));
			}

			if (sArg.startsWith("-a")) {
				sScript = sArg.substring(2);
				bRunScript = true;
			}

			if (sArg.startsWith("-l")) {
				sScript = sArg.substring(2);
			}
		}

		vc = new ValentinConsole(sIP, iPort, sScript, bRunScript);
		vc.show();
	}

	/**
	 * Konstruktor der Klasse ValentinConsole
	 */
	public ValentinConsole(String sIP, int iPort, String sScript, boolean bRunScript) {
		super();

		this.initVC();
		this.initialize();

		if (this.lk_cConfigFile != null) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, "null");
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, "null");
			} else {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPath,
						lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1, "null");
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent2, "null");
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent2, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent3, "null");
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent3, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent4) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent4, "null");
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent4, "null");
			}

			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, "null");
			} else {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPath,
						lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent2, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent3, "null");
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent4) == null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent4, "null");
			}
		}

		boolean bConnect = false;

		if (sScript != null) {
			lk_cErrorMessage.setShowWindow(false, true);
			jTabbedPaneMain.setSelectedIndex(2);
			lk_cBeanShellScriptingUI.loadScript(sScript);
		}

		if ((sIP != null) || (iPort != 0)) {
			CVNetworkTCP cInterfaceNetworkTCP;
			CVNetworkSettings cNetworkInterfaceSettings;

			jComboBoxConnectBarChooseInterface.setSelectedItem((String) "TCP network");

			cInterfaceNetworkTCP = lk_cConnectionManager.getTCPNetworkInterface();
			cNetworkInterfaceSettings = (CVNetworkSettings) cInterfaceNetworkTCP.getInterfaceSettings();
			if (sIP != null) {
				cNetworkInterfaceSettings.setIPAdress(sIP);
			}
			if (iPort != 0) {
				cNetworkInterfaceSettings.setPort(iPort);
			}

			jButtonConnectBarConnect.doClick();
			bConnect = true;
		}

		if ((sScript != null) && bRunScript) {
			lk_cBeanShellScriptingUI.processButtonRunScript(true);
		}

		return;
	}

	/**
	 * Initialisiere Valentin Komponenten
	 */
	private void initVC() {
		// Konsole zur Eingabe von Daten
		this.lk_cConsoleInput = new CVConsole();
		this.lk_sConsoleInputTitle = "Console ASCII";

		// Ausgabe von Fehelermeldungen als Dialog
		this.lk_cErrorMessage = new CVErrorMessage(this);
		// Ausgabe von Fehlermeldungen in Logdatei
		this.lk_cErrorFile = new CVLogging(System.getProperty("java.io.tmpdir", ".") + "/VetoErrorLog.txt",
				this.lk_cErrorMessage);
		// Ausgabe von Fehlermeldungen in Statuszeile des Programms
		this.lk_cStatusMessage = new CVStatusLine();

		// Anlegen der Konfigurationsdatei
		this.lk_cConfigFile = new CVConfigFile("ValentinConsole", "1.0");
		this.lk_cCommandFile = new CVCommandFile("ValentinFavCommands");

		// Hexadezimale Ausgabe der Konsolendaten
		this.lk_cConsoleInputHEX = new CVTextDisplay(this.lk_cErrorMessage, this.lk_cErrorFile, this.lk_cStatusMessage);
		this.lk_cConsoleInputHEX.setOutputFormat(false, true); // (ASCII, HEX)
		this.lk_sConsoleInputHEXTitle = "Console HEX";

		// Verbindungsmanager zum Drucker
		this.lk_cConnectionManager = new CVConnectionManager(this.lk_cConfigFile, this.lk_cErrorMessage,
				this.lk_cErrorFile, this.lk_cStatusMessage);

		// Grafische Oberflaeche Skriptverarbeitung
		this.lk_cBeanShellScriptingUI = new CVUIBeanShell(this, this.lk_cErrorMessage, this.lk_cErrorFile,
				this.lk_cStatusMessage, this.lk_cConfigFile, this.lk_cConnectionManager);
		this.lk_sBeanShellScriptingTitle = "Scripting";

		// Start-/Stopzeichen CVPL
		this.lk_cSohEtb = CVSohEtb.x0117;
		this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);

		this.lk_cConsoleInput.restoreHistory(lk_cConfigFile);

		return;
	}

	/**
	 * Anzeige der grafischen Oberflaeche zur Konfiguration TCP-Netzwerk.
	 *
	 */
	private void showUINetworkTCPConfig() {
		lk_cTCPNetworkUI = new CVUINetwork(this.lk_cErrorMessage, this.lk_cErrorFile, this.lk_cStatusMessage,
				this.lk_cConnectionManager, CVNetworkProtocol.TCP);
		lk_cTCPNetworkUI.setModal(true);
		lk_cTCPNetworkUI.setLocationRelativeTo(vc);
		lk_cTCPNetworkUI.setVisible(true);
		return;
	}

	/**
	 * Anzeige der grafischen Oberflaeche zur Konfiguration UDP-Netzwerk.
	 *
	 */
	private void showUINetworkUDPConfig() {
		CVUINetwork cUDPNetworkUI = new CVUINetwork(this.lk_cErrorMessage, this.lk_cErrorFile, this.lk_cStatusMessage,
				this.lk_cConnectionManager, CVNetworkProtocol.UDP);
		cUDPNetworkUI.setPreferredSize(new java.awt.Dimension(400, 400));
		cUDPNetworkUI.pack();
		cUDPNetworkUI.setModal(true);
		cUDPNetworkUI.setLocationRelativeTo(vc);
		cUDPNetworkUI.setVisible(true);
		return;
	}

	/**
	 * Anzeige der grafischen Oberflaeche zur Konfiguration serieller Ports.
	 *
	 */
	private void showUISerialPortConfig() {
		CVUISerial serialUI = new CVUISerial(this.lk_cErrorMessage, this.lk_cErrorFile, this.lk_cStatusMessage,
				this.lk_cConnectionManager);
		serialUI.setModal(true);
		serialUI.setLocationRelativeTo(vc);
		serialUI.setVisible(true);
		return;
	}

	/**
	 * Anzeige der grafischen Oberflaeche zur Konfiguration serieller Ports.
	 *
	 */	
	private void showUIParallelPortConfig() {
/*	    
		CVUIParallel parallelUI = new CVUIParallel(this.lk_cErrorMessage, this.lk_cErrorFile, this.lk_cStatusMessage,
				this.lk_cConnectionManager);
		parallelUI.setModal(true);
		parallelUI.setLocationRelativeTo(vc);
		parallelUI.setVisible(true);
		return;
*/
	}

	/**
	 * Console mit Drucker verbinden.
	 *
	 * @param connectionInterface verwendete Schnittstelle
	 * @return true, wenn Verbindung hergestellt, sonst false
	 */
	private boolean connectConsole(CVInterface connectionInterface) {
		this.lk_cConnectionManager.setSink(this.lk_cConsoleInput.getWriter(), true, false);
		this.lk_cConnectionManager.setSink(this.lk_cConsoleInputHEX.getWriter(), true, true);
		this.lk_cConnectionManager.setSource(this.lk_cConsoleInput.getReader());

		this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);

		if (this.lk_cConnectionManager.connect(connectionInterface) == true) {
			this.lk_cConsoleInput.getTextArea().setEnabled(true);
			this.lk_cConsoleInput.setBinaryOutput(this.lk_cConnectionManager.getInterfaceBinaryOutput());
			return true;
		}
		return false;
	}

	/**
	 * Verbindung zwischen Console und Drucker beenden.
	 *
	 * @param connectionInterface verwendete Schnittstelle
	 * @return true, wenn Verbindung abgebaut, sonst false
	 */
	private boolean disconnectConsole(CVInterface connectionInterface) {
		this.lk_cConsoleInput.getTextArea().setEnabled(false);
		this.lk_cConsoleInput.saveHistory(lk_cConfigFile);

		return this.lk_cConnectionManager.disconnect(connectionInterface);
	}

	/**
	 * Scripting mit Drucker verbinden
	 *
	 * @param connectionInterface verwendete Schnittstelle
	 * @return true, wenn Verbindung hergestellt, sonst false
	 */
	private boolean connectScripting(CVInterface connectionInterface) {
		this.lk_cConnectionManager.setSink(this.lk_cBeanShellScriptingUI.getBeanShell().getWriter(), true, false);
		this.lk_cConnectionManager.setSink(this.lk_cBeanShellScriptingUI.getDisplayInterfaceASCIIWriter(), true, true);
		this.lk_cConnectionManager.setSink(this.lk_cBeanShellScriptingUI.getDisplayInterfaceHEXWriter(), true, true);
		this.lk_cConnectionManager.setSource(this.lk_cBeanShellScriptingUI.getBeanShell().getReader());

		this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);

//        if(this.lk_cConnectionManager.connect(connectionInterface) == true)
		if (this.lk_cConnectionManager.connectLight(connectionInterface) == true) {
			return true;
		}

		return false;
	}

	/**
	 * Verbindung zwischen Scripting und Drucker beenden.
	 *
	 * @param connectionInterface verwendete Schnittstelle
	 * @return true, wenn Verbindung abgebaut, sonst false
	 */
	private boolean disconnectScripting(CVInterface connectionInterface) {
		return this.lk_cConnectionManager.disconnect(connectionInterface);
	}

	public static void doAutoReconnect() {
		CVNetworkTCP sNetworkTCPItf = vc.lk_cConnectionManager.getTCPNetworkInterface();
		CVNetworkSettings cNetworkSettingsTCP = (CVNetworkSettings) sNetworkTCPItf.getInterfaceSettings();
		if (cNetworkSettingsTCP.getTCPAutoReconn()) {
			sNetworkTCPItf.doAutoReconnect();
			vc.bAutoReconnectRunning = true;
			vc.jButtonConnectBarDisconnect.setEnabled(true);
		}
	}

	public static void connect() {
		vc.jButtonConnectBarConnect.doClick();
	}

	/**
	 * Verbindung zum Drucker herstellen.
	 *
	 * @return true, wenn mit Drucker verbunden.
	 */
	public boolean connectPrinter() {
		if (bAutoReconnectRunning) {
			vc.lk_cConnectionManager.getTCPNetworkInterface().stopAutoReconnect();
			bAutoReconnectRunning = false;
		}

		if (this.lk_cConnectionManager.isConnected() == true) {
			// schon mit Drucker verbunden
			this.lk_cErrorMessage.write("allready connected to printer");

			return false;
		} else {
			// welche Schnittstelle wird verwendet
			String sDescrInterface = (String) this.jComboBoxConnectBarChooseInterface.getSelectedItem();
			CVInterface cSelectedInterface = null;

			if (sDescrInterface.equals((String) "TCP network")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getTCPNetworkInterface();
			} else if (sDescrInterface.equals((String) "UDP network")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getUDPNetworkInterface();
			} else if (sDescrInterface.equals((String) "serial port")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getSerialInterface();
			}
/*			
			else {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getParallelInterface();
			}
*/
			// soll Console oder Scripting verbunden werden
			int iSelTabIndex = this.jTabbedPaneMain.getSelectedIndex();
			String sSelTabTitle = this.jTabbedPaneMain.getTitleAt(iSelTabIndex);

			if (sSelTabTitle.equals(this.lk_sConsoleInputTitle) == true) {
				if (this.connectConsole(cSelectedInterface) == true) {
					// andere Tab deaktivieren
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sBeanShellScriptingTitle),
							false);

					return true;
				}
			}

			if (sSelTabTitle.equals(this.lk_sBeanShellScriptingTitle) == true) {
				if (this.connectScripting(cSelectedInterface) == true) {
					// andere Tab deaktivieren
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sConsoleInputTitle),
							false);
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sConsoleInputHEXTitle),
							false);
					return true;
				}
			}

			return false;
		}
	}

	public static void disconnect() {
		vc.jButtonConnectBarDisconnect.doClick();
	}

	/**
	 * Verbindung zum Drucker beenden.
	 *
	 * @return true, wenn Verbindung beendet.
	 */
	public boolean disconnectPrinter() {
		if (bAutoReconnectRunning) {
			vc.lk_cConnectionManager.getTCPNetworkInterface().stopAutoReconnect();
			bAutoReconnectRunning = false;
			return true;
		}

		if (this.lk_cConnectionManager.isConnected() == false) {
			// mit keinem Drucker verbunden
			this.lk_cErrorMessage.write("not connected to printer");

			return false;
		} else {
			// welche Schnittstelle wird verwendet
			String sDescrInterface = (String) this.jComboBoxConnectBarChooseInterface.getSelectedItem();
			CVInterface cSelectedInterface = null;

			if (sDescrInterface.equals((String) "TCP network")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getTCPNetworkInterface();
			} else if (sDescrInterface.equals((String) "UDP network")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getUDPNetworkInterface();
			} else if (sDescrInterface.equals((String) "serial port")) {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getSerialInterface();
			}
/*			
			else {
				cSelectedInterface = (CVInterface) this.lk_cConnectionManager.getParallelInterface();
			}
*/

			// soll Console oder Scripting beendet werden
			int cSelTabIndex = this.jTabbedPaneMain.getSelectedIndex();
			String sSelTabTitle = this.jTabbedPaneMain.getTitleAt(cSelTabIndex);

			if (sSelTabTitle.equals(this.lk_sConsoleInputTitle) == true) {
				if (this.disconnectConsole(cSelectedInterface) == true) {
					// andere Tab wieder aktivieren
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sBeanShellScriptingTitle),
							true);

					return true;
				}
			}

			if (sSelTabTitle.equals(this.lk_sBeanShellScriptingTitle) == true) {
				if (this.disconnectScripting(cSelectedInterface) == true) {
					// andere Tab wieder aktivieren
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sConsoleInputTitle),
							true);
					this.jTabbedPaneMain.setEnabledAt(this.jTabbedPaneMain.indexOfTab(this.lk_sConsoleInputHEXTitle),
							true);
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Datei zum Drucker senden
	 *
	 */
	private void transmitFile() {
		final JFileChooser fc = new JFileChooser();

		// Einlesen gewaehlter Pfad aus Konfigurationsdatei
		if (this.lk_cConfigFile != null) {
			String configValue = null;
			configValue = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPath);
			if (configValue != null) {
				fc.setCurrentDirectory(new File(configValue));
			}
		}

		int returnVal = fc.showOpenDialog(ValentinConsole.this);

		// Datei mit Auswahldialog abfragen
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File fileTransmit = fc.getSelectedFile();
			if (this.lk_cConfigFile != null) {
				String absolutePath = fileTransmit.getAbsolutePath().toString();
				sortRecentItems(absolutePath);
				sortRecentItemsPath(absolutePath, true);
				this.lk_cConfigFile.setConfig(this.lk_szConfigTokenPath, absolutePath);
			}

			CVSohEtb cStoreCVSohEtb = this.lk_cConnectionManager.getSohEtb();
			this.lk_cConnectionManager.setSohEtb(CVSohEtb.none);

			JFrame owner = this;
			new Thread(new Runnable() {
				public void run() {
					CVFileTransmit fileTransmitUI = new CVFileTransmit(lk_cErrorMessage, lk_cErrorFile,
							lk_cStatusMessage, owner);
					fileTransmitUI.setFile(fileTransmit);
					fileTransmitUI.setOutputStream(lk_cConnectionManager.getInterfaceBinaryOutput());
					fileTransmitUI.setLocationRelativeTo(owner);
					fileTransmitUI.setVisible(true);
					fileTransmitUI.startFileTransfer();
				}
			}).start();

			this.lk_cConnectionManager.setSohEtb(cStoreCVSohEtb);
			// UI Open Recent neu laden
			refeshOpenRecentItems();
		}

		return;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		// this.setBounds(0, 0, 800, 600);
		this.setJMenuBar(getJMenuBarMain());
		this.setContentPane(getJPanelMain());
		this.setTitle("VETO - Valentin Embedded Test Office 1.1");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		this.pack();
	}

	/**
	 * This method initializes jTabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPaneMain() {
		if (jTabbedPaneMain == null) {
			jTabbedPaneMain = new JTabbedPane();
			jTabbedPaneMain.addTab(this.lk_sConsoleInputTitle, null, getJScrollPaneConsole(), null);
			jTabbedPaneMain.addTab(this.lk_sConsoleInputHEXTitle, null, this.lk_cConsoleInputHEX.getTextAreaPane(),
					null);
			jTabbedPaneMain.addTab(this.lk_sBeanShellScriptingTitle, null, this.lk_cBeanShellScriptingUI, null);
		}
		return jTabbedPaneMain;
	}

	/**
	 * This method initializes jPanelMain
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelMain() {
		if (jPanelMain == null) {
			jPanelMain = new javax.swing.JPanel();
			jPanelMain.setLayout(new java.awt.BorderLayout());
			jPanelMain.add(getJPanelConnectBar(), BorderLayout.NORTH);
			jPanelMain.add(getJTabbedPaneMain(), java.awt.BorderLayout.CENTER);
			jPanelMain.add(lk_cStatusMessage, BorderLayout.SOUTH);
		}
		return jPanelMain;
	}

	/**
	 * This method initializes jScrollPaneConsole
	 *
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPaneConsole() {
		if (jScrollPaneConsole == null) {
			jScrollPaneConsole = new javax.swing.JScrollPane();
			jScrollPaneConsole.setViewportView(lk_cConsoleInput.getTextArea());
			lk_cConsoleInput.getTextArea().setEnabled(false);
			lk_cConsoleInput.getTextArea().setRows(40);
		}

		return jScrollPaneConsole;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelConnectBar() {
		if (jPanelConnectBar == null) {
			jLabelConnectBarEncoding = new JLabel();
			jLabelConnectBarInterface = new JLabel();
			FlowLayout ConnectBarFlowLayout = new FlowLayout();
			jPanelConnectBar = new JPanel();
			jPanelConnectBar.setLayout(ConnectBarFlowLayout);
			//jPanelConnectBar.setPreferredSize(new java.awt.Dimension(20, 36));
			ConnectBarFlowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			jLabelConnectBarInterface.setText("Interface:");
			jLabelConnectBarInterface.setToolTipText("choose interface for connection");
			//jLabelConnectBarInterface.setPreferredSize(new java.awt.Dimension(100, 26));
			//jLabelConnectBarInterface.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			//jLabelConnectBarInterface.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelConnectBarEncoding.setText("Encoding:");
			// jLabelConnectBarEncoding.setPreferredSize(new java.awt.Dimension(100, 26));
			jLabelConnectBarEncoding.setToolTipText("encoding CVPL");
			//jLabelConnectBarEncoding.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			//jLabelConnectBarEncoding.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			jPanelConnectBar.add(jLabelConnectBarInterface, null);
			jPanelConnectBar.add(getJComboBoxConnectBarChooseInterface(), null);
			jPanelConnectBar.add(getJButtonConnectBarConfigInterface(), null);
			jPanelConnectBar.add(new JLabel("    "));
			jPanelConnectBar.add(jLabelConnectBarEncoding, null);
			jPanelConnectBar.add(getJComboBoxConnectBarChooseEncoding(), null);
			jPanelConnectBar.add(new JLabel("    "));
			jPanelConnectBar.add(getJButtonConnectBarConnect(), null);
			jPanelConnectBar.add(getJButtonConnectBarDisconnect(), null);
		}
		return jPanelConnectBar;
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxConnectBarChooseInterface() {
		if (jComboBoxConnectBarChooseInterface == null) {
			jComboBoxConnectBarChooseInterface = new JComboBox();
			// jComboBoxConnectBarChooseInterface.setPreferredSize(new java.awt.Dimension(100, 26));
			jComboBoxConnectBarChooseInterface.setToolTipText("choose interface for connection");
			jComboBoxConnectBarChooseInterface.setBackground(Color.white);

			// Einlesen aller Schnittstellen
			jComboBoxConnectBarChooseInterface.addItem((String) "serial port");
			jComboBoxConnectBarChooseInterface.addItem((String) "parallel port");
			jComboBoxConnectBarChooseInterface.addItem((String) "TCP network");
			jComboBoxConnectBarChooseInterface.addItem((String) "UDP network");

			jComboBoxConnectBarChooseInterface.setSelectedItem((String) "serial port");

			// Einlesen gewaehlte Schnittstelle aus Konfigurationsdatei
			if (lk_cConfigFile != null) {
				String configValue = null;
				configValue = lk_cConfigFile.getConfig(lk_szConfigTokenInterface);
				if (configValue != null) {
					jComboBoxConnectBarChooseInterface.setSelectedItem(configValue);
				}
			}

			jComboBoxConnectBarChooseInterface.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					// Speichern gewaehlte Schnittstelle in Konfigurationsdatei
					if (lk_cConfigFile != null) {
						lk_cConfigFile.setConfig(lk_szConfigTokenInterface,
								(String) jComboBoxConnectBarChooseInterface.getSelectedItem());
					}
				}
			});
		}
		return jComboBoxConnectBarChooseInterface;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonConnectBarConfigInterface() {
		if (jButtonConnectBarConfigInterface == null) {
			jButtonConnectBarConfigInterface = new JButton();
			// jButtonConnectBarConfigInterface.setPreferredSize(new java.awt.Dimension(100, 26));
			jButtonConnectBarConfigInterface.setText("Configure");
			jButtonConnectBarConfigInterface.setToolTipText("configure interface");
			jButtonConnectBarConfigInterface.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String currentInterface = (String) jComboBoxConnectBarChooseInterface.getSelectedItem();
					if (currentInterface.equals((String) "TCP network") == true) {
						// TCP network
						showUINetworkTCPConfig();
					} else if (currentInterface.equals((String) "UDP network") == true) {
						// UDP network
						showUINetworkUDPConfig(); // TODO Searchbutton einbauen
					} else if (currentInterface.equals((String) "serial port") == true) {
						// serial port
						showUISerialPortConfig();
					} else {
						// parallel port
						showUIParallelPortConfig();
					}
				}
			});
		}
		return jButtonConnectBarConfigInterface;
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxConnectBarChooseEncoding() {
		if (jComboBoxConnectBarChooseEncoding == null) {
			jComboBoxConnectBarChooseEncoding = new JComboBox();
			// jComboBoxConnectBarChooseEncoding.setPreferredSize(new java.awt.Dimension(100, 26));
			jComboBoxConnectBarChooseEncoding.setBackground(Color.white);
			jComboBoxConnectBarChooseEncoding.setToolTipText("encoding CVPL");

//          Einlesen aller Enkodierungen
			for (Enumeration encodingEnum = CVSohEtb.elements(); encodingEnum.hasMoreElements();) {
				CVSohEtb encodingCurrent = (CVSohEtb) encodingEnum.nextElement();
				jComboBoxConnectBarChooseEncoding.addItem((String) encodingCurrent.toString());
				jComboBoxConnectBarChooseEncoding.setSelectedItem((String) encodingCurrent.toString());
			}

			// Einlesen gewaehlte Enkodierung aus Konfigurationsdatei
			if (lk_cConfigFile != null) {
				String configValue = null;
				configValue = lk_cConfigFile.getConfig(lk_szConfigTokenSohEtb);
				if (configValue != null) {
					jComboBoxConnectBarChooseEncoding.setSelectedItem(configValue);
					lk_cSohEtb = CVSohEtb.fromString(configValue);
					this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);
				} else {
					jComboBoxConnectBarChooseEncoding.setSelectedIndex(0);
				}
			} else {
				jComboBoxConnectBarChooseEncoding.setSelectedIndex(0);
			}

			jComboBoxConnectBarChooseEncoding.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					lk_cSohEtb = CVSohEtb.fromString((String) jComboBoxConnectBarChooseEncoding.getSelectedItem());
					lk_cConnectionManager.setSohEtb(lk_cSohEtb);

					// Speichern gewaehlte Enkodierung in Konfigurationsdatei
					if (lk_cConfigFile != null) {
						lk_cConfigFile.setConfig(lk_szConfigTokenSohEtb,
								(String) jComboBoxConnectBarChooseEncoding.getSelectedItem());
					}
				}
			});
		}
		return jComboBoxConnectBarChooseEncoding;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonConnectBarConnect() {
		if (jButtonConnectBarConnect == null) {
			jButtonConnectBarConnect = new JButton();
			// jButtonConnectBarConnect.setPreferredSize(new java.awt.Dimension(100, 26));
			jButtonConnectBarConnect.setText("Connect");
			jButtonConnectBarConnect.setToolTipText("connect to printer");
			jButtonConnectBarConnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (connectPrinter() == true) {
						jButtonConnectBarConnect.setEnabled(false);
						jButtonConnectBarDisconnect.setEnabled(true);

						jComboBoxConnectBarChooseEncoding.setEnabled(false);
						jButtonConnectBarConfigInterface.setEnabled(false);
						jComboBoxConnectBarChooseInterface.setEnabled(false);
						jMenuItemConfigureNetworkTCP.setEnabled(false);
						jMenuItemConfigureNetworkUDP.setEnabled(false);
						jMenuItemConfigureRS232.setEnabled(false);
						jMenuItemConfigureParallel.setEnabled(false);

						jMenuItemTransmitFile.setEnabled(true);
						jMenuFileSubmenuRecentFile.setEnabled(true);
						jMenuFileSubmenuRecentPath.setEnabled(true);
						refeshOpenRecentItems();
					}
				}
			});
		}
		return jButtonConnectBarConnect;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonConnectBarDisconnect() {
		if (jButtonConnectBarDisconnect == null) {
			jButtonConnectBarDisconnect = new JButton();
			// jButtonConnectBarDisconnect.setPreferredSize(new java.awt.Dimension(100, 26));
			jButtonConnectBarDisconnect.setText("Disconnect");
			jButtonConnectBarDisconnect.setToolTipText("disconnect printer");
			jButtonConnectBarDisconnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (disconnectPrinter() == true) {
						jButtonConnectBarConnect.setEnabled(true);
						jButtonConnectBarDisconnect.setEnabled(false);

						jComboBoxConnectBarChooseEncoding.setEnabled(true);
						jButtonConnectBarConfigInterface.setEnabled(true);
						jComboBoxConnectBarChooseInterface.setEnabled(true);
						jMenuItemConfigureNetworkTCP.setEnabled(true);
						jMenuItemConfigureNetworkUDP.setEnabled(true);
						jMenuItemConfigureRS232.setEnabled(true);
						jMenuItemConfigureParallel.setEnabled(true);

						jMenuItemTransmitFile.setEnabled(false);
						jMenuFileSubmenuRecentFile.setEnabled(false);
						jMenuFileSubmenuRecentPath.setEnabled(false);
					}
				}
			});
		}
		return jButtonConnectBarDisconnect;
	}

	/**
	 * This method initializes jJMenuBar
	 *
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJMenuBarMain() {
		jMenuBarMain = new JMenuBar();
		jMenuBarMain.setName("");
		jMenuBarMain.setPreferredSize(new java.awt.Dimension(0, 30));
		jMenuBarMain.add(getJMenuFile());
		jMenuBarMain.add(getJMenuInterface());
		jMenuBarMain.add(getJMenuConsole());
		jMenuBarMain.add(getCommandsSelection());
		jMenuBarMain.add(getScriptSelection());
		jMenuBarMain.add(getJMenuInfo());
		return jMenuBarMain;
	}

	/**
	 * This method initializes jMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuInfo() {
		if (jMenuInfo == null) {
			jMenuInfo = new JMenu();
			jMenuInfo.setText("Info");
			jMenuInfo.setMnemonic(java.awt.event.KeyEvent.VK_F);
			jMenuInfo.add(getJMenuItemInfo());
			jMenuInfo.add(getJMenuItemLog());
		}
		return jMenuInfo;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemInfo() {
		if (jMenuItemInfo == null) {
			jMenuItemInfo = new JMenuItem();
			jMenuItemInfo.setText("Info");
			jMenuItemInfo.setEnabled(true);
			jMenuItemInfo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JOptionPane.showMessageDialog(vc, "Valentin Embedded Test Office\nVersion 1.1", "Info",
							JOptionPane.OK_OPTION);
				}
			});
		}
		return jMenuItemInfo;
	}

   /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemLog() {
        if (jMenuItemLog == null) {
            jMenuItemLog = new JMenuItem();
            jMenuItemLog.setText("Show Log");
            jMenuItemLog.setEnabled(true);
            jMenuItemLog.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String strLog = null;
                    
                    try {
                        FileReader readTextFile=new FileReader(
                                System.getProperty("java.io.tmpdir", ".") + "/VetoErrorLog.txt");
                        Scanner fileReaderScan = new Scanner(readTextFile);
                        strLog = "";
                        while (fileReaderScan.hasNextLine())
                        {
                            String temp = fileReaderScan.nextLine()+"\n";                        
                            strLog = strLog + temp;
                        }
                        fileReaderScan.close();
                    }
                    catch (IOException ex) {
                        if (lk_cErrorMessage != null)
                        {
                            lk_cErrorMessage.write("GetLog: " + ex.getMessage());
                        }
                        if (lk_cErrorFile != null)
                        {
                            lk_cErrorFile.write("GetLog: " + ex.getMessage());
                        }
                    }                                                            
                    
                    JDialog dialog = new JDialog(vc);                   
                    JTextArea textArea = new JTextArea(strLog);
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    JLabel jLabel =  new JLabel(
                            System.getProperty("java.io.tmpdir", ".") + "/VetoErrorLog.txt");
                    dialog.add(jLabel, BorderLayout.NORTH);
                    dialog.add(scrollPane, BorderLayout.CENTER);
                    dialog.pack();
                    dialog.setModal(true);
                    dialog.setLocationRelativeTo(vc);
                    dialog.setVisible(true);
                }
            });
        }
        return jMenuItemLog;
    }
	
	/**
	 * This method initializes jMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setName("");
			jMenuFile.setText("File");
			jMenuFile.setMnemonic(java.awt.event.KeyEvent.VK_F);
			jMenuFile.add(getJMenuItemTransmitFile());
			jMenuFileSubmenuRecentFile = new JMenu();
			jMenuFileSubmenuRecentFile.setText("Open Recent File");
			jMenuFileSubmenuRecentFile.setEnabled(false);
			jMenuFileSubmenuRecentFile.add(getJMenuItemOpenRecent0());
			jMenuFile.add(jMenuFileSubmenuRecentFile);
			jMenuFile.add(getJMenuItemUpdate());
			jMenuFileSubmenuRecentPath = new JMenu();
			jMenuFileSubmenuRecentPath.setText("Open Recent Path");
			jMenuFileSubmenuRecentPath.setEnabled(false);
			jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath0());
			jMenuFile.add(jMenuFileSubmenuRecentPath);
		}
		return jMenuFile;
	}

	private JMenuItem getJMenuItemUpdate() {
		if (jMenuItemUpdate == null) {
			jMenuItemUpdate = new JMenuItem();
			jMenuItemUpdate.setText("Transmit Update");
			jMenuItemUpdate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (lk_cConfigFile != null) {
						ValentinUpdater updater = new ValentinUpdater(lk_cConfigFile, null);
					} else {
						ValentinUpdater updater = new ValentinUpdater();
					}
				}
			});
		}
		return jMenuItemUpdate;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemTransmitFile() {
		if (jMenuItemTransmitFile == null) {
			jMenuItemTransmitFile = new JMenuItem();
			jMenuItemTransmitFile.setText("Transmit File");
			jMenuItemTransmitFile.setToolTipText("transmit file to printer");
			jMenuItemTransmitFile.setEnabled(false);
			jMenuItemTransmitFile.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					transmitFile();
				}
			});
		}
		return jMenuItemTransmitFile;
	}

	private JMenuItem getJMenuItemOpenRecentPath0() {
		if (this.lk_cConfigFile != null) {
			String filePath = null;
			filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0);
			if (filePath != null && !filePath.equals("null") && !filePath.equals("")) {
				if (jMenuItemOpenRecentPath0 == null) {
					jMenuItemOpenRecentPath0 = new JMenuItem();
				}
				jMenuItemOpenRecentPath0.setText(filePath);
				jMenuItemOpenRecentPath0.setEnabled(true);
			} else {
				if (jMenuItemOpenRecentPath0 == null) {
					jMenuItemOpenRecentPath0 = new JMenuItem();
					jMenuItemOpenRecentPath0.setText("No recently opened paths");
					jMenuItemOpenRecentPath0.setEnabled(false);
				}
			}
		} else {
			if (jMenuItemOpenRecentPath0 == null) {
				jMenuItemOpenRecentPath0 = new JMenuItem();
				jMenuItemOpenRecentPath0.setText("No recently opened paths");
				jMenuItemOpenRecentPath0.setEnabled(false);
			}
		}
		jMenuItemOpenRecentPath0.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentPath(0);
			}
		});
		return jMenuItemOpenRecentPath0;
	}

	private JMenuItem getJMenuItemOpenRecentPath1() {
		String filePath = null;
		filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1);
		if (jMenuItemOpenRecentPath1 == null) {
			jMenuItemOpenRecentPath1 = new JMenuItem();
		}
		jMenuItemOpenRecentPath1.setText(filePath);
		jMenuItemOpenRecentPath1.setEnabled(true);
		jMenuItemOpenRecentPath1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentPath(1);
			}
		});
		return jMenuItemOpenRecentPath1;
	}

	private JMenuItem getJMenuItemOpenRecentPath2() {
		String filePath = null;
		filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2);
		if (jMenuItemOpenRecentPath2 == null) {
			jMenuItemOpenRecentPath2 = new JMenuItem();
		}
		jMenuItemOpenRecentPath2.setText(filePath);
		jMenuItemOpenRecentPath2.setEnabled(true);
		jMenuItemOpenRecentPath2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentPath(2);
			}
		});
		return jMenuItemOpenRecentPath2;
	}

	private JMenuItem getJMenuItemOpenRecentPath3() {
		String filePath = null;
		filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3);
		if (jMenuItemOpenRecentPath3 == null) {
			jMenuItemOpenRecentPath3 = new JMenuItem();
		}
		jMenuItemOpenRecentPath3.setText(filePath);
		jMenuItemOpenRecentPath3.setEnabled(true);
		jMenuItemOpenRecentPath3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentPath(3);
			}
		});
		return jMenuItemOpenRecentPath3;
	}

	private JMenuItem getJMenuItemOpenRecentPath4() {
		String filePath = null;
		filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent4);
		if (jMenuItemOpenRecentPath4 == null) {
			jMenuItemOpenRecentPath4 = new JMenuItem();
		}
		jMenuItemOpenRecentPath4.setText(filePath);
		jMenuItemOpenRecentPath4.setEnabled(true);
		jMenuItemOpenRecentPath4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentPath(4);
			}
		});
		return jMenuItemOpenRecentPath4;
	}

	private void openRecentPath(int i) {
		String filePath = null;
		String lk_szConfigTokenPathRecent = "ValentinConsoleSettingsPathRecent";
		filePath = this.lk_cConfigFile.getConfig(lk_szConfigTokenPathRecent + i);

		this.lk_cConfigFile.setConfig(lk_szConfigTokenPath, filePath);
		sortRecentItemsPath(filePath, false);
		transmitFile();
		if (jMenuItemOpenRecentPath0 != null) {
			jMenuFileSubmenuRecentPath.remove(jMenuItemOpenRecentPath0);
			jMenuItemOpenRecentPath0 = null;
		}
		if (jMenuItemOpenRecentPath1 != null) {
			jMenuFileSubmenuRecentPath.remove(jMenuItemOpenRecentPath1);
			jMenuItemOpenRecentPath1 = null;
		}
		if (jMenuItemOpenRecentPath2 != null) {
			jMenuFileSubmenuRecentPath.remove(jMenuItemOpenRecentPath2);
			jMenuItemOpenRecentPath2 = null;
		}
		if (jMenuItemOpenRecentPath3 != null) {
			jMenuFileSubmenuRecentPath.remove(jMenuItemOpenRecentPath3);
			jMenuItemOpenRecentPath3 = null;
		}
		if (jMenuItemOpenRecentPath4 != null) {
			jMenuFileSubmenuRecentPath.remove(jMenuItemOpenRecentPath4);
			jMenuItemOpenRecentPath4 = null;
		}
		refeshOpenRecentItems();
	}

	private void sortRecentItemsPath(String path, Boolean cutPath) {
		if (cutPath) {
			if (path.contains("\\")) {
				path = path.substring(0, path.lastIndexOf('\\'));
			}
		}
		if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent4).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0).toString())) {
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		} else {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenPathRecent0, path);
		}
	}

	/**
	 * This method refreshes the recently opened files
	 */
	private void refeshOpenRecentItems() {

		String filePath = null;
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0).toString().equals("null")) {
			if (jMenuItemOpenRecentPath0 == null) {
				jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath0());
			}
			filePath = lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent0);
			jMenuItemOpenRecentPath0.setText("1: " + filePath);
			jMenuItemOpenRecentPath0.setEnabled(true);
		}

		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1).toString().equals("null")) {
			if (jMenuItemOpenRecentPath1 == null) {
				jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath1());
			}
			filePath = lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent1);
			jMenuItemOpenRecentPath1.setText("2: " + filePath);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2).toString().equals("null")) {
			if (jMenuItemOpenRecentPath2 == null) {
				jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath2());
			}
			filePath = lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent2);
			jMenuItemOpenRecentPath2.setText("3: " + filePath);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3).toString().equals("null")) {
			if (jMenuItemOpenRecentPath3 == null) {
				jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath3());
			}
			filePath = lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent3);
			jMenuItemOpenRecentPath3.setText("4: " + filePath);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent4).toString().equals("null")) {
			if (jMenuItemOpenRecentPath4 == null) {
				jMenuFileSubmenuRecentPath.add(getJMenuItemOpenRecentPath4());
			}
			filePath = lk_cConfigFile.getConfig(this.lk_szConfigTokenPathRecent4);
			jMenuItemOpenRecentPath4.setText("5: " + filePath);
		}

		String fileName = null;
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0).toString().equals("null")) {
			fileName = lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0);
			jMenuItemOpenRecent0.setText("1: " + fileName);
			jMenuItemOpenRecent0.setEnabled(true);
		}

		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1).toString().equals("null")) {
			if (jMenuItemOpenRecent1 == null) {
				jMenuFileSubmenuRecentFile.add(getJMenuItemOpenRecent1());
			}
			fileName = lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1);
			jMenuItemOpenRecent1.setText("2: " + fileName);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2).toString().equals("null")) {
			if (jMenuItemOpenRecent2 == null) {
				jMenuFileSubmenuRecentFile.add(getJMenuItemOpenRecent2());
			}
			fileName = lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2);
			jMenuItemOpenRecent2.setText("3: " + fileName);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent3).toString().equals("null")) {
			if (jMenuItemOpenRecent3 == null) {
				jMenuFileSubmenuRecentFile.add(getJMenuItemOpenRecent3());
			}
			fileName = lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent3);
			jMenuItemOpenRecent3.setText("4: " + fileName);
		}
		if (!this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent4).toString().equals("null")) {
			if (jMenuItemOpenRecent4 == null) {
				jMenuFileSubmenuRecentFile.add(getJMenuItemOpenRecent4());
			}
			fileName = lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent4);
			jMenuItemOpenRecent4.setText("5: " + fileName);
		}
	}

	private void sortRecentItems(String path) {
		File fileTransmit = new File(path);
		if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent4).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent3));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1).toString())) {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		} else if (path.equals(this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0).toString())) {
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		} else {
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent4,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent3));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent3,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent2,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1));
			}
			if (this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0) != null) {
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0));
				lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent1,
						this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0));
			}
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFilePathRecent0, fileTransmit.getAbsolutePath());
			lk_cConfigFile.setConfig(this.lk_szConfigTokenFileNameRecent0, fileTransmit.getName());
		}
	}

	private void openRecentFile(int i) {
		String filePath = null;
		String fileName = null;

		String lk_szConfigTokenFilePathRecent = "ValentinConsoleSettingsFilePathRecent";
		String lk_szConfigTokenFileNameRecent = "ValentinConsoleSettingsFileNameRecent";
		filePath = this.lk_cConfigFile.getConfig(lk_szConfigTokenFilePathRecent + i);
		fileName = this.lk_cConfigFile.getConfig(lk_szConfigTokenFileNameRecent + i);

		this.lk_cConfigFile.setConfig(lk_szConfigTokenPath, filePath);

		File fileTransmit = new File(filePath);

		sortRecentItems(filePath);

		CVSohEtb cStoreCVSohEtb = this.lk_cConnectionManager.getSohEtb();

		this.lk_cConnectionManager.setSohEtb(CVSohEtb.none);
		JFrame owner = this;
		new Thread(new Runnable() {
			public void run() {
				CVFileTransmit fileTransmitUI = new CVFileTransmit(lk_cErrorMessage, lk_cErrorFile, lk_cStatusMessage, owner);
				fileTransmitUI.setFile(fileTransmit);
				fileTransmitUI.setOutputStream(lk_cConnectionManager.getInterfaceBinaryOutput());
				fileTransmitUI.setVisible(true);
				fileTransmitUI.startFileTransfer();
			}
		}).start();

		this.lk_cConnectionManager.setSohEtb(cStoreCVSohEtb);
		refeshOpenRecentItems();
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */

	private JMenuItem getJMenuItemOpenRecent0() {
		if (this.lk_cConfigFile != null) {
			String fileName = null;
			fileName = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent0);
			String filePath = null;
			filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent0);
			if (fileName != null && filePath != null) {
				if (jMenuItemOpenRecent0 == null) {
					jMenuItemOpenRecent0 = new JMenuItem();
				}
				jMenuItemOpenRecent0.setText("1: " + fileName);
				jMenuItemOpenRecent0.setEnabled(true);

			} else {
				if (jMenuItemOpenRecent0 == null) {
					jMenuItemOpenRecent0 = new JMenuItem();
					jMenuItemOpenRecent0.setText("No recently opened files");
					jMenuItemOpenRecent0.setEnabled(false);
				}
			}
		} else {
			if (jMenuItemOpenRecent0 == null) {
				jMenuItemOpenRecent0 = new JMenuItem();
				jMenuItemOpenRecent0.setText("No recently opened files");
				jMenuItemOpenRecent0.setEnabled(false);
			}
		}
		jMenuItemOpenRecent0.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentFile(0);
			}
		});
		return jMenuItemOpenRecent0;
	}

	private JMenuItem getJMenuItemOpenRecent1() {
		String fileName = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent1);
		String filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent1);
		if (fileName != null && filePath != null) {
			if (jMenuItemOpenRecent1 == null) {
				jMenuItemOpenRecent1 = new JMenuItem();
			}
			jMenuItemOpenRecent1.setText("2: " + fileName);
			jMenuItemOpenRecent1.setEnabled(true);
		}
		jMenuItemOpenRecent1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentFile(1);
			}
		});
		return jMenuItemOpenRecent1;
	}

	private JMenuItem getJMenuItemOpenRecent2() {
		String fileName = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent2);
		String filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent2);
		if (fileName != null && filePath != null) {
			if (jMenuItemOpenRecent2 == null) {
				jMenuItemOpenRecent2 = new JMenuItem();
			}
			jMenuItemOpenRecent2.setText("3: " + fileName);
			jMenuItemOpenRecent2.setEnabled(true);
		}
		jMenuItemOpenRecent2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentFile(2);
			}
		});
		return jMenuItemOpenRecent2;
	}

	private JMenuItem getJMenuItemOpenRecent3() {
		String fileName = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent3);
		String filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent3);
		if (fileName != null && filePath != null) {
			if (jMenuItemOpenRecent3 == null) {
				jMenuItemOpenRecent3 = new JMenuItem();
			}
			jMenuItemOpenRecent3.setText("4: " + fileName);
			jMenuItemOpenRecent3.setEnabled(true);
		}
		jMenuItemOpenRecent3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentFile(3);
			}
		});
		return jMenuItemOpenRecent3;
	}

	private JMenuItem getJMenuItemOpenRecent4() {
		String fileName = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFileNameRecent4);
		String filePath = this.lk_cConfigFile.getConfig(this.lk_szConfigTokenFilePathRecent4);
		if (fileName != null && filePath != null) {
			if (jMenuItemOpenRecent4 == null) {
				jMenuItemOpenRecent4 = new JMenuItem();
			}
			jMenuItemOpenRecent4.setText("5: " + fileName);
			jMenuItemOpenRecent4.setEnabled(true);
		}
		jMenuItemOpenRecent4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openRecentFile(4);
			}
		});
		return jMenuItemOpenRecent4;
	}

	/**
	 * This method initializes jMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuInterface() {
		if (jMenuInterface == null) {
			jMenuInterface = new JMenu();
			jMenuInterface.setText("Interface");
			jMenuInterface.setToolTipText("configure interfaces");
			jMenuInterface.setMnemonic(java.awt.event.KeyEvent.VK_I);
			jMenuInterface.add(getJMenuItemConfigureNetworkTCP());
			jMenuInterface.add(getJMenuItemConfigureNetworkUDP());
			jMenuInterface.add(getJMenuItemConfigureRS232());
			jMenuInterface.add(getJMenuItemConfigureParallel());
		}
		return jMenuInterface;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemConfigureNetworkTCP() {
		if (jMenuItemConfigureNetworkTCP == null) {
			jMenuItemConfigureNetworkTCP = new JMenuItem();
			jMenuItemConfigureNetworkTCP.setText("Configure Network TCP");
			jMenuItemConfigureNetworkTCP.setToolTipText("configure TCP network interface");
			jMenuItemConfigureNetworkTCP.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showUINetworkTCPConfig();
				}
			});
		}
		return jMenuItemConfigureNetworkTCP;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemConfigureNetworkUDP() {
		if (jMenuItemConfigureNetworkUDP == null) {
			jMenuItemConfigureNetworkUDP = new JMenuItem();
			jMenuItemConfigureNetworkUDP.setText("Configure Network UDP");
			jMenuItemConfigureNetworkUDP.setToolTipText("configure UDP network interface");
			jMenuItemConfigureNetworkUDP.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showUINetworkUDPConfig();
				}
			});
		}
		return jMenuItemConfigureNetworkUDP;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemConfigureRS232() {
		if (jMenuItemConfigureRS232 == null) {
			jMenuItemConfigureRS232 = new JMenuItem();
			jMenuItemConfigureRS232.setText("Configure RS232");
			jMenuItemConfigureRS232.setToolTipText("configure serial ports");
			jMenuItemConfigureRS232.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showUISerialPortConfig();
				}
			});
		}
		return jMenuItemConfigureRS232;
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemConfigureParallel() {
		if (jMenuItemConfigureParallel == null) {
			jMenuItemConfigureParallel = new JMenuItem();
			jMenuItemConfigureParallel.setText("Configure Parallel");
			jMenuItemConfigureParallel.setToolTipText("configure parallel ports");
			jMenuItemConfigureParallel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showUIParallelPortConfig();
				}
			});
		}
		return jMenuItemConfigureParallel;
	}

	/**
	 * This method initializes jMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuConsole() {
		if (jMenuConsole == null) {
			jMenuConsole = new JMenu();
			jMenuConsole.setText("Console");
			jMenuConsole.setMnemonic(java.awt.event.KeyEvent.VK_C);
			jMenuConsole.add(getJMenuItemClearConsole());
		}
		return jMenuConsole;
	}

	/**
	 * This method initializes jMenu for Scripts in set Folder
	 * gets Folder path from cfg file and list all Scripts in this folder
	 * on click run this script
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getScriptSelection() {
		jMenuScriptSelection = new JMenu();
		jMenuScriptSelection.setText("Scripts");
		jMenuScriptSelection.setMnemonic(java.awt.event.KeyEvent.VK_S);
		String path = null;
		if (lk_cConfigFile != null) {
			path = lk_cConfigFile.getConfig("ScriptFolder");
		}
		if (path != null) {
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".bsh"));
			if (listOfFiles != null) {
				if (listOfFiles.length > 0) {
					for (int i = 0; i < listOfFiles.length; i++) {
						JMenuItem jMenuItem = new JMenuItem();
						jMenuItem.setText(listOfFiles[i].getName());
						int j = i;
						jMenuItem.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(java.awt.event.ActionEvent e) {
								lk_cBeanShellScriptingUI.loadScript(listOfFiles[j].getAbsolutePath());
								lk_cBeanShellScriptingUI.processButtonRunScript(false);
							}
						});
						jMenuScriptSelection.add(jMenuItem);
					}
				}
			} else {
				JMenuItem jMenuItemNoItem = new JMenuItem();
				jMenuItemNoItem.setText("No Scripts");
				jMenuItemNoItem.setEnabled(false);
				jMenuScriptSelection.add(jMenuItemNoItem);
			}
		}
		JMenuItem jMenuItemNoItem = new JMenuItem();
		jMenuItemNoItem.setText("Change Path");
		jMenuItemNoItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JDialog skripPathFrame = new JDialog(vc);
				skripPathFrame.setVisible(true);
				skripPathFrame.setLayout(new FlowLayout());
				JLabel lbChangePath = new JLabel("Set Path to:");
				JTextField tfNewPath = new JTextField(15);
				JButton btSearchPath = new JButton("Open");
				btSearchPath.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int returnVal = chooser.showOpenDialog(vc);	
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							tfNewPath.setText(chooser.getSelectedFile().toString());
						}
					}
				});
				JButton btSetPath = new JButton("Set Path");
				btSetPath.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {				    
						if (!tfNewPath.getText().trim().isEmpty()) {
							lk_cConfigFile.setConfig("ScriptFolder", tfNewPath.getText().trim());
							vc.setJMenuBar(getJMenuBarMain());
							vc.validate();
							vc.repaint();
							skripPathFrame.dispose();
						}
					}
				});
				skripPathFrame.add(lbChangePath);
				skripPathFrame.add(tfNewPath);
				skripPathFrame.add(btSearchPath);
				skripPathFrame.add(btSetPath);
				skripPathFrame.pack();
                //skripPathFrame.setModal(true);
                skripPathFrame.setLocationRelativeTo(vc);
			}
		});
		jMenuScriptSelection.add(jMenuItemNoItem);

		return jMenuScriptSelection;
	}

	private JMenuItem getJMenuItemCmd(String cmd, String desc) {
		JMenuItem jMenuItemCmd = new JMenuItem();
		jMenuItemCmd.setText(cmd + " - " + desc);
		jMenuItemCmd.setToolTipText(desc);
		jMenuItemCmd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				System.out.println(cmd);
				lk_cConsoleInput.getTextArea().append(cmd);
			}
		});
		return jMenuItemCmd;
	}

	List commandFileListCmd = null;
	List commandFileListDescr = null;

	private JMenu getCommandsSelection() {
		if (jMenuCommandSelection == null) {
			jMenuCommandSelection = new JMenu();
			jMenuCommandSelection.setText("Commands");
			jMenuCommandSelection.setMnemonic(java.awt.event.KeyEvent.VK_O);
			getJMenuItemsCommandSelection();
			Object cmdElements[] = commandFileListCmd.toArray();
			Object descrElements[] = commandFileListDescr.toArray();
			if (commandFileListCmd.size() > 0) {
				for (int i = 0; i < commandFileListCmd.size(); i++) {
					jMenuCommandSelection.add(getJMenuItemCmd(cmdElements[i].toString(), descrElements[i].toString()));
				}
			} else {
				JMenuItem jMenuItemNoItem = new JMenuItem();
				jMenuItemNoItem.setText("No Favorites");
				jMenuItemNoItem.setEnabled(false);
				jMenuCommandSelection.add(jMenuItemNoItem);
			}
		}
		return jMenuCommandSelection;
	}

	private void getJMenuItemsCommandSelection() {
		commandFileListCmd = lk_cCommandFile.getCompleteConfigCmd();
		commandFileListDescr = lk_cCommandFile.getCompleteConfigDescr();
	}

	/**
	 * This method initializes jMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemClearConsole() {
		if (jMenuItemClearConsole == null) {
			jMenuItemClearConsole = new JMenuItem();
			jMenuItemClearConsole.setText("Clear");
			jMenuItemClearConsole.setToolTipText("clear console");
			jMenuItemClearConsole.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					lk_cConsoleInput.getTextArea().setText("");
					lk_cConsoleInputHEX.getTextArea().setText("");
				}
			});
		}
		return jMenuItemClearConsole;
	}
} // @jve:visual-info decl-index=0 visual-constraint="10,10"
