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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.distrimind.madkit.exceptions.KilledException;
import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.message.ConversationFilter;
import com.distrimind.madkit.message.MessageFilter;
import com.distrimind.madkit.message.task.TasksExecutionConfirmationMessage;

/**
 * The super class of all MaDKit threaded agents, v 5. It provides support for
 * <ul>
 * <li>Agent's Life cycle, logging, and naming.
 * <li>Agent launching and killing.
 * <li>Artificial society creation and management.
 * <li>Messaging.
 * <li>Minimal graphical interface management.
 * </ul>
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 5.1.0
 */
public class Agent extends AbstractAgent {

	Thread myThread;

	final private AgentExecutor agentExecutor;
	final private boolean isDaemon;

	public Agent(boolean isDaemon) {
		this.isDaemon = isDaemon;
		agentExecutor = new AgentExecutor(this);
	}

	public Agent() {
		this(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message receiveMessage(final Message m) {
		if (!isWaitForMessagePurge()) {
			return super.receiveMessage(m);
		} else
			return null;
	}

	/**
	 * Changes the priority of the agent's thread. This should be used only starting
	 * from the {@link Agent#activate()} to have a concrete effect. Default priority
	 * is set to {@link Thread#NORM_PRIORITY} - 1 to ensure swing responsiveness.
	 * 
	 * @param newPriority
	 *            priority to set this thread to
	 * @exception IllegalArgumentException
	 *                If the priority is not in the range
	 *                <code>Thread.MIN_PRIORITY</code> to
	 *                <code>Thread.MAX_PRIORITY</code>.
	 * @exception SecurityException
	 *                if the current thread cannot modify this thread.
	 * @see Thread
	 * @since MadKit 5.0.1
	 */
	// do not give any access to the thread because its name is normalized
	public void setThreadPriority(int newPriority) {
		if (myThread != null) {
			myThread.setPriority(newPriority);
		}
	}

	/**
	 * Returns this thread's priority.
	 *
	 * @return this thread's priority for this agent.
	 * @see Thread
	 * @since MadKit 5.0.1
	 */
	public int getThreadPriority() {
		if (myThread != null) {
			return myThread.getPriority();
		}
		return Thread.NORM_PRIORITY;
	}

	/**
	 * pretty dirty solution for fake and terminated kernel
	 * 
	 * @param o
	 */
	Agent(Object o) {
		super(o);
		isDaemon = false;
		agentExecutor = null;
	}

	/**
	 * Tells if the agent is a daemon.
	 * 
	 * @return <code>true</code> if the agent is a Daemon
	 * @since MaDKit 5.0.0.9
	 */
	public boolean isDaemon() {
		return isDaemon;
	}

	/**
	 * @return the agentExecutor
	 */
	final AgentExecutor getAgentExecutor() {
		return agentExecutor;
	}

	/**
	 * @param e
	 */
	final void suicide(SelfKillException e) {
		getAgentExecutor().getLiveProcess().cancel(false);
		/*
		 * getAgentExecutor().getLiveProcess().cancel(false);
		 * getAgentExecutor().getEndProcess().cancel(false); super.suicide(e);
		 * terminate();
		 */
	}

	boolean isWaitForMessagePurge() {
		State s = state.get();
		return s == State.LIVING_BUG_WAIT_FOR_KILL
				|| (s == State.ZOMBIE && s.getPreviousState() == State.LIVING_BUG_WAIT_FOR_KILL);
	}

	final boolean living() {
		try {
			state.set(State.LIVING);
			setMyThread(Thread.currentThread());
			logMethod(true);
			try {
				try {
					preLiveCycle();
					while (isAlive() || isWaitForMessagePurge()) {
						try {
							liveCycle();
						} catch (InterruptedException e) {
							if (!isWaitForMessagePurge())
								throw e;
						}
						synchronized (state) {
							if (isWaitForMessagePurge()) {
								if (messageBox.isEmpty()) {
									state.notifyAll();
									break;
								}
							}
						}
					}
				} catch (InterruptedException e) {
				}

				/*
				 * finally { synchronized(state) { state.notify(); } }
				 */

			} catch (SelfKillException e) {
				suicide(e);
			} catch (Throwable e) {
				synchronized (state) {
					logLifeException(e);
					// alive.set(false);
				}
			}
			/*
			 * if(! alive.get()){ try { Thread.sleep(1); } catch (InterruptedException e) {
			 * }//answer the kill }
			 */
		} catch (KilledException e) {
			logLifeException(e);
		}
		logMethod(false);
		return true;
	}

	/**
	 * This method is executed one time just before {@link #liveCycle()}
	 * @throws InterruptedException if the current thread is interrupted
	 */
	protected void preLiveCycle() throws InterruptedException {

	}

	/**
	 * This method corresponds to the second behavior which is called by the MaDKit
	 * kernel when a threaded agent is launched (i.e. an agent which subclasses this
	 * class). Usually, this is the place where all the agent's work lies. Most of
	 * the time, this work is about waiting for a message and thus treat it as it
	 * should be. When the agent want to die, it must call the function
	 * {@link #killAgent(AbstractAgent, int, KillingType)}.
	 * <p>
	 * Here is a typical example:
	 * </p>
	 * 
	 * <pre>
	 *  
	 *	protected void liveCycle()
	 *	{
	 *		Message m = waitNextMessage();
	 *		if (m!=null)
	 *			handleMessage(m); //a private method that does the appropriate job 
	 *  	}
	 *	}
	 * </pre>
	 * @throws InterruptedException if the current thread is interrupted
	 */
	protected void liveCycle() throws InterruptedException {
		setLogLevel(Level.INFO);
		logger.talk(
				"\n\tHi Human and hello World !!\n\n I am an instance of the madkit.kernel.Agent class\n As such, I am a MaDKit threaded Agent\n and thus have an autonomous activity!");
		pause(5000);
		logger.talk("\n\n And in fact, I am the simplest agent ever\n because I simply do nothing at all :)\n\n");
		pause(4000);
		int i = (int) (Math.random() * 3000 + 4500);
		logger.info("I will quit in " + i + " milliseconds... ");
		pause(i);
		logger.info("Bye !");
		this.killAgent(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReturnCode killAgent(AbstractAgent target, int timeOutSeconds, KillingType killing_type) {

		// if this is a self kill done by the agent itself, not an object which has
		// access to the agent
		if (target == this && (myThread == Thread.currentThread()
				|| Thread.currentThread().getName().equals(getAgentThreadName(getState())))) {
			// if (alive.compareAndSet(true, false))
			if (alive.get()) {
				if (isFinestLogOn())
					logger.log(Level.FINEST,
							Influence.KILL_AGENT + " (" + timeOutSeconds + ")" + target.getName() + "...");
				throw new SelfKillException("" + timeOutSeconds, this, timeOutSeconds, KillingType.JUST_KILL_IT);
			}
		}
		return super.killAgent(target, timeOutSeconds, killing_type);
	}

	/**
	 * Sends a message and waits indefinitely for an answer to it. This has the same
	 * effect as
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, null)</code>
	 * 
	 * @param receiver
	 *            the targeted agent by the send.
	 * @param messageToSend
	 *            the message to send.
	 * @return the reply received as soon as available, or <code>null</code> if
	 *         there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String,
	 *      Integer)
	 * @since MaDKit 5
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend)
			throws InterruptedException { // NO_UCD
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, null);
	}

	/**
	 * Sends a message and waits for an answer to it. This has the same effect as
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, timeOutMilliSeconds)</code>
	 * 
	 * @param receiver
	 *            the targeted agent by the send.
	 * @param messageToSend
	 *            the message to send.
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String,
	 *      Integer)
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MaDKit 5
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend,
			final int timeOutMilliSeconds) throws InterruptedException {
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, Integer.valueOf(timeOutMilliSeconds));
	}

	/**
	 * Sends a message and waits for an answer to it. This has the same effect as
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, senderRole, null)</code>
	 * 
	 * @param receiver
	 *            the targeted agent by the send.
	 * @param messageToSend
	 *            the message to send.
	 * @param senderRole
	 *            the role with which the sending is done.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String,
	 *      Integer)
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MaDKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend,
			String senderRole) throws InterruptedException {
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, senderRole, null);
	}

	/**
	 * Sends a message and waits for an answer to it. Additionally, the sending is
	 * done using a specific role for the sender.
	 * 
	 * @param receiver
	 *            the targeted agent by the send.
	 * @param messageToSend
	 *            the message to send.
	 * @param senderRole
	 *            the role with which the sending is done.
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message, that is any {@link AbstractAgent.ReturnCode} different from
	 *         {@link AbstractAgent.ReturnCode#SUCCESS} (see
	 *         {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)}).
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendMessageWithRole(AgentAddress, Message, String)
	 * @see AbstractAgent.ReturnCode
	 * @since MaDKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend,
			String senderRole, Integer timeOutMilliSeconds) throws InterruptedException {
		// no need to checkAliveness : this is done in noLogSendingMessage
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("sendMessageAndWaitForReply : sending " + messageToSend + " to " + receiver
					+ ", and waiting reply...");
		ReturnCode rc = getKernel().sendMessage(this, receiver, messageToSend, senderRole);
		if (rc != SUCCESS && rc != ReturnCode.TRANSFER_IN_PROGRESS) {
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
	}

	/**
	 * Sends a message to an agent having this position in the organization and
	 * waits indefinitely for an answer to it. This has the same effect as
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, null)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if
	 *         there was an error when sending the message.
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendMessageWithRoleAndWaitForReply(AbstractGroup, String, Message, String, Integer)
	 * @since MaDKitLanEdition 1.0
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 */
	public Message sendMessageAndWaitForReply(AbstractGroup group, final String role, Message messageToSend)
			throws InterruptedException {
		return sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization and
	 * waits indefinitely for an answer to it. This has the same effect as
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, senderRole, null)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send.
	 * @param senderRole
	 *            the role with which the sending is done. If <code>null</code> the
	 *            agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if
	 *         there was an error when sending the message.
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendMessageWithRoleAndWaitForReply(AbstractGroup, String, Message, String, Integer)
	 * @since MaDKitLanEdition 1.0
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 */
	public Message sendMessageWithRoleAndWaitForReply(AbstractGroup group, final String role, Message messageToSend,
			final String senderRole) throws InterruptedException {
		return sendMessageWithRoleAndWaitForReply(group, role, messageToSend, senderRole, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization and
	 * waits for an answer to it. This has the same effect as
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, timeOutMilliSeconds)</code>
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send.
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @see #sendMessageWithRoleAndWaitForReply(AbstractGroup, String, Message, String, Integer)
	 * @since MaDKitLanEdition 1.0
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message sendMessageAndWaitForReply(AbstractGroup group, final String role, Message messageToSend,
			final int timeOutMilliSeconds) throws InterruptedException {
		return sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, Integer.valueOf(timeOutMilliSeconds));
	}

	/**
	 * Sends a message to an agent having this position in the organization and
	 * waits for an answer to it. The targeted agent is selected randomly among
	 * matched agents. The sender is excluded from this search.
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role
	 *            the role name
	 * @param messageToSend
	 *            the message to send.
	 * @param senderRole
	 *            the role with which the sending is done.
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the
	 *         message.
	 * @since MaDKitLanEdition 1.0
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message sendMessageWithRoleAndWaitForReply(AbstractGroup group, final String role, Message messageToSend,
			final String senderRole, final Integer timeOutMilliSeconds) throws InterruptedException {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("sendMessageAndWaitForReply : sending " + messageToSend + " to any "
					+ I18nUtilities.getCGRString(group, role)
					+ (timeOutMilliSeconds == null ? ""
							: ", and waiting reply for "
									+ TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds.intValue()) + " s..."));
		ReturnCode rc = getKernel().sendMessage(this, group, role, messageToSend, senderRole);
		if (rc != SUCCESS && rc != ReturnCode.TRANSFER_IN_PROGRESS) {
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
	}

	/**
	 * Sends a reply message and waits indefinitely for an answer to it. This has
	 * the same effect as
	 * <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, null)</code>.
	 * 
	 * @param messageToReplyTo
	 *            the original message previously received.
	 * @param reply
	 *            the new message.
	 * @return the reply received as soon as available.
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply)
			throws InterruptedException {
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, null);
	}

	/**
	 * Sends a reply message and waits for an answer to it. This has the same effect
	 * as
	 * <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, timeOutMilliSeconds)</code>.
	 * 
	 * @param messageToReplyTo
	 *            the original message previously received
	 * @param reply
	 *            the new message
	 * @param timeOutMilliSeconds the delay before this function is unlocked when no reply was received
	 * @return the reply received as soon as available
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply,
			int timeOutMilliSeconds) throws InterruptedException {
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, Integer.valueOf(timeOutMilliSeconds));
	}

	/**
	 * Sends a reply message and waits indefinitely for an answer to it. This has
	 * the same effect as
	 * <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, senderRole, null)</code>.
	 * 
	 * @param messageToReplyTo
	 *            the original message previously received
	 * @param reply
	 *            the new message
	 * @param senderRole the sender role
	 * @return the reply received as soon as available
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply,
			String senderRole) throws InterruptedException {
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, senderRole, null);
	}

	/**
	 * Sends a reply message and waits for an answer to it. Additionally, the reply
	 * is done using a specific role for the sender.
	 * 
	 * @param messageToReplyTo
	 *            the original message previously received
	 * @param reply
	 *            the reply message.
	 * @param senderRole
	 *            the role with which the reply is sent.
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait. If <code>null</code> the agent will wait
	 *            indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the
	 *         time out has elapsed or if there was an error when sending the reply,
	 *         that is any {@link AbstractAgent.ReturnCode} different from
	 *         {@link AbstractAgent.ReturnCode#SUCCESS} (see
	 *         {@link AbstractAgent#sendReplyWithRole(Message, Message, String)}).
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #sendReplyWithRole(Message, Message, String)
	 * @see AbstractAgent.ReturnCode
	 * @since MaDKit 5
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply,
			String senderRole, Integer timeOutMilliSeconds) throws InterruptedException {
		ReturnCode rc = sendReplyWithRole(messageToReplyTo, reply, senderRole);
		if (rc != SUCCESS && rc != ReturnCode.TRANSFER_IN_PROGRESS) {
			return null;
		}
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("sendReplyAndWaitForReply : sending " + reply + " as reply to " + messageToReplyTo
					+ ", and waiting reply...");
		return waitAnswer(reply, timeOutMilliSeconds);
	}

	/**
	 * Broadcasts a message and wait for answers considering a timeout duration.
	 * 
	 * @param group
	 *            the group(s) and the community(ies) name
	 * @param role the role
	 * @param message the message
	 * @param senderRole the sender role
	 * @param timeOutMilliSeconds delay before unlocking the function
	 * @return a list of messages which are answers to the <code>message</code>
	 *         which has been broadcasted.
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MaDKitLanEdition 1.0
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 */
	public Replies broadcastMessageWithRoleAndWaitForReplies(AbstractGroup group, final String role, Message message,
			final String senderRole, final Integer timeOutMilliSeconds) throws InterruptedException {
		return new Replies(message, getKernel().broadcastMessageWithRoleAndWaitForReplies(this, group, role, message,
				senderRole, timeOutMilliSeconds));
	}

	/**
	 * This method is the blocking version of nextMessage(). If there is no message
	 * in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNextMessage(long)
	 * @return the first received message
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message waitNextMessage() throws InterruptedException {
		if (logger != null) {
			if (logger.isLoggable(Level.FINEST))
				logger.finest("waitNextMessage...");
			final Message m = waitingNextMessageForEver();
			if (logger.isLoggable(Level.FINEST))
				logger.finest("..." + Words.NEW_MSG + ": " + m);
			return m;
		}
		return waitingNextMessageForEver();
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * message considering a certain delay.
	 * 
	 * @param timeOutMilliseconds
	 *            the maximum time to wait, in milliseconds.
	 * 
	 * @return the first message in the mailbox, or <code>null</code> if no message
	 *         has been received before the time out delay is elapsed
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message waitNextMessage(final long timeOutMilliseconds) throws InterruptedException {
		AgentLogger logger = this.logger;
		if (logger != null) {
			if (logger.isLoggable(Level.FINEST))
				logger.finest("Waiting next message during " + timeOutMilliseconds + " milliseconds...");
			final Message m = waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
			if (logger.isLoggable(Level.FINEST)) {
				if (m != null)
					logger.finest("waitNextMessage->" + Words.NEW_MSG + ": " + m);
				else
					logger.finest("waitNextMessage time out !");
			}
			return m;

		}
		return waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * Retrieves and removes the next message that complies with the filter, waiting
	 * for ever if necessary until a matching message becomes available.
	 * 
	 * @param filter the filter
	 * 
	 * @return the first received message that matches the filter
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message waitNextMessage(final MessageFilter filter) throws InterruptedException {
		final List<Message> receptions = new ArrayList<>();
		Message m = waitingNextMessageForEver();
		while (!filter.accept(m)) {
			receptions.add(m);
			m = waitingNextMessageForEver();
		}
		addAllToMessageBox(receptions);
		// if (!receptions.isEmpty()) {
		// synchronized (messageBox) {
		// messageBox.addAll(receptions);
		// }
		// }
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("a match has arrived " + m);
		return m;
	}

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * acceptable message up to a certain delay.
	 * 
	 * @param timeOutMilliseconds
	 *            the maximum time to wait, in milliseconds.
	 * @param filter the filter
	 * 
	 * @return a message that matches or <code>null</code> otherwise.
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public Message waitNextMessage(final Integer timeOutMilliseconds, final MessageFilter filter)
			throws InterruptedException {
		if (timeOutMilliseconds == null) {
			return waitNextMessage(filter);
		}
		// conversion
		final long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(timeOutMilliseconds.intValue());
		final List<Message> receptions = new ArrayList<>();
		final long endTime = System.nanoTime() + timeOutNanos;
		Message answer = waitingNextMessage(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && !filter.accept(answer)) {
			receptions.add(answer);
			answer = waitingNextMessage(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		addAllToMessageBox(receptions);
		// if (!receptions.isEmpty()) {
		// synchronized (messageBox) {
		// messageBox.addAll(receptions);
		// }
		// }
		if (logger != null && logger.isLoggable(Level.FINEST)) {
			logger.finest(answer == null ? "...Waiting time out, no compliant message received"
					: "...a match has arrived : " + answer);
		}
		return answer;
	}

	
	private Message waitingNextMessageForEver() throws InterruptedException {
		checkInterruptedExceptionForMessageWaiting();
		return messageBox.take().markMessageAsRead();
	}

	@Override
	void checkInterruptedExceptionForMessageWaiting() throws InterruptedException {
		if (isWaitForMessagePurge() && messageBox.isEmpty())
			throw new InterruptedException();

	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query
	 *            the message for which a reply is waited for
	 * 
	 * @return the first reply to the query message
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final Message query) throws InterruptedException {
		return waitNextMessage(new ConversationFilter(query));
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message
	 * referenced by a {@link ConversationID}, waiting for ever if necessary until a
	 * matching reply becomes available.
	 * 
	 * @param id
	 *            the conversation ID for which a reply is waited for
	 * 
	 * @return the first reply to the query message
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final ConversationID id) throws InterruptedException {
		return waitNextMessage(new ConversationFilter(id));
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query
	 *            the message for which a reply is waited for
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait, in milliseconds.
	 * 
	 * @return the first reply to the query message
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final Message query, final Integer timeOutMilliSeconds) throws InterruptedException {
		return waitNextMessage(timeOutMilliSeconds, new ConversationFilter(query));
	}

	/**
	 * Retrieves and removes the next message that is a reply to the query message
	 * referenced by a {@link ConversationID}, waiting for ever if necessary until a
	 * matching reply becomes available.
	 * 
	 * @param id
	 *            the conversation ID for which a reply is waited for
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait, in milliseconds.
	 * 
	 * @return the first reply to the query message
	 * @throws InterruptedException if the current thread is interrupted
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final ConversationID id, final Integer timeOutMilliSeconds) throws InterruptedException {
		return waitNextMessage(timeOutMilliSeconds, new ConversationFilter(id));
	}

	/**
	 * Add a new task to be executed at a specific time by the task agent which
	 * correspond to the given task agent name. The function ends when the task is
	 * finished. The task agent aims to execute tasks at defined times. Tasks can
	 * also be repetitive. The task agent must be initialized through the function
	 * launchTaskManagerAgent(String, int)
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task_agent_name, _task, false);</code>
	 * 
	 * @param _task_agent_name
	 *            the TaskAgent name
	 * @param _task
	 *            the task to execute
	 * @return the confirmation message or null if a problem occurs
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #launchTaskManagerAgent(String, int)
	 * 
	 * @see Task
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public TasksExecutionConfirmationMessage
	 * scheduleTaskAndWaitForConfirmation(String _task_agent_name, Task<?> _task)
	 * throws InterruptedException { TaskID cid=scheduleTask(_task_agent_name,
	 * _task, true); if (cid==null) return null; Message m = waitAnswer(cid); if
	 * (m==null) return null; if (m instanceof TasksExecutionConfirmationMessage)
	 * return (TasksExecutionConfirmationMessage)m; else
	 * getMadkitKernel().handleException(Influence.EXECUTE_TASK, new
	 * IllegalAccessError("the message "+m+" is unexpected.")); return null; }
	 */

	/**
	 * Add a new task to be executed at a specific time by the default task agent.
	 * The function ends when the task is finished. The task agent aims to execute
	 * tasks at defined times. Tasks can also be repetitive. The default task agent
	 * is automatically initialized with one thread.
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task, false);</code>
	 * 
	 * @param _task
	 *            the task to execute
	 * @return the confirmation message or null if a problem occurs
	 * @throws InterruptedException if the current thread is interrupted
	 * @see Task
	 * @since MadKitLanEdition 1.0
	 */
	public TasksExecutionConfirmationMessage scheduleTaskAndWaitForConfirmation(Task<?> _task)
			throws InterruptedException {
		ConversationID cid = scheduleTask(_task, true);
		if (cid == null)
			return null;
		Message m = waitAnswer(cid);
		if (m == null)
			return null;
		if (m instanceof TasksExecutionConfirmationMessage)
			return (TasksExecutionConfirmationMessage) m;
		else
			getMadkitKernel().handleException(Influence.EXECUTE_TASK,
					new IllegalAccessError("the message " + m + " is unexpected."));
		return null;

	}

	/**
	 * Add a new task to be executed at a specific time by the task agent which
	 * correspond to the given task agent name. The function ends when the task is
	 * finished. The task agent aims to execute tasks at defined times. Tasks can
	 * also be repetitive. The task agent must be initialized through the function
	 * launchTaskManagerAgent(String, int)
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task_agent_name, _task, false);</code>
	 * 
	 * @param _task_agent_name
	 *            the TaskAgent name
	 * @param _task
	 *            the task to execute
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait, in milliseconds.
	 * @return the confirmation message or null if a problem occurs
	 * @throws InterruptedException if the current thread is interrupted
	 * @see #launchTaskManagerAgent(String, int)
	 * 
	 * @see Task
	 * @since MadKitLanEdition 1.0
	 */
	/*
	 * public TasksExecutionConfirmationMessage
	 * scheduleTaskAndWaitForConfirmation(String _task_agent_name, Task<?> _task,
	 * final Integer timeOutMilliSeconds) throws InterruptedException { TaskID
	 * cid=scheduleTask(_task_agent_name, _task, true); if (cid==null) return null;
	 * Message m = waitAnswer(cid, timeOutMilliSeconds); if (m==null) return null;
	 * if (m instanceof TasksExecutionConfirmationMessage) return
	 * (TasksExecutionConfirmationMessage)m; else
	 * getMadkitKernel().handleException(Influence.EXECUTE_TASK, new
	 * IllegalAccessError("the message "+m+" is unexpected.")); return null; }
	 */

	/**
	 * Add a new task to be executed at a specific time by the default task agent.
	 * The function ends when the task is finished. The task agent aims to execute
	 * tasks at defined times. Tasks can also be repetitive. The default task agent
	 * is automatically initialized with one thread.
	 * 
	 * this function is equivalent than
	 * <code>this.scheduleTask(_task, false);</code>
	 * 
	 * @param _task
	 *            the task to execute
	 * @param timeOutMilliSeconds
	 *            the maximum time to wait, in milliseconds.
	 * @return the confirmation message or null if a problem occurs
	 * @throws InterruptedException if the current thread is interrupted
	 * @see Task
	 * @since MadKitLanEdition 1.0
	 */
	public TasksExecutionConfirmationMessage scheduleTaskAndWaitForConfirmation(Task<?> _task,
			final Integer timeOutMilliSeconds) throws InterruptedException {
		ConversationID cid = scheduleTask(_task, true);
		if (cid == null)
			return null;
		Message m = waitAnswer(cid, timeOutMilliSeconds);
		if (m == null)
			return null;
		if (m instanceof TasksExecutionConfirmationMessage)
			return (TasksExecutionConfirmationMessage) m;
		else
			getMadkitKernel().handleException(Influence.EXECUTE_TASK,
					new IllegalAccessError("the message " + m + " is unexpected."));
		return null;

	}

}
