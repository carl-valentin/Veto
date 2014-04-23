package de.carlvalentin.Common.Printf;

import com.sharkysoft.printf.*;

/**
 * Printf()vf&uuml Java - realisiert mit der Bibliothek lava3 von Sharkysoft
 */
public class CVPrintf 
{
    /**
     * Printfklasse aus der Bibliothek lava3 von Sharkysoft
     */
    private Printf   lk_cSharkysoftPrintf;
    
    /**
     * Konstruktor der Klasse CVPrintf
     */
	public CVPrintf()
    {        
		return;
    }
    
    /**
     * Printf im C-Stil
     * 
     * @param sFormat Text mit Formatangaben
     * @param cParam Parameter und Variablen f&uumlr Platzhalter in sFormat
     * @return formatierter Text
     */
    public String printf(String sFormat, Object[] cParam)
    {
    	return lk_cSharkysoftPrintf.format(sFormat, cParam);
    }
}
