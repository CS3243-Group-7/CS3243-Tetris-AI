package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

public class TetrisGeneticLearner {

	private static final String RESULT_FILE_NAME = "Result";
	private final static double[] PARAMS_RANGE = { -1.0, 1.0 };
	private final static int NUM_PARAMS = 4;

	// Settings
	private static int SAMPLE_SIZE = 10;
	private static int GAMES_PER_GENE = 1;
	private static int NUM_GENES_TO_PAIR = 4;
	private final static int PIECES_PER_GAME = 500;
	private final static double CHANCE_TO_MUTATE = 0.05;
	private final static double[] MUTATION_RANGE = { -0.2, 0.2 };
	private final static double ERROR_TOLERANCE = 0.1;

	// Data structures
	private TreeSet<Gene> geneSortedByFitness;
	private ArrayList<Gene> genePool;

	// Result of learning
	double[] finalParams;

	public TetrisGeneticLearner() {

		// Initialize genes with random parameters
		genePool = new ArrayList<Gene>();
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			double[] params = new double[NUM_PARAMS];
			for (int j = 0; j < NUM_PARAMS; j++) {
				params[j] = Math.random() / (PARAMS_RANGE[1] - PARAMS_RANGE[0])
						+ PARAMS_RANGE[0];
			}
			genePool.add(new Gene(params));
		}
	}

	public void run() {

		int iteration = 0;

		while (true) {

			iteration++;

			// Obtain fitness for all genes
			geneSortedByFitness = new TreeSet<Gene>();
			for (int i = 0; i < genePool.size(); i++) {
				Gene gene = genePool.get(i);
				int totalScore = 0;
				for (int j = 0; j < GAMES_PER_GENE; j++) {
					totalScore += playGame(gene.params);
				}
				gene.fitness = totalScore / GAMES_PER_GENE;
				geneSortedByFitness.add(gene);
				// System.out.println("Gene " + i + " fitness: " + gene.fitness
				// + " total genes: " + geneSortedByFitness.size());
			}

			// Trim gene pool to sample size
			// In the event gene pool has undergone pairing
			if (genePool.size() > SAMPLE_SIZE) {
				for (int i = 0; i < genePool.size() - SAMPLE_SIZE; i++)
					geneSortedByFitness.pollLast();
			}

			Gene bestGene = geneSortedByFitness.first();
			Gene worstGene = geneSortedByFitness.last();
			System.out.println("Best fitness: " + bestGene.fitness
					+ " Worst fitness: " + worstGene.fitness);

			// Save result of this iteration
			try {
				File file = new File(RESULT_FILE_NAME + "_samp-" + SAMPLE_SIZE
						+ "_games-" + GAMES_PER_GENE + "_numPair-"
						+ NUM_GENES_TO_PAIR + ".csv");

				if (!file.exists())
					file.createNewFile();

				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(
						file, true));
				fileWriter.write(iteration + "," + bestGene.fitness + ","
						+ worstGene.fitness + ",");

				for (int i = 0; i < NUM_PARAMS; i++) {
					fileWriter.write(String.valueOf(bestGene.params[i]));
					if (i < NUM_PARAMS - 1)
						fileWriter.write(",");
					else
						fileWriter.write("\n");
				}

				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Exit if tolerance passed
			if ((double) bestGene.fitness / (double) worstGene.fitness - 1.0 < ERROR_TOLERANCE) {
				finalParams = new double[NUM_PARAMS];
				for (int i = 0; i < finalParams.length; i++)
					finalParams[i] = bestGene.params[i];
				return;
			}

			// Pair top genes
			LinkedList<Gene> newGenes = new LinkedList<Gene>();
			for (int i = 0; i < NUM_GENES_TO_PAIR; i += 2) {
				Gene gene1 = geneSortedByFitness.pollFirst();
				Gene gene2 = geneSortedByFitness.pollFirst();

				double[] newParams = new double[NUM_PARAMS];
				for (int j = 0; j < NUM_PARAMS; j++) {
					double newParam = (gene1.params[j] * gene1.fitness + gene2.params[j]
							* gene2.fitness)
							/ (gene1.fitness + gene2.fitness);
					newParams[j] = newParam;
				}

				// Mutate with a chance
				if (Math.random() < CHANCE_TO_MUTATE) {
					for (int j = 0; j < NUM_PARAMS; j++) {
						// Change each parameter by a random amount
						newParams[j] += Math.random()
								/ (MUTATION_RANGE[1] - MUTATION_RANGE[0])
								+ MUTATION_RANGE[0];
					}
				}
				newGenes.add(new Gene(newParams));
			}

			// Generate new gene poll
			while (!newGenes.isEmpty()) {
				genePool.add(newGenes.poll());
			}
		}
	}

	// Play game once and return fitness of parameters
	private int playGame(double[] params) {

		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton();

		int piecesPlayed = 0;

		while (!s.hasLost() && piecesPlayed < PIECES_PER_GAME) {

			s.makeMove(p.pickMove(s, s.legalMoves(), params));

			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			piecesPlayed++;
		}

		return s.getRowsCleared();
	}

	public static void main(String[] args) {

		if (args.length != 0) {
			SAMPLE_SIZE = Integer.valueOf(args[0]);
			GAMES_PER_GENE = Integer.valueOf(args[1]);
			NUM_GENES_TO_PAIR = Integer.valueOf(args[2]);
		}

		TetrisGeneticLearner learner = new TetrisGeneticLearner();
		learner.run();

		System.out.print("Final params: ");
		for (int i = 0; i < NUM_PARAMS; i++)
			System.out.print(learner.finalParams[i] + ", ");
	}
}

class Gene implements Comparable<Gene> {

	final static int INVALID_FITNESS = -999999;

	double[] params;
	int fitness;
	int order;

	public Gene(double[] params) {
		this.params = params;
		fitness = INVALID_FITNESS;
	}

	@Override
	public int compareTo(Gene g) {
		return g.fitness - fitness > 0 ? 1 : -1;
	}
}
