package madkit.simulation;

import static madkit.simulation.SimuOrganization.MODEL_ROLE;

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


	/**
	 * Constructs a simulation model with a default starting seed.
	 */
	public SimuModel() {
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

}
