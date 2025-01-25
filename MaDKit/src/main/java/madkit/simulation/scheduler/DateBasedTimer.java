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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import madkit.kernel.Scheduler;

/**
 * This class encapsulates the time of the simulation. Its purpose is that it can be
 * passed across objects without problem. That is, {@link BigDecimal} is immutable and
 * therefore creates a new instance for each modification.
 *
 * @since MaDKit 5.3
 * @see Scheduler LocalDateTime
 */
public class DateBasedTimer extends SimuTimer<LocalDateTime> {

	private ChronoUnit defaultUnit;

	/**
	 * Creates a date-based instance using a specific {@link LocalDateTime} as starting point.
	 *
	 * @param initialDate a {@link LocalDateTime} to start with
	 * @param endDate     a {@link LocalDateTime} to end with
	 * @see LocalDateTime
	 */
	public DateBasedTimer(LocalDateTime initialDate, LocalDateTime endDate) {
		super(initialDate, endDate);
		defaultUnit = ChronoUnit.SECONDS;
		setTimeFormat("%1$tD %1$tT");
	}

	/**
	 * Creates a date-based instance using a specific {@link LocalDateTime} as starting point.
	 *
	 * @param initialDate a {@link LocalDateTime} to start with
	 * @see LocalDateTime
	 */
	public DateBasedTimer(LocalDateTime initialDate) {
		this(initialDate, LocalDateTime.MAX);
	}

	/**
	 * Creates a date-based time which value is <code>LocalDateTime.of(1, 1, 1, 0, 0)</code>;
	 */
	public DateBasedTimer() {
		this(LocalDateTime.of(1, 1, 1, 0, 0));
	}

	/**
	 * Increments the current date by the specified amount and unit.
	 *
	 * @param amountToAdd the amount to add
	 * @param unit        the unit of the amount to add
	 */
	public void incrementCurrentDate(long amountToAdd, ChronoUnit unit) {
		setCurrentTime(getCurrentTime().plus(amountToAdd, unit));
	}

	/**
	 * Increments the current date by the specified amount using the default unit.
	 *
	 * @param amountToAdd the amount to add
	 */
	public void incrementCurrentDate(long amountToAdd) {
		incrementCurrentDate(amountToAdd, defaultUnit);
	}

	/**
	 * Sets the default temporal unit for date increments.
	 *
	 * @param unit the default temporal unit
	 */
	public void setDefaultTemporalUnit(ChronoUnit unit) {
		defaultUnit = unit;
	}

	/**
	 * Gets the default temporal unit for date increments.
	 *
	 * @return the default temporal unit
	 */
	public ChronoUnit getDefaultTemporalUnit() {
		return defaultUnit;
	}

	/**
	 * Adds one time unit to the current date using the default unit.
	 */
	public void addOneTimeUnit() {
		incrementCurrentDate(1, defaultUnit);
	}

}