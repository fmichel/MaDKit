package madkit.marketorg;

import static madkit.marketorg.MarketOrganization.BROKER_ROLE;
import static madkit.marketorg.MarketOrganization.CLIENT_GROUP;
import static madkit.marketorg.MarketOrganization.CLIENT_ROLE;
import static madkit.marketorg.MarketOrganization.COMMUNITY;
import static madkit.marketorg.MarketOrganization.PROVIDER_ROLE;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.StringMessage;
import madkit.random.RandomizedInteger;

/**
 * The client agent is responsible for looking for a product in the market
 * organization.
 * 
 * @version 6.0
 */
public class Client extends Agent {

	private String product;

	/**
	 * The base pause used to wait when required. It is randomized when the agent is launched
	 * thanks to the {@link RandomizedInteger} annotation.
	 */
	@RandomizedInteger(min = 1000, max = 4000)
	private int basePause = 1500;

	/**
	 * On activation, the client creates the community and the group, requests the
	 * client role, and launches its GUI.
	 */
	@Override
	protected void onActivation() {
		createGroup(COMMUNITY, CLIENT_GROUP, true, null);
		requestRole(COMMUNITY, CLIENT_GROUP, CLIENT_ROLE, null);
		product = Provider.availableTransports.get(prng().nextInt(Provider.availableTransports.size()));
		new MarketAgentGUI(this, 0);
		getLogger().info(() -> "I will be looking for a " + product + " in " + basePause + " ms !");
		pause(basePause);
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
					pause(basePause);
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
		pause(prng().nextInt(1000, 2500));
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
			pause(prng().nextInt(1000, 3000));
			return true;
		}
		return false;
	}

	/**
	 * This creates the agents of the market organization.
	 * 
	 * @param args MaDKit arguments
	 */
	public static void main(String[] args) {
		executeThisAgent(
//				"--noLog",
//				"--kernelLogLevel", "ALL",
//				"--agentLogLevel", "ALL",
				"-la", Broker.class.getName() + ",2", "-la", Client.class.getName() + ",2", "-la",
				Provider.class.getName() + ",10"

		);
	}

	/**
	 * This java bean mutator is required for the {@link RandomizedInteger} to work.
	 * 
	 * @return the basePause
	 */
	public int getBasePause() {
		return basePause;
	}

	/**
	 * This java bean mutator is required for the {@link RandomizedInteger} to work.
	 * 
	 * @param basePause the basePause to set
	 */
	public void setBasePause(int basePause) {
		this.basePause = basePause;
	}
}
