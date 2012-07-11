package PingPongMultiAgent.src;

public class ResulatMatch {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * nom du joueur
	 */
	private String joueur;
	/**
	 * nom de l'adversaire
	 */
	private String adversaire;
	/**
	 * score du joueur
	 */
	private int score;
	
	public ResulatMatch(String joueur, String adversaire, int score) {
		super();
		this.joueur = joueur;
		this.adversaire = adversaire;
		this.score = score;
	}

	public String joueur() {
		return joueur;
	}

	public String adversaire() {
		return adversaire;
	}

	public int score() {
		return score;
	}
	
	
}
