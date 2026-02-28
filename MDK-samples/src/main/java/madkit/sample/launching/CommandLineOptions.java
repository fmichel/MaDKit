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
package madkit.sample.launching;

import madkit.kernel.Agent;
import madkit.kernel.Madkit;

/**
 * Demonstrates how to read MaDKit command-line configuration options from within an agent.
 * <p>
 * The {@link madkit.kernel.KernelConfig} object (accessible via {@link #getKernelConfig()})
 * holds all configuration properties for the current MaDKit session. Key options include:
 * <ul>
 * <li>{@code agentLogLevel} — the logging level for agents (e.g. {@code INFO}, {@code FINE})</li>
 * <li>{@code headless} — whether the platform runs without a GUI</li>
 * <li>{@code noLog} — whether logging is completely disabled</li>
 * <li>{@code desktop} — whether the MaDKit desktop GUI is displayed</li>
 * </ul>
 * <p>
 * Run with options like: {@code --agentLogLevel FINE --headless true}
 *
 * @see madkit.kernel.KernelConfig
 * @see Madkit
 */
public class CommandLineOptions extends Agent {

	/**
	 * Called when the agent is launched.
	 * Reads and logs several key configuration values from the {@link madkit.kernel.KernelConfig}.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "agentLogLevel = " + getKernelConfig().getLevel("agentLogLevel"));
		getLogger().info(() -> "headless      = " + getKernelConfig().getBoolean("headless"));
		getLogger().info(() -> "noLog         = " + getKernelConfig().getBoolean("noLog"));
		getLogger().info(() -> "desktop       = " + getKernelConfig().getBoolean("desktop"));
	}

	/**
	 * Launches a single instance of {@link CommandLineOptions}.
	 * <p>
	 * Pass MaDKit options on the command line, for example:
	 * <pre>
	 * java madkit.sample.launching.CommandLineOptions --agentLogLevel FINE --headless true
	 * </pre>
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
