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
public class CVPLsendThread extends Thread
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
    public CVPLsendThread(Writer writerStatus) 
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
            System.err.println("CVPLsendThread: I/O Exception writeStatus: " 
                    + ex.getMessage());
            this.writeError(   "CVPLsendThread: I/O Exception writeStatus: " 
                    + ex.getMessage());
        }
        catch (HeadlessException ex) 
        {
            System.err.println("CVPLsendThread: Headless Exception " +
                    "writeStatus: " + ex.getMessage());
            this.writeError(   "CVPLsendThread: Headless Exception " +
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
            JOptionPane.showMessageDialog(null, s, "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) 
        {
            System.err.println("CVPLsendThread: I/O Excexption writeError: " 
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
     * Zeilenumbruch erkannt wird. Die eingelesenen Daten werden mit den 
     * gewählten Start- und Stopzeichen der CVPL versehen und dann an
     * den Writer weitergesendet.
     */
    public void run()
    {
        char c;
        while(true)
        {
            if((this.lk_InputReader != null)&&(this.lk_OuputWriter != null))
            {
                if(this.lk_sSohEtb.toString().equals(SohEtb.none.toString()))
                {
                    //keine Start-/Stopzeichen einfügen
                    try
                    {
                        this.lk_OuputWriter.write(
                                this.lk_InputReader.read());
                    }
                    catch(IOException ex)
                    {
                        System.err.println("CVPLsendThread: I/O Exception " +
                                "run: " + ex.getMessage());
                        this.writeError(   "CVPLsendThread: I/O Exception " +
                                "run: " + ex.getMessage());
                    }
                }
                else
                {
                    // Start-/Stopzeichen einfügen
                    try
                    {
                        c = (char)this.lk_InputReader.read();
                        switch(c)
                        {
                        case '\n':
                            // Start- und Stopzeichen der CVPL hinzufuegen
                            this.lk_OuputWriter.write(
                                this.lk_sSohEtb.gl_strSOH + 
                                this.lk_szCurrentLine +
                                this.lk_sSohEtb.gl_strETB,
                                0, this.lk_szCurrentLine.length() + 2);
                            this.lk_OuputWriter.flush();
                            this.lk_szCurrentLine = "";
                            break;
                        case '\b':
                            this.lk_szCurrentLine = 
                                this.lk_szCurrentLine.substring(
                                        0, this.lk_szCurrentLine.length()-1); 
                            break;
                        default:
                            this.lk_szCurrentLine += c;
                            break;
                        }
                    }
                    catch(IOException ex)
                    {
                        System.err.println("CVPLsendThread: I/O Exception " +
                                "run: " + ex.getMessage());
                        this.writeError(   "CVPLsendThread: I/O Exception " +
                                "run: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
