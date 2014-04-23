package de.carlvalentin.Interface;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVConfigFile;
import de.carlvalentin.Common.CVLogging;
import java.io.*;

/**
 * Abstrakte Basisklasse zur Implementation der verschiedenen Schnittstellen für
 * die Kommunikation mit den Druckern.
 */
public abstract class CVInterface 
{
    /**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    protected CVErrorMessage     lk_cErrorMessage = null;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    protected CVLogging          lk_cErrorFile = null;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    protected CVStatusLine       lk_cStatusMessage = null;
    
    /**
     * True, wenn Verbindung ueber Schnittstelle hergestellt.
     * 
     * @see CVInterface#isConnected()
     */
    protected boolean            lk_bIsConnected;
    
    /**
     * Reader, um Daten von dem Interface zu lesen.
     * 
     * @see CVInterface#getInterfaceReader()
     */
    protected InputStreamReader  lk_cInputStreamReader = null;
    
    /**
     * Binaere Datenstrom vom Interface, wird nicht bearbeitet.
     */
    protected InputStream        lk_cInputStreamBinary = null;
        
    /**
     * Alle empfangegenen Daten werden in Logdatei geschrieben.
     * 
     * @see CVInterface#setInterfaceReaderLog(CVLogging)
     */
    protected CVLogging          lk_cInputStreamReaderLog = null;
    
    /**
     * Writer, um Daten in das Interface zu schreiben.
     * 
     * @see CVInterface#getInterfaceWriter()
     */
    protected OutputStreamWriter lk_cOutputStreamWriter = null;
    
    /**
     * Binaerer Datenstrom zum Interface, wird nicht bearbeitet.
     */
    protected OutputStream       lk_cOutputStreamBinary = null;
    
    /**
     * Alle gesendeten Daten werden in Logdatei geschrieben.
     */
    protected CVLogging          lk_cOutputStreamWriterLog = null;
    
    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    protected CVConfigFile       lk_cConfigFile = null;
    
    /**
     * Konstruktor der abstrakten Klasse Interface.
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in eine Datei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVInterface(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        this.lk_bIsConnected = false;
        
        this.lk_cInputStreamBinary  = null;
        this.lk_cInputStreamReader  = null;
        this.lk_cOutputStreamBinary = null;
        this.lk_cOutputStreamWriter = null;
        
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cErrorFile     = errorFile;
        this.lk_cStatusMessage = statusMessage;
        this.lk_cConfigFile    = configFile;
        
        return;
    }
    
    /**
     * Aufruf durch Garbage Collector
     */
    protected void finalize() throws Throwable
    {
        this.lk_cInputStreamBinary.close();
        this.lk_cInputStreamReader.close();
        this.lk_cOutputStreamBinary.close();
        this.lk_cOutputStreamWriter.close();
        
    	return;
    }
    
    /**
     * Abfrage, ob Schnittstelle verbunden ist.
     * 
     * @return Verbindungsstatus (true = verbunden).
     */
    public boolean isConnected()
    {
    	return this.lk_bIsConnected;
    }
    
    /**
     * Abfrage, ob Interface einen Eingabekanal hat.
     * 
     * @return true, wenn Eingabekanal vorhanden
     */
    public boolean hasInterfaceReader()
    {
    	if(this.lk_cInputStreamReader != null)
        {
    		return true;
        }
        return false;
    }
    
    /**
     * Abfrage des Readers, um von dem Interface zu lesen.
     * 
     * @return Reader zum Lesen des Interface.
     */
    public InputStreamReader getInterfaceReader()
    {
    	return this.lk_cInputStreamReader;
    }
    
    /**
     * Abfrage des Datenstroms zum Empfang von Binaerdaten.
     * 
     * @return Datenstrom zum Empfang von Binaerdaten.
     */
    public InputStream getInterfaceBinaryInput()
    {
        return this.lk_cInputStreamBinary;
    }
    
    /**
     * Uebergabe Logging-Objekt fuer gelesene Daten.
     * 
     * @param log Logging-Objekt gelesene Daten.
     */
    public void setInterfaceReaderLog(CVLogging log)
    {
    	this.lk_cInputStreamReaderLog = log;
        
        return;
    }
    
    /**
     * Abfrage, ob Interface einen Ausgabekanal hat.
     * 
     * @return true, wenn Ausgabekanal vorhanden
     */
    public boolean hasInterfaceWriter()
    {
        if(this.lk_cOutputStreamWriter != null)
        {
            return true;
        }
        return false;
    }
    
    /**
     * Abfrage des Writers, um in das Interface zu schreiben.
     * 
     * @return Writer zum Schreiben in das Interface.
     */
    public OutputStreamWriter getInterfaceWriter()
    {
    	return this.lk_cOutputStreamWriter;
    }
    
    /**
     * Abfrage des Datenstroms zum Senden von Binaerdaten.
     * 
     * @return Datenstrom zum Senden von Binaerdaten.
     */
    public OutputStream getInterfaceBinaryOutput()
    {
    	return this.lk_cOutputStreamBinary;
    }
    
    /**
     * Uebergabe Logging-Objekt fuer geschriebene Daten.
     * 
     * @param log Logging-Objekt geschriebene Daten.
     */
    public void setInterfaceWriterLog(CVLogging log)
    {
    	this.lk_cOutputStreamWriterLog = log;
    }
    
    /**
     * Oeffnen des Interface.
     * 
     * @return true, wenn Interface geoeffnet werden konnte.
     */
    abstract public boolean open();
    
    /**
     * Schliessen des Interface.
     * 
     * @return true, wenn Interface geschlossen werden konnte.
     */
    abstract public boolean close();
    
    /**
     * Abfrage der aktuellen Einstellungen.
     * 
     * @return Objekt zur Speicherung der Einstellungen.
     */
    abstract public Object getInterfaceSettings();
    
    /**
     * Setzen der aktuellen Einstellungen.
     * 
     * @param settings Objekt zur Speicherung der Einstellungen.
     */
    abstract public void setInterfaceSettings(Object settings);
}