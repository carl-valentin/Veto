package de.carlvalentin.ValentinConsole;

import java.awt.HeadlessException;
import java.io.*;

import javax.swing.JOptionPane;

/**
 * Klasse zum Senden von Daten gemäss der Konventionen der CVPL von einem
 * Reader zu einem Writer(Javaklassen). Der genaue Ursprung des Datenstroms
 * im Reader und das Ziel des Writer sind für die Bearbeitung der Daten
 * unerheblich. Die Klasse verpackt die Daten in die geforderten Start- und
 * Stopzeichen der CVPL.
 */
public class CVPLrecvThread extends Thread
{
    private Writer             lk_WriterStatus;
    
    private Writer             lk_OuputWriter;
    private Reader             lk_InputReader;
    
    private String             lk_szCurrentLine;
    
    private SohEtb             lk_sSohEtb;
    
    /**
     * Konstruktor der Klasse CVPLsendThread
     * 
     * @param writerStatus Ein Writer auf den Loggingausgaben gemacht werden
     *                     k&ouml;nnen.
     */
    public CVPLrecvThread(Writer writerStatus) 
    {
        this.lk_WriterStatus = writerStatus;
        
        this.lk_InputReader = null;
        this.lk_OuputWriter = null;
        
        this.lk_szCurrentLine = "";
    }
    
    /**
     * Gibt eine Statusmeldung in der Statuszeile des Programms aus.
     * 
     * @param s Inhalt der Statusmeldung
     */
    private void writeStatus(String s) 
    {
        try 
        {
            lk_WriterStatus.write(s);
        }
        catch (IOException ex) 
        {
            System.err.println("CVPLrecvThread: I/O Exception writeStatus: " 
                    + ex.getMessage());
            this.writeError(   "CVPLrecvThread: I/O Exception writeStatus: " 
                    + ex.getMessage());
        }
        catch (HeadlessException ex) 
        {
            System.err.println("CVPLrecvThread: Headless Exception " +
                    "writeStatus: " + ex.getMessage());
            this.writeError(   "CVPLrecvThread: Headless Exception " +
                    "writeStatus: " + ex.getMessage());
        }
    }
    
    /**
     * Gibt eine Fehlermeldung als Dialogbox aus.
     * 
     * @param s Inhalt der Fehlermeldung
     */
    private void writeError(String s) 
    {
        try 
        {
            this.lk_WriterStatus.write(s);
            JOptionPane.showMessageDialog(
                    null, s, "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) 
        {
            System.err.println("CVPLrecvThread: I/O Excexption writeError: " 
                    + ex.getMessage());
        }
    }
    
    /**
     * Festlegen des Eingabekanals.
     * 
     * @param input Reader, aus welchem die Daten ausgelesen werden.
     */
    public void setInputReader(Reader input)
    {
        this.lk_InputReader = input;
    }
    
    /**
     * Festlegen des Auagabekanals.
     * 
     * @param output Writer, auf welchen die Daten geschrieben werden sollen
     */
    public void setOutputWriter(Writer output)
    {
        this.lk_OuputWriter = output;
    }
    
    /**
     * Festlegen der Start- und Stopzeichen aus der CVPL
     * 
     * @param sohEtb Datenstruktur mit Start- und Stopzeichen
     */
    public void setSohEtb(SohEtb sohEtb)
    {
        this.lk_sSohEtb = sohEtb;
    }
    
    /**
     * Zentral Ausführungsfunktion des Thread.
     * 
     * <br>Es werden so lang Daten vom Reader eingelesen, bis ein 
     * Startzeichen der CVPL erkannt wird. Die folgenden Daten werden bis
     * zum Empfang des entsprechenden Stopzeichens zwischengespeichert. Nach
     * Erhalt des Stopzeichens werden die Daten in den Writer geschrieben.
     */
    public void run()
    {
        int iC;
        while(true)
        {
            if((this.lk_InputReader != null)&&(this.lk_OuputWriter != null))
            {
                if(this.lk_sSohEtb.toString().equals(SohEtb.none.toString()))
                {
                    // keine Start-/Stopzeichen verarbeiten
                    try
                    {
                        this.lk_OuputWriter.write(
                                this.lk_InputReader.read());
                    }
                    catch(IOException ex)
                    {
                        System.err.println("CVPLrecvThread: I/O Exception " +
                                "run: " + ex.getMessage());
                        this.writeError(   "CVPLrecvThread: I/O Exception " +
                                "run: " + ex.getMessage());
                    }
                }
                else
                {
                    // Start-/Stopzeichen verarbeiten
                    try
                    {
                        iC = 0;
                        this.lk_szCurrentLine = "";
                        // Auf Startzeichen der CVPL warten
                        do
                        {
                            iC = this.lk_InputReader.read();
                        }while(iC != this.lk_sSohEtb.gl_iSOH);
                        this.lk_szCurrentLine += (char)iC;
                        // Komplette Nachricht einlesen
                        do
                        {
                            iC = this.lk_InputReader.read();
                            this.lk_szCurrentLine += (char)iC;
                        }while(iC != this.lk_sSohEtb.gl_iETB);
                        this.lk_szCurrentLine += '\n';
                        this.lk_OuputWriter.write(this.lk_szCurrentLine);
                    }
                    catch(IOException ex)
                    {
                        System.err.println("CVPLrecvThread: I/O Exception " +
                                "run: " + ex.getMessage());
                        this.writeError(   "CVPLrecvThread: I/O Exception " +
                                "run: " + ex.getMessage());
                    }
                }
            }
        }
    }
}