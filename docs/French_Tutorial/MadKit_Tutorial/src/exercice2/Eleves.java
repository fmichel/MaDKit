package exercice2;

import madkit.kernel.Agent;
import madkit.messages.ObjectMessage;

@SuppressWarnings("serial")
public class Eleves extends Agent {
	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
	@Override
	public void activate() {
		createGroupIfAbsent("salle K029", "lycee"); 
		requestRole("salle K029", "lycee", "eleve", null);// les agents appartient a la communaut� salle K029 ,
		//au groupe lycee et ont le role d'eleve
	}

	@SuppressWarnings("unchecked")
	public void live() {
		pause(5000);
		while (true) {
			ObjectMessage<String> m = (ObjectMessage<String>) waitNextMessage(1000); // l'agent attend 1000 millisecondes un message
			if (m != null) {
				if (m.getContent().equals("Perdu"))// si le message est "perdu" il s'est tromp� et met fin a son cycle de vie
				{
					if (logger != null)
						logger.info("Je me suis trompe...");
					killAgent(this);

				} else // sinon l'agent a gagn�
				{
					if (logger != null)
						logger.info("J'ai gagne");

				}

			}
			if (Math.random() < 0.70)
				sendMessageWithRole("salle K029", "lycee", "prof", 
						new ObjectMessage<String>("4"), "eleve");// envoi de maniere al�atoire un message " 4" 
			//aux agents de la communaute salle K029 du groupe lycee et de role prof
				sendMessageWithRole("salle K029", "lycee", "prof",
						new ObjectMessage<String>("2"), "eleve");// envoi de maniere al�atoire un message " 2" 
				//aux agents de la communaute salle K029 du groupe lycee et de role prof
		}
	}
}
