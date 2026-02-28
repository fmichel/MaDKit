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
package madkit.sample.agent.management;

import madkit.kernel.Agent;

/**
 * Demonstrates launching and then killing another agent programmatically.
 * <p>
 * This agent creates a {@link WorkerAgent}, lets it run for 3 seconds, then kills it
 * using {@link #killAgent(Agent)}. It logs:
 * <ul>
 * <li>The launch result</li>
 * <li>The kill return code</li>
 * <li>The worker's {@link #isAlive()} state after being killed (should be {@code false})</li>
 * </ul>
 *
 * @see Agent#killAgent(Agent)
 * @see Agent#launchAgent(Agent)
 * @see WorkerAgent
 */
public class AgentKiller extends Agent {

	/**
	 * Launches a {@link WorkerAgent}, waits 3 seconds, kills it, and logs the results.
	 */
	@Override
	protected void onLive() {
		WorkerAgent worker = new WorkerAgent();
		ReturnCode launchRc = launchAgent(worker);
		getLogger().info(() -> "Worker launch result: " + launchRc);

		getLogger().info(() -> "Worker launched, waiting...");
		pause(3000);

		ReturnCode killRc = killAgent(worker);
		getLogger().info(() -> "Kill return code: " + killRc);
		getLogger().info(() -> "Worker isAlive: " + worker.isAlive());
	}

	/**
	 * Launches a single instance of {@link AgentKiller}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
