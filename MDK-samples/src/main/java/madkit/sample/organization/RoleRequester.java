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
 * Demonstrates the various return codes of {@link Agent#requestRole(String, String, String)}.
 * <p>
 * This agent attempts to request roles in both existing and non-existing groups
 * to illustrate the different outcomes:
 * <ul>
 * <li>{@link ReturnCode#SUCCESS} — the role was successfully granted.</li>
 * <li>{@link ReturnCode#NOT_GROUP} — the targeted group does not exist.</li>
 * <li>{@link ReturnCode#NOT_COMMUNITY} — the targeted community does not exist.</li>
 * </ul>
 * <p>
 * When used standalone, the agent will get {@code NOT_COMMUNITY} because no group
 * has been created yet. When launched after a {@link GroupCreator}, the first request
 * succeeds and only the non-existent group request fails.
 * <p>
 * Run this class directly to see the return codes in the log output.
 *
 * @see Agent#requestRole(String, String, String)
 * @see Agent.ReturnCode
 */
public class RoleRequester extends Agent {

	/**
	 * Requests roles in existing and non-existing groups to demonstrate return codes.
	 * <p>
	 * The first call targets {@code "sample-group"} in {@code "sample-community"},
	 * which may or may not exist depending on whether a {@link GroupCreator} has been
	 * launched beforehand. The second call deliberately targets a non-existent group
	 * to produce a {@link ReturnCode#NOT_GROUP} result.
	 */
	@Override
	protected void onActivation() {
		ReturnCode rc = requestRole("sample-community", "sample-group", "participant");
		getLogger().info(() -> "requestRole 'participant' in 'sample-group': " + rc);

		ReturnCode rc2 = requestRole("sample-community", "nonexistent-group", "participant");
		getLogger().info(() -> "requestRole 'participant' in 'nonexistent-group': " + rc2 + " (expected NOT_GROUP)");
	}

	/**
	 * Launches a single instance of {@link RoleRequester}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
