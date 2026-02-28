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
package madkit.sample.gui.basic;

import madkit.kernel.Agent;

/**
 * Demonstrates a threaded agent whose log messages stream into the default GUI in
 * real-time.
 * <p>
 * Because this agent overrides {@link #onLive()}, MaDKit automatically gives it its own
 * execution thread. Combined with {@link #setupDefaultGUI()}, log messages produced
 * during the live phase appear progressively in the output pane, providing visual
 * feedback of the agent's activity.
 * <p>
 * The agent runs 10 processing steps with a 1-second pause between each, then
 * terminates.
 *
 * @see Agent#setupDefaultGUI()
 * @see Agent#onLive()
 * @see Agent#pause(int)
 */
public class ThreadedAgentWithGUI extends Agent {

	/**
	 * Called when the agent is launched. Creates the default GUI window and logs a startup
	 * message.
	 */
	@Override
	protected void onActivation() {
		setupDefaultGUI();
		getLogger().info(() -> "Threaded agent with GUI started");
	}

	/**
	 * Main behavior loop — runs 10 processing steps with a 1-second pause between each.
	 * Each step logs a progress message that appears in the GUI output pane in real-time.
	 */
	@Override
	protected void onLive() {
		for (int i = 1; i <= 10; i++) {
			final int step = i;
			getLogger().info(() -> "Processing step " + step + "/10...");
			pause(1000);
		}
	}

	/**
	 * Called when the agent finishes (after {@link #onLive()} returns). Logs a completion
	 * message.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "All steps completed. Agent ending.");
	}

	/**
	 * Launches a single instance of {@link ThreadedAgentWithGUI}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
