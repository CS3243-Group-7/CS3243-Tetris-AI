package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Learner {

    public static int NUM_WEIGHTS = Features.NUM_FEATURES;
    public static Logger logger = Logger.getGlobal();
    public static int GAMES_COUNT = 50;
    public static double TOP_PERFORMERS = 1.0;
    private double[][] featureWeights;
    
    // last best score: 2473.2
    // double[] lastBestScore = {107586.63663457146, 750753.4845484055, -150417.96616116664, -208169.70729578854, 2731.960240795902, -651269.0404827954, -66463.07469214208, -146345.66859520436};
	
    
    private void initialiseFeatureWeights() {
        featureWeights = new double[GAMES_COUNT][NUM_WEIGHTS];
        for (int i = 0; i < GAMES_COUNT; i++) {
//        	if (i < 5) { // put 5 of last score to ensure survival
//        		featureWeights[i] = lastBestScore;
//        	}
            for (int j = 0; j < NUM_WEIGHTS; j++) {
                int negativeMultiplier = (Math.random() > 0.5) ? -1 : 1;
                featureWeights[i][j] = negativeMultiplier * Math.random() * 500;
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
        double[] childFeatureWeights = new double[NUM_WEIGHTS];
        
        for (int i = 0; i < NUM_WEIGHTS; i++) {
            double randNum = Math.random();
            if (randNum < 0.5) {
            	childFeatureWeights[i] = featureWeightsOne[i];
            } else {
            	childFeatureWeights[i] = featureWeightsTwo[i];
            }
        }

        
        
        return childFeatureWeights;
    }
    
    private double[] mutate(double[] featureWeights) {
        double mutationProbability = 1.0/10;//Features.NUM_FEATURES;
        for (int i = 0; i < NUM_WEIGHTS; i++) {
            if (Math.random() < mutationProbability) {
                int negativeMultiplier = (Math.random() > 0.5) ? -1 : 1;
                featureWeights[i] = negativeMultiplier * Math.random() * 500;
            }
        }
        
        return featureWeights;
    }
    private double getCutOffScore(double[] gameScores) {
        double[] clonedGameScores = gameScores.clone();
        Arrays.sort(clonedGameScores);
        return clonedGameScores[(int)(GAMES_COUNT * (1.0 - TOP_PERFORMERS))];
    }
    
    private void updateWeights(double[] gameScores) {
        int[] chosenIndexes = new int[5];
        ArrayList<double[]> newFeatureWeights = new ArrayList<double[]>();

        while(newFeatureWeights.size() < GAMES_COUNT) {
	        // sample random group of 5
	        ArrayList<Integer> list = new ArrayList<Integer>();
	        for (int j=0; j<GAMES_COUNT; j++) {
	            list.add(new Integer(j));
	        }
	        Collections.shuffle(list);
	        for (int j=0; j<5; j++) {
	            chosenIndexes[j] = list.get(j);
	        }
	        
	        // find highest scoring 2
	        int highScoreIndex1 = -1,
	        	highScoreIndex2 = -1;
	        for (int i=0; i < 5; i++) {
	        	if (highScoreIndex1 == -1) {
	        		highScoreIndex1 = chosenIndexes[i];
	        		continue;
	        	} else {
	        		if (gameScores[chosenIndexes[i]] > gameScores[highScoreIndex1]) {
	        			highScoreIndex2 = highScoreIndex1;
	        			highScoreIndex1 = chosenIndexes[i];
	        			continue;
	        		}
	        	}
	        	
	        	if (highScoreIndex2 == -1) {
	        		highScoreIndex2 = chosenIndexes[i];
	        		continue;
	        	} else {
	        		if (gameScores[chosenIndexes[i]] > gameScores[highScoreIndex2]) {
	        			highScoreIndex2 = chosenIndexes[i];
	        			continue;
	        		}
	        	} 
                
	        }
	        
	        // reproduce 5 times
            double[] childFeatureWeights;
	        for (int i=0; i < 5; i++) {
	        	childFeatureWeights = reproduce(
	        			featureWeights[highScoreIndex1], 
	        			featureWeights[highScoreIndex2]);
	            newFeatureWeights.add(childFeatureWeights);
	        }

	        
	        
	       
        }
        
        double[] newweight;
        for (int i=0; i < GAMES_COUNT; i++) {
        	newweight = newFeatureWeights.remove(0);
        	newweight = mutate(newweight);
        	featureWeights[i] = newweight;
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
                // learner.printAllFeatureWeights();
                String gameResults = "Games: "; 
                double[] averageGameScores = new double[GAMES_COUNT];
                final double[] gameFeatureWeights = new double[NUM_WEIGHTS];
                for (int i = 0; i < GAMES_COUNT; i++) {
                    for (int j = 0; j < NUM_WEIGHTS; j++) {
                        gameFeatureWeights[j] = learner.featureWeights[i][j];
                    }
                    
                    double totalScoreOfGames = 0;
                    int gameRuns = 10;
                    final int[] gameScores = new int[gameRuns];
                    Thread[] threads = new Thread[gameRuns];
                    for (int j = 0; j < gameRuns; j++) {
                        final int index = j;
                        
                        threads[j] = new Thread() {
                            @Override
                            public void run() {
                                gameScores[index] = learner.runGame(gameFeatureWeights);
                            }
                        };
                        threads[j].start();
                    }
                    for (int j = 0; j < gameRuns; j++) {
                        try {
                            threads[j].join();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    for (int j = 0; j < gameRuns; j++) {
                        totalScoreOfGames += gameScores[j];
                    }
                    double averageScoreOfGames = totalScoreOfGames/(double)gameRuns;
                    
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
                System.out.println("    Best score for CURRENT cycle: " + bestScoreOfCycle);
                System.out.println("    Current best score and weights: " + bestScore + " and " + Arrays.toString(bestFeatureWeights) + " from cycle " + cycleOfBestScore);
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