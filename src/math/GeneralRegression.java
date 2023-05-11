package math;

import java.util.ArrayList;

import math.linAlg.Matrix;
import math.linAlg.Solvers;

public class GeneralRegression {
	
	int order;
	double[] weights;
	Matrix dataMatrix;
	
	RegressionTypes type;
	
	DataManager refDataManager;
	
	GeneralRegression(int orderNew, DataManager refDataManagerNew) {
		type = RegressionTypes.POLYNOMIAL;
		setOrder(orderNew);
		refDataManager = refDataManagerNew;
	}
	
	public void setOrder(int orderNew) {
		order = orderNew;
		weights = new double[order];
	}
	
	public int getOrder() {
		return order;
	}
	
	public double[] transformX(double x) {
		return transformX(x, order);
	}
	
	public double[] transformX(double x, int customOrder) {
		switch(type) {
			case POLYNOMIAL:
				return transformXPolynomial(x, customOrder);
			case COSINE:
				return transformXCosine(x, customOrder);
			case GAUSSIAN:
				return transformXGaussian(x, customOrder);
			case SIGMOID:
				return transformXSigmoid(x, customOrder);
			default:
				return transformXPolynomial(x, customOrder);
		}
	}
	
	public void setRegressionType( RegressionTypes typeNew) {
		type = typeNew;
		refDataManager.invokeCompleteUpdate();
		refDataManager.repaintAnimation();
	}
	
	double[] transformXPolynomial(double x, int customOrder) {
		double[] phi = new double[customOrder];
		for(int i = 0; i < customOrder; i++) {
			phi[i] = Math.pow(x, (double)i);
		}
		return phi;
	}
	
	double[] transformXCosine(double x, int customOrder) {
		double[] phi = new double[customOrder];
		for(int i = 0; i < customOrder; i++) {
			phi[i] = Math.cos(Math.PI * (double)i * x);
		}
		return phi;
	}
	
	double[] transformXGaussian(double x, int customOrder) {
		double[] phi = new double[customOrder];
		phi[0] = 1.0;
		for(int i = 1; i < customOrder; i++) {
			double mean = Math.floor(((double)i) / 2.0) * Math.pow(-1.0, (double)i - 1.0);
			phi[i] = Math.exp(-Math.pow(x - mean, 2.0));
		}
		return phi;
	}
	
	double[] transformXSigmoid(double x, int customOrder) {
		double[] phi = new double[customOrder];
		phi[0] = 1.0;
		for(int i = 1; i < customOrder; i++) {
			double mean = Math.floor(((double)i) / 2.0) * Math.pow(-1.0, (double)i - 1.0);
			phi[i] = 1.0 / (1.0 + Math.exp(mean - x));
		}
		return phi;
	}
	
	public void generateDataMatrix(ArrayList<double[]> rawData) {
		dataMatrix = new Matrix(rawData.size(), order + 1);
		for(int i = 0; i < rawData.size(); i++) {
			double[] transformedData = transformX(rawData.get(i)[0], order + 1);
			dataMatrix.setRow(i, transformedData);
		}
	}
	
	public void generateWeights(ArrayList<double[]> rawData) {
		Matrix y = new Matrix(rawData.size(), 1);
		for(int i = 0; i < y.getNumRows(); i++) {
			y.setElement(i, 0, rawData.get(i)[1]);
		}
		Matrix A = dataMatrix.getTranspose().getMultiplication(dataMatrix);
		Matrix b = dataMatrix.getTranspose().getMultiplication(y);
		
		weights = Solvers.MINRES(A, b).getContentAsVector();
	}

	public void updateWeights(ArrayList<double[]> data) {
		generateDataMatrix(data);
		generateWeights(data);
	}

	public double evaluate(double x) {
		double y = 0;
		double[] phi = transformX(x, weights.length);
		for(int i = 0; i < weights.length; i++) {
			y += weights[i] * phi[i];
		}
		return y;
	}
	
	public double[] evaluateBasisFunctionsAt(double x) {
		return transformX(x, order + 1);
	}
	
}
