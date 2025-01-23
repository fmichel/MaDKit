package madkit.bees;

import java.awt.Point;

import javafx.scene.paint.Color;
import madkit.simulation.SimuAgent;

public abstract class Bee extends SimuAgent {

	protected int xVelocity;
	protected int yVelocity;

	private BeeData data;

	/**
	 * Default role for bees
	 */
	public static final String BEE_ROLE = "bee";

	/**
	 * Role for queen bee
	 */
	public static final String QUEEN = "queen";

	/**
	 * Role for follower bee
	 */
	public static final String FOLLOWER = "follower";

	@Override
	protected void onActivation() {
		initData();
		requestRole(getCommunity(), getModelGroup(), BEE_ROLE);
	}

	/**
	 * Initialize data
	 */
	private void initData() {
		data = new BeeData();
		Point position = data.getCurrentPosition();
		int x = prng().nextInt(getEnvironment().getWidth());
		int y = prng().nextInt(getEnvironment().getHeight());
		position.setLocation(x, y);
		data.getPreviousPosition().setLocation(position);
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

	protected int randomFromRange(int val) {
		val /= 2;
		return prng().nextInt(val * 2 + 1) - val;
	}

	protected BeeData getData() {
		return data;
	}

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
