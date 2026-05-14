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
package madkit.sample.messaging;

import madkit.kernel.Agent;
import madkit.messages.StringMessage;

/**
 * A non-threaded agent that sends messages to other agents by role.
 * <p>
 * This agent joins the {@code "messaging-community"} / {@code "chat-group"}
 * organization with the {@code "sender"} role during activation.
 * It exposes a public {@link #sendTo(String, String, String)} method that
 * creates a {@link StringMessage} and sends it to a target specified by
 * community, group, and role.
 * <p>
 * Because this agent is non-threaded (no {@code onLive()}), the sending must
 * be triggered externally, for example by the {@link MessagingLauncher}.
 * <p>
 * Run this class directly to see the agent activate (no messages will be sent
 * unless another agent calls {@link #sendTo}).
 *
 * @see Agent#send(madkit.kernel.Message, String, String, String)
 * @see StringMessage
 * @see MessagingLauncher
 */
public class SenderAgent extends Agent {

	/**
	 * Joins the messaging organization during activation.
	 * <p>
	 * Creates the group {@code "chat-group"} in {@code "messaging-community"}
	 * and requests the {@code "sender"} role.
	 */
	@Override
	protected void onActivation() {
		createGroup("messaging-community", "chat-group");
		ReturnCode rc = requestRole("messaging-community", "chat-group", "sender");
		getLogger().info(() -> "Joined as 'sender': " + rc);
	}

	/**
	 * Sends a {@link StringMessage} to the agent(s) holding the specified role.
	 * <p>
	 * The message is sent using {@link Agent#send(madkit.kernel.Message, String, String, String)},
	 * which selects a random agent with the target role if multiple agents qualify.
	 *
	 * @param community the target community
	 * @param group     the target group
	 * @param role      the target role
	 */
	public void sendTo(String community, String group, String role) {
		StringMessage msg = new StringMessage("Hello from SenderAgent!");
		ReturnCode rc = send(msg, community, group, role);
		getLogger().info(() -> "send to '" + role + "' result: " + rc);
	}

	/**
	 * Launches a single instance of {@link SenderAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
