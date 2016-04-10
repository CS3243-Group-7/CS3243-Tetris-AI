package main;
import training.Feature;
/**
 * Created by zeulb on 3/19/16.
 */
public class EMPlayer {

    public static final int SEARCH_DEPTH = 2;
    public static final double NEGATIVE_INFINITY = -2000000;

    public int height;
    private Feature[] currentFeatures = new Feature[] {
        //Feature.getSumHeight(-0.510066),
        Feature.getRowTransitions(-0.424),
                Feature.getColumnTransitions(-2.026),
                Feature.getHighestHole(-0.663),
                Feature.getSumWellDepths(-0.425),
                Feature.getCompletedLines(0.760666),
                Feature.getHoleCount(-0.35663),
                Feature.getBumpiness(-0.184483)
    };;

    public double evaluate(SearchState s, int moves) {
        return ((s.hasLost()) ? NEGATIVE_INFINITY : getFeaturesScore(s.getField()));
    }

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

    public double expectedMove(int moves, SearchState s) {
        if (moves == SEARCH_DEPTH || s.hasLost()) {
            return evaluate(s, moves);
        }
        double expectedValue = 0.0;
        for(int nextPiece = 0; nextPiece < State.N_PIECES; nextPiece++) {
            s.setNextPiece(nextPiece);
            double value = maxMove(moves + 1, s);
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

            double nextValue = expectedMove(1, searchState);
            if (nextValue <= maxValue) {
                continue;
            }
            maxValue = nextValue;
            maxMove = move;
        }

        //System.out.println("Move: " + maxMove + " Value: " + maxValue + " Rows: " + s.getRowsCleared());
        return maxMove;
    }

    private double getFeaturesScore(int[][] field) {
        double sum = 0;
        for (int i = 0; i < currentFeatures.length; i++) {
            sum += currentFeatures[i].getScore(field);
        }
        return sum;
    }

    public int play() {
        State s = new State();
        int step = 0;
        while (!s.hasLost()) {
            ++step;
            s.makeMove(pickMove(s));
        }
        return s.getRowsCleared();
    }

    public int play(Feature[] features) {
        currentFeatures = features;
        return play();
    }

    public static void main(String[] args) {
        State s = new State();
        //new TFrame(s);
        EMPlayer p = new EMPlayer();
        int co = 0;
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s));
            //s.draw();
            //s.drawNext(0, 0);
            if (co%100 == 0) {
                System.out.println(s.getRowsCleared());
            }
            co++;
        }
        System.out.println("You have completed " + s.getRowsCleared()
                + " rows.");
    }

}
