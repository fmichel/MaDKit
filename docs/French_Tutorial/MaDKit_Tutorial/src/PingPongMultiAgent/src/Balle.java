package PingPongMultiAgent.src;

import java.awt.Color;

public class Balle {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * couleur de la balle
	 */
	private Color ballColor;
	/**
	 * force de la balle
	 */
	private double ballForce;

	public Balle(Color couleur, double force) {
		this.ballColor = couleur;
		this.ballForce = force;
	}

	public Color couleur() {
		return this.ballColor;
	}

	public double force() {
		return this.ballForce;
	}

	public void definirBallColor(Color ballColor) {
		this.ballColor = ballColor;
	}

	public void definirBallForce(double ballForce) {
		this.ballForce = ballForce;
	}

}