package de.carlvalentin.Interface;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.ValentinConsole.ValentinConsole;

// SSHJ SSH library (hierynomus fork 0.32.0)
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.kex.KeyExchange;
import net.schmizz.sshj.transport.kex.Curve25519SHA256;
import net.schmizz.sshj.common.Factory;
import java.io.*;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

/**
 * Uebertraegt Daten ueber SSH und die Netzwerkschnittstelle an den Drucker.
 * Verwendet SSHJ 0.32.0 fuer SSH-Verbindungen mit erweiterter Algorithm-Unterstuetzung.
 */
public class CVNetworkSSH extends CVInterface
{
    /**
     * Speichert die Einstellungen der Netzwerkschnittstelle.
     */
    private CVNetworkSettings lk_cNetworkSettingsSSH = null;

    /**
     * SSHJ SSH Client.
     */
    private SSHClient lk_cSSHClient = null;

    /**
     * Session for data transfer.
     */
    private Session lk_cSession = null;
    
    /**
     * Shell for I/O streams.
     */
    private Session.Shell lk_cShell = null;
    
    /**
     * InputStream vom SSH Channel.
     */
    private InputStream lk_cSSHInputStream = null;

    /**
     * OutputStream vom SSH Channel.
     */
    private OutputStream lk_cSSHOutputStream = null;

    private boolean bAutoReconnectRunning = false;

    /**
     * Konstruktor der Klasse CVNetworkSSH
     */
    public CVNetworkSSH(
            CVErrorMessage errorMessage,
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        super(errorMessage, errorFile, statusMessage, configFile);

        this.lk_cNetworkSettingsSSH = new CVNetworkSettings(
                                        this.lk_cErrorMessage,
                                        this.lk_cErrorFile,
                                        this.lk_cStatusMessage,
                                        this.lk_cConfigFile,
                                        CVNetworkProtocol.SSH);

        this.lk_cSSHClient = null;

        // Register BouncyCastle for full algorithm support
        try {
            Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Security.addProvider((java.security.Provider)
                Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").getConstructor().newInstance());
            if(this.lk_cErrorFile != null) {
                this.lk_cErrorFile.write("CVNetworkSSH: BouncyCastle registered for enhanced crypto");
            }
        } catch(Exception e) {
            if(this.lk_cErrorFile != null) {
                this.lk_cErrorFile.write("CVNetworkSSH: BouncyCastle not available, using JCE");
            }
        }

        return;
    }

    /**
     * Aufraeumen, bevor das Objekt geloescht wird.
     */
    public void finalize() throws Throwable
    {
        if(this.lk_bIsConnected == true)
        {
            if(this.close() != true) { }
        }

        super.finalize();

        return;
    }

    /**
     * Oeffnen des Interface.
     */
    public boolean open()
    {
        if(this.lk_bIsConnected == true)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkSSH->open: " +
                    "SSH network interface already open");
            }

            return false;
        }

        //----------------------------------------------------------------------
        // SSH-Verbindung herstellen mit SSHJ 0.32.0
        //----------------------------------------------------------------------
        try
        {
            String host = this.lk_cNetworkSettingsSSH.getIPAdress();
            int port = this.lk_cNetworkSettingsSSH.getPort();
            String username = this.lk_cNetworkSettingsSSH.getSSHUsername();
            String password = this.lk_cNetworkSettingsSSH.getSSHPassword();
            
            if(this.lk_cErrorFile != null) {
                this.lk_cErrorFile.write("CVNetworkSSH->open: Connecting to " + host + ":" + port + " as " + username);
            }
            System.out.println("SSHJ: Connecting to " + host + ":" + port + " as " + username);

            // Create config with modern key exchange algorithms
            Config config = new DefaultConfig() {
                {
                    List<Factory.Named<KeyExchange>> kex = new ArrayList<>();
                    // Curve25519 for modern SSH servers
                    kex.add(new Curve25519SHA256.Factory());
                    kex.add(new Curve25519SHA256.FactoryLibSsh());
                    // Diffie-Hellman groups for compatibility
                    kex.add(com.hierynomus.sshj.transport.kex.DHGroups.Group14SHA256());
                    kex.add(com.hierynomus.sshj.transport.kex.DHGroups.Group14SHA1());
                    kex.add(com.hierynomus.sshj.transport.kex.DHGroups.Group1SHA1());
                    kex.add(new net.schmizz.sshj.transport.kex.DHGexSHA1.Factory());
                    setKeyExchangeFactories(kex);
                }
            };

            // SSHJ SSH Client erstellen
            this.lk_cSSHClient = new SSHClient(config);
            
            // Host key verification
            this.lk_cSSHClient.addHostKeyVerifier(new HostKeyVerifier() {
                public boolean verify(String host, int port, PublicKey key) {
                    if(lk_cErrorFile != null) {
                        lk_cErrorFile.write("CVNetworkSSH->open: Host key received: " + key.getAlgorithm());
                    }
                    return true;
                }
                public List<String> findExistingAlgorithms(String host, int port) {
                    return new ArrayList<String>();
                }
            });

            // Verbinden
            this.lk_cSSHClient.connect(host, port);
            
            // Authentifizierung
            if(password != null && !password.isEmpty()) {
                this.lk_cSSHClient.authPassword(username, password);
                if(this.lk_cErrorFile != null) {
                    this.lk_cErrorFile.write("CVNetworkSSH->open: Authenticated with password");
                }
            } else {
                // Key-based auth versuchen
                try {
                    this.lk_cSSHClient.authPublickey(username);
                    if(this.lk_cErrorFile != null) {
                        this.lk_cErrorFile.write("CVNetworkSSH->open: Authenticated with key");
                    }
                } catch(UserAuthException e) {
                    if(this.lk_cErrorFile != null) {
                        this.lk_cErrorFile.write("CVNetworkSSH->open: Key auth failed: " + e.getMessage());
                    }
                    throw e;
                }
            }
            
            if(this.lk_cErrorFile != null) {
                this.lk_cErrorFile.write("CVNetworkSSH->open: SSHJ connected and authenticated");
            }
            System.out.println("SSHJ: Connected and authenticated");
            
            // Session starten und Shell oeffnen
            this.lk_cSession = this.lk_cSSHClient.startSession();
            this.lk_cSession.allocateDefaultPTY();
            this.lk_cShell = this.lk_cSession.startShell();
            
            // Input/Output Streams vom Shell
            this.lk_cSSHInputStream = this.lk_cShell.getInputStream();
            this.lk_cSSHOutputStream = this.lk_cShell.getOutputStream();
            
            if(this.lk_cErrorFile != null) {
                this.lk_cErrorFile.write("CVNetworkSSH->open: Shell started");
            }
            System.out.println("SSHJ: Shell started");
        }
        catch(TransportException ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkSSH->open: TransportException: " + ex.getMessage());
            }
            System.err.println("SSHJ TransportException: " + ex.getMessage());
            this.lk_cStatusMessage.write("CVNetworkSSH: SSH connection failed");
            if(this.lk_cSSHClient != null) {
                try { this.lk_cSSHClient.disconnect(); } catch(Exception e) {}
            }
            return false;
        }
        catch(UserAuthException ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkSSH->open: UserAuthException: " + ex.getMessage());
            }
            System.err.println("SSHJ UserAuthException: " + ex.getMessage());
            this.lk_cStatusMessage.write("CVNetworkSSH: SSH authentication failed");
            if(this.lk_cSSHClient != null) {
                try { this.lk_cSSHClient.disconnect(); } catch(Exception e) {}
            }
            return false;
        }
        catch(java.io.IOException ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkSSH->open: IOException: " + ex.getMessage());
            }
            System.err.println("SSHJ IOException: " + ex.getMessage());
            this.lk_cStatusMessage.write("CVNetworkSSH: network port not open");
            if(this.lk_cSSHClient != null) {
                try { this.lk_cSSHClient.disconnect(); } catch(Exception e) {}
            }
            return false;
        }

        //----------------------------------------------------------------------
        // Ein- / Ausgabestroeme oeffnen
        //----------------------------------------------------------------------
        try
        {
            this.lk_cInputStreamBinary = this.lk_cSSHInputStream;
            this.lk_cInputStreamReader = new InputStreamReader(
                this.lk_cInputStreamBinary, "US-ASCII");

            this.lk_cOutputStreamBinary = this.lk_cSSHOutputStream;
            this.lk_cOutputStreamWriter = new OutputStreamWriter(
                this.lk_cOutputStreamBinary, "US-ASCII");
            this.lk_cOutputStreamWriter.flush();
        }
        catch(UnsupportedEncodingException ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkSSH->open: " +
                    "UnsupportedEncoding Exception: wrong encoding network - " +
                    ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkSSH: network port not open");

            return false;
        }
        catch(Exception ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                    "CVNetworkSSH->open: Exception: " +
                    "wrong streams network - " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkSSH: network port not open");

            return false;
        }

        this.lk_cStatusMessage.write("CVNetworkSSH: SSH connection established");

        this.lk_bIsConnected = true;

        return true;
    }

    /**
     * Schliessen des Interface.
     */
    public boolean close()
    {
        // Even if already disconnected (due to I/O error), close resources
        //----------------------------------------------------------------------
        // SSH-Verbindung schliessen
        //----------------------------------------------------------------------
        try
        {
            // Close output writer first
            if(this.lk_cOutputStreamWriter != null)
            {
                try {
                    this.lk_cOutputStreamWriter.flush();
                } catch(Exception e) {}
                try {
                    this.lk_cOutputStreamWriter.close();
                } catch(Exception e) {}
                this.lk_cOutputStreamWriter = null;
            }
            if(this.lk_cOutputStreamBinary != null)
            {
                try {
                    this.lk_cOutputStreamBinary.flush();
                } catch(Exception e) {}
                try {
                    this.lk_cOutputStreamBinary.close();
                } catch(Exception e) {}
                this.lk_cOutputStreamBinary = null;
            }
            if(this.lk_cInputStreamReader != null)
            {
                try {
                    this.lk_cInputStreamReader.close();
                } catch(Exception e) {}
                this.lk_cInputStreamReader = null;
            }
            if(this.lk_cInputStreamBinary != null)
            {
                try {
                    this.lk_cInputStreamBinary.close();
                } catch(Exception e) {}
                this.lk_cInputStreamBinary = null;
            }

            // Close SSH shell
            if(this.lk_cShell != null)
            {
                try {
                    this.lk_cShell.close();
                } catch(Exception e) {}
                this.lk_cShell = null;
            }
            
            // Close SSH session
            if(this.lk_cSession != null)
            {
                try {
                    this.lk_cSession.close();
                } catch(Exception e) {}
                this.lk_cSession = null;
            }
            
            // Disconnect SSH client
            if(this.lk_cSSHClient != null)
            {
                try {
                    this.lk_cSSHClient.disconnect();
                } catch(Exception e) {}
                this.lk_cSSHClient = null;
            }
        }
        catch(Exception ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorMessage.write("CVNetworkSSH->close: " +
                    "Exception: could not close streams or SSH session - " +
                    ex.getMessage());
            }
            this.lk_cStatusMessage.write(
                    "CVNetworkSSH: network port not closed");

            return false;
        }

        this.lk_cStatusMessage.write("CVNetworkSSH: SSH network interface closed");

        this.lk_bIsConnected = false;

        return true;
    }

    /**
     * Pruefen ob Interface geoeffnet ist.
     */
    public boolean isConnected()
    {
        return this.lk_bIsConnected;
    }

    /**
     * Automatische Wiederherstellung der Verbindung.
     */
    public void setAutoReconnect(boolean active)
    {
        this.bAutoReconnectRunning = active;
    }

    /**
     * Gibt den Namen der Netzwerkschnittstelle zurueck.
     */
    public String getInterfaceName()
    {
        return "CVNetworkSSH";
    }

    /**
     * Gibt die IP-Adresse zurueck.
     */
    public String getIPAddress()
    {
        return this.lk_cNetworkSettingsSSH.getIPAdress();
    }

    /**
     * Gibt den Port zurueck.
     */
    public String getPort()
    {
        return Integer.toString(this.lk_cNetworkSettingsSSH.getPort());
    }

    /**
     * Setzt die Konfiguration der Netzwerkschnittstelle.
     */
    public void setInterfaceSettings(Object cSettings)
    {
        if(cSettings != null) {
            this.lk_cNetworkSettingsSSH = (CVNetworkSettings) cSettings;
        }
        return;
    }

    /**
     * Setzt die Konfiguration der Netzwerkschnittstelle.
     */
    public void setInterfaceSettings(Object cSettings, Object networkProtocol)
    {
        if(cSettings != null) {
            this.lk_cNetworkSettingsSSH = (CVNetworkSettings) cSettings;
        }
        return;
    }

    /**
     * Gibt die Netzwerkschnittstelle zurueck.
     */
    public CVInterface getInterface()
    {
        return this;
    }

    /**
     * Gibt die Einstellungen der Netzwerkschnittstelle zurueck.
     */
    public Object getInterfaceSettings()
    {
        return this.lk_cNetworkSettingsSSH;
    }

    /**
     * Gibt die Einstellungen der Netzwerkschnittstelle zurueck.
     */
    public CVNetworkSettings getNetworkSettings()
    {
        return this.lk_cNetworkSettingsSSH;
    }
}
