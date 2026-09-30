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
 * encouraged to load and test the software's suitability as regards its
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package madkit.sample.messaging;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * Demonstrates the basic request/reply exchange without failure or timeout
 * scenarios. The requester creates the organization, launches a responder,
 * and waits for the expected reply.
 *
 * @see Agent#sendWaitReply(Message, String, String, String, Integer)
 * @see Agent#reply(Message, Message)
 */
public class RequestReplyDemo extends Agent {

    private static final String COMMUNITY = "messaging-community";
    private static final String GROUP = "request-reply-group";

    /** Creates the organization and launches the responder. */
    @Override
    protected void onActivation() {
        createGroup(COMMUNITY, GROUP);
        requestRole(COMMUNITY, GROUP, "requester");
        launchAgent(new Replier());
    }

    /** Sends one request and reports its reply. */
    @Override
    protected void onLive() {
        StringMessage reply = sendWaitReply(new StringMessage("Ping!"), COMMUNITY, GROUP,
                "responder", null);
        reportOutcome(reply == null ? "no reply" : reply.getContent());
    }

    protected void reportOutcome(String result) {
        getLogger().info(() -> "request/reply: " + result);
    }

    /** Launches this sample. */
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
            Message request = waitNextMessage();
            if (request != null) {
                reply(new StringMessage("Pong!"), request);
            }
        }
    }
}
