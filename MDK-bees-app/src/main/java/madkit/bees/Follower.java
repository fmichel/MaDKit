package madkit.bees;

import java.awt.Point;
import java.util.List;

import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

/**
 * A bee that follows a leader
 */
public class Follower extends Bee {

	BeeData leaderData = null;
	AgentAddress leader = null;

	@Override
	public void onActivation() {
		super.onActivation();
		requestSimuRole(getModelGroup(), FOLLOWER);
	}

	/** The "do it" method called by the activator */
	@Override
	public void buzz() {
		ObjectMessage<BeeData> m = nextMessage();
		if (m != null) {
			updateLeader(m);
		}
		super.buzz();
	}

	private void updateLeader(ObjectMessage<BeeData> m) {
		BeeData content = m.getContent();
		if (content == null) {
			if (m.getSender().equals(leader)) {// leader quitting
				leader = null;
			}
			return;
		}
		if (leader == null) {
			followNewLeader(m);
		}
		else {
				List<AgentAddress> queens = getAgentsWithRole(getCommunity(), getModelGroup(), Bee.QUEEN);
				if (!queens.isEmpty() && prng().nextDouble() < (1.0 / queens.size())) {// change leader randomly
					followNewLeader(m);
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
