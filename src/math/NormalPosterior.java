package math;

import java.util.ArrayList;

import math.linAlg.LinAlgBasics;
import math.linAlg.Matrix;
import math.linAlg.Solvers;

public class NormalPosterior {
	
	Matrix priorMu;
	Matrix priorSigma;
	double regressionSigmaSq;
	Matrix regressionPhi;
	Matrix regressionY;
	
	Matrix priorSigmaInv;
	Matrix postMu;
	Matrix postSigma;
	Matrix postSigmaInv;
	
	GeneralRegression refRegression;
	
	public NormalPosterior(GeneralRegression refRegressionNew) {
		priorMu = new Matrix(1,1);
		priorSigma = new Matrix(1,1);
		regressionSigmaSq = 1.0;
		regressionPhi = new Matrix(1,1);
		regressionY = new Matrix(1,1);
		
		priorSigmaInv = new Matrix(1,1);
		postMu = new Matrix(1,1);
		postSigma = new Matrix(1,1);
		postSigmaInv = new Matrix(1,1);
		
		refRegression = refRegressionNew;
	}
	
	public int getOrder() {
		return postMu.getNumRows();
	}
	
	public void setPriorParameters(Matrix priorMuNew, Matrix priorSigmaNew) {
		priorMu = priorMuNew;
		priorSigma = priorSigmaNew;
		priorSigmaInv = Solvers.invertMatrix(priorSigma);
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
		postSigmaInv = priorSigmaInv.getAddMatrix(regressionPhi.getTranspose().getMultiplication(regressionPhi).getScaleMultiplication(Math.pow(regressionSigmaSq, -1.0)));
		postSigmaInv.updateDeterminante();
		postSigma = Solvers.invertMatrix(postSigmaInv);
		postSigma.updateDeterminante();
		postMu = postSigma.getMultiplication(priorSigmaInv.getMultiplication(priorMu).getAddMatrix(regressionPhi.getTranspose().getMultiplication(regressionY).getScaleMultiplication(Math.pow(regressionSigmaSq, -1.0))));
	}
	
	public int getPolynomialDegree() {
		return priorMu.getNumRows();
	}
	
	double maxValue() {
		return Math.pow(Math.pow(2*Math.PI, postMu.getNumRows()) * LinAlgBasics.getDeterminante(postSigma), -0.5);
	}
	
	private double[] getLagrangeLambda(double level, Matrix x) {
		double muMu = postMu.getTranspose().getMultiplication(postMu).getElement(0, 0);
		double muInvSigmaMu = postMu.getTranspose().getMultiplication(postSigmaInv).getMultiplication(postMu).getElement(0, 0);
		double normalDensityStuff = 2.0 * Math.log(level * Math.sqrt(Math.pow(2.0 * Math.PI, postMu.getNumRows()) * postSigma.getDeterminante()));
		double xSigmaX = x.getTranspose().getMultiplication(postSigma).getMultiplication(x).getElement(0, 0);
		
		double[] result = new double[2];
		result[0] = 0.5 * Math.sqrt( xSigmaX / ( -muMu + muInvSigmaMu - normalDensityStuff ));
		result[1] = - result[0];
		return result;
	}
	
	public double[] constrainedOptima(double relativeLevel, Matrix x) {
		double level = relativeLevel * maxValue();
		double[] lambda = getLagrangeLambda(level, x);
		double[] optima = new double[2];
		Matrix omega0 = postSigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[0])).getAddMatrix(postMu);
		Matrix omega1 = postSigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[1])).getAddMatrix(postMu);
		optima[0] = omega0.getTranspose().getMultiplication(x).getElement(0, 0);
		optima[1] = omega1.getTranspose().getMultiplication(x).getElement(0, 0);
		double[] result = { Math.min(optima[0], optima[1]), Math.max(optima[0], optima[1]) };
		return result;
	}
	
}
