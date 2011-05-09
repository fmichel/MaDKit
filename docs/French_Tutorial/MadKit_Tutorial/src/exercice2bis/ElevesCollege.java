package exercice2bis;

import madkit.kernel.Agent;
import madkit.messages.ObjectMessage;

public class ElevesCollege extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
	@Override
	public void activate() {
		createGroupIfAbsent("personne", "college");
		requestRole("personne", "college", "eleve", null);// les agents
															// appartient a la
															// communaut�
															// personne ,
		// au groupe college et ont le role d'eleve
	}

	@SuppressWarnings("unchecked")
	public void live() {
		pause(5000);
		while (true) {
			ObjectMessage<String> m = (ObjectMessage<String>) waitNextMessage(1000);// l'agent
																					// attend
																					// 1000
																					// millisecondes
																					// la
																					// r�ception
																					// d'un
																					// message
			if (m != null) {
				if (m.getContent().equals("Perdu"))// si l'agent re�oit un
													// message "perdu" il met
													// fin � son cycle de vie
				{
					if (logger != null)
						logger.info("Je me suis trompe...");
					killAgent(this);

				} else // sinon il a gagn�
				{
					if (logger != null)
						logger.info("J'ai gagne");

				}

			}
			if (Math.random() < 0.70)
				sendMessageWithRole("personne", "lycee", "prof",
						new ObjectMessage<String>("4"), "eleve");// envoi de
																	// maniere
																	// al�atoire
																	// un
																	// message
																	// " 4"
			// aux agents de la communaute personne du groupe lycee et de role
			// prof
			else
				sendMessageWithRole("personne", "lycee", "prof",
						new ObjectMessage<String>("2"), "eleve");// envoi de
																	// maniere
																	// al�atoire
																	// un
																	// message
																	// " 2"
			// aux agents de la communaute personne du groupe lycee et de role
			// prof
		}
	}
}
