package de.carlvalentin.Interface.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;

public class CVUINetworkSearch6 {

	JDialog jFrameSelectPrinters = null;
	int port = 9101;
	String bdcMessage = "\u0001RCLX---FF:FF:FF:FF:FF:FF UBB\u0017" + "\n\r" + "^RCLX---FF:FF:FF:FF:FF:FF UBB_";
	String Adress = "ff02::1";
	Thread broadcast = null;
	static ArrayList<String> printerIpWithName = new ArrayList<String>();
	JList<String> printerList = null;
	static DefaultListModel<String> listModel = new DefaultListModel<String>();
	static JLabel title = null;
	static JButton btnOk = null;
	static JButton btnSearchAgain = null;

	public CVUINetworkSearch6() {
	}

	public boolean windowExist() {
		if (jFrameSelectPrinters == null) {
			return false;
		} else {
			if (!jFrameSelectPrinters.isVisible()) {
				jFrameSelectPrinters = null;
				return false;
			}
			return true;
		}
	}

	public void searchPrinters() {

		broadcast = new BroadcastThread6(Adress, port, bdcMessage, 5000);
		broadcast.start();
	}

	public void showPrinters(JDialog owner) {
		jFrameSelectPrinters = new JDialog(owner);
		jFrameSelectPrinters.setTitle("Printer search IPv6");

		jFrameSelectPrinters.setLayout(new BorderLayout());
		title = new JLabel("Searching for printers ... please wait");
		jFrameSelectPrinters.add(title, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new java.awt.Dimension(400, 400));
		printerList = new JList<String>(listModel);
		scrollPane.setViewportView(printerList);
		jFrameSelectPrinters.add(scrollPane, BorderLayout.CENTER);
		btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.setEnabled(false);
		btnOk.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				btnOk();
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(btnOk);
		btnSearchAgain = new JButton();
		btnSearchAgain.setText("Search again");
		btnSearchAgain.setEnabled(false);
		btnSearchAgain.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				title.setText("Searching for printers ... please wait");
				listModel.removeAllElements();
				printerIpWithName.clear();
				searchPrinters();
			}
		});
		buttonPanel.add(btnSearchAgain);
		jFrameSelectPrinters.add(buttonPanel, BorderLayout.SOUTH);
		jFrameSelectPrinters.pack();
		jFrameSelectPrinters.setMinimumSize(jFrameSelectPrinters.getSize());
		// jFrameSelectPrinters.setBounds(0, 0, 400, 300);
		jFrameSelectPrinters.setVisible(true);
		jFrameSelectPrinters.setModal(true);
	}

	private void btnOk() {
		int index = printerList.getSelectedIndex();
		String selectedPrinter = (String) printerIpWithName.get(index);
		selectedPrinter = selectedPrinter.substring(selectedPrinter.lastIndexOf(' ')+1);
		if (CVUINetwork.jTextFieldTCPUDPIPAdress != null) {
			if (selectedPrinter.lastIndexOf('%') != -1 && selectedPrinter.charAt(selectedPrinter.lastIndexOf('%')) == '%') {
				selectedPrinter =
						selectedPrinter.substring(selectedPrinter.lastIndexOf(' ')+1,
								selectedPrinter.length()-(selectedPrinter.length()-selectedPrinter.lastIndexOf('%')));
			}
			CVUINetwork.jTextFieldTCPUDPIPAdress.setText(selectedPrinter);
			jFrameSelectPrinters.dispose();
		}
	}
}

class BroadcastThread6 extends Thread {
	InetAddress adress = null;
	int port;
	String broadcastMessage = null;
	int timeout;

	public BroadcastThread6(String adress, int port, String broadcastMessage, int timeout) {
		try {
			this.adress = InetAddress.getByName(adress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = port;
		this.broadcastMessage = broadcastMessage;
		this.timeout = timeout;
	}

	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		byte[] buffer = broadcastMessage.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adress, port);

		byte[] receiveData = new byte[2048];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		try {
			socket.setSoTimeout(timeout);
			socket.send(packet);
			System.out.print(adress);
			System.out.println(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				if (e instanceof java.net.SocketTimeoutException) {
					break;
				}
				e.printStackTrace();
			}

			String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			String recieved[] = sentence.split(";");
			String name = recieved[2].substring(4);
			name = "Name: "+name + ", IP-Address: " + receivePacket.getAddress().toString().substring(1);
			CVUINetworkSearch6.printerIpWithName.add(name);
		}
		socket.close();
		//System.out.println("Socket geschlossen");
		CVUINetworkSearch6.title.setText("Printer List: ");
		if (!CVUINetworkSearch6.printerIpWithName.isEmpty()) {
			//CVUINetworkSearch6.listModel.removeAllElements();
			CVUINetworkSearch6.listModel.clear();

			for(String element : CVUINetworkSearch6.printerIpWithName){

				CVUINetworkSearch6.listModel.addElement(element);
			}
		}
		CVUINetworkSearch6.btnOk.setEnabled(true);
		CVUINetworkSearch6.btnSearchAgain.setEnabled(true);
	}
}
