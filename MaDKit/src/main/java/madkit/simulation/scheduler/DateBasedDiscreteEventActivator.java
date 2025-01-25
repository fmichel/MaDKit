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

package madkit.simulation.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;

import madkit.kernel.Activator;

/**
 * A behavior activator that is designed to work with a
 * {@link DateBasedDiscreteEventScheduler}, that is following a discrete-event simulation
 * scheme. This activator activates all the agents of the corresponding CGR for each
 * specific date for which it is activated.
 *
 * It encapsulates an activation date which is coded using a {@link LocalDateTime} object.
 *
 * @see DateBasedDiscreteEventScheduler
 * @see LocalDateTime
 * @see Activator
 * @see Duration
 * @see MethodActivator
 * 
 */
public class DateBasedDiscreteEventActivator extends MethodActivator {

	/** The next date at which this activator should be triggered. */
	private LocalDateTime nextActivationDate;

	/** The default interval between activations. */
	private Duration defaultInterval = Duration.ofSeconds(1);

	/**
	 * Constructs a new DateBasedDiscreteEventActivator with the specified group, role, and
	 * behavior to activate.
	 *
	 * @param group                 the group of the agents
	 * @param role                  the role of the agents
	 * @param theBehaviorToActivate the behavior to activate
	 */
	public DateBasedDiscreteEventActivator(String group, String role, String theBehaviorToActivate) {
		super(group, role, theBehaviorToActivate);
	}

	/**
	 * Returns the current simulation time.
	 *
	 * @return the current simulation time
	 */
	public LocalDateTime getCurrentTime() {
		return (LocalDateTime) getSimuTimer().getCurrentTime();
	}

	/**
	 * Defines an ordering so that the scheduler can classify activators according to their
	 * date and priority.
	 *
	 * @param o the activator to compare to
	 * @return a negative integer, zero, or a positive integer as this activator is less than,
	 *         equal to, or greater than the specified activator
	 */
	@Override
	public int compareTo(Activator o) {
		if (o instanceof DateBasedDiscreteEventActivator dba) {
			int result = getNextActivationDate().compareTo(dba.getNextActivationDate());
			if (result != 0) {
				return result;
			}
		}
		return super.compareTo(o);
	}

	/**
	 * Returns the scheduler associated with this activator.
	 *
	 * @return the scheduler associated with this activator
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DateBasedDiscreteEventScheduler getScheduler() {
		return (DateBasedDiscreteEventScheduler) super.getScheduler();
	}

	/**
	 * Returns the next date at which this activator should be triggered.
	 *
	 * @return the next date at which this activator should be triggered
	 */
	public LocalDateTime getNextActivationDate() {
		return nextActivationDate;
	}

	/**
	 * Sets the next date at which the activator will be triggered.
	 *
	 * @param nextActivationDate a {@link LocalDateTime} which should be greater than the
	 *                           current simulation time
	 */
	public void setNextActivationDate(LocalDateTime nextActivationDate) {
		this.nextActivationDate = nextActivationDate;
	}

	/**
	 * Executes the behavior of the activator and updates the next activation date.
	 *
	 * @param args the arguments to pass to the behavior
	 */
	@Override
	public void execute(Object... args) {
		super.execute(args);
		setNextActivationDate(getCurrentTime().plus(defaultInterval));
	}

	/**
	 * Returns a string representation of the activator, including the next activation date.
	 *
	 * @return a string representation of the activator
	 */
	@Override
	public String toString() {
		return getNextActivationDate().toString() + " ->" + super.toString();
	}

	/**
	 * Returns the default interval between activations.
	 *
	 * @return the default interval between activations
	 */
	public Duration getDefaultInterval() {
		return defaultInterval;
	}

	/**
	 * Sets the default interval between activations.
	 *
	 * @param defaultInterval the default interval between activations
	 */
	public void setDefaultInterval(Duration defaultInterval) {
		this.defaultInterval = defaultInterval;
	}
}
