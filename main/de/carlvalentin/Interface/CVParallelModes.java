package de.carlvalentin.Interface;

import java.util.Enumeration;

import javax.comm.ParallelPort;

/**
 * Enum-Klasse zur Darstellung der unterstuetzten Baudraten
 */
public final class CVParallelModes 
{
    public final String sz_Description;
    public final int i_Mode;
    private String id;
    public final int ord;
    
    private CVParallelModes prev;
    private CVParallelModes next;

    private static int upperBound = 0;
    private static CVParallelModes first = null;
    private static CVParallelModes last = null;
    
    private CVParallelModes(int mode, String desc)
    {
        this.i_Mode = mode;
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
            private CVParallelModes curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVParallelModes c = curr;
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
    
    public static CVParallelModes first() 
    { 
        return first; 
    }
    
    public static CVParallelModes last()  
    { 
        return last;  
    }
    
    public CVParallelModes prev()  
    { 
        return this.prev; 
    }
    public CVParallelModes next()  
    { 
        return this.next; 
    }
    
    public static CVParallelModes fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo( CVParallelModes.modeANY.toString() ) == 0)
        {            
            return CVParallelModes.modeANY;
        }

        if (str.compareTo( CVParallelModes.modeECP.toString() ) == 0)
        {           
            return CVParallelModes.modeECP;
        }
        
        if (str.compareTo( CVParallelModes.modeEPP.toString() ) == 0)
        {
            return CVParallelModes.modeEPP;
        }
        
        if (str.compareTo( CVParallelModes.modeNIBBLE.toString() ) == 0)
        {            
            return CVParallelModes.modeNIBBLE;
        }

        if (str.compareTo( CVParallelModes.modePS2.toString() ) == 0)
        {           
            return CVParallelModes.modePS2;
        }
        
        if (str.compareTo( CVParallelModes.modeSPP.toString() ) == 0)
        {
            return CVParallelModes.modeSPP;
        }
        
        return null;
    }
    
    public static final CVParallelModes modeANY =    new CVParallelModes( 
            ParallelPort.LPT_MODE_ANY,    "ANY - picks best available");
    public static final CVParallelModes modeECP =    new CVParallelModes( 
            ParallelPort.LPT_MODE_ECP,    "ECP - enhanced capabilities port");
    public static final CVParallelModes modeEPP =    new CVParallelModes( 
            ParallelPort.LPT_MODE_EPP,    "EPP - extended parallel port");
    public static final CVParallelModes modeNIBBLE = new CVParallelModes( 
            ParallelPort.LPT_MODE_NIBBLE, "NIBBLE - nibble mode");
    public static final CVParallelModes modePS2 =    new CVParallelModes( 
            ParallelPort.LPT_MODE_PS2,    "PS2 - byte mode");
    public static final CVParallelModes modeSPP =    new CVParallelModes( 
            ParallelPort.LPT_MODE_SPP,    "SPP - compatibility port");
}
