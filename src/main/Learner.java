package main;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Learner {

    public static Logger logger = Logger.getGlobal();
    public static int GAMES_COUNT = 1000;
    private double[][] featureWeights;
    
    private void initialiseFeatureWeights() {
        featureWeights = new double[GAMES_COUNT][Features.NUM_FEATURES];
        for (int i = 0; i < GAMES_COUNT; i++) {
            for (int j = 0; j < Features.NUM_FEATURES; j++) {
                int negativeMultiplier = (Math.random() > 0.5) ? -1 : 1;
                featureWeights[i][j] = negativeMultiplier * Math.random() * 1000000;
            }
        }
    }
    
    private int runGame(double[] featureWeights) {
        State s = new State();
        //TFrame frame = new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton(featureWeights);
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            //s.draw();
            //s.drawNext(0, 0);
        }
        //frame.dispose();
        return s.getRowsCleared();
    }
    
    /** Gets a random index from 0 to gameScores.length - 1
     * Chance for a index to get chosen is proportional to the respective game's score
     * @param gameScores
     * @return
     */
    public static int getRandomIndex(int[] gameScores) {
        int scoreSum = 0;
        for (int i = 0; i < GAMES_COUNT; i++) {
            scoreSum += gameScores[i];
        }
        
        double randomVal = Math.random();
        
        double cumulativePercentage = 0;
        for (int i = 0; i < GAMES_COUNT; i++) {
            cumulativePercentage = (double)gameScores[i]/scoreSum;
            if (cumulativePercentage >= randomVal) return i;
        }
        
        return (int) Math.floor(Math.random() * GAMES_COUNT);
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
        double mutationProbability = 1.0/100;//Features.NUM_FEATURES;
        for (int i = 0; i < Features.NUM_FEATURES; i++) {
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
    
    private void updateWeights(int[] gameScores) {
        // select new weights based on performance
        
        for (int i = 0; i < GAMES_COUNT; i++) {
            int chosenIndex = getRandomIndex(gameScores);
            int chosenIndex2 = getRandomIndex(gameScores);
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
            Learner learner = new Learner();
            learner.initialiseFeatureWeights();
            int cycleNo = 1;
            int bestScore = 0;
            int cycleOfBestScore = 0;
            double[] bestFeatureWeights = new double[Features.NUM_FEATURES];
            while (true) {
                int bestScoreOfCycle = 0;
                logger.info("Starting cycle " + cycleNo);
                //learner.printAllFeatureWeights();
                String gameResults = "Games: "; 
                int[] gameScores = new int[GAMES_COUNT];
                for (int i = 0; i < GAMES_COUNT; i++) {
                    double[] gameFeatureWeights = {learner.featureWeights[i][0], learner.featureWeights[i][1], learner.featureWeights[i][2], learner.featureWeights[i][3]};
                    int gameScore = learner.runGame(gameFeatureWeights);
                    gameResults += gameScore;
                    if (i != GAMES_COUNT - 1) {
                        gameResults += ", ";
                    } else {
                        gameResults += "\n";
                    }
                    gameScores[i] = gameScore;
                    if (gameScore > bestScore) {
                        cycleOfBestScore = cycleNo;
                        bestScore = gameScore;
                        bestFeatureWeights = learner.featureWeights[i];
                    }
                    if (gameScore > bestScoreOfCycle) {
                        bestScoreOfCycle = gameScore;
                    }
                }
                logger.info(gameResults);
                logger.info("Best score for CURRENT cycle: " + bestScoreOfCycle);
                logger.info("Current best score and weights: " + bestScore + " and " + Arrays.toString(bestFeatureWeights) + " from cycle " + cycleOfBestScore);
                // evaluate and update weights
                learner.updateWeights(gameScores);
                // end of learning
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
