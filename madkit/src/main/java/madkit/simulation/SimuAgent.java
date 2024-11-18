/**
 * 
 */
package madkit.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import madkit.kernel.AbstractScheduler;
import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;
import madkit.simulation.activator.DiscreteEventAgentsActivator;

/**
 * 
 */
public class SimuAgent extends Agent {

	SimulationEngine simuEngine;

	public SimulationEngine getSimuEngine() {
		return simuEngine;
	}

	/**
	 * @param a
	 * @param timeout
	 * @return
	 */
	@Override
	protected ReturnCode launchAgent(Agent a, int timeout) {
		/*
		 * any agent launched by an agent linked to a simuEngine becomes part of this
		 * engine. The root one being a SimuEgine itself
		 */
		if (simuEngine != null && a instanceof SimuAgent sa) {
			sa.simuEngine = simuEngine;
		}
		return super.launchAgent(a, timeout);
	}

	/**
	 * Returns the simulation community
	 * 
	 * @return the community
	 */
	public String getCommunity() {
		return getSimuEngine().getCommunity();
	}

	public String getEngineGroup() {
		return getSimuEngine().getEngineGroup();
	}

	public String getModelGroup() {
		return getSimuEngine().getModelGroup();
	}

	public <S extends AbstractScheduler<?>> S getScheduler() {
		return getSimuEngine().getScheduler();
	}

	public <M extends SimulationModel> M getModel() {
		return getSimuEngine().getModel();
	}

	public <E extends Environment> E getEnvironment() {
		return getSimuEngine().getEnvironment();
	}

	public List<SimuAgent> getViewers() {
		return getSimuEngine().getViewers();
	}

	public void onInitialization() {
	}

	public void onSimulationStart() {
	}
	
	protected ReturnCode requestSimuRole(String group, String role) {
		return requestRole(getCommunity(), group, role);
	}

	protected ReturnCode createSimuGroup(String group) {
		return createGroup(getCommunity(), group);
	}

	protected ReturnCode leaveSimuRole(String group, String role) {
		return leaveRole(getCommunity(), group, role);
	}

	protected ReturnCode leaveSimuGroup(String group) {
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
