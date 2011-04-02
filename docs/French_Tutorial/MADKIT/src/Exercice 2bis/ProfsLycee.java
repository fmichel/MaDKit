package Exercice2bis;

import madkit.kernel.Agent;
import madkit.messages.ObjectMessage;

public class ProfsLycee extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	int nbreErreurs = 0;

	public void activate() {
		createGroupIfAbsent("personne", "lycee");
		requestRole("personne", "lycee", "prof", null);// les agents appartient a la communauté personne ,
		//au groupe lycee et ont le role de prof
	}

	public void live() {
		pause(5000);
		while (true) {
			if (this.nbreErreurs == 1)// si il y a 1 erreurs le prof n'a plus d'eleves , ce nombre peut etre modifié a tout moment
			{
				if (logger != null)
					logger.info("Je n'ai plus d'eleve...");
			} else {
				if (Math.random() > 0.95) // sinon il pose une quetion à ses eleves
				{
					if (logger != null)
						logger.info("Je pose une question");
					sendMessageWithRole("personne", "lycee", "eleve",
							new ObjectMessage<String>("2+2"), "prof");
				}
			}

			ObjectMessage<String> m = (ObjectMessage<String>) waitNextMessage(1000);//l'agent attend 1000 millisecondes la réception d'un message
			if (m != null) {
				if (m.getContent().equals("2")) // si il reçoit un message "2" il incrémente le nombre d'erreur 
					//et indique a l'expediteur qu'il s'est trompé
				{
					this.nbreErreurs++;
					sendMessage(m.getSender(), new ObjectMessage<String>(
							"Perdu"));
				}

				if (m.getContent().equals("4"))// si il reçoit un message "4" et indique a l'expediteur qu'il a trouvé la bonne réponse
				{
					sendMessage(m.getSender(), new ObjectMessage<String>(
							"Bravo"));
					waitNextMessage(1000);
					if (logger != null)
						logger.info("Bravo" + m.getSender());

				}

			}

		}
	}
}
