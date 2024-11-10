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
import java.util.Random;

import javafx.scene.paint.Color;
import madkit.kernel.Agent;
import madkit.simulation.SimuParticipant;

/**
 * @version 2.3
 * @author Fabien Michel
 */
public abstract class AbstractBee extends Agent implements SimuParticipant {

	protected static final Random generator = new Random(System.currentTimeMillis());

	protected int xVelocity;
	protected int yVelocity;

	protected BeeInformation myInformation;

	public static final String BEE_ROLE = "bee";

	public static final String QUEEN_ROLE = "queen";

	public static final String FOLLOWER_ROLE = "follower";

	@Override
	protected void onActivation() {
		super.onActivation();
		myInformation = new BeeInformation();
		setEnvironment();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BeeEnvironment getEnvironment() {
		return SimuParticipant.super.getEnvironment();
	}

	public void setEnvironment() {
		final Point myLocation = myInformation.getCurrentPosition();
		if (myLocation.x > getEnvironment().getWidth() || myLocation.y > getEnvironment().getHeight() || myLocation.x <= 0
				|| myLocation.y <= 0) {
			myLocation.setLocation(generator.nextInt((int) (getEnvironment().getWidth() - 20)) + 10,
					generator.nextInt((int) (getEnvironment().getHeight() - 20)) + 10);
			myInformation.getPreviousPosition().setLocation(myLocation);
		}
		int beeMAcceleration = (int) getEnvironment().getBeeAcceleration();
		xVelocity = randomFromRange(beeMAcceleration);
		yVelocity = randomFromRange(beeMAcceleration);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " on " + getEnvironment() + " " + myInformation;
	}

	protected void buzz() {
		final Point location = myInformation.getCurrentPosition();
		myInformation.getPreviousPosition().setLocation(location);

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
		return generator.nextInt(val * 2 + 1) - val;
//		return generator.nextInt(val /2, val + 1);
	}

}

class BeeInformation {

	private final Point currentPosition;
	private final Point  previousPosition;
	private Color beeColor;

	public BeeInformation() {
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
