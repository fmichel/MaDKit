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
import java.util.logging.Level;

import madkit.messages.ObjectMessage;

/**
 * The leader of a group of bees.
 * 
 */
public class QueenBee extends Bee {

	/** The border. */
	static int border = 20;

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.ALL);
		playRole(BeeOrganization.QUEEN);
		super.onActivation();
		notifyFollowers();
	}

	/**
	 * The "do it" method called by the activator
	 */
	@Override
	protected void buzz() {
		super.buzz();
		// check to see if the queen hits the edge
		Point location = getData().getCurrentPosition();
		if (location.x < border || location.x > (getEnvironment().getWidth() - border)) {
			xVelocity = -xVelocity;
			location.x += (xVelocity);
		}
		if (location.y < border || location.y > (getEnvironment().getHeight() - border)) {
			yVelocity = -yVelocity;
			location.y += (yVelocity);
		}
	}

	/**
	 * This method is called when the agent is ending. Here we notify the followers that the
	 * queen is dead.
	 */
	@Override
	protected void onEnd() {
		setData(null);
		notifyFollowers();
		leaveRole(BeeOrganization.QUEEN);
		leaveRole(BeeOrganization.BEE);
	}

	/**
	 * Gets the max velocity.
	 *
	 * @return the max velocity
	 */
	@Override
	protected int getMaxVelocity() {
		return (int) getEnvironment().getQueenVelocity();
	}

	/**
	 * Compute new velocities.
	 */
	@Override
	protected void computeNewVelocities() {
		int acc = (int) getEnvironment().getQueenAcceleration();
		xVelocity += randomFromRange(acc);
		yVelocity += randomFromRange(acc);
	}

	/**
	 * Notify followers that the queen is either dead or a new one.
	 */
	private void notifyFollowers() {
		broadcast(new ObjectMessage<>(getData()),
				getAgentsWithRole(getCommunity(), getModelGroup(), BeeOrganization.FOLLOWER));
	}

}
