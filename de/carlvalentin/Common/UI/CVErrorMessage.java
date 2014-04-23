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
    private boolean   lk_bConsole = false;

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

        if(lk_bConsole == true)
        {
            System.err.print(message);
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
        
        if(lk_bConsole == true)
        {
            System.err.print(message);
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
        if(this.lk_bShowWindow == true)
        {
            JOptionPane.showMessageDialog(
                    this.lk_cParentWindow,
                    message,
                    "Message",
                    JOptionPane.PLAIN_MESSAGE);
        }
        
        if(lk_bConsole == true)
        {
            System.err.print(message);
        }
        
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

        if(lk_bConsole == true)
        {
            System.err.print(message);
        }
        
        return;
    }

    /**
     * Anzeige Fehlermeldung ein- und ausschalten
     *
     * @param bShowWindow true - Anzeige ein, false - Anzeige aus
     */
    public void setShowWindow(boolean bShowWindow, boolean bConsole)
    {
        this.lk_bShowWindow = bShowWindow;
        this.lk_bConsole = bConsole;

        return;
    }
}
