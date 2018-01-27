package de.carlvalentin.Interface;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.CVConfigFile;
import gnu.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Speichert Einstellungen fuer die serielle Schnittstelle.
 */
public class CVSerialSettings extends CVInterfaceSettings
{
    /**
     * Struktur zum Suchen der seriellen Ports des Systems.
     */
    private CommPortIdentifier lk_cCommPortIdentifier;
    
    /**
     * Vektor aller seriellen Ports des Systems - CommPortIdentifier.
     */
    private Vector             lk_cCommPortVector;
    
    /**
     * Aktuell ausgewaehlter serieller Port.
     */
    private CommPortIdentifier lk_cCommPortSelected;
    /**
     * Token zum Speichern des gewaehlten Ports in Konfigurationsdatei
     */
    private final String lk_szConfigTokenCommPortSelected = 
        "SerialPortSettingsCommPortSelected";
    
    /**
     * Eingestellte Baudrate fuer die serielle Schnittstelle.
     */
    private CVSerialBaudrate   lk_cBaudRate;
    /**
     * Token zum Speichern der gewaehlten Baudrate in Konfigurationsdatei
     */
    private final String lk_szConfigTokenBaudRate = 
        "SerialPortSettingsBaudRate";
    
    /**
     * Eingestellte Anzahl Datenbits fuer die serielle Schnittstelle.
     */
    private CVSerialDatabits   lk_cDataBits;
    /**
     * Token zum Speichern der gewaehlten Datenbits in Konfigurationsdatei
     */
    private final String lk_szConfigTokenDataBits = 
        "SerialPortSettingsDataBits";
    
    /**
     * Eingestellte Anzahl Stopbits fuer die serielle Schnittstelle.
     */
    private CVSerialStopbits   lk_cStopBits;
    /**
     * Token zum Speichern der gewaehlten Stopbits in Konfigurationsdatei.
     */
    private final String lk_szConfigTokenStopBits = 
        "SerialPortSettingsStopBits";
    
    /**
     * Eingestellte Parity
     */
    private CVSerialParity     lk_cParity;
    /**
     * Token zum Speichern der gewaehlten Parity in Konfigurationsdatei.
     */
    private final String lk_szConfigTokenParity = 
        "SerialPortSettingsParity";
    
    /**
     * Eingestelltes Handshakeverfahren
     */
    private CVSerialHandshake  lk_cHandshake;
    /**
     * Token zum Speichern des gewaehlten Handshakeverfahrens in Konfigurationsdatei.
     */
    private final String lk_szConfigTokenHandshake = 
        "SerialPortSettingsHandshake";
    
    /**
     * Konstruktor fuer die Klasse CVSerialSettings
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
	public CVSerialSettings(
            CVErrorMessage errorMessage,
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);
        
        this.searchSerialPorts();
        this.setDefaults();
        
        // Konfigurationsdatei einlesen
        //this.lk_cConfigFile = new CVConfigFile("CVVetoSerial", "1.0");
        this.initData();                
        
		return;
    }
    
    /**
     * Werte fuer Datenstrukturen der Klasse aus Konfigurationsdatei einlesen.
     *
     */
    protected void initData()
    {
        if(this.lk_cConfigFile != null)
        {
        	String configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenBaudRate);
        	if(configValue != null)
        	{
        		this.lk_cBaudRate = CVSerialBaudrate.fromString(configValue);
        	}
        
        	configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenCommPortSelected);
        	if(configValue != null)
        	{
        		for(int iCounter = 0; 
                    iCounter < this.lk_cCommPortVector.size(); 
                    iCounter ++)
        		{
        			CommPortIdentifier helpCommPort = (CommPortIdentifier) 
                        this.lk_cCommPortVector.get(iCounter);
        			if(configValue.equals(
                            (String)helpCommPort.getName()) == true)
        			{
        				this.lk_cCommPortSelected = helpCommPort;
        				break;
        			}
        		}
        	}
        
        	configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenDataBits);
        	if(configValue != null)
        	{
        		this.lk_cDataBits = CVSerialDatabits.fromString(configValue);
        	}
        
        	configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenParity);
        	if(configValue != null)
        	{
        		this.lk_cParity = CVSerialParity.fromString(configValue);
        	}
        
        	configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenStopBits);
        	if(configValue != null)
        	{
        		this.lk_cStopBits = CVSerialStopbits.fromString(configValue);
        	}
            
            configValue = null;
            
            configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenHandshake);
            if(configValue != null)
            {
                this.lk_cHandshake = CVSerialHandshake.fromString(configValue);
            }
        }
            
    	return;
    }
    
    /**
     * Ueberprueft Einstellungen fuer die serielle Schnittstelle.
     * 
     * @return true, wenn Einstellungen korrekt sind.
     */
    public boolean validateSettings()
    {
        // ueberpruefe ausgewaehlte Schnittstelle
        for(int iCounter = 0;
            iCounter < this.lk_cCommPortVector.size();
            iCounter ++)
        {
        	if(this.lk_cCommPortSelected.equals((CommPortIdentifier)
                    this.lk_cCommPortVector.get(iCounter)) == true)
            {
                break;
            }
            if(iCounter == (this.lk_cCommPortVector.size() - 1))
            {
                /*if(this.lk_cErrorMessage != null)
                {
                	this.lk_cErrorMessage.write("CVSerialSettings: " +
                            "not a valid serial port");
                }*/
                
            	return false;
            }
        }
       
    	return true;
    }
    
    /**
     * Setzte Defaulteinstellungen fuer die serielle Schnittstelle.
     */
    public void setDefaults()
    {
        String configValue = "COM1";
        for(int iCounter = 0;
            iCounter < this.lk_cCommPortVector.size(); 
            iCounter ++)
        {
            CommPortIdentifier helpCommPort = (CommPortIdentifier) 
                this.lk_cCommPortVector.get(iCounter);
            if(configValue.equals(
                    (String)helpCommPort.getName()) == true)
            {
                this.lk_cCommPortSelected = helpCommPort;
                break;
            }
        }
        
        // Baudrate
        this.lk_cBaudRate  = CVSerialBaudrate.bps9600;
        
        // Datenbits
        this.lk_cDataBits  = CVSerialDatabits.bits8;
        
        // Stopbits
        this.lk_cStopBits  = CVSerialStopbits.bits2;
        
        // Parity
        this.lk_cParity    = CVSerialParity.none;

        // Parity
        this.lk_cHandshake = CVSerialHandshake.none;
        
    	return;
    }
    
    /**
     * Suchen der seriellen Ports des Systems
     *
     */
    private void searchSerialPorts()
    {
        this.lk_cCommPortVector = new Vector();
        this.lk_cCommPortVector.clear();
        
        // suche serielle Schnittstellen
        for(Enumeration enumCommPorts = 
            this.lk_cCommPortIdentifier.getPortIdentifiers(); 
            enumCommPorts.hasMoreElements(); )
        {
            CommPortIdentifier cCurrentPort = 
                (CommPortIdentifier)enumCommPorts.nextElement();
            if(cCurrentPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                this.lk_cCommPortVector.add(cCurrentPort);
            }
        }
        
        // keine seriellen Schnittstellen gefunden
        if(this.lk_cCommPortVector.size() == 0)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("No serial ports found!");
            }
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("No serial ports found!");
            }
        }
     
        return;
    }
    
    /**
     * Abfrage der Liste aller seriellen Schnittstellen.
     * 
     * @return Vector der gefundenen seriellen Schnittstellen.
     */
    public Vector getCommPortVector()
    {
    	return this.lk_cCommPortVector;
    }
    
    /**
     * Abfrage der ausgewaehlten seriellen Schnittstelle.
     * 
     * @return Zur Zeit ausgewaehlte Schnittstelle.
     */
    public CommPortIdentifier getCommPort()
    {
    	return this.lk_cCommPortSelected;
    }
    
    /**
     * Setzen der ausgewaehlten seriellen Schnittstelle.
     * 
     * @param commPortName Name der ausgewaehlten seriellen Schnittstelle.
     */
    public void setCommPort(String commPortName)
    {
    	for(int iCounter = 0; 
            iCounter < this.lk_cCommPortVector.size(); 
            iCounter ++)
        {
    		CommPortIdentifier helpCommPort =
                (CommPortIdentifier) this.lk_cCommPortVector.get(iCounter);
            if(commPortName.equals((String)helpCommPort.getName()) == true)
            {
            	this.lk_cCommPortSelected = helpCommPort;
                break;
            }
        }
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenCommPortSelected, 
                commPortName);
        }
        
        return;
    }
    
    /**
     * Abfrage der akutell verwendeten Baudrate.
     * 
     * @return Aktuell eingestellte Baudrate.
     */
    public CVSerialBaudrate getBaudrate()
    {
    	return this.lk_cBaudRate;
    }
    
    /**
     * Setzen der aktuell verwendeten Baudrate.
     * 
     * @param baudRate Neue Baudrate.
     */
    public void setBaudrate(CVSerialBaudrate baudRate)
    {
    	this.lk_cBaudRate = baudRate;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenBaudRate, 
                this.lk_cBaudRate.toString());
        }
        
        return;
    }
    
    /**
     * Abfrage der aktuell verwendeten Anzahl an Datenbits.
     * 
     * @return Aktuelle Anzahl an Datenbits.
     */
    public CVSerialDatabits getDatabits()
    {
    	return this.lk_cDataBits;
    }
    
    /**
     * Setzen der aktuell verwendeten Anzahl an Datenbits.
     * 
     * @param dataBits Neue Anzahl an Datenbits.
     */
    public void setDatabits(CVSerialDatabits dataBits)
    {
    	this.lk_cDataBits = dataBits;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenDataBits, 
                this.lk_cDataBits.toString());
        }
        
        return;
    }
    
    /**
     * Abfrage der aktuell verwendeten Anzahl an Stopbits.
     * 
     * @return Aktuelle Anzahl an Stopbits.
     */
    public CVSerialStopbits getStopbits()
    {
    	return this.lk_cStopBits;
    }
    
    /**
     * Setzen der aktuell verwendeten Anzahl an Stopbits.
     * 
     * @param stopBits Neue Anzahl an Stopbits.
     */
    public void setStopbits(CVSerialStopbits stopBits)
    {
    	this.lk_cStopBits = stopBits;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenStopBits, 
                this.lk_cStopBits.toString());
        }
        
        return;
    }
    
    /**
     * Abfrage der verwendeten Parity.
     * 
     * @return Aktuelle Parity.
     */
    public CVSerialParity getParity()
    {
    	return this.lk_cParity;
    }
    
    /**
     * Setzen der aktuell verwendeten Parity.
     * 
     * @param parity Neue Parity.
     */
    public void setParity(CVSerialParity parity)
    {
    	this.lk_cParity = parity;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenParity, 
                this.lk_cParity.toString());
        }
        
        return;
    }
    
    /**
     * Abfrage des verwendeten Handshakeverfahrens
     * 
     * @return Aktuelles Handshakeverfahren.
     */
    public CVSerialHandshake getHandshake()
    {
        return this.lk_cHandshake;
    }
    
    /**
     * Setzen des aktuell verwendeten Handshakeverfahrens.
     * 
     * @param handshake Neues Handshakeverfahren.
     */
    public void setHandshake(CVSerialHandshake handshake)
    {
        this.lk_cHandshake = handshake;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenHandshake, 
                this.lk_cHandshake.toString());
        }
        
        return;
    }
}
