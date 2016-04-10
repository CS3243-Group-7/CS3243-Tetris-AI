package main;


public class PlayerSkeleton {

	public static final int SEARCH_DEPTH = 1;
	public static final double NEGATIVE_INFINITY = -2000000;

	private double[] featureWeights;
	
	public PlayerSkeleton(double[] featureWeights) {
	    this.featureWeights = featureWeights;
	}

	public double evaluate(SearchState s) {
	    double score = ((s.hasLost()) ? NEGATIVE_INFINITY : new Features(s.getField(), featureWeights).evaluate());
	    //System.out.println("Score returned is: " + score);
		return score;
	}

	// Gets the highest state score of all possible moves
	public double getBestScoreOfPossibleMoves(int moves, SearchState s) {
		int[][] legalMoves = s.legalMoves();
		double maxValue = NEGATIVE_INFINITY;
		for(int move = 0; move < legalMoves.length; move++) {
			SearchState cloneState = s.clone();
			cloneState.makeMove(move);
			maxValue = Math.max(maxValue, getAverageScoreOfState(moves - 1, cloneState));
		}
		return maxValue;
	}

	// Gets the average score of the different resulting states after performing the best move for each possible piece 
	public double getAverageScoreOfState(int depth, SearchState s) {
		if (depth == 1 || s.hasLost()) {
			return evaluate(s);
		}
		s.resolveMove();
		double expectedValue = 0.0;
		for(int nextPiece = 0; nextPiece < State.N_PIECES; nextPiece++) {
			s.setNextPiece(nextPiece);
			double value = getBestScoreOfPossibleMoves(depth, s);
			expectedValue += value;
		}
		expectedValue /= State.N_PIECES;
		return expectedValue;
	}

	//implement this function to have a working system
	public int pickMove(State s) {
		int[][] legalMoves = s.legalMoves();
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxMove = 0;
		for(int move = 0; move < legalMoves.length; move++) {
			SearchState searchState = new SearchState(s);
			searchState.makeMove(move);

			double nextValue = getAverageScoreOfState(SEARCH_DEPTH, searchState);
			if (nextValue > maxValue) {
	            maxValue = nextValue;
	            maxMove = move;
			}
		}
		//System.out.println("Best attainable score of states: " + maxValue);

		return maxMove;
	}
	
	public static void main(String[] args) {
	    // ensure weights have same number of weights as features
	    final double[] finalisedFeatureWeights = {262277.7288257435, 368932.27367163956, -961034.7867291301, -157346.07062358136, -1.464370383197647};
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton(finalisedFeatureWeights);
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s));
			s.draw();
			s.drawNext(0,0);
			
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}

class Features {

	// From
	// https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/
	final static int NUM_FEATURES = 8;
	final static int SUM_HEIGHT = 0;
	final static int COMPLETED_LINES = 1;
	final static int HOLES = 2;
	final static int BUMPINESS = 3;
	final static int BLOCKADES = 4;
	final static int HIGHEST_HOLE = 5;
	final static int WELL_COUNT = 6;
    final static int VERTICAL_ROUGHNESS = 7;

	// TODO: Enter parameters here after deciding on them
	//final static double[] FEATURE_PARAMS = {262277.7288257435, 368932.27367163956, -961034.7867291301, -157346.07062358136, -1.464370383197647};

	double[] featureWeights;
	int[] featureValues;

	public Features(int[][] field, double[] featureWeights) {
	    this.featureWeights = featureWeights;
		featureValues = new int[NUM_FEATURES];
		identifyFeatures(field);
	}

	public double evaluate() {
		double score = 0.0;
		for (int i = 0; i < NUM_FEATURES; i++) {
			score += (featureValues[i] * featureWeights[i]);
		}

		return score;
	}

	private void identifyFeatures(int[][] field) {
		int[] maxHeight = getMaxHeight(field);

		for (int i = 0; i < NUM_FEATURES; i++) {
		    featureValues[i] = getFeatureValue(field, maxHeight, i);
		}
	}

	private int getFeatureValue(int[][] field, int[] maxHeight, int featureID) {

		// TODO: Edit after deciding on features
		switch (featureID) {
			case SUM_HEIGHT:

			    int sumHeight = 0;
	            for (int j = 0; j < State.COLS; j++) {
	                sumHeight += maxHeight[j];
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
			case BLOCKADES:
	            int blockades = 0;
	            for (int j = 0; j < State.COLS; j++) {
	                boolean holeFound = false;
	                for (int i = 0; i < maxHeight[j]; i++) {
	                    if (field[i][j] == 0) {
	                        holeFound = true;
	                    }
	                    if (holeFound && field[i][j] != 0) {
	                        blockades++;
	                    }
	                }
	            }
	            return blockades;
			case HIGHEST_HOLE:
                int height = 0;

                for (int j = 0; j < State.COLS; j++) {
                    for (int i = 0; i < maxHeight[j] - 1; i++) {
                        if (field[i][j] == 0 && i > height)
                            height = i;
                    }
                }
                return height;
			case WELL_COUNT:
			    int wells = 0;

                for (int j = 0; j < State.COLS; j++) {
                    for (int i = 0; i < maxHeight[j] - 3; i++) {
                        if (field[i][j] == 0)
                            wells++;
                    }
                }

                return wells;
			case VERTICAL_ROUGHNESS:
			    int roughness = 0;
			    for (int j = 0; j < State.COLS; j++) {
                    for (int i = 0; i < maxHeight[j] - 1; i++) {
                        if ((field[i][j] == 0 && field[i+1][j] != 0) || (field[i][j] != 0 && field[i+1][j] == 0))
                            roughness++;
                    }
                }
			    return roughness;
			default:
				return -1;
		}
	}

	private int[] getMaxHeight(int[][] field) {

		int[] maxHeight = new int[State.COLS];

		for (int j = 0; j < State.COLS; j++) {
			for (int i = State.ROWS - 1; i >= 0; i--) {
				if (field[i][j] != 0) {
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