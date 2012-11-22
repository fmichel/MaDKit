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

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.ACTIVATED;
import static madkit.kernel.AbstractAgent.State.ENDING;
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.LIVING;
import static madkit.kernel.AbstractAgent.State.TERMINATED;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import madkit.action.ActionInfo;
import madkit.action.GUIManagerAction;
import madkit.agr.CloudCommunity;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.gui.AgentStatusPanel;
import madkit.gui.OutputPanel;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;
import madkit.kernel.Madkit.Option;
import madkit.message.EnumMessage;
import madkit.message.GUIMessage;
import madkit.message.hook.HookMessage.AgentActionEvent;

// * <img src="doc-files/Capture.png" alt=""/>
/**
 * The super class of all MaDKit agents, v 5. It provides support for
 * <ul>
 * <li>Agent's Life cycle, logging, and naming.
 * <li>Agent launching and killing.
 * <li>Artificial society creation and management.
 * <li>Messaging.
 * <li>Minimal graphical interface management.
 * </ul>
 * <p>
 * The agent's behavior is <i>intentionally not defined</i>. It is up to the
 * agent developer to choose an agent model or to develop his specific agent
 * library on top of the facilities provided by the MaDKit API. However, all the
 * launched agents share the same organizational view, and the basic messaging
 * code, so integration of different agents is quite easy, even when they are
 * coming from different developers or have heterogeneous models.
 * <p>
 * Agent-related methods (most of this API) is only effective after the agent
 * has been launched and thus registered in the current Madkit session.
 * Especially, that means that most of the API has no effect in the constructor
 * method of an Agent and will only produce a warning if used.
 * <p>
 * <h2>MaDKit v.5 new features</h2>
 * <p>
 * <ul>
 * <li>One of the big change that comes with version 5 is how agents are
 * identified and localized within the artificial society. An agent is no longer
 * binded to a single agent address but has as many agent addresses as holden
 * positions in the artificial society. see {@link AgentAddress} for more
 * information.</li>
 * <br>
 * <li>With respect to the previous change, a <code><i>withRole</i></code>
 * version of all the messaging methods has been added. See
 * {@link #sendMessageWithRole(AgentAddress, Message, String)} for an example of
 * such a method.</li>
 * <br>
 * <li>A replying mechanism has been introduced through
 * <code><i>SendReply</i></code> methods. It enables the agent with the
 * possibility of replying directly to a given message. Also, it is now possible
 * to get the reply to a message, or to wait for a reply ( for {@link Agent}
 * subclasses only as they are threaded) See
 * {@link #sendReply(Message, Message)} for more details.</li>
 * <br>
 * <li>Agents now have a <i>formal</i> state during a MaDKit session. See the
 * {@link #getState()} method for detailed information.</li>
 * <br>
 * <li>One of the most convenient improvement of v.5 is the logging mechanism
 * which is provided. See the {@link #logger} attribute for more details.</li>
 * <p>
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @version 5.3
 */
public class AbstractAgent implements Comparable<AbstractAgent>, Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6180517348998118712L;

	private final static transient AtomicInteger	agentCounter		= new AtomicInteger(0);

	static final transient MadkitKernel				FAKE_KERNEL			= new FakeKernel();
	private static final transient MadkitKernel				TERMINATED_KERNEL	= new TerminatedKernel();

	final AtomicReference<State>						state					= new AtomicReference<AbstractAgent.State>(
																								AbstractAgent.State.NOT_LAUNCHED);
	transient MadkitKernel								kernel				= FAKE_KERNEL;

	final private int										_hashCode;

	private boolean										hasGUI;
	/**
	 * name is lazily created to save memory
	 */
	private String											name;
	final AtomicBoolean									alive					= new AtomicBoolean();						//default false
	final BlockingQueue<Message>						messageBox			= new LinkedBlockingQueue<Message>();		// TODO
																																				// lazy
																																				// creation

	/**
	 * <code>logger</code> should be used to print messages and trace the agent's life cycle. 
	 * According to a log level, the messages will be displayed in the console and/or the GUI 
	 * and/or a file according to the settings.
	 * 
	 * Thanks to the logging mechanism of the SDK, various log level could be used.
	 * 
	 * The following idiom should be used because {@link Logger} is set to <code>null</code>
	 * when {@link AgentLogger#setLevel(Level)} is used with {@link Level#OFF}. This allows
	 * to efficiently optimize the runtime speed when they are a lot of agents 
	 * (e.g. in a simulation mode). Indeed, thanks to this idiom, useless strings will not
	 * be built, thus saving a lot of time. 
	 * 
	 * <pre>
	 * if (logger != null)
	 * 	logger.info(&quot;info message&quot;);
	 * </pre>
	 * 
	 * {@link #getLogger()} should not be used here because it always returns a non <code>null</code>
	 * logger.
	 * 
	 * @see java.util.logging.Level
	 * @see java.util.logging.Logger
	 */
	protected AgentLogger								logger;

	public AbstractAgent() {
		_hashCode = agentCounter.getAndIncrement();// TODO bench outside
		logger = AgentLogger.defaultAgentLogger;
	}

	@SuppressWarnings("unused")
	AbstractAgent(Object fake) {
		_hashCode = -1;
	}
	
	MadkitKernel getMadkitKernel(){
		return kernel.getMadkitKernel();
	}

	/**
	 * Activates the MaDKit GUI initialization when launching the agent whatever
	 * the launching parameters. By default agents are launched without a GUI
	 * but some of them always need one: This ensures that the agent will have one.
	 * This method should be used only in the constructor of the 
	 * agent, otherwise it will be useless as it specifies a boot 
	 * property of the agent.
	 * 
	 */
	public void createGUIOnStartUp() {
		if (state.get().compareTo(ACTIVATED) < 0) {
			hasGUI = true;
		}
	}

	/**
	 * Tells if this agent has a GUI automatically built by the kernel
	 * @return <code>true</code> if this agent has a GUI built by the kernel
	 */
	public boolean hasGUI() {
		return hasGUI;
	}

	/**
	 * The ID of an agent. All the agents have different hashCode value
	 * in one kernel. Thus it can be used to identify one agent. In a networked
	 * environment, this value should be used in combination with the
	 * kernelAddress of the agent for unique identification. This also holds when
	 * multiple MaDKit kernels are launched within the same JVM.
	 * 
	 * @return the agent's unique ID in the MaDKit kernel
	 */
	@Override
	final public int hashCode() {// TODO should be regenerated if agent are sent
											// through the network in next releases
		return _hashCode;
	}

	/**
	 * @return
	 */
	final AtomicBoolean getAlive() {
		return alive;
	}

	/**
	 * Returns <code>true</code> if the agent has been launched and is not ended nor killed.
	 * 
	 */
	public boolean isAlive() {
		return alive.get();
	}

	// //////////////////////////////////////////// LIFE CYCLE

	// public void dieWithException(Exception e){
	// throw new AgentLifeException(e);
	// }

	// public void dieWithException(String message, Exception e){
	// throw new AgentLifeException(message,e);
	// }

	private final void activationFirstStage() {
		if (!state.compareAndSet(INITIALIZING, ACTIVATED))// TODO remove it when
																			// OK
			throw new AssertionError("not init in activation");
		// that only when state is set to ACTIVATED
		setMyThread(Thread.currentThread());
		// can be killed from now on
		if (!alive.compareAndSet(false, true)) {// can be killed from now on
			throw new AssertionError("already alive in launch");
		}
		if (hasGUI) {
			if (logger != null) {
				logger.finer("** setting up  GUI **");
			}
			// to avoid the log of logged kernel
//			if(
					getMadkitKernel().broadcastMessageWithRoleAndWaitForReplies(
					this, 
					LocalCommunity.NAME, 
					Groups.GUI,
					Organization.GROUP_MANAGER_ROLE, 
					new GUIMessage(GUIManagerAction.SETUP_AGENT_GUI, this), 
					null, 
					3000);// == null)
//					hasGUI = false;
			// getKernel().getMadkitKernel().sendMessageAndWaitForReply(//TODO
			// LocalCommunity.NAME,
			// Groups.SYSTEM,
			// Roles.GUI_MANAGER,
			// new GUIMessage(GUIManagerAction.SETUP_AGENT_GUI,this),
			// 10000);
		}
		logMethod(true);
	}

	/**
	 * @param myThread
	 *           the myThread to set
	 * @since MaDKit 5
	 */
	void setMyThread(final Thread thread) {
		thread.setName(getState() + "-" + hashCode());
	}

	final String getAgentThreadName(final State s) {
		return s + "-" + hashCode();
	}

	/**
	 * This is only called by MK threads and cannot be interrupted
	 * 
	 * @return
	 */
	boolean activation() {
		boolean result = false;
		try {
			try {
				activationFirstStage();// the activated flag must be in the try
				activate();
				synchronized (state) {
					Thread.currentThread().setName(getAgentThreadName(State.LIVING));
				}// cannot be hard killed after that
				result = true;
			} catch (SelfKillException e) {
				logLifeException(e);
				logMethod(false);
				state.set(LIVING);// for the following kill to work
				suicide(e);
				return true;
			} catch (Throwable e) {
				validateDeathOnException(e, LIVING);
			}
		} catch (KilledException e) {
			logLifeException(e);
		}
		logMethod(false);
		return result;
	}

	final void logMethod(final boolean entering) {
		if (logger != null)
			logger.finer("** " + (entering ? Words.ENTERING : Words.EXITING) + " " + getState().lifeCycleMethod() + " **");
	}

	/**
	 * @param e
	 */
	void suicide(SelfKillException e) {
		getMadkitKernel().startEndBehavior(this, Integer.parseInt(e.getMessage()), true);
	}

	/**
	 * This method corresponds to the first behavior which is called by the
	 * MaDKit kernel when an agent is launched. Usually a good place to take a
	 * position in the organization of the artificial society.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * 
	 * <pre>
	 * <tt>@Override</tt>
	 * protected void activate()
	 * {
	 * 	AbstractAgent.ReturnCode returnCode = requestRole("a community", "a group", "my role");
	 * 	if (returnCode == AbstractAgent.ReturnCode.SUCCESS){
	 * 		if(logger != null)
	 * 			logger.info("I am now playing my role in the artificial society");
	 * 	}
	 * 	else{
	 * 		if(logger != null)
	 * 			logger.warning("something wrong, return code is "+returnCode);
	 * 	}
	 * }
	 * 
	 * </pre>
	 * 
	 */
	protected void activate() {
	}

	final boolean ending() { // TODO boolean need ? NO
		state.set(ENDING);
		Thread.currentThread().setName(getAgentThreadName(ENDING));
		logMethod(true);
		try {
			synchronized (state) {// can be hard killed from now on
				state.notify();
			}
			try {
				end();
			} catch (Exception e) {
				validateDeathOnException(e, TERMINATED);
			}
			synchronized (state) {
				alive.set(false);
				Thread.currentThread().setName(getAgentThreadName(TERMINATED));
			}
		} catch (KilledException e) {
			logLifeException(e);
		}
		logMethod(false);
		synchronized (state) {// notifying for AA
			state.notify();
		}
		return true;
	}

	private void validateDeathOnException(Throwable e, State threadNewState) {
		synchronized (state) {
			logLifeException(e);
			Thread.currentThread().setName(getAgentThreadName(threadNewState));
			if (!alive.compareAndSet(true, false)) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
				}// answer the kill
			}
		}
	}

	/**
	 * Calls when the life cycle quits
	 */
	// not final because of Scheduler and Watcher
	void terminate() {
		Thread.currentThread().setName(getAgentThreadName(TERMINATED));
		synchronized (state) {
			state.set(TERMINATED);
			state.notify();
		}
		kernel = getMadkitKernel();
		if (hasGUI) {
			kernel.broadcastMessageWithRole(this, LocalCommunity.NAME, Groups.GUI, Organization.GROUP_MANAGER_ROLE, new GUIMessage(
					GUIManagerAction.DISPOSE_AGENT_GUI, this), null);
		}
		// if (getState().equals(TERMINATED))// TODO remove that
		// throw new AssertionError("terminating twice " + getName());
		try {
			kernel.removeAgentFromOrganizations(this);// TODO catch because of
																	// probe/activator
		} catch (Throwable e) {
			logLifeException(e);
		}
		// messageBox.clear(); // TODO test speed and no need for that
		if (logger != null) {
			logger.finest("** TERMINATED **");

			// TODO This should be done anyway but this would slow down kills
			// So there a risk of memory leak here because logger can be set to null after creation and still exists in AgentLogger.loggers 
			// But that should not be a problem because such a practice is usually not used
			AgentLogger.removeLogger(this);
		}
		if(hasGUI){
			AgentLogLevelMenu.remove(this);
			AgentStatusPanel.remove(this);
		}
		if(kernel.isHooked())
			kernel.informHooks(AgentActionEvent.AGENT_TERMINATED, getName());
		kernel = TERMINATED_KERNEL;
	}

	/**
	 * This method corresponds to the last behavior which is called by the MaDKit
	 * kernel. This call occurs when a threaded agent normally exits its live
	 * method or when the agent is killed. Usually a good place to release taken
	 * resources or log what has to be logged.
	 * 
	 * It has to be noted that the kernel automatically takes care of removing
	 * the agent from the organizations it is in. However, this cleaning is not
	 * logged by the agent. Therefore it could be of interest for the agent to do
	 * that itself.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * 
	 * <pre>
	 * <tt>@Override</tt>
	 * protected void end()
	 * {
	 * 	AbstractAgent.ReturnCode returnCode = leaveRole("a community", "a group", "my role");
	 * 	if (returnCode == AbstractAgent.ReturnCode.SUCCESS){
	 * 		if(logger != null)
	 * 			logger.info("I am leaving the artificial society");
	 * 	}
	 * 	else{
	 * 		if(logger != null)
	 * 			logger.warning("something wrong when ending, return code is "+returnCode);
	 * 	}
	 * 	if(logger != null)
	 * 		logger.info("I am done");
	 * 	}
	 * }
	 * </pre>
	 * 
	 */
	protected void end() {
	}

	/**
	 * Launches a new agent in the MaDKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,false)</code>
	 * 
	 * @param agent
	 *           the agent to launch.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MaDKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent) {
		return launchAgent(agent, Integer.MAX_VALUE, false);
	}

	/**
	 * Launches a new agent in the MaDKit platform. This has the same effect as
	 * <code>launchAgent(agent,timeOutSeconds,false)</code>
	 * 
	 * @param agent
	 *           the agent to launch.
	 * @param timeOutSeconds
	 *           time to wait the end of the agent's activation until returning a
	 *           TIMEOUT.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code>{@link ReturnCode#AGENT_CRASH}</code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final int timeOutSeconds) { 
		return launchAgent(agent, timeOutSeconds, false);
	}

	/**
	 * Launches a new agent in the MaDKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,withGUIManagedByTheBooter)</code>
	 * 
	 * @param agent
	 *           the agent to launch.
	 * @param createFrame
	 *           if <code>true</code>, the kernel will launch a JFrame for this
	 *           agent.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MaDKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final boolean createFrame) { 
		return launchAgent(agent, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent and returns when the agent has completed its
	 * {@link AbstractAgent#activate()} method or when
	 * <code>timeOutSeconds</code> seconds elapsed. That is, the launched agent
	 * has not finished its {@link AbstractAgent#activate()} before the time out
	 * time elapsed. Additionally, if <code>createFrame</code> is
	 * <code>true</code>, it tells to MaDKit that an agent GUI should be managed
	 * by the Kernel. In such a case, the kernel takes the responsibility to
	 * assign a JFrame to the agent and to manage its life cycle (e.g. if the
	 * agent ends or is killed then the JFrame is closed) Using this feature
	 * there are two possibilities:
	 * <ul>
	 * <li>1. the agent overrides the method
	 * {@link AbstractAgent#setupFrame(JFrame)} and so setup the default JFrame
	 * as will</li>
	 * <li>2. the agent does not override it so that MaDKit will setup the JFrame
	 * with the default Graphical component delivered by the MaDKit platform:
	 * {@link OutputPanel}
	 * </ul>
	 * 
	 * @param agent
	 *           the agent to launch.
	 * @param timeOutSeconds
	 *           time to wait for the end of the agent's activation until
	 *           returning a TIMEOUT.
	 * @param createFrame
	 *           if <code>true</code>, the kernel will launch a JFrame for this
	 *           agent.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final int timeOutSeconds, final boolean createFrame) {
		return getKernel().launchAgent(this, agent, timeOutSeconds, createFrame);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, Integer.MAX_VALUE, false)</code>.
	 * 
	 * @param agentClass
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass) { 
		return launchAgent(agentClass, Integer.MAX_VALUE, false);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, timeOutSeconds, false)</code>.
	 * 
	 * @param timeOutSeconds
	 *           time to wait the end of the agent's activation until returning
	 *           <code>null</code>
	 * @param agentClass
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds) { 
		return launchAgent(agentClass, timeOutSeconds, false);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, Integer.MAX_VALUE, defaultGUI)</code>.
	 * 
	 * @param createFrame
	 *           if <code>true</code> a default GUI will be associated with the
	 *           launched agent
	 * @param agentClass
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, final boolean createFrame) { 
		return launchAgent(agentClass, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent using its full class name and returns when the
	 * launched agent has completed its {@link AbstractAgent#activate()} method
	 * or when the time out is elapsed. This has the same effect as
	 * {@link #launchAgent(AbstractAgent, int, boolean)} but allows to launch
	 * agent using a class name found reflexively for instance. Additionally,
	 * this method will launch the last compiled byte code of the corresponding
	 * class if it has been reloaded using
	 * {@link MadkitClassLoader#reloadClass(String)}. Finally, if the launch
	 * timely succeeded, this method returns the instance of the created agent.
	 * 
	 * @param timeOutSeconds
	 *           time to wait the end of the agent's activation until returning
	 *           <code>null</code>
	 * @param createFrame
	 *           if <code>true</code> a default GUI will be associated with the
	 *           launched agent
	 * @param agentClass
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds, final boolean createFrame) {// TODO
																																			// with
																																			// args
		if (logger != null)
			logger.finest(Words.LAUNCH + " " + agentClass + " GUI " + createFrame);
		try {
			final AbstractAgent a = (AbstractAgent) getMadkitClassLoader().loadClass(agentClass).newInstance();
			if (ReturnCode.SUCCESS == launchAgent(a, timeOutSeconds, createFrame))
				return a;
		} catch (InstantiationException e) {
			cannotLaunchAgent(agentClass, e, " :  no default constructor");
//			final String msg = ErrorMessages.CANT_LAUNCH + agentClass + " : no default constructor";
//			SwingUtilities.invokeLater(new Runnable() {
//
//				public void run() {
//					JOptionPane.showMessageDialog(null, msg, "Launch failed", JOptionPane.WARNING_MESSAGE);
//				}
//			});
//			getLogger().severeLog(msg, e);
		} catch (IllegalAccessException e) {
			cannotLaunchAgent(agentClass, e, " : constructor not public");//TODO launch anyway ??
		} catch (ClassCastException e) {
			cannotLaunchAgent(agentClass, e, " : Not an agent class");
		} catch (ClassNotFoundException e) {
			cannotLaunchAgent(agentClass, e, null);
		} catch (KernelException e) {
			cannotLaunchAgent(agentClass, e, null);
		}
		return null;
	}

	/**
	 * @param agentClass
	 * @param e
	 */
	final void cannotLaunchAgent(String agentClass, Throwable e, String infos) {
		getLogger().severeLog(ErrorMessages.CANT_LAUNCH + " " + agentClass + " : "+(infos != null ? infos :""), e);
	}
	
	/**
	 * Optimizes mass agent launching. Launches <i><code>bucketSize</code></i>
	 * agent instances of this <i><code>agentClassName</code></i> and put them in
	 * the artificial society at the locations defined by
	 * <code>cgrLocations</code>. Each string of the <code>cgrLocations</code>
	 * array defines a complete CGR location. So for example,
	 * <code>cgrLocations</code> could be defined and used with code such as :
	 * 
	 * <p>
	 * 
	 * <pre>
	 * launchAgentBucketWithRoles("madkit.bees.Bee", 1000000, "community;group;role","anotherC;anotherG;anotherR")
	 * </pre>
	 * 
	 * In this example all the agents created by this process will have these two
	 * roles in the artificial society, even if they do not request it in their
	 * {@link #activate()} method as explained below.
	 * <p>
	 * For maximizing this optimization, if <code>cgrLocations</code> is not
	 * <code>null</code> then calls to
	 * {@link #createGroup(String, String, boolean, Gatekeeper)},
	 * {@link #requestRole(String, String, String, Object)},
	 * {@link #leaveGroup(String, String)} or
	 * {@link #leaveRole(String, String, String)} used in the {@link #activate()}
	 * methods will be ignored, as it is assumed that these are contained in
	 * <code>cgrLocations</code>. It can be <code>null</code>
	 * <p>
	 * 
	 * If some of the corresponding groups do not exist before this call, the
	 * caller agent will automatically become the manager of these groups.
	 * 
	 * @param agentClass
	 *           the name of the class from which the agents should be built.
	 * @param bucketSize
	 *           the desired number of instances.
	 * @param roles
	 *           default locations in the artificial society for the launched
	 *           agents. Each string of the <code>cgrLocations</code> array
	 *           defines a complete CGR location by separating C, G and R with
	 *           semicolon as follows: <code>"community;group;role"</code>
	 * @return a list containing all the agents which have been launched, or
	 *         <code>null</code> if the operation has failed
	 * @since MaDKit 5.0.0.6
	 */
	public List<AbstractAgent> launchAgentBucket(String agentClass, int bucketSize, String... roles) { 
		List<AbstractAgent> bucket = null;
		try {
			bucket = getMadkitKernel().createBucket(agentClass, bucketSize);
			launchAgentBucket(bucket, roles);
		} catch (InstantiationException e) {//TODO waiting java 7
			cannotLaunchAgent(agentClass, e, null);
		} catch (IllegalAccessException e) {
			cannotLaunchAgent(agentClass, e, null);
		} catch (ClassNotFoundException e) {
			cannotLaunchAgent(agentClass, e, null);
		}
		return bucket;
	}
	
	/**
	 * Similar to {@link #launchAgentBucket(String, int, String...)}
	 * except that the list of agents to launch is given. Especially, this could
	 * be used when the agents have no default constructor.
	 * 
	 * @param bucket the list of agents to launch
	 * @param roles
	 *           default locations in the artificial society for the launched
	 *           agents. Each string of the <code>cgrLocations</code> array
	 *           defines a complete CGR location by separating C, G and R with
	 *           semicolon as follows: <code>"community;group;role"</code>
	 *          
	 */
	@SuppressWarnings("unchecked")
	public void launchAgentBucket(List<? extends AbstractAgent> bucket, String... roles) {
		getKernel().launchAgentBucketWithRoles(this, (List<AbstractAgent>) bucket, roles);
	}

	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,Integer.MAX_VALUE)</code> so that the targeted
	 * agent has a lot of time to complete its {@link #end()} method.
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the target
	 *         has been already killed.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the
	 *         target has not been launched.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see #killAgent(AbstractAgent, int)
	 */
	public ReturnCode killAgent(final AbstractAgent target) {
		return killAgent(target, Integer.MAX_VALUE);
	}

	/**
	 * Kills the targeted agent. The kill process stops the agent's life cycle
	 * but allows it to process its {@link #end()} method until the time out
	 * elapsed.
	 * <p>
	 * If the target is in the activate or live method (Agent subclasses), it
	 * will be brutally stop and then proceed its end method.
	 * 
	 * <p>
	 * 
	 * The method returns only when the targeted agent actually ends its life. So
	 * if the target contains a infinite loop, the caller can be blocked. Using a
	 * timeout thus ensures that the caller will be blocked only a certain amount
	 * of time. Using 0 as timeout will stop the target as soon as possible,
	 * eventually brutally stop the its life cycle. In such a case, if its end
	 * method has not been started, it will never run.
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li> <li><code>
	 *         {@link ReturnCode#ALREADY_KILLED}</code>: If the target has been
	 *         already killed.</li> <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}
	 *         </code>: If the target has not been launched.</li> <li><code>
	 *         {@link ReturnCode#TIMEOUT}</code>: If the target's end method took
	 *         too much time and has been brutally stopped.</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 */
	public ReturnCode killAgent(final AbstractAgent target, final int timeOutSeconds) {// TODO
																													// check
																													// threads
																													// origin
		if (target == this && Thread.currentThread().getName().equals(getAgentThreadName(getState()))) {
			if (isFinestLogOn())
				logger.log(Level.FINEST, Influence.KILL_AGENT + " (" + timeOutSeconds + ")" + target.getLoggingName() + "...");
			if (alive.compareAndSet(true, false)) {
				throw new SelfKillException("" + timeOutSeconds);
			}
		}
		return getKernel().killAgent(this, target, timeOutSeconds);
	}

	/**
	 * @return the current kernel
	 */
	final MadkitKernel getKernel() {
		return kernel;
	}

	/**
	 * @param kernel
	 *           the kernel to set
	 */
	final void setKernel(MadkitKernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * Returns the agent's name.
	 * 
	 * @return the name to display in logger info, GUI title and so on. Default
	 *         is "<i>class name + internal ID</i>"
	 * 
	 */
	public String getName() {
		if (name == null)
			name = getClass().getSimpleName() + "-" + _hashCode;
		return name;
	}

	/**
	 * Changes the agent's name
	 * @param name
	 *           the name to display in logger info, GUI title and so on, default
	 *           is "class name + internal ID"
	 */
	public void setName(final String name) {// TODO trigger gui changes and so on need AgentModel
		if (!getName().equals(name)) {
			this.name = name;
			if (logger != null && logger != AgentLogger.defaultAgentLogger) {
				logger = null;
				getLogger();
			}
		}
	}

	/**
	 * Sets the agent's log level. This should be
	 * used instead of directly {@link AgentLogger#setLevel(Level)} because
	 * this works when {@link #logger} is <code>null</code> and allows
	 * to set it to <code>null</code> to save cpu time.
	 * 
	 * @param newLevel The log level under which log messages are displayed. 
	 * If {@link Level#OFF} is used
	 * then {@link #logger} is set to <code>null</code>
	 * 
	 * @see #logger
	 */
	public void setLogLevel(final Level newLevel) {
		if (Level.OFF == newLevel) {
			if (logger != null && logger != AgentLogger.defaultAgentLogger) {
				logger.setLevel(newLevel);
			}
			logger = null;
			setKernel(getMadkitKernel());
		}
		else {
			getLogger().setLevel(newLevel);
			setKernel(kernel.getLoggedKernel());
		}
	}

	/**
	 * Returns the agent's logger.
	 * 
	 * @return the agent's logger. It cannot be <code>null</code> as it will be
	 *         created if necessary. But you can then still put {@link #logger}
	 *         to <code>null</code> for optimizing your code by using
	 *         {@link #setLogLevel(Level)} with {@link Level#OFF}.
	 * 
	 * @see AbstractAgent#logger
	 * @since MaDKit 5.0.0.6
	 */
	public AgentLogger getLogger() {
		if (logger == AgentLogger.defaultAgentLogger || logger == null) {
			synchronized (this) {
				logger = AgentLogger.getLogger(this);
			}
		}
		return logger;
	}

	/**
	 * Compares this agent with the specified agent for order with respect to
	 * instantiation time.
	 * 
	 * @param other
	 *           the agent to be compared.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @return a negative integer, a positive integer or zero as this agent has
	 *         been instantiated before, after or is the same agent than the
	 *         specified agent.
	 * 
	 */
	@Override
	public int compareTo(final AbstractAgent other) { 
		return _hashCode - other._hashCode;
	}

	// ///////////////////////////////////////////////////////// GROUP & ROLE
	// METHODS AGR

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, false, null)</code>
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the
	 *         operation failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in
	 *         activate and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(List, String...)}
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group) {
		return createGroup(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, isDistributed, null)</code>
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group.
	 * @param isDistributed
	 *           if <code>true</code> the new group will be distributed when
	 *           multiple MaDKit kernels are connected.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the
	 *         operation failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in
	 *         activate and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(List, String...)}
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed) {
		return createGroup(community, group, isDistributed, null);
	}

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role
	 * defined by {@link Organization#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i>, in this created
	 * group. Especially, if the agent leaves the role of <i>
	 * {@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i>, it will also
	 * automatically leave the group and thus all the roles it has in this group.
	 * 
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>
	 * {@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i> using the role
	 * defined by {@link Organization#GROUP_CANDIDATE_ROLE}, which value is <i>
	 * {@value madkit.agr.Organization#GROUP_CANDIDATE_ROLE}</i>.
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group.
	 * @param isDistributed
	 *           if <code>true</code> the new group will be distributed when
	 *           multiple MaDKit kernels are connected.
	 * @param keyMaster
	 *           any object that implements the {@link Gatekeeper} interface. If
	 *           not <code>null</code>, this object will be used to check if an
	 *           agent can be admitted in the group. When this object is null,
	 *           there is no group access control.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li> <li><code>
	 *         {@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed
	 *         because such a group already exists.</li> 
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in
	 *         activate and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(List, String...)}
	 *         </li>
	 *         </ul>
	 * 
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed, final Gatekeeper keyMaster) {
		return getKernel().createGroup(this, community, group, keyMaster, isDistributed);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if
	 * the group already exists. This has the same effect as
	 * <code>createGroupIfAbsent(community, group, false, null)</code>
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group.
	 * @return <code>true</code> if the group has been created,
	 *         <code>false</code> if it was already present. </ul>
	 * 
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean,
	 *      Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group) { 
		return createGroupIfAbsent(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if
	 * the group already exists. This has the same effect as
	 * <code>createGroupIfAbsent(community, group, isDistributed, null)</code>
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group
	 * @param isDistributed
	 *           if <code>true</code> the new group will be distributed when
	 *           multiple MaDKit kernels are connected.
	 * @return <code>true</code> if the group has been created,
	 *         <code>false</code> if it was already present. </ul>
	 * 
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean,
	 *      Gatekeeper)
	 * @since MaDKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group, boolean isDistributed) { 
		return createGroupIfAbsent(community, group, isDistributed, null);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if
	 * the group already exists. If this operation succeed, the agent will
	 * automatically handle the role of <i>group manager</i> in the created
	 * group.
	 * 
	 * @param community
	 *           the community within which the group will be created. If this
	 *           community does not exist it will be created.
	 * @param group
	 *           the name of the new group
	 * @param isDistributed
	 *           if <code>true</code> the new group will be distributed when
	 *           multiple MaDKit kernels are connected.
	 * @param keyMaster
	 *           any object that implements the {@link Gatekeeper} interface. If
	 *           not <code>null</code>, this object will be used to check if an
	 *           agent can be admitted in the group. When this object is null,
	 *           there is no group access control.
	 * @return <code>true</code> if the group has been created,
	 *         <code>false</code> if it was already present. </ul>
	 * 
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @since MaDKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group, boolean isDistributed,
			final Gatekeeper keyMaster) {
		return getKernel().createGroupIfAbsent(this, community, group, keyMaster, isDistributed);
	}

	/**
	 * Makes this agent leaves the group of a particular community.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of this group.</li>
	 *         </ul>
	 * 
	 * @since MaDKit 5.0
	 * @see ReturnCode
	 */
	public ReturnCode leaveGroup(final String community, final String group) {
		return getKernel().leaveGroup(this, community, group);
	}

	/**
	 * Requests a role within a group of a particular community. This has the
	 * same effect as <code>requestRole(community, group, role, null)</code>.
	 * So the passKey is <code>null</code> and the group must
	 * not be secured for this to succeed.
	 * 
	 * @param community
	 *           the group's community.
	 * @param group
	 *           the targeted group.
	 * @param role
	 *           the desired role.
	 * @see #requestRole(String, String, String, Object)
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role) {
		return requestRole(community, group, role, null);
	}

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 * 
	 * @param community
	 *           the group's community.
	 * @param group
	 *           the targeted group.
	 * @param role
	 *           the desired role.
	 * @param passKey
	 *           the <code>passKey</code> to enter a secured group. It is
	 *           generally delivered by the group's <i>group manager</i>. It
	 *           could be <code>null</code>, which is sufficient to enter an
	 *           unsecured group. Especially,
	 *           {@link #requestRole(String, String, String)} uses a null
	 *           <code>passKey</code>.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If this method is
	 *         used in activate and that this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(List, String...)}
	 *         </li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * 
	 * @since MaDKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role, final Object passKey) {
		return getKernel().requestRole(this, community, group, role, passKey);
	}

	/**
	 * Abandons an handled role within a group of a particular community.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role
	 *         is not handled by this agent.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @since MaDKit 5.0
	 */
	public ReturnCode leaveRole(final String community, final String group, final String role) { 
		return getKernel().leaveRole(this, community, group, role);
	}

	final void handleException(final Influence i, final Throwable e) {
		if (isWarningOn()) {
			setAgentStackTrace(e);
			logger.log(Level.WARNING, i.failedString(), e);
		}
	}

	final boolean isWarningOn() {
		return logger != null && logger.getWarningLogLevel().intValue() >= logger.getLevel().intValue();
	}

	final void setAgentStackTrace(final Throwable e) {
		StackTraceElement[] stackTrace = e.getStackTrace();
		if (stackTrace.length > 0) {
			final List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
			// stack.add(stackTrace[1]);
			final String agentClassName = getClass().getName();
			for (int i = 0; i < stackTrace.length; i++) {
				final String trace = stackTrace[i].getClassName();
				if (!(trace.contains("madkit.kernel") || trace.contains("java.")) || trace.contains(agentClassName)) {
					stack.add(stackTrace[i]);
				}
			}
			e.setStackTrace(stack.toArray(new StackTraceElement[0]));
		}
	}

	/**
	 * Returns the agent address of this agent at this CGR location.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @return the agent's address in this location or <code>null</code> if this
	 *         agent does not handle this role.
	 * @since MaDKit 5.0.0.15
	 */
	public AgentAddress getAgentAddressIn(final String community, final String group, final String role) {
		return kernel.getAgentAddressIn(this, community, group, role);
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this
	 *         role or <code>null</code> if such an agent does not exist.
	 */
	public AgentAddress getAgentWithRole(final String community, final String group, final String role) {
		return getKernel().getAgentWithRole(this, community, group, role);
	}

	/**
	 * Returns an {@link java.util.List} containing agents that handle this role
	 * in the organization. The caller is excluded from this list.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @return a {@link java.util.List} containing agents that handle this role
	 *         or <code>null</code> if no agent has been found.
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role) { 
		return getAgentsWithRole(community, group, role, false);
	}

	/**
	 * Returns an {@link java.util.List} containing all the agents that handle
	 * this role in the organization.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param callerIncluded
	 *           if <code>false</code>, the caller is removed from the list if it
	 *           is in.
	 * @return a {@link java.util.List} containing agents that handle this role
	 *         or <code>null</code> if no agent has been found.
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role,
			boolean callerIncluded) {
		return getKernel().getAgentsWithRole(this, community, group, role, callerIncluded);
	}

	/**
	 * Retrieves and removes the oldest received message contained in the
	 * mailbox.
	 * 
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
	public Message nextMessage() {
		if (logger != null) {
			final Message m = messageBox.poll();
			logger.finest("getting nextMessage = " + m);
			return m;
		}
		return messageBox.poll();
	}

	/**
	 * Purges the mailbox and returns the most
	 * recent received message at that time.
	 * 
	 * @return the most recent received message or <code>null</code> if the
	 *         mailbox is already empty.
	 */
	public Message purgeMailbox() { 
		Message m = null;
		synchronized (messageBox) {
			try {
				while (true) {
					m = messageBox.remove();
				}
			} catch (NoSuchElementException e) {
			}
		}
		return m;
	}

	/**
	 * Tells if there is a message in the mailbox
	 * @return <code>true</code> if there is no message in
	 *         the mailbox.
	 */
	public boolean isMessageBoxEmpty() { 
		return messageBox.isEmpty();
	}

	/**
	 * Sends a message to an agent using an agent address. This has the same
	 * effect as <code>sendMessageWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param receiver
	 * @param messageToSend
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode sendMessage(final AgentAddress receiver, final Message messageToSend) {
		return sendMessageWithRole(receiver, messageToSend, null);
	}

	// * <li><code>{@link ReturnCode#NETWORK_DOWN}</code>: If the
	// * <code>receiver</code> is running on another kernel but the network
	// * is down.</li>
	/**
	 * Sends a message, using an agent address, specifying explicitly the role
	 * used to send it.
	 * 
	 * @param receiver
	 *           the targeted agent
	 * @param message
	 *           the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode sendMessageWithRole(final AgentAddress receiver, final Message message, final String senderRole) {
		return getKernel().sendMessage(this, receiver, message, senderRole);
	}

	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect
	 * as sendMessageWithRole(community, group, role, messageToSend,null). If
	 * several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param message
	 *           the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendMessage(final String community, final String group, final String role, final Message message) {
		return sendMessageWithRole(community, group, role, message, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(community, group, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param message
	 *           the message to send
	 * @param senderRole
	 *           the agent's role with which the message has to be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendMessageWithRole(final String community, final String group, final String role, final Message message,
			final String senderRole) {
		return getKernel().sendMessage(this, community, group, role, message, senderRole);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community, but not to the sender.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param message
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode broadcastMessage(final String community, final String group, final String role, final Message message) {
		return broadcastMessageWithRole(community, group, role, message, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community using a specific role for the sender. The sender is excluded
	 * from the search.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param messageToSend
	 * @param senderRole the agent's role with which the message should be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode broadcastMessageWithRole(final String community, final String group, final String role,
			final Message messageToSend, final String senderRole) {
		return getKernel().broadcastMessageWithRole(this, community, group, role, messageToSend, senderRole);
	}

	// void broadcasting(final List<AgentAddress> receivers, Message m){
	// for (final AgentAddress agentAddress : receivers) {
	// if (agentAddress != null) {//TODO this should not be possible
	// m = m.clone();
	// m.setReceiver(agentAddress);
	// kernel.sendMessage(m);
	// }
	// }
	// }

	/**
	 * Sends a message by replying to a previously received message. The sender
	 * is excluded from this search.
	 * 
	 * @param messageToReplyTo
	 *           the previously received message.
	 * @param reply
	 *           the reply itself.
	 * @param senderRole the agent's role with which the message should be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         no longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendReplyWithRole(final Message messageToReplyTo, final Message reply, final String senderRole) { 
		reply.setID(messageToReplyTo.getConversationID());
		return getKernel().sendMessage(this, messageToReplyTo.getSender(), reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message. This has the
	 * same effect as
	 * <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param messageToReplyTo
	 *           the previously received message.
	 * @param reply
	 *           the reply itself.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         no longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see AbstractAgent#sendReplyWithRole(Message, Message, String)
	 * 
	 */
	public ReturnCode sendReply(final Message messageToReplyTo, final Message reply) {
		return sendReplyWithRole(messageToReplyTo, reply, null);
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 * 
	 * @param originalMessage
	 *           the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	public Message getReplyTo(final Message originalMessage) { 
		final long searchID = originalMessage.getConversationID();
		for (final Iterator<Message> it = messageBox.iterator(); it.hasNext();) {
			final Message m = it.next();
			if (m.getConversationID() == searchID) {
				it.remove();
				return m;
			}
		}
		return null;
	}

	/**
	 * This method offers a convenient way for regular object to send messages to
	 * Agents, especially threaded agents. For instance when a GUI wants to
	 * discuss with its linked agent: This allows to enqueue work to do in their
	 * life cycle
	 * 
	 * @param m
	 */
	public void receiveMessage(final Message m) {
		messageBox.offer(m); // TODO test vs. arraylist and synchronized
		// if(messageBox == null)
		// messageBox = new LinkedBlockingDeque<Message>();
		// messageBox.offer(m); // TODO test vs. arraylist and synchronized
	}

	/**
	 * Gets the MaDKit session property indicated by the specified key. This call
	 * is equivalent to <code>getMadkitConfig().getProperty(key)</code>
	 * 
	 * @param key
	 *           the name of the MaDKit property
	 * @return the string value of the MaDKit property, or <code>null</code> if
	 *         there is no property with that key.
	 * @see #setMadkitProperty(String, String)
	 * @see Madkit
	 */
	final public String getMadkitProperty(String key) {
		return getMadkitConfig().getProperty(key);
	}

	/**
	 * Sets the MaDKit session property indicated by the specified key.
	 * 
	 * @param key
	 *           the name of the MaDKit property
	 * @see #getMadkitProperty(String)
	 * @see Madkit
	 */
	public void setMadkitProperty(String key, String value) { 
		getMadkitConfig().setProperty(key, value);	
	}

	/**
	 * Called when the default GUI mechanism is used upon agent creation. This
	 * provides an empty frame which will be used as GUI for the agent. The
	 * life cycle of the frame is automatically managed: the frame is disposed when the
	 * agent is terminated. Some menus are available by default. Default code
	 * is only one line: <code>frame.add(new IOPanel(this));</code>.
	 * 
	 * Default settings for the frame are:
	 * <ul>
	 * <li>width = 400</li>
	 * <li>height = 300</li>
	 * <li>location = center of the screen</li>
	 * <li>a JMenuBar with: {@link MadkitMenu}, {@link AgentMenu} and {@link AgentLogLevelMenu}</li> 
	 * </ul>
	 * 
	 * @param frame
	 *           the default frame which has been created by MaDKit for this
	 *           agent.
	 * @since MaDKit 5.0.0.8
	 * @see madkit.gui.OutputPanel
	 */
	public void setupFrame(final JFrame frame) {
		frame.add(new OutputPanel(this));
	}

	// /////////////////////////////////////////////// UTILITIES
	// /////////////////////////////////

	public Map<String, Map<String, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global) {
		return getKernel().getOrganizationSnapShot(global);
	}

	/**
	 * Tells if a community exists in the artificial society.
	 * 
	 * @param community
	 *           the name of the community
	 * @return <code>true</code> If a community with this name exists,
	 *         <code>false</code> otherwise.
	 */
	public boolean isCommunity(final String community) { 
		return getKernel().isCommunity(this, community);
	}

	/**
	 * Tells if a group exists in the artificial society.
	 * 
	 * @param community
	 *           the name of the community the group is in
	 * @param group
	 *           the name of the group
	 * @return <code>true</code> If a group with this name exists in this
	 *         community, <code>false</code> otherwise.
	 */
	public boolean isGroup(final String community, final String group) { 
		return getKernel().isGroup(this, community, group);
	}

	/**
	 * Tells if a role exists in the artificial society.
	 * 
	 * @param community
	 *           the name of the community the group is in
	 * @param group
	 *           the name of the group
	 * @param role
	 *           the name of the role
	 * @return <code>true</code> If a role with this name exists in this
	 *         <community;group> couple, <code>false</code> otherwise.
	 */
	public boolean isRole(final String community, final String group, final String role) {
		return getKernel().isRole(this, community, group, role);
	}

	// and the status
	/**
	 * @return a <code>String</code> representing the name of the agent
	 */
	@Override
	public String toString() {
		return getName() + " " + getState();
		// if(getState() == SimulationState.NOT_LAUNCHED || getState() == TERMINATED)
		// return getName();//+" *"+getState()+"*";
		// // return getName()+" *"+getState()+"* running on "+getKernelAddress();
		// return getName()+" running on "+getKernelAddress();
	}

	/**
	 * Returns the Properties object of this MaDKit session. That is by default
	 * the parameter which has been used to launch the kernel the agent
	 * is running on. If the agent has not been launched yet, the
	 * Properties returned is the default MaDKit configuration.
	 * It can be programmatically modified to launch a
	 * new session with different parameters. It can also be used as a
	 * black board shared by all the agents of a kernel by adding
	 * new user defined properties at run time or via the command line. 
	 * The default set of MaDKit properties includes
	 * values for the following keys:
	 * <table summary="Shows madkit keys and associated values">
	 * <tr>
	 * <th>Key</th>
	 * <th>Description of Associated Value</th>
	 * </tr>
	 * <tr>
	 * <td><code>madkit.version</code></td>
	 * <td>MaDKit kernel version</td>
	 * </tr>
	 * <tr>
	 * <td><code>build.id</code></td>
	 * <td>MaDKit kernel build ID</td></tr
	 * <tr>
	 * <td><code>madkit.repository.url</code></td>
	 * <td>the agent repository for this version, usually http://www.madkit.net/repository/MaDKit-${madkit.version}/ </td>
	 * </tr>
	 * <tr>
	 * <td><code>desktop</code></td>
	 * <td><code>true</code> or <code>false</code>: Launch the desktop during boot phase</td>
	 * </tr>
	 * <tr>
	 * <td><code>launchAgents</code></td>
	 * <td>The agents launched during the boot phase</td>
	 * </tr>
	 * <tr>
	 * <td><code>createLogFiles</code></td>
	 * <td>true</code> or <code>false</code>: Create log files automatically for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>logDirectory</code></td>
	 * <td>The directory used for the log files (./logs by default)</td>
	 * </tr>
	 * <tr>
	 * <td><code>agentLogLevel</code></td>
	 * <td>the default log level for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>warningLogLevel</code></td>
	 * <td>the default warning log level for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>network</code></td>
	 * <td><code>true</code> or <code>false</code>: Launch the network during boot phase</td>
	 * </tr>
	 * </table>
	 * <p>
	 * 
	 * 
	 * @return the Properties object defining the values of each MaDKit options
	 *         in the current session.
	 * @see Option LevelOption BooleanOption 
	 * @since MaDKit 5.0.0.10
	 */
	public Properties getMadkitConfig() {
		return getKernel().getMadkitConfig();
	}

	/**
	 * Called by the logged kernel to see if it is worth to build the log message
	 * 
	 * @return if it is is worth to build the log message
	 */
	final boolean isFinestLogOn() {
		if (logger != null) {
			return !(Level.FINEST.intValue() < logger.getLevel().intValue());
		}
		// As it is called by the logged kernel
		// updating the level accordingly -> the user has set logger to null
		// himself
		setLogLevel(Level.OFF);
		return false;
	}

	/**
	 * Returns the kernel address on which the agent is running.
	 * 
	 * @return the kernel address representing the MaDKit kernel on which the
	 *         agent is running
	 */
	public KernelAddress getKernelAddress() {
		return getKernel().getKernelAddress();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Utilities
	// //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	final boolean logLifeException(final Throwable e) {
		if (e instanceof KilledException || e instanceof IllegalMonitorStateException) {
			if (logger != null)
				logger.warning("-*-GET KILLED in " + getState().lifeCycleMethod() + "-*-");
		}
		else {
			if (alive.get()) {
				getLogger().severeLog(
						"-*-" + getState().lifeCycleMethod() + " BUG*-*", e);
			}
		}
		return false;
	}

	final String getLoggingName() {
		if (name != null)
			return "[" + name + "]";
		return "[" + getClass().getSimpleName() + "-" + _hashCode + "]";
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Synchronization
	// //////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * @since MaDKit 5.0.0.9
	 */
	private Message waitingNextMessage(final long timeout, final TimeUnit unit) {
		try {
			return messageBox.poll(timeout, unit);
		} catch (InterruptedException e) {
			handleInterruptedException();
			return null;
		}
	}

	/**
	 * Wipes out an entire community at once. Mostly useful when doing simulated
	 * systems. This greatly optimizes the time required to make all the agents
	 * leave a community.
	 * 
	 * @since MaDKit 5.0.0.9
	 * @param community
	 *           the community to destroy
	 */
	public void destroyCommunity(String community) {
		getKernel().destroyCommunity(this, community);
	}

	/**
	 * Wipes out an entire group at once. Mostly useful when doing simulated
	 * systems. This greatly optimizes the time required to make all the agents
	 * leave a group.
	 * 
	 * @since MaDKit 5.0.0.10
	 * @param community
	 *           the community
	 * @param group
	 *           the group to destroy
	 */
	public void destroyGroup(String community, String group) {
		getKernel().destroyGroup(this, community, group);
	}

	/**
	 * Wipes out an entire role at once. Mostly useful when doing simulated
	 * systems. This greatly optimizes the time required to make all the agents
	 * leave a role.
	 * 
	 * @since MaDKit 5.0.0.10
	 * @param community
	 *           the community
	 * @param group
	 *           the group
	 * @param role
	 *           the group to destroy
	 */
	public void destroyRole(String community, String group, String role) {
		getKernel().destroyRole(this, community, group, role);
	}

	/**
	 * @since MaDKit 5.0.0.9
	 */
	List<Message> waitAnswers(final Message message, final int size, final Integer timeOutMilliSeconds) {
		final long endTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds);
		final long conversationID = message.getConversationID();
		int missing = size;
		final List<Message> receptions = new ArrayList<Message>(messageBox.size());
		final List<Message> answers = new ArrayList<Message>(size);
		while (missing > 0 && System.nanoTime() < endTime) {
			Message answer = waitingNextMessage(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
			if (answer == null)
				break;
			if (answer.getConversationID() == conversationID) {
				answers.add(answer);
				missing--;
			}
			else
				receptions.add(answer);
		}
		if (!receptions.isEmpty()) {
			synchronized (messageBox) {
				messageBox.addAll(receptions);
			}
		}
		if (!answers.isEmpty())
			return answers;
		return null;
	}

	/**
	 * Logs and propagates the exception so that agents properly leave when
	 * interrupted. When you have to deal with such an exception and do not want
	 * to add <code>throws InterruptedException</code> in your code, it is both
	 * important for the responsiveness of your application and a good practice
	 * to not swallow it by doing something like.
	 * 
	 * <pre>
	 * <code>
	 * try {
	 * 	...something that can throw an InterruptedException
	 * } catch (InterruptedException e) {
	 * 	e.printStackTrace(); //swallowing it
	 * }
	 * </code>
	 * </pre>
	 * 
	 * So this method is a shortcut for :
	 * 
	 * <pre>
	 * <code>		
	 * if(logger != null){
	 * 	logger.log(Level.WARNING," Interrupted (killed) by ",e);
	 * }
	 * Thread.currentThread().interrupt();
	 * </code>
	 * </pre>
	 * 
	 * and should be used like this :
	 * 
	 * <pre>
	 * <code>		
	 * try {
	 * 	...something that can throw an InterruptedException
	 * } catch (InterruptedException e) {
	 * 	handleInterruptedException(e);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param e
	 *           the InterruptedException which has to be propagated
	 * @since MaDKit 5.0.0.12
	 */
	final void handleInterruptedException() {// TODO
		if (Thread.currentThread().getName().equals(getAgentThreadName(getState())) && alive.compareAndSet(true, false))
			throw new SelfKillException("" + 0);// TODO why 0 ?
		Thread.currentThread().interrupt();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Using Agent Address
	// /////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Agent SimulationState
	// //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * An agent state. An agent can be in one of the following states:
	 * <ul>
	 * <li>{@link #NOT_LAUNCHED}<br>
	 * An agent which has not yet been launched is in this state.</li>
	 * <li>{@link #INITIALIZING}<br>
	 * An agent that has been launched but which has not started its
	 * {@link #activate()} method yet is in this state.</li>
	 * <li>{@link #ACTIVATED}<br>
	 * An agent that is processing its {@link #activate()} method is in this
	 * state.</li>
	 * <li>{@link #LIVING}<br>
	 * An agent that is processing its {@link Agent#live()} method is in this
	 * state.</li>
	 * <li>{@link #ENDING}<br>
	 * An agent that is processing its {@link #end()} method is in this state.</li>
	 * <li>{@link #TERMINATED}<br>
	 * An agent which has finished its life cycle is in this state.</li>
	 * </ul>
	 * 
	 * <p>
	 * An agent can be in only one state at a given point in time.
	 * 
	 * @author Fabien Michel
	 * @since MaDKit 5.0
	 * @see #getState
	 */
	public enum State {

		/**
		 * The agent has not been launched yet.
		 */
		NOT_LAUNCHED,

		/**
		 * The agent has been launched and is being registered by the kernel but
		 * it has not started its {@link #activate()} method yet.
		 */
		INITIALIZING,

		/**
		 * The agent is processing its {@link #activate()} method. This state is
		 * also the "running" state of {@link AbstractAgent} subclasses (i.e. when
		 * they have finished their activation) as they do not have a
		 * {@link Agent#live()} managed by the kernel in their life cycle. On the
		 * contrary to {@link Agent} subclasses which next state is
		 * {@link #LIVING}).
		 */
		ACTIVATED,

		/**
		 * The agent is processing its {@link Agent#live()} method.
		 */
		LIVING,

		/**
		 * The agent is processing its {@link AbstractAgent#end()} method.
		 */
		ENDING,

		/**
		 * The agent has finished its life cycle in the MaDKit platform.
		 */
		TERMINATED;

		final String lifeCycleMethod() {
			switch (this) {
			case ACTIVATED:
				return "ACTIVATE";
			case LIVING:
				return "LIVE";
			case TERMINATED:
				return "TERMINATE";
			case ENDING:
				return "END";
			default:
				return name();
			}
		}
	}

	/**
	 * Returns the current state of the agent in the MaDKit platform.
	 * 
	 * This method provides a way of knowing what is the current state of the
	 * agent regarding its life cycle. This could be convenient when you design a
	 * method that could work differently depending on the actual state of the
	 * agent.
	 * 
	 * @return the current state of the agent:
	 *         <ul>
	 *         <li><code>{@link State#NOT_LAUNCHED}</code>: the agent has not
	 *         been launched yet. This especially means that most of the methods
	 *         of this API still do not work for this agent as it has not been
	 *         registered yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#INITIALIZING}</code>: the agent is being
	 *         registered by the kernel but has not started its
	 *         {@link #activate()} method yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#ACTIVATED}</code>: the agent is processing
	 *         its {@link #activate()} method. This state is also the "running"
	 *         state of {@link AbstractAgent} subclasses (i.e. when they have
	 *         finished their activation) as they do not have a
	 *         {@link Agent#live()} managed by the kernel in their life cycle. On
	 *         the contrary to {@link Agent} subclasses which next state is
	 *         {@link State#LIVING}.</li>
	 *         <br/>
	 *         <li><code>{@link State#LIVING}</code>: returned when {@link Agent}
	 *         subclasses are processing their {@link Agent#live()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#ENDING}</code>: the agent is processing its
	 *         {@link #end()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#TERMINATED}</code>: the agent has finished
	 *         its life in the MaDKit platform. Especially, most of the methods
	 *         of this API will no longer work for this agent.</li>
	 *         </ul>
	 * 
	 */
	public State getState() {
		return state.get();
	}

	/**
	 * Kills the caller and launches a new instance of this agent using the latest
	 * byte code available for the corresponding class.
	 */
	public void reload() {
		launchAgent(getClass().getName(), 0, true);
		killAgent(this);
	}

	/**
	 * Proceeds an {@link EnumMessage} so that if it is correctly built, the
	 * agent will trigger its corresponding behavior using the parameters of the
	 * message.
	 * 
	 * @param message
	 *           the message to proceed
	 * @since MaDKit 5.0.0.14
	 * @see EnumMessage
	 */
	public <E extends Enum<E>> void proceedEnumMessage(EnumMessage<E> message) {
		if (logger != null)
			logger.finest("proceeding command message " + message);
		Object[] parameters = message.getContent();
		Method m = null;
		try {
			m = findMethodFromParameters(ActionInfo.enumToMethodName(message.getCode()), parameters);
			m.invoke(this, parameters);
		} catch (Error e) {
			throw e;
		} catch (NoSuchMethodException e) {
			if (logger != null)
				logger.warning("I do not know how to " + ActionInfo.enumToMethodName(message.getCode())
						+ Arrays.deepToString(parameters));
			logForSender("I have sent a message which has not been understood", message);//TODO i18n
		} catch (IllegalArgumentException e) {
			if (logger != null)
				logger.warning("Cannot proceed message : wrong argument " + m);
			logForSender("I have sent an incorrect command message ", message);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {// TODO dirty : think about that
			Throwable t = e.getCause();
			if (t instanceof SelfKillException) {
				throw (SelfKillException) t;
			}
		}
	}

	private void logForSender(String msg, EnumMessage<?> cm) {
		try {
			cm.getSender().getAgent().logger.warning(msg + cm);
		} catch (NullPointerException e1) {
			// logger is off or sender is null
		}
	}

	private Method findMethodFromParameters(String name2, Object[] parameters) throws NoSuchMethodException {
		Method m;
		final Class<?>[] types = convertToObjectTypes(convertToTypes(parameters));
		m = findMethodIn(name2, getClass().getMethods(), types);
		if (m == null) {
			m = findMethodIn(name2, getClass().getDeclaredMethods(), types);
			if (m != null)
				m.setAccessible(true);
		}
		if (m == null)
			throw new NoSuchMethodException();
		return m;
	}

	private Class<?>[] convertToObjectTypes(final Class<?>[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			final Class<?> paramCl = parameters[i];
			if (paramCl != null && paramCl.isPrimitive()) {
				parameters[i] = primitiveTypes.get(paramCl);
			}
		}
		return parameters;
	}

	private Class<?>[] convertToTypes(final Object[] parameters) {
		final Class<?>[] paramClasses = new Class<?>[parameters.length];
		for (int i = 0; i < paramClasses.length; i++) {
			if (parameters[i] != null) {
				paramClasses[i] = parameters[i].getClass();
			}
		}
		return paramClasses;
	}

	private Method findMethodIn(String name2, Method[] methods, Class<?>[] parameters) {
		for (Method method : methods) {
			if (method.getName().equals(name2) && checkArgumentTypes(convertToObjectTypes(method.getParameterTypes()), parameters)) {
				return method;
			}
		}
		return null;
	}

	private boolean checkArgumentTypes(Class<?>[] types, Class<?>[] parameters) {
		if (parameters.length == types.length) {
			for (int i = 0; i < types.length; i++) {
				if (parameters[i] != null && !types[i].isAssignableFrom(parameters[i])) {
					// System.err.println("\nNot equals "+types[i]+" against "+parameters[i]+"\n");
					return false;
				}
			}
			return true;
		}
		return false;
	}

	final private static Map<Class<?>, Class<?>>	primitiveTypes	= new HashMap<Class<?>, Class<?>>();
	static {
		primitiveTypes.put(int.class, Integer.class);
		primitiveTypes.put(boolean.class, Boolean.class);
		primitiveTypes.put(byte.class, Byte.class);
		primitiveTypes.put(char.class, Character.class);
		primitiveTypes.put(float.class, Float.class);
		primitiveTypes.put(void.class, Void.class);
		primitiveTypes.put(short.class, Short.class);
		primitiveTypes.put(double.class, Double.class);
		primitiveTypes.put(long.class, Long.class);
	}

	public MadkitClassLoader getMadkitClassLoader() {// TODO log if no kernel
		return getMadkitKernel().getMadkitClassLoader();
	}

	// /**
	// * @return an Executor which could be used to do tasks asynchronously
	// */
	// public Executor getMadkitExecutor(){
	// return kernel.getMadkitKernel().getMadkitExecutor();
	// }

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Return codes
	// //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * This class enumerates all the return codes which could be obtained with
	 * essential methods of the {@link AbstractAgent} and {@link Agent} classes.
	 * 
	 * @author Fabien Michel
	 * @since MaDKit 5.0
	 * 
	 */
	public enum ReturnCode {

		SUCCESS,
		/**
		 * Indicates that a community does not exist
		 */
		NOT_COMMUNITY,
		/**
		 * Indicates that a group does not exist
		 */
		NOT_GROUP,
		/**
		 * Indicates that a role does not exist
		 */
		NOT_ROLE,
		/**
		 * Indicates that the agent is not in a group
		 */
		NOT_IN_GROUP,
		// TERMINATED_AGENT,
		/**
		 * Returned when the agent already has the requested role
		 */
		ROLE_ALREADY_HANDLED,
		/**
		 * Returned when requesting a role in a secured group fails
		 */
		ACCESS_DENIED,
		/**
		 * Returned when the agent does not have
		 * a role that it is supposed to have doing a
		 * particular action, e.g.
		 * {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)}
		 */
		ROLE_NOT_HANDLED,
		/**
		 * Returned when using
		 * {@link AbstractAgent#createGroup(String, String, boolean, Gatekeeper)}
		 * and that a group already exists
		 */
		ALREADY_GROUP,
		/**
		 * Returned when launching an agent which is already launched
		 */
		ALREADY_LAUNCHED,
		/**
		 * Returned by various timed primitives of the Agent class like
		 * {@link Agent#sendMessageAndWaitForReply(AgentAddress, Message)} or
		 * {@link AbstractAgent#launchAgent(AbstractAgent, int, boolean)}
		 */
		TIMEOUT,
		/**
		 * Returned by launch primitives when the launched agent
		 * crashes in activate
		 */
		AGENT_CRASH,
		// NOT_AN_AGENT_CLASS,
		/**
		 * Returned by kill primitives when the targeted
		 * agent has not been launched priorly
		 */
		NOT_YET_LAUNCHED,
		/**
		 * Returned by kill primitives when the targeted
		 * agent is already terminated
		 */
		ALREADY_KILLED,
		/**
		 * Returned by send primitives when the targeted agent address
		 * does not exist anymore, i.e. the related agent has leaved
		 * the corresponding role
		 */
		INVALID_AGENT_ADDRESS,
		/**
		 * Returned by send primitives when the targeted CGR location
		 * does not exist or contain any agent
		 */
		NO_RECIPIENT_FOUND,
		/**
		 * Returned when
		 * {@link AbstractAgent#requestRole(String, String, String, Object)} or
		 * {@link AbstractAgent#createGroup(String, String, boolean, Gatekeeper)}
		 * is used in activate and that the agent has been launched using
		 * {@link AbstractAgent#launchAgentBucket(List, String...)} or
		 * {@link AbstractAgent#launchAgentBucket(String, int, String...)} 
		 * </li>
		 */
		IGNORED,
		/**
		 * Returned on special errors. This should not
		 * be encountered
		 */
		SEVERE;

		// NETWORK_DOWN;

		final static ResourceBundle	messages	= I18nUtilities.getResourceBundle(ReturnCode.class.getSimpleName());

		// static ResourceBundle messages =
		// I18nUtilities.getResourceBundle(ReturnCode.class);

		public String toString() {
			return messages.getString(name());
		}
	}

	enum Influence {
		CREATE_GROUP,
		REQUEST_ROLE,
		LEAVE_ROLE,
		LEAVE_GROUP,
		GET_AGENTS_WITH_ROLE,
		GET_AGENT_WITH_ROLE,
		SEND_MESSAGE,
		BROADCAST_MESSAGE,
		BROADCAST_MESSAGE_AND_WAIT,
		LAUNCH_AGENT,
		KILL_AGENT,
		GET_AGENT_ADDRESS_IN,
		RELOAD_CLASS;

		public String failedString() {
			return toString() + Words.FAILED + " : ";
		}

		@Override
		public String toString() {
			return name() + " ";
		}

		String successString() {
			return toString() + SUCCESS + " : ";
		}
	}

	/**
	 * Tells if the kernel on which this agent is running is online.
	 * 
	 * @return <code>true</code> if the kernel is online.
	 */
	public boolean isKernelOnline() {
		// bypassing logging
		return getMadkitKernel().isRole(this, CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS,
				CloudCommunity.Roles.NET_AGENT);
	}

	AgentExecutor getAgentExecutor() {
		return null;
	}

	/**
	 * This offers a convenient way to create main a main method that launches the agent
	 * class under development. The agent is launched in a new instance MaDKit.
	 * This call only works in the main method of the agent.
	 * MaDKit. Here is an example of use that will work in any subclass of {@link AbstractAgent}:
	 * 
	 * <pre>
	 * <code>
	 * public static void main(String[] args) {
	 * 	AbstractAgent.executeThisAgent(args);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Still, the agent must have a default constructor for that to work.
	 * 
	 * @param nbOfInstances
	 *           specify how many of this kind should be launched
	 * 
	 * @param createFrame
	 * @param args
	 *           MaDKit options. For example, this will launch the agent in
	 *           desktop mode :
	 *           <pre>
	 * <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(BooleanOption.desktop.toString());
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @see Option BooleanOption LevelOption
	 * @since MaDKit 5.0.0.14
	 */
	@SuppressWarnings("unused")
	public static void executeThisAgent(int nbOfInstances, boolean createFrame, String... args) {
		final StackTraceElement[] trace = new Throwable().getStackTrace();
		final ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(Madkit.Option.launchAgents.toString(),
				trace[trace.length - 1].getClassName() + "," + (createFrame ? "true" : "false") + "," + nbOfInstances));
		if (args != null) {
			arguments.addAll(Arrays.asList(args));
		}
		new Madkit(arguments.toArray(new String[0]));
	}

	/**
	 * This offers a convenient way to create a main method 
	 * that launches the agent
	 * class under development. 
	 * This call only works in the main method of the agent.
	 * This call is equivalent to
	 * <code>executeThisAgent(1, true, args)</code>
	 * 
	 * @param args
	 *           MaDKit options
	 * @see #executeThisAgent(int, boolean, String...)
	 * @since MaDKit 5.0.0.14 
	 */
	protected static void executeThisAgent(String... args) {
		executeThisAgent(1, true, args);
	}

	/**
	 * This offers a convenient way to create a main method that launches the agent
	 * class under development. 
	 * This call only works in the main method of the agent.
	 * This call is equivalent to
	 * <code>executeThisAgent(null, 1, true)</code>
	 * 
	 * @see #executeThisAgent(int, boolean, String...)
	 * @since MaDKit 5.0.0.15
	 */
	protected static void executeThisAgent() { 
		executeThisAgent(1, true);
	}

}