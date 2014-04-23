package de.carlvalentin.Common.UI;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Zeigt einen Dialog mit einer Fehlermeldung an.
 */
public class CVErrorMessage 
{
	/**
     * Programmfenster, zu welchem der Dialog gehoeren soll.
     */
    private Component lk_cParentWindow;
    
    /**
     * Zeigt an, ob Fenster mit Fehlermeldung angezeigt werden soll
     */
    private boolean   lk_bShowWindow = true;
    
    /**
     * Konstruktor der Klasse CVErrorMessage.
     * 
     * @param parentWindow Programmfenster, zu welchem der Dialog gehoeren soll.
     */
    public CVErrorMessage(Component parentWindow)
    {
        this.lk_cParentWindow = parentWindow;
        
    	return;
    }
    
    /**
     * Anzeigen eines Dialoges mit Fhelermeldung.
     * 
     * @param message Text der Fehlermeldung.
     * @param window Fenster, zu dem die Fehlermeldung gehoert.
     * @return true = Yes-Button; false = No-Button
     */
    private boolean showDialog(Component window, String message, boolean bAsk)
    {
        int iAnswer = JOptionPane.NO_OPTION;
        
        if (bAsk)
        {
            iAnswer = JOptionPane.showConfirmDialog(
                         window,
                         message,
                         "Error during program execution!",
                         JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(
                    window,
                    message,
                    "Error during program execution!",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        if (iAnswer == JOptionPane.YES_OPTION)
        {
        	return true;
        }
    	return false;
    }
    
    /**
     * Ausgabe einer Fehlermeldung
     * 
     * @param message Text der Fehlermeldung.
     */
    public void write(String message)
    {
    	if(this.lk_bShowWindow == true)
    	{
    		this.showDialog(this.lk_cParentWindow, message, false);
    	}
        
        return;
    }
    
    /**
     * Ausgabe einer Fehlermeldung
     * 
     * @param message Text der Fehlermeldung.
     * @return true fuer Abbruch
     */
    public boolean writeAskAbort(String message)
    {
        if(this.lk_bShowWindow == true)
        {
            return this.showDialog(this.lk_cParentWindow, 
                            message + "\nDo you want to abort?", 
                            true);
        }
        
        return false;
    }
    
    /**
     * Ausgabe eines Textes
     * 
     * @param message Text der Fehlermeldung.
     */
    public void writeMsg(String message)
    {
        JOptionPane.showMessageDialog(
                this.lk_cParentWindow,
                message,
                "Message",
                JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Ausgabe einer Fehlermeldung
     * 
     * @param message Text der Fehlermeldung.
     * @param window Fenster, zu dem die Fehlermeldung gehoert.
     */
    public void write(Component window, String message)
    {
    	if(this.lk_bShowWindow == true)
    	{
    		this.showDialog(window, message, false);
    	}
        
        return;
    }
    
    /**
     * Anzeige Fehlermeldung ein- und ausschalten
     * 
     * @param bShowWindow true - Anzeige ein, false - Anzeige aus
     */
    public void setShowWindow(boolean bShowWindow)
    {
    	this.lk_bShowWindow = bShowWindow;
    	
    	return;
    }
}
