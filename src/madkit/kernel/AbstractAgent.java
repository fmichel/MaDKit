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

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.ACTIVATED;
import static madkit.kernel.AbstractAgent.State.ENDING;
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.TERMINATED;

import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.omg.PortableInterceptor.SUCCESSFUL;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.agr.Organization;
import madkit.gui.GUIMessage;
import madkit.gui.OutputPanel;
import madkit.gui.actions.MadkitActions;
import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;

/**
 * The super class of all MadKit agents, v 5.
 * It provides support for
 * <ul>
 * <li>Agent's Life cycle, logging, and naming.
 * <li>Agent launching and killing.
 * <li>Artificial society creation and management.
 * <li>Messaging.
 * <li>Minimal graphical interface management.
 * </ul>
 * <p>
 * The agent's behavior is <i>intentionally not defined</i>. It is up to the agent developer to choose an agent model or to develop his specific agent library on top of the facilities provided by the
 * MadKit API. However, all the launched agents share the same organizational view, and the basic messaging code, so integration of different agents is quite easy, even when they are coming from
 * different developers or have heterogeneous models.
 * <p>
 * Agent-related methods (most of this API) is only effective after the agent has been launched and thus registered in the current Madkit session. Especially, that means that most of the API has no
 * effect in the constructor method of an Agent and will only produce a warning if used.
 * <p>
 * <h2>MadKit v.5 new features</h2>
 * <p>
 * <ul>
 * <li>One of the big change that comes with version 5 is how agents are identified and localized within the artificial society. An agent is no longer binded to a single agent address but has as many
 * agent addresses as holden positions in the artificial society. see {@link AgentAddress} for more information.</li>
 * <br>
 * <li>With respect to the previous change, a <code><i>withRole</i></code> version of all the messaging methods has been added. See {@link #sendMessageWithRole(AgentAddress, Message, String)} for an
 * example of such a method.</li>
 * <br>
 * <li>A replying mechanism has been introduced through <code><i>sendReply</i></code> methods. It enables the agent with the possibility of replying directly to a given message. Also, it is now
 * possible to get the reply to a message, or to wait for a reply ( for {@link Agent} subclasses only as they are threaded) See {@link #sendReply(Message, Message)} for more details.</li>
 * <br>
 * <li>Agents now have a <i>formal</i> state during a MadKit session. See the {@link #getState()} method for detailed information.</li>
 * <br>
 * <li>One of the most convenient improvement of v.5 is the logging mechanism which is provided. See the {@link #logger} attribute for more details.</li>
 * <p>
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @version 5.1
 */
public class AbstractAgent implements Comparable<AbstractAgent>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5267125141901955998L;

	private final static transient AtomicInteger agentCounter = new AtomicInteger(-1);

	static final transient MadkitKernel FAKE_KERNEL = new FakeKernel(null);

	final static transient AgentLogger defaultLogger = AgentLogger.defaultAgentLogger;

	final AtomicReference<State> state = new AtomicReference<AbstractAgent.State>(AbstractAgent.State.NOT_LAUNCHED);
	transient MadkitKernel kernel;

	final private int _hashCode;

	boolean hasGUI;
	/**
	 * name is lazy created to save memory
	 */
	private String name;
	final private AtomicBoolean alive = new AtomicBoolean();// default is false
	final LinkedBlockingDeque<Message> messageBox = new LinkedBlockingDeque<Message>();// TODO lazy creation

	/**
	 * <code>logger</code> can be used to trace the agent's life cycle.
	 * 
	 * <pre>
	 * if (logger != null)
	 * 	logger.info(&quot;info message&quot;);
	 * </pre>
	 * 
	 * @see java.util.logging.Logger
	 */
	protected AgentLogger logger = defaultLogger;

	public AbstractAgent() {
		_hashCode = agentCounter.getAndIncrement();//TODO bench outside
		kernel = FAKE_KERNEL;
	}

	final void activateGUI(){
		hasGUI = true;
	}

	final public boolean hasGUI(){
		return hasGUI;
	}


	/**
	 * Returns the ID of an agent. All the agents have different 
	 * hashCode value in one kernel. Thus it can be used to identify one agent.
	 * In a networked environment, this value should be used in combination with
	 * the kernelAddress of the agent for unique identification it 
	 * This also holds when multiple MadKit kernels are launched within the same JVM.
	 * 
	 * @return the agent's unique ID in the MadKit kernel
	 */
	@Override
	final public int hashCode() {// TODO should be regenerated if agent are sent through the network
		return _hashCode;
	}

	// /**
	// * Each agent reference has a unique ID which also corresponds to the value returned by hashCode()
	// * So this method simply compares agents' ID.
	// * @return true if both references target the same agent
	// * @see java.lang.Object#equals(java.lang.Object)
	// * @see java.lang.Object#hashCode()
	// */
	// @Override
	// public boolean equals(Object obj) {
	// if(obj instanceof AbstractAgent)
	// return obj.hashCode() == _hashCode;
	// return false;
	// }

	// /**
	// * @return the kernel
	// */
	// final AtomicReference<Kernel> getKernel() {
	// return kernel;
	// }

	/**
	 * @return the dying
	 */
	final AtomicBoolean getAlive() {
		return alive;
	}

	/**
	 * tells if the agent is still registered in the current MadKit platform
	 * 
	 * @return true if the agent has been launched and not yet terminated, false otherwise
	 */
	public boolean isAlive() {
		return getAlive().get();
	}

	// //////////////////////////////////////////// LIFE CYCLE
	boolean activation() {
		//TODO I18N
		if (!state.compareAndSet(INITIALIZING, ACTIVATED))// TODO remove it when OK
			throw new AssertionError("not init in activation");
		try {
			if(hasGUI){
				if(logger != null){
					logger.finer("** setting up  GUI **");
				}
				requestRole(LocalCommunity.NAME, Groups.SYSTEM, "default");//TODO think about that
				kernel.getMadkitKernel().broadcastMessageWithRoleAndWaitForReplies(
						this,
						LocalCommunity.NAME, 
						Groups.SYSTEM, 
						Roles.GUI_MANAGER, 
						new GUIMessage(MadkitActions.AGENT_SETUP_GUI,this), 
						null, 
						10000);
			}
			if (logger != null) {
				logger.finer("** entering ACTIVATE **");
			}
			activate();
		} 
		catch (KilledException e) {//self kill
			getAlive().set(false);
			if (logger != null) {
				logger.warning("-*-GET KILLED in ACTIVATE-*- "+e.getMessage());
				logger.finer("** exiting ACTIVATE **");
			}
			ending();
			terminate();
			return false;
		} catch (Throwable e) {
			logLifeException(e);
			logger.finer("** exiting ACTIVATE **");//logger cannot be null here
			// TODO no more ending if activate failed ???
			if (getAlive().get()) {
				ending();
				terminate();
			}
			return false;
		}
		if (logger != null) {
			logger.finer("** exiting ACTIVATE **");
		}
		return true;
	}

	/**
	 * This method corresponds to the first behavior which is called by the MadKit kernel when an agent is launched.
	 * Usually a good place to take a position in the organization of the artificial society.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * 
	 * <pre>
	 *  <tt>      @Override</tt>
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
		//		if(logger != null)
		//			logger.talk("\n\tHello World !!\n\n\tI am a MadKit abstract agent\n\tand I am not threaded.\n\tYou can extend me to do large scale simulation");
	}

	boolean ending() { // TODO boolean need ?
		if (state.get() == ENDING)// TODO remove that
			throw new AssertionError("ENDING twice " + getName());
		state.set(ENDING);
		if (logger != null) {
			logger.finer("** entering END **");
		}
		try {
			end();
		} catch (KilledException e) {
			if (logger != null && getAlive().get()) {//not killed before
				logger.warning("-*-GET KILLED in END-*- " + e.getMessage());
			}
			return false;
		} catch (Throwable e) {
			logLifeException(e);
			return false;
		} finally {
			getAlive().set(false);
			if (logger != null) {
				logger.finer("** exiting END **");// TODO display it or not in case of kill ?
			}
		}
		return true;
	}

	/**
	 * Calls when the life cycle quits
	 */
	void terminate() {
		getAlive().set(false);
		setKernel(kernel.getMadkitKernel());
		if (hasGUI) {
			kernel.broadcastMessageWithRole(this, LocalCommunity.NAME, Groups.SYSTEM, Roles.GUI_MANAGER, new GUIMessage(
					MadkitActions.AGENT_DISPOSE_GUI, this), null);
		}
		if (getState().equals(TERMINATED))// TODO remove that
			throw new AssertionError("terminating twice " + getName());
		try {
			kernel.removeAgentFromOrganizations(this);// TODO catch because of probe/activator
		} catch (Throwable e) {
			logLifeException(e);
		}
		state.set(TERMINATED);
		messageBox.clear(); // TODO test speed and no need for that
		if (logger != null) {
			logger.finest("** TERMINATED **");
		}
		kernel = FAKE_KERNEL;//TODO kernel for terminated agent
	}

	/**
	 * This method corresponds to the last behavior which is called by the MadKit kernel.
	 * This call occurs when a threaded agent normally exits its live method or when the agent is killed.
	 * Usually a good place to release taken resources or log what has to be logged.
	 * 
	 * It has to be noted that the kernel automatically take care of removing
	 * the agent from the organizations it is in. However, this cleaning is not logged by the agent.
	 * Therefore it could be of interest for the agent to do that itself.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * 
	 * <pre>
	 *  <tt>      @Override</tt>
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
	 * Launches a new agent in the MadKit platform.
	 * This has the same effect as <code>launchAgent(agent,Integer.MAX_VALUE,false)</code>
	 * 
	 * @param agent the agent to launch.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @throws NullPointerException if <code>agent</code> is <code>null</code>
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent) throws KernelException{
		return launchAgent(agent, Integer.MAX_VALUE, false);
	}

	/**
	 * Launches a new agent in the MadKit platform.
	 * This has the same effect as <code>launchAgent(agent,timeOutSeconds,false)</code>
	 * 
	 * @param agent the agent to launch.
	 * @param timeOutSeconds time to wait the end of the agent's activation until returning a TIMEOUT.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation time of the agent is greater than <code>timeOutSeconds</code> seconds</li>
	 *         <li><code>{@link ReturnCode#AGENT_CRASH}</code>: If the agent crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @throws NullPointerException if <code>agent</code> is <code>null</code>
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final int timeOutSeconds) {
		return launchAgent(agent, timeOutSeconds, false);
	}

	/**
	 * Launches a new agent in the MadKit platform.
	 * This has the same effect as <code>launchAgent(agent,Integer.MAX_VALUE,withGUIManagedByTheBooter)</code>
	 * 
	 * @param agent the agent to launch.
	 * @param createFrame if <code>true</code>, the kernel will launch a JFrame for this agent.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @throws NullPointerException if <code>agent</code> is <code>null</code>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final boolean createFrame) {
		return launchAgent(agent, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent and returns when the agent has completed its {@link AbstractAgent#activate()} method
	 * or when <code>timeOutSeconds</code> seconds elapsed. That is, the launched agent has not finished
	 * its {@link AbstractAgent#activate()} before the time out time elapsed.
	 * Additionally, if <code>createFrame</code> is <code>true</code>, it tells
	 * to MadKit that an agent GUI should be managed by the Kernel. In such a case,
	 * the kernel takes the responsibility to assign
	 * a JFrame to the agent and to manage its life cycle (e.g. if the agent ends or is killed then the JFrame is closed)
	 * Using this feature there are two possibilities:
	 * <ul>
	 * <li>1. the agent overrides the method {@link AbstractAgent#setupFrame(JFrame)} and so setup the default JFrame as will</li>
	 * <li>2. the agent does not override it so that MadKit will setup the JFrame with the default Graphical component delivered by the MadKit platform: {@link OutputPanel}
	 * </ul>
	 * 
	 * @param agent the agent to launch.
	 * @param timeOutSeconds time to wait for the end of the agent's activation until returning a TIMEOUT.
	 * @param createFrame if <code>true</code>, the kernel will launch a JFrame for this agent.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation time of the agent is greater than <code>timeOutSeconds</code> seconds</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @throws NullPointerException if <code>agent</code> is <code>null</code>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final int timeOutSeconds, final boolean createFrame) {
		if(agent == null)
			throw new NullPointerException("agent");
		return kernel.launchAgent(this, agent, timeOutSeconds, createFrame);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as <code>launchAgent(agentClass, Integer.MAX_VALUE, false)</code>.
	 * 
	 * @param agentClass the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
	 * @throws NullPointerException if <code>agentClass</code> is <code>null</code>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public AbstractAgent launchAgent(String agentClass) {
		return launchAgent(agentClass, Integer.MAX_VALUE, false);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as <code>launchAgent(agentClass, timeOutSeconds, false)</code>.
	 * 
	 * @param timeOutSeconds time to wait the end of the agent's activation until returning <code>null</code>
	 * @param agentClass the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
	 * @throws NullPointerException if <code>agentClass</code> is <code>null</code>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds) {
		return launchAgent(agentClass, timeOutSeconds, false);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as <code>launchAgent(agentClass, Integer.MAX_VALUE, defaultGUI)</code>.
	 * 
	 * @param createFrame if <code>true</code> a default GUI will be associated with the launched agent
	 * @param agentClass the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
	 * @throws NullPointerException if <code>agentClass</code> is <code>null</code>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public AbstractAgent launchAgent(String agentClass, final boolean createFrame){
		return launchAgent(agentClass, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent using its full class name and returns
	 * when the launched agent has completed its {@link AbstractAgent#activate()} method
	 * or when the time out is elapsed.
	 * This has the same effect as {@link #launchAgent(AbstractAgent, int, boolean)} but
	 * allows to launch agent using a class name found reflexively for instance.
	 * Additionally, this method will launch the last compilation of the corresponding
	 * class if it has been reloaded using {@link AbstractAgent#reloadAgentClass(String)}.
	 * Finally, if the launch timely succeeded, this method returns the instance of the
	 * created agent.
	 * 
	 * @param timeOutSeconds time to wait the end of the agent's activation until returning <code>null</code>
	 * @param createFrame if <code>true</code> a default GUI will be associated with the launched agent
	 * @param agentClass the full class name of the agent to launch
	 * @throws NullPointerException if <code>agentClass</code> is <code>null</code>
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds, final boolean createFrame) {
		if(logger != null)
			logger.finest(Words.LAUNCH+" " + agentClass);
		try {
			final AbstractAgent a = (AbstractAgent) getMadkitClassLoader().loadClass(agentClass).newInstance();
			if(ReturnCode.SUCCESS == launchAgent(a, timeOutSeconds, createFrame))
				return a;
		} catch (InstantiationException e) {
			final String msg = ErrorMessages.CANT_LAUNCH + agentClass + " because it has no default constructor ";
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, msg,
							"Launch failed", JOptionPane.WARNING_MESSAGE);
				}
			});
			getLogger().severeLog(ErrorMessages.CANT_LAUNCH + agentClass + " because it has no default constructor "+e.getMessage(),e);
		} catch (IllegalAccessException e) {
			getLogger().severeLog(ErrorMessages.CANT_LAUNCH + agentClass, e);
		} catch (ClassCastException e) {
			getLogger().severeLog(ErrorMessages.CANT_LAUNCH + agentClass + " : not an agent class", e);
		} catch (ClassNotFoundException e) {
			getLogger().severeLog(ErrorMessages.CANT_LAUNCH +" "+agentClass+" : ", e);
		}
		return null;
	}

	/**
	 * Launches <i><code>bucketSize</code></i> agent instances of this <i><code>agentClassName</code></i>.
	 * This has the same effect as <code>launchAgentBucketWithRoles(agentClassName, bucketSize, (Collection<String>) null)</code>.
	 * 
	 * @param agentClassName the name of the class from which the agents should be built.
	 * @param bucketSize the desired number of agents.
	 * @return a list containing all the agents which have been launched,
	 *         or <code>null</code> if the operation failed
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public List<AbstractAgent> launchAgentBucket(String agentClassName, int bucketSize) {
		return launchAgentBucketWithRoles(agentClassName, bucketSize, (Collection<String>) null);
	}

	/**
	 * Launches <i><code>bucketSize</code></i> agent instances of this <i><code>agentClassName</code></i>
	 * and put them at certain location in the artificial society.
	 * 
	 * @param agentClassName the name of the class from which the agents should be built.
	 * @param bucketSize the desired number of agents.
	 * @return a list containing all the agents which have been launched,
	 *         or <code>null</code> if the operation failed
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	// TODO javadoc examples
	public List<AbstractAgent> launchAgentBucketWithRoles(String agentClassName, int bucketSize, Collection<String> rolesName) {
		return kernel.launchAgentBucketWithRoles(this, agentClassName, bucketSize, rolesName);
	}

	/**
	 * Kills the targeted agent.
	 * This has the same effect as <code>killAgent(target,Integer.MAX_VALUE)</code> so that
	 * the targeted agent has a lot of time to complete its {@link #end()} method.
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the group does not exist.</li>
	 *         </ul>
	 * @since MadKit 5.0
	 * @see #killAgent(AbstractAgent, int)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public ReturnCode killAgent(final AbstractAgent target) {
		return killAgent(target, Integer.MAX_VALUE);
	}

	/**
	 * Kills the targeted agent. The kill process stops the agent's life cycle but allows it to
	 * process its {@link #end()} method until the time out elapsed.
	 * This has the same effect as <code>requestRole(community, group, role, null)</code>
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the group does not exist.</li>
	 *         </ul>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode killAgent(final AbstractAgent target, final int timeOutSeconds) {
		if(target == this){//bypassing the warning log about time out when 0
			return kernel.getMadkitKernel().killAgent(this, target, timeOutSeconds);
		}
		//			throw new KilledException("by ["+getName()+"]");
		//			if(Thread.currentThread().getName().contains("MK_EXECUTOR")){
		//			}
		//			else{
		//				return kernel.killAgent(this, target, Integer.MAX_VALUE);
		//			}
		//		}
		return kernel.killAgent(this, target, timeOutSeconds);
		//		if (target.getState().compareTo(ACTIVATED) < 0) {
		//			return NOT_YET_LAUNCHED;
		//		}
		//		if (target.getState().equals(TERMINATED) || !target.alive.get()) {
		//			return ALREADY_KILLED;
		//		}
		//		final Future<ReturnCode> killAttempt = MadkitKernel.serviceExecutor.submit(new Callable<ReturnCode>() {
		//			public ReturnCode call() {
		//				return kernel.killAgent(AbstractAgent.this, target, timeoutSeconds);
		//			}
		//		});
		//		try {
		//			return killAttempt.get();
		//		} catch (InterruptedException e) {
		//			// get killed myself !!
		//			throw (new KilledException());
		//		} catch (ExecutionException e) {
		//			// logSevereException(logger, e, "during killing of "+target.getName()+" *********please bug report !!");
		//			throw (new KilledException());
		//		}
	}

	/**
	 * @return the real kernel
	 */
	final MadkitKernel getKernel() {
		return kernel;
	}

	/**
	 * @param kernel the kernel to set
	 */
	final void setKernel(MadkitKernel kernel) {
		if (kernel != null) {//TODO no need
			this.kernel = kernel;
		}
	}

	/**
	 * Returns the name of this agent.
	 * 
	 * @return the name to display in logger info, GUI title and so on.
	 *         Default is "<i>class name + internal ID</i>"
	 * 
	 */
	public String getName() {
		if (name == null)
			name = getClass().getSimpleName() + "-" + _hashCode;
		return name;
	}

	/**
	 * @param name the name to display in logger info, GUI title and so on, default is "class name + internal ID"
	 */
	public void setName(final String name) {// TODO trigger gui changes and so on
		if (! getName().equals(name)) {
			this.name = name;
		}
	}

	/**
	 * Sets the agent's log level
	 */
	public synchronized void setLogLevel(final Level newLevel) {
		if(Level.OFF == newLevel){
			if (logger != null && logger != defaultLogger) {
				logger.setLevel(newLevel);
			}
			logger = null;
			setKernel(kernel.getMadkitKernel());
		}
		else{
			getLogger().setLevel(newLevel);
			setKernel(kernel.getLoggedKernel());
		}
	}

	/**
	 * Returns the agent's logger.
	 * 
	 * @return the agent's logger. It cannot be <code>null</code> as it will be created
	 * if necessary. But you can still put {@link #logger} to <code>null</code>
	 * for optimizing your code by using {@link #setLogLevel(Level)} with {@link Level#OFF}.
	 * 
	 * @since MadKit 5.0.0.6
	 */
	public AgentLogger getLogger(){
		if(logger == defaultLogger || logger == null){
			logger = AgentLogger.getLogger(this);
		}
		return logger;
	}

	/**
	 * Compares this agent with the specified agent for order with respect to instantiation time.
	 * 
	 * @param other the agent to be compared.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @return a negative integer, a positive integer or zero as this agent
	 *         has been instantiated before, after or is the same agent than the specified agent.
	 * 
	 */
	@Override
	public int compareTo(final AbstractAgent other) {
		return _hashCode - other._hashCode;
	}

	// ///////////////////////////////////////////////////////// GROUP & ROLE METHODS AGR

	/**
	 * Creates a new Group within a community.
	 * This has the same effect as <code>createGroup(community, group, false, null)</code>
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed because such a group already exists.</li>
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MadKit 5.0
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public ReturnCode createGroup(final String community, final String group) {
		return createGroup(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community.
	 * This has the same effect as <code>createGroup(community, group, isDistributed, null)</code>
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed when multiple MadKit kernels are connected.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed because such a group already exists.</li>
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, Gatekeeper)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed) {
		return createGroup(community, group, isDistributed, null);
	}

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role defined by {@link Organization#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i>, in this created group. Especially, if the agent leaves the role of <i>{@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i>, it
	 * will also automatically leave the group and thus all the roles it has in this group.
	 * 
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>{@value madkit.agr.Organization#GROUP_MANAGER_ROLE}</i> using the role defined by {@link Organization#GROUP_CANDIDATE_ROLE},
	 * which value is <i>{@value madkit.agr.Organization#GROUP_CANDIDATE_ROLE}</i>.
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed when multiple MadKit kernels are connected.
	 * @param keyMaster any object that implements the {@link Gatekeeper} interface. If not <code>null</code>,
	 *        this object will be used to check if an agent can be admitted in the group.
	 *        When this object is null, there is no group access control.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully created.</li> 
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed because such a
	 *         group already exists.</li>
	 *         </ul>
	 * 
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed, final Gatekeeper keyMaster) {
		return kernel.createGroup(this, community, group, group, keyMaster, isDistributed);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if the group already exists.
	 * This has the same effect as <code>createGroupIfAbsent(community, group, false, null)</code>
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group.
	 * @return <code>true</code> if the group has been created, <code>false</code> if it was already present.
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean, Gatekeeper)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group) {
		return createGroupIfAbsent(community, group, false, null);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if the group already exists.
	 * This has the same effect as <code>createGroupIfAbsent(community, group, isDistributed, null)</code>
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group
	 * @param isDistributed if <code>true</code> the new group will be distributed when multiple MadKit kernels are connected.
	 * @return <code>true</code> if the group has been created, <code>false</code> if it was already present.
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean, Gatekeeper)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group, boolean isDistributed) {
		return createGroupIfAbsent(community, group, isDistributed, null);
	}

	/**
	 * Creates a new Group within a community but does not produce any warning if the group already exists.
	 * If this operation succeed, the agent will automatically handle the role of <i>group manager</i> in the
	 * created group.
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group
	 * @param isDistributed if <code>true</code> the new group will be distributed when multiple MadKit kernels are connected.
	 * @param keyMaster any object that implements the {@link Gatekeeper} interface. If not <code>null</code>,
	 *        this object will be used to check if an agent can be admitted in the group.
	 *        When this object is null, there is no group access control.
	 * @return <code>true</code> if the group has been created, <code>false</code> if it was already present.
	 *         </ul>
	 * 
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group, boolean isDistributed, final Gatekeeper keyMaster) {
		return kernel.createGroupIfAbsent(this, community, group, group, keyMaster, isDistributed);
	}

	/**
	 * Makes this agent leaves the group of a particular community.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of this group.</li>
	 *         </ul>
	 * 
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 * @see ReturnCode
	 */
	public ReturnCode leaveGroup(final String community, final String group) {
		return kernel.leaveGroup(this, community, group);
	}

	/**
	 * Requests a role within a group of a particular community.
	 * This has the same effect as <code>requestRole(community, group, role, null)</code>
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access denied by the manager of that secured group.</li>
	 *         </ul>
	 * @param community the group's community.
	 * @param group the desired group.
	 * @param role the desired role.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @see #requestRole(String, String, String, Object)
	 * @since MadKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role) {
		return requestRole(community, group, role, null);
	}

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 * 
	 * @param community the group's community.
	 * @param group the desired group.
	 * @param role the desired role.
	 * @param passKey the <code>passKey</code> to enter a secured group. It is generally delivered by the group's <i>group manager</i>.
	 *        It could be <code>null</code>, which is sufficient to enter an unsecured group.
	 *        Especially, {@link #requestRole(String, String, String)} uses a null <code>passKey</code>.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access denied by the manager of that secured group.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 * @since MadKit 5.0
	 */
	public ReturnCode requestRole(final String community, final String group, final String role, final Object passKey) {
		return kernel.requestRole(this, community, group, role, passKey);
	}

	/**
	 * Abandons an handled role within a group of a particular community.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role is not handled by this agent.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0
	 */
	public ReturnCode leaveRole(final String community, final String group, final String role) {
		return kernel.leaveRole(this, community, group, role);
	}


	final void handleException(final Influence i, final MadkitWarning e) {
		if (logger != null && 
				logger.getWarningLogLevel().intValue() >=	logger.getLevel().intValue()) {
			setAgentStackTrace(e);
			logger.log(Level.WARNING, i.failedString(), e);
		}
	}

	final boolean isWarningOn(){
		return logger != null && logger.getWarningLogLevel().intValue() >=	logger.getLevel().intValue();
	}

	void setAgentStackTrace(final Throwable e) {
		StackTraceElement[] stackTrace = e.getStackTrace();
		if (stackTrace.length > 0) {
			final List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
			//			stack.add(stackTrace[1]);
			String agentClassName = getClass().getName();
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
	 * This makes the distinction between AA and Agent
	 * 
	 * @return the lifeCycle
	 * @since MadKit 5.0.0.9
	 */
	List<Future<Boolean>> getMyLifeCycle() {
		return null;
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this position in the organization.
	 * The caller is excluded from the search.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @return an {@link AgentAddress} corresponding to an agent handling this role or <code>null</code> if such an agent does not exist.
	 */
	public AgentAddress getAgentWithRole(final String community, final String group, final String role) {
		return kernel.getAgentWithRole(this, community, group, role);
	}

	/**
	 * Returns an {@link java.util.List} containing agents that handle this role in the organization.
	 * The caller is excluded from this list.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @return a {@link java.util.List} containing agents that handle this role or <code>null</code> if no agent has been found.
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role) {
		return getAgentsWithRole(community, group, role, false);
	}

	/**
	 * Returns an {@link java.util.List} containing all the agents that handle this role in the organization.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param callerIncluded if <code>false</code>, the caller is removed from the list if it is in.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @return a {@link java.util.List} containing agents that handle this role or <code>null</code> if no agent has been found.
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role, boolean callerIncluded) {
		return kernel.getAgentsWithRole(this, community, group, role, callerIncluded);
	}

	/**
	 * Returns the next message contained in the message box.
	 * 
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
	public Message nextMessage() {
		// no checkAliveness : this could be done in the constructor.
		if (logger != null) {
			final Message m = messageBox.poll();
			logger.finest("getting nextMessage = " + m);
			return m;
		}
		return messageBox.poll();
	}

	/**
	 * Returns the message which has been received the most recently and 
	 * removes it from the mailbox.
	 * 
	 * @return the message which has been received the most recently.
	 */
	public Message getMostRecentMessage(){
		return messageBox.pollLast();
	}

	public boolean isMessageBoxEmpty(){
		return messageBox.isEmpty();
	}


	/**
	 * Sends a message to an agent using an agent address.
	 * This has the same effect as <code>sendMessageWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param receiver
	 * @param messageToSend
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AA}</code>: If the receiver address is no longer valid. This is the case when the corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @see AgentAddress
	 */
	public ReturnCode sendMessage(final AgentAddress receiver, final Message messageToSend) {
		return sendMessageWithRole(receiver, messageToSend, null);
	}

	/**
	 * Sends a message, using an agent address, specifying explicitly the role used to send it.
	 * 
	 * @param receiver the targeted agent
	 * @param message the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AA}</code>: If the receiver address is no longer valid. This is the case when the corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         <li><code>{@link ReturnCode#NETWORK_DOWN}</code>: If the <code>receiver</code> is running 
	 *         on another kernel but the network agent is down.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @see AgentAddress
	 */
	public ReturnCode sendMessageWithRole(final AgentAddress receiver, final Message message, final String senderRole) {
		return kernel.sendMessage(this, receiver, message, senderRole);
	}

	/**
	 * Sends a message to an agent having this position in the organization, specifying explicitly the role used to send it.
	 * This has the same effect as sendMessageWithRole(community, group, role, messageToSend,null).
	 * If several agents match, the target is chosen randomly.
	 * The sender is excluded from this search.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param message the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 */
	public ReturnCode sendMessage(final String community, final String group, final String role, final Message message) {
		return sendMessageWithRole(community, group, role, message, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization.
	 * This has the same effect as sendMessageWithRole(community, group, role, messageToSend,null).
	 * If several agents match, the target is chosen randomly.
	 * The sender is excluded from this search.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param message the message to send
	 * @param senderRole the agent's role with which the message has to be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 */
	public ReturnCode sendMessageWithRole(final String community, final String group, final String role, final Message message, final String senderRole) {
		return kernel.sendMessage(this, community, group, role, message, senderRole);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community, but not to the sender.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param message
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 */
	public ReturnCode broadcastMessage(final String community, final String group, final String role, final Message message) {
		return broadcastMessageWithRole(community, group, role, message, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender.
	 * The sender is excluded from the search.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param messageToSend
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 */
	public ReturnCode broadcastMessageWithRole(final String community, final String group, final String role, final Message messageToSend, String senderRole) {
		return kernel.broadcastMessageWithRole(this, community, group, role, messageToSend, senderRole);
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
	 * Sends a message by replying to a previously received message.
	 * The sender is excluded from this search.
	 * 
	 * @param messageToReplyTo the previously received message.
	 * @param reply the reply itself.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @throws NullPointerException if <code>reply</code> or <code>messageToReplyTo</code> is <code>null</code>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendReplyWithRole(final Message messageToReplyTo, final Message reply, String senderRole) {
		reply.setID(messageToReplyTo.getConversationID());
		return kernel.sendMessage(this,messageToReplyTo.getSender(), reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message.
	 * This has the same effect as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param messageToReplyTo the previously received message.
	 * @param reply the reply itself.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * 
	 */
	public ReturnCode sendReply(final Message messageToReplyTo, final Message reply) {
		return sendReplyWithRole(messageToReplyTo, reply, null);
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 * 
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no reply to this message has been received.
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
	 * This method offers a convenient way for "non Agent modules" to send messages to Agent, especially threaded agents.
	 * For instance when a GUI wants to discuss with its linked agent: This allows to enqueue work to do in their life cycle
	 * 
	 * @param m
	 */
	public void receiveMessage(Message m) {
		messageBox.offer(m); // TODO test vs. arraylist and synchronized
		//		if(messageBox == null)
		//			messageBox = new LinkedBlockingDeque<Message>();
		//		messageBox.offer(m); // TODO test vs. arraylist and synchronized
	}

	/**
	 * Gets the MadKit session property indicated by the specified key.
	 * This call is equivalent to <code>getMadkitConfig().getProperty(key)</code>
	 * 
	 * @param key the name of the MadKit property
	 * @return the string value of the MadKit property,
	 *         or <code>null</code> if there is no property with that key.
	 * @see #setMadkitProperty(String, String)
	 * @see Madkit
	 */
	final public String getMadkitProperty(String key) {
		return getMadkitConfig().getProperty(key);
	}

	/**
	 * Sets the MadKit session property indicated by the specified key.
	 * 
	 * @param key the name of the MadKit property
	 * @see #getMadkitProperty(String)
	 * @see Madkit
	 */
	public void setMadkitProperty(String key, String value) {
		kernel.setMadkitProperty(this, key, value);
	}

	/**
	 * Called when the default GUI mechanism is used upon agent creation. 
	 * The life cycle of the frame is automatically managed (i.e. disposed 
	 * when the agent is terminated) and some menus
	 * are available by default.
	 * Default code is only one line: frame.add(new IOPanel(this));
	 * 
	 * @param frame the default frame which has been created by MadKit for this agent.
	 * @since MadKit 5.0.0.8
	 * @see madkit.gui.OutputPanel
	 */
	public void setupFrame(final JFrame frame){
		frame.add(new OutputPanel(this));
	}

	// /////////////////////////////////////////////// UTILITIES /////////////////////////////////

	/**	 * Asks MasKit to reload class byte code so that new instances reflect compilation changes
	 * during run time. This reloads the class byte code so that new instances, 
	 * created using {@link Class#newInstance()} on a class object obtained with
	 * {@link #getNewestClassVersion(String)}, will reflect compilation changes
	 * during run time. 
	 * 
	 * Especially, using
	 * {@link #launchAgent(AbstractAgent, int, boolean)} always uses
	 * the most recent byte code for the targeted agent without requiring a MadKit restart.
	 * 
	 * @param className the fully qualified name of the desired class.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reload can be done.</li>
	 *         </ul>
	 * @throws ClassNotFoundException if the class cannot be found
	 * @throws NullPointerException if <code>className</code> is <code>null</code> 
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0.0.3
	 */
	public ReturnCode reloadAgentClass(String className) throws ClassNotFoundException {
		//		try {
		return kernel.reloadClass(this, className);
		//		} catch (ClassNotFoundException e) {
		//			throw new ClassNotFoundException(className);
		//		}
	}

	/**
	 * returns the newest version of a class object given its name. If {@link #reloadAgentClass(String)} has been used this
	 * returns the class object corresponding to the last compilation of the java code. Especially, in such a case, this 
	 * returns a different version than {@link Class#forName(String)} 
	 * if the agent that uses it has not been reloaded at the same time. This is because {@link Class#forName(String)} 
	 * uses the {@link ClassLoader} of the current class while this method uses the last class loader which is used by
	 * MadKit, i.e. the one created for loading classes on which {@link #reloadAgentClass(String)} has been invoked.
	 * Especially, {@link #launchAgent(String, int, boolean)} always uses the newest version of the agent class.
	 * 
	 * @param className the fully qualified name of the desired class.
	 * @return the newest version of a class object given its name.
	 * @throws ClassNotFoundException 
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5.0.0.8
	 */
	public Class<?> getNewestClassVersion(String className) throws ClassNotFoundException{
		return kernel.getNewestClassVersion(this, className);
	}


	public Map<String, Map<String, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global){
		return kernel.getOrganizationSnapShot(global);
	}


	/**
	 * Tells if a community exists in the artificial society.
	 * 
	 * @param community the name of the community
	 * @return <code>true</code> If a community with this name exists, <code>false</code> otherwise.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public boolean isCommunity(final String community) {
		return kernel.isCommunity(this, community);
	}

	/**
	 * Tells if a group exists in the artificial society.
	 * 
	 * @param community the name of the community the group is in
	 * @param group the name of the group
	 * @return <code>true</code> If a group with this name exists in this community, <code>false</code> otherwise.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public boolean isGroup(final String community, final String group) {
		return kernel.isGroup(this, community, group);
	}

	/**
	 * Tells if a role exists in the artificial society.
	 * 
	 * @param community the name of the community the group is in
	 * @param group the name of the group
	 * @param role the name of the role
	 * @return <code>true</code> If a role with this name exists in this <community;group> couple, <code>false</code> otherwise.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public boolean isRole(final String community, final String group, final String role) {
		return kernel.isRole(this, community, group, role);
	}

	// and the status
	/**
	 * @return a <code>String</code> representing the name of the agent
	 */
	@Override
	public String toString() {
		return getName();
		//		if(getState() == State.NOT_LAUNCHED || getState() == TERMINATED)
		//			return getName();//+" *"+getState()+"*";
		////		return getName()+" *"+getState()+"* running on "+getKernelAddress();
		//		return getName()+" running on "+getKernelAddress();
	}

	/**
	 * Returns the MadKit session Properties object. 
	 * If the agent has not been already launched, this
	 * returns the object holds by the last MadKit instance
	 * launched. If no MadKit instance has been created, this
	 * returns the MadKit default configuration.
	 * 
	 * @return the Properties object defining the values of
	 * each MadKit options in the current session.
	 * @since MadKit 5.0.0.10
	 */
	public Properties getMadkitConfig(){
		return kernel.getMadkitConfig();
	}

	/**
	 * Called by the logged kernel to see if it is worth to
	 * build the log message
	 * 
	 * @return if it is is worth to build the log message
	 */
	final boolean isFinestLogOn(){
		if(logger != null){
			return ! (Level.FINEST.intValue() < logger.getLevel().intValue());
		}
		else{
			//updating the level accordingly -> the user has set logger to null himself
			setLogLevel(Level.OFF);
			return false;
		}
	}

	/**
	 * Returns the kernel address on which the agent is running.
	 * 
	 * @return the kernel address representing the MadKit kernel
	 * on which the agent is running
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public KernelAddress getKernelAddress(){
		return kernel.getKernelAddress();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Utilities //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	final void logLifeException(final Throwable e) {
		String m = "-*-";
		switch (getState()) {
		case ACTIVATED:
			m += "ACTIVATE BUG-*- :";// " + getI18N("terminated");
			break;
		case LIVING:
			m += "LIVE BUG-*- :";// + getI18N("terminated");
			break;
		case ENDING:
			m += "END BUG-*- :";
			break;
		default:
			m += "TERMINATE BUG-*- :";
		}
		getLogger().severeLog(m, e);
	}

	//	/**
	//	 * @param e
	//	 * @param m
	//	 */
	//	private void severeLog(Throwable e, String m) {
	//		kernel.getMadkitKernel().getLogger().log(Level.FINEST,m,e);
	//		setAgentStackTrace(e);
	//		getLogger().severeLog(m, e);
	//	}

	final String getLoggingName() {
		if(name != null)
			return "[" + name + "]";
		return "["+getClass().getSimpleName()+ "-" + _hashCode+ "]";
		//		String loggingName = getClass().getSimpleName() + "-" + _hashCode;
		//		if (name == null || name.equals(loggingName)) {
		//			return "[" + loggingName + "]";
		//		}
		//		return "[" + loggingName+"-" + getName() + "]";
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Synchronization //////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	//	/**
	//	 * @param timeout
	//	 * @param unit
	//	 * @return
	//	 * @throws InterruptedException 
	//	 * @since MadKit 5.0.0.9
	//	 */
	//	private Message waitingNextMessageForEver() {
	//		try {
	//			return messageBox.takeFirst();
	//		} catch (InterruptedException e) {
	//			throw new KilledException(e);
	//		} catch (IllegalMonitorStateException e) {
	//			throw new KilledException(e);
	//		}
	//	}

	/**
	 * @throws InterruptedException 
	 * @since MadKit 5.0.0.9
	 */
	private Message waitingNextMessage(final long timeout, final TimeUnit unit) {
		try {
			return messageBox.pollFirst(timeout, unit);
		} catch (InterruptedException e) {
			throw new KilledException(e);
		} catch (IllegalMonitorStateException e) {
			throw new KilledException(e);
		}
	}

	/**
	 * Wipes out an entire community at once. 
	 * Mostly useful when doing simulated systems.
	 * This greatly optimizes the time 
	 * required to make all the agents leave a community.
	 * 
	 * @since MadKit 5.0.0.9
	 * @param community the community to destroy
	 */
	public void destroyCommunity(String community) {
		kernel.destroyCommunity(this,community);
	}

	/**
	 * Wipes out an entire group at once. 
	 * Mostly useful when doing simulated systems.
	 * This greatly optimizes the time 
	 * required to make all the agents leave a group.
	 * 
	 * @since MadKit 5.0.0.10
	 * @param community the community
	 * @param group the group to destroy
	 */
	public void destroyGroup(String community, String group) {
		kernel.destroyGroup(this, community, group);
	}

	/**
	 * Wipes out an entire role at once. 
	 * Mostly useful when doing simulated systems.
	 * This greatly optimizes the time 
	 * required to make all the agents leave a role.
	 * 
	 * @since MadKit 5.0.0.10
	 * @param community the community
	 * @param group the group
	 * @param role the group to destroy
	 */
	public void destroyRole(String community, String group, String role) {
		kernel.destroyRole(this, community, group, role);
	}

	/**
	 * @throws InterruptedException 
	 * @since MadKit 5.0.0.9
	 */
	List<Message> waitAnswers(Message message, int size, Integer timeOutMilliSeconds){
		final long endTime = System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds);
		final long conversationID = message.getConversationID();
		int missing = size;
		final LinkedList<Message> receptions = new LinkedList<Message>();
		final LinkedList<Message> answers = new LinkedList<Message>();
		while(missing > 0 && System.nanoTime() < endTime){
			Message answer = waitingNextMessage(endTime - System.nanoTime(),TimeUnit.NANOSECONDS);
			if(answer == null)
				break;
			if(answer.getConversationID() == conversationID){
				answers.add(answer);
				missing--;
			}
			else
				receptions.add(answer);
		}
		if (! receptions.isEmpty()) {
			Collections.reverse(receptions);
			for (final Message m : receptions) {
				messageBox.addFirst(m);
			}
		}
		if(! answers.isEmpty())
			return answers;
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Using Agent Address /////////////////////
	// /////////////////////////////////////////////////////////////////////////////


	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Agent State //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * An agent state. An agent can be in one of the following states:
	 * <ul>
	 * <li>{@link #NOT_LAUNCHED}<br>
	 * An agent which has not yet been launched is in this state.</li>
	 * <li>{@link #INITIALIZING}<br>
	 * An agent that has been launched but which has not started its {@link #activate()} method yet is in this state.</li>
	 * <li>{@link #ACTIVATED}<br>
	 * An agent that is processing its {@link #activate()} method is in this state.</li>
	 * <li>{@link #LIVING}<br>
	 * An agent that is processing its {@link Agent#live()} method is in this state.</li>
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
	 * @since MadKit 5.0
	 * @see #getState
	 */
	public enum State {

		/**
		 * The agent has not been launched yet.
		 */
		NOT_LAUNCHED,

		/**
		 * The agent has been launched and is being registered by the kernel
		 * but it has not started its {@link #activate()} method yet.
		 */
		INITIALIZING,

		/**
		 * The agent is processing its {@link #activate()} method.
		 * This state is also
		 * the "running" state of {@link AbstractAgent} subclasses
		 * (i.e. when they have finished their activation) as
		 * they do not have a {@link Agent#live()} managed by the kernel in
		 * their life cycle. On the contrary to {@link Agent} subclasses which next state is {@link #LIVING}).
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
		 * The agent has finished its life cycle in the MadKit platform.
		 */
		TERMINATED;
	}

	/**
	 * Returns the current state of the agent in the MadKit platform.
	 * 
	 * This method provides a way of knowing what is the current state of the agent regarding its life cycle.
	 * This could be convenient when you design a method that could work differently depending
	 * on the actual state of the agent.
	 * 
	 * @return the current state of the agent:
	 *         <ul>
	 *         <li><code>{@link State#NOT_LAUNCHED}</code>: the agent has not been launched yet. This especially means that most of the methods of this API still do not work for this agent as it has
	 *         not been registered yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#INITIALIZING}</code>: the agent is being registered by the kernel but has not started its {@link #activate()} method yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#ACTIVATED}</code>: the agent is processing its {@link #activate()} method. This state is also the "running" state of {@link AbstractAgent} subclasses (i.e. when
	 *         they have finished their activation) as they do not have a {@link Agent#live()} managed by the kernel in their life cycle. On the contrary to {@link Agent} subclasses which next state is
	 *         {@link State#LIVING}.</li>
	 *         <br/>
	 *         <li><code>{@link State#LIVING}</code>: returned when {@link Agent} subclasses are processing their {@link Agent#live()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#ENDING}</code>: the agent is processing its {@link #end()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#TERMINATED}</code>: the agent has finished its life in the MadKit platform. Especially, most of the methods of this API will no longer work for this agent.</li>
	 *         </ul>
	 * 
	 */
	public State getState() {
		return state.get();
	}

	public URLClassLoader getMadkitClassLoader(){
		return kernel.getMadkitKernel().getMadkitClassLoader();
	}

	//	/**
	//	 * @return an Executor which could be used to do tasks asynchronously
	//	 */
	//	public Executor getMadkitExecutor(){
	//		return kernel.getMadkitKernel().getMadkitExecutor();
	//	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Return codes //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * This class enumerates all the return codes which could be obtained with essential methods of the {@link AbstractAgent} and {@link Agent} classes.
	 * 
	 * @author Fabien Michel
	 * @since MadKit 5.0
	 * 
	 */
	public enum ReturnCode {

		SUCCESS,
		NOT_COMMUNITY,
		NOT_GROUP,
		NOT_IN_GROUP,
		NOT_ROLE,
		TERMINATED_AGENT,
		ROLE_ALREADY_HANDLED,
		ACCESS_DENIED,
		ROLE_NOT_HANDLED,
		ALREADY_GROUP,
		ALREADY_LAUNCHED,
		TIMEOUT,
		AGENT_CRASH,
		NOT_AN_AGENT_CLASS,
		NOT_YET_LAUNCHED,
		ALREADY_KILLED,
		INVALID_AA,
		NO_RECIPIENT_FOUND,
		SEVERE,
		NETWORK_DOWN;

		final static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class.getSimpleName());
		//		static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class);

		public String toString() {
			return messages.getString(name());
		}
	}

	enum Influence{
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
		RELOAD_CLASS;
		//		final static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class.getSimpleName());
		//		/**
		//		 * @param r
		//		 * @return the influence name followed by the return code
		//		 */
		//		public String resultString(ReturnCode r){
		//			if(r != ReturnCode.SUCCESS){
		//				return name()+" "+Words.FAILED+" "+r+" : ";
		//			}
		//			return name()+" "+r+" : ";
		//		}

		public String failedString() {
			return name()+" "+Words.FAILED+" : ";
		}
		@Override
		public String toString() {
			return name()+" ";
		}

		public String successString() {
			return name()+" "+SUCCESS+" : ";
		}
	}


	public boolean isKernelConnected() {
		return kernel.isOnline();
	}

	AgentExecutor getAgentExecutor() {
		return null;
	}

	//	@Override
	//	protected void finalize() throws Throwable {
	//		System.err.println("FINALIZING "+this);
	//	}

}