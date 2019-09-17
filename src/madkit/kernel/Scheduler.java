/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import static madkit.kernel.Scheduler.SimulationState.PAUSED;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import madkit.action.SchedulingAction;
import madkit.gui.AgentFrame;
import madkit.gui.SwingUtil;
import madkit.message.SchedulingMessage;

/**
 * <pre>
 * Scheduler is the core agent for defining multi-agent based simulations.
 *
 * This class defines a generic threaded agent which is in charge of activating the simulated agents using {@link Activator}.
 * {@link Activator} are tool objects which are able to trigger any available method belonging to the agents that have a role within a group.
 *
 * The purpose of this approach is to allow the manipulation of agents regardless of their concrete Java classes.
 *
 * So a scheduler holds a collection of activators that target specific groups and roles and thus allow to define
 * very complex scheduling policies if required. A default behavior is implemented and corresponds to the triggering
 * of the activators according to the order in which they have been added to the scheduler engine using {@link #addActivator(Activator)}.
 *
 * The default state of a scheduler is {@link SimulationState#PAUSED}.
 *
 * The default delay between two simulation steps is 0 milliseconds (max speed).
 *
 * Default GUI components are defined for this agent and they could be easily integrated in any GUI.
 *
 * As of MaDKit 5.3, two different temporal schemes could used:
 * <li>tick-based: The time is represented as a {@link BigDecimal} which is incremented at will. {@link BigDecimal} is used to avoid
 * rounding errors that may happen when working with double values.
 * This is the preferred choice if the simulation is based on simple loop following a discrete time approach.
 *
 * <li> date-based: The time is represented using {@link LocalDateTime}. This is far more convenient when the model refers
 * to a real-world case for which representing usual temporal units such as hours or weeks is required (the default used unit is {@link ChronoUnit#SECONDS}).
 * This mode also allow to handle agents that evolve considering different time scales or during specific period of the day.
 *
 * Those two modes are exclusive and can be selected using the corresponding constructor of the scheduler
 *
 * @author Fabien Michel
 * @since MaDKit 2.0
 * @version 5.3
 * @see Activator
 * @see BigDecimal
 * @see LocalDateTime
 */
public class Scheduler extends Agent {

    /**
     * A simulation state. The simulation process managed by a scheduler agent can be in one of the following states:
     * <ul>
     * <li>{@link #RUNNING}<br>
     * The simulation process is running normally.</li>
     * <li>{@link #STEP}<br>
     * The scheduler will process one simulation step and then will be in the {@link #PAUSED} state.</li>
     * <li>{@link #PAUSED}<br>
     * The simulation is paused. This is the default state.</li>
     * </ul>
     *
     * @author Fabien Michel
     * @since MaDKit 5.0
     * @see #getSimulationState
     */
    public enum SimulationState {
    /**
     * The simulation process is running normally.
     */
    RUNNING,

    /**
     * The scheduler will process one simulation step and then will be in the {@link #PAUSED} state.
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

    private static final Preferences SCHEDULER_UI_PREFERENCES = Preferences.userRoot().node(Scheduler.class.getName());

    private final List<Activator<? extends AbstractAgent>> activators = new ArrayList<>();

    private static final ActivatorComparator activatorComparator = new ActivatorComparator();

    private SimulationState simulationState = SimulationState.PAUSED;

    private Action run, step, speedUp, speedDown;

    // private JLabel timer;
    private int delay;

    /**
     * speed model, especially useful for the GUI
     */
    @SuppressWarnings("serial")
    private final DefaultBoundedRangeModel speedModel = new DefaultBoundedRangeModel(400, 0, 0, 400) {

	@Override
	public void setValue(int n) {
	    super.setValue(n);
	    delay = 400 - getValue();
	    SCHEDULER_UI_PREFERENCES.putInt(getName() + "speed", getValue());
	}
    };

    /**
     * Returns the delay between two simulation steps
     *
     * @return the delay between two simulation steps.
     */
    public int getDelay() {
	return delay;
    }

    /**
     * Sets the delay between two simulation steps. That is the real time pause between two calls of
     * {@link #doSimulationStep()}. Using the Scheduler's GUI, the value can be adjusted from 0 to 400.
     *
     * @param delay
     *            the real time pause between two steps in milliseconds, an integer between 0 and 400: O is max speed.
     */
    public void setDelay(final int delay) {
	speedModel.setValue(speedModel.getMaximum() - delay);
    }

    /**
     * Returns the simulation current tick.
     *
     * @return the gVT
     * @deprecated as of MaDKit 5.3, replaced by {@link #getSimulationTime()}
     */
    public double getGVT() {
	return getSimulationTime().getCurrentTick().doubleValue();
    }

    /**
     * Sets the simulation global virtual time.
     *
     * @param value
     *            the current simulation time
     *
     * @deprecated as of MaDKit 5.3, replaced by {@link #getSimulationTime()}
     */
    public void setGVT(final double value) {
	getSimulationTime().setCurrentTick(value);
    }

    private SimulationTimeModel gvtModel;

    /**
     * Constructs a <code>Scheduler</code> using a tick-based {@link SimulationTime}.
     */
    public Scheduler() {
	setSimulationTime(new TickBasedTime());//This needs to be set first and will be overridden if required
	buildActions();
    }

    /**
     * Constructs a <code>Scheduler</code> using a tick-based that will end the simulation for the specified tick.
     *
     * @param endTick
     *            the tick at which the simulation will automatically stop
     */
    public Scheduler(final double endTick) {
	this();
	getSimulationTime().setEndTick(BigDecimal.valueOf(endTick));
    }

    /**
     * Constructs a <code>Scheduler</code> using a date-based time which relies on {@link LocalDateTime}
     *
     * @param initialDate the date at which the simulation should begin e.g. <code>LocalDateTime.of(1, 1, 1, 0, 0)</code>
     *            the initial date of the simulation
     */
    public Scheduler(final LocalDateTime initialDate) {
	this();
	setSimulationTime(new DateBasedTime(initialDate));
    }

    /**
     * Setup the default Scheduler GUI when launched with the default MaDKit GUI mechanism.
     *
     * @see madkit.kernel.AbstractAgent#setupFrame(AgentFrame)
     */
    @Override
    public void setupFrame(AgentFrame frame) {
	super.setupFrame(frame);
	frame.add(getSchedulerToolBar(), BorderLayout.PAGE_START);
	frame.add(getSchedulerStatusLabel(), BorderLayout.PAGE_END);
//	getSimulationTime().setCurrentTick(getSimulationTime().getCurrentTick());
	frame.getJMenuBar().add(getSchedulerMenu(), 2);
	speedModel.setValue(SCHEDULER_UI_PREFERENCES.getInt(getName() + "speed", speedModel.getValue()));
	setSimulationState(SCHEDULER_UI_PREFERENCES.getBoolean(getName() + "autostart", false) ? SimulationState.RUNNING : SimulationState.PAUSED);
	// TODO make startSimu an action
    }

    /**
     * Adds an activator to the simulation engine.
     * This has to be done to make an activator work properly.
     *
     * @param activator
     *            an activator.
     * @since MaDKit 5.0.0.8
     */
    public void addActivator(final Activator<? extends AbstractAgent> activator) {
	activator.setSimulationTime(getSimulationTime());
	if (kernel.addOverlooker(this, activator)) {
	    if(activator.getPriority() == null) {
		setActivatorPriority(activator,activators.size());
	    }
	    activators.add(activator);
	    Collections.sort(activators, activatorComparator);
	}
	getLogger().fine(() -> "Activator added: " + activator);
    }

    /**
     * Sets the priority of an {@link Activator}.
     *
     * @param activator
     * @param priority
     */
    public void setActivatorPriority(Activator<? extends AbstractAgent> activator, int priority) {
	activator.setPriority(priority);
	if(activators.contains(activator)) {
	    Collections.sort(activators, activatorComparator);
	}
    }

    /**
     * Removes an activator from the simulation engine.
     *
     * @param activator
     *            an activator.
     */
    public void removeActivator(final Activator<? extends AbstractAgent> activator) {
	kernel.removeOverlooker(this, activator);
	activators.remove(activator);
	getLogger().fine(() -> "Activator removed: " + activator);
    }

    /**
     * Defines a default simulation loop which is automatically during the scheduler's life.
     *
     * This method should be overridden to define a customized scheduling policy.
     *
     * By default, it executes all the activators in the order they have been added, using
     * {@link Activator#execute(Object...)}, and then increments the simulation time by one unit. Default implementation is:
     *
     * <pre>
     * logActivationStep();
     * for (final Activator<? extends AbstractAgent> activator : activators) {
     *     executeAndLog(activator);
     * }
     * getSimulationTime().addOneTimeUnit();
     * </pre>
     *
     * By default logs are displayed only if {@link #getLogger()} is set above {@link Level#FINER}.
     */
    public void doSimulationStep() {
	logActivationStep();
	for (final Activator<? extends AbstractAgent> activator : activators) {
	    executeAndLog(activator);
	}
	getSimulationTime().addOneTimeUnit();
    }

    /**
     * Logs the current simulation step value.
     *
     * logs are displayed only if {@link #getLogger()} is set above {@link Level#FINER}.
     */
    public void logActivationStep() {
	getLogger().finer(() -> "Current simulation step -> " + getSimulationTime());
    }

    /**
     * Triggers the execute method of this <code>activator</code> and logs it using the {@link Level#FINER} logging level
     *
     * @param activator the activator to trigger
     * @param args the args that will be passed to the targeted method
     */
    public void executeAndLog(final Activator<? extends AbstractAgent> activator, Object... args) {
	getLogger().finer(() -> "Activating--> " + activator);
	activator.execute(args);
    }

    @Override
    protected void end() {
	simulationState = PAUSED;
	getLogger().fine(() -> "Simulation stopped at time = " + getSimulationTime());
    }

    /**
     * The state of the simulation.
     *
     * @return the state in which the simulation is.
     * @see SimulationState
     */
    public SimulationState getSimulationState() {
	return simulationState;
    }

    /**
     * Changes the state of the scheduler
     *
     * @param newState
     *            the new state
     */
    protected void setSimulationState(final SimulationState newState) {// TODO proceedEnumMessage
	if (simulationState != newState) {
	    simulationState = newState;
	    switch (simulationState) {
	    case STEP:
		run.setEnabled(true);
		break;
	    case PAUSED:
		run.setEnabled(true);
		break;
	    case RUNNING:
		run.setEnabled(false);
		break;
	    case SHUTDOWN:
		run.setEnabled(false);
		break;
	    default:// impossible
		logLifeException(new Exception("state not handle : " + newState.toString()));
	    }
	}
    }

    /**
     * Scheduler's default behavior. default code is:
     *
     * <pre>
     * while (isAlive()) {
     *     if (GVT &gt; getSimulationDuration()) {
     * 	if (logger != null)
     * 	    logger.info(&quot;Simulation has reached end time -> &quot; + getSimulationDuration());
     * 	return;
     *     }
     *     pause(getDelay());
     *     checkMail(nextMessage());
     *     switch (getSimulationState()) {
     *     case RUNNING:
     * 	doSimulationStep();
     * 	break;
     *     case PAUSED:
     * 	paused();
     * 	break;
     *     case STEP:
     * 	simulationState = PAUSED;
     * 	doSimulationStep();
     * 	break;
     *     case SHUTDOWN:
     * 	return; // shutdown
     *     default:
     * 	getLogger().severe(&quot;state not handled &quot; + getSimulationState);
     *     }
     * }
     * </pre>
     *
     * @see madkit.kernel.Agent#live()
     */
    @Override
    protected void live() {
	while (isAlive()) {
	    if (getSimulationTime().hasReachedEndTime()) {
		getLogger().info(() -> "Simulation has reached end time -> " + getSimulationTime());
		return;
	    }
	    pause(delay);
	    checkMail(nextMessage());
	    switch (simulationState) {
	    case RUNNING:
		doSimulationStep();
		break;
	    case PAUSED:
		paused();
		break;
	    case STEP:
		simulationState = PAUSED;
		doSimulationStep();
		break;
	    case SHUTDOWN:
		return; // shutdown
	    default:
		getLogger().severeLog("state not handled " + simulationState);
	    }
	}
    }

    /**
     * Changes my state according to a {@link SchedulingMessage} and sends a reply to the sender as acknowledgment.
     *
     * @param m
     *            the received message
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
		case SPEED_UP:
		    speedModel.setValue(speedModel.getValue() - 50);
		    break;
		case SPEED_DOWN:
		    speedModel.setValue(speedModel.getValue() + 50);
		    break;
		}
		if (m.getSender() != null) {
		    sendReply(m, m);
		}
	    }
	    catch(ClassCastException e) {
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
     * @see madkit.kernel.AbstractAgent#terminate()
     */
    @Override
    final void terminate() {
	removeAllActivators();
	super.terminate();
    }

    /**
     * Remove all the activators which have been previously added
     */
    public void removeAllActivators() {
	for (final Activator<? extends AbstractAgent> a : activators) {
	    kernel.removeOverlooker(this, a);
	}
	activators.clear();
    }

    /**
     * Sets the simulation time for which the scheduler should end the simulation.
     *
     * @deprecated as of MDK 5.3, replaced by {@link #getSimulationTime()} available methods
     *
     * @param endTime
     *            the end time to set
     */
    @Deprecated
    public void setSimulationDuration(final double endTime) {
	getSimulationTime().setEndTick(BigDecimal.valueOf(endTime));
    }

    /**
     * @return the simulationDuration
     *
     * @deprecated as of MDK 5.3, replaced by {@link #getSimulationTime()} available methods
     */
    public double getSimulationDuration() {
	return getSimulationTime().getEndTick().doubleValue();
    }

    private void buildActions() {
	run = SchedulingAction.RUN.getActionFor(this);
	step = SchedulingAction.STEP.getActionFor(this);
	speedUp = SchedulingAction.SPEED_UP.getActionFor(this);
	speedDown = SchedulingAction.SPEED_DOWN.getActionFor(this);
    }

    /**
     * Returns a toolbar which could be used in any GUI.
     *
     * @return a toolBar controlling the scheduler's actions
     */
    public JToolBar getSchedulerToolBar() {
	final JToolBar toolBar = new JToolBar("scheduler toolbar");
	toolBar.add(run);
	toolBar.add(step);
	final JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	p.setBorder(new TitledBorder("speed"));
	final JSlider sp = new JSlider(speedModel);
	sp.addMouseWheelListener(new MouseWheelListener() {

	    @Override
	    public void mouseWheelMoved(MouseWheelEvent e) {
		int move = -e.getWheelRotation();
		if (sp.getValue() < 398) {
		    move *= 10;
		}
		move = (move + sp.getValue()) > sp.getMaximum() ? sp.getMaximum() : move + sp.getValue();
		sp.setValue(move);
		sp.getChangeListeners()[0].stateChanged(new ChangeEvent(this));
	    }
	});
	sp.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(ChangeEvent e) {
		updateToolTip(p, sp);
	    }

	});
	updateToolTip(p, sp);
	// p.setPreferredSize(new Dimension(150, 25));
	p.add(sp);
	// toolBar.addSeparator();
	// toolBar.add(Box.createRigidArea(new Dimension(40,5)));
	// toolBar.add(Box.createHorizontalGlue());
	toolBar.add(p);
	// toolBar.add(getGVTLabel());
	SwingUtil.scaleAllAbstractButtonIconsOf(toolBar, 24);
	return toolBar;
    }

    /**
     * @param p
     * @param sp
     */
    private void updateToolTip(final JPanel p, final JSlider sp) {
	final String text = "pause = " + getDelay() + " ms";
	sp.setToolTipText(text);
	p.setToolTipText(text);
    }

    /**
     * Returns a menu which could be used in any GUI.
     *
     * @return a menu controlling the scheduler's actions
     */
    public JMenu getSchedulerMenu() {
	JMenu myMenu = new JMenu("Scheduling");
	myMenu.setMnemonic(KeyEvent.VK_S);
	myMenu.add(run);
	myMenu.add(step);
	myMenu.add(speedUp);
	myMenu.add(speedDown);
	final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("autostart", SCHEDULER_UI_PREFERENCES.getBoolean(getName() + "autostart", false));
	menuItem.addChangeListener(e -> SCHEDULER_UI_PREFERENCES.putBoolean(getName() + "autostart", menuItem.isSelected()));
	myMenu.add(menuItem);
	return myMenu;
    }

    /**
     * Returns a label giving some information on the simulation process
     *
     * @return a label giving some information on the simulation process
     */
    public JLabel getSchedulerStatusLabel() {
	if (gvtModel == null) {
	    gvtModel = new SimulationTimeModel();
	}
	@SuppressWarnings("serial")
	SimulationTimeJLabel timer = new SimulationTimeJLabel() {

	    @Override
	    public void update(Observable o, Object arg) {
		setText(arg+"\t\t\t  -  " + simulationState);
	    }
	};
//	timer.setText("GVT");
	gvtModel.addObserver(timer);
	timer.setBorder(new EmptyBorder(4, 4, 4, 4));
	timer.setHorizontalAlignment(JLabel.LEADING);
	return timer;
    }

    /**
     * Returns a label giving the simulation time
     *
     * @return a label giving the simulation time
     */
    public JLabel getGVTLabel() {
	if (gvtModel == null) {
	    gvtModel = new SimulationTimeModel();
	}
	final SimulationTimeJLabel timer = new SimulationTimeJLabel();
	gvtModel.addObserver(timer);
	timer.setBorder(new EmptyBorder(4, 4, 4, 4));
	timer.setHorizontalAlignment(JLabel.LEADING);
	return timer;
    }

    public interface SimulationTime{

	final static String TICK_BASED_MODE_REQUIRED = "This can only be used in a tick-based simulation. See Scheduler constructors doc";

	final static String DATE_BASED_MODE_REQUIRED = "This can only be used in a date-based simulation. See Scheduler constructors doc";

	/**
	 * Sets the current tick a the simulation to the specified value
	 *
	 * @param value
	 */
	public default void setCurrentTick(@SuppressWarnings("unused") BigDecimal value){
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Shortcut for <code>setCurrentTick(BigDdecimal.valuof(value));</code>
	 *
	 * @param value
	 *            specifies the current tick value
	 */
	public default void setCurrentTick(@SuppressWarnings("unused") double value){
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Shortcut for <code>setCurrentDate(getCurrentDate().plus(amountToAdd, unit));</code>
	 *
	 * @param date  the date to set as current date
	 *
	 */
	public default void setCurrentDate(LocalDateTime date) {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 * Gets the current tick
	 *
	 * @return the current tick of the simulation
	 */
	public default BigDecimal getCurrentTick() {
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Gets the current date
	 *
	 * @return the current date of the simulation
	 */
	public default LocalDateTime getCurrentDate() {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 * Adds one time unit to the simulation current time
	 */
	abstract public void addOneTimeUnit();

	/**
	 * Checks if the simulation has reached the specified end time
	 *
	 * @return <code>true</code> if the simulation should be stopped.
	 */
	abstract public boolean hasReachedEndTime();

	/**
	 * Shortcut for <code>setCurrentTick(getCurrentTick().add(delta));</code>
	 *
	 * @param delta specifies how much time should be added
	 */
	public default void incrementCurrentTick(BigDecimal delta) {
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Shortcut for <code>setCurrentTick(getCurrentTick().add(BigDecimal.valueOf(delta)));</code>
	 *
	 * @param delta specifies how much time should be added
	 */
	public default void incrementCurrentTick(double delta) {
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Shortcut for <code>setCurrentDate(getCurrentDate().plus(amountToAdd, unit));</code>
	 *
	 * @param amountToAdd  the amount of the unit to add to the result, may be negative
	 * @param unit  the unit of the amount to add, not null
	 *
	 */
	public default void incrementCurrentDate(long amountToAdd, ChronoUnit unit) {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 *
	 * @param amountToAdd  the amount of default temporal unit to add
	 * @see #setDefaultTemporalUnit(ChronoUnit)
	 */
	public default void incrementCurrentDate(long amountToAdd) {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 * Sets the default temporal unit which will be used by {@link #addOneTimeUnit()}
	 * and {@link #incrementCurrentDate(long)} in a date-based mode
	 *
	 * @param unit a temporal unit as defined in {@link ChronoUnit}
	 */
	public default void setDefaultTemporalUnit(ChronoUnit unit) {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 * Gets the default temporal unit which will be used by {@link #addOneTimeUnit()}
	 * and {@link #incrementCurrentDate(long)} in a date-based mode
	 *
	 */
	public default ChronoUnit getDefaultTemporalUnit() {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}

	/**
	 * returns the tick for which the simulation should end.
	 *
	 * @return the endTick
	 */
	public default BigDecimal getEndTick() {
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}


	/**
	 * Sets the tick at which the simulation should end
	 *
	 * @param endTick the endTick to set
	 */
	public default void setEndTick(BigDecimal endTick) {
	    throw new UnsupportedOperationException(TICK_BASED_MODE_REQUIRED);
	}

	/**
	 * Sets the date at which the simulation should stop
	 *
	 * @param endDate the date at which the simulation should stop
	 */
	public default void setEndDate(LocalDateTime endDate) {
	    throw new UnsupportedOperationException(DATE_BASED_MODE_REQUIRED);
	}
    }

    abstract class SimuTime implements SimulationTime{
	protected void updateUIs() {
	    if (gvtModel != null)
		gvtModel.notifyObservers(this);
	}
    }

    /**
     * This class encapsulates a the time of the simulation. Its purpose is that it can be
     * passed across objects without problem. That is, {@link BigDecimal} is immutable and therefore creates a new instance
     * for each modification.
     *
     * @author Fabien Michel
     * @since MaDKit 5.3
     * @see Scheduler LocalDateTime
     */
    final class DateBasedTime extends SimuTime {

	private LocalDateTime currentDate;
	private LocalDateTime endDate;
	private ChronoUnit defaultUnit;

	/**
	/**
	 * Creates a date-based instance using a specific {@link LocalDateTime} as starting point.
	 *
	 * @param initialDate
	 *            a {@link LocalDateTime} to start with
	 * @see LocalDateTime
	 */
	DateBasedTime(LocalDateTime initialDate) {
	    currentDate = initialDate;
	    endDate = LocalDateTime.MAX;
	    defaultUnit = ChronoUnit.SECONDS;
	}

	/**
	 * Creates a date-based time which value is <code>LocalDateTime.of(1, 1, 1, 0, 0)</code>;
	 *
	 */
	DateBasedTime() {
	    this(LocalDateTime.of(1, 1, 1, 0, 0));
	}

	public void setCurrentDate(LocalDateTime date) {
	    this.currentDate = date;
	    updateUIs();
	}

	public LocalDateTime getCurrentDate() {
	    return currentDate;
	}

	@Override
	public void setEndDate(LocalDateTime endDate) {
	    this.endDate = endDate;
	}

	public boolean hasReachedEndTime() {
	    return endDate.compareTo(currentDate) < 0;
	}

	public void incrementCurrentDate(long amountToAdd, ChronoUnit unit) {
	    setCurrentDate(getCurrentDate().plus(amountToAdd, unit));
	}

	public void incrementCurrentDate(long amountToAdd) {
	    incrementCurrentDate(amountToAdd, defaultUnit);
	}

	@Override
	public String toString() {
		return currentDate.toString();
	}

	@Override
	public void setDefaultTemporalUnit(ChronoUnit unit) {
	    defaultUnit = unit;
	}

	@Override
	public ChronoUnit getDefaultTemporalUnit() {
	    return defaultUnit;
	}

	@Override
	public void addOneTimeUnit() {
	    incrementCurrentDate(1, defaultUnit);
	}
    }

    final class TickBasedTime extends SimuTime {

	private BigDecimal currentTick;
	private BigDecimal endTick;

	/**
	 * Creates a tick-based time whose initial tick value is {@link BigDecimal#ZERO};
	 *
	 */
	TickBasedTime() {
	    currentTick = BigDecimal.ZERO;
	    endTick = BigDecimal.valueOf(Double.MAX_VALUE);
	}

	public void setCurrentTick(BigDecimal value) {
	    this.currentTick = value;
	    updateUIs();
	}

	@Override
	public void incrementCurrentTick(double delta) {
	    incrementCurrentTick(BigDecimal.valueOf(delta));
	}

	public void incrementCurrentTick(BigDecimal delta) {
	    setCurrentTick(getCurrentTick().add(delta));
	}

	public BigDecimal getCurrentTick() {
	    return currentTick;
	}

	public void setEndTick(BigDecimal endTick) {
	    this.endTick = endTick;
	}

	public boolean hasReachedEndTime() {
	    return endTick.compareTo(currentTick) < 0;
	}

	/**
	 * Shortcut for <code>setCurrentTick(getCurrentTick().add(delta));</code>
	 *
	 * @param delta
	 *            specifies how much time should be added
	 */
	public void addDeltaTime(BigDecimal delta) {
	    setCurrentTick(getCurrentTick().add(delta));
	}

	/**
	 * @return the endTick
	 */
	public BigDecimal getEndTick() {
	    return endTick;
	}

	@Override
	public String toString() {
	    return currentTick.toString();
	}

	@Override
	public void addOneTimeUnit() {
	    addDeltaTime(BigDecimal.ONE);
	}
    }

    final class SimulationTimeModel extends Observable {

	@Override
	public void notifyObservers(Object arg) {
	    setChanged();
	    super.notifyObservers(arg);
	}
    }
}

class ActivatorComparator implements Comparator<Activator<? extends AbstractAgent>>{
	@Override
	public int compare(Activator<? extends AbstractAgent> o1, Activator<? extends AbstractAgent> o2) {
	    return Integer.compare(o1.getPriority(), o2.getPriority());

	}
}



class SimulationTimeJLabel extends JLabel implements Observer {

    private static final long serialVersionUID = 2320718202738802489L;

    @Override
    public void update(Observable o, Object arg) {
	setText(arg.toString());
    }

}
