package madkit.simulation;

import java.math.BigDecimal;


public class TickBasedTimer extends SimulationTimer<BigDecimal> {

	public TickBasedTimer(BigDecimal initialTick, BigDecimal endTick) {
		super(initialTick,endTick);
	}
	
	/**
	 * Creates a tick-based time whose initial tick value is
	 * {@link BigDecimal#ZERO};
	 *
	 */
	public TickBasedTimer() {
		this(BigDecimal.ZERO);
	}

	public TickBasedTimer(BigDecimal initialTick) {
		this(initialTick,BigDecimal.valueOf(Double.MAX_VALUE));
	}

	public void incrementCurrentTick(double delta) {
		incrementCurrentTick(BigDecimal.valueOf(delta));
	}

	public void incrementCurrentTick(BigDecimal delta) {
		setCurrentTime(getCurrentTime().add(delta));
	}

	/**
	 * Shortcut for <code>setCurrentTick(getCurrentTick().add(delta));</code>
	 *
	 * @param delta specifies how much time should be added
	 */
	public void addDeltaTime(BigDecimal delta) {
		setCurrentTime(getCurrentTime().add(delta));
	}

	public void addOneTimeUnit() {
		addDeltaTime(BigDecimal.ONE);
	}

}