package math.linAlg;

public class Solvers {
	
	static double TOL = 10e-10;
	
	public static Matrix MINRES(Matrix A, Matrix b) {
		Matrix x = new Matrix(A.getNumCols(), 1);
		x.setZero();
		int maxIter = A.getNumRows();
		double tolerance = 1e-10;
		
		Matrix r = b.getAddMatrix(A.getMultiplication(x).getScaleMultiplication(-1));
		Matrix p = new Matrix(r);
		Matrix s = A.getMultiplication(p);
		
		Matrix sOld1 = new Matrix(s);
		Matrix sOld2 = new Matrix(s);
		Matrix pOld1 = new Matrix(p);
		Matrix pOld2 = new Matrix(p);
		
		for(int i = 0; i < maxIter; i++) {
			pOld2.setContent(pOld1.getContent());
			pOld1.setContent(p.getContent());
			sOld2.setContent(sOld1.getContent());
			sOld1.setContent(s.getContent());
			
			double alpha = r.getScalarProduct(s) / s.getScalarProduct(s);
			x.addMatrix(p.getScaleMultiplication(alpha));
			r.addMatrix(s.getScaleMultiplication(-alpha));
			
			if(r.getScalarProduct(r) < tolerance) {
				break;
			}
			p.setContent(s.getContent());
			s.setContent(A.getMultiplication(s).getContent());
			
			double beta1 = s.getScalarProduct(sOld1)/ sOld1.getScalarProduct(sOld1);
			p.addMatrix(pOld1.getScaleMultiplication(-beta1));
			s.addMatrix(sOld1.getScaleMultiplication(-beta1));
			
			if(i > 0) {
				double beta2 = s.getScalarProduct(sOld2) / sOld2.getScalarProduct(sOld2);
				p.addMatrix(pOld2.getScaleMultiplication(-beta2));
				s.addMatrix(sOld2.getScaleMultiplication(-beta2));
			}
		}
		
		return x;
	}
	
	public static Matrix[] decomposeQR(Matrix input) {
		Matrix[] answer = new Matrix[2];
		Matrix matrixQ = new Matrix(input.getNumRows(), input.getNumCols());
		Matrix matrixR = new Matrix(input.getContent());
		
		for(int step = 0; step < input.getNumCols(); step++) {
			Matrix currentColumn = matrixR.getSubMatrix(step, step, matrixR.getNumRows()-1, step);
			double alpha = LinAlgBasics.getVectorPNorm(currentColumn, 2);
			Matrix u = currentColumn.getAddMatrix(LinAlgBasics.getUnitVector(input.getNumRows() - step, 1).getScaleMultiplication(-alpha));
			if(LinAlgBasics.getVectorPNorm(u, 2) == 0) {
				continue;
			}
			
			Matrix v = u.getScaleMultiplication(1.0 / LinAlgBasics.getVectorPNorm(u, 2));
			Matrix smallQ = new Matrix(input.getNumRows() - step, input.getNumCols() - step).getAddMatrix(v.getMultiplication(v.getTranspose()).getScaleMultiplication(-2.0));
			Matrix Q = new Matrix(input.getNumRows(), input.getNumCols());
			Q.setSubMatrix(step, step, smallQ);
			
			matrixR = Q.getMultiplication(matrixR);
			matrixQ = matrixQ.getMultiplication(Q);
		}
		
		matrixQ = matrixQ.getTranspose();
		
		answer[0] = matrixQ;
		answer[1] = matrixR;
		
		return answer;
	}
	
	public static Matrix invertRMatrix(Matrix input) {
		Matrix result = new Matrix(input.getNumRows(), input.getNumCols());
		
		for(int i = input.getNumRows() - 1; i >= 0; i--) {
			result.setElement(i, i, 1.0 / input.getElement(i, i));
			for(int j = i + 1; j < input.getNumCols(); j++) {
				double nextEntry = 0.0;
				for(int k = i + 1; k < input.getNumRows(); k++) {
					nextEntry += result.getElement(k, j) * input.getElement(i, k);
				}
				result.setElement(i, j, - nextEntry / input.getElement(i, i));
			}
		}
		
		return result;
	}
	
	public static Matrix invertMatrix(Matrix input) {
		Matrix[] qrDecomposition = decomposeQR(input);
		Matrix result = invertRMatrix(qrDecomposition[1]);
		result.multiply(qrDecomposition[0]);
		return result;
	}
	
}
