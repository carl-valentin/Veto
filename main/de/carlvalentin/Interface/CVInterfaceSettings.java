package de.carlvalentin.Interface;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.CVConfigFile;

/**
 * Abstrakte Basisklasse zum Speichern von Einstellungen fuer die
 * unterschiedlichen Schnittstellen.
 */
public abstract class CVInterfaceSettings 
{
	/**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    protected CVErrorMessage     lk_cErrorMessage;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    protected CVLogging          lk_cErrorFile;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    protected CVStatusLine       lk_cStatusMessage;
    
    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    protected CVConfigFile       lk_cConfigFile;
    
    /**
     * Konstruktor der Klasse CVInterfaceSettings.
     * 
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVInterfaceSettings(
            CVErrorMessage errorMessage,
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        this.lk_cErrorMessage  = errorMessage;
        this.lk_cErrorFile     = errorFile;
        this.lk_cStatusMessage = statusMessage;
        this.lk_cConfigFile    = configFile;
     
        return;
    }
    
    /**
     * Gewaehlte Einstellungen auf Korrektheit pruefen.
     * 
     * @return true, wenn Einstellungen korrekt sind.
     */
    abstract public boolean validateSettings(); 
    
    /**
     * Setzt Defaultwerte fuer das jeweilige Interface.
     *
     */
    abstract public void setDefaults();
    
    /**
     * Einlesen der Konfiguration aus der Konfigurationsdatei.
     *
     */
    abstract protected void initData();
    
}
