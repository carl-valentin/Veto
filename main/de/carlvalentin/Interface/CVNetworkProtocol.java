package de.carlvalentin.Interface;

import java.util.Enumeration;

/**
 * Enum-Klasse zur Auswahl der unterstuetzten Netzwerkprotokolle
 */
public final class CVNetworkProtocol 
{
	public final String sz_Description;
	
	private String id;
    public final int ord;
	
	private CVNetworkProtocol prev;
    private CVNetworkProtocol next;

    private static int upperBound = 0;
    private static CVNetworkProtocol first = null;
    private static CVNetworkProtocol last = null;
    
    private CVNetworkProtocol(String desc)
    {
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
    	
    	return;
    }
    
    public static Enumeration elements() 
    {
        return new Enumeration() 
        {
            private CVNetworkProtocol curr = first;
            public boolean hasMoreElements() 
            {
                return curr != null;
            }
            public Object nextElement() 
            {
                CVNetworkProtocol c = curr;
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
    
    public static CVNetworkProtocol first() 
    { 
        return first; 
    }
    
    public static CVNetworkProtocol last()  
    { 
        return last;  
    }
    
    public CVNetworkProtocol prev()  
    { 
        return this.prev; 
    }
    public CVNetworkProtocol next()  
    { 
        return this.next; 
    }
    
    public static CVNetworkProtocol fromString(String str)
    {
        if (str == null) return null;
        
        if (str.compareTo(CVNetworkProtocol.TCP.toString()) == 0)
        {            
            return CVNetworkProtocol.TCP;
        }

        if (str.compareTo(CVNetworkProtocol.UDP.toString()) == 0)
        {           
            return CVNetworkProtocol.UDP;
        }
        
        return null;
    }
    
    public static final CVNetworkProtocol TCP = 
        new CVNetworkProtocol("TCP/IP");
    public static final CVNetworkProtocol UDP = 
        new CVNetworkProtocol("UDP/IP");
}
