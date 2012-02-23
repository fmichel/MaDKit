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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;

/**
 * This probe inspects fields of type T on agents of type A and its subclasses.
 * 
 * @param <A> the group's agent most common class type (i.e. AbstractAgent)
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int, i.e. a primitive type)
 * @author Fabien Michel
 * @since MadKit 5.0.0.13
 * @version 5.1
 * 
 */
public class PropertyProbe<A extends AbstractAgent,T> extends Probe<A>//TODO make a thread safe version
{ 
	final private Map<Class<? extends A>, Field> fields = new HashMap<Class<? extends A>, Field>();
	final private String fieldName;
	private Field cachedFiled;
	private Class<? extends A> cachedClass;

	public PropertyProbe(String community, String group, String role,String fieldName)
	{
		super(community, group, role);
		this.fieldName = fieldName;
	}

	/**
	 * @param agentClass
	 */
	private void updateCache(A agent) {
		@SuppressWarnings("unchecked")
		final Class<? extends A> agentClass = (Class<? extends A>) agent.getClass();
		if(agentClass != cachedClass){
			cachedClass = agentClass;
			cachedFiled = fields.get(cachedClass);
			if(cachedFiled == null){
				try {
					cachedFiled = findFieldOn(cachedClass,fieldName);
					fields.put(cachedClass,cachedFiled);
				} catch (NoSuchFieldException e) {
					throw new SimulationException(toString()+" on "+agent,e);
				}
			}
		}
	}

	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @param agent the agent to probe
	 * @return the actual value of the agent's field 
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue(final A agent) {
		updateCache(agent);
		try {
			return (T) cachedFiled.get(agent);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+agent,e);
		}
	}
	
	/**
	 * Should be used to work with primitive types
	 * or fields which are initially <code>null</code>
	 * @param agent
	 * @param value
	 */
	public void setPropertyValue(final A agent, final T value){
		updateCache(agent);
		try {
			cachedFiled.set(agent, value);
		} catch (IllegalArgumentException e) {
			throw new SimulationException(toString()+" on "+agent,e);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+agent,e);
		}
	}

//	final public List<T> getAllProperties(){
//		final ArrayList<T> list = new ArrayList<T>(size());
//		for (final A agent : getCurrentAgentsList()) {
//			list.add(getPropertyValue(agent));
//		}
//		return list;
//	}

}