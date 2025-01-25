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
package madkit.kernel;

import static madkit.kernel.Scheduler.SimulationState.RUNNING;
import static madkit.kernel.Scheduler.SimulationState.SHUTDOWN;
import static madkit.kernel.Scheduler.SimulationState.STEP;
import static madkit.simulation.SimuOrganization.ENVIRONMENT_ROLE;
import static madkit.simulation.SimuOrganization.SCHEDULER_ROLE;
import static madkit.simulation.SimuOrganization.VIEWER_ROLE;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ScrollEvent;
import madkit.action.SchedulingAction;
import madkit.messages.SchedulingMessage;
import madkit.simulation.SimuAgent;
import madkit.simulation.SimuLauncher;
import madkit.simulation.SimuOrganization;
import madkit.simulation.Viewer;
import madkit.simulation.scheduler.DateBasedTimer;
import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.SimuTimer;
import madkit.simulation.scheduler.TickBasedTimer;
import net.jodah.typetools.TypeResolver;

/**
 * <pre>
 * Scheduler is the core agent for defining multi-agent based simulations in MaDKit.
 * 
 * <pre>
 * This class defines a generic threaded agent which is in charge of activating the
 * simulated agents using {@link Activator}. {@link Activator} are tool objects which are
 * able to trigger any available method belonging to agents having a role within a group.
 * 
 * <pre>
 * The interest of this approach is twofold:
 * <ul>
 * <li>Firstly it allows the manipulation of agents regardless of their concrete Java
 * classes.
 * <li>Secondly it allows to define complex scheduling policies by defining different
 * activators.
 * </ul>
 * 
 * <pre>
 * So a scheduler holds a collection of activators that target specific groups and roles
 * and thus allow to define very complex scheduling policies if required. A default
 * behavior is implemented and corresponds to the triggering of the activators according
 * to the order in which they have been added to the scheduler engine using
 * {@link #addActivator(Activator)}.
 * 
 * <pre>
 * The default state of a scheduler is {@link SimulationState#PAUSED}.
 * 
 * <pre>
 * The default delay between two simulation steps is 0 milliseconds (max speed).
 * 
 * <pre>
 * Default GUI components are defined for this agent and they could be easily integrated
 * in any GUI.
 *
 * @version 6.0
 * @param <T> the type of the simulation time. It should be a subclass of either a
 *            {@link DateBasedTimer} or a {@link TickBasedTimer}. Two different temporal
 *            schemes could used:
 *            <ul>
 *            <li>tick-based: The time is represented as a {@link BigDecimal} which is
 *            incremented at will. {@link BigDecimal} is used to avoid rounding errors
 *            that may happen when working with double values. This is the preferred
 *            choice if the simulation is based on simple loop following a discrete time
 *            approach.</li>
 * 
 *            <li>date-based: The time is represented using {@link LocalDateTime}. This is
 *            far more convenient when the model refers to a real-world case for which
 *            representing usual temporal units such as hours or weeks is required (the
 *            default used unit is {@link ChronoUnit#SECONDS}). This mode also allow to
 *            handle agents that evolve considering different time scales or during
 *            specific period of the day.</li>
 *            </ul>
 * @see Activator
 * @see BigDecimal
 * @see LocalDateTime
 */
public abstract class Scheduler<T extends SimuTimer<?>> extends SimuAgent {

	private final List<Activator> activators = new ArrayList<>();

	private T simuTime;

	private static final ActivatorComparator activatorComparator = new ActivatorComparator();

	private SimulationState simulationState = SimulationState.PAUSED;

	private Action run;

	private IntegerProperty pause = new SimpleIntegerProperty(0);

	private ActionGroup shedulingActions;

	/**
	 * Default constructor. It automatically initializes the simulation time according to the
	 * type of the simulation time.
	 */
	@SuppressWarnings("unchecked")
	protected Scheduler() {
		Class<?> timeClass = TypeResolver.resolveRawArguments(Scheduler.class, this.getClass())[0];
		if (DateBasedTimer.class.isAssignableFrom(timeClass)) {
			setSimulationTime((T) new DateBasedTimer());
		} else {
			setSimulationTime((T) new TickBasedTimer());
		}
	}

	/**
	 * On activation, by default the scheduler requests the role
	 * {@link SimuOrganization#SCHEDULER_ROLE} in the group {@link SimuOrganization#ENGINE_GROUP}.
	 */
	@Override
	protected void onActivation() {
		try {
			Field f = SimuLauncher.class.getDeclaredField("scheduler");
			f.setAccessible(true);// NOSONAR
			f.set(getLauncher(), this);// NOSONAR
			f.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		requestRole(getCommunity(), getEngineGroup(), SCHEDULER_ROLE);
	}

	/**
	 * The main loop of the scheduler agent. It is automatically called by the MaDKit kernel
	 * once the agent is activated. Firstly, the scheduler waits for the first message to
	 * start the simulation. Then, it enters an infinite loop where it waits for messages,
	 * checks the simulation state, and processes the simulation step accordingly. The loop is
	 * interrupted when the simulation reaches the end time or when a SHUTDOWN message is
	 * received.
	 *
	 */
	@Override
	protected void onLive() {
		waitStartingMessage();
		while (!Thread.currentThread().isInterrupted()) {
			if (getSimuTimer().hasReachedEndTime()) {
				getLogger().info(() -> "Simulation has reached end time -> " + getSimuTimer());
				return;
			}
			pause(pause.get());
			checkMail(nextMessage());
			exitOnKill();
			switch (simulationState) {
			case RUNNING:
				doSimulationStep();
				break;
			case PAUSED:
				paused();
				break;
			case STEP:
				simulationState = SimulationState.PAUSED;
				doSimulationStep();
				break;
			case SHUTDOWN:
				return; // shutdown
			default:
				getLogger().severe(() -> "state not handled " + simulationState);
			}
		}
	}

	private void waitStartingMessage() {
		while (true) {
			checkMail(waitNextMessage());
			if (simulationState == RUNNING || simulationState == STEP || simulationState == SHUTDOWN) {
				onSimulationStart();
				break;
			}
		}
	}

	/**
	 * Called when the simulation starts, or when the user clicks on the onsimulationrestart
	 * button. By default, it resets the simulation time and calls the
	 * {@link #onSimulationStart()} method of the environment agent
	 * 
	 */
	@Override
	public void onSimulationStart() {
		getLogger().fine("------- Starting simulation --------");
		getSimuTimer().reset();
		getLogger().finer("------- seting time to " + getSimuTimer());
	}

	/**
	 * Called when the simulation ends.
	 */
	@Override
	protected void onEnd() {
		simulationState = SimulationState.PAUSED;
		getLogger().info(() -> "------- Simulation stopped! Time was = " + getSimuTimer());
		getLogger().finest("removing all activators");
		removeAllActivators();
		super.onEnd();
	}

	/**
	 * Changes my state according to a {@link SchedulingMessage} and sends a reply to the
	 * sender as acknowledgment.
	 *
	 * @param m the received message
	 */
	private void checkMail(Message m) {
		if (m != null) {
			try {
				SchedulingAction code = ((SchedulingMessage) m).getCode();
				switch (code) {
				case RUN:
					setSimulationState(SimulationState.RUNNING);
					break;
				case STEP:
					setSimulationState(SimulationState.STEP);
					break;
				case PAUSE:
					setSimulationState(SimulationState.PAUSED);
					break;
				case SHUTDOWN:
					setSimulationState(SimulationState.SHUTDOWN);
					break;
				default:
					getLogger().severe(() -> "code not handled " + code);
				}
				if (m.getSender() != null) {
					reply(m, m);
				}
			} catch (ClassCastException e) {
				getLogger().warning(() -> "I received a message that I cannot understand" + m);
			}
		}
	}

	/**
	 * Runs {@link #checkMail(Message)} every 1000 ms.
	 */
	private void paused() {
		checkMail(waitNextMessage(1000));
	}

	/**
	 * Changes the state of the scheduler
	 *
	 * @param newState the new state
	 */
	private void setSimulationState(final SimulationState newState) {
		if (simulationState != newState) {
			simulationState = newState;
			switch (simulationState) {
			case STEP, PAUSED:
				enableRunAction(false);
				break;
			case RUNNING, SHUTDOWN:
				enableRunAction(true);
				break;
			default:// impossible
			}
		}
	}

	private void enableRunAction(boolean b) {
		if (run != null) {
			run.setDisabled(b);
		}
	}

	/**
	 * Defines a simulation step.
	 *
	 * This method should be overridden to define a customized scheduling policy.
	 *
	 */
	protected abstract void doSimulationStep();

	/**
	 * Returns the delay between two simulation steps.
	 *
	 * @return the pause between two steps
	 */
	public int getPause() {
		return pause.get();
	}

	/**
	 * Sets the delay between two simulation steps.
	 *
	 * @param delay the pause between two steps
	 */
	public void setPause(int delay) {
		pause.set(delay);
	}

	/**
	 * Gets a {@link Slider} that can manipulate the pause between two simulation steps.
	 * 
	 * @return a {@link Slider} to change the pause between two simulation steps
	 */
	public Slider getPauseSlider() {
		Slider slider = new Slider(0, 400, getPause());
		slider.setMinWidth(200);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(10);
		slider.setMinorTickCount(1);
		slider.setBlockIncrement(1);
		slider.setSnapToTicks(true);
		pause.bindBidirectional(slider.valueProperty());
		slider.setOnScroll(
				(ScrollEvent event) -> slider.setValue(pause.getValue() + (event.getDeltaY() > 0 ? 1.0 : -1.0)));
		return slider;
	}

	/**
	 * Returns a {@link ToolBar} made of scheduling actions, a pause slider and a label
	 * displaying the current simulation time.
	 * 
	 * @return a {@link ToolBar} made of common scheduling actions.
	 */
	public ToolBar getToolBar() {
		ToolBar tb = ActionUtils.createToolBar(getActions(), ActionTextBehavior.HIDE);
		tb.getItems().add(getPauseSlider());
		tb.getItems().add(getTimeLabel());
		return tb;
	}

	/**
	 * Returns the actions group that can be used to control the simulation.
	 * 
	 * @return the shedulingActions as an ActionGroup
	 */
	public ActionGroup getShedulerActions() {
		if (shedulingActions == null) {
			run = SchedulingAction.RUN.getActionFrom(this);
			Action step = SchedulingAction.STEP.getActionFrom(this);
			Collection<Action> actions = new ArrayList<>();
			actions.add(run);
			actions.add(step);
			shedulingActions = new ActionGroup("Scheduling", actions);
		}
		return shedulingActions;
	}

	/**
	 * Returns the actions that can be used to control the simulation.
	 * 
	 * @return the actions as a List
	 */
	public List<Action> getActions() {
		return getShedulerActions().getActions();
	}

	/**
	 * Returns a Label displaying the time of the simulation.
	 *
	 * @return the current simulation state
	 */
	public Label getTimeLabel() {
		return simuTime.getTimeLabel();
	}

	/**
	 * Sets the simulation time of the simulation.
	 * 
	 * @param simulationTime the simulationTime to set
	 */
	protected void setSimulationTime(T simulationTime) {
		this.simuTime = simulationTime;
	}

	/**
	 * Returns the {@link SimuTimer} of the scheduler.
	 *
	 * @return the {@link SimuTimer} of the scheduler
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getSimuTimer() {
		return simuTime;
	}

	/**
	 * Gets the list of activators that are currently added to the simulation engine by this
	 * scheduler.
	 * 
	 * @return a list of activators.
	 */
	public List<Activator> getActivators() {
		return activators;
	}

	/**
	 * Adds the default activator for viewers and returns it. The default activator for
	 * viewers is an activator that triggers the method {@link Viewer#display()} of agents
	 * playing the role {@link SimuOrganization#VIEWER_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}
	 * 
	 * @return an activator that triggers viewers display method
	 */
	public MethodActivator addViewersActivator() {
		MethodActivator v = new MethodActivator(getEngineGroup(), VIEWER_ROLE, "display");
		addActivator(v);
		return v;
	}

	/**
	 * Adds the default activator for environment and returns it. The default activator for
	 * environment is an activator that triggers the method update of agents playing the role
	 * {@link SimuOrganization#ENVIRONMENT_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}
	 * 
	 * @return an activator that triggers the environment update method
	 */
	public MethodActivator addEnvironmentActivator() {
		MethodActivator v = new MethodActivator(getEngineGroup(), ENVIRONMENT_ROLE, "update");
		addActivator(v);
		return v;
	}

	/**
	 * Updates the activators schedule according to their priorities.
	 */
	void updateActivatorsSchedule() {
		Collections.sort(activators, activatorComparator);
	}

	/**
	 * Adds an activator to the simulation engine. This has to be done before the simulation
	 * starts. It is possible to add several activators to the simulation engine. Once added,
	 * the activator can be triggered by the scheduler agent using the
	 * {@link Activator#execute(Object...)} method.
	 *
	 * @param activator an activator to add to the simulation engine.
	 */
	public void addActivator(Activator activator) {
		activator.setScheduler(this);
		if (getOrganization().addOverlooker(this, activator)) {
			activators.add(activator);
			updateActivatorsSchedule();
			getLogger().fine(() -> activator + " added");
		} else {
			getLogger().warning(() -> activator + " already added");
		}
	}

	/**
	 * Removes an activator from the simulation engine.
	 *
	 * @param activator an activator.
	 */
	public void removeActivator(Activator activator) {
		getOrganization().removeOverlooker(activator);
		activators.remove(activator);
		getLogger().fine(() -> "Activator removed: " + activator);
	}

	/**
	 * Logs the current simulation time value.
	 *
	 * logs are displayed only if {@link #getLogger()} is set above {@link Level#FINE}.
	 */
	public void logCurrrentTime() {
		getLogger().fine(() -> "Current simulation time -> " + getSimuTimer());
	}

	/**
	 * Triggers the execute method of this <code>activator</code> and logs it using the
	 * {@link Level#FINER} logging level.
	 *
	 * @param activator the activator to trigger
	 * @param args      the args that will be passed to the targeted method
	 */
	protected void executeAndLog(Activator activator, Object... args) {
		getLogger().finer(() -> "Activating--> " + activator);
		activator.execute(args);
	}

	/**
	 * Remove all the activators which have been previously added.
	 */
	public void removeAllActivators() {
		activators.forEach(a -> getOrganization().removeOverlooker(a));
		activators.clear();
	}

	/**
	 * A simulation state. The simulation process managed by a scheduler agent can be in one
	 * of the following states:
	 * <ul>
	 * <li>{@link #RUNNING}<br>
	 * The simulation process is running normally.</li>
	 * <li>{@link #STEP}<br>
	 * The scheduler will process one simulation step and then will be in the {@link #PAUSED}
	 * state.</li>
	 * <li>{@link #PAUSED}<br>
	 * The simulation is paused. This is the default state.</li>
	 * </ul>
	 *
	 * @since MaDKit 5.0
	 */
	public enum SimulationState {
		/**
		 * The simulation process is running normally.
		 */
		RUNNING,

		/**
		 * The scheduler will process one simulation step and then will be in the {@link #PAUSED}
		 * state.
		 */
		STEP,

		/**
		 * The simulation is paused.
		 */
		PAUSED,

		/**
		 * The simulation is stopped.
		 */
		SHUTDOWN
	}

	/**
	 * The Class ActivatorComparator.
	 */
	static class ActivatorComparator implements Comparator<Activator> {

		/**
		 * Compare.
		 *
		 * @param o1 the o 1
		 * @param o2 the o 2
		 * @return the int
		 */
		@Override
		public int compare(Activator o1, Activator o2) {
			return Integer.compare(o1.getPriority(), o2.getPriority());

		}
	}
}
