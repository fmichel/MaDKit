/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.sample.agent.daemon;

import madkit.kernel.Agent;

/**
 * Launches a {@link BackgroundService} daemon agent, performs a short task, and then
 * finishes — causing the kernel to shut down.
 * <p>
 * Because {@link BackgroundService} implements {@link madkit.kernel.DaemonAgent}, it does
 * not prevent the kernel from stopping. When this demo agent returns from
 * {@link #onLive()}, no non-daemon agents remain active, so the kernel terminates and the
 * {@link BackgroundService} is automatically stopped.
 * <p>
 * This pattern is useful for launching monitoring or housekeeping services that should
 * only live as long as the real work is being done.
 *
 * @see BackgroundService
 * @see madkit.kernel.DaemonAgent
 */
public class DaemonDemo extends Agent {

	/** The number of work iterations to perform before finishing. */
	private static final int ITERATION_COUNT = 3;

	/**
	 * Launches a {@link BackgroundService} daemon agent.
	 */
	@Override
	protected void onActivation() {
		launchAgent(new BackgroundService());
		getLogger().info(() -> "BackgroundService launched");
	}

	/**
	 * Performs {@value #ITERATION_COUNT} iterations of work with 1.5-second pauses, then
	 * returns — triggering kernel shutdown and daemon termination.
	 */
	@Override
	protected void onLive() {
		for (int i = 1; i <= ITERATION_COUNT; i++) {
			final int current = i;
			getLogger().info(() -> "DaemonDemo working... iteration " + current + "/" + ITERATION_COUNT);
			pause(1500);
		}
		getLogger().info(() -> "DaemonDemo finished — kernel will shut down, stopping the daemon");
	}

	/**
	 * Launches a single instance of {@link DaemonDemo}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
