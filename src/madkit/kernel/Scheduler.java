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
import java.time.temporal.TemporalUnit;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
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
 * This class defines a generic threaded scheduler agent. It holds a collection of activators. The default state of a
 * scheduler is {@link SimulationState#PAUSED}. The default delay between two steps is 0 ms (max speed).
 * 
 * @author Fabien Michel
 * @since MaDKit 2.0
 * @version 5.2
 * @see Activator
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

    private final Set<Activator<? extends AbstractAgent>> activators = new LinkedHashSet<>();

    private SimulationState simulationState = SimulationState.PAUSED;

    private Action run, step, speedUp, speedDown;

    // private JLabel timer;
    private int delay;

    /**
     * @deprecated as of MaDKit 5.3, replaced by {@link #getSimulationTime()}
     */
    private double GVT = 0; // simulation global virtual time

    /**
     * specify the delay between 2 steps
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
     * Returns the simulation global virtual time.
     * 
     * @return the gVT
     * @deprecated
     */
    public double getGVT() {
	return GVT;
    }

    /**
     * Sets the simulation global virtual time.
     * 
     * @param GVT
     *            the actual simulation time
     * 
     * @deprecated
     */
    public void setGVT(final double GVT) {
	this.GVT = GVT;
	if (gvtModel != null) {
	    gvtModel.notifyObservers((int) GVT);
	}
    }

    private double simulationDuration;

    private SimulationTimeModel gvtModel;

    /**
     * Construct a <code>Scheduler</code> using a tick-based {@link SimulationTime}.
     */
    public Scheduler() {
	buildActions();
	setSimulationTime(new SimulationTime());
	getSimulationTime().setSimulationEnd(BigDecimal.valueOf(Double.MAX_VALUE));
    }

    /**
     * Constructor specifying the tick at which the simulation should end.
     * 
     * @param endTick
     *            the tick at which the simulation will automatically stop
     */
    @Deprecated
    public Scheduler(final double endTick) {
	this();
	getSimulationTime().setSimulationEnd(BigDecimal.valueOf(endTick));
	// simulationTime = new SimulationTime();
    }

    /**
     * Constructor specifying the "human" actualDate at which the simulation should start.
     * 
     * @param endTick
     *            the initial date of the simulation
     */
    public Scheduler(final LocalDateTime initialDate) {
	this();
	setSimulationTime(new SimulationTime(initialDate));
	getSimulationTime().setSimulationEnd(LocalDateTime.MAX);
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
	getSimulationTime().setActualTick(getSimulationTime().getActualTime());
	frame.getJMenuBar().add(getSchedulerMenu(), 2);
	speedModel.setValue(SCHEDULER_UI_PREFERENCES.getInt(getName() + "speed", speedModel.getValue()));
	setSimulationState(SCHEDULER_UI_PREFERENCES.getBoolean(getName() + "autostart", false) ? SimulationState.RUNNING : SimulationState.PAUSED);
	// TODO make startSimu an action
    }

    /**
     * Adds an activator to the kernel engine. This has to be done to make an activator work properly
     * 
     * @param activator
     *            an activator.
     * @since MaDKit 5.0.0.8
     */
    public void addActivator(final Activator<? extends AbstractAgent> activator) {
	activator.setSimulationTime(getSimulationTime());
	if (kernel.addOverlooker(this, activator)) {
	    activators.add(activator);
	}
	getLogger().fine(() -> "Activator added: " + activator);
    }

    /**
     * Removes an activator from the kernel engine.
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
     * Executes all the activators in the order they have been added, using {@link Activator#execute(Object...)}, and then
     * increments the simulation time of this scheduler by one unit. This also automatically calls the multicore mode of the
     * activator if it is set so. This method should be overridden to define customized scheduling policy. So default
     * implementation is :
     * 
     * <pre>
     * <tt>@Override</tt>
     * 	public void doSimulationStep() {
     * 		if (logger != null) {
     * 			logger.finer("Doing simulation step " + GVT);
     * 		}
     * 		for (final Activator<? extends AbstractAgent> activator : activators) {
     * 			if (logger != null)
     * 				logger.finer("Activating\n--------> " + activator);
     * 			activator.execute();
     * 		}
     * 		simulationTime.addDeltaTime(BigDecimal.ONE);
     * </pre>
     */
    public void doSimulationStep() {
	getLogger().finer(() -> "Doing step timestamped " + getSimulationTime().getActualTime());
	for (final Activator<? extends AbstractAgent> activator : activators) {
	    executeAndLog(activator);
	    // try {
	    // } catch (SimulationException e) {//TODO is it better ?
	    // setSimulationState(SimulationState.SHUTDOWN);
	    // getLogger().log(Level.SEVERE, e.getMessage(), e);
	    // }
	}
	// setGVT(GVT + 1);
	getSimulationTime().addDeltaTime(BigDecimal.ONE);
    }

    /**
     * Triggers the execute method of this <code>activator</code> and logs it using the {@link Level#FINER} logging level
     * 
     * @param activator the activator to trigger
     * @param args the args that will be passed to the targeted method
     */
    public void executeAndLog(final Activator<? extends AbstractAgent> activator, Object... args) {
	getLogger().finer(() -> "Activating --------> " + activator);
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
	    if (getSimulationTime().hasPassedEnd()) {
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
     * @deprecated as of MDK 5.3, replaced by
     * 
     * @param endTime
     *            the end time to set
     */
    @Deprecated 
    public void setSimulationDuration(final double endTime) {
	this.simulationDuration = endTime;
	getSimulationTime().setSimulationEnd(BigDecimal.valueOf(endTime));
    }
    
    /**
     * @return the simulationDuration
     */
    @Deprecated
    public double getSimulationDuration() {
	return simulationDuration;
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
	timer.setText("GVT");
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

    /**
     * This class encapsulates a the time of the simulation. Its purpose is that it can be
     * passed across objects without problem. That is, {@link BigDecimal} is immutable and therefore creates a new instance
     * for each modification.
     * 
     * @author Fabien Michel
     * @since MaDKit 5.3
     * @see Scheduler BigDecimal
     */
    public class SimulationTime {

	private BigDecimal actualTick;
	private BigDecimal endTick;
	
	private LocalDateTime actualDate;
	private LocalDateTime endDate;

	/**
	/**
	 * Creates a calendar-based instance using a specific {@link LocalDateTime}
	 * 
	 * @param initialDate
	 *            a {@link LocalDateTime} to start with
	 * @see LocalDateTime
	 */
	SimulationTime(LocalDateTime initialDate) {
	    this(BigDecimal.ZERO, initialDate);
	}

	/**
	 * Creates a tick-based instance whose initial tick value is {@link BigDecimal#ZERO};
	 * 
	 */
	SimulationTime() {
	    this(BigDecimal.ZERO, null);
	}

	private SimulationTime(BigDecimal initialTick, LocalDateTime initialDate) {
	    actualTick = initialTick;
	    actualDate = initialDate;
	}

	public void setActualTick(BigDecimal actualTick) {
	    this.actualTick = actualTick;
	    updateUIs();
	}

	public void setActualDate(LocalDateTime actualDate) {
	    this.actualDate = actualDate;
	    updateUIs();
	}

	private void updateUIs() {
	    if (gvtModel != null)
		gvtModel.notifyObservers(this);
	}

	public BigDecimal getActualTime() {
	    return actualTick;
	}
	
	public LocalDateTime getActualDate() {
	    return actualDate;
	}
	
	public void setSimulationEnd(BigDecimal endTick) {
	    this.endTick = endTick;
	}

	public void setSimulationEnd(LocalDateTime endDate) {
	    this.endDate = endDate;
	    this.endTick = null;
	}
	
	public boolean hasPassedEnd() {
	    if(endTick != null) {
		return endTick.compareTo(actualTick) < 0;
	    }
	    return endDate.compareTo(actualDate) < 0;
	}

	/**
	 * Shortcut for <code>setActualTime(getActualTime().add(delta));</code>
	 * 
	 * @param delta
	 *            specifies how much time should be added
	 * @return the new actual time
	 */
	public BigDecimal addDeltaTime(BigDecimal delta) {
	    setActualTick(getActualTime().add(delta));
	    return getActualTime();
	}
	
	/**
	 * Shortcut for <code>setActualDate(getActualDate().plus(amountToAdd, unit));</code>
	 * 
	 * @param amountToAdd  the amount of the unit to add to the result, may be negative
	 * @param unit  the unit of the amount to add, not null
	 * 
	 * @return the new current date
	 */
	public LocalDateTime addDuration(long amountToAdd, TemporalUnit unit) {
	    setActualDate(getActualDate().plus(amountToAdd, unit));
	    return getActualDate();
	}
	
	/**
	 * @return the endTick
	 */
	public BigDecimal getEndTick() {
	    return endTick;
	}

	
	/**
	 * @param endTick the endTick to set
	 */
	public void setEndTick(BigDecimal endTick) {
	    this.endTick = endTick;
	}
	
	@Override
	public String toString() {
	    if(actualDate != null) {
		return actualDate.toString();
	    }
	    return actualTick.toString();
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


class SimulationTimeJLabel extends JLabel implements Observer {

    private static final long serialVersionUID = 2320718202738802489L;

    @Override
    public void update(Observable o, Object arg) {
	setText(arg.toString());
    }

}
