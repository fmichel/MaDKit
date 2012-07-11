package exercice4;

@SuppressWarnings("serial")
public class GestionnaireMeteo extends AgentMeteo {
	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
//	private ArrayList<AgentAddress> listeAmis;

	@Override
	public void live() {
		while (true) {
			// on met a jour la temperature avec une valeur aleatoire entre -15 et 34
			temperature = (int) (Math.random() * 50 - 15);
			pause(1000);
		}
	}

}
