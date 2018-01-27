package de.carlvalentin.Interface.UI;

import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Common.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.Protocol.CVConnectionManager;

import gnu.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
/**
 * Grafische Oberflaeche zur Konfiguration der seriellen Schnittstelle
 */
public class CVUISerial extends JFrame
{
    /**
     * Serielle Schnittstelle.
     */
    private CVSerial           lk_cSerialInterface;
    
    /**
     * Einstellungen serielle Schnittstelle.
     */
    private CVSerialSettings   lk_cSerialInterfaceSettings;
    
    /**
     * Verwaltet die Verbindungen zum Drucker
     */
    private CVConnectionManager lk_cConnectionManager;
    
	/**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage     lk_cErrorMessage;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging          lk_cErrorFile;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine       lk_cStatusMessage;
    
	private JPanel jPanelMain = null;
	private JPanel jPanelButtonBar = null;
	private JButton jButtonOK = null;
	private JButton jButtonCancel = null;
	private JTabbedPane jTabbedPaneMain = null;
	private JPanel jPanelSerialSettings = null;
	private JPanel jPanelSerialLogging = null;
	private JLabel jLabelSerialPort = null;
	private JComboBox jComboBoxSerialPort = null;
	private JLabel jLabelBaudrate = null;
	private JComboBox jComboBoxBaudrate = null;
	private JLabel jLabelDatabits = null;
	private JComboBox jComboBoxDatabits = null;
	private JLabel jLabelStopbits = null;
	private JComboBox jComboBoxStopbits = null;
	private JLabel jLabelParity = null;
	private JComboBox jComboBoxParity = null;
	private JLabel jLabelHandshake = null;
	private JComboBox jComboBoxHandshake = null;
    /**
     * 
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param cConnectionManager Verwaltet Verbindungen zum Drucker
     */
    public CVUISerial(
            CVErrorMessage      cErrorMessage, 
            CVLogging           cErrorFile,
            CVStatusLine        cStatusMessage,
            CVConnectionManager cConnectionManager)
    {
        this.lk_cErrorMessage      = cErrorMessage;
        this.lk_cErrorFile         = cErrorFile;
        this.lk_cStatusMessage     = cStatusMessage;
        this.lk_cConnectionManager = cConnectionManager;
        
        this.lk_cSerialInterface         =
                this.lk_cConnectionManager.getSerialInterface();
        this.lk_cSerialInterfaceSettings = (CVSerialSettings)
                this.lk_cSerialInterface.getInterfaceSettings();
    	this.initialize();
        
        return;
    }

    
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setBounds(0, 0, 408, 427);
        this.setName("CVUISerialFrame");
        this.setContentPane(getJPanelMain());
        this.setTitle("configure serial port");
        this.setVisible(false);
			
	}
    /**
     * Aufraeumen, bevor Objekt geloescht wird.
     */
    protected void finalize() throws Throwable
    {
    	return;
    }
    
    /**
     * Button OK wurde gedrueckt, Einstellungen werden uebernommen.
     *
     */
    private void processButtonOK()
    {
        this.lk_cSerialInterfaceSettings.setCommPort(
                (String)this.jComboBoxSerialPort.getSelectedItem());
        this.lk_cSerialInterfaceSettings.setBaudrate(
                CVSerialBaudrate.fromString(
                        (String)this.jComboBoxBaudrate.getSelectedItem()));
        this.lk_cSerialInterfaceSettings.setDatabits(
                CVSerialDatabits.fromString(
                        (String)this.jComboBoxDatabits.getSelectedItem()));
        this.lk_cSerialInterfaceSettings.setStopbits(
                CVSerialStopbits.fromString(
                        (String)this.jComboBoxStopbits.getSelectedItem()));
        this.lk_cSerialInterfaceSettings.setParity(
                CVSerialParity.fromString(
                        (String)this.jComboBoxParity.getSelectedItem()));
        this.lk_cSerialInterfaceSettings.setHandshake(
                CVSerialHandshake.fromString(
                        (String)this.jComboBoxHandshake.getSelectedItem()));
        
        if(this.lk_cSerialInterfaceSettings.validateSettings() == true)
        {
        	this.lk_cSerialInterface.setInterfaceSettings(
        			(Object)this.lk_cSerialInterfaceSettings);
            
            this.lk_cConnectionManager.setSerialInterface(
                    this.lk_cSerialInterface);
            
            this.setVisible(false);
            
            return;
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write(this,
                        "CVUISerial: wrong serial port settings");
            }
            
        	return;
        }
    }
    
    /**
     * Button Cancel wurde gedrueckt, Einstellungen werden nicht uebernommen.
     *
     */
    private void processButtonCancel()
    {
        this.setVisible(false);
    	return;
    }
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelMain() {
		if (jPanelMain == null) {
			jPanelMain = new JPanel();
			jPanelMain.setLayout(new BorderLayout());
			jPanelMain.setPreferredSize(new java.awt.Dimension(400,400));
			jPanelMain.add(getJPanelButtonBar(), java.awt.BorderLayout.SOUTH);
			jPanelMain.add(getJTabbedPaneMain(), java.awt.BorderLayout.CENTER);
		}
		return jPanelMain;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelButtonBar() {
		if (jPanelButtonBar == null) {
			jPanelButtonBar = new JPanel();
			jPanelButtonBar.setLayout(new BorderLayout());
			jPanelButtonBar.setPreferredSize(new java.awt.Dimension(30,30));
			jPanelButtonBar.add(getJButtonOK(), java.awt.BorderLayout.WEST);
			jPanelButtonBar.add(getJButtonCancel(), java.awt.BorderLayout.EAST);
		}
		return jPanelButtonBar;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton();
			jButtonOK.setPreferredSize(new java.awt.Dimension(100,25));
			jButtonOK.setText("OK");
			jButtonOK.setToolTipText("process settings");
			jButtonOK.addActionListener(new java.awt.event.ActionListener() 
            { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {    
					processButtonOK();
				}
			});
		}
		return jButtonOK;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setPreferredSize(new java.awt.Dimension(100,25));
			jButtonCancel.setText("Cancel");
			jButtonCancel.setToolTipText("do not process settings");
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() 
            { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {    
					processButtonCancel();
				}
			});
		}
		return jButtonCancel;
	}
	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JTabbedPane getJTabbedPaneMain() {
		if (jTabbedPaneMain == null) {
			jTabbedPaneMain = new JTabbedPane();
			jTabbedPaneMain.addTab("Settings", null, getJPanelSerialSettings(), "settings serial port");
			jTabbedPaneMain.addTab("Logging", null, getJPanelSerialLogging(), "logging serial port");
		}
		return jTabbedPaneMain;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelSerialSettings() {
		if (jPanelSerialSettings == null) {
			jLabelHandshake = new JLabel();
			GridLayout GridLayoutSerialSettings = new GridLayout();
			jLabelSerialPort = new JLabel();
			jLabelBaudrate = new JLabel();
			jLabelDatabits = new JLabel();
			jLabelStopbits = new JLabel();
			jLabelParity = new JLabel();
			jPanelSerialSettings = new JPanel();
			jPanelSerialSettings.setLayout(GridLayoutSerialSettings);
			GridLayoutSerialSettings.setRows(9);
			GridLayoutSerialSettings.setColumns(2);
            GridLayoutSerialSettings.setHgap(10);
            GridLayoutSerialSettings.setVgap(10);
			jLabelSerialPort.setText("serial port:");
			jPanelSerialSettings.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			jLabelBaudrate.setText("baud rate:");
			jLabelDatabits.setText("data bits:");
			jLabelStopbits.setText("stop bits:");
			jLabelParity.setText("parity:");
			jLabelSerialPort.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelBaudrate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelDatabits.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelStopbits.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelParity.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabelHandshake.setText("handshake:");
			jLabelHandshake.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jPanelSerialSettings.add(jLabelSerialPort, null);
			jPanelSerialSettings.add(getJComboBoxSerialPort(), null);
			jPanelSerialSettings.add(jLabelBaudrate, null);
			jPanelSerialSettings.add(getJComboBoxBaudrate(), null);
			jPanelSerialSettings.add(jLabelDatabits, null);
			jPanelSerialSettings.add(getJComboBoxDatabits(), null);
			jPanelSerialSettings.add(jLabelStopbits, null);
			jPanelSerialSettings.add(getJComboBoxStopbits(), null);
			jPanelSerialSettings.add(jLabelParity, null);
			jPanelSerialSettings.add(getJComboBoxParity(), null);
			jPanelSerialSettings.add(jLabelHandshake, null);
			jPanelSerialSettings.add(getJComboBoxHandshake(), null);
		}
		return jPanelSerialSettings;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelSerialLogging() {
		if (jPanelSerialLogging == null) {
			jPanelSerialLogging = new JPanel();
		}
		return jPanelSerialLogging;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxSerialPort() {
		if (jComboBoxSerialPort == null) {
			jComboBoxSerialPort = new JComboBox();
			jComboBoxSerialPort.setBackground(java.awt.Color.white);
		}
        // Liste aller serieller Schnittstellen abfragen
        Vector commPortsVector = 
            this.lk_cSerialInterfaceSettings.getCommPortVector();
        CommPortIdentifier commPortSelected =
            this.lk_cSerialInterfaceSettings.getCommPort();
        for(int iCounter = 0; iCounter < commPortsVector.size(); iCounter++)
        {
        	CommPortIdentifier commPortCurrent = 
                (CommPortIdentifier) commPortsVector.get(iCounter);
            jComboBoxSerialPort.addItem((String)commPortCurrent.getName());
            if(commPortCurrent.equals(commPortSelected) == true)
            {
                jComboBoxSerialPort.setSelectedIndex(iCounter);
            }
        }
		return jComboBoxSerialPort;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxBaudrate() {
		if (jComboBoxBaudrate == null) {
			jComboBoxBaudrate = new JComboBox();
			jComboBoxBaudrate.setBackground(java.awt.Color.white);   
		}
        // Liste aller unterstuetzten Baudraten einf&uuml;gen
        CVSerialBaudrate baudRateSelected = 
            this.lk_cSerialInterfaceSettings.getBaudrate();
        for(Enumeration baudRateEnum = CVSerialBaudrate.elements();
            baudRateEnum.hasMoreElements(); )
        {
        	CVSerialBaudrate baudRateCurrent = 
                (CVSerialBaudrate) baudRateEnum.nextElement();
            jComboBoxBaudrate.addItem((String)baudRateCurrent.toString());
        }
        jComboBoxBaudrate.setSelectedItem((String)baudRateSelected.toString());
        return jComboBoxBaudrate;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxDatabits() {
		if (jComboBoxDatabits == null) {
			jComboBoxDatabits = new JComboBox();
            jComboBoxDatabits.setBackground(java.awt.Color.white);
		}
        // Liste aller unterstuetzten Anzahlen an Datenbits einfuegen
        CVSerialDatabits dataBitsSelected =
            this.lk_cSerialInterfaceSettings.getDatabits();
        for(Enumeration dataBitsEnum = CVSerialDatabits.elements();
            dataBitsEnum.hasMoreElements(); )
        {
        	CVSerialDatabits dataBitsCurrent =
                (CVSerialDatabits) dataBitsEnum.nextElement();
            jComboBoxDatabits.addItem((String)dataBitsCurrent.toString());
        }
        jComboBoxDatabits.setSelectedItem((String)dataBitsSelected.toString());
		return jComboBoxDatabits;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxStopbits() {
		if (jComboBoxStopbits == null) {
			jComboBoxStopbits = new JComboBox();
            jComboBoxStopbits.setBackground(java.awt.Color.white);
		}
        // Liste aller unterstuetzten Anzahlen an Stopbits einfuegen
        CVSerialStopbits stopBitsSelected =
            this.lk_cSerialInterfaceSettings.getStopbits();
        for(Enumeration stopBitsEnum = CVSerialStopbits.elements();
            stopBitsEnum.hasMoreElements(); )
        {
        	CVSerialStopbits stopBitsCurrent =
                (CVSerialStopbits) stopBitsEnum.nextElement();
            jComboBoxStopbits.addItem((String)stopBitsCurrent.toString());
        }
        jComboBoxStopbits.setSelectedItem((String)stopBitsSelected.toString());
		return jComboBoxStopbits;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxParity() {
		if (jComboBoxParity == null) {
			jComboBoxParity = new JComboBox();
			jComboBoxParity.setBackground(java.awt.Color.white);
		}
        // Liste aller unterstuetzten Parities einfuegen
        CVSerialParity paritySelected =
            this.lk_cSerialInterfaceSettings.getParity();
        for(Enumeration parityEnum = CVSerialParity.elements();
            parityEnum.hasMoreElements(); )
        {
        	CVSerialParity parityCurrent =
                (CVSerialParity) parityEnum.nextElement();
            jComboBoxParity.addItem((String)parityCurrent.toString());
        }
        jComboBoxParity.setSelectedItem((String)paritySelected.toString());
		return jComboBoxParity;
	}

	/**
	 * This method initializes jComboBox
	 * 	
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxHandshake() {
		if (jComboBoxHandshake == null) {
			jComboBoxHandshake = new JComboBox();
			jComboBoxHandshake.setBackground(java.awt.Color.white);
		}
        // Liste aller unterstuetzten Handshakes einfuegen
        CVSerialHandshake handshakeSelected =
            this.lk_cSerialInterfaceSettings.getHandshake();
        for(Enumeration handshakeEnum = CVSerialHandshake.elements();
            handshakeEnum.hasMoreElements(); )
        {
            CVSerialHandshake handshakeCurrent =
                (CVSerialHandshake) handshakeEnum.nextElement();
            jComboBoxHandshake.addItem((String)handshakeCurrent.toString());
        }
        jComboBoxHandshake.setSelectedItem((String)handshakeSelected.toString());
		return jComboBoxHandshake;
	}
 }
