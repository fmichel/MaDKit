package madkit.simu.template;

import madkit.gui.SliderProperty;
import madkit.gui.UIProperty;
import madkit.simulation.SimuModel;

/**
 * The `MyModel` class extends the `SimuModel` class and represents the model
 * for the simulation. It uses annotations to specify UI properties and slider
 * properties for the simulation parameters.
 */
public class MyModel extends SimuModel {

	/**
	 * The speed property of the model, which can be adjusted using a slider in the
	 * UI. The slider has a minimum value of 1, a maximum value of 50, and a scroll
	 * precision of 1.
	 */
	@SliderProperty(minValue = 1, maxValue = 50, scrollPrecision = 1)
	@UIProperty(displayName = "speed")
	private double speed = 20.0;

	/**
	 * Gets the speed of the model.
	 *
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed of the model.
	 *
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
}
