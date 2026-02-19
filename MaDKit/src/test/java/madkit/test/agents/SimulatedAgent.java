package madkit.test.agents;

import static madkit.kernel.MadkitConcurrentTestCase.GROUP;
import static madkit.kernel.MadkitConcurrentTestCase.ROLE;

import madkit.simulation.SimuAgent;

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
