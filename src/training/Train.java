package training;

import training.ga.Chromosome;
import training.ga.Population;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Train implements Runnable {

    //final static double[] FEATURE_PARAMS = { -0.510066, 0.760666, -0.35663, -0.184483 };
    private Feature[] features;
    static Chromosome bestDude = null;
    private Chromosome bestGuy;

    private void initFeatures() {
        features = new Feature[] {
                //Feature.getSumHeight(-0.510066),
                Feature.getRowTransitions(-0.424),
                Feature.getColumnTransitions(-2.026),
                Feature.getHighestHole(-0.663),
                Feature.getSumWellDepths(-0.425),
                Feature.getCompletedLines(0.760666),
                Feature.getHoleCount(-0.35663),
                Feature.getBumpiness(-0.684483),
                Feature.getTetrominoHeight(-0.684483)
                //Feature.getLandingHeight(-0.684483)
        };
    }

    @Override
    public void run() {
        System.out.println("Thread #" + Thread.currentThread().getId() + " has been started");
        initFeatures();
        Population population = new Population(5, features);
        bestGuy = population.evolve();
        System.out.println("Thread #" + Thread.currentThread().getId() + " >>>> BestGuy: " + bestGuy);
        update();
    }

    private synchronized void update() {
        System.out.println("========================================================");
        if (bestDude == null || bestGuy.getFitness() > bestDude.getFitness()) {
            bestDude = bestGuy;
            System.out.println("Thread #" + Thread.currentThread().getId() + " NEW BEST");
        }
        System.out.println(">>>>>>>> BestDude: " + bestDude);
        System.out.println("========================================================");
        System.out.println();
    }

    public static void main(String[] args) {
        //int runCount = 100;
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++) {
            Runnable worker = new Train();
            service.execute(worker);
        }
        service.shutdown();
    }
}
