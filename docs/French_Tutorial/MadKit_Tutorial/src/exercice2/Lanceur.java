package exercice2;

import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;

@SuppressWarnings("serial")
public class Lanceur extends Agent {
	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
 private List<AbstractAgent> agentsList = new ArrayList<AbstractAgent>();

	protected void activate() {
		int initialPause = 1000;

		for (int i = 1; i <= 3; i++) {
			Eleves a = new Eleves();
			if (launchAgent(a, true) == ReturnCode.SUCCESS) {
				agentsList.add(a);
				if (logger != null)
					logger.info("Eleve N" + i + "launched");
				pause((initialPause > 0 ? initialPause : 20));
				initialPause -= Math.random() * 100;
			}
		}

		Profs a = new Profs();
		if (launchAgent(a, true) == ReturnCode.SUCCESS) {
			agentsList.add(a);
			if (logger != null)
				logger.info("Prof launched");
			pause((initialPause > 0 ? initialPause : 20));
			initialPause -= Math.random() * 100;

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
