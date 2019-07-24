package de.carlvalentin.Common;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;

import java.io.*;

/**
 * Zugriffs- und Verwaltungsfunktionen zum Umgang mit Dateien
 */
public class CVFileManagement 
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
     * Konstruktor der Klasse CVFileManagement.
     * 
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf der Statuszeile.
     */
    public CVFileManagement(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage)
    {
        this.lk_cErrorFile     = errorFile;
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cStatusMessage = statusMessage;
     
        return;
    }
        
    public boolean storeStringToFile(File cWriteFile, String sContent)
    {
        return storeBytesToFile(cWriteFile, sContent.getBytes());
    }
    
    public boolean storeStringToFile(File cWriteFile, String sContent,
                                     boolean append)
    {
    	return storeBytesToFile(cWriteFile, sContent.getBytes(), append);
    }

    public boolean storeBytesToFile(File cWriteFile, byte[] bytes)
    {
        return storeBytesToFile(cWriteFile, bytes, false);
    }
    
    public boolean storeBytesToFile(File cWriteFile, byte[] bytes, 
                                    boolean append)
    {
    	boolean bReturnValue = true;
        
        FileOutputStream cOutStreamWriteFile = null;
        try
        {
            cOutStreamWriteFile = new FileOutputStream(cWriteFile, append);
            
            cOutStreamWriteFile.write(bytes);
        }
        catch(IOException ex)
        {
            bReturnValue = false;
            
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVFileManagement->" +
                        "IOException: file " + cWriteFile.getName() +
                        " IO error");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVFileManagement->" +
                        "IOException: file " + cWriteFile.getName() +
                        " IO error - " + ex.getMessage());
            }
        }
        finally
        {
            try 
            {
                if ( cOutStreamWriteFile != null )
                {
                    cOutStreamWriteFile.close();
                }
            } 
            catch (IOException ex) {}
        }
        return bReturnValue;
    }
    
    /**
     * Lese String aus Datei ein
     * 
     * @param cReadFile Datei, aus der gelesen werden soll
     * @return String, wenn erfolgreich, sonst null
     */
    public String readStringFromFile(File cReadFile)
    {
    	String sReturnValue = null;
        
    	byte bBufferFile[] = new byte[(int) cReadFile.length()];
         
    	FileInputStream cInStreamReadFile = null;
         
    	try
		{
    		cInStreamReadFile = new FileInputStream(cReadFile);
             
            int iBytesRead = cInStreamReadFile.read(
                     bBufferFile, 0, (int) cReadFile.length());
             
             if(iBytesRead != (int) cReadFile.length())
             {
                sReturnValue = null;
                 
                 if(this.lk_cErrorMessage != null)
                 {
                     this.lk_cErrorMessage.write("CVFileManagement->" + 
                             "file: " + cReadFile.getName() +
                             " not all bytes read");
                 }
                 if(this.lk_cErrorFile != null)
                 {
                    this.lk_cErrorFile.write("CVFileManagement->" + 
                             "file: " + cReadFile.getName() +
                             " not all bytes read");
                 }  
             }
             else
             {
                sReturnValue = new String(bBufferFile, 0, iBytesRead);
             }
         }
         catch(IOException ex)
         {
            sReturnValue = null;
             
             if(this.lk_cErrorMessage != null)
             {
                 this.lk_cErrorMessage.write("CVFileManagement->" +
                         "IOException: file " + cReadFile.getName() +
                         " IO error");
             }
             if(this.lk_cErrorFile != null)
             {
                 this.lk_cErrorFile.write("CVFileManagement->" +
                         "IOException: file " + cReadFile.getName() +
                         " IO error - " + ex.getMessage());
             }
         }
         finally
         {
             try 
             {
                 if ( cInStreamReadFile != null )
                 {
                     cInStreamReadFile.close();
                 }
             } 
             catch (IOException ex) {}
         }
         return sReturnValue;
    }
}
