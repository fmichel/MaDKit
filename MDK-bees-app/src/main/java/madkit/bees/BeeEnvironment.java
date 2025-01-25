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
package madkit.bees;

import madkit.gui.SliderProperty;
import madkit.simulation.environment.Environment2D;

/**
 * The `BeeEnvironment` class extends `Environment2D` to provide additional configuration
 * capabilities specific to the bee simulation.
 * <p>
 * This class defines the size of the environment and some modeling parameters. It defines
 * Java bean mutators for the queen and bee acceleration and velocity so that they can be
 * set from the GUI.
 * 
 */
public class BeeEnvironment extends Environment2D {

	@SliderProperty(category = "Bees", min = 0, max = 20, scrollPrecision = 1)
	private double queenAcceleration = 13.0;

	@SliderProperty(category = "Bees", min = 0, max = 20, scrollPrecision = 1)
	private double beeAcceleration = 4.0;

	@SliderProperty(category = "Bees", min = 0, max = 20, scrollPrecision = 1)
	private double queenVelocity = 13;

	@SliderProperty(category = "Bees", min = 0, max = 20, scrollPrecision = 1)
	private double beeVelocity = 19;

	/**
	 * Constructs a BeeEnvironment with default dimensions (1600x1000).
	 */
	public BeeEnvironment() {
		super(1500, 1000);
	}

	/**
	 * Returns the acceleration of queens.
	 * 
	 * @return the acceleration of queens
	 */
	public double getQueenAcceleration() {
		return queenAcceleration;
	}

	/**
	 * Sets the acceleration of queens.
	 * 
	 * @param queenAcceleration the acceleration of queens
	 */
	public void setQueenAcceleration(double queenAcceleration) {
		this.queenAcceleration = queenAcceleration;
	}

	/**
	 * Returns the acceleration of bees.
	 * 
	 * @return the acceleration of bees
	 */
	public double getBeeAcceleration() {
		return beeAcceleration;
	}

	/**
	 * Sets the acceleration of bees.
	 * 
	 * @param beeAcceleration the beeAcceleration to set
	 */
	public void setBeeAcceleration(double beeAcceleration) {
		this.beeAcceleration = beeAcceleration;
	}

	/**
	 * Returns the queen velocity.
	 * 
	 * @return the queenVelocity
	 */
	public double getQueenVelocity() {
		return queenVelocity;
	}

	/**
	 * Sets the queen velocity.
	 * 
	 * @param queenVelocity the queenVelocity to set
	 */
	public void setQueenVelocity(double queenVelocity) {
		this.queenVelocity = queenVelocity;
	}

	/**
	 * Returns the bee velocity.
	 * 
	 * @return the beeVelocity
	 */
	public double getBeeVelocity() {
		return beeVelocity;
	}

	/**
	 * Sets the bee velocity.
	 * 
	 * @param beeVelocity the beeVelocity to set
	 */
	public void setBeeVelocity(double beeVelocity) {
		this.beeVelocity = beeVelocity;
	}

}
