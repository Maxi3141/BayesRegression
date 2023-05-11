package math.linAlg;

public class Matrix {
	
	double[][] content;
	double determinante = 1;
	boolean autoUpdateDeterminante = false;
	
	public Matrix(Matrix templateMatrix) {
		content = templateMatrix.getContent();
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public Matrix(int dimRows, int dimCols) {
		content = new double[dimRows][dimCols];
		setIdentity();
	}
	
	public Matrix(double[][] contentNew) {
		content = contentNew;
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public Matrix(double[] contentVecNew) {
		content = new double[contentVecNew.length][1];
		for(int i = 0; i < contentVecNew.length; i++) {
			content[i][0] = contentVecNew[i];
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public Matrix(String inputString) {
		try {
			setContentFromString(inputString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setDeterminanteAutoUpdate(boolean autoUpdateDeterminanteNew) {
		autoUpdateDeterminante = autoUpdateDeterminanteNew;
	}
	
	public void updateDeterminante() {
		determinante = LinAlgBasics.getDeterminante(this);
	}
	
	public double getDeterminante() {
		return determinante;
	}
	
	public void setContentFromString(String contentString) throws Exception {
		String[] rowStrings = contentString.split(";");
		if(rowStrings.length == 0) {
			throw new Exception("Illegal input string.");
		}
		content = new double[rowStrings.length][rowStrings[0].split(",").length];
		for(int i = 0; i < rowStrings.length; i++) {
			String[] entryStrings = rowStrings[i].split(",");
			if(entryStrings.length != rowStrings[0].split(",").length) {
				throw new Exception("Illegal dimensions.");
			}
			for(int j = 0; j < entryStrings.length; j++) {
				content[i][j] = Double.valueOf(entryStrings[j]);
			}
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public double getElement(int i, int j) {
		return content[i][j];
	}
	
	public double[] getRow(int row) {
		return content[row];
	}
	
	/// All boundaries are inclusive.
	public Matrix getSubMatrix(int rowStart, int colStart, int rowEnd, int colEnd) {
		if(rowEnd < rowStart || colEnd < colStart || rowStart < 0 || colStart < 0 || rowEnd >= getNumRows() || colEnd >= getNumCols()) {
			return new Matrix(1,1);
		}
		Matrix result = new Matrix(rowEnd - rowStart + 1, colEnd - colStart + 1);
		for(int i = rowStart; i <= rowEnd; i++) {
			for(int j = colStart; j <= colEnd; j++) {
				result.setElement(i - rowStart, j - colStart, content[i][j]);
			}
		}
		return result;
	}
	
	public void setSubMatrix(int rowStart, int colStart, Matrix newSubMatrix) {
		setSubMatrix(rowStart, colStart, newSubMatrix.getContent());
	}
	
	public void setSubMatrix(int rowStart, int colStart, double[][] newContent) {
		if(rowStart < 0 || colStart < 0) {
			return;
		}
		for(int i = 0; i < Math.min(content.length - rowStart, newContent.length); i++) {
			for(int j = 0; j < Math.min(content[i].length - colStart, newContent[i].length); j++) {
				content[i + rowStart][j + colStart] = newContent[i][j];
			}
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public void setRow(int rowNumber, double[] rowContent) {
		if(rowNumber < 0 || rowNumber >= content.length) {
			System.out.println("MATRIX.setRow() failed. Code 0.");
			return;
		}
		if(content[rowNumber].length != rowContent.length) {
			System.out.println("MATRIX.setRow() failed. Code 1");
			return;
		}
		for(int i = 0; i < Math.min(content[rowNumber].length, rowContent.length); i++) {
			content[rowNumber][i] = rowContent[i];
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public void setRow(int rowNumber, Matrix rowContent) {
		setRow(rowNumber, rowContent.getRow(0));
	}
	
	public void setColumn(int colNumber, double[] colContent) {
		if(colNumber < 0 || colNumber >= content[0].length) {
			System.out.println("MATRIX.setColumn() failed. Code 0.");
			return;
		}
		if(content.length != colContent.length) {
			System.out.println("MATRIX.setColumn() failed. Code 1");
			return;
		}
		for(int i = 0; i < Math.min(content.length, colContent.length); i++) {
			content[i][colNumber] = colContent[i];
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public void setColumn(int colNumber, Matrix colContent) {
		setColumn(colNumber, colContent.getColumn(0));
	}
	
	public Matrix getRemovedMatrix(int row, int col) {
		Matrix result = new Matrix(getNumRows() - 1, getNumCols() - 1);
		result.setSubMatrix(0, 0, getSubMatrix(0, 0, row - 1, col -1));
		result.setSubMatrix(0, col, getSubMatrix(0, col + 1, row - 1, getNumCols() - 1));
		result.setSubMatrix(row, 0, getSubMatrix(row + 1, 0, getNumRows() - 1, col - 1));
		result.setSubMatrix(row, col, getSubMatrix(row + 1, col + 1, getNumRows() - 1, getNumCols() - 1));
		return result;
	}
	
	public void setContent(double[][] contentNew) {
		content = contentNew;
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public double[] getColumn(int col) {
		double[] answer = new double[content.length];
		for(int j = 0; j < content.length; j++) {
			answer[j] = content[j][col];
		}
		return answer;
	}
	
	public int getNumCols() {
		return content[0].length;
	}
	
	public int getNumRows() {
		return content.length;
	}
	
	public double[][] getContent() {
		double[][] answer = new double[content.length][content[0].length];
		for(int i = 0; i < content.length; i++) {
			for(int j = 0; j < content[0].length; j++) {
				answer[i][j] = content[i][j];
			}
		}
		return answer;
	}
	
	public double[] getContentAsVector() {
		double[] answer = new double[getNumCols() * getNumRows()];
		for(int i = 0; i < getNumCols(); i++) {
			for(int j = 0; j < getNumRows(); j++) {
				answer[i*j+j] = getElement(j, i);
			}
		}
		return answer;
	}
	
	public double getMaxElement() {
		double answer = content[0][0];
		for(int i = 0; i < getNumRows(); i++) {
			for(int j = 0; j < getNumCols(); j++) {
				if(getElement(i,j) > answer) {
					answer = getElement(i,j);
				}
			}
		}
		return answer;
	}
	
	public double getMinElement() {
		double answer = content[0][0];
		for(int i = 0; i < getNumRows(); i++) {
			for(int j = 0; j < getNumCols(); j++) {
				if(getElement(i,j) < answer) {
					answer = getElement(i,j);
				}
			}
		}
		return answer;
	}
	
	public void setElement(int row, int col, double val) {
		content[row][col] = val;
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public void setZero() {
		for(int i = 0; i < content.length; i++) {
			for(int j = 0; j < content[0].length; j++) {
				content[i][j] = 0;
			}
		}
		determinante = 0.0;
	}
	
	void setIdentity() {
		setZero();
		if(content.length == 0) {
			return;
		}
		int diagLength = Math.min(content.length, content[0].length);
		for(int i = 0; i < diagLength; i++) {
			content[i][i] = 1;
		}
		determinante = 1.0;
	}
	
	public void setRandom() {
		for(int i = 0; i < content.length; i++) {
			for(int j = 0; j < content[i].length; j++) {
				content[i][j] = Math.random();
			}
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public Matrix getTranspose() {
		if(content.length == 0) {
			return new Matrix(1,1);
		}
		Matrix answer = new Matrix(content[0].length, content.length);
		for(int row = 0; row < content.length; row++) {
			for(int col = 0; col < content[row].length; col++) {
				answer.setElement(col, row, content[row][col]);
			}
		}
		if(autoUpdateDeterminante) {
			answer.updateDeterminante();
		}
		return answer;
	}
	
	public void addMatrix(Matrix sumMatrix) {
		if(getNumRows() == sumMatrix.getNumRows() && getNumCols() == sumMatrix.getNumCols()) {
			for(int row = 0; row < content.length; row++) {
				for(int col = 0; col < content[row].length; col++) {
					content[row][col] += sumMatrix.getElement(row, col);
				}
			}
			if(autoUpdateDeterminante) {
				updateDeterminante();
			}
		}
	}
	
	public Matrix getAddMatrix(Matrix sumMatrix) {
		Matrix answer = new Matrix(getContent());
		answer.addMatrix(sumMatrix);
		return answer;
	}
	
	public void scaleMultiplication(double factor) {
		for(int row = 0; row < content.length; row++) {
			for(int col = 0; col < content[0].length; col++) {
				content[row][col] = content[row][col] * factor;
			}
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public Matrix getScaleMultiplication(double factor) {
		Matrix answer = new Matrix(getContent());
		answer.scaleMultiplication(factor);
		return answer;
	}
	
	public Matrix getMultiplication(Matrix factorM) {
		if(getNumCols() == factorM.getNumRows()) {
			Matrix answer = new Matrix(getNumRows(), factorM.getNumCols());
			for(int row = 0; row < answer.getNumRows(); row++) {
				for(int col = 0; col < answer.getNumCols(); col++) {
					double currentEntry = 0;
					for(int i = 0; i < getNumCols(); i++) {
						currentEntry += getElement(row, i) * factorM.getElement(i, col);
					}
					answer.setElement(row, col, currentEntry);
				}
			}
			if(autoUpdateDeterminante) {
				answer.updateDeterminante();
			}
			return answer;
			
		}else {
			return new Matrix(1,1);
		}
	}
	
	public void multiply(Matrix factorM) {
		content = getMultiplication(factorM).getContent();
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public void multRow(int out, double mult) {
		for(int col = 0; col < getNumCols(); col++) {
			content[out][col] *= mult;
		}
		if(autoUpdateDeterminante) {
			determinante *= mult;
		}
	}
	
	public void swapRow(int in, int out) {
		for(int col = 0; col < getNumCols(); col++) {
			double outTMPVal = content[out][col];
			content[out][col] = content[in][col];
			content[in][col] = outTMPVal;
		}
		if(in != out) {
			if(autoUpdateDeterminante) {
				determinante *= -1.0;
			}
		}
	}
	
	public void addRow(int in, int out, double mult) {
		for(int col = 0; col < getNumCols(); col++) {
			content[out][col] += content[in][col] * mult;
		}
		if(autoUpdateDeterminante) {
			updateDeterminante();
		}
	}
	
	public double getScalarProduct(Matrix input) {
		double result = 0;
		if(getNumCols() != 1 || input.getNumCols() != 1 || getNumRows() != input.getNumRows()) {
			return result;
		}
		for(int i = 0; i < getNumRows(); i++) {
			result += getElement(i, 0) * input.getElement(i, 0); 
		}
		return result;
	}
	
	public void debugPrint() {
		int maxCharLength = 0;
		for(int row = 0; row < content.length; row++) {
			for(int col = 0; col < content[row].length; col++) {
				if(String.valueOf(content[row][col]).length() > maxCharLength) {
					maxCharLength = String.valueOf(content[row][col]).length();
				}
			}
		}
		
		for(int row = 0; row < content.length; row++) {
			for(int col = 0; col < content[row].length; col++) {
				String currentEntry = String.valueOf(content[row][col]);
				while(currentEntry.length() < maxCharLength) {
					currentEntry = currentEntry + " ";
				}
				System.out.print(currentEntry + "  ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
}
