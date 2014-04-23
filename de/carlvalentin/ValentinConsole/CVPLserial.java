package de.carlvalentin.ValentinConsole;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.comm.*;
import javax.swing.*;

/**
 * &Uuml;bertr&auml;gt Daten &uuml;ber die serielle Schnittstelle, und 
 * setzt automatisch ein Startzeichen an den Anfang des Datenstroms 
 * und ein Stopzeichen an das Ende des Datenstroms, gem&auml;&szlig; der 
 * Carl Valentin Printer Language (CVPL).
 */
public class CVPLserial implements SerialPortEventListener
{
    private CommPortIdentifier lk_cCommPortIdentifier;
    private boolean            lk_bCommPortIdentified = false;
    private Vector             lk_cCommPortVector;
    
    private SerialPort         lk_cSerialPort;
    private String             lk_szSerialPortName;
    private boolean            lk_bSerialPortOpen = false;
    private int                lk_iSerialPortDataBits;
    private int                lk_iSerialPortParity;
    private int                lk_iSerialPortBaudrate;
    private int                lk_iSerialPortStopBits;
    
    private Writer             lk_WriterStatus;
    
    private InputStreamReader  lk_cSerialPortInputStream;
    private OutputStreamWriter lk_cSerialPortOutputStream;
    
    private JCheckBox          lk_cCBEventCarrierDetect;
    private JCheckBox          lk_cCBEventClearTosSend;
    private JCheckBox          lk_cCBEventDataAvailable;
    private JCheckBox          lk_cCBEventDataSetReady;
    
    /**
     * Konstrukot der Klasse CVPLSerial.
     * 
     * @param writerStatus Ein Writer auf den Loggingausgaben gemacht werden
     *                     k&ouml;nnen.
     */
    public CVPLserial(Writer writerStatus) 
    {
        this.lk_WriterStatus = writerStatus;
        
        // keine Checkboxen fuer Events
        this.lk_cCBEventCarrierDetect = null;
        this.lk_cCBEventClearTosSend  = null;
        this.lk_cCBEventDataAvailable = null;
        this.lk_cCBEventDataSetReady  = null;
        
        // setzen Defaultwerte
        this.lk_iSerialPortDataBits = SerialPort.DATABITS_8;
        this.lk_iSerialPortParity   = SerialPort.PARITY_NONE;
        this.lk_iSerialPortBaudrate = 57600;
        this.lk_iSerialPortStopBits = SerialPort.STOPBITS_2;
    }
    
    /**
     * Gibt eine Statusmeldung in der Statuszeile des Programms aus.
     * 
     * @param s Inhalt der Statusmeldung
     */
    private void writeStatus(String s) 
    {
        try 
        {
            lk_WriterStatus.write(s);
        }
        catch (IOException ex) 
        {
            System.err.println("CVPLserial: I/O Exception writeStatus: " 
                    + ex.getMessage());
            this.writeError(   "CVPLserial: I/O Exception writeStatus: " 
                    + ex.getMessage());
        }
        catch (HeadlessException ex) 
        {
            System.err.println("CVPLserial: Headless Exception writeStatus: " 
                    + ex.getMessage());
            this.writeError(   "CVPLserial: Headless Exception writeStatus: " 
                    + ex.getMessage());
        }
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
            this.lk_WriterStatus.write(s);
            JOptionPane.showMessageDialog(
                    null, s, "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) 
        {
            System.err.println("CVPLserial: I/O Excexption writeError: " 
                    + ex.getMessage());
        }
    }
    
    /**
     * Sucht alle im System vorhandenen seriellen Schnittstellen.
     * 
     * <br>Alle gefundenen seriellen Schnittstellen werden in einer internen 
     * Liste gespeichert.
     * 
     * @see CVPLserial#getCommPortList()
     * @see CVPLserial#setPort(String) 
     */
    private void searchCommPorts()
    {
        this.lk_cCommPortVector = new Vector();
        this.lk_cCommPortVector.clear();
        for(Enumeration enumCommPorts = 
            this.lk_cCommPortIdentifier.getPortIdentifiers(); 
            enumCommPorts.hasMoreElements(); )
        {
            CommPortIdentifier cCurrentPort = 
                (CommPortIdentifier)enumCommPorts.nextElement();
            if(cCurrentPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                this.lk_cCommPortVector.add(cCurrentPort);
                this.writeStatus
                    ("Found serial port: " + cCurrentPort.getName());
            }
        }  
        this.lk_bCommPortIdentified = true;
        
        // setzen Defaultport
        if(this.lk_cCommPortVector.size() > 0)
        {
            CommPortIdentifier cDefaultPort =
                (CommPortIdentifier)this.lk_cCommPortVector.get(0);
            this.lk_szSerialPortName = cDefaultPort.getName();
        }
    }

    /**
     * Abfrage der Liste der im System vorhendenen seriellen Schnittstellen.
     * 
     * @return Liste der im System vorhandenen CommPorts
     * 
     * @see CVPLserial#setPort(String)
     */
    public Enumeration getCommPortList()
    {
        if(this.lk_bCommPortIdentified == false)
        {
            this.searchCommPorts();
        }
    	return this.lk_cCommPortVector.elements();
    }
    
    /**
     * Setzt den Systemnamen der augewaehlten seriellen Schnittstelle.
     * 
     * <br>Der Name ist vom System fest vorgegeben. Defaultmaessig wird der
     * erste verfuegbare Port ausgewaehlt.
     *  
     * @param szSerialPortName Name des ausgewählten Ports
     * 
     * @see CVPLserial#getCommPortList()
     */
    public void setPort(String szSerialPortName)
    {
        this.lk_szSerialPortName = szSerialPortName;
        this.writeStatus("Choose serial port: " + szSerialPortName);
    }
    
    /**
     * Setzt die Anzahl der Datenbits der seriellen Schnittstelle.
     * 
     * @param szDataBits Anzahl an Datenbits. Moegliche Werte:
     * <ul>
     * <li>"7" - sieben Datenbits</li>
     * <li>"8" - acht Datenbits - Default</li>
     * </ul>
     */
    public void setDataBits(String szDataBits)
    {
        if(szDataBits.equals((String)"7"))
        {
            this.lk_iSerialPortDataBits = SerialPort.DATABITS_7;
            this.writeStatus("Set data bits: 7");
        }
        else // "8"
        {
            this.lk_iSerialPortDataBits = SerialPort.DATABITS_8;
            this.writeStatus("Set data bits: 8");
        }
    }
    
    /**
     * Setzt die Paritaet der seriellen Schnittstelle.
     * 
     * @param szParity Parität. Moegliche Werte:
     * <ul>
     * <li>"even" - gerade Paritaet</li>
     * <li>"odd" - ungerade Paritaet</li>
     * <li>"none" - keine Paritaet - Default</li>
     * </ul>
     */
    public void setParity(String szParity)
    {
        if(szParity.equals((String)"even"))
        {
            this.lk_iSerialPortParity = SerialPort.PARITY_EVEN;
            this.writeStatus("Set parity: even");
        }
        else if(szParity.equals((String)"odd"))
        {
            this.lk_iSerialPortParity = SerialPort.PARITY_ODD;
            this.writeStatus("Set parity: odd");
        }
        else // "none"
        {
            this.lk_iSerialPortParity = SerialPort.PARITY_NONE;
            this.writeStatus("Set parity: none");
        }
    }
    
    /**
     * Setzt die Baudrate der seriellen Schnittstelle.
     * 
     * @param szBaudRate Baudrate. Moegliche Werte:
     * <ul>
     * <li>"2400 Bits/Sec"</li>
     * <li>"4800 Bits/Sec"</li>
     * <li>"9600 Bits/Sec"</li>
     * <li>"19200 Bits/Sec"</li>
     * <li>"38400 Bits/Sec"</li>
     * <li>"57600 Bits/Sec" - Default</li>
     * </ul>
     */
    public void setBaudRate(String szBaudRate)
    {
        if(szBaudRate.equals((String)"2400 Bits/Sec"))
        {
            this.lk_iSerialPortBaudrate = 2400;
            this.writeStatus("Set baud rate: 2400");
        }
        else if(szBaudRate.equals((String)"4800 Bits/Sec"))
        {
            this.lk_iSerialPortBaudrate = 4800;
            this.writeStatus("Set baud rate: 4800");
        }
        else if(szBaudRate.equals((String)"9600 Bits/Sec"))
        {
            this.lk_iSerialPortBaudrate = 9600;
            this.writeStatus("Set baud rate: 9600");
        }
        else if(szBaudRate.equals((String)"19200 Bits/Sec"))
        {
            this.lk_iSerialPortBaudrate = 19200;
            this.writeStatus("Set baud rate: 19200");
        }
        else if(szBaudRate.equals((String)"38400 Bits/Sec"))
        {
            this.lk_iSerialPortBaudrate = 38400;
            this.writeStatus("Set baud rate: 38400");
        }
        else // "57600 Bits/Sec"
        {
            this.lk_iSerialPortBaudrate = 57600;
            this.writeStatus("Set baud rate: 57600");
        }
    }
    
    /**
     * Setzt die Anzahl der Stopbits der seriellen Schnittstelle.
     * 
     * @param szStopBits Anzahl der Stopbits. Moegliche Werte:
     * <ul>
     * <li>"1" - ein Stopbit</li>
     * <li>"2" - zwei Stopbits - Default</li>
     * </ul>
     */
    public void setStopBits(String szStopBits)
    {
        if(szStopBits.equals((String)"1"))
        {
            this.lk_iSerialPortStopBits = SerialPort.STOPBITS_1;
            this.writeStatus("Set stop bits: 1");
        }
        else // "2"
        {
            this.lk_iSerialPortStopBits = SerialPort.STOPBITS_2;
            this.writeStatus("Set stop bits: 2");
        }
    }
    
    /**
     * Oeffnen der seriellen Schnittstelle.
     * 
     * @return Konnte die Schnittstelle geoeffnet werden, wird true
     * zurueckgegeben
     * 
     * @see CVPLserial#closeSerialPort()
     */
    public boolean openSerialPort()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            this.writeError("Close serial port before open it");
            return false;
        }
        
        // Gewaehlte Schnittstelle im Vektor aller Schnittstellen finden 
        CommPortIdentifier cSelectPort;
        for(int iCounter = 0; 
            iCounter < this.lk_cCommPortVector.size(); 
            iCounter++)
        {
            cSelectPort = 
                (CommPortIdentifier)this.lk_cCommPortVector.get(iCounter);
            if(this.lk_szSerialPortName.equals(cSelectPort.getName()))
            {
                try
                {
                    this.lk_cSerialPort = 
                        (SerialPort)cSelectPort.open("Veto", 500);
                }
                catch(PortInUseException ex)
                {
                    System.err.println("CVPLserial: PortInUse Exception " +
                            "openSerialPort: " + ex.getMessage());
                    this.writeError(   "CVPLserial: PortInUse Exception " +
                            "openSerialPort: " + ex.getMessage());
                    return false;
                }
                break;
            }
        }
        
        // Konfigurieren der seriellen Schnittstelle
        try
        {
            this.lk_cSerialPort.setSerialPortParams
                (this.lk_iSerialPortBaudrate, this.lk_iSerialPortDataBits, 
                this.lk_iSerialPortStopBits, this.lk_iSerialPortParity);
        }
        catch(UnsupportedCommOperationException ex)
        {
            System.err.println("CVPLserial: UnsupportedCommOperation " +
                    "Exception openSerialPort: " + ex.getMessage());
            this.writeError(   "CVPLserial: UnsupportedCommOperation " +
                    "Exception openSerialPort: " + ex.getMessage());
            return false;
        }
        
        // Anlegen der Datenstroeme zur Ein- und Ausgabe
        try
        {
            this.lk_cSerialPortInputStream = new InputStreamReader(
                this.lk_cSerialPort.getInputStream(), "US-ASCII");
            this.lk_cSerialPortOutputStream = new OutputStreamWriter(
                this.lk_cSerialPort.getOutputStream(), "US-ASCII");
        }
        catch(UnsupportedEncodingException ex)
        {
            System.err.println("CVPLserial: WrongEncoding Exception " +
                    "openSerialPort: " + ex.getMessage());
            this.writeError(   "CVPLserial: WrongEncoding Exception " +
                    "openSerialPort: " + ex.getMessage());
            return false;
        }
        catch(IOException ex)
        {
            System.err.println("CVPLserial: I/O Exception openSerialPort: " 
                    + ex.getMessage());
            this.writeError(   "CVPLSerial: I/O Exception openSerialPort: " 
                    + ex.getMessage());
            return false;
        }
        
        // Eventlistener auf diese Klasse registrieren
        try
        {
            this.lk_cSerialPort.addEventListener(this);
        }
        catch(TooManyListenersException ex)
        {
            System.err.println("CVPLserial: TooManyListeners Exception " +
                    "openSerialPort: " + ex.getMessage());
            this.writeError(   "CVPLserial: TooManyListeners Exception " +
                    "openSerialPort: " + ex.getMessage());
            return false;
        }
        
        // Events registrieren und Checkboxes setzen
        if(this.lk_cCBEventCarrierDetect != null)
        {
            this.lk_cSerialPort.notifyOnCarrierDetect(true);
            this.lk_cCBEventCarrierDetect.setSelected(
                    this.lk_cSerialPort.isCD());
        }
        if(this.lk_cCBEventClearTosSend != null)
        {
            this.lk_cSerialPort.notifyOnCTS(true);
            this.lk_cCBEventClearTosSend.setSelected(
                    this.lk_cSerialPort.isCTS());
        }
        if(this.lk_cCBEventDataAvailable != null)
        {
            this.lk_cSerialPort.notifyOnDataAvailable(true);
            this.lk_cCBEventDataAvailable.setSelected(false);
        }
        if(this.lk_cCBEventDataSetReady != null)
        {
            this.lk_cSerialPort.notifyOnDSR(true);
            this.lk_cCBEventDataSetReady.setSelected(
                    this.lk_cSerialPort.isDSR());
        }
        
        // Serielle Schnittstelle geoeffnet
        this.writeStatus("Open serial port: "+this.lk_cSerialPort.getName());
        this.lk_bSerialPortOpen = true;
        return true;
    }
    
    /**
     * Schliessen der seriellen Schnittstelle
     * 
     * @return Konnte die Schnittstelle geschlossen werden, wird true 
     * zurueckgegeben
     * 
     * @see CVPLserial#openSerialPort()
     */
    public boolean closeSerialPort()
    {
        if(this.lk_bSerialPortOpen == false)
        {
            this.writeStatus("No open serial port");
            return false;
        }
        
        // Eventlistener dieser Klasse entfernen
        this.lk_cSerialPort.removeEventListener();
        
        // Checkboxes ruecksetzen
        if(this.lk_cCBEventCarrierDetect != null)
        {
            this.lk_cCBEventCarrierDetect.setSelected(false);
        }
        if(this.lk_cCBEventClearTosSend != null)
        {
            this.lk_cCBEventClearTosSend.setSelected(false);
        }
        if(this.lk_cCBEventDataAvailable != null)
        {
            this.lk_cCBEventDataAvailable.setSelected(false);
        }
        if(this.lk_cCBEventDataSetReady != null)
        {
            this.lk_cCBEventDataSetReady.setSelected(false);
        }
        
        // Serielle Schnittstelle schliessen
        this.lk_cSerialPort.close();
        this.writeStatus("Close serial port: " 
                + this.lk_cSerialPort.getName());
        
        this.lk_bSerialPortOpen = false;
        return true;
    }
    
    /**
     * Abfrage des Eingabestrom.
     * 
     * <br>Ueber den erhaltenen Eingabestrom koennen Daten von der seriellen
     * Schnittstelle gelesen werden. Ist die Schnittstelle nicht geoeffnet,
     * wird null zurueckgegeben.
     * 
     * @return Reader für Eingabestrom der seriellen Schnittstelle
     * 
     * @see CVPLserial#getPortWriter()
     * @see CVPLserial#openSerialPort()
     */
    public Reader getPortReader()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return (Reader)this.lk_cSerialPortInputStream;
        }
        return null;
    }
    
    /**
     * Abfrage des Ausgabestrom.
     * 
     * <br>Ueber den erhaltenen Ausgabestrom koennen Daten auf die serielle
     * Schnittstelle geschrieben werden. Ist die Schnittstelle nicht 
     * geoeffnet, wird null zurueckgegeben. 
     * 
     * @return Writer für Ausgabestrom der seriellen Schnittstelle
     * 
     * @see CVPLserial#getPortReader()
     * @see CVPLserial#openSerialPort()
     */
    public Writer getPortWriter()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return (Writer)this.lk_cSerialPortOutputStream;
        }
        return null;
    }
    
    /**
     * Registriere JCheckBox fuer das Event SerialPortEvent.CD.
     * 
     * <br>Notification fuer das Event SerialPortEvent.CD wird aktiviert. Je
     * nach Zustand des CD-Bits im UART wird die Chackbox gesetzt oder
     * freigelassen.
     *  
     * @param cCheckBox JCheckBox fuer die Darstellung des Events
     * 
     * @see CVPLserial#serialEvent(SerialPortEvent)
     */
    public void registerCheckboxCarrierDetect(JCheckBox cCheckBox)
    {
        this.lk_cCBEventCarrierDetect = cCheckBox;
    }
    
    /**
     * Registriere JCheckBox fuer das Event SerialPortEvent.CTS.
     * 
     * <br>Notification fuer das Event SerialPortEvent.CTS wird aktiviert. Je
     * nach Zustand des CTS-Bits im UART wird die Checkbox gesetzt oder
     * freigelassen.
     * 
     * @param cCheckBox JCheckBox fuer die Darstellung des Events
     * 
     * @see CVPLserial#serialEvent(SerialPortEvent)
     */
    public void registerCheckboxClearToSend(JCheckBox cCheckBox)
    {
        this.lk_cCBEventClearTosSend = cCheckBox;
    }
    
    /**
     * Registriere JCheckbox fuer das Event SerialPortEvent.DATA_AVAILABLE.
     * 
     * <br>Notification fuer das Event SerialPortEvent.DATA_AVAILABLE wird
     * aktiviert. Je nach Zustand (Daten vorhanden) wird die Checkbox
     * gesetzt oder freigelassen.
     *  
     * @param cCheckBox JCheckBox fuer die Darstellung des Events
     * 
     * @see CVPLserial#serialEvent(SerialPortEvent)
     */
    public void registerCheckboxDataAvailable(JCheckBox cCheckBox)
    {
        this.lk_cCBEventDataAvailable = cCheckBox;
    }
    
    /**
     * Registriere JCheckbox fuer das Event SerialPortEvent.DSR.
     * 
     * <br>Notification fuer das Event SerialPortEvent.DSR wird aktiviert. Je
     * nach Zustand des DSR-Bits im UART wird die Checkbox gesetzt oder
     * freigelassen.
     * 
     * @param cCheckBox JCheckBox fuer die Darstellung des Events
     * 
     * @see CVPLserial#serialEvent(SerialPortEvent)
     */
    public void registerCheckboxDataSetReady(JCheckBox cCheckBox)
    {
        this.lk_cCBEventDataSetReady = cCheckBox;
    }
    
    /**
     * Eventhandler fuer die serielle Schnittstelle.
     * 
     * <br>Verarbeitet folgende Events:
     * <ul>
     * <li>SerialPortEvent.CD - carrier detect</li>
     * <li>SerialPortEvent.CTS - clear to send</li>
     * <li>SerialPortEvent.DATA_AVAILABLE</li>
     * <li>SerialPortEvent.DSR - data set ready</li>
     * </ul>
     * Fuer jedes Evebt wird die entsprechende Checkbo gesetzt.
     * 
     * @see CVPLserial#registerCheckboxCarrierDetect(JCheckBox)
     * @see CVPLserial#registerCheckboxClearToSend(JCheckBox)
     * @see CVPLserial#registerCheckboxDataAvailable(JCheckBox)
     * @see CVPLserial#registerCheckboxDataSetReady(JCheckBox)
     */  
    public void serialEvent( SerialPortEvent ev )
    {
        switch(ev.getEventType())
        {
        case SerialPortEvent.CD:  // carrier detect
            if(this.lk_cCBEventCarrierDetect != null)
            {
                this.lk_cCBEventCarrierDetect.setSelected(ev.getNewValue());
            }
            break;
        case SerialPortEvent.CTS: // clear to send
            if(this.lk_cCBEventClearTosSend != null)
            {
                this.lk_cCBEventClearTosSend.setSelected(ev.getNewValue());
            }
            break;
        case SerialPortEvent.DATA_AVAILABLE:
            if(this.lk_cCBEventDataAvailable != null)
            {
                this.lk_cCBEventDataAvailable.setSelected(ev.getNewValue());
            }
            break;
        case SerialPortEvent.DSR: // data set ready
            if(this.lk_cCBEventDataSetReady != null)
            {
                this.lk_cCBEventDataSetReady.setSelected(ev.getNewValue());
            }
            break;
        default:
            break;
        }
    }
    
    /**
     * Statusabfrage fuer das Bit CD im UART
     * 
     * @return aktueller Wert des Bits (true oder false)
     */
    public boolean getCarrierDetect()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return this.lk_cSerialPort.isCD();
        }
        return false;
    }
    
    /**
     * Statusabfrage fuer das Bit CTS im UART
     * 
     * @return aktueller Wert des Bits (true oder false)
     */
    public boolean getClearToSend()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return this.lk_cSerialPort.isCTS();
        }
        return false;
    }
    
    /**
     * Statusabfrage fuer das Bit DSR im UART
     * 
     * @return aktueller Wert des Bits (true oder false)
     */
    public boolean getDataSetReady()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return this.lk_cSerialPort.isDSR();
        }
        return false;
    }
    
    /**
     * Statusabfrage fuer das Bit DTR im UART
     * 
     * @return aktueller Wert des Bits (true oder false)
     * 
     * @see CVPLserial#setDataTerminalReady(boolean)
     */
    public boolean getDataTerminalReady()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return this.lk_cSerialPort.isDTR();
        }
        return false;
    }
    
    /**
     * Setzen des Bits DTR im UART
     * 
     * @param bValue neuer Wert fuer das Bit (true oder false)
     * 
     * @see CVPLserial#getDataTerminalReady()
     */
    public void setDataTerminalReady(boolean bValue)
    {
        if(this.lk_bSerialPortOpen == true)
        {
            this.lk_cSerialPort.setDTR(bValue);
        }
    }
    
    /**
     * Statusabfrage fuer das Bit RTS im UART
     * 
     * @return aktueller Wert des Bits (true oder false)
     * 
     * @see CVPLserial#setRequestToSend(boolean)
     */
    public boolean getRequestToSend()
    {
        if(this.lk_bSerialPortOpen == true)
        {
            return this.lk_cSerialPort.isRTS();
        }
        return false;
    }
    /**
     * Setzen des Bits RTS im UART
     * 
     * @param bValue neuer Wert fuer das Bit (true oder false)
     * 
     * @see CVPLserial#getRequestToSend()
     */
    public void setRequestToSend(boolean bValue)
    {
        if(this.lk_bSerialPortOpen == true)
        {
            this.lk_cSerialPort.setRTS(bValue);
        }
    }
}
