package math;

import java.util.ArrayList;

import math.linAlg.Matrix;
import math.linAlg.Solvers;

public class NormalPosteriorPredictor {
	
	Matrix priorMu;
	Matrix priorSigma;
	double regressionSigmaSq;
	Matrix regressionPhi;
	Matrix regressionY;
	
	Matrix priorSigmaInv;
	Matrix postMu;
	Matrix postSigma;
	
	double tmpMean = 0;
	double tmpVariance = 0;
	
	GeneralRegression refRegression;
	
	NormalPosteriorPredictor(GeneralRegression refRegressionNew) {
		priorMu = new Matrix(1,1);
		priorSigma = new Matrix(1,1);
		regressionSigmaSq = 1.0;
		regressionPhi = new Matrix(1,1);
		regressionY = new Matrix(1,1);
		
		priorSigmaInv = new Matrix(1,1);
		postMu = new Matrix(1,1);
		postSigma = new Matrix(1,1);
		
		refRegression = refRegressionNew;
	}
	
	public int getOrder() {
		return postMu.getNumRows();
	}
	
	public void setPriorParameters(Matrix priorMuNew, Matrix priorSigmaNew) {
		priorMu = priorMuNew;
		priorSigma = priorSigmaNew;
		priorSigmaInv = Solvers.invertMatrix(priorSigma);
		updatePosteriorParameters();
	}
	
	public void setRegressionData(Matrix regressionPhiNew, Matrix regressionYNew) {
		regressionPhi = regressionPhiNew;
		regressionY = regressionYNew;
		updatePosteriorParameters();
	}
	
	public void setRegressionData(ArrayList<double[]> rawData, int polynomialOrder) {
		regressionPhi = new Matrix(rawData.size(), polynomialOrder + 1);
		regressionY = new Matrix(rawData.size(), 1);
		for(int i = 0; i < rawData.size(); i++) {
			regressionPhi.setRow(i, refRegression.transformX(rawData.get(i)[0], polynomialOrder + 1));
			regressionY.setElement(i, 0, rawData.get(i)[1]);
		}
		updatePosteriorParameters();
	}
	
	public void setRegressionSigmaSquared(double sigmaSqNew) {
		regressionSigmaSq = sigmaSqNew;
		updatePosteriorParameters();
	}
	
	void updatePosteriorParameters() {
		Matrix postSigmaInv = priorSigmaInv.getAddMatrix(regressionPhi.getTranspose().getMultiplication(regressionPhi).getScaleMultiplication(Math.pow(regressionSigmaSq, -1.0)));
		postSigmaInv.updateDeterminante();
		postSigma = Solvers.invertMatrix(postSigmaInv);
		postSigma.updateDeterminante();
		postMu = postSigma.getMultiplication(priorSigmaInv.getMultiplication(priorMu).getAddMatrix(regressionPhi.getTranspose().getMultiplication(regressionY).getScaleMultiplication(Math.pow(regressionSigmaSq, -1.0))));
	}
	
	public double[] evaluateTotal(Matrix x) {
		double[] result = new double[2];
		result[0] = x.getTranspose().getMultiplication(postMu).getElement(0, 0);
		result[1] = x.getTranspose().getMultiplication(postSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
		return result;
	}
	
	public void prepareRelativeEvaluation(Matrix x) {
		tmpMean = x.getTranspose().getMultiplication(postMu).getElement(0, 0);
		tmpVariance = x.getTranspose().getMultiplication(postSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
	}
	
	public double[] prepareRelativeEvaluationExternal(Matrix x) {
		double[] result = new double[2];
		result[0] = x.getTranspose().getMultiplication(postMu).getElement(0, 0);
		result[1] = x.getTranspose().getMultiplication(postSigma).getMultiplication(x).getElement(0, 0) + regressionSigmaSq;
		return result;
	}
	
	public double evaluateRelative(double y) {
		return Math.exp(-0.5 / tmpVariance * Math.pow((y - tmpMean), 2.0));
	}
	
	public double evaluteRelativeExternal(double y, double extMean, double extVariance) {
		return Math.exp(-0.5 / extVariance * Math.pow((y - extMean), 2.0));
	}
	
}
