package de.carlvalentin.Interface;

import java.io.*;

import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Common.*;

import javax.comm.*;

/**
 * Uebertraegt Daten ueber die serielle Schnittstelle an den Drucker.
 */
public class CVSerial extends CVInterface 
{
    /**
     * Einstellungen fuer die serielle Schnittstelle.
     */
	private CVSerialSettings   lk_cSerialSettings;
    
    /**
     * In den Einstellunegen ausgewaehlter serieller Port
     */
    private CommPortIdentifier lk_cCommPortSelected;
    
    /**
     * Serielles Portobjekt, ueber welches kommuniziert wird.
     */
    private SerialPort         lk_cSerialPort;
    
	/**
     * Konstruktor der Klasse CVSerial.
	 *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in eine Datei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
	 */
    public CVSerial(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);
        
        this.lk_cSerialSettings = new CVSerialSettings(
                                        this.lk_cErrorMessage,
										this.lk_cErrorFile,
										this.lk_cStatusMessage,
                                        this.lk_cConfigFile);
        
        this.lk_cCommPortSelected = this.lk_cSerialSettings.getCommPort();
        this.lk_cSerialPort       = null;
        
        return;
    }
    
    /**
     * Aufraeumen, bevor das Objekt geloescht wird.
     */
    public void finalize() throws Throwable
    {
    	if(this.lk_bIsConnected == true)
    	{
    		if(this.close() != true)
    		{
            
    		}
    	}
        
        super.finalize();
        
    	return;
    }
    
    /**
     * Oeffnen des Interface.
     * 
     * @return true, wenn Interface geoeffnet werden konnte.
     */
    public boolean open()
    {
        if(this.lk_bIsConnected == true)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVSerial->open: " +
                    "serial port allready open");
            }
            
        	return false;
        }
        
        // serielle Schnittstelle oeffnen
        try
        {
            this.lk_cSerialPort = 
                (SerialPort) this.lk_cCommPortSelected.open("Veto", 500);
        }
        catch(PortInUseException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVSerial->PortInUseException: " +
                        "could not open serial port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVSerial->PortInUseException: " +
                        "could not open serial port - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVSerial: serial port not open");
            
        	return false;
        }
        
        // Parameter der seriellen Schnittstelle einstellen
        try
        {
        	this.lk_cSerialPort.setSerialPortParams(
                this.lk_cSerialSettings.getBaudrate().i_Baudrate,
                this.lk_cSerialSettings.getDatabits().i_Databits,
                this.lk_cSerialSettings.getStopbits().i_Stopbits,
                this.lk_cSerialSettings.getParity().i_Parity);
            
            this.lk_cSerialPort.setFlowControlMode(
                this.lk_cSerialSettings.getHandshake().i_Handshake);
        }
        catch(UnsupportedCommOperationException ex)
        {
        	/*if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVSerial->UnsupportedComm" +
                        "OperationException: wrong settings serial port");
            }*/
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("CVSerial->UnsupportedCommOperation" +
                        "Exception: wrong settings serial port - " 
                        + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVSerial: serial port not open");
            
            return false;
        }
        
        // Anlegen der Datenstroeme zur Ein- und Ausgabe
        try
        {
            this.lk_cInputStreamBinary = this.lk_cSerialPort.getInputStream();
            this.lk_cInputStreamReader = new InputStreamReader(
                this.lk_cInputStreamBinary, "US-ASCII");
            
            this.lk_cOutputStreamBinary = this.lk_cSerialPort.getOutputStream();
            this.lk_cOutputStreamWriter = new OutputStreamWriter(
                this.lk_cOutputStreamBinary, "US-ASCII");
            this.lk_cOutputStreamWriter.flush();
        }
        catch(UnsupportedEncodingException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVSerial->" +
                    "UnsupportedEncodingException: wrong encoding serial port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVSerial->UnsupportedEncoding" +
                        "Exception: wrong encoding serial port - " 
                        + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVSerial: serial port not open");
            
            return false;
        }
        catch(IOException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                    "CVSerial->IOException: wrong streams serial port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVSerial->IOException: wrong streams serial port - " 
                    + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVSerial: serial port not open");
            
            return false;
        }
        
        this.lk_cStatusMessage.write("CVSerial: serial port open");
        
        this.lk_bIsConnected = true;
        
    	return true;
    }
    
    /**
     * Schliessen des Interface.
     * 
     * @return true, wenn Interface geschlossen werden konnte.
     */
    public boolean close()
    {
        if(this.lk_bIsConnected == false)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVSerial->close: " +
                    "no serial port open");
            }
            
        	return false;
        }
        
        // serielle Schnittstelle schliessen
        if(this.lk_cSerialPort != null)
        {
            try
            {
            	if(this.lk_cOutputStreamWriter != null)
            	{
            		this.lk_cOutputStreamWriter.flush();
            		this.lk_cOutputStreamWriter.close();
            		this.lk_cOutputStreamWriter = null;
            	}
            	if(this.lk_cOutputStreamBinary != null)
            	{
            		this.lk_cOutputStreamBinary.flush();
            		this.lk_cOutputStreamBinary.close();
            		this.lk_cOutputStreamBinary = null;
            	}
            	if(this.lk_cInputStreamReader != null)
            	{
            		this.lk_cInputStreamReader.close();
            		this.lk_cInputStreamReader = null;
            	}
            	if(this.lk_cInputStreamBinary != null)
            	{
            		this.lk_cInputStreamBinary.close();
            		this.lk_cInputStreamBinary = null;
            	}
            	if(this.lk_cSerialPort != null)
            	{
            		this.lk_cSerialPort.close();
            		this.lk_cSerialPort = null;
            	}
            }
            catch(IOException ex)
            {
                /*if(this.lk_cErrorMessage != null)
                {
                	this.lk_cErrorMessage.write("CVSerial->IOException: " +
                        "could not close streams");
                }*/
                if(this.lk_cErrorFile != null)
                {
                	this.lk_cErrorMessage.write("CVSerial->IOException: " +
                        "could not close stream - " + ex.getMessage());
                }
                this.lk_cStatusMessage.write(
                        "CVSerial: serial port not closed");
                
                return false;
            }
            
            this.lk_cStatusMessage.write("CVSerial: serial port closed");
            
            this.lk_bIsConnected = false;
            
            return true;
        }
        
        this.lk_cStatusMessage.write("CVSerial: no serial port open");
        
    	return false;
    }
    
    /**
     * Abfrage der aktuellen Einstellungen.
     * 
     * @return Objekt zur Speicherung der Einstellungen.
     */
    public Object getInterfaceSettings()
    {
    	return (Object) this.lk_cSerialSettings;
    }
    
    /**
     * Setzen der aktuellen Einstellungen.
     * 
     * @param settings Objekt zur Speicherung der Einstellungen.
     */
    public void setInterfaceSettings(Object settings)
    {
    	this.lk_cSerialSettings = (CVSerialSettings) settings;
        
        this.lk_cCommPortSelected = this.lk_cSerialSettings.getCommPort();
        
        return;
    }
}
