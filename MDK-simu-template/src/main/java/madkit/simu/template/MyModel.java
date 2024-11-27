package madkit.simu.template;

import madkit.simulation.Parameter;
import madkit.simulation.SimulationModel;
import madkit.simulation.SliderAnnotation;

public class MyModel extends SimulationModel {

	@SliderAnnotation(minValue = 0, maxValue = 50, scrollPrecision = 1)
	@Parameter(displayName = "speed") // slider required
	private double speed = 20.0;// //meters per hour

	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
