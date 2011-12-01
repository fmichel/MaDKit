/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Scheduler;

/**
 * An activator that invokes a single method with no parameters on a group of agents.
 * This class encapsulates behavior invocation on MadKit agents for scheduler agents.
 * This activator allows to call a particular Java method on agents 
 * regardless of their actual class type as long
 * as they extend {@link AbstractAgent}. 
 * This has to be used by {@link Scheduler} subclasses to 
 * create simulation applications.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.1
 * @version 1.0
 * 
 */
public class GenericBehaviorActivator<A extends AbstractAgent> extends Activator<A>
{     
	/** methods maps an agent class to its corresponding Method object for runtime invocation*/
	private final Map<Class<? extends A>,Method> methods;
	private String methodName;
	private Class<? extends A> cachedClass = null;

	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler} 
	 * agent using the {@link Scheduler#addActivator(Activator)} method.
	 * Once added, it could be used to trigger the behavior on all the agents which are at this CGR location, regardless
	 * of their class type as long as they extend {@link AbstractAgent}
	 * @param community
	 * @param group
	 * @param role
	 * @param theBehaviorToActivate name of the Java method which will be invoked
	 */
	public GenericBehaviorActivator(final String community, final String group, final String role,final String theBehaviorToActivate)
	{
		super(community, group, role);
		methods = new HashMap<Class<? extends A>, Method>();
		methodName = theBehaviorToActivate;
	}

	public String getBehaviorName()	{
		return methodName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void adding(final A theAgent) {
		final Class<? extends A> agentClass = (Class<? extends A>) theAgent.getClass();
		if(agentClass != cachedClass && ! methods.containsKey(agentClass)){
//			Method m;
			try {
//				m = findMethodOn(agentClass,methodName);
//				synchronized (methods) {//TODO maybe useless as overwriting maybe ok here
					methods.put(agentClass,findMethodOn(agentClass,methodName));
//				}
				cachedClass = agentClass;
			} catch (NoSuchMethodException e) {
				logFailureOn(theAgent, e);
			}
		}
	}
	
	private void execute(List<A> agents){
		//local cache for multicore execute and adding collision
		Method cachedM = null;
		Class<? extends A> cachedC = null;
		for (A a : agents) {
			if (a.isAlive()) {
				@SuppressWarnings("unchecked")
				final Class<? extends A> agentClass = (Class<? extends A>) a.getClass();
				if (agentClass != cachedC) {
					cachedC = agentClass;
					cachedM = methods.get(agentClass);
				}
				try {
					cachedM.invoke(a);
				} catch (IllegalArgumentException e) {
					logFailureOn(a, e);
				} catch (IllegalAccessException e) {
					logFailureOn(a, e);
				} catch (InvocationTargetException e) {
					logFailureOn(a, e);
				}
			}
		}
	}
	
	private void logFailureOn(AbstractAgent a, Throwable t){
		a.getLogger().severeLog("Can't work on method: " + methodName + " on "+ a, t);
	}
	
	@Override
	public void multicoreExecute() {
		final int cpuCoreNb = nbOfSimultaneousTasks();
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
	
	//TODO should be possible and not that hard, but is there really a need ?
//	public void setBehaviorName(final String theBehaviorToActivate){
//		theBehaviorToActivate = theBehaviorToActivate;
//	}
//
	@Override
	/**
	 * Triggers the corresponding behavior on all the agents
	 *  which are at the CGR location defined by this activator.
	 * 
	 */
	public void execute() {
		execute(getCurrentAgentsList());
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public void multicoreExecute() {
//		int cpuCoreNb = nbOfSimultaneousTasks();
//		final ArrayList<Callable<Void>> workers = new ArrayList<Callable<Void>>(cpuCoreNb);
//		List<A> list = getCurrentAgentsList();
//		int bucketSize = list.size();
//		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
//		final A[] agents = list.toArray((A[]) new AbstractAgent[0]);
//		for (int i = 0; i < cpuCoreNb; i++) {
//			final int index = i;
//			workers.add(new Callable<Void>() {
//				public Void call() throws Exception {
//					int maxIndex = nbOfAgentsPerTask*(index+1);
//					for (int j = nbOfAgentsPerTask*index; j < maxIndex; j++) {
//						agentBehaviors.concurrentExecuteBehavior(agents[j]);
//					}
//					return null;
//				}
//			});
//		}
//		try {
//			getMadkitServiceExecutor().invokeAll(workers);
//			int maxIndex = nbOfAgentsPerTask*cpuCoreNb;
//			for (int i = agents.length-1; i >= maxIndex; i--) {
//				agentBehaviors.concurrentExecuteBehavior(agents[i]);
//			}
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();//do not swallow it !
//		}
//	}

}









