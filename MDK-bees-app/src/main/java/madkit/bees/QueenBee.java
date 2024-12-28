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

import madkit.kernel.Message;
import madkit.messages.ObjectMessage;

/**
 * The leader of a group.
 * 
 * @version 2.0.0.3
 * @author Fabien Michel, Olivier Gutknecht
 */
public class QueenBee extends AbstractBee {

	private static final long serialVersionUID = -6999130646300839798L;
	static int border = 20;

	@Override
	protected void buzz() {
		Message m = nextMessage();
		if (m != null) {
			reply(new ObjectMessage<BeeInformation>(myInformation), m);
		}

		super.buzz();

		if (getEnvironment() != null) {
			// check to see if the queen hits the edge
			final Point location = myInformation.getCurrentPosition();
			if (location.x < border || location.x > (getEnvironment().getWidth() - border)) {
				xVelocity = -xVelocity;
				location.x += (xVelocity);
			}
			if (location.y < border || location.y > (getEnvironment().getHeight() - border)) {
				yVelocity = -yVelocity;
				location.y += (yVelocity);
			}
		}
	}

	@Override
	protected void onActivation() {
		super.onActivation();
		requestRole(getCommunity(), getModelGroup(), AbstractBee.QUEEN_ROLE, null);
		requestRole(getCommunity(), getModelGroup(), AbstractBee.BEE_ROLE, null);
		broadcast(new ObjectMessage<>(myInformation), getAgentsWithRole(getCommunity(), getModelGroup(), AbstractBee.FOLLOWER_ROLE));
	}

	@Override
	protected void onEnd() {
		broadcast(new ObjectMessage<>(myInformation), getAgentsWithRole(getCommunity(), getModelGroup(), AbstractBee.FOLLOWER_ROLE));
	}

	@Override
	protected int getMaxVelocity() {
		if (getEnvironment() != null) {
			return (int) getEnvironment().getQueenVelocity();
		}
		return 0;
	}

	@Override
	protected void computeNewVelocities() {
		if (getEnvironment() != null) {
			int acc = (int) getEnvironment().getQueenAcceleration();
			xVelocity += randomFromRange(acc);
			yVelocity += randomFromRange(acc);
		}
	}

}
