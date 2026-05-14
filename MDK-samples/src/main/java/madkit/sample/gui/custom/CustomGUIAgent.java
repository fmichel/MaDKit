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
package madkit.sample.gui.custom;

import madkit.kernel.Agent;

/**
 * Demonstrates how to use a custom {@link madkit.gui.DefaultAgentGUI} subclass to build
 * a tailored agent interface.
 * <p>
 * Instead of calling {@link #setupDefaultGUI()}, this agent instantiates
 * {@link CustomAgentGUI} directly. The custom GUI class overrides factory methods to
 * replace the center and left panels while keeping the default menu bar, toolbar, and
 * property sheet.
 *
 * @see CustomAgentGUI
 * @see madkit.gui.DefaultAgentGUI
 */
public class CustomGUIAgent extends Agent {

	/**
	 * Called when the agent is launched. Creates a {@link CustomAgentGUI} that provides
	 * custom center and left panels, then logs an activation message.
	 */
	@Override
	protected void onActivation() {
		new CustomAgentGUI(this);
		getLogger().info(() -> "Custom GUI agent activated with custom panels");
	}

	/**
	 * Launches a single instance of {@link CustomGUIAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
