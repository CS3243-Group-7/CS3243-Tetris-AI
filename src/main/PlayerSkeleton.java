package main;


public class PlayerSkeleton {

	public static final int SEARCH_DEPTH = 1;

	public double evaluate(SearchState s) {
		return s.getRowsCleared();
	}

	public double maxMove(int depthLeft, SearchState s) {
		int[][] legalMoves = s.legalMoves();
		double maxValue = 0.0;
		for(int orient = 0; orient < legalMoves.length; orient++) {
			for(int slot = 0; slot < legalMoves[orient].length; slot++) {
				SearchState cloneState = s.clone();
				cloneState.makeMove(legalMoves[orient][slot]);
				maxValue = Math.max(maxValue, expectedMove(depthLeft, cloneState));
			}
		}
		return maxValue;
	}

	public double expectedMove(int depthLeft, SearchState s) {
		if (depthLeft == 0 || s.hasLost()) {
			return evaluate(s);
		}
		double expectedValue = 0.0;
		for(int nextPiece = 0; nextPiece < State.N_PIECES; nextPiece++) {
			s.setNextPiece(nextPiece);
			expectedValue += maxMove(depthLeft - 1, s);
		}
		expectedValue /= State.N_PIECES;
		return expectedValue;
	}

	//implement this function to have a working system
	public int pickMove(State s) {
		int[][] legalMoves = s.legalMoves();
		double maxValue = 0.0;
		int maxMove = 0;
		for(int orient = 0; orient < legalMoves.length; orient++) {
			for(int slot = 0; slot < legalMoves[orient].length; slot++) {
				int nextMove = legalMoves[orient][slot];
				SearchState searchState = new SearchState(s);
				s.makeMove(nextMove);
				searchState.makeMove(nextMove);

				double nextValue = expectedMove(SEARCH_DEPTH, searchState);
				if (nextValue < maxValue) {
					continue;
				}
				maxValue = nextValue;
				maxMove = nextMove;
			}
		}

		System.out.println("Move: " + maxMove + " Value: " + maxValue);
		return maxMove;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
