package main;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by zeulb on 3/19/16.
 */
public class GameEvaluator {

    public static void main(String[] args) {
        int play = 20;
        int totalRows = 0;
        int maxRows = 0;
        int minRows = 1000000;
        ArrayList<Integer> rows = new ArrayList<>();
        for(int i = 0; i < play; i++) {
            State s = new State();
            PlayerSkeleton p = new PlayerSkeleton();
            while (!s.hasLost()) {
                s.makeMove(p.pickMove(s));
            }
            int rowsCleared = s.getRowsCleared();
            totalRows += rowsCleared;
            maxRows = Math.max(maxRows, rowsCleared);
            minRows = Math.min(minRows, rowsCleared);
            rows.add(rowsCleared);
        }
        Collections.sort(rows);
        System.out.println("Games played: " + play);
        System.out.println("Average rows cleared : " + 1.0 * totalRows / play);
        System.out.println("Maximum rows cleared : " + maxRows);
        System.out.println("Minimum rows cleared : " + minRows);
        System.out.println("Median rows cleared : " + rows.get(play/2));

    }

}
