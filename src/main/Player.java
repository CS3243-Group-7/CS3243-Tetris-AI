package main;

import training.Feature;

import java.util.Arrays;

public class Player {

    final static double MIN_SCORE = -999999.0;
    private Feature[] currentFeatures;

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
        return getFeaturesScore(field);
        /***********************/
    }

    private double getFeaturesScore(int[][] field) {
        double sum = 0;
        for (int i = 0; i < currentFeatures.length; i++) {
            sum += currentFeatures[i].getScore(field);
        }
        return sum;
    }

    public int play(Feature[] features) {
        currentFeatures = features;
        State s = new State();
        //new TFrame(s);
        int step = 0;
        while (!s.hasLost()) {
            ++step;
            s.makeMove(pickMove(s, s.legalMoves()));
            /*
            s.draw();
            s.drawNext(0, 0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
        }
        return s.getRowsCleared();
    }

}

