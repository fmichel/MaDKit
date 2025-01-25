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
package madkit.simu.template;

import madkit.gui.SliderProperty;
import madkit.random.RandomizedDouble;
import madkit.simulation.SimuModel;

/**
 * The `MyModel` class extends the `SimuModel` class and represents the model for the
 * simulation. It uses annotations to specify UI properties and slider properties for the
 * simulation parameters.
 */
public class MyModel extends SimuModel {

	/**
	 * The speed property of the model, which can be adjusted using a slider in the UI. The
	 * slider has a minimum value of 1, a maximum value of 50, and a scroll precision of 1.
	 */
	@SliderProperty(min = 1, max = 50, scrollPrecision = 1)
	@RandomizedDouble(min = 5, max = 20)
	private double speed = 10.0;

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
