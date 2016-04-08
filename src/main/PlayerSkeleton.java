package main;


public class PlayerSkeleton {

	public static final int SEARCH_DEPTH = 2;
	public static final double NEGATIVE_INFINITY = -2000000;

	public int height;

	public double evaluate(SearchState s, int moves) {
		//double bonusScore = (s.getRowsCleared() - height) * 0.760666 + moves * 0;
		return ((s.hasLost()) ? NEGATIVE_INFINITY : new Features(s.getField()).evaluate());
	}

	// Gets the highest state score of all possible moves
	public double maxMove(int moves, SearchState s) {
		int[][] legalMoves = s.legalMoves();
		double maxValue = NEGATIVE_INFINITY;
		for(int move = 0; move < legalMoves.length; move++) {
			SearchState cloneState = s.clone();
			cloneState.makeMove(move);
			maxValue = Math.max(maxValue, expectedMove(moves, cloneState));
		}
		return maxValue;
	}

	// Gets the average score of the different resulting states after performing the best move for each possible piece 
	public double expectedMove(int moves, SearchState s) {
		if (moves == 1 || s.hasLost()) {
			return evaluate(s, moves);
		}
		double expectedValue = 0.0;
		for(int nextPiece = 0; nextPiece < State.N_PIECES; nextPiece++) {
			s.setNextPiece(nextPiece);
			double value = maxMove(moves - 1, s);
			expectedValue += value;
		}
		expectedValue /= State.N_PIECES;
		return expectedValue;
	}

	//implement this function to have a working system
	public int pickMove(State s) {
		height = s.getRowsCleared();
		int[][] legalMoves = s.legalMoves();
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxMove = 0;
		for(int move = 0; move < legalMoves.length; move++) {
			SearchState searchState = new SearchState(s);
			searchState.makeMove(move);

			double nextValue = expectedMove(SEARCH_DEPTH, searchState);
			if (nextValue <= maxValue) {
				continue;
			}
			maxValue = nextValue;
			maxMove = move;
		}

		//System.out.println("Move: " + maxMove + " Value: " + maxValue + " Rows: " + s.getRowsCleared());
		return maxMove;
	}
	
	public static void main(String[] args) {
		State s = new State();
		//new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s));
			//s.draw();
			//s.drawNext(0,0);
			/*
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
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
	final static double[] FEATURE_PARAMS = { -0.510066, 0.760666, -0.35663, -0.184483 };

	int[] featureValues;

	public Features(int[][] field) {
		featureValues = new int[NUM_FEATURES];
		identifyFeatures(field);
	}

	public double evaluate() {
		double score = 0.0;
		for (int i = 0; i < NUM_FEATURES; i++) {

			/*
			if (i < NUM_FEATURES - 1)
				System.out.print(featureNames[i] + ": " + featureValues[i] + ", ");
			else
				System.out.println(featureNames[i] + ": " + featureValues[i]);
			*/
			score += (featureValues[i] * FEATURE_PARAMS[i]);
		}

		//System.out.println("Score: "  + score);
		return score;
	}

	private void identifyFeatures(int[][] field) {

		int[] maxHeight = getMaxHeight(field);

		// TODO: Edit after deciding on features
		featureValues[SUM_HEIGHT] = getFeatureValue(field, maxHeight,
				SUM_HEIGHT);
		featureValues[COMPLETED_LINES] = getFeatureValue(field, maxHeight,
				COMPLETED_LINES);
		featureValues[HOLES] = getFeatureValue(field, maxHeight, HOLES);
		featureValues[BUMPINESS] = getFeatureValue(field, maxHeight, BUMPINESS);
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