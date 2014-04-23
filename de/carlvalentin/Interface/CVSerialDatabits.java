package de.carlvalentin.Interface;

import java.util.Enumeration;

import javax.comm.SerialPort;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Anzahl an Datenbits
 */
public final class CVSerialDatabits 
{
	public final String sz_Description;
    public final int i_Databits;
    private String id;
    public final int ord;
    
    private CVSerialDatabits prev;
    private CVSerialDatabits next;

    private static int upperBound = 0;
    private static CVSerialDatabits first = null;
    private static CVSerialDatabits last = null;
    
    private CVSerialDatabits(int bits, String desc)
    {
        this.i_Databits = bits;
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
            private CVSerialDatabits curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVSerialDatabits c = curr;
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
    
    public static CVSerialDatabits first() 
    { 
        return first; 
    }
    
    public static CVSerialDatabits last()  
    { 
        return last;  
    }
    
    public CVSerialDatabits prev()  
    { 
        return this.prev; 
    }
    public CVSerialDatabits next()  
    { 
        return this.next; 
    }
    
    public static CVSerialDatabits fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSerialDatabits.bits7.toString()) == 0)
        {            
            return CVSerialDatabits.bits7;
        }

        if (str.compareTo(CVSerialDatabits.bits8.toString()) == 0)
        {           
            return CVSerialDatabits.bits8;
        }
        
        return null;
    }
    
    public static final CVSerialDatabits bits7 = 
        new CVSerialDatabits(SerialPort.DATABITS_7, "7 bits");
    public static final CVSerialDatabits bits8 = 
        new CVSerialDatabits(SerialPort.DATABITS_8, "8 bits");
}
