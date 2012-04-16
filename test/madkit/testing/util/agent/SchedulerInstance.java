package madkit.testing.util.agent;

import madkit.kernel.Scheduler;

public class SchedulerInstance extends Scheduler {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	@Override
	protected void activate() {
		setDelay(500);
	}
	public static void main(String[] args) {
		executeThisAgent();
	}
}
