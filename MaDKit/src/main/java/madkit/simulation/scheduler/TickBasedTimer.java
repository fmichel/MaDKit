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

import java.math.BigDecimal;

/**
 * A timer based on ticks for simulation purposes. This class extends the SimuTimer class
 * and uses BigDecimal for tick values.
 */
public class TickBasedTimer extends SimuTimer<BigDecimal> {

	/**
	 * Constructs a TickBasedTimer with the specified initial and end tick values.
	 *
	 * @param initialTick the initial tick value
	 * @param endTick     the end tick value
	 */
	public TickBasedTimer(BigDecimal initialTick, BigDecimal endTick) {
		super(initialTick, endTick);
	}

	/**
	 * Constructs a TickBasedTimer with an initial tick value of {@link BigDecimal#ZERO}.
	 */
	public TickBasedTimer() {
		this(BigDecimal.ZERO);
	}

	/**
	 * Constructs a TickBasedTimer with the specified initial tick value and an end tick value
	 * of {@link Double#MAX_VALUE}.
	 *
	 * @param initialTick the initial tick value
	 */
	public TickBasedTimer(BigDecimal initialTick) {
		this(initialTick, BigDecimal.valueOf(Double.MAX_VALUE));
	}

	/**
	 * Increments the current tick by the specified delta value.
	 *
	 * @param delta the amount to increment the current tick by
	 */
	public void incrementCurrentTick(double delta) {
		incrementCurrentTick(BigDecimal.valueOf(delta));
	}

	/**
	 * Increments the current tick by the specified delta value.
	 *
	 * @param delta the amount to increment the current tick by
	 */
	public void incrementCurrentTick(BigDecimal delta) {
		setCurrentTime(getCurrentTime().add(delta));
	}

	/**
	 * Adds the specified delta time to the current tick. Shortcut for
	 * <code>setCurrentTick(getCurrentTick().add(delta));</code>
	 *
	 * @param delta specifies how much time should be added
	 */
	public void addDeltaTime(BigDecimal delta) {
		setCurrentTime(getCurrentTime().add(delta));
	}

	/**
	 * Adds one time unit to the current tick.
	 */
	public void addOneTimeUnit() {
		addDeltaTime(BigDecimal.ONE);
	}
}
