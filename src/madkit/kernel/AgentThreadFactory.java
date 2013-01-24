/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.util.concurrent.ThreadFactory;

/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0
 * 
 */
final class AgentThreadFactory extends Object implements ThreadFactory {

	final private boolean		daemonThreads;
	final private ThreadGroup	group;

	AgentThreadFactory(final KernelAddress kernelAddress, final boolean daemonThreadFactory) {
		daemonThreads = daemonThreadFactory;
		group = new ThreadGroup(daemonThreads ? "DAEMON" : "LIFE" + kernelAddress) {

			public void uncaughtException(Thread t, Throwable e) {
				if (e instanceof KilledException) {
					e.printStackTrace();
					throw new AssertionError("killedException uncaught");
				}
				System.err.println("--------------internal BUG--------------------");
				System.err.println("\n-----------------uncaught exception on " + t);// TODO
				e.printStackTrace();
			}
		};
	}

	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(final Runnable r) {
		final Thread t = new Thread(group, r);
		t.setDaemon(daemonThreads);
		return t;
	}

	ThreadGroup getThreadGroup() {
		return group;
	}

}
