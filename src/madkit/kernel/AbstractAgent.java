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
import static madkit.kernel.Utils.getI18N;
import static madkit.kernel.Utils.printCGR;

import java.awt.Component;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import madkit.gui.OutputPanel;

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
 * @author Olivier Gutknecht
 * @author Fabien Michel (since v.3)
 * @version 5.1
 */
public class AbstractAgent implements Comparable<AbstractAgent>, Serializable {

	private static final long serialVersionUID = 1431823907218926925L;

	private static final RootKernel fakeKernel = new RootKernel();

	final static AgentLogger defaultLogger = AgentLogger.getDefaultAgentLogger();
	final static AgentThreadFactory normalAgentThreadFactory = new AgentThreadFactory("MKRA", false);//TODO move that into the kernel ensuring same JVM functioning
	final static AgentThreadFactory daemonAgentThreadFactory = new AgentThreadFactory("MKDA", true);

	private final static AtomicInteger agentCounter = new AtomicInteger(1);

	final AtomicReference<State> state = new AtomicReference<AbstractAgent.State>(AbstractAgent.State.NOT_LAUNCHED);
	RootKernel kernel;

	final private int _hashCode;
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
		_hashCode = agentCounter.getAndIncrement();
		kernel = fakeKernel;
	}

	/**
	 * The ID of an agent (i.e. its hashCode() value). All the agents have different 
	 * hashCode value and it can be used to identify one agent.
	 * In a networked environment this value should be used in combination with
	 * the kernelAddress of the agent for identifying it 
	 * This also holds when multiple MadKit kernels are launched within the same JVM.
	 * 
	 * @return the agent's unique ID
	 * @see java.lang.Object#hashCode()
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
		return alive.get();
	}

	// //////////////////////////////////////////// LIFE CYCLE
	boolean activation() {
		// kernel.agentCounter++; //TODO useless !! + I18N
		try {
			if (!state.compareAndSet(INITIALIZING, ACTIVATED))// TODO remove it when OK
				throw new AssertionError("not init in activation");
			if (logger != null) {
				logger.finer("** entering ACTIVATE **");
			}
			activate();
		} catch (KilledException e) {//TODO this part is useless: check that
			if (logger != null) {
				logger.warning("-*-GET KILLED in ACTIVATE-*- by killedException: " + getI18N("terminated"));
				logger.finer("** exiting ACTIVATE **");
			}
			if (alive.get()) {
				ending();
			}
			if (alive.get()) {
				terminate();
			}
			return false;
		} catch (Exception e) {
			kernel.kernelLog("Problem for "+this+" in ACTIVATE ", Level.FINER, e);
			logSevereException(e);
			if (logger != null) {
				logger.finer("** exiting ACTIVATE **");
			}
			// no more ending if activate failed
			// ending();
			if (alive.get()) {
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
			if (logger != null) {
				logger.warning("-*-GET KILLED in END-*-: " + getI18N("terminated"));
			}
			return false;
		} catch (Exception e) {// TODO another word for terminated in end
			logSevereException(e);
			return false;
		} finally {
			alive.set(false);
			if (logger != null) {
				logger.finer("** exiting END **");// TODO display it or not in case of kill ?
			}
			// terminate();
		}
		return true;
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
	 *         <li><code> {@link ReturnCode#LAUNCH_TIME_OUT} </code>: If your agent is activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#INVALID_ARG} </code>: If <code>agent</code> is <code>null</code>.</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent) {
		return launchAgent(agent, Integer.MAX_VALUE, false);
	}

	/**
	 * Launches a new agent in the MadKit platform.
	 * This has the same effect as <code>launchAgent(agent,timeOutSeconds,false)</code>
	 * 
	 * @param agent the agent to launch.
	 * @param timeOutSeconds time to wait the end of the agent's activation until returning a LAUNCH_TIME_OUT.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#LAUNCH_TIME_OUT} </code>: If the activation time of the agent is greater than <code>timeOutSeconds</code> seconds</li>
	 *         <li><code>{@link ReturnCode#AGENT_CRASH}</code>: If the agent crashed during its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#INVALID_ARG} </code>: If <code>agent</code> is <code>null</code>.</li>
	 *         </ul>
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
	 *         <li><code> {@link ReturnCode#LAUNCH_TIME_OUT} </code>: If your agent is activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#INVALID_ARG} </code>: If <code>agent</code> is <code>null</code>.</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final boolean createFrame) {
		return launchAgent(agent, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent and returns when the agent has completed its {@link AbstractAgent#activate()} method
	 * or when <code>timeOutSeconds</code> seconds elapsed. That is, the launched agent has not finished
	 * its {@link AbstractAgent#activate()} before the time out time elapsed.
	 * Additionally, if <code>withGUIManagedByTheBooter</code> is <code>true</code>, it tells
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
	 * @param timeOutSeconds time to wait for the end of the agent's activation until returning a LAUNCH_TIME_OUT.
	 * @param createFrame if <code>true</code>, the kernel will launch a JFrame for this agent.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has succeeded. This also means that the agent has successfully completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#LAUNCH_TIME_OUT} </code>: If the activation time of the agent is greater than <code>timeOutSeconds</code> seconds</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent crashed during its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#INVALID_ARG} </code>: If <code>agent</code> is <code>null</code>.</li>
	 *         </ul>
	 * @since MadKit 5.0
	 */
	public ReturnCode launchAgent(final AbstractAgent agent, final int timeOutSeconds, final boolean createFrame) {
		return kernel.launchAgent(this, agent, timeOutSeconds, createFrame);
	}

	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as <code>launchAgent(agentClass, Integer.MAX_VALUE, false)</code>.
	 * 
	 * @param agentClass the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
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
	 */
	public AbstractAgent launchAgent(String agentClass, final boolean createFrame) {
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
	 * @return the instance of the launched agent or <code>null</code> if the operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds, final boolean createFrame) {
		return kernel.launchAgent(this, agentClass, timeOutSeconds, createFrame);
	}

	/**
	 * Launches <i><code>bucketSize</code></i> agent instances of this <i><code>agentClassName</code></i>.
	 * This has the same effect as <code>launchAgentBucketWithRoles(agentClassName, bucketSize, (Collection<String>) null)</code>.
	 * 
	 * @param agentClassName the name of the class from which the agents should be built.
	 * @param bucketSize the desired number of agents.
	 * @return a list containing all the agents which have been launched,
	 *         or <code>null</code> if the operation failed
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
	 * @since MadKit 5.0
	 */
	public ReturnCode killAgent(final AbstractAgent target, final int timeOutSeconds) {
		if(target == this)
			return kernel.killAgent(this, target, Integer.MAX_VALUE);
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
	 * Returns the agent's logger.
	 * 
	 * @return the agent's logger. It may be <code>null</code> if the log level of this agent has been set to {@link Level#OFF}.
	 */
	public AgentLogger getLogger() {
		return logger;
	}

	/**
	 * @return the kernel
	 */
	final RootKernel getKernel() {
		return kernel;
	}

	/**
	 * @param kernel the kernel to set
	 */
	final void setKernel(RootKernel kernel) {
		if (kernel != null) {
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
	public void setName(final String name) {// TODO trigger gui changes and son on
		if (! getName().equals(name)) {
			this.name = name;
			if (logger != null)
				setLogLevel(logger.getLevel());
		}
	}

	/**
	 * 
	 */
	public synchronized void setLogLevel(final Level newLevel) {
		setLogLevel(newLevel, logger == null ? Level.parse(getMadkitProperty(Madkit.warningLogLevel)) : logger.getWarningLogLevel());
	}

	/**
	 * 
	 */
	public synchronized void setLogLevel(final Level newLevel, final Level warningLogLevel) {
		kernel.setLogLevel(this, getLoggingName(), newLevel,warningLogLevel);
		madkit.gui.Utils.updateAgentUI(this);
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
	 *         <li><code>{@link ReturnCode#NULL_STRING}</code>: If <code>community</code> or <code>group</code> is <code>null</code>.</li> 
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, GroupIdentifier)
	 * @since MadKit 5.0
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
	 *         <li><code>{@link ReturnCode#NULL_STRING}</code>: If <code>community</code> or <code>group</code> is <code>null</code>.</li> 
	 *         </ul>
	 *         </ul>
	 * 
	 * @see AbstractAgent#createGroup(String, String, boolean, GroupIdentifier)
	 * @since MadKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed) {
		return createGroup(community, group, isDistributed, null);
	}

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role defined by {@link Madkit.Roles#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.kernel.Madkit.Roles#GROUP_MANAGER_ROLE}</i>, in this created group. Especially, if the agent leaves the role of <i>{@value madkit.kernel.Madkit.Roles#GROUP_MANAGER_ROLE}</i>, it
	 * will also automatically leave the group and thus all the roles it has in this group.
	 * 
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>{@value madkit.kernel.Madkit.Roles#GROUP_MANAGER_ROLE}</i> using the role defined by {@link Madkit.Roles#GROUP_CANDIDATE_ROLE},
	 * which value is <i>{@value madkit.kernel.Madkit.Roles#GROUP_CANDIDATE_ROLE}</i>.
	 * 
	 * @param community the community within which the group will be created.
	 *        If this community does not exist it will be created.
	 * @param group the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed when multiple MadKit kernels are connected.
	 * @param theIdentifier any object that implements the {@link GroupIdentifier} interface. If not <code>null</code>,
	 *        this object will be used to check if an agent can be admitted in the group.
	 *        When this object is null, there is no group access control.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been successfully created.</li> 
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed because such a
	 *         group already exists.</li>
	 *         <li><code>{@link ReturnCode#NULL_STRING}</code>: If <code>community</code> or <code>group</code> is <code>null</code>.</li> 
	 *         </ul>
	 * 
	 * @see GroupIdentifier
	 * @see ReturnCode
	 * @see Madkit.Roles
	 * @since MadKit 5.0
	 */
	public ReturnCode createGroup(final String community, final String group, boolean isDistributed, final GroupIdentifier theIdentifier) {
		return kernel.createGroup(this, community, group, group, theIdentifier, isDistributed);
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
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean, GroupIdentifier)
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
	 * @see AbstractAgent#createGroupIfAbsent(String, String, boolean, GroupIdentifier)
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
	 * @param theIdentifier any object that implements the {@link GroupIdentifier} interface. If not <code>null</code>,
	 *        this object will be used to check if an agent can be admitted in the group.
	 *        When this object is null, there is no group access control.
	 * @return <code>true</code> if the group has been created, <code>false</code> if it was already present.
	 *         </ul>
	 * 
	 * @see GroupIdentifier
	 * @see ReturnCode
	 * @since MadKit 5.0
	 */
	public boolean createGroupIfAbsent(final String community, final String group, boolean isDistributed, final GroupIdentifier theIdentifier) {
		return kernel.createGroupIfAbsent(this, community, group, group, theIdentifier, isDistributed);
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
	 *         <li><code>{@link ReturnCode#NULL_STRING}</code>: If <code>role</code> is <code>null</code>.</li>
	 *         </ul>
	 * @param community the group's community.
	 * @param group the desired group.
	 * @param role the desired role.
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
	 *         <li><code>{@link ReturnCode#NULL_STRING}</code>: If <code>role</code> is <code>null</code>.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see GroupIdentifier
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
	 * @since MadKit 5.0
	 */
	public ReturnCode leaveRole(final String community, final String group, final String role) {
		return kernel.leaveRole(this, community, group, role);
	}

	final ReturnCode handleException(final MadkitWarning e) {
		if (logger != null && logger.getWarningLogLevel().intValue() >= logger.getLevel().intValue()) {
			setAgentStackTrace(e);
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return e.getCode();
	}

	private void setAgentStackTrace(final Throwable e) {
		final List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
		// boolean notFound = true;
		StackTraceElement[] stackTrace = e.getStackTrace();
		stack.add(stackTrace[1]);
		String agentClassName = getClass().getName();
		for (int i = 2; i < stackTrace.length ; i++){
			final String trace = stackTrace[i].getClassName();
			if (! (trace.contains("madkit.kernel") || trace.contains("java.")) || trace.contains(agentClassName)) {
				stack.add(stackTrace[i]);
			}
		}
		e.setStackTrace(stack.toArray(new StackTraceElement[0]));
	}

	/**
	 * 
	 */
	void terminate() {// TODO should be in mk
		alive.set(false);
		if (getState().equals(TERMINATED))// TODO remove that
			throw new AssertionError("terminating twice " + getName());
		state.set(TERMINATED);
		try {
			kernel.disposeGUIOf(this);
			kernel.removeAgentFromOrganizations(this);// TODO catch because of probe/activator
		} catch (Exception e) {
			kernel.kernelLog("Problem for "+this+" in TERMINATE ", Level.FINER, e);
			logSevereException(e);
		}
		messageBox.clear(); // TODO test speed and no need for that
		if (logger != null) {
			logger.finest("terminated");
			for (Handler h : logger.getHandlers()) {
				h.close();
			}
		}
		kernel = fakeKernel;
	}

	/**
	 * @return
	 */
	ExecutorService getAgentExecutor() {
		return null;
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this position in the organization.
	 * The caller is excluded from the search.
	 * 
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
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
	 * @return a {@link java.util.List} containing agents that handle this role or <code>null</code> if no agent has been found.
	 */
	public List<AgentAddress> getAgentsWithRole(final String community, final String group, final String role) {
		return kernel.getAgentsWithRole(this, community, group, role);
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
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>receiver</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AA}</code>: If the receiver address is no longer valid. This is the case when the corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>messageToSend</code> is <code>null</code>.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>receiver</code> or message<code>message</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AA}</code>: If the receiver address is no longer valid. This is the case when the corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>messageToSend</code> is <code>null</code>.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>message</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#INVALID_ARG}</code>: If <code>message</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#NULL_MSG}</code>: If the <code>messageToSend</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#NULL_MSG}</code>: If the <code>messageToSend</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
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
	 *         <li><code>{@link ReturnCode#NULL_MSG}</code>: If the <code>reply</code> or the <code>messageToReplyTo</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendReplyWithRole(final Message messageToReplyTo, final Message reply, String senderRole) {
		return kernel.sendReplyWithRole(this, messageToReplyTo, reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message.
	 * This has the same effect as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param messageToReplyTo the previously received message.
	 * @param reply the reply itself.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has succeeded.</li>
	 *         <li><code>{@link ReturnCode#NULL_MSG}</code>: If the <code>reply</code> or the <code>messageToReplyTo</code> is <code>null</code>.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent was found as recipient, i.e. the sender was the only agent having this role.</li>
	 *         </ul>
	 * @see ReturnCode
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
		final long searchID = originalMessage.getID();
		for (final Iterator<Message> it = messageBox.iterator(); it.hasNext();) {
			final Message m = it.next();
			if (m.getID() == searchID) {
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
	 * 
	 * @param key the name of the MadKit property
	 * @return the string value of the MadKit property,
	 *         or <code>null</code> if there is no property with that key.
	 * @see #setMadkitProperty(String, String)
	 * @see Madkit
	 */
	public String getMadkitProperty(String key) {
		return kernel.getMadkitProperty(this, key);
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

	/**
	 * Reloads the class of the agent in the class loader.
	 * So that {@link #launchAgent(AbstractAgent, int, boolean)} will
	 * use the most recent compiled code for this class without requiring a MadKit restart.
	 * 
	 * @param agentClass the name of the class to reload.
	 */
	public void reloadAgentClass(String agentClass) {
		kernel.reloadClass(this, agentClass);
	}
	
	/**
	 * Tells if a community exists in the artificial society.
	 * 
	 * @param community the name of the community
	 * @return <code>true</code> If a community with this name exists, <code>false</code> otherwise.
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
	 */
	public boolean isRole(final String community, final String group, final String role) {
		return kernel.isRole(this, community, group, role);
	}

	// and the status
	/**
	 * @return a <code>String</code> representing the name of the agent
	 */
	@Override
	public String toString() {// TODO
		// String status;
		// switch (runState.get()) {
		// case NOT_EXIST:
		// status = "Not exist (not launched)";
		// break;
		//
		// default:
		// break;
		// }
		return getName();
	}
	
	/**
	 * Returns the kernel address on which the agent is running.
	 * 
	 * @return the kernel address representing the MadKit kernel
	 * on which the agent is running
	 */
	public KernelAddress getKernelAddress(){
		return kernel.getKernelAddress(this);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Utilities //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	final void logSevereException(final Throwable e) {
		String m = "-*-";
		switch (getState()) {
		case ACTIVATED:
			m += "ACTIVATE BUG-*- : " + getI18N("terminated");
			break;
		case LIVING:
			m += "LIVE BUG-*- : " + getI18N("terminated");
			break;
		case ENDING:
			m += "END BUG-*- :";
			break;
		default:
			m += "TERMINATE BUG-*- :";
		}
		if (logger == null)
			setLogLevel(Level.INFO);
		setAgentStackTrace(e);
		logger.log(Level.SEVERE, m, e);
	}

	final String getLoggingName() {
		String loggingName = getClass().getSimpleName() + "-" + _hashCode;
		if (name == null || name.equals(loggingName)) {
			return "[" + loggingName + "]";
		}
		return "[" + loggingName+"-" + getName() + "]";
	}

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

		SUCCESS(Utils.getI18N("success")),

		NOT_COMMUNITY(Utils.getI18N("notExist")),

		NOT_GROUP(Utils.getI18N("notExist")),

		NOT_ROLE(Utils.getI18N("notExist")),

		TERMINATED_AGENT(Utils.getI18N("terminated_agent")),

		ROLE_ALREADY_HANDLED(Utils.getI18N("alreadyHandled")),

		ACCESS_DENIED(Utils.getI18N("denied")),

		ROLE_NOT_HANDLED(Utils.getI18N("notHandled")),

		NOT_IN_GROUP(Utils.getI18N("notInGroup")),

		ALREADY_GROUP(Utils.getI18N("alreadyExists")),

		ALREADY_LAUNCHED(Utils.getI18N("alreadyLaunched")),

		INVALID_ARG(Utils.getI18N("nullS")),

		LAUNCH_TIME_OUT(Utils.getI18N("timeOut")),

		AGENT_CRASH(Utils.getI18N("agentCrash")),

		CLASS_NOT_FOUND(Utils.getI18N("classNotFound")),

		NOT_AN_AGENT_CLASS(Utils.getI18N("notAgentClass")),

		NOT_YET_LAUNCHED(Utils.getI18N("notYetLaunched")),

		ALREADY_KILLED(Utils.getI18N("alreadyKilled")),

		NULL_MSG(Utils.getI18N("notYetLaunched")),

		NULL_STRING(Utils.getI18N("nullS")),

		NULL_AA(Utils.getI18N("nullAA")),

		INVALID_AA(Utils.getI18N("invAA")),

		NO_RECIPIENT_FOUND(Utils.getI18N("noRecepient")),

		SEVERE(Utils.getI18N("invAA")),

		NETWORK_DOWN(Utils.getI18N("invAA"));

		final private String message;

		private ReturnCode(String i18nMessage) {
			message = i18nMessage;
		}

		final String getMessage() {//TODO i18n at runtime on put get18n in getmessage
			return message;
		}

	}

}