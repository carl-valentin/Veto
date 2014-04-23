package de.carlvalentin.Protocol;

import java.util.*;
import java.lang.Integer;
import java.io.UnsupportedEncodingException;

/**
 * SOH/ETB Enum-Klasse zur Dartellung der Start/Stop-Zeichen der 
 * Carl Valentin Printer Language (CVPL).<br>
 * <br>
 * Aufrufbeispiel:
 * <pre>
 *     SohEtb sohEtb = SohEtb.x5E5F;     // Start/Stop-Zeichen 5E/5F
 *     int iSoh = sohEtb.gl_iSOH;        // iSoh = 0x5E
 *     String strEtb = sohEtb.gl_strETB; // strEtb = "_"
 * </pre>
 */
public final class CVSohEtb {
    public final String gl_strSOH;
    public final String gl_strETB;
    public final int  gl_iSOH;
    public final int  gl_iETB;
    private String id;
    public final int ord;
    private CVSohEtb prev;
    private CVSohEtb next;

    private static int upperBound = 0;
    private static CVSohEtb first = null;
    private static CVSohEtb last = null;
    
    private CVSohEtb(byte bSOH, byte bETB) {
        byte[] baSOH = new byte[1]; 
        byte[] baETB = new byte[1];
        String strSOH;
        String strETB;
        
        baSOH[0] = bSOH; 
        baETB[0] = bETB;        
        this.gl_iSOH = bSOH;
        this.gl_iETB = bETB;
        try {
            strSOH = new String(baSOH, "US-ASCII");
            strETB = new String(baETB, "US-ASCII");
        }       
        catch (UnsupportedEncodingException ex) {
            System.err.println("Encoding US-ASCII is not supported " + 
                               "- try to encode with default encoding");
            strSOH = new String(baSOH);
            strETB = new String(baETB);             
        }
        this.gl_strSOH = strSOH;
        this.gl_strETB = strETB;
        //this.id = gl_strSOH + "/" + gl_strETB;
        this.id = "0x" + Integer.toHexString(this.gl_iSOH) + "/" + 
                  "0x" + Integer.toHexString(this.gl_iETB);
        this.ord = upperBound++;
        if (first == null) first = this;
        if (last != null) {
            this.prev = last;
            last.next = this;
        }
        last = this;
    }
    
    public static Enumeration elements() {
        return new Enumeration() {
            private CVSohEtb curr = first;
            public boolean hasMoreElements() {
                return curr != null;
            }
            public Object nextElement() {
                CVSohEtb c = curr;
                curr = curr.next();
                return c;
            }
        };
    }
    
    public String toString() {return this.id; }
    
    /**
     * Liefert den Wert &uuml;ber die von toString zur&uuml;ckgegebene
     * Stringrepresentation:
     * <pre>
     *     SohEtb sohEtb = SohEtb.x0117;           // sohEtb hat jetzt
     *                                             // den Wert 01/17
     *     String str5E5F = SohEtb.x5E5F.toString;
     *     sohEtb = SohEtb.fromString(str5E5F);    // sohEtb hat jetzt 
     *                                             // den Wert 5E/5F
     *     }
     * </pre>
     */
    public static CVSohEtb fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSohEtb.x0117.toString()) == 0)
        {            
            return CVSohEtb.x0117;
        }

        if (str.compareTo(CVSohEtb.x5E5F.toString()) == 0)
        {           
            return CVSohEtb.x5E5F;
        }
        
        if (str.compareTo(CVSohEtb.none.toString()) == 0)
        {
            return CVSohEtb.none;
        }
        
        return null;
    }
    
    public static int size() { return upperBound; }
    public static CVSohEtb first() { return first; }
    public static CVSohEtb last()  { return last;  }
    public CVSohEtb prev()  { return this.prev; }
    public CVSohEtb next()  { return this.next; }
    
    public static final CVSohEtb x0117 = 
        new CVSohEtb((byte)0x01, (byte)0x17);
    public static final CVSohEtb x5E5F = 
        new CVSohEtb((byte)0x5E, (byte)0x5F);
    public static final CVSohEtb none =
        new CVSohEtb((byte)0x00, (byte)0x00);
}