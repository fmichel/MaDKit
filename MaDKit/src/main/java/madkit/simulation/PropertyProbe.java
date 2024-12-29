package madkit.simulation;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import madkit.kernel.Agent;
import madkit.kernel.Probe;

/**
 * This probe inspects a field of type T on a group of agents.
 * 
 * @param <T> the type of the property, i.e. Integer (this works if the field is
 *            an int, i.e. a inspected primitive type should use its wrapper)
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 6.0
 */
public class PropertyProbe<T> extends Probe {

	private final Map<Class<?>, Field> fields = new HashMap<>();
	private final String fieldName;

	private Field cachedField;
	private Class<?> cachedClass;

	private Comparator<T> comparator;

	/**
	 * Builds a new PropertyProbe considering a CGR location and the name of the
	 * class's field.
	 * 
	 * @param group
	 * @param role
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
			cachedField = fields.computeIfAbsent(cachedClass, f -> {
				try {
					return findFieldOn(agentClass, fieldName);
				} catch (NoSuchFieldException e) {
					throw new SimuException(toString() + " on " + agent, e);
				}
			});
		}
	}

	/**
	 * Returns the current value of the agent's field
	 * 
	 * @param agent the agent to probe
	 * @return the actual value of the agent's field
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue(final Agent agent) {
		updateCache(agent);
		try {
			return (T) cachedField.get(agent);
		} catch (IllegalAccessException e) {
			throw new SimuException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Should be used to work with primitive types or fields which are initially
	 * <code>null</code>
	 * 
	 * @param agent
	 * @param value
	 */
	public void setPropertyValue(final Agent agent, final T value) {
		updateCache(agent);
		try {
			cachedField.set(agent, value);// NOSONAR
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SimuException(toString() + " on " + agent, e);
		}
	}

	/**
	 * Returns the maximum for the property over all the agents. T must be a
	 * numerical type or {@link Comparable} for this to work,
	 * {@link ClassCastException} is thrown otherwise.
	 * 
	 * @return the maximum value for this property
	 */
	public T getMax() {
		return getMax(getComparator());
	}

	/**
	 * Returns the maximum for the property over all the agents.
	 * 
	 * @param c the {@link Comparator} to use
	 * @return the maximum value for this property
	 * @throws NoSuchElementException if the probe is empty
	 */
	public T getMax(Comparator<T> c) throws NoSuchElementException {
		return getAgents().stream().map(a -> getPropertyValue(a)).max(c).get();// NOSONAR
	}

	/**
	 * Returns the maximum for the property over all the agents. T must be a
	 * numerical type or {@link Comparable} for this to work,
	 * {@link ClassCastException} is thrown otherwise.
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
		return getAgents().stream().map(a -> getPropertyValue(a)).min(c).get();// NOSONAR
	}

	/**
	 * Returns the maximum for the property over all the agents. T must extends
	 * {@link Number} for this to work, {@link NoSuchElementException} is thrown
	 * otherwise.
	 * 
	 * @return the maximum value for this property
	 */
	public double getAverage() {
		return getAgents().stream().mapToDouble(a -> ((Number) getPropertyValue(a)).doubleValue()).average()
				.getAsDouble();
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