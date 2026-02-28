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
 * Demonstrates the simplest way to create an agent with a default GUI window.
 * <p>
 * Calling {@link #setupDefaultGUI()} in {@link #onActivation()} creates an
 * {@link madkit.kernel.FXAgentStage} containing an {@link madkit.gui.FXOutputPane} that
 * displays the agent's log output. This is the most straightforward approach when you
 * just need a window showing what the agent is doing.
 * <p>
 * This agent is non-threaded: it only runs {@link #onActivation()} and then stays alive
 * as a passive agent.
 *
 * @see Agent#setupDefaultGUI()
 * @see madkit.kernel.FXAgentStage
 * @see madkit.gui.FXOutputPane
 */
public class AgentWithDefaultGUI extends Agent {

	/**
	 * Called when the agent is launched. Creates the default GUI window and logs messages
	 * that will appear in the GUI output pane.
	 */
	@Override
	protected void onActivation() {
		setupDefaultGUI();
		getLogger().info(() -> "Default GUI created! This message appears in the GUI log area.");
		getLogger().info(() -> "The default GUI shows an output pane bound to the agent's logger.");
	}

	/**
	 * Launches a single instance of {@link AgentWithDefaultGUI}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
