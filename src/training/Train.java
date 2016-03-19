package training;

import training.ga.Chromosome;
import training.ga.Population;

public class Train {
    public static void main(String[] args) {
        Population population = new Population(20);
        Chromosome bestGuy = population.evolve();
        System.out.println("bestGuy: " + bestGuy);
    }
}
