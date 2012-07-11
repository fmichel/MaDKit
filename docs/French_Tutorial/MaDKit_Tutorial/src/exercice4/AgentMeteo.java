package exercice4;

import madkit.kernel.Agent;

@SuppressWarnings("serial")
public class AgentMeteo extends Agent {
	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle
	protected static int temperature;

	@Override
	public void activate() {
		
	}

	@Override
	public void live() {
		pause(5000);
		while (true) {
			// on lit la valeur de l'attribut temperature, et on reagit en consequence
			if (temperature <= 0) {
				if (logger != null)
					logger.info("On va faire du ski!");
			}
			if (temperature >= 20) {
				if (logger != null)
					logger.info("On va � la plage!");
			} else {
				if (logger != null)
					logger.info("On fait rien de special :(");
			}
			pause(500);
		}
	}

	@Override
	public void end() {
		killAgent(this);
	}

}
