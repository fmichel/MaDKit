package madkit.marketorg;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.messages.IntegerMessage;
import madkit.messages.Messages;
import madkit.messages.StringMessage;

import static madkit.marketorg.MarketOrganization.*;

/**
 * @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 6.1
 */
public class Broker extends Agent {

	static int nbOfBrokersOnScreen = 0;

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.ALL);
		createGroup(COMMUNITY, CLIENT_GROUP, true, null);
		createGroup(COMMUNITY, PROVIDERS_GROUP, true, null);
		requestRole(COMMUNITY, CLIENT_GROUP, BROKER_ROLE,
				null);
		requestRole(COMMUNITY, PROVIDERS_GROUP, BROKER_ROLE,
				null);
	}

	@Override
	protected void onLiving() {
		while (isAlive()) {
			Message m = getMailbox().purge();// to always treat the latest request
			if (m == null) {
				m = waitNextMessage();// waiting a request
			}
			if (m.getSenderRole().equals(CLIENT_ROLE)) {
				handleClientRequest((StringMessage) m);
			}
		}
	}

	private void handleClientRequest(StringMessage request) {
		if (!checkAgentAddress(request.getSender())) // Is the client still there ?
			return;
		final String content = request.getContent();
		getLogger().info(() -> "I received a request for a " + content + " \nfrom " + request.getSender());
		List<AgentAddress> providers = getAgentsWithRole(COMMUNITY, PROVIDERS_GROUP, content + "-" + PROVIDER_ROLE);
		if(providers.isEmpty()) {
			getLogger().info(() -> "No one is selling " +content+" !!\nPlease launch other providers !");
			return;
		}
		// get proposals from providers
//		List<Message> bids = getBids(request);
		List<IntegerMessage> bids = broadcastWithRoleWaitForReplies(
				new StringMessage("make-bid-please"), 
				providers, 
				BROKER_ROLE,
				3000);
		if (bids.isEmpty()) {
			getLogger().info(() -> "No bid from providers on time...");
		} else {
			getLogger().info(() -> "Received bids "+bids);
			IntegerMessage best = Messages.anyMessageWithMinContent(bids);
			makeTransactionHappenBetweenClientAndProvider(request, best);
		}
	}

	/**
	 * @param request
	 * @param best
	 */
	private void makeTransactionHappenBetweenClientAndProvider(Message request, Message best) {
		// creating a contract group
		String contractGroupId = Instant.now().toString();

		// sending the location to the provider
		Message ack = sendWithRoleWaitReply(
				new StringMessage(contractGroupId), // send group's info
				best.getSender(), // the address of the provider
				BROKER_ROLE, // I am a broker
				1000); // I cannot wait the end of the universe

		if (ack != null) {// The provider has entered the contract group
			getLogger().info(() -> "Provider is ready !\nSending the contract number to client");
			reply(new StringMessage(contractGroupId), request); // send group's info to the client
			pause((int) (Math.random() * 2000 + 1000));// let us celebrate and take vacation!!
		} else { // no answer from the provider...
			getLogger().info(() -> "Provider disappears !!!!");
		}
	}


//	@Override
//	protected void end() {
////		reload();
//		// launch another broker
////		AgentAction.RELOAD.getActionFor(this).actionPerformed(null);
//	}

}
