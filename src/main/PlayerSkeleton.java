package main;


public class PlayerSkeleton {

	public static final int SEARCH_DEPTH = 5;

	public double evaluate(SearchState s) {
		return 0.0;
	}


	public double maxMove(int depthLeft, SearchState s) {

	}

	public double expectedMove(int depthLeft, SearchState s) {

	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		int orient = (int)Math.floor(Math.random() * legalMoves.length);
		int slot = (int)Math.floor(Math.random() * legalMoves[orient].length);

        System.out.println("Orient: " + orient + "/" + legalMoves.length);
        System.out.println("Slot: " + slot + "/" + legalMoves[orient].length);
		int move = legalMoves[orient][slot];
		System.out.println("Move: " + move);
		return move;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
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
