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

import java.math.BigDecimal;
import java.util.logging.Level;

import madkit.gui.UIProperty;
import madkit.kernel.Activator;
import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * The `MyScheduler` class extends the `TickBasedScheduler` class and is
 * responsible for scheduling the simulation steps. It uses activators to manage
 * the execution of agents and viewers during each simulation step.
 */
public class MyScheduler extends TickBasedScheduler {

	/**
	 * This property just illustrates how to use the UIProperty annotation to
	 * specify data that can be modified through the GUI. It is not really of any use in this
	 * simulation.
	 */
	@UIProperty(category = "simulation", displayName = "initial date")
	private int initialTick = 0;

	/**
	 * Activator for the agents in the simulation.
	 */
	private MethodActivator agents;

	/**
	 * Activator for the viewers in the simulation.
	 */
	private Activator viewers;

	/**
	 * This method is called when the scheduler is activated. It sets the logger
	 * level, initializes the activators for agents and viewers, and adds them to
	 * the scheduler.
	 */
	@Override
	protected void onActivation() {
		// Set the logger level to FINER for detailed logging
		getLogger().setLevel(Level.FINER);
		// Call the superclass's onActivation method
		super.onActivation();
		// Initialize the activator for agents with the method "doIt" for the role "simuAgent"
		agents = new MethodActivator(getModelGroup(), "simuAgent", "doIt");
		// Add the agents activator to the scheduler
		addActivator(agents);
		// Enable shuffling mode for the agents activator
		agents.setShufflingMode(true);

		// Add the activator for viewers using the inherited addViewersActivator method 
		viewers = addViewersActivator();
		// The previous line is equivalent to the following code:
		// viewers = new MethodActivator(getEngineGroup(), SimuOrganization.VIEWER_ROLE, "display");
		// addActivator(viewers);
	}

	/**
	 * This method is called to perform a simulation step. It executes the
	 * activators for agents and viewers, and then calls the superclass method to
	 * complete the step.
	 */
	@Override
	public void doSimulationStep() {
		// Execute the agents activator
		agents.execute();
		// Execute the viewers activator
		viewers.execute();
		// Call the superclass's doSimulationStep method, that adds one time unit to the current time
		super.doSimulationStep();
	}

	/**
	 * Sets the starting date of the simulation. Updates the simulation timer's
	 * start time to the specified date.
	 *
	 * @param date the starting date to set
	 */
	public void setInitialTick(int date) {
		initialTick = date;
		getSimuTimer().setStartTime(BigDecimal.valueOf(date));
	}

	/**
	 * Gets the starting date of the simulation.
	 *
	 * @return the starting date
	 */
	public int getInitialTick() {
		return initialTick;
	}

}
