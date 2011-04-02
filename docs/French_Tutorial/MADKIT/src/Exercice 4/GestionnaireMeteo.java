package Exercice4;

import java.util.ArrayList;

import madkit.kernel.AgentAddress;

@SuppressWarnings("serial")
public class GestionnaireMeteo extends AgentMeteo {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	private ArrayList<AgentAddress> listeAmis;

	@SuppressWarnings("unchecked")
	@Override
	public void live() {
		while (true) {
			// on met a jour la temperature avec une valeur aleatoire entre -15 et 34
			temperature = (int) (Math.random() * 50 - 15);
			pause(1000);
		}
	}

}
