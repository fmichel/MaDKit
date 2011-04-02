package PingPongMultiAgent.src;

import java.awt.Color;
import java.awt.Component;

//import madkit.constants.MadkitCommandLineOptions;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

@SuppressWarnings("serial")
public class PingPong extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	private AgentAddress currentPartner = null;
	private ObjectMessage<Balle> ball = null;
	private Color ballColor = null;
	private double ballForce;
	private Component myGUI;
	private final static int SCORE_ELIMINATOIRE = 5;
	private int handicap = 0;

	@Override
	public void activate() {
		myGUI = getGUIComponent(); // getting the default GUI component assigned
									// by the Madkit kernel
		createGroupIfAbsent("ping-pong", "room", true, null);
		requestRole("ping-pong", "room", "player", null);
	}

	@Override
	public void live() {
		while (true) {
			searching();
			playing();
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void searching() {
		currentPartner = null; // searching a new partner
		changeGUIColor(Color.WHITE);
		ball = null;
		while (currentPartner == null) {
			ball = (ObjectMessage<Balle>) waitNextMessage(1000);
			if (ball != null) {
				currentPartner = ball.getSender();
			} else {
				currentPartner = getAgentWithRole("ping-pong", "room", "player");
			}
		}
		if (logger != null)
			logger.info("I found someone to play with : " + currentPartner
					+ " !!!!!! ");
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void playing() {
		if (ball == null) {
			// On interdit le blanc comme couleur de fond
			while ((ballColor = getRandomColor()) == Color.WHITE)
				;
			ballForce = Math.random();
			ball = (ObjectMessage<Balle>) sendMessageAndWaitForReply(
					currentPartner, new ObjectMessage<Balle>(new Balle(
							ballColor, ballForce)), 1300);
			if (ball == null) { // nobody replied !
				if (logger != null)
					logger.info(currentPartner
							+ " did not replied to me :( !! ");
				currentPartner = null;
				return;
			}
		} else {
			ballColor = ball.getContent().couleur();
		}

		changeGUIColor(ballColor);
		ObjectMessage<Balle> ballMessage = new ObjectMessage<Balle>(new Balle(
				ballColor, Math.random()));
		int nbrePoints = 0;
		for (int i = 0; i < 10; i++) {// if ball == null partner is gone !!

			ball = (ObjectMessage<Balle>) sendReplyAndWaitForReply(ball,
					ballMessage, 1300);
			if (ball == null) {
				if (logger != null)
					logger.info(currentPartner + " is gone :( !! ");
				break;
			}

			if (logger != null) {
				String res = "";
				if (ball.getContent().force() > Math.random()) {
					res = " I won the point";
					nbrePoints++;
				} else
					res = " I lost the point";
				/*
				 * On dŽtermine une "dŽfense" alŽatoire, et l'on consid�re que
				 * le point est perdu si la dŽfense est strictement infŽrieure ˆ
				 * la force de la balle.
				 */
				logger.info(" Playing :) with " + currentPartner
						+ " ball nb is " + i + res);
			}
			pause((int) (Math.random() * 1000));
			ballMessage = new ObjectMessage<Balle>(new Balle(ballColor,
					Math.random()));
		}
		if (logger != null)
			logger.info("I won " + nbrePoints + " times!");
		// Envoi du resultat a l'arbitre
		sendMessageWithRole(
				"ping-pong",
				"room",
				"arbitre",
				new ObjectMessage<ResulatMatch>(new ResulatMatch(
						this.getName(), currentPartner.toString(), nbrePoints)),
				"player");
		if (nbrePoints < (PingPong.SCORE_ELIMINATOIRE + this.handicap))
			// l'agent est elimine de la competition
			killAgent(this);
		else
			// l'agent reste dans la competition
			this.handicap++;
		while (nextMessage() != null)
			; // purge mailBox from old playing attempts
	}

	/**
	 * 
	 */
	private void changeGUIColor(Color c) {
		if (myGUI != null) {
			myGUI.setBackground(c);
		}
	}

	private Color getRandomColor() {
		return new Color((int) (Math.random() * 256),
				(int) (Math.random() * 256), (int) (Math.random() * 256));
	}

}