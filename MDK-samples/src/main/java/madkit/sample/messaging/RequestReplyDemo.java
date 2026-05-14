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
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * Demonstrates a request-reply conversation between two agents.
 * <p>
 * This threaded agent acts as both the orchestrator and the requester.
 * During activation it sets up the organization and launches a {@link Replier}
 * helper agent. In its {@link #onLive()} phase, it sends a {@code "Ping!"}
 * message using {@link #sendWaitReply(Message, String, String, String, Integer)}
 * and logs the reply.
 * <p>
 * The {@link Replier} inner agent waits for a message and replies with
 * {@code "Pong!"} using {@link #reply(Message, Message)}.
 * <p>
 * This sample illustrates:
 * <ul>
 * <li>The {@code sendWaitReply} blocking send-and-wait pattern.</li>
 * <li>The {@code reply} method for responding to a specific message.</li>
 * <li>Using inner agent classes for self-contained demos.</li>
 * </ul>
 * <p>
 * Run this class directly to see the Ping/Pong exchange.
 *
 * @see Agent#sendWaitReply(Message, String, String, String, Integer)
 * @see Agent#reply(Message, Message)
 * @see StringMessage
 */
public class RequestReplyDemo extends Agent {

	/**
	 * Sets up the organization and launches the {@link Replier} agent.
	 * <p>
	 * Creates the group {@code "request-reply-group"} in
	 * {@code "messaging-community"}, requests the {@code "requester"} role,
	 * and launches a {@link Replier} that will handle incoming messages.
	 */
	@Override
	protected void onActivation() {
		createGroup("messaging-community", "request-reply-group");
		requestRole("messaging-community", "request-reply-group", "requester");
		getLogger().info(() -> "Requester ready — launching Replier...");
		launchAgent(new Replier());
	}

	/**
	 * Sends a {@code "Ping!"} message and waits up to 5 seconds for a reply.
	 * <p>
	 * Uses {@link #sendWaitReply(Message, String, String, String, Integer)} which
	 * combines sending and blocking wait into a single call. The reply is logged
	 * if received, otherwise a timeout notice is logged.
	 */
	@Override
	protected void onLive() {
		getLogger().info(() -> "Sending Ping!...");
		StringMessage response = sendWaitReply(
				new StringMessage("Ping!"),
				"messaging-community", "request-reply-group", "responder",
				5000);
		if (response != null) {
			getLogger().info(() -> "Received reply: " + response.getContent());
		} else {
			getLogger().info(() -> "No reply received (timeout)");
		}
	}

	/**
	 * Launches a single instance of {@link RequestReplyDemo}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}

	/**
	 * A helper agent that waits for one message and replies with {@code "Pong!"}.
	 * <p>
	 * This agent is threaded (overrides {@link #onLive()}) so that it can use the
	 * blocking {@link #waitNextMessage()} method. It requests the
	 * {@code "responder"} role in the same group as the requester.
	 */
	private static class Replier extends Agent {

		/**
		 * Joins the organization as a {@code "responder"}.
		 */
		@Override
		protected void onActivation() {
			requestRole("messaging-community", "request-reply-group", "responder");
			getLogger().info(() -> "Replier ready, waiting for messages...");
		}

		/**
		 * Waits for a message and replies with {@code "Pong!"}.
		 * <p>
		 * Uses the blocking {@link #waitNextMessage()} call, then
		 * {@link #reply(Message, Message)} to send the response back
		 * to the original sender.
		 */
		@Override
		protected void onLive() {
			Message received = waitNextMessage();
			getLogger().info(() -> "Received: " + received + " — replying with Pong!");
			reply(new StringMessage("Pong!"), received);
		}
	}
}
