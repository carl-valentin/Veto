package de.carlvalentin.Updater;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;

import javax.swing.*;

import de.carlvalentin.Common.CVConfigFile;

enum CVPLSTATE
{
    SEARCHSOH,
    SOHFOUND_ANALYSE_1ST_CMD_SIGN,
    REMCON_TD_ANALYZE_2ND_CMD_SIGN,
    REMCON_TD_ANALYZE_3_SEPERATOR,
    REMCON_TD_GET_DISPLAY_CONTENT,
    REMCON_WAIT4ETB_ETB,
    WAIT4ETB,
};

public class ValentinUpdater {
	Socket socket;
	boolean connected = false;

	JFrame updateFrame;
	JPanel top;
	JPanel bottom;

	JLabel lbIp;
	JLabel lbPort;
	JTextField tfIp;
	JTextField tfPort;
	JButton btConnect;
	JButton btDisConnect;
	JCheckBox checkUpdAll;
	JFileChooser fileChooser;
	JProgressBar progressBar;
	JLabel lbStatus;
	JLabel lbRemConLine1;
	JLabel lbRemConLine2;
	
	boolean bStop = false;
	
	CVConfigFile configFile;

	String folderPath = "P:\\Firmware\\Freigaben";
	String KEYLASTUPDATEIP = "lastupdateip";

	public ValentinUpdater() {
		updaterMain();
	}
	public ValentinUpdater(CVConfigFile cfgFile) {
		configFile = cfgFile;
		updaterMain();
	}

	public ValentinUpdater(String folderPath) {
		this.folderPath = folderPath;
		updaterMain();
	}

	public void updaterMain() {
		updateFrame = new JFrame();
		updateFrame.setTitle("Valentin Updater");
		updateFrame.setLayout(new BorderLayout());
		top = new JPanel();
		bottom = new JPanel();
		createUIElements();	
		
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileChooserAction(e);
            }
        }); 
		
		updateFrame.add(top, BorderLayout.NORTH);
		updateFrame.add(fileChooser, BorderLayout.CENTER);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));
		lbRemConLine1.setAlignmentX( Component.LEFT_ALIGNMENT );
		lbRemConLine2.setAlignmentX( Component.LEFT_ALIGNMENT );
		progressBar.setAlignmentX( Component.LEFT_ALIGNMENT );
		bottom.add(lbRemConLine1, BorderLayout.SOUTH); 
		bottom.add(lbRemConLine2, BorderLayout.SOUTH); 
		bottom.add(progressBar, BorderLayout.SOUTH);
		bottom.add(lbStatus, BorderLayout.SOUTH);
		updateFrame.add(bottom, BorderLayout.SOUTH);
		updateFrame.setVisible(true);
		updateFrame.pack();
	}

	private void createUIElements() {
		if (lbIp == null) {
			lbIp = new JLabel();
		}
		if (lbPort == null) {
			lbPort = new JLabel();
		}
		if (tfIp == null) {
			tfIp = new JTextField(18);
			if(configFile!= null) {
				String lastIp = configFile.getConfig(KEYLASTUPDATEIP);
				if(lastIp != null && lastIp != "") {
					tfIp.setText(lastIp);
				}
			}
		}
		if (tfPort == null) {
			tfPort = new JTextField(5);
			tfPort.setText("9100");
		}
		if (btConnect == null) {
			btConnect = new JButton();
			btConnect.setText("Connect");
			btConnect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(configFile!= null) {
						configFile.setConfig(KEYLASTUPDATEIP, tfIp.getText());
					}
					setUIConnected();
					bStop = false;
		            new Thread()
		            {
		                public void run() {
		                    connect(60000);
		                }
		            }.start(); 
				}
			});
		}
		if (btDisConnect == null) {
			btDisConnect = new JButton();
			btDisConnect.setText("Disconnect");
			btDisConnect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					disconnect();
				}    
			});
            btDisConnect.setEnabled(false);
		}
		if (checkUpdAll == null) {
		    checkUpdAll = new JCheckBox("Update Fw+Data");
		    checkUpdAll.setSelected(true);
		}
		
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			File testPath = new File(folderPath);
			if (testPath.exists()) {
				fileChooser.setCurrentDirectory(testPath);
			}
		}
		
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
		}		
		if (lbStatus == null) {
		    lbStatus = new JLabel("Status line: disconnected");
		}
		
		lbIp.setText("IP:");
		lbPort.setText("Port:");

		top.add(lbIp);
		top.add(tfIp);
		top.add(lbPort);
		top.add(tfPort);
		top.add(btConnect);
		top.add(btDisConnect);
		top.add(checkUpdAll);
		
		lbRemConLine1 = new JLabel(" ");
		lbRemConLine2 = new JLabel(" ");
		Font font = lbRemConLine1.getFont();
		lbRemConLine1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, font.getSize()));
		lbRemConLine2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, font.getSize()));
	}

	private void setUIConnected() { 
        tfIp.setEditable(false);
        tfPort.setEditable(false);
        btConnect.setEnabled(false);
        btDisConnect.setEnabled(true);
	}
	
	private void setUIDisconnected() {
        tfIp.setEditable(true);
        tfPort.setEditable(true);
        btConnect.setEnabled(true);
        btDisConnect.setEnabled(false);  
        progressBar.setValue(0);
        lbRemConLine1.setText(" ");
        lbRemConLine2.setText(" ");
	}
	
	private void fileChooserAction(ActionEvent e) {
        if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
            System.out.println("Cancel");
            disconnect();
            updateFrame.dispatchEvent(new WindowEvent(updateFrame, WindowEvent.WINDOW_CLOSING));
        }
        if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {                   
            System.out.println("File selected: " + fileChooser.getSelectedFile());
            new Thread()
            {
                public void run() {
                    int i;
                    setUIConnected();
                    bStop = false;
                    if (!isConnected()) {
                        connect(60000);
                    }
                    if (bStop)
                        return;
                    sendUpdate(fileChooser.getSelectedFile());                    
                    try {
                        i = 60;
                        OutputStream os = socket.getOutputStream();
                        while (i>0 && !bStop) {
                            Thread.sleep(1000);
                            os.write(0);
                            lbStatus.setText("Wait till fin: " + i + "s");
                            i--;
                        }
                        if (bStop)
                            return;
                        disconnect();
                        Thread.sleep(1000);
                    } catch (SocketException e) { 
                        if (!e.getMessage().equals("Connection reset by peer: socket write error")) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(updateFrame, 
                                    "Unknown error, see console output");
                            disconnect();
                            return;
                        }
                        disconnect();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(updateFrame, 
                                "Unknown error, see console output");
                        e.printStackTrace();
                        disconnect();
                        return;
                    }
                   
                    if (fileChooser.getSelectedFile().getName().endsWith("firmware.prn")
                            && checkUpdAll.isSelected())
                    {
                        setUIConnected();
                        bStop = false;
                        connect(60000);
                        String fFw = fileChooser.getSelectedFile().getAbsolutePath();
                        String fData = fFw.replaceAll("firmware.prn", "data.prn");                        
                        sendUpdate(new File(fData));
                        try {
                            i = 60;
                            OutputStream os = socket.getOutputStream();
                            while (i>0 && !bStop) {
                                Thread.sleep(1000);
                                os.write(0);
                                lbStatus.setText("Wait till fin: " + i + "s");
                                i--;
                            }
                        } catch (SocketException e) { 
                            if (!e.getMessage().equals("Connection reset by peer: socket write error")) {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(updateFrame, 
                                        "Unknown error, see console output");
                                disconnect();
                                return;
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(updateFrame, 
                                    "Unknown error, see console output");
                            e.printStackTrace();
                            disconnect();
                            return;
                        }

                        if (bStop)
                            return;
                        disconnect();
                    }
                    
                    System.out.println("Update finished!");
                    lbStatus.setText("Update finished!");
                }
            }.start();  
        }
	}
	
	private static boolean ipValidate(final String ip) {
	    String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
	    return ip.matches(PATTERN);
	}
	
	private void connect(int iTimeout) {
	    int iNumSocketTimeout = 5000;
	    int iNumOfTrys = iTimeout/iNumSocketTimeout;
	    int i;
	    boolean bTimeout;
	       
	    String ip = tfIp.getText(); 	    
	    int port = 0;	   	    
	    
	    try {
	        port = Integer.parseInt(tfPort.getText());
	    } 
	    catch (NumberFormatException e) {	        
	    }
	    catch (Exception e)
	    {
            JOptionPane.showMessageDialog(updateFrame, 
                    "Unknown error, see console output");
	        e.printStackTrace();
	    }
	    
	    if (ip.isEmpty() || port==0 || !ipValidate(ip))
	    {
	        JOptionPane.showMessageDialog(updateFrame, 
	                "Please enter a valid IP-Address and Port Number");
	        return;
	    }
	    
	    i = 0;	    
	    do {
	        i++;
	        lbStatus.setText("Connection attempt No " + i + "...");
    	    try {   
    	        bTimeout = false;
    	        socket = new Socket();
        	    socket.connect(new InetSocketAddress(ip, port), iNumSocketTimeout);
        	    connected = true;
        	    
                startRecvTask();
                
                setUIConnected();
                                
                System.out.println("connected");
                lbStatus.setText("connected");
        	    break;
            } 
    	    catch (SocketTimeoutException e) {
    	        bTimeout = true;
            } 
    	    catch (IOException e) {
    	        if (socket != null)
    	        {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(updateFrame, 
                            "Unknown error, see console output");
    	        }
                System.out.println("disconnected");
                lbStatus.setText("disconnected");
                setUIDisconnected();
                socket = null;
                return;
            }    	    
	    } while (i<iNumOfTrys && !bStop);
	    
	    if (bTimeout) {
	        if (!bStop) {
    	        JOptionPane.showMessageDialog(updateFrame, 
    	                "Timeout! No connection established");
	        }
            System.out.println("disconnected");
            lbStatus.setText("disconnected");
            setUIDisconnected();
            socket = null;
	    }
	}
	
	private void startRecvTask() {        
        try {
            OutputStream os = socket.getOutputStream();                
            byte[] oByte;
            oByte = "^RCRA--r2_".getBytes();
            oByte[0] = 0x01;
            oByte[9] = 0x17;
            os.write(oByte);                
            oByte = "^RCRB--r1_".getBytes();
            oByte[0] = 0x01;
            oByte[9] = 0x17;
            os.write(oByte);
            oByte = "^TK_".getBytes();
            oByte[0] = 0x01;
            oByte[3] = 0x17;
            os.write(oByte);
            oByte = "^TD_".getBytes();
            oByte[0] = 0x01;
            oByte[3] = 0x17;
            os.write(oByte);                
            os.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(updateFrame, 
                    "Unknown error, see console output");
            e.printStackTrace();
        }       

        new Thread()
        {
            public void run() {
                recvStatus();
            }
        }.start();
	}

    public boolean isConnected() {
        return connected;
    }
	
	protected void disconnect() {
	    bStop = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(updateFrame, 
                        "Unknown error, see console output");
                e.printStackTrace();
            }
            socket = null;
        }
        setUIDisconnected();
        System.out.println("disconnected");
        lbStatus.setText("disconnected");
        connected = false;
    }
	
	public void sendUpdate(File updateFile) {
	    try {
	        byte[] mybytearray = new byte[(int) updateFile.length()];
	        FileInputStream fis = new FileInputStream(updateFile);
	        BufferedInputStream bis = new BufferedInputStream(fis);
	        OutputStream os = socket.getOutputStream();
	        String str = "Sending " + updateFile + "(" + mybytearray.length + " bytes)";
	        System.out.println(str);
	        lbStatus.setText(str);
	        System.out.println(Math.round(updateFile.length()/100));
	        byte[] buffer = new byte[Math.round(updateFile.length()/100)];
	        int n;
	        int i=0;
	        while ( (n=bis.read(buffer))>=0 && !bStop) {
	            os.write(buffer, 0, n);
	            progressBar.setValue(i);
	            i++;
	        }
	        os.flush();
	        System.out.println("Flushed");
	        bis.close();
        } 
	    catch (SocketException e) { 
            if (!e.getMessage().equals("Socket closed")) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(updateFrame, 
                        "Unknown error, see console output");
            }
	    }
	    catch (Exception e) {
            JOptionPane.showMessageDialog(updateFrame, 
                    "Unknown error, see console output");
	        e.printStackTrace();
	    }
	}
	
	private void doRemConOutput(byte[] inBytes, CVPLSTATE sState, int iStartCmd, 
	                            int iEndCmd, int iRemConLineSep) {
        if (sState == CVPLSTATE.SEARCHSOH && iEndCmd>iStartCmd) {
            if (iRemConLineSep>iStartCmd) {
                String s1 = new String(inBytes, iStartCmd, iRemConLineSep-iStartCmd);
                lbRemConLine1.setText(s1);
                // System.out.println("'" + s1 + "'");
                if (iEndCmd>iRemConLineSep) {
                    String s2 = new String(inBytes, iRemConLineSep+1, iEndCmd-iRemConLineSep-1);
                    lbRemConLine2.setText(s2);
                    // System.out.println("'" + s2 + "'");
                }
            }
            else {
                String s = new String(inBytes, iStartCmd, iEndCmd-iStartCmd);
                lbRemConLine1.setText(s);
                // System.out.println("'" + s + "'");
            }
        }
	}
	
    private void recvStatus() {
        byte[] inBytes = new byte[1024];
        CVPLSTATE sState = CVPLSTATE.SEARCHSOH;
        byte soh = 0x01;
        byte etb = 0x17;
        int iLen;
        int iStartCmd = 0;
        int iEndCmd = 0;
        int iRemConLineSep = 0;
        int i;
        
        System.out.println("recvStatus started");
        
        try {
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            while (!bStop) {
                iLen = in.read(inBytes);

                for (i=0; i<iLen; i++) {
                                        
                    switch (sState)
                    {
                        case SEARCHSOH:
                            if (inBytes[i] == soh)
                            {
                                iStartCmd = i;
                                iEndCmd = 0;
                                iRemConLineSep = 0;
                                sState = CVPLSTATE.SOHFOUND_ANALYSE_1ST_CMD_SIGN;
                            }
                            break;

                        case SOHFOUND_ANALYSE_1ST_CMD_SIGN:
                            switch (inBytes[i])
                            {
                                case 'T':
                                    sState = CVPLSTATE.REMCON_TD_ANALYZE_2ND_CMD_SIGN;
                                    break;

                                default:
                                    sState = CVPLSTATE.WAIT4ETB;
                                    break;
                            }
                            break;

                        case REMCON_TD_ANALYZE_2ND_CMD_SIGN:
                            if (inBytes[i] == 'D')
                            {
                                sState = CVPLSTATE.REMCON_TD_ANALYZE_3_SEPERATOR;
                            }
                            else
                            {
                                sState = CVPLSTATE.SEARCHSOH;
                                iEndCmd = i+1;
                            }
                            break;

                        case REMCON_TD_ANALYZE_3_SEPERATOR:
                            if (inBytes[i] == '"')
                            {
                                sState = CVPLSTATE.REMCON_TD_GET_DISPLAY_CONTENT;
                                iStartCmd = i+1;
                            }
                            else
                            {
                                sState = CVPLSTATE.SEARCHSOH;
                                iEndCmd = i+1;
                            }
                            break;

                        case REMCON_TD_GET_DISPLAY_CONTENT:
                            if (inBytes[i] == '\n') {
                                iRemConLineSep = i;
                            }
                            if (inBytes[i] == '"')
                            {
                                sState = CVPLSTATE.REMCON_WAIT4ETB_ETB;
                                iEndCmd = i;
                            }
                            break;

                        case REMCON_WAIT4ETB_ETB:
                            if (inBytes[i] == etb)
                            {
                                sState = CVPLSTATE.SEARCHSOH;
                                doRemConOutput(inBytes, sState, iStartCmd, 
                                        iEndCmd, iRemConLineSep);
                            }
                            break;
                            
                        case WAIT4ETB: // Wait for ETB
                            if (inBytes[i] == etb)
                            {
                                sState = CVPLSTATE.SEARCHSOH;
                                iEndCmd = i+1;
                            }
                            break;
                    }                    
                    
                }
            }
        } catch (SocketException e) { 
            if (!e.getMessage().equals("Socket closed")) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(updateFrame, 
                        "Unknown error, see console output");
            }
        } catch (SocketTimeoutException e) { 

        } catch (IOException e) {            
            e.printStackTrace();
            JOptionPane.showMessageDialog(updateFrame, 
                    "Unknown error, see console output");
        }
        
        System.out.println("recvStatus finished");
    }
}
