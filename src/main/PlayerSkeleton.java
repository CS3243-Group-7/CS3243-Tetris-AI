package main;

public class PlayerSkeleton {

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		double bestScore = 0.0;
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
		for(int i = 0; i < field.length; i++)
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
			return -1.0;
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
		Features features = new Features(field);
		return features.evaluate();
		/***********************/
	}


	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
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

	// TODO: Enter parameters here after deciding on them
	final static double[] FEATURE_PARAMS = { 1.0, 1.0, 1.0, 1.0 };

	int[] featureValues;

	public Features(int[][] field) {
		featureValues = new int[NUM_FEATURES];
		identifyFeatures(field);
	}

	public double evaluate() {
		double score = 0;
		for (int i = 0; i < NUM_FEATURES; i++) {
			score += (featureValues[i] * FEATURE_PARAMS[i]);
		}
		return score;
	}

	// TODO: Implement after deciding on features
	private void identifyFeatures(int[][] field) {
		// Dummy to be changed
		featureValues = new int[]{0, 0, 0, 0};
	}
}
