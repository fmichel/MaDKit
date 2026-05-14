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
import madkit.kernel.Gatekeeper;

/**
 * Creates a secured group protected by a {@link Gatekeeper}.
 * <p>
 * This agent uses {@link Agent#createGroup(String, String, boolean, Gatekeeper)}
 * to create a group where role requests are verified against a password.
 * The {@link Gatekeeper} is implemented as a lambda that checks whether the
 * agent's member card equals {@code "secret-password"}.
 * <p>
 * Only agents providing the correct password via
 * {@link Agent#requestRole(String, String, String, Object)} will be admitted;
 * all others receive {@link ReturnCode#ACCESS_DENIED}.
 * <p>
 * Run this class to see the secured group creation. Use {@link SecuredGroupDemo}
 * to see the full access-control workflow.
 *
 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
 * @see Gatekeeper
 * @see Agent.ReturnCode#ACCESS_DENIED
 */
public class SecuredGroupManager extends Agent {

	/**
	 * Creates a secured group with a password-based {@link Gatekeeper}.
	 * <p>
	 * The group {@code "vip-group"} is created in {@code "secured-community"}
	 * with a gatekeeper that only admits agents whose member card equals
	 * {@code "secret-password"}. The agent then takes the {@code "manager"} role,
	 * passing the correct password since even the group creator must satisfy
	 * the gatekeeper when requesting a role.
	 */
	@Override
	protected void onActivation() {
		ReturnCode groupRc = createGroup("secured-community", "vip-group", false,
				(agentNetworkID, roleName, memberCard) -> "secret-password".equals(memberCard));
		getLogger().info(() -> "createGroup 'vip-group' (secured): " + groupRc);

		ReturnCode roleRc = requestRole("secured-community", "vip-group", "manager", "secret-password");
		getLogger().info(() -> "requestRole 'manager': " + roleRc);

		getLogger().info(() -> "Secured group is ready — waiting for agents to join");
	}

	/**
	 * Launches a single instance of {@link SecuredGroupManager}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
