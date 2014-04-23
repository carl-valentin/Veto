package de.carlvalentin.Scripting;

import de.carlvalentin.Common.*;
import de.carlvalentin.Common.Printf.*;
import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Protocol.*;
import de.carlvalentin.Interface.*;
import de.carlvalentin.ValentinConsole.ValentinConsole;

import bsh.Interpreter;
import bsh.EvalError;

import java.io.*;
import javax.swing.*;


/**
 * @author Andreas Frieser
 *
 * Klasse zur Integration der Skriptumgebung BeanShell in eigene Programme.
 */
public class CVBeanShell
{
    private ValentinConsole    lk_valentinConsole = null;
    
	/**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage     lk_cErrorMessage = null;
    
    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging          lk_cErrorFile = null;
    
    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine       lk_cStatusMessage = null;
    
    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    private CVConfigFile       lk_cConfigFile = null;
    
    /**
     * Interpreter fuer die verwendete Skriptsprache BeanShell
     */
    private Interpreter        lk_cBeanShellInterpreter = null;
    
    /**
     * Writer fuer externen Zugriff - Daten vom Interface lesen
     */
    private PipedWriter        lk_cPipedWriterExtern = null;
    /**
     * Interner Reader zum Lesen aus Writer fuer externen Zugriff
     */
    private BufferedReader     lk_cBufferedReaderInput = null;
    
    /**
     * Reader fuer externen Zugriff - Daten in Interface schreiben
     */
    private PipedReader        lk_cPipedReaderExtern = null;
    /**
     * Interner Writer zum Schreiben in Reader fuer externen Zugriff
     */
    private BufferedWriter     lk_cBufferedWriterOutput = null;
    
    /**
     * Internes TCP-Netzwerkinterface des Skriptinterpreters
     */
    private CVNetworkTCP       lk_cBeanShellInterfaceNetworkTCP = null;
    
    /**
     * Internes UDP-Netzwerkinterface des Skriptinterpreters
     */
    private CVNetworkUDP       lk_cBeanShellInterfaceNetworkUDP = null;
    
    /**
     * Internes Interface des Skriptinterpreters zur seriellen Schnittstelle 
     */
    private CVSerial           lk_cBeanShellInterfaceSerial = null;
    
    /**
     * Internes Interface des Skriptinterpreters zur parallelen Schnittstelle
     */
    private CVParallel         lk_cBeanShellInterfaceParallel = null;
    
    /**
     * Printf im C-Stil zur Formatierung von Textausgaben
     */
    private CVPrintf           lk_cPrintf = null;
    
    /**
     * Textausgabe des Skriptinterpreters
     */
    private JTextArea          lk_cDisplayTextOutput = null;
    
    /**
     * Textfeld zum Anzeigen der gesendeten Daten im ASCII-Format
     */
    private CVTextDisplay       lk_cDisplayInterfaceASCII = null;
    
    /**
     * Textfeld zum Anzeigen der gesendeten Daten im HEX-Format
     */
    private CVTextDisplay       lk_cDisplayInterfaceHEX = null;
    
    /**
     * Zeigt an, ob Skriptinterpreter ein Skript ausführt
     */
    private boolean            lk_bScriptIsRunning = false;
    
    private boolean            lk_bStopScript = false;
    
    private String lk_sScript = null;    
    
    private Thread lk_tScript;
    
    /**
     * Konstruktor der Klasse CVBeanShell
     * 
     * @param errorMessage Ausgabe von Fehlermeldungen als Dialog.
     * @param errorFile Ausgabe von Fehlermeldungen in Logdatei.
     * @param statusMessage Ausgabe von Statusmeldungen auf Statuszeile.
     * @param configFile Einlesen und Schreiben Konfigurationsdatei.
     */
    public CVBeanShell(
            ValentinConsole valentinConsole,
            CVErrorMessage errorMessage, 
            CVLogging      errorFile,
            CVStatusLine   statusMessage,
            CVConfigFile   configFile)
    {
        this.lk_valentinConsole = valentinConsole; 
        this.lk_cErrorMessage   = errorMessage;
        this.lk_cErrorFile      = errorFile;
        this.lk_cStatusMessage  = statusMessage;
        this.lk_cConfigFile     = configFile;
     
        this.initCVBeanShell();
        
        return;
    }
    
    /**
     * Aufruf durch Gabage Collector
     */
    protected void finalize() throws Throwable
    {
        if(this.lk_cBufferedReaderInput != null)
        {
        	this.lk_cBufferedReaderInput.close();
            this.lk_cBufferedReaderInput = null;
        }
        if(this.lk_cBufferedWriterOutput != null)
        {
        	this.lk_cBufferedWriterOutput.close();
            this.lk_cBufferedWriterOutput = null;
        }
        if(this.lk_cPipedReaderExtern != null)
        {
        	this.lk_cPipedReaderExtern.close();
            this.lk_cPipedReaderExtern = null;
        }
        if(this.lk_cPipedWriterExtern != null)
        {
        	this.lk_cPipedWriterExtern.close();
            this.lk_cPipedWriterExtern = null;
        }
             
        return;
    }
    
    private Thread createInterpreterThread()
    {
        return new Thread( new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    if (lk_sScript == null)
                    {
                        try {
                            java.lang.Thread.sleep(500);
                        } catch (InterruptedException e) { }
                    }
                    else
                    {                        
                        try
                        {
                            if (!lk_bScriptIsRunning)
                            {
                                lk_bScriptIsRunning = true;

                                lk_cStatusMessage.write("" +
                                        "Script is running");
                                lk_bStopScript = false;
                                lk_cBeanShellInterpreter.eval("bStopScript = false");
                                lk_cBeanShellInterpreter.eval(lk_sScript);
                                lk_cStatusMessage.write("Script ended");
                                
                                lk_sScript = null;
                                lk_bScriptIsRunning = false;
                            }
                        }
                        catch(EvalError err)
                        {
                            try
                            {                            
                                if(lk_cErrorMessage != null)
                                {
                                    lk_cErrorMessage.write("CVBeanShell->runScript: " +
                                           "EvalError executing scriptfile: \n " + 
                                           err.getMessage());
                                }
                                if(lk_cErrorFile != null)
                                {
                                    lk_cErrorFile.write("CVBeanShell->runScript: " +
                                        "EvalError executing scriptfile: " + 
                                        err.getMessage());
                                }

                                if(lk_cDisplayTextOutput != null)
                                {
                                    lk_cDisplayTextOutput.append("\n<--------------->\n" +
                                            "CVBeanShell->runScript: " +
                                            "EvalError executing scriptfile: \n " + 
                                            err.getMessage() + "\n" +
                                            "File: " + err.getErrorSourceFile() +
                                            " - Line: " + err.getErrorLineNumber() + "\n" +
                                            "Error: " + err.getErrorText() + "\n" +
                                            "Stack: " + err.getScriptStackTrace() + "\n" +
                                            "\n<--------------->\n");
                                }
                            }
                            catch (NullPointerException ex)
                            {
                            }
                            
                            if (lk_bScriptIsRunning)
                            {
                                lk_cStatusMessage.write("Script ended");
                                
                                lk_sScript = null;
                                lk_bScriptIsRunning = false;
                            }                            
                        }
                    }
                }
            }
        });       
    }
    
    /**
     * Initialisere Klassenkomponenten und -strukturen
     */
    private void initCVBeanShell()
    {
        this.lk_cBeanShellInterpreter = new Interpreter();
        
        this.lk_cPrintf = new CVPrintf();
        
        //----------------------------------------------------------------------
        // Anlegen Reader fuer externen Zugriff - Daten in Interface schreiben
        //----------------------------------------------------------------------
        // interner Zugriff -> BufferedWriter
        //                                  \
        //                                  PipedReader -> externer Zugriff
        try
        {
        	this.lk_cPipedReaderExtern = new PipedReader();
            this.lk_cBufferedWriterOutput = new BufferedWriter(
                    new PipedWriter(this.lk_cPipedReaderExtern));
        }
        catch(IOException ex)
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create output piped writer");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create output writer: " + 
                        ex.getMessage());
            }
        }
        
        //----------------------------------------------------------------------
        // Anlegen Writer fuer externen Zugriff - Daten vom Interface lesen
        //----------------------------------------------------------------------
        // externer Zugriff -> PipedWriter
        //                               \
        //                               BufferedReader -> interner Zugriff
        try
        {
            this.lk_cPipedWriterExtern = new PipedWriter();
        	this.lk_cBufferedReaderInput = new BufferedReader(
                new PipedReader(this.lk_cPipedWriterExtern));
        }
        catch(IOException ex)
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create input buffered reader");
            }
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("CVBeanShell->initCVBeanShell: " +
                        "IOException create input buffered reader: " + 
                        ex.getMessage());
            }
        }
        
        //----------------------------------------------------------------------
        // Konfiguration des Skriptinterpreters
        //----------------------------------------------------------------------
        try
        {
        	//------------------------------------------------------------------
            // externe Kommandoskripts einbinden
        	//------------------------------------------------------------------
            this.lk_cBeanShellInterpreter.eval(
                    "addClassPath(\".\");");
            this.lk_cBeanShellInterpreter.eval(
                    "import de.carlvalentin.Interface.*;");
        	this.lk_cBeanShellInterpreter.eval(
                      "importCommands(\"/bshscripts\");");                    
        	/*this.lk_cBeanShellInterpreter.eval(
            	"addClassPath(\"D:\\Eclipse\\de.carlvalentin.java.veto\\de\\carlvalentin\\\");");
        	this.lk_cBeanShellInterpreter.eval(
            	"importCommands(\"/Scripting\");");*/
            this.lk_cBeanShellInterpreter.eval("" +
                    "bsh.system.shutdownOnExit = false;");
            
        	//------------------------------------------------------------------
            // Reader und Writer fuer Zugriff auf Interface weitergeben
        	//------------------------------------------------------------------
            this.lk_cBeanShellInterpreter.set(
                    "cInterfaceReader", this.lk_cBufferedReaderInput);
            this.lk_cBeanShellInterpreter.set(
                    "cInterfaceWriter", this.lk_cBufferedWriterOutput);
            this.lk_cBeanShellInterpreter.set(
                    "cExternReader", this.lk_cPipedReaderExtern);
            this.lk_cBeanShellInterpreter.set(
                    "cExternWriter", this.lk_cPipedWriterExtern);
            
            //------------------------------------------------------------------
            // Ausgabe von Fehlermeldungen
            //------------------------------------------------------------------
            this.lk_cBeanShellInterpreter.set(
                    "cErrorMessage",  this.lk_cErrorMessage);
            this.lk_cBeanShellInterpreter.set(
                    "cErrorFile",     this.lk_cErrorFile);
            this.lk_cBeanShellInterpreter.set(
                    "cStatusMessage", this.lk_cStatusMessage);
            
            //------------------------------------------------------------------
            // Printf
            //------------------------------------------------------------------
            this.lk_cBeanShellInterpreter.set("cPrintf", this.lk_cPrintf);
            
            //------------------------------------------------------------------
            // Sontiges
            //------------------------------------------------------------------            
            this.lk_cBeanShellInterpreter.set("bStopScript", this.lk_bStopScript);
            this.lk_cBeanShellInterpreter.set("ScriptPath", 
                                              System.getProperty("user.dir"));
        }
        catch(EvalError err)
        {
            if(this.lk_cErrorMessage != null)
            {
            	this.lk_cErrorMessage.write("CVBeanShell->initCVBeanShell: " +
                        "EvalError initializing scripting engine");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVBeanShell->initCVBeanShell: " +
                        "EvalError initializing scripting engine: " +
                        err.getMessage());
            }
        }

        lk_tScript = createInterpreterThread();
        
        lk_tScript.start();
        
    	return;
    }
    
    /**
     * Fuegt eine JTextArea unter dem Namen cTextArea in den Interpreter ein.
     * 
     * @param cTextArea Einzufuegende JTextArea
     */
    public void setDisplayScriptOutput(JTextArea cTextArea)
    {
        try
        {
        	this.lk_cBeanShellInterpreter.set("cTextArea", cTextArea);
        }
        catch(EvalError err) 
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVBeanShell->setScriptOutput: " +
                        "EvalError setting output text area");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write("CVBeanShell->setScriptOutput: " +
                        "EvalError setting output text area: " + 
                        err.getMessage());
            }
        }
        
        this.lk_cDisplayTextOutput = cTextArea;
        
        return;
    }
    
    /**
     * Fuegt das Display zum Darstellen der transportierten Daten im 
     * ASCII-Format in den Skriptinterpreter ein.
     * 
     * @param cDisplay Display zum Anzeigen der ASCII-Daten
     */
    public void setDisplayInterfaceASCII(CVTextDisplay cDisplay)
    {
    	try
    	{
    		this.lk_cBeanShellInterpreter.set(
    				"cDisplayInterfaceASCII", cDisplay);
    	}
    	catch(EvalError err)
    	{
    		if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write(
        				"CVBeanShell->setDisplayInterfaceASCII: " +
                        "EvalError setting interface display");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                		"CVBeanShell->setDisplayInterfaceASCII: " +
                        "EvalError setting interface display: " + 
                        err.getMessage());
            }
    	}
    	
    	this.lk_cDisplayInterfaceASCII = cDisplay;
    	
    	return;
    }
    
    /**
     * Fuegt das Dsiplay zum Darstellen der transportierten Daten im
     * HEX-Format in den Skriptinterpreter ein.
     * 
     * @param cDisplay Display zum Anzeigen der HEX-Daten
     */
    public void setDisplayInterfaceHEX(CVTextDisplay cDisplay)
    {
    	try
    	{
    		this.lk_cBeanShellInterpreter.set(
    				"cDisplayInterfaceHEX", cDisplay);
    	}
    	catch(EvalError err)
    	{
    		if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write(
        				"CVBeanShell->setDisplayInterfaceHEX: " +
                        "EvalError setting interface display");
            }
            if(this.lk_cErrorFile != null)
            {
                this.lk_cErrorFile.write(
                		"CVBeanShell->setDisplayInterfaceHEX: " +
                        "EvalError setting interface display: " + 
                        err.getMessage());
            }
    	}
    	
    	this.lk_cDisplayInterfaceHEX = cDisplay;
    	
    	return;
    }
    
    /**
     * Fuegt Schnittstelleninterfaces in den Skriptinterpreter ein
     * 
     * @param cConnectionManager verwaltet Verbindungen zum Drucker
     */
    public void setScriptInterfaces(CVConnectionManager cConnectionManager)
    {
        try
        {            
            this.lk_cBeanShellInterpreter.set
                ("ValentinConsole", this.lk_valentinConsole);
        	this.lk_cBeanShellInterpreter.set
               	("NetworkInterfaceTCP", this.lk_cBeanShellInterfaceNetworkTCP);
        	this.lk_cBeanShellInterpreter.set
        		("NetworkInterfaceUDP", this.lk_cBeanShellInterfaceNetworkUDP);
            this.lk_cBeanShellInterpreter.set
               	("ParallelInterface", this.lk_cBeanShellInterfaceParallel);
            this.lk_cBeanShellInterpreter.set
               	("SerialInterface", this.lk_cBeanShellInterfaceSerial);
            this.lk_cBeanShellInterpreter.set
               	("cConnectionManager", cConnectionManager);
        }
        catch(EvalError err)
        {
        	if(this.lk_cErrorMessage != null)
            {
        		this.lk_cErrorMessage.write("CVBeanShell->setScriptInterface:" +
        				" EvalError setting interfaces");
            }
            if(this.lk_cErrorFile != null)
            {
            	this.lk_cErrorFile.write("CVBeanShell->setScriptInterface: " +
                        "EvalError setting interfaces: " + err.getMessage());
            }
            if(this.lk_cDisplayTextOutput != null)
            {
            	this.lk_cDisplayTextOutput.append(
            			"CVBeanShell->setScriptInterface: " +
                        "EvalError setting interfaces: " + err.getMessage());
            }
        }
        
    	return;
    }

    /**
     * SkriptPfad fuer Benutzung im Skript setzen.
     */
    public void setScriptPath(String sScriptPath)
    {
        try
        {
        	this.lk_cBeanShellInterpreter.set("ScriptPath", sScriptPath);
        }
        catch(EvalError err)
        {               
        }
    }    
    
    /**
     * Skript im Interpreter ausfuehren.
     * 
     * @param sScript auszufuehrendes Skript
     */
    public void runScript(final String sScript)
    {
        if (!this.lk_bScriptIsRunning)
        {
        	lk_sScript = sScript;    
        }
    	return;
    }
    
    /**
     * Stoppe laufende Ausführung eines Skripts
     */
    public void stopScript()
    {
    	if(this.lk_bScriptIsRunning == true)
    	{               
            if(this.lk_bStopScript == true)
            {
                lk_tScript.stop();
                lk_cStatusMessage.write("Script ended");            
                lk_sScript = null;
                lk_bScriptIsRunning = false;
                lk_tScript = createInterpreterThread();
                lk_tScript.start();
            }
            else
            {
                try
                {
                    lk_bStopScript = true;
                    this.lk_cBeanShellInterpreter.eval("bStopScript = true");
                }
                catch(EvalError err)
                {            	
                }
            }
    	}
    	
    	return;
    }
    
    /**
     * Abfrage des Readers - Daten in Interface schreiben
     * 
     * @return Reader zum Lesen von Daten
     */
    public Reader getReader()
    {
    	return this.lk_cPipedReaderExtern;
    }
    
    /**
     * Abfrage des Writers - Daten vom Interface lesen
     * 
     * @return Writer zum Schreiben von Daten
     */
    public Writer getWriter()
    {
    	return this.lk_cPipedWriterExtern; 
    }
}
