/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * A threaded agent that waits for incoming messages.
 * <p>
 * This agent overrides {@link #onLive()}, which gives it its own thread.
 * During its live phase, it blocks on {@link #waitNextMessage(long)} with a
 * 5-second timeout. When a message arrives, it logs the sender and content;
 * if the timeout expires, it logs that no message was received.
 * <p>
 * The agent joins the {@code "messaging-community"} / {@code "chat-group"}
 * organization with the {@code "receiver"} role during activation.
 * <p>
 * Run this class directly to see the agent wait and eventually time out.
 * Use {@link MessagingLauncher} to see it receive a real message.
 *
 * @see Agent#waitNextMessage(long)
 * @see StringMessage
 * @see MessagingLauncher
 */
public class ReceiverAgent extends Agent {

	/**
	 * Joins the messaging organization during activation.
	 * <p>
	 * Creates the group {@code "chat-group"} in {@code "messaging-community"}
	 * (or gets {@link ReturnCode#ALREADY_GROUP} if it exists) and requests the
	 * {@code "receiver"} role.
	 */
	@Override
	protected void onActivation() {
		createGroup("messaging-community", "chat-group");
		ReturnCode rc = requestRole("messaging-community", "chat-group", "receiver");
		getLogger().info(() -> "Joined as 'receiver': " + rc);
	}

	/**
	 * Waits for an incoming message with a 5-second timeout.
	 * <p>
	 * If a message is received, its type and sender are logged. If the message
	 * is a {@link StringMessage}, the string content is also logged.
	 * If no message arrives within the timeout, a timeout notice is logged.
	 */
	@Override
	protected void onLive() {
		getLogger().info(() -> "Waiting for a message (5s timeout)...");
		Message msg = waitNextMessage(5000);
		if (msg != null) {
			getLogger().info(() -> "Received message from: " + msg.getSender());
			if (msg instanceof StringMessage sm) {
				getLogger().info(() -> "StringMessage content: " + sm.getContent());
			}
		} else {
			getLogger().info(() -> "No message received within timeout");
		}
	}

	/**
	 * Launches a single instance of {@link ReceiverAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
