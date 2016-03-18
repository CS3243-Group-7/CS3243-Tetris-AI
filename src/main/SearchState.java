package main;

/**
 * Created by zeulb on 3/19/16.
 */
public class SearchState {

    private static int[][] getCloneField(int[][] field) {
        int n = field.length;
        int[][] cloneField = new int[n][];
        for(int i = 0; i < n; i++) {
            cloneField[i] = field[i].clone();
        }
        return cloneField;
    }

    public boolean lost = false;

    //current turn
    private int turn = 0;
    private int cleared = 0;

    //each square in the grid - int means empty - other values mean the turn it was placed
    private int[][] field = new int[State.ROWS][State.COLS];
    //top row+1 of each column
    //0 means empty
    private int[] top = new int[State.COLS];

    //number of next piece
    protected int nextPiece;

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
    }

    public int getNextPiece() {
        return nextPiece;
    }

    public boolean hasLost() {
        return lost;
    }

    public int getRowsCleared() {
        return cleared;
    }

    public int getTurnNumber() {
        return turn;
    }

    public void setNextPiece(int nextPiece) {
        this.nextPiece = nextPiece;
    }

    public SearchState(State s) {
        this.lost = s.hasLost();
        this.turn = s.getTurnNumber();
        this.cleared = s.getRowsCleared();
        this.field = getCloneField(s.getField());
        this.top = s.getTop().clone();
        this.nextPiece = s.getNextPiece();
    }

    public SearchState(SearchState s) {
        this.lost = s.hasLost();
        this.turn = s.getTurnNumber();
        this.cleared = s.getRowsCleared();
        this.field = getCloneField(s.getField());
        this.top = s.getTop().clone();
        this.nextPiece = s.getNextPiece();
    }

    //gives legal moves for
    public int[][] legalMoves() {
        return State.legalMoves[nextPiece];
    }

    //make a move based on the move index - its order in the legalMoves list
    public void makeMove(int move) {
        makeMove(State.legalMoves[nextPiece][move]);
    }

    //make a move based on an array of orient and slot
    public void makeMove(int[] move) {
        makeMove(move[State.ORIENT],move[State.SLOT]);
    }

    //returns false if you lose - true otherwise
    public boolean makeMove(int orient, int slot) {
        turn++;
        //height if the first column makes contact
        int height = top[slot]-State.getpBottom()[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for(int c = 1; c < State.pWidth[nextPiece][orient];c++) {
            height = Math.max(height,top[slot+c]-State.getpBottom()[nextPiece][orient][c]);
        }

        //check if game ended
        if(height+State.getpHeight()[nextPiece][orient] >= State.ROWS) {
            lost = true;
            return false;
        }


        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < State.pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for(int h = height+State.getpBottom()[nextPiece][orient][i]; h < height+State.getpTop()[nextPiece][orient][i]; h++) {
                field[h][i+slot] = turn;
            }
        }

        //adjust top
        for(int c = 0; c < State.getpWidth()[nextPiece][orient]; c++) {
            top[slot+c]=height+State.getpTop()[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for(int r = height+State.getpHeight()[nextPiece][orient]-1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for(int c = 0; c < State.COLS; c++) {
                if(field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if(full) {
                rowsCleared++;
                cleared++;
                //for each column
                for(int c = 0; c < State.COLS; c++) {

                    //slide down all bricks
                    for(int i = r; i < top[c]; i++) {
                        field[i][c] = field[i+1][c];
                    }
                    //lower the top
                    top[c]--;
                    while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
                }
            }
        }
        return true;
    }

    public SearchState clone() {
        return new SearchState(this);
    }

}
