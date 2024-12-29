/*
 * Copyright 1997, 2012, 2024 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.bees;

import madkit.gui.SliderProperty;
import madkit.gui.UIProperty;
import madkit.simulation.environment.Environment2D;

/**
 * Class representing the size of the environment and some modeling parameters.
 * 
 */
public class BeeEnvironment extends Environment2D {

	@SliderProperty(minValue = 0, maxValue = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "queenAcceleration") // slider required
	private double queenAcceleration = 13.0;

	@SliderProperty(minValue = 0, maxValue = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "beeAcceleration") // slider required
	private double beeAcceleration = 4.0;

	@SliderProperty(minValue = 0, maxValue = 20, scrollPrecision = 1)
	@UIProperty(category = "Bees", displayName = "queenbeeVelocity") // slider required
	private double queenVelocity = 13;

	@SliderProperty(minValue = 0, maxValue = 20, scrollPrecision = 1)
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
