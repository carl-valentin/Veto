package de.carlvalentin.Protocol;

import java.io.*;
import java.util.*;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Interface.*;

/**
 * Verwaltet Verbindungen zum Drucker
 */
public class CVConnectionManager
{
    /**
     * Ausgabe von Fehlermeldungen in Form von Dialogen
     */
    private CVErrorMessage       lk_cErrorMessage = null;

    /**
     * Ausgabe von Fehlermeldungen in Logdatei
     */
    private CVLogging            lk_cErrorFile = null;

    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine         lk_cStatusMessage = null;

    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    private CVConfigFile         lk_cConfigFile = null;

    /**
     * Gibt an, ob Verbindung zum Drucker besteht
     */
    private boolean              lk_cIsConnected;

    /**
     * Thread um Daten vom Drucker zu empfangen
     */
    private CVProtocolRecvThread lk_cRecvThread = null;

    /**
     * Thread um Daten an den Drucker zu senden
     */
    private CVProtocolSendThread lk_cSendThread = null;

    /**
     * Kodierung der Start- und Stopzeichen der CVPL.
     */
    private CVSohEtb             lk_cSohEtb = null;

    /**
     * Interface zur Netzwerkschnittstelle TCP-Protokoll
     */
    private CVNetworkTCP         lk_cInterfaceNetworkTCP = null;

    /**
     * Interface zur Netzwerkschnittstelle UDP-Protokoll
     */
    private CVNetworkUDP         lk_cInterfaceNetworkUDP = null;

    /**
     * Interface zur seriellen Schnittstelle
     */
    private CVSerial             lk_cInterfaceSerial = null;

    /**
     * Interface zur parallelen Schnittstelle
     */
    private CVParallel           lk_cInterfaceParallel = null;

    /**
     * Interface, uber welches Verbindung zum Drucker besteht
     */
    private CVInterface          lk_cInterfaceCurrentConnected = null;

    /**
     * Eingabestrom (Reader) aus welchem der Sendethread liest.
     */
    private Reader               lk_cSourceReader = null;

    /**
     * Liste Ausgabestroeme (Writer), in welche der Empfangsthread schreibt
     */
    private Vector               lk_cVectorSinkWriterRecvThread;
    private Vector               lk_cVectorSinkWriterSendThread;

    /**
     * Konstruktor der Klasse CVConnectionManager
     *
     * @param cConfigFile Einlesen und Schreiben Konfigurationsdatei.
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     */
    public CVConnectionManager(
            CVConfigFile   cConfigFile,
            CVErrorMessage cErrorMessage,
            CVLogging      cErrorFile,
            CVStatusLine   cStatusMessage)
    {
        this.lk_cConfigFile    = cConfigFile;
        this.lk_cErrorFile     = cErrorFile;
        this.lk_cErrorMessage  = cErrorMessage;
        this.lk_cStatusMessage = cStatusMessage;

        this.initCVConnectionManager();

    	return;
    }

    /**
     * Aufruf durch Garbage Collector
     */
    protected void finalize() throws Throwable
    {
    	if(this.lk_cInterfaceNetworkTCP != null)
    	{
    		this.lk_cInterfaceNetworkTCP.finalize();
    		this.lk_cInterfaceNetworkTCP = null;
    	}
    	if(this.lk_cInterfaceNetworkUDP != null)
    	{
    		this.lk_cInterfaceNetworkUDP.finalize();
    		this.lk_cInterfaceNetworkUDP = null;
    	}
    	if(this.lk_cInterfaceParallel != null)
    	{
    		this.lk_cInterfaceParallel.finalize();
    		this.lk_cInterfaceParallel = null;
    	}
    	if(this.lk_cInterfaceSerial != null)
    	{
    		this.lk_cInterfaceSerial.finalize();
    		this.lk_cInterfaceSerial = null;
    	}

        if(this.lk_cSourceReader != null)
        {
        	this.lk_cSourceReader = null;
        }

        return;
    }

    /**
     * Initialisiere Klassenkomponenten
     */
    private void initCVConnectionManager()
    {
        this.lk_cIsConnected = false;

        this.lk_cRecvThread = null;
        this.lk_cSendThread = null;

        this.lk_cSohEtb = CVSohEtb.none;

        //this.lk_cVectorSinkWriterRecvThread = new Vector();
        //this.lk_cVectorSinkWriterSendThread = new Vector();

        this.lk_cSourceReader = null;
    }

    /**
     * Abfrage, ob Verbindung zum Drucker besteht
     *
     * @return true, wenn Verbindung, sonst false
     */
    public boolean isConnected()
    {
    	return this.lk_cIsConnected;
    }

    /**
     * Abfrage Netzwerkinterface TCP-Protokoll
     *
     * @return Netzwerkinterface TCP-Protokoll
     */
    public CVNetworkTCP getTCPNetworkInterface()
    {
    	if(this.lk_cInterfaceNetworkTCP == null)
    	{
    		this.lk_cInterfaceNetworkTCP = new CVNetworkTCP(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConfigFile);
    	}
    	return this.lk_cInterfaceNetworkTCP;
    }

    /**
     * Setzen Netzwerkinterface TCP-Protokoll
     *
     * @param cTCPNetworkInterface Netzwerkinterface TCP-Protokoll
     */
    public void setTCPNetworkInterface(CVNetworkTCP cTCPNetworkInterface)
    {
        if(cTCPNetworkInterface != null)
        {
        	this.lk_cInterfaceNetworkTCP = cTCPNetworkInterface;
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setTCPNetworkInterface: null pointer");
            }
        }

        return;
    }

    /**
     * Abfrage Netzwerkinterface UDP-Protokoll
     *
     * @return Netzwerkinterface UDP-Protokoll
     */
    public CVNetworkUDP getUDPNetworkInterface()
    {
    	if(this.lk_cInterfaceNetworkUDP == null)
    	{
    		this.lk_cInterfaceNetworkUDP = new CVNetworkUDP(
            		this.lk_cErrorMessage,
            		this.lk_cErrorFile,
            		this.lk_cStatusMessage,
            		this.lk_cConfigFile);
    	}
    	return this.lk_cInterfaceNetworkUDP;
    }

    /**
     * Setzen Netzwerkinterface UDP-Protokoll
     *
     * @param cUDPNetworkInterface Netzwerkinterface UDP-Protokoll
     */
    public void setUDPNetworkInterface(CVNetworkUDP cUDPNetworkInterface)
    {
    	if(cUDPNetworkInterface != null)
    	{
    		this.lk_cInterfaceNetworkUDP = cUDPNetworkInterface;
    	}
    	else
    	{
    		if(this.lk_cErrorMessage != null)
    		{
    			this.lk_cErrorMessage.write("CVConnectionManager->" +
    					"setUDPNetworkInterface: null pointer");
    		}
    	}

    	return;
    }

    /**
     * Abfrage Parallelinterface
     *
     * @return Parallelinterface
     */
    public CVParallel getParallelInterface()
    {
    	if(this.lk_cInterfaceParallel == null)
    	{
    		this.lk_cInterfaceParallel = new CVParallel(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConfigFile);
    	}
    	return this.lk_cInterfaceParallel;
    }

    /**
     * Setzen Parallelinterface
     *
     * @param cParallelInterface Parallelinterface
     */
    public void setParallelInterface(CVParallel cParallelInterface)
    {
        if(cParallelInterface != null)
        {
        	this.lk_cInterfaceParallel = cParallelInterface;
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setParallelInterface: null pointer");
            }
        }

        return;
    }

    /**
     * Abfrage serielles Interface
     *
     * @return serielles Interface
     */
    public CVSerial getSerialInterface()
    {
    	if(this.lk_cInterfaceSerial == null)
    	{
    		this.lk_cInterfaceSerial = new CVSerial(
                    this.lk_cErrorMessage,
                    this.lk_cErrorFile,
                    this.lk_cStatusMessage,
                    this.lk_cConfigFile);
    	}
    	return this.lk_cInterfaceSerial;
    }

    /**
     * Setzen serielles Interface
     *
     * @param cSerialInterface serielles Interface
     */
    public void setSerialInterface(CVSerial cSerialInterface)
    {
        if(cSerialInterface != null)
        {
        	this.lk_cInterfaceSerial = cSerialInterface;
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setSerialInterface: null pointer");
            }
        }

        return;
    }

    /**
     * Abfrage des Interface, ueber welches eine Verbindung zum Drucker besteht
     *
     * @return Mit Drucker verbundenes Interface
     */
    public CVInterface getConnectedInterface()
    {
    	return this.lk_cInterfaceCurrentConnected;
    }

    /**
     * Setzt die Quelle des Datenstroms (Reader) zum Interface
     *
     * @param cSourceReader Reader der Eingabekomponente
     */
	public void setSource(Reader cSourceReader)
    {
        if(cSourceReader != null)
        {
        	this.lk_cSourceReader = cSourceReader;
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setSource: null pointer");
            }
        }

		return;
    }

    /**
     * Setzt das Ziel des Datenstroms (Writer) vom Interface
     *
     * @param cSinkWriter Writer der Ausgabekomponente
     * @param bRecvThread true, Writer wird f&uuml;r Empfangsthread registriert
     * @param bSendThread true, Writer wird f&uuml;r Sendenthread registriert
     */
    public void setSink(Writer cSinkWriter,
    					boolean bRecvThread,
    					boolean bSendThread)
    {
        if(cSinkWriter != null)
        {
        	if(bRecvThread == true)
        	{
        		if(this.lk_cVectorSinkWriterRecvThread == null)
        		{
        			this.lk_cVectorSinkWriterRecvThread = new Vector();
        		}
        		this.lk_cVectorSinkWriterRecvThread.add((Object)cSinkWriter);
        	}
        	if(bSendThread == true)
        	{
        		if(this.lk_cVectorSinkWriterSendThread == null)
        		{
        			this.lk_cVectorSinkWriterSendThread = new Vector();
        		}
        		this.lk_cVectorSinkWriterSendThread.add((Object)cSinkWriter);
        	}
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setSink: null pointer");
            }
        }

        return;
    }

    /**
     * Binaeren Ausgabestrom zum Interface abfragen
     *
     * @return Ausgabestrom, wenn vorhanden, sonst null
     */
    public OutputStream getInterfaceBinaryOutput()
    {
    	if(this.lk_cSendThread != null)
        {
    		return this.lk_cSendThread.getBinaryOutput();
        }

        if(this.lk_cErrorMessage != null)
        {
        	this.lk_cErrorMessage.write("CVConnectionManager->" +
                    "getInterfaceBinaryOutput: interface has no binary output" +
                    " or send thread not running");
        }
        if(this.lk_cErrorFile != null)
        {
        	this.lk_cErrorFile.write("CVConnectionManager->" +
                    "getInterfaceBinaryOutput: interface has no binary output" +
                    "or send thread not running ");
        }

        return null;
    }

    /**
     * Binaeren Eingabestrom vom Interface abfragen
     *
     * @return Eingabestrom, wenn vorhanden, sonst null
     */
    public InputStream getInterfaceBinaryInput()
    {
    	if(this.lk_cRecvThread != null)
        {
    		return this.lk_cRecvThread.getBinaryInput();
        }

        if(this.lk_cErrorMessage != null)
        {
        	this.lk_cErrorMessage.write("CVConnectionManager->" +
                    "getInterfaceBinaryInput: interface has no binary input " +
                    "or receive thread not running");
        }
        if(this.lk_cErrorFile != null)
        {
            this.lk_cErrorFile.write("CVConnectionManager->" +
                    "getInterfaceBinaryInput: interface has no binary input " +
                    "or receive thread not running ");
        }

        return null;
    }

    /**
     * Abfrage der Kodierung der Start- und Stopzeichen der CVPL
     *
     * @return aktuell verwendete Kodierung der Start- und Stopzeichen der CVPL
     */
    public CVSohEtb getSohEtb()
    {
    	return this.lk_cSohEtb;
    }

    /**
     * Setzen der Kodierung der Start- und Stopzeichen der CVPL
     *
     * @param cSohEtb Kodierung CVPL
     */
    public void setSohEtb(CVSohEtb cSohEtb)
    {
        if(cSohEtb != null)
        {
        	this.lk_cSohEtb = cSohEtb;

        	/*if(this.lk_cRecvThread != null)
        	{
        		this.lk_cRecvThread.setSohEtb(this.lk_cSohEtb);
        	}*/

        	//Umschaltung im laufenden Thread nur f&uuml;r Datei&uuml;bertragung notwendig
        	if(this.lk_cSendThread != null)
        	{
        		this.lk_cSendThread.setSohEtb(this.lk_cSohEtb);
        	}
        }
        else
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "setSohEtb: null pointer");
            }
        }

    	return;
    }

    public boolean connectLight(CVInterface cInterface)
    {
        if((this.lk_cIsConnected  == true)||
           (this.lk_cSohEtb       == null))
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: allready connected to printer");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: allready connected to printer");
            }

            return false;
        }

        //----------------------------------------------------------------------
        // Interface oeffnen
        //----------------------------------------------------------------------
        if(cInterface.open() == false)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: could not open interface");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: could not open interface");
            }

            return false;
        }

    	this.lk_cInterfaceCurrentConnected = cInterface;

    	this.lk_cIsConnected = true;

    	return true;
    }

    /**
     * Stellt eine Verbindung zum Drucker her
     *
     * @param cInterface gewaehlte Schnittstelle
     * @return true, wenn Verbindung hergestellt, sonst false
     */
    public boolean connect(CVInterface cInterface)
    {
    	if((this.lk_cIsConnected  == true)||
           (this.lk_cVectorSinkWriterRecvThread == null) ||
           (this.lk_cVectorSinkWriterSendThread == null) ||
           (this.lk_cSourceReader == null)||
           (this.lk_cSohEtb       == null))
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: allready connected to printer");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: allready connected to printer");
            }

        	return false;
        }

    	//----------------------------------------------------------------------
        // Interface oeffnen
    	//----------------------------------------------------------------------
        if(cInterface.open() == false)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: could not open interface");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: could not open interface");
            }

        	return false;
        }

        //----------------------------------------------------------------------
        // Parallelport hat unter Java keine Moeglichkeit Daten zu empfangen
        //----------------------------------------------------------------------
        if(cInterface.equals((CVInterface)this.lk_cInterfaceParallel) != true)
        {
        	//------------------------------------------------------------------
        	// Thread zum Empfangen von Daten anlegen
        	//------------------------------------------------------------------
        	this.lk_cRecvThread = new CVProtocolRecvThread(
        			this.lk_cErrorMessage,
					this.lk_cErrorFile,
					this.lk_cStatusMessage);
        	this.lk_cRecvThread.setSohEtb(this.lk_cSohEtb);

        	if(cInterface.hasInterfaceReader() == true)
        	{
        		this.lk_cRecvThread.setThreadReader(
        				cInterface.getInterfaceReader());
        		this.lk_cRecvThread.setThreadWriter
        					(this.lk_cVectorSinkWriterRecvThread);
        		this.lk_cRecvThread.setBinaryInput(
        				cInterface.getInterfaceBinaryInput());
        	}
        	else
        	{
        		if(this.lk_cErrorMessage != null)
        		{
        			this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: interface has no reader - no receive thread");
        		}
        		if(this.lk_cErrorFile != null)
        		{
        			this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: interface has no reader - no receive thread");
        		}

        		return false;
        	}
        }

        //----------------------------------------------------------------------
        // Thread zum Senden von Daten anlegen
        //----------------------------------------------------------------------
        this.lk_cSendThread = new CVProtocolSendThread(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);
        this.lk_cSendThread.setSohEtb(this.lk_cSohEtb);
        if(cInterface.hasInterfaceWriter() == true)
        {
            this.lk_cSendThread.setThreadReader(this.lk_cSourceReader);
            this.lk_cVectorSinkWriterSendThread.add
        				((Object)cInterface.getInterfaceWriter());
            this.lk_cSendThread.setThreadWriter
            			(this.lk_cVectorSinkWriterSendThread);

            this.lk_cSendThread.setBinaryOutput(
                cInterface.getInterfaceBinaryOutput());
        }
        else
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "connect: interface has no writer - no send thread");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "connect: interface has no writer - no send thread");
            }

        	return false;
        }

        //----------------------------------------------------------------------
        // Threads starten
        //----------------------------------------------------------------------
        if(this.lk_cRecvThread != null)
        {
        	this.lk_cRecvThread.start();
        }
        if(this.lk_cSendThread != null)
        {
        	this.lk_cSendThread.start();
        }

        this.lk_cInterfaceCurrentConnected = cInterface;

        this.lk_cIsConnected = true;

    	return true;
    }

    /**
     * Beendet bestehende Verbindung
     *
     * @param cInterface gewaehlte Schnittstelle
     * @return true, wenn Verbindung beendet, sonst false
     */
    public boolean disconnect(CVInterface cInterface)
    {
        if((this.lk_cIsConnected  == false)||
           (this.lk_cVectorSinkWriterRecvThread == null) ||
           (this.lk_cVectorSinkWriterSendThread == null) ||
           (this.lk_cSourceReader == null))
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "disconnect: not connected to printer");
            }
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("CVConnectionManager->" +
                        "disconnect: not connected to printer");
            }

        	return false;
        }

        //----------------------------------------------------------------------
        // Threads beenden
        //----------------------------------------------------------------------
        if(this.lk_cRecvThread != null)
        {
        	this.lk_cRecvThread.stopThread();
        	this.lk_cRecvThread = null;
        }
        if(this.lk_cSendThread != null)
        {
        	this.lk_cSendThread.stopThread();
        	this.lk_cSendThread = null;
        }

        try
        {
        	Thread.sleep(250);
        }
        catch(InterruptedException ex) {}

        //----------------------------------------------------------------------
        // Interface schliessen
        //----------------------------------------------------------------------
        if(cInterface.close() == false)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVConnectionManager->" +
                        "disconnect: could not close interface");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVConnectionManager->" +
                        "disconnect: could not close interface");
            }

        	return false;
        }

        //----------------------------------------------------------------------
        // Interface loeschen
        //----------------------------------------------------------------------
        if(cInterface.equals(this.lk_cInterfaceNetworkTCP) == true)
        {
        	this.lk_cInterfaceNetworkTCP = null;
        }
        if(cInterface.equals(this.lk_cInterfaceNetworkUDP) == true)
        {
        	this.lk_cInterfaceNetworkUDP = null;
        }
        if(cInterface.equals(this.lk_cInterfaceParallel) == true)
        {
        	this.lk_cInterfaceParallel = null;
        }
        if(cInterface.equals(this.lk_cInterfaceSerial) == true)
        {
        	this.lk_cInterfaceSerial = null;
        }
        if(this.lk_cVectorSinkWriterRecvThread != null)
        {
        	this.lk_cVectorSinkWriterRecvThread.clear();
        	this.lk_cVectorSinkWriterRecvThread = null;
        }
        if(this.lk_cVectorSinkWriterSendThread != null)
        {
        	this.lk_cVectorSinkWriterSendThread.clear();
        	this.lk_cVectorSinkWriterSendThread = null;
        }

        this.lk_cInterfaceCurrentConnected = null;

        this.lk_cIsConnected = false;

    	return true;
    }
}
