
package madkit.kernel;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;

/**
 * @author Fabien Michel
 * @version 0.92
 * @since MaDKit 5.0
 */
final class AgentThreadFactory extends Object implements ThreadFactory {

	private static final int MKRA_PRIORITY = Thread.NORM_PRIORITY - 1;
	private static final int MKDA_PRIORITY = Thread.MAX_PRIORITY;
	private final boolean daemonThreads;
	private final ThreadGroup group;

	AgentThreadFactory(final KernelAddress kernelAddress, final boolean daemonThreadFactory) {
		daemonThreads = daemonThreadFactory;
		group = new ThreadGroup(daemonThreads ? "DAEMON" : "LIFE" + kernelAddress) {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.err.println("--------------internal BUG--------------------");
				System.err.println("\n-----------------uncaught exception on " + t);// TODO
				e.printStackTrace();
			}
		};
		if (daemonThreadFactory) {
			group.setMaxPriority(MKDA_PRIORITY);
		} else {
			group.setMaxPriority(MKRA_PRIORITY);
		}
	}

	@Override
	public Thread newThread(final Runnable r) {
		final Thread t = new Thread(group, r);
		t.setDaemon(daemonThreads);
		t.setPriority(Thread.NORM_PRIORITY - 1);
		return t;
	}

	/**
	 * @param a
	 * @return
	 */
	Thread getAgentThread(Agent a) {
		final Thread[] list = new Thread[group.activeCount()];
		group.enumerate(list);
		return Arrays.stream(list).filter(t -> t.getName().equals("" + a.hashCode())).findAny().orElse(null);
	}

	@Override
	public String toString() {
		final Thread[] list = new Thread[group.activeCount()];
		group.enumerate(list);
		return Arrays.deepToString(list);
	}

}