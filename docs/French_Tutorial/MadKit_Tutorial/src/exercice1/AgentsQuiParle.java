package exercice1;

import java.util.ArrayList;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.messages.StringMessage;

@SuppressWarnings("serial")
public class AgentsQuiParle extends Agent {

	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
	private ArrayList<AgentAddress> listeAgents; // creation d'une liste d'agents

	
	static int Nombreagents=2;// le nombre d'agent est de 2 dans ce premier exercice 
	
	
	public void activate() {
		this.listeAgents = new ArrayList<AgentAddress>();
		createGroupIfAbsent("amis", "piece");
		requestRole("amis", "piece", "personne", null); // les agents appartient a la communaut� amis ,
		//au groupe piece et ont le role de personne
	}
	@SuppressWarnings("unchecked")
	@Override
	public void live() {
		pause(5000);
		while (Nombreagents>1) // si il ne reste qu'un agent il ne peut pas envoyer de message a un autre
		{
			
			StringMessage m = (StringMessage) waitNextMessage(1000);
			if (m != null) {
				if (m.getContent().equals("prenom?")){ // si l'agent re�oit un message "prenom?" il repond qui il est et demande qui est l'agent qui lui a parl�
					sendMessage(m.getSender(), new StringMessage(
					"et toi?"));waitNextMessage(1000); // attente d'un message en r�ponse
					this.listeAgents.add(m.getSender());// ajout de l'agent � la liste d'agents
					
					if (logger != null)
						logger.info("je suis " + m.getReceiver());
					
				}
				if (m.getContent().equals("et toi?")) {// si l'agent re�oit un message " et toi?" il r�pond "secret" 
					sendMessage(m.getSender(), new StringMessage(
							"secret"));
					if (logger != null)
						logger.info("pas envie de lui qui je suis dire mais je sais qui il est c'est :" + m.getSender());
					killAgent(this);
					Nombreagents--;
					
					
				}
			
				
			}
			if (Nombreagents >1)
			if (Math.random() < 0.98)
				sendMessageWithRole("amis", "piece", "personne",
						new StringMessage("prenom?"), "personne");// envoi de maniere al�atoire un message " prenom?" 
			//aux agents de la communaute amis du groupe piece et de role personne
			else
				sendMessageWithRole("amis", "piece", "personne",
						new StringMessage("et toi?"), "personne");// envoi de maniere al�atoire un message " et toi?"
			//aux agents de la communaute amis du groupe piece et de role personne
		}
	}

	@Override
	public void end() {
		
	}

}
