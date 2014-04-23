package de.carlvalentin.Common.UI;

import javax.swing.*;
import java.io.*;
import java.awt.*;

import de.carlvalentin.Common.*;

/**
 * Grafischer Dialog mit Fortschrittsbalken zum &Uumlbertragen einer Datei
 */
public class CVFileTransmit extends JDialog
{
    /**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage     lk_cErrorMessage;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging          lk_cErrorFile;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine       lk_cStatusMessage;
    
    /**
     * Zu &uumlbertragende Datei
     */
    private File               lk_cFileTransmit;
    
    /**
     * Datenstrom aus dem gelesen wird (Datei)
     */
    private DataInputStream    lk_cDataInputStream;
    
    /**
     * Datenstrom, in den geschrieben wird (Schnittstelle)
     */
    private OutputStream       lk_cDataOutputStream;
    
	private JPanel       jPanelMain           = null;
	private JTextField   jTextFieldFileName   = null;
	private JProgressBar jProgressBarTransmit = null;
	/**
     * Konstruktor der Klasse CVFileTransmit
     * 
	 * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
	 * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
	 * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
	 */
    public CVFileTransmit(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage)
    {
        super();
        
        this.lk_cErrorFile     = errorFile;
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cStatusMessage = statusMessage;
        
        this.lk_cDataInputStream  = null;
        this.lk_cDataOutputStream = null;
        this.lk_cFileTransmit     = null;
     
        this.initialize();
        
        return;
    }		

    /**
     * Aufraeumen, bevor Objekt geloescht wird
     */
    protected void finalize() throws Throwable
    {
        if(this.isVisible() == true)
        {
        	this.setVisible(false);
        }
        
        try
        {
        	this.lk_cDataInputStream.close();
        }
        catch(IOException ex) {}
        
        return;
    }
    
    /**
     * Startet die Datei&uumlbertragung
     * 
     * @return true, wenn die Datei komplett &uumlbertragen wurde, sonst false
     */
    public boolean startFileTransfer()
    {
        if(this.lk_cFileTransmit == null)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(this, 
                        "CVFileTransmit - no file specified");
            }
        	return false;
        }
        
        if(this.lk_cDataOutputStream == null)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(this, 
                        "CVFileTransmit - no interface specified");
            }
            return false;
        }
        
        return this.transferFile();
    }
    
    /**
     * &Uumlbertr&aumlgt die Datei
     * 
     * @return true, wenn Datei komplett &uumlbertragen, sonst false
     */
    private boolean transferFile()
    {
        boolean bReturnValue = true;
        
        final int iBlockLength = 1000;
        int iTransmittedBlocks = 0;
        int iMaxNumBlocks      = 0;
        int iDataRead          = 0;
        long lFileLength       = 0;
        
        byte[] bData = new byte[iBlockLength];
        
        try
        {
            // Eingabestrom von Datei anlegen
        	this.lk_cDataInputStream = new DataInputStream(
        			new FileInputStream(this.lk_cFileTransmit));
            
            // Dateinamen ausgeben
            this.jTextFieldFileName.setText(
                    this.lk_cFileTransmit.getName());
            
            // Datei- und Blockl&aumlnge berechenen
            lFileLength = this.lk_cFileTransmit.length();
            iMaxNumBlocks = (int) (lFileLength / iBlockLength) + 1;
            this.jProgressBarTransmit.setMinimum(iTransmittedBlocks);
            this.jProgressBarTransmit.setMaximum(iMaxNumBlocks);

            if(this.lk_cStatusMessage != null)
            {
            	this.lk_cStatusMessage.write("file transmission start");
            }
            
            // Datei &uumlbertragen
            do 
            {                              
                iDataRead = 
                    this.lk_cDataInputStream.read(bData, 0, iBlockLength);
                if (iDataRead != -1) 
                {
                    this.lk_cDataOutputStream.flush();
                    this.lk_cDataOutputStream.write(bData, 0, iDataRead);
                    this.lk_cDataOutputStream.flush();
                }
                
                iTransmittedBlocks++;
                this.jProgressBarTransmit.setValue(iTransmittedBlocks);
                
            } while (iDataRead != -1);
            
            this.lk_cDataInputStream.close();
            
            if(this.lk_cStatusMessage != null)
            {
            	this.lk_cStatusMessage.write("file transmission complete");
            }
        }
        catch(FileNotFoundException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVFileTransmit->FileNot" +
                    "FoundException: file " + this.lk_cFileTransmit.getName() +
                    " not found");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVFileTransmit->FileNot" +
                    "FoundException: file " + this.lk_cFileTransmit.getName() +
                    " not found - " + ex.getMessage());
            }
            
            bReturnValue = false;
        }
        catch(IOException ex)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVFileTransmit->" +
                    "IOException: file " + this.lk_cFileTransmit.getName() +
                    " IO error");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVFileTransmit->" +
                    "FoundException: file " + this.lk_cFileTransmit.getName() +
                    " IO error - " + ex.getMessage());
            }
            
            bReturnValue = false;
        }
        finally
        {
        	this.setVisible(false);
        }

        return bReturnValue;
    }
    
    /**
     * Legt die zu &uumlbertragende Datei fest
     * 
     * @param file Zu &uumlbertragende Datei
     */
    public void setFile(File file)
    {
        this.lk_cFileTransmit = file;
        
    	return;
    }
    
    /**
     * Legt den Ausgabestrom fest
     * 
     * @param stream Ausgabestrom
     */
    public void setOutputStream(OutputStream stream)
    {
        this.lk_cDataOutputStream = stream;
        
    	return;
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setTitle("file transmission");
        this.setBounds(0, 0, 408, 127);
        this.setResizable(true);
        this.setContentPane(getJPanelMain());
        this.pack();
        this.setVisible(false);
            
    }
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanelMain() {
		if (jPanelMain == null) {
			BorderLayout CVFileTransmitBorderLayout = new BorderLayout();
			jPanelMain = new JPanel();
			jPanelMain.setLayout(CVFileTransmitBorderLayout);
			CVFileTransmitBorderLayout.setHgap(10);
			CVFileTransmitBorderLayout.setVgap(10);
			jPanelMain.setPreferredSize(new java.awt.Dimension(400,100));
			jPanelMain.add
                (getJTextFieldFileName(), java.awt.BorderLayout.NORTH);
			jPanelMain.add
                (getJProgressBarTransmit(), java.awt.BorderLayout.CENTER);
			jPanelMain.setVisible(true);
		}
		return jPanelMain;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextFieldFileName() {
		if (jTextFieldFileName == null) {
			jTextFieldFileName = new JTextField();
			jTextFieldFileName.setEditable(false);
			jTextFieldFileName.setPreferredSize(new java.awt.Dimension(10,40));
			jTextFieldFileName.setToolTipText("transmitted file");
			jTextFieldFileName.setHorizontalAlignment
                (javax.swing.JTextField.CENTER);
		}
		return jTextFieldFileName;
	}
	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */    
	private JProgressBar getJProgressBarTransmit() {
		if (jProgressBarTransmit == null) {
			jProgressBarTransmit = new JProgressBar();
			jProgressBarTransmit.setStringPainted(true);
			jProgressBarTransmit.setToolTipText("progress file transmission");
			jProgressBarTransmit.setValue(0);
			jProgressBarTransmit.setVisible(true);
		}
		return jProgressBarTransmit;
	}
}
