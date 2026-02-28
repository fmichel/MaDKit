/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
import madkit.kernel.DaemonAgent;

/**
 * A background service agent that implements {@link DaemonAgent}.
 * <p>
 * Because it implements {@link DaemonAgent}, this agent will <strong>not</strong> prevent
 * the MaDKit kernel from shutting down. When every non-daemon agent has finished, the
 * kernel terminates and this agent is automatically stopped — even if its
 * {@link #onLive()} loop is still running.
 * <p>
 * This agent has no {@code main()} method because it is designed to be launched by another
 * agent (see {@link DaemonDemo}).
 *
 * @see DaemonAgent
 * @see DaemonDemo
 */
public class BackgroundService extends Agent implements DaemonAgent {

	/**
	 * Logs that the background service has started.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Background service activated (daemon)");
	}

	/**
	 * Runs an infinite monitoring loop, logging a heartbeat every second. Uses
	 * {@link #exitOnKill()} to allow graceful termination when the kernel shuts down.
	 */
	@Override
	protected void onLive() {
		int tick = 0;
		while (true) {
			final int current = ++tick;
			getLogger().info(() -> "Background heartbeat #" + current);
			pause(1000);
			exitOnKill();
		}
	}

	/**
	 * Logs that the background service is shutting down.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "Background service stopped");
	}
}
