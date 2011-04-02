package PingPongMultiAgent.src;

import madkit.kernel.Agent;
import madkit.messages.ObjectMessage;

@SuppressWarnings("serial")
public class Arbitre extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	/**
	 * tableau ou les scores sont affiches
	 */
	private TableauScore tableauScore;

	@Override
	protected void activate() {
		this.tableauScore = new TableauScore(13);
		createGroupIfAbsent("ping-pong", "room", true, null);
		requestRole("ping-pong", "room", "arbitre", null);
		if (logger != null)
			logger.info("I'm gonna be keeping the scores !");
	}

	@Override
	protected void live() {
		while (true)
			this.suivreScore();
	}

	@SuppressWarnings("unchecked")
	private void suivreScore() {
		ObjectMessage<ResulatMatch> score;
		// attend la reception du resultat d'un match
		score = (ObjectMessage<ResulatMatch>) waitNextMessage(1000);
		if (score != null) {
			// si on score a ete recu, on l'affiche sur la tableau des scores
			this.tableauScore.ecrireResulat(score.getContent());
		}
	}
}
