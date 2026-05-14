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
package madkit.sample.messaging.enumdispatch;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.EnumMessage;

/**
 * A threaded agent that receives {@link EnumMessage EnumMessage&lt;ActionEnum&gt;}
 * and dispatches them to matching methods using
 * {@link Agent#proceedEnumMessage}.
 * <p>
 * When a message carrying an {@link ActionEnum} constant arrives, the MaDKit
 * reflection mechanism converts the enum name to a method name (e.g.
 * {@code GREET} &rarr; {@code greet}) and invokes that method with the
 * message's parameters. This agent exposes three such methods:
 * <ul>
 * <li>{@link #greet(String)} &mdash; logs a personalised greeting.</li>
 * <li>{@link #compute(int, int)} &mdash; adds two integers and logs the
 *     result.</li>
 * <li>{@link #report()} &mdash; logs the total number of messages
 *     processed.</li>
 * </ul>
 * <p>
 * The agent joins the {@code "enum-community"} / {@code "dispatch-group"}
 * organization with the {@code "dispatcher"} role so that it can be targeted
 * by the {@link EnumDispatchDemo} launcher.
 * <p>
 * Run this class directly to see the agent start and wait for messages
 * (it will eventually time out). Use {@link EnumDispatchDemo} for a
 * complete demonstration.
 *
 * @see ActionEnum
 * @see EnumDispatchDemo
 * @see Agent#proceedEnumMessage
 * @see EnumMessage
 */
public class DispatchAgent extends Agent {

	/** The community used by this sample. */
	static final String COMMUNITY = "enum-community";

	/** The group used by this sample. */
	static final String GROUP = "dispatch-group";

	/** The role that identifies the dispatching agent. */
	static final String DISPATCHER_ROLE = "dispatcher";

	/** The role that identifies the sender agent. */
	static final String SENDER_ROLE = "sender";

	/** Counter tracking the total number of dispatched messages. */
	private int processedCount;

	/**
	 * Joins the dispatch organization during activation.
	 * <p>
	 * Creates the group {@value #GROUP} in {@value #COMMUNITY} and requests
	 * the {@value #DISPATCHER_ROLE} role. The group is created idempotently
	 * so that either the dispatcher or the sender can start first.
	 */
	@Override
	protected void onActivation() {
		processedCount = 0;
		createGroup(COMMUNITY, GROUP);
		ReturnCode rc = requestRole(COMMUNITY, GROUP, DISPATCHER_ROLE);
		getLogger().info(() -> "Joined as '" + DISPATCHER_ROLE + "': " + rc);
	}

	/**
	 * Message-processing loop that waits for incoming messages and dispatches
	 * them.
	 * <p>
	 * The agent blocks on {@link #waitNextMessage(long)} with a 10-second
	 * timeout. When an {@link EnumMessage} arrives it is handed to
	 * {@link #proceedEnumMessage}, which reflectively invokes the method
	 * matching the message's {@link ActionEnum} code. Other message types
	 * are logged and ignored.
	 * <p>
	 * The loop terminates when no message is received within the timeout
	 * period, at which point the agent logs a summary and exits its live
	 * phase.
	 */
	@Override
	protected void onLive() {
		getLogger().info(() -> "Waiting for EnumMessages...");
		while (true) {
			// Block until a message arrives or the timeout expires
			Message msg = waitNextMessage(10_000);
			if (msg == null) {
				getLogger().info(() -> "No more messages — exiting live phase (processed "
						+ processedCount + " message(s))");
				break;
			}
			if (msg instanceof EnumMessage<?> enumMsg) {
				// Delegate to proceedEnumMessage which reflectively calls the matching method
				@SuppressWarnings("unchecked")
				EnumMessage<ActionEnum> actionMsg = (EnumMessage<ActionEnum>) enumMsg;
				getLogger().info(() -> "Dispatching: " + actionMsg.getCode());
				proceedEnumMessage(actionMsg);
				processedCount++;
			} else {
				getLogger().warning(() -> "Unexpected message type: " + msg.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Handles the {@link ActionEnum#GREET} action.
	 * <p>
	 * Called reflectively by {@link #proceedEnumMessage} when the received
	 * message carries {@code ActionEnum.GREET}. Logs a personalised greeting
	 * using the provided name.
	 *
	 * @param name the name to greet
	 */
	public void greet(String name) {
		getLogger().info(() -> "Hello, " + name + "! Greetings from DispatchAgent.");
	}

	/**
	 * Handles the {@link ActionEnum#COMPUTE} action.
	 * <p>
	 * Called reflectively by {@link #proceedEnumMessage} when the received
	 * message carries {@code ActionEnum.COMPUTE}. Adds the two operands and
	 * logs the result.
	 *
	 * @param a the first operand
	 * @param b the second operand
	 */
	public void compute(int a, int b) {
		int result = a + b;
		getLogger().info(() -> "Computing " + a + " + " + b + " = " + result);
	}

	/**
	 * Handles the {@link ActionEnum#REPORT} action.
	 * <p>
	 * Called reflectively by {@link #proceedEnumMessage} when the received
	 * message carries {@code ActionEnum.REPORT}. Logs the current count of
	 * processed messages.
	 */
	public void report() {
		getLogger().info(() -> "Status report: " + processedCount + " message(s) processed so far.");
	}

	/**
	 * Launches a single instance of {@link DispatchAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
