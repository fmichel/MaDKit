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
package madkit.sample.logging;

import madkit.kernel.Agent;

/**
 * Demonstrates creating a log file for an agent using
 * {@link madkit.kernel.AgentLogger#createLogFile()}.
 * <p>
 * On activation, a log file is created in the default {@code Logs/} directory. All
 * subsequent log messages are written both to the console and to the file. The file name
 * is derived from the agent's name and a timestamp.
 * <p>
 * After running, check the {@code Logs/} directory to see the generated log file.
 *
 * @see madkit.kernel.AgentLogger#createLogFile()
 * @see madkit.kernel.AgentLogger#LOGS_DIRECTORY
 */
public class LogFileAgent extends Agent {

	/**
	 * Creates a log file and writes several log messages to it.
	 */
	@Override
	protected void onActivation() {
		getLogger().createLogFile();
		getLogger().info(() -> "Log file created — this message is written to both console and file");
		getLogger().info(() -> "All subsequent messages will also appear in the log file");
		getLogger().warning(() -> "Warnings are logged to the file too");
	}

	/**
	 * Launches a single instance of {@link LogFileAgent}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
