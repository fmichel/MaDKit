package madkit.kernel;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

import madkit.reflection.MethodFinder;
import madkit.simulation.SimulationTimer;
import madkit.simulation.activator.MethodActivator;

/**
 * This class defines a tool for scheduling mechanism. An activator is
 * configured according to a community, a group and a role. It could be used to
 * activate a group of agents on a particular behavior (a method of the agent's
 * class) Subclasses should override {@link #execute(List, Object...)} for
 * defining how a sequential execution of a list of agents take place. By
 * default, this list corresponds to all the agents in a single core mode or to
 * partial views of the entire list when the multicore mode is used. The
 * multicore mode is set to <code>false</code> by default.
 *
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @since MaDKit 2.0
 * @see Scheduler
 * @see MethodActivator
 * @version 6.0
 */
public abstract class Activator extends Overlooker implements Comparable<Activator> {

	private int priority = 0;

	private boolean multicoreOn = false;

	private AbstractScheduler<?> scheduler;

	/**
	 * Builds a new Activator on the given CGR location of the artificial society
	 * with multicore mode set to <code>false</code>. This has the same effect as
	 * <code>Activator(community, group, role, false)</code>.
	 *
	 * @param community
	 * @param group
	 * @param role
	 * @see Scheduler
	 */
	public Activator(String community, String group, String role) {
		super(community, group, role);
	}

	@Override
	public int compareTo(Activator o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	/**
	 * @return <code>true</code> if the multicore mode is on, <i>i.e.</i> the
	 *         activator triggers agents behaviors in parallel
	 */
	public boolean isMulticoreOn() {
		return multicoreOn;
	}

	/**
	 * Change the multicore use
	 * 
	 * @param multicoreOn the multicoreOn mode to set
	 */
	public void setMulticoreOn(boolean multicoreOn) {
		this.multicoreOn = multicoreOn;
	}

	/**
	 * @param scheduler the scheduler to set
	 */
	public void setScheduler(AbstractScheduler<?> scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * The priority of this activator when conflicting with another Activator. A
	 * lesser priority means that the activator will be triggered first. When the
	 * activator's priority is not set, a default priority is defined using the
	 * order in which the activators are added to the simulation engine using
	 * {@link Scheduler#addActivator(Activator)}, that is the first activator has
	 * priority 0, the second has 1, and so on.
	 *
	 * Setting the activator's priority should be done using
	 * {@link Scheduler#setActivatorPriority(Activator, int)} so that the scheduler
	 * can maintain the ordering coherence of the activators list.
	 *
	 * By default, when two activators have the same priority, the order of
	 * activation is undefined.
	 *
	 * @return the priority of this scheduler.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the scheduler
	 */
	public AbstractScheduler<? extends SimulationTimer<?>> getScheduler() {
		return scheduler;
	}

	/**
	 * Sets the priority of the activator
	 *
	 * @param priority an int defining the activator's priority
	 */
	void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Trigger the execution of this activator. For instance, subclasses can use the
	 * {@link Overlooker#getCurrentAgentsList()} to make all the agents do some when
	 * this method is called.
	 * 
	 * @param args arguments that could be passed by the scheduler
	 * @see Scheduler#onSimulationStep()
	 */
	public abstract void execute(Object... args);

	/**
	 * Executes a specific method on a targeted agent.
	 *
	 * @param agent        the targeted agent.
	 * @param behaviorName the name of a method belonging to the agents (even
	 *                     private or inherited ones)
	 * @param args         parameters to be passed for the invocation
	 * @throws ReflectiveOperationException
	 */
	public static void executeBehaviorOf(Agent agent, String behaviorName, Object... args) {
		try {
			Method m = MethodFinder.getMethodOn(agent.getClass(), behaviorName, args);
			m.invoke(agent,args);
		} catch (Throwable e) {
			agent.getLogger().log(Level.SEVERE, e, () -> "Cannot execute behavior " + behaviorName);
			throw new RuntimeException(e);
		}
	}

//	/**
//	 * This should define what has to be done on the agents for a simulation step.
//	 * By default, this calls is automatically made using a list containing all the
//	 * agents for this CGR, i.e. {@link Overlooker#getCurrentAgentsList()} is used
//	 * by default. When the multicore mode is on, the list is only a portion and
//	 * this method will automatically be distributed over several threads. So, one
//	 * has to take care about how the activator's fields are used here to avoid a
//	 * {@link ConcurrentModificationException} for instance.
//	 *
//	 * @param agentsList
//	 */
//	public abstract void execute(List<A> agentsList, Object... args);

	@Override
	public String toString() {
		return "P(" + getPriority() + ") " + super.toString();
	}

	/**
	 * Get the {@link SimulationTime} associated with the simulation
	 *
	 * @return the simulationTime associated with the simulation
	 */
	public SimulationTimer<?> getSimuTimer() {
		return getScheduler().getSimuTimer();
	}
}