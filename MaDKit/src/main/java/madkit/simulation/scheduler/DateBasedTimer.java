package madkit.simulation.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import madkit.kernel.Scheduler;

/**
 * This class encapsulates the time of the simulation. Its purpose is that it
 * can be passed across objects without problem. That is, {@link BigDecimal} is
 * immutable and therefore creates a new instance for each modification.
 *
 * @author Fabien Michel
 * @since MaDKit 5.3
 * @see Scheduler LocalDateTime
 */
public class DateBasedTimer extends SimuTimer<LocalDateTime> {

	private ChronoUnit defaultUnit;

	/**
	 * Creates a date-based instance using a specific {@link LocalDateTime} as
	 * starting point.
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
	 * Creates a date-based instance using a specific {@link LocalDateTime} as
	 * starting point.
	 *
	 * @param initialDate a {@link LocalDateTime} to start with
	 * @see LocalDateTime
	 */
	public DateBasedTimer(LocalDateTime initialDate) {
		this(initialDate, LocalDateTime.MAX);
	}

	/**
	 * Creates a date-based time which value is
	 * <code>LocalDateTime.of(1, 1, 1, 0, 0)</code>;
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