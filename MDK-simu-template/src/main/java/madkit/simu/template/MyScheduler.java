
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
	private int startingDate = 0;

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
	public void setStartingDate(int date) {
		startingDate = date;
		getSimuTimer().setStartTime(BigDecimal.valueOf(date));
	}

	/**
	 * Gets the starting date of the simulation.
	 *
	 * @return the starting date
	 */
	public int getStartingDate() {
		return startingDate;
	}

}
