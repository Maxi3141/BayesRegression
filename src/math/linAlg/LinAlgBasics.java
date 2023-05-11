package math.linAlg;

public final class LinAlgBasics {
	
	public static Matrix getUnitVector(int length, int unit) {
		Matrix answer = new Matrix(length, 1);
		answer.setZero();
		answer.setElement(unit-1, 0, 1);
		return answer;
	}
	
	public static double getScalarProduct(Matrix inVec0, Matrix inVec1) {
		if(inVec0.getNumCols() != 1 || inVec1.getNumCols() != 1 || inVec1.getNumRows() != inVec0.getNumRows()) {
			return 1;
		}else {
			double answer = 0;
			for(int i = 0; i < inVec0.getNumRows(); i++) {
				answer = answer +  inVec0.getElement(i, 0) * inVec1.getElement(i, 0);
			}
			return answer;
		}
	}
	
	public static double getVectorPNorm(Matrix inVec, double p) {
		if(inVec.getNumCols() > 1) {
			return 1;
		}else {
			double answerP = 0;
			for(int i = 0; i < inVec.getNumRows(); i++) {
				answerP += Math.pow(inVec.getElement(i, 0), p);
			}
			return Math.pow(answerP, 1/p);
		}
	}
	
	public static Matrix invertLMatrix(Matrix inMat) {
		Matrix answer = new Matrix(inMat.getNumRows(), inMat.getNumCols());
		
		for(int row = 0; row < answer.getNumRows(); row++) {
			
			answer.setElement(row, row, 1/inMat.getElement(row, row));
			
			for(int col = 0; col < row; col++) {
				double tmpVal = 0;
				for(int i = col; i < row; i++) {
					tmpVal += inMat.getElement(row, i) * answer.getElement(i, col);
				}
				answer.setElement(row, col, (-1)*tmpVal/inMat.getElement(row, row));
			}
		}
		
		return answer;
	}
	
	public static Matrix invertUMatrix(Matrix inMat) {
		Matrix answer = new Matrix(inMat.getNumRows(), inMat.getNumCols());
		
		for(int row = answer.getNumRows()-1; row >= 0; row--) {
			
			answer.setElement(row, row, 1/inMat.getElement(row, row));
			
			for(int col = answer.getNumCols()-1; col > row; col--) {
				double tmpVal = 0;
				for(int i = col; i > row; i--) {
					tmpVal += inMat.getElement(row, i) * answer.getElement(i, col);
				}
				answer.setElement(row, col, (-1)*tmpVal/inMat.getElement(row, row));
			}
		}
		
		return answer;
	}
	
	public static Matrix generatePermutationMatrix(int size, int c0, int c1) {
		Matrix answer = new Matrix(size, size);
		answer.setElement(c0, c0, 0);
		answer.setElement(c1, c1, 0);
		answer.setElement(c0, c1, 1);
		answer.setElement(c1, c0, 1);
		return answer;
	}
	
	public static double simpleAbsNorm(Matrix input) {
		double answer = 0;
		for(int i = 0; i < input.getNumRows(); i++) {
			for(int j = 0; j < input.getNumCols(); j++) {
				answer = answer + Math.abs(input.getElement(i, j));
			}
		}
		return answer;
	}
	
	public static Matrix solveLU(Matrix[] LU, Matrix b) {
		Matrix x = invertLMatrix(LU[0]).getMultiplication(b);
		x = invertUMatrix(LU[1]).getMultiplication(x);	
		return x;
	}
	
	public static Matrix solveLUP(Matrix[] LUP, Matrix b) {
		Matrix x = LUP[2].getMultiplication(b);
		x = invertLMatrix(LUP[0]).getMultiplication(x);
		x = invertUMatrix(LUP[1]).getMultiplication(x);
		return x;
	}
	
	public static Matrix getKroneckerVecProd(Matrix inVec) {
		Matrix answer = new Matrix(inVec.getNumRows(), inVec.getNumRows());
		for(int rowIndex = 0; rowIndex < answer.getNumRows(); rowIndex++) {
			for(int colIndex = 0; colIndex < answer.getNumCols(); colIndex++) {
				answer.setElement(rowIndex, colIndex, inVec.getElement(rowIndex, 0) * inVec.getElement(colIndex, 0));
			}
		}
		return answer;
	}
	
	public static double getDeterminante(Matrix input) {
		if(input.getNumCols() != input.getNumRows()) {
			return 0.0;
		}
		if(input.getNumCols() == 1) {
			return input.getElement(0, 0);
		}
		double result = 0.0;
		for(int i = 0; i < input.getNumRows(); i++) {
			result += Math.pow(-1.0, (double)i) * input.getElement(i, 0) * getDeterminante(input.getRemovedMatrix(i, 0));
		}
		return result;
	}
	
}
