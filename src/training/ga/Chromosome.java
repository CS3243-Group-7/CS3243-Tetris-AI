package training.ga;

import main.Player;
import training.Feature;
import utility.Pair;

import java.util.Arrays;
import java.util.Random;

public class Chromosome {

    private static final int TRIES = 4;

    private Feature[] features;
    private Random random = new Random();
    private int fitness;

    public Chromosome(Feature[] features) {
        this.features = features;
    }

    public Chromosome(Chromosome other) {
        Feature[] otherFeatures = other.getFeatures();
        features = new Feature[otherFeatures.length];
        for (int i = 0; i < features.length; i++) {
            features[i] = new Feature(otherFeatures[i]);
        }
    }

    public Feature[] getFeatures() {
        return features;
    }

    public static boolean areCompatible(Chromosome a, Chromosome b) {
        return a.features.getClass().equals(b.getFeatures().getClass());
    }

    public static Pair<Chromosome, Chromosome> crossOver(Chromosome a, Chromosome b) {
        if (Chromosome.areCompatible(a, b)) {
            a = new Chromosome(a);
            b = new Chromosome(b);
            Random random = new Random();
            int[] permutation = new int[a.features.length];
            for (int i = 0; i < a.features.length; i++) {
                permutation[i] = i;
                /*
                int j = random.nextInt(i + 1);
                int t = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = t;
                */
            }
            for (int i = 0; i < a.features.length; i++) {
                double aValue = a.features[i].getValue();
                double bValue = b.features[i].getValue();
                if (random.nextBoolean()) {
                    b.features[i].setValue(aValue);
                    a.features[permutation[i]].setValue(bValue);
                }
            }
            return new Pair<>(a, b);
        } else {
            throw new RuntimeException();
        }
    }

    public void mutate() {
        int id = random.nextInt(features.length);
        double oldValue = features[id].getValue();
        double newValue = random.nextGaussian() * 10 + oldValue;
        features[id].setValue(newValue);
    }

    public void calcFitness() {
        int tot = 0;
        for (int i = 0; i < TRIES; i++) {
            tot += new Player().play(features);
        }
        fitness = tot / TRIES;
    }

    public int getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Lines scored: ");
        str.append(getFitness());
        str.append("\n");
        str.append("{ ");
        Arrays.stream(features).forEach(feature -> str.append(feature.getEval() + " "));
        str.append("}");
        return str.toString();
    }
}
