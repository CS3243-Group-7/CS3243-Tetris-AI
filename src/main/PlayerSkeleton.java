package main;


public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		int orient = (int)Math.floor(Math.random() * legalMoves.length);
		int slot = (int)Math.floor(Math.random() * legalMoves[orient].length);

        System.out.println("Orient: " + orient);
        System.out.println("Slot: " + slot);
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
