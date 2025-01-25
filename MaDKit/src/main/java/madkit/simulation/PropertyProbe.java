/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import madkit.kernel.Agent;
import madkit.kernel.Probe;

/**
 * This probe inspects a field of type T on a group of agents.
 * 
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int,
 *            i.e. a inspected primitive type should use its wrapper)
 */
public class PropertyProbe<T> extends Probe {

	private final Map<Class<?>, Field> fields = new HashMap<>();
	private final String fieldName;

	private Field cachedField;
	private Class<?> cachedClass;

	private Comparator<T> comparator;

	/**
	 * Builds a new PropertyProbe considering a CGR location and the name of the class's
	 * field.
	 *
	 * @param group     the group
	 * @param role      the role
	 * @param fieldName the name of a field which is encapsulated in type T
	 */
	public PropertyProbe(String group, String role, String fieldName) {
		super(group, role);
		this.fieldName = fieldName;
	}

	/**
	 * update <code>cachedField</code> if required
	 * 
	 * @param agentClass
	 */
	private void updateCache(Agent agent) {
		Class<?> agentClass = agent.getClass();
		if (agentClass != cachedClass) {
			cachedClass = agentClass;
			cachedField = fields.computeIfAbsent(cachedClass, _ -> {
				try {
					return findFieldOn(agentClass, fieldName);
				} catch (NoSuchFieldException e) {
					throw new SimuException(toString() + " on " + agent, e);
				}
			});
		}
	}

	/**
	 * Returns the current value of the agent's field.
	 *
	 * @param agent the agent to probe
	 * @return the actual value of the agent's field
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue(Agent agent) {
		updateCache(agent);
		try {
			return (T) cachedField.get(agent);
		} catch (IllegalAccessException e) {
			throw new SimuException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Should be used to work with primitive types or fields which are initially
	 * <code>null</code>.
	 *
	 * @param agent the agent
	 * @param value the value
	 */
	public void setPropertyValue(Agent agent, T value) {
		updateCache(agent);
		try {
			cachedField.set(agent, value);// NOSONAR
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SimuException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Returns the maximum for the property over all the agents. T must be a numerical type or
	 * {@link Comparable} for this to work, {@link ClassCastException} is thrown otherwise.
	 * 
	 * @return the maximum value for this property
	 */
	public T getMax() {
		return getMax(getComparator());
	}

	/**
	 * Returns the maximum for the property over all the agents using the specified
	 * {@link Comparator}. Moreover, the targeted group/role couple must not be empty,
	 * otherwise a {@link NoSuchElementException} is thrown.
	 * 
	 * @param comparator the {@link Comparator} to use
	 * @return the maximum value for this property
	 * @throws NoSuchElementException if the probe is empty
	 */
	public T getMax(Comparator<T> comparator) throws NoSuchElementException {
		return streamValues().max(comparator).get();// NOSONAR
	}

	/**
	 * Returns the maximum for the property over all the agents. T must be a numerical type or
	 * {@link Comparable} for this to work, {@link ClassCastException} is thrown otherwise.
	 * 
	 * @return the maximum value for this property
	 */
	public T getMin() {
		return getMin(getComparator());
	}

	/**
	 * Returns the minimum for the property over all the agents.
	 * 
	 * @param c the {@link Comparator} to use
	 * @return the maximum value for this property
	 * @throws NoSuchElementException if the probe is empty
	 */
	public T getMin(Comparator<T> c) throws NoSuchElementException {
		return streamValues().min(c).get();// NOSONAR
	}

	/**
	 * Returns the average for the property over all the agents. T must extends {@link Number}
	 * for this to work, Moreover, the targeted groupp/role couple must not be empty,
	 * otherwise a {@link NoSuchElementException} is thrown.
	 * 
	 * @return the maximum value for this property
	 */
	public double getAverage() {
		return streamValues().mapToDouble(v -> ((Number) v).doubleValue()).average().getAsDouble();
	}

	/**
	 * Returns a stream of the values of the property for each agent.
	 *
	 * @return a stream of the values of the property for each agent
	 */
	public Stream<T> streamValues() {
		return getAgents().stream().map(a -> getPropertyValue(a));
	}

	/**
	 * @return the comparator
	 */
	private Comparator<T> getComparator() {
		if (comparator == null) {
			comparator = new Comparator<T>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(T o1, T o2) {
					return ((Comparable<T>) o1).compareTo(o2);
				}
			};
		}
		return comparator;
	}

}