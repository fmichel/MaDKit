package madkit.logging;

import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.Option;


@SuppressWarnings("serial")
public class LogFileTester extends Agent {
	
	@Override
	protected void live() {
		if(logger != null)
			logger.info("test");
		logger.talk("test");
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new Madkit(BooleanOption.createLogFiles.toString(),Option.launchAgents.toString(),LogFileTester.class.getName());
	}

}
