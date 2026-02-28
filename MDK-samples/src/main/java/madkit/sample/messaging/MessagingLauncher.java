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

/**
 * Orchestrates a simple messaging demonstration between a sender and a receiver.
 * <p>
 * This non-threaded launcher agent:
 * <ol>
 * <li>Launches a {@link ReceiverAgent} (threaded, will block waiting for messages).</li>
 * <li>Launches a {@link SenderAgent} (non-threaded, ready after activation).</li>
 * <li>Calls {@link SenderAgent#sendTo(String, String, String)} to send a message
 * to the receiver.</li>
 * </ol>
 * <p>
 * Because {@link Agent#launchAgent(Agent)} blocks until the launched agent's
 * {@link Agent#onActivation()} completes, the receiver is already waiting for
 * messages by the time the sender sends. The receiver's threaded
 * {@link ReceiverAgent#onLive()} runs concurrently on its own thread.
 * <p>
 * Run this class to see a complete send/receive exchange.
 *
 * @see SenderAgent
 * @see ReceiverAgent
 */
public class MessagingLauncher extends Agent {

	/**
	 * Launches the receiver and sender agents, then triggers the send.
	 * <p>
	 * The receiver is launched first so it is already blocking on
	 * {@link Agent#waitNextMessage(long)} when the sender sends.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "=== Starting Messaging Demo ===");

		ReceiverAgent receiver = new ReceiverAgent();
		launchAgent(receiver);

		SenderAgent sender = new SenderAgent();
		launchAgent(sender);

		sender.sendTo("messaging-community", "chat-group", "receiver");

		getLogger().info(() -> "=== Messaging Demo Complete ===");
	}

	/**
	 * Launches a single instance of {@link MessagingLauncher}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
