package madkit.simu.template;

import madkit.gui.UIProperty;

public class AnotherSImuAgent extends SimulatedAgent {
	@UIProperty(displayName = "a property")
	public static double valueS = 100;

	/**
	 * @return the valueS
	 */
	public static double getValueS() {
		return valueS;
	}

	/**
	 * @param valueS the valueS to set
	 */
	public static void setValueS(double valueS) {
		AnotherSImuAgent.valueS = valueS;
	}

}
