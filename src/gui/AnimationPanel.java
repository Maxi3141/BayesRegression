package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import math.DataManager;

public class AnimationPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener {
	
	private static final long serialVersionUID = 8075460531029437053L;

	DataManager refDataManager;
	
	double[] topLeftCorner = {-1.0, 1.0};
	double[] botRightCorner = {1.0, -1.0};
	
	int[] beginOfMouseEvent = new int[2];
	double[] topLeftOld = topLeftCorner;
	double[] botRightOld = botRightCorner;
	double[] mouseAtStart = new double[2];
	boolean mouseEventIsRunning = false;
	int[] lastMousePosition = new int[2];
	
	int gridTextPadding = 15;
	
	double zoomLevel = 1.0;
	
	int dataPointSize = 15;
	float strokeWidthGrid = 2.0f;
	float strokeWidthOutput = 2.0f;
	boolean useMultithreading = true;
	int numCores = 4;
	int densityResolutionX = 10;
	int densityResolutionY = 1;
	Color[][] priorPredictionColorTable;
	Color[][] posteriorPredictionColorTable;
	
	boolean drawData;
	boolean drawRegression;
	int drawPrior;
	int drawPosterior;
	double[] paramDensityLevels = {0.9, 0.5, 0.2};
	int drawPriorPrediction;
	int drawPosteriorPrediction;
	boolean drawBasisFunctions;
	
	Color mleColor = Color.red;
	Color dataColor = Color.red;
	Color[] priorColor = { new Color(50, 50, 50), new Color(100, 100, 100), new Color(150, 150, 150) };
	Color[] posteriorColor = { new Color(8, 168, 36), new Color(80, 217, 104), new Color(153, 240, 168) };
	Color[] priorPredictionColor = { new Color(225, 196, 125), new Color(225, 170, 28), new Color(225, 150, 0) };
	Color[] postPredictionColor = { new Color(244, 185, 255), new Color(230, 102, 255), new Color(213, 0, 255) };
	
	Action removeDataAction = new AbstractAction() {
		private static final long serialVersionUID = -1090752822702788202L;

		@Override
		public void actionPerformed(ActionEvent e) {
			removeDataPoint();
		}
	};
	
	AnimationPanel(DataManager refDataManagerNew) {
		refDataManager = refDataManagerNew;
		
		setBackground(Color.white);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);
		
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke("R"), "REMOVE_DATA_KEY");
		getActionMap().put("REMOVE_DATA_KEY", removeDataAction);
		
		priorPredictionColorTable = new Color[1][1];
		posteriorPredictionColorTable = new Color[1][1];
		
		drawData = true;
		drawRegression = true;
		drawPrior = 0;
		drawPosterior = 0;
		drawPriorPrediction = 0;
		drawPosteriorPrediction = 0;
		drawBasisFunctions = false;
	}
	
	public void setDrawData(boolean drawDataNew) {
		drawData = drawDataNew;
	}
	
	public void setDrawRegression(boolean drawRegressionNew) {
		drawRegression = drawRegressionNew;
	}
	
	public void setDrawPrior(int drawPriorNew) {
		drawPrior = drawPriorNew;
	}
	
	public void setDrawPosterior(int drawPosteriorNew) {
		drawPosterior = drawPosteriorNew;
	}
	
	public void setDrawPriorPrediction(int drawPriorPredictionNew) {
		drawPriorPrediction = drawPriorPredictionNew;
	}
	
	public void setDrawPostPrediction(int drawPosteriorPredictionNew) {
		drawPosteriorPrediction = drawPosteriorPredictionNew;
	}
	
	public void setDrawBasisFunctions(boolean drawBasisFunctionsNew) {
		drawBasisFunctions = drawBasisFunctionsNew;
	}
	
	/*
	 * Input: Cartesian coordinates x and y
	 * Corresponding location on screen with respect to the panel's pixels
	 */
	int[] getPixelFromNumber(double xCoord, double yCoord) {
		int[] result = new int[2];
		double currentWidth = (double)getWidth();
		double currentHeight = (double)getHeight();
		result[0] = (int)( (xCoord - topLeftCorner[0]) / (botRightCorner[0] - topLeftCorner[0]) * currentWidth );
		result[1] = (int)( (1.0 - (yCoord - botRightCorner[1]) / (topLeftCorner[1] - botRightCorner[1])) * currentHeight );
		return result;
	}
	
	int[] getPixelFromNumber(double[] input) {
		return getPixelFromNumber(input[0], input[1]);
	}
	
	/*
	 * Input: x and y coordinate of the location on screen with respect to the panel's pixels
	 * Output: Corresponding x and y coordinate of the location in Cartesian coordinate system
	 */
	double[] getNumberFromPixel(int xPixel, int yPixel) {
		double[] result = new double[2];
		double currentWidth = (double)getWidth();
		double currentHeight = (double)getHeight();
		double xPixelD = (double)xPixel;
		double yPixelD = (double)yPixel;
		result[0] = (xPixelD / currentWidth) * (botRightCorner[0] - topLeftCorner[0]) + topLeftCorner[0];
		result[1] = (1.0 - yPixelD / currentHeight) * (topLeftCorner[1] - botRightCorner[1]) + botRightCorner[1];
		return result;
	}
	
	double[] getNumberFromPixel(int[] input) {
		return getNumberFromPixel(input[0], input[1]);
	}
	
	/*
	 *  Determine spacing between coordinate grid lines in number
	 */
	double scaleOfCoordinates() {
		double maxDiff = Math.max(Math.abs(topLeftCorner[0] - botRightCorner[0]), Math.abs(topLeftCorner[1] - botRightCorner[1]));
		double scale = Math.log10(maxDiff);
		double gridSpacing = Math.pow(10.0, Math.floor(scale) - 1.0);
		return gridSpacing;
	}
	
	/*
	 * Input: Number
	 * Output: String to display at coordinate axis
	 */
	String gridDisplayNumber(double input) {
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		String result = df.format(input);
		if(result.equals("-0")) {
			result = "0";
		}
		return result;
	}
	
	// Determine botRightCorner and topLeftCorner after zoom level has changed
	void refreshScaling() {
		double centerX = 0.5 * (botRightCorner[0] + topLeftCorner[0]);
		double centerY = 0.5 * (topLeftCorner[1] + botRightCorner[1]);
		double panelRatio = Math.min((double)getWidth(), (double)getHeight()) / Math.max((double)getWidth(), (double)getHeight());
		double transformedZoomLevel = Math.exp(zoomLevel);
		
		botRightCorner[0] = centerX + transformedZoomLevel * (getWidth() > getHeight() ? 1.0 : panelRatio);
		topLeftCorner[0] = centerX - transformedZoomLevel * (getWidth() > getHeight() ? 1.0 : panelRatio);
		botRightCorner[1] = centerY - transformedZoomLevel * (getHeight() > getWidth() ? 1.0 : panelRatio);
		topLeftCorner[1] = centerY + transformedZoomLevel * (getHeight() > getWidth() ? 1.0 : panelRatio);
		
		repaint();
	}
	
	void removeDataPoint() {
		double[] numberMousePosition = getNumberFromPixel(lastMousePosition);
		refDataManager.removeClosestTo(numberMousePosition);
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
	/*	if(refDataManager.getDataList().size() > 0 && drawPosteriorPrediction == 2) {
			paintPosteriorPredictionFull(g);
		} */ 
		
		if(drawPriorPrediction == 2) {
			paintPriorPredictionFull(g);
		}
		
		// Create the coordinate system
		paintCoordinateGrid(g);
		
		// Paint the distribution of the prior
		if(drawPrior != 0) {
			paintPrior(g);
		}
		
		// Paint the distribution of the posterior
		if(drawPosterior != 0) {
			paintPosterior(g);
		}
		
		
		// Draw prior prediction
		if(drawPriorPrediction == 1) {
			paintPriorPredictionSimple(g);
		}
		
		// Draw posterior prediction
		if(refDataManager.getDataList().size() > 0 && drawPosteriorPrediction == 1) {
			paintPosteriorPredictionSimple(g);
		}
		
		//DEBUG
		if(refDataManager.getDataList().size() > 0 && drawPosteriorPrediction == 2) {
			paintPosteriorPredictionFull(g);
		}
		
		// Draw the regression graph
		if(drawRegression) {
			paintRegression(g);
		}
		
		// Draw the data points
		if(drawData) {
			paintDataPoints(g);
		}
		
		// Draw basis function of current regression type
		if(drawBasisFunctions) {
			paintBasisFunctions(g);
		}
	}
	
	void paintRegression(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(mleColor);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(strokeWidthOutput));
		double xOld = getNumberFromPixel(0, 0)[0];
		double yOld = refDataManager.evaluateRegressionAt(xOld);
		for(int i = 1; i < getWidth(); i++) {
			double xNew = getNumberFromPixel(i, 0)[0];
			double yNew = refDataManager.evaluateRegressionAt(xNew);
			int[] pixelOld = getPixelFromNumber(xOld, yOld);
			int[] pixelNew = getPixelFromNumber(xNew, yNew);
			g2.draw(new Line2D.Float(pixelOld[0], pixelOld[1], pixelNew[0], pixelNew[1]));
			xOld = xNew;
			yOld = yNew;
		}
	}
	
	void paintDataPoints(Graphics g) {
		g.setColor(dataColor);
		for(int i = 0; i < refDataManager.getDataList().size(); i++) {
			int[] pixelPosition = getPixelFromNumber(refDataManager.getDataPoint(i));
			g.fillOval(pixelPosition[0] - dataPointSize / 2, pixelPosition[1] - dataPointSize / 2, dataPointSize, dataPointSize);
		}
	}
	
	void paintPrior(Graphics g) {
		if(drawPrior > 2) {
			paintSinglePriorLevel(g, paramDensityLevels[2], priorColor[2]);
		}
		if(drawPrior > 1) {
			paintSinglePriorLevel(g, paramDensityLevels[1], priorColor[1]);
		}
		paintSinglePriorLevel(g, paramDensityLevels[0], priorColor[0]);
	}
	
	void paintPosterior(Graphics g) {
		if(drawPosterior > 2) {
			paintSinglePosteriorLevel(g, paramDensityLevels[2], posteriorColor[2]);
		}
		if(drawPosterior > 1) {
			paintSinglePosteriorLevel(g, paramDensityLevels[1], posteriorColor[1]);
		}
		paintSinglePosteriorLevel(g, paramDensityLevels[0], posteriorColor[0]);
	}
	
	void paintSinglePriorLevel(Graphics g, double level, Color col) {
		g.setColor(col);
		int resolution = 2;
		double xOld = getNumberFromPixel(0, 0)[0];
		double[] yOld = refDataManager.evaluatePriorForPolynomial(level, xOld);
		for(int i = 1; i < getWidth(); i += resolution) {
			double xNew = getNumberFromPixel(i, 0)[0];
			double[] yNew = refDataManager.evaluatePriorForPolynomial(level, xNew);
			int[] pixelBotOld = getPixelFromNumber(xOld, yOld[1]);
			int[] pixelTopOld = getPixelFromNumber(xOld, yOld[0]);
			int[] pixelBotNew = getPixelFromNumber(xNew, yNew[1]);
			int[] pixelTopNew = getPixelFromNumber(xNew, yNew[0]);
			int[] polygonX = {(int)pixelBotOld[0], (int)pixelTopOld[0], (int)pixelTopNew[0], (int)pixelBotNew[0]};
			int[] polygonY = {(int)pixelBotOld[1], (int)pixelTopOld[1], (int)pixelTopNew[1], (int)pixelBotNew[1]};
			g.fillPolygon(polygonX, polygonY, 4);
			xOld = xNew;
			yOld = yNew;
		}
	}
	
	void paintSinglePosteriorLevel(Graphics g, double level, Color col) {
		g.setColor(col);
		int resolution = 2;
		double xOld = getNumberFromPixel(0, 0)[0];
		double[] yOld = refDataManager.evaluatePosteriorForPolynomial(level, xOld);
		for(int i = 1; i < getWidth(); i += resolution) {
			double xNew = getNumberFromPixel(i, 0)[0];
			double[] yNew = refDataManager.evaluatePosteriorForPolynomial(level, xNew);
			int[] pixelBotOld = getPixelFromNumber(xOld, yOld[1]);
			int[] pixelTopOld = getPixelFromNumber(xOld, yOld[0]);
			int[] pixelBotNew = getPixelFromNumber(xNew, yNew[1]);
			int[] pixelTopNew = getPixelFromNumber(xNew, yNew[0]);
			int[] polygonX = {(int)pixelBotOld[0], (int)pixelTopOld[0], (int)pixelTopNew[0], (int)pixelBotNew[0]};
			int[] polygonY = {(int)pixelBotOld[1], (int)pixelTopOld[1], (int)pixelTopNew[1], (int)pixelBotNew[1]};
			g.fillPolygon(polygonX, polygonY, 4);
			xOld = xNew;
			yOld = yNew;
		}
	}
	
	void paintPriorPredictionSimple(Graphics g) {
		int resolution = 1;
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(strokeWidthOutput));
		double xOld = getNumberFromPixel(0, 0)[0];
		double[] yOld = refDataManager.evaluatePriorPredictionForPolynomialSimple(xOld);
		for(int i = 1; i < getWidth(); i += resolution) {
			double xNew = getNumberFromPixel(i, 0)[0];
			double[] yNew = refDataManager.evaluatePriorPredictionForPolynomialSimple(xNew);
			int[] pixelOldMid = getPixelFromNumber(xOld, yOld[0]);
			int[] pixelOldBot = getPixelFromNumber(xOld, yOld[0] - yOld[1]);
			int[] pixelOldTop = getPixelFromNumber(xOld, yOld[0] + yOld[1]);
			int[] pixelNewMid = getPixelFromNumber(xNew, yNew[0]);
			int[] pixelNewBot = getPixelFromNumber(xNew, yNew[0] - yNew[1]);
			int[] pixelNewTop = getPixelFromNumber(xNew, yNew[0] + yNew[1]);
			g2.setColor(priorPredictionColor[2]);
			g2.draw(new Line2D.Float(pixelOldMid[0], pixelOldMid[1], pixelNewMid[0], pixelNewMid[1]));
			g2.setColor(priorPredictionColor[1]);
			g2.draw(new Line2D.Float(pixelOldTop[0], pixelOldTop[1], pixelNewTop[0], pixelNewTop[1]));
			g2.draw(new Line2D.Float(pixelOldBot[0], pixelOldBot[1], pixelNewBot[0], pixelNewBot[1]));
			xOld = xNew;
			yOld = yNew;
		}
	}
	
	void paintPriorPredictionFull(Graphics g) {
		if(useMultithreading) {
			paintPriorPredictionFullMultithreading(g);
		} else {
			paintPriorPredictionFullSinglethreading(g);
		}
	}
	
	void paintPriorPredictionFullMultithreading(Graphics g) {
		int pixelsPerCore = (int)Math.ceil((double)getWidth() / (numCores * densityResolutionX));
		// First generate the colors for all pixels using multithreading and store all values in posteriorPredictionColorTable
		for(int i = 0; i < numCores; i++) {
			final int helperI = i;
			Thread t = new Thread(() -> 
				generatePriorPredictionColorTableSingleCore(helperI * pixelsPerCore, Math.min((helperI+1) * pixelsPerCore, getWidth() / densityResolutionX), helperI));
			t.start();
		}
		for(int i = 0; i < priorPredictionColorTable.length; i++) {
			for(int j = 0; j < priorPredictionColorTable[i].length; j++) {
				g.setColor(priorPredictionColorTable[i][j]);
				g.fillRect(i * densityResolutionX, j * densityResolutionY, densityResolutionX + 1, densityResolutionY + 1);
			}
		}
	}
	
	void generatePriorPredictionColorTableSingleCore(int indexStart, int indexEnd, int threadIndex) {
		for(int i = indexStart; i < indexEnd; i++) {
			int xPixel = i * densityResolutionX;
			double xPos = getNumberFromPixel(i * densityResolutionX, 0)[0];
			double[] currentDensityParams = refDataManager.preparePriorPredictionForRelativeEvaluationExternal(xPos);
			for(int j = 0; j < priorPredictionColorTable[i].length; j++) {
				int yPixel = j * densityResolutionY;
				double yPos = getNumberFromPixel(xPixel, yPixel)[1];
				int alpha = (int)(255.0 * refDataManager.evaluatePriorPredictionForPolynomialFullExternal(yPos, currentDensityParams[0], currentDensityParams[1]));
				int red = priorPredictionColor[2].getRed();
				int green = priorPredictionColor[2].getGreen();
				int blue = priorPredictionColor[2].getBlue();
				priorPredictionColorTable[i][j] = new Color(red, green, blue, alpha);
			}
		}
	}
	
	void paintPriorPredictionFullSinglethreading(Graphics g) {
		for(int i = 0; i < getWidth(); i += densityResolutionX) {
			double xPos = getNumberFromPixel(i, 0)[0];
			refDataManager.preparePriorPredictionForRelativeEvaluation(xPos);
			for(int j = 0; j < getHeight(); j += densityResolutionY) {
				double yPos = getNumberFromPixel(i, j)[1];
				int alpha = (int)(255.0 * refDataManager.evaluatePriorPredictionForPolynomialFull(yPos));
				int red = priorPredictionColor[2].getRed();
				int green = priorPredictionColor[2].getGreen();
				int blue = priorPredictionColor[2].getBlue();
				g.setColor(new Color(red, green, blue, alpha));
				g.fillRect(i, j, densityResolutionX, densityResolutionY);
			}
		}
	}
	
	void paintPosteriorPredictionSimple(Graphics g) {
		int resolution = 1;
		Graphics2D g2 = (Graphics2D) g;
		double xOld = getNumberFromPixel(0, 0)[0];
		double[] yOld = refDataManager.evaluatePosteriorPredictionForPolynomialSimple(xOld);
		for(int i = 1; i < getWidth(); i += resolution) {
			double xNew = getNumberFromPixel(i, 0)[0];
			double[] yNew = refDataManager.evaluatePosteriorPredictionForPolynomialSimple(xNew);
			int[] pixelOldMid = getPixelFromNumber(xOld, yOld[0]);
			int[] pixelOldBot = getPixelFromNumber(xOld, yOld[0] - yOld[1]);
			int[] pixelOldTop = getPixelFromNumber(xOld, yOld[0] + yOld[1]);
			int[] pixelNewMid = getPixelFromNumber(xNew, yNew[0]);
			int[] pixelNewBot = getPixelFromNumber(xNew, yNew[0] - yNew[1]);
			int[] pixelNewTop = getPixelFromNumber(xNew, yNew[0] + yNew[1]);
			g2.setColor(postPredictionColor[2]);
			g2.draw(new Line2D.Float(pixelOldMid[0], pixelOldMid[1], pixelNewMid[0], pixelNewMid[1]));
			g2.setColor(postPredictionColor[1]);
			g2.draw(new Line2D.Float(pixelOldTop[0], pixelOldTop[1], pixelNewTop[0], pixelNewTop[1]));
			g2.draw(new Line2D.Float(pixelOldBot[0], pixelOldBot[1], pixelNewBot[0], pixelNewBot[1]));
			xOld = xNew;
			yOld = yNew;
		}
	}
	
	void paintPosteriorPredictionFull(Graphics g) {
		if(useMultithreading) {
			paintPosteriorPredictionFullMultithreading(g);
		} else {
			paintPosteriorPredictionFullSinglethreading(g);
		}
	}
	
	void paintPosteriorPredictionFullMultithreading(Graphics g) {
		int pixelsPerCore = (int)Math.ceil((double)getWidth() / (numCores * densityResolutionX));
		// First generate the colors for all pixels using multithreading and store all values in posteriorPredictionColorTable
		for(int i = 0; i < numCores; i++) {
			final int helperI = i;
			Thread t = new Thread(() -> 
				generatePosteriorPredictionColorTableSingleCore(helperI * pixelsPerCore, Math.min((helperI+1) * pixelsPerCore, getWidth() / densityResolutionX), helperI));
			t.start();
		}
		for(int i = 0; i < posteriorPredictionColorTable.length; i++) {
			for(int j = 0; j < posteriorPredictionColorTable[i].length; j++) {
				g.setColor(posteriorPredictionColorTable[i][j]);
				g.fillRect(i * densityResolutionX, j * densityResolutionY, densityResolutionX, densityResolutionY);
			}
		}
	}
	
	void paintPosteriorPredictionFullSinglethreading(Graphics g) {
		for(int i = 0; i < getWidth(); i += densityResolutionX) {
			double xPos = getNumberFromPixel(i, 0)[0];
			refDataManager.preparePosteriorPredictionForRelativeEvaluation(xPos);
			for(int j = 0; j < getHeight(); j += densityResolutionY) {
				double yPos = getNumberFromPixel(i, j)[1];
				int alpha = (int)(255.0 * refDataManager.evaluatePosteriorPredictionForPolynomialFull(yPos));
				int red = postPredictionColor[2].getRed();
				int green = postPredictionColor[2].getGreen();
				int blue = postPredictionColor[2].getBlue();
				g.setColor(new Color(red, green, blue, alpha));
				g.fillRect(i, j, densityResolutionX, densityResolutionY);
			}
		}
	}
	
	void generatePosteriorPredictionColorTableSingleCore(int indexStart, int indexEnd, int threadIndex) {
		for(int i = indexStart; i < indexEnd; i++) {
			int xPixel = i * densityResolutionX;
			double xPos = getNumberFromPixel(i * densityResolutionX, 0)[0];
			double[] currentDensityParams = refDataManager.preparePosteriorPredictionForRelativeEvaluationExternal(xPos);
			for(int j = 0; j < posteriorPredictionColorTable[i].length; j++) {
				int yPixel = j * densityResolutionY;
				double yPos = getNumberFromPixel(xPixel, yPixel)[1];
				int alpha = (int)(255.0 * refDataManager.evaluatePosteriorPredictionForPolynomialFullExternal(yPos, currentDensityParams[0], currentDensityParams[1]));
				int red = postPredictionColor[2].getRed();
				int green = postPredictionColor[2].getGreen();
				int blue = postPredictionColor[2].getBlue();
				posteriorPredictionColorTable[i][j] = new Color(red, green, blue, alpha);
			}
		}
	}
	
	void paintCoordinateGrid(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(strokeWidthGrid));
	        
		//draw coordinate grid
		double gridSpacing = scaleOfCoordinates();
		double horizontalGridHeight = Math.floor(botRightCorner[1] / gridSpacing) * gridSpacing;
		do {
			int[] pixelPos = getPixelFromNumber(0.0, horizontalGridHeight);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(new Line2D.Float(0, pixelPos[1], getWidth(), pixelPos[1]));
			if(pixelPos[0] < 0) {
				pixelPos[0] = 0;
			}
			if(pixelPos[0] > getWidth() - gridTextPadding) {
				pixelPos[0] = getWidth() - gridTextPadding;
			}
			g2.setColor(Color.BLACK);
			g2.drawString(gridDisplayNumber(horizontalGridHeight), pixelPos[0] + 1, pixelPos[1] + 11);
			horizontalGridHeight += gridSpacing;
		}while(horizontalGridHeight < topLeftCorner[1]);
		
		double verticalGridWidth = Math.floor(topLeftCorner[0] / gridSpacing) * gridSpacing;
		do {
			int[] pixelPos = getPixelFromNumber(verticalGridWidth, 0.0);
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(pixelPos[0], 0, pixelPos[0], getHeight());
			if(pixelPos[1] < 0) {
				pixelPos[1] = 0;
			}
			if(pixelPos[1] > getHeight() - gridTextPadding) {
				pixelPos[1] = getHeight() - gridTextPadding;
			}
			g2.setColor(Color.BLACK);
			g2.drawString(gridDisplayNumber(verticalGridWidth), pixelPos[0] + 1, pixelPos[1] + 11);
			verticalGridWidth += gridSpacing;
		}while(verticalGridWidth < botRightCorner[0]);
		
		//draw main axis
		g2.setColor(Color.black);
		int[] originPixel = getPixelFromNumber(0.0, 0.0);
		g2.drawLine(0, originPixel[1], getWidth(), originPixel[1]);
		g2.drawLine(originPixel[0], 0, originPixel[0], getHeight());
	}
	
	void paintBasisFunctions(Graphics g) {
		int resolution = 2;
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(strokeWidthOutput));
		double x = getNumberFromPixel(0, 0)[0];
		double[] y = refDataManager.evaluateBasisFunctionsAt(x);
		Color[] basisFunctionColors = new Color[y.length];
		for(int i = 0; i < y.length; i++) {
			basisFunctionColors[i] = Color.getHSBColor((float)i / (float)y.length, 1.0f, 0.7f);
		}
		double[] yOld = y;
		for(int i = resolution; i < getWidth(); i += resolution) {
			yOld = y.clone();
			x = getNumberFromPixel(i, 0)[0];
			y = refDataManager.evaluateBasisFunctionsAt(x);
			for(int j = 0; j < y.length; j++) {
				g2.setColor(basisFunctionColors[j]);
				int yOldPixel = getPixelFromNumber(x, yOld[j])[1];
				int yPixel = getPixelFromNumber(x, y[j])[1];
				g2.drawLine(i - resolution, yOldPixel, i, yPixel);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if(SwingUtilities.isRightMouseButton(me)) {
			double[] eventCoords = getNumberFromPixel(me.getX(), me.getY());
			double xDiff = mouseAtStart[0] - eventCoords[0];
			double yDiff = mouseAtStart[1] - eventCoords[1];
			topLeftCorner[0] = topLeftOld[0] + xDiff;
			topLeftCorner[1] = topLeftOld[1] + yDiff;
			botRightCorner[0] = botRightOld[0] + xDiff;
			botRightCorner[1] = botRightOld[1] + yDiff;
			repaint();
		}else if(SwingUtilities.isLeftMouseButton(me)) {
			double[] eventCoords = getNumberFromPixel(me.getX(), me.getY());
			refDataManager.moveClosestToPosition(eventCoords[0], eventCoords[1]);
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		lastMousePosition[0] = me.getX();
		lastMousePosition[1] = me.getY();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		double[] numberPosition = getNumberFromPixel(me.getX(), me.getY());
		refDataManager.addData(numberPosition);
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent me) {
		if(!mouseEventIsRunning) {
			topLeftOld = topLeftCorner;
			botRightOld = botRightCorner;
			mouseAtStart = getNumberFromPixel(me.getX(), me.getY());
			mouseEventIsRunning = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		mouseEventIsRunning = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
		zoomLevel += mwe.getPreciseWheelRotation() / 10.0;
		refreshScaling();
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		refreshScaling();
		priorPredictionColorTable = new Color[getWidth() / densityResolutionX][getHeight() / densityResolutionY];
		posteriorPredictionColorTable = new Color[getWidth() / densityResolutionX][getHeight() / densityResolutionY];
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}





































