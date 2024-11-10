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

import java.util.Arrays;
import java.util.List;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.StringMessage;

/**
 * @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 6.0
 */
public class Provider extends Agent {

	public static List<String> availableTransports = Arrays.asList("train", "boat", "plane", "bus");

//    static {
//	for (String competence : availableTransports) {
//	    icons.put(competence, new ImageIcon(Provider.class.getResource("images/" + competence + ".png")));
//	}
//    }

	private static int nbOfProvidersOnScreen = 0;

	private String competence;

	public Provider() {
		competence = Provider.availableTransports.get((int) (Math.random() * Provider.availableTransports.size()));
	}

	public void onActivation() {
		createGroup(MarketOrganization.COMMUNITY, MarketOrganization.PROVIDERS_GROUP, true, null);
		requestRole(MarketOrganization.COMMUNITY, MarketOrganization.PROVIDERS_GROUP,
				competence + "-" + MarketOrganization.PROVIDER_ROLE, null);
		getLogger().info("Selling "+competence);
	}

	@Override
	public void onLiving() {
		while (isAlive()) {
			StringMessage m = waitNextMessage();
			if (m.getSenderRole().equals(MarketOrganization.BROKER_ROLE))
				handleBrokerMessage(m);
			else
				finalizeContract(m);
		}
	}

	private void handleBrokerMessage(StringMessage m) {
		if (m.getContent().equals("make-bid-please")) {
			getLogger().info(() -> "I received a call for bid from " + m.getSender());
			reply(new IntegerMessage((int) (Math.random() * 500)), m);
		} else {
			iHaveBeenSelected(m);
		}
	}

	private void iHaveBeenSelected(StringMessage m) {
		getLogger().info("I have been selected :)");
		String contractGroup = m.getContent();
		createGroup(MarketOrganization.COMMUNITY, contractGroup, true);
		requestRole(MarketOrganization.COMMUNITY, contractGroup, MarketOrganization.PROVIDER_ROLE);
		reply(new Message(), m); // just an acknowledgment
	}

	private void finalizeContract(StringMessage m) {
		getLogger().info("I have sold something: That's great !");
		reply(new StringMessage("ticket"), m);
		pause((int) (Math.random() * 2000 + 1000));// let us celebrate !!
		leaveGroup(MarketOrganization.COMMUNITY, m.getSender().getGroup());
	}
	
	public static void main(String[] args) {
		executeThisAgent();
	}

}
