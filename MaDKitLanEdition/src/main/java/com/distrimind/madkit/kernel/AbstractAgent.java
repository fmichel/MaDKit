/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package com.distrimind.madkit.kernel;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.AbstractAgent.State.ENDING;
import static com.distrimind.madkit.kernel.AbstractAgent.State.INITIALIZING;
import static com.distrimind.madkit.kernel.AbstractAgent.State.LIVING;
import static com.distrimind.madkit.kernel.AbstractAgent.State.TERMINATED;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.distrimind.madkit.action.ActionInfo;
import com.distrimind.madkit.action.GUIManagerAction;
import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.exceptions.KilledException;
import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.agr.LocalCommunity.Groups;
import com.distrimind.madkit.agr.LocalCommunity.Roles;
import com.distrimind.madkit.gui.AgentStatusPanel;
import com.distrimind.madkit.gui.OutputPanel;
import com.distrimind.madkit.gui.menu.AgentLogLevelMenu;
import com.distrimind.madkit.gui.menu.AgentMenu;
import com.distrimind.madkit.gui.menu.MadkitMenu;
import com.distrimind.madkit.i18n.ErrorMessages;
import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.madkit.kernel.network.AskForConnectionMessage;
import com.distrimind.madkit.kernel.network.AskForTransferMessage;
import com.distrimind.madkit.kernel.network.Connection;
import com.distrimind.madkit.kernel.network.ConnectionIdentifier;
import com.distrimind.madkit.kernel.network.LocalLanMessage;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.StatsBandwidth;
import com.distrimind.madkit.kernel.network.TransferFilter;
import com.distrimind.madkit.kernel.network.TransfersReturnsCodes;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.access.AccessData;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.ConversationFilter;
import com.distrimind.madkit.message.EnumMessage;
import com.distrimind.madkit.message.GUIMessage;
import com.distrimind.madkit.message.MessageFilter;
import com.distrimind.madkit.message.hook.AgentLifeEvent;
import com.distrimind.madkit.message.hook.MessageEvent;
import com.distrimind.madkit.message.hook.OrganizationEvent;
import com.distrimind.madkit.message.task.TasksExecutionConfirmationMessage;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.madkit.util.concurrent.LinkedBlockingDeque;
import com.distrimind.util.crypto.MessageDigestType;

// * <img src="doc-files/Capture.png" alt=""/>
/**
 * The super class of all MaDKit agents, v 5. It provides support for
 * <ul>
 * <li>Agent's Life cycle, logging, and naming.</li>
 * <li>Agent launching and killing.</li>
 * <li>Artificial society creation and management.</li>
 * <li>Messaging.</li>
 * <li>Minimal graphical interface management.</li>
 * </ul>
 * <br>
 * The agent's behavior is <i>intentionally not defined</i>. It is up to the
 * agent developer to choose an agent model or to develop his specific agent
 * library on top of the facilities provided by the MaDKit API. However, all the
 * launched agents share the same organizational view, and the basic messaging
 * code, so integration of different agents is quite easy, even when they are
 * coming from different developers or have heterogeneous models.
 * <br>
 * Agent-related methods (most of this API) is only effective after the agent
 * has been launched and thus registered in the current Madkit session.
 * Especially, that means that most of the API has no effect in the constructor
 * method of an Agent and will only produce a warning if used.
 * <br>
 * <h2>MaDKit v.5 new features</h2>
 * <br>
 * <ul>
 * <li>One of the big change that comes with version 5 is how agents are
 * identified and localized within the artificial society. An agent is no longer
 * binded to a single agent address but has as many agent addresses as holden
 * positions in the artificial society. see {@link AgentAddress} for more
 * information.</li> 
 * <li>With respect to the previous change, a <code><i>withRole</i></code>
 * version of all the messaging methods has been added. See
 * {@link #sendMessageWithRole(AgentAddress, Message, String)} for an example of
 * such a method.</li> 
 * <li>A replying mechanism has been introduced through
 * <code><i>SendReply</i></code> methods. It enables the agent with the
 * possibility of replying directly to a given message. Also, it is now possible
 * to get the reply to a message, or to wait for a reply ( for {@link Agent}
 * subclasses only as they are threaded) See
 * {@link #sendReply(Message, Message)} for more details.</li>
 * <li>Agents now have a <i>formal</i> state during a MaDKit session. See the
 * {@link #getState()} method for detailed information.</li> 
 * <li>One of the most convenient improvement of v.5 is the logging mechanism
 * which is provided. See the {@link #logger} attribute for more details.</li>
 * </ul>
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub
 * @version 6.0
 * @since MadKitLanEdition 1.0
 */
public class AbstractAgent implements Comparable<AbstractAgent> {

	private final static transient AtomicLong agentCounter = new AtomicLong(
			(long) (Math.random() * (double) Integer.MAX_VALUE));

	static final transient MadkitKernel FAKE_KERNEL = new FakeKernel();
	private static final transient MadkitKernel TERMINATED_KERNEL = new TerminatedKernel();

	final AtomicReference<State> state = new AtomicReference<>(State.NOT_LAUNCHED);
	transient MadkitKernel kernel = FAKE_KERNEL;

	final private long agentID;

	private boolean hasGUI;
	/**
	 * name is lazily created to save memory
	 */
	private String name;
	final AtomicBoolean alive = new AtomicBoolean(); // default false
	final LinkedBlockingDeque<Message> messageBox = new LinkedBlockingDeque<>(); // TODO
																					// lazy
																					// creation

	private volatile ArrayList<Replies> conversations = null;

	@SuppressWarnings("unchecked")
	void addConversation(Replies replies) {
		if (replies != null) {
			synchronized (this) {
				ArrayList<Replies> c = conversations;
				if (c == null)
					c = new ArrayList<>();
				else {
					c = (ArrayList<Replies>) c.clone();
				}
				c.add(replies);
				conversations = c;
			}
		}
	}

	@SuppressWarnings("unchecked")
	boolean removeConversation(Replies replies) {
		if (replies != null) {
			synchronized (this) {
				ArrayList<Replies> c = conversations;
				if (c == null)
					return false;
				c = (ArrayList<Replies>) c.clone();
				if (c.remove(replies)) {
					if (c.isEmpty())
						conversations = null;
					else
						conversations = c;
					return true;
				}
			}
		}
		return false;
	}

	Replies getConversation(Message m) {
		ArrayList<Replies> c = conversations;
		if (c == null)
			return null;

		for (Replies r : c) {
			if (r.isConcernedBy(m))
				return r;
		}
		return null;
	}

	/**
	 * <code>logger</code> should be used to print messages and trace the agent's
	 * life cycle. According to a log level, the messages will be displayed in the
	 * console and/or the GUI and/or a file according to the settings.
	 * 
	 * Thanks to the logging mechanism of the SDK, various log level could be used.
	 * 
	 * The following idiom should be used because {@link Logger} is set to
	 * <code>null</code> when {@link AgentLogger#setLevel(Level)} is used with
	 * {@link Level#OFF}. This allows to efficiently optimize the runtime speed when
	 * they are a lot of agents (e.g. in a simulation mode). Indeed, thanks to this
	 * idiom, useless strings will not be built, thus saving a lot of time.
	 * 
	 * <pre>
	 * if (logger != null)
	 * 	logger.info(&quot;info message&quot;);
	 * </pre>
	 * 
	 * {@link #getLogger()} should not be used here because it always returns a non
	 * <code>null</code> logger.
	 * 
	 * @see java.util.logging.Level
	 * @see java.util.logging.Logger
	 */
	protected volatile AgentLogger logger;

	public AbstractAgent() {
		agentID = agentCounter.getAndIncrement();// TODO bench outside
		logger = AgentLogger.defaultAgentLogger;
	}

	/**
	 * for building fake kernels
	 * 
	 * @param fake
	 */
	AbstractAgent(Object fake) {
		agentID = -1;
	}

	/**
	 * @return the real kernel
	 */
	MadkitKernel getMadkitKernel() {
		return kernel.getMadkitKernel();
	}

	/**
	 * Activates the MaDKit GUI initialization when launching the agent whatever the
	 * launching parameters. By default agents are launched without a GUI but some
	 * of them always need one: This ensures that the agent will have one. This
	 * method should be used only in the constructor of the agent, otherwise it will
	 * be useless as it specifies a boot property of the agent.
	 * 
	 */
	public void createGUIOnStartUp() {
		if (state.get().compareTo(State.ACTIVATING) < 0) {
			hasGUI = true;
		}
	}

	/**
	 * Tells if this agent has a GUI automatically built by the kernel
	 * 
	 * @return <code>true</code> if this agent has a GUI built by the kernel
	 */
	public boolean hasGUI() {
		return hasGUI;
	}

	/**
	 * 
	 * Causes the currently executing thread to sleep (temporarily cease execution)
	 * for the specified number of milliseconds, subject to the precision and
	 * accuracy of system timers and schedulers. The thread does not lose ownership
	 * of any monitors.
	 * 
	 * Use this function instead of {@link Thread#sleep(long)} in order to avoid
	 * dead lock, especially when using {@link AgentFakeThread}.
	 *
	 * @param millis
	 *            the length of time to sleep in milliseconds
	 *
	 * @throws IllegalArgumentException
	 *             if the value of {@code millis} is negative
	 *
	 * @throws InterruptedException
	 *             if any thread has interrupted the current thread. The
	 *             <i>interrupted status</i> of the current thread is cleared when
	 *             this exception is thrown.
	 */
	public void sleep(long millis) throws InterruptedException {
		if (millis > 0) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest(Words.PAUSE + " " + millis + " ms.");

			Thread.sleep(millis);
		}
	}

	/**
	 * 
	 * Causes the currently executing thread to sleep (temporarily cease execution)
	 * for the specified number of milliseconds, subject to the precision and
	 * accuracy of system timers and schedulers. The thread does not lose ownership
	 * of any monitors.
	 * 
	 * Use this function instead of {@link Thread#sleep(long)} in order to avoid
	 * dead lock, especially when using {@link AgentFakeThread}.
	 *
	 * @param milliSeconds
	 *            the length of time to sleep in milliseconds
	 *
	 * @throws IllegalArgumentException
	 *             if the value of {@code millis} is negative
	 *
	 * @throws InterruptedException
	 *             if any thread has interrupted the current thread. The
	 *             <i>interrupted status</i> of the current thread is cleared when
	 *             this exception is thrown.
	 */
	protected void pause(final int milliSeconds) throws InterruptedException {
		sleep(milliSeconds);
	}

	/**
	 * Wait until the function {@link LockerCondition#isLocked()} return false
	 * 
	 * Use this function instead of {@link #wait()} in order to avoid dead lock,
	 * especially when using {@link AgentFakeThread}.
	 * 
	 * @param lockerCondition wait until this locker condition enables the thread unlocking
	 * @throws InterruptedException if an interrupt signal occurs 
	 */
	public void wait(LockerCondition lockerCondition) throws InterruptedException {
		getMadkitKernel().regularWait(this, lockerCondition);
	}

	/**
	 * The ID of an agent. All the agents have different hashCode value in one
	 * kernel. Thus it can be used to identify one agent. In a networked
	 * environment, this value should be used in combination with the kernelAddress
	 * of the agent for unique identification. This also holds when multiple MaDKit
	 * kernels are launched within the same JVM.
	 * 
	 * @return the agent's unique ID in the MaDKit kernel
	 */
	@Override
	final public int hashCode() {// TODO should be regenerated if agent are sent through the network in next
									// releases
		return (int) agentID;
	}

	/**
	 * Gets the agent id
	 * 
	 * @return the agent ID
	 */
	public long getAgentID() {
		return agentID;
	}

	/**
	 * Return a string representing a unique identifier for the agent over the
	 * network.
	 * 
	 * @return the agent's network identifier
	 */
	final public AgentNetworkID getNetworkID() {
		return new AgentNetworkID(getKernelAddress(), agentID);
	}

	final AtomicBoolean getAlive() {
		return alive;
	}

	/**
	 * Returns <code>true</code> if the agent has been launched and is not ended nor
	 * killed.
	 * @return <code>true</code> if the agent has been launched and is not ended nor
	 * killed.
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
		synchronized (state) {
			if (!state.compareAndSet(INITIALIZING, State.ACTIVATING))// TODO remove it when OK
				throw new AssertionError("not init in activation");
			state.notifyAll();
		}
		// that only when state is set to ACTIVATED
		setMyThread(Thread.currentThread());
		// can be killed from now on
		if (!alive.compareAndSet(false, true)) {// can be killed from now on
			throw new AssertionError("already alive in launch");
		}
		if (hasGUI) {
			if (logger != null && logger.isLoggable(Level.FINER)) {
				logger.finer("** setting up  GUI **");
			}
			sendMessage(Groups.GUI, Roles.GUI,
					new GUIMessage(GUIManagerAction.SETUP_AGENT_GUI, AbstractAgent.this));
			try {// wait answer using a big hack
				messageBox.take();// works because the agent cannot be joined in anyway
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logMethod(true);
	}

	private final void postActivate() {
		synchronized (state) {
			if (!state.compareAndSet(State.ACTIVATING, State.ACTIVATED))
				throw new AssertionError("not init in activation");

			state.notifyAll();
		}

	}

	/**
	 * @param myThread
	 *            the myThread to set
	 * @since MaDKit 5
	 */
	void setMyThread(final Thread thread) {
		thread.setName(getAgentThreadName(getState()));
	}

	public final String getAgentThreadName(final State s) {
		return s + "-" + getAgentID();
	}

	/**
	 * when this function finish, the current agent is ready to by killed
	 */
	protected void waitUntilReadyForKill() {

	}

	/**
	 * This is only called by MK threads and cannot be interrupted
	 * 
	 * @return <code>true</code> if the agent did not crash
	 */
	final ReturnCode activation() {
		ReturnCode result = ReturnCode.AGENT_CRASH;

		try {
			activationFirstStage();// the activated flag must be in the try
			activate();
			/*
			 * if (this instanceof AgentFakeThread) { AgentFakeThread
			 * This=(AgentFakeThread)this; for (Message m : This.messageBox)
			 * This.manageTaskMessage(m); }
			 */
			Thread.currentThread().setName("");
			/*
			 * synchronized (state) {
			 * Thread.currentThread().setName(getAgentThreadName(State.LIVING)); }// cannot
			 * be hard killed after that
			 */
			postActivate();
			result = SUCCESS;
		} catch (SelfKillException e) {
			logMethod(false);
			synchronized (state) {
				state.set(LIVING);// for the following kill to work
			}
			suicide(e);
			return SUCCESS;
		} catch (Throwable e) {
			validateDeathOnException(e, LIVING);
		}

		logMethod(false);
		return result;
	}

	final void logMethod(final boolean entering) {
		if (logger != null && logger.isLoggable(Level.FINER))
			logger.finer(
					"** " + (entering ? Words.ENTERING : Words.EXITING) + " " + getState().lifeCycleMethod() + " **");
	}

	/**
	 * @param e
	 */
	void suicide(SelfKillException e) {
		getMadkitKernel().startEndBehavior(this, true, true);
	}

	/**
	 * This method corresponds to the first behavior which is called by the MaDKit
	 * kernel when an agent is launched. Usually a good place to take a position in
	 * the organization of the artificial society.
	 * <p>
	 * Here is a typical example:
	 * </p>
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
	 * @throws InterruptedException
	 *             if an interrupted exception occurs
	 */
	protected void activate() throws InterruptedException {
	}

	final boolean ending() { // TODO boolean need ? NO
		if (state.get() != State.ZOMBIE)
			state.set(ENDING);
		Thread.currentThread().setName(getAgentThreadName(ENDING));
		logMethod(true);
		try {
			synchronized (state) {// can be hard killed from now on
				state.notify();
			}
			try {
				end();
			} catch (InterruptedException e) {
			} catch (Throwable e) {
				validateDeathOnException(e, TERMINATED);
			}
			synchronized (state) {
				alive.set(false);
				Thread.currentThread().setName(getAgentThreadName(TERMINATED));
			}

		} catch (KilledException e) {

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
				} // answer the kill
			}
		}
	}

	/**
	 * Calls when the life cycle quits
	 */
	// not final because of Scheduler and Watcher
	void terminate() {
		for (Message m : messageBox) {
			if (m.needReply())
				sendReplyEmpty(m);
		}

		Thread.currentThread().setName(getAgentThreadName(TERMINATED));

		synchronized (state) {
			state.set(TERMINATED);
			state.notify();
		}
		kernel = getMadkitKernel();

		if (hasGUI) {
			ReturnCode rc=kernel.broadcastMessageWithRole(this, Groups.GUI, Roles.GUI,
					new GUIMessage(GUIManagerAction.DISPOSE_AGENT_GUI, this), null, false);
			if (rc!=ReturnCode.SUCCESS)
				getLogger().warning("Agent GUI disposing. Impossible send message to GUI Manager Agent : "+rc);
		}

		try {
			kernel.removeAgentFromOrganizations(this);// TODO catch because of probe/activator
		} catch (Throwable e) {
			logLifeException(e);
		}

		if (logger != null) {
			logger.finest("** TERMINATED **");
			// TODO This should be done anyway but this would slow down kills
			// So there a risk of memory leak here because logger can be set to null after
			// creation and still exists in AgentLogger.loggers
			// But that should not be a problem because such a practice is usually not used
			logger.close();
			logger = null;
		}
		if (hasGUI) {
			AgentLogLevelMenu.remove(this);
			AgentStatusPanel.remove(this);
		}

		if (kernel.isHooked())
			kernel.informHooks(AgentActionEvent.AGENT_TERMINATED, this);

		kernel = TERMINATED_KERNEL;
		Thread.currentThread().setName("");

	}

	/**
	 * This method corresponds to the last behavior which is called by the MaDKit
	 * kernel. This call occurs when a threaded agent normally exits its live method
	 * or when the agent is killed. Usually a good place to release taken resources
	 * or log what has to be logged.
	 * 
	 * It has to be noted that the kernel automatically takes care of removing the
	 * agent from the organizations it is in. However, this cleaning is not logged
	 * by the agent. Therefore it could be of interest for the agent to do that
	 * itself.
	 * <p>
	 * Here is a typical example:
	 * </p>
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
	 * @throws InterruptedException
	 *             if an interrupted exception occurs
	 */
	protected void end() throws InterruptedException {
	}

	/**
	 * Launches a new agent in the MaDKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,false)</code>
	 * 
	 * @param agent
	 *            the agent to launch.
	 * @return
	 *         <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully completed
	 *         its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent
	 *         has been already launched</li>
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
	 *            the agent to launch.
	 * @param timeOutSeconds
	 *            time to wait the end of the agent's activation until returning a
	 *            TIMEOUT.
	 * @return
	 *         <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully completed
	 *         its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent
	 *         has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation time
	 *         of the agent is greater than <code>timeOutSeconds</code> seconds</li>
	 *         <li><code>{@link ReturnCode#AGENT_CRASH}</code>: If the agent crashed
	 *         during its <code>activate</code> method</li>
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
	 *            the agent to launch.
	 * @param createFrame
	 *            if <code>true</code>, the kernel will launch a JFrame for this
	 *            agent.
	 * @return
	 *         <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully completed
	 *         its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent
	 *         has been already launched</li>
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
	 * {@link AbstractAgent#activate()} method or when <code>timeOutSeconds</code>
	 * seconds elapsed. That is, the launched agent has not finished its
	 * {@link AbstractAgent#activate()} before the time out time elapsed.
	 * Additionally, if <code>createFrame</code> is <code>true</code>, it tells to
	 * MaDKit that an agent GUI should be managed by the Kernel. In such a case, the
	 * kernel takes the responsibility to assign a JFrame to the agent and to manage
	 * its life cycle (e.g. if the agent ends or is killed then the JFrame is
	 * closed) Using this feature there are two possibilities:
	 * <ul>
	 * <li>1. the agent overrides the method
	 * {@link AbstractAgent#setupFrame(JFrame)} and so setup the default JFrame as
	 * will</li>
	 * <li>2. the agent does not override it so that MaDKit will setup the JFrame
	 * with the default Graphical component delivered by the MaDKit platform:
	 * {@link OutputPanel}
	 * </ul>
	 * 
	 * @param agent
	 *            the agent to launch.
	 * @param timeOutSeconds
	 *            time to wait for the end of the agent's activation until returning
	 *            a TIMEOUT.
	 * @param createFrame
	 *            if <code>true</code>, the kernel will launch a JFrame for this
	 *            agent.
	 * @return
	 *         <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully completed
	 *         its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this agent
	 *         has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation time
	 *         of the agent is greater than <code>timeOutSeconds</code> seconds</li>
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
	 *            the full class name of the agent to launch
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
	 *            time to wait the end of the agent's activation until returning
	 *            <code>null</code>
	 * @param agentClass
	 *            the full class name of the agent to launch
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
	 *            if <code>true</code> a default GUI will be associated with the
	 *            launched agent
	 * @param agentClass
	 *            the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, final boolean createFrame) {
		return launchAgent(agentClass, Integer.MAX_VALUE, createFrame);
	}

	/**
	 * Launches a new agent using its full class name and returns when the launched
	 * agent has completed its {@link AbstractAgent#activate()} method or when the
	 * time out is elapsed. This has the same effect as
	 * {@link #launchAgent(AbstractAgent, int, boolean)} but allows to launch agent
	 * using a class name found reflexively.
	 * 
	 * The targeted agent class should have a default constructor for this to work.
	 * 
	 * Additionally, this method will launch the last compiled byte code of the
	 * corresponding class if it has been reloaded using
	 * {@link MadkitClassLoader#reloadClass(String)}. Finally, if the launch timely
	 * succeeded, this method returns the instance of the created agent.
	 * 
	 * @param timeOutSeconds
	 *            time to wait the end of the agent's activation until returning
	 *            <code>null</code>
	 * @param createFrame
	 *            if <code>true</code> a default GUI will be associated with the
	 *            launched agent
	 * @param agentClass
	 *            the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 */
	public AbstractAgent launchAgent(String agentClass, int timeOutSeconds, final boolean createFrame) {// TODO with
																										// args, and
																										// regardless of
																										// visibility
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest(Words.LAUNCH + " " + agentClass + " GUI " + createFrame);
		try {
			final Constructor<?> c = MadkitClassLoader.getLoader().loadClass(agentClass).getDeclaredConstructor();
			c.setAccessible(true);
			final AbstractAgent a = (AbstractAgent) c.newInstance();
			if (ReturnCode.SUCCESS == launchAgent(a, timeOutSeconds, createFrame))
				return a;
		} catch (InstantiationException | ClassCastException | ClassNotFoundException | IllegalAccessException
				| KernelException | NoSuchMethodException | SecurityException | IllegalArgumentException
				| InvocationTargetException e) {
			getLogger().severeLog(Influence.LAUNCH_AGENT.failedString(), e);
		}
		return null;
	}

	/**
	 * @param agentClass
	 * @param e
	 */
	final void cannotLaunchAgent(String agentClass, Throwable e, String infos) {
		getLogger().severeLog(ErrorMessages.CANT_LAUNCH + " " + agentClass + " : " + (infos != null ? infos : ""), e);
	}

	/**
	 * Optimizes mass agent launching. Launches <i><code>bucketSize</code></i>
	 * instances of <i><code>agentClassName</code></i> (an agent class) and put them
	 * in the artificial society at the locations defined by
	 * <code>cgrLocations</code>. Each string of the <code>cgrLocations</code> array
	 * defines a complete CGR location. So for example, <code>cgrLocations</code>
	 * could be defined and used with code such as :
	 * 
	 * 
	 * 
	 * <pre>
	 * launchAgentBucketWithRoles("madkitgroupextension.OneAgent", 1000000,
	 * 		new Role(new Group("community", "group"), "role"),
	 * 		new Role(new Group("anotherC", "anotherG"), "anotherR"))
	 * </pre>
	 * 
	 * In this example all the agents created by this process will have these two
	 * roles in the artificial society, even if they do not request them in their
	 * {@link AbstractAgent#activate()} method.
	 * <p>
	 * Additionally, in order to avoid to change the code of the agent considering
	 * how they will be launched (using the bucket mode or not). One should use the
	 * following alternative of the usual request method :
	 * {@link #bucketModeRequestRole(Group, String, Object)}: If used in
	 * {@link AbstractAgent#activate()}, these requests will be ignored when the
	 * bucket mode is used or normally proceeded otherwise.
	 * <p>
	 * 
	 * If some of the corresponding groups do not exist before this call, the caller
	 * agent will automatically become the manager of these groups.
	 * 
	 * @param agentClassName
	 *            the name of the class from which the agents should be built.
	 * @param bucketSize
	 *            the desired number of instances.
	 * @param cpuCoreNb
	 *            the number of parallel tasks to use. Beware that if cpuCoreNb is
	 *            greater than 1, the agents' constructors and
	 *            {@link AbstractAgent#activate()} methods will be called
	 *            simultaneously so that one has to be careful if shared resources
	 *            are accessed by the agents
	 * @param roles
	 *            default locations in the artificial society for the launched
	 *            agents. Each string of the <code>cgrLocations</code> array defines
	 *            a complete CGR location by separating C, G and R with commas as
	 *            follows:
	 *            <code>new Role(new Group("community", "group"), "role")</code>. It
	 *            can be <code>null</code>.
	 * @return a list containing all the agents which have been launched, or
	 *         <code>null</code> if the operation has failed
	 * @since MaDKit 5.0.0.6
	 * @since MadKitLanEdition 1.0
	 */
	public List<AbstractAgent> launchAgentBucket(String agentClassName, int bucketSize, int cpuCoreNb, Role... roles) {
		if (cpuCoreNb < 1 || bucketSize < 0)
			throw new IllegalArgumentException(
					"launchAgentBucket : cpuCoreNb = " + cpuCoreNb + " bucketsize = " + bucketSize);
		List<AbstractAgent> bucket = null;
		try {
			bucket = getMadkitKernel().createBucket(agentClassName, bucketSize, cpuCoreNb);
			launchAgentBucket(bucket, cpuCoreNb, roles);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			cannotLaunchAgent(agentClassName, e, null);
		}
		return bucket;

	}

	/**
	 * This has the same effect as
	 * <code>launchAgentBucket(agentClass, bucketSize, 1, roles)</code>.
	 * 
	 * @param agentClass
	 *            the name of the class from which the agents should be built.
	 * @param bucketSize
	 *            the desired number of instances.
	 * @param roles
	 *            default locations in the artificial society for the launched
	 *            agents. Each string of the <code>cgrLocations</code> array defines
	 *            a complete CGR location by separating C, G and R with commas as
	 *            follows:
	 *            <code>new Role(new Group("community", "group"), "role")</code>
	 * @return a list containing all the agents which have been launched, or
	 *         <code>null</code> if the operation has failed
	 * @since MaDKit 5.0.2
	 */
	public List<AbstractAgent> launchAgentBucket(String agentClass, int bucketSize, Role... roles) {
		return launchAgentBucket(agentClass, bucketSize, 1, roles);
	}

	/**
	 * This call is equivalent to This has the same effect as
	 * <code>launchAgentBucket(bucket, 1, roles)</code>, That is only one core will
	 * be used for the launch.
	 * 
	 * @param bucket
	 *            the list of agents to launch
	 * @param roles
	 *            default locations in the artificial society for the launched
	 *            agents. Each string of the <code>cgrLocations</code> array defines
	 *            a complete CGR location by separating C, G and R with commas as
	 *            follows:
	 *            <code>new Role(new Group("community", "group"), "role")</code>. It
	 *            can be <code>null</code>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void launchAgentBucket(List<? extends AbstractAgent> bucket, Role... roles) {
		getKernel().launchAgentBucketWithRoles(this, (List<AbstractAgent>) bucket, 1, roles);
	}

	/**
	 * Similar to {@link #launchAgentBucket(String, int, int, Role...)} except that the
	 * list of agents to launch is given. Especially, this could be used when the
	 * agents have no default constructor.
	 * 
	 * @param bucket
	 *            the list of agents to launch
	 * @param nbOfParallelTasks
	 *            the number of parallel tasks to use for launching the agents.
	 *            Beware that if <code>nbOfParallelTasks</code> is greater than 1,
	 *            the agents' {@link #activate()} methods will be call
	 *            simultaneously so that one has to be careful if shared resources
	 *            are accessed
	 * @param roles
	 *            default locations in the artificial society for the launched
	 *            agents. Each string of the <code>cgrLocations</code> array defines
	 *            a complete CGR location by separating C, G and R with commas as
	 *            follows:
	 *            <code>new Role(new Group("community", "group"), "role")</code>. It
	 *            can be <code>null</code>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void launchAgentBucket(List<? extends AbstractAgent> bucket, int nbOfParallelTasks, Role... roles) {
		getKernel().launchAgentBucketWithRoles(this, (List<AbstractAgent>) bucket, nbOfParallelTasks, roles);
	}

	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,Integer.MAX_VALUE, KillingType.JUST_KILL_IT)</code>
	 * 
	 * @param target
	 *            the agent to kill
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the target has
	 *         been already killed.</li>
	 *         <li><code>{@link ReturnCode#KILLING_ALREADY_IN_PROGRESS}</code>: If
	 *         the target already in a killing process.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the target
	 *         has not been launched.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see #killAgent(AbstractAgent, int, KillingType)
	 */
	public ReturnCode killAgent(final AbstractAgent target) {
		return killAgent(target, Integer.MAX_VALUE, KillingType.JUST_KILL_IT);
	}

	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,Integer.MAX_VALUE, killing_type)</code>
	 * 
	 * @param target
	 *            the agent to kill
	 * @param killing_type
	 *            the kill type (see {@link KillingType})
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the target has
	 *         been already killed.</li>
	 *         <li><code>{@link ReturnCode#KILLING_ALREADY_IN_PROGRESS}</code>: If
	 *         the target already in a killing process.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the target
	 *         has not been launched.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see #killAgent(AbstractAgent, int, KillingType)
	 * @see KillingType
	 */
	public ReturnCode killAgent(final AbstractAgent target, KillingType killing_type) {
		return killAgent(target, Integer.MAX_VALUE, killing_type);
	}

	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,timeOutSeconds, KillingType.JUST_KILL_IT)</code>
	 * 
	 * @param target
	 *            the agent to kill
	 * @param timeOutSeconds
	 *            time in seconds before considering the agent as a zombie
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the target has
	 *         been already killed.</li>
	 *         <li><code>{@link ReturnCode#KILLING_ALREADY_IN_PROGRESS}</code>: If
	 *         the target already in a killing process.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the target
	 *         has not been launched.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see #killAgent(AbstractAgent, int, KillingType)
	 */
	public ReturnCode killAgent(final AbstractAgent target, final int timeOutSeconds) {
		return this.killAgent(target, timeOutSeconds, KillingType.JUST_KILL_IT);
	}

	/**
	 * Kills the targeted agent. The kill process interrupts the agent's life cycle
	 * and allows it to process its {@link #end()} method.
	 *
	 * When the time out elapsed, the kill process will consider the agent as a
	 * zombie.
	 * 
	 * The method returns only when the targeted agent actually ends its life. So if
	 * the target contains a infinite loop, the caller can be blocked.
	 * 
	 * @param target
	 *            the agent to kill
	 * @param timeOutSeconds
	 *            time in seconds before considering the agent as a zombie
	 * @param killing_type
	 *            the kill type (see {@link KillingType})
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>
	 *         {@link ReturnCode#ALREADY_KILLED}</code>: If the target has been
	 *         already killed.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}
	 *         </code>: If the target has not been launched.</li>
	 *         
	 *         <li><code>{@link ReturnCode#KILLING_ALREADY_IN_PROGRESS}</code>: If
	 *         the target already in a killing process.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end method took
	 *         too much time and has been brutally stopped.</li>
	 *         </ul>
	 * @since MaDKit 5.0
	 * @see KillingType
	 */
	public ReturnCode killAgent(final AbstractAgent target, final int timeOutSeconds, KillingType killing_type) {// TODO
																													// check
																													// threads
																													// origin
		if (target == this && Thread.currentThread().getName().equals(getAgentThreadName(getState()))) {
			if (isFinestLogOn())
				logger.log(Level.FINEST, Influence.KILL_AGENT + " (" + timeOutSeconds + ")" + target.getName() + "...");
			// if (alive.compareAndSet(true, false)) {
			if (alive.get()) {
				throw new SelfKillException("" + timeOutSeconds, this, timeOutSeconds, KillingType.JUST_KILL_IT);
			}
		}

		return getKernel().killAgent(this, target, timeOutSeconds, killing_type);
	}

	/**
	 * @return the current kernel
	 */
	final MadkitKernel getKernel() {
		return kernel;
	}

	final boolean isKillingInProgress() {
		State s = getState();
		return s.include(State.WAIT_FOR_KILL) || s.include(State.ENDING);
	}

	/**
	 * @param kernel
	 *            the kernel to set
	 */
	final void setKernel(MadkitKernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * The agent's name.
	 * 
	 * @return the name to display in logger info, GUI title and so on. Default is
	 *         "<i>class name + internal ID</i>"
	 * 
	 */
	public String getName() {
		if (name == null)
			name = getClass().getSimpleName() + "-" + agentID;
		return name;
	}

	/**
	 * Changes the agent's name
	 * 
	 * @param name
	 *            the name to display in logger info, GUI title and so on, default
	 *            is "class name + internal ID"
	 */
	public void setName(final String name) {// TODO trigger gui changes and so on need AgentModel
		this.name = name;
	}

	/**
	 * Sets the agent's log level. This should be used instead of directly
	 * {@link AgentLogger#setLevel(Level)} because this also works when
	 * {@link #logger} is <code>null</code> and allows to set it to
	 * <code>null</code> to save cpu time.
	 * 
	 * @param newLevel
	 *            The log level under which log messages are displayed. If
	 *            {@link Level#OFF} is used then {@link #logger} is set to
	 *            <code>null</code>
	 * 
	 * @see #logger
	 */
	public void setLogLevel(final Level newLevel) {
		if (newLevel == null)
			throw new NullPointerException("newLevel");
		if (Level.OFF == newLevel) {
			if (logger != null && logger != AgentLogger.defaultAgentLogger) {
				logger.setLevel(newLevel);
				loggerModified(null);
			}
			logger = null;
			setKernel(getMadkitKernel());
		} else {
			getLogger().setLevel(newLevel);
			setKernel(kernel.getLoggedKernel());
		}
	}

	/**
	 * Returns the agent's logger.
	 * 
	 * @return the agent's logger. It cannot be <code>null</code> as it will be
	 *         created if necessary. But you can then still put {@link #logger} to
	 *         <code>null</code> for optimizing your code by using
	 *         {@link #setLogLevel(Level)} with {@link Level#OFF}.
	 * 
	 * @see AbstractAgent#logger
	 * @since MaDKit 5.0.0.6
	 */
	final public AgentLogger getLogger() {
		if (logger == AgentLogger.defaultAgentLogger || logger == null) {
			synchronized (this) {

				logger = AgentLogger.getLogger(this);
				loggerModified(logger);
			}
		}
		return logger;
	}

	/**
	 * This function is called when the logger is altered.
	 * 
	 * @param logger
	 *            the new logger
	 */
	protected void loggerModified(AgentLogger logger) {

	}

	/**
	 * Compares this agent with the specified agent for order with respect to
	 * instantiation time.
	 * 
	 * @param other
	 *            the agent to be compared.
	 * 
	 * @return a negative integer, a positive integer or zero as this agent has been
	 *         instantiated before, after or is the same agent than the specified
	 *         agent.
	 * 
	 */
	@Override
	public int compareTo(final AbstractAgent other) {
		long diff = agentID - other.agentID;
		if (diff < 0)
			return -1;
		else if (diff > 0)
			return 1;
		else
			return 0;
	}

	// ///////////////////////////////////////////////////////// GROUP & ROLE
	// METHODS AGR

	/**
	 * Creates a new Group within a community. This function is unuseful if groups
	 * auto-creation is activated through the function
	 * {@link #setAutoCreateGroup(boolean)}.
	 * 
	 * @param group
	 *            the group
	 * @param passKey
	 *            the <code>passKey</code> to enter a secured parent group. It is
	 *            generally delivered by the group's <i>group manager</i>. It could
	 *            be <code>null</code>, which is sufficient to enter an unsecured
	 *            group. Especially, {@link #createGroup(Group)} uses a
	 *            <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>{@link ReturnCode#MULTI_GROUP_NOT_ACCEPTED}</code>: If the
	 *         operation failed because such a group represent also its sub
	 *         groups.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the operation
	 *         failed because such a group has no right to be added into its parent
	 *         group.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(String, int, int, Role...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * 
	 * @see Group
	 * @see ReturnCode
	 * @see #setAutoCreateGroup(boolean)
	 * @since MaDKitLanEdition 1.0
	 */
	public ReturnCode createGroup(Group group, Object passKey) {
		if (getState() == INITIALIZING) {
			if (isWarningOn()) {
				handleException(Influence.CREATE_GROUP, new OrganizationWarning(ReturnCode.IGNORED, group));
			}
			return ReturnCode.IGNORED;
		}
		return getKernel().createGroup(this, group, passKey, true);
	}

	/**
	 * Creates a new Group within a community. This function is unuseful is groups
	 * auto-creation is activated through the function
	 * {@link #setAutoCreateGroup(boolean)}.
	 * 
	 * @param group
	 *            the group
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>{@link ReturnCode#MULTI_GROUP_NOT_ACCEPTED}</code>: If the
	 *         operation failed because such a group represent also its sub
	 *         groups.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the operation
	 *         failed because such a group has no right to be added into its parent
	 *         group.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(List, int, Role...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * 
	 * @see Group
	 * @see ReturnCode
	 * @see #setAutoCreateGroup(boolean)
	 * @since MaDKitLanEdition 1.0
	 */
	public ReturnCode createGroup(Group group) {
		return createGroup(group, null);
	}

	/**
	 * Tells if a group must be manually created through
	 * {@link AbstractAgent#createGroup(Group)} or if groups can be automatically
	 * created.
	 * 
	 * @param value
	 *            true if groups can be automatically created
	 * @since MaDKitLanEdition 1.0
	 */
	public void setAutoCreateGroup(boolean value) {
		getKernel().setAutoCreateGroup(this, value);
	}

	/**
	 * Tells if a group must be manually created through
	 * {@link AbstractAgent#createGroup(Group)} or if groups can be automatically
	 * created.
	 * 
	 * @return true if groups can be automatically created
	 */
	public boolean isAutoCreateGroup() {
		return getKernel().isAutoCreateGroup(this);
	}

	/**
	 * Creates a new Group within a community even if the agent has been launched
	 * using using one of the <code>launchAgentBucket</code> methods. This method is
	 * only useful when called within the {@link #activate()} method.
	 * <p>
	 * For instance, this is useful if you launch one million of agents and when
	 * only some of them have to create a specific group, not defined in the
	 * parameters of {@link #launchAgentBucket(String, int, int, Role...)}
	 * 
	 * This function is unuseful is groups auto-creation is activated through the
	 * function {@link #setAutoCreateGroup(boolean)}.
	 * 
	 * @param group
	 *            the group
	 * @param passKey
	 *            the <code>passKey</code> to enter a secured parent group. It is
	 *            generally delivered by the group's <i>group manager</i>. It could
	 *            be <code>null</code>, which is sufficient to enter an unsecured
	 *            group. Especially, {@link #bucketModeCreateGroup(Group)} uses a
	 *            <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         
	 *         <li><code>{@link ReturnCode#MULTI_GROUP_NOT_ACCEPTED}</code>: If the
	 *         operation failed because such a group represent also its sub
	 *         groups.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the operation
	 *         failed because such a group has no right to be added into its parent
	 *         group.</li> 
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         </ul>
	 * 
	 * 
	 * @see ReturnCode
	 * @see Group
	 * @see #setAutoCreateGroup(boolean)
	 * @since MaDKitLanEdition 1.0
	 * @since MaDKit 5.0.2
	 */
	public ReturnCode bucketModeCreateGroup(Group group, Object passKey) {
		return kernel.createGroup(this, group, passKey, true);
	}

	/**
	 * Creates a new Group within a community even if the agent has been launched
	 * using using one of the <code>launchAgentBucket</code> methods. This method is
	 * only useful when called within the {@link #activate()} method.
	 * <p>
	 * For instance, this is useful if you launch one million of agents and when
	 * only some of them have to create a specific group, not defined in the
	 * parameters of {@link #launchAgentBucket(String, int, int, Role...)}
	 * 
	 * This function is unuseful is groups auto-creation is activated through the
	 * function {@link #setAutoCreateGroup(boolean)}.
	 * 
	 * @param group
	 *            the group
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#MULTI_GROUP_NOT_ACCEPTED}</code>: If the
	 *         operation failed because such a group represent also its sub
	 *         groups.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the operation
	 *         failed because such a group has no right to be added into its parent
	 *         group.</li> 
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         </ul>
	 * 
	 * 
	 * @see ReturnCode
	 * @see #setAutoCreateGroup(boolean)
	 * @see Group
	 * @since MaDKitLanEdition 1.0
	 * @since MaDKit 5.0.2
	 */
	public ReturnCode bucketModeCreateGroup(Group group) {
		return bucketModeCreateGroup(group, null);
	}

	/**
	 * Makes this agent leaves the group of a particular community.
	 * 
	 * @param _group
	 *            the group
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of this group.</li>
	 *         </ul>
	 * 
	 * @since MadKitLanEdition 1.0
	 * @see Group
	 * @see ReturnCode
	 */
	public ReturnCode leaveGroup(Group _group) {
		return getKernel().leaveGroup(this, _group, true);
	}

	/**
	 * Requests a role within a group of a particular community. This has the same
	 * effect as <code>requestRole(group, role, null, false)</code>. So the passKey
	 * is <code>null</code> and the group must not be secured for this to succeed.
	 * 
	 * @param group
	 *            the targeted group.
	 * @param role
	 *            the desired role.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If the agent has been
	 *         launched using a <code>launchAgentBucket</code> method such as
	 *         {@link AbstractAgent#launchAgentBucket(List, int, Role...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul> 
	 * @see #requestRole(Group, String, Object)
	 * @since MadKitLanEdition 1.0
	 * @see Group
	 * 
	 */
	public ReturnCode requestRole(final Group group, final String role) {
		return requestRole(group, role, null);
	}

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 * 
	 * @param group
	 *            the targeted group.
	 * @param role
	 *            the desired role.
	 * @param passKey
	 *            the <code>passKey</code> to enter a secured group. It is generally
	 *            delivered by the group's <i>group manager</i>. It could be
	 *            <code>null</code>, which is sufficient to enter an unsecured
	 *            group. Especially, {@link #requestRole(Group, String)} uses a
	 *            <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If the agent has been
	 *         launched using a <code>launchAgentBucket</code> method such as
	 *         {@link AbstractAgent#launchAgentBucket(List, int, Role...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * @see Group
	 * 
	 * @since MaDKitLanEdition 1.0
	 */
	public ReturnCode requestRole(final Group group, final String role, final Object passKey) {
		if (getState() == INITIALIZING) {
			if (isWarningOn()) {
				handleException(Influence.REQUEST_ROLE, new OrganizationWarning(ReturnCode.IGNORED, group, role));
			}
			return ReturnCode.IGNORED;
		}
		return kernel.requestRole(this, group, role, passKey, true);
	}

	/**
	 * Requests a role even if the agent has been launched using one of the
	 * <code>launchAgentBucket</code> methods with non <code>null</code> roles.
	 * 
	 * For instance, this is useful if you launch one million of agents and when
	 * only some of them have to take a specific role which cannot be defined in the
	 * parameters of {@link #launchAgentBucket(String, int, int, Role...)} because they
	 * are priorly unknown and build at runtime.
	 * 
	 * @param _group
	 *            the targeted group.
	 * @param role
	 *            the desired role.
	 * @param passKey
	 *            the <code>passKey</code> to enter a secured group. It is generally
	 *            delivered by the group's <i>group manager</i>. It could be
	 *            <code>null</code>, which is sufficient to enter an unsecured
	 *            group. Especially, {@link #requestRole(Group, String)} uses a
	 *            <code>null</code> <code>passKey</code>.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * @see Group
	 * 
	 * @since MaDKitLanEdition 1.0
	 */
	public ReturnCode bucketModeRequestRole(Group _group, final String role, final Object passKey) {
		return kernel.requestRole(this, _group, role, passKey, true);
	}

	/**
	 * Abandons an handled role within a group of a particular community.
	 * 
	 * @param _group
	 *            the group
	 * @param role
	 *            the role name
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role is
	 *         not handled by this agent.</li>
	 *         </ul>
	 * @see AbstractAgent.ReturnCode
	 * @see Group
	 * 
	 * @since MaDKitLanEdition 1.0
	 * 
	 */
	public ReturnCode leaveRole(Group _group, final String role) {
		return getKernel().leaveRole(this, _group, role, true);
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
			final List<StackTraceElement> stack = new ArrayList<>();
			final String agentClassName = getClass().getName();
			for (int i = 0; i < stackTrace.length; i++) {
				final String trace = stackTrace[i].getClassName();
				if (!(trace.startsWith("madkit.kernel") || trace.startsWith("java.") || trace.startsWith("sun."))
						|| trace.contains(agentClassName)) {
					stack.add(stackTrace[i]);
				}
			}
			e.setStackTrace(stack.toArray(new StackTraceElement[0]));
		}
	}

	/**
	 * Agent's address at this CGR location.
	 * 
	 * 
	 * @param group the group
	 * @param role the role
	 * @return the agent's address in this location or <code>null</code> if this
	 *         agent does not handle this role or if group represents also its sub
	 *         groups.
	 * @since MaDKitLanEdition 1.0
	 * @see Group
	 */
	public AgentAddress getAgentAddressIn(Group group, final String role) {
		if (group.isUsedSubGroups())
			return null;
		else
			return kernel.getAgentAddressIn(this, group, role);
	}

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 * 
	 * @param group
	 *            the group
	 * @param role
	 *            the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this role
	 *         or <code>null</code> if such an agent does not exist.
	 * @see Group
	 */
	public AgentAddress getAgentWithRole(AbstractGroup group, final String role) {
		return getKernel().getAgentWithRole(this, group, role);
	}

	/**
	 * {@link AgentAddress} corresponding to an agent having this position in the
	 * organization on a particular kernel. The caller is excluded from the search.
	 * 
	 * @param group
	 *            the group
	 * @param role
	 *            the role name
	 * @param from
	 *            the kernel address on which the agent is running
	 * @return an {@link AgentAddress} corresponding to an agent handling this role
	 *         on the targeted kernel or <code>null</code> if such an agent does not
	 *         exist.
	 * @see Group
	 */
	public AgentAddress getDistantAgentWithRole(AbstractGroup group, final String role, final KernelAddress from) {
		ArrayList<AgentAddress> res = new ArrayList<>();
		for (Group g : group.getRepresentedGroups(from)) {
			res.add(getKernel().getDistantAgentWithRole(this, g, role, from));
		}
		if (res.size() == 0)
			return null;
		return res.get((int) (Math.random() * res.size()));
	}

	/**
	 * A list containing other agents playing this role in the organization. The
	 * caller is excluded from this list.
	 * 
	 * @param group
	 *            the group name
	 * @param role
	 *            the role name
	 * @return a {@link java.util.Set} containing agents that handle this role
	 * @see Group
	 */
	public Set<AgentAddress> getAgentsWithRole(AbstractGroup group, final String role) {

		return getAgentsWithRole(group, role, false);
	}

	/**
	 * A list containing all the agents playing this role in the organization.
	 * 
	 * @param group
	 *            the group
	 * @param role
	 *            the role name
	 * @param callerIncluded
	 *            if <code>false</code>, the caller is removed from the list if it
	 *            is in.
	 * @return a {@link java.util.Set} containing agents that handle this role.
	 * @see Group
	 */
	public Set<AgentAddress> getAgentsWithRole(AbstractGroup group, final String role, boolean callerIncluded) {

		return getKernel().getAgentsWithRole(this, group, role, callerIncluded);
	}

	/**
	 * Retrieves and removes the oldest received message contained in the mailbox.
	 * 
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
	public Message nextMessage() {
		final Message m = messageBox.poll();
		if (logger != null && logger.isLoggable(Level.FINEST)) {
			logger.finest("nextMessage = " + m);
		}
		if (m == null)
			return null;
		else
			return m.markMessageAsRead();
	}

	/**
	 * Retrieves and removes the first message of the mailbox that matches the
	 * filter.
	 * 
	 * @param filter the message filter
	 * @return The next acceptable message or <code>null</code> if such message has
	 *         not been found.
	 */
	public Message nextMessage(final MessageFilter filter) {

		messageBox.getLocker().lock();
		try {
			for (final Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
				final Message m = iterator.next();

				if (filter.accept(m)) {
					iterator.remove();
					return m.markMessageAsRead();
				}
			}
			return null;
		} finally {
			messageBox.getLocker().unlock();
		}
	}

	/**
	 * Retrieves and removes all the messages of the mailbox that match the filter.
	 * 
	 * @param filter
	 *            if <code>null</code> all the messages are returned and removed
	 *            from the mailbox.
	 * @return the ordered list of matching messages, or an empty list if none has
	 *         been found.
	 */
	public List<Message> nextMessages(final MessageFilter filter) {
		if (filter == null) {
			messageBox.getLocker().lock();
			try {
				final ArrayList<Message> match = new ArrayList<>(messageBox);
				for (int i = 0; i < match.size(); i++) {
					Message m = match.get(i);
					if (m != null)
						match.set(i, m.markMessageAsRead());
				}
				messageBox.clear();
				return match;
			} finally {
				messageBox.getLocker().unlock();
			}
		}
		final List<Message> match = new ArrayList<>();
		messageBox.getLocker().lock();
		try {
			for (Iterator<Message> iterator = messageBox.iterator(); iterator.hasNext();) {
				final Message m = iterator.next();
				if (filter.accept(m)) {
					iterator.remove();
					if (m != null)
						match.add(m.markMessageAsRead());
				}
			}
		} finally {
			messageBox.getLocker().unlock();
		}
		return match;
	}

	/**
	 * Gets the last received message.
	 * 
	 * @return the last received message or <code>null</code> if the mailbox is
	 *         empty.
	 */
	public Message getLastReceivedMessage() {
		Message m = messageBox.pollLast();
		if (m == null)
			return null;
		else
			return m.markMessageAsRead();
	}

	/**
	 * Gets the last received message according to a filter.
	 * 
	 * @param filter
	 *            the message filter to use
	 * 
	 * @return the last received message that matches the filter or
	 *         <code>null</code> if such message has not been found.
	 */
	public Message getLastReceivedMessage(final MessageFilter filter) {
		messageBox.getLocker().lock();
		try {
			for (final Iterator<Message> iterator = messageBox.descendingIterator(); iterator.hasNext();) {
				final Message message = iterator.next();
				if (filter.accept(message)) {
					iterator.remove();
					if (message == null)
						return null;
					else
						return message.markMessageAsRead();
				}
			}
			return null;
		} finally {
			messageBox.getLocker().unlock();
		}
	}

	/**
	 * Purges the mailbox and returns the most recent received message at that time.
	 * 
	 * @return the most recent received message or <code>null</code> if the mailbox
	 *         is already empty.
	 */
	public Message purgeMailbox() {
		messageBox.getLocker().lock();
		try {
			Message m = null;
			if (!messageBox.isEmpty()) {
				m = messageBox.pollLast();
				if (m != null)
					m = m.markMessageAsRead();
				for (Message m2 : messageBox)
					if (m2 != null)
						m2.markMessageAsRead();
				messageBox.clear();
			}
			return m;
		} finally {
			messageBox.getLocker().unlock();
		}

	}

	/**
	 * Tells if there is a message in the mailbox
	 * 
	 * @return <code>true</code> if there is no message in the mailbox.
	 */
	public boolean isMessageBoxEmpty() {
		return messageBox.isEmpty();
	}

	/**
	 * Sends a message to an agent using an agent address. This has the same effect
	 * as <code>sendMessageWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param receiver the receiver agent address
	 * @param messageToSend the message to send
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
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
	 * Sends a message, using an agent address, specifying explicitly the role used
	 * to send it.
	 * 
	 * @param receiver
	 *            the targeted agent
	 * @param message
	 *            the message to send
	 * @param senderRole
	 *            the sender role
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
	public ReturnCode sendMessageWithRole(final AgentAddress receiver, final Message message, final String senderRole) {
		return getKernel().sendMessage(this, receiver, message, senderRole);
	}

	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect as
	 * <code>sendMessageWithRole(group, role, messageToSend,null)</code>. If several
	 * agents match, the target is chosen randomly. The sender is excluded from this
	 * search.
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitLanEdition 1.0
	 */
	public ReturnCode sendMessage(AbstractGroup group, final String role, final Message messageToSend) {
		return sendMessageWithRole(group, role, messageToSend, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(groups, role, messageToSend,null)</code> . If
	 * several agents match, the target is chosen randomly. The sender is excluded
	 * from this search.
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send
	 * @param senderRole
	 *            the agent's role with which the message has to be sent
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitLanEdition 1.0
	 */
	public ReturnCode sendMessageWithRole(AbstractGroup group, final String role, final Message messageToSend,
			final String senderRole) {
		return getKernel().sendMessage(this, group, role, messageToSend, senderRole);
	}

	/**
	 * Tells if the current agent is concerned by the given agent address
	 * 
	 * @param agentAddress
	 *            the agent address to test
	 * @return true if the current agent is concerned by the given agent address.
	 */
	public boolean isConcernedBy(AgentAddress agentAddress) {
		return getKernel().isConcernedBy(this, agentAddress);
	}

	/**
	 * Request a hook on an agent action.
	 * 
	 * <pre>
	 * <code>
	 * requestHookEvents(AgentActionEvent.REQUEST_ROLE);
	 * </code>
	 * </pre>
	 * 
	 * In this example, the sender will be informed by the kernel of all successful
	 * requestRole operation made by the agents. This information will be
	 * transmitted using a subclass of HookMessage depending on the nature of the
	 * event. That is, {@link OrganizationEvent}, {@link MessageEvent} or
	 * {@link AgentLifeEvent} messages will be sent by the kernel according to the
	 * type of the hook which has been requested.
	 * <p>
	 * To give up the hook, just call the function {@link AbstractAgent#releaseHookEvents} and it will remove the sender from
	 * the subscriber list.
	 * 
	 * @param hookType
	 *            the action event type to monitor
	 * @return {@link ReturnCode#SUCCESS} or {@link ReturnCode#IGNORED}
	 * @see #releaseHookEvents
	 */
	public ReturnCode requestHookEvents(AgentActionEvent hookType) {
		return getKernel().requestHookEvents(this, hookType, false);
	}

	/**
	 * Release a hook on an agent action.
	 * 
	 * @param hookType
	 *            the action event type
	 * @return {@link ReturnCode#SUCCESS} or {@link ReturnCode#IGNORED}
	 * @see #requestHookEvents
	 */
	public ReturnCode releaseHookEvents(AgentActionEvent hookType) {
		return getKernel().releaseHookEvents(this, hookType);
	}

	/**
	 * Tells if the given agent address is local and valid.
	 * 
	 * @param agentAddress
	 *            the agent address to test
	 * @return true if the given agent address is local and valid.
	 */
	public boolean isLocalAgentAddressValid(AgentAddress agentAddress) {
		return getKernel().isLocalAgentAddressValid(this, agentAddress);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community,
	 * but not to the sender.
	 * 
	 * equivalent to
	 * <code>broadcastMessageWithRole(group, role, message, receiveAllRepliesInOneBlock, null)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param roleName
	 *            the role name
	 * @param message
	 *            the message
	 * @param receiveAllRepliesInOneBlock
	 *            if set true, all replies will be received into one block thanks to
	 *            {@link Replies}.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitLanEdition 1.0
	 */
	public ReturnCode broadcastMessage(AbstractGroup group, final String roleName, final Message message,
			boolean receiveAllRepliesInOneBlock) {
		return broadcastMessageWithRole(group, roleName, message, receiveAllRepliesInOneBlock, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community,
	 * but not to the sender.
	 * 
	 * equivalent to
	 * <code>broadcastMessageWithRole(group, role, null, message)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param roleName
	 *            the role name
	 * @param message
	 *            the message
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitLanEdition 1.0
	 */
	public ReturnCode broadcastMessage(AbstractGroup group, final String roleName, final Message message) {
		return broadcastMessageWithRole(group, roleName, message, null);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender. The sender is excluded from the search.
	 * 
	 * equivalent to
	 * <code>broadcastMessageWithRole(group, role, message, false, senderRole, true)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param roleName
	 *            the role name
	 * @param messageToSend
	 *            the message
	 * @param senderRole
	 *            the role name of the sender
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
	public ReturnCode broadcastMessageWithRole(AbstractGroup group, final String roleName, final Message messageToSend,
			final String senderRole) {
		return this.broadcastMessageWithRole(group, roleName, messageToSend, false, senderRole);
	}

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender. The sender is excluded from the search.
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param roleName
	 *            the role name
	 * @param messageToSend
	 *            the message
	 * @param receiveAllRepliesInOneBlock
	 *            if set true, all replies will be received into one block thanks to
	 *            {@link Replies}.
	 * @param senderRole
	 *            the role name of the sender
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the community
	 *         does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no agent
	 *         was found as recipient, i.e. the sender was the only agent having
	 *         this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
	public ReturnCode broadcastMessageWithRole(AbstractGroup group, final String roleName, final Message messageToSend,
			boolean receiveAllRepliesInOneBlock, final String senderRole) {
		return getKernel().broadcastMessageWithRole(this, group, roleName, messageToSend, senderRole,
				receiveAllRepliesInOneBlock);
	}

	/**
	 * Sends a message by replying to a previously received message. The sender is
	 * excluded from this search.
	 * 
	 * @param messageToReplyTo
	 *            the previously received message.
	 * @param reply
	 *            the reply itself.
	 * @param senderRole
	 *            the agent's role with which the message should be sent
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no
	 *         longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
	public ReturnCode sendReplyWithRole(final Message messageToReplyTo, final Message reply, final String senderRole) {
		final AgentAddress target = messageToReplyTo.getSender();
		if (target == null)
			return ReturnCode.CANT_REPLY;
		reply.setIDFrom(messageToReplyTo);
		return getKernel().sendMessage(this, target, reply, senderRole);
	}

	/**
	 * Sends a message by replying to a previously received message. This has the
	 * same effect as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param messageToReplyTo
	 *            the previously received message.
	 * @param reply
	 *            the reply itself.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no
	 *         longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see AbstractAgent#sendReplyWithRole(Message, Message, String)
	 * 
	 */
	public ReturnCode sendReply(final Message messageToReplyTo, final Message reply) {
		return sendReplyWithRole(messageToReplyTo, reply, null);
	}

	/**
	 * Sends an empty message by replying to a previously received message. This has
	 * the same effect as
	 * <code>sendReplyWithRole(messageToReplyTo, new EmptyMessage(), null)</code>.
	 * 
	 * @param messageToReplyTo
	 *            the previously received message.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is no
	 *         longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the receiver
	 *         agent address.</li>
	 *         </ul>
	 * @see AbstractAgent#sendReplyWithRole(Message, Message, String)
	 * 
	 */
	public ReturnCode sendReplyEmpty(final Message messageToReplyTo) {
		return sendReplyWithRole(messageToReplyTo, new EmptyMessage(), null);
	}

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 * 
	 * @param originalMessage
	 *            the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	public Message getReplyTo(final Message originalMessage) {
		return nextMessage(new ConversationFilter(originalMessage));
	}

	/**
	 * This method offers a convenient way for regular object to send messages to
	 * Agents, especially threaded agents. For instance when a GUI wants to discuss
	 * with its linked agent: This allows to enqueue work to do in their life cycle
	 * 
	 * @param m the received message
	 * @return the message actually received
	 */
	public Message receiveMessage(final Message m) {
		if (m == null)
			return null;
		Message messageTaken = m;
		Replies r = getConversation(m);
		if (r != null) {
			synchronized (r) {
				if (r.addReply(m) && removeConversation(r)) {
					messageTaken = r;
					r = null;
				} else {
					messageTaken = null;
				}
			}
		}
		if (messageTaken != null) {
			if (messageTaken instanceof LocalLanMessage)
				getMadkitKernel().receivingPotentialNetworkMessage(this, (LocalLanMessage) messageTaken);
			messageBox.offer(messageTaken); // TODO test vs. arraylist and synchronized
		}
		// if(messageBox == null)
		// messageBox = new LinkedBlockingDeque<Message>();
		// messageBox.offer(m); // TODO test vs. arraylist and synchronized
		return messageTaken;
	}

	/**
	 * Called when the default GUI mechanism is used upon agent creation. This
	 * provides an empty frame which will be used as GUI for the agent. The life
	 * cycle of the frame is automatically managed: the frame is disposed when the
	 * agent is terminated. Some menus are available by default. Default code is
	 * only one line: <code>frame.add(new IOPanel(this));</code>.
	 * 
	 * Default settings for the frame are:
	 * <ul>
	 * <li>width = 400</li>
	 * <li>height = 300</li>
	 * <li>location = center of the screen</li>
	 * <li>a JMenuBar with: {@link MadkitMenu}, {@link AgentMenu} and
	 * {@link AgentLogLevelMenu}</li>
	 * </ul>
	 * 
	 * @param frame
	 *            the default frame which has been created by MaDKit for this agent.
	 * @since MaDKit 5.0.0.8
	 * @see com.distrimind.madkit.gui.OutputPanel
	 */
	public void setupFrame(final JFrame frame) {
		frame.add(new OutputPanel(this));
	}

	// /////////////////////////////////////////////// UTILITIES
	// /////////////////////////////////

	/**
	 * Returns a snapshot view of all the current organization for debugging
	 * purpose. Community -&gt; Group -&gt; Role -&gt; AgentAddress
	 * 
	 * @param global
	 *            if <code>true</code> this takes into account agents coming from
	 *            other connected kernels
	 * 
	 * @return a data containing all the organization structure
	 */
	public Map<String, Map<Group, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global) {
		return getKernel().getOrganizationSnapShot(global);
	}

	/**
	 * Returns a snapshot view of all the current organization for debugging
	 * purpose. Group -&gt; Role -&gt; AgentAddress
	 * 
	 * @param concerned_groups
	 *            the concerned communities and groups
	 * @param global
	 *            if <code>true</code> this takes into account agents coming from
	 *            other connected kernels
	 * 
	 * @return a data containing all the organization structure
	 */
	public Map<String, Map<Group, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(
			Collection<Group> concerned_groups, boolean global) {
		return getKernel().getOrganizationSnapShot(concerned_groups, global);
	}

	/**
	 * Returns the names of the communities that exist.
	 * 
	 * @return an alphanumerically ordered set containing the names of the
	 *         communities which exist.
	 * 
	 * @since MaDKit 5.0.0.20
	 */
	public TreeSet<String> getExistingCommunities() {
		return getKernel().getExistingCommunities();
	}

	/**
	 * Returns the names of the groups that exist in this community.
	 * 
	 * @param community
	 *            the community's name
	 * 
	 * @return a list of groups which exist in this community, or <code>null</code>
	 *         if this community does not exist.
	 * 
	 * @since MaDKit 5.0.0.20
	 */
	public Enumeration<Group> getExistingGroups(final String community) {
		return getKernel().getExistingGroups(community);
	}

	/**
	 * Gets the names of the groups the agent is in according to a community
	 * 
	 * @param community the community name
	 * @return a list containing the groups the agent is in, or <code>null</code> if
	 *         this community does not exist. This set could be empty.
	 */
	public TreeSet<Group> getMyGroups(final String community) {
		return getKernel().getGroupsOf(this, community);
	}

	/**
	 * Gets the names of the roles that the agent has in a specific group
	 * 
	 * @param group the group name
	 * @return a sorted set containing the names of the roles the agent has in a
	 *         group, or <code>null</code> if the community or the group does not
	 *         exist. This set could be empty.
	 */
	public TreeSet<String> getMyRoles(Group group) {
		return getKernel().getRolesOf(this, group);
	}

	/**
	 * Returns the names of the roles that exist in this group.
	 * 
	 * @param group
	 *            the group
	 * 
	 * @return an alphanumerically ordered set containing the names of the roles
	 *         which exist in this group, or <code>null</code> if it does not exist.
	 * 
	 * @since MaDKit 5.0.0.20
	 */
	public TreeSet<String> getExistingRoles(Group group) {
		return getKernel().getExistingRoles(group);
	}

	/**
	 * Checks if this agent address is still valid. I.e. the corresponding agent is
	 * still playing this role.
	 * @param agentAddress the agent address
	 * @return <code>true</code> if the address still exists in the organization.
	 * @since MaDKit 5.0.4
	 */
	public boolean checkAgentAddress(final AgentAddress agentAddress) {
		return getMadkitKernel().resolveAddress(agentAddress) != null;
	}

	/**
	 * Tells if a community exists in the artificial society.
	 * 
	 * @param community
	 *            the name of the community
	 * @return <code>true</code> If a community with this name exists,
	 *         <code>false</code> otherwise.
	 */
	public boolean isCommunity(final String community) {
		return getKernel().isCommunity(this, community);
	}

	/**
	 * Tells if a group was created
	 * 
	 * @param group
	 *            the group
	 * @return <code>true</code> If a group with this name was created,
	 *         <code>false</code> otherwise.
	 */
	public boolean isCreatedGroup(Group group) {
		return group.isMadKitCreated(getKernelAddress());
	}

	/**
	 * Tells if the agent is currently playing a role with the given group.
	 * 
	 * @param group
	 *            the group
	 * @return <code>true</code> If a group with this name exists in this community,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasGroup(Group group) {
		try {
			InternalGroup ig = getMadkitKernel().getGroup(group);
			if (ig == null)
				return false;

			return ig.hasRoleWith(this);
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * Tells if the agent is currently playing a specific role.
	 * 
	 * @param group the group
	 * @param role the role
	 * @return <code>true</code> if the agent is playing this role
	 * 
	 * @since MaDKit 5.0.3
	 */
	public boolean hasRole(Group group, final String role) {
		try {
			return getMadkitKernel().getRole(group, role).contains(this);
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * Tells if a role exists in the artificial society.
	 * 
	 * @param group
	 *            the the group
	 * @param role
	 *            the name of the role
	 * @return <code>true</code> If a role with this name exists in this
	 *         &lt;community;group&gt; couple, <code>false</code> otherwise.
	 */
	public boolean isCreatedRole(Group group, final String role) {
		return getKernel().isRole(this, group, role);
	}

	/**
	 * @return a <code>String</code> giving the name and the current state of the
	 *         agent
	 */
	@Override
	public String toString() {
		return getName() + " (" + getState() + ")";
	}

	/**
	 * Returns the Properties object of this MaDKit session. That is by default the
	 * parameter which has been used to launch the kernel the agent is running on.
	 * If the agent has not been launched yet, the Properties returned is the
	 * default MaDKit configuration. It can be programmatically modified to launch a
	 * new session with different parameters. It can also be used as a black board
	 * shared by all the agents of a kernel by adding new user defined properties at
	 * run time or via the command line. 
	 * 
	 * 
	 * 
	 * @return the Properties object defining the values of each MaDKit options in
	 *         the current session.
	 * @see MadkitProperties
	 * @since MaDKit 5.0.0.10
	 */
	public MadkitProperties getMadkitConfig() {
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
	 * The kernel's address on which this agent is running.
	 * 
	 * @return the kernel address representing the MaDKit kernel on which the agent
	 *         is running
	 */
	public KernelAddress getKernelAddress() {
		return kernel.getKernelAddress();
	}

	/**
	 * Returns the server's info, IP and port, if the kernel is online.
	 * 
	 * @return server's info: e.g. /192.168.1.14:4444
	 */
	public String getServerInfo() {
		return getMadkitKernel().getServerInfo();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Utilities
	// //////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	final boolean logLifeException(final Throwable e) {
		if (e instanceof KilledException || e instanceof IllegalMonitorStateException) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("-*-GET KILLED in " + getState().lifeCycleMethod() + "-*-");
		} else {
			if (alive.get() || state.get() == ENDING) {
				getLogger().severeLog("-*-" + getState().lifeCycleMethod() + " BUG*-*", e);
			} else {
				e.printStackTrace();
			}
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////// Synchronization
	// //////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * @throws InterruptedException
	 * @since MaDKit 5.0.0.9
	 */
	Message waitingNextMessage(final long timeout, final TimeUnit unit) throws InterruptedException {
		checkInterruptedExceptionForMessageWaiting();
		Message m = messageBox.poll(timeout, unit);
		if (m != null)
			return m.markMessageAsRead();
		else
			return null;
	}

	/**
	 * Wipes out an entire community at once. Mostly useful when doing simulated
	 * systems. This greatly optimizes the time required to make all the agents
	 * leave a community.
	 * 
	 * @since MaDKitLanEdition 1.0
	 * @param community
	 *            the community to destroy
	 */
	public void destroyCommunity(String community) {
		getKernel().destroyCommunity(this, community);
	}

	/**
	 * Wipes out an entire group at once. Mostly useful when doing simulated
	 * systems. This greatly optimizes the time required to make all the agents
	 * leave a group.
	 * 
	 * @since MaDKitLanEdition 1.0
	 * @param group
	 *            the group to destroy
	 */
	public void destroyGroup(Group group) {
		getKernel().destroyGroup(this, group);
	}

	/**
	 * Wipes out an entire role at once. Mostly useful when doing simulated systems.
	 * This greatly optimizes the time required to make all the agents leave a role.
	 * 
	 * @since MaDKitLanEdition 1.0
	 * @param group
	 *            the group
	 * @param role
	 *            the group to destroy
	 */
	public void destroyRole(Group group, String role) {
		getKernel().destroyRole(this, group, role);
	}


	void checkInterruptedExceptionForMessageWaiting() throws InterruptedException {

	}

	/**
	 * @throws InterruptedException
	 * @since MaDKit 5.0.0.9
	 */
	List<Message> waitAnswers(final Message message, final int size, final Integer timeOutMilliSeconds)
			throws InterruptedException {
		checkInterruptedExceptionForMessageWaiting();
		final long endTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds.intValue());
		final ConversationID conversationID = message.getConversationID();
		int missing = size;
		final List<Message> receptions = new ArrayList<>(messageBox.size());
		final ArrayList<Message> answers = new ArrayList<>(size);
		while (missing > 0 && System.nanoTime() < endTime) {
			Message answer = waitingNextMessage(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
			if (answer == null)
				break;
			if (answer.getConversationID().equals(conversationID)) {
				answers.add(answer);
				missing--;
			} else
				receptions.add(answer);
		}
		addAllToMessageBox(receptions);
		if (!answers.isEmpty()) {
			for (int i = 0; i < answers.size(); i++) {
				answers.set(i, answers.get(i).markMessageAsRead());
			}
			return answers;
		}
		return null;
	}

	/**
	 * @param receptions
	 */
	void addAllToMessageBox(final List<Message> receptions) {
		messageBox.getLocker().lock();
		try {
			messageBox.addAll(receptions);
		} finally {
			messageBox.getLocker().unlock();
		}
	}

	/**
	 * Logs and propagates the exception so that agents properly leave when
	 * interrupted. When you have to deal with such an exception and do not want to
	 * add <code>throws InterruptedException</code> in your code, it is both
	 * important for the responsiveness of your application and a good practice to
	 * not swallow it by doing something like.
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
	 *            the InterruptedException which has to be propagated
	 * @since MaDKit 5.0.0.12
	 */
	/*
	 * final void handleInterruptedException() {// TODO if
	 * (Thread.currentThread().getName().equals(getAgentThreadName(getState())) &&
	 * alive.compareAndSet(true, false)) throw new SelfKillException("" + 0,
	 * this);// TODO why 0 ?
	 * 
	 * Thread.currentThread().interrupt(); }
	 */

	/**
	 * launch all the agents defined in an xml configuration file
	 * 
	 * @param document
	 *            the XML document to parse
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * @throws ParserConfigurationException if a problem occurs
	 * @throws IOException if a problem occurs
	 * @throws SAXException if a problem occurs
	 */
	public ReturnCode launchXmlAgents(Document document)
			throws SAXException, IOException, ParserConfigurationException {
		final NodeList nodes = document.getElementsByTagName(XMLUtilities.AGENT);
		ReturnCode r = ReturnCode.SEVERE;
		for (int i = 0; i < nodes.getLength(); i++) {
			r = launchNode(nodes.item(i));
		}
		return r;
	}

	/**
	 * launch all the agents defined in an xml configuration file
	 * 
	 * @param xmlFile
	 *            the XML file to parse
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * @throws ParserConfigurationException if a problem occurs
	 * @throws IOException if a problem occurs
	 * @throws SAXException if a problem occurs
	 */
	public ReturnCode launchXmlAgents(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
		return launchXmlAgents(XMLUtilities.getDOM(xmlFile));
	}

	/**
	 * Launch agents by parsing an XML node. The method immediately returns without
	 * waiting the end of the agents' activation,
	 * 
	 * @param agentXmlNode
	 *            the XML node
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * 
	 * @see XMLUtilities
	 */
	public ReturnCode launchNode(Node agentXmlNode) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("launchNode " + XMLUtilities.nodeToString(agentXmlNode));
		final NamedNodeMap namesMap = agentXmlNode.getAttributes();
		try {
			List<AbstractAgent> list = null;
			int nbOfInstances = 1;
			try {
				nbOfInstances = Integer.parseInt(namesMap.getNamedItem(XMLUtilities.NB_OF_INSTANCES).getNodeValue());
			} catch (NullPointerException e) {
			}
			list = getKernel().createBucket(namesMap.getNamedItem(XMLUtilities.CLASS).getNodeValue(), nbOfInstances, 1);

			// required for bucket mode with no roles
			boolean bucketMode = false;
			try {
				bucketMode = Boolean.parseBoolean(namesMap.getNamedItem(XMLUtilities.BUCKET_MODE).getNodeValue());
			} catch (NullPointerException e) {
			}

			NodeList attributes = agentXmlNode.getChildNodes();
			List<Role> roles = new ArrayList<>();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				switch (node.getNodeName()) {
				case XMLUtilities.ATTRIBUTES:
					NamedNodeMap att = node.getAttributes();
					final Class<? extends AbstractAgent> agentClass = list.get(0).getClass();
					for (int j = 0; j < att.getLength(); j++) {
						Node item = att.item(j);
						setAgentValues(Probe.findFieldOn(agentClass, item.getNodeName()), item.getNodeValue(), list);
					}
					break;
				case XMLUtilities.BUCKET_MODE_ROLE:
					bucketMode = true;
					NamedNodeMap roleAttributes = node.getAttributes();
					roles.add(new Role(Group.getGroupFromPath(roleAttributes.item(0).getNodeValue(),
							roleAttributes.item(1).getNodeValue()), roleAttributes.item(2).getNodeValue()));
					break;
				default:
					break;
				}
			}

			if (bucketMode) {
				launchAgentBucket(list, roles.toArray(new Role[roles.size()]));
			} else {
				try {
					Level logLevel = Level.parse(namesMap.getNamedItem(XMLUtilities.LOG_LEVEL).getNodeValue());
					for (AbstractAgent abstractAgent : list) {
						abstractAgent.setLogLevel(logLevel);
					}
				} catch (NullPointerException e) {
				}

				boolean guiMode = false;
				try {
					guiMode = Boolean.parseBoolean(namesMap.getNamedItem(XMLUtilities.GUI).getNodeValue());
				} catch (NullPointerException e) {
				}
				for (final AbstractAgent abstractAgent : list) {
					launchAgent(abstractAgent, 0, guiMode);// TODO check return code -> only to here, do a version with
															// parameterized timeout
				}
			}
		} catch (NullPointerException | ClassNotFoundException | NoSuchFieldException | NumberFormatException
				| InstantiationException | IllegalAccessException e) {
			getLogger().severeLog("launchNode " + Words.FAILED + " : " + XMLUtilities.nodeToString(agentXmlNode), e);
			return ReturnCode.SEVERE;
		}
		return ReturnCode.SUCCESS;
	}

	/**
	 * @param stringValue
	 * @param type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void setAgentValues(final Field f, final String stringValue, List<AbstractAgent> l)
			throws IllegalAccessException {
		final Class<?> type = f.getType();
		if (type.isPrimitive()) {
			if (type == int.class) {
				int value = Integer.parseInt(stringValue);
				for (AbstractAgent a : l) {
					f.setInt(a, value);
				}
			} else if (type == boolean.class) {
				boolean value = Boolean.parseBoolean(stringValue);
				for (AbstractAgent a : l) {
					f.setBoolean(a, value);
				}
			} else if (type == float.class) {
				float value = Float.parseFloat(stringValue);
				for (AbstractAgent a : l) {
					f.setFloat(a, value);
				}
			} else if (type == double.class) {
				double value = Double.parseDouble(stringValue);
				for (AbstractAgent a : l) {
					f.setDouble(a, value);
				}
			} else if (type == byte.class) {
				byte value = Byte.parseByte(stringValue);
				for (AbstractAgent a : l) {
					f.setByte(a, value);
				}
			} else if (type == short.class) {
				short value = Short.parseShort(stringValue);
				for (AbstractAgent a : l) {
					f.setShort(a, value);
				}
			} else if (type == long.class) {
				long value = Long.parseLong(stringValue);
				for (AbstractAgent a : l) {
					f.setLong(a, value);
				}
			}
		} else if (type == Integer.class) {
			int value = Integer.parseInt(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Integer(value));
			}
		} else if (type == Boolean.class) {
			boolean value = Boolean.parseBoolean(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Boolean(value));
			}
		} else if (type == Float.class) {
			float value = Float.parseFloat(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Float(value));
			}
		} else if (type == Double.class) {
			double value = Double.parseDouble(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Double(value));
			}
		} else if (type == String.class) {
			for (AbstractAgent a : l) {
				f.set(a, stringValue);
			}
		} else if (type == Byte.class) {
			byte value = Byte.parseByte(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Byte(value));
			}
		} else if (type == Short.class) {
			short value = Short.parseShort(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Short(value));
			}
		} else if (type == Long.class) {
			long value = Long.parseLong(stringValue);
			for (AbstractAgent a : l) {
				f.set(a, new Long(value));
			}
		} else {
			if (logger != null)
				logger.severe("Do not know how to change attrib " + stringValue);
		}
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
	 * <li>{@link #ACTIVATING}<br>
	 * An agent that is processing its {@link #activate()} method is in this
	 * state.</li>
	 * <li>{@link #LIVING}<br>
	 * An agent that is processing its {@link Agent#preLiveCycle()} method just before calling in a loop its {@link Agent#liveCycle()} method is in this
	 * state.
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
		NOT_LAUNCHED(null),

		/**
		 * The agent has been launched and is being registered by the kernel but it has
		 * not started its {@link #activate()} method yet.
		 */
		INITIALIZING(NOT_LAUNCHED),

		/**
		 * 
		 * The agent is processing its {@link #activate()} method.
		 */
		ACTIVATING(INITIALIZING),

		/**
		 * 
		 * The agent is processing has been activated.
		 */
		ACTIVATED(ACTIVATING),

		/**
		 * The agent is processing has been initialized and activated.
		 */
		LIVING(ACTIVATED),

		/**
		 * the agent has to read all its messages before being killed.
		 */
		WAIT_FOR_KILL(LIVING, INITIALIZING),

		/**
		 * Equivalent to LIVING, but the agent has to read all its messages before being
		 * killed. No new message can be received with this state.
		 */
		LIVING_BUG_WAIT_FOR_KILL(LIVING, LIVING, WAIT_FOR_KILL),

		/**
		 * The agent is processing its {@link AbstractAgent#end()} method.
		 */
		ENDING(LIVING),

		/**
		 * The agent can't be killed and becomes a zombie process
		 */
		ZOMBIE(LIVING),

		/**
		 * The agent has finished its life cycle in the MaDKit platform.
		 */
		TERMINATED(ENDING);

		private final State included_states[];

		private int previousState;

		State(State previousState, State... _included_states) {
			included_states = _included_states;
			this.previousState = previousState == null ? this.ordinal() : previousState.ordinal();
		}

		void setPreviousState(State s) {
			previousState = s.ordinal();
		}

		State getPreviousState() {
			return State.values()[previousState];
		}

		final String lifeCycleMethod() {
			switch (this) {
			case ACTIVATING:
				return "ACTIVATE";
			case LIVING:
				return "LIVE";
			case LIVING_BUG_WAIT_FOR_KILL:
				return "WAIT FOR KILL";
			case TERMINATED:
				return "TERMINATE";
			case ENDING:
				return "END";
			default:
				return name();
			}
		}

		/**
		 * 
		 * @param state the state
		 * @return true if this state is included by the given state parameter
		 */
		public boolean include(State state) {
			if (included_states == null)
				return false;
			if (this.equals(state))
				return true;
			for (State s : included_states) {
				if (s.equals(state))
					return true;
			}
			return false;
		}

	}

	/**
	 * When killing an agent thanks to the function
	 * {@link AbstractAgent#killAgent(AbstractAgent, int, KillingType)}, this
	 * enumeration specifies the kill method.
	 * 
	 * @author Jason Mahdjoub
	 * @since MadkitLanEdition 1.0
	 * @version 1.0
	 */
	public enum KillingType {
		/**
		 * Do not kill the considered agent until its message box is empty. However,
		 * when the kill order is given, the considered agent cannot receive new
		 * messages. When the agent is killed, the message box is empty. At this moment,
		 * the agent is killed just after a life cycle. This works only for
		 * {@link Agent} and {@link AgentFakeThread} agent types.
		 */
		WAIT_AGENT_PURGE_ITS_MESSAGES_BOX_BEFORE_KILLING_IT,

		/**
		 * Before killing the considered agent, each unread message contained into its
		 * message box is returned to its sender, thanks to the
		 * {@link UndelievredMessage} class. After that, the agent is killed just after
		 * a life cycle.
		 */
		// KILL_IT_NOW_AND_RETURNS_UNREADED_MESSAGES,

		/**
		 * The agent is killed just after a life cycle. Do not returns undelivered
		 * messages.
		 */
		JUST_KILL_IT,
	}

	/**
	 * Returns the current state of the agent in the MaDKit platform.
	 * 
	 * This method provides a way of knowing what is the current state of the agent
	 * regarding its life cycle. This could be convenient when you design a method
	 * that could work differently depending on the actual state of the agent.
	 * 
	 * @return the current state of the agent:
	 *         <ul>
	 *         <li><code>{@link State#NOT_LAUNCHED}</code>: the agent has not been
	 *         launched yet. This especially means that most of the methods of this
	 *         API still do not work for this agent as it has not been registered
	 *         yet.</li>
	 *         <li><code>{@link State#INITIALIZING}</code>: the agent is being
	 *         registered by the kernel but has not started its {@link #activate()}
	 *         method yet.</li>
	 *         <li><code>{@link State#ACTIVATING}</code>: the agent is processing
	 *         its {@link #activate()} method. This state is also the "running"
	 *         state of {@link AbstractAgent} subclasses (i.e. when they have
	 *         finished their activation) as they do not have a {@link Agent#liveCycle()}
	 *         managed by the kernel in their life cycle. On the contrary to
	 *         {@link Agent} subclasses which next state is
	 *         {@link State#LIVING}.</li>
	 *         <li><code>{@link State#LIVING}</code>: returned when {@link Agent}
	 *         subclasses are processing in loop their {@link Agent#liveCycle()} method.</li>
	 *         
	 *         <li><code>{@link State#ENDING}</code>: the agent is processing its
	 *         {@link #end()} method.</li>
	 *         <li><code>{@link State#TERMINATED}</code>: the agent has finished its
	 *         life in the MaDKit platform. Especially, most of the methods of this
	 *         API will no longer work for this agent.</li>
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
		try {
			MadkitClassLoader.reloadClass(getClass().getName());
		} catch (ClassNotFoundException e) {
			// not possible but who knows...
			getLogger().severeLog("", e);
		}
		launchAgent(getClass().getName(), 0, true);
		killAgent(this);
	}

	/**
	 * Proceeds an {@link EnumMessage} so that if it is correctly built, the agent
	 * will trigger its corresponding behavior using the parameters of the message.
	 * 
	 * @param message
	 *            the message to proce
	 * @param <E> the enum message type 
	 * @since MaDKit 5.0.0.14
	 * @see EnumMessage
	 */
	public <E extends Enum<E>> void proceedEnumMessage(EnumMessage<E> message) {
		if (logger != null && logger.isLoggable(Level.FINEST))
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
			logForSender("I have sent a message which has not been understood", message);// TODO i18n
		} catch (IllegalArgumentException e) {
			if (logger != null)
				logger.warning("Cannot proceed message : wrong argument " + m);
			logForSender("I have sent an incorrect command message ", message);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {// TODO dirty : think about that
			Throwable t = e.getCause();
			if (t instanceof SelfKillException) {
				throw (SelfKillException) t;
			}
			t.printStackTrace();
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
			if (method.getName().equals(name2)
					&& checkArgumentTypes(convertToObjectTypes(method.getParameterTypes()), parameters)) {
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

	final private static Map<Class<?>, Class<?>> primitiveTypes = new HashMap<>();
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

	/**
	 * replaced by {@link MadkitClassLoader#getLoader()}
	 * 
	 * @return
	 */
	// @Deprecated
	// public MadkitClassLoader MadkitClassLoader.getLoader() {
	// return MadkitClassLoader.getLoader();
	// }

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
		 * Returned when the agent does not have a role that it is supposed to have
		 * doing a particular action, e.g.
		 * {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)}
		 */
		ROLE_NOT_HANDLED,
		/**
		 * Returned when using
		 * {@link AbstractAgent#createGroup(Group, Object)} and
		 * that a group already exists
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
		 * Returned by launch primitives when the launched agent crashes in activate
		 */
		AGENT_CRASH,
		// NOT_AN_AGENT_CLASS,
		/**
		 * Returned by kill primitives when the targeted agent has not been launched
		 * priorly
		 */
		NOT_YET_LAUNCHED,

		/**
		 * Returned by kill primitives when the targeted agent is already being killed.
		 */
		KILLING_ALREADY_IN_PROGRESS,

		/**
		 * Returned by kill primitives when the targeted agent is already terminated
		 */
		ALREADY_KILLED,

		/**
		 * Returned by send primitives when the targeted agent address does not exist
		 * anymore, i.e. the related agent has leaved the corresponding role
		 */
		INVALID_AGENT_ADDRESS,
		/**
		 * Returned by send primitives when the targeted CGR location does not exist or
		 * contain any agent
		 */
		NO_RECIPIENT_FOUND,
		/**
		 * Returned when
		 * {@link AbstractAgent#requestRole(Group, String, Object)} or
		 * {@link AbstractAgent#createGroup(Group, Object)} is
		 * used in activate and that the agent has been launched using
		 * {@link AbstractAgent#launchAgentBucket(List, int, Role...)} or
		 * {@link AbstractAgent#launchAgentBucket(String, int, int, Role...)}
		 */
		IGNORED,
		/**
		 * Returned when an agent tries to reply to a message which has not been
		 * received from another agent, e.g. newly created or sent directly by an object
		 * using {@link AbstractAgent#receiveMessage(Message)}.
		 */
		CANT_REPLY,
		/**
		 * Returned on special errors. This should not be encountered
		 */
		SEVERE,

		/**
		 * returned when operations does not accept as parameter {@link AbstractGroup}
		 * that represent more than one group.
		 */
		MULTI_GROUP_NOT_ACCEPTED,

		/**
		 * Indicates that the lan transfer has partially failed (some peers has fail and
		 * some other has succeeded).
		 */
		TRANSFERS_PARTIALLY_FAILED,

		/**
		 * Indicates that all the lan transfers to all peers has failed
		 */
		TRANSFERS_FAILED,

		/**
		 * Indicates that the lan transfer has failed
		 */
		TRANSFER_FAILED,

		/**
		 * The transfer is in progress
		 */
		TRANSFER_IN_PROGRESS;

		private TransfersReturnsCodes returns_code = null;
		private int numberOfConcernedAgents = -1;

		void setReturnsCode(TransfersReturnsCodes returns_Code) {
			this.returns_code = returns_Code;
		}

		/**
		 * Gets the set of returns code with there data transfer reports, associated to
		 * each distant (onto network) kernel address.
		 * 
		 * @return the LAN tranfer returns codes/reports, or null if this returns code
		 *         is not assiciated to a LAN transfer.
		 * @see TransfersReturnsCodes
		 */
		public TransfersReturnsCodes getTransfersReturnsCodes() {
			return returns_code;
		}

		// NETWORK_DOWN;

		final static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class.getSimpleName());

		// static ResourceBundle messages =
		// I18nUtilities.getResourceBundle(ReturnCode.class);

		public String toString() {
			return messages.getString(name());
		}

		/**
		 * The number of concerned individual agents. Network broadcast message are not
		 * concerned by this count.
		 * 
		 * @return the number of concerned individual agents.
		 */
		public int getNumberOfConcernedAgents() {
			return numberOfConcernedAgents;
		}

		void setNumberOfConcernedAgents(int nb) {
			numberOfConcernedAgents = nb;
		}
	}

	enum Influence {
		CREATE_GROUP, REQUEST_ROLE, LEAVE_ROLE, LEAVE_GROUP, GET_AGENTS_WITH_ROLE, GET_AGENT_WITH_ROLE, SEND_MESSAGE, BROADCAST_MESSAGE, BROADCAST_MESSAGE_AND_WAIT, LAUNCH_AGENT, KILL_AGENT, GET_AGENT_ADDRESS_IN, RELOAD_CLASS, EXECUTE_TASK;

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
		return getMadkitKernel().isRole(this, LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT);
	}

	// AgentExecutor getAgentExecutor() {
	// return null;
	// }

	/**
	 * This offers a convenient way to create main a main method that launches the
	 * agent class under development. The agent is launched in a new instance
	 * MaDKit. This call only works in the main method of the agent's class. MaDKit.
	 * Here is an example of use that will work in any subclass of
	 * {@link AbstractAgent}:
	 * 
	 * <pre>
	 * <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(args);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Still, the agent must have a default constructor for that to work.
	 * 
	 * @param nbOfInstances
	 *            specify how many of this kind should be launched
	 * 
	 * @param createFrame true if a frame must be created
	 * @param args
	 *            MaDKit options. For example, this will launch the agent in desktop
	 *            mode :
	 * 
	 *            
	 *            <code>
	 * public static void main(String[] args) {
	 * 	executeThisAgent(BooleanOption.desktop.toString());
	 * }
	 * </code>
	 * 
	 * 
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
	 *         {@link Madkit#doAction(com.distrimind.madkit.action.KernelAction, Object...)}
	 * 
	 * 
	 * @since MaDKit 5.0.0.14
	 */
	protected static Madkit executeThisAgent(int nbOfInstances, boolean createFrame, String... args) {
		StackTraceElement element = null;
		for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
			if (stackTraceElement.getMethodName().equals("main")) {
				element = stackTraceElement;
				break;
			}
		}

		final ArrayList<String> arguments = new ArrayList<>(Arrays.asList("--launchAgents",
				"{" + element.getClassName() + "," + createFrame + "," + nbOfInstances + "}"));
		if (args != null) {
			arguments.addAll(Arrays.asList(args));
		}
		return new Madkit(arguments.toArray(new String[arguments.size()]));
	}

	/**
	 * This offers a convenient way to create a main method that launches the agent
	 * class under development. This call only works in the main method of the
	 * agent's class. This call is equivalent to
	 * <code>executeThisAgent(1, true, args)</code>
	 * 
	 * @param args
	 *            MaDKit options
	 * 
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
	 *         {@link Madkit#doAction(com.distrimind.madkit.action.KernelAction, Object...)}
	 * 
	 * @see AbstractAgent#executeThisAgent(int, boolean, String...)
	 * @since MaDKit 5.0.0.14
	 */
	protected static Madkit executeThisAgent(String... args) {
		return executeThisAgent(1, true, args);
	}

	/**
	 * This offers a convenient way to create a main method that launches the agent
	 * class under development. This call only works in the main method of the
	 * agent's class. This call is equivalent to
	 * <code>executeThisAgent(null, 1, true)</code>
	 * 
	 * @return the kernel instance that actually launches this agent, so that it is
	 *         possible to do other actions after the launch using
	 *         {@link Madkit#doAction(com.distrimind.madkit.action.KernelAction, Object...)}
	 * 
	 * @see AbstractAgent#executeThisAgent(int, boolean, String...)
	 * @since MaDKit 5.0.0.15
	 */
	protected static Madkit executeThisAgent() {
		return executeThisAgent(1, true);
	}

	/**
	 * Launch a Task Manager Service Executor whose given name is a unique
	 * identifier. The task executor service aims to execute tasks at defined times.
	 * Tasks can also be repetitive.
	 * 
	 * This function has the same effect than
	 * <code>launchTaskExectutorService(name, 1, MadkitKernel.DEFAULT_THREAD_PRIORITY, -1)</code>.
	 * 
	 * @param name
	 *            the name of the service executor
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#SEVERE}</code>: If a problem occurs.</li>
	 *         </ul>
	 * @see AbstractAgent#launchTaskExectutorService(String, int)
	 * @see AbstractAgent#killTaskExectutorService(String)
	 * @see AbstractAgent#scheduleTask(String, Task, boolean)
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public ReturnCode launchTaskExectutorService(String name) { return
	 * launchTaskExectutorService(name, 1, MadkitKernel.DEFAULT_THREAD_PRIORITY,
	 * -1); }
	 */

	/**
	 * Launch a Task Manager Service Executor whose given name is a unique
	 * identifier. The task executor service aims to execute tasks at defined times.
	 * Tasks can also be repetitive.
	 * 
	 * This function has the same effect than
	 * <code>launchTaskExectutorService(name, 1)</code>.
	 * 
	 * @param name
	 *            the name of the service executor
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are idle,
	 *            unless timeOutSeconds is greater than 0
	 * @param newPriority
	 *            priority to set this thread to
	 * @param timeOutSeconds
	 *            the time to wait in seconds. A time value of zero will cause
	 *            excess threads to terminate immediately after executing tasks.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#SEVERE}</code>: If a problem occurs.</li>
	 *         </ul>
	 * @see AbstractAgent#killTaskExectutorService(String)
	 * @see AbstractAgent#scheduleTask(String, Task, boolean)
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public ReturnCode launchTaskExectutorService(String name, int
	 * minimumPoolSize, int newPriority, long timeOutSeconds) { if
	 * (getMadkitKernel().launchAndOrGetScheduledExecutorService(this, name,
	 * minimumPoolSize, newPriority, timeOutSeconds)!=null) return
	 * ReturnCode.SUCCESS; else return ReturnCode.SEVERE; }
	 */

	/**
	 * Kill the Task Manager Service Executor that corresponds to the given name.
	 * The Task Manager Service Executor aims to execute tasks at defined times.
	 * Tasks can also be repetitive.
	 * 
	 * @param name
	 *            the name of the TaskAgent
	 * @return if the service executor was found and killed.
	 * 
	 * @see AbstractAgent#launchTaskExectutorService(String, int, int, long)
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public boolean killTaskExectutorService(String name) { return
	 * getMadkitKernel().killScheduledExecutorService(this, name)!=null; }
	 */

	/**
	 * Returns true if there is an existing Task Manager Service Executor which
	 * corresponds to the given name
	 * 
	 * @param name
	 *            the TaskAgent name
	 * @return true, if there is an existing Task Manager Service Executor which
	 *         corresponds to the given name. False else.
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public boolean isTaskManagerAgentExisting(String name) { return
	 * getKernel().getScheduledExecutorService(this, name)!=null; }
	 */

	/**
	 * Add a new task to be executed at a specific time by the task agent which
	 * correspond to the given task task executor service name. The task agent aims
	 * to execute tasks at defined times. Tasks can also be repetitive.
	 * 
	 * @param _task_agent_name
	 *            the TaskAgent name
	 * @param _task
	 *            the task to execute
	 * @param ask_for_execution_confirmation
	 *            if set to 'true', means that a confirmation message
	 *            {@link TaskExecutionConfirmationMessage} will be sent to the
	 *            requester for every execution of the given task.
	 * @return a task ID that reference the task, or null if a problem occurs
	 * @see AbstractAgent#launchTaskManagerAgent(String, int)
	 * @see AbstractAgent#cancelTask(String, Task)
	 * @see TaskExecutionConfirmationMessage
	 * @see Task
	 * @see TaskID
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public TaskID scheduleTask(String _task_agent_name, Task<?> _task, boolean
	 * ask_for_execution_confirmation) { return getKernel().scheduleTask(this,
	 * _task_agent_name, _task, ask_for_execution_confirmation); }
	 */

	/**
	 * Add a new task to be executed at a specific time by the task agent which
	 * correspond to the given task executor service name. The task agent aims to
	 * execute tasks at defined times. Tasks can also be repetitive.
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task_agent_name, _task, false);</code>
	 * 
	 * @param _task_agent_name
	 *            the TaskAgent name
	 * @param _task
	 *            the task to execute
	 * @return a task ID that reference the task, or null if a problem occurs
	 * @see AbstractAgent#launchTaskManagerAgent(String, int)
	 * @see AbstractAgent#cancelTask(String, Task)
	 * @see Task
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public TaskID scheduleTask(String _task_agent_name, Task<?> _task) { return
	 * this.scheduleTask(_task_agent_name, _task, false); }
	 */

	/**
	 * Cancel a programmed execution of a task.
	 * 
	 * @param task_id
	 *            a task ID that reference the task
	 * @param mayInterruptIfRunning
	 *            true if the thread executing this task should be interrupted;
	 *            otherwise, in-progress tasks are allowed to complete
	 * 
	 * @return false if the task could not be cancelled, typically because it has
	 *         already completed normally; true otherwise
	 * 
	 * @see AbstractAgent#scheduleTask(Task)
	 * @see AbstractAgent#scheduleTask(Task, boolean)
	 * @see Task
	 * @see TaskID
	 * @since MadKitLanEdition 1.0
	 */
	public boolean cancelTask(TaskID task_id, boolean mayInterruptIfRunning) {
		return getKernel().cancelTask(this, task_id, mayInterruptIfRunning);
	}

	/**
	 * Add a new task to be executed at a specific time by the default task executor
	 * service. The task agent aims to execute tasks at defined times. Tasks can
	 * also be repetitive.
	 * 
	 * @param _task
	 *            the task to execute
	 * @param ask_for_execution_confirmation
	 *            if set to 'true', means that a confirmation message
	 *            {@link TasksExecutionConfirmationMessage} will be sent to the
	 *            requester for every execution of the given task.
	 * @return a task ID that reference the task
	 * @see AbstractAgent#cancelTask(TaskID, boolean)
	 * @see Task
	 * @see TaskID
	 * @see TasksExecutionConfirmationMessage
	 * @since MadKitLanEdition 1.0
	 */
	public TaskID scheduleTask(Task<?> _task, boolean ask_for_execution_confirmation) {
		return getKernel().scheduleTask(this, _task, ask_for_execution_confirmation);
	}

	/**
	 * Add a new task to be executed at a specific time by the default task executor
	 * service. The task agent aims to execute tasks at defined times. Tasks can
	 * also be repetitive.
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task, false);</code>
	 * 
	 * @param _task
	 *            the task to execute
	 * @return a task ID that reference the task
	 * @see AbstractAgent#cancelTask(TaskID, boolean)
	 * @see Task
	 * @see TaskID
	 * @since MadKitLanEdition 1.0
	 */
	public TaskID scheduleTask(Task<?> _task) {
		return this.scheduleTask(_task, false);
	}

	/**
	 * Changes the priority of threads representing the task executor service.
	 * <p>
	 * First the <code>checkAccess</code> method of this thread is called with no
	 * arguments. This may result in throwing a <code>SecurityException</code>.
	 * <p>
	 * Otherwise, the priority of this thread is set to the smaller of the specified
	 * <code>newPriority</code> and the maximum permitted priority of the thread's
	 * thread group.
	 *
	 * @param newPriority
	 *            priority to set this thread to
	 * @exception IllegalArgumentException
	 *                If the priority is not in the range <code>MIN_PRIORITY</code>
	 *                to <code>MAX_PRIORITY</code>.
	 * @exception SecurityException
	 *                if the current thread cannot modify this thread.
	 * 
	 * @see Thread#MAX_PRIORITY
	 * @see Thread#MIN_PRIORITY
	 */
	public void setTaskExecutorServicePriority(int newPriority) {
		getMadkitKernel().setThreadPriotityForServiceExecutor(newPriority);
		// return getKernel().setTaskManagerExecutorPriority(this, _task_agent_name,
		// newPriority);
	}

	/**
	 * Automatically request the given role into the given represented groups, only
	 * for groups that have been requested with other agents. Do nothing else. When
	 * other agents leave roles, those that correspond to the current auto-requested
	 * role are automatically leaved from this agent.
	 * 
	 * @param _group
	 *            the abstract group and these represented groups.
	 * @param _role
	 *            the role to request
	 * @param _passKey
	 *            the <code>passKey</code> to enter a secured group. It is generally
	 *            delivered by the group's <i>group manager</i>. It could be
	 *            <code>null</code>, which is sufficient to enter an unsecured
	 *            group. Especially, {@link #autoRequestRole(AbstractGroup, String)}
	 *            uses a null <code>passKey</code>.
	 * @see AbstractAgent#leaveAutoRequestedGroup(AbstractGroup)
	 * @see AbstractAgent#leaveAutoRequestedRole(String)
	 * @see AbstractAgent#leaveAutoRequestedRole(AbstractGroup, String)
	 * @see AbstractAgent#leaveAllAutoRequestedGroups()
	 */
	public void autoRequestRole(AbstractGroup _group, String _role, Object _passKey) {
		if (_group == null || _role == null)
			return;
		getKernel().autoRequesteRole(this, _group, _role, _passKey);
	}

	/**
	 * Automatically request the given role into the given represented groups, only
	 * for groups that have been requested with other agents. Do nothing else. When
	 * other agents leave roles, those that correspond to the current auto-requested
	 * role are automatically leaved from this agent.
	 * 
	 * @param _group
	 *            the abstract group and these represented groups.
	 * @param _role
	 *            the role to request
	 * @see AbstractAgent#leaveAutoRequestedGroup(AbstractGroup)
	 * @see AbstractAgent#leaveAutoRequestedRole(String)
	 * @see AbstractAgent#leaveAutoRequestedRole(AbstractGroup, String)
	 * @see AbstractAgent#leaveAllAutoRequestedGroups()
	 */
	public void autoRequestRole(AbstractGroup _group, String _role) {
		this.autoRequestRole(_group, _role, null);
	}

	/**
	 * Tells if this agent automically request the given group/role if another
	 * another has requested this group/role.
	 * 
	 * @param _group
	 *            the abstract group and these represented groups.
	 * @param _role
	 *            the role to request
	 * @return true if this agent automically request the given group/role if
	 *         another another has requested this group/role.
	 * @see AbstractAgent#autoRequestRole(AbstractGroup, String, Object)
	 */
	public boolean isConcernedByAutoRequestRole(Group _group, String _role) {
		return getKernel().isConcernedByAutoRequestRole(this, _group, _role);
	}

	/**
	 * Remove role from automatically requested roles. But does not leave the given
	 * role. For this, please refer to {@link #leaveGroup(Group)}.
	 * 
	 * @param role
	 *            the role name
	 * @see AbstractAgent#autoRequestRole(AbstractGroup, String, Object)
	 */
	public void leaveAutoRequestedRole(String role) {
		getKernel().leaveAutoRequestedRole(this, role);
	}

	/**
	 * Remove role from automatically requested roles in the specified group. But
	 * does not leave the given group/role. For this, please refer to
	 * {@link #leaveGroup(Group)}.
	 * @param group the group name
	 * @param role
	 *            the role name
	 * @see #autoRequestRole(AbstractGroup, String, Object)
	 */
	public void leaveAutoRequestedRole(AbstractGroup group, String role) {
		getKernel().leaveAutoRequestedRole(this, group, role);
	}

	/**
	 * Remove the given group from automatically requested groups. But does not
	 * leave the given group. For this, please refer to {@link #leaveGroup(Group)}.
	 * 
	 * @param _group
	 *            the given group
	 * @see #autoRequestRole(AbstractGroup, String, Object)
	 */
	public void leaveAutoRequestedGroup(AbstractGroup _group) {
		getKernel().leaveAutoRequestedGroup(this, _group);
	}

	/**
	 * Remove all groups from automatically requested groups. But does not leave the
	 * groups. For this, please refer to {@link #leaveGroup(Group)}.
	 * 
	 * @see #autoRequestRole(AbstractGroup, String, Object)
	 */
	public void leaveAllAutoRequestedGroups() {
		getKernel().removeAllAutoRequestedGroups(this);
	}

	/**
	 * If a {@link SelfKillException} is generated during a kill process, especially
	 * during an agent function operated by a scheduler, this function can be called
	 * to try to kill the considered agent.
	 * 
	 * @param e
	 *            the given self kill exception
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>
	 *         {@link ReturnCode#ALREADY_KILLED}</code>: If the target has been
	 *         already killed.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}
	 *         </code>: If the target has not been launched.</li>
	 *         <li><code>
	 *         <li><code>{@link ReturnCode#KILLING_ALREADY_IN_PROGRESS}</code>: If
	 *         the target already in a killing process.</li>
	 *         {@link ReturnCode#TIMEOUT}</code>: If the target's end method took
	 *         too much time and has been brutally stopped.</li>
	 *         <li><code>{@link ReturnCode#SEVERE}</code>: If the kill process has
	 *         generated an exception.</li>
	 *         <li><code>
	 *         </ul>
	 * 
	 */
	ReturnCode tryToCompleteKill(SelfKillException e) {
		try {
			return getKernel().killAgent(this, e.agent, e.timeOutSeconds, e.killing_type);
		} catch (Exception e2) {
			return ReturnCode.SEVERE;
		}
	}

	/**
	 * Sends big data to the given agent, using an agent address.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 * @throws IOException if data can't be read
	 */
	public BigDataTransferID sendBigData(AgentAddress agentAddress, RandomInputStream stream) throws IOException {
		return this.sendBigData(agentAddress, stream, null);
	}

	/**
	 * Sends big data to the given agent, using an agent address.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param attachedData
	 *            user custom data that will be sent with the
	 *            {@link BigDataPropositionMessage}.
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 * @throws IOException if data can't be read
	 */
	public BigDataTransferID sendBigData(AgentAddress agentAddress, RandomInputStream stream, Serializable attachedData)
			throws IOException {
		return this.sendBigData(agentAddress, stream, 0, stream.length(), attachedData, null);
	}

	/**
	 * Sends big data to the given agent, using an agent address.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param pos
	 *            the start position in the stream
	 * @param length
	 *            the data length to read into the given stream
	 * @param messageDigestType
	 *            message digest type used to check validity of the transfered data.
	 *            Can be null.
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 * @throws IOException if data can't be read
	 */
	public BigDataTransferID sendBigData(AgentAddress agentAddress, RandomInputStream stream, long pos, long length,
			MessageDigestType messageDigestType) throws IOException {
		return this.sendBigData(agentAddress, stream, pos, length, null, messageDigestType);
	}

	/**
	 * Sends big data to the given agent, using an agent address.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param pos
	 *            the start position in the stream
	 * @param length
	 *            the data length to read into the given stream
	 * @param attachedData
	 *            user custom data that will be sent with the
	 *            {@link BigDataPropositionMessage}.
	 * @param messageDigestType
	 *            message digest type used to check validity of the transfered data.
	 *            Can be null.
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 * @throws IOException if data can't be read
	 */
	public BigDataTransferID sendBigData(AgentAddress agentAddress, RandomInputStream stream, long pos, long length,
			Serializable attachedData, MessageDigestType messageDigestType) throws IOException {
		return getKernel().sendBigData(this, agentAddress, stream, pos, length, attachedData, null, messageDigestType);
	}

	/**
	 * Sends big data to the given agent, using an agent address, specifying
	 * explicitly the role used to send it.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param senderRole
	 *            the sender role
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 * @throws IOException if data can't be read
	 */
	public BigDataTransferID sendBigDataWithRole(AgentAddress agentAddress, RandomInputStream stream, String senderRole)
			throws IOException {
		return this.sendBigDataWithRole(agentAddress, stream, null, senderRole);
	}

	/**
	 * Sends big data to the given agent, using an agent address, specifying
	 * explicitly the role used to send it.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param attachedData
	 *            user custom data that will be sent with the
	 *            {@link BigDataPropositionMessage}.
	 * @param senderRole
	 *            the sender role
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws IOException if data can't be read
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 */
	public BigDataTransferID sendBigDataWithRole(AgentAddress agentAddress, RandomInputStream stream,
			Serializable attachedData, String senderRole) throws IOException {
		return this.sendBigDataWithRole(agentAddress, stream, 0, stream.length(), attachedData, null, senderRole);
	}

	/**
	 * Sends big data to the given agent, using an agent address, specifying
	 * explicitly the role used to send it.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param pos
	 *            the start position in the stream
	 * @param length
	 *            the data length to read into the given stream
	 * @param messageDigestType
	 *            message digest type used to check validity of the transfered data.
	 *            Can be null.
	 * @param senderRole
	 *            the sender role
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws IOException if data can't be read
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 */
	public BigDataTransferID sendBigDataWithRole(AgentAddress agentAddress, RandomInputStream stream, long pos,
			long length, MessageDigestType messageDigestType, String senderRole) throws IOException {
		return this.sendBigDataWithRole(agentAddress, stream, pos, length, null, messageDigestType, senderRole);
	}

	/**
	 * Sends big data to the given agent, using an agent address, specifying
	 * explicitly the role used to send it.
	 * 
	 * The targeted agent will first receive a {@link BigDataPropositionMessage}.
	 * 
	 * By calling the function
	 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
	 * the transfer will be able to begin.
	 * 
	 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
	 * transfer will rejected.
	 * 
	 * A message {@link BigDataResultMessage} is sent in return to the current
	 * agent, asking the transfer, to inform him of the transfer result (see
	 * {@link BigDataResultMessage.Type}). The same message is sent to the targeted
	 * agent.
	 * 
	 * @param agentAddress
	 *            the targeted agent
	 * @param stream
	 *            the stream to send
	 * @param pos
	 *            the start position in the stream
	 * @param length
	 *            the data length to read into the given stream
	 * @param attachedData
	 *            user custom data that will be sent with the
	 *            {@link BigDataPropositionMessage}.
	 * @param messageDigestType
	 *            message digest type used to check validity of the transfered data.
	 *            Can be null.
	 * @param senderRole
	 *            the sender role
	 * 
	 * @return a big data transfer ID that identify the transfer, and that gives
	 *         statistics about the transfer speed in real time. Returns null if the
	 *         <code>agentAddress</code> of the <code>senderRole</code> are invalid.
	 * 
	 * @see BigDataPropositionMessage
	 * @see BigDataResultMessage
	 * @see BigDataTransferID
	 * @throws IOException if data can't be read
	 * @throws NullPointerException
	 *             if <code>agentAddress==null</code> or if
	 *             <code>stream==null</code>
	 * @throws IllegalArgumentException
	 *             if <code>pos</code> or <code>length</code> are invalid
	 */
	public BigDataTransferID sendBigDataWithRole(AgentAddress agentAddress, RandomInputStream stream, long pos,
			long length, Serializable attachedData, MessageDigestType messageDigestType, String senderRole)
			throws IOException {
		return getKernel().sendBigData(this, agentAddress, stream, pos, length, attachedData, senderRole,
				messageDigestType);
	}

	/**
	 * connect or disconnect two kernels directly in a wide area network. It
	 * requires a parameter of type {@link AskForConnectionMessage}.
	 * 
	 * @param message
	 *            the connection properties
	 * @throws IllegalAccessException
	 *             if the network is not enabled (see
	 *             {@link NetworkProperties#network}.
	 * @see AskForConnectionMessage
	 */
	public void manageDirectConnection(AskForConnectionMessage message) throws IllegalAccessException {
		getMadkitKernel().manageDirectConnection(this, message);
	}

	/**
	 * connect or disconnect two kernels indirectly by making data transfered by the
	 * current kernel to constitute a meshed network. It requires a parameter of
	 * type {@link AskForTransferMessage}.
	 * 
	 * @param message
	 *            the connection properties
	 * @throws IllegalAccessException
	 *             if the network is not enabled (see
	 *             {@link NetworkProperties#network}.
	 * @see AskForTransferMessage
	 */
	public void manageTransferConnection(AskForTransferMessage message) throws IllegalAccessException {
		getMadkitKernel().manageTransferConnection(this, message);
	}

	/**
	 * Declare to the kernel an anomaly with the given connection.
	 * 
	 * Case candidateToBan is set to false : the anomaly will be saved into a
	 * database. If too much anomalies occurs
	 * ({@link NetworkProperties#nbMaxAnomaliesBeforeTrigeringExpulsion}), than the
	 * connection is closed and the IP address is expulsed for a while
	 * ({@link NetworkProperties#expulsionDuration}). If too much expulsions occurs
	 * ({@link NetworkProperties#nbMaxExpulsions}), the kernel will consider
	 * anomalies the candidateToBan set to true. Case candidateToBan is set to true
	 * : the anomaly will be also saved into a database. If too much anomalies
	 * occurs ({@link NetworkProperties#nbMaxAnomaliesBeforeTrigeringBanishment}), than the
	 * connection is closed and the IP address banned for a while
	 * ({@link NetworkProperties#banishmentDuration}). If too much bans occurs
	 * ({@link NetworkProperties#nbMaxBanishments}), the kernel will ban the IP
	 * address indefinitely. The difference between an expulsion and a banishment is
	 * defined by the properties see bellow (thresholds and durations)
	 * 
	 * @param candidateToBan
	 *            set it to true if you want the IP address if the connection must
	 *            be banned.
	 * @param connection_identifier
	 *            the connection identifier
	 * @param message
	 *            the message associated with the anomaly (can be null)
	 * @return a return code
	 */
	public ReturnCode anomalyDetectedWithOneConnection(boolean candidateToBan,
			ConnectionIdentifier connection_identifier, String message) {
		return getMadkitKernel().anomalyDetectedWithOneConnection(this, candidateToBan, connection_identifier, message);
	}

	/**
	 * Declare to the kernel an anomaly with the given kernel address.
	 * 
	 * Case candidateToBan is set to false : the anomaly will be saved into a
	 * database. If too much anomalies occurs
	 * ({@link NetworkProperties#nbMaxAnomaliesBeforeTrigeringExpulsion}), than the
	 * connections associated with the kernel address and their IP addresses are
	 * expulsed for a while ({@link NetworkProperties#expulsionDuration}). If too
	 * much expulsions occurs ({@link NetworkProperties#nbMaxExpulsions}), the kernel
	 * will consider anomalies the candidateToBan set to true. Case candidateToBan
	 * is set to true : the anomaly will be also saved into a database. If too much
	 * anomalies occurs ({@link NetworkProperties#nbMaxAnomaliesBeforeTrigeringBanishment}),
	 * than the connections associated with the kernel address and their IP
	 * addresses are banned for a while
	 * ({@link NetworkProperties#banishmentDuration}). If too much bans occurs
	 * ({@link NetworkProperties#nbMaxBanishments}), the kernel will ban the
	 * connections associated with the kernel address and their IP addresses
	 * indefinitely. The difference between an expulsion and a banishment is defined
	 * by the properties see bellow (thresholds and durations)
	 * 
	 * @param candidateToBan
	 *            set it to true if you want the connections associated with the
	 *            kernel address and their IP addresses must be banned.
	 * @param kernelAddress
	 *            the kernel address
	 * @param message
	 *            the message associated with the anomaly (can be null)
	 *            
	 * @return a return code
	 */
	public ReturnCode anomalyDetectedWithOneDistantKernel(boolean candidateToBan, KernelAddress kernelAddress,
			String message) {
		return getMadkitKernel().anomalyDetectedWithOneDistantKernel(this, candidateToBan, kernelAddress, message);
	}

	/**
	 * Gets LAN statistics related to one connection
	 * 
	 * @param connectionIdentifier
	 *            the connection identifier
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	public StatsBandwidth getStatsBandwith(ConnectionIdentifier connectionIdentifier) {
		return getMadkitConfig().networkProperties.getStatsBandwith(connectionIdentifier);
	}

	/**
	 * Gets LAN statistics corresponding to one distant Madkit kernel
	 * 
	 * @param kernel_address
	 *            the Madkit kernel
	 * @return the corresponding statistics or null if no statistics were found.
	 */
	public StatsBandwidth getStatsBandwith(KernelAddress kernel_address) {
		return getMadkitConfig().networkProperties.getStatsBandwith(kernel_address);
	}

	/**
	 * Set a transfer trigger used to filter indirect connections
	 * 
	 * @param transferTriggers
	 *            the transfer filter
	 * @see TransferFilter
	 */
	public void setTransferTriggers(TransferFilter transferTriggers) {
		getMadkitConfig().networkProperties.setTransferTriggers(transferTriggers);
	}

	/**
	 * Gets the transfer trigger used to filter indirect connections
	 * 
	 * @return the transfer filter (null by default)
	 * @see TransferFilter
	 */
	public TransferFilter getTransferTriggers() {
		return getMadkitConfig().networkProperties.getTransferTriggers();
	}

	/**
	 * Returns true if a connection is possible with the given parameters and the
	 * current properties values
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_interface_address
	 *            the local inet address
	 * @param takeConnectionInitiative
	 *            tells if the current peer will take connection initiative
	 * @param thisAskConnection
	 *            the current peer we ask connection
	 * @param mustSupportBidirectionnalConnectionInitiative
	 *            tells if the connection support bi-directional connection
	 *            initiative
	 * @return true if a connection is possible with the given parameters and the
	 *         current properties values
	 * @see NetworkProperties#addAccessData(AccessData)
	 * @see NetworkProperties#addConnectionProtocol(ConnectionProtocolProperties)
	 * @see NetworkProperties#addAccessProtocolProperties(AbstractAccessProtocolProperties)
	 */
	public boolean isConnectionPossible(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean takeConnectionInitiative, boolean thisAskConnection,
			boolean mustSupportBidirectionnalConnectionInitiative) {
		return getMadkitConfig().networkProperties.isConnectionPossible(_distant_inet_address, _local_interface_address,
				takeConnectionInitiative, !thisAskConnection, mustSupportBidirectionnalConnectionInitiative);
	}

	/**
	 * Returns true if a connection is possible with the given parameters and the
	 * current properties values
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_interface_address
	 *            the local inet address
	 * @param thisAskConnection
	 *            the current peer we ask connection
	 * @param takeConnectionInitiative
	 *            tells if the current peer will take connection initiative
	 * @return true if a connection is possible with the given parameters and the
	 *         current properties values
	 * @see NetworkProperties#addAccessData(AccessData)
	 * @see NetworkProperties#addConnectionProtocol(ConnectionProtocolProperties)
	 * @see NetworkProperties#addAccessProtocolProperties(AbstractAccessProtocolProperties)
	 */
	public boolean isConnectionPossible(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean thisAskConnection, boolean takeConnectionInitiative) {
		return this.isConnectionPossible(_distant_inet_address, _local_interface_address, takeConnectionInitiative,
				!thisAskConnection, false);
	}

	/**
	 * Returns true if a connection is possible with the given parameters and the
	 * current properties values
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_interface_address
	 *            the local inet address
	 * @return true if a connection is possible with the given parameters and the
	 *         current properties values
	 * @see NetworkProperties#addAccessData(AccessData)
	 * @see NetworkProperties#addConnectionProtocol(ConnectionProtocolProperties)
	 * @see NetworkProperties#addAccessProtocolProperties(AbstractAccessProtocolProperties)
	 */
	public boolean isConnectionPossible(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address) {
		return this.isConnectionPossible(_distant_inet_address, _local_interface_address, false, true);
	}

	/**
	 * Gets the list of effective LAN connections
	 * 
	 * @return the list of effective LAN connections
	 */
	public Set<Connection> getEffectiveConnections() {
		return getMadkitKernel().getEffectiveConnections(this);
	}

	/**
	 * Gets the list of available distant Madkit kernels
	 * 
	 * @return the list of available distant Madkit kernels
	 */
	public Set<KernelAddress> getAvailableDistantKernels() {
		return getMadkitKernel().getAvailableDistantKernels(this);
	}

	/**
	 * Gets the list of accessible groups given by a distant Madkit kernel
	 * 
	 * @param kernelAddress
	 *            the distant Madkit kernel
	 * @return the list of accessible groups given by a distant Madkit kernel
	 */
	public List<Group> getAccessibleGroupsGivenByDistantPeer(KernelAddress kernelAddress) {
		return getMadkitKernel().getAccessibleGroupsGivenByDistantPeer(this, kernelAddress);
	}

	/**
	 * Gets the list of accessible groups given to a distant Madkit kernel
	 * 
	 * @param kernelAddress
	 *            the distant Madkit kernel
	 * @return the list of accessible groups given to a distant Madkit kernel
	 */
	public List<Group> getAccessibleGroupsGivenToDistantPeer(KernelAddress kernelAddress) {
		return getMadkitKernel().getAccessibleGroupsGivenToDistantPeer(this, kernelAddress);
	}

	/**
	 * Gets the identifiers logged with a distant Madkit kernel
	 * 
	 * @param kernelAddress
	 *            the distant Madkit kernel
	 * @return the identifiers logged with a distant Madkit kernel
	 */
	public List<PairOfIdentifiers> getEffectiveDistantLogins(KernelAddress kernelAddress) {
		return getMadkitKernel().getEffectiveDistantLogins(this, kernelAddress);
	}

	/**
	 * Store a reference of the given blackboard into a given group. If a blackboard
	 * with the same name has already be set, do not memorize the given blackboard
	 * as argument. All agents into this group will be able to access to the same
	 * blackboard.
	 * 
	 * @param group
	 *            the group
	 * @param blackboardName
	 *            the black board name
	 * @param blackboard
	 *            the blackboard.
	 * @return If a black existed previously, return the previous blackboard. Else
	 *         return the saved blackboard. Return null if this agent is not present
	 *         into the given group.
	 */
	public Object weakSetBlackboard(Group group, String blackboardName, Object blackboard) {
		return getMadkitKernel().weakSetBlackboard(this, group, blackboardName, blackboard);
	}

	/**
	 * Store a reference of the given blackboard into a given group. If a blackboard
	 * with the same name has already be set, replace it. All agents into this group
	 * will be able to access to the same blackboard.
	 * 
	 * @param group
	 *            the group
	 * @param blackboardName
	 *            the black board name
	 * @param blackboard
	 *            the blackboard.
	 * @return return the previous set blackboard. Return null if this agent is not
	 *         present into the given group.
	 */
	public Object setBlackboard(Group group, String blackboardName, Object blackboard) {
		return getMadkitKernel().setBlackboard(this, group, blackboardName, blackboard);
	}

	/**
	 * Get the referenced blackboard into a given group, and with a given name.
	 * 
	 * @param group
	 *            the group
	 * @param blackboardName
	 *            the black board name
	 * @return the referenced blackboard, or null if no blackboard exists. Return
	 *         null if this agent is not present into the given group.
	 */
	public Object getBlackboard(Group group, String blackboardName) {
		return getMadkitKernel().getBlackboard(this, group, blackboardName);
	}

	/**
	 * Remove the referenced blackboard into a given group, and with a given name.
	 * 
	 * @param group
	 *            the group
	 * @param blackboardName
	 *            the black board name
	 * @return the previous referenced blackboard, or null if no blackboard exists.
	 *         Return null if this agent is not present into the given group.
	 */
	public Object removeBlackboard(Group group, String blackboardName) {
		return getMadkitKernel().removeBlackboard(this, group, blackboardName);
	}

}