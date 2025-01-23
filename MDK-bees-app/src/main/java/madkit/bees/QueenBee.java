package madkit.bees;

import java.awt.Point;
import java.util.logging.Level;

import madkit.messages.ObjectMessage;

/**
 * The leader of a group of bees.
 * 
 */
public class QueenBee extends Bee {

	static int border = 20;

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.ALL);
		requestRole(getCommunity(), getModelGroup(), QUEEN);
		super.onActivation();
		notifyFollowers();
	}

	/**
	 * Notify followers that the queen is either dead or a new one.
	 */
	private void notifyFollowers() {
		broadcast(new ObjectMessage<>(getData()), getAgentsWithRole(getCommunity(), getModelGroup(), FOLLOWER));
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

	@Override
	protected int getMaxVelocity() {
		return (int) getEnvironment().getQueenVelocity();
	}

	@Override
	protected void computeNewVelocities() {
		int acc = (int) getEnvironment().getQueenAcceleration();
		xVelocity += randomFromRange(acc);
		yVelocity += randomFromRange(acc);
	}

	/**
	 * This method is called when the agent is ending. Here we notify the followers that the
	 * queen is dead.
	 */
	@Override
	protected void onEnd() {
		setData(null);
		notifyFollowers();
		leaveRole(getCommunity(), getModelGroup(), QUEEN);
		leaveRole(getCommunity(), getModelGroup(), BEE_ROLE);
	}

}
