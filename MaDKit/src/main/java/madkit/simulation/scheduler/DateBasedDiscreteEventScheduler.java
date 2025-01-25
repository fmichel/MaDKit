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

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.Activator;
import madkit.kernel.Scheduler;

/**
 * This class defines a scheduler for discrete event simulation. It is based on the
 * {@link DateBasedTimer} class which is used to manage the simulation time. The scheduler
 * uses a priority queue to manage the activation list of the activators.
 */
public class DateBasedDiscreteEventScheduler extends Scheduler<DateBasedTimer> {

	private PriorityQueue<DateBasedDiscreteEventActivator> activatorActivationList = new PriorityQueue<>();

	/**
	 * Constructs a new scheduler with a {@link DateBasedTimer} as simulation time.
	 */
	public DateBasedDiscreteEventScheduler() {
		setSimulationTime(new DateBasedTimer());
	}

	@Override
	public void doSimulationStep() {
		DateBasedDiscreteEventActivator nextActivator = getActivationList().poll();
		getSimuTimer().setCurrentTime(nextActivator.getNextActivationDate());
		nextActivator.execute();
		getActivationList().add(nextActivator);
	}

	/**
	 * Returns the current simulation time.
	 * 
	 * @return the current simulation time
	 */
	public LocalDateTime getCurrentTime() {
		return getSimuTimer().getCurrentTime();
	}

	/**
	 * Logs the activation list.
	 */
	public void logActivationList() {
		getLogger().fine(() -> {
			StringBuilder s = new StringBuilder("\nActivation list ->\n");
			for (DateBasedDiscreteEventActivator dateBasedDiscreteEventActivator : getActivationList()) {
				s.append(dateBasedDiscreteEventActivator).append('\n');
			}
			return s.toString();
		});
	}

	/**
	 * Adds an activator to the scheduler. If the activator is a
	 * {@link DateBasedDiscreteEventActivator} it is also added to the activation list.
	 */
	@Override
	public boolean addActivator(Activator activator) {
		if (super.addActivator(activator)) {
			if (activator instanceof DateBasedDiscreteEventActivator dbdea && !getActivationList().contains(activator)) {
				getActivationList().add(dbdea);
			}
			return true;
		}
		return false;
	}

	/**
	 * Should be used when an activator changes its next event date so that the new one is
	 * earlier. So, this method triggers the reordering of this activator.
	 * 
	 * @param a
	 */
	public void updateActivatorRanking(Activator a) {
		if (activatorActivationList.remove(a)) {
			activatorActivationList.add((DateBasedDiscreteEventActivator) a);
		}
	}

	@Override
	public void removeActivator(Activator activator) {
		super.removeActivator(activator);
		getActivationList().remove(activator);
	}

	/**
	 * Returns the activation list.
	 * 
	 * @return the activation list
	 */
	public PriorityQueue<DateBasedDiscreteEventActivator> getActivationList() {
		return activatorActivationList;
	}

}
