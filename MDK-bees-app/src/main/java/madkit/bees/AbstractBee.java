/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.awt.Point;

import javafx.scene.paint.Color;
import madkit.simulation.SimuAgent;

/**
 * @version 2.3
 * @author Fabien Michel
 */
public abstract class AbstractBee extends SimuAgent {

	protected int xVelocity;
	protected int yVelocity;

	private BeeData data;

	/**
	 * Default role for bees     
	 */
	public static final String BEE_ROLE = "bee";

	
	/**
	 *  Role for queen bee
	 */
	public static final String QUEEN_ROLE = "queen";

	/**
	 * Role for follower bee 
	 */
	public static final String FOLLOWER_ROLE = "follower";

	@Override
	protected void onActivation() {
		super.onActivation();
		data = new BeeData();
		initData();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BeeEnvironment getEnvironment() {
		return super.getEnvironment();
	}

	/**
	 * Initialize data 
	 */
	protected void initData() {
		Point position = data.getCurrentPosition();
		if (position.x > getEnvironment().getWidth() || position.y > getEnvironment().getHeight() || position.x <= 0
				|| position.y <= 0) {
			position.setLocation(
					prng().nextInt((getEnvironment().getWidth() - 20)) + 10,
					prng().nextInt((getEnvironment().getHeight() - 20)) + 10);
			data.getPreviousPosition().setLocation(position);
		}
		int beeMAcceleration = (int) getEnvironment().getBeeAcceleration();
		xVelocity = randomFromRange(beeMAcceleration);
		yVelocity = randomFromRange(beeMAcceleration);
	}

	protected void buzz() {
		Point location = data.getCurrentPosition();
		data.getPreviousPosition().setLocation(location);

		computeNewVelocities();
		normalizeVelocities(getMaxVelocity());
		// update the bee's position
		location.x += xVelocity;
		location.y += yVelocity;

	}

	protected abstract int getMaxVelocity();

	protected abstract void computeNewVelocities();

	private void normalizeVelocities(int maxVelocity) {
		// keep speed limited to maximums
		if (xVelocity > maxVelocity)
			xVelocity = maxVelocity;
		else if (xVelocity < -maxVelocity)
			xVelocity = -maxVelocity;

		if (yVelocity > maxVelocity)
			yVelocity = maxVelocity;
		else if (yVelocity < -maxVelocity)
			yVelocity = -maxVelocity;
	}

	public int randomFromRange(int val) {
		val /= 2;
		return prng().nextInt(val * 2 + 1) - val;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " on " + getEnvironment() + " " + data;
	}

	/**
	 * @return the data
	 */
	public BeeData getData() {
		return data;
	}

}

class BeeData {

	private final Point currentPosition;
	private final Point  previousPosition;
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

	@Override
	public String toString() {
		return "<" + previousPosition + "," + currentPosition + ">";
	}

	/**
	 * @return the beeColor
	 */
	public javafx.scene.paint.Color getBeeColor() {
		return beeColor;
	}

	/**
	 * @param beeColor the beeColor to set
	 */
	public void setBeeColor(Color beeColor) {
		this.beeColor = beeColor;
	}
}
