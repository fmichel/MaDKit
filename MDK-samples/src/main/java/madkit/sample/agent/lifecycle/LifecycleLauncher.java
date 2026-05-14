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
package madkit.sample.agent.lifecycle;

import madkit.kernel.Agent;

/**
 * Demonstrates launching another agent and observing its lifecycle state.
 * <p>
 * This agent is itself threaded (overrides {@link #onLive()}) so it can use
 * {@link #pause(int)} to wait while the launched {@link LifecycleAgent} runs.
 * It checks {@link Agent#isAlive()} at different points to show how the alive
 * state changes through the lifecycle:
 * <ul>
 * <li>Before launch: {@code isAlive() == false}</li>
 * <li>After launch (activation complete): {@code isAlive() == true}</li>
 * <li>After {@code onLive()} finishes and {@code onEnd()} completes: {@code isAlive() == false}</li>
 * </ul>
 *
 * @see Agent#launchAgent(Agent)
 * @see Agent#isAlive()
 */
public class LifecycleLauncher extends Agent {

	/**
	 * Called when the launcher agent is activated.
	 * Logs that the launcher is ready.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "Launcher activated");
	}

	/**
	 * Launches a {@link LifecycleAgent}, observes its lifecycle state at different
	 * points, and logs the results.
	 */
	@Override
	protected void onLive() {
		LifecycleAgent agent = new LifecycleAgent();

		getLogger().info(() -> "Before launch: isAlive = " + agent.isAlive());

		ReturnCode rc = launchAgent(agent);
		getLogger().info(() -> "Launch return code: " + rc);
		getLogger().info(() -> "After launch: isAlive = " + agent.isAlive());

		// Wait long enough for the LifecycleAgent to complete its 3 iterations (3 × 500 ms)
		pause(3000);

		getLogger().info(() -> "After live completes: isAlive = " + agent.isAlive());
	}

	/**
	 * Launches a single instance of {@link LifecycleLauncher}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
