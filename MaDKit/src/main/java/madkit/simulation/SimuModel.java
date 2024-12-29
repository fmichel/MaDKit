package madkit.simulation;

import static madkit.simulation.SimuOrganization.MODEL_ROLE;

import java.util.Comparator;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import madkit.kernel.Probe;
import madkit.kernel.Watcher;

/**
 * This class is an engine agent that is in charge of managing core parts of the
 * simulation model.
 * <p>
 * Essentially, it defines the pseudo random number generator (PRNG) that has to
 * be used by the simulation agents for ensuring the reproducibility of the
 * simulation. The PRNG is initialized with a seed that can be set by the user.
 * The seed is a long integer that can be changed using the
 * {@link #setPRNGSeed(int)} method.
 * <p>
 * Moreover, it is a watcher agent and can thus use {@link Probe} to monitor the
 * simulation agents. It can also implement the {@link #onSimulationStart()}
 * method to perform actions when the simulation starts.
 * <p>
 * 
 * @see Watcher
 * @since MaDKit 6.0
 */
public class SimuModel extends Watcher {

	private RandomGenerator randomGenerator;
	/**
	 * Need a many seed bits, and then increment on it
	 */
	private long initialSeed = 0xFEDCBA0987654321L;

	/**
	 * Constructs a simulation model with a default starting seed.
	 */
	public SimuModel() {
		randomGenerator = getBest().create(initialSeed);
	}

	/**
	 * This method is called when the agent is activated. It is used to request the
	 * role {@link SimuOrganization#MODEL_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}.
	 */
	@Override
	protected void onActivation() {
		getLauncher().setModel(this);
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
	 * This method is called when the simulation starts. By default, it calls
	 * {@link #setPRNGSeed(int)} with 0 as parameter. It can be overridden to
	 * perform specific actions when the simulation starts.
	 * 
	 */
	@Override
	public void onSimulationStart() {
		setPRNGSeed(0);
	}

	/**
	 * Returns the pseudo random number generator that has to be used by the
	 * simulation agents.
	 * 
	 * @return the pseudo random number generator of the simulation
	 */
	@Override
	public RandomGenerator prng() {
		return randomGenerator;
	}

	/**
	 * Sets the pseudo random number generator that has to be used by the
	 * simulation.
	 * 
	 * @param randomGenerator the randomGenerator to set
	 */
	public void setRandomGnerator(RandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

}
