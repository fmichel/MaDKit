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
package madkit.sample.logging;

import java.util.logging.Level;

import madkit.kernel.Agent;

/**
 * Demonstrates logging at every standard {@link Level} and changing the log level at
 * runtime.
 * <p>
 * On activation the agent first sets its level to {@link Level#ALL} and logs a message at
 * each of the six standard levels: SEVERE, WARNING, INFO, FINE, FINER, and FINEST. It
 * then switches to {@link Level#FINE} and logs again at every level — only messages at
 * FINE or above will be visible.
 * <p>
 * Run this class to observe how log-level filtering works in practice.
 *
 * @see java.util.logging.Level
 * @see madkit.kernel.AgentLogger
 */
public class VerboseAgent extends Agent {

	/**
	 * Logs at all standard levels with level ALL, then switches to FINE and logs again.
	 */
	@Override
	protected void onActivation() {
		logAtAllLevels("Level ALL");

		getLogger().setLevel(Level.FINE);
		getLogger().info(() -> "--- Switching log level to FINE ---");

		logAtAllLevels("Level FINE");
	}

	/**
	 * Logs a message at each of the six standard log levels.
	 *
	 * @param prefix a label prepended to each message for identification
	 */
	private void logAtAllLevels(String prefix) {
		getLogger().severe(() -> prefix + " | SEVERE message");
		getLogger().warning(() -> prefix + " | WARNING message");
		getLogger().info(() -> prefix + " | INFO message");
		getLogger().fine(() -> prefix + " | FINE message");
		getLogger().finer(() -> prefix + " | FINER message");
		getLogger().finest(() -> prefix + " | FINEST message");
	}

	/**
	 * Launches a single instance of {@link VerboseAgent} with log level set to ALL so that
	 * the first round of messages is fully visible.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent("--agentLogLevel", "ALL");
	}
}
