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
import static madkit.kernel.Scheduler.State.RUNNING;
import static madkit.kernel.Scheduler.State.STEP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import madkit.gui.OutputPanel;
import madkit.messages.ObjectMessage;

/**
 * This class defines a generic threaded scheduler agent. It holds a collection of activators.
 * 
 * @author Fabien Michel since V.2
 * @author Olivier Gutknecht 
 * @since MadKit 2.0
 * @version 5.0
 */
public class Scheduler extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2224235651899852429L;

	/**
	 * A simulation state. The simulation process managed by 
	 * a scheduler agent can be in one of the following states: 
	 * <ul>
	 * <li>{@link #RUNNING}<br>
	 *     The simulation process is running normally.
	 *     </li>
	 * <li>{@link #STEP}<br>
	 *    The scheduler will process one simulation step
	 *    and then will be in the {@link #PAUSED} state.
	 *     </li>
	 * <li>{@link #PAUSED}<br>
	 *     The simulation is paused.
	 *     </li>
	 * </ul>
	 *
	 * <p>
	 * An agent can be in only one state at a given point in time. 
	 * 
	 * @author Fabien Michel
	 * @since MadKit 5.0
	 * @see #getSimulationState
	 */
	public enum State {

		/**
		 *  The simulation process is running normally.
		 */
		RUNNING,

		/**
		 * The scheduler will process one simulation step
		 * and then will be in the {@link #PAUSED} state.
		 *
		 */
		STEP,

		/**
		 * The simulation is paused.
		 */
		PAUSED
	}

	private State simulationState = State.PAUSED;

	final private Set<Activator<? extends AbstractAgent>> activators = new LinkedHashSet<Activator<? extends AbstractAgent>> ();
	private int delay=20;

	AbstractAction run, step, pause, reset,speedUp,speedDown;
	JLabel timer;
	JSlider speedSlider;

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(final int delay) {
		this.delay = delay;
		if(speedSlider != null){
			speedSlider.setValue(delay);
		}
	}

	private double GVT = 0; // simulation global virtual time
	/**
	 * @return the gVT
	 */
	public double getGVT() {
		return GVT;
	}

	/**
	 * @param gVT the gVT to set
	 */
	public void setGVT(final double gVT) {
		GVT = gVT;
		updateStatusDisplay();
	}

	private double simulationDuration;
	private double startTime;


	public Scheduler(){
		this(0,Double.MAX_VALUE);
	}

	public Scheduler(final double endTime){
		this(0,endTime);
	}

	public Scheduler(final double startTime, final double endTime){
		buildActions();
		setSimulationDuration(endTime);
		this.setStartTime(startTime);
	}

	/**
	 * Setup the default Scheduler GUI when launched with the default gui mechanism.
	 * 
	 * @see madkit.kernel.AbstractAgent#setupFrame(javax.swing.JFrame)
	 * @since MadKit 5.0.0.8
	 */
	@Override
	public void setupFrame(JFrame frame) {
		super.setupFrame(frame);
		frame.add(getSchedulerToolBar(),BorderLayout.PAGE_START);
		frame.add(getSchedulerStatusLabel(), BorderLayout.PAGE_END);
		updateStatusDisplay();
		frame.validate();
		JMenuBar menubar = frame.getJMenuBar();
		menubar.add(getSchedulerMenu());
	}
	
	
	private void updateStatusDisplay(){
		if(timer != null)
			timer.setText("Simulation "+getSimulationState()+", time is "+getGVT());
	}

	public void addActivator(final Activator<? extends AbstractAgent> activator)
	{
		if(kernel.addOverlooker(this, activator))
			activators.add(activator);
		if(logger != null)
			logger.fine("Activator added: "+activator);
	}

	public void removeActivator(final Activator<? extends AbstractAgent> activator)
	{
		kernel.removeOverlooker(this, activator);
		activators.remove(activator);
	}

	public void doSimulationStep(){
		if (logger != null) {
			logger.finer("Doing a simulation step");
			for (final Activator<? extends AbstractAgent> activator : activators) {
				if (logger != null) 
					logger.finest("Activating "+activator);
				activator.execute();
			}
		}
		else{
			for (final Activator<? extends AbstractAgent> activator : activators) {
				activator.execute();
			}
		}
		setGVT(GVT+1);
	}

	public void stoped(){
		pause(300);
	}

	public void end() {
		simulationState=PAUSED;
		if(logger != null)
			logger.info("Simulation stopped !");
	}

	/**
	 * @return the agentState
	 */
	public State getSimulationState() {
		return simulationState;
	}

	/**
	 * @param agentState the agentState to set
	 */
	synchronized public void setSimulationState(final State newState) {
		switch (newState) {
		case RUNNING:
			receiveMessage(new ObjectMessage<State>(RUNNING));
			break;
		case STEP:
			receiveMessage(new ObjectMessage<State>(STEP));
			break;
		case PAUSED:
			receiveMessage(new ObjectMessage<State>(PAUSED));
			break;
		default:
			break;
		}
	}

	private void changeState(final State newState){
		if(simulationState != newState){
			simulationState = newState;
			switch (simulationState) {
			case RUNNING:
				run.setEnabled(false);
				break;
			case STEP:
				run.setEnabled(true);
				break;
			case PAUSED:
				run.setEnabled(true);
				break;
			default:
				break;
			}
		}
	}

	public void live() {
		while(true) {
			if(GVT > simulationDuration){
				if(logger != null)
					logger.fine("Quitting: Simulation has reached end time "+getGVT());
				return; // TODO logging
			}
			if (delay == 0)
				Thread.yield();
			else
				pause(delay);
			checkMail(nextMessage());
			switch(simulationState){
			case RUNNING:
				doSimulationStep();
				break;
			case PAUSED:
				updateStatusDisplay();
				paused();
				break;
			case STEP:
				simulationState=PAUSED;
				doSimulationStep();
				break;
			default:
				return; //shutdown
			}
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void checkMail(final Message m) {
		if(m != null){
			try {
				changeState(((ObjectMessage<State>) m).getContent());
				if(m.getSender() != null){
					sendReply(m, m);
				}
			} catch (ClassCastException e) {
				if(logger != null)
					logger.info("I received a message that I cannot understand"+m);
			}
		}
	}

	private void paused(){
		checkMail(waitNextMessage());
		//        if(defaultPattern){
		//        	displayAllWorld.execute();
		//            viewersDoIt.execute();
		//        }
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
		for(final Activator<? extends AbstractAgent> a : activators ){
			kernel.removeOverlooker(this,a);
		}
		activators.clear();
	}

	/**
	 * @param simulationDuration the simulationDuration to set
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
	 * @param startTime the startTime to set
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

	private void buildActions(){
		
		run = new AbstractAction("run") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setSimulationState(RUNNING);
			}
		};
		madkit.gui.Utils.initAction(run,
				"run the simulation",
				"run the simulation",
				"run",
				"run",
				KeyEvent.VK_R,
				"scheduler.run",
				KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_MASK),
				true);

		step = new AbstractAction("step") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setSimulationState(STEP);
			}
		};
		madkit.gui.Utils.initAction(step,
				"does one step and pauses the simulation",
				"does one step and pauses the simulation",
				"step",
				"step",
				KeyEvent.VK_S,
				"scheduler.step",
				KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK),
				true);
//		initActionIcon(step, "does one step and pauses the simulation",KeyEvent.VK_S);

		speedUp = new AbstractAction("speedUp") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				speedSlider.setValue(speedSlider.getValue()-50);
			}
		};
		madkit.gui.Utils.initAction(speedUp,
				"speed up the simulation",
				"speed up the simulation",
				"speedUp",
				"speedUp",
				KeyEvent.VK_P,
				"scheduler.speedUp",
				KeyStroke.getKeyStroke(KeyEvent.VK_P,KeyEvent.CTRL_MASK),
				true);

		speedDown = new AbstractAction("speedDown") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				speedSlider.setValue(speedSlider.getValue()+50);
			}
		};
		madkit.gui.Utils.initAction(speedDown,
				"speed down the simulation",
				"speed down the simulation",
				"speedDown",
				"speedDown",
				KeyEvent.VK_O,
				"scheduler.speedDown",
				KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_MASK),
				true);
	}

	/**
	 * @return
	 */
	public JToolBar getSchedulerToolBar(){
		final JToolBar toolBar = new JToolBar("scheduler toolbar");
		toolBar.add(run);
		toolBar.add(step);

		JPanel p = new JPanel(); 
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
		p.setBorder(new TitledBorder("speed")); 
		speedSlider = new JSlider(0, 1000, 20);
		speedSlider.setPaintTicks(true); 
		speedSlider.setPaintLabels(false);
		speedSlider.setMajorTickSpacing(500); 
		speedSlider.setMinorTickSpacing(100);
		speedSlider.setInverted(true);
		speedSlider.setSnapToTicks(false);
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (! speedSlider.getValueIsAdjusting()) {
					setDelay(speedSlider.getValue());
				}
			}
		});
		speedSlider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				speedSlider.setValue(e.getWheelRotation()*50+speedSlider.getValue());
			}
		});
		//      s.getAccessibleContext().setAccessibleName(resourceManager.getString("SliderDemo.plain")); 
		//      s.getAccessibleContext().setAccessibleDescription(resourceManager.getString("SliderDemo.a_plain_slider")); 

		//      p.add(Box.createRigidArea(new Dimension(5,5)));
		p.setPreferredSize(new Dimension(150, 50));
		p.add(speedSlider);
		toolBar.addSeparator();
//		toolBar.add(Box.createRigidArea(new Dimension(40,5))); 
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(p); 


		//		timer.setSize(30, 20);
		return toolBar;
	}

	public JMenu getSchedulerMenu(){
		JMenu myMenu = new JMenu("Scheduling");
		myMenu.setMnemonic(KeyEvent.VK_S);
		myMenu.add(run);
		myMenu.add(step);
		myMenu.add(speedUp);
		myMenu.add(speedDown);
		return myMenu;
	}
	
	public JLabel getSchedulerStatusLabel(){
		timer = new JLabel();
		timer.setBorder(new EmptyBorder(4, 4, 4, 4)); 
		timer.setHorizontalAlignment(JLabel.LEADING);
		return timer;
	}

}