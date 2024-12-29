
package madkit.simulation.scheduler;

import java.math.BigDecimal;

/**
 * A timer based on ticks for simulation purposes. This class extends the
 * SimuTimer class and uses BigDecimal for tick values.
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
	 * Constructs a TickBasedTimer with an initial tick value of
	 * {@link BigDecimal#ZERO}.
	 */
	public TickBasedTimer() {
		this(BigDecimal.ZERO);
	}

	/**
	 * Constructs a TickBasedTimer with the specified initial tick value and an end
	 * tick value of {@link Double#MAX_VALUE}.
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
