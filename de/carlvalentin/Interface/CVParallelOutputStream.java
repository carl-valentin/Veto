/*
 * Passt die Daten an wie vom Parallelen Treiber gefordert
 */
package de.carlvalentin.Interface;

import java.io.IOException;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.ParallelPort;
import gnu.io.PortInUseException;

import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.UI.CVStatusLine;

/**
 * @author BTisler
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CVParallelOutputStream extends OutputStream {
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    protected CVLogging          lk_cErrorFile = null;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    protected CVStatusLine       lk_cStatusMessage = null;
    
    private ParallelPort lk_cParallelPort;
    private CommPortIdentifier lk_cCommPortSelected;
    private OutputStream lk_Out;
    
    public CVParallelOutputStream(ParallelPort port,
            CommPortIdentifier comm, CVLogging log,
            CVStatusLine status) throws IOException{
        lk_cCommPortSelected = comm;
        lk_cParallelPort = port;
        lk_Out = port.getOutputStream();
        lk_cErrorFile = log;
        lk_cStatusMessage = status;
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#close(int)
     */
    public void close() throws IOException {
    	lk_Out.flush();
        lk_Out.close();
        lk_cParallelPort.close();
    }
    
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
        lk_Out.write(b);
	}

    /* (non-Javadoc)
     * @see java.io.OutputStream#flush(int)
     */
    public void flush() throws IOException {
        lk_cParallelPort.close();
        try
        {
            this.lk_cParallelPort = 
                (ParallelPort) lk_cCommPortSelected.open("Veto", 500);
        }     
        catch(PortInUseException ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVParallelOutputStream->PortInUseException: " +
                        "could not open parallel port - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVParallelOutputStream: parallel port not open");
            
            throw new IOException();
        }
        lk_Out = lk_cParallelPort.getOutputStream();
    }
    
}
