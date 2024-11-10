package madkit.simulation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This class encapsulates the time of the simulation. Its purpose is that it
 * can be passed across objects without problem. That is, {@link BigDecimal} is
 * immutable and therefore creates a new instance for each modification.
 *
 * @author Fabien Michel
 * @since MaDKit 5.3
 * @see Scheduler LocalDateTime
 */
public class DateBasedTimer extends SimulationTimer<LocalDateTime> {

	private ChronoUnit defaultUnit;
	
	/**
	 * /** Creates a date-based instance using a specific {@link LocalDateTime} as
	 * starting point.
	 *
	 * @param initialDate a {@link LocalDateTime} to start with
	 * @see LocalDateTime
	 */
	public DateBasedTimer(LocalDateTime initialDate, LocalDateTime endDate) {
		super(initialDate,endDate);
		defaultUnit = ChronoUnit.SECONDS;
		setTimeFormat("%1$tD %1$tT");
	}

	/**
	 * /** Creates a date-based instance using a specific {@link LocalDateTime} as
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
	 *
	 */
	public DateBasedTimer() {
		this(LocalDateTime.of(1, 1, 1, 0, 0));
	}

	public void incrementCurrentDate(long amountToAdd, ChronoUnit unit) {
		setCurrentTime(getCurrentTime().plus(amountToAdd, unit));
	}

	public void incrementCurrentDate(long amountToAdd) {
		incrementCurrentDate(amountToAdd, defaultUnit);
	}

	public void setDefaultTemporalUnit(ChronoUnit unit) {
		defaultUnit = unit;
	}

	public ChronoUnit getDefaultTemporalUnit() {
		return defaultUnit;
	}

	public void addOneTimeUnit() {
		incrementCurrentDate(1, defaultUnit);
	}

}