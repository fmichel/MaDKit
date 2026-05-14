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
package madkit.sample.launching;

import madkit.kernel.Agent;

/**
 * Demonstrates launching multiple instances of the same agent class.
 * <p>
 * Uses {@link Agent#executeThisAgent(int, String...)} to start 5 instances simultaneously.
 * Each instance logs its own unique name (class name + internal ID), illustrating that
 * every agent is an independent entity with its own identity.
 * <p>
 * Run this class to see 5 agents activate and log their names.
 *
 * @see Agent#executeThisAgent(int, String...)
 * @see Agent#getName()
 */
public class MultipleLaunching extends Agent {

	/**
	 * Called when each agent instance is launched.
	 * Logs the agent's unique name to demonstrate multi-instance identity.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Agent " + getName() + " is activated!");
	}

	/**
	 * Launches 5 instances of {@link MultipleLaunching}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(5, args);
	}
}
