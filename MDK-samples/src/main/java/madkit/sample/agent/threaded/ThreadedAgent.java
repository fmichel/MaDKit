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
package madkit.sample.agent.threaded;

import madkit.kernel.Agent;

/**
 * Demonstrates an agent that gets its own thread by overriding {@link #onLive()}.
 * <p>
 * In MaDKit, any agent that overrides {@link #onLive()} is automatically given its own
 * execution thread. This agent runs a finite loop of 10 iterations, pausing 500 ms
 * between each, and then terminates naturally when {@code onLive()} returns.
 * <p>
 * The full lifecycle is: {@link #onActivation()} → {@link #onLive()} → {@link #onEnd()}.
 *
 * @see Agent#onLive()
 * @see Agent#pause(int)
 */
public class ThreadedAgent extends Agent {

	/**
	 * Called when the agent is launched.
	 * Logs that activation has occurred.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Threaded agent activated");
	}

	/**
	 * Main behavior loop — runs 10 iterations with a 500 ms pause between each.
	 * Because this method is overridden, the agent receives its own thread.
	 */
	@Override
	protected void onLive() {
		for (int i = 1; i <= 10; i++) {
			final int iteration = i;
			getLogger().info(() -> "Iteration " + iteration);
			pause(500);
		}
	}

	/**
	 * Called when the agent finishes (after {@link #onLive()} returns).
	 * Logs that the agent is ending.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "Agent is ending");
	}

	/**
	 * Launches a single instance of {@link ThreadedAgent}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
