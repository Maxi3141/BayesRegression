package gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import math.DataManager;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = -8196795869145497676L;

	// aniPanel controlls everything that is connected to the animation and its interaction with the user
	AnimationPanel aniPanel;
	
	// dataManager controlls the data and coordinates all mathematical computing
	DataManager dataManager;
	
	// settings for the regression
	SettingsFrame settingsFrame;
	
	// setting for the animation
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
