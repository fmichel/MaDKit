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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;

/**
 * This probe inspects properties of type P on type A agents or subclasses.
 * 
 * @param <A> the group's agent most common type (i.e. AbstractAgent)
 * @param <P> the type of the property (i.e. Integer)
 * @author Fabien Michel
 * @since MadKit 4.0
 * @version 5.0
 * 
 */
public class PropertyProbe<A extends AbstractAgent,P> extends Probe<A>
{ 
	private ConcurrentHashMap<A, P> properties = new ConcurrentHashMap<A, P>();
	private String fieldName;

	public PropertyProbe(String community, String group, String role,String propertyName)
	{
		super(community, group, role);
		fieldName = propertyName;
	}

	@Override
	public void initialize()
	{
		properties = new ConcurrentHashMap<A, P>(size());
		super.initialize();//will call adding on all agents
	}
	
	/**
	 * @see madkit.kernel.Probe#adding(AbstractAgent)
	 */
	@Override
	public void adding(final A theAgent) {
		findFieldOfAgent(theAgent);
	}
	
	/**
	 * @see madkit.kernel.Probe#removing(AbstractAgent)
	 */
	@Override
	public void removing(final A theAgent) {
		properties.remove(theAgent);
	}

	/**
	 * @param theAgent
	 */
	@SuppressWarnings("unchecked")
	private void findFieldOfAgent(final A theAgent) {
		try {
			properties.put(theAgent, (P) theAgent.getClass().getField(fieldName).get(theAgent));
		}
		catch(NoSuchFieldException e) {
			Logger.getLogger("[TMP]").severe("\nCan't find property: "+fieldName+" on "+ theAgent +" "+e.getMessage());
		}catch (IllegalAccessException e) {
			Logger.getLogger("[TMP]").severe("\nCan't access property: "+fieldName+" on "+ theAgent +" "+e.getMessage());
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