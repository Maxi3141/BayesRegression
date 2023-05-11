package gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import math.DataManager;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = -8196795869145497676L;

	AnimationPanel aniPanel;
	
	DataManager dataManager;
	
	SettingsFrame settingsFrame;
	
	AnimationSettingsPanel aniSetPanel;
	
	public MainFrame() {
		dataManager = new DataManager();
		
		aniPanel = new AnimationPanel(dataManager);
		dataManager.setAniPanel(aniPanel);
		
		settingsFrame = new SettingsFrame(dataManager, aniPanel);
		
		aniSetPanel = new AnimationSettingsPanel(aniPanel);
		
		setTitle("Regression");
		setSize(1000,1000);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		add(aniPanel, BorderLayout.CENTER);
		add(aniSetPanel, BorderLayout.PAGE_END);
		
		setVisible(true);
	}
	
}
