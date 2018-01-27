package de.carlvalentin.Interface;

import java.util.Enumeration;

import gnu.io.SerialPort;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Anzahl an Datenbits.
 */
public final class CVSerialStopbits 
{
	public final String sz_Description;
    public final int i_Stopbits;
    private String id;
    public final int ord;
    
    private CVSerialStopbits prev;
    private CVSerialStopbits next;

    private static int upperBound = 0;
    private static CVSerialStopbits first = null;
    private static CVSerialStopbits last = null;
    
    private CVSerialStopbits(int bits, String desc)
    {
        this.i_Stopbits = bits;
        this.sz_Description = desc;
        
        this.id = this.sz_Description;
        
        this.ord = upperBound++;
        
        if (first == null) first = this;
        if (last != null) 
        {
            this.prev = last;
            last.next = this;
        }
        last = this;
    }
    
    public static Enumeration elements() 
    {
        return new Enumeration() 
        {
            private CVSerialStopbits curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVSerialStopbits c = curr;
                curr = curr.next();
                return c;
            }
        };
    }
    
    public String toString() 
    {
        return this.id; 
    }
    
    public static int size() 
    { 
        return upperBound; 
    }
    
    public static CVSerialStopbits first() 
    { 
        return first; 
    }
    
    public static CVSerialStopbits last()  
    { 
        return last;  
    }
    
    public CVSerialStopbits prev()  
    { 
        return this.prev; 
    }
    public CVSerialStopbits next()  
    { 
        return this.next; 
    }
    
    public static CVSerialStopbits fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSerialStopbits.bits2.toString()) == 0)
        {            
            return CVSerialStopbits.bits2;
        }

        if (str.compareTo(CVSerialStopbits.bits1.toString()) == 0)
        {           
            return CVSerialStopbits.bits1;
        }
        
        return null;
    }
    
    public static final CVSerialStopbits bits2 = 
        new CVSerialStopbits(SerialPort.STOPBITS_2, "2 bits");
    public static final CVSerialStopbits bits1 = 
        new CVSerialStopbits(SerialPort.STOPBITS_1, "1 bits");
}
