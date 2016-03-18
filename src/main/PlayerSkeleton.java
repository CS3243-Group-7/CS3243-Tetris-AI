package main;

public class PlayerSkeleton {
	
	final static double MIN_SCORE = -999999.0;
	private double[] featureWeights;
	
	PlayerSkeleton(double[] featureWeights) {
	    this.featureWeights = featureWeights;
	}
	
	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		double bestScore = MIN_SCORE;
		int bestMove = 0;

		for (int i = 0; i < legalMoves.length; i++) {
			double moveScore = checkMove(s, legalMoves[i][State.SLOT],
					legalMoves[i][State.ORIENT]);
			if (moveScore > bestScore) {
				bestMove = i;
				bestScore = moveScore;
			}
		}
		return bestMove;
	}

	private double checkMove(State s, int slot, int orient) {
		
		int nextPiece = s.nextPiece;
		int[][][] pBottom = State.getpBottom();
		int[][][] pTop = State.getpTop();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();

		int[] top = s.getTop().clone();
		int[][] field = new int[State.ROWS][];
		for (int i = 0; i < field.length; i++)
			field[i] = s.getField()[i].clone();

		/** CHECK IF GAME LOST **/
		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];
		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}

		// check if game ended
		if (height + pHeight[nextPiece][orient] >= State.ROWS) {
			return MIN_SCORE;
		}
		/************************/

		/** UPDATE FIELD **/
		// for each column in the piece - fill in the appropriate blocks
		for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

			// from bottom to top of brick
			for (int h = height + pBottom[nextPiece][orient][i]; h < height
					+ pTop[nextPiece][orient][i]; h++) {
				field[h][i + slot] = 1;
			}
		}
		/******************/

		/** OBTAIN EVALUATION **/
		Features features = new Features(field, featureWeights);
		return features.evaluate();
		/***********************/
	}

	public static void main(String[] args) {
		/*State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			System.out.println("You have cleared " + s.getRowsCleared() + " rows.");
		}
		System.out.println("You have completed " + s.getRowsCleared() + " rows.");
		*/
	}

}

class Features {

	// From
	// https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/
	final static int NUM_FEATURES = 4;
	final static int SUM_HEIGHT = 0;
	final static int COMPLETED_LINES = 1;
	final static int HOLES = 2;
	final static int BUMPINESS = 3;
	final static String[] featureNames = { "SUM_HEIGHT", "COMPLETED_LINES",
			"HOLES", "BUMPINESS" };

	// TODO: Enter parameters here after deciding on them
	double[] featureParams;

	int[] featureWeights;

	public Features(int[][] field, double[] featureWeights) {
	    featureParams = featureWeights;
		this.featureWeights = new int[NUM_FEATURES];
		identifyFeatures(field);
	}

	public double evaluate() {
		double score = 0.0;
		for (int i = 0; i < NUM_FEATURES; i++) {
			/*if (i < NUM_FEATURES - 1)
				System.out.print(featureNames[i] + ": " + featureWeights[i] + ", ");
			else
				System.out.println(featureNames[i] + ": " + featureWeights[i]);*/
			score += (featureWeights[i] * featureParams[i]);
		}
		//System.out.println("Heuristic score: "  + score);
		return score;
	}

	private void identifyFeatures(int[][] field) {
		int[] maxHeight = getMaxHeight(field);
		// TODO: Edit after deciding on features
		featureWeights[SUM_HEIGHT] = getFeatureValue(field, maxHeight, SUM_HEIGHT);
		featureWeights[COMPLETED_LINES] = getFeatureValue(field, maxHeight, COMPLETED_LINES);
		featureWeights[HOLES] = getFeatureValue(field, maxHeight, HOLES);
		featureWeights[BUMPINESS] = getFeatureValue(field, maxHeight, BUMPINESS);
	}

	private int getFeatureValue(int[][] field, int[] maxHeight, int featureID) {
		// TODO: Edit after deciding on features
		switch (featureID) {
		case SUM_HEIGHT:
			int sumHeight = 0;
			for (int j = 0; j < State.COLS; j++) {
				for (int i = State.ROWS - 1; i >= 0; i--) {
					if (field[i][j] > 0) {
						sumHeight += maxHeight[j];
						break;
					}
				}
			}
			return sumHeight;

		case COMPLETED_LINES:
			int completedLines = 0;
			int minHeight = State.ROWS + 1;
			for (int i = 0; i < maxHeight.length; i++)
				minHeight = Math.min(minHeight, maxHeight[i]);
			for (int i = 0; i < minHeight; i++) {
				boolean lineComplete = true;
				for (int j = 0; j < State.COLS; j++) {
					if (field[i][j] == 0) {
						lineComplete = false;
						break;
					}
				}
				if (lineComplete)
					completedLines++;
			}
			return completedLines;

		case HOLES:
			int holes = 0;
			for (int j = 0; j < State.COLS; j++) {
				for (int i = 0; i < maxHeight[j] - 1; i++) {
					if (field[i][j] == 0)
						holes++;
				}
			}
			return holes;

		case BUMPINESS:
			int bumps = 0;
			for (int j = 0; j < State.COLS - 1; j++)
				bumps += Math.abs(maxHeight[j] - maxHeight[j + 1]);
			return bumps;

		default:
			return -1;
		}
	}

	private int[] getMaxHeight(int[][] field) {
		int[] maxHeight = new int[State.COLS];
		for (int j = 0; j < State.COLS; j++) {
			for (int i = State.ROWS - 1; i >= 0; i--) {
				if (field[i][j] > 0) {
					maxHeight[j] = (i + 1);
					break;
				}
				if (i == 0)
					maxHeight[j] = 0;
			}
		}
		return maxHeight;
	}
}
