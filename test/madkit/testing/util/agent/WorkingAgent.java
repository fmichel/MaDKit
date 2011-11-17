package madkit.testing.util.agent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

public class WorkingAgent extends DoItDuringLifeCycleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -175432600448922540L;

	public WorkingAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	public WorkingAgent() {
		super(true, true, true);
	}

	public void doIt() {
		for (int i = 0; i < 100000000; i++) {
			@SuppressWarnings("unused")
			double d = Math.random() * 2;
			d *= Math.PI * 100;
			// if(i % 10000000 == 0)
			// if(logger != null)
			// logger.info("yo");
		}
		super.doIt();
	}

	public void doIta() {
		for (long i = 0; i < Long.MAX_VALUE; i++) {
			@SuppressWarnings("unused")
			double d = Math.random() * 2;
			d *= Math.PI * 100;
			// if(i % 10000000 == 0)
			// if(logger != null)
			// logger.info("yo");
		}
		super.doIt();
	}

	public static void main(String[] args) {
		String[] myArgs = {BooleanOption.desktop.toString()};
		AbstractAgent.executeThisAgent(myArgs);
	}
}