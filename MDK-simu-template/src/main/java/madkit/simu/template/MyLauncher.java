package madkit.simu.template;

import madkit.gui.UIProperty;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;
import madkit.simulation.environment.Environment2D;

/**
 * The `MyLauncher` class extends the `SimuLauncher` class and is responsible
 * for launching the simulation. It uses annotations to specify the scheduler,
 * environment, model, and viewers for the simulation.
 */
@EngineAgents(scheduler = MyScheduler.class, environment = Environment2D.class, model = MyModel.class, viewers = {
		MyViewer.class })
public class MyLauncher extends SimuLauncher {

	/**
	 * The `MyLauncher` class extends the `SimuLauncher` class and is responsible
	 * for launching the simulation. It uses annotations to specify the scheduler,
	 * environment, model, and viewers for the simulation.
	 */
	@UIProperty
	private static int nbOfAgents = 10;

	/**
	 * This method is called when the simulated agents are launched. It creates and
	 * launches the specified number of `SimulatedAgent` instances.
	 */
	@Override
	public void onLaunchSimulatedAgents() {
		for (int i = 0; i < nbOfAgents; i++) {
			SimulatedAgent agent;
			if (prng().nextBoolean())
				agent = new AnotherSImuAgent();
			else
				agent = new SimulatedAgent();
			launchAgent(agent);
		}
	}

	/**
	 * Gets the number of agents to be launched.
	 * 
	 * @return the number of agents
	 */
	public static int getNbOfAgents() {
		return nbOfAgents;
	}

	  /**
	   * Sets the number of agents to be launched.
	   * 
	   * @param nbOfAgents the number of agents to set
	   */
	public static void setNbOfAgents(int nbOfAgents) {
		MyLauncher.nbOfAgents = nbOfAgents;
	}

	  /**
	   * The main method to execute this agent.
	   * It can be configured to pass options to the MaDKit platform.
	   * 
	   * @param args the command line arguments
	   */
	public static void main(String[] args) {
		executeThisAgent();
	}

}
