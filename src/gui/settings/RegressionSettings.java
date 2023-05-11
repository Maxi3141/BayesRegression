package gui.settings;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.SettingsFrame;
import math.RegressionTypes;

public class RegressionSettings extends JPanel implements ChangeListener, DocumentListener, ActionListener {
	
	private static final long serialVersionUID = 7287390901243912835L;
	
	JLabel polyOrderLabel;
	JSpinner polyOrderSpinner;
	
	JLabel polySigmaLabel;
	JTextField polySigmaText;
	
	JComboBox<String> regressionTypeBox;
	JLabel regressionTypeLabel;
	
	JCheckBox showBasisFunctionsBox;
	
	SettingsFrame refSettingsFrame;
	
	public RegressionSettings(SettingsFrame refSettingsFrameNew) {
		refSettingsFrame = refSettingsFrameNew;
		
		polyOrderLabel = new JLabel("Order of polynomial:");
		SpinnerModel spinnerModel = new SpinnerNumberModel(1, 0, 20, 1);
		polyOrderSpinner = new JSpinner(spinnerModel);
		polyOrderSpinner.addChangeListener(this);
		JPanel orderHelperPanel = new JPanel();
		orderHelperPanel.setLayout(new GridLayout(1,2));
		orderHelperPanel.add(polyOrderLabel);
		orderHelperPanel.add(polyOrderSpinner);
		
		polySigmaLabel = new JLabel("sigma^2 = ");
		polySigmaText = new JTextField(5);
		polySigmaText.setText("1.0");
		polySigmaText.getDocument().addDocumentListener(this);
		JPanel sigmaHelperPanel = new JPanel();
		sigmaHelperPanel.setLayout(new GridLayout(1, 2));
		sigmaHelperPanel.add(polySigmaLabel);
		sigmaHelperPanel.add(polySigmaText);
		
		regressionTypeLabel = new JLabel("Type of regression:");
		String[] regressionTypeChoices = {"Polynomial", "Cosine", "Gaussian", "Sigmoid"};
		regressionTypeBox = new JComboBox<String>(regressionTypeChoices);
		regressionTypeBox.addActionListener(this);
		JPanel typeHelperPanel = new JPanel();
		typeHelperPanel.setLayout(new FlowLayout());
		typeHelperPanel.add(regressionTypeLabel);
		typeHelperPanel.add(regressionTypeBox);
		
		showBasisFunctionsBox = new JCheckBox("Show basis functions");
		showBasisFunctionsBox.addActionListener(this);
		
		JPanel helperPanel = new JPanel();
		helperPanel.setLayout(new GridLayout(4, 1, 10, 10));
		helperPanel.add(orderHelperPanel);
		helperPanel.add(sigmaHelperPanel);
		helperPanel.add(typeHelperPanel);
		helperPanel.add(showBasisFunctionsBox);
		add(helperPanel);
	}
	
	void updateSigma(String textFieldText) {
		double newSigmaSq = 1.0;
		try {
			newSigmaSq = Double.valueOf(textFieldText);
		} catch(Exception e) {}
		if(newSigmaSq < 0) {
			newSigmaSq = 1.0;
		}
		refSettingsFrame.setRegressionSigmaSq(newSigmaSq);
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		if(ce.getSource() == polyOrderSpinner) {
			refSettingsFrame.setRegressionOrder((int)polyOrderSpinner.getValue());
		}
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		if(de.getDocument() == polySigmaText.getDocument()) {
			updateSigma(polySigmaText.getText());
		}
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		if(de.getDocument() == polySigmaText.getDocument()) {
			updateSigma(polySigmaText.getText());
		}
	}

	@Override
	public void changedUpdate(DocumentEvent de) {
		// Empty
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == regressionTypeBox) {
			RegressionTypes selectedType = RegressionTypes.valueOf(((String)regressionTypeBox.getSelectedItem()).toUpperCase());
			refSettingsFrame.setRegressionType(selectedType);
		}
		if(ae.getSource() == showBasisFunctionsBox) {
			refSettingsFrame.setDrawBasisFunctions(showBasisFunctionsBox.isSelected());
		}
	}
	
}
