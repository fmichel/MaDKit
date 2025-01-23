package madkit.bees;

import madkit.gui.SliderProperty;
import madkit.gui.UIProperty;
import madkit.simulation.environment.Environment2D;

/**
 * Class representing the size of the environment and some modeling parameters.
 * 
 */
public class BeeEnvironment extends Environment2D {

	@SliderProperty(min = 0, max = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "queenAcceleration") // slider required
	private double queenAcceleration = 13.0;

	@SliderProperty(min = 0, max = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "beeAcceleration") // slider required
	private double beeAcceleration = 4.0;

	@SliderProperty(min = 0, max = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "queenbeeVelocity") // slider required
	private double queenVelocity = 13;

	@SliderProperty(min = 0, max = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "beeVelocity") // slider required
	private double beeVelocity = 19;

	/**
	 *   
	 */
	public BeeEnvironment() {
		super(1600, 1000);
	}

	/**
	 * @return the queenAcceleration
	 */
	public double getQueenAcceleration() {
		return queenAcceleration;
	}

	/**
	 * @param queenAcceleration the queenAcceleration to set
	 */
	public void setQueenAcceleration(double queenAcceleration) {
		this.queenAcceleration = queenAcceleration;
	}

	/**
	 * @return the beeAcceleration
	 */
	public double getBeeAcceleration() {
		return beeAcceleration;
	}

	/**
	 * @param beeAcceleration the beeAcceleration to set
	 */
	public void setBeeAcceleration(double beeAcceleration) {
		this.beeAcceleration = beeAcceleration;
	}

	/**
	 * @return the queenVelocity
	 */
	public double getQueenVelocity() {
		return queenVelocity;
	}

	/**
	 * @param queenVelocity the queenVelocity to set
	 */
	public void setQueenVelocity(double queenVelocity) {
		this.queenVelocity = queenVelocity;
	}

	/**
	 * @return the beeVelocity
	 */
	public double getBeeVelocity() {
		return beeVelocity;
	}

	/**
	 * @param beeVelocity the beeVelocity to set
	 */
	public void setBeeVelocity(double beeVelocity) {
		this.beeVelocity = beeVelocity;
	}

}
