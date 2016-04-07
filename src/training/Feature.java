package training;

import main.State;

import java.util.function.Function;

public class Feature {

    private final Function<int[][], Double> eval;
    private double value;

    public Feature(double defaultValue, Function<int[][], Double> eval) {
        value = defaultValue;
        this.eval = eval;
    }

    public Feature(Feature other) {
        this.value = other.getValue();
        this.eval = other.getEval();
    }

    public Function<int[][], Double> getEval() {
        return eval;
    }

    public double getScore(int[][] field) {
        return value * eval.apply(field);
    }

    public void setValue(double newValue) {
        value = newValue;
    }

    public double getValue() {
        return value;
    }

    /**
     * Common features
     */
    public static Feature getSumHeight(double defaultValue) {
        return new Feature(defaultValue, (field) -> {

            int[] maxHeight = getMaxHeight(field);

            int sumHeight = 0;

            for (int j = 0; j < State.COLS; j++) {
                for (int i = State.ROWS - 1; i >= 0; i--) {
                    if (field[i][j] > 0) {
                        sumHeight += maxHeight[j];
                        break;
                    }
                }
            }

            return (double) sumHeight;
        });
    }

    public static Feature getCompletedLines(double defaultValue) {
        return new Feature(defaultValue, (field) -> {

            int[] maxHeight = getMaxHeight(field);

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

            return (double) completedLines;
        });
    }

    public static Feature getHoleCount(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int[] maxHeight = getMaxHeight(field);

            int holes = 0;
            for (int j = 0; j < State.COLS; j++) {
                for (int i = 0; i < maxHeight[j] - 1; i++) {
                    if (field[i][j] == 0)
                        holes++;
                }
            }

            return (double) holes;
        });
    }

    public static Feature getBumpiness(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int[] maxHeight = getMaxHeight(field);

            int bumpiness = 0;
            for (int i = 0; i + 1 < State.COLS; i++) {
                bumpiness += Math.abs(maxHeight[i + 1] - maxHeight[i]);
            }

            return (double) bumpiness;
        });
    }

    private static int[] getMaxHeight(int[][] field) {
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
