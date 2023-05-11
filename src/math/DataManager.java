package math;

import java.util.ArrayList;

import gui.AnimationPanel;
import math.linAlg.Matrix;

public class DataManager {
	
	ArrayList<double[]> dataList;
	
	GeneralRegression regAlgorithm;
	
	NormalPrior normPrior;
	NormalPosterior normPosterior;
	
	NormalPriorPredictor priorPredictor;
	NormalPosteriorPredictor postPredictor;
	
	AnimationPanel refAniPanel;
	
	public DataManager() {
		dataList = new ArrayList<double[]>();
		regAlgorithm = new GeneralRegression(1, this);
		normPrior = new NormalPrior(new Matrix("0;0"), new Matrix("1,0;0,1"));
		normPosterior = new NormalPosterior(regAlgorithm);
		normPosterior.setPriorParameters(normPrior.getMu(), normPrior.getSigma());
		priorPredictor = new NormalPriorPredictor();
		postPredictor = new NormalPosteriorPredictor(regAlgorithm);
	}
	
	public void setAniPanel(AnimationPanel refAniPanelNew) {
		refAniPanel = refAniPanelNew;
	}
	
	public void setToNormalPrior(Matrix mu, Matrix sigma) {
		normPrior = new NormalPrior(mu, sigma);
		normPosterior.setPriorParameters(mu, sigma);
		priorPredictor.setPriorParameters(mu, sigma);
		postPredictor.setPriorParameters(mu, sigma);
	}
	
	public void setRegressionOrder(int order) {
		regAlgorithm.setOrder(order);
		updateRegression();
		
	}
	
	public void setRegressionSigmaSq(double sigmaSq) {
		normPosterior.setRegressionSigmaSquared(sigmaSq);
		priorPredictor.setRegressionSigmaSquared(sigmaSq);
		postPredictor.setRegressionSigmaSquared(sigmaSq);
	}
	
	public void invokeCompleteUpdate() {
		updateRegression();
		updatePolynomialNormalPosterior();
		updatePolynomialPosteriorPredictor();
		// Stuff missing?
	}
	
	void updateRegression() {
		regAlgorithm.updateWeights(dataList);
	}
	
	void updatePolynomialNormalPosterior() {
		normPosterior.setRegressionData(dataList, regAlgorithm.getOrder());
	}
	
	void updatePolynomialPosteriorPredictor() {
		postPredictor.setRegressionData(dataList, regAlgorithm.getOrder());
	}
	
	public double evaluateRegressionAt(double x) {
		return regAlgorithm.evaluate(x);
	}
	
	Matrix transformX(double x, int order) {
		Matrix phi = new Matrix(order , 1);
		phi.setColumn(0, regAlgorithm.transformX(x, order));
		return phi;
	}
	
	public double[] evaluatePriorForPolynomial(double level, double x) {
		Matrix phi = transformX(x, normPrior.getOrder());
		return normPrior.constrainedOptima(level, phi);
	}
	
	public double[] evaluatePosteriorForPolynomial(double level, double x) {
		return normPosterior.constrainedOptima(level, transformX(x, normPosterior.getOrder()));
	}
	
	//Input: Point on x axis
	//Output: Mean (return[0]) and variance (return[1]) of distribution at that x 
	public double[] evaluatePriorPredictionForPolynomialSimple(double x) {
		return priorPredictor.evaluateTotal(transformX(x, priorPredictor.getOrder()));
	}
	
	public void preparePriorPredictionForRelativeEvaluation(double x) {
		priorPredictor.prepareRelativeEvaluation(transformX(x, priorPredictor.getOrder()));
	}
	
	public double[] preparePriorPredictionForRelativeEvaluationExternal(double x) {
		return priorPredictor.prepareRelativeEvaluationExternal(transformX(x, priorPredictor.getOrder()));
	}
	
	public float evaluatePriorPredictionForPolynomialFull(double y) {
		return (float)priorPredictor.evaluateRelative(y);
	}
	
	public float evaluatePriorPredictionForPolynomialFullExternal(double y, double extMean, double extVariance) {
		return (float)priorPredictor.evaluateRelativeExternal(y, extMean, extVariance);
	}
	
	public double[] evaluatePosteriorPredictionForPolynomialSimple(double x) {
		return postPredictor.evaluateTotal(transformX(x, postPredictor.getOrder()));
	}
	
	public void preparePosteriorPredictionForRelativeEvaluation(double x) {
		postPredictor.prepareRelativeEvaluation(transformX(x, postPredictor.getOrder()));
	}
	
	public double[] preparePosteriorPredictionForRelativeEvaluationExternal(double x) {
		return postPredictor.prepareRelativeEvaluationExternal(transformX(x, postPredictor.getOrder()));
	}
	
	public float evaluatePosteriorPredictionForPolynomialFull(double y) {
		return (float)postPredictor.evaluateRelative(y);
	}
	
	public float evaluatePosteriorPredictionForPolynomialFullExternal(double y, double extMean, double extVariance) {
		return (float)postPredictor.evaluteRelativeExternal(y, extMean, extVariance);
	}
	
	public ArrayList<double[]> getDataList() {
		return dataList;
	}
	public double[] getDataPoint(int index) {
		return dataList.get(index);
	}
	
	void addData(double x, double y) {
		double[] newDataPoint = {x , y};
		dataList.add(newDataPoint);
		updateRegression();
		updatePolynomialNormalPosterior();
		updatePolynomialPosteriorPredictor();
	}
	
	public void addData(double[] newDataPoint) {
		dataList.add(newDataPoint);
		updateRegression();
		updatePolynomialNormalPosterior();
		updatePolynomialPosteriorPredictor();
	}
	
	public void removeClosestTo(double x, double y) {
		int index = indexOfClosest(x, y);
		if(index > -1) {
			dataList.remove(index);
			updateRegression();
			updatePolynomialNormalPosterior();
			updatePolynomialPosteriorPredictor();
		}
	}
	
	public void removeClosestTo(double[] position) {
		removeClosestTo(position[0], position[1]);
	}
	
	public void moveClosestToPosition(double x, double y) {
		int index = indexOfClosest(x, y);
		if(index > -1) {
			dataList.get(index)[0] = x;
			dataList.get(index)[1] = y;
			updateRegression();
			updatePolynomialNormalPosterior();
			updatePolynomialPosteriorPredictor();
		}
	}
	
	int indexOfClosest(double x, double y) {
		int result = -1;
		if(dataList.size() != 0) {
			result = 0;
			double currentMinimum = Math.pow(dataList.get(0)[0] - x, 2.0) + Math.pow(dataList.get(0)[1] - y, 2.0);
			for(int i = 1; i < dataList.size(); i++) {
				double newMinimum = Math.pow(dataList.get(i)[0] - x, 2.0) + Math.pow(dataList.get(i)[1] - y, 2.0);
				if(newMinimum < currentMinimum) {
					currentMinimum = newMinimum;
					result = i;
				}
			}
		}
		return result;
	}
	
	public void setRegressionType(RegressionTypes typeNew) {
		regAlgorithm.setRegressionType(typeNew);
	}
	
	void repaintAnimation() {
		refAniPanel.repaint();
	}
	
	public double[] evaluateBasisFunctionsAt(double x) {
		return regAlgorithm.evaluateBasisFunctionsAt(x);
	}
	
}
