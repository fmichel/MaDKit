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

import java.util.List;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * Demonstrates broadcasting a message to multiple agents at once.
 * <p>
 * This threaded agent creates a group, launches 3 {@link Listener} agents,
 * then uses {@link Agent#broadcast(Message, List)} to send a single
 * {@link StringMessage} to all listeners simultaneously.
 * <p>
 * The broadcast requires a list of {@link AgentAddress} obtained via
 * {@link Agent#getAgentsWithRole(String, String, String)}. Each listener
 * receives its own copy of the message.
 * <p>
 * Run this class to see the broadcaster send one message that is received
 * by all three listener agents.
 *
 * @see Agent#broadcast(Message, List)
 * @see Agent#getAgentsWithRole(String, String, String)
 * @see StringMessage
 */
public class BroadcastDemo extends Agent {

	/** The number of listener agents to launch. */
	private static final int LISTENER_COUNT = 3;

	/**
	 * Sets up the organization and launches the {@link Listener} agents.
	 * <p>
	 * Creates the group {@code "broadcast-group"} in
	 * {@code "messaging-community"} and requests the {@code "broadcaster"} role.
	 * Then launches {@value #LISTENER_COUNT} listener agents.
	 */
	@Override
	protected void onActivation() {
		createGroup("messaging-community", "broadcast-group");
		requestRole("messaging-community", "broadcast-group", "broadcaster");

		for (int i = 0; i < LISTENER_COUNT; i++) {
			launchAgent(new Listener());
		}
		getLogger().info(() -> LISTENER_COUNT + " listeners launched");
	}

	/**
	 * Broadcasts a message to all listener agents.
	 * <p>
	 * Pauses briefly to allow listeners to settle, then retrieves all agents
	 * with the {@code "listener"} role and broadcasts a {@link StringMessage}
	 * to them. After broadcasting, pauses again to let listeners process the
	 * message.
	 */
	@Override
	protected void onLive() {
		pause(500);

		List<AgentAddress> listeners = getAgentsWithRole("messaging-community", "broadcast-group", "listener");
		getLogger().info(() -> "Found " + listeners.size() + " listener(s)");

		ReturnCode rc = broadcast(new StringMessage("Broadcast message!"), listeners);
		getLogger().info(() -> "broadcast result: " + rc);

		pause(2000);
	}

	/**
	 * Launches a single instance of {@link BroadcastDemo}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}

	/**
	 * A simple threaded listener agent that waits for one broadcast message.
	 * <p>
	 * Requests the {@code "listener"} role in the broadcast group and then
	 * blocks in {@link #onLive()} waiting for a message with a 5-second timeout.
	 */
	private static class Listener extends Agent {

		/**
		 * Joins the broadcast group as a {@code "listener"}.
		 */
		@Override
		protected void onActivation() {
			requestRole("messaging-community", "broadcast-group", "listener");
			getLogger().info(() -> "Listener ready");
		}

		/**
		 * Waits for a broadcast message and logs it.
		 * <p>
		 * Uses {@link #waitNextMessage(long)} with a 5-second timeout.
		 * If a {@link StringMessage} is received, its content is logged.
		 */
		@Override
		protected void onLive() {
			Message msg = waitNextMessage(5000);
			if (msg instanceof StringMessage sm) {
				getLogger().info(() -> "Received broadcast: " + sm.getContent());
			} else if (msg != null) {
				getLogger().info(() -> "Received: " + msg);
			} else {
				getLogger().info(() -> "No broadcast received (timeout)");
			}
		}
	}
}
