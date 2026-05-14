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
package madkit.sample.agent.threaded;

import madkit.kernel.Agent;

/**
 * Demonstrates a long-running autonomous agent with graceful termination.
 * <p>
 * This agent runs an infinite loop, logging a counter every second. It uses
 * {@link #exitOnKill()} to check whether the agent's thread has been interrupted (e.g. by
 * a kill request). When interrupted, an {@link madkit.kernel.AgentInterruptedException}
 * is thrown, which MaDKit catches to trigger the {@link #onEnd()} cleanup method.
 * <p>
 * This pattern — {@code while(true) { work; pause; exitOnKill(); }} — is the idiomatic
 * way to write a long-running MaDKit agent that can be cleanly stopped.
 *
 * @see Agent#exitOnKill()
 * @see Agent#pause(int)
 * @see Agent#onEnd()
 */
public class AutonomousAgent extends Agent {

	/**
	 * Called when the agent is launched. Logs that the autonomous agent has been activated.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Autonomous agent activated");
	}

	/**
	 * Runs an infinite loop, logging a counter and pausing 1 second between iterations. Calls
	 * {@link #exitOnKill()} each iteration to allow graceful termination.
	 */
	@Override
	protected void onLive() {
		int counter = 0;
		while (true) {
			getLogger().info("Working... step " + (++counter));
			pause(1000);
			exitOnKill();
		}
	}

	/**
	 * Called when the agent is killed or finishes. Logs a graceful shutdown message.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "Autonomous agent shutting down gracefully");
	}

	/**
	 * Launches a single instance of {@link AutonomousAgent}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
