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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;

/**
 * This probe inspects properties of type P on type A agents or subclasses.
 * 
 * @param <A> the group's agent most common type (i.e. AbstractAgent)
 * @param <P> the type of the property (i.e. Integer)
 * @author Fabien Michel
 * @since MadKit 4.0
 * @version 5.1
 * 
 */
public class PropertyProbe<A extends AbstractAgent,P> extends Probe<A>
{ 
	private Map<A, P> properties = new ConcurrentHashMap<A, P>();
	private String fieldName;

	public PropertyProbe(String community, String group, String role,String propertyName)
	{
		super(community, group, role);
		fieldName = propertyName;
	}

	@Override
	public void initialize()
	{
		properties = new ConcurrentHashMap<A, P>(size());//TODO load factor
		super.initialize();//will call adding on all agents
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void adding(final A theAgent) {
		try {
			properties.put(theAgent, (P) findFieldOn(theAgent.getClass(),fieldName));
		} catch(NoSuchFieldException e) {
			theAgent.getLogger().severeLog("\nCan't find property: "+fieldName+" on "+ theAgent,e);
		} catch (ClassCastException e) {
			theAgent.getLogger().severeLog("\nProperty: "+fieldName+" is not of declared type",e);
		}
	}
	
	/**
	 * @see madkit.kernel.Probe#removing(AbstractAgent)
	 */
	@Override
	protected void removing(final A theAgent) {
		properties.remove(theAgent);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void adding(List<A> agents) {//bench that : it shoud be faster
		try {
			if (! agents.isEmpty()) {
				final Map<A, P> newP = new ConcurrentHashMap<A, P>(agents.size()+properties.size(),.9f); 
				final Field f = findFieldOn(agents.get(0).getClass(),fieldName);
				for (A a : agents) {
					newP.put(a, (P) f.get(a));//TODO will fail if all the agents are not of the same type
				}
				newP.putAll(properties);
				properties = newP;
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			agents.get(0).getLogger().severeLog("\nProperty: "+fieldName+" is not of declared type",e);
		}
	}


	public Map<A, P> getAgentToPropertyMap() {
		return properties;
	}
	
	public P getPropertyOf(final A theAgent){
		return properties.get(theAgent);
	}
	
	public Collection<P> getAllProperties() {
		return properties.values();
	}

}