package de.carlvalentin.Scripting.UI;

import de.carlvalentin.Common.UI.*;
import de.carlvalentin.Common.*;
import de.carlvalentin.Protocol.*;
import de.carlvalentin.Scripting.*;
import de.carlvalentin.ValentinConsole.ValentinConsole;

import java.awt.*;
import java.io.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * Grafische Oberfl&auml;che zur Steuerung der Skriptumgebung BeanShell.
 */
public class CVUIBeanShell extends JPanel
{
    private ValentinConsole     lk_valentinConsole = null;

    /**
     * Ausgabe von Fehlermeldungen in Dialogform.
     */
    private CVErrorMessage      lk_cErrorMessage = null;

    /**
     * Ausgabe von Fehlermeldungen in eine Logdatei
     */
    private CVLogging           lk_cErrorFile = null;

    /**
     * Ausgabe von Statusmeldungen auf Statuszeile
     */
    private CVStatusLine        lk_cStatusMessage = null;

    /**
     * Datei zum Speichern der akutellen Konfiguration
     */
    private CVConfigFile        lk_cConfigFile = null;

    /**
     * Verwaltet die Verbindungen zum Drucker
     */
    private CVConnectionManager lk_cConnectionManager = null;

    /**
     * Skriptverarbeitung
     */
    private CVBeanShell         lk_cBeanShell = null;

    /**
     * Textfeld zum Anzeigen und Modifizieren von Skriptdateien
     */
    private CVUIBeanShellEditor lk_cBeanShellEditor = null;

    /**
     * Textfeld zum Anzeigen der gesendeten Daten im ASCII-Format
     */
    private CVTextDisplay       lk_cDisplayInterfaceASCII = null;

    /**
     * Textfeld zum Anzeigen der gesendeten Daten im HEX-Format
     */
    private CVTextDisplay       lk_cDisplayInterfaceHEX = null;

    /**
     * Zugriffs- und Verwaltungsfunktionen fuer Dateien
     */
    private CVFileManagement    lk_cFileManagement = null;

    /**
     * Token zum Speichern des gewaehlten Dateipfades in Konfigurationsdatei
     */
    private final String        lk_szConfigTokenPath =
        "CVUIBeanShellScriptfilePath";


    private JTabbedPane jTabbedPaneScript       = null;
    private RTextScrollPane jScrollPaneScriptEditor = null;
    private JScrollPane jScrollPaneScriptOutput = null;
    private JTextArea   jTextAreaScriptOutput   = null;

    /**
     * Obere Knopfleiste
     */
    private JPanel     jPanelScriptButtonBarTop   = null;
    private JButton    jButtonLoadScript          = null;
    private JButton    jButtonSaveScript          = null;
    private JLabel     jLabelCurrentScript        = null;
    private JTextField jTextFieldCurrentScript    = null;
    private JButton    jButtonSaveOutput          = null;
    private JButton    jButtonClearOutput         = null;
    private JCheckBox  jCheckBoxShowErrorMessages = null;

    /**
     * Untere Knopfleiste
     */
    private JPanel  jPanelScriptButtonBarBottom = null;
    private JButton jButtonRunScript            = null;
    private JButton jButtonStopScript           = null;
    private JButton jButtonStepScript           = null;
    private JButton jButtonFind                 = null;
    private JTextField searchField              = null;
    private JCheckBox regexCB                   = null;
    private JCheckBox matchCaseCB               = null;
    private ActionListener alFind               = null;

    private boolean lk_bStdOutPrintf = false;

    /**
     * Konstruktor der Klasse CVUIBeanShell.
     *
     * @param cErrorMessage Ausgabe von Fehlermeldungen als Dialog
     * @param cErrorFile Ausgabe von Fehlermeldungen in Logdatei
     * @param cStatusMessage Ausgabe von Statusmeldungen auf der Statuszeile
     * @param cConfigFile Einlesen und Schreiben Konfigurationsdatei
     * @param cConnectionManager Verwaltet die Verbindungen zum Drucker
     */
    public CVUIBeanShell(
            ValentinConsole     valentinConsole,
            CVErrorMessage      cErrorMessage,
            CVLogging           cErrorFile,
            CVStatusLine        cStatusMessage,
            CVConfigFile        cConfigFile,
            CVConnectionManager cConnectionManager)
    {
        this.lk_valentinConsole    = valentinConsole;
        this.lk_cErrorMessage      = cErrorMessage;
        this.lk_cErrorFile         = cErrorFile;
        this.lk_cStatusMessage     = cStatusMessage;
        this.lk_cConfigFile        = cConfigFile;
        this.lk_cConnectionManager = cConnectionManager;

        this.lk_cBeanShell = new CVBeanShell(
                this.lk_valentinConsole,
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage,
                this.lk_cConfigFile);

        this.lk_cBeanShellEditor = new CVUIBeanShellEditor(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage,
                this.lk_cConfigFile);

        this.lk_cFileManagement = new CVFileManagement(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);

        this.lk_cDisplayInterfaceASCII = new CVTextDisplay(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);

        this.lk_cDisplayInterfaceHEX = new CVTextDisplay(
                this.lk_cErrorMessage,
                this.lk_cErrorFile,
                this.lk_cStatusMessage);

        this.initCVUIBeanShell();
        this.initialize();

        this.setupBeanShellInterpreter();

        return;
    }

    /**
     * Initialisiere Klassenkomponenten
     */
    private void initCVUIBeanShell()
    {
        this.lk_cDisplayInterfaceASCII.setOutputFormat(true, false);
        this.lk_cDisplayInterfaceHEX.setOutputFormat(false, true);

        return;
    }

    /**
     * Initialisiere BeanShell-Skriptinterpreter
     */
    private void setupBeanShellInterpreter()
    {
        // JTextArea ist in BeanShell &uuml;ber cTextArea zugreifbar
        this.lk_cBeanShell.setDisplayScriptOutput(this.jTextAreaScriptOutput);

        // Displays zur Anzetge der transportierten Daten
        this.lk_cBeanShell.setDisplayInterfaceASCII(
                this.lk_cDisplayInterfaceASCII);
        this.lk_cBeanShell.setDisplayInterfaceHEX(
                this.lk_cDisplayInterfaceHEX);

        // Schnittstelle in BeanShell laden
        this.lk_cBeanShell.setScriptInterfaces(this.lk_cConnectionManager);

        return;
    }

    /**
     * Skript in Editor laden
     */
    private void processButtonLoadScript()
    {
        final JFileChooser cFileChooser = new JFileChooser();
        cFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        //----------------------------------------------------------------------
        // Einlesen gewaehlter Pfad aus Konfigurationsdatei
        //----------------------------------------------------------------------
        if(this.lk_cConfigFile != null)
        {
            String configValue = null;
            configValue =
                this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPath);
            if(configValue != null)
            {
                cFileChooser.setCurrentDirectory(new File(configValue));
            }
        }

        int returnVal = cFileChooser.showOpenDialog(this);

        //----------------------------------------------------------------------
        // Datei mit Auswahldialog abfragen
        //----------------------------------------------------------------------
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File cScriptFile     = cFileChooser.getSelectedFile();

            //------------------------------------------------------------------
            // Speichern gewaehlter Pfad in Konfigurationsdatei
            //------------------------------------------------------------------
            if(this.lk_cConfigFile != null)
            {
                lk_cConfigFile.setConfig(
                   this.lk_szConfigTokenPath,
                   cScriptFile.getAbsolutePath());
            }

            if(true == this.lk_cBeanShellEditor.loadScriptFile(cScriptFile))
            {
                this.jTextFieldCurrentScript.setText(cScriptFile.getName());
                this.lk_cBeanShell.setScriptPath(cScriptFile.getParent() + "\\");
            }
        }

        return;
    }

    /**
     * Skript aus Editor speichern
     */
    private void processButtonSaveScript()
    {
        final JFileChooser cFileChooser = new JFileChooser();
        cFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        //----------------------------------------------------------------------
        // Einlesen gewaehlter Pfad aus Konfigurationsdatei
        //----------------------------------------------------------------------
        if(this.lk_cConfigFile != null)
        {
            String configValue = null;
            configValue =
                this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPath);
            if(configValue != null)
            {
                cFileChooser.setCurrentDirectory(new File(configValue));
            }
        }

        int returnVal = cFileChooser.showSaveDialog(this);

        //----------------------------------------------------------------------
        // Datei mit Auswahldialog abfragen
        //----------------------------------------------------------------------
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File cScriptFile     = cFileChooser.getSelectedFile();

            //------------------------------------------------------------------
            // Speichern gewaehlter Pfad in Konfigurationsdatei
            //------------------------------------------------------------------
            if(this.lk_cConfigFile != null)
            {
                lk_cConfigFile.setConfig(
                   this.lk_szConfigTokenPath,
                   cScriptFile.getAbsolutePath());
            }

            if(true == this.lk_cBeanShellEditor.storeScriptFile(cScriptFile))
            {
                this.jTextFieldCurrentScript.setText(cScriptFile.getName());
            }
        }

        return;
    }

    /**
     * Skriptausgaben speichern
     */
    private void processButtonSaveOutput()
    {
        final JFileChooser cFileChooser = new JFileChooser();
        cFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        //----------------------------------------------------------------------
        // Einlesen gewaehlter Pfad aus Konfigurationsdatei
        //----------------------------------------------------------------------
        if(this.lk_cConfigFile != null)
        {
            String configValue = null;
            configValue =
                this.lk_cConfigFile.getConfig(this.lk_szConfigTokenPath);
            if(configValue != null)
            {
                cFileChooser.setCurrentDirectory(new File(configValue));
            }
        }

        int returnVal = cFileChooser.showSaveDialog(this);

        //----------------------------------------------------------------------
        // Datei mit Auswahldialog abfragen
        //----------------------------------------------------------------------
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File cOutputFile     = cFileChooser.getSelectedFile();

            //------------------------------------------------------------------
            // Speichern gewaehlter Pfad in Konfigurationsdatei
            //------------------------------------------------------------------
            if(this.lk_cConfigFile != null)
            {
                lk_cConfigFile.setConfig(
                   this.lk_szConfigTokenPath,
                   cOutputFile.getAbsolutePath());
            }

            this.lk_cFileManagement.storeStringToFile(
                    cOutputFile, this.jTextAreaScriptOutput.getText());
        }

        return;
    }

    /**
     * Skript starten
     */
    public void processButtonRunScript(boolean bAutoScript)
    {
        lk_bStdOutPrintf = bAutoScript;
        this.lk_cBeanShell.runScript(lk_cBeanShellEditor.getScriptFile(),
                bAutoScript);

        return;
    }

    /**
     * Skript stoppen
     */
    private void processButtonStopScript()
    {
        this.lk_cBeanShell.stopScript();

        return;
    }

    /**
     * Fehlermeldungen ein- oder ausschalten
     *
     * @param bButtonState true - Meldungen ein, false - Meldungen aus
     */
    private void processButoonEnableErrorMessages(boolean bButtonState)
    {
        this.lk_cErrorMessage.setShowWindow(bButtonState, false);

        return;
    }

    /**
     * Skript im Einzelschrittmodus
     */
    private void processButtonStepScript()
    {
        return;
    }

    private void processButtonFind()
    {
        JDialog dialog = new JDialog(lk_valentinConsole);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        dialog.add(panel);

        searchField = new JTextField(30);
        panel.add(searchField);

        JButton nextButton = new JButton("Find Next");
        nextButton.setActionCommand("FindNext");
        nextButton.addActionListener(alFind);
        panel.add(nextButton);
        searchField.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              nextButton.doClick(0);
           }
        });
        JButton prevButton = new JButton("Find Previous");
        prevButton.setActionCommand("FindPrev");
        prevButton.addActionListener(alFind);
        panel.add(prevButton);
        regexCB = new JCheckBox("Regex");
        panel.add(regexCB);
        matchCaseCB = new JCheckBox("Match Case");
        panel.add(matchCaseCB);

        dialog.pack();
        dialog.setModal(false);
        dialog.setVisible(true);

        return;
    }

    /**
     * Abfrage zur Klasse gehoerender Skriptinterpreter
     *
     * @return Skriptinterpreter
     */
    public CVBeanShell getBeanShell()
    {
        return this.lk_cBeanShell;
    }

    /**
     * Abfrage des Writers fuer die ASCII-Ausgabe der ueber die Schnittstelle
     * transportierten Daten.
     *
     * @return Writer fuer ASCII-Ausgabe
     */
    public Writer getDisplayInterfaceASCIIWriter()
    {
        return this.lk_cDisplayInterfaceASCII.getWriter();
    }

    /**
     * Abfrage des Writers fuer die HEX-Ausgabe der ueber die Schnittstelle
     * transportierten Daten.
     *
     * @return Writer fuer HEX-Ausgabe
     */
    public Writer getDisplayInterfaceHEXWriter()
    {
        return this.lk_cDisplayInterfaceHEX.getWriter();
    }


    public void loadScript(String sScript)
    {
        File cScriptFile = new File(sScript);

        if (lk_cBeanShellEditor.loadScriptFile(cScriptFile))
        {
            jTextFieldCurrentScript.setText(cScriptFile.getName());
            lk_cBeanShell.setScriptPath(cScriptFile.getParent() + "\\");
        }
    }


    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        BorderLayout borderLayoutCVUIBeanShell = new BorderLayout();
        this.setLayout(borderLayoutCVUIBeanShell);
        borderLayoutCVUIBeanShell.setHgap(5);
        borderLayoutCVUIBeanShell.setVgap(5);
        this.add(getJPanelScriptButtonBarTop(), java.awt.BorderLayout.NORTH);
        this.add(getJPanelScriptButtonBarBottom(), java.awt.BorderLayout.SOUTH);
        this.add(getJTabbedPaneScript(), java.awt.BorderLayout.CENTER);
    }
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelScriptButtonBarTop() {
        if (jPanelScriptButtonBarTop == null) {
            jLabelCurrentScript = new JLabel();
            FlowLayout flowLayout2 = new FlowLayout();
            jPanelScriptButtonBarTop = new JPanel();
            jPanelScriptButtonBarTop.setLayout(flowLayout2);
            flowLayout2.setAlignment(java.awt.FlowLayout.LEFT);
            jLabelCurrentScript.setText("Current Script:");
            jPanelScriptButtonBarTop.add(getJButtonLoadScript(), null);
            jPanelScriptButtonBarTop.add(getJButtonSaveScript(), null);
            jPanelScriptButtonBarTop.add(jLabelCurrentScript, null);
            jPanelScriptButtonBarTop.add(getJTextFieldCurrentScript(), null);
            jPanelScriptButtonBarTop.add(getJButtonSaveOutput(), null);
            jPanelScriptButtonBarTop.add(getJButtonClearOutput(), null);
        }
        return jPanelScriptButtonBarTop;
    }
    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPaneScript() {
        if (jTabbedPaneScript == null) {
            jTabbedPaneScript = new JTabbedPane();
            jTabbedPaneScript.addTab(
                    "ScriptEditor", null, getJScrollPaneScriptEditor(),
                    "view and modify scripts");
            jTabbedPaneScript.addTab(
                    "Script Output", null, getJScrollPaneScriptOutput(),
                    "output current script");
            jTabbedPaneScript.addTab(
                    "Interface ASCII", null,
                    this.lk_cDisplayInterfaceASCII.getTextAreaPane(),
                    "interface data ASCII formatted");
/*
            jTabbedPaneScript.addTab(
                    "Interface HEX", null,
                    this.lk_cDisplayInterfaceHEX.getTextAreaPane(),
                    "interface data HEX formatted");
*/
        }
        return jTabbedPaneScript;
    }
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelScriptButtonBarBottom() {
        if (jPanelScriptButtonBarBottom == null) {
            FlowLayout flowLayout3 = new FlowLayout();
            jPanelScriptButtonBarBottom = new JPanel();
            jPanelScriptButtonBarBottom.setLayout(flowLayout3);
            flowLayout3.setAlignment(java.awt.FlowLayout.LEFT);
            jPanelScriptButtonBarBottom.add(getJButtonRunScript(), null);
            jPanelScriptButtonBarBottom.add(getJButtonStopScript(), null);
            //jPanelScriptButtonBarBottom.add(getJButtonStepScript(), null);
            jPanelScriptButtonBarBottom.add(
                    getJCheckBoxShowErrorMessages(), null);
            jPanelScriptButtonBarBottom.add(getJButtonFind(), null);

            alFind = new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    // "FindNext" => search forward, "FindPrev" => search backward
                    String command = e.getActionCommand();
                    boolean forward = "FindNext".equals(command);

                    // Create an object defining our search parameters.
                    SearchContext context = new SearchContext();
                    String text = searchField.getText();
                    if (text.length() == 0) {
                       return;
                    }
                    context.setSearchFor(text);
                    context.setMatchCase(matchCaseCB.isSelected());
                    context.setRegularExpression(regexCB.isSelected());
                    context.setSearchForward(forward);
                    context.setWholeWord(false);

                    boolean found = SearchEngine.find(lk_cBeanShellEditor, context).wasFound();
                    if (!found) {
                       JOptionPane.showMessageDialog(lk_valentinConsole, "Text not found");
                    }
                }
            };

        }
        return jPanelScriptButtonBarBottom;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonLoadScript() {
        if (jButtonLoadScript == null) {
            jButtonLoadScript = new JButton();
            jButtonLoadScript.setText("Load Script");
            jButtonLoadScript.setToolTipText("load script to editor");
            jButtonLoadScript.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonLoadScript();
                }
            });
        }
        return jButtonLoadScript;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonSaveScript() {
        if (jButtonSaveScript == null) {
            jButtonSaveScript = new JButton();
            jButtonSaveScript.setText("Save Script");
            jButtonSaveScript.setToolTipText("save script from editor to file");
            jButtonSaveScript.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonSaveScript();
                }
            });
        }
        return jButtonSaveScript;
    }
    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldCurrentScript() {
        if (jTextFieldCurrentScript == null) {
            jTextFieldCurrentScript = new JTextField();
            jTextFieldCurrentScript.setEditable(false);
            jTextFieldCurrentScript.setText("no script loaded");
            jTextFieldCurrentScript.setPreferredSize(
                    new java.awt.Dimension(200,20));
        }
        return jTextFieldCurrentScript;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonSaveOutput() {
        if (jButtonSaveOutput == null) {
            jButtonSaveOutput = new JButton();
            jButtonSaveOutput.setText("Save Output");
            jButtonSaveOutput.setToolTipText("save script output to file");
            jButtonSaveOutput.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonSaveOutput();
                }
            });
        }
        return jButtonSaveOutput;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonRunScript() {
        if (jButtonRunScript == null) {
            jButtonRunScript = new JButton();
            jButtonRunScript.setText("Run Script");
            jButtonRunScript.setToolTipText("execute the whole script");
            jButtonRunScript.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonRunScript(false);
                }
            });
        }
        return jButtonRunScript;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonStopScript() {
        if (jButtonStopScript == null) {
            jButtonStopScript = new JButton();
            jButtonStopScript.setToolTipText("stop current script execution");
            jButtonStopScript.setText("Stop script");
            jButtonStopScript.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonStopScript();
                }
            });
        }
        return jButtonStopScript;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonStepScript() {
        if (jButtonStepScript == null) {
            jButtonStepScript = new JButton();
            jButtonStepScript.setText("Step Script");
            jButtonStepScript.setToolTipText("execute next script command");
            jButtonStepScript.setEnabled(false);
            jButtonStepScript.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonStepScript();
                }
            });
        }
        return jButtonStepScript;
    }

    private JButton getJButtonFind() {
        if (jButtonFind == null) {
            jButtonFind = new JButton();
            jButtonFind.setText("Find");
            jButtonFind.setEnabled(true);
            jButtonFind.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButtonFind();
                }
            });
        }
        return jButtonFind;
    }

    class documentListenerJTA implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {}
        public void insertUpdate(DocumentEvent e) {}
        public void removeUpdate(DocumentEvent e) {}
    }
    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextAreaScriptOutput() {
        if (jTextAreaScriptOutput == null) {
            jTextAreaScriptOutput = new JTextArea();
            jTextAreaScriptOutput.setEditable(false);
            jTextAreaScriptOutput.setFont(
                    new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            jTextAreaScriptOutput.getDocument().addDocumentListener(
                    new documentListenerJTA()
            {
                public void insertUpdate(DocumentEvent e)
                {
                    if (!lk_bStdOutPrintf)
                    {
                        return;
                    }
                    try
                    {
                        System.out.print(e.getDocument().getText(
                                            e.getOffset(), e.getLength()));
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {

                    }
                }
            }
            );
        }
        return jTextAreaScriptOutput;
    }
    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPaneScriptOutput() {
        if (jScrollPaneScriptOutput == null) {
            jScrollPaneScriptOutput = new JScrollPane();
            jScrollPaneScriptOutput.setViewportView(getJTextAreaScriptOutput());
        }
        return jScrollPaneScriptOutput;
    }
    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPaneScriptEditor() {
        if (jScrollPaneScriptEditor == null) {
            jScrollPaneScriptEditor = new RTextScrollPane(this.lk_cBeanShellEditor);
            jScrollPaneScriptEditor.setFoldIndicatorEnabled(true);
        }
        return jScrollPaneScriptEditor;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonClearOutput() {
        if (jButtonClearOutput == null) {
            jButtonClearOutput = new JButton();
            jButtonClearOutput.setText("Clear Output");
            jButtonClearOutput.setToolTipText("clear script output window");
            jButtonClearOutput.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    jTextAreaScriptOutput.setText("");
                }
            });
        }
        return jButtonClearOutput;
    }
    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBoxShowErrorMessages() {
        if (jCheckBoxShowErrorMessages == null) {
            jCheckBoxShowErrorMessages = new JCheckBox();
            jCheckBoxShowErrorMessages.setName("Show Error Messages");
            jCheckBoxShowErrorMessages.setSelected(true);
            jCheckBoxShowErrorMessages.setText("Show Error Messages");
            jCheckBoxShowErrorMessages.setToolTipText(
                    "enable or disable separate windows with error messages");
            jCheckBoxShowErrorMessages.addActionListener(
                    new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    processButoonEnableErrorMessages(
                            jCheckBoxShowErrorMessages.isSelected());
                }
            });
        }
        return jCheckBoxShowErrorMessages;
    }
 }
