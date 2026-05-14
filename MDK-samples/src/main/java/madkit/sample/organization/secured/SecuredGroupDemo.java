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
 * Orchestrates a full secured-group demonstration showing both successful
 * and denied access.
 * <p>
 * The launch sequence is:
 * <ol>
 * <li>{@link SecuredGroupManager} — creates the secured group with a
 * password-checking {@link madkit.kernel.Gatekeeper}.</li>
 * <li>{@link AuthorizedAgent} — joins successfully using the correct password.</li>
 * <li>{@link UnauthorizedAgent} — is denied access using an incorrect password.</li>
 * </ol>
 * <p>
 * Run this class to see the complete access-control workflow in action.
 *
 * @see SecuredGroupManager
 * @see AuthorizedAgent
 * @see UnauthorizedAgent
 */
public class SecuredGroupDemo extends Agent {

	/**
	 * Launches the demonstration agents in sequence.
	 * <p>
	 * Each {@link #launchAgent(Agent)} call blocks until the launched agent's
	 * {@link #onActivation()} completes, ensuring the secured group exists
	 * before other agents try to join it.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "=== Starting Secured Group Demo ===");

		// Step 1: Create the secured group
		launchAgent(new SecuredGroupManager());

		// Step 2: Authorized agent joins with correct password
		launchAgent(new AuthorizedAgent());

		// Step 3: Unauthorized agent is denied with wrong password
		launchAgent(new UnauthorizedAgent());

		getLogger().info(() -> "=== Secured Group Demo Complete ===");
	}

	/**
	 * Launches a single instance of {@link SecuredGroupDemo}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
