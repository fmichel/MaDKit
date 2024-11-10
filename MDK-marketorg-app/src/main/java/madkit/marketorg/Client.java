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

import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.messages.StringMessage;

/**
 * @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 5.1
 */
public class Client extends Agent {

	static int nbOfClientsOnScreen = 0;

	private final String product = Provider.availableTransports
			.get((int) (Math.random() * Provider.availableTransports.size()));

	@Override
	protected void onActivation() {
		createGroup(MarketOrganization.COMMUNITY, MarketOrganization.CLIENT_GROUP, true, null);
		requestRole(MarketOrganization.COMMUNITY, MarketOrganization.CLIENT_GROUP, MarketOrganization.CLIENT_ROLE,
				null);
		int pause = 1000 + (int) (Math.random() * 2000);
		getLogger().info(() -> "I will be looking for a " + product + " in " + pause + " ms !");
		pause(pause);
	}

	@Override
	protected void onLiving() {
		boolean haveTicket = false;
		while (!haveTicket) {
			StringMessage brokerAnswer = null;
			while (brokerAnswer == null) {
				brokerAnswer = sendWithRoleWaitReply(
						new StringMessage(product), 
						MarketOrganization.COMMUNITY,
						MarketOrganization.CLIENT_GROUP, 
						MarketOrganization.BROKER_ROLE, 
						MarketOrganization.CLIENT_ROLE,
						4000);
				getLogger().info(() -> "For now there is nothing for me :(");
				pause(500);
			}
			logFindBroker(brokerAnswer);// I found a broker and he has something for me
			haveTicket = buyTicket(brokerAnswer);
		}
	}

	@Override
	protected void onEnding() {
		getLogger().info(() -> "I will quit soon now, buit I will launch another one like me !");
		pause((int) (Math.random() * 2000 + 500));
		reload();
//		launchAgent(new Client(), true, 4);
	}

	private void logFindBroker(Message brokerAnswer) {
		getLogger().info(() -> "I found a broker : " + brokerAnswer.getSender());
		pause(1000);
	}

	private boolean buyTicket(StringMessage brokerAnswer) {
		String contractGroupID = brokerAnswer.getContent();
		requestRole(MarketOrganization.COMMUNITY, contractGroupID, MarketOrganization.CLIENT_ROLE);
		Message ticket = sendWaitReply(
				new StringMessage("money"),
				MarketOrganization.COMMUNITY, 
				contractGroupID,
				MarketOrganization.PROVIDER_ROLE,
				4000
				);
		if (ticket != null) {
			getLogger().info("Yeeeaah: I have my ticket :) ");
			leaveGroup(MarketOrganization.COMMUNITY, MarketOrganization.CLIENT_GROUP);
			pause((int) (1000 + Math.random() * 2000));
			return true;
		}
		return false;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nbOfClientsOnScreen = 0;
		Broker.nbOfBrokersOnScreen = 0;
//		executeThisAgent();
		new Madkit(
				"-la",Broker.class.getName() + ",true,1"
				,"-la",Client.class.getName() + ",true,1"
				,"-la",Provider.class.getName() + ",false,20"
				
				);
//		+ Client.class.getName()+ ",true,2;" + Provider.class.getName() + ",true,7");
	}
}
