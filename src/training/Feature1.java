package training;

import main.State;

public class Feature1 {
    // aggregate height
    // complete lines
    // holes
    // bumpiness
    // lines scored
    double[] features = new double[] /* {-0.510066, 0.760666, -0.35663, -0.184483, 0}; */ {-0.510066, 0.545710, -0.356630, -0.184483, -1.414055};

    public Feature1() {

    }

    public Feature1(Feature1 other) {
        for (int i = 0; i < getFeaturesCount(); i++) {
            features[i] = other.getFeatureValue(i);
        }
    }

    public int getFeaturesCount() {
        return 5;
    }

    public double getScore(int[][] field, int linesScored) {
        int[] maxHeight = getMaxHeight(field);
        double total = 0;
        for (int i = 0; i < 5; i++) {
            total += features[i] * getFeatureValue(field, maxHeight, i);
        }
        return total;
    }

    public double getFeatureValue(int index) {
        assert index >= 0 && index < getFeaturesCount();
        return features[index];
    }

    private int getFeatureValue(int[][] field, int[] maxHeight, int featureID) {
        // TODO: Edit after deciding on features
        switch (featureID) {
            case 0:

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

            case 1:

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

            case 2:

                int holes = 0;

                for (int j = 0; j < State.COLS; j++) {
                    for (int i = 0; i < maxHeight[j] - 1; i++) {
                        if (field[i][j] == 0)
                            holes++;
                    }
                }

                return holes;

            case 3:

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

    public void change(int index, double newValue) {
        assert index >= 0 && index < getFeaturesCount();
        features[index] = newValue;
    }
}
