/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Callable;

import madkit.simulation.activator.GenericBehaviorActivator;

/**
 * This class defines a tool for scheduling mechanism.
 * An activator is configured according to a community, a group and a role.
 * It could be used to activate a group of agents on a particular behavior (a method of the agent's class)
 * Subclasses should override {@link #execute(List)} for defining how 
 * a sequential execution of a list of agents take place. By default, this list 
 * corresponds to all the agents in a single core mode or to partial views of
 * the entire list when the multicore mode is used.
 * The multicore mode is set to <code>false</code> by default.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @since MaDKit 2.0
 * @see Scheduler
 * @see GenericBehaviorActivator 
 * @version 5.0
 * 
 */
public abstract class Activator<A extends AbstractAgent> extends Overlooker<A>{

	private int nbOfsimultaneousTasks = 1;
	/**
	 * Builds a new Activator on the given CGR location of the
	 * artificial society with multicore mode set to <code>false</code>.
	 * This has the same effect as 
	 * <code>Activator(community, group, role, false)</code>.
	 * @param community
	 * @param group
	 * @param role
	 * @see Scheduler
	 */
	public Activator(String community, String group, String role) {
		super(community, group, role);
	}

	/**
	 * Call #execute(List<A> agentsList) on all the agents, i.e. using 
	 * {@link Overlooker#getCurrentAgentsList()}.
	 * 
	 * By default, this is automatically called by the default scheduler's 
	 * loop once the activator is added.
	 * 
	 * @see Scheduler#doSimulationStep()
	 */
	public void execute() {
		if (isMulticoreModeOn()) {
			multicoreExecute();
		}
		else{
			execute(getCurrentAgentsList());
		}
	}

	/**
	 * This should define what has to be done on the agents
	 * for a simulation step. By default, this calls is automatically made
	 * using a list containing all the agents for this CGR, 
	 * i.e. {@link Overlooker#getCurrentAgentsList()} is used by default.
	 * 
	 * When the multicore mode is on, the list is only a portion and
	 * this method will automatically be distributed over several threads.
	 * So, one has to take care about how the activator's fields are used
	 * here to avoid a {@link ConcurrentModificationException} for instance.
	 * 
	 * @param agentsList
	 */
	public abstract void execute(List<A> agentsList);

	/**
	 * Executes the behavior on all the agents in a concurrent way, using several processor cores if available.
	 * This call decomposes the execution of the activator in {@link #nbOfParallelTasks()} tasks so that
	 * there are independently performed by the available core of the host.
	 * <p>
	 * Default implementation 
	 * Beware that using this call will produce different outputs for each run unless a concurrent simulation
	 * model is used. That is to say, a model supporting concurrent phases in the simulation execution such as the
	 * <a href="http://www.aamas-conference.org/Proceedings/aamas07/html/pdf/AAMAS07_0179_07a7765250ef7c3551a9eb0f13b75a58.pdf">IRM4S model<a/>
	 * 
	 */
	protected void multicoreExecute() {
		final int cpuCoreNb = nbOfParallelTasks();
		final ArrayList<Callable<Void>> workers = new ArrayList<Callable<Void>>(cpuCoreNb);
		final List<A> list = getCurrentAgentsList();
		int bucketSize = list.size();
		final int nbOfAgentsPerTask = bucketSize / cpuCoreNb;
		for (int i = 0; i < cpuCoreNb; i++) {
			final int index = i;
			workers.add(new Callable<Void>() {
				public Void call() throws Exception {
					int firstIndex = nbOfAgentsPerTask*index;//TODO check that using junit
					execute(list.subList(firstIndex, firstIndex+nbOfAgentsPerTask));
					return null;
				}
			});
		}
		workers.add(new Callable<Void>() {
			public Void call() throws Exception {
				execute(list.subList(nbOfAgentsPerTask*cpuCoreNb, list.size()));
				return null;
			}
		});
		try {
			getMadkitServiceExecutor().invokeAll(workers);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();//do not swallow it !
		}
	}

	@Override
	public String toString() {
		return super.toString()+" multicore mode "+isMulticoreModeOn();
	}

	/**
	 * @return <code>true</code> if the multicore mode is on. I.e. 
	 * {@link #nbOfParallelTasks()} > 1.
	 * This method could be used by the default behavior of scheduler agents as 
	 * they test in which mode each activator has to be used.
	 */
	public boolean isMulticoreModeOn() {
		return nbOfsimultaneousTasks > 1;
	}

	/**
	 * Sets the number of tasks which will be used on a multicore
	 * architecture. If set to a number greater
	 * than 1, the scheduler will automatically use {@link #multicoreExecute()}
	 * on this activator when {@link Activator#execute()} is called.
	 * If set to 1, the agents are sequentially activated. Beware
	 * that this is the only way
	 * to do exact replication of simulations, unless you have clear
	 * specifications for your model, see {@link #multicoreExecute()}.
	 * 
	 * @param nbOfParallelTasks the number of simultaneous tasks
	 * that this activator will use to make a step. Default is 1 upon
	 * creation, so that 
	 * {@link #isMulticoreModeOn()} returns <code>false</code>.
	 */
	public void useMulticore(int nbOfParallelTasks) {
		nbOfsimultaneousTasks = nbOfParallelTasks < 2 ? 1 : nbOfParallelTasks;
	}

	/**
	 * Returns the number of tasks that will
	 * be created by this activator in order to
	 * benefit from multicore platforms.
	 * 
	 * @return the number of tasks that will be created.
	 */
	public int nbOfParallelTasks() {
		return nbOfsimultaneousTasks;
	}

	/**
	 * Returns the agent's method named <code>methodName</code>
	 * considering a given agentClass. This also works 
	 * for the private methods of the class, even inherited ones.
	 * 
	 * 
	 * @param agentClass the targeted agent 
	 * @param methodName the name of the method
	 * @return the agent's method named <code>methodName</code>
	 * @throws NoSuchMethodException 
	 */
	//	* This also works on <code>private</code> field.
	public Method findMethodOn(Class<? extends A> agentClass, final String methodName) throws NoSuchMethodException {
		Method m;
		while(true) {
			try {
				m = agentClass.getDeclaredMethod(methodName);
				if(m != null){
					if (! m.isAccessible()) {//TODO seems to be always the case the first time
						m.setAccessible(true);
					}
					return m;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				agentClass = (Class<? extends A>) agentClass.getSuperclass();//TODO not go further than A
				if (agentClass == AbstractAgent.class) {//TODO bench vs local variable
					throw e;
				}
			}
		} 
	}
}