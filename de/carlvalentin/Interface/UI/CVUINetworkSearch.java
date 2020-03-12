package de.carlvalentin.Interface.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

public class CVUINetworkSearch {

	JDialog jFrameSelectPrinters = null;
	int port = 9100;
	String bdcMessage = "\u0001RCLX---FF:FF:FF:FF:FF:FF UBB\u0017" + "\n\r" + "^RCLX---FF:FF:FF:FF:FF:FF UBB_";
	String Adress = null;
	Thread broadcast = null;
	static ArrayList<String> printerIpWithName = new ArrayList<String>();
	JList printerList = null;
	static DefaultListModel listModel = new DefaultListModel();
	static JLabel title = null;
	static JButton btnOk = null;
	static JButton btnSearchAgain = null;

	public CVUINetworkSearch() {
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
		List<InetAddress> broadcastList = null;
		try {
			broadcastList = listAllBroadcastAddresses();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (broadcastList != null && !broadcastList.isEmpty()) {
			Object[] broadcastArray = broadcastList.toArray();
			// System.out.println(bdcMessage);
			for (int i = 0; i < broadcastArray.length; i++) {
				Adress = broadcastArray[i].toString();
				Adress = Adress.substring(1);
				broadcast = new BroadcastThread(Adress, port, bdcMessage, 5000);
				broadcast.start();
			}
		}
	}

	public void showPrinters(JDialog owner) {
		jFrameSelectPrinters = new JDialog(owner);
		jFrameSelectPrinters.setTitle("Printer search");

		jFrameSelectPrinters.setLayout(new BorderLayout());
		title = new JLabel("Searching for printers ... please wait");
		jFrameSelectPrinters.add(title, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new java.awt.Dimension(400, 400));
		printerList = new JList(listModel);
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
		String selectedPrinter = printerIpWithName.get(index);
		selectedPrinter = selectedPrinter.substring(selectedPrinter.lastIndexOf(':')+2);
		if (CVUINetwork.jTextFieldTCPUDPIPAdress != null) {
			CVUINetwork.jTextFieldTCPUDPIPAdress.setText(selectedPrinter);
			jFrameSelectPrinters.dispose();
		}
	}

	private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
		List<InetAddress> broadcastList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}
			networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}
		return broadcastList;
	}

}

class BroadcastThread extends Thread {
	InetAddress adress = null;
	int port;
	String broadcastMessage = null;
	int timeout;

	public BroadcastThread(String adress, int port, String broadcastMessage, int timeout) {
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
			CVUINetworkSearch.printerIpWithName.add(name);
		}
		socket.close();
		//System.out.println("Socket geschlossen");
		CVUINetworkSearch.title.setText("Printer List: ");
		if (!CVUINetworkSearch.printerIpWithName.isEmpty()) {
			CVUINetworkSearch.listModel.removeAllElements();
			for (int i = 0; i < CVUINetworkSearch.printerIpWithName.size(); i++) {
				CVUINetworkSearch.listModel.addElement(CVUINetworkSearch.printerIpWithName.get(i));
			}
		}
		CVUINetworkSearch.btnOk.setEnabled(true);
		CVUINetworkSearch.btnSearchAgain.setEnabled(true);
	}
}
