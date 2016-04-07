package training;

import training.ga.Chromosome;
import training.ga.Population;

public class Train {

    //final static double[] FEATURE_PARAMS = { -0.510066, 0.760666, -0.35663, -0.184483 };
    private Feature[] features;

    private void initFeatures() {
        features = new Feature[] {
                Feature.getSumHeight(-0.510066),
                Feature.getCompletedLines(0.760666),
                Feature.getHoleCount(-0.35663),
                Feature.getBumpiness(-0.184483)
        };
    }

    public void train() {
        initFeatures();
        Population population = new Population(20, features);
        Chromosome bestGuy = population.evolve();
        System.out.println("bestGuy: " + bestGuy);
    }

    public static void main(String[] args) {
        new Train().train();
    }
}
