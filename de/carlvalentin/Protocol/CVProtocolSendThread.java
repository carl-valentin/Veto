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
public class CVProtocolSendThread extends CVProtocolThread
{
    /**
     * Binaerer Ausgabestrom des Threads - Durchleitung aller Daten
     */
    private OutputStream lk_cBinaryOutput = null;
    
    /**
     * Konstruktor der Klasse CVProtocolSendThread
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     */
	public CVProtocolSendThread(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage)
    {
		super(errorMessage, errorFile, statusMessage);
        
        return;
    }
    
    /**
     * Aufruf durch den Garabage Collector
     */
    public void finalize() throws Throwable
    {
        if(this.lk_cBinaryOutput != null)
        {
        	this.lk_cBinaryOutput.flush();
        	this.lk_cBinaryOutput.close();
        }
        
        super.finalize();
        
    	return;
    }
    
    /**
     * Setzen des binaeren Ausgabestrom des Threads - Daten werden nicht 
     * bearbeitet.
     * 
     * @param out Binaerer Ausgabestrom.
     */
    public void setBinaryOutput(OutputStream out)
    {
        this.lk_cBinaryOutput = out;
        
        return;
    }
    
    /**
     * Abfrage des binaeren Ausgabestrom des Thread - Daten werden nicht 
     * bearbeitet.
     * 
     * @return Binaerer Eingabestrom.
     */
    public OutputStream getBinaryOutput()
    {
        return this.lk_cBinaryOutput;
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
     * Ausfuehrungsfunktion  des Threads
     */
    public void run()
    {
    	char c;
        while(true)
        {
        	if((this.lk_cInputReader != null)&&
               (this.lk_cVectorOutputWriter != null) &&
               (this.lk_bIsStopped   == false))
            {
        		if(this.lk_cSohEtb.equals(CVSohEtb.none) == true)
                {
                	//----------------------------------------------------------
                    //keine Start-/Stopzeichen einfügen
                	//----------------------------------------------------------
                    try
                    {                    	
                    	//if(this.lk_cInputReader.ready() == true)
                    	//{
                    		c = (char)this.lk_cInputReader.read();
                    		for(int i=0; 
                    			i<this.lk_cVectorOutputWriter.size(); 
                    			i++)
                    		{
                    			((Writer)this.lk_cVectorOutputWriter.get(i)).
                    				write((int)c);
                    			((Writer)this.lk_cVectorOutputWriter.get(i)).
                    				flush();
                    		}
                    	//}
                    }
                    catch(IOException ex)
                    {
                        if(this.lk_cErrorMessage != null)
                        {
                        	this.lk_cErrorMessage.write(
                                "CVProtocolSendThread: I/O Exception run");
                        }
                        if(this.lk_cErrorFile != null)
                        {
                        	this.lk_cErrorFile.write( "CVPLSendThread: " +
                        			"I/O Exception run: " + ex.getMessage());
                        }
                        
                        return; // Thread verlassen
                    }
                }
                else
                {
                	//----------------------------------------------------------
                    // Start-/Stopzeichen einfügen
                	//----------------------------------------------------------
                    try
                    {
                    	//if(this.lk_cInputReader.ready() == true)
                    	//{
                    		c = (char)this.lk_cInputReader.read();
                    		switch(c)
                    		{
                    		case '\n':
                    			//----------------------------------------------
                    			// Start- und Stopzeichen der CVPL hinzufuegen
                    			//----------------------------------------------
                    			for(int i=0; 
                    				i<this.lk_cVectorOutputWriter.size(); 
                            		i++)
                    			{
                    				if(((Writer)this.lk_cVectorOutputWriter.
                    					get(i)) != null)
                    				{
                    					((Writer)this.lk_cVectorOutputWriter.
                    						get(i)).write(
                    						this.lk_cSohEtb.gl_strSOH + 
                    						this.lk_szCurrentLine +
                    						this.lk_cSohEtb.gl_strETB + "\n",
                    						0, 
                    						this.lk_szCurrentLine.length() + 3);
                    					((Writer)this.lk_cVectorOutputWriter.
                    						get(i)).flush();
                    				}
                    			}
                    			this.lk_szCurrentLine = "";
                    			break;
                    		case '\b':
                    			this.lk_szCurrentLine = 
                    				this.lk_szCurrentLine.substring(
                                        0, this.lk_szCurrentLine.length()-1); 
                    			break;
                    		default:
                    			this.lk_szCurrentLine += c;
                            	break;
                    		}
                    	//}
                    }
                    catch(IOException ex)
                    {
                        if(this.lk_cErrorMessage != null)
                        {
                        	this.lk_cErrorMessage.write(
                                "CVProtocolSendThread: I/O Exception run");
                        }
                        if(this.lk_cErrorFile != null)
                        {
                        	this.lk_cErrorFile.write( "CVPLSendThread: " +
                        			"I/O Exception run: " + ex.getMessage());
                        }
                        
                        return; // Thread verlassen
                    }
                }
            }
        }
    }
}
