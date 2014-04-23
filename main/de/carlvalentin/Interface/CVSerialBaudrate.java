package de.carlvalentin.Interface;

import java.util.Enumeration;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Baudraten
 */
public final class CVSerialBaudrate 
{
	public final String sz_Description;
    public final int i_Baudrate;
    private String id;
    public final int ord;
    
    private CVSerialBaudrate prev;
    private CVSerialBaudrate next;

    private static int upperBound = 0;
    private static CVSerialBaudrate first = null;
    private static CVSerialBaudrate last = null;
    
    private CVSerialBaudrate(int baud, String desc)
    {
    	this.i_Baudrate = baud;
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
            private CVSerialBaudrate curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVSerialBaudrate c = curr;
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
    
    public static CVSerialBaudrate first() 
    { 
        return first; 
    }
    
    public static CVSerialBaudrate last()  
    { 
        return last;  
    }
    
    public CVSerialBaudrate prev()  
    { 
        return this.prev; 
    }
    public CVSerialBaudrate next()  
    { 
        return this.next; 
    }
    
    public static CVSerialBaudrate fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSerialBaudrate.bps2400.toString()) == 0)
        {            
            return CVSerialBaudrate.bps2400;
        }

        if (str.compareTo(CVSerialBaudrate.bps4800.toString()) == 0)
        {           
            return CVSerialBaudrate.bps9600;
        }
        
        if (str.compareTo(CVSerialBaudrate.bps9600.toString()) == 0)
        {
            return CVSerialBaudrate.bps9600;
        }
        
        if (str.compareTo(CVSerialBaudrate.bps19200.toString()) == 0)
        {            
            return CVSerialBaudrate.bps19200;
        }

        if (str.compareTo(CVSerialBaudrate.bps57600.toString()) == 0)
        {           
            return CVSerialBaudrate.bps57600;
        }
        
        if (str.compareTo(CVSerialBaudrate.bps115200.toString()) == 0)
        {
            return CVSerialBaudrate.bps115200;
        }
        
        return null;
    }
    
    public static final CVSerialBaudrate bps2400 = 
        new CVSerialBaudrate(  2400,   "2400 bps");
    public static final CVSerialBaudrate bps4800 = 
        new CVSerialBaudrate(  4800,   "4800 bps");
    public static final CVSerialBaudrate bps9600 = 
        new CVSerialBaudrate(  9600,   "9600 bps");
    public static final CVSerialBaudrate bps19200 = 
        new CVSerialBaudrate( 19200,  "19200 bps");
    public static final CVSerialBaudrate bps57600 = 
        new CVSerialBaudrate( 57600,  "57600 bps");
    public static final CVSerialBaudrate bps115200 = 
        new CVSerialBaudrate(115200, "115200 bps");
}
