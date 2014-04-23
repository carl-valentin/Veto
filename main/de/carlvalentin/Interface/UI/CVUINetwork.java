package de.carlvalentin.Interface.UI;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.Protocol.*;

import java.awt.*;
import javax.swing.*;
/**
 * Grafische Oberflaeche zur Konfiguration der Netzwerkschnittstelle
 */
public class CVUINetwork extends JFrame
{
	/**
	 * Ausgewaehltes Netzwerkprotokoll
	 */
	private CVNetworkProtocol   lk_cNetworkProtocol = null;
	
    /**
     * Netzwerkschnittstelle TCP-Protokoll
     */
    private CVNetworkTCP        lk_cTCPNetworkInterface = null;
    
    /**
     * Netzwerkschnittstelle UDP-Protokoll
     */
    private CVNetworkUDP        lk_cUDPNetworkInterface = null;
    
    /**
     * Einstellungen Netzwerkschnittstelle
     */
    private CVNetworkSettings   lk_cNetworkInterfaceSettings = null;
    
    /**
     * Verwaltet die Verbindungen zum Drucker
     */
    private CVConnectionManager lk_cConnectionManager = null;
    
	/**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage      lk_cErrorMessage = null;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging           lk_cErrorFile = null;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine        lk_cStatusMessage = null;
    
	private JPanel      jPanelMain = null;
	private JPanel      jPanelButtonBar = null;
	private JButton     jButtonOK = null;
	private JButton     jButtonCancel = null;
	private JTabbedPane jTabbedPaneMain = null;
	
	/**
	 * Einstellungen fuer Logging
	 */
	private JPanel      jPanelNetworkLogging = null;
	
	/**
	 * Netzwerkeinstellungen
	 */
	private JPanel      jPanelNetworkSettings = null;
	//--------------------------------------------------------------------------
	// TCP und UDP
	//--------------------------------------------------------------------------
	private JLabel      jLabelTCPUDPIPAddress = null;
	private JTextField  jTextFieldTCPUDPIPAdress = null;
	private JLabel      jLabelTCPUDPPort = null;
	private JTextField  jTextFieldTCPUDPPort = null;
	
	//--------------------------------------------------------------------------
	// TCP
	//--------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------
	// UDP
	//--------------------------------------------------------------------------
	private JCheckBox   jCheckBoxUDPBroadcast = null;
	
    /**
     * Konstruktor der Klasse CVUINetwork
     * 
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param cConnectionManager Verwaltet Verbindungen zum Drucker
     * @param cNetworkProtocol Verwendetes Netzwerkprotokoll
     */
    public CVUINetwork(
            CVErrorMessage      cErrorMessage, 
            CVLogging           cErrorFile,
            CVStatusLine        cStatusMessage,
            CVConnectionManager cConnectionManager,
            CVNetworkProtocol   cNetworkProtocol)
    {
    	this.lk_cErrorMessage      = cErrorMessage;
        this.lk_cErrorFile         = cErrorFile;
        this.lk_cStatusMessage     = cStatusMessage;
        this.lk_cConnectionManager = cConnectionManager;
        this.lk_cNetworkProtocol   = cNetworkProtocol;
        
        //----------------------------------------------------------------------
        // TCP
        //----------------------------------------------------------------------
        if(this.lk_cNetworkProtocol == CVNetworkProtocol.TCP)
        {
        	this.lk_cTCPNetworkInterface = 
                	this.lk_cConnectionManager.getTCPNetworkInterface();
        	this.lk_cNetworkInterfaceSettings = (CVNetworkSettings) 
            		this.lk_cTCPNetworkInterface.getInterfaceSettings();
        }
        //----------------------------------------------------------------------
        // UDP
        //----------------------------------------------------------------------
        if(this.lk_cNetworkProtocol == CVNetworkProtocol.UDP)
        {
        	this.lk_cUDPNetworkInterface =
        			this.lk_cConnectionManager.getUDPNetworkInterface();
        	this.lk_cNetworkInterfaceSettings = (CVNetworkSettings)
        			this.lk_cUDPNetworkInterface.getInterfaceSettings();
        }
        
    	this.initialize();
        
        return;
    }

    /**
     * Aufraeumen, bevor Objekt geloescht wird
     */
    protected void finalize() throws Throwable
    {
        this.setVisible(false);
        return;
    }
    
    /**
     * Button OK wurde gedrueckt
     *
     */
    private void processButtonOK()
    {
        this.lk_cNetworkInterfaceSettings.setIPAdress(
            this.jTextFieldTCPUDPIPAdress.getText());
        
        this.lk_cNetworkInterfaceSettings.setPort(
            Integer.decode(this.jTextFieldTCPUDPPort.getText()).intValue());
        
        if(/*this.lk_cNetworkInterfaceSettings.validateSettings() == */true)
        {
        	//------------------------------------------------------------------
        	// TCP
        	//------------------------------------------------------------------
        	if(this.lk_cNetworkProtocol == CVNetworkProtocol.TCP)
        	{
        		this.lk_cTCPNetworkInterface.setInterfaceSettings(
        				(Object) this.lk_cNetworkInterfaceSettings);
            	this.lk_cConnectionManager.setTCPNetworkInterface(
        				this.lk_cTCPNetworkInterface);
        	}
        	//------------------------------------------------------------------
        	// UDP
        	//------------------------------------------------------------------
        	if(this.lk_cNetworkProtocol == CVNetworkProtocol.UDP)
        	{
        		this.lk_cUDPNetworkInterface.setInterfaceSettings(
        				(Object) this.lk_cNetworkInterfaceSettings);
        		this.lk_cConnectionManager.setUDPNetworkInterface(
        				this.lk_cUDPNetworkInterface);
        	}
            
        	this.setVisible(false);
            
        	return;
        }
        else
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(this, 
                        "CVUINetwork: wrong network settings");
            }
            
        	return;
        }
    }
    
    /**
     * Button Cancel wurde gedrueckt
     *
     */
    private void processButtonCancel()
    {
        this.setVisible(false);
        return;
    }
    
	/**
	 * Initialisierung der grafischen Oberflaeche
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setBounds(0, 0, 408, 427);
        this.setResizable(true);
        this.setName("CVUINetworkFrame");
        this.setContentPane(getJPanelMain());
        this.setTitle("configure network settings");
        this.setVisible(false);
			
	}
    
	/**
	 * Initialisierung von jPanelMain	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelMain() 
	{
		if (this.jPanelMain == null) 
		{
			this.jPanelMain = new JPanel();
			this.jPanelMain.setLayout(new BorderLayout());
			this.jPanelMain.setPreferredSize(new java.awt.Dimension(400,400));
			this.jPanelMain.add
					(getJPanelButtonBar(), java.awt.BorderLayout.SOUTH);
			this.jPanelMain.add
					(getJTabbedPaneMain(), java.awt.BorderLayout.CENTER);
		}
		return jPanelMain;
	}
	
	/**
	 * Initialisierung von jPanelButtonBar	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelButtonBar() 
	{
		if (this.jPanelButtonBar == null) 
		{
			this.jPanelButtonBar = new JPanel();
			this.jPanelButtonBar.setLayout(new BorderLayout());
			this.jPanelButtonBar.setPreferredSize
					(new java.awt.Dimension(30,30));
			this.jPanelButtonBar.add
					(getJButtonOK(), java.awt.BorderLayout.WEST);
			this.jPanelButtonBar.add
					(getJButtonCancel(), java.awt.BorderLayout.EAST);
		}
		return jPanelButtonBar;
	}
	
	/**
	 * Initialisierung von jButtonOK	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonOK() 
	{
		if (this.jButtonOK == null) 
		{
			this.jButtonOK = new JButton();
			this.jButtonOK.setPreferredSize(new java.awt.Dimension(100,25));
			this.jButtonOK.setText("OK");
			this.jButtonOK.setToolTipText("process settings");
			this.jButtonOK.addActionListener(new java.awt.event.ActionListener() 
            { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {    
					processButtonOK();
				}
			});
		}
		return this.jButtonOK;
	}
	
	/**
	 * Initialisierung von jButtonCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonCancel() 
	{
		if (this.jButtonCancel == null) 
		{
			this.jButtonCancel = new JButton();
			this.jButtonCancel.setPreferredSize(new java.awt.Dimension(100,25));
			this.jButtonCancel.setText("Cancel");
			this.jButtonCancel.setToolTipText("do not process settings");
			this.jButtonCancel.addActionListener(new java.awt.event.ActionListener() 
            { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {    
					processButtonCancel();
				}
			});
		}
		return this.jButtonCancel;
	}
	
	/**
	 * Initialisierung von jTabbedPaneMain	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JTabbedPane getJTabbedPaneMain() 
	{
		if (this.jTabbedPaneMain == null) 
		{
			this.jTabbedPaneMain = new JTabbedPane();
			this.jTabbedPaneMain.addTab("Settings", null, 
					getJPanelNetworkSettings(), "settings network interface");
			this.jTabbedPaneMain.addTab("Logging", null, 
					getJPanelNetworkLogging(), "logging network interface");
		}
		return this.jTabbedPaneMain;
	}
	
	/**
	 * Initialisierung von jPanelNetworkSettings	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelNetworkSettings() 
	{
		if (this.jPanelNetworkSettings == null) 
		{
			GridLayout GridLayoutNetworkSettings = new GridLayout();
			this.jPanelNetworkSettings = new JPanel();
			this.jPanelNetworkSettings.setLayout(GridLayoutNetworkSettings);
			GridLayoutNetworkSettings.setRows(9);
			GridLayoutNetworkSettings.setHgap(10);
			GridLayoutNetworkSettings.setVgap(10);
			GridLayoutNetworkSettings.setColumns(1);
			//------------------------------------------------------------------
			// Beschriftung IP-Adresse
			//------------------------------------------------------------------
			this.jLabelTCPUDPIPAddress = new JLabel();
			this.jLabelTCPUDPIPAddress.setText("IP address:");
			this.jLabelTCPUDPIPAddress.setHorizontalAlignment(
					javax.swing.SwingConstants.CENTER);
			this.jLabelTCPUDPIPAddress.setHorizontalTextPosition(
					javax.swing.SwingConstants.CENTER);
			this.jLabelTCPUDPIPAddress.setToolTipText("printer ip address");
			//------------------------------------------------------------------
			// Beschriftung Port
			//------------------------------------------------------------------
			this.jLabelTCPUDPPort = new JLabel();
			this.jLabelTCPUDPPort.setText("Port:");
			this.jLabelTCPUDPPort.setHorizontalAlignment(
					javax.swing.SwingConstants.CENTER);
			this.jLabelTCPUDPPort.setHorizontalTextPosition(
					javax.swing.SwingConstants.CENTER);
			this.jLabelTCPUDPPort.setToolTipText("printer network port");
			//------------------------------------------------------------------
			// Einstellungen fuer TCP und UDP
			//------------------------------------------------------------------
			this.jPanelNetworkSettings.add(jLabelTCPUDPIPAddress, null);
			this.jPanelNetworkSettings.add(getJTextFieldTCPUDPIPAdress(), null);
			this.jPanelNetworkSettings.add(jLabelTCPUDPPort, null);
			this.jPanelNetworkSettings.add(getJTextFieldTCPUDPPort(), null);
			//------------------------------------------------------------------
			// Einstellungen fuer TCP
			//------------------------------------------------------------------
			if(this.lk_cNetworkProtocol == CVNetworkProtocol.TCP)
			{
				
			}
			//------------------------------------------------------------------
			// Einstellungen fuer UDP
			//------------------------------------------------------------------
			if(this.lk_cNetworkProtocol == CVNetworkProtocol.UDP)
			{
				this.jPanelNetworkSettings.add(this.getJCheckBoxUDPBroadcast());
			}
		}
		return jPanelNetworkSettings;
	}
	
	/**
	 * Initialisierung von jPanelNetworkLogging	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelNetworkLogging() 
	{
		if (this.jPanelNetworkLogging == null) 
		{
			this.jPanelNetworkLogging = new JPanel();
			jPanelNetworkLogging.setVisible(true);
		}
		return this.jPanelNetworkLogging;
	}
	
	/**
	 * Initialisierung von jTextFieldTCPUDPIPAdress	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextFieldTCPUDPIPAdress() 
	{
		if (jTextFieldTCPUDPIPAdress == null) 
		{
			this.jTextFieldTCPUDPIPAdress = new JTextField();
			this.jTextFieldTCPUDPIPAdress.setToolTipText("printer ip address");
			this.jTextFieldTCPUDPIPAdress.setText(
                    this.lk_cNetworkInterfaceSettings.getIPAdress());
			this.jTextFieldTCPUDPIPAdress.setHorizontalAlignment(
					javax.swing.JTextField.CENTER);
		}
		return this.jTextFieldTCPUDPIPAdress;
	}
	
	/**
	 * Initialisierung von jTextFieldTCPUDPPort	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextFieldTCPUDPPort() 
	{
		if (this.jTextFieldTCPUDPPort == null) 
		{
			this.jTextFieldTCPUDPPort = new JTextField();
			this.jTextFieldTCPUDPPort.setHorizontalAlignment(
					javax.swing.JTextField.CENTER);
			this.jTextFieldTCPUDPPort.setText(Integer.toString(
                    this.lk_cNetworkInterfaceSettings.getPort()));
			this.jTextFieldTCPUDPPort.setToolTipText("printer network port");
		}
		return this.jTextFieldTCPUDPPort;
	}
	
	/**
	 * Initialisierung von jCheckBoxUDPBoradcast
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxUDPBroadcast()
	{
		if(this.jCheckBoxUDPBroadcast == null)
		{
			this.jCheckBoxUDPBroadcast = new JCheckBox();
			this.jCheckBoxUDPBroadcast.setName("UDP Broadcast");
			this.jCheckBoxUDPBroadcast.setSelected(
					this.lk_cNetworkInterfaceSettings.getUDPBroadcast());
			this.jCheckBoxUDPBroadcast.setText("UDP Broadcast");
			this.jCheckBoxUDPBroadcast.setToolTipText(
					"send UDP messages as broadcast");
			this.jCheckBoxUDPBroadcast.setHorizontalAlignment
					(SwingConstants.CENTER);
			jCheckBoxUDPBroadcast.setVisible(false);
		}
		return this.jCheckBoxUDPBroadcast;
	}
}
