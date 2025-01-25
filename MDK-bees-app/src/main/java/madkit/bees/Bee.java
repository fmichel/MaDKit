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

import java.awt.Point;

import javafx.scene.paint.Color;
import madkit.simulation.SimuAgent;

/**
 * The Class Bee.
 */
public abstract class Bee extends SimuAgent {

	/** The x velocity. */
	protected int xVelocity;

	/** The y velocity. */
	protected int yVelocity;
	private BeeData data;

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
		initData();
		playRole(BeeOrganization.BEE);
	}

	/**
	 * Initialize data
	 */
	private void initData() {
		data = new BeeData();
		int beeMAcceleration = (int) getEnvironment().getBeeAcceleration();
		xVelocity = randomFromRange(beeMAcceleration);
		yVelocity = randomFromRange(beeMAcceleration);
		randomLocation();
	}
	
	/**
	 * Sets location to a random position.
	 */
	public void randomLocation() {
		Point position = data.getCurrentPosition();
		int x = prng().nextInt(BeeEnvironment.margins, getEnvironment().getWidth() - BeeEnvironment.margins);
		int y = prng().nextInt(BeeEnvironment.margins, getEnvironment().getHeight() - BeeEnvironment.margins);
		position.setLocation(x, y);
		data.getPreviousPosition().setLocation(position);
	}

	/**
	 * This method is called when the scheduler do one step of the simulation.
	 */
	protected void buzz() {
		Point location = data.getCurrentPosition();
		data.getPreviousPosition().setLocation(location);
		computeNewVelocities();
		normalizeVelocities(getMaxVelocity());
		// update the bee's position
		location.x += xVelocity;
		location.y += yVelocity;

	}

	/**
	 * Gets the max velocity.
	 *
	 * @return the max velocity
	 */
	protected abstract int getMaxVelocity();

	/**
	 * Compute new velocities.
	 */
	protected abstract void computeNewVelocities();

	private void normalizeVelocities(int maxVelocity) {
		// keep speed limited to maximums
		if (xVelocity > maxVelocity) {
			xVelocity = maxVelocity;
		} else if (xVelocity < -maxVelocity) {
			xVelocity = -maxVelocity;
		}
		if (yVelocity > maxVelocity) {
			yVelocity = maxVelocity;
		} else if (yVelocity < -maxVelocity) {
			yVelocity = -maxVelocity;
		}
	}

	/**
	 * Random from range.
	 *
	 * @param val the val
	 * @return the int
	 */
	protected int randomFromRange(int val) {
		val /= 2;
		return prng().nextInt(val * 2 + 1) - val;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	protected BeeData getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	protected void setData(BeeData data) {
		this.data = data;
	}

	/**
	 * Redefines to benefit from automatic casting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BeeEnvironment getEnvironment() {
		return super.getEnvironment();
	}
}

/**
 * This class represents the data associated with a bee agent. It contains the current and
 * previous positions of the bee, as well as the color of the bee.
 */
class BeeData {

	private Point currentPosition;
	private Point previousPosition;
	private Color beeColor;

	public BeeData() {
		currentPosition = new Point();
		previousPosition = new Point();
		beeColor = Color.color(Math.random(), Math.random(), Math.random());
	}

	/**
	 * @return the currentPosition
	 */
	public Point getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * @return the previousPosition
	 */
	public Point getPreviousPosition() {
		return previousPosition;
	}

	/**
	 * @return the beeColor
	 */
	public Color getBeeColor() {
		return beeColor;
	}

	/**
	 * @param beeColor the beeColor to set
	 */
	public void setBeeColor(Color beeColor) {
		this.beeColor = beeColor;
	}
}
