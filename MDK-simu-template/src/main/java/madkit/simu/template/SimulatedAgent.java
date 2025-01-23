package madkit.simu.template;

import madkit.random.RandomizedBoolean;
import madkit.simulation.SimuAgent;
import madkit.simulation.environment.Environment2D;

@SuppressWarnings("all") // remove irrelevant warnings
public class SimulatedAgent extends SimuAgent {

	@RandomizedBoolean
	public boolean moveRandomly = true;

	/**
	 * Probed by the MyViewer class to display the agent's position.
	 */
	private double x;
	/**
	 * Probed by the MyViewer class to display the agent's position.
	 */
	private double y;

	@Override
	protected void onActivation() {
		requestRole(getCommunity(), getModelGroup(), "simuAgent");
		x = prng().nextInt(getEnvironment().getWidth());
		y = prng().nextInt(getEnvironment().getHeight());
	}

	/**
	 * 
	 */
	private void moveRandomly() {
		MyModel model = getModel();
		if (moveRandomly) {
			x += prng().nextDouble(model.getSpeed()) * (prng().nextBoolean() ? 1 : -1);
			y += prng().nextDouble(model.getSpeed()) * (prng().nextBoolean() ? 1 : -1);
		} else {
			x += 1;
			y += 1;
		}

		// wrap around
		x += getEnvironment().getWidth();
		x %= getEnvironment().getWidth();
		y += getEnvironment().getHeight();
		y %= getEnvironment().getHeight();
	}

	/**
	 * This method is called at each simulation step.
	 */
	private void doIt() {
		moveRandomly();
	}

	/**
	 * This override allows to automatically cast the result of this method to the type used
	 * in the simulation model.
	 */
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}
}