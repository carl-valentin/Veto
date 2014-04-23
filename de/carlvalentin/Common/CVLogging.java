package de.carlvalentin.Common;

import de.carlvalentin.Common.UI.CVErrorMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NullPointerException;
import java.sql.Timestamp;

/**
 * Schreibt mittels write uebergebene Meldungen in eine Logdatei.
 */
public class CVLogging 
{
    /**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage   lk_cErrorMessage;
  
    /**
     * Name der Logdatei
     */
    private String           lk_szLogFileName;
    
    /**
     * Fileobjekt der Logdatei.
     */
    private File             lk_cLogFile;
    
    /**
     * Ausgabestrom in die Logdatei
     */
    private FileOutputStream lk_cLogFileOutputStream;
    
    /**
     * Konstruktor der Klasse CVLogging
     * 
     * @param fileName Name (Pfad) der Logdatei
     * @param errorMessage Ausgabe von Fehlermeldungen
     */
	public CVLogging(String fileName, CVErrorMessage errorMessage)
    {
        this.lk_cErrorMessage = errorMessage;
        this.lk_szLogFileName = fileName;
                
        this.open();
  
        return;
    }
    
    /**
     * Aufraeumen bevor das Objekt geloescht wird
     */
    protected void finalize() throws Throwable
    {
    	super.finalize();
        
        if (this.close() != true)
        {
            
        }
        
        return;
    }
    
    /**
     * Logdatei oeffnen und Ausgabestrom einrichten
     * 
     * @return true, wenn Datei und Ausgabestrom geoeffnet werden konnten.
     */
    public boolean open()
    {
    	// Logdatei anlegen
        try
        {
            this.lk_cLogFile = new File(this.lk_szLogFileName);
            if(this.lk_cLogFile.exists() == true)
            {
                this.lk_cLogFile.delete();
            }
            this.lk_cLogFile.createNewFile();
        }
        catch(NullPointerException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                        "CVLogging: wrong file name: " + 
                        this.lk_cLogFile.getAbsolutePath());
            }
            return false;
        }
        catch(IOException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                        "CVLogging: could not create file: " + 
                        this.lk_cLogFile.getAbsolutePath());
            }
            return false;
        }
        
        // Logdatei oeffnen
        try
        {
            this.lk_cLogFileOutputStream = 
                new FileOutputStream(this.lk_cLogFile);
        }
        catch(FileNotFoundException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(
                        "CVLogging: could not open file: " +
                        this.lk_cLogFile.getAbsolutePath());
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Logdatei schliessen
     * 
     * @return true, wenn Logdatei geschlossen werden konnte.
     */
    public boolean close()
    {
        try
        {
        	this.lk_cLogFileOutputStream.close();
        }
        catch(IOException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(
                        "CVLogging: could not close file: " + 
                        this.lk_cLogFile.getAbsolutePath());
            }
        }
        
    	return true;
    }
    
    /**
     * Schreibt eine Nachricht mit Zeitstempel in die Logdatei
     * 
     * @param message Nachricht fuer Logdatei
     */
    public void write(String message)
    {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        String out = timeStamp.toString() + " - " + message + "\n";
        try
        {
        	this.lk_cLogFileOutputStream.write(out.getBytes());
            this.lk_cLogFileOutputStream.flush();
        }
        catch(IOException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(
                        "CVLogging: could not write to file: " + 
                        this.lk_cLogFile.getAbsolutePath());
            }
        }
    }
}
