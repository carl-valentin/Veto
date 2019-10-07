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
import javax.swing.JTextField;

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

	String folderPath = "P:\\Firmware\\Freigaben";

	public ValentinUpdater() {
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
		updateFrame.setSize(600, 400);
		updateFrame.setTitle("Valentin Updater");
		updateFrame.setLayout(new BorderLayout());
		GUI = new JPanel();
		showGUI();
		updateFrame.add(GUI, BorderLayout.CENTER);
		updateFrame.setVisible(true);
		// updateFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO evtl
		// auskommentieren oder aendern
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

		lbIp.setText("IP:");
		lbPort.setText("Port:");

		GUI.add(lbIp);
		GUI.add(tfIp);
		GUI.add(lbPort);
		GUI.add(tfPort);
		GUI.add(btConnect);
		GUI.add(fc);
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
				fc.setVisible(true);
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
				GUI.remove(fc);
				GUI.add(btDisConnect);
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
		if (!network.sendUpdate(selectedFile)) {
			disconnect();
			connect();
		}
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
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = socket.getOutputStream();
			System.out.println("Sending " + updateFile + "(" + mybytearray.length + " bytes)");
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
			System.out.println("Flushed");
			valentinUpdater.disconnect();
			// valentinUpdater.updateFrame.dispatchEvent(new
			// WindowEvent(valentinUpdater.updateFrame, WindowEvent.WINDOW_CLOSING));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
