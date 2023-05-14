package gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import gui.settings.InfoPanel;
import gui.settings.PriorSettings;
import gui.settings.RegressionSettings;
import math.DataManager;
import math.RegressionTypes;
import math.linAlg.Matrix;

public class SettingsFrame extends JFrame {
	
	private static final long serialVersionUID = -8673343529444358601L;

	JTabbedPane settingTabs;
	
	InfoPanel infoPanel;
	RegressionSettings regressionTab;
	PriorSettings priorTab;
	
	DataManager refDataManager;
	AnimationPanel refAnimationPanel;
	
	SettingsFrame(DataManager refDataManagerNew, AnimationPanel refAnimationPanelNew) {
		refDataManager = refDataManagerNew;
		refAnimationPanel = refAnimationPanelNew;
		
		infoPanel = new InfoPanel();
		regressionTab = new RegressionSettings(this);
		priorTab = new PriorSettings(this);
		
		settingTabs = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		settingTabs.addTab("Info", infoPanel);
		settingTabs.addTab("Regression", regressionTab);
		settingTabs.addTab("Prior", priorTab);
		
		setTitle("Settings");
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(settingTabs);
		setVisible(true);
	}
	
	/* Resize prior settings according to the new regression order
	*  and tell dataManager to adjust all connected math
	*/
	public void setRegressionOrder(int order) {
		priorTab.updateDimensions(order + 1);
		priorTab.updateGUIFormat();
		refDataManager.setRegressionOrder(order);
		refAnimationPanel.repaint();
	}
	
	
	public void setRegressionSigmaSq(double sigmaSqNew) {
		refDataManager.setRegressionSigmaSq(sigmaSqNew);
		refAnimationPanel.repaint();
	}
	
	public void setPriorToNormalPrior(Matrix mu, Matrix sigma) {
		refDataManager.setToNormalPrior(mu, sigma);
		refAnimationPanel.repaint();
	}
	
	public void setRegressionType(RegressionTypes typeNew) {
		refDataManager.setRegressionType(typeNew);
	}
	
	public void setDrawBasisFunctions(boolean drawBasisFunctions) {
		refAnimationPanel.setDrawBasisFunctions(drawBasisFunctions);
		refAnimationPanel.repaint();
	}
	
}

