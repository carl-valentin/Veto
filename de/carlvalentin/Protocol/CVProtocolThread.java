package de.carlvalentin.Protocol;

import de.carlvalentin.Protocol.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Common.*;

import java.io.*;
import java.util.*;

/**
 * Abstrakte Basisklasse zur Implementierung von Threads, welche das Protokoll
 * zur Kommunikation mit den Druckern verarbeiten.
 */
public abstract class CVProtocolThread extends Thread
{
    /**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    protected CVErrorMessage    lk_cErrorMessage = null;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    protected CVLogging         lk_cErrorFile = null;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    protected CVStatusLine      lk_cStatusMessage = null;
    
    /**
     * Eingabestrom (Reader) aus welchem der Thread liest.
     * 
     * @see CVProtocolThread#setThreadReader(Reader)
     */
    protected Reader            lk_cInputReader = null;
    
    /**
     * Liste von Ausgabestroemen (Writer) in welche der Thread schreibt
     */
    protected Vector            lk_cVectorOutputWriter = null;
    
    /**
     * Kodierung der Start- und Stopzeichen der CVPL.
     */
    protected CVSohEtb          lk_cSohEtb = null;
    
    /**
     * Momentan verarbeitete Textzeile
     */
    protected String            lk_szCurrentLine;
    
    /**
     * Zeigt an, ob der Thread gestoppt ist
     */
    protected boolean           lk_bIsStopped = false;
    
    /**
     * Semaphore zum Anhalten des Threads
     */
    protected CVBinarySemaphore lk_cIsStoppedSemaphore = null;
    
	/**
     * Konstruktor der abstrakten Klasse CVProtocolThread.
	 *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
	 */
	public CVProtocolThread(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage)
    {
        this.lk_cInputReader = null;
        this.lk_cVectorOutputWriter = null;
        
        this.lk_cSohEtb = null;
        
        this.lk_cErrorFile     = errorFile;
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cStatusMessage = statusMessage;
        
        this.lk_szCurrentLine = "";
        
        this.lk_cIsStoppedSemaphore = new CVBinarySemaphore();
        
		return;
    }
    
    /**
     * Aufurf durch den Garbage Collector
     */
    protected void finalize() throws Throwable
    {
        return;
    }
    
    /**
     * Ausfuehrungsfunktion des Threads.
     */
    abstract public void run();
    
    /**
     * Beendet die Verarbeitung des Threads.
     */
    abstract public void stopThread();
    
    /**
     * Festlegen des Eingabestroms (Reader) des Thread.
     * 
     * @param inputReader Eingabestrom (Reader) aus welchem der Thread liest.
     */
    public void setThreadReader(Reader inputReader)
    {
    	this.lk_cInputReader = inputReader;
        
        return;
    }
    
    /**
     * Abfrage des aktuellen Eingabestroms (Reader) aus welchem der Thread liest
     * 
     * @return Eingabestrom (Reader) aus welchem der Thread liest.
     */
    public Reader getThreadReader()
    {
    	return this.lk_cInputReader;
    }
    
    /**
     * Festlegen der Ausgabestroeme (Writer) des Thread.
     * 
     * @param cVectorThreadWriter Vector der zu verwendenden Ausgabestroeme
     */
    public void setThreadWriter(Vector cVectorThreadWriter)
    {
    	this.lk_cVectorOutputWriter = cVectorThreadWriter;
    	
    	return;
    }
    
    /**
     * Abfrage des aktuellen Ausgabestrom(Writer) in welchen der Thread schreibt
     * 
     * @return Ausgabestrom (Writer) in welchen der Thread schreibt.
     */
    public Vector getThreadWriter()
    {
    	return this.lk_cVectorOutputWriter;
    }
    
    /**
     * Festlegen der Start- und Stopzeichen aus der CVPL.
     * 
     * @param sohEtb Datenstruktur mit Start- und Stopzeichen.
     */
    public void setSohEtb(CVSohEtb sohEtb)
    {
        this.lk_cSohEtb = sohEtb;
        
        return;
    }
    
    /**
     * Abfrage der aktuellen Start- und Stopzeichen aus der CVPL.
     * 
     * @return Datenstruktur mit Start- und Stopzeichen.
     */
    public CVSohEtb getSohEtb()
    {
    	return this.lk_cSohEtb;
    }
}
