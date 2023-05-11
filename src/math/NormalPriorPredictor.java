package math;

import math.linAlg.Matrix;

public class NormalPriorPredictor {
	
	Matrix priorMu;
	Matrix priorSigma;
	double regressionSigmaSq;
	
	double tmpMean = 0;
	double tmpVariance = 0;
	
	NormalPriorPredictor() {
		priorMu = new Matrix(2,1);
		priorMu.setZero();
		priorSigma = new Matrix(2,2);
		regressionSigmaSq = 1.0;
	}
	
	public void setPriorParameters(Matrix priorMuNew, Matrix priorSigmaNew) {
		priorMu = priorMuNew;
		priorSigma = priorSigmaNew;
	//	priorSigma.updateDeterminante();
	}
	
	public int getOrder() {
		return priorMu.getNumRows();
	}
	
	public void setRegressionSigmaSquared(double regressionSigmaSqNew) {
		regressionSigmaSq = regressionSigmaSqNew;
		System.out.println("Set regression sigma sq set to " +  regressionSigmaSq + " in NormalPriorPredictor.");
	}
	
	public double[] evaluateTotal(Matrix x) {
		double[] result = new double[2];
		result[0] = x.getTranspose().getMultiplication(priorMu).getElement(0, 0);
		result[1] = x.getTranspose().getMultiplication(priorSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
		return result;
	}
	
	public void prepareRelativeEvaluation(Matrix x) {
		tmpMean = x.getTranspose().getMultiplication(priorMu).getElement(0, 0);
		tmpVariance = x.getTranspose().getMultiplication(priorSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
	}
	
	public double[] prepareRelativeEvaluationExternal(Matrix x) {
		double[] result = new double[2];
		result[0] = x.getTranspose().getMultiplication(priorMu).getElement(0, 0);
		result[1] = x.getTranspose().getMultiplication(priorSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
		return result;
	}
	
	public double evaluateRelative(double y) {
		return Math.exp(-0.5 / tmpVariance * Math.pow((y - tmpMean), 2.0));
	}
	
	public double evaluateRelativeExternal(double y, double extMean, double extVariance) {
		return Math.exp(-0.5 / extVariance * Math.pow((y - extMean), 2.0));
	}
	
}
