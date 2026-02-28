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
package madkit.sample.agent.lifecycle;

import madkit.kernel.Agent;

/**
 * Demonstrates what happens when an agent throws an exception during {@link #onLive()}.
 * <p>
 * Unlike a crash in {@link #onActivation()}, a crash in {@code onLive()} does trigger
 * {@link #onEnd()}. This is because the agent was successfully activated (it is alive),
 * and MaDKit guarantees cleanup via {@code onEnd()} for any agent that was alive.
 * <p>
 * Run this class to observe that {@code onEnd()} is indeed called after the live crash.
 *
 * @see Agent#onLive()
 * @see Agent#onEnd()
 */
public class CrashInLiveDemo extends Agent {

	/**
	 * Called when the agent is launched. Logs successful activation.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Activated successfully");
	}

	/**
	 * Throws a {@link RuntimeException} intentionally to demonstrate crash-in-live behavior.
	 *
	 * @throws RuntimeException always, to simulate a runtime failure during live
	 */
	@Override
	protected void onLive() {
		getLogger().info(() -> "About to crash in onLive()...");
		throw new RuntimeException("Intentional crash in onLive!");
	}

	/**
	 * Called after the crash in {@link #onLive()}.
	 * Demonstrates that cleanup is guaranteed for agents that were alive.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "onEnd() called after crash in onLive() — cleanup is guaranteed!");
	}

	/**
	 * Launches a single instance of {@link CrashInLiveDemo}.
	 * The kernel will log the crash and then call {@code onEnd()}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
