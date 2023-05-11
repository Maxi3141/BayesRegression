package math;

import math.linAlg.Matrix;

public interface GeneralPrior {
	
	int getPolynomialDegree();
	double evaluateDensity(Matrix w);
	double constrainedMax(double level, Matrix x);
	double constrainedMin(double level, Matrix x);
	double[] constrainedOptima(double level, Matrix x);
	
}
