package de.carlvalentin.Common.UI;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.Printf.*;

/**
 * Stellt eine mdofizierte JTextArea fuerr die Ausgabe von Text in verschiedener
 * Formatierung zur Verfuegung.
 */
public class CVTextDisplay 
{
	/**
	 *  class is serializable - Variable vorgeschrieben 
	 */
	static final long        serialVersionUID = 0;
	
	/**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage   lk_cErrorMessage;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging        lk_cErrorFile;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine     lk_cStatusMessage;
    
    /**
     * Feld zur Ausgabe der Daten
     */
    private JTextArea          lk_cTextArea;
    private JScrollPane        lk_cTextAreaPane;
    
    /**
     * ASCII-Ausgabe ein- und ausschalten
     */
    private boolean            lk_bUseASCIIOutput = true;
    
    /**
     * Hexadezimale Ausgabe ein- und ausschalten
     */
    private boolean            lk_bUseHexOutput = false;
    
    /**
     * Writer fuer externen Zugriff - Daten vom Interface lesen
     */
    private PipedWriter        lk_cPipedWriterExtern = null;
    /**
     * Interner Reader zum Lesen aus Writer fuer externen Zugriff
     */
    private BufferedReader     lk_cBufferedReaderInput = null;
	
	/**
	 * Konstruktor der Klasse CVTextDisplay
	 * 
	 * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
	 */
	public CVTextDisplay(
			CVErrorMessage cErrorMessage, 
            CVLogging      cErrorFile,
            CVStatusLine   cStatusMessage)
	{
		super();
		
		this.lk_cErrorFile     = cErrorFile;
		this.lk_cErrorMessage  = cErrorMessage;
		this.lk_cStatusMessage = cStatusMessage;
		
		this.initCVTextDisplay();
		
		this.readThread();
		
		return;
	}
	
	/**
     * Aufruf durch Gabage Collector
     */
    protected void finalize() throws Throwable
    {
    	if(this.lk_cBufferedReaderInput != null)
        {
        	this.lk_cBufferedReaderInput.close();
            this.lk_cBufferedReaderInput = null;
        }
        if(this.lk_cPipedWriterExtern != null)
        {
        	this.lk_cPipedWriterExtern.close();
            this.lk_cPipedWriterExtern = null;
        }
    	
    	return;
    }
    
    /**
     * Initialisere Klassenkomponenten und -strukturen
     */
    private void initCVTextDisplay()
    {
    	this.lk_cTextArea = new JTextArea();
		this.lk_cTextArea.setEditable(false);
		this.lk_cTextArea.setFont(new Font("Monospaced", 0, 12));
		
		this.lk_cTextAreaPane = new JScrollPane();
		this.lk_cTextAreaPane.setViewportView(this.lk_cTextArea);
		
        // Anlegen Writer fuer externen Zugriff - Daten vom Interface lesen
        try
        {
            this.lk_cPipedWriterExtern = new PipedWriter();
        	this.lk_cBufferedReaderInput = new BufferedReader(
                new PipedReader(this.lk_cPipedWriterExtern));
        }
        catch(IOException ex)
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create input buffered reader");
            }
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create input buffered reader: " + 
                        ex.getMessage());
            }
        }
		
    	return;
    }
    
    /**
     * Startet Thread zum Auslesen der eingetroffenen Daten
     */
    private void readThread()
    {        
    	new Thread( new Runnable()
    	{
    		public void run()
    	    {
                boolean bIOException = false;
    			char cReadValue = 0x00;
    			
    			//Auslesen der Daten aus lk_cBufferedReaderInput
    			while(true)
    			{
    				try
    				{
    					while(lk_cBufferedReaderInput.ready() == false)
    					{
    						try
    						{
    							Thread.sleep(1);
    						}
    						catch(InterruptedException ex)
    						{
    							
    						}
    					}
    					while(lk_cBufferedReaderInput.ready() == true)
    					{
    						cReadValue = (char) lk_cBufferedReaderInput.read();
    						if(cReadValue == '\n')
    						{
    							writeASCII("\n");
    						}
    						else
    						{
    							// Ausgabe im ASCII-Format
    							if(lk_bUseASCIIOutput == true)
    							{
    								writeASCII(String.valueOf(cReadValue));
    							}
    							
    							// Ausgabe im hexadezimalen Format
    							if(lk_bUseHexOutput == true)
    							{
    								writeHEX(String.valueOf(cReadValue));
    							}
    						}
    					}
    				}
    				catch(IOException ex)
    				{
    					// auftretende Exceptions scheinen Abaluf nicht zu
    					// behindern -> blaehen ErrorLog nur auf
    					/*if(lk_cErrorFile != null)
    					{
    						lk_cErrorFile.write("CVTextDisplay->readThread: " + 
    						    "IOException run: " + ex.getMessage() + "\n");
    					}*/
                        bIOException = true;
    				}
                    if (bIOException)
                    {
                        bIOException = false;
                        try
                        {
                            Thread.sleep(1);
                        }
                        catch(InterruptedException ex)
                        {
                            
                        }
                    }
    			}
    	    }
    	}).start();
    	
    	return;
    }
	
	/**
	 * Ausgabeformat fuer Text einstellen
	 * 
	 * @param bASCII true, Ausgabe in ASCII (default)
	 * @param bHEX   true, Ausgabe als Hexwerte
	 */
	public void setOutputFormat(boolean bASCII, boolean bHEX)
	{
		this.lk_bUseASCIIOutput = bASCII;
		this.lk_bUseHexOutput   = bHEX;
		
		return;
	}
    
    /**
     * Abfrage des Writers - Daten vom Interface lesen
     * 
     * @return Writer zum Schreiben von Daten
     */
    public Writer getWriter()
    {
    	return this.lk_cPipedWriterExtern; 
    }
	
    /**
     * Abfrage des Feldes zur Ausgabe der Daten
     * 
     * @return Feld zur Ausgabe der Daten
     */
    public JTextArea getTextArea()
    {
    	return this.lk_cTextArea;
    }
    
    /**
     * Abfrage des Feldes zur Ausgabe der daten inklusive Umgebung zum Scrollen
     *  
     * @return Scrollbares Feld zur Ausgabe der Daten
     */
    public JScrollPane getTextAreaPane()
    {
    	return this.lk_cTextAreaPane;
    }
    
	/**
	 * Gebe Daten im ASCII-Format aus.
	 * 
	 * @param sData auszugebende Daten
	 */
	public void writeASCII(String sData)
	{
		this.lk_cTextArea.append(sData);
		
		return;
	}
	
	/**
	 * Gebe Daten als hexadezimale Werte aus
	 * 
	 * @param sData auszugebende Daten
	 */
	public void writeHEX(String sData)
	{
		byte[] bData = sData.getBytes();
		
		for(int i=0; i<bData.length; i++)
		{
			this.writeASCII(this.toHexString(bData[i]) + " ");
		}
		
		return;
	}
	
	/**
	 * Wandelt einen Bytewert in Hexwert und gibt diesen als String zurueck.
	 * 
	 * @param bData zu wandelnder Bytewert
	 * @return String mit hexadezimalem Wert
	 */
	private String toHexString(byte bData)
	{
		return "0x" + Integer.toHexString(bData & 0xFF);
	}
}
