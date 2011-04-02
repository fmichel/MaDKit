package Exercice4;

import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;

@SuppressWarnings("serial")
public class LanceurAgents extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	private List<AbstractAgent> agentsList = new ArrayList<AbstractAgent>();

	protected void activate() {

		GestionnaireMeteo g = new GestionnaireMeteo();
		if (launchAgent(g, true) == ReturnCode.SUCCESS) {
			agentsList.add(g);
			if (logger != null)
				logger.info("Agents Gestionnaire Meteo launched");

			AgentMeteo a = new AgentMeteo();
			if (launchAgent(a, true) == ReturnCode.SUCCESS) {
				agentsList.add(a);
				if (logger != null)
					logger.info("Agents Meteo launched");
			}
		}
	}

	protected void live() {
		pause(Integer.MAX_VALUE);
	}

	@Override
	protected void end() {
		int initialPause = 2000;
		while (!agentsList.isEmpty()) {
			AbstractAgent ag = agentsList
					.remove((int) (agentsList.size() * Math.random()));
			killAgent(ag);
			pause((initialPause > 0 ? initialPause : 100));
			initialPause -= Math.random() * 100;
			if (logger != null)
				logger.info("living " + agentsList);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] argss = { "--agentLogLevel", "INFO", "--launchAgents",
				LanceurAgents.class.getName() };
		Madkit.main(argss);
	}

}