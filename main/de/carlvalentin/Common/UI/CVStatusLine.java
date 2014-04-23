package de.carlvalentin.Common.UI;

import javax.swing.JPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
/**
 * Grafische Statuszeile fuer Textmeldungen
 */
public class CVStatusLine extends JPanel {

	private JLabel jLabelStatusText = null;
	/**
	 * This method initializes 
	 * 
	 */
	public CVStatusLine() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new java.awt.Dimension(200,25));
        this.add(getJLabelStatusText(), java.awt.BorderLayout.WEST);
			
	}
	/**
	 * This method initializes jLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */    
	private JLabel getJLabelStatusText() {
		if (jLabelStatusText == null) {
			jLabelStatusText = new JLabel();
			jLabelStatusText.setText("program status line");
			jLabelStatusText.setToolTipText("program status line");
			jLabelStatusText.setHorizontalAlignment(
                    javax.swing.SwingConstants.LEFT);
			jLabelStatusText.setHorizontalTextPosition(
                    javax.swing.SwingConstants.LEFT);
		}
		return jLabelStatusText;
	}
    
    /**
     * Nachricht auf der Statuszeile ausgeben.
     * 
     * @param message Auszzugebende Nachricht.
     */
    public void write(String message)
    {
    	this.jLabelStatusText.setText(message);
    }
 }
