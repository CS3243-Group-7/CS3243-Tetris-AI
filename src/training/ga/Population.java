package training.ga;

import training.Feature;
import utility.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Population {
    private int populationSize;
    private List<Chromosome> pool = new ArrayList<>();

    public Population(int populationSize, Feature[] features) {
        this.populationSize = populationSize;
        for (int i = 0; i < populationSize; i++) {
            pool.add(new Chromosome(Arrays.copyOf(features, features.length)));
        }
    }

    public Chromosome evolve() {
        int time = 0;
        int maxFitness = 0;
        for (Chromosome chromosome : pool) {
            chromosome.calcFitness();
            maxFitness = Math.max(maxFitness, chromosome.getFitness());
        }
        while (time < 1000 && maxFitness < 1000000) {
            System.out.printf("Time: %d, Clear %d\n", time, maxFitness);
            time++;
            List<Chromosome> nextPool = new ArrayList<>();
            nextPool.addAll(pool);
            List<Chromosome> parents = new ArrayList<>();
            for (int i = 0; i < pool.size(); i++) {
                if (Math.random() < 0.5) {
                    parents.add(pool.get(i));
                }
            }
            for (int i = 0; i + 1 < parents.size(); i++) {
                Pair<Chromosome, Chromosome> res = Chromosome.crossOver(parents.get(i), parents.get(i + 1));
                nextPool.add(res.getFirst());
                nextPool.add(res.getSecond());
            }
            for (int i = 0; i < pool.size(); i++) {
                if (Math.random() < 0.1) {
                    Chromosome e = new Chromosome(pool.get(i));
                    nextPool.add(e);
                    e.mutate();
                }
            }
            for (int i = 0; i < nextPool.size(); i++) {
                nextPool.get(i).calcFitness();
            }
            Collections.sort(nextPool, (a, b) -> -Integer.compare(a.getFitness(), b.getFitness()));
            if (nextPool.get(0).getFitness() < nextPool.get(1).getFitness()) {
                throw new RuntimeException();
            }
            pool.clear();
            maxFitness = 0;
            for (int i = 0; i < populationSize; i++) {
                pool.add(nextPool.get(i));
                maxFitness = Math.max(maxFitness, pool.get(i).getFitness());
            }
        }
        return pool.get(0);
    }
}
