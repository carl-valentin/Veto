package de.carlvalentin.Interface;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.ValentinConsole.ValentinConsole;

import com.jcraft.jsch.*;

import java.io.*;

/**
 * Uebertraegt Daten ueber SSH und die Netzwerkschnittstelle an den Drucker.
 */
public class CVNetworkSSH extends CVInterface
{
    /**
     * Speichert die Einstellungen der Netzwerkschnittstelle.
     */
    private CVNetworkSettings lk_cNetworkSettingsSSH = null;

    /**
     * SSH Session.
     */
    private Session lk_cSSHSession = null;

    /**
     * SSH Channel fuer den Datentransport.
     */
    private Channel lk_cSSHChannel = null;

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
     *
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in eine Datei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
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

        this.lk_cSSHSession = null;

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
                this.lk_cErrorMessage.write("CVNetworkSSH->open: " +
                    "SSH network interface already open");
            }

            return false;
        }

        //----------------------------------------------------------------------
        // SSH-Verbindung herstellen
        //----------------------------------------------------------------------
        try
        {
            // Debug-Logging fuer JSch aktivieren
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                public boolean isEnabled(int level) { return true; }
                public void log(int level, String message) {
                    String prefix = "";
                    if(level == DEBUG) prefix = "[JSch DEBUG] ";
                    else if(level == INFO) prefix = "[JSch INFO] ";
                    else if(level == WARN) prefix = "[JSch WARN] ";
                    else if(level == ERROR) prefix = "[JSch ERROR] ";
                    
                    if(lk_cErrorFile != null) {
                        lk_cErrorFile.write(prefix + message);
                    }
                    System.out.println(prefix + message);
                }
            });
            
            JSch jsch = new JSch();

            // SSH-Session erstellen
            this.lk_cSSHSession = jsch.getSession(
                this.lk_cNetworkSettingsSSH.getSSHUsername(),
                this.lk_cNetworkSettingsSSH.getIPAdress(),
                this.lk_cNetworkSettingsSSH.getPort());

            // Passwort setzen wenn vorhanden
            String password = this.lk_cNetworkSettingsSSH.getSSHPassword();
            if (password != null && !password.isEmpty())
            {
                this.lk_cSSHSession.setPassword(password);
            }

            // SSH-Eigenschaften setzen
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password,publickey");
            this.lk_cSSHSession.setConfig(config);

            // Verbinden
            this.lk_cSSHSession.connect(30000);

            // Port-Forwarding einrichten fuer die Verbindung zum Drucker
            // Wir leiten Local-Port -> Remote-Host:RemotePort
            int localPort = 0; // 0 = automatisch vergeben
            String remoteHost = "127.0.0.1"; // Host auf der anderen Seite der SSH-Verbindung
            int remotePort = this.lk_cNetworkSettingsSSH.getPort();

            // Forwarded Port oeffnen
            this.lk_cSSHSession.setPortForwardingL(localPort, remoteHost, remotePort);

            // Shell-Kanal oeffnen fuer Datentransport
            this.lk_cSSHChannel = this.lk_cSSHSession.openChannel("shell");
            this.lk_cSSHChannel.setInputStream(System.in, true);
            this.lk_cSSHChannel.setOutputStream(System.out, true);

            this.lk_cSSHInputStream = this.lk_cSSHChannel.getInputStream();
            this.lk_cSSHOutputStream = this.lk_cSSHChannel.getOutputStream();

            this.lk_cSSHChannel.connect(10000);
        }
        catch(JSchException ex)
        {
            // Detaillierte Fehlerinformationen sammeln
            String serverVersion = "unknown";
            String clientVersion = "JSCH 2.28.0";
            if(this.lk_cSSHSession != null) {
                try { serverVersion = this.lk_cSSHSession.getServerVersion(); } catch(Exception e) {}
            }
            
            StringBuilder details = new StringBuilder();
            details.append("CVNetworkSSH->open: JSchException: ").append(ex.getMessage()).append("\n");
            details.append("  Server: ").append(this.lk_cNetworkSettingsSSH.getIPAdress())
                  .append(":").append(this.lk_cNetworkSettingsSSH.getPort()).append("\n");
            details.append("  Server Version: ").append(serverVersion).append("\n");
            details.append("  Client Version: ").append(clientVersion).append("\n");
            details.append("  User: ").append(this.lk_cNetworkSettingsSSH.getSSHUsername()).append("\n");
            
            if(ex.getCause() != null) {
                details.append("  Cause: ").append(ex.getCause().getMessage()).append("\n");
            }
            
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(details.toString());
            }
            System.err.println(details.toString());
            this.lk_cStatusMessage.write("CVNetworkSSH: SSH connection failed");

            return false;
        }
        catch(Exception ex)
        {
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVNetworkSSH->open: " +
                    "Exception: " + ex.getMessage());
            }
            this.lk_cStatusMessage.write("CVNetworkSSH: network port not open");

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
     *
     * @return true, wenn Interface geschlossen werden konnte.
     */
    public boolean close()
    {
        if(this.lk_bIsConnected == false)
        {
            if(this.lk_cErrorMessage != null)
            {
                this.lk_cErrorMessage.write("CVNetworkSSH->close: " +
                    "SSH network interface not open");
            }

            return false;
        }

        //----------------------------------------------------------------------
        // SSH-Verbindung schliessen
        //----------------------------------------------------------------------
        try
        {
            if(this.lk_cOutputStreamWriter != null)
            {
                this.lk_cOutputStreamWriter.flush();
                this.lk_cOutputStreamWriter.close();
                this.lk_cOutputStreamWriter = null;
            }
            if(this.lk_cOutputStreamBinary != null)
            {
                this.lk_cOutputStreamBinary.flush();
                this.lk_cOutputStreamBinary.close();
                this.lk_cOutputStreamBinary = null;
            }
            if(this.lk_cInputStreamReader != null)
            {
                this.lk_cInputStreamReader.close();
                this.lk_cInputStreamReader = null;
            }
            if(this.lk_cInputStreamBinary != null)
            {
                this.lk_cInputStreamBinary.close();
                this.lk_cInputStreamBinary = null;
            }

            if(this.lk_cSSHChannel != null)
            {
                this.lk_cSSHChannel.disconnect();
                this.lk_cSSHChannel = null;
            }

            if(this.lk_cSSHSession != null)
            {
                this.lk_cSSHSession.disconnect();
                this.lk_cSSHSession = null;
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

        this.lk_cStatusMessage.write("CVNetworkSSH: SSH connection closed");

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
        return (Object) this.lk_cNetworkSettingsSSH;
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
            this.lk_cNetworkSettingsSSH = (CVNetworkSettings) cSettings;
        }

        return;
    }

    public void doAutoReconnect()
    {
        // SSH Auto-Reconnect - optional implementieren
        Thread thread = new Thread(){
            public void run() {
                boolean bSuccess = false;
                int i = 1;
                bAutoReconnectRunning = true;
                while (!bSuccess && bAutoReconnectRunning) {
                    try {
                        sleep(10000);
                        // Versuche neu zu verbinden
                        // (vereinfacht - in echter Implementierung waere
                        // ein echter Reconnect-Versuch notwendig)
                        if (lk_cSSHSession == null || !lk_cSSHSession.isConnected()) {
                            // Hier koennte man eine neue Verbindung aufbauen
                            lk_cStatusMessage.write("CVNetworkSSH: AutoReconnect attempt " + i++);
                        } else {
                            bSuccess = true;
                            lk_cStatusMessage.write("CVNetworkSSH: AutoReconnect succeed");
                            ValentinConsole.connect();
                        }
                    }
                    catch(Exception ex) {
                        lk_cStatusMessage.write("CVNetworkSSH: AutoReconnect attempt " + i++);
                    }
                }
                if (!bSuccess) {
                    lk_cStatusMessage.write("CVNetworkSSH: AutoReconnect failed");
                }
                bAutoReconnectRunning = false;
            }
        };

        thread.start();
    }

    public void stopAutoReconnect()
    {
        bAutoReconnectRunning = false;
    }
}