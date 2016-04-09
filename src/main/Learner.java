package main;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Learner {

    public static int NUM_WEIGHTS = Features.NUM_FEATURES;
    public static Logger logger = Logger.getGlobal();
    public static int GAMES_COUNT = 1000;
    private double[][] featureWeights;
    
    private void initialiseFeatureWeights() {
        featureWeights = new double[GAMES_COUNT][NUM_WEIGHTS];
        for (int i = 0; i < GAMES_COUNT; i++) {
            for (int j = 0; j < NUM_WEIGHTS; j++) {
                int negativeMultiplier = (Math.random() > 0.5) ? -1 : 1;
                featureWeights[i][j] = negativeMultiplier * Math.random() * 1000000;
            }
        }
    }
    
    private int runGame(double[] featureWeights) {
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(featureWeights);
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s));
        }
        return s.getRowsCleared();
    }
    
    /** Gets a random index from 0 to gameScores.length - 1
     * Chance for a index to get chosen is proportional to the respective game's score
     * @param gameScores
     * @return
     */
    public static int getRandomIndex(double[] gameScores, double cutOffScore) {
        int scoreSum = 0;
        for (int i = 0; i < gameScores.length; i++) {
            if (cutOffScore <= gameScores[i]) {
                scoreSum += gameScores[i];
            }
        }
        
        double randomVal = Math.random();
        
        double cumulativePercentage = 0;
        for (int i = 0; i < gameScores.length; i++) {
            if (cutOffScore <= gameScores[i]) {
                cumulativePercentage += (double) gameScores[i] / scoreSum;
                if (cumulativePercentage >= randomVal) return i;
            }
        }
        
        System.out.println("Error occured: randoming a game");
        return (int) Math.floor(Math.random() * GAMES_COUNT);
    }
    
    // crossover
    private double[] reproduce(double[] featureWeightsOne, double[] featureWeightsTwo) {
        int crossOverPoint = (int) Math.floor(Math.random() * NUM_WEIGHTS);
        double[] childFeatureWeights = new double[NUM_WEIGHTS];
        for (int i = 0; i < NUM_WEIGHTS; i++) {
            childFeatureWeights[i] = (i > crossOverPoint) ? featureWeightsTwo[i] : featureWeightsOne[i];
        }
        
        return childFeatureWeights;
    }
    
    private double[] mutate(double[] featureWeights) {
        double mutationProbability = 1.0/NUM_WEIGHTS;//100;//Features.NUM_FEATURES;
        for (int i = 0; i < NUM_WEIGHTS; i++) {
            if (Math.random() < mutationProbability) {
                Long longValue = Double.doubleToLongBits(featureWeights[i]);
                String bitValue = Long.toBinaryString(longValue);
                
                // prepend leading zeroes
                int bitValueLength = bitValue.length();
                for (int j = 0; j < 64 - bitValueLength; j++) {
                    bitValue = '0' + bitValue;
                }
                int posToMutate = (int) Math.floor(Math.random() * 64);
                char newBit = bitValue.charAt(posToMutate) == '1' ? '0' : '1';
                if (posToMutate == 63) {
                    bitValue = bitValue.substring(0, posToMutate) + newBit;
                } else {
                    bitValue = bitValue.substring(0, posToMutate) + newBit + bitValue.substring(posToMutate + 1);
                }
                
                boolean isNegative = (bitValue.length() > 0 && bitValue.charAt(0) == '1') ? true : false;
                if (isNegative) {
                    bitValue = '-' + bitValue.substring(1);
                } else {
                    bitValue = bitValue.substring(1);
                }
                featureWeights[i] = Double.longBitsToDouble(Long.valueOf(bitValue, 2));
            }
        }
        
        return featureWeights;
    }
    private double getCutOffScore(double[] gameScores) {
        double[] clonedGameScores = gameScores.clone();
        Arrays.sort(clonedGameScores);
        return clonedGameScores[(int)(GAMES_COUNT * 0.9)];
    }
    
    private void updateWeights(double[] gameScores) {
        // select new weights based on performance

        double cutOffScore = getCutOffScore(gameScores);
        System.out.println("cutOffScore: " + cutOffScore);
        for (int i = 0; i < GAMES_COUNT; i++) {
            int chosenIndex = getRandomIndex(gameScores, cutOffScore);
            int chosenIndex2 = getRandomIndex(gameScores, cutOffScore);
            double[] childFeatureWeights = reproduce(featureWeights[chosenIndex], featureWeights[chosenIndex2]);
            childFeatureWeights = mutate(childFeatureWeights);
            featureWeights[i] = childFeatureWeights;
        }
    }
    
    public void printAllFeatureWeights() {
        for (int i = 0; i < featureWeights.length; i++) {
            System.out.println("Feature weights " + i + ": " + Arrays.toString(featureWeights[i]));
        }
    }
    
    public static void initializeLogFile() throws IOException {
        FileHandler logHandler = new FileHandler("tetris.log");
        LogManager.getLogManager().reset(); // removes printout to console
                                            // aka root handler
        logHandler.setFormatter(new SimpleFormatter()); // set output to a
                                                        // human-readable
                                                        // log format
        logger.addHandler(logHandler);
    }
    
    public static void main(String[] args) {
        try {
            initializeLogFile();
            final Learner learner = new Learner();
            learner.initialiseFeatureWeights();
            int cycleNo = 1;
            double bestScore = 0;
            int cycleOfBestScore = 0;
            double[] bestFeatureWeights = new double[NUM_WEIGHTS];
            while (true) {
                long time = System.nanoTime()/1000000;
                double bestScoreOfCycle = 0;
                logger.info("Starting cycle " + cycleNo);
                //learner.printAllFeatureWeights();
                String gameResults = "Games: "; 
                double[] averageGameScores = new double[GAMES_COUNT];
                final double[] gameFeatureWeights = new double[NUM_WEIGHTS];
                for (int i = 0; i < GAMES_COUNT; i++) {
                    for (int j = 0; j < NUM_WEIGHTS; j++) {
                        gameFeatureWeights[j] = learner.featureWeights[i][j];
                    }
                    
                    double totalScoreOfGames = 0;
                    final int[] gameScores = new int[GAMES_COUNT];
                    Thread[] threads = new Thread[100];
                    for (int j = 0; j < 100; j++) {
                        final int index = j;
                        
                        threads[j] = new Thread() {
                            @Override
                            public void run() {
                                gameScores[index] = learner.runGame(gameFeatureWeights);
                            }
                        };
                        threads[j].start();
                    }
                    for (int j = 0; j < 100; j++) {
                        try {
                            threads[j].join();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    for (int j = 0; j < 100; j++) {
                        totalScoreOfGames += gameScores[j];
                    }
                    double averageScoreOfGames = totalScoreOfGames/100.0;
                    
                    gameResults += averageScoreOfGames;
                    if (i != GAMES_COUNT - 1) {
                        gameResults += ", ";
                    } else {
                        gameResults += "\n";
                    }
                    averageGameScores[i] = averageScoreOfGames;
                    if (averageScoreOfGames > bestScore) {
                        cycleOfBestScore = cycleNo;
                        bestScore = averageScoreOfGames;
                        bestFeatureWeights = learner.featureWeights[i];
                    }
                    if (averageScoreOfGames > bestScoreOfCycle) {
                        bestScoreOfCycle = averageScoreOfGames;
                    }
                    
                }
                logger.info(gameResults);
                logger.info("Best score for CURRENT cycle: " + bestScoreOfCycle);
                logger.info("Current best score and weights: " + bestScore + " and " + Arrays.toString(bestFeatureWeights) + " from cycle " + cycleOfBestScore);
                // evaluate and update weights
                learner.updateWeights(averageGameScores);
                // end of learning

                long time2 = System.nanoTime()/1000000;
                System.out.println("Total time for cycle " + cycleNo +": " + (time2-time) + "ms.");
                cycleNo++;
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}