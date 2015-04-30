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
import java.util.HashMap;
import java.util.Map;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;
import madkit.simulation.SimulationException;

/**
 * This probe inspects fields of type T on agents of type A and its subclasses.
 * 
 * @param <A>
 *            the most common class type expected in this group (e.g.
 *            AbstractAgent)
 * @param <T>
 *            the type of the property, i.e. Integer (this works if the field is
 *            an int, i.e. a primitive type)
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 5.1
 * 
 */
public class PropertyProbe<A extends AbstractAgent, T> extends Probe<A>// TODO
																		// make
																		// a
																		// thread
																		// safe
																		// version
{
	final private Map<Class<? extends A>, Field> fields = new HashMap<>();
	final private String fieldName;
	private Field cachedField;
	private Class<? extends A> cachedClass;

	/**
	 * Builds a new PropertyProbe considering a CGR location and the name of the class's field.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param fieldName the name of a field which is encapsulated in the type <A>
	 */
	public PropertyProbe(String community, String group, String role,	String fieldName) {
		super(community, group, role);
		this.fieldName = fieldName;
	}

	/**
	 * @param agentClass
	 */
	private void updateCache(A agent) {
		@SuppressWarnings("unchecked")
		final Class<? extends A> agentClass = (Class<? extends A>) agent
				.getClass();
		if (agentClass != cachedClass) {
			cachedClass = agentClass;
			cachedField = fields.get(cachedClass);
			if (cachedField == null) {
				try {
					cachedField = findFieldOn(cachedClass, fieldName);
					fields.put(cachedClass, cachedField);
				} catch (NoSuchFieldException e) {
					throw new SimulationException(toString() + " on " + agent,	e);
				}
			}
		}
	}

	/**
	 * Returns the current value of the agent's field
	 * 
	 * @param agent
	 *            the agent to probe
	 * @return the actual value of the agent's field
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue(final A agent) {
		updateCache(agent);
		try {
			return (T) cachedField.get(agent);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Should be used to work with primitive types or fields which are initially
	 * <code>null</code>
	 * 
	 * @param agent
	 * @param value
	 */
	public void setPropertyValue(final A agent, final T value) {
		updateCache(agent);
		try {
			cachedField.set(agent, value);
		} catch (IllegalArgumentException e) {
			throw new SimulationException(toString() + " on " + agent, e);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Returns the maximum for the property over all the agents. The property
	 * must be numerical or {@link Comparable} for this to work.
	 * 
	 * @return the maximum value for this property
	 */
	@SuppressWarnings("unchecked")
	public T getMaxValue() {
		T max = null;
		for (final A a : getCurrentAgentsList()) {
			try {
				if (max == null) {
					max = getPropertyValue(a);
				}
				final T val = getPropertyValue(a);
				if (((Comparable<T>) val).compareTo(max) > 0) {
					max = val;
				}
			} catch (ClassCastException e) {
				throw new SimulationException(toString() + " on " + a, e);
			}
		}
		return max;
	}

	/**
	 * Returns the minimum for the property over all the agents. The property
	 * must be numerical or {@link Comparable} for this to work.
	 * 
	 * @return the minimum value for this property
	 */
	@SuppressWarnings("unchecked")
	public T getMinValue() {
		T min = null;
		for (final A a : getCurrentAgentsList()) {
			try {
				if (min == null) {
					min = getPropertyValue(a);
				}
				final T val = getPropertyValue(a);
				if (((Comparable<T>) val).compareTo(min) < 0) {
					min = val;
				}
			} catch (ClassCastException e) {
				throw new SimulationException(toString() + " on " + a, e);
			}
		}
		return min;
	}
	
	/**
	 * Returns the average value for the property over all the agents. The property
	 * must be numerical for this to work.
	 * 
	 * @return the average value for this property
	 */
	public double getAverageValue() {
		double total = 0;
		for (final A a : getCurrentAgentsList()) {
			try {
				total += ((Number) getPropertyValue(a)).doubleValue();
			} catch (ClassCastException e) {
				throw new SimulationException(toString() + " on " + a, e);
			}
		}
		return total / size();
	}

	// final public List<T> getAllProperties(){
	// final ArrayList<T> list = new ArrayList<T>(size());
	// for (final A agent : getCurrentAgentsList()) {
	// list.add(getPropertyValue(agent));
	// }
	// return list;
	// }

}