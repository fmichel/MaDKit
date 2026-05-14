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
 * Demonstrates the {@code --noLog} optimization flag.
 * <p>
 * When MaDKit is started with {@code --noLog}, a no-op logger is assigned to every agent.
 * Calls to {@code getLogger().info(...)}, etc. become essentially free — no message
 * formatting, no handler invocation, and no memory footprint for log records. This is
 * crucial when running large-scale simulations with thousands of agents where logging
 * overhead would be unacceptable.
 * <p>
 * The message logged in {@link #onActivation()} will <strong>not</strong> appear in the
 * console because the {@code main} method passes {@code --noLog}.
 *
 * @see madkit.kernel.AgentLogger
 */
public class NoLogAgent extends Agent {

	/**
	 * Logs a message that will not be visible because logging is disabled via
	 * {@code --noLog}.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "This message will NOT be seen because --noLog is active");
	}

	/**
	 * Launches a single instance of {@link NoLogAgent} with the {@code --noLog} flag,
	 * completely disabling logging for maximum performance.
	 *
	 * @param args MaDKit command-line options (ignored — {@code --noLog} is always set)
	 */
	public static void main(String[] args) {
		executeThisAgent("--noLog");
	}
}
