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
package madkit.sample.agent.management;

import java.util.ArrayList;
import java.util.List;

import madkit.kernel.Agent;

/**
 * Demonstrates launching multiple agents programmatically from another agent.
 * <p>
 * This agent launches 3 {@link WorkerAgent} instances during its {@link #onLive()} phase,
 * logs the {@link ReturnCode} of each launch, and then pauses to let them run.
 * <p>
 * This pattern is common in MAS: a manager or supervisor agent creates and coordinates
 * worker agents at runtime.
 *
 * @see Agent#launchAgent(Agent)
 * @see WorkerAgent
 */
public class AgentLauncher extends Agent {

	/** The number of worker agents to launch. */
	private static final int WORKER_COUNT = 3;

	// Store references to launched workers
	private final List<WorkerAgent> workers = new ArrayList<>();

	/**
	 * Launches {@value #WORKER_COUNT} {@link WorkerAgent} instances, logs each return code,
	 * and pauses 3 seconds to let them work.
	 */
	@Override
	protected void onLive() {
		for (int i = 1; i <= WORKER_COUNT; i++) {
			final int index = i;
			WorkerAgent worker = new WorkerAgent();
			ReturnCode rc = launchAgent(worker);
			if (rc == ReturnCode.SUCCESS) {
				workers.add(worker);
			}
			getLogger().info(() -> "Worker " + index + " launch result: " + rc);
		}
		getLogger().info(() -> WORKER_COUNT + " workers launched successfully");
		pause(3000);
	}

	/**
	 * Kills all launched worker agents when this launcher agent ends.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "Killing all worker agents...");
		for (WorkerAgent worker : workers) {
			killAgent(worker);
		}
		getLogger().info(() -> "All worker agents killed.");
	}

	/**
	 * Launches a single instance of {@link AgentLauncher}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}