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
 * Demonstrates how to create a group and request a role using the CGR
 * (Community-Group-Role) organizational model.
 * <p>
 * In MaDKit, the artificial society is structured around three concepts:
 * <ul>
 * <li><b>Community</b>: a top-level namespace that groups related groups together.</li>
 * <li><b>Group</b>: a set of agents that can interact. Created with
 * {@link #createGroup(String, String)}.</li>
 * <li><b>Role</b>: a function an agent plays within a group. Requested with
 * {@link #requestRole(String, String, String)}.</li>
 * </ul>
 * <p>
 * This agent creates a group called {@code "sample-group"} in the {@code "sample-community"}
 * community, then requests the {@code "manager"} role within that group.
 * <p>
 * Run this class directly to see the organization setup in the log output.
 *
 * @see Agent#createGroup(String, String)
 * @see Agent#requestRole(String, String, String)
 * @see Agent.ReturnCode
 */
public class GroupCreator extends Agent {

	/**
	 * Creates a group and requests a role during agent activation.
	 * <p>
	 * First, {@link #createGroup(String, String)} is called to create
	 * {@code "sample-group"} in {@code "sample-community"}. Then,
	 * {@link #requestRole(String, String, String)} is called to take the
	 * {@code "manager"} role. Both return codes are logged.
	 */
	@Override
	protected void onActivation() {
		ReturnCode groupResult = createGroup("sample-community", "sample-group");
		getLogger().info(() -> "createGroup result: " + groupResult);

		ReturnCode roleResult = requestRole("sample-community", "sample-group", "manager");
		getLogger().info(() -> "requestRole 'manager' result: " + roleResult);

		getLogger().info(() -> "Organization setup complete");
	}

	/**
	 * Launches a single instance of {@link GroupCreator}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
