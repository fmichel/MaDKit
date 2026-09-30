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
 * Demonstrates request/reply messaging outcomes with MaDKit roles.
 * <p>
 * The requester runs three deterministic scenarios: a normal Ping/Pong
 * exchange, a send to a role with no recipients, and a send to a silent
 * recipient with a finite timeout. The latter two cases both return
 * {@code null}, but for different reasons: no recipient is found immediately
 * while a silent recipient consumes the configured wait bound.
 *
 * @see Agent#sendWaitReply(Message, String, String, String, Integer)
 * @see Agent#reply(Message, Message)
 * @see StringMessage
 */
public class RequestReplyTimeoutDemo extends Agent {

	private static final String COMMUNITY = "messaging-community";
	private static final String GROUP = "request-reply-group";
	private static final int REPLY_TIMEOUT_MILLISECONDS = 2_000;
	private static final int NO_RESPONSE_TIMEOUT_MILLISECONDS = 250;

	/**
	 * Creates the request/reply organization and launches responders for each
	 * outcome shown by this sample.
	 */
	@Override
	protected void onActivation() {
		createGroup(COMMUNITY, GROUP);
		requestRole(COMMUNITY, GROUP, "requester");
		launchAgent(new Replier());
		launchAgent(new SilentResponder());
	}

	/**
	 * Demonstrates a successful reply, an absent recipient, and a recipient that
	 * does not reply before the bounded timeout expires.
	 * <p>
	 * A missing role is detected immediately by {@code sendWaitReply}; it does
	 * not consume the timeout. The silent responder demonstrates why callers
	 * should always use a finite timeout when a reply is optional or a peer may
	 * stop unexpectedly.
	 */
	@Override
	protected void onLive() {
		StringMessage reply = sendWaitReply(new StringMessage("Ping!"), COMMUNITY, GROUP,
				"responder", REPLY_TIMEOUT_MILLISECONDS);
		reportOutcome("successful reply", reply == null ? "no reply" : reply.getContent());

		long missingStarted = System.nanoTime();
		StringMessage missingRecipient = sendWaitReply(new StringMessage("Nobody home"), COMMUNITY, GROUP,
				"missing-responder", NO_RESPONSE_TIMEOUT_MILLISECONDS);
		long missingElapsedMilliseconds = (System.nanoTime() - missingStarted) / 1_000_000;
		reportOutcome("missing recipient", missingRecipient == null
				? "no recipient (immediate, " + missingElapsedMilliseconds + " ms)"
				: missingRecipient.getContent());

		long started = System.nanoTime();
		StringMessage timedOut = sendWaitReply(new StringMessage("Are you there?"), COMMUNITY, GROUP,
				"silent-responder", NO_RESPONSE_TIMEOUT_MILLISECONDS);
		long elapsedMilliseconds = (System.nanoTime() - started) / 1_000_000;
		reportOutcome("bounded no-response", timedOut == null ? "no reply after " + elapsedMilliseconds + " ms" : timedOut.getContent());
	}

	protected void reportOutcome(String scenario, String result) {
		getLogger().info(() -> scenario + ": " + result);
	}

	/**
	 * Launches the request/reply sample.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}

	private static class Replier extends Agent {
		@Override
		protected void onActivation() {
			requestRole(COMMUNITY, GROUP, "responder");
		}

		@Override
		protected void onLive() {
			Message request = waitNextMessage(REPLY_TIMEOUT_MILLISECONDS);
			if (request != null) {
				reply(new StringMessage("Pong!"), request);
			}
		}
	}

	private static class SilentResponder extends Agent {
		@Override
		protected void onActivation() {
			requestRole(COMMUNITY, GROUP, "silent-responder");
		}

		@Override
		protected void onLive() {
			waitNextMessage(NO_RESPONSE_TIMEOUT_MILLISECONDS);
		}
	}
}
