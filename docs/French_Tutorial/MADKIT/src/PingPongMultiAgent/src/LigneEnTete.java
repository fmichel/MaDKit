package PingPongMultiAgent.src;

@SuppressWarnings("serial")
public final class LigneEnTete extends LigneTexteField{
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	public LigneEnTete() {
		super();
		this.remplitTextField("Nom du joueur", 0);
		this.remplitTextField("Nom de l'adversaire", 1);
		this.remplitTextField("Score", 2);

	}

}
