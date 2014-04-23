package de.carlvalentin.Interface;

import java.util.Enumeration;

import javax.comm.SerialPort;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Parities.
 */
public final class CVSerialParity 
{
	public final String sz_Description;
    public final int i_Parity;
    private String id;
    public final int ord;
    
    private CVSerialParity prev;
    private CVSerialParity next;

    private static int upperBound = 0;
    private static CVSerialParity first = null;
    private static CVSerialParity last = null;
    
    private CVSerialParity(int bits, String desc)
    {
        this.i_Parity = bits;
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
            private CVSerialParity curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVSerialParity c = curr;
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
    
    public static CVSerialParity first() 
    { 
        return first; 
    }
    
    public static CVSerialParity last()  
    { 
        return last;  
    }
    
    public CVSerialParity prev()  
    { 
        return this.prev; 
    }
    public CVSerialParity next()  
    { 
        return this.next; 
    }
    
    public static CVSerialParity fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVSerialParity.none.toString()) == 0)
        {            
            return CVSerialParity.none;
        }

        if (str.compareTo(CVSerialParity.even.toString()) == 0)
        {           
            return CVSerialParity.even;
        }
        
        if (str.compareTo(CVSerialParity.odd.toString()) == 0)
        {
        	return CVSerialParity.odd;
        }
        
        return null;
    }
    
    public static final CVSerialParity none = 
        new CVSerialParity(SerialPort.PARITY_NONE, "none");
    public static final CVSerialParity even = 
        new CVSerialParity(SerialPort.PARITY_EVEN, "even");
    public static final CVSerialParity odd =
        new CVSerialParity(SerialPort.PARITY_ODD, "odd");
}
