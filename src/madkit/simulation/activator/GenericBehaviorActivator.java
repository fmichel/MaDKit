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
package madkit.simulation.activator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Scheduler;
import madkit.simulation.SimulationException;

/**
 * An activator that invokes a single method with no parameters on a group of agents.
 * This class encapsulates behavior invocation on MaDKit agents for scheduler agents.
 * This activator allows to call a particular Java method on agents 
 * regardless of their actual class type as long
 * as they extend {@link AbstractAgent}. 
 * This has to be used by {@link Scheduler} subclasses to 
 * create simulation applications.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @version 1.0
 * 
 */
public class GenericBehaviorActivator<A extends AbstractAgent> extends Activator<A>
{     
	/** methods maps an agent class to its corresponding Method object for runtime invocation*/
	private final Map<Class<? extends A>,Method> methods;
	private String methodName;
//	private Class<? extends A> cachedClass = null;

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
	
	/**
	 * Triggers the corresponding behavior on all the agents
	 *  which are at the CGR location defined by this activator.
	 * 
	 * @see madkit.kernel.Activator#execute(java.util.List)
	 */
	@SuppressWarnings("null")
	@Override
	public void execute(final List<A> agents){
		//local cache for multicore execute and avoiding adding collision
		Method cachedM = null;
		Class<? extends A> cachedC = null;
		for (final A a : agents) {
			if (a.isAlive()) {
				@SuppressWarnings("unchecked")
				final Class<? extends A> agentClass = (Class<? extends A>) a.getClass();
				if (agentClass != cachedC) {
					cachedC = agentClass;
					cachedM = methods.get(agentClass);
					if(cachedM == null){
						try {
							cachedM = findMethodOn(agentClass,methodName);
						} catch (NoSuchMethodException e) {
							throw new SimulationException(toString(),e);
						}
						synchronized (methods) {
							methods.put(agentClass, cachedM);
						}
					}
				}
				try {
					cachedM.invoke(a);
				} catch (IllegalAccessException e) {
					throw new SimulationException(toString(),e);
				} catch (InvocationTargetException e) {
					throw new SimulationException(toString()+" on "+cachedM+" "+a,e.getCause());
				}
			}
		}
	}
}









