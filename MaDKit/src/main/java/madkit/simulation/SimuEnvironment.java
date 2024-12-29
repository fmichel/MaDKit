package madkit.simulation;

import static madkit.simulation.SimuOrganization.ENVIRONMENT_ROLE;

import madkit.kernel.Watcher;

/**
 * This class represents an environment in which agents evolve. It is a
 * specialized watcher that should be extended to implement the environment of
 * the simulated agents.
 * 
 *
 */
public class SimuEnvironment extends Watcher {

	/**
	 * On activation, the environment is added to the simulation engine and requests
	 * the {@link SimuOrganization#ENVIRONMENT_ROLE} in both the
	 * {@link SimuOrganization#MODEL_GROUP} and the
	 * {@link SimuOrganization#ENGINE_GROUP}. The
	 * {@link SimuOrganization#MODEL_GROUP} group is created by this agent.
	 */
	@Override
	protected void onActivation() {
		getLauncher().setEnvironment(this);
		createGroup(getCommunity(), getModelGroup());
		requestRole(getCommunity(), getModelGroup(), ENVIRONMENT_ROLE);
		requestRole(getCommunity(), getEngineGroup(), ENVIRONMENT_ROLE);
	}
}
