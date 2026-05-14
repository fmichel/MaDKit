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
package madkit.sample.organization;

import madkit.kernel.Agent;

/**
 * Orchestrates a full Community-Group-Role (CGR) demonstration by launching
 * several agents that interact through the organizational model.
 * <p>
 * The launch sequence is:
 * <ol>
 * <li>{@link GroupCreator} — creates the group and takes the {@code "manager"} role.</li>
 * <li>Two {@link RoleRequester} agents — request the {@code "participant"} role
 * (and also try a non-existent group).</li>
 * <li>{@link OrganizationExplorer} — queries the organization to discover the
 * manager and participants.</li>
 * </ol>
 * <p>
 * Because {@link Agent#launchAgent(Agent)} waits for each agent's
 * {@link Agent#onActivation()} to complete before returning, the organization
 * is built incrementally: each subsequently launched agent sees the roles
 * created by previously launched agents.
 * <p>
 * Run this class to see the complete CGR workflow in action.
 *
 * @see GroupCreator
 * @see RoleRequester
 * @see OrganizationExplorer
 */
public class GroupAndRoleDemo extends Agent {

	/**
	 * Launches the demonstration agents in sequence.
	 * <p>
	 * Each {@link #launchAgent(Agent)} call blocks until the launched agent's
	 * {@link #onActivation()} completes, ensuring a deterministic setup order.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "=== Starting CGR Organization Demo ===");

		// Step 1: Create the group and the manager role
		launchAgent(new GroupCreator());

		// Step 2: Two participants join the group
		launchAgent(new RoleRequester());
		launchAgent(new RoleRequester());

		// Step 3: An explorer queries the organization
		launchAgent(new OrganizationExplorer());

		getLogger().info(() -> "=== CGR Organization Demo Complete ===");
	}

	/**
	 * Launches a single instance of {@link GroupAndRoleDemo}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
