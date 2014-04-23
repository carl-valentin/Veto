package de.carlvalentin.ValentinConsole;

import java.io.*;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.swing.*;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung um im 
 * Carl Valentin Printer Language-Format (CVPL) mit einem Device 
 * kommunizieren zu k&ouml;nnen. 
 */
public class ValentinConsole extends JFrame {
	
    private final String strFileLastOpenedID = "Last opened file";
    private final String strNetworkIPConfigID = "IP-Address";
    private final String strNetworkSohEtbConfigID = "SOH/ETBNetwork";
    
    private final String strRS232PortID = "RS232-Port";
    private final String strRS232BaudrateID = "RS232-Baudrate";
    private final String strRS232DatabitsID = "RS232-Databits";
    private final String strRS232StopbitsID = "RS232-Stopbits";
    private final String strRS232ParityID = "RS232-Parity";
    private final String strRS232SohEtbConfigID = "SOH/ETBRS232";
    
    private StatusWriter writerStatus = new StatusWriter();
    
	private CVPLnet    cvplNet    = new CVPLnet(writerStatus);
    private CVPLsendThread sendDataThreadNetwork;
    private CVPLrecvThread recvDataThreadNetwork;
    private SohEtb sohEtbNetwork = SohEtb.x0117;
    private CVPLserial cvplSerial = new CVPLserial(writerStatus);
    private CVPLsendThread sendDataThreadRS232;
    private CVPLrecvThread recvDataThreadRS232;
    private SohEtb sohEtbRS232   = SohEtb.x0117;
    
	private de.carlvalentin.ValentinConsole.Console console = 
        new de.carlvalentin.ValentinConsole.Console();
    private Config config = new Config("ValentinConsole", "0.1");
    private File fileLastOpened = null;

	private javax.swing.JPanel jPanelMain = null;
	private javax.swing.JPanel jPanelToolbarNetwork = null;
	private javax.swing.JScrollPane jScrollPaneConsole = null;
	private javax.swing.JPanel jPanelBottom = null;	
	private javax.swing.JTextField jTextFieldIPAddr = null;
	private javax.swing.JButton jButtonNetworkConnect = null;
	private javax.swing.JLabel jLabelIPAddr = null;
	private javax.swing.JRadioButton jrb0117Network = null;
	private javax.swing.JRadioButton jrb5E5FNetwork = null;
	
	private javax.swing.JLabel jLabelStatus = null;    
    
	private JToolBar jToolBarNetwork = null;
	private JMenuBar jJMenuBar = null;
	private JMenu jMenuFile = null;
	private JMenuItem jMenuItemOpen = null;
	private JPanel jPanelToolbar = null;
	private JPanel jPanelToolbarRS232 = null;
	private JToolBar jToolBarRS232A = null;
	private JLabel jLabelRS232Port = null;
	private JComboBox jComboBoxRS232PortList = null;
	private JLabel jLabelRS232Baudrate = null;
	private JComboBox jComboBoxRS232BaudrateList = null;
	private JLabel jLabelRS232DataBits = null;
	private JComboBox jComboBoxRS232DatabitsList = null;
	private JLabel jLabelRS232Stopbits = null;
	private JComboBox jComboBoxRS232StopbitsList = null;
	private JLabel jLabelRS232Parity = null;
	private JComboBox jComboBoxRS232ParityList = null;
	private JToolBar jToolBarRS232B = null;
	private JButton jButtonRS232Open = null;
	private JButton jButtonRS232Close = null;
	private JRadioButton jRadioButtonEncoding5E5FRS232 = null;
	private JRadioButton jRadioButtonEncoding0117RS232 = null;
	private JRadioButton jRadioButtonEncodingNoneRS232 = null;
	private JCheckBox jCheckBoxRS232CD = null;
	private JCheckBox jCheckBoxRS232CTS = null;
	private JCheckBox jCheckBoxRS232DSR = null;
	private JCheckBox jCheckBoxRS232DA = null;
	private JLabel jLabelUARTBits = null;
	private JCheckBox jCheckBoxRS232DTR = null;
	private JCheckBox jCheckBoxRS232RTS = null;
	/**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */    
	private JToolBar getJToolBarNetwork() {
		if (jToolBarNetwork == null) {
			jToolBarNetwork = new JToolBar();
			jToolBarNetwork.add(getJLabelIPAddr());
			jToolBarNetwork.add(getJTextFieldIPAddr());
			jToolBarNetwork.add(getJButtonNetworkConnect());
			jToolBarNetwork.add(getJrb0117Network());
			jToolBarNetwork.add(getJrb5E5FNetwork());
		}
		return jToolBarNetwork;
	}
	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */    
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.setName("");
			jJMenuBar.setPreferredSize(new java.awt.Dimension(0,30));
			jJMenuBar.add(getJMenuFile());
		}
		return jJMenuBar;
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
			jMenuFile.add(getJMenuItemOpen());
		}
		return jMenuFile;
	}
	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItemOpen() {
		if (jMenuItemOpen == null) {
			jMenuItemOpen = new JMenuItem();
			jMenuItemOpen.setText("Open");
			jMenuItemOpen.setMnemonic(java.awt.event.KeyEvent.VK_O);
			jMenuItemOpen.setEnabled(false);
			jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    final JFileChooser fc = new JFileChooser();
                    int returnVal;
                    
                    if (fileLastOpened != null) {
                    	fc.setCurrentDirectory(fileLastOpened);
                    }
                    returnVal = fc.showOpenDialog(ValentinConsole.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {                        
                        fileLastOpened = fc.getSelectedFile();
                        config.setConfig(strFileLastOpenedID, 
                                         fileLastOpened.getAbsolutePath());
                                               
                        try {
                            final int iBufSize = 1500;
                            byte[] bData = new byte[iBufSize];
                            FileInputStream fileInputStream = 
                                new FileInputStream(fileLastOpened);
                            DataInputStream fileOpened = 
                                new DataInputStream( fileInputStream ); 
                           
                            int iDataRead = 0;
                                                        
                            do {                              
                                iDataRead = 
                                    fileOpened.read(bData, 0, iBufSize);                                
                                    if (iDataRead != -1) {
                                        if (jButtonNetworkConnect.isEnabled()) {
                                        	cvplNet.writeRaw(bData, iDataRead);
                                        }
                                        else
                                        {
                                            cvplSerial.writeRaw(bData, iDataRead);
                                        }
                                    }
                            } while (iDataRead != -1);

                            fileOpened.close();
                            fileInputStream.close();
                        }
                        catch (FileNotFoundException ex) {
                        }
                        catch (IOException ex) {
                            writeError("I/O error while trying to read from file" + 
                                       fileLastOpened.getName());
                        }
                    }
				}
			});
		}
		return jMenuItemOpen;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelToolbar() {
		if (jPanelToolbar == null) {
			jPanelToolbar = new JPanel();
			jPanelToolbar.setLayout(new BorderLayout());
			jPanelToolbar.add(getJPanelToolbarNetwork(), java.awt.BorderLayout.NORTH);
			jPanelToolbar.add(getJPanelToolbarRS232(), java.awt.BorderLayout.SOUTH);
		}
		return jPanelToolbar;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelToolbarRS232() {
		if (jPanelToolbarRS232 == null) {
			jPanelToolbarRS232 = new JPanel();
			jPanelToolbarRS232.setLayout(new BorderLayout());
			jPanelToolbarRS232.setPreferredSize(new java.awt.Dimension(20,72));
			jPanelToolbarRS232.add(getJToolBarRS232A(), java.awt.BorderLayout.NORTH);
			jPanelToolbarRS232.add(getJToolBarRS232B(), java.awt.BorderLayout.SOUTH);
		}
		return jPanelToolbarRS232;
	}
	/**
	 * This method initializes jToolBar1	
	 * 	
	 * @return javax.swing.JToolBar	
	 */    
	private JToolBar getJToolBarRS232A() {
		if (jToolBarRS232A == null) {
			jLabelRS232Baudrate = new JLabel();
			jLabelRS232DataBits = new JLabel();
			jLabelRS232Stopbits = new JLabel();
			jLabelRS232Parity = new JLabel();
			jLabelRS232Port = new JLabel();
			jToolBarRS232A = new JToolBar();
			jLabelRS232Port.setText("Serial port:");
			jToolBarRS232A.setPreferredSize(new java.awt.Dimension(402,36));
			jToolBarRS232A.setBackground(new java.awt.Color(214,204,204));
			jLabelRS232Baudrate.setText("Baudrate:");
			jLabelRS232DataBits.setText("Databits:");
			jLabelRS232Stopbits.setText("Stopbits");
			jLabelRS232Parity.setText("Parity:");
			jToolBarRS232A.add(jLabelRS232Port);
			jToolBarRS232A.add(getJComboBoxRS232PortList());
			jToolBarRS232A.add(jLabelRS232Baudrate);
			jToolBarRS232A.add(getJComboBoxRS232BaudrateList());
			jToolBarRS232A.add(jLabelRS232DataBits);
			jToolBarRS232A.add(getJComboBoxRS232DatabitsList());
			jToolBarRS232A.add(jLabelRS232Stopbits);
			jToolBarRS232A.add(getJComboBoxRS232StopbitsList());
			jToolBarRS232A.add(jLabelRS232Parity);
			jToolBarRS232A.add(getJComboBoxRS232ParityList());
		}
		return jToolBarRS232A;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxRS232PortList() {
        String strPort;
        
		if (jComboBoxRS232PortList == null) {
			jComboBoxRS232PortList = new JComboBox();
			jComboBoxRS232PortList.setBackground(java.awt.Color.white);
            for (java.util.Enumeration sPortList = cvplSerial.getCommPortList(); sPortList.hasMoreElements(); )
            {
            	String szPortName = ((CommPortIdentifier)sPortList.nextElement()).getName();
                jComboBoxRS232PortList.addItem(szPortName);
            }

            if (jComboBoxRS232PortList.getItemCount() > 0)
            {
                jComboBoxRS232PortList.setSelectedIndex(0);
                cvplSerial.setPort((String)jComboBoxRS232PortList.getSelectedItem());
            }
            
            strPort = config.getConfig(strRS232PortID);
            if (strPort == null) 
            {
                strPort = "COM1";
            }

            for (int i=0; i<jComboBoxRS232PortList.getItemCount(); i++) {
            	if ( ((String)jComboBoxRS232PortList.getItemAt(i)).
                        equals(strPort) ) {
                    jComboBoxRS232PortList.setSelectedIndex(i);
                    cvplSerial.setPort((String)jComboBoxRS232PortList.getSelectedItem());            		
                }
            }
		}
		return jComboBoxRS232PortList;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxRS232BaudrateList() {
		if (jComboBoxRS232BaudrateList == null) {
			jComboBoxRS232BaudrateList = new JComboBox();
			jComboBoxRS232BaudrateList.setBackground(java.awt.Color.white);
            jComboBoxRS232BaudrateList.addItem("57600 Bits/Sec");
            jComboBoxRS232BaudrateList.addItem("38400 Bits/Sec");
            jComboBoxRS232BaudrateList.addItem("19200 Bits/Sec");
            jComboBoxRS232BaudrateList.addItem("9600 Bits/Sec");
            jComboBoxRS232BaudrateList.addItem("4800 Bits/Sec");
            jComboBoxRS232BaudrateList.addItem("2400 Bits/Sec");
            if(jComboBoxRS232BaudrateList.getItemCount() > 0)
            {
                String strBaudRate;
                
                strBaudRate = config.getConfig(strRS232BaudrateID);
                if (strBaudRate != null) 
                {
                    if(strBaudRate.equals((String)"2400 Bits/Sec"))
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(5);
                    }
                    else if(strBaudRate.equals((String)"4800 Bits/Sec"))
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(4);
                    }
                    else if(strBaudRate.equals((String)"9600 Bits/Sec"))
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(3);
                    }
                    else if(strBaudRate.equals((String)"19200 Bits/Sec"))
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(2);
                    }
                    else if(strBaudRate.equals((String)"38400 Bits/Sec"))
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(1);
                    }
                    else // "57600 Bits/Sec"
                    {
                        jComboBoxRS232BaudrateList.setSelectedIndex(0);
                    }                    
                }
                else
                {
                
                	jComboBoxRS232BaudrateList.setSelectedIndex(3); // 9600
                }
                
                cvplSerial.setBaudRate((String)jComboBoxRS232BaudrateList.
                        getSelectedItem());
            }
		}
		return jComboBoxRS232BaudrateList;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxRS232DatabitsList() {
		if (jComboBoxRS232DatabitsList == null) {
			jComboBoxRS232DatabitsList = new JComboBox();
			jComboBoxRS232DatabitsList.setBackground(java.awt.Color.white);
            jComboBoxRS232DatabitsList.addItem("8");
            jComboBoxRS232DatabitsList.addItem("7");
            if(jComboBoxRS232DatabitsList.getItemCount() > 0)
            {
                String strDatabits;
                
                strDatabits = config.getConfig(strRS232DatabitsID);
                if (strDatabits != null) 
                {
                    if(strDatabits.equals((String)"7")) 
                    {
                        jComboBoxRS232DatabitsList.setSelectedIndex(1);
                    }
                    else // "8"
                    {
                        jComboBoxRS232DatabitsList.setSelectedIndex(0);
                    }
                }
                else 
                {
                	jComboBoxRS232DatabitsList.setSelectedIndex(0);
                }
                
                cvplSerial.setDataBits((String)jComboBoxRS232DatabitsList.
                        getSelectedItem());                
            }
		}
		return jComboBoxRS232DatabitsList;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxRS232StopbitsList() {
		if (jComboBoxRS232StopbitsList == null) {
			jComboBoxRS232StopbitsList = new JComboBox();
			jComboBoxRS232StopbitsList.setBackground(java.awt.Color.white);
            jComboBoxRS232StopbitsList.addItem("1");
            jComboBoxRS232StopbitsList.addItem("2");
            if(jComboBoxRS232StopbitsList.getItemCount() > 0)
            {
                String strStopbits;
                
                strStopbits = config.getConfig(strRS232StopbitsID);
                if (strStopbits != null) 
                {
                    if(strStopbits.equals((String)"1"))
                    {
                        jComboBoxRS232StopbitsList.setSelectedIndex(0);
                    }
                    else // "2"
                    {
                        jComboBoxRS232StopbitsList.setSelectedIndex(1);
                    }
                }
                else
                {
                	jComboBoxRS232StopbitsList.setSelectedIndex(1);
                }
                
                cvplSerial.setStopBits((String)jComboBoxRS232StopbitsList.
                        getSelectedItem());                
            }
		}
		return jComboBoxRS232StopbitsList;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBoxRS232ParityList() {
		if (jComboBoxRS232ParityList == null) {
			jComboBoxRS232ParityList = new JComboBox();
			jComboBoxRS232ParityList.setBackground(java.awt.Color.white);
            jComboBoxRS232ParityList.addItem("none");
            jComboBoxRS232ParityList.addItem("odd");
            jComboBoxRS232ParityList.addItem("even");
            if(jComboBoxRS232ParityList.getItemCount() > 0)
            {
                String strParity;
                
                strParity = config.getConfig(strRS232ParityID);
                if (strParity != null) 
                {
                    if(strParity.equals((String)"even"))
                    {
                        jComboBoxRS232ParityList.setSelectedIndex(2);
                    }
                    else if(strParity.equals((String)"odd"))
                    {
                        jComboBoxRS232ParityList.setSelectedIndex(1);
                    }
                    else // "none"
                    {
                        jComboBoxRS232ParityList.setSelectedIndex(0);
                    }
                }
                else
                {
                	jComboBoxRS232ParityList.setSelectedIndex(0);
                }
                
                cvplSerial.setParity((String)jComboBoxRS232ParityList.
                        getSelectedItem());
            }
		}
		return jComboBoxRS232ParityList;
	}
	/**
	 * This method initializes jToolBar1	
	 * 	
	 * @return javax.swing.JToolBar	
	 */    
	private JToolBar getJToolBarRS232B() {
		if (jToolBarRS232B == null) {
			jLabelUARTBits = new JLabel();
			jToolBarRS232B = new JToolBar();
			jToolBarRS232B.setBackground(new java.awt.Color(214,204,204));
			jToolBarRS232B.setPreferredSize(new java.awt.Dimension(402,36));
			jLabelUARTBits.setText("UART Bits:");
			jLabelUARTBits.setBackground(new java.awt.Color(214,204,204));
			jToolBarRS232B.add(getJRadioButtonEncodingNoneRS232());
			jToolBarRS232B.add(getJRadioButtonEncoding0117RS232());
			jToolBarRS232B.add(getJRadioButtonEncoding5E5FRS232());
			jToolBarRS232B.add(jLabelUARTBits);
			jToolBarRS232B.add(getJCheckBoxRS232RTS());
			jToolBarRS232B.add(getJCheckBoxRS232DTR());
			jToolBarRS232B.add(getJCheckBoxRS232DSR());
			jToolBarRS232B.add(getJCheckBoxRS232DA());
			jToolBarRS232B.add(getJCheckBoxRS232CTS());
			jToolBarRS232B.add(getJCheckBoxRS232CD());
			jToolBarRS232B.add(getJButtonRS232Open());
			jToolBarRS232B.add(getJButtonRS232Close());
		}
		return jToolBarRS232B;
	}
    
    private void disableToolBarsRS232()
    {        
        jButtonRS232Open.setEnabled(false);
        jComboBoxRS232PortList.setEnabled(false);
        jComboBoxRS232BaudrateList.setEnabled(false);
        jComboBoxRS232DatabitsList.setEnabled(false);
        jComboBoxRS232StopbitsList.setEnabled(false);
        jComboBoxRS232ParityList.setEnabled(false);
        jRadioButtonEncoding0117RS232.setEnabled(false);
        jRadioButtonEncoding5E5FRS232.setEnabled(false);
        jRadioButtonEncodingNoneRS232.setEnabled(false);
        jCheckBoxRS232CD.setEnabled(false);
        jCheckBoxRS232CTS.setEnabled(false);
        jCheckBoxRS232DA.setEnabled(false);
        jCheckBoxRS232DSR.setEnabled(false);
        jCheckBoxRS232DTR.setEnabled(false);
        jCheckBoxRS232RTS.setEnabled(false);
    }
    
    private void enableToolBarsRS232()
    {
        jButtonRS232Open.setEnabled(true);
        jComboBoxRS232PortList.setEnabled(true);
        jComboBoxRS232BaudrateList.setEnabled(true);
        jComboBoxRS232DatabitsList.setEnabled(true);
        jComboBoxRS232StopbitsList.setEnabled(true);
        jComboBoxRS232ParityList.setEnabled(true);
        jRadioButtonEncoding0117RS232.setEnabled(true);
        jRadioButtonEncoding5E5FRS232.setEnabled(true);
        jRadioButtonEncodingNoneRS232.setEnabled(true);
        jCheckBoxRS232CD.setEnabled(true);
        jCheckBoxRS232CTS.setEnabled(true);
        jCheckBoxRS232DA.setEnabled(true);
        jCheckBoxRS232DSR.setEnabled(true);
        jCheckBoxRS232DTR.setEnabled(true);
        jCheckBoxRS232RTS.setEnabled(true);        
    }    
    
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonRS232Open() {
		if (jButtonRS232Open == null) {
			jButtonRS232Open = new JButton();
			jButtonRS232Open.setPreferredSize(new java.awt.Dimension(40,36));
			jButtonRS232Open.setText("Open serial port");
			jButtonRS232Open.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    cvplSerial.setBaudRate((String)jComboBoxRS232BaudrateList.
                            getSelectedItem());
                    cvplSerial.setDataBits((String)jComboBoxRS232DatabitsList.
                            getSelectedItem());
                    cvplSerial.setStopBits((String)jComboBoxRS232StopbitsList.
                            getSelectedItem());
                    cvplSerial.setParity((String)jComboBoxRS232ParityList.
                            getSelectedItem());
                    cvplSerial.setPort((String)jComboBoxRS232PortList.
                            getSelectedItem());
                    
                    if(cvplSerial.openSerialPort() == true)
                    {
                        disableToolBarNetwork();
                        
                        jButtonRS232Open.setEnabled(false);
                        jButtonRS232Close.setEnabled(true);
                    
                        jComboBoxRS232PortList.setEnabled(false);
                        jComboBoxRS232BaudrateList.setEnabled(false);
                        jComboBoxRS232DatabitsList.setEnabled(false);
                        jComboBoxRS232StopbitsList.setEnabled(false);
                        jComboBoxRS232ParityList.setEnabled(false);
                        jRadioButtonEncoding0117RS232.setEnabled(false);
                        jRadioButtonEncoding5E5FRS232.setEnabled(false);
                        jRadioButtonEncodingNoneRS232.setEnabled(false);
                        
                        sendDataThreadRS232 = new CVPLsendThread(writerStatus);
                        sendDataThreadRS232.setInputReader(console.getReader());
                        sendDataThreadRS232.setOutputWriter(cvplSerial.getPortWriter());
                        sendDataThreadRS232.setSohEtb(sohEtbRS232);
                        sendDataThreadRS232.start();
                        recvDataThreadRS232 = new CVPLrecvThread(writerStatus);
                        recvDataThreadRS232.setInputReader(cvplSerial.getPortReader());
                        recvDataThreadRS232.setOutputWriter(console.getWriter());
                        recvDataThreadRS232.setSohEtb(sohEtbRS232);
                        recvDataThreadRS232.start();
                        console.getTextArea().setEnabled(true);
                        
                        jMenuItemOpen.setEnabled(true);
                        
                        config.setConfig(strRS232PortID, 
                                (String)jComboBoxRS232PortList.
                                getSelectedItem());
                        config.setConfig(strRS232BaudrateID, 
                                (String)jComboBoxRS232BaudrateList.
                                getSelectedItem());
                        config.setConfig(strRS232DatabitsID, 
                                (String)jComboBoxRS232DatabitsList.
                                getSelectedItem());
                        config.setConfig(strRS232StopbitsID, 
                                (String)jComboBoxRS232StopbitsList.
                                getSelectedItem());
                        config.setConfig(strRS232ParityID, 
                                (String)jComboBoxRS232ParityList.
                                getSelectedItem());
                        config.setConfig(strRS232SohEtbConfigID, 
                                         sohEtbRS232.toString());
                    }
				}
			});
		}
		return jButtonRS232Open;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonRS232Close() {
		if (jButtonRS232Close == null) {
			jButtonRS232Close = new JButton();
			jButtonRS232Close.setPreferredSize(new java.awt.Dimension(40,36));
			jButtonRS232Close.setText("Close serial port");
			jButtonRS232Close.setEnabled(false);
			jButtonRS232Close.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    if(cvplSerial.closeSerialPort() == true)
                    {
                        jButtonRS232Close.setEnabled(false);
                        jButtonRS232Open.setEnabled(true);
                    
                        console.saveHistory(config);
                        
                        jComboBoxRS232PortList.setEnabled(true);
                        jComboBoxRS232BaudrateList.setEnabled(true);
                        jComboBoxRS232DatabitsList.setEnabled(true);
                        jComboBoxRS232StopbitsList.setEnabled(true);
                        jComboBoxRS232ParityList.setEnabled(true);
                        jRadioButtonEncoding0117RS232.setEnabled(true);
                        jRadioButtonEncoding5E5FRS232.setEnabled(true);
                        jRadioButtonEncodingNoneRS232.setEnabled(true);
                        
                        sendDataThreadRS232.stop();
                        recvDataThreadRS232.stop();
                        console.getTextArea().setEnabled(false);
                        
                        jMenuItemOpen.setEnabled(false);
                        
                        enableToolBarNetwork();
                    }
				}
			});
		}
		return jButtonRS232Close;
	}
	/**
	 * This method initializes jrbEncoding5E5FRS232	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getJRadioButtonEncoding5E5FRS232() {
		if (jRadioButtonEncoding5E5FRS232 == null) {
			jRadioButtonEncoding5E5FRS232 = new JRadioButton();
			jRadioButtonEncoding5E5FRS232.setText("5E/5F");
			jRadioButtonEncoding5E5FRS232.setBackground(new java.awt.Color(214,204,204));
			jRadioButtonEncoding5E5FRS232.setSelected(true);
			jRadioButtonEncoding5E5FRS232.setToolTipText("use 0x5E and 0x5F to encode start and stop according to the CVPL");
            if (sohEtbRS232 == SohEtb.x5E5F)
            {
                jRadioButtonEncoding5E5FRS232.setSelected(true);
            }
            else
            {
                jRadioButtonEncoding5E5FRS232.setSelected(false);
            }
			jRadioButtonEncoding5E5FRS232.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    sohEtbRS232 = SohEtb.x5E5F;
                    jRadioButtonEncoding5E5FRS232.setSelected(true);
                    jRadioButtonEncoding0117RS232.setSelected(false);
                    jRadioButtonEncodingNoneRS232.setSelected(false);
				}
			});
		}
		return jRadioButtonEncoding5E5FRS232;
	}
	/**
	 * This method initializes jRadioButton2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getJRadioButtonEncoding0117RS232() {
		if (jRadioButtonEncoding0117RS232 == null) {
			jRadioButtonEncoding0117RS232 = new JRadioButton();
			jRadioButtonEncoding0117RS232.setText("01/17");
			jRadioButtonEncoding0117RS232.setSelected(true);
			jRadioButtonEncoding0117RS232.setBackground(new java.awt.Color(214,204,204));
			jRadioButtonEncoding0117RS232.setToolTipText("use 0x01 and 0x17 to encode start and stop according to the CVPL");
            if (sohEtbRS232 == SohEtb.x0117)
            {
                jRadioButtonEncoding0117RS232.setSelected(true);
            }
            else
            {
                jRadioButtonEncoding0117RS232.setSelected(false);
            }
			jRadioButtonEncoding0117RS232.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    sohEtbRS232 = SohEtb.x0117;
                    jRadioButtonEncoding0117RS232.setSelected(true);
                    jRadioButtonEncoding5E5FRS232.setSelected(false);
                    jRadioButtonEncodingNoneRS232.setSelected(false);
				}
			});
		}
		return jRadioButtonEncoding0117RS232;
	}
	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getJRadioButtonEncodingNoneRS232() {
		if (jRadioButtonEncodingNoneRS232 == null) {
			jRadioButtonEncodingNoneRS232 = new JRadioButton();
			jRadioButtonEncodingNoneRS232.setBackground(new java.awt.Color(214,204,204));
			jRadioButtonEncodingNoneRS232.setSelected(true);
			jRadioButtonEncodingNoneRS232.setText("none");
			jRadioButtonEncodingNoneRS232.setToolTipText("no start and stop sign");           
            if (sohEtbRS232 == null)
            {
                jRadioButtonEncodingNoneRS232.setSelected(true);
            }
            else
            {
                jRadioButtonEncodingNoneRS232.setSelected(false);
            }

			jRadioButtonEncodingNoneRS232.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    sohEtbRS232 = SohEtb.none;
                    jRadioButtonEncoding0117RS232.setSelected(false);
                    jRadioButtonEncoding5E5FRS232.setSelected(false);
                    jRadioButtonEncodingNoneRS232.setSelected(true);
				}
			});
		}
		return jRadioButtonEncodingNoneRS232;
	}
    
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232CD() {
		if (jCheckBoxRS232CD == null) {
			jCheckBoxRS232CD = new JCheckBox();
			jCheckBoxRS232CD.setText("CD");
			jCheckBoxRS232CD.setToolTipText("carrier detect");
			jCheckBoxRS232CD.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232CD.setEnabled(true);
			jCheckBoxRS232CD.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    jCheckBoxRS232CD.setSelected(!jCheckBoxRS232CD.isSelected());
				}
			});
            cvplSerial.registerCheckboxCarrierDetect(jCheckBoxRS232CD);
		}
		return jCheckBoxRS232CD;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232CTS() {
		if (jCheckBoxRS232CTS == null) {
			jCheckBoxRS232CTS = new JCheckBox();
			jCheckBoxRS232CTS.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232CTS.setText("CTS");
			jCheckBoxRS232CTS.setToolTipText("clear to send");
			jCheckBoxRS232CTS.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    jCheckBoxRS232CTS.setSelected(!jCheckBoxRS232CTS.isSelected());
				}
			});
            cvplSerial.registerCheckboxClearToSend(jCheckBoxRS232CTS);
		}
		return jCheckBoxRS232CTS;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232DSR() {
		if (jCheckBoxRS232DSR == null) {
			jCheckBoxRS232DSR = new JCheckBox();
			jCheckBoxRS232DSR.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232DSR.setText("DSR");
			jCheckBoxRS232DSR.setToolTipText("data set ready");
			jCheckBoxRS232DSR.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    jCheckBoxRS232DSR.setSelected(!jCheckBoxRS232DSR.isSelected());
				}
			});
            cvplSerial.registerCheckboxDataSetReady(jCheckBoxRS232DSR);
		}
		return jCheckBoxRS232DSR;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232DA() {
		if (jCheckBoxRS232DA == null) {
			jCheckBoxRS232DA = new JCheckBox();
			jCheckBoxRS232DA.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232DA.setText("DA");
			jCheckBoxRS232DA.setToolTipText("data available");
			jCheckBoxRS232DA.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    jCheckBoxRS232DA.setSelected(!jCheckBoxRS232DA.isSelected());
				}
			});
            cvplSerial.registerCheckboxDataAvailable(jCheckBoxRS232DA);
		}
		return jCheckBoxRS232DA;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232DTR() {
		if (jCheckBoxRS232DTR == null) {
			jCheckBoxRS232DTR = new JCheckBox();
			jCheckBoxRS232DTR.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232DTR.setText("DTR");
			jCheckBoxRS232DTR.setToolTipText("data terminal ready");
            new Thread( new Runnable()
                    {
                        public void run()
                        {
                            while(true)
                            {
                                jCheckBoxRS232DTR.setSelected(cvplSerial.getRequestToSend());
                                try
                                {
                                    Thread.sleep(1000);
                                }
                                catch(InterruptedException ex)
                                {
                                    System.err.println("jCheckBoxRS232DTR Interrupted Exception: " + ex.getMessage());
                                    writeError(        "jCheckBoxRS232DTR Interrupted Exception: " + ex.getMessage());
                                }
                            }
                        }
                    }
                    ).start();
			jCheckBoxRS232DTR.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    cvplSerial.setDataTerminalReady(jCheckBoxRS232DTR.isSelected());
				}
			});
		}
		return jCheckBoxRS232DTR;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBoxRS232RTS() {
		if (jCheckBoxRS232RTS == null) {
			jCheckBoxRS232RTS = new JCheckBox();
			jCheckBoxRS232RTS.setBackground(new java.awt.Color(214,204,204));
			jCheckBoxRS232RTS.setText("RTS");
			jCheckBoxRS232RTS.setToolTipText("request to send");
            new Thread( new Runnable()
            {
                public void run()
                {
                    while(true)
                    {
                        jCheckBoxRS232RTS.setSelected(cvplSerial.getRequestToSend());
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch(InterruptedException ex)
                        {
                            System.err.println("jCheckBoxRS232RTS Interrupted Exception: " + ex.getMessage());
                            writeError(        "jCheckBoxRS232RTS Interrupted Exception: " + ex.getMessage());
                        }
                    }
                }
            }
            ).start();
			jCheckBoxRS232RTS.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    cvplSerial.setRequestToSend(jCheckBoxRS232RTS.isSelected());
				}
			});
		}
		return jCheckBoxRS232RTS;
	}
    
    public static void main(String[] args) 
    {
        System.setProperty("java.class.path", "Veto.jar;.\\comm.jar");
		ValentinConsole rc = new ValentinConsole();
		rc.show();
	}

	/**
	 * This is the default constructor
	 */
	public ValentinConsole() {
		super();
		initialize();
	}
    
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        String strIPAddr;
        SohEtb sohEtb;
        String strFileLastOpened;

        sohEtb = SohEtb.fromString(config.getConfig(strNetworkSohEtbConfigID));
        if (sohEtb != null) 
        {
        	this.sohEtbNetwork = sohEtb;
        }
        
        sohEtb = SohEtb.fromString(config.getConfig(strRS232SohEtbConfigID));
        if (sohEtb != null)
        {
            this.sohEtbRS232 = sohEtb;
        }
        
        strFileLastOpened = config.getConfig(strFileLastOpenedID);
        if (strFileLastOpened != null) 
        {
        	fileLastOpened = new File(strFileLastOpened);
        }
        
		this.setBounds(0, 0, 800, 600);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJPanelMain());
		this.setTitle("ValentinConsole");
		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		
		javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
		group.add(jrb0117Network);
		group.add(jrb5E5FNetwork);
        
        strIPAddr = config.getConfig(strNetworkIPConfigID);
        if (strIPAddr != null) {
            jTextFieldIPAddr.setText(strIPAddr);
        }
        
        console.restoreHistory(config);
    }
	/**
	 * This method initializes jPanelMain
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelMain() {
		if(jPanelMain == null) {
			jPanelMain = new javax.swing.JPanel();
			jPanelMain.setLayout(new java.awt.BorderLayout());
			jPanelMain.add(getJPanelToolbar(), java.awt.BorderLayout.NORTH);
			jPanelMain.add(getJScrollPaneConsole(), java.awt.BorderLayout.CENTER);
			jPanelMain.add(getJPanelBottom(), java.awt.BorderLayout.SOUTH);
		}
		return jPanelMain;
	}
	/**
	 * This method initializes jPanelTop1
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelToolbarNetwork() {
		if(jPanelToolbarNetwork == null) {
			jPanelToolbarNetwork = new javax.swing.JPanel();
			jPanelToolbarNetwork.setLayout(new BorderLayout());
			jPanelToolbarNetwork.setPreferredSize(new java.awt.Dimension(20,36));
			jPanelToolbarNetwork.add(getJToolBarNetwork(), java.awt.BorderLayout.NORTH);
		}
		return jPanelToolbarNetwork;
	}
	/**
	 * This method initializes jScrollPaneConsole
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPaneConsole() {
		if(jScrollPaneConsole == null) {
			jScrollPaneConsole = new javax.swing.JScrollPane();
            jScrollPaneConsole.setViewportView(console.getTextArea());
            console.getTextArea().setEnabled(false);
		}
		return jScrollPaneConsole;
	}
	/**
	 * This method initializes jPanelTop1
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanelBottom() {
		if(jPanelBottom == null) {
			jPanelBottom = new javax.swing.JPanel();
			jPanelBottom.setLayout(new java.awt.BorderLayout());
			jPanelBottom.add(getJLabelStatus(), java.awt.BorderLayout.NORTH);
			jPanelBottom.setName("");
			jPanelBottom.setToolTipText("");
			jPanelBottom.setPreferredSize(new java.awt.Dimension(10,17));
		}
		return jPanelBottom;
	}	
    
	/**
	 * This method initializes jTextFieldIPAddr
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextFieldIPAddr() {
		if(jTextFieldIPAddr == null) {
			jTextFieldIPAddr = new javax.swing.JTextField();
			jTextFieldIPAddr.setText("192.168.0.21");
			jTextFieldIPAddr.setPreferredSize(new java.awt.Dimension(100,21));
		}
		return jTextFieldIPAddr;
	}

    private void disableToolBarNetwork()
    {        
        jrb0117Network.setEnabled(false);
        jrb5E5FNetwork.setEnabled(false);
        jButtonNetworkConnect.setEnabled(false);
        jTextFieldIPAddr.setEnabled(false);
    }
    
    private void enableToolBarNetwork()
    {
        jrb0117Network.setEnabled(true);
        jrb5E5FNetwork.setEnabled(true);
        jButtonNetworkConnect.setEnabled(true);
        jTextFieldIPAddr.setEnabled(true);
    }
    
   /**
	 * This method initializes jButtonNetworkConnect
	 * 
	 * @return javax.swing.JButtonNetworkConnect
	 */
	private javax.swing.JButton getJButtonNetworkConnect() {
		if(jButtonNetworkConnect == null) {
			jButtonNetworkConnect = new javax.swing.JButton();
			jButtonNetworkConnect.setText("Connect");
			jButtonNetworkConnect.setPreferredSize(new java.awt.Dimension(99,27));
			jButtonNetworkConnect.setActionCommand("Connect");
			jButtonNetworkConnect.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {
                    doNetworkConnect();
				}
			});
		}
		return jButtonNetworkConnect;
	}
    
    private void doNetworkConnect()
    {
        if (jTextFieldIPAddr.isEnabled()) 
        {
            jButtonNetworkConnect.setEnabled(false);
            String[] strIPTok = jTextFieldIPAddr.getText().split(":");
            String strIP = strIPTok[0];
            int iPort;
            if (strIPTok.length > 1) 
            { 
                iPort = Integer.parseInt(strIPTok[1]);
            }
            else 
            {
                iPort = 9100;
            }
            if (cvplNet.openConnect(strIP, iPort)) 
            {
                disableToolBarsRS232();
                jTextFieldIPAddr.setEnabled(false);
                jButtonNetworkConnect.setText("Disconnect");
                jrb0117Network.setEnabled(false);
                jrb5E5FNetwork.setEnabled(false);
                jMenuItemOpen.setEnabled(true);
                console.getTextArea().setEnabled(true);
            }
            
            // Threads zum Senden/Empfangen starten
            sendDataThreadNetwork = new CVPLsendThread(writerStatus);
            sendDataThreadNetwork.setInputReader(console.getReader());
            sendDataThreadNetwork.setOutputWriter(cvplNet.getNetworkWriter());
            sendDataThreadNetwork.setSohEtb(sohEtbNetwork);
            sendDataThreadNetwork.start();
            recvDataThreadNetwork = new CVPLrecvThread(writerStatus);
            recvDataThreadNetwork.setInputReader(cvplNet.getNetworkReader());
            recvDataThreadNetwork.setOutputWriter(console.getWriter());
            recvDataThreadNetwork.setSohEtb(sohEtbNetwork);
            recvDataThreadNetwork.start();
            console.getTextArea().setEnabled(true);
            
            config.setConfig(strNetworkIPConfigID, jTextFieldIPAddr.getText());
            config.setConfig(strNetworkSohEtbConfigID, sohEtbNetwork.toString());
            
            jButtonNetworkConnect.setEnabled(true);
        }
        else
        {
            // Threads zum Senden/Empfangen beenden
            sendDataThreadNetwork.stop();
            recvDataThreadNetwork.stop();
            console.getTextArea().setEnabled(false);
            
            cvplNet.closeConnect();
            console.saveHistory(config);
            console.getTextArea().setEnabled(false);
            jMenuItemOpen.setEnabled(false);
            jrb0117Network.setEnabled(true);
            jrb5E5FNetwork.setEnabled(true);
            jButtonNetworkConnect.setText("Connect");
            jTextFieldIPAddr.setEnabled(true);
            enableToolBarsRS232();
        }
    }
    
	/**
	 * This method initializes jLabelIPAddr
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabelIPAddr() {
		if(jLabelIPAddr == null) {
			jLabelIPAddr = new javax.swing.JLabel();
			jLabelIPAddr.setText("IP-Address:");
		}
		return jLabelIPAddr;
	}
	/**
	 * This method initializes jrb0117
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJrb0117Network() {
		if(jrb0117Network == null) {
			jrb0117Network = new javax.swing.JRadioButton();
			jrb0117Network.setText("01/17");
			jrb0117Network.setRolloverEnabled(false);
            if (sohEtbNetwork == SohEtb.x0117)
            {
                jrb0117Network.setSelected(true);
            }
            else
            {
                jrb0117Network.setSelected(false);
            }
			jrb0117Network.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {
                    sohEtbNetwork = SohEtb.x0117;
				}
			});
		}
		return jrb0117Network;
	}
	/**
	 * This method initializes jrb5E5F
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJrb5E5FNetwork() {
		if(jrb5E5FNetwork == null) 
        {
			jrb5E5FNetwork = new javax.swing.JRadioButton();
			jrb5E5FNetwork.setText("5E/5F");
            if (sohEtbNetwork == SohEtb.x5E5F)
            {
                jrb5E5FNetwork.setSelected(true);
            }
            else
            {
                jrb5E5FNetwork.setSelected(false);
            }
			jrb5E5FNetwork.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) 
                {
                    sohEtbNetwork = SohEtb.x5E5F;
				}
			});
		}
		return jrb5E5FNetwork;
	}
    
	/**
	 * This method initializes jLabelStatus
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabelStatus() 
    {
		if(jLabelStatus == null) 
        {
			jLabelStatus = new javax.swing.JLabel();
			jLabelStatus.setText("Status");
		}
		return jLabelStatus;
	}

    /**
     * Gibt eine Fehlermeldung als Dialogbox aus.
     * 
     * @param s Inhalt der Fehlermeldung
     */
    private void writeError(String s) 
    {
        try 
        {
            writerStatus.write(s);
            JOptionPane.showMessageDialog(null, s, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) {
            System.err.println("I/O Excexption writeError: "  + ex.getMessage());
        }
    }
    
    
    private class StatusWriter extends Writer {
        private StringBuffer buf = new StringBuffer();
        
        public synchronized void write(char[] data, int off, int len) {
            for (int i = off; i < len; i++) {
                buf.append(data[i]);
            }
            flushBuffer();
        }
        
        public synchronized void flush() throws IOException {
            if (buf.length() > 0) {
            	flushBuffer();
            }
        }
        
        public void close() throws IOException {
            flush();
        }
        
        private void flushBuffer()
        {
            final String str = buf.toString();
            buf.setLength(0);
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if(jLabelStatus != null)
                    {
                        jLabelStatus.setText(str);
                    }
                }
            });
        }
    }
}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
