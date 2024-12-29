/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.marketorg;

import static madkit.marketorg.MarketOrganization.BROKER_ROLE;
import static madkit.marketorg.MarketOrganization.COMMUNITY;
import static madkit.marketorg.MarketOrganization.PROVIDERS_GROUP;
import static madkit.marketorg.MarketOrganization.PROVIDER_ROLE;
import static madkit.marketorg.MarketOrganization.RANDOM;

import java.util.Arrays;
import java.util.List;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.StringMessage;

/**
 * The provider agent is responsible for selling a product in the market
 * organization.
 * 
 * @version 6.0
 * 
 */
public class Provider extends Agent {

	public static List<String> availableTransports = Arrays.asList("train", "boat", "plane", "bus");

	private static int nbOfProvidersOnScreen = 0;

	private String competence;

	public Provider() {
		competence = getRandomTransport();
	}

	/**
	 * On activation, the provider creates the community and the group, requests the
	 * provider role, and launches its GUI
	 */
	public void onActivation() {
		createGroup(COMMUNITY, PROVIDERS_GROUP, true, null);
		requestRole(COMMUNITY, PROVIDERS_GROUP, competence + "-" + PROVIDER_ROLE, null);
		new MarketAgentGUI(this, 800);
		getLogger().info("Selling " + competence);
	}

	/**
	 * On live, the provider waits for the broker call for bid and replies with a
	 * bid. When the provider is selected, it finalizes the contract.
	 */
	@Override
	public void onLive() {
		while (isAlive()) {
			StringMessage m = waitNextMessage();
			if (m.getSenderRole().equals(BROKER_ROLE))
				handleBrokerMessage(m);
			else
				finalizeContract(m);
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
			reply(new IntegerMessage(RANDOM.nextInt(100, 800)), m);
		} else {
			iHaveBeenSelected(m);
		}
	}

	private void iHaveBeenSelected(StringMessage m) {
		getLogger().info("I have been selected :)");
		String contractGroup = m.getContent();
		createGroup(COMMUNITY, contractGroup, true);
		requestRole(COMMUNITY, contractGroup, PROVIDER_ROLE);
		reply(new Message(), m); // just an acknowledgment
	}

	private void finalizeContract(StringMessage m) {
		getLogger().info("I have sold something: That's great !");
		reply(new StringMessage("ticket"), m);
		pause(RANDOM.nextInt(1000, 3000));// let us celebrate !!
		leaveGroup(COMMUNITY, m.getSender().getGroup());
	}

	@Override
	public String getName() {
		return super.getName() + "-" + competence;
	}

	public static String getRandomTransport() {
		return availableTransports.get(RANDOM.nextInt(availableTransports.size()));
	}

	public static void main(String[] args) {
		executeThisAgent();
	}

}
