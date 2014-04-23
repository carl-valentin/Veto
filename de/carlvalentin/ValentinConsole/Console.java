package de.carlvalentin.ValentinConsole;

/**
 * Stellt eine Shell-&Auml;hnliche Konsole zur Verf&uuml;gung
 * (Erweitert die pnuts-Konsole um das Speichern und 
 *  Wiederherstellen der History) 
 */
public class Console extends pnuts.tools.Console {
    private final int iHistorySizeSaved = 10;
    private final String strHistoryConfigID = "History";
    int i;
    
	public void saveHistory(Config config) {
        int iHistorySize = history.size();
        int iHistorySaveIndex;
        
        if (iHistorySize < iHistorySizeSaved) {
        	iHistorySaveIndex = 0;
        }
        else {
        	iHistorySaveIndex = iHistorySize - iHistorySizeSaved;
        }
        
        for (i=0; iHistorySaveIndex<iHistorySize; iHistorySaveIndex++, i++) {
            config.setConfig(strHistoryConfigID + i, 
            		history.elementAt(iHistorySaveIndex).toString());
        }
    }

    public void restoreHistory(Config config) {
    	int i;
        String strHistoryElement;
        
        i = 0;
        strHistoryElement = config.getConfig(strHistoryConfigID + i);
        while (strHistoryElement != null) {
        	history.add(strHistoryElement);
            i++;
            strHistoryElement = config.getConfig(strHistoryConfigID + i);
        }
        
        if (historyIndex == -1) {
        	historyIndex = i;
        }
    }
    
}
