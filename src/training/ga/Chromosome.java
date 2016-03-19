package training.ga;

import main.Player;
import main.State;
import training.Feature1;
import utility.Pair;

import java.util.Random;

public class Chromosome {
    private Feature1 feature;
    private Random random = new Random();
    private int fitness;

    public Chromosome(Feature1 feature) {
        this.feature = feature;
    }

    public Chromosome(Chromosome other) {
        this.feature = new Feature1(other.getFeature());
    }

    public Feature1 getFeature() {
        return feature;
    }

    public static boolean areCompatible(Chromosome a, Chromosome b) {
        return a.feature.getClass().equals(b.getFeature().getClass());
    }

    public static Pair<Chromosome, Chromosome> crossOver(Chromosome a, Chromosome b) {
        if (Chromosome.areCompatible(a, b)) {
            a = new Chromosome(a);
            b = new Chromosome(b);
            Random random = new Random();
            int[] permutation = new int[a.feature.getFeaturesCount()];
            for (int i = 0; i < a.feature.getFeaturesCount(); i++) {
                permutation[i] = i;
                int j = random.nextInt(i + 1);
                int t = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = t;
            }
            for (int i = 0; i < a.feature.getFeaturesCount(); i++) {
                double aValue = a.feature.getFeatureValue(permutation[i]);
                double bValue = b.feature.getFeatureValue(i);
                if (random.nextBoolean()) {
                    b.feature.change(i, aValue);
                    a.feature.change(permutation[i], bValue);
                }
            }
            return new Pair<>(a, b);
        } else {
            throw new RuntimeException();
        }
    }

    public void mutate() {
        int id = random.nextInt(feature.getFeaturesCount());
        double oldValue = feature.getFeatureValue(id);
        double newValue = random.nextGaussian() + oldValue;
        feature.change(id, newValue);
    }

    public void calcFitness() {
        fitness = new Player().play(feature);
    }

    public int getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return String.format("Lines scored: %d\nWeights: {%f, %f, %f, %f, %f}\n", getFitness(),
                feature.getFeatureValue(0), feature.getFeatureValue(1), feature.getFeatureValue(2),
                feature.getFeatureValue(3), feature.getFeatureValue(4));
    }
}
