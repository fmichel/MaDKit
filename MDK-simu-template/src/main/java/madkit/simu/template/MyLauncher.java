/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
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
