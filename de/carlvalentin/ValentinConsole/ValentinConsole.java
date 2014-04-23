package de.carlvalentin.ValentinConsole;

import java.io.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import pnuts.tools.Console;
/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung um im 
 * Carl Valentin Printer Language-Format (CVPL) mit einem Device 
 * kommunizieren zu k&ouml;nnen. 
 */
public class ValentinConsole extends JFrame {
	
    private SendThread sendThread;
    private class SendThread extends Thread {
        public void run() {
            char c;
            Thread thisThread = Thread.currentThread();
            Reader readerConsole = console.getReader();
            while (sendThread == thisThread) {
                try {               
                    c = (char)readerConsole.read();                 
                    switch (c) {
                        case '\n':
                            cvplNet.write(strLastLine, sohEtb);
                            strLastLine = "";
                            break;
                            
                        case '\b':
                            strLastLine = 
                                strLastLine.substring(
                                    0, strLastLine.length()-1); 
                            break;
                            
                        default:
                            strLastLine += c;
                            break;
                    }                   
                }
                catch (IOException ex) {
                    writeError("Console I/O Exception");
                }
            }
        }
    }    
    
	private RecvThread recvThread;
	private class RecvThread extends Thread {
		public void run() {
            String str;
			Thread thisThread = Thread.currentThread();
			java.io.Writer writerConsole = console.getWriter();
			while (recvThread == thisThread) {
				try {
                    str = cvplNet.read(sohEtb);
                    if (str != null)
                    {
                    	writerConsole.write(str + "\n");
                    	writerConsole.flush();
                    }
				}
				catch (IOException ex) {
					writeError("Console I/O Exception");
				}					                   
			}
		}
	}	
	
    private StatusWriter writerStatus = new StatusWriter();    
	private CVPLnet cvplNet = new CVPLnet(writerStatus);
	private String strLastLine = new String();
	private SohEtb sohEtb = SohEtb.x0117;
	private Console console = new Console();

	private javax.swing.JPanel jPanelMain = null;
	private javax.swing.JPanel jPanelTop = null;
	private javax.swing.JScrollPane jScrollPaneConsole = null;
	private javax.swing.JPanel jPanelBottom = null;	
	private javax.swing.JTextField jTextFieldIPAddr = null;
	private javax.swing.JButton jButton = null;
	private javax.swing.JLabel jLabelIPAddr = null;
	private javax.swing.JRadioButton jrb0117 = null;
	private javax.swing.JRadioButton jrb5E5F = null;
	
	private javax.swing.JLabel jLabelStatus = null;    
    
	public static void main(String[] args) {
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
        
		this.setContentPane(getJPanelMain());
		this.setSize(600, 600);
		this.setTitle("ValentinConsole");
		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		
		javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
		group.add(jrb0117);
		group.add(jrb5E5F);
        
        sendThread = new SendThread();
        sendThread.start();
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
			jPanelMain.add(getJPanelTop(), java.awt.BorderLayout.NORTH);
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
	private javax.swing.JPanel getJPanelTop() {
		if(jPanelTop == null) {
			jPanelTop = new javax.swing.JPanel();
			jPanelTop.add(getJLabelIPAddr(), null);
			jPanelTop.add(getJTextFieldIPAddr(), null);
			jPanelTop.add(getJButton(), null);
			jPanelTop.add(getJrb0117(), null);
			jPanelTop.add(getJrb5E5F(), null);
			jPanelTop.setPreferredSize(new java.awt.Dimension(20,36));
		}
		return jPanelTop;
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
    
    private void doConnect() {
        if (jTextFieldIPAddr.isEnabled()) {
            jButton.setEnabled(false);
            new Thread(new Runnable() {
                public void run(){
                	String[] strIPTok = jTextFieldIPAddr.getText().split(":");
                	String strIP = strIPTok[0];
                	int iPort;
                	if (strIPTok.length > 1) { 
                		iPort = Integer.parseInt(strIPTok[1]);
                	}
                	else {
                		iPort = 9100;
                	}
                    if (cvplNet.openConnect(strIP, iPort)) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {   
                                jTextFieldIPAddr.setEnabled(false);
                                jButton.setText("Disconnect");
                                jrb0117.setEnabled(false);
                                jrb5E5F.setEnabled(false);
                                console.getTextArea().setEnabled(true);
                            }
                        });
                        recvThread = new RecvThread();
                        recvThread.start();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jButton.setEnabled(true);
                        }
                    });
                }
            }).start();
        }
        else {
            recvThread = null;
            cvplNet.closeConnect();
            console.getTextArea().setEnabled(false);
            jrb0117.setEnabled(true);
            jrb5E5F.setEnabled(true);
            jButton.setText("Connect");
            jTextFieldIPAddr.setEnabled(true);                                                                      
        }
    }
    
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		if(jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setText("Connect");
			jButton.setPreferredSize(new java.awt.Dimension(99,27));
			jButton.setActionCommand("Connect");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doConnect();
				}
			});
		}
		return jButton;
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
	private javax.swing.JRadioButton getJrb0117() {
		if(jrb0117 == null) {
			jrb0117 = new javax.swing.JRadioButton();
			jrb0117.setText("01/17");
			jrb0117.setRolloverEnabled(false);
			jrb0117.setSelected(true);
			jrb0117.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					sohEtb = SohEtb.x0117;
				}
			});
		}
		return jrb0117;
	}
	/**
	 * This method initializes jrb5E5F
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private javax.swing.JRadioButton getJrb5E5F() {
		if(jrb5E5F == null) {
			jrb5E5F = new javax.swing.JRadioButton();
			jrb5E5F.setText("5E/5F");
			jrb5E5F.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					sohEtb = SohEtb.x5E5F;
				}
			});
		}
		return jrb5E5F;
	}
	/**
	 * This method initializes jLabelStatus
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabelStatus() {
		if(jLabelStatus == null) {
			jLabelStatus = new javax.swing.JLabel();
			jLabelStatus.setText("Status");
		}
		return jLabelStatus;
	}

    private void writeError(String s) {
        try {
            writerStatus.write(s);
            JOptionPane.showMessageDialog(null, s, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) {
            System.err.println("I/O Excexption while write to StatusWriter");
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
        
        private void flushBuffer() {
            final String str = buf.toString();
            buf.setLength(0);
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                	jLabelStatus.setText(str);
                }
            });
        }
    }
}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
