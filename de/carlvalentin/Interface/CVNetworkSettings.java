package de.carlvalentin.Interface;

import de.carlvalentin.Common.UI.CVErrorMessage;
import de.carlvalentin.Common.UI.CVStatusLine;
import de.carlvalentin.Common.CVLogging;
import de.carlvalentin.Common.CVConfigFile;
import java.lang.Integer;

/**
 * Speichert Einstellungen fuer die Netzwerkschnittstelle.
 */
public class CVNetworkSettings extends CVInterfaceSettings
{
    /**
     * Pattern zur Ueberpruefung von IP-Adressen
     */
    private static final String IP_PATTERN = "^((\\d{1,2}|1\\d\\d|2[0-4]\\" +
            "d|25[0-5])\\.){3}(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    /**
     * Verwendetes Netzwerkprotokoll
     */
    private CVNetworkProtocol lk_cNetworkProtocol = null;

    /**
     * IP-Adresse des Druckers.
     */
    private String       lk_szTCPUDPPrinterIPAddress;
    /**
     * Token zum Speichern der ausgewaehlten IP-Adresse in Konfigurationsdatei
     */
    private final String lk_szConfigTokenTCPUDPPrinterIPAddress =
            "NetworkSettingsPrinterIPAddress";

    /**
     * Netzwerkport des Druckers
     */
    private int          lk_iTCPUDPPrinterPort;
    /**
     * Token zum Speichern des ausgewaehlten Ports in Konfigurationsdatei
     */
    private final String lk_szConfigTokenTCPUDPPrinterPort =
            "NetworkSettingsPrinterPort";

    /**
     * UDP-Pakete als Broadcast verschicken
     */
    private boolean      lk_bUDPBroadcast = false;
    /**
     * Token zum Spechern ob UDP-Pakete als Broadcast gesendet werden
     */
    private final String lk_szConfigTokenUDPBroadcast =
            "NetworkSettingsUDPBroadcast";

    private final String lk_szConfigTokenTCPKeepAlive =
            "NetworkSettingsTCPKeepAlive";
    private boolean      lk_bTCPKeepAlive = false;

    private final String lk_szConfigTokenTCPAutoReconn =
            "NetworkSettingsTCPAutoReconn";
    private boolean      lk_bTCPAutoReconn = false;

    private final String lk_szConfigTokenTCPSendAfterConnOnOff =
            "NetworkSettingsTCPSendAfterConnOnOff";
    private boolean      lk_bTCPAutoSendAfterConnOnOff = false;

    private final String lk_szConfigTokenTCPSendAfterConn =
            "NetworkSettingsTCPSendAfterConn";
    private String      lk_szTCPAutoSendAfterConn = "";

    /**
     * Konstruktor der Klasse CVNetworkSettings.
     *
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param cStatusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param cConfigFile Einlesen und Schreiben Konfigurationsdatei.
     * @param cNetworkProtocol Verwendetes Netzwerkprotokoll
     */
	public CVNetworkSettings(
            CVErrorMessage    cErrorMessage,
            CVLogging         cErrorFile,
            CVStatusLine      cStatusMessage,
            CVConfigFile      cConfigFile,
            CVNetworkProtocol cNetworkProtocol)
    {
        super(cErrorMessage, cErrorFile, cStatusMessage, cConfigFile);

        this.lk_cNetworkProtocol = cNetworkProtocol;

        this.setDefaults();

        this.initData();

		return;
    }

    /**
     * Werte fuer Datenstrukturen der Klasse aus Konfigurationsdatei einlesen.
     *
     */
    protected void initData()
    {
        if(this.lk_cConfigFile != null)
        {
        	String configValue = null;

        	//------------------------------------------------------------------
        	// TCP und UDP
        	//------------------------------------------------------------------
        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenTCPUDPPrinterIPAddress);
        	if(configValue != null)
        	{
        		this.lk_szTCPUDPPrinterIPAddress = configValue;
        	}
        	configValue = null;

        	configValue = this.lk_cConfigFile.getConfig(
                this.lk_szConfigTokenTCPUDPPrinterPort);
        	if(configValue != null)
        	{
        		this.lk_iTCPUDPPrinterPort = Integer.parseInt(configValue);
        	}

        	//------------------------------------------------------------------
        	// TCP
        	//------------------------------------------------------------------
        	if(this.lk_cNetworkProtocol == CVNetworkProtocol.TCP)
        	{
                configValue = this.lk_cConfigFile.getConfig(
                        this.lk_szConfigTokenTCPKeepAlive);
                if(configValue != null)
                {
                    if(configValue.equals((String)"true"))
                    {
                        this.lk_bTCPKeepAlive = true;
                    }
                    else
                    {
                        this.lk_bTCPKeepAlive = false;
                    }
                }
                configValue = null;

                configValue = this.lk_cConfigFile.getConfig(
                        this.lk_szConfigTokenTCPAutoReconn);
                if(configValue != null)
                {
                    if(configValue.equals((String)"true"))
                    {
                        this.lk_bTCPAutoReconn = true;
                    }
                    else
                    {
                        this.lk_bTCPAutoReconn = false;
                    }
                }
                configValue = null;

                configValue = this.lk_cConfigFile.getConfig(
                        this.lk_szConfigTokenTCPSendAfterConnOnOff);
                if(configValue != null)
                {
                    if(configValue.equals((String)"true"))
                    {
                        this.lk_bTCPAutoSendAfterConnOnOff = true;
                    }
                    else
                    {
                        this.lk_bTCPAutoSendAfterConnOnOff = false;
                    }
                }
                configValue = null;

                configValue = this.lk_cConfigFile.getConfig(
                        this.lk_szConfigTokenTCPSendAfterConn);
                    if(configValue != null)
                    {
                        this.lk_szTCPAutoSendAfterConn = configValue;
                    }
                    configValue = null;
        	}

        	//------------------------------------------------------------------
        	// UDP
        	//------------------------------------------------------------------
        	if(this.lk_cNetworkProtocol == CVNetworkProtocol.UDP)
        	{
        		configValue = this.lk_cConfigFile.getConfig(
        			this.lk_szConfigTokenUDPBroadcast);
        		if(configValue != null)
        		{
        			if(configValue.equals((String)"true"))
        			{
        				this.lk_bUDPBroadcast = true;
        			}
        			else
        			{
        				this.lk_bUDPBroadcast = false;
        			}
        		}
        		configValue = null;
        	}
        }

        return;
    }

    /**
     * Ueberprueft Einstellungen fuer die Netzwerkschnittstelle.
     *
     * @return true, wenn Einstellungen korrekt sind.
     */
    public boolean validateSettings()
    {
    	//----------------------------------------------------------------------
    	// TCP und UDP
    	//----------------------------------------------------------------------
        if (isValidIp(this.lk_szTCPUDPPrinterIPAddress) != true)
        {
            /*if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write(
                    "CVNetworkSettings: no a valid IP address");
            }*/

        	return false;
        }

        //----------------------------------------------------------------------
        // TCP
        //----------------------------------------------------------------------
        if (this.lk_cNetworkProtocol == CVNetworkProtocol.TCP)
        {
        	if (this.lk_bUDPBroadcast == true)
        	{
        		/*if(this.lk_cErrorMessage != null)
                {
                	this.lk_cErrorMessage.write(
                        "CVNetworkSettings: UDP broadcast in TCP protocol");
                }*/

            	return false;
        	}
        }

        //----------------------------------------------------------------------
        // UDP
        //----------------------------------------------------------------------
        if (this.lk_cNetworkProtocol == CVNetworkProtocol.UDP)
        {
            if ( (this.lk_bTCPKeepAlive == true) || (this.lk_bTCPAutoReconn == true) )
            {
                /*if(this.lk_cErrorMessage != null)
                {
                    this.lk_cErrorMessage.write(
                        "CVNetworkSettings: TCP KeepAlive in UDP protocol");
                }*/

                return false;
            }
        }

        return true;
    }

    /**
     * Fragt das verwendete Netzwerkprotokoll ab
     *
     * @return verwendetes Netzwerkprotokoll (null - keines eingestellt)
     */
    public CVNetworkProtocol getNetworkProtocol()
    {
    	return this.lk_cNetworkProtocol;
    }

    /**
     * Setzte Defaulteinstellungen fuer die Netzwerkschnittstelle.
     */
    public void setDefaults()
    {
    	//----------------------------------------------------------------------
    	// TCP und UDP
    	//----------------------------------------------------------------------
        this.lk_szTCPUDPPrinterIPAddress = "192.168.0.21";
        this.lk_iTCPUDPPrinterPort       = 9100;

        //----------------------------------------------------------------------
        // TCP
        //----------------------------------------------------------------------
        this.lk_bTCPKeepAlive            = false;
        this.lk_bTCPAutoReconn           = false;
        this.lk_bTCPAutoSendAfterConnOnOff = false;
        this.lk_szTCPAutoSendAfterConn   = "";

        //----------------------------------------------------------------------
        // UDP
        //----------------------------------------------------------------------
        this.lk_bUDPBroadcast            = false;

        return;
    }

    /**
     * Setzen der zu verwendenden IP-Adresse.
     *
     * @param ip IP-Adresse in Textform.
     */
    public void setIPAdress(String ip)
    {
    	this.lk_szTCPUDPPrinterIPAddress = ip;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
        			this.lk_szConfigTokenTCPUDPPrinterIPAddress,
                    this.lk_szTCPUDPPrinterIPAddress);
        }

        return;
    }

    /**
     * Abfrage der aktuell verwendeten IP-Adresse.
     *
     * @return IP-Adresse in Textform.
     */
    public String getIPAdress()
    {
    	return this.lk_szTCPUDPPrinterIPAddress;
    }

    /**
     * Setzen des zu verwendenden Netzwerkports.
     *
     * @param port Netzwerkport.
     */
    public void setPort(int port)
    {
    	this.lk_iTCPUDPPrinterPort = port;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
        	this.lk_cConfigFile.setConfig(
                    this.lk_szConfigTokenTCPUDPPrinterPort,
        			Integer.toString(this.lk_iTCPUDPPrinterPort));
        }

        return;
    }

    /**
     * Abfrage des aktuell verwendeten Netzwerkports
     *
     * @return Netzwerkport.
     */
    public int getPort()
    {
    	return this.lk_iTCPUDPPrinterPort;
    }

    /**
     * UDP-Broadcast ein- bzw. ausschalten
     *
     * @param bUDPBroadcast true - Broadcast ein, false - Broadcast aus
     */
    public void setUDPBroadcast(boolean bUDPBroadcast)
    {
    	this.lk_bUDPBroadcast = bUDPBroadcast;

    	// Speichern in Konfigurationsdatei
    	if(this.lk_cConfigFile != null)
    	{
    		if(this.lk_bUDPBroadcast == true)
    		{
    			this.lk_cConfigFile.setConfig(
    					this.lk_szConfigTokenUDPBroadcast,
    					"true");
    		}
    		else
    		{
    			this.lk_cConfigFile.setConfig(
    					this.lk_szConfigTokenUDPBroadcast,
    					"false");
    		}
    	}

    	return;
    }

    /**
     * Abfrage UDP-Broadcast
     *
     * @return true - Broadcast ein, false - Broadcast aus
     */
    public boolean getUDPBroadcast()
    {
    	return this.lk_bUDPBroadcast;
    }

    public void setTCPKeepAlive(boolean b)
    {
        lk_bTCPKeepAlive = b;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            if(this.lk_bTCPKeepAlive == true)
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPKeepAlive,
                        "true");
            }
            else
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPKeepAlive,
                        "false");
            }
        }
    }

    public boolean getTCPKeepAlive()
    {
        return lk_bTCPKeepAlive;
    }

    public void setTCPAutoReconn(boolean b)
    {
        lk_bTCPAutoReconn = b;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            if(this.lk_bTCPAutoReconn == true)
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPAutoReconn,
                        "true");
            }
            else
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPAutoReconn,
                        "false");
            }
        }
    }

    public boolean getTCPAutoReconn()
    {
        return lk_bTCPAutoReconn;
    }

    public void setTCPAutoSendAfterConnOnOff(boolean b)
    {
        lk_bTCPAutoSendAfterConnOnOff = b;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            if(this.lk_bTCPAutoSendAfterConnOnOff == true)
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPSendAfterConnOnOff,
                        "true");
            }
            else
            {
                this.lk_cConfigFile.setConfig(
                        this.lk_szConfigTokenTCPSendAfterConnOnOff,
                        "false");
            }
        }
    }

    public boolean getTCPAutoSendAfterConnOnOff()
    {
        return lk_bTCPAutoSendAfterConnOnOff;
    }

    public void setTCPAutoSendAfterConn(String ip)
    {
        this.lk_szTCPAutoSendAfterConn = ip;

        // Speichern in Konfigurationsdatei
        if(this.lk_cConfigFile != null)
        {
            this.lk_cConfigFile.setConfig(
                    this.lk_szConfigTokenTCPSendAfterConn,
                    this.lk_szTCPAutoSendAfterConn);
        }

        return;
    }

    public String getTCPAutoSendAfterConn()
    {
        return this.lk_szTCPAutoSendAfterConn;
    }

    /**
     * Ueberprueft String auf gueltige IP-Adresse
     *
     * @param ip IP-Adresse.
     * @return true, wenn Adresse gueltig.
     */
    private static boolean isValidIp(final String ip)
    {
        return ip.matches(IP_PATTERN);
    }
}
