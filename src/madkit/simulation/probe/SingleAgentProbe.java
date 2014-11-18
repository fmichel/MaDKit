/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.simulation.probe;

import java.lang.reflect.Field;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;
import madkit.simulation.SimulationException;

/**
 * This probe inspects fields of type T on only one agent of type A and its subclasses.
 * This is designed for probing one single agent, i.e. methods are designed  
 * and optimized in this respect.
 * 
 * @param <A> the most common class type expected in this group (e.g. AbstractAgent)
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int, i.e. a primitive type)
 * @author Fabien Michel
 * @since MaDKit 5.0.0.18
 * @version 1.0
 * 
 */
public class SingleAgentProbe<A extends AbstractAgent,T> extends Probe<A>//TODO make a thread safe version
{ 
	final private String fieldName;
	private Field field;
	private A probedAgent;

	/**
	 * Builds a new SingleAgentProbe considering a CGR location and the name of the class's field.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param fieldName the name of a field which is encapsulated in the type <A>
	 */
	public SingleAgentProbe(String community, String group, String role, String fieldName)
	{
		super(community, group, role);
		this.fieldName = fieldName;
	}
	
	@Override
	protected void adding(A agent) {
		if(field == null){//TODO replace or not
			try {
				field = findFieldOn(agent.getClass(), fieldName);
				probedAgent = agent;
			} catch (NoSuchFieldException e) {
				throw new SimulationException(toString()+" on "+agent,e);
			}
		}
	}
	
	@Override
	protected void removing(A agent) {
		super.removing(agent);
		field = null;
	}
	
	/**
	 * Get the current probed agent.
	 * @return the agent which is currently probed
	 */
	public A getProbedAgent(){
		return probedAgent;
	}

	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @return the value of the agent's field 
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue() {
		try {
			return (T) field.get(probedAgent);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+probedAgent,e);
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + (probedAgent == null ? "" : " : "+probedAgent);
	}
	
	/**
	 * Should be used to work with primitive types
	 * or fields which are initially <code>null</code>
	 * @param value
	 */
	public void setPropertyValue(final T value){
		try {
			field.set(probedAgent, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+probedAgent,e);
		}
	}

}