package de.carlvalentin.Interface.UI;

import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Common.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.Protocol.*;

import gnu.io.*;

import java.util.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;

/**
 * Grafische Oberflaeche zur Konfiguration der parallelen Schnittstelle
 */
public class CVUIParallel extends JDialog
{
    /**
     * Serielle Schnittstelle.
     */
    private CVParallel         lk_cParallelInterface;

    /**
     * Einstellungen serielle Schnittstelle.
     */
    private CVParallelSettings lk_cParallelInterfaceSettings;

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
	private JTabbedPane jTabbedPaneMain = null;
	private JPanel jPanelParallelSettings = null;
	private JPanel jPanelParallelLogging = null;
	private JLabel jLabelParallelPort = null;
	private JComboBox jComboBoxParallelPort = null;
	private JLabel jLabel = null;
	private JComboBox jComboBoxMode = null;
	private JButton jButtonOK = null;
	private JButton jButtonCancel = null;
    /**
     *
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param cConnectionManager Verwaltet Verbindungen zum Drucker
     */
    public CVUIParallel(
            CVErrorMessage      cErrorMessage,
            CVLogging           cErrorFile,
            CVStatusLine        cStatusMessage,
            CVConnectionManager cConnectionManager)
    {
        this.lk_cErrorMessage      = cErrorMessage;
        this.lk_cErrorFile         = cErrorFile;
        this.lk_cStatusMessage     = cStatusMessage;
        this.lk_cConnectionManager = cConnectionManager;

        this.lk_cParallelInterface         =
                this.lk_cConnectionManager.getParallelInterface();
        this.lk_cParallelInterfaceSettings = (CVParallelSettings)
                this.lk_cParallelInterface.getInterfaceSettings();
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
        this.setName("CVUIParallelFrame");
        this.setContentPane(getJPanelMain());
        this.setTitle("configure parallel port");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
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
        this.lk_cParallelInterfaceSettings.setCommPort(
            (String)this.jComboBoxParallelPort.getSelectedItem());
    	this.lk_cParallelInterfaceSettings.setParallelMode(
            CVParallelModes.fromString(
            		(String)this.jComboBoxMode.getSelectedItem()));

        if(this.lk_cParallelInterfaceSettings.validateSettings() == true)
        {
            this.lk_cParallelInterface.setInterfaceSettings(
            		(Object)this.lk_cParallelInterfaceSettings);

            this.lk_cConnectionManager.setParallelInterface(
                    this.lk_cParallelInterface);

            this.setVisible(false);

            return;
        }
        else
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(this,
                        "CVUIParallel: wrong parallel port settings");
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
            jPanelMain.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
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
	 * This method initializes jTabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPaneMain == null) {
			jTabbedPaneMain = new JTabbedPane();
			jTabbedPaneMain.setPreferredSize(new java.awt.Dimension(137,333));
			jTabbedPaneMain.addTab("Settings", null, getJPanelParallelSettings(), "settings parallel port");
			jTabbedPaneMain.addTab("Logging", null, getJPanelParallelLogging(), "logging parallel port");
		}
		return jTabbedPaneMain;
	}
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelParallelSettings() {
		if (jPanelParallelSettings == null) {
			GridLayout GridLayoutParallelSettings = new GridLayout();
			jLabelParallelPort = new JLabel();
			jLabel = new JLabel();
			jPanelParallelSettings = new JPanel();
			jPanelParallelSettings.setLayout(GridLayoutParallelSettings);
			GridLayoutParallelSettings.setRows(9);
			GridLayoutParallelSettings.setColumns(2);
			GridLayoutParallelSettings.setHgap(10);
			GridLayoutParallelSettings.setVgap(10);
			jLabelParallelPort.setText("parallel port:");
			jLabelParallelPort.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabel.setText("mode:");
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jPanelParallelSettings.add(jLabelParallelPort, null);
			jPanelParallelSettings.add(getJComboBoxParallelPort(), null);
			jPanelParallelSettings.add(jLabel, null);
			jPanelParallelSettings.add(getJComboBoxMode(), null);
		}
		return jPanelParallelSettings;
	}
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelParallelLogging() {
		if (jPanelParallelLogging == null) {
			jPanelParallelLogging = new JPanel();
		}
		return jPanelParallelLogging;
	}
	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxParallelPort() {
		if (jComboBoxParallelPort == null) {
			jComboBoxParallelPort = new JComboBox();
			jComboBoxParallelPort.setBackground(java.awt.Color.white);
		}
//       Liste aller serieller Schnittstellen abfragen
        Vector commPortsVector =
            this.lk_cParallelInterfaceSettings.getCommPortVector();
        CommPortIdentifier commPortSelected =
            this.lk_cParallelInterfaceSettings.getCommPort();
        for(int iCounter = 0; iCounter < commPortsVector.size(); iCounter++)
        {
            CommPortIdentifier commPortCurrent =
                (CommPortIdentifier) commPortsVector.get(iCounter);
            jComboBoxParallelPort.addItem((String)commPortCurrent.getName());
            if(commPortCurrent.equals(commPortSelected) == true)
            {
                jComboBoxParallelPort.setSelectedIndex(iCounter);
            }
        }
		return jComboBoxParallelPort;
	}
	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxMode() {
		if (jComboBoxMode == null) {
			jComboBoxMode = new JComboBox();
			jComboBoxMode.setBackground(java.awt.Color.white);
		}
        // Liste aller Schnittstellenmodi einfuegen
        CVParallelModes parallelModeSelected =
            this.lk_cParallelInterfaceSettings.getParallelMode();
        for(Enumeration parallelModeEnum = CVParallelModes.elements();
            parallelModeEnum.hasMoreElements(); )
        {
            CVParallelModes parallelModeCurrent =
                (CVParallelModes) parallelModeEnum.nextElement();
            jComboBoxMode.addItem((String)parallelModeCurrent.toString());
        }
        jComboBoxMode.setSelectedItem((String)parallelModeSelected.toString());
        jComboBoxMode.setEnabled(false);
		return jComboBoxMode;
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
      }
