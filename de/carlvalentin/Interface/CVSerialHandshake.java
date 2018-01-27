package de.carlvalentin.Interface;

import java.util.Enumeration;

import gnu.io.SerialPort;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Handshakeverfahren
 */
public final class CVSerialHandshake
{
    public final String sz_Description;
    public final int i_Handshake;
    private String id;
    public final int ord;
    
    private CVSerialHandshake prev;
    private CVSerialHandshake next;

    private static int upperBound = 0;
    private static CVSerialHandshake first = null;
    private static CVSerialHandshake last = null;
    
    private CVSerialHandshake(int handshake, String desc)
    {
        this.i_Handshake = handshake;
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
            private CVSerialHandshake curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVSerialHandshake c = curr;
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
    
    public static CVSerialHandshake first() 
    { 
        return first; 
    }
    
    public static CVSerialHandshake last()  
    { 
        return last;  
    }
    
    public CVSerialHandshake prev()  
    { 
        return this.prev; 
    }
    public CVSerialHandshake next()  
    { 
        return this.next; 
    }
    
    public static CVSerialHandshake fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSerialHandshake.none.toString()) == 0)
        {            
            return CVSerialHandshake.none;
        }

        if (str.compareTo(CVSerialHandshake.xon_xoff.toString()) == 0)
        {           
            return CVSerialHandshake.xon_xoff;
        }
        
        return null;
    }
    
    public static final CVSerialHandshake none = 
        new CVSerialHandshake(SerialPort.FLOWCONTROL_NONE, "none");
    public static final CVSerialHandshake xon_xoff = 
        new CVSerialHandshake(SerialPort.FLOWCONTROL_XONXOFF_IN 
                              | SerialPort.FLOWCONTROL_XONXOFF_OUT, "xon/xoff");    
}
