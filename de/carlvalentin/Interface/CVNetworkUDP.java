package de.carlvalentin.Interface;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;

import java.io.*;
import java.net.*;

/**
 * Uebertraegt Daten ueber UDP und die Netzwerkschnittstelle an den Drucker.
 */
public class CVNetworkUDP extends CVInterface 
{
	/**
     * Speichert die Einstellungen der Netzwerkschnittstelle.
     */
    private CVNetworkSettings lk_cNetworkSettingsUDP = null;
    
    /**
     * Socket zur Netzwerkkommunikation
     */
    private DatagramSocket    lk_cNetworkSocketUDP = null;
    
    /**
     * Daten von Applikation einlesen.
     */
    private PipedInputStream  lk_cApplicationInputStream = null;
    
    /**
     * Daten an Applikation weitergeben.
     */
    private PipedOutputStream lk_cApplicationOutputStream = null;
    
    /**
     * Gibt an, ob Thread zum Datenempfang laeuft
     */
    private boolean           lk_bDataRecvThreadIsRunning = false;
    private CVBinarySemaphore lk_cDataRecvThreadSemaphore = null;

    /**
     * Gibt an, ob Thread zum Datenversand laeuft
     */
    private boolean           lk_bDataSendThreadIsRunning = false;
    private CVBinarySemaphore lk_cDataSendThreadSemaphore = null;
	
	/**
	 * Konstruktor der Klasse CVNetworkUDP
	 * 
	 * @param errorMessage
	 * @param errorFile
	 * @param statusMessage
	 * @param configFile
	 */
    
   	public CVNetworkUDP(
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {		
		super(errorMessage, errorFile, statusMessage, configFile);
		
		this.lk_cNetworkSettingsUDP = new CVNetworkSettings(
                						this.lk_cErrorMessage,
                						this.lk_cErrorFile,
                						this.lk_cStatusMessage,
                						this.lk_cConfigFile,
                						CVNetworkProtocol.UDP);
		
		this.lk_cNetworkSocketUDP = null;
		
		this.lk_cDataRecvThreadSemaphore = new CVBinarySemaphore();
		this.lk_cDataSendThreadSemaphore = new CVBinarySemaphore();
		
		return;
    }
	
	/**
     * Aufraeumen, bevor das Objekt geloescht wird.
     */
    public void finalize() throws Throwable
    {
    	if(this.lk_bIsConnected == true)
    	{
    		if(this.close() != true)
    		{
            
    		}
    	}
        
        super.finalize();
    	
    	return;
    }
    
    /**
     * Oeffnen des Interface.
     * 
     * @return true, wenn Interface geoeffnet werden konnte.
     */
    public boolean open()
    {
    	if(this.lk_bIsConnected == true)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkUDP->open: " +
                    "UDP network interface allready open");
            }
            
        	return false;
        }
    	
    	//----------------------------------------------------------------------
    	// Netzwerkschnittstelle oeffnen
    	//----------------------------------------------------------------------
    	try
    	{
    		this.lk_cNetworkSocketUDP = new DatagramSocket();
    		this.lk_cNetworkSocketUDP.setBroadcast(
    				this.lk_cNetworkSettingsUDP.getUDPBroadcast());
    	}
    	catch(SocketException ex)
    	{
    		/*if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVNetworkUDP->open: " +
            		"SocketException: cannot create socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkTCP->open: " +
                	"SocketException: cannot create socket - " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkUDP: network port not open");
            
            return false;
    	}
    	catch(SecurityException ex)
    	{
    		/*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkUDP->open: " + 
                	"SecurityException: not allowed to open TCP socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkUDP->open : " +
                	"SecurityException: not allowed to open TCP socket - " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkTCP: network port not open");
            
            return false;
    	}
    	
    	//----------------------------------------------------------------------
    	// Ein- / Ausgabestroeme erstellen
    	//----------------------------------------------------------------------
    	//  <- InputStreamReader                PipedOutputStream <-         
    	//                     \                /
    	//                     (Piped)InputStream
    	this.lk_cInputStreamBinary = new PipedInputStream();
    	this.lk_cInputStreamReader = 
    						new InputStreamReader(this.lk_cInputStreamBinary);
    	try
    	{
    		this.lk_cApplicationOutputStream = 	new PipedOutputStream(
    				(PipedInputStream)this.lk_cInputStreamBinary);
    	}
    	catch(IOException ex) 
    	{
    		/*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                    "CVNetworkUDP->open: IOException: wrong streams network");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVNetwork-UDP->open: IOException: " +
                    "wrong streams network - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkUDP: network port not open");
            
            return false;
    	}
    	//  -> OutputStreamWriter                 PipedInputStream ->
    	//                      \                 /
    	//                      (Piped)OutputStream
    	this.lk_cOutputStreamBinary = new PipedOutputStream();
    	this.lk_cOutputStreamWriter =
    						new OutputStreamWriter(this.lk_cOutputStreamBinary);
    	try
    	{
    		this.lk_cApplicationInputStream = new PipedInputStream(
    				(PipedOutputStream)this.lk_cOutputStreamBinary); 
    	}
    	catch(IOException ex)
    	{
    		/*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write(
                    "CVNetworkUDP->open: IOException: wrong streams network");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVNetwork-UDP->open: IOException: " +
                    "wrong streams network - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkUDP: network port not open");
            
            return false;
    	}
    	
    	//----------------------------------------------------------------------
    	// Threads starten
    	//----------------------------------------------------------------------
    	this.startRecvThread();
    	this.startSendThread();
    	
    	this.lk_cStatusMessage.write("CVNetworkUDP: network port open");
        
        this.lk_bIsConnected = true;
    	
    	return true;
    }
    
    /**
     * Thread zum Empfangen von Daten
     *
     */
    private void startRecvThread()
    {
    	new Thread( new Runnable()
    	{
    		public void run()
    		{
    			lk_cDataRecvThreadSemaphore.grab();
    			lk_bDataRecvThreadIsRunning = true;
    			lk_cDataRecvThreadSemaphore.release();
    			
    			int iDataLength = 1536;
    			int iDataPointer = 0;
    			byte[] bData = new byte[iDataLength];
    			
    			DatagramPacket cNetworkPacket = null;
    			
    			while(true)
    			{
    				//----------------------------------------------------------
    				// Soll Thread weiterlaufen?
    				//----------------------------------------------------------
    				lk_cDataRecvThreadSemaphore.grab();
    				if(lk_bDataRecvThreadIsRunning == false)
    				{
    					lk_cDataRecvThreadSemaphore.release();
    					break;
    				}
    				lk_cDataRecvThreadSemaphore.release();
    				
    				//----------------------------------------------------------
    				// Daten von Socket einlesen
    				//----------------------------------------------------------
    				cNetworkPacket = new DatagramPacket(bData, iDataLength);
    				try
    				{
    					try
    					{
    						lk_cNetworkSocketUDP.setSoTimeout(100);
    						lk_cNetworkSocketUDP.receive(cNetworkPacket);
    					}
    					catch(InterruptedIOException ex)
    					{
    						// receive timed out
    						continue;
    					}
    					
    					iDataPointer = cNetworkPacket.getLength();
    					lk_cApplicationOutputStream.write(
    							cNetworkPacket.getData(), 0, iDataPointer);
    					lk_cApplicationOutputStream.flush();
    				}
    				catch(PortUnreachableException ex)
					{
						if(lk_cErrorMessage != null)
				        {
				            lk_cErrorMessage.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"PortUnreachableException: " +
				                	"wrong target (printer) settings");
				        }
				        if(lk_cErrorFile != null)
				        {
				            lk_cErrorFile.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"PortUnreachableException: " +
				            		"wrong target (printer) settings:" +
				            		ex.getMessage());
				        }
					}
					catch(SecurityException ex)
					{
						if(lk_cErrorMessage != null)
				        {
				            lk_cErrorMessage.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"SecurityException: " +
				                	"wrong security settings");
				        }
				        if(lk_cErrorFile != null)
				        {
				            lk_cErrorFile.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"SecurityException: " +
				            		"wrong security settings" +
				            		ex.getMessage());
				        }
					}
					catch(IOException ex)
					{
						if(lk_cErrorMessage != null)
				        {
				            lk_cErrorMessage.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"IOException: " +
				                	"problem during data transmission");
				        }
				        if(lk_cErrorFile != null)
				        {
				            lk_cErrorFile.write(
				            		"CVNetworkUDP->sendThread: " +
				            		"IOException: " +
				            		"problem during data transmission" +
				            		ex.getMessage());
				        }
					}
    			}
    			
    			return;
    		}
    	}).start();
    	
    	return;
    }
    
    /**
     * Thread zum Senden von Daten
     *
     */
    private void startSendThread()
    {
    	new Thread( new Runnable()
    	{
    		public void run()
    		{
    			lk_cDataSendThreadSemaphore.grab();
    			lk_bDataSendThreadIsRunning = true;
    			lk_cDataSendThreadSemaphore.release();
    			
    			int iDataLength = 1536;
    			int iDataPointer = 0;
    			byte[] bDataBuffer = new byte[iDataLength];
    			
    			DatagramPacket cNetworkPacket = null;
    			
    			while(true)
    			{
    				//----------------------------------------------------------
    				// Soll Thread weiterlaufen ?
    				//----------------------------------------------------------
    				lk_cDataSendThreadSemaphore.grab();
    				if(lk_bDataSendThreadIsRunning == false)
    				{
    					lk_cDataSendThreadSemaphore.release();
    					break;
    				}
    				lk_cDataSendThreadSemaphore.release();
    				
    				//----------------------------------------------------------
    				// Daten auf Socket ausgeben
    				//----------------------------------------------------------
    				try
    				{
    					if(lk_cApplicationInputStream.available() > 0)
    					{
    						iDataPointer = 0;
    						while((lk_cApplicationInputStream.available() > 0)&&
    							  (iDataPointer < iDataLength))
    						{
    							bDataBuffer[iDataPointer] = 
    								(byte) lk_cApplicationInputStream.read();
    							iDataPointer++;
    						}
    						cNetworkPacket = new DatagramPacket(
    												bDataBuffer,
    												iDataPointer);
    						cNetworkPacket.setAddress(Inet6Address.getByName(
    									lk_cNetworkSettingsUDP.getIPAdress()));
    						cNetworkPacket.setPort(
    									lk_cNetworkSettingsUDP.getPort());
    						try
    						{
    							lk_cNetworkSocketUDP.send(cNetworkPacket);
    						}
    						catch(PortUnreachableException ex)
    						{
    							if(lk_cErrorMessage != null)
    				            {
    				                lk_cErrorMessage.write(
    				                    "CVNetworkUDP->sendThread: " +
    				                    "PortUnreachableException: " +
    				                    "wrong target (printer) settings");
    				            }
    				            if(lk_cErrorFile != null)
    				            {
    				                lk_cErrorFile.write(
    				                	"CVNetworkUDP->sendThread: " +
        				                "PortUnreachableException: " +
        				                "wrong target (printer) settings:" +
        				                ex.getMessage());
    				            }
    						}
    						catch(SecurityException ex)
    						{
    							if(lk_cErrorMessage != null)
    				            {
    				                lk_cErrorMessage.write(
    				                    "CVNetworkUDP->sendThread: " +
    				                    "SecurityException: " +
    				                    "wrong security settings");
    				            }
    				            if(lk_cErrorFile != null)
    				            {
    				                lk_cErrorFile.write(
    				                	"CVNetworkUDP->sendThread: " +
        				                "SecurityException: " +
        				                "wrong security settings" +
        				                ex.getMessage());
    				            }
    						}
    						catch(IOException ex)
    						{
    							if(lk_cErrorMessage != null)
    				            {
    				                lk_cErrorMessage.write(
    				                    "CVNetworkUDP->sendThread: " +
    				                    "IOException: " +
    				                    "problem during data transmission");
    				            }
    				            if(lk_cErrorFile != null)
    				            {
    				                lk_cErrorFile.write(
    				                	"CVNetworkUDP->sendThread: " +
        				                "IOException: " +
        				                "problem during data transmission" +
        				                ex.getMessage());
    				            }
    						}
    					}
    				}
    				catch(IOException ex)
    				{
    					if(lk_cErrorMessage != null)
			            {
			                lk_cErrorMessage.write(
			                    "CVNetworkUDP->sendThread: IOException: " +
			                    "problem working with data streams");
			            }
			            if(lk_cErrorFile != null)
			            {
			                lk_cErrorFile.write(
			                	"CVNetworkUDP->sendThread: IOException: " +
				                "problem working with data streams" +
				                ex.getMessage());
			            }
    				}
    				
    				try
    				{
    					Thread.sleep(5);
    				}
    				catch(InterruptedException ex)
    				{
    					
    				}
    			}
    			
    			return;
    		}
    	}).start();
    	return;
    }
    
    /**
     * Schliessen des Interface.
     * 
     * @return true, wenn Interface geschlossen werden konnte.
     */
    public boolean close()
    {
    	if(this.lk_bIsConnected == false)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkUDP->close: " +
                    "UDP network interface not open");
            }
            
        	return false;
        }
    	
    	//----------------------------------------------------------------------
    	// Threads stoppen
    	//----------------------------------------------------------------------
    	lk_cDataRecvThreadSemaphore.grab();
    	if(lk_bDataRecvThreadIsRunning == true)
    	{
    		lk_bDataRecvThreadIsRunning = false;
    	}
		lk_cDataRecvThreadSemaphore.release();
		
    	lk_cDataSendThreadSemaphore.grab();
    	if(lk_bDataSendThreadIsRunning == true)
    	{
    		lk_bDataSendThreadIsRunning = false;
    	}
		lk_cDataSendThreadSemaphore.release();
		
		// Auf Threads warten
		try
		{
			Thread.sleep(250);
		}
		catch(InterruptedException ex)
		{
			
		}

    	//----------------------------------------------------------------------
    	// Netzwerkschnittstelle schliessen
    	//----------------------------------------------------------------------
    	try
    	{
    		if(this.lk_cOutputStreamWriter != null)
        	{
        		this.lk_cOutputStreamWriter.flush();
        		this.lk_cOutputStreamWriter.close();
        		this.lk_cOutputStreamWriter = null;
        	}
    		
        	if(this.lk_cInputStreamReader != null)
        	{
        		this.lk_cInputStreamReader.close();
        		this.lk_cInputStreamReader = null;
        	}
    		
    		if(this.lk_cNetworkSocketUDP != null)
    		{
    			this.lk_cNetworkSocketUDP.close();
    		}
    	}
    	catch(IOException ex)
    	{
    		/*if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkUDP->close: " + 
                	IOException: could not close streams or socket");
            }*/
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorMessage.write("CVNetworkUDP->close: " +
                	"IOException: could not close stream or socket- " + 
                	ex.getMessage());
            }
            this.lk_cStatusMessage.write(
            		"CVNetworkUDP: network port not closed");
            
            return false;
    	}
    	
    	this.lk_cNetworkSocketUDP = null;
        
        this.lk_cStatusMessage.write("CVNetworkUDP: network port closed");
        
        this.lk_bIsConnected = false;
    	
    	return true;
    }
    
    /**
     * Abfrage der aktuellen Einstellungen.
     * 
     * @return Objekt zur Speicherung der Einstellungen.
     */
    public Object getInterfaceSettings()
    {
    	return (Object) this.lk_cNetworkSettingsUDP;
    }
    
    /**
     * Setzen der aktuellen Einstellungen.
     * 
     * @param cSettings Objekt zur Speicherung der Einstellungen.
     */
    public void setInterfaceSettings(Object cSettings)
    {
    	if(cSettings != null)
    	{
    		this.lk_cNetworkSettingsUDP = (CVNetworkSettings) cSettings;
    	}
    	
    	return;
    }
}
