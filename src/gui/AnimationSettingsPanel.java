package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AnimationSettingsPanel extends JPanel implements ActionListener, ChangeListener{

	private static final long serialVersionUID = -2932732334279744845L;
	
	JCheckBox drawDataBox;
	JCheckBox drawRegressionBox;
	JComboBox<String> drawPriorPredictionBox;
	JComboBox<String> drawPostPredictionBox;
	JComboBox<String> drawPriorBox;
	JComboBox<String> drawPosteriorBox;
	
	JScrollPane scrollPane;
	
	AnimationPanel refAniPanel;
	
	AnimationSettingsPanel(AnimationPanel refAniPanelNew) {
		refAniPanel = refAniPanelNew;
		
		String[] densityDisplayChoices = {"Off", "0.9", "0.9, 0.5", "0.9, 0.5, 0.2"};
		String[] predictionDisplayChoices = {"Off", "Simple", "Full"};
		
		drawDataBox = new JCheckBox("Show data");
		drawDataBox.addChangeListener(this);
		drawDataBox.setSelected(true);
		drawRegressionBox = new JCheckBox("Show regression");
		drawRegressionBox.addChangeListener(this);
		drawRegressionBox.setSelected(true);
		
		JLabel priorPredictionLabel = new JLabel("Prior prediction:");
		drawPriorPredictionBox = new JComboBox<String>(predictionDisplayChoices);
		drawPriorPredictionBox.addActionListener(this);
		JPanel priorPredictionHelperPanel = new JPanel();
		priorPredictionHelperPanel.setLayout(new FlowLayout());
		priorPredictionHelperPanel.add(priorPredictionLabel);
		priorPredictionHelperPanel.add(drawPriorPredictionBox);
		
		JLabel postPredictionLabel = new JLabel("Posterior prediction:");
		drawPostPredictionBox = new JComboBox<String>(predictionDisplayChoices);
		drawPostPredictionBox.addActionListener(this);
		JPanel postPredictionHelperPanel = new JPanel();
		postPredictionHelperPanel.setLayout(new FlowLayout());
		postPredictionHelperPanel.add(postPredictionLabel);
		postPredictionHelperPanel.add(drawPostPredictionBox);
		
		JLabel priorLabel = new JLabel("Show prior");
		drawPriorBox = new JComboBox<String>(densityDisplayChoices);
		drawPriorBox.addActionListener(this);
		JPanel priorHelperPanel = new JPanel();
		priorHelperPanel.setLayout(new GridLayout(1,2));
		priorHelperPanel.add(priorLabel);
		priorHelperPanel.add(drawPriorBox);
		
		JLabel posteriorLabel = new JLabel("Show posterior");
		drawPosteriorBox = new JComboBox<String>(densityDisplayChoices);
		drawPosteriorBox.addActionListener(this);
		JPanel posteriorHelperPanel = new JPanel();
		posteriorHelperPanel.setLayout(new GridLayout(1, 2));
		posteriorHelperPanel.add(posteriorLabel);
		posteriorHelperPanel.add(drawPosteriorBox);
		
		JPanel helperPanel = new JPanel();
		helperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		helperPanel.add(drawDataBox);
		helperPanel.add(drawRegressionBox);
		helperPanel.add(priorHelperPanel);
		helperPanel.add(posteriorHelperPanel);
		helperPanel.add(priorPredictionHelperPanel);
		helperPanel.add(postPredictionHelperPanel);
		
		add(helperPanel);
	}
	
	int densityDisplayChoiceToInt(String input) {
		if(input.equals("0.9")) {
			return 1;
		}
		if(input.equals("0.9, 0.5")) {
			return 2;
		}
		if(input.equals("0.9, 0.5, 0.2")) {
			return 3;
		}
		return 0;
	}
	
	int predictionDisplayChoicesToInt(String input) {
		if(input.equals("Simple")) {
			return 1;
		}
		if(input.equals("Full")) {
			return 2;
		}
		return 0;
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		if(ce.getSource() == drawDataBox) {
			refAniPanel.setDrawData(drawDataBox.isSelected());
		}
		if(ce.getSource() == drawRegressionBox) {
			refAniPanel.setDrawRegression(drawRegressionBox.isSelected());
		}
		refAniPanel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == drawPriorBox) {
			refAniPanel.setDrawPrior(densityDisplayChoiceToInt((String)drawPriorBox.getSelectedItem()));
		}
		if(ae.getSource() == drawPosteriorBox) {
			refAniPanel.setDrawPosterior(densityDisplayChoiceToInt((String)drawPosteriorBox.getSelectedItem()));
		}
		if(ae.getSource() == drawPriorPredictionBox) {
			refAniPanel.setDrawPriorPrediction(predictionDisplayChoicesToInt((String)drawPriorPredictionBox.getSelectedItem()));
		}
		if(ae.getSource() == drawPostPredictionBox) {
			refAniPanel.setDrawPostPrediction(predictionDisplayChoicesToInt((String)drawPostPredictionBox.getSelectedItem()));
		}
		refAniPanel.repaint();
	}
	
}
