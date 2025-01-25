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
package madkit.marketorg;

import static madkit.marketorg.MarketOrganization.BROKER_ROLE;
import static madkit.marketorg.MarketOrganization.CLIENT_GROUP;
import static madkit.marketorg.MarketOrganization.CLIENT_ROLE;
import static madkit.marketorg.MarketOrganization.COMMUNITY;
import static madkit.marketorg.MarketOrganization.PROVIDERS_GROUP;
import static madkit.marketorg.MarketOrganization.PROVIDER_ROLE;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

import madkit.gui.UIProperty;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.Messages;
import madkit.messages.StringMessage;
import madkit.random.RandomizedBoolean;

/**
 * The broker agent is responsible for handling the client requests and managing the
 * transactions between the clients and the providers. It is the central agent in the
 * market organization.
 * 
 * @version 6.0
 */
public class Broker extends Agent {

	/**
	 * The broker's activity status. If the broker is on vacation, it will not handle the
	 * client requests. The {@link UIProperty} annotation is used to make this property
	 * visible in the GUI. The {@link RandomizedBoolean} annotation is used to make this
	 * property randomized when the agent is launched.
	 */
	@UIProperty
	@RandomizedBoolean
	private boolean onVacation = true;

	/**
	 * On activation, the broker creates the community and the groups, requests the broker
	 * role for the client and the provider groups, and launches its GUI.
	 */
	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.ALL);
		createGroup(COMMUNITY, CLIENT_GROUP);
		createGroup(COMMUNITY, PROVIDERS_GROUP);
		requestRole(COMMUNITY, CLIENT_GROUP, BROKER_ROLE);
		requestRole(COMMUNITY, PROVIDERS_GROUP, BROKER_ROLE);
		new MarketAgentGUI(this, 400);
	}

	/**
	 * On living, the broker waits for the client requests and handles them.
	 */
	@Override
	protected void onLive() {
		while (isAlive()) {
			if (isOnVacation()) {
				Message m = getMailbox().purge();// to always treat the latest request
				if (m == null) {
					m = waitNextMessage();// waiting a request
				}
				if (m.getSenderRole().equals(CLIENT_ROLE)) {
					handleClientRequest((StringMessage) m);
				}
			} else {
				getLogger().info(() -> "I am on vacation !");
				pause(2000);// Vacation
			}
		}
	}

	/**
	 * Handles the client request by broadcasting a request to the providers and then managing
	 * the transaction between the client and the provider.
	 * 
	 * @param request the client request
	 */
	private void handleClientRequest(StringMessage request) {
		if (!checkAgentAddress(request.getSender())) { // Is the client still there ?
			return;
		}
		final String content = request.getContent();
		getLogger().info(() -> "I received a request for a " + content + " \nfrom " + request.getSender());
		List<AgentAddress> providers = getAgentsWithRole(COMMUNITY, PROVIDERS_GROUP, content + "-" + PROVIDER_ROLE);
		if (providers.isEmpty()) {
			getLogger().info(() -> "No one is selling " + content + " !!\nPlease launch other providers !");
			return;
		}
		// get proposals from providers
		List<IntegerMessage> bids = broadcastWithRoleWaitForReplies(new StringMessage("make-bid-please"), providers,
				BROKER_ROLE, 3000);
		if (bids.isEmpty()) {
			getLogger().info(() -> "No bid from providers on time...");
		} else {
			getLogger().info(() -> "Received bids " + bids);
			IntegerMessage bestOffer = Messages.anyMessageWithMinContent(bids);
			makeTransactionHappenBetweenClientAndProvider(request, bestOffer);
		}
	}

	/**
	 * Make the transaction happen between the client and the provider
	 * 
	 * @param request   the client request
	 * @param bestOffer the best offer from the providers
	 */
	private void makeTransactionHappenBetweenClientAndProvider(Message request, Message bestOffer) {
		// creating a contract group
		String contractGroupId = Instant.now().toString();

		// sending the location to the provider
		Message ack = sendWithRoleWaitReply(new StringMessage(contractGroupId), // send group's info
				bestOffer.getSender(), // the address of the provider
				BROKER_ROLE, // I am a broker
				1000); // I cannot wait the end of the universe

		if (ack != null) {// The provider has entered the contract group
			getLogger().info(() -> "Provider is ready !\nSending the contract number to client");
			reply(new StringMessage(contractGroupId), request); // send group's info to the client
			pause(prng().nextInt(1000, 2000));// let us celebrate and take vacation!!
		} else { // no answer from the provider...
			getLogger().info(() -> "Provider disappears !!!!");
		}
	}

	/**
	 * The java bean mutator to the broker's activity status. It is required for the
	 * {@link UIProperty} annotation to work.
	 * 
	 * @return <code>true</code> if the broker is on vacation, <code>false</code> otherwise
	 */
	public boolean isOnVacation() {
		return onVacation;
	}

	/**
	 * The java bean mutator to the broker's activity status. It is required for the
	 * {@link UIProperty} annotation to work.
	 * 
	 * @param onVacation the onVacation to set
	 */
	public void setOnVacation(boolean onVacation) {
		this.onVacation = onVacation;
	}
}
