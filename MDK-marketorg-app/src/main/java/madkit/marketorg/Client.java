/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
import static madkit.marketorg.MarketOrganization.CLIENT_GROUP;
import static madkit.marketorg.MarketOrganization.CLIENT_ROLE;
import static madkit.marketorg.MarketOrganization.COMMUNITY;
import static madkit.marketorg.MarketOrganization.PROVIDER_ROLE;
import static madkit.marketorg.MarketOrganization.RANDOM;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * The client agent is responsible for looking for a product in the market
 * organization.
 * 
 * @version 6.0
 */
public class Client extends Agent {

	private String product;

	/**
	 * On activation, the client creates the community and the group, requests the
	 * client role, and launches its GUI.
	 */
	@Override
	protected void onActivation() {
		createGroup(COMMUNITY, CLIENT_GROUP, true, null);
		requestRole(COMMUNITY, CLIENT_GROUP, CLIENT_ROLE, null);
		product = Provider.availableTransports.get(RANDOM.nextInt(Provider.availableTransports.size()));
		new MarketAgentGUI(this, 0);
		int pause = RANDOM.nextInt(1000, 2000);
		getLogger().info(() -> "I will be looking for a " + product + " in " + pause + " ms !");
		pause(pause);
	}

	/**
	 * On live, the client looks for a broker and buys a ticket.
	 */
	@Override
	protected void onLive() {
		boolean haveTicket = false;
		while (!haveTicket) {
			StringMessage brokerAnswer = null;
			while (brokerAnswer == null) {
				brokerAnswer = sendWithRoleWaitReply(new StringMessage(product), COMMUNITY, CLIENT_GROUP, BROKER_ROLE,
						CLIENT_ROLE, 4000);
				if (brokerAnswer == null) {
					getLogger().info(() -> "For now there is nothing for me :(");
					pause(1000);
				}
			}
			logFindBroker(brokerAnswer);// I found a broker and he has something for me
			haveTicket = buyTicket(brokerAnswer);
		}
	}

	/**
	 * On end, the client quits and launches another one.
	 */
	@Override
	protected void onEnd() {
		getLogger().info(() -> "I will quit soon now, buit I will launch another one like me !");
		pause(RANDOM.nextInt(1000, 2500));
		launchAgent(new Client(), 4);
	}

	private void logFindBroker(Message brokerAnswer) {
		getLogger().info(() -> "I found a broker : " + brokerAnswer.getSender());
		pause(1000);
	}

	/**
	 * Finalize the contract with the provider. Return true if the contract is
	 * finalized.
	 * 
	 * @param brokerAnswer the broker answer containing the contract group
	 * @return true if the contract is finalized
	 */
	private boolean buyTicket(StringMessage brokerAnswer) {
		String contractGroupID = brokerAnswer.getContent();
		requestRole(COMMUNITY, contractGroupID, CLIENT_ROLE);
		Message ticket = sendWaitReply(new StringMessage("money"), COMMUNITY, contractGroupID, PROVIDER_ROLE, 4000);
		if (ticket != null) {
			getLogger().info("Yeeeaah: I have my ticket :) ");
			leaveGroup(COMMUNITY, CLIENT_GROUP);
			pause(RANDOM.nextInt(1000, 3000));
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent(
//				"--noLog",
				"--kernelLogLevel", "ALL",
				"--agentLogLevel", "ALL",
				"-la", Broker.class.getName() + ",2", "-la", Client.class.getName() + ",2", "-la",
				Provider.class.getName() + ",10"

		);
	}
}
