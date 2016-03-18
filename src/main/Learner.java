package main;

public class Learner {
    public static int GAMES_COUNT = 10;
    private double[][] featureWeights;
    
    private void initialiseFeatureWeights() {
        featureWeights = new double[GAMES_COUNT][Features.NUM_FEATURES];
        for (int i = 0; i < GAMES_COUNT; i++) {
            for (int j = 0; j < Features.NUM_FEATURES; j++) {
                featureWeights[i][j] = 0;
            }
        }
    }
    
    private double runGame(double[] featureWeights) {
        State s = new State();
        TFrame frame = new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton(featureWeights);
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            s.draw();
            s.drawNext(0, 0);
        }
        frame.dispose();
        return s.getRowsCleared();
    }
    
    /** Gets a random index from 0 to gameScores.length - 1
     * Chance for a index to get chosen is proportional to the respective game's score
     * @param gameScores
     * @return
     */
    public static int getRandomIndex(double[] gameScores) {
        double scoreSum = 0;
        for (int i = 0; i < GAMES_COUNT; i++) {
            scoreSum += gameScores[i];
        }
        
        double randomVal = Math.random();
        
        double cumulativePercentage = 0;
        for (int i = 0; i < GAMES_COUNT; i++) {
            cumulativePercentage = gameScores[i]/scoreSum;
            if (cumulativePercentage >= randomVal) return i;
        }
        
        return GAMES_COUNT - 1;
    }
    
    // crossover
    private double[] reproduce(double[] featureWeightsOne, double[] featureWeightsTwo) {
        int crossOverPoint = (int) Math.floor(Math.random() * Features.NUM_FEATURES);
        double[] childFeatureWeights = new double[Features.NUM_FEATURES];
        for (int i = 0; i < Features.NUM_FEATURES; i++) {
            childFeatureWeights[i] = (i > crossOverPoint) ? featureWeightsTwo[i] : featureWeightsOne[i];
        }
        
        return childFeatureWeights;
    }
    
    private double[] mutate(double[] featureWeights) {
        double mutationProbability = 1.0/Features.NUM_FEATURES;
        for (int i = 0; i < Features.NUM_FEATURES; i++) {
            if (Math.random() < mutationProbability) {
                Long longValue = Double.doubleToLongBits(featureWeights[i]);
                System.out.println("longValue is: " + longValue);
                String bitValue = Long.toString(longValue, 2);
                
                int bitValueLength = bitValue.length();
                for (int j = 0; j < 64 - bitValueLength; j++) {
                    bitValue = '0' + bitValue;
                }
                System.out.println("bitValue is: " + bitValue);
                int posToMutate = (int) Math.floor(Math.random() * 63) + 1;
                char newBit = bitValue.charAt(posToMutate) == '1' ? '0' : '1';
                if (posToMutate == 63) {
                    bitValue = bitValue.substring(0, posToMutate) + newBit;
                } else {
                    bitValue = bitValue.substring(0, posToMutate) + newBit + bitValue.substring(posToMutate + 1);
                }
                featureWeights[i] = Double.longBitsToDouble(Long.valueOf(bitValue, 2));
            }
        }
        
        return featureWeights;
    }
    
    private void updateWeights(double[] gameScores) {
        // select new weights based on performance
        double[][] selectFeatureWeights = new double[GAMES_COUNT][Features.NUM_FEATURES];
        
        for (int i = 0; i < GAMES_COUNT; i++) {
            int chosenIndex = getRandomIndex(gameScores);
            int chosenIndex2 = getRandomIndex(gameScores);
            double[] childFeatureWeights = reproduce(featureWeights[chosenIndex], featureWeights[chosenIndex2]);
            childFeatureWeights = mutate(childFeatureWeights);
            selectFeatureWeights[i] = childFeatureWeights;
        }
    }
    
    public static void main(String[] args) {
        Learner learner = new Learner();
        learner.initialiseFeatureWeights();
        int cycleNo = 1;
        while (true) {
            System.out.println("Starting cycle " + cycleNo);
            double[] gameScores = new double[GAMES_COUNT];
            for (int i = 0; i < GAMES_COUNT; i++) {
                double[] gameFeatureWeights = {learner.featureWeights[i][0], learner.featureWeights[i][1], learner.featureWeights[i][2], learner.featureWeights[i][3]};
                double gameScore = learner.runGame(gameFeatureWeights);
                System.out.println("Game " + i + " ended with a score of " + gameScore);
                gameScores[i] = gameScore;
            }
            
            // evaluate and update weights
            learner.updateWeights(gameScores);
            
            
            // end of learning
            
            cycleNo++;
        }
    }
}
