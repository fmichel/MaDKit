package madkit.simulation;

import static madkit.simulation.DefaultOrganization.MODEL_ROLE;

import java.util.Comparator;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import madkit.kernel.Watcher;

/**
 * @author Fabien Michel
 *
 */
public class SimulationModel extends Watcher {

	private RandomGenerator randomGenerator;
	/**
	 * Need a many seed bits, and then increment on it
	 */
	private long initialSeed = 0xFEDCBA0987654321L;

	public SimulationModel() {
		randomGenerator = getBest().create(initialSeed);
	}

	@Override
	protected void onActivation() {
		getSimuEngine().setModel(this);
		requestSimuRole(getEngineGroup(), MODEL_ROLE);
	}

	private RandomGeneratorFactory<RandomGenerator> getBest() {
		return RandomGeneratorFactory.all().filter(rgf -> !rgf.name().equals("SecureRandom")) // SecureRandom has
																															// MAX_VALUE stateBits.
				.sorted(Comparator.comparingInt(RandomGeneratorFactory<RandomGenerator>::stateBits).reversed()).findFirst()
				.orElse(RandomGeneratorFactory.of("Random"));
	}

	/**
	 * Initialize the PRNG with a new seed. The seed is computed by adding seedIndex
	 * to the built-in long (0xFEDCBA0987654321L) which is used as initial seed.
	 * This is done so that the obtained long respects the many seed bits
	 * characteristic. Moreover it is known that a good practice, considering how
	 * seeds should be chosen, is to take them in sequence. See this blog: <a href=
	 * "https://www.johndcook.com/blog/2016/01/29/random-number-generator-seed-mistakes">Random
	 * number generator seed mistakes</a> So a simulation suite can be obtained by
	 * using this method with a consecutive list of int: 1, 2, 3...
	 * 
	 * @param seedIndex
	 */
	public void setPRNGSeed(int seedIndex) {
		initialSeed += seedIndex;
		randomGenerator = getBest().create(initialSeed);
	}

	/**
	 * @return the randomGenerator
	 */
	@Override
	public RandomGenerator prng() {
		return randomGenerator;
	}

	/**
	 * @param randomGenerator the randomGenerator to set
	 */
	public void setRandomGnerator(RandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	public static void main(String[] args) {
		SimulationModel name = new SimulationModel();
		RandomGeneratorFactory<RandomGenerator> best = name.getBest();
	}

}
