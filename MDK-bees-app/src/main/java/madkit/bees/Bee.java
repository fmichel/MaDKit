/*******************************************************************************
 * Copyright (c) 1997, 2025, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.bees;

import java.awt.Point;
import java.util.List;

import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

/**
 * A bee that follows a leader
 * @version 2.3
 * @author Fabien Michel, Olivier Gutknecht
 */
public class Bee extends AbstractBee {

	BeeData leaderData = null;
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
		ObjectMessage<BeeData> m = nextMessage();
		if (m == null) {
			return;
		}
		if (m.getSender().equals(leader)) {// leader quitting
			leader = null;
			leaderData = null;
		} else {
			if (leader == null)
				followNewLeader(m);
			else {
				List<AgentAddress> queens = getAgentsWithRole(getCommunity(), getModelGroup(), AbstractBee.QUEEN_ROLE);
				if (queens != null && prng().nextDouble() < (1.0 / queens.size())) {// change leader randomly
					followNewLeader(m);
				}
			}
		}
	}

	/**
	 * @param leaderMessage
	 */
	private void followNewLeader(ObjectMessage<BeeData> leaderMessage) {
		leader = leaderMessage.getSender();
		leaderData = leaderMessage.getContent();
		getData().setBeeColor(leaderData.getBeeColor());
	}

	@Override
	protected void computeNewVelocities() {
		Point location = getData().getCurrentPosition();
		// distances from bee to queen
		int dtx;
		int dty;
		if (leaderData != null) {
			final Point leaderLocation = leaderData.getCurrentPosition();
			dtx = leaderLocation.x - location.x;
			dty = leaderLocation.y - location.y;
		} else {
			dtx = prng().nextInt(5);
			dty = prng().nextInt(5);
			if (prng().nextBoolean()) {
				dtx = -dtx;
				dty = -dty;
			}
		}
			int acc = (int) getEnvironment().getBeeAcceleration();
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
			return (int) getEnvironment().getBeeVelocity();
	}
}
