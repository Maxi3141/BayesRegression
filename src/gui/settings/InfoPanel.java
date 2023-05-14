package gui.settings;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Component;

public class InfoPanel extends JPanel {
	
	private static final long serialVersionUID = 2763146750835399642L;
	
	JLabel inputsLabel;
	JLabel separatorLabel;
	JLabel explainLabel;
	
	public InfoPanel() {
		inputsLabel = new JLabel(
				"<html>"
				+ "Left-click<br>"
				+ "Left-drag<br>"
				+ "Right-drag<br>"
				+ "Mouse wheel<br>"
				+ "Key \"R\"<br>"
				+ "</html>");
		inputsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		separatorLabel = new JLabel("<html>:<br>:<br>:<br>:<br>:</htmll>");
		
		explainLabel = new JLabel(
				"<html>"
				+ "Add data point<br>"
				+ "Move closest data point<br>"
				+ "Move in coordinate system<br>"
				+ "Zoom-in and zoom-out<br>"
				+ "Remove closest data point<br>"
				+ "</html>");
		
		add(inputsLabel);
		add(separatorLabel);
		add(explainLabel);
	}
	
}
