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
import static madkit.marketorg.MarketOrganization.COMMUNITY;
import static madkit.marketorg.MarketOrganization.PROVIDERS_GROUP;
import static madkit.marketorg.MarketOrganization.PROVIDER_ROLE;

import java.util.Arrays;
import java.util.List;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.StringMessage;

/**
 * The provider agent is responsible for selling a product in the market organization.
 * 
 * @version 6.0
 * 
 */
public class Provider extends Agent {

	/**
	 * The available transports that will be available for sale in the market
	 */
	public static final List<String> availableTransports = Arrays.asList("train", "boat", "plane", "bus");

	/**
	 * The competence of the provider (the transport it sells)
	 */
	private String competence;

	/**
	 * On activation, the provider creates the community and the group, requests the provider
	 * role, and launches its GUI
	 */
	@Override
	protected void onActivation() {
		competence = availableTransports.get(prng().nextInt(availableTransports.size()));
		createGroup(COMMUNITY, PROVIDERS_GROUP);
		requestRole(COMMUNITY, PROVIDERS_GROUP, competence + "-" + PROVIDER_ROLE);
		new MarketAgentGUI(this, 800);
		getLogger().info("Selling " + competence);
	}

	/**
	 * On live, the provider waits for the broker call for bid and replies with a bid. When
	 * the provider is selected, it finalizes the contract.
	 */
	@Override
	protected void onLive() {
		while (isAlive()) {
			StringMessage m = waitNextMessage();
			if (m.getSenderRole().equals(BROKER_ROLE)) {
				handleBrokerMessage(m);
			} else {
				finalizeContract(m);
			}
		}
	}

	/**
	 * Handles the broker message.
	 * 
	 * @param m the message
	 */
	private void handleBrokerMessage(StringMessage m) {
		if (m.getContent().equals("make-bid-please")) {
			getLogger().info(() -> "I received a call for bid from " + m.getSender());
			reply(new IntegerMessage(prng().nextInt(100, 800)), m);
		} else {
			iHaveBeenSelected(m);
		}
	}

	private void iHaveBeenSelected(StringMessage m) {
		getLogger().info("I have been selected :)");
		String contractGroup = m.getContent();
		createGroup(COMMUNITY, contractGroup);
		requestRole(COMMUNITY, contractGroup, PROVIDER_ROLE);
		reply(new Message(), m); // just an acknowledgment
	}

	private void finalizeContract(StringMessage m) {
		getLogger().info("I have sold something: That's great !");
		reply(new StringMessage("ticket"), m);
		pause(prng().nextInt(1000, 3000));// let us celebrate !!
		leaveGroup(COMMUNITY, m.getSender().getGroup());
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return super.getName() + "-" + competence;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		executeThisAgent();
	}

}
