/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.kernel.Scheduler.SimulationState.PAUSED;

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
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import madkit.action.SchedulingAction;
import madkit.message.SchedulingMessage;

/**
 * This class defines a generic threaded scheduler agent. It holds a collection
 * of activators. The default state of a scheduler is {@link SimulationState#PAUSED}. The
 * default delay between two steps is 0 ms (max speed).
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @since MaDKit 2.0
 * @version 5.1
 * @see Activator
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
	 * @since MaDKit 5.0
	 * @see #getSimulationState
	 */
	public enum SimulationState {

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

	private SimulationState simulationState = SimulationState.PAUSED;

	final private Set<Activator<? extends AbstractAgent>> activators = new LinkedHashSet<Activator<? extends AbstractAgent>>();

	private Action run, step, speedUp, speedDown;

	private JLabel timer;
	private JSlider speedSlider;

	/**
	 * specify the delay between 2 steps
	 */
	private DefaultBoundedRangeModel speedModel = new DefaultBoundedRangeModel(0, 50, 0, 1001);

	/**
	 * Returns the delay between two simulation steps
	 * 
	 * @return the delay between two simulation steps.
	 */
	public int getDelay() {
		return speedModel.getValue();
	}

	/**
	 * Sets the delay between two simulation steps. That is the pause time
	 * between to call to {@link #doSimulationStep()}
	 * 
	 * @param delay
	 *           the delay to set, an integer between 0 and 1000 (ms): O is max
	 *           speed
	 */
	public void setDelay(final int delay) {
		speedModel.setValue(delay);
	}

	private double GVT = 0; // simulation global virtual time

	/**
	 * Returns the simulation global virtual time.
	 * 
	 * @return the gVT
	 */
	public double getGVT() {
		return GVT;
	}

	/**
	 * Sets the simulation global virtual time.
	 * 
	 * @param GVT
	 *           the actual simulation time
	 */
	public void setGVT(final double GVT) {
		this.GVT = GVT;
		if (timer != null)
			timer.setText("Simulation " + simulationState + ", time is " + GVT);
	}

	private double simulationDuration;

	/**
	 * This constructor is equivalent to <code>Scheduler(Double.MAX_VALUE)</code>
	 */
	public Scheduler() {
		this(Double.MAX_VALUE);
	}

	// public Scheduler(boolean multicore) {
	// this(0, Double.MAX_VALUE);
	// }

	/**
	 * Constructor specifying the time at which the simulation ends.
	 * 
	 * @param endTime
	 *           the GVT at which the simulation will automatically stop
	 */
	public Scheduler(final double endTime) {
		buildActions();
		setSimulationDuration(endTime);
	}

	// @Override
	// protected void activate() {
	// if(logger != null)
	// logger.talk("\n\tHi human !\n\n I am an instance of the madkit.kernel.Scheduler class.\n I am specialized in simulation scheduling.\n I use activators on the artificial society\n to trigger agents' behaviors and simulate artificial worlds.\n You can extend me to create your own simulations !");
	// }

	/**
	 * Setup the default Scheduler GUI when launched with the default MaDKit GUI
	 * mechanism.
	 * 
	 * @see madkit.kernel.AbstractAgent#setupFrame(javax.swing.JFrame)
	 * @since MaDKit 5.0.0.8
	 */
	@Override
	public void setupFrame(JFrame frame) {
		super.setupFrame(frame);
		frame.add(getSchedulerToolBar(), BorderLayout.PAGE_START);
		frame.add(getSchedulerStatusLabel(), BorderLayout.PAGE_END);
		setGVT(GVT);
		frame.validate();
		frame.getJMenuBar().add(getSchedulerMenu());
	}

	/**
	 * Adds an activator to the kernel engine. This has to be done to make an
	 * activator work properly
	 * 
	 * @param activator
	 *           an activator.
	 */
	public void addActivator(final Activator<? extends AbstractAgent> activator) {
		if (kernel.addOverlooker(this, activator))
			activators.add(activator);
		if (logger != null)
			logger.fine("Activator added: " + activator);
	}

	/**
	 * Removes an activator from the kernel engine.
	 * 
	 * @param activator
	 *           an activator.
	 */
	public void removeActivator(final Activator<? extends AbstractAgent> activator) {
		kernel.removeOverlooker(this, activator);
		activators.remove(activator);
		if (logger != null)
			logger.fine("Activator removed: " + activator);
	}

	/**
	 * Executes all the activators in the order they have been added, using
	 * {@link Activator#execute()}, and then increments the global virtual time
	 * of this scheduler by one unit.
	 * 
	 * This also automatically calls the multicore mode of the activator if it
	 * is set so. This method should be overridden to define customized
	 * scheduling policy. So default implementation is :
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
	 * 		setGVT(getGVT() + 1);
	 * </pre>
	 */
	public void doSimulationStep() {
		if (logger != null) {
			logger.finer("Doing simulation step " + GVT);
		}
		for (final Activator<? extends AbstractAgent> activator : activators) {
			if (logger != null)
				logger.finer("Activating\n--------> " + activator);
//			try {
				activator.execute();
//			} catch (SimulationException e) {//TODO is it better ?
//				setSimulationState(SimulationState.SHUTDOWN);
//				getLogger().log(Level.SEVERE, e.getMessage(), e);
//			}
		}
		setGVT(GVT + 1);
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
	 * @see SimulationState
	 */
	public SimulationState getSimulationState() {
		return simulationState;
	}

	/**
	 * Changes the state of the scheduler
	 * 
	 * @param newState
	 *           the new state
	 */
	protected void setSimulationState(final SimulationState newState) {
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
	 * Scheduler's default behavior.
	 * 
	 * default code is:
	 * 
	 * <pre>
	 * while (isAlive()) {
	 * 	if (GVT &gt; getSimulationDuration()) {
	 * 		if (logger != null)
	 * 			logger.info(&quot;Quitting: Simulation has reached end time &quot;
	 * 					+ getSimulationDuration());
	 * 		return;
	 * 	}
	 * 	if (getDelay() == 0)
	 * 		Thread.yield();
	 * 	else
	 * 		pause(getDelay());
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
	 * @see madkit.kernel.Agent#live()
	 */
	protected void live() {
		while (isAlive()) {
			if (GVT > simulationDuration) {
				if (logger != null)
					logger.info("Quitting: Simulation has reached end time " + simulationDuration);
				return;
			}
			if (getDelay() == 0)
				Thread.yield();
			else
				pause(getDelay());
			checkMail(nextMessage());
			switch (simulationState) {
			case RUNNING:
				doSimulationStep();
				break;
			case PAUSED:
				// updateStatusDisplay();
				paused();
				break;
			case STEP:
				simulationState = PAUSED;
				doSimulationStep();
				break;
			case SHUTDOWN:
				return; // shutdown
			default:
				getLogger().severe("state not handled " + simulationState);
			}
		}
	}

	/**
	 * Changes my state according to a {@link SchedulingMessage} and sends
	 * a reply to the sender as acknowledgment.
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
			} catch (ClassCastException e) {
				if (logger != null)
					logger.info("I received a message that I cannot understand" + m);
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

		setSpeedSlider(new JSlider(speedModel));

		getSpeedSlider().setPaintTicks(true);
		getSpeedSlider().setPaintLabels(false);
		getSpeedSlider().setMajorTickSpacing(speedModel.getMaximum() / 2);
		getSpeedSlider().setMinorTickSpacing(100);
		getSpeedSlider().setInverted(true);
		getSpeedSlider().setSnapToTicks(false);

		getSpeedSlider().addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				getSpeedSlider().setValue(e.getWheelRotation() * 50 + getSpeedSlider().getValue());
			}
		});

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
	 * @return a menu controlling the scheduler's actions
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
	 * 
	 * @return a label giving some information on the simulation process
	 */
	public JLabel getSchedulerStatusLabel() {
		timer = new JLabel();
		timer.setBorder(new EmptyBorder(4, 4, 4, 4));
		timer.setHorizontalAlignment(JLabel.LEADING);
		return timer;
	}

	/**
	 * @param speedSlider
	 *           the speedSlider to set
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
