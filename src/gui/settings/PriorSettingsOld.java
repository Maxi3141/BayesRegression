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

public class PriorSettingsOld extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -5632380091997414998L;

	int order = 2;
	
	Matrix muMatrix;
	Matrix sigmaMatrix;
	
	JLabel muLabel;
	JTextField muText;
	JLabel sigmaLabel;
	JTextField sigmaText;
	
	JPanel helperPanel0;
	JPanel helperPanel1;
	JPanel helperPanel2;
	
	JPanel muPanel;
	JLabel[] muContentLabel;
	JPanel sigmaPanel;
	JLabel[][] sigmaContentLabel;
	
	JButton activateButton;
	
	SettingsFrame refSettingsFrame;
	
	public PriorSettingsOld(SettingsFrame refSettingsFrameNew) {
		refSettingsFrame = refSettingsFrameNew;
		
		muLabel = new JLabel("mu = ");
		muText = new JTextField(10);
		muText.setText("0;0");
		sigmaLabel = new JLabel("Sigma = ");
		sigmaText = new JTextField(10);
		sigmaText.setText("1,0;0,1");
		
		helperPanel0 = new JPanel();
		helperPanel0.setLayout(new GridLayout(2,2));
		helperPanel0.add(muLabel);
		helperPanel0.add(muText);
		helperPanel0.add(sigmaLabel);
		helperPanel0.add(sigmaText);

		muPanel = new JPanel();
		sigmaPanel = new JPanel();
		helperPanel1 = new JPanel();
		updateContentMatrices();
		updateContentLabels();
		helperPanel1.setLayout(new GridLayout(1,2));
		JPanel helperPanel1_1 = new JPanel();
		helperPanel1_1.add(muPanel);
		helperPanel1.add(helperPanel1_1);
		JPanel helperPanel1_2 = new JPanel();
		helperPanel1_2.add(sigmaPanel);
		helperPanel1.add(helperPanel1_2);
		
		helperPanel2 = new JPanel();
		helperPanel2.setLayout(new BorderLayout());
		helperPanel2.add(helperPanel0, BorderLayout.PAGE_START);
		helperPanel2.add(helperPanel1, BorderLayout.CENTER);
		
		activateButton = new JButton("Update");
		activateButton.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(helperPanel2, BorderLayout.CENTER);
		add(activateButton, BorderLayout.PAGE_END);
	}
	
	
	
	void updateContentMatrices() {
		muMatrix = new Matrix(muText.getText());
		sigmaMatrix = new Matrix(sigmaText.getText());
	}
	
	void updateContentLabels() {
		muPanel.removeAll();
		muPanel.setLayout(new GridLayout(muMatrix.getNumRows(), 1, 10, 10));
		for(int i = 0; i < muMatrix.getNumRows(); i++) {
			muPanel.add(new JLabel(String.valueOf(muMatrix.getElement(i, 0))));
		}
		sigmaContentLabel = new JLabel[sigmaMatrix.getNumRows()][sigmaMatrix.getNumCols()];
		sigmaPanel.removeAll();
		sigmaPanel.setLayout(new GridLayout(sigmaMatrix.getNumRows(), sigmaMatrix.getNumCols(), 10, 10));
		for(int i = 0; i < sigmaMatrix.getNumRows(); i++) {
			for(int j = 0; j < sigmaMatrix.getNumCols(); j++) {
				sigmaPanel.add(new JLabel(String.valueOf(sigmaMatrix.getElement(i, j))));
			}
		}
		helperPanel1.revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == activateButton) {
			updateContentMatrices();
			updateContentLabels();
			refSettingsFrame.setPriorToNormalPrior(muMatrix, sigmaMatrix);
		}
	}
	
}
