package de.carlvalentin.Interface;

import java.io.*;
import gnu.io.*;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.*;
import de.carlvalentin.Interface.CVParallelSettings;
import de.carlvalentin.Interface.CVParallelOutputStream;

/**
 * Uebertraegt Daten ueber die parallele Schnittstelle an den Drucker.
 */
public class CVParallel extends CVInterface 
{
    /**
     * Einstellungen fuer die parallele Schnittstelle.
     */
    private CVParallelSettings   lk_cParallelSettings;

    /**
     * In den Einstellunegen ausgewaehlter serieller Port
     */
    private CommPortIdentifier   lk_cCommPortSelected;
    
    /**
     * Serielles Portobjekt, ueber welches kommuniziert wird.
     */
    private ParallelPort         lk_cParallelPort;
    
    /**
     * Konstruktor der Klasse CVSerial.
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in eine Datei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVParallel(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);
        
        this.lk_cParallelSettings = new CVParallelSettings(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage,
                this.lk_cConfigFile);
        
        this.lk_cCommPortSelected = this.lk_cParallelSettings.getCommPort();
        this.lk_cParallelPort       = null;
        
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
                this.lk_cErrorMessage.write("CVParallel->open: " +
                    "serial port allready open");
            }
            
            return false;
        }
        
        // parallele Schnittstelle oeffnen
        try
        {
            this.lk_cParallelPort = 
                (ParallelPort) this.lk_cCommPortSelected.open("Veto", 500);
        }
        catch(PortInUseException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVParallel->PortInUseException: " +
                        "could not open parallel port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVParallel->PortInUseException: " +
                        "could not open parallel port - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVParallel: parallel port not open");
            
            return false;
        }
        
        // Parameter der parallelen Schnittstelle einstellen
        try
        {
            this.lk_cParallelPort.setMode(
                this.lk_cParallelSettings.getParallelMode().i_Mode);
        }
        catch(UnsupportedCommOperationException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVParallel->UnsupportedComm" +
                        "OperationException: wrong settings parallel port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVParallel->UnsupportedCommOperation" +
                        "Exception: wrong settings parallel port - " 
                        + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVSerial: parallel port not open");
            
            return false;
        }
        
        // Anlegen der Datenstroeme zur Ein- und Ausgabe
        try
        {
            /*----------------------------------------------------------------*/
            /* Probleme mit der Bibliothek javax.comm                         */
            //this.lk_cInputStreamBinary = this.lk_cParallelPort.getInputStream();
            //this.lk_cInputStreamReader = new InputStreamReader(
            //    this.lk_cInputStreamBinary, "US-ASCII");
            this.lk_cInputStreamBinary = null;
            this.lk_cInputStreamReader = null;
            /*----------------------------------------------------------------*/
            
            this.lk_cOutputStreamBinary = new CVParallelOutputStream(
                this.lk_cParallelPort, this.lk_cCommPortSelected, 
                this.lk_cErrorFile, this.lk_cStatusMessage);
            this.lk_cOutputStreamWriter = new OutputStreamWriter(
                this.lk_cOutputStreamBinary, "US-ASCII");
            this.lk_cOutputStreamWriter.flush();
        }
        catch(UnsupportedEncodingException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVParallel->" +
                  "UnsupportedEncodingException: wrong encoding parallel port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVParallel->UnsupportedEncoding" +
                        "Exception: wrong encoding parallel port - " 
                        + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVParallel: parallel port not open");
            
            return false;
        }
        catch(IOException ex)
        {
            /*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                    "CVParallel->IOException: wrong streams parallel port");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVParallel->IOException: wrong streams parallel port - " 
                    + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVParallel: parallel port not open");
            
            return false;
        }
        
        this.lk_cStatusMessage.write("CVParallel: parallel port open");
        
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
                this.lk_cErrorMessage.write("CVParallel->close: " +
                    "no parallel port open");
            }
            
            return false;
        }
        
        // serielle Schnittstelle schliessen
        if(this.lk_cParallelPort != null)
        {
            try
            {
                this.lk_cOutputStreamWriter.flush();
                this.lk_cOutputStreamWriter.close();
                /*------------------------------------------------------------*/
                /* Probleme mit der Bibliothek javax.com                      */
                //this.lk_cInputStreamReader.close();
                /*------------------------------------------------------------*/
            }
            catch(IOException ex)
            {
                /*if(this.lk_cErrorMessage != null)
                {
                    this.lk_cErrorMessage.write("CVParallel->IOException: " +
                        "could not close streams");
                }*/
                if(this.lk_cErrorFile != null)
                {
                    this.lk_cErrorMessage.write("CVParallel->IOException: " +
                        "could not close stream - " + ex.getMessage());
                }
                this.lk_cStatusMessage.write(
                        "CVSerial: parallel port not closed");
                
                return false;
            }
            
            // Der Port wirde bereits beim schliessen des ParallelOutputSreams
            // geschlossen. Ein weiteres schliessen gibt nur Fehler ...
//            this.lk_cParallelPort.close();
            this.lk_cParallelPort = null;
            
            this.lk_cStatusMessage.write("CVSerial: parallel port closed");
            
            this.lk_bIsConnected = false;
            
            return true;
        }
        
        this.lk_cStatusMessage.write("CVSerial: no parallel port open");
        
        return false;
    }
    
    /**
     * Abfrage der aktuellen Einstellungen.
     * 
     * @return Objekt zur Speicherung der Einstellungen.
     */
    public Object getInterfaceSettings()
    {
    	return (Object) this.lk_cParallelSettings;
    }
    
    /**
     * Setzen der aktuellen Einstellungen.
     * 
     * @param settings Objekt zur Speicherung der Einstellungen.
     */
    public void setInterfaceSettings(Object settings)
    {
    	this.lk_cParallelSettings = (CVParallelSettings) settings;
        
        this.lk_cCommPortSelected = this.lk_cParallelSettings.getCommPort();
        
        return;
    }
}
