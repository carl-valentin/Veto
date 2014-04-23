package de.carlvalentin.Protocol;

import java.io.*;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVLogging;

/**
 * Klasse zum Senden von Daten gemäss der Konventionen der CVPL von einem
 * Reader zu einem Writer(Javaklassen). Der genaue Ursprung des Datenstroms
 * im Reader und das Ziel des Writer sind für die Bearbeitung der Daten
 * unerheblich. Die Klasse verpackt die Daten in die geforderten Start- und
 * Stopzeichen der CVPL.
 */
public class CVProtocolRecvThread extends CVProtocolThread 
{
    /**
     * Binaerer Eingabestrom des Threads - Durchleitung aller Daten
     */
    private InputStream lk_cBinaryInput = null;
    private OutputStream lk_cBinaryOutput = null;    
    
    /**
     * Konstruktor der Klasse CVProtocolRecvThread
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     */
	public CVProtocolRecvThread(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage)
    {
		super(errorMessage, errorFile, statusMessage);
        
        return;
    }
    
    /**
     * Aufurf durch den Garbage Collector
     */
    public void finalize() throws Throwable
    {
        if(this.lk_cBinaryInput != null)
        {
        	this.lk_cBinaryInput.close();
        }       
        
        super.finalize();
        
    	return;
    }
    
    /**
     * Setzen des binaeren Eingabestrom des Threads - Daten werden nicht 
     * bearbeitet.
     * 
     * @param in Binaerer Eingabestrom.
     */
    public void setBinaryInput(InputStream in)
    {
    	this.lk_cBinaryInput = in;
        
        return;
    }    
    
    /**
     * Abfrage des binaeren Eingabestrom des Thread - Daten werden nicht 
     * bearbeitet.
     * 
     * @return Binaerer Eingabestrom.
     */
    public InputStream getBinaryInput()
    {
    	return this.lk_cBinaryInput;
    }
    
    /**
     * Beendet die Verarbeitung des Threads
     */
    public void stopThread()
    {
    	this.lk_cIsStoppedSemaphore.grab();
    	this.lk_bIsStopped = true;
    	
        this.stop();
        
        if(this.lk_cInputReader != null)
    	{
    		this.lk_cInputReader = null;
    	}
    	if(this.lk_cVectorOutputWriter != null)
    	{
    		this.lk_cVectorOutputWriter = null;
    	}
        
        this.lk_cIsStoppedSemaphore.release();
        
    	return;
    }
    
    /**
     * Ausfuehrungsfunktion des Threads.
     */
    public void run()
    {
    	int iC;
        while(true)
        {
            if((this.lk_cInputReader != null)&&
               (this.lk_cVectorOutputWriter != null) ||
               (this.lk_bIsStopped   == false))
            {
                if(this.lk_cSohEtb.equals(CVSohEtb.none) == true)
                {
                    // keine Start-/Stopzeichen verarbeiten
                    try
                    {
                        iC = this.lk_cInputReader.read();
                    	for(int i=0; i<this.lk_cVectorOutputWriter.size(); i++)
                        {
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).write(iC);
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).flush();
                        }
                    }
                    catch(IOException ex)
                    {
                        if(this.lk_cErrorMessage != null)
                        {
                        	this.lk_cErrorMessage.write(
                                "CVProtocolRecvThread: I/O Exception run: ");
                        }
                        if(this.lk_cErrorFile != null)
                        {
                        	this.lk_cErrorFile.write( "CVPLRecvThread: " +
                        			"I/O Exception run: " + ex.getMessage());
                        }
                        
                        return; // Thread verlassen
                    }
                }
                else
                {
                    // Start-/Stopzeichen verarbeiten
                    try
                    {
                        iC = 0;
                        this.lk_szCurrentLine = "";
                        // Auf Startzeichen der CVPL warten
                        while(true)
                        {
                        	iC = this.lk_cInputReader.read();
                        	if(iC == this.lk_cSohEtb.gl_iSOH)
                        	{
                        		break;
                        	}
                        	
                        	// Alles bis zum Startzeichen einlesen
                        	for(int i=0; i<this.lk_cVectorOutputWriter.size(); 
                        		i++)
                        	{
                        		((Writer)this.lk_cVectorOutputWriter.get(i)).
                        					write(iC);
                        		((Writer)this.lk_cVectorOutputWriter.get(i)).
                        					flush();
                        	}
                        }
                        // Komplette Nachricht einlesen
                        this.lk_szCurrentLine += (char)iC;
                        do
                        {
                            iC = this.lk_cInputReader.read();
                            this.lk_szCurrentLine += (char)iC;
                        }while(iC != this.lk_cSohEtb.gl_iETB);
                        this.lk_szCurrentLine += '\n';
                        for(int i=0; i<this.lk_cVectorOutputWriter.size(); i++)
                        {
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).write
                        				(this.lk_szCurrentLine);
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).flush();
                        }
                    }
                    catch(IOException ex)
                    {
                        if(this.lk_cErrorMessage != null)
                        {
                        	this.lk_cErrorMessage.write(
                                "CVProtocolRecvThread: I/O Exception run");
                        }
                        if(this.lk_cErrorFile != null)
                        {
                        	this.lk_cErrorFile.write( "CVPLRecvThread: " +
                        			"I/O Exception run: " + ex.getMessage());
                        }
                        
                        return; // Thread verlassen
                    }
                }
            }
        }
    }
}
