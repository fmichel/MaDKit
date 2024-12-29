package madkit.simu.template;

import madkit.gui.UIProperty;
import madkit.simulation.SimuAgent;
import madkit.simulation.environment.Environment2D;

@SuppressWarnings("unchecked")
public class SimulatedAgent extends SimuAgent{
	
	@UIProperty(displayName="random move")
	public static boolean moveRandomly = true;
	
	private double x;
	private double y;

	@Override
	protected void onActivation() {
		requestRole(getCommunity(), getModelGroup(), "simuAgent");
		x = prng().nextInt(getEnvironment().getWidth());
		y = prng().nextInt(getEnvironment().getHeight());
	}
	
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}

	private void doIt() {
		moveRandomly();
	}

	/**
	 * 
	 */
	private void moveRandomly() {
		MyModel model = getModel();
		x += prng().nextDouble(model.getSpeed())*(prng().nextBoolean() ? 1 : -1);
		x %= getEnvironment().getWidth();
		if(x<0)
			x+=getEnvironment().getWidth();
		y += prng().nextDouble(model.getSpeed())*(prng().nextBoolean() ? 1 : -1);
		y %= getEnvironment().getHeight();
		if(y<0)
			y+=getEnvironment().getHeight();
		}

	/**
	 * @return the moveRandomly
	 */
	public static boolean isMoveRandomly() {
		return moveRandomly;
	}

	/**
	 * @param moveRandomly the moveRandomly to set
	 */
	public static void setMoveRandomly(boolean moveRandomly) {
		SimulatedAgent.moveRandomly = moveRandomly;
	}

}
