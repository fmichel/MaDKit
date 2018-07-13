/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.simulation.SimulationException;
import com.distrimind.madkit.simulation.activator.GenericBehaviorActivator;

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
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @see Scheduler
 * @see GenericBehaviorActivator
 * @version 6.0
 * 
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class Activator<A extends AbstractAgent> extends Overlooker<A> {

	private int nbOfsimultaneousTasks = 1;

	/**
	 * Builds a new Activator on the given CGR location of the artificial society
	 * with multicore mode set to <code>false</code>.
	 * 
	 * This has the same effect than <code>Activator(groups, role, true)</code>
	 * 
	 * @param groups the groups
	 * @param role the role
	 * @see Scheduler
	 */
	public Activator(AbstractGroup groups, String role) {
		super(groups, role);
	}

	/**
	 * Builds a new Activator on the given CGR location of the artificial society
	 * with multicore mode set to <code>false</code>.
	 * 
	 * @param groups the groups
	 * @param role the role
	 * @param unique
	 *            Tells if the function {@link Overlooker#getCurrentAgentsList()}
	 *            must returns unique references.
	 * @see Scheduler
	 */
	public Activator(AbstractGroup groups, String role, boolean unique) {
		super(groups, role, unique);
	}

	/**
	 * Call #execute(List&lt;A&gt; agentsList) on all the agents, i.e. using
	 * {@link Overlooker#getCurrentAgentsList()}.
	 * 
	 * By default, this is automatically called by the default scheduler's loop once
	 * the activator is added.
	 * 
	 * @param args
	 *            arguments that could be used by the scheduler to pass information
	 *            to this activator for an activation
	 * 
	 * @see Scheduler#doSimulationStep()
	 */
	public void execute(Object... args) {
		if (isMulticoreModeOn()) {
			multicoreExecute();
		} else {
			execute(getCurrentAgentsList(), args);
		}
	}

	/**
	 * This should define what has to be done on the agents for a simulation step.
	 * By default, this calls is automatically made using a list containing all the
	 * agents for this CGR, i.e. {@link Overlooker#getCurrentAgentsList()} is used
	 * by default.
	 * 
	 * When the multicore mode is on, the list is only a portion and this method
	 * will automatically be distributed over several threads. So, one has to take
	 * care about how the activator's fields are used here to avoid a
	 * {@link ConcurrentModificationException} for instance.
	 * 
	 * @param agentsList the agent list
	 * @param args
	 *            arguments that could be used by the scheduler to pass information
	 *            to this activator for an activation
	 */
	public abstract void execute(List<A> agentsList, Object... args);

	/**
	 * Executes the behavior on all the agents in a concurrent way, using several
	 * processor cores if available. This call decomposes the execution of the
	 * activator in {@link #nbOfParallelTasks()} tasks so that there are
	 * independently performed by the available core of the host.
	 * <br>
	 * Default implementation Beware that using this call will produce different
	 * outputs for each run unless a concurrent simulation model is used. That is to
	 * say, a model supporting concurrent phases in the simulation execution such as
	 * the <a href=
	 * "http://www.aamas-conference.org/Proceedings/aamas07/html/pdf/AAMAS07_0179_07a7765250ef7c3551a9eb0f13b75a58.pdf">IRM4S
	 * model</a>
	 * @param args the arguments
	 * 
	 */
	protected void multicoreExecute(final Object... args) {
		final int cpuCoreNb = nbOfParallelTasks();
		final ArrayList<Callable<Void>> workers = new ArrayList<>(cpuCoreNb);
		final List<A> list = getCurrentAgentsList();
		int bucketSize = list.size();
		final int nbOfAgentsPerTask = bucketSize / cpuCoreNb;
		for (int i = 0; i < cpuCoreNb; i++) {
			final int index = i;
			workers.add(new Callable<Void>() {
				public Void call() {
					int firstIndex = nbOfAgentsPerTask * index;// TODO check that using junit
					execute(list.subList(firstIndex, firstIndex + nbOfAgentsPerTask), args);
					return null;
				}
			});
		}
		workers.add(new Callable<Void>() {
			public Void call() {
				execute(list.subList(nbOfAgentsPerTask * cpuCoreNb, list.size()), args);
				return null;
			}
		});
		try {
			this.getMadkitServiceExecutor().invokeAll(workers);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();// do not swallow it !
		}
	}

	@Override
	public String toString() {
		return super.toString() + (isMulticoreModeOn() ? " multicore mode " : "");
	}

	/**
	 * @return <code>true</code> if the multicore mode is on. I.e.
	 *         {@link #nbOfParallelTasks()} &gt; 1. This method could be used by the
	 *         default behavior of scheduler agents as they test in which mode each
	 *         activator has to be used.
	 */
	public boolean isMulticoreModeOn() {
		return nbOfsimultaneousTasks > 1;
	}

	/**
	 * Sets the number of tasks which will be used on a multicore architecture. If
	 * set to a number greater than 1, the scheduler will automatically use
	 * {@link #multicoreExecute(Object...)} on this activator when
	 * {@link Activator#execute(Object...)} is called. If set to 1, the agents are
	 * sequentially activated. Beware that this is the only way to do exact
	 * replication of simulations, unless you have clear specifications for your
	 * model, see {@link #multicoreExecute(Object...)}.
	 * 
	 * @param nbOfParallelTasks
	 *            the number of simultaneous tasks that this activator will use to
	 *            make a step. Default is 1 upon creation, so that
	 *            {@link #isMulticoreModeOn()} returns <code>false</code>.
	 */
	public void useMulticore(int nbOfParallelTasks) {
		nbOfsimultaneousTasks = nbOfParallelTasks < 2 ? 1 : nbOfParallelTasks;
	}

	/**
	 * Returns the number of tasks that will be created by this activator in order
	 * to benefit from multicore platforms.
	 * 
	 * @return the number of tasks that will be created.
	 */
	public int nbOfParallelTasks() {
		return nbOfsimultaneousTasks;
	}

	/**
	 * Returns the MDK ExecutorService that executes task in parallel
	 * 
	 * @return an ExecutorService running task in parallel
	 */
	public ExecutorService getMadkitServiceExecutor() {
		return madkit_kernel.get().getMadkitServiceExecutor();
	}

	/**
	 * Returns the agent's method named <code>methodName</code> considering a given
	 * agentClass. This also works for the private methods of the class, even
	 * inherited ones.
	 * 
	 * 
	 * @param agentClass
	 *            the targeted agent
	 * @param methodName
	 *            the name of the method
	 * @param <T> the class type
	 * @return the agent's method named <code>methodName</code>
	 * 
	 * @throws NoSuchMethodException if the method was not found
	 */
	// * This also works on <code>private</code> field.
	@SuppressWarnings("unchecked")
	public static <T> Method findMethodOn(Class<T> agentClass, final String methodName) throws NoSuchMethodException {
		Method m;
		while (true) {
			try {
				m = agentClass.getDeclaredMethod(methodName);
				if (m != null) {
					if (!m.isAccessible()) {// TODO seems to be always the case the first time
						m.setAccessible(true);
					}
					return m;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				agentClass = (Class<T>) agentClass.getSuperclass();// TODO not go further than A
				if (agentClass == AbstractAgent.class) {// TODO bench vs local variable
					throw e;
				}
			}
		}
	}

	/**
	 * This method handle exception generated by an agent function call.
	 * 
	 * If this agent function call generated a SelfKillException, than this function
	 * will try to kill properly the considered agent.
	 * 
	 * @param e
	 *            the generated exception
	 * @param default_generated_exception
	 *            the exception thrown by this function if the exception
	 *            <code>e</code> cannot be handled.
	 * @return true
	 * @throws SimulationException
	 *             if the given generated exception <code>e</code> cannot be
	 *             handled, or if its handle generates another exception.
	 */
	protected boolean handleException(Throwable e, SimulationException default_generated_exception)
			throws SimulationException {
		if (e instanceof SelfKillException) {
			SelfKillException ske = (SelfKillException) e;
			try {
				ske.agent.tryToCompleteKill(ske);
			} catch (Exception e2) {
				throw new SimulationException(toString() + " on kill process of agent " + ske.agent, e2);
			}
			return true;
		} else
			throw default_generated_exception;
	}

}