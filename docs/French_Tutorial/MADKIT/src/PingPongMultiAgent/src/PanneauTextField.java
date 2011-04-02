package PingPongMultiAgent.src;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class PanneauTextField extends JPanel{
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * contient les references des LigneTextField contenues
	 * dans le panneau
	 */
	private ArrayList<LigneTexteField> listeLigneTextField;
	/**
	 * ligne sur laquelle la devra s'effectue le prochain
	 * affichage
	 */
	private int ligneSuivante;
	
	public PanneauTextField(int nbreLignes) {
		super();
		this.listeLigneTextField = new ArrayList<LigneTexteField>();
		this.ligneSuivante = 0;
		GridLayout grille = new GridLayout(nbreLignes+1, 1);
		this.setLayout(grille);
		this.add(new LigneEnTete());
		for (int i = 0; i < grille.getRows() - 1;i++){
			LigneTexteField ligneT = new LigneTexteField();
			this.listeLigneTextField.add(ligneT);
			this.add(ligneT);
		}
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @param n
	 * @action remplit la ligne suivant du panneau avec les valeurs s1, s2 et n
	 */
	public void remplitLigneSuivant(String s1, String s2, int n){
		this.listeLigneTextField.get(ligneSuivante).remplitLigneTextField(s1, s2, n);
		this.ligneSuivante = (this.ligneSuivante +1) % this.listeLigneTextField.size();
	}

}
