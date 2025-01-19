/**
 * 
 */
package madkit.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;

import madkit.kernel.Agent;
import madkit.kernel.Scheduler;
import madkit.simulation.scheduler.DateBasedDiscreteEventActivator;
import madkit.simulation.scheduler.DateBasedTimer;
import madkit.simulation.scheduler.DiscreteEventAgentsActivator;
import madkit.simulation.scheduler.SimuTimer;

/**
 * An agent class designed to be used with the default simulation engine provided by this
 * package. This class provides a number of convenience methods for interacting with the
 * simulation engine and other agents within the simulation.
 * 
 * A SimuAgent has to be launched by a {@link SimuLauncher}, or transitively by another
 * agent launched by a SimuLauncher. This is necessary for the SimuAgent to be linked to a
 * {@link SimuLauncher}.
 * 
 * This ensures that the agent has access to the simulation model, environment, scheduler,
 * and other simulation-specific components.
 * 
 * Essentially, this ensures that all the agents of one simulation instance share the same
 * pseudo-random number generator and simulation time.
 * 
 * If the agent is not launched by a SimuLauncher, it will throw an
 * {@link IllegalStateException} when attempting to access these components.
 * 
 * @see SimuLauncher
 */
public class SimuAgent extends Agent {

	SimuLauncher simuLauncher;

	/**
	 * Retrieves the simulation engine associated with this agent.
	 *
	 * @return the {@link SimuLauncher} instance.
	 * @throws IllegalStateException if the agent has not been launched or was not
	 *                               launched by a {@link SimuAgent}.
	 */
	public SimuLauncher getLauncher() {
		if (simuLauncher == null) {
			throw new IllegalStateException(
					"SimuAgent not yet launched, or not launched by a SimuLauncher transitively. See Javadoc for details.");
		}
		return simuLauncher;
	}

	/**
	 * Launches a new agent. If the agent being launched is also a
	 * {@link SimuAgent}, it will inherit the simulation engine from this agent.
	 *
	 * @param agent   the agent to be launched.
	 * @param timeout the maximum time to wait for the agent to launch.
	 * @return the result of the launch operation as a
	 *         {@link madkit.kernel.Agent.ReturnCode}.
	 */
	@Override
	public ReturnCode launchAgent(Agent agent, int timeout) {
		/*
		 * Any agent launched by an agent linked to a simuLauncher becomes part of this
		 * engine. The root one being a SimuLauncher itself.
		 */
		if (simuLauncher != null && agent instanceof SimuAgent sa) {
			sa.simuLauncher = simuLauncher;

		}
		return super.launchAgent(agent, timeout);
	}

	/**
	 * Returns the community associated with the simulation.
	 *
	 * @return the name of the simulation community.
	 */
	public String getCommunity() {
		return getLauncher().getCommunity();
	}

	/**
	 * Returns the engine group associated with the simulation.
	 *
	 * @return the name of the engine group.
	 */
	public String getEngineGroup() {
		return getLauncher().getEngineGroup();
	}

	/**
	 * Returns the model group associated with the simulation.
	 *
	 * @return the name of the model group.
	 */
	public String getModelGroup() {
		return getLauncher().getModelGroup();
	}

	/**
	 * Retrieves the scheduler associated with the simulation engine.
	 *
	 * @param <S> the type of the scheduler.
	 * @return the scheduler instance.
	 */
	public <S extends Scheduler<?>> S getScheduler() {
		return getLauncher().getScheduler();
	}

	/**
	 * Retrieves the simulation model associated with this agent.
	 *
	 * @param <M> the type of the simulation model.
	 * @return the simulation model instance.
	 */
	public <M extends SimuModel> M getModel() {
		return getLauncher().getModel();
	}

	/**
	 * Retrieves the environment associated with the simulation.
	 *
	 * @param <E> the type of the environment.
	 * @return the environment instance.
	 */
	public <E extends SimuEnvironment> E getEnvironment() {
		return getLauncher().getEnvironment();
	}

	/**
	 * Retrieves the pseudo-random number generator used in the simulation model.
	 *
	 * @return the {@link RandomGenerator} instance.
	 */
	@Override
	public RandomGenerator prng() {
		return getLauncher().prng();
	}

	/**
	 * Retrieves the list of viewer agents associated with the simulation.
	 *
	 * @return a list of {@link SimuAgent} viewers.
	 */
	public List<SimuAgent> getViewers() {
		return getLauncher().getViewers();
	}

	/**
	 * Method to be called on simulation startup. This can be overridden by
	 * subclasses to perform initialization tasks.
	 */
	public void onSimulationStart() {
		// Override to implement startup behavior
	}

	/**
	 * Requests a role within the simulation community.
	 *
	 * @param group the group within which the role is requested.
	 * @param role  the role to be requested.
	 * @return the result of the role request as a
	 *         {@link madkit.kernel.Agent.ReturnCode}.
	 */
	public ReturnCode requestSimuRole(String group, String role) {
		return requestRole(getCommunity(), group, role);
	}

	/**
	 * Creates a simulation group with the specified name.
	 *
	 * @param group the name of the group to be created
	 * @return a ReturnCode indicating the result of the group creation
	 */
	public ReturnCode createSimuGroup(String group) {
		return createGroup(getCommunity(), group);
	}

	/**
	 * Leaves a specified role in the given simulation group.
	 *
	 * @param group the name of the group from which to leave the role
	 * @param role  the name of the role to be left
	 * @return a ReturnCode indicating the result of leaving the role
	 */
	public ReturnCode leaveSimuRole(String group, String role) {
		return leaveRole(getCommunity(), group, role);
	}

	/**
	 * Leaves the specified simulation group.
	 *
	 * @param group the name of the group to be left
	 * @return a ReturnCode indicating the result of leaving the group
	 */
	public ReturnCode leaveSimuGroup(String group) {
		return leaveGroup(getCommunity(), group);
	}

	/**
	 * Returns the {@link SimuTimer} of the current simulation. This is
	 * automatically initialized when the agent is associated with an activator for
	 * the first time. So it stays <code>null</code> if the agent is not related to
	 * a simulation
	 *
	 * @return the simulationTime of the simulation in which the agent participates
	 * @throws NullPointerException if this agent is not part of a simulation
	 */
	@SuppressWarnings("unchecked")
	public <T extends SimuTimer<?>> T getSimuTimer() {
		return simuLauncher != null ? (T) simuLauncher.getScheduler().getSimuTimer() : null;
	}

	/**
	 * Method which is used by discrete-event simulation activators for doing
	 * fine-grained simulations. By default, this method returns an event which is
	 * one second ahead of the current date of the simulation. So, this method can
	 * be overridden to fulfill the simulation requirement
	 *
	 * @return the date of the next event for this agent.
	 *
	 * @see DiscreteEventAgentsActivator
	 * @see DateBasedDiscreteEventActivator
	 * @throws ClassCastException if the agent is not part a simulation whose
	 *                            scheduling is based on a {@link DateBasedTimer}
	 */
	public LocalDateTime getNextEventDate() throws ClassCastException {
		DateBasedTimer time = getSimuTimer();
		return time.getCurrentTime().plus(Duration.ofSeconds(1));
	}

}
