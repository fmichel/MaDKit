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

import madkit.kernel.AbstractAgent;

/**
 * Class encapsulating behavior invocation on MadKit agents.
 * 
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 * 
 */
public class BehaviorInvoker<A extends AbstractAgent> {//TODO bench that with generic of type A extends AA

	/** methods maps an agent class to its corresponding Method object for runtime invocation*/
	private final Map<Class<? extends AbstractAgent>,Method> methods;
	private String methodName;
	private Class<?> cachedClass = null;
	private Method cachedMethod = null;

	/**
	 * @param behaviorName 
	 * 
	 */
	public BehaviorInvoker(final String behaviorName) {
		methods = new HashMap<Class<? extends AbstractAgent>, Method>();
		methodName = behaviorName;
	}


	/**
	 * @return the methodName
	 */
	public String getBehaviorName() {
		return methodName;
	}


	/**
	 * @param methodName the methodName to set
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
	 * @param agent the targeted instance
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


}
