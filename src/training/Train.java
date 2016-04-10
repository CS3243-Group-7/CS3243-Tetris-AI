package training;

import training.ga.Chromosome;
import training.ga.Population;

public class Train {

    //final static double[] FEATURE_PARAMS = { -0.510066, 0.760666, -0.35663, -0.184483 };
    private Feature[] features;

    private void initFeatures() {
        features = new Feature[] {
                //Feature.getSumHeight(-0.510066),
                Feature.getRowTransitions(-0.424),
                Feature.getColumnTransitions(-2.026),
                Feature.getHighestHole(-0.663),
                Feature.getSumWellDepths(-0.425),
                Feature.getCompletedLines(0.760666),
                Feature.getHoleCount(-0.35663),
                Feature.getBumpiness(-0.184483),
                Feature.getTetrominoHeight(-0.184483),
                Feature.getLandingHeight(-0.384483)
        };
    }

    public void train() {
        initFeatures();
        Population population = new Population(5, features);
        Chromosome bestGuy = population.evolve();
        System.out.println(">>>> BestGuy: " + bestGuy);
    }

    public static void main(String[] args) {
        int runCount = 10;
        while(runCount-- > 0) {
            new Train().train();
        }
    }
}
