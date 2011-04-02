
package exercice1;

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
		int initialPause = 1000;

		for (int i = 1; i <=2 ; i++) {
			AgentsQuiParle a = new AgentsQuiParle();
			if (launchAgent(a, true) == ReturnCode.SUCCESS) {
				agentsList.add(a);
				if (logger != null)
					logger.info("Agents N" + i + "launched");
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
				LanceurAgents.class.getName() };
		Madkit.main(argss);
	}

}