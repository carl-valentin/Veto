package de.carlvalentin.ValentinConsole;

import de.carlvalentin.Common.*;

/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung
 * (Erweitert die pnuts-Konsole um das Speichern und 
 *  Wiederherstellen der History) 
 */
public class CVConsole extends pnuts.tools.Console
{
    /**
     * Anzahl gespeicherter Befehle in der History
     */
	private final int    lk_iHistorySizeSaved  = 10;
    
    /**
     * Toekn zum Speichern der History in der Konfigurationsdatei
     */
    private final String lk_strHistoryConfigID = "History";
    
    /**
     * Konstruktor der Klasse CVConsole
     */
    public CVConsole()
    {
        super();
        
    	return;
    }
    
    /**
     * Speichern der History in Konfigurationsdatei
     * 
     * @param cConfigFile Konfigurationsdatei
     */
    public void saveHistory(CVConfigFile cConfigFile) 
    {
        int i = 0;
        
        int iHistorySize = history.size();
        int iHistorySaveIndex;
        
        if (iHistorySize < this.lk_iHistorySizeSaved) {
            iHistorySaveIndex = 0;
        }
        else {
            iHistorySaveIndex = iHistorySize - this.lk_iHistorySizeSaved;
        }
        
        for (i=0; iHistorySaveIndex<iHistorySize; iHistorySaveIndex++, i++) {
            cConfigFile.setConfig(this.lk_strHistoryConfigID + i, 
                    history.elementAt(iHistorySaveIndex).toString());
        }
    }

    /**
     * Auslesen der History aus Konfigurationsdatei
     * 
     * @param cConfigFile Konfigurationsdatei
     */
    public void restoreHistory(CVConfigFile cConfigFile) {
        int i = 0;
        String strHistoryElement;
        
        i = 0;
        strHistoryElement = cConfigFile.getConfig(
                                                this.lk_strHistoryConfigID + i);
        while (strHistoryElement != null) {
            history.add(strHistoryElement);
            i++;
            strHistoryElement = cConfigFile.getConfig(
                                                this.lk_strHistoryConfigID + i);
        }
        
        if (historyIndex == -1) {
            historyIndex = i;
        }
    }
}
