package de.carlvalentin.Common;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class CVCommandFile {
	private String strConfigFileName;
	private List list = Collections.synchronizedList(new LinkedList());

	private class Command_Element {
		public String strID;
		public String strValue;

		private Command_Element() {
			return;
		}

		public Command_Element(String _strID, String _strValue) {
			strID = _strID;
			strValue = _strValue;

			return;
		}
	}

	private void writeError(String s) {
		try {
			JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
		} catch (java.awt.HeadlessException ex) {
			System.err.println(ex);
		}

		return;
	}

	private CVCommandFile() {
		return;
	}

	public CVCommandFile(String strName) {
		strConfigFileName = strName + ".cfg";

		try {
			FileReader fileConfig = new FileReader(strConfigFileName);
			StringBuffer inBuf = new StringBuffer();
			String strID = null;
			int i;
			char c;

			while ((i = fileConfig.read()) > 0) {
				c = (char) i;
				if (c == '#') {
					if ((i = fileConfig.read()) > 0) {
						c = (char) i;
						if (c == '#') {
							while ((i = fileConfig.read()) > 0) {
								c = (char) i;
								if (c == '\n') {
									break;
								}
							}
							inBuf = new StringBuffer();
						} else {
							if (c == '\t') {
								strID = inBuf.toString();
								inBuf = new StringBuffer();
							} else if (c == '\n') {
								Command_Element cfgElement = new Command_Element(strID, inBuf.toString());
								list.add(cfgElement);
								inBuf = new StringBuffer();
							} else {
								inBuf.append('#');
								inBuf.append(c);
							}
						}
					} else {
						inBuf.append(c);
					}
				} else if (c == '\t') {
					strID = inBuf.toString();
					inBuf = new StringBuffer();
				} else if (c == '\n') {
					Command_Element cfgElement = new Command_Element(strID, inBuf.toString());
					list.add(cfgElement);
					inBuf = new StringBuffer();
				} else {
					inBuf.append(c);
				}
			}

			fileConfig.close();
		} catch (FileNotFoundException ex) {

		} catch (IOException ex) {
			System.err.println(ex);
			writeError("I/O error while trying to read from file " + strConfigFileName);
		}

		return;
	}

	/**
	 * Liest den Wert zur ID <code>strID</code> aus der
	 * "Cookie"-/Konfigurationsdatei ein.
	 * 
	 * @return <code>null</code> falls die ID noch nicht in der Datei gefunden
	 *         wurde, sonst den Wert als String
	 */
	public String getConfig(String strID) {
		Object objElement[] = list.toArray();
		for (int i = 0; i < list.size(); i++) {
			Command_Element cfgElement = (Command_Element) objElement[i];
			if (strID.compareTo(cfgElement.strID) == 0) {
				return cfgElement.strValue;
			}
		}
		return null;
	}

	public List getCompleteConfigCmd() {
		List listID = new LinkedList();
		Object objElement[] = list.toArray();
		for (int i = 0; i < list.size(); i++) {
			Command_Element cfgElement = (Command_Element) objElement[i];
			listID.add(cfgElement.strID);
		}
		return listID;
	}

	public List getCompleteConfigDescr() {
		List listCmd = new LinkedList();
		Object objElement[] = list.toArray();
		for (int i = 0; i < list.size(); i++) {
			Command_Element cfgElement = (Command_Element) objElement[i];
			listCmd.add(cfgElement.strValue);
		}
		return listCmd;
	}

	/**
	 * Speichert den Wert <code>strValue</code> in der "Cookie"-/Konfigurationsdatei
	 * unter der ID <code>strID</code>. M.H. der ID kann man den Wert mittels
	 * {@link #getConfig} wieder einlesen.
	 */
	public boolean setConfig(String strID, String strValue) {
		Object objElement[] = list.toArray();
		for (int i = 0; i < list.size(); i++) {
			Command_Element cfgElement = (Command_Element) objElement[i];
			if (strID.compareTo(cfgElement.strID) == 0) {
				cfgElement.strValue = strValue;
				writeConfig();
				return true;
			}
		}

		Command_Element cfgElement = new Command_Element(strID, strValue);
		list.add(cfgElement);
		writeConfig();
		return true;
	}

	private void writeConfig() {
		try {
			FileWriter fileConfig = new FileWriter(strConfigFileName);
			Object objElement[] = list.toArray();
			for (int i = 0; i < list.size(); i++) {
				Command_Element cfgElement = (Command_Element) objElement[i];
				fileConfig.write(cfgElement.strID + "\t" + cfgElement.strValue + "\n");
			}
			fileConfig.close();
		} catch (IOException ex) {
			System.err.println(ex);
			writeError("I/O error while trying to write to file " + strConfigFileName);
		}

		return;
	}
}
