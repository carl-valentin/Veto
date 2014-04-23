package de.carlvalentin.ValentinConsole;

import java.util.*;
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
public final class SohEtb {
	public final String gl_strSOH;
	public final String gl_strETB;
	public final int  gl_iSOH;
	public final int  gl_iETB;
	private String id;
	public final int ord;
	private SohEtb prev;
	private SohEtb next;

	private static int upperBound = 0;
	private static SohEtb first = null;
	private static SohEtb last = null;
    
	private SohEtb(byte bSOH, byte bETB) {
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
		this.id = gl_strSOH + "/" + gl_strETB;
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
			private SohEtb curr = first;
			public boolean hasMoreElements() {
		  		return curr != null;
			}
			public Object nextElement() {
		  		SohEtb c = curr;
		  		curr = curr.next();
		  		return c;
			}
	 	};
	}
	public String toString() {return this.id; }
	public static int size() { return upperBound; }
	public static SohEtb first() { return first; }
	public static SohEtb last()  { return last;  }
	public SohEtb prev()  { return this.prev; }
	public SohEtb next()  { return this.next; }
	
	public static final SohEtb x0117 = 
		new SohEtb((byte)0x01, (byte)0x17);
	public static final SohEtb x5E5F = 
		new SohEtb((byte)0x5E, (byte)0x5F);
}
