package madkit.testing.util.agent;

import madkit.kernel.Scheduler;


public class SchedulerGUI extends Scheduler {
	
	
	public SchedulerGUI() {
		setDelay(500);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent();
	}

}
