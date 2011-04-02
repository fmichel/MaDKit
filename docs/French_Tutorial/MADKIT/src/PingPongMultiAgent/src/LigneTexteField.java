package PingPongMultiAgent.src;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;


@SuppressWarnings("serial")
public class LigneTexteField extends JPanel{
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * contient les references de JTextField contenus dans le LigneTextField
	 */
	private ArrayList<JTextField> listeField;
	
	public LigneTexteField() {
		super();
		this.listeField = new ArrayList<JTextField>();
		GridLayout grille = new GridLayout(1, 3);
		this.setLayout(grille);
		
		for (int i = 0; i < 3; i++){
			JPanel panT = new JPanel();
			JTextField textF = new JTextField(i == 2 ? 2 : 8);
			textF.setEnabled(false);
			this.listeField.add(textF);
			panT.add(textF, BorderLayout.CENTER);
			this.add(panT);
		}
	}
	
	/**
	 * @param s
	 * @param i
	 * @action remplit le i-eme JTextField avec la valeur s
	 */
	public void remplitTextField(String s, int i){
		this.listeField.get(i).setColumns(0);
		this.listeField.get(i).setText(s);
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @param n
	 * @action remplit le LigneTextField avec les valeurs s1, s2 et n
	 */
	public void remplitLigneTextField(String s1, String s2, int n){
		remplitTextField(s1, 0);
		remplitTextField(s2, 1);
		remplitTextField(Integer.toString(n), 2);
		
	}

}
