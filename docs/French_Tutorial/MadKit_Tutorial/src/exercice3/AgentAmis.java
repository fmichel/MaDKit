package exercice3;

import java.util.ArrayList;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.messages.StringMessage;

@SuppressWarnings("serial")
public class AgentAmis extends Agent {
	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle

	private ArrayList<AgentAddress> listeAmis;

	@Override
	public void activate() {
		this.listeAmis = new ArrayList<AgentAddress>();
		createGroupIfAbsent("amis", "piece");
		requestRole("amis", "piece", "personne", null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void live() {
		pause(5000);
		while (true) {
			StringMessage m = (StringMessage) waitNextMessage(1000);
			if (m != null) {
				if (m.getContent().equals("ami?")){
					this.listeAmis.add(m.getSender());
					if (logger != null)
						logger.info("Je suis amis avec " + m.getSender());
				}
				if (m.getContent().equals("partir?")) {
					sendMessage(m.getSender(), new StringMessage(
							"on y va"));
					if (logger != null)
						logger.info("Je part avec " + m.getSender());
					killAgent(this);
				}
				if (m.getContent().equals("on y va"))
					killAgent(this);
			}
			if (Math.random() < 0.95)
				sendMessageWithRole("amis", "piece", "personne",
						new StringMessage("ami?"), "personne");
			else
				sendMessageWithRole("amis", "piece", "personne",
						new StringMessage("partir?"), "personne");
		}
	}

	@Override
	public void end() {
		killAgent(this);
	}

}
