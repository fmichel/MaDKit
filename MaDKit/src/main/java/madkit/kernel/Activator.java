/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import java.lang.reflect.Method;
import java.util.logging.Level;

import madkit.reflection.MethodFinder;
import madkit.simulation.SimuException;
import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.SimuTimer;

/**
 * This class defines a tool for scheduling mechanism. An activator is configured
 * according to a community, a group and a role.
 * <p>
 * Subclasses should override {@link #execute(Object...)} for defining how a sequential
 * execution of a list of agents take place. By default, this list corresponds to all the
 * agents having the group and role defined in the activator.
 *
 * @since MaDKit 2.0
 * @see Scheduler
 * @see MethodActivator
 * @version 6.0
 */
public abstract class Activator extends Overlooker implements Comparable<Activator> {

	private int priority = 0;

	private Scheduler<?> scheduler;

	/**
	 * Builds a new Activator on the given CGR location of the artificial society. Once
	 * created, it has to be added by a {@link Scheduler} agent using the
	 * {@link Scheduler#addActivator(Activator)}.
	 *
	 * @param community the name of the community where the Activator will operate
	 * @param group     the name of the group within the community
	 * @param role      the role assigned to the Activator within the group
	 */
	protected Activator(String community, String group, String role) {
		super(community, group, role);
	}

	/**
	 * Builds a new Activator on the given CGR location of the artificial society, without
	 * specifying the community. This constructor is used to simplify declaration when used
	 * with the default implementation of a simulation engine provided in the
	 * madkit.simulation package. Once created, it has to be added by a {@link Scheduler}
	 * agent using the {@link Scheduler#addActivator(Activator)}.
	 *
	 * @param group the name of the group within the community
	 * @param role  the role assigned to the Activator within the group
	 */
	protected Activator(String group, String role) {
		this(null, group, role);
	}

	/**
	 * Implementing the {@link Comparable} interface for sorting activators according to their
	 * priority.
	 */
	@Override
	public int compareTo(Activator o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	/**
	 * Two activators are equals if they have the same community, group, role and priority.
	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Activator a) {
//			return a.getCommunity().equals(getCommunity()) && a.getGroup().equals(getGroup())
//					&& a.getRole().equals(getRole()) && a.getPriority() == getPriority();
//		}
//		return false;
//	}

	/**
	 * The priority of this activator when conflicting with another Activator. A lesser
	 * priority means that the activator will be triggered first. Default priority is 0.
	 *
	 * By default, when two activators have the same priority, the order of activation is not
	 * guaranteed.
	 *
	 * @return the priority of this activator.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Gets the scheduler that manages this activator.
	 * 
	 * @return the scheduler that manages this activator.
	 */
	@SuppressWarnings("unchecked")
	public <S extends Scheduler<?>> S getScheduler() {
		return (S) scheduler;
	}

	/**
	 * Sets the priority of the activator. see {@link #getPriority()}
	 *
	 * @param priority an int defining the activator's priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
		scheduler.updateActivatorsSchedule();
	}

	/**
	 * Trigger the execution of this activator. For instance, subclasses can use the
	 * {@link #getAgents()} to make all the agents do something. This method should be called
	 * by the scheduler.
	 * 
	 * @param args arguments that could be passed by the scheduler
	 * @see Scheduler#doSimulationStep()
	 */
	public abstract void execute(Object... args);

	/**
	 * Executes a specific method on a targeted agent.
	 *
	 * @param agent        the targeted agent.
	 * @param behaviorName the name of a method belonging to the agents (even private or
	 *                     inherited ones)
	 * @param args         parameters to be passed for the invocation
	 */
	public static void executeBehaviorOf(Agent agent, String behaviorName, Object... args) {
		try {
			Method m = MethodFinder.getMethodOn(agent.getClass(), behaviorName, args);
			m.invoke(agent, args);
		} catch (Exception e) {
			agent.getLogger().log(Level.SEVERE, e, () -> "Cannot execute behavior " + behaviorName);
			throw new SimuException("Cannot execute behavior " + behaviorName, e);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " Priority(" + getPriority() + ")";
	}

	/**
	 * Get the {@link SimuTimer} associated with the simulation
	 *
	 * @return the simulationTime associated with the simulation
	 */
	public SimuTimer<?> getSimuTimer() {
		return getScheduler().getSimuTimer();
	}

	/**
	 * Set the scheduler that manages this activator.
	 * 
	 * @param scheduler the scheduler to set
	 */
	void setScheduler(Scheduler<?> scheduler) {
		this.scheduler = scheduler;
	}

}