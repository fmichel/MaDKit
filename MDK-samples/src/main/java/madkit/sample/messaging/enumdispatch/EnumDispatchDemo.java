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

import static madkit.sample.messaging.enumdispatch.DispatchAgent.COMMUNITY;
import static madkit.sample.messaging.enumdispatch.DispatchAgent.DISPATCHER_ROLE;
import static madkit.sample.messaging.enumdispatch.DispatchAgent.GROUP;
import static madkit.sample.messaging.enumdispatch.DispatchAgent.SENDER_ROLE;

import madkit.kernel.Agent;
import madkit.messages.EnumMessage;

/**
 * Demonstrates MaDKit's enum-based message dispatch by launching a
 * {@link DispatchAgent} and sending it several
 * {@link EnumMessage EnumMessage&lt;ActionEnum&gt;} instances.
 * <p>
 * This threaded agent acts as the orchestrator for the sample:
 * <ol>
 * <li>During {@link #onActivation()}, it creates the organization and
 *     launches a {@link DispatchAgent}.</li>
 * <li>During {@link #onLive()}, it sends three enum messages — one for each
 *     {@link ActionEnum} constant — pausing briefly between sends so that the
 *     console output is easy to follow.</li>
 * </ol>
 * <p>
 * Each message is built with {@code new EnumMessage<>(ActionEnum.XXX, params...)}
 * and sent using {@link Agent#send(madkit.kernel.Message, String, String, String)}.
 * The receiver ({@link DispatchAgent}) dispatches each message to its matching
 * method via {@link Agent#proceedEnumMessage}.
 * <p>
 * <b>Expected console output</b> (abbreviated):
 * <pre>
 *   [DispatchAgent]  Dispatching: GREET
 *   [DispatchAgent]  Hello, MaDKit! Greetings from DispatchAgent.
 *   [DispatchAgent]  Dispatching: COMPUTE
 *   [DispatchAgent]  Computing 17 + 25 = 42
 *   [DispatchAgent]  Dispatching: REPORT
 *   [DispatchAgent]  Status report: 2 message(s) processed so far.
 * </pre>
 * <p>
 * Run this class directly to execute the full demonstration.
 *
 * @see ActionEnum
 * @see DispatchAgent
 * @see EnumMessage
 * @see Agent#proceedEnumMessage
 */
public class EnumDispatchDemo extends Agent {

	/**
	 * Sets up the organization and launches the {@link DispatchAgent}.
	 * <p>
	 * Creates the group {@value DispatchAgent#GROUP} in
	 * {@value DispatchAgent#COMMUNITY}, requests the
	 * {@value DispatchAgent#SENDER_ROLE} role, and launches a
	 * {@link DispatchAgent} that will wait for incoming messages.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "=== Starting EnumMessage Dispatch Demo ===");

		// Set up the organization — create group and request sender role
		createGroup(COMMUNITY, GROUP);
		requestRole(COMMUNITY, GROUP, SENDER_ROLE);

		// Launch the dispatch agent (its onActivation will request the dispatcher role)
		DispatchAgent dispatcher = new DispatchAgent();
		ReturnCode rc = launchAgent(dispatcher);
		getLogger().info(() -> "DispatchAgent launched: " + rc);
	}

	/**
	 * Sends three {@link EnumMessage} instances to the {@link DispatchAgent},
	 * one for each {@link ActionEnum} constant.
	 * <p>
	 * A short pause is inserted between each send so that the console output
	 * is easy to read. The messages are:
	 * <ol>
	 * <li>{@code GREET("MaDKit")} &mdash; triggers {@link DispatchAgent#greet(String)}</li>
	 * <li>{@code COMPUTE(17, 25)} &mdash; triggers {@link DispatchAgent#compute(int, int)}</li>
	 * <li>{@code REPORT()} &mdash; triggers {@link DispatchAgent#report()}</li>
	 * </ol>
	 */
	@Override
	protected void onLive() {
		// Brief pause to let the DispatchAgent settle into its waitNextMessage loop
		pause(500);

		// --- GREET message: pass a String parameter ---
		sendEnumMessage(ActionEnum.GREET, "MaDKit");

		pause(500);

		// --- COMPUTE message: pass two Integer parameters ---
		sendEnumMessage(ActionEnum.COMPUTE, 17, 25);

		pause(500);

		// --- REPORT message: no parameters ---
		sendEnumMessage(ActionEnum.REPORT);

		getLogger().info(() -> "=== All messages sent — demo complete ===");
	}

	/**
	 * Creates an {@link EnumMessage} for the given action and sends it to the
	 * agent holding the {@value DispatchAgent#DISPATCHER_ROLE} role.
	 * <p>
	 * The message is addressed by community, group, and role so that MaDKit
	 * selects the appropriate receiver automatically.
	 *
	 * @param action the {@link ActionEnum} constant identifying the action
	 * @param params optional parameters to include in the message
	 */
	private void sendEnumMessage(ActionEnum action, Object... params) {
		EnumMessage<ActionEnum> msg = new EnumMessage<>(action, params);
		ReturnCode rc = send(msg, COMMUNITY, GROUP, DISPATCHER_ROLE);
		getLogger().info(() -> "Sent " + action + " -> " + rc);
	}

	/**
	 * Launches a single instance of {@link EnumDispatchDemo}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
