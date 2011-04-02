package Exercice2bis;

import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;

@SuppressWarnings("serial")
public class Lanceur extends Agent {
	// codes réalisés par PRADEILLES Vincent et HISLER Gaelle
	private List<AbstractAgent> agentsList = new ArrayList<AbstractAgent>();

	protected void activate() {
		int initialPause = 1000;

		
			ElevesLycee a = new ElevesLycee();
			if (launchAgent(a, true) == ReturnCode.SUCCESS) {
				agentsList.add(a);
				if (logger != null)
					logger.info("Eleve lycee launched");
				pause((initialPause > 0 ? initialPause : 20));
				initialPause -= Math.random() * 100;
		
				ElevesCollege b = new ElevesCollege();
				if (launchAgent(b, true) == ReturnCode.SUCCESS) {
					agentsList.add(b);
					if (logger != null)
						logger.info("Eleve college launched");
					pause((initialPause > 0 ? initialPause : 20));
					initialPause -= Math.random() * 100;
				}
		
		ProfsLycee a1 = new ProfsLycee();
		if (launchAgent(a1, true) == ReturnCode.SUCCESS) {
			agentsList.add(a1);
			if (logger != null)
				logger.info("Prof lycee launched");
			pause((initialPause > 0 ? initialPause : 20));
			initialPause -= Math.random() * 100;
		}
		ProfsCollege a11 = new ProfsCollege();
		if (launchAgent(a11, true) == ReturnCode.SUCCESS) {
			agentsList.add(a11);
			if (logger != null)
				logger.info("Prof college launched");
			pause((initialPause > 0 ? initialPause : 20));
			initialPause -= Math.random() * 100;
		}
		}
		}
	

	protected void live() {
		pause(5000);
	}

	@Override
	protected void end() {
		int initialPause = 3000;
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
				Lanceur.class.getName() };
		Madkit.main(argss);
	}

}
