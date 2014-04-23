package de.carlvalentin.Interface;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;

import java.io.*;
import java.net.*;

/**
 * Uebertraegt Daten ueber TCP und die Netzwerkschnittstelle an den Drucker.
 */
public class CVNetworkTCP extends CVInterface 
{
    /**
     * Speichert die Einstellungen der Netzwerkschnittstelle.
     */
    private CVNetworkSettings lk_cNetworkSettingsTCP = null;
    
    /**
     * Socket zur Netzwerkkommunikation.
     */
    private Socket            lk_cNetworkSocketTCP   = null;
    
    /**
     * Konstruktor der Klasse CVNetwork
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in eine Datei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
	public CVNetworkTCP(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);
        
        this.lk_cNetworkSettingsTCP = new CVNetworkSettings(
                                        this.lk_cErrorMessage,
                                        this.lk_cErrorFile,
                                        this.lk_cStatusMessage,
                                        this.lk_cConfigFile,
                                        CVNetworkProtocol.TCP);
        
        this.lk_cNetworkSocketTCP = null;
        
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
                this.lk_cErrorMessage.write("CVNetworkTCP->open: " +
                    "TCP network interface allready open");
            }
            
        	return false;
        }
        
        //----------------------------------------------------------------------
        // Netzwerkschnittstelle oeffnen
        //----------------------------------------------------------------------
        try
        {
        	this.lk_cNetworkSocketTCP = new Socket(
                    this.lk_cNetworkSettingsTCP.getIPAdress(),
                    this.lk_cNetworkSettingsTCP.getPort());
        }
        catch(UnknownHostException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVNetworkTCP->open: " +
            		"UnknownHostException: cannot find host");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkTCP->open: " +
                	"UnknownHostException: cannot find host - " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
        }
        catch(IOException ex)
        {
        	/*if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVNetworkTCP->open: IOException:" + 
        			" could not open TCP socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkTCP->open: IOException: " +
                	"could not open TCP socket - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
        }
        catch(SecurityException ex)
        {
        	/*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkTCP->open: " + 
                	"SecurityException: not allowed to open TCP socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkTCP->open : " +
                	"SecurityException: not allowed to open TCP socket - " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
        }
        
        //----------------------------------------------------------------------
        // Ein- / Ausgabestroeme oeffnen
        //----------------------------------------------------------------------
        try
        {
            this.lk_cInputStreamBinary = 
            	this.lk_cNetworkSocketTCP.getInputStream();
            this.lk_cInputStreamReader = new InputStreamReader(
                this.lk_cInputStreamBinary, "US-ASCII");
            
            this.lk_cOutputStreamBinary = 
                this.lk_cNetworkSocketTCP.getOutputStream();
            this.lk_cOutputStreamWriter = new OutputStreamWriter(
                this.lk_cOutputStreamBinary, "US-ASCII");
            this.lk_cOutputStreamWriter.flush();
        }
        catch(UnsupportedEncodingException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkTCP->open" +
                    "UnsupportedEncodingException: wrong encoding network");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkTCP->open: " +
                	"UnsupportedEncoding Exception: wrong encoding network - " + 
                    ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
        }
        catch(IOException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                    "CVNetworkTCP->open: IOException: wrong streams network");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVNetwork-TCP->open: IOException: " +
                    "wrong streams network - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
        }
        
        this.lk_cStatusMessage.write("CVNetworkTCP: network port open");
        
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
                this.lk_cErrorMessage.write("CVNetworkTCP->close: " +
                    "TCP network interface not open");
            }
            
        	return false;
        }
        
        //----------------------------------------------------------------------
        // Netzwerkschnittstelle schliessen
        //----------------------------------------------------------------------
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
            
            if(this.lk_cNetworkSocketTCP != null)
            {
            	this.lk_cNetworkSocketTCP.close();
            	this.lk_cNetworkSocketTCP = null;
            }
        }
        catch(IOException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkTCP->close: " + 
                	IOException: could not close streams or socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorMessage.write("CVNetworkTCP->close: " +
                	"IOException: could not close stream or socket- " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write(
            		"CVNetworkTCP: network port not closed");
            
            return false;
        }
        
        this.lk_cStatusMessage.write("CVNetworkTCP: network port closed");
        
        this.lk_bIsConnected = false;
        
        return true;
    }
    
    /**
     * Abfrage der aktuellen Einstellungen.
     * 
     * @return Objekt zur Speicherung der Einstellungen.
     */
    public Object getInterfaceSettings()
    {
    	return (Object) this.lk_cNetworkSettingsTCP;
    }
    
    /**
     * Setzen der aktuellen Einstellungen.
     * 
     * @param cSettings Objekt zur Speicherung der Einstellungen.
     */
    public void setInterfaceSettings(Object cSettings)
    {
    	if(cSettings != null)
    	{
    		this.lk_cNetworkSettingsTCP = (CVNetworkSettings) cSettings;
    	}
    	
    	return;
    }
}
