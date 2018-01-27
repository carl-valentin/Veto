package de.carlvalentin.Interface;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.CVConfigFile;
import gnu.io.*;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Speichert Einstellungen fuer die parallele Schnittstelle.
 */
public class CVParallelSettings extends CVInterfaceSettings
{
    /**
     * Struktur zum Suchen der parallelen Ports des Systems.
     */
    private CommPortIdentifier lk_cCommPortIdentifier;
    
    /**
     * Vektor aller parallelen Ports des Systems - CommPortIdentifier.
     */
    private Vector             lk_cCommPortVector;
    
    /**
     * Aktuell ausgewaehlter paralleler Port.
     */
    private CommPortIdentifier lk_cCommPortSelected;
    /**
     * Token zum Speichern des gewaehlten Ports in Konfigurationsdatei
     */
    private final String lk_szConfigTokenCommPortSelected = 
        "ParallelPortSettingsCommPortSelected";
    
    /**
     * Eingestellter Modus parallele Schnittstelle.
     */
    private CVParallelModes   lk_cParallelMode;
    /**
     * Token zum Speichern der gewaehlten Modus in Konfigurationsdatei
     */
    private final String lk_szConfigTokenParallelMode = 
        "ParallelPortSettingsCommPortMode";
    
    /**
     * Konstruktor fuer die Klasse CVParallelSettings
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVParallelSettings(
            CVErrorMessage errorMessage,
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);
        
        this.searchParallelPorts();
        this.setDefaults();
        
        // Konfigurationsdatei einlesen
        //this.lk_cConfigFile = new CVConfigFile("CVVetoParallel", "1.0");
        this.initData();
        
        return;
    }
    
    /**
     * Einlesen der Konfiguration aus der Konfigurationsdatei.
     *
     */
    protected void initData()
    {
        // Daten aus Konfigurationsdatei einlesen
        if(this.lk_cConfigFile != null)
        {
        	String configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenCommPortSelected);
        	if(configValue != null)
        	{
        		for(int iCounter = 0; 
                    iCounter < this.lk_cCommPortVector.size(); 
                    iCounter ++)
        		{
        			CommPortIdentifier helpCommPort = (CommPortIdentifier) 
                        this.lk_cCommPortVector.get(iCounter);
        			if(configValue.equals(
                        (String)helpCommPort.getName()) == true)
        			{
        				this.lk_cCommPortSelected = helpCommPort;
        				break;
        			}
        		}
        	}
        
        	configValue = null;
        
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenParallelMode);
        	if(configValue != null)
        	{
        		this.lk_cParallelMode = CVParallelModes.fromString(configValue);
        	}
        }
        
        return;
    }
    
	/**
     * Gewaehlte Einstellungen auf Korrektheit pruefen.
     * 
     * @return true, wenn Einstellungen korrekt sind.
     */
    public boolean validateSettings()
    {
    	// ueberpruefe ausgewaehlte Schnittstelle
        for(int iCounter = 0;
            iCounter < this.lk_cCommPortVector.size();
            iCounter ++)
        {
            if(this.lk_cCommPortSelected.equals((CommPortIdentifier)
                    this.lk_cCommPortVector.get(iCounter)) == true)
            {
                break;
            }
            if(iCounter == (this.lk_cCommPortVector.size() - 1))
            {
                /*if(this.lk_cErrorMessage != null)
                {
                    this.lk_cErrorMessage.write("CVSerialSettings: " +
                            "not a valid parallel port");
                }*/
                
                return false;
            }
        }
        
    	return true;
    }
    
    /**
     * Setzt Defaultwerte fuer das jeweilige Interface.
     *
     */
    public void setDefaults()
    {
    	// waehle erste Schnittstelle aus
        if(this.lk_cCommPortVector.size() > 0)
        {
            this.lk_cCommPortSelected = 
                    (CommPortIdentifier) this.lk_cCommPortVector.get(0);
        }
        
        // setze Schnittstellenmodus
        this.lk_cParallelMode = CVParallelModes.modeSPP;
        
    	return;
    }
    
    /**
     * Suchen der parallelen Ports des Systems
     *
     */
    private void searchParallelPorts()
    {
        this.lk_cCommPortVector = new Vector();
        this.lk_cCommPortVector.clear();
        
        // suche serielle Schnittstellen
        for(Enumeration enumCommPorts = 
            this.lk_cCommPortIdentifier.getPortIdentifiers(); 
            enumCommPorts.hasMoreElements(); )
        {
            CommPortIdentifier cCurrentPort = 
                (CommPortIdentifier)enumCommPorts.nextElement();
            if(cCurrentPort.getPortType() == CommPortIdentifier.PORT_PARALLEL)
            {
                this.lk_cCommPortVector.add(cCurrentPort);
            }
        }
        
        // keine seriellen Schnittstellen gefunden
        if(this.lk_cCommPortVector.size() == 0)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("No parallel ports found!");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("No parallel ports found!");
            }
        }
     
        return;
    }
    
    /**
     * Abfrage der Liste aller parallelen Schnittstellen.
     * 
     * @return Vector der gefundenen seriellen Schnittstellen.
     */
    public Vector getCommPortVector()
    {
        return this.lk_cCommPortVector;
    }
    
    /**
     * Abfrage der ausgewaehlten parallelen Schnittstelle.
     * 
     * @return Zur Zeit ausgewaehlte Schnittstelle.
     */
    public CommPortIdentifier getCommPort()
    {
        return this.lk_cCommPortSelected;
    }
    
    /**
     * Setzen der ausgewaehlten seriellen Schnittstelle.
     * 
     * @param commPortName Name der ausgewaehlten seriellen Schnittstelle.
     */
    public void setCommPort(String commPortName)
    {
        for(int iCounter = 0; 
            iCounter < this.lk_cCommPortVector.size(); 
            iCounter ++)
        {
            CommPortIdentifier helpCommPort =
                (CommPortIdentifier) this.lk_cCommPortVector.get(iCounter);
            if(commPortName.equals((String)helpCommPort.getName()) == true)
            {
                this.lk_cCommPortSelected = helpCommPort;
                break;
            }
        }
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenCommPortSelected, commPortName);
        }
        
        return;
    }
    
    /**
     * Abfrage des aktuellen Schnittstellenmodus.
     * 
     * @return Aktuell eingestellter Schnittstellenmodus.
     */
    public CVParallelModes getParallelMode()
    {
        return this.lk_cParallelMode;
    }
    
    /**
     * Setzen des aktuellen Schnittstellenmodus.
     * 
     * @param mode Neuer Schnittstellenmodus.
     */
    public void setParallelMode(CVParallelModes mode)
    {
        this.lk_cParallelMode = mode;
        
        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                this.lk_szConfigTokenParallelMode, 
                this.lk_cParallelMode.toString());
        }
        
        return;
    }
}
