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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;

/**
 * This probe inspects fields of type T on agents of type A and its subclasses.
 * 
 * @param <A> the group's agent most common type (i.e. AbstractAgent)
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int, that is a primitive type)
 * @author Fabien Michel
 * @since MadKit 5.0.0.13
 * @version 5.1
 * 
 */
public class FieldProbe<A extends AbstractAgent,T> extends Probe<A>
{ 
	private Map<Class<? extends A>, Field> properties = new ConcurrentHashMap<Class<? extends A>, Field>();
	final private String fieldName;

	public FieldProbe(String community, String group, String role,String fieldName)
	{
		super(community, group, role);
		this.fieldName = fieldName;
	}

	@Override
	public void initialize()
	{
		properties = new ConcurrentHashMap<Class<? extends A>, Field>(size());//TODO load factor
		super.initialize();//calls adding on all agents
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void adding(final A theAgent) {
		try {
			final Class<? extends A> cl = (Class<? extends A>) theAgent.getClass();
			if(properties.get(cl) == null)
				properties.put(cl, findFieldOn(cl,fieldName));
		} catch(NoSuchFieldException e) {
			theAgent.getLogger().severeLog("\nCan't find property: "+fieldName+" on "+ theAgent,e);
		}
	}
	
	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @param agent the agent to probe
	 * @return the actual value of the agent's field 
	 */
	@SuppressWarnings("unchecked")
	public T getFieldValueFor(final A agent) {
		try {
			return (T) properties.get(agent.getClass()).get(agent);
		} catch (IllegalArgumentException e) {
			agent.getLogger().severeLog("unable to access field"+fieldName, e);
		} catch (IllegalAccessException e) {
			agent.getLogger().severeLog("unable to access field"+fieldName, e);
//		} catch (ClassCastException e) { //very strange because the cast always works here
//			agent.getLogger().severeLog("unable to access field"+fieldName, e);
		}
		return null;
	}
	
}