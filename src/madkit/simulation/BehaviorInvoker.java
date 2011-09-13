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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sound.sampled.Line;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;

/**
 * Class encapsulating behavior invocation on MadKit agents.
 * A BehaviorInvoker allows to call a particular Java method on agents regardless of their actual class type as long
 * as they extend {@link AbstractAgent}. A BehaviorInvoker is especially used by {@link Activator} subclasses to 
 * create simulation applications.
 * 
 * @author Fabien Michel
 * @since MadKit 5
 * @see GenericBehaviorActivator
 * @version 0.91
 * 
 */
public class BehaviorInvoker<A extends AbstractAgent> {//TODO bench that with generic of type A extends AA

	/** methods maps an agent class to its corresponding Method object for runtime invocation*/
	private final Map<Class<? extends AbstractAgent>,Method> methods;
	private String methodName;
	private Class<?> cachedClass = null;
	private Method cachedMethod = null;

	/**
	 * Constructs a new BehaviorInvoker that will activate the Java method of the agent which has this name.
	 * @param behaviorName the method's name
	 * 
	 */
	public BehaviorInvoker(final String behaviorName) {
		methods = new HashMap<Class<? extends AbstractAgent>, Method>();
		methodName = behaviorName;
	}


	/**
	 * Returns the method's name corresponding to the behavior activated by this behaviorInvoker.
	 * @return the method's name.
	 */
	public String getBehaviorName() {
		return methodName;
	}


	/**
	 * Sets the behavior activated by this behaviorInvoker.
	 * @param methodName the methodName corresponding to the behavior to triggered
	 */
	public void setMethodName(final String methodName) {
		if (! this.methodName.equals(methodName)) {
			this.methodName = methodName;
			methods.clear();
			cachedClass = null;
			cachedMethod = null;
		}
	}


	/**
	 * Executes the behavior on a particular agent instance.
	 * 
	 * @param agent the agent on which the behavior will be triggered
	 */
	public void executeBehaviorOf(final A agent)
	{
		final Class<? extends AbstractAgent> agentClass = (Class<? extends AbstractAgent>) agent.getClass();
		if (agentClass != cachedClass) {
			cachedClass = agentClass;
			cachedMethod = methods.get(cachedClass);
			if(cachedMethod == null){
				try {
					cachedMethod = agentClass.getMethod(methodName);
					methods.put(agentClass, cachedMethod);
				} catch (SecurityException e) {
					Logger.getLogger("[TMP]").severe("\nCan't find method: " + methodName + " on "+ agent + " " + e.getMessage());
				} catch (NoSuchMethodException e) {
					Logger.getLogger("[TMP]").severe(
							"\nCan't find method: " + methodName + " on "
							+ agent + " " + e.getMessage());
				}
			}
		}
		try {
//			synchronized (theAgent) {
				if (agent.isAlive()) {
					cachedMethod.invoke(agent);
				}
//			}
		} catch (IllegalArgumentException e) {//TODO redirect logging
			Logger.getLogger("[TMP]").severe("\nCan't invoke method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		} catch (IllegalAccessException e) {
			Logger.getLogger("[TMP]").severe("\nCan't access method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		} catch (InvocationTargetException e) {
			Logger.getLogger("[TMP]").severe("\nCan't invoke method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		}
	}

	/**
	 * Thread safe version of {@link #executeBehaviorOf(AbstractAgent)}. 
	 * This is used by {@link GenericBehaviorActivator#multicoreExecute()}.
	 * 
	 * @param agent the agent on which the behavior will be triggered
	 * @since MadKit 5.0.0.11
	 */
	public void concurrentExecuteBehavior(final A agent)
	{
		final Class<? extends AbstractAgent> agentClass = (Class<? extends AbstractAgent>) agent.getClass();
		Method m = methods.get(agentClass);
		if(m == null){
			try {
				m = agentClass.getMethod(methodName);
				synchronized (methods) {
					methods.put(agentClass, m);
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			if (agent.isAlive()) {
				m.invoke(agent);
			}
		} catch (IllegalArgumentException e) {//TODO redirect logging
			Logger.getLogger("[TMP]").severe("\nCan't invoke method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		} catch (IllegalAccessException e) {
			Logger.getLogger("[TMP]").severe("\nCan't access method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		} catch (InvocationTargetException e) {
			Logger.getLogger("[TMP]").severe("\nCan't invoke method: " + methodName + " on "+ agent + " " + e.getMessage());
			if (e.getCause() == null) 
				e.printStackTrace(); 
			else
				e.getCause().printStackTrace();
		}
	}

}
