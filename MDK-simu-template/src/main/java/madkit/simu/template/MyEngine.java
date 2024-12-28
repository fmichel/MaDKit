package madkit.simu.template;

import madkit.simulation.EngineAgents;
import madkit.simulation.Parameter;
import madkit.simulation.SimulationEngine;
import madkit.simulation.environment.Environment2D;
import madkit.simulation.viewer.RolesPopulationLineChartDrawer;

@EngineAgents(
		scheduler=MyScheduler.class,
		environment = Environment2D.class,
		model = MyModel.class,
		viewers = {MyViewer.class,RolesPopulationLineChartDrawer.class}
		)
public class MyEngine extends SimulationEngine {
	
	@Parameter
	private static int nbOfAgents = 10;
	
	@Override
	public void launchSimulatedAgents() {
		for (int i = 0; i < nbOfAgents; i++) {
			launchAgent(new SimulatedAgent());
		}
	}
	
	public static void main(String[] args) {
		executeThisAgent("--kernelLogLevel", "ALL"
//				, "--start"
//				,"--viewers",MyViewer.class.getName()
				);
	}
	
	/**
	 * @return the nbOfAgents
	 */
	public static int getNbOfAgents() {
		return nbOfAgents;
	}

	/**
	 * @param nbOfAgents the nbOfAgents to set
	 */
	public static void setNbOfAgents(int nbOfAgents) {
		MyEngine.nbOfAgents = nbOfAgents;
	}

}
