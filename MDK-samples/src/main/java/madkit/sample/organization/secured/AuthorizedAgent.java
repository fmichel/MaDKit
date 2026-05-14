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
package madkit.sample.organization.secured;

import madkit.kernel.Agent;

/**
 * An agent that successfully joins a secured group by providing the correct password.
 * <p>
 * This agent uses {@link Agent#requestRole(String, String, String, Object)} with
 * the correct member card ({@code "secret-password"}) to join the
 * {@code "vip-group"} created by {@link SecuredGroupManager}.
 * The expected result is {@link ReturnCode#SUCCESS}.
 * <p>
 * Run this class after a {@link SecuredGroupManager} has been launched, or use
 * {@link SecuredGroupDemo} to see the full workflow.
 *
 * @see Agent#requestRole(String, String, String, Object)
 * @see SecuredGroupManager
 */
public class AuthorizedAgent extends Agent {

	/**
	 * Requests the {@code "member"} role in the secured group with the correct password.
	 * <p>
	 * The member card {@code "secret-password"} matches the {@link madkit.kernel.Gatekeeper}
	 * configured by {@link SecuredGroupManager}, so the request should succeed.
	 */
	@Override
	protected void onActivation() {
		ReturnCode rc = requestRole("secured-community", "vip-group", "member", "secret-password");
		getLogger().info(() -> "requestRole with correct password: " + rc);
	}

	/**
	 * Launches a single instance of {@link AuthorizedAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
