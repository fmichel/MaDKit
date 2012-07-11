package PingPongMultiAgent.src;

import java.awt.BorderLayout;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class TableauScore extends JFrame {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * panneau contenant les champs de texte qui permettent
	 * l'affichage
	 */
	private PanneauTextField panT;

	public TableauScore(int nbreLignes) {
		super();
		this.panT = new PanneauTextField(nbreLignes);
		this.add(panT, BorderLayout.CENTER);

		this.setSize(800, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * @param score le resultat d'un match
	 * @action inscrit le resultat du match sur panT
	 */
	public void ecrireResulat(ResulatMatch score) {
		this.panT.remplitLigneSuivant(score.joueur(), score.adversaire(),
				score.score());
	}
}
