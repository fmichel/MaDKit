package madkit.kernel;

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

import static madkit.kernel.AbstractScheduler.SimulationState.RUNNING;
import static madkit.kernel.AbstractScheduler.SimulationState.SHUTDOWN;
import static madkit.kernel.AbstractScheduler.SimulationState.STEP;
import static madkit.simulation.DefaultOrganization.ENVIRONMENT_ROLE;
import static madkit.simulation.DefaultOrganization.SCHEDULER_ROLE;
import static madkit.simulation.DefaultOrganization.VIEWER_ROLE;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ScrollEvent;
import madkit.action.SchedulingAction;
import madkit.messages.SchedulingMessage;
import madkit.simulation.*;
import madkit.simulation.SimulationTimer;
import madkit.simulation.activator.MethodActivator;
import net.jodah.typetools.TypeResolver;

/**
 * <pre>
 * Scheduler is the core agent for defining multi-agent based simulations.
 *
 * This class defines a generic threaded agent which is in charge of activating
 * the simulated agents using {@link Activator}. {@link Activator} are tool
 * objects which are able to trigger any available method belonging to the
 * agents that have a role within a group.
 *
 * The purpose of this approach is to allow the manipulation of agents
 * regardless of their concrete Java classes.
 *
 * So a scheduler holds a collection of activators that target specific groups
 * and roles and thus allow to define very complex scheduling policies if
 * required. A default behavior is implemented and corresponds to the triggering
 * of the activators according to the order in which they have been added to the
 * scheduler engine using {@link #addActivator(Activator)}.
 *
 * The default state of a scheduler is {@link SimulationState#PAUSED}.
 *
 * The default delay between two simulation steps is 0 milliseconds (max speed).
 *
 * Default GUI components are defined for this agent and they could be easily
 * integrated in any GUI.
 * </pre>
 *
 * As of MaDKit 5.3, two different temporal schemes could used:
 * <ul>
 * <li>tick-based: The time is represented as a {@link BigDecimal} which is
 * incremented at will. {@link BigDecimal} is used to avoid rounding errors that
 * may happen when working with double values. This is the preferred choice if
 * the simulation is based on simple loop following a discrete time
 * approach.</li>
 *
 * <li>date-based: The time is represented using {@link LocalDateTime}. This is
 * far more convenient when the model refers to a real-world case for which
 * representing usual temporal units such as hours or weeks is required (the
 * default used unit is {@link ChronoUnit#SECONDS}). This mode also allow to
 * handle agents that evolve considering different time scales or during
 * specific period of the day.</li>
 * </ul>
 *
 * Those two modes are exclusive and can be selected using the corresponding
 * constructor of the scheduler
 *
 * @author Fabien Michel
 * @since MaDKit 2.0
 * @version 5.3
 * @see Activator
 * @see BigDecimal
 * @see LocalDateTime
 */
public abstract class AbstractScheduler<T extends SimulationTimer<?>> extends SimuAgent {

	private final List<Activator> activators = new ArrayList<>();

	private T simuTime;

	private static final ActivatorComparator activatorComparator = new ActivatorComparator();

	private SimulationState simulationState = SimulationState.PAUSED;

	private Action run;

	private Action step;

	private IntegerProperty pause = new SimpleIntegerProperty(0);

	private ActionGroup shedulingActions;

	/**
	 * Constructs a <code>Scheduler</code> using a tick-based
	 * {@link SimulationTime}.
	 */
	@SuppressWarnings("unchecked")
	protected AbstractScheduler() {
		Class<?> timeClass = TypeResolver.resolveRawArguments(AbstractScheduler.class, this.getClass())[0];
		if (DateBasedTimer.class.isAssignableFrom(timeClass)) {
			setSimulationTime((T) new DateBasedTimer());
		} else {
			setSimulationTime((T) new TickBasedTimer());
		}
	}

	@Override
	protected void onActivation() {
		try {
			Field f = SimulationEngine.class.getDeclaredField("scheduler");
			f.setAccessible(true);//NOSONAR
			f.set(getSimuEngine(), this);//NOSONAR
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		requestRole(getCommunity(), getEngineGroup(), SCHEDULER_ROLE);
	}

	/**
	 * Scheduler's default behavior. default code is:
	 *
	 * <pre>
	 * while (isAlive()) {
	 * 	if (GVT &gt; getSimulationDuration()) {
	 * 		if (logger != null)
	 * 			logger.info(&quot;Simulation has reached end time -> &quot; + getSimulationDuration());
	 * 		return;
	 * 	}
	 * 	pause(getDelay());
	 * 	checkMail(nextMessage());
	 * 	switch (getSimulationState()) {
	 * 	case RUNNING:
	 * 		doSimulationStep();
	 * 		break;
	 * 	case PAUSED:
	 * 		paused();
	 * 		break;
	 * 	case STEP:
	 * 		simulationState = PAUSED;
	 * 		doSimulationStep();
	 * 		break;
	 * 	case SHUTDOWN:
	 * 		return; // shutdown
	 * 	default:
	 * 		getLogger().severe(&quot;state not handled &quot; + getSimulationState);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @see madkit.kernel.Agent#onLiving()
	 */
	@Override
	protected void onLiving() {
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
				onStart();
				break;
			}
		}
	}

	/**
	 * Called when the very first RUN message is received.
	 */
	public void onStart() {
		getLogger().fine("------- Starting simulation --------");
		getLogger().finer(" -- reseting time");
		getSimuTimer().reset();
		getLogger().fine(() -> " -- Calling onStart on " + getEnvironment());
	}

	@Override
	protected void onEnding() {
		simulationState = SimulationState.PAUSED;
		getLogger().info(() -> "------- Simulation stopped! Time was = " + getSimuTimer());
		getLogger().finest("removing all activators");
		removeAllActivators();
		super.onEnding();
	}

	/**
	 * Changes my state according to a {@link SchedulingMessage} and sends a reply
	 * to the sender as acknowledgment.
	 *
	 * @param m the received message
	 */
	protected void checkMail(final Message m) {
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
	protected void paused() {
		checkMail(waitNextMessage(1000));
	}

	/**
	 * Changes the state of the scheduler
	 *
	 * @param newState the new state
	 */
	protected void setSimulationState(final SimulationState newState) {
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
	 * Defines a default simulation step which is automatically during the
	 * scheduler's life.
	 *
	 * This method should be overridden to define a customized scheduling policy.
	 *
	 * By default, it executes all the activators in the order they have been added,
	 * using {@link Activator#execute(Object...)}, and then increments the
	 * simulation time by one unit. Default implementation is:
	 *
	 * <pre>
	 * logActivationStep();
	 * for (final Activator activator : activators) {
	 * 	executeAndLog(activator);
	 * }
	 * getSimulationTime().addOneTimeUnit();
	 * </pre>
	 *
	 * By default logs are displayed only if {@link #getLogger()} is set above
	 * {@link Level#FINER}.
	 */
	public abstract void doSimulationStep();

	/**
	 * Returns the delay between two simulation steps
	 * 
	 * @return the pause between two steps
	 */
	public int getPause() {
		return pause.get();
	}

	public void setPause(int delay) {
		pause.set(delay);
	}

	/**
	 * @return
	 */
	public Slider getNewPauseSlider() {
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
	 * @return
	 */
	public ToolBar getToolBar() {
		ToolBar tb = ActionUtils.createToolBar(getActions(), ActionTextBehavior.HIDE);
		tb.getItems().add(getNewPauseSlider());
		tb.getItems().add(getTimeLabel());
		return tb;
	}

	public List<Node> getActionListFor() {
		return List.of(ActionUtils.createButton(run, ActionTextBehavior.HIDE), ActionUtils.createButton(step),
				getNewPauseSlider(), getTimeLabel());
	}

	/**
	 * 
	 */
	public Label getTimeLabel() {
		return simuTime.getTimeLabel();
	}

	/**
	 * @param simulationTime the simulationTime to set
	 */
	protected void setSimulationTime(T simulationTime) {
		this.simuTime = simulationTime;
	}

	/**
	 * Returns the {@link SimulationTime} of the current simulation. This is
	 * automatically initialized when the agent is associated with an activator for
	 * the first time. So it stays <code>null</code> if the agent is not related to
	 * a simulation
	 *
	 * @return the simulationTime of the simulation in which the agent participates
	 */
	@Override
	public T getSimuTimer() {
		return simuTime;
	}

	/**
	 * @return the activators
	 */
	public List<Activator> getActivators() {
		return activators;
	}

	public MethodActivator addViewersActivator() {
		MethodActivator v = new MethodActivator(getEngineGroup(), VIEWER_ROLE, "observe");
		addActivator(v);
		return v;
	}

	public MethodActivator addEnvironmentActivator() {
		MethodActivator v = new MethodActivator(getEngineGroup(), ENVIRONMENT_ROLE, "update");
		addActivator(v);
		return v;
	}

	/**
	 * Updates the activators schedule according to their priorities
	 */
	public void updateActivatorsSchedule() {
		Collections.sort(activators, activatorComparator);
	}

	/**
	 * Adds an activator to the simulation engine. This has to be done to make an
	 * activator work properly.
	 *
	 * @param activator an activator.
	 * @since MaDKit 5.0.0.8
	 */
	public void addActivator(final Activator activator) {
		activator.setScheduler(this);
		if (getOrgnization().addOverlooker(this, activator)) {
			activators.add(activator);
			updateActivatorsSchedule();
			getLogger().fine(() -> activator + " added");
		} else
			getLogger().warning(() -> activator + " already added");
	}

	/**
	 * Removes an activator from the simulation engine.
	 *
	 * @param activator an activator.
	 */
	public void removeActivator(final Activator activator) {
		getOrgnization().removeOverlooker(activator);
		activators.remove(activator);
		getLogger().fine(() -> "Activator removed: " + activator);
	}

	public void executeActivators(Object... args) {
		activators.stream().forEach(a -> executeAndLog(a, args));
	}

	/**
	 * Logs the current simulation step value.
	 *
	 * logs are displayed only if {@link #getLogger()} is set above
	 * {@link Level#FINER}.
	 */
	public void logCurrrentStep() {
		getLogger().finer(() -> "Current simulation time -> " + getSimuTimer());
	}

	/**
	 * Triggers the execute method of this <code>activator</code> and logs it using
	 * the {@link Level#FINER} logging level
	 *
	 * @param activator the activator to trigger
	 * @param args      the args that will be passed to the targeted method
	 */
	public void executeAndLog(final Activator activator, Object... args) {
		getLogger().finer(() -> "Activating--> " + activator);
		activator.execute(args);
	}

	/**
	 * Remove all the activators which have been previously added
	 */
	public void removeAllActivators() {
		activators.stream().forEach(a -> getOrgnization().removeOverlooker(a));
		activators.clear();
	}

	/**
	 * A simulation state. The simulation process managed by a scheduler agent can
	 * be in one of the following states:
	 * <ul>
	 * <li>{@link #RUNNING}<br>
	 * The simulation process is running normally.</li>
	 * <li>{@link #STEP}<br>
	 * The scheduler will process one simulation step and then will be in the
	 * {@link #PAUSED} state.</li>
	 * <li>{@link #PAUSED}<br>
	 * The simulation is paused. This is the default state.</li>
	 * </ul>
	 *
	 * @author Fabien Michel
	 * @since MaDKit 5.0
	 */
	public enum SimulationState {
		/**
		 * The simulation process is running normally.
		 */
		RUNNING,

		/**
		 * The scheduler will process one simulation step and then will be in the
		 * {@link #PAUSED} state.
		 */
		STEP,

		/**
		 * The simulation is paused.
		 */
		PAUSED,

		/**
		 * The simulation is ending
		 */
		SHUTDOWN
	}

	static class ActivatorComparator implements Comparator<Activator> {

		@Override
		public int compare(Activator o1, Activator o2) {
			return Integer.compare(o1.getPriority(), o2.getPriority());

		}
	}

	public static void main(String[] args) {
		executeThisAgent();
	}

	/**
	 * @return the shedulingActions
	 */
	public ActionGroup getShedulingActions() {
		if (shedulingActions == null) {
			run = SchedulingAction.RUN.getFxActionFrom(this);
			step = SchedulingAction.STEP.getFxActionFrom(this);
			Collection<Action> actions = new ArrayList<>();
			actions.add(run);
			actions.add(step);
			shedulingActions = new ActionGroup("Scheduling", actions);
		}
		return shedulingActions;
	}

	public List<Action> getActions() {
		return getShedulingActions().getActions();
	}
}
