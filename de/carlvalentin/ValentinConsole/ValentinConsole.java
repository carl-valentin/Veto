package de.carlvalentin.ValentinConsole;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.Interface.UI.*;
import de.carlvalentin.Protocol.*;
import de.carlvalentin.Protocol.UI.*;
import de.carlvalentin.Scripting.UI.*;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JMenu;
/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung um im
 * Carl Valentin Printer Language-Format (CVPL) mit einem Device
 * kommunizieren zu k&ouml;nnen.
 */
public class ValentinConsole extends JFrame {

    /**
     * Ausgabe von Fehlermeldungen in Form von Dialogen
     */
    private CVErrorMessage       lk_cErrorMessage = null;

    /**
     * Ausgabe von Fehlermeldungen in Logdatei
     */
    private CVLogging            lk_cErrorFile = null;

    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine         lk_cStatusMessage = null;

    private CVUINetwork          lk_cTCPNetworkUI;

    /**
     * Verwaltet die Verbindungen zum Drucker
     */
    private CVConnectionManager  lk_cConnectionManager = null;

    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    private CVConfigFile         lk_cConfigFile = null;

    /**
     * Einstellung Start-/Stopzeichen CVPL
     */
    private CVSohEtb             lk_cSohEtb = null;
    /**
     * Toekn zum Speichern der gewaehlten Zeichen in Konfigurationsdatei
     */
    private final String         lk_szConfigTokenSohEtb =
        "ValentinConsoleSettingsSohEtb";

    /**
     * Token zum Speichern der gewaehlten Schnittstelle in Konfigurationsdatei
     */
    private final String         lk_szConfigTokenInterface =
        "ValentinConsoleSettingsInterface";

    /**
     * Token zum Speichern des gewaehlten Dateipfades in Konfigurationsdatei
     */
    private final String         lk_szConfigTokenPath =
        "ValentinConsoleSettingsPath";

    /**
     * Grafische Oberflaeche fuer Einstellung Start-/Stopzeichen CVPL
     */
    private CVUISohEtb           lk_cUISohEtb = null;

    /**
     *  Konsole fuer Benutzereingaben
     */
    private CVConsole            lk_cConsoleInput = null;
    /**
     *  Titel der Konsole fuer Benutzereingaben
     */
    private String               lk_sConsoleInputTitle;

    /**
     * Gibt Inhalt der Konsole hexadezimal aus
     */
    private CVTextDisplay        lk_cConsoleInputHEX = null;
    /**
     * Titel der hexadezimalen Ausgabe der Konsolendaten
     */
    private String               lk_sConsoleInputHEXTitle;

    /**
     *  Grafische Oberflaeche fuer Skriptverarbeitung
     */
    private CVUIBeanShell        lk_cBeanShellScriptingUI = null;
    /**
     *  Titel der grafischen Oberflaeche fuer Skriptverarbeitung
     */
    private String               lk_sBeanShellScriptingTitle;

    private javax.swing.JPanel      jPanelMain         = null;
    private JTabbedPane             jTabbedPaneMain    = null;
    private javax.swing.JScrollPane jScrollPaneConsole = null;

    /**
     * Zentrale Knopfleiste zur Einstellung der Verbindung/Schnittstelle
     */
    private JPanel    jPanelConnectBar                   = null;
    private JLabel    jLabelConnectBarInterface          = null;
    private JComboBox jComboBoxConnectBarChooseInterface = null;
    private JButton   jButtonConnectBarConfigInterface   = null;
    private JLabel    jLabelConnectBarEncoding           = null;
    private JComboBox jComboBoxConnectBarChooseEncoding  = null;
    private JButton   jButtonConnectBarConnect           = null;
    private JButton   jButtonConnectBarDisconnect        = null;

    /**
     * Zentrale Men&uumlbar
     */
    private JMenuBar jMenuBarMain = null;
    /**
     * Info Men&uuml
     */
    private JMenu     jMenuInfo             = null;
    /**
     * Men&uuml zur Arbeit mit Dateien
     */
    private JMenu     jMenuFile             = null;
    private JMenuItem jMenuItemTransmitFile = null;
    private JMenuItem jMenuItemInfo         = null;
    /**
     * Men&uuml zur Konfiguration der verschiedenen Schnittstellen
     */
    private JMenu     jMenuInterface                = null;
    private JMenuItem jMenuItemConfigureNetworkTCP  = null;
    private JMenuItem jMenuItemConfigureNetworkUDP  = null;
    private JMenuItem jMenuItemConfigureRS232       = null;
    private JMenuItem jMenuItemConfigureParallel    = null;
    /**
     * Men&uuml zur Beeinflussung der Konsole
     */
    private JMenu     jMenuConsole          = null;
    private JMenuItem jMenuItemClearConsole = null;

     /**
     * main-Funktion des Programms
     *
     */
    public static void main(String[] args)
    {
        int i=0;
        String sArg;
        String sScript = null;
        String sIP = null;
        int iPort = 0;

        //----------------------------------------------------------------------
        // Classpath anpassen
        //----------------------------------------------------------------------
        System.setProperty("java.class.path",
                "Veto.jar;" +
                ".\\comm.jar;" +
                ".\\bsh-2.0b2.jar;" +
                ".\\lava3-core.jar;" +
                ".\\lava3-printf.jar");

        while (i<args.length && args[i].startsWith("-"))
        {
            sArg = args[i++];

            if (sArg.equals("-h"))
            {
                System.out.println("-sSERVER:    Device IP-Address");
                System.out.println("-pPORT:      Printer Port-Number");
                System.out.println("-aSCRIPT");
                System.exit(0);
            }

            if (sArg.startsWith("-s"))
            {
                sIP = sArg.substring(2);
            }

            if (sArg.startsWith("-p"))
            {
                iPort = Integer.parseInt(sArg.substring(2));
            }

            if (sArg.startsWith("-a"))
            {
                sScript = sArg.substring(2);
            }
        }

        ValentinConsole rc = new ValentinConsole(sIP, iPort, sScript);
        rc.show();
    }

    /**
     * Konstruktor der Klasse ValentinConsole
     */
    public ValentinConsole(String sIP, int iPort, String sScript)
    {
        super();

        this.initVC();
        this.initialize();

        if (sScript != null)
        {
            lk_cErrorMessage.setShowWindow(false, true);
            jTabbedPaneMain.setSelectedIndex(1);
            lk_cBeanShellScriptingUI.loadScript(sScript);
        }

        if ( (sIP != null) || (iPort != 0) )
        {
            CVNetworkTCP      cInterfaceNetworkTCP;
            CVNetworkSettings cNetworkInterfaceSettings;

            jComboBoxConnectBarChooseInterface.setSelectedItem(
                    (String)"TCP network");

            cInterfaceNetworkTCP
                = lk_cConnectionManager.getTCPNetworkInterface();
            cNetworkInterfaceSettings = (CVNetworkSettings)
                cInterfaceNetworkTCP.getInterfaceSettings();
            if (sIP != null)
            {
                cNetworkInterfaceSettings.setIPAdress(sIP);
            }
            if (iPort != 0)
            {
                cNetworkInterfaceSettings.setPort(iPort);
            }

            jButtonConnectBarConnect.doClick();
        }

        if (sScript != null)
        {
            lk_cBeanShellScriptingUI.processButtonRunScript(true);
        }

        return;
    }

    /**
     * Initialisiere Valentin Komponenten
     */
    private void initVC()
    {
        // Konsole zur Eingabe von Daten
        this.lk_cConsoleInput = new CVConsole();
        this.lk_sConsoleInputTitle = "Console ASCII";

        // Ausgabe von Fehelermeldungen als Dialog
        this.lk_cErrorMessage = new CVErrorMessage(this);
        // Ausgabe von Fehlermeldungen in Logdatei
        this.lk_cErrorFile =
                new CVLogging("ErrorLog.txt", this.lk_cErrorMessage);
        // Ausgabe von Fehlermeldungen in Statuszeile des Programms
        this.lk_cStatusMessage = new CVStatusLine();

        // Anlegen der Konfigurationsdatei
        this.lk_cConfigFile = new CVConfigFile("ValentinConsole", "0.1");

        // Hexadezimale Ausgabe der Konsolendaten
        this.lk_cConsoleInputHEX = new CVTextDisplay(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);
        this.lk_cConsoleInputHEX.setOutputFormat(false, true); // (ASCII, HEX)
        this.lk_sConsoleInputHEXTitle = "Console HEX";

        // Verbindungsmanager zum Drucker
        this.lk_cConnectionManager = new CVConnectionManager(
                this.lk_cConfigFile,
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);

        // Grafische Oberflaeche Skriptverarbeitung
        this.lk_cBeanShellScriptingUI = new CVUIBeanShell(
                this,
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage,
                this.lk_cConfigFile,
                this.lk_cConnectionManager);
        this.lk_sBeanShellScriptingTitle = "Scripting";

        // Start-/Stopzeichen CVPL
        this.lk_cSohEtb = CVSohEtb.x0117;

        this.lk_cConsoleInput.restoreHistory(lk_cConfigFile);

        return;
    }

    /**
     * Anzeige der grafischen Oberflaeche zur Konfiguration TCP-Netzwerk.
     *
     */
    private void showUINetworkTCPConfig()
    {
        lk_cTCPNetworkUI = new CVUINetwork(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConnectionManager,
                    CVNetworkProtocol.TCP);
        lk_cTCPNetworkUI.setVisible(true);

        return;
    }

    /**
     * Anzeige der grafischen Oberflaeche zur Konfiguration UDP-Netzwerk.
     *
     */
    private void showUINetworkUDPConfig()
    {
        CVUINetwork cUDPNetworkUI = new CVUINetwork(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConnectionManager,
                    CVNetworkProtocol.UDP);
        cUDPNetworkUI.setVisible(true);
        return;
    }

    /**
     * Anzeige der grafischen Oberflaeche zur Konfiguration serieller Ports.
     *
     */
    private void showUISerialPortConfig()
    {
        CVUISerial serialUI = new CVUISerial(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConnectionManager);
        serialUI.setVisible(true);

        return;
    }

    /**
     * Anzeige der grafischen Oberflaeche zur Konfiguration serieller Ports.
     *
     */
    private void showUIParallelPortConfig()
    {
        CVUIParallel parallelUI = new CVUIParallel(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConnectionManager);
        parallelUI.setVisible(true);

        return;
    }

    /**
     * Console mit Drucker verbinden.
     *
     * @param connectionInterface verwendete Schnittstelle
     * @return true, wenn Verbindung hergestellt, sonst false
     */
    private boolean connectConsole(CVInterface connectionInterface)
    {
        this.lk_cConnectionManager.setSink
                (this.lk_cConsoleInput.getWriter(), true, false);
        this.lk_cConnectionManager.setSink
                (this.lk_cConsoleInputHEX.getWriter(), true, true);
        this.lk_cConnectionManager.setSource
                (this.lk_cConsoleInput.getReader());

        this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);

        if(this.lk_cConnectionManager.connect(connectionInterface) == true)
        {
            this.lk_cConsoleInput.getTextArea().setEnabled(true);

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
    private boolean disconnectConsole(CVInterface connectionInterface)
    {
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
    private boolean connectScripting(CVInterface connectionInterface)
    {
        this.lk_cConnectionManager.setSink(
                this.lk_cBeanShellScriptingUI.getBeanShell().getWriter(),
                true, false);
        this.lk_cConnectionManager.setSink(
                this.lk_cBeanShellScriptingUI.getDisplayInterfaceASCIIWriter(),
                true, true);
        this.lk_cConnectionManager.setSink(
                this.lk_cBeanShellScriptingUI.getDisplayInterfaceHEXWriter(),
                true, true);
        this.lk_cConnectionManager.setSource(
                this.lk_cBeanShellScriptingUI.getBeanShell().getReader());

        this.lk_cConnectionManager.setSohEtb(this.lk_cSohEtb);

//        if(this.lk_cConnectionManager.connect(connectionInterface) == true)
        if(this.lk_cConnectionManager.connectLight(connectionInterface) == true)
        {
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
    private boolean disconnectScripting(CVInterface connectionInterface)
    {
        return this.lk_cConnectionManager.disconnect(connectionInterface);
    }

    /**
     * Verbindung zum Drucker herstellen.
     *
     * @return true, wenn mit Drucker verbunden.
     */
    private boolean connectPrinter()
    {
        if(this.lk_cConnectionManager.isConnected() == true)
        {
            // schon mit Drucker verbunden
            this.lk_cErrorMessage.write("allready connected to printer");

            return false;
        }
        else
        {
            // welche Schnittstelle wird verwendet
            String sDescrInterface = (String)
            this.jComboBoxConnectBarChooseInterface.getSelectedItem();
            CVInterface cSelectedInterface = null;

            if(sDescrInterface.equals((String)"TCP network"))
            {
                cSelectedInterface = (CVInterface)
                this.lk_cConnectionManager.getTCPNetworkInterface();
            }
            else if(sDescrInterface.equals((String)"UDP network"))
            {
                cSelectedInterface = (CVInterface)
                this.lk_cConnectionManager.getUDPNetworkInterface();
            }
            else if(sDescrInterface.equals((String)"serial port"))
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getSerialInterface();
            }
            else
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getParallelInterface();
            }

            // soll Console oder Scripting verbunden werden
            int iSelTabIndex = this.jTabbedPaneMain.getSelectedIndex();
            String sSelTabTitle = this.jTabbedPaneMain.getTitleAt(iSelTabIndex);

            if(sSelTabTitle.equals(this.lk_sConsoleInputTitle) == true)
            {
                if(this.connectConsole(cSelectedInterface) == true)
                {
                    // andere Tab deaktivieren
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sBeanShellScriptingTitle),
                        false);

                    return true;
                }
            }

            if(sSelTabTitle.equals(this.lk_sBeanShellScriptingTitle) == true)
            {
                if(this.connectScripting(cSelectedInterface) == true)
                {
                    // andere Tab deaktivieren
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sConsoleInputTitle),
                        false);
/*
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sConsoleInputHEXTitle),
                        false);
*/
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Verbindung zum Drucker beenden.
     *
     * @return true, wenn Verbindung beendet.
     */
    private boolean disconnectPrinter()
    {
        if(this.lk_cConnectionManager.isConnected() == false)
        {
            // mit keinem Drucker verbunden
            this.lk_cErrorMessage.write("not connected to printer");

            return false;
        }
        else
        {
            // welche Schnittstelle wird verwendet
            String sDescrInterface = (String)
                this.jComboBoxConnectBarChooseInterface.getSelectedItem();
            CVInterface cSelectedInterface = null;

            if(sDescrInterface.equals((String)"TCP network"))
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getTCPNetworkInterface();
            }
            else if(sDescrInterface.equals((String)"UDP network"))
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getUDPNetworkInterface();
            }
            else if(sDescrInterface.equals((String)"serial port"))
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getSerialInterface();
            }
            else
            {
                cSelectedInterface = (CVInterface)
                        this.lk_cConnectionManager.getParallelInterface();
            }

            // soll Console oder Scripting beendet werden
            int cSelTabIndex = this.jTabbedPaneMain.getSelectedIndex();
            String sSelTabTitle = this.jTabbedPaneMain.getTitleAt(cSelTabIndex);

            if(sSelTabTitle.equals(this.lk_sConsoleInputTitle) == true)
            {
                if(this.disconnectConsole(cSelectedInterface) == true)
                {
                    // andere Tab wieder aktivieren
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sBeanShellScriptingTitle),
                        true);

                    return true;
                }
            }

            if(sSelTabTitle.equals(this.lk_sBeanShellScriptingTitle) == true)
            {
                if(this.disconnectScripting(cSelectedInterface) == true)
                {
                    // andere Tab wieder aktivieren
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sConsoleInputTitle),
                        true);
/*
                    this.jTabbedPaneMain.setEnabledAt(
                        this.jTabbedPaneMain.indexOfTab(
                            this.lk_sConsoleInputHEXTitle),
                        true);
*/
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
    private void transmitFile()
    {
        final JFileChooser fc = new JFileChooser();

        // Einlesen gewaehlter Pfad aus Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            String configValue = null;
            configValue =
                this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPath);
            if(configValue != null)
            {
                fc.setCurrentDirectory(new File(configValue));
            }
        }

        int returnVal = fc.showOpenDialog(ValentinConsole.this);

        // Datei mit Auswahldialog abfragen
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File fileTransmit     = fc.getSelectedFile();

            // Speichern gewaehlter Pfad in Konfigurationsdatei
            if(this.lk_cConfigFile != null)
            {
                lk_cConfigFile.setConfig(
                   this.lk_szConfigTokenPath,
                   fileTransmit.getAbsolutePath());
            }

            CVSohEtb cStoreCVSohEtb = this.lk_cConnectionManager.getSohEtb();
            this.lk_cConnectionManager.setSohEtb(CVSohEtb.none);

            new Thread(new Runnable()
            {
                public void run()
                {
                    CVFileTransmit fileTransmitUI = new CVFileTransmit(
                            lk_cErrorMessage,
                            lk_cErrorFile,
                            lk_cStatusMessage);
                    fileTransmitUI.setFile(fileTransmit);
                    fileTransmitUI.setOutputStream(
                            lk_cConnectionManager.getInterfaceBinaryOutput());
                    fileTransmitUI.setVisible(true);
                    fileTransmitUI.startFileTransfer();
                }
            }).start();

            this.lk_cConnectionManager.setSohEtb(cStoreCVSohEtb);
        }

        return;
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize()
    {
        this.setBounds(0, 0, 800, 600);
        this.setJMenuBar(getJMenuBarMain());
        this.setContentPane(getJPanelMain());
        this.setTitle("VETO - Valentin Embedded Test Office");
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

  /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPaneMain()
    {
        if (jTabbedPaneMain == null)
        {
            jTabbedPaneMain = new JTabbedPane();
            jTabbedPaneMain.addTab(this.lk_sConsoleInputTitle,
                    null, getJScrollPaneConsole(), null);
/*
            jTabbedPaneMain.addTab(this.lk_sConsoleInputHEXTitle,
                    null, this.lk_cConsoleInputHEX.getTextAreaPane(), null);
*/
            jTabbedPaneMain.addTab(this.lk_sBeanShellScriptingTitle,
                    null, this.lk_cBeanShellScriptingUI, null);
        }
        return jTabbedPaneMain;
    }
    /**
     * This method initializes jPanelMain
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJPanelMain()
    {
        if(jPanelMain == null)
        {
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
    private javax.swing.JScrollPane getJScrollPaneConsole()
    {
        if(jScrollPaneConsole == null)
        {
            jScrollPaneConsole = new javax.swing.JScrollPane();
            jScrollPaneConsole.setViewportView(lk_cConsoleInput.getTextArea());
            lk_cConsoleInput.getTextArea().setEnabled(false);
        }
        return jScrollPaneConsole;
    }
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelConnectBar()
    {
        if (jPanelConnectBar == null)
        {
            jLabelConnectBarEncoding = new JLabel();
            jLabelConnectBarInterface = new JLabel();
            FlowLayout ConnectBarFlowLayout = new FlowLayout();
            jPanelConnectBar = new JPanel();
            jPanelConnectBar.setLayout(ConnectBarFlowLayout);
            jPanelConnectBar.setPreferredSize(new java.awt.Dimension(20,36));
            ConnectBarFlowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            jLabelConnectBarInterface.setText("Interface:");
            jLabelConnectBarInterface.setToolTipText(
                    "choose interface for connection");
            jLabelConnectBarInterface.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jLabelConnectBarInterface.setHorizontalTextPosition(
                    javax.swing.SwingConstants.CENTER);
            jLabelConnectBarInterface.setHorizontalAlignment(
                    javax.swing.SwingConstants.CENTER);
            jLabelConnectBarEncoding.setText("Encoding:");
            jLabelConnectBarEncoding.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jLabelConnectBarEncoding.setToolTipText("encoding CVPL");
            jLabelConnectBarEncoding.setHorizontalAlignment(
                    javax.swing.SwingConstants.CENTER);
            jLabelConnectBarEncoding.setHorizontalTextPosition(
                    javax.swing.SwingConstants.CENTER);
            jPanelConnectBar.add(jLabelConnectBarInterface, null);
            jPanelConnectBar.add(getJComboBoxConnectBarChooseInterface(), null);
            jPanelConnectBar.add(getJButtonConnectBarConfigInterface(), null);
            jPanelConnectBar.add(jLabelConnectBarEncoding, null);
            jPanelConnectBar.add(getJComboBoxConnectBarChooseEncoding(), null);
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
    private JComboBox getJComboBoxConnectBarChooseInterface()
    {
        if (jComboBoxConnectBarChooseInterface == null)
        {
            jComboBoxConnectBarChooseInterface = new JComboBox();
            jComboBoxConnectBarChooseInterface.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jComboBoxConnectBarChooseInterface.setToolTipText(
                    "choose interface for connection");
            jComboBoxConnectBarChooseInterface.setBackground(Color.white);

            // Einlesen aller Schnittstellen
            jComboBoxConnectBarChooseInterface.addItem(
                    (String)"serial port");
            jComboBoxConnectBarChooseInterface.addItem(
                    (String)"parallel port");
            jComboBoxConnectBarChooseInterface.addItem(
                    (String)"TCP network");
            jComboBoxConnectBarChooseInterface.addItem(
                    (String)"UDP network");

            jComboBoxConnectBarChooseInterface.setSelectedItem(
                    (String)"serial port");

            // Einlesen gewaehlte Schnittstelle aus Konfigurationsdatei
            if(lk_cConfigFile != null)
            {
                String configValue = null;
                configValue =
                    lk_cConfigFile.getConfig(lk_szConfigTokenInterface);
                if(configValue != null)
                {
                    jComboBoxConnectBarChooseInterface.setSelectedItem(
                        configValue);
                }
            }

            jComboBoxConnectBarChooseInterface.addItemListener
                (new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    // Speichern gewaehlte Schnittstelle in Konfigurationsdatei
                    if(lk_cConfigFile != null)
                    {
                        lk_cConfigFile.setConfig(
                          lk_szConfigTokenInterface, (String)
                          jComboBoxConnectBarChooseInterface.getSelectedItem());
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
            jButtonConnectBarConfigInterface.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jButtonConnectBarConfigInterface.setText("Configure");
            jButtonConnectBarConfigInterface.setToolTipText(
                    "configure interface");
            jButtonConnectBarConfigInterface.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    String currentInterface = (String)
                        jComboBoxConnectBarChooseInterface.getSelectedItem();
                    if(currentInterface.equals(
                            (String)"TCP network") == true)
                    {
                        // TCP network
                        showUINetworkTCPConfig();
                    }
                    else if(currentInterface.equals(
                            (String)"UDP network") == true)
                    {
                        // UDP network
                        showUINetworkUDPConfig();
                    }
                    else if(currentInterface.equals(
                            (String)"serial port") == true)
                    {
                        // serial port
                        showUISerialPortConfig();
                    }
                    else
                    {
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
            jComboBoxConnectBarChooseEncoding.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jComboBoxConnectBarChooseEncoding.setBackground(Color.white);
            jComboBoxConnectBarChooseEncoding.setToolTipText("encoding CVPL");

//          Einlesen aller Enkodierungen
            for(Enumeration encodingEnum = CVSohEtb.elements();
                encodingEnum.hasMoreElements(); )
            {
                CVSohEtb encodingCurrent = (CVSohEtb)encodingEnum.nextElement();
                jComboBoxConnectBarChooseEncoding.addItem(
                        (String)encodingCurrent.toString());
                jComboBoxConnectBarChooseEncoding.setSelectedItem(
                        (String)encodingCurrent.toString());
            }

            // Einlesen gewaehlte Enkodierung aus Konfigurationsdatei
            if(lk_cConfigFile != null)
            {
                String configValue = null;
                configValue = lk_cConfigFile.getConfig(lk_szConfigTokenSohEtb);
                if(configValue != null)
                {
                    jComboBoxConnectBarChooseEncoding.setSelectedItem(
                        configValue);
                    lk_cSohEtb = CVSohEtb.fromString(configValue);
                }
                else
                {
                    jComboBoxConnectBarChooseEncoding.setSelectedIndex(0);
                }
            }
            else
            {
                jComboBoxConnectBarChooseEncoding.setSelectedIndex(0);
            }

            jComboBoxConnectBarChooseEncoding.addItemListener(
                    new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    lk_cSohEtb = CVSohEtb.fromString((String)
                        jComboBoxConnectBarChooseEncoding.getSelectedItem());

                    // Speichern gewaehlte Enkodierung in Konfigurationsdatei
                    if(lk_cConfigFile != null)
                    {
                        lk_cConfigFile.setConfig(
                           lk_szConfigTokenSohEtb, (String)
                           jComboBoxConnectBarChooseEncoding.getSelectedItem());
                    }
                }
            });
        }
        return jComboBoxConnectBarChooseEncoding;
    }

    public void setButtonsOnConnect(String s) {
        jComboBoxConnectBarChooseInterface.setSelectedItem(s);
        jButtonConnectBarConnect.setEnabled(false);
        jButtonConnectBarDisconnect.setEnabled(true);
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonConnectBarConnect() {
        if (jButtonConnectBarConnect == null) {
            jButtonConnectBarConnect = new JButton();
            jButtonConnectBarConnect.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jButtonConnectBarConnect.setText("Connect");
            jButtonConnectBarConnect.setToolTipText("connect to printer");
            jButtonConnectBarConnect.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    if(connectPrinter() == true)
                    {
                        jButtonConnectBarConnect.setEnabled(false);
                        jButtonConnectBarDisconnect.setEnabled(true);

                        jComboBoxConnectBarChooseEncoding.setEnabled(false);
                        jButtonConnectBarConfigInterface.setEnabled(false);
                        jComboBoxConnectBarChooseInterface.setEnabled(false);
                        jMenuItemConfigureNetworkTCP.setEnabled(false);
                        jMenuItemConfigureRS232.setEnabled(false);
                        jMenuItemConfigureParallel.setEnabled(false);

                        jMenuItemTransmitFile.setEnabled(true);
                    }
                }
            });
        }
        return jButtonConnectBarConnect;
    }

    public void setButtonsOnDisconnect() {
        jButtonConnectBarConnect.setEnabled(true);
        jButtonConnectBarDisconnect.setEnabled(false);
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonConnectBarDisconnect() {
        if (jButtonConnectBarDisconnect == null) {
            jButtonConnectBarDisconnect = new JButton();
            jButtonConnectBarDisconnect.setPreferredSize(
                    new java.awt.Dimension(100,26));
            jButtonConnectBarDisconnect.setText("Disconnect");
            jButtonConnectBarDisconnect.setToolTipText("disconnect printer");
            jButtonConnectBarDisconnect.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    if(disconnectPrinter() == true)
                    {
                        jButtonConnectBarConnect.setEnabled(true);
                        jButtonConnectBarDisconnect.setEnabled(false);

                        jComboBoxConnectBarChooseEncoding.setEnabled(true);
                        jButtonConnectBarConfigInterface.setEnabled(true);
                        jComboBoxConnectBarChooseInterface.setEnabled(true);
                        jMenuItemConfigureNetworkTCP.setEnabled(true);
                        jMenuItemConfigureRS232.setEnabled(true);
                        jMenuItemConfigureParallel.setEnabled(true);

                        jMenuItemTransmitFile.setEnabled(false);
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
        if (jMenuBarMain == null) {
            jMenuBarMain = new JMenuBar();
            jMenuBarMain.setName("");
            jMenuBarMain.setPreferredSize(new java.awt.Dimension(0,30));
            jMenuBarMain.add(getJMenuFile());
            jMenuBarMain.add(getJMenuInterface());
            jMenuBarMain.add(getJMenuConsole());
            jMenuBarMain.add(getJMenuInfo());
        }

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
            //jMenuInfo.setMnemonic(java.awt.event.KeyEvent.VK_C);
            jMenuInfo.add(getJMenuItemInfo());
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
            jMenuItemInfo.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    JOptionPane.showMessageDialog(null,
                            "Valentin Embedded Test Office\nVersion 0.7",
                            "Info",
                            JOptionPane.OK_OPTION);
                }
            });
        }
        return jMenuItemInfo;
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
        }
        return jMenuFile;
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
            jMenuItemTransmitFile.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    transmitFile();
                }
            });
        }
        return jMenuItemTransmitFile;
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
            jMenuItemConfigureNetworkTCP.setToolTipText(
                    "configure TCP network interface");
            jMenuItemConfigureNetworkTCP.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
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
            jMenuItemConfigureNetworkUDP.setToolTipText(
                    "configure UDP network interface");
            jMenuItemConfigureNetworkUDP.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
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
            jMenuItemConfigureRS232.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
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
            jMenuItemConfigureParallel.setToolTipText(
                        "configure parallel ports");
            jMenuItemConfigureParallel.
                        addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
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
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemClearConsole() {
        if (jMenuItemClearConsole == null) {
            jMenuItemClearConsole = new JMenuItem();
            jMenuItemClearConsole.setText("Clear");
            jMenuItemClearConsole.setToolTipText("clear console");
            jMenuItemClearConsole.addActionListener
                (new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    lk_cConsoleInput.getTextArea().setText("");
                    lk_cConsoleInputHEX.getTextArea().setText("");
                }
            });
        }
        return jMenuItemClearConsole;
    }
}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
