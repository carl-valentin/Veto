package de.carlvalentin.Protocol;

import java.io.*;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Interface.CVNetworkSettings;
import de.carlvalentin.ValentinConsole.ValentinConsole;
import de.carlvalentin.Common.CVLogging;

/**
 * Klasse zum Senden von Daten gem&auml;ss der Konventionen der CVPL von einem
 * Reader zu einem Writer(Javaklassen). Der genaue Ursprung des Datenstroms
 * im Reader und das Ziel des Writer sind f&uuml;r die Bearbeitung der Daten
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

    private Writer lk_cKeepAliveItfWriter = null;
    private boolean lk_bKeepAliveErrorOnFail = true;

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
     * Interface Write fuer Keep live
     */
    public void setKeepAliveItfWriter(Writer itfWriter, boolean bError)
    {
        this.lk_cKeepAliveItfWriter = itfWriter;
        lk_bKeepAliveErrorOnFail = bError;

        return;
    }


    /**
     * Beendet die Verarbeitung des Threads
     */
    public void stopThread()
    {
    	this.lk_cIsStoppedSemaphore.grab();
    	this.lk_bIsStopped = true;

        this.interrupt();

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

    private boolean keepAliveCheck()
    {
        if (this.lk_cKeepAliveItfWriter != null)
        {
            try
            {
                this.lk_cKeepAliveItfWriter.write(" ");
                this.lk_cKeepAliveItfWriter.flush();
            }
            catch(IOException iex)
            {
                if (lk_bKeepAliveErrorOnFail)
                {
                    if (this.lk_cErrorMessage != null)
                    {
                        this.lk_cErrorMessage.write(
                            "RecvThread: KeepAlive: " + iex.getMessage());
                    }
                    if (this.lk_cErrorFile != null)
                    {
                        this.lk_cErrorFile.write( "CVPLRecvThread: " +
                                "KeepAlive: " + iex.getMessage());
                    }
                }
                ValentinConsole.disconnect();
                ValentinConsole.doAutoReconnect();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Ausfuehrungsfunktion des Threads.
     */
    public void run()
    {
    	int iC;
        while(!lk_bIsStopped)
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
                        try
                        {
                            iC = this.lk_cInputReader.read();
                            if (iC == -1) {
                                if (!this.lk_cInputReader.ready())
                                {
                                	if (!keepAliveCheck())
                                		return;
                                    continue;
                                }
                            }
                        }
                        catch(IOException ex)
                        {
                            if (this.lk_cInputReader == null)
                                return;

                            if (!this.lk_cInputReader.ready())
                            {
                                continue;
                            }

                            if (this.lk_cErrorMessage != null)
                            {
                                this.lk_cErrorMessage.write(
                                    "RecvThread: IOException: " + ex.getMessage());
                            }
                            if (this.lk_cErrorFile != null)
                            {
                                this.lk_cErrorFile.write( "CVPLRecvThread: " +
                                        "I/O Exception: " + ex.getMessage());
                            }

                            return; // Thread verlassen
                        }
                    	for(int i=0; i<this.lk_cVectorOutputWriter.size(); i++)
                        {
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).write(iC);
                           ((Writer)this.lk_cVectorOutputWriter.get(i)).flush();
                        }
                    }
                    catch(IOException ex)
                    {
                        if (this.lk_cErrorMessage != null)
                        {
                            this.lk_cErrorMessage.write(
                                "RecvThread: IOException: " + ex.getMessage());
                        }
                        if (this.lk_cErrorFile != null)
                        {
                            this.lk_cErrorFile.write( "CVPLRecvThread: " +
                                    "I/O Exception: " + ex.getMessage());
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
                        while(!lk_bIsStopped)
                        {
                            try
                            {
                                iC = this.lk_cInputReader.read();
                                if (iC == -1) {
                                    if (!this.lk_cInputReader.ready())
                                    {
                                    	if (!keepAliveCheck())
                                    		return;
                                        continue;
                                    }
                                }
                            }
                            catch(IOException ex)
                            {
                                if (this.lk_cInputReader == null)
                                    return;

                                if (!this.lk_cInputReader.ready())
                                {
                                	if (!keepAliveCheck())
                                		return;
                                    continue;
                                }

                                if (this.lk_cErrorMessage != null)
                                {
                                    this.lk_cErrorMessage.write(
                                        "RecvThread: IOException: " + ex.getMessage());
                                }
                                if (this.lk_cErrorFile != null)
                                {
                                    this.lk_cErrorFile.write( "CVPLRecvThread: " +
                                            "I/O Exception: " + ex.getMessage());
                                }

                                return; // Thread verlassen
                            }
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
                            if (iC == -1) {
                                if (!this.lk_cInputReader.ready())
                                {
                                	if (!keepAliveCheck())
                                		return;
                                    continue;
                                }
                            }
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
                        if (this.lk_cErrorMessage != null)
                        {
                            this.lk_cErrorMessage.write(
                                "RecvThread: IOException: " + ex.getMessage());
                        }
                        if (this.lk_cErrorFile != null)
                        {
                            this.lk_cErrorFile.write( "CVPLRecvThread: " +
                                    "I/O Exception: " + ex.getMessage());
                        }

                        return; // Thread verlassen
                    }
                }
            }
        }
    }
}
