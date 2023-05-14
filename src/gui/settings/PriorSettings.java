package gui.settings;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gui.SettingsFrame;
import math.linAlg.Matrix;

public class PriorSettings extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 7061465695024691743L;

	int order = 2;
	
	Matrix muMatrix;
	Matrix sigmaMatrix;
	
	JPanel helperPanel;
	
	JLabel muLabel;
	JLabel sigmaLabel;
	
	JPanel muPanel;
	JTextField[] muContentText;
	JPanel sigmaPanel;
	JTextField[][] sigmaContentText;
	
	JButton activateButton;
	
	SettingsFrame refSettingsFrame;
	
	public PriorSettings(SettingsFrame refSettingsFrameNew) {
		refSettingsFrame = refSettingsFrameNew;
		
		muLabel = new JLabel("mu = ");
		sigmaLabel = new JLabel("Sigma = ");
		
		helperPanel = new JPanel();
		muPanel = new JPanel();
		sigmaPanel = new JPanel();
		initMatrices();
		updateGUIFormat();
		JPanel muHelperPanel = new JPanel();
		muHelperPanel.setLayout(new BorderLayout());
		muHelperPanel.add(muLabel, BorderLayout.LINE_START);
		muHelperPanel.add(muPanel, BorderLayout.CENTER);
		JPanel sigmaHelperPanel = new JPanel();
		sigmaHelperPanel.setLayout(new BorderLayout());
		sigmaHelperPanel.add(sigmaLabel, BorderLayout.LINE_START);
		sigmaHelperPanel.add(sigmaPanel, BorderLayout.CENTER);
		helperPanel.setLayout(new BorderLayout());
		helperPanel.add(muHelperPanel, BorderLayout.LINE_START);
		helperPanel.add(sigmaHelperPanel, BorderLayout.CENTER);
		
		activateButton = new JButton("Update");
		activateButton.addActionListener(this);
		
		JPanel helperPanel2 = new JPanel();
		helperPanel2.add(helperPanel);
		setLayout(new BorderLayout());
		add(helperPanel2, BorderLayout.CENTER);
		add(activateButton, BorderLayout.PAGE_END);
	}
	
	void initMatrices() {
		muMatrix = new Matrix(order, 1);
		muMatrix.setZero();
		sigmaMatrix = new Matrix(order, order);
	}
	
	public void updateDimensions(int orderNew) {
		order = orderNew;
	}
	
	/* Update dimensions of prior input to new regression order.
	 * Remember old inputs and fill new ones (if order was increased) with identity matrix
	 */
	public void updateGUIFormat() {
		muContentText = new JTextField[order];
		sigmaContentText = new JTextField[order][order];
		
		for(int i = 0; i < Math.min(order, muMatrix.getNumRows()); i++) {
			muContentText[i] = new JTextField(4);
			muContentText[i].setText(String.valueOf(muMatrix.getElement(i, 0)));
		}
		for(int i = muMatrix.getNumRows(); i < order; i++) {
			muContentText[i] = new JTextField();
			muContentText[i].setText("0.0");
		}
		
		for(int i = 0; i < order; i++) {
			for(int j = 0; j < order; j++) {
				sigmaContentText[i][j] = new JTextField(3);
				sigmaContentText[i][j].setText("0.0");
			}
		}
		for(int i = 0; i < Math.min(order, sigmaMatrix.getNumRows()); i++) {
			for(int j = 0; j < Math.min(order, sigmaMatrix.getNumCols()); j++) {
				sigmaContentText[i][j].setText(String.valueOf(sigmaMatrix.getElement(i, j)));
			}
		}
		for(int i = sigmaMatrix.getNumRows(); i < order; i++) {
			sigmaContentText[i][i].setText("1.0");
		}
		
		muPanel.removeAll();
		sigmaPanel.removeAll();
		muPanel.setLayout(new GridLayout(order, 1, 10, 10));
		sigmaPanel.setLayout(new GridLayout(order, order, 10, 10));
		
		for(int i = 0; i < order; i++) {
			muPanel.add(muContentText[i]);
			for(int j = 0; j < order; j++) {
				sigmaPanel.add(sigmaContentText[i][j]);
			}
		}
		helperPanel.revalidate();
		updateContentMatrices();
		refSettingsFrame.setPriorToNormalPrior(muMatrix, sigmaMatrix);
	}
	
	void updateContentMatrices() {
		muMatrix = new Matrix(order, 1);
		sigmaMatrix = new Matrix(order, order);
		for(int i = 0; i < order; i++) {
			double newMuEntry = 0.0;
			try {
				newMuEntry = Double.valueOf(muContentText[i].getText());
			} catch(Exception e) {
				muContentText[i].setText("0.0");
				newMuEntry = 0.0;
			}
			muMatrix.setElement(i, 0, newMuEntry);
			
			for(int j = 0; j < order; j++) {
				double newSigmaEntry = 0.0;
				try {
					newSigmaEntry = Double.valueOf(sigmaContentText[i][j].getText());
				} catch(Exception e) {
					if(i == j) {
						sigmaContentText[i][j].setText("1.0");
						newSigmaEntry = 1.0;
					} else {
						sigmaContentText[i][j].setText("0.0");
						newSigmaEntry = 0.0;
					}
				}
				sigmaMatrix.setElement(i, j, newSigmaEntry);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == activateButton) {
			updateContentMatrices();
			refSettingsFrame.setPriorToNormalPrior(muMatrix, sigmaMatrix);
		}
	}
	
}
