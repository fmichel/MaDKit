/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.kernel.Scheduler.State.PAUSED;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
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
import madkit.message.SchedulingMessage;

/**
 * This class defines a generic threaded scheduler agent. It holds a collection
 * of activators. The default state of a scheduler is {@link State#PAUSED}.
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @since MadKit 2.0
 * @version 5.1
 */
public class Scheduler extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2224235651899852429L;

	/**
	 * A simulation state. The simulation process managed by a scheduler agent
	 * can be in one of the following states:
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
	 * @since MadKit 5.0
	 * @see #getSimulationState
	 */
	public enum State {

		/**
		 * The simulation process is running normally.
		 */
		RUNNING,

		/**
		 * The scheduler will process one simulation step and then will be in the
		 * {@link #PAUSED} state.
		 * 
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

	private State simulationState = State.PAUSED;

	final private Set<Activator<? extends AbstractAgent>> activators = new LinkedHashSet<Activator<? extends AbstractAgent>>();
	private int delay = 20;

	Action run,step,speedUp,speedDown;

	JLabel timer;
	private JSlider speedSlider;

	/**
	 * Returns the delay between two simulation steps
	 * @return the delay between two simulation steps.
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Sets the delay between two simulation steps. That is the pause time between to call to {@link #doSimulationStep()}
	 * 
	 * @param delay the delay to set
	 */
	public void setDelay(final int delay) {
		this.delay = delay;
		if (getSpeedSlider() != null) {
			getSpeedSlider().setValue(delay);
		}
	}

	private double GVT = 0; // simulation global virtual time

	/**
	 * Returns the simulation global virtual time.
	 * @return the gVT
	 */
	public double getGVT() {
		return GVT;
	}

	/**
	 * Sets the simulation global virtual time.
	 * 
	 * @param gVT the gVT to set
	 */
	public void setGVT(final double gVT) {
		GVT = gVT;
		updateStatusDisplay();
	}

	private double simulationDuration;
	private double startTime;

	/**
	 * This constructor is equivalent to 
	 * <code>Scheduler(0, Double.MAX_VALUE)</code>
	 */
	public Scheduler() {
		this(0, Double.MAX_VALUE);
	}

//	public Scheduler(boolean multicore) {
//		this(0, Double.MAX_VALUE);
//	}

	/**
	 * This constructor is equivalent to 
	 * <code>Scheduler(0, endTime)</code>
	 */
	public Scheduler(final double endTime) {
		this(0, endTime);
	}

//	public Scheduler(final double startTime, final double endTime) {
//		buildActions();
//		setSimulationDuration(endTime);
//		this.setStartTime(startTime);
//	}

//	@Override
//	protected void activate() {
//		if(logger != null)
//			logger.talk("\n\tHi human !\n\n I am an instance of the madkit.kernel.Scheduler class.\n I am specialized in simulation scheduling.\n I use activators on the artificial society\n to trigger agents' behaviors and simulate artificial worlds.\n You can extend me to create your own simulations !");
//	}
	
	/**
	 * Constructor specifying the global times at which the simulation starts and ends.
	 * 
	 * @param startTime the first step global virtual time
	 * @param endTime the time at which the simualtion will automatically stop
	 */
	public Scheduler(final double startTime, final double endTime) {
		buildActions();
		setSimulationDuration(endTime);
		this.setStartTime(startTime);
	}

	/**
	 * Setup the default Scheduler GUI when launched with the default MadKit
	 * GUI mechanism.
	 * 
	 * @see madkit.kernel.AbstractAgent#setupFrame(javax.swing.JFrame)
	 * @since MadKit 5.0.0.8
	 */
	@Override
	public void setupFrame(JFrame frame) {
		super.setupFrame(frame);
		frame.add(getSchedulerToolBar(), BorderLayout.PAGE_START);
		frame.add(getSchedulerStatusLabel(), BorderLayout.PAGE_END);
		updateStatusDisplay();
		frame.validate();
		frame.getJMenuBar().add(getSchedulerMenu());
	}

	private void updateStatusDisplay() {
		if (timer != null)
			timer.setText("Simulation " + getSimulationState() + ", time is " + getGVT());
	}

	public void addActivator(final Activator<? extends AbstractAgent> activator) {
		if (kernel.addOverlooker(this, activator))
			activators.add(activator);
		if (logger != null)
			logger.fine("Activator added: " + activator);
	}

	public void removeActivator(final Activator<? extends AbstractAgent> activator) {
		kernel.removeOverlooker(this, activator);
		activators.remove(activator);
	}

	public void doSimulationStep() {
		if (logger != null) {
			logger.finer("Doing a simulation step");
			for (final Activator<? extends AbstractAgent> activator : activators) {
				if (logger != null)
					logger.finest("Activating " + activator);
				triggerActivator(activator);
			}
		} else {
			for (final Activator<? extends AbstractAgent> activator : activators) {
				triggerActivator(activator);
			}
		}
		setGVT(GVT + 1);
	}
	
	/**
	 * Triggers the activator's execution process. This process automatically
	 * calls the multicore mode of the activator if it is set so.
	 * 
	 * @param activator the activator to execute.
	 */
	public void triggerActivator(final Activator<? extends AbstractAgent> activator){
		if(activator.isMulticoreModeOn()){
			activator.multicoreExecute();
		}
		else{
			activator.execute();
		}
	}

	@Override
	protected void end() {
		simulationState = PAUSED;
		if (logger != null)
			logger.info("Simulation stopped !");
	}

	/**
	 * The state of the simualtion.
	 * 
	 * @return the state in which the simulation is.
	 * @see State
	 */
	public State getSimulationState() {
		return simulationState;
	}

	/**
	 * Changes the state of the scheduler accordingly
	 * @param newState the new state
	 */
	protected void setState(final State newState) {
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
			default://impossible
				logLifeException(new Exception("state not handle : "+newState.toString()));
			}
		}
	}

	public void live() {
		while (isAlive()) {
			if (GVT > simulationDuration) {
				if (logger != null)
					logger.fine("Quitting: Simulation has reached end time " + getGVT());
				return; // TODO logging
			}
			if (delay == 0)
				Thread.yield();
			else
				pause(delay);
			checkMail(nextMessage());
			switch (simulationState) {
			case RUNNING:
				doSimulationStep();
				break;
			case PAUSED:
				updateStatusDisplay();
				paused();
				break;
			case STEP:
				simulationState = PAUSED;
				doSimulationStep();
				break;
			case SHUTDOWN:
				return; // shutdown
			default:
				getLogger().severe("state not handled "+simulationState);
			}
		}
	}

	protected void checkMail(final Message m) {
		if (m != null) {
			try {
				switch (((SchedulingMessage) m).getCode()) {
				case RUN:
					setState(State.RUNNING);
					break;
				case STEP:
					setState(State.STEP);
					break;
				case SHUTDOWN:
					setState(State.SHUTDOWN);
					break;
				}
				if (m.getSender() != null) {
					sendReply(m, m);
				}
			} catch (ClassCastException e) {
				if (logger != null)
					logger.info("I received a message that I cannot understand" + m);
			}
		}
	}

	private void paused() {
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
	 * 
	 */
	public void removeAllActivators() {
		for (final Activator<? extends AbstractAgent> a : activators) {
			kernel.removeOverlooker(this, a);
		}
		activators.clear();
	}

	/**
	 * @param simulationDuration
	 *           the simulationDuration to set
	 */
	public void setSimulationDuration(final double simulationDuration) {
		this.simulationDuration = simulationDuration;
	}

	/**
	 * @return the simulationDuration
	 */
	public double getSimulationDuration() {
		return simulationDuration;
	}

	/**
	 * @param startTime
	 *           the startTime to set
	 */
	public void setStartTime(final double startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
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

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new TitledBorder("speed"));
		setSpeedSlider(new JSlider(0, 1000, 20));
		getSpeedSlider().setPaintTicks(true);
		getSpeedSlider().setPaintLabels(false);
		getSpeedSlider().setMajorTickSpacing(500);
		getSpeedSlider().setMinorTickSpacing(100);
		getSpeedSlider().setInverted(true);
		getSpeedSlider().setSnapToTicks(false);
		getSpeedSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!getSpeedSlider().getValueIsAdjusting()) {
					setDelay(getSpeedSlider().getValue());
				}
			}
		});
		getSpeedSlider().addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				getSpeedSlider().setValue(e.getWheelRotation() * 50 + getSpeedSlider().getValue());
			}
		});
		// s.getAccessibleContext().setAccessibleName(resourceManager.getString("SliderDemo.plain"));
		// s.getAccessibleContext().setAccessibleDescription(resourceManager.getString("SliderDemo.a_plain_slider"));

		// p.add(Box.createRigidArea(new Dimension(5,5)));
		p.setPreferredSize(new Dimension(150, 50));
		p.add(getSpeedSlider());
		toolBar.addSeparator();
		// toolBar.add(Box.createRigidArea(new Dimension(40,5)));
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(p);

		// timer.setSize(30, 20);
		return toolBar;
	}

	/**
	 * Returns a menu which could be used in any GUI.
	 * 
	 * @return a  menu controlling the scheduler's actions
	 */
	public JMenu getSchedulerMenu() {
		JMenu myMenu = new JMenu("Scheduling");
		myMenu.setMnemonic(KeyEvent.VK_S);
		myMenu.add(run);
		myMenu.add(step);
		myMenu.add(speedUp);
		myMenu.add(speedDown);
		return myMenu;
	}

	/**
	 * Returns a label giving some information on the simulation process
	 * @return a label giving some information on the simulation process
	 */
	public JLabel getSchedulerStatusLabel() {
		timer = new JLabel();
		timer.setBorder(new EmptyBorder(4, 4, 4, 4));
		timer.setHorizontalAlignment(JLabel.LEADING);
		return timer;
	}

	/**
	 * @param speedSlider the speedSlider to set
	 */
	private void setSpeedSlider(JSlider speedSlider) {
		this.speedSlider = speedSlider;
	}

	/**
	 * @return the speedSlider
	 */
	public JSlider getSpeedSlider() {
		return speedSlider;
	}
	
}
