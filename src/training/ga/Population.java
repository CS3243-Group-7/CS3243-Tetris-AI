package training.ga;

import training.Feature;
import utility.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.math.*;

public class Population {
    private int populationSize;
    private List<Chromosome> pool = new ArrayList<>();

    static Object lock = new Object();

    /*
   Time: 49, Clear 22338
-22.438780022805837 -45.064314711053854 -27.721263040811543 -12.780864859022078 -10.528864905504271 -54.599860057271286 -9.967402960897353
     */

    /*
    -16.55657680222304 -98.3562513580288 -57.6233495706022 -25.062805224468093 23.04803343396107 -51.62361183660132 -31.435740906954603
Time: 339, Clear 206364
     */
    public Population(int populationSize, Feature[] features) {
        this.populationSize = populationSize;
        for (int i = 0; i < populationSize; i++) {
            Feature[] wow = new Feature[features.length];
            for (int j = 0; j < features.length; j++) {
                wow[j] = new Feature(features[j]);
            }
            pool.add(new Chromosome(wow));
            for (int j = 0; j < 100; j++) {
                pool.get(i).mutate();
            }
        }
    }

    public boolean isEqualFeatures(Feature[] features1, Feature[] features2) {
        if (features1 == null) {
            return false;
        }
        for (int j = 0; j < features1.length; j++) {
            if (Math.abs(features1[j].getValue() - features2[j].getValue()) > 1e-4) {
                return false;
            }
        }
        return true;
    }

    public Chromosome evolve() {
        int time = 0;
        for (Chromosome chromosome : pool) {
            chromosome.calcFitness();
        }
        Feature[] previousFeatures = null;
        int stuckTimeout = 3;
        while (time < 500) {
            Collections.sort(pool, (a, b) -> -Integer.compare(a.getFitness(), b.getFitness()));

            Feature[] features = pool.get(0).getFeatures();

            synchronized (lock) {
                System.out.print("Thread #" + Thread.currentThread().getId());
                for (int j = 0; j < features.length; j++) {
                    System.out.print(" " + features[j].getValue());
                }
                System.out.println();
                System.out.printf("Thread #" + Thread.currentThread().getId() + " Time: %d, Clear %d\n", time, pool.get(0).getFitness());
            }

            if (isEqualFeatures(previousFeatures, features)) {
                stuckTimeout--;
                if (stuckTimeout == 0) {
                    break;
                }
            } else {
                stuckTimeout = 3;
            }
            previousFeatures = features;

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
            nextPool.stream().forEach(chromosome -> chromosome.calcFitness());
            Collections.sort(nextPool, (a, b) -> -Integer.compare(a.getFitness(), b.getFitness()));
            if (nextPool.get(0).getFitness() < nextPool.get(1).getFitness()) {
                throw new RuntimeException();
            }
            pool.clear();
            for (int i = 0; i < populationSize; i++) {
                pool.add(nextPool.get(i));
            }
        }
        return pool.get(0);
    }

    private synchronized void print() {

    }
}
