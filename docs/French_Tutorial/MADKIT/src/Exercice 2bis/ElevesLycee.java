package Exercice2bis;

import madkit.kernel.Agent;
import madkit.messages.ObjectMessage;

public class ElevesLycee extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	@Override
	public void activate() {
		createGroupIfAbsent("personne", "lycee");
		requestRole("personne", "lycee", "eleve", null);// les agents appartient a la communauté personne ,
		//au groupe lycee et ont le role d'eleve
	}

	public void live() {
		pause(5000);
		while (true) {
			ObjectMessage<String> m = (ObjectMessage<String>) waitNextMessage(1000);//l'agent attend 1000 millisecondes la réception d'un message
			if (m != null) {
				if (m.getContent().equals("Perdu"))//si l'agent reçoit un message "perdu" il met fin à son cycle de vie
				{
					if (logger != null)
						logger.info("Je me suis trompe...");
					killAgent(this);

				} else // sinon il a gagné
				{
					if (logger != null)
						logger.info("J'ai gagne");

				}

			}
			if (Math.random() < 0.70)
				sendMessageWithRole("personne", "lycee", "prof",
						new ObjectMessage<String>("4"), "eleve");// envoi de maniere aléatoire un message " 4" 
			//aux agents de la communaute personne du groupe lycee et de role prof
			else
				sendMessageWithRole("personne", "lycee", "prof",
						new ObjectMessage<String>("2"), "eleve");// envoi de maniere aléatoire un message " 2" 
			//aux agents de la communaute personne du groupe lycee et de role prof
		}
	}
}
