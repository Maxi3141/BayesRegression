package math;

import math.linAlg.LinAlgBasics;
import math.linAlg.Matrix;
import math.linAlg.Solvers;

public class NormalPrior implements GeneralPrior {
	
	Matrix mu;
	Matrix sigma;
	Matrix invSigma;
	
	public NormalPrior() {
		
	}
	
	public NormalPrior(Matrix muNew, Matrix sigmaNew) {
		setMatrices(muNew, sigmaNew);
	}
	
	@Override
	public int getPolynomialDegree() {
		return mu.getNumRows();
	}
	
	public Matrix getMu() {
		return mu;
	}
	
	public Matrix getSigma() {
		return sigma;
	}
	
	public void setMatrices(Matrix muNew, Matrix sigmaNew) {
		mu = new Matrix( muNew );
		sigma = new Matrix( sigmaNew );
	/*	System.out.println("mu, sigma = ");
		mu.debugPrint();
		sigma.debugPrint();	*/
		invSigma = Solvers.invertMatrix( sigma );
		sigma.updateDeterminante();
	}
	
	public int getOrder() {
		return mu.getNumRows();
	}
	
	@Override
	public double evaluateDensity(Matrix w) {
		double normalizer = Math.pow(Math.pow(2*Math.PI, mu.getNumRows()) * LinAlgBasics.getDeterminante(sigma), -0.5);
		Matrix difference = mu.getAddMatrix(w.getScaleMultiplication(-1.0));
		Matrix invertSigma = Solvers.invertMatrix(sigma).getScaleMultiplication(-0.5);
		double deviation = Math.exp(difference.getTranspose().getMultiplication(invertSigma).getMultiplication(difference).getElement(0, 0));
		return normalizer * deviation;
	}
	
	double maxValue() {
		return Math.pow(Math.pow(2*Math.PI, mu.getNumRows()) * LinAlgBasics.getDeterminante(sigma), -0.5);
	}
	
/*	private double[] getLagrangeLambda(double level, Matrix x) {
		double[] result = new double[2];
		
		double a = 0.25 * mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0);
		double b = 0.5 * mu.getTranspose().getMultiplication(x).getElement(0, 0) - mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0);
		double c = 0.25 * x.getTranspose().getMultiplication(sigma).getMultiplication(x).getElement(0, 0)
			   - x.getTranspose().getMultiplication(mu).getElement(0, 0) + mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0)
	//		   + 2.0 * Math.log(level * Math.sqrt(Math.pow(2*Math.PI, mu.getNumRows()) * LinAlgBasics.getDeterminante(sigma)));
			   - 2.0 * Math.log(level * Math.sqrt(Math.pow(2*Math.PI, mu.getNumRows()) * LinAlgBasics.getDeterminante(sigma)));
		if(a == 0) {
			result[0] = (-b + Math.sqrt(Math.pow(b, 2.0) - 4.0 * a * c)) / (2.0 * a);
			result[1] = (-b - Math.sqrt(Math.pow(b, 2.0) - 4.0 * a * c)) / (2.0 * a);
		} else {
			result[0] = -c / b;
			result[1] = -c / b;
		}
			
		return result;
	} */
	
/*	private double[] getLagrangeLambdaMax(double level, Matrix x) {
		double muInvSigmaMu = mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0);
		double xMu = x.getTranspose().getMultiplication(mu).getElement(0, 0);
		double xSigmaX = x.getTranspose().getMultiplication(sigma).getMultiplication(x).getElement(0, 0);
		double normalDensityStuff = 2.0 * Math.log(level * Math.sqrt(Math.pow(2.0 * Math.PI, mu.getNumRows()) * sigma.getDeterminante()));
		
		double a = muInvSigmaMu;
		double b = -2.0 * muInvSigmaMu;
		double c = -xMu + muInvSigmaMu + normalDensityStuff;
		double d = xMu;
		double e = 0.25 * xSigmaX;
		
		return Solvers.solveOrderFourPolynomial(a, b, c, d, e);
	}
	
	private double[] getLagrangeLambdaMin(double level, Matrix x) {
		double muInvSigmaMu = mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0);
		double xMu = x.getTranspose().getMultiplication(mu).getElement(0, 0);
		double xSigmaX = x.getTranspose().getMultiplication(sigma).getMultiplication(x).getElement(0, 0);
		double normalDensityStuff = 2.0 * Math.log(level * Math.sqrt(Math.pow(2.0 * Math.PI, mu.getNumRows()) * sigma.getDeterminante()));
		
		double a = muInvSigmaMu;
		double b = -2.0 * muInvSigmaMu;
		double c = xMu + muInvSigmaMu + normalDensityStuff;
		double d = -xMu;
		double e = 0.25 * xSigmaX;
		
		return Solvers.solveOrderFourPolynomial(a, b, c, d, e);
	} */
	
	private double[] getLagrangeLambda(double level, Matrix x) {
		double muMu = mu.getTranspose().getMultiplication(mu).getElement(0, 0);
		double muInvSigmaMu = mu.getTranspose().getMultiplication(invSigma).getMultiplication(mu).getElement(0, 0);
		double normalDensityStuff = 2.0 * Math.log(level * Math.sqrt(Math.pow(2.0 * Math.PI, mu.getNumRows()) * sigma.getDeterminante()));
		double xSigmaX = x.getTranspose().getMultiplication(sigma).getMultiplication(x).getElement(0, 0);
		
		double[] result = new double[2];
		result[0] = 0.5 * Math.sqrt( xSigmaX / ( -muMu + muInvSigmaMu - normalDensityStuff ));
		result[1] = - result[0];
		return result;
	}
	
	@Override
	public double constrainedMax(double relativeLevel, Matrix x) {
		double level = relativeLevel * maxValue();
	//	double[] lambda = getLagrangeLambdaMax(level, x);
		double[] lambda = getLagrangeLambda(level, x);
		double maximizers[] = new double[lambda.length];
		for(int i = 0; i < lambda.length; i++) {
	//		Matrix omega = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[i])).getAddMatrix(mu.getScaleMultiplication(lambda[i]));
			Matrix omega = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[i])).getAddMatrix(mu);
			maximizers[i] = omega.getTranspose().getMultiplication(x).getElement(0, 0);
		}
		
		double result = maximizers[0];
		for(int i = 1; i < maximizers.length; i++) {
			if(maximizers[i] > result) {
				result = maximizers[i];
			}
		}
		
		return result;
	}
	
	@Override
	public double constrainedMin(double relativeLevel, Matrix x) {
		double level = relativeLevel * maxValue();
	//	double[] lambda = getLagrangeLambdaMin(level, x);
		double[] lambda = getLagrangeLambda(level, x);
		double minimizers[] = new double[lambda.length];
		for(int i = 0; i < lambda.length; i++) {
	//		Matrix omega = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[i])).getAddMatrix(mu.getScaleMultiplication(lambda[i]));
			Matrix omega = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[i])).getAddMatrix(mu);
			minimizers[i] = omega.getTranspose().getMultiplication(x).getElement(0, 0);
		}
		
		double result = minimizers[0];
		for(int i = 1; i < minimizers.length; i++) {
			if(minimizers[i] < result) {
				result = minimizers[i];
			}
		}
		
		return result;
	}
	
	@Override
	public double[] constrainedOptima(double relativeLevel, Matrix x) {
		double level = relativeLevel * maxValue();
		double[] lambda = getLagrangeLambda(level, x);
		double[] optima = new double[2];
		Matrix omega0 = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[0])).getAddMatrix(mu);
		Matrix omega1 = sigma.getMultiplication(x).getScaleMultiplication(-1.0/(2.0*lambda[1])).getAddMatrix(mu);
		optima[0] = omega0.getTranspose().getMultiplication(x).getElement(0, 0);
		optima[1] = omega1.getTranspose().getMultiplication(x).getElement(0, 0);
		double[] result = { Math.min(optima[0], optima[1]), Math.max(optima[0], optima[1]) };
		return result;
	}
}
