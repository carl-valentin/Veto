package de.carlvalentin.Common;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Speicher Konfigurationsdaten in einer "Cookie"-Datei im Temp-Verzeichniss
 * des Benutzers.
 */
public class CVConfigFile 
{
    private final String strConfigVersionID = "Cfg-File Version";
    private final String strConfigVersion = "0.1";
    
    private String strConfigFileName;
    private List list = Collections.synchronizedList(new LinkedList());
    
    private class Config_Element 
    {
        public String strID;
        public String strValue;
        
        private Config_Element() 
        {
        	return;
        }
        
        public  Config_Element(String _strID, String _strValue) 
        {
            strID = _strID;
            strValue = _strValue;
            
            return;
        }
    }
    
    private void writeError(String s) 
    {
        try 
        {
            JOptionPane.showMessageDialog(null, s, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (java.awt.HeadlessException ex) 
        {
            System.err.println(ex);
        }
        
        return;
    }    

    private boolean checkVersion(String strVersionID, String strVersion) 
    {
        String strVersionInFile;
        
        strVersionInFile = getConfig(strVersionID);
        if (strVersionInFile == null) 
        {
            return false;
        }
        else 
        {
            if (strVersion.compareTo(strVersionInFile) != 0) 
            {
                return false;
            }
        }
        
        return true;
    }
    
    private CVConfigFile() 
    {
        return;
    }

    /**
     * @param strName Name der "Cookie"-/Konfigurationsdatei
     * @param strVersion Version der Datei. Stimmt die hier &uuml;bergebene 
     *                   Version nicht mit der in der g&ouml;öffneten Datei 
     *                   &uuml;berein, dann wird der Inhalt ignoriert und 
     *                   die Datei mit einer neuen &uuml;berschrieben. 
     */
    public CVConfigFile(String strName, String strVersion) 
    {
        strConfigFileName = strName + ".cfg";
        
        try 
        {
            FileReader fileConfig = 
                new FileReader(System.getProperty("java.io.tmpdir",".")
                               + strConfigFileName);
            StringBuffer inBuf = new StringBuffer();
            String strID = null;
            int i;
            char c;
            
            String strVersionInFile;

            while ( (i = fileConfig.read()) > 0 ) 
            {
                c = (char)i;
                if (c == '|') 
                {
                    strID = inBuf.toString();
                    inBuf = new StringBuffer();
                }
                else if (c == '\n') 
                {
                    Config_Element cfgElement = 
                        new Config_Element(strID, inBuf.toString());
                    list.add(cfgElement);
                    inBuf = new StringBuffer();
                }
                else 
                {
                    inBuf.append(c);
                }
            }
            
            fileConfig.close();
        }
        catch (FileNotFoundException ex) 
        {
            
        }
        catch (IOException ex) 
        {
            writeError("I/O error while trying to read from file" + 
                       strConfigFileName);
        }
        
        // Wenn Versionen nicht uebereinstimmen, eingelesene Daten 
        // wegwerfen und mit Defaultwerten ueberschreiben
        if (!checkVersion(strName, strVersion) || 
            !checkVersion(strConfigVersionID, strConfigVersion)    ) 
        {
            list = Collections.synchronizedList(new LinkedList());
            setConfig(strName, strVersion);
            setConfig(strConfigVersionID, strConfigVersion);
        }
        
        return;
    }    
    
    /**
     * Liest den Wert zur ID <code>strID</code> aus der 
     * "Cookie"-/Konfigurationsdatei ein.
     * @return <code>null</code> falls die ID noch nicht in der Datei 
     *         gefunden wurde, sonst den Wert als String
     */
    public String getConfig(String strID) 
    {     
        Object objElement[] = list.toArray();
        for (int i=0; i<list.size(); i++) 
        {
            Config_Element cfgElement = (Config_Element)objElement[i];
            if (strID.compareTo(cfgElement.strID) == 0) 
            {
                return cfgElement.strValue;
            }
        }
        return null;
    }
    
    /**
     * Speichert den Wert <code>strValue</code> in der 
     * "Cookie"-/Konfigurationsdatei unter der ID <code>strID</code>. 
     * M.H. der ID kann man den Wert mittels {@link #getConfig} 
     * wieder einlesen.
     */
    public boolean setConfig(String strID, String strValue)
    {
        Object objElement[] = list.toArray();
        for (int i=0; i<list.size(); i++) 
        {
            Config_Element cfgElement = (Config_Element)objElement[i];
            if (strID.compareTo(cfgElement.strID) == 0) 
            {
                cfgElement.strValue = strValue;
                writeConfig();
                return true;
            }
        }
        
        Config_Element cfgElement = 
            new Config_Element(strID, strValue);
        list.add(cfgElement);        
        writeConfig();
        return true;
    }
    
    private void writeConfig() 
    {       
        try 
        {     
            FileWriter fileConfig = 
                new FileWriter(System.getProperty("java.io.tmpdir",".")
                               + strConfigFileName);
            Object objElement[] = list.toArray();
            for (int i=0; i<list.size(); i++) 
            {
                Config_Element cfgElement = (Config_Element)objElement[i];
                fileConfig.write(cfgElement.strID + "|" + 
                                 cfgElement.strValue + "\n");
            }            
            fileConfig.close();
        }
        catch (IOException ex) 
        {
            writeError("I/O error while trying to write to file" + 
                    strConfigFileName);
        }
        
        return;
    }
}
