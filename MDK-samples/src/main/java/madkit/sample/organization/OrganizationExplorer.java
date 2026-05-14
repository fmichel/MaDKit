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

import java.util.List;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;

/**
 * Demonstrates how to query the organizational structure at runtime.
 * <p>
 * After creating a group and requesting a role, this agent uses the organization
 * query methods to discover other agents:
 * <ul>
 * <li>{@link #getAgentWithRole(String, String, String)} — returns a single
 * {@link AgentAddress} of a random agent with the specified role (excluding the
 * caller), or {@code null} if none is found.</li>
 * <li>{@link #getAgentsWithRole(String, String, String)} — returns a list of
 * {@link AgentAddress} for all agents with the specified role (excluding the caller).
 * The list is empty if no other agent holds the role.</li>
 * </ul>
 * <p>
 * When run alone, both queries return {@code null} / empty because the caller is
 * the only agent in the group. When combined with other agents via
 * {@link GroupAndRoleDemo}, the queries produce non-empty results.
 * <p>
 * Run this class directly to see the query results in the log output.
 *
 * @see Agent#getAgentWithRole(String, String, String)
 * @see Agent#getAgentsWithRole(String, String, String)
 * @see AgentAddress
 */
public class OrganizationExplorer extends Agent {

	/**
	 * Creates a group, requests a role, and queries the organization.
	 * <p>
	 * The agent creates {@code "sample-group"} in {@code "sample-community"} (or
	 * gets {@link ReturnCode#ALREADY_GROUP} if it already exists), requests the
	 * {@code "explorer"} role, and then uses the query API to look for other agents
	 * holding the {@code "manager"} role.
	 */
	@Override
	protected void onActivation() {
		ReturnCode groupRc = createGroup("sample-community", "sample-group");
		getLogger().info(() -> "createGroup result: " + groupRc);

		ReturnCode roleRc = requestRole("sample-community", "sample-group", "explorer");
		getLogger().info(() -> "requestRole 'explorer' result: " + roleRc);

		// Query for a single agent with the "manager" role
		AgentAddress manager = getAgentWithRole("sample-community", "sample-group", "manager");
		getLogger().info(() -> "getAgentWithRole 'manager': " + manager);

		// Query for all agents with the "participant" role
		List<AgentAddress> participants = getAgentsWithRole("sample-community", "sample-group", "participant");
		getLogger().info(() -> "getAgentsWithRole 'participant': " + participants.size() + " found -> " + participants);
	}

	/**
	 * Launches a single instance of {@link OrganizationExplorer}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
