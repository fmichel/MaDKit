package madkit.test.agents;

/**
 *
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class SimulatedAgentBis extends CGRAgent {

	private int privatePrimitiveField = 1;
	public double publicPrimitiveField = 2;

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
