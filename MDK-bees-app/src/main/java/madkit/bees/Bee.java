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
import java.util.List;

import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

/**
 * @version 2.3
 * @author Fabien Michel, Olivier Gutknecht
 */
public class Bee extends AbstractBee {

	BeeInformation leaderInfo = null;
	AgentAddress leader = null;

	@Override
	public void onActivation() {
		super.onActivation();
		requestRole(getCommunity(), getModelGroup(), "bee", null);
		requestRole(getCommunity(), getModelGroup(), "follower", null);
	}

	/** The "do it" method called by the activator */
	@Override
	public void buzz() {
		updateLeader();
		super.buzz();
	}

	private void updateLeader() {
		ObjectMessage<BeeInformation> m = nextMessage();
		if (m == null) {
			return;
		}
		if (m.getSender().equals(leader)) {// leader quitting
			leader = null;
			leaderInfo = null;
		} else {
			if (leader == null)
				followNewLeader(m);
			else {
				List<AgentAddress> queens = getAgentsWithRole(getCommunity(), getModelGroup(), AbstractBee.QUEEN_ROLE);
				if (queens != null && generator.nextDouble() < (1.0 / queens.size())) {// change leader randomly
					followNewLeader(m);
				}
			}
		}
	}

	/**
	 * @param leaderMessage
	 */
	private void followNewLeader(ObjectMessage<BeeInformation> leaderMessage) {
		leader = leaderMessage.getSender();
		leaderInfo = leaderMessage.getContent();
		myInformation.setBeeColor(leaderInfo.getBeeColor());
	}

	@Override
	protected void computeNewVelocities() {
		final Point location = myInformation.getCurrentPosition();
		// distances from bee to queen
		int dtx;
		int dty;
		if (leaderInfo != null) {
			final Point leaderLocation = leaderInfo.getCurrentPosition();
			dtx = leaderLocation.x - location.x;
			dty = leaderLocation.y - location.y;
		} else {
			dtx = generator.nextInt(5);
			dty = generator.nextInt(5);
			if (generator.nextBoolean()) {
				dtx = -dtx;
				dty = -dty;
			}
		}
		int acc = 0;
		if (getEnvironment() != null) {
			acc = (int) getEnvironment().getBeeAcceleration();
		}
		int dist = Math.abs(dtx) + Math.abs(dty);
		if (dist == 0)
			dist = 1; // avoid dividing by zero
		// the randomFromRange adds some extra jitter to prevent the bees from flying in
		// formation
		xVelocity += ((dtx * acc) / dist) + randomFromRange(2);
		yVelocity += ((dty * acc) / dist) + randomFromRange(2);
	}

	@Override
	protected int getMaxVelocity() {
		if (getEnvironment() != null) {
			return (int) getEnvironment().getBeeVelocity();
		}
		return 0;
	}
}
