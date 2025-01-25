package madkit.test.agents;

import static madkit.kernel.MadkitUnitTestCase.GROUP;
import static madkit.kernel.MadkitUnitTestCase.ROLE;

import madkit.simulation.SimuAgent;

/**
 *
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class SimulatedAgent extends SimuAgent {

	private int privatePrimitiveField = 1;
	public double publicPrimitiveField = 2;
	@SuppressWarnings("unused")
	private Object objectField = new Object();
	private boolean activated = false;

	@Override
	protected void onActivation() {
		createSimuGroup(GROUP);
		requestSimuRole(GROUP, ROLE);
	}

	public void doIt() {
//		getLogger().info("doing it");
	}

	/**
	 * @return the privatePrimitiveField
	 */
	public int getPrivatePrimitiveField() {
		return privatePrimitiveField;
	}

	/**
	 * @param privatePrimitiveField the privatePrimitiveField to set
	 */
	public void setPrivatePrimitiveField(int privatePrimitiveField) {
		this.privatePrimitiveField = privatePrimitiveField;
	}
}
