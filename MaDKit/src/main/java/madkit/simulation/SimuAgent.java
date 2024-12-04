/**
 * 
 */
package madkit.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;

import madkit.kernel.AbstractScheduler;
import madkit.kernel.Agent;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;
import madkit.simulation.activator.DiscreteEventAgentsActivator;

/**
 * Represents an agent in a simulation environment. This agent is linked to a
 * {@link SimulationEngine} and can manage simulation-related tasks such as
 * launching other agents, accessing simulation models, and managing roles
 * within the simulation community.
 */
public class SimuAgent extends Agent {

	SimulationEngine simuEngine;

	/**
	 * Retrieves the simulation engine associated with this agent.
	 *
	 * @return the {@link SimulationEngine} instance.
	 * @throws IllegalStateException if the agent has not been launched or was not
	 *                               launched by a {@link SimuAgent}.
	 */
	public SimulationEngine getSimuEngine() {
		if (simuEngine == null) {
			throw new IllegalStateException("Agent not yet launched, or not launched by a SimuAgent");
		}
		return simuEngine;
	}

	/**
	 * Launches a new agent. If the agent being launched is also a
	 * {@link SimuAgent}, it will inherit the simulation engine from this agent.
	 *
	 * @param a       the agent to be launched.
	 * @param timeout the maximum time to wait for the agent to launch.
	 * @return the result of the launch operation as a {@link ReturnCode}.
	 */
	@Override
	public ReturnCode launchAgent(Agent a, int timeout) {
		/*
		 * Any agent launched by an agent linked to a simuEngine becomes part of this
		 * engine. The root one being a SimuEngine itself.
		 */
		if (simuEngine != null && a instanceof SimuAgent sa) {
			sa.simuEngine = simuEngine;
		}
		return super.launchAgent(a, timeout);
	}

	/**
	 * Returns the community associated with the simulation.
	 *
	 * @return the name of the simulation community.
	 */
	public String getCommunity() {
		return getSimuEngine().getCommunity();
	}

	/**
	 * Returns the engine group associated with the simulation.
	 *
	 * @return the name of the engine group.
	 */
	public String getEngineGroup() {
		return getSimuEngine().getEngineGroup();
	}

	/**
	 * Returns the model group associated with the simulation.
	 *
	 * @return the name of the model group.
	 */
	public String getModelGroup() {
		return getSimuEngine().getModelGroup();
	}

	/**
	 * Retrieves the scheduler associated with the simulation engine.
	 *
	 * @param <S> the type of the scheduler.
	 * @return the scheduler instance.
	 */
	public <S extends AbstractScheduler<?>> S getScheduler() {
		return getSimuEngine().getScheduler();
	}

	/**
	 * Retrieves the simulation model associated with this agent.
	 *
	 * @param <M> the type of the simulation model.
	 * @return the simulation model instance.
	 */
	public <M extends SimulationModel> M getModel() {
		return getSimuEngine().getModel();
	}

	/**
	 * Retrieves the environment associated with the simulation.
	 *
	 * @param <E> the type of the environment.
	 * @return the environment instance.
	 */
	public <E extends Environment> E getEnvironment() {
		return getSimuEngine().getEnvironment();
	}

	/**
	 * Retrieves the pseudo-random number generator used in the simulation model.
	 *
	 * @return the {@link RandomGenerator} instance.
	 */
	public RandomGenerator prng() {
		return getModel().prng();
	}

	/**
	 * Retrieves the list of viewer agents associated with the simulation.
	 *
	 * @return a list of {@link SimuAgent} viewers.
	 */
	public List<SimuAgent> getViewers() {
		return getSimuEngine().getViewers();
	}

	/**
	 * Method to be called on simulation startup. This can be overridden by
	 * subclasses to perform initialization tasks.
	 */
	public void onSimuStartup() {
		// Override to implement startup behavior
	}

	/**
	 * Requests a role within the simulation community.
	 *
	 * @param group the group within which the role is requested.
	 * @param role  the role to be requested.
	 * @return the result of the role request as a {@link ReturnCode}.
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
	 * Returns the {@link SimulationTimer} of the current simulation. This is
	 * automatically initialized when the agent is associated with an activator for
	 * the first time. So it stays <code>null</code> if the agent is not related to
	 * a simulation
	 *
	 * @return the simulationTime of the simulation in which the agent participates
	 * @throws NullPointerException if this agent is not part of a simulation
	 */
	@SuppressWarnings("unchecked")
	public <T extends SimulationTimer<?>> T getSimuTimer() {// TODO move this in SimuParticipant
		return simuEngine != null ? (T) simuEngine.getScheduler().getSimuTimer() : null;
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
