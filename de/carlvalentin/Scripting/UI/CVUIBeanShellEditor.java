package de.carlvalentin.Scripting.UI;

import de.carlvalentin.Common.CVConfigFile;
import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.CVFileManagement;
import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import java.io.*;

/**
 * Textfeld zur Anzeige und Verarbeitung von Skripten
 */
public class CVUIBeanShellEditor extends RSyntaxTextArea
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
     * Datei zum Speichern der akutellen Konfiguration
     */
    private CVConfigFile       lk_cConfigFile;

    /**
     * Zugriffs- und Verwaltungsfunktionen fuer Dateien
     */
    private CVFileManagement   lk_cFileManagement;
    
    /**
     * Verwendete Skriptdatei
     */
    private File               lk_cFileScript;
    
    /**
     * Konstruktor der Klasse CVUIBeanShell.
     * 
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf der Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVUIBeanShellEditor(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cErrorFile     = errorFile;
        this.lk_cStatusMessage = statusMessage;
        this.lk_cConfigFile    = configFile;
        
        this.lk_cFileManagement = new CVFileManagement(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);
        
        this.initCVUIBeanShellEditor();
        
        return;
    }
    
    /**
     * Initialisiere Klassenkomponenten
     */
    private void initCVUIBeanShellEditor()
    {
        this.lk_cFileScript = null;
        
        this.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
        this.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        this.setCodeFoldingEnabled(true);
        this.setAntiAliasingEnabled(true);
        
    	return;
    }
    
    /**
     * Skriptdatei in Editor laden
     * 
     * @param cScriptFile zu ladende Skriptdatei
     * @return true, wenn erfolgreich, sonst false
     */
    public boolean loadScriptFile(File cScriptFile)
    {
        this.lk_cFileScript = cScriptFile;
        
        String sContent = 
            this.lk_cFileManagement.readStringFromFile(this.lk_cFileScript);
        
        if(sContent == null)
        {
        	return false;
        }
        
        this.setText(sContent);
        
        return true;
    }
    
    /**
     * Skriptdatei aus Editor speichern
     * 
     * @param cScriptFile zu speichernde Skriptdatei
     * @return true, wenn erfolgreich, sonst false
     */
    public boolean storeScriptFile(File cScriptFile)
    {
        this.lk_cFileScript = cScriptFile;
        
        return this.lk_cFileManagement.storeStringToFile(
                this.lk_cFileScript, this.getText());
    }
    
    /**
     * Abfrage der aktuellen Skriptdatei
     * 
     * @return aktuelles Skript
     */
    public String getScriptFile()
    {
    	return this.getText();
    }
}
