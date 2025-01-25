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

import madkit.random.RandomizedBoolean;
import madkit.simulation.SimuAgent;
import madkit.simulation.environment.Environment2D;

/**
 * The Class SimulatedAgent.
 */
@SuppressWarnings("all") // remove irrelevant warnings
public class SimulatedAgent extends SimuAgent {

	/** The move randomly. */
	@RandomizedBoolean
	private boolean moveRandomly = true;

	/**
	 * Probed by the MyViewer class to display the agent's position.
	 */
	private double x;
	/**
	 * Probed by the MyViewer class to display the agent's position.
	 */
	private double y;

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
		playRole("simuAgent");
		x = prng().nextInt(getEnvironment().getWidth());
		y = prng().nextInt(getEnvironment().getHeight());
	}

	/**
	 * 
	 */
	private void moveRandomly() {
		MyModel model = getModel();
		if (moveRandomly) {
			x += prng().nextDouble(model.getSpeed()) * prng().nextInt(2) * (prng().nextBoolean() ? 1 : -1);
			y += prng().nextDouble(model.getSpeed()) * prng().nextInt(2) * (prng().nextBoolean() ? 1 : -1);
		} else {
			x += 1;
			y += 1;
		}
		// wrap around
		x += getEnvironment().getWidth();
		x %= getEnvironment().getWidth();
		y += getEnvironment().getHeight();
		y %= getEnvironment().getHeight();
	}

	/**
	 * This method is called at each simulation step.
	 */
	private void doIt() {
		moveRandomly();
	}

	/**
	 * This override allows to automatically cast the result of this method to the type used
	 * in the simulation model.
	 */
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}
}