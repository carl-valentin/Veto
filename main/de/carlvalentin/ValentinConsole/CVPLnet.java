package de.carlvalentin.ValentinConsole;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import java.awt.HeadlessException;

/**
 * &Uuml;bertr&auml;gt Daten &uuml;ber das Netzwerk, und setzt automatisch
 * ein Startzeichen an den Anfang des Datenstroms und ein Stopzeichen 
 * an das Ende des Datenstroms, gem&auml;&szlig; der 
 * Carl Valentin Printer Language (CVPL).
 */
public class CVPLnet {
	private Socket 			   lk_socket2Printer;
	private InputStreamReader  lk_cStreamFromPrinter;	
	private OutputStreamWriter lk_cStream2Printer;
    private Writer             lk_WriterStatus;
    private boolean            lk_bCloseConn = false;
	
	/**
	 * This is the default constructor
	 */
	private CVPLnet() {
		
	}
    
    /**
     * @param writerStatus Ein Writer auf den Loggingausgaben gemacht werden
     *                     k&ouml;nnen.
     */
    public CVPLnet(Writer writerStatus) {
        lk_WriterStatus = writerStatus;
    }
	
    private void writeStatus(String s) {
        try {
            lk_WriterStatus.write(s);
        }
        catch (IOException ex) {
            System.err.println("I/O Excexption while write to StatusWriter");
        }
        catch (HeadlessException ex) {
        	System.err.println(ex);
        }
    }
    
    private void writeError(String s) {
        try {
            lk_WriterStatus.write(s);
            JOptionPane.showMessageDialog(null, s, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) {
            System.err.println("I/O Excexption while write to StatusWriter");
        }
    }
    
	public boolean openConnect(String strIPAddr, int iPort) {		

		writeStatus("Try to connect to printer " + strIPAddr + 
				    " on Port " + iPort + "...");

		try {
			lk_socket2Printer = new Socket(strIPAddr, iPort);
			lk_cStreamFromPrinter  = 
				new InputStreamReader(
					lk_socket2Printer.getInputStream(), "US-ASCII");						
			lk_cStream2Printer = 
				new OutputStreamWriter(
					lk_socket2Printer.getOutputStream(), "US-ASCII");
		}
		catch (UnsupportedEncodingException ex) {
			writeError("Encoding US-ASCII is not supported");
			return false;			
		}
		catch (UnknownHostException ex) {
			writeError("Don't know about host " + strIPAddr);
			return false;
		} 
		catch (IOException ex) {
			writeError("Couldn't get I/O for the connection to " + 
						strIPAddr);
			return false;
		}
		catch (SecurityException ex) {
			writeError("Connection to " + strIPAddr + "forbidden");
			return false;
		}
		writeStatus("Connected to device");
		return true;
	}
	
    /**
     * 
     * @return Reader mit einem Stream vom Drucker
     */
	public Reader getNetworkReader() {
		return lk_cStreamFromPrinter;
	}
    
    /**
     * 
     * @return
     */
    public Writer getNetworkWriter()
    {
        return lk_cStream2Printer;
    }
	
	public void write(String string2Write, SohEtb sohEtb) {
		try {
			lk_cStream2Printer.write(sohEtb.gl_strSOH + string2Write + 
									 sohEtb.gl_strETB, 0, 
									 string2Write.length()+2);
			lk_cStream2Printer.flush();
		}
		catch (IOException ex) {
			writeError("I/O Exception occured while trying to " + 
						"send data");
		}
	}
	
    /**
     * Schreibt die Daten ohne CVPL, f&uuml;r Daten die schon in CVPL sind.
     * (Ist jetzt eine Schnelll&ouml;sung und irgendwie nicht sauber,  
     *  typischer Quck'n'Dirty-Kot.
     *  Hier m&uuml;sste man sich mal generell ein anderes 
     *  Klassendesign &uuml;berlegen.)
     */
    public void writeRaw(byte[] bData, int iDataLength) {
        try {
            BufferedOutputStream cStream2Printer = 
                new BufferedOutputStream(
                    lk_socket2Printer.getOutputStream());
            
            cStream2Printer.write(bData, 0, iDataLength);
            cStream2Printer.flush();
        }
        catch (IOException ex) {
            writeError("I/O Exception occured while trying to " + 
                        "send data");
        }
    }    

	public String read(SohEtb sohEtb) {
		String strFrame = new String();
		String strC;
		int iC;
		int i = 0;
		try {
			do {
				iC = lk_cStreamFromPrinter.read();
			} while (iC != sohEtb.gl_iSOH);
			strFrame += (char)iC;
			do {
				iC = lk_cStreamFromPrinter.read();
				strFrame += (char)iC;
			} while (iC != sohEtb.gl_iETB);
			return strFrame;
		}
		catch (IOException ex) {
            if (!lk_bCloseConn) {
                writeError("I/O Exception occured while trying to " + 
                           "receive data");
            }
            lk_bCloseConn = false;
		}
		return null;
	}
	
	public void closeConnect() {
		try {
            lk_bCloseConn = true;
			lk_cStream2Printer.close();
			lk_cStreamFromPrinter.close();
			lk_socket2Printer.close();						
		}
		catch (IOException ex) {
			writeStatus(ex.toString());
			return;	
		}	
		writeStatus("Connection to printer closed");
	}
}
