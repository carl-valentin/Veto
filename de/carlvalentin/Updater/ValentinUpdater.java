package de.carlvalentin.Updater;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import de.carlvalentin.Common.CVConfigFile;

public class ValentinUpdater {

	Network network;

	JFrame updateFrame;
	JPanel GUI;

	JLabel lbIp;
	JLabel lbPort;
	JTextField tfIp;
	JTextField tfPort;
	JButton btConnect;
	JButton btDisConnect;
	JFileChooser fc;
	JProgressBar progressBar;
	
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
		if (network == null) {
			network = new Network(this);
		}
		updateFrame = new JFrame();
		updateFrame.setSize(700, 400);
		updateFrame.setTitle("Valentin Updater");
		updateFrame.setLayout(new BorderLayout());
		GUI = new JPanel();
		showGUI();
		updateFrame.add(GUI, BorderLayout.CENTER);
		updateFrame.setVisible(true);
	}

	private void showGUI() {
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
				@Override
				public void actionPerformed(ActionEvent e) {
					if(configFile!= null) {
						configFile.setConfig(KEYLASTUPDATEIP, tfIp.getText());
					}
					connect();
				}
			});
		}
		if (btDisConnect == null) {
			btDisConnect = new JButton();
			btDisConnect.setText("Disconnect");
			btDisConnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					disconnect();
				}
			});
		}
		if (fc == null) {
			fc = new JFileChooser();
			File testPath = new File(folderPath);
			if (testPath.exists()) {
				fc.setCurrentDirectory(testPath);
			}
		}
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
		}

		lbIp.setText("IP:");
		lbPort.setText("Port:");

		GUI.add(lbIp);
		GUI.add(tfIp);
		GUI.add(lbPort);
		GUI.add(tfPort);
		GUI.add(btConnect);
	}

	protected void disconnect() {
		if (network.disconnect()) {
			updateFrame.dispatchEvent(new WindowEvent(updateFrame, WindowEvent.WINDOW_CLOSING));
		}
	}

	private void connect() {
		if (!tfIp.getText().isEmpty() && !tfPort.getText().isEmpty()) {
			if (network.connect(tfIp.getText(), Integer.parseInt(tfPort.getText()))) {
				tfIp.setEditable(false);
				tfPort.setEditable(false);
				btConnect.setEnabled(false);
				btDisConnect.setEnabled(true);
				fc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
							System.out.println("Cancel");
							disconnect();
							updateFrame.dispatchEvent(new WindowEvent(updateFrame, WindowEvent.WINDOW_CLOSING));
						}
						if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
							fc.setVisible(false);
							updateSelected(fc.getSelectedFile());
						}
					}
				});
				GUI.add(btDisConnect);
				GUI.add(progressBar);
				GUI.add(fc);
				updateFrame.revalidate();
			} else {
				if (network.socket != null) {
					if (network.socket.isConnected()) {
						System.out.println("Already connected");
						JOptionPane.showMessageDialog(null, "Already connected");
					}
				} else {
					System.out.println("Not Connected");
					JOptionPane.showMessageDialog(null, "Not Connected");
				}
			}
		} else {
			System.out.println("No Values permitted");
			JOptionPane.showMessageDialog(null, "No Values permitted");
		}
	}

	private void updateSelected(File selectedFile) {
		System.out.println("File selected: " + selectedFile);
		btDisConnect.setEnabled(false);
		new Thread()
		{
		    public void run() {
		    	if (!network.sendUpdate(selectedFile)) {
					disconnect();
					connect();
				}
		    }
		}.start();
	}
	
	public void setProcess(int percentage) {
		progressBar.setValue(percentage);
	}
}

class Network {
	ValentinUpdater valentinUpdater;
	Socket socket;

	public Network(ValentinUpdater valentinUpdater) {
		this.valentinUpdater = valentinUpdater;
	}

	public boolean connect(String ip, int port) {
		try {
			socket = new Socket(ip, port);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean disconnect() {
		if (socket != null) {
			try {
				socket.close();
				
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	public boolean sendUpdate(File updateFile) {
		try {
			byte[] mybytearray = new byte[(int) updateFile.length()];
			FileInputStream fis = new FileInputStream(updateFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			OutputStream os = socket.getOutputStream();
			System.out.println("Sending " + updateFile + "(" + mybytearray.length + " bytes)");
			System.out.println(Math.round(updateFile.length()/100));
			byte[] buffer = new byte[Math.round(updateFile.length()/100)];
		    int n;
		    int i=0;
		    while ((n = bis.read(buffer)) >= 0) {
		      os.write(buffer, 0, n);
		      valentinUpdater.setProcess(i);
		      i++;
		    }
			os.flush();
			System.out.println("Flushed");
			valentinUpdater.disconnect();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
