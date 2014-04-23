package de.carlvalentin.Protocol.UI;

import de.carlvalentin.Protocol.CVSohEtb;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Grafische Oberfläche um den Inhalt eines CVSohEtb-Objektes ueber einen 
 * Auswahldialog mit allen Moeglichkeiten zu bestimmen.
 */
public class CVUISohEtb 
{
	/**
     * Programmfenster, zu welchem der Dialog gehoeren soll.
	 */
    private Component lk_cParentWindow;
    
    /**
     * Das zu aendernde Objekt der Klasse CVSohEtb
     */
    private CVSohEtb  lk_cSohEtb;
    
    /**
     * Konstruktor der Klass CVUISohEtb.
     * 
     * @param parentWindow Programmfenster, zu welchem der Dialog gehoeren soll.
     * @param value Das zu aendernde Objekt der Klasse CVSohEtb
     */
    public CVUISohEtb(Component parentWindow, CVSohEtb value)
    {
        this.lk_cParentWindow = parentWindow;
        this.lk_cSohEtb       = value;
        
        return;
    }
    
    /**
     * Anzeige des Dialogs und Verarbeitung des Ergebnis (setzen der 
     * ausgewaehlten Werte).
     */
    public void showDialog()
    {
        Object[] possibilityList = {"0x01/0x17", "0x5E/0x5F", "none"};
        String currentSetting = (String)possibilityList[2];
        if(this.lk_cSohEtb.gl_iSOH == 0x01)
        {
        	currentSetting = (String)possibilityList[0];
        }
        if(this.lk_cSohEtb.gl_iSOH == 0x5E)
        {
        	currentSetting = (String)possibilityList[1];
        }
        String value = (String)JOptionPane.showInputDialog(
                this.lk_cParentWindow, 
                "Select start/stop bit encoding from list:",
                "Select Protocol Encoding",
                JOptionPane.PLAIN_MESSAGE,
                null, // no icon
                possibilityList,
                currentSetting);
        if ((value != null) && (value.length() > 0))
        {
        	if(value.equals((String)"0x01/0x17"))
            {
        		this.lk_cSohEtb = CVSohEtb.x0117;
            }
            if(value.equals((String)"0x5E/0x5F"))
            {
                this.lk_cSohEtb = CVSohEtb.x5E5F;
            }
            if(value.equals((String)"none"))
            {
                this.lk_cSohEtb = CVSohEtb.none;
            }
        }
    	return;
    }
}
