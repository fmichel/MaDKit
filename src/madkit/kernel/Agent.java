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
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.LIVING;
import static madkit.kernel.Madkit.Roles.GUI_MANAGER_ROLE;
import static madkit.kernel.Madkit.Roles.LOCAL_COMMUNITY;
import static madkit.kernel.Madkit.Roles.SYSTEM_GROUP;
import static madkit.kernel.Utils.printCGR;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import madkit.gui.actions.MadkitActions;
import madkit.gui.messages.GUIMessage;

/**
 * The super class of all MadKit threaded agents, v 5. 
 * It provides support for 
 * <ul>
 * <li> Agent's Life cycle, logging, and naming.
 * <li> Agent launching and killing.
 * <li> Artificial society creation and management.
 * <li> Messaging.
 * <li> Minimal graphical interface management.
 * </ul>

 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @since MadKit 1.0
 * @version 5.1
 */
public class Agent extends AbstractAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8564494100061187968L;
	Thread myThread;
	
	final private AgentExecutor agentExecutor;
	final private boolean isDaemon;

	//	/**
	//	 * @return the myThread
	//	 */
	//	final Thread getMyThread() {
	//		return myThread;
	//	}

	/**
	 * 
	 */
	public Agent(boolean isDaemon) {
		this.isDaemon= isDaemon;
		agentExecutor = new AgentExecutor(this);
	}

	public Agent(){
		this(false);
	}
	
	/**
	 * Tells if the agent is a daemon.
	 * 
	 * @return <code>true</code> if the agent is a Daemon
	 * @since MadKit 5.0.0.9
	 */
	public boolean isDaemon() {
		return isDaemon;
	}

//	/**
//	 * @param agentExecutor the agentExecutor to set
//	 */
//	void setAgentExecutor(AgentExecutor ae) {
//		this.agentExecutor = ae;
//	}

	/**
	 * @return the agentExecutor
	 */
	@Override
	AgentExecutor getAgentExecutor() {
		return agentExecutor;
	}

	@Override
		boolean activation() {
			if(hasGUI){
				if(logger != null){
					logger.finer("** setting up GUI **");
				}
				requestRole(LOCAL_COMMUNITY, SYSTEM_GROUP, "default");
				kernel.broadcastMessageWithRoleAndWaitForReplies(
						this,
						LOCAL_COMMUNITY, 
						SYSTEM_GROUP, 
						GUI_MANAGER_ROLE, 
						new GUIMessage(MadkitActions.AGENT_SETUP_GUI,this), 
						null, 
						10000);//How much and why ?
			}
			if(logger != null){
				logger.finer("** entering ACTIVATE **");
			}
			if(! state.compareAndSet(INITIALIZING, ACTIVATED))
				throw new AssertionError("not init in activation");
			try {
				activate();
			} catch (KilledException e) {
				if(logger != null){
					logger.warning("-*-GET KILLED in ACTIVATE-*- : "+e.getMessage());
				}
				return false;
			} catch (Throwable e) {
				logLifeException(e);
				return false;
			} finally {
				if(logger != null)
					logger.finer("** exiting ACTIVATE **");
			}
			return true;
		}

	final boolean living() {
		if(! state.compareAndSet(ACTIVATED, LIVING))
			throw new AssertionError("not activated in live");//TODO remove test
		if(logger != null){
			logger.finer("** entering LIVE **");
		}
		try {
			live();
		} catch (KilledException e) {
			if(logger != null){
				logger.finer("-*-GET KILLED in LIVE-*- : "+e.getMessage());
			}
			return false;
		} catch (Throwable e) {
			logLifeException(e);
			return false;
		} finally {
			//			getRunState().set(ENDING);
			if(logger != null)
				logger.finer("** exiting LIVE **");
		}
		return true;
	}

	/**
	 * This method corresponds to the second behavior which is called by the MadKit kernel 
	 * when a threaded agent is launched (i.e. an agent which subclasses this class).
	 * Usually, this is the place where all the agent's work lies.
	 * Most of the time, this work is about waiting for a message and thus treat it
	 * as it should be.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * <pre>
	 *  <tt>      @Override</tt>
	 *	protected void live()
	 *	{
	 *		while(true){
	 *			Message m = waitNextMessage();
	 *			handleMessage(m); //a private method that does the appropriate job 
	 *  	}
	 *	}
	 * </pre>
	 */
	protected void live() {
		setLogLevel(Level.INFO);
		logger.talk("\tHello World !\n\n\tI am the simpliest agent ever\n\tbecause I simply do nothing at all :)\n\n");
		pause(2000);
		int i = (int) (Math.random()*2000+2500);
		logger.info("I will quit in "+i+" milliseconds... Bye !");
		pause(i);
	}

	/** Kills a targeted agent
	 * @see madkit.kernel.AbstractAgent#killAgent(madkit.kernel.AbstractAgent, int)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	@Override
	public ReturnCode killAgent(AbstractAgent target, int timeOutSeconds) {//TODO fins something else
		if(target == this){
			if(myThread != Thread.currentThread()){
				return kernel.getMadkitKernel().killAgent(this, target, timeOutSeconds);
			}
			else{
				throw new KilledException("by ["+getName()+"]");
			}
		}
		return super.killAgent(target, timeOutSeconds);
	}

	/**
	 * Sends a message and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, null)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @return the reply received as soon as available, or <code>null</code>
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend){
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, null);
	}

	/**
	 * Sends a message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, timeOutMilliSeconds)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend,final int timeOutMilliSeconds){
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, timeOutMilliSeconds);
	}

	/**
	 * Sends a message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, senderRole, null)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend, String senderRole){
		return sendMessageWithRoleAndWaitForReply(receiver, messageToSend, senderRole, null);
	}

	/**
	 * Sends a message and waits for an answer to it.
	 * Additionally, the sending is done using a specific role for the sender.
	 * 
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message, that is any {@link AbstractAgent.ReturnCode}
	 * different from {@link AbstractAgent.ReturnCode#SUCCESS} 
	 * (see {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)}). 
	 * 
	 * @see #sendMessageWithRole(AgentAddress, Message, String)
	 * @see AbstractAgent.ReturnCode
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend, String senderRole, Integer timeOutMilliSeconds ){
		//no need to checkAliveness : this is done in noLogSendingMessage
		if(logger != null)
			logger.finest("sendMessageAndWaitForReply : sending "+messageToSend+" to "+receiver+", and waiting reply...");
		if(kernel.sendMessage(this, receiver, messageToSend,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds == null ? null : TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds));
	}

	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, null, null)</code>
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param messageToSend the message to send.
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> 
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(String, String, String, Message, String, Integer)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5
	 */
	public Message sendMessageAndWaitForReply(final String community, final String group, final String role, Message messageToSend){
		return sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, null,null);
	}

	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, senderRole, null)</code>
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code>
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(String, String, String, Message, String, Integer)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendMessageWithRoleAndWaitForReply(final String community, final String group, final String role, 
			Message messageToSend,
			final String senderRole){
		return sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, senderRole, null);
	}

	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, null, timeOutMilliSeconds)</code>
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param messageToSend the message to send.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(String, String, String, Message, String, Integer)
	 * @throws KernelException if this agent has not been launched or is already terminated
	 * @since MadKit 5
	 */
	public Message sendMessageAndWaitForReply(final String community, final String group, final String role, Message messageToSend, final int timeOutMilliSeconds){
		return sendMessageWithRoleAndWaitForReply(community, group, role, messageToSend, null, timeOutMilliSeconds);
	}

	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits for an answer to it.
	 * The targeted agent is selected randomly among matched agents.
	 * The sender is excluded from this search.
	 * @param community the community name
	 * @param group the group name
	 * @param role the role name
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendMessageWithRoleAndWaitForReply(final String community, final String group, final String role, 
			Message messageToSend,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		if(logger != null)
			logger.finest("sendMessageAndWaitForReply : sending "+messageToSend+" to any "+printCGR(community, group, role)+
					(timeOutMilliSeconds == null ? "":", and waiting reply for "+TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds)+" s..."));
		if(kernel.sendMessage(this,community,group,role, messageToSend,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(messageToSend,timeOutMilliSeconds == null ? null : TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds) );
	}

	/**
	 * Sends a reply message and waits indefinitely for an answer to it.
	 * This has the same effect as <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, null)</code>.
	 * 
	 * @param messageToReplyTo the original message previously received.
	 * @param reply the new message.
	 * @return the reply received as soon as available.
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply){
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, null);
	}

	/**
	 * Sends a reply message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, timeOutMilliSeconds)</code>.
	 * 
	 * @param messageToReplyTo the original message previously received
	 * @param reply the new message
	 * @return the reply received as soon as available
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply,int timeOutMilliSeconds){
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, timeOutMilliSeconds);
	}

	/**
	 * Sends a reply message and waits indefinitely for an answer to it.
	 * This has the same effect as <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, senderRole, null)</code>.
	 * @param messageToReplyTo the original message previously received
	 * @param reply the new message
	 * @return the reply received as soon as available
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply,String senderRole){
		return sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, senderRole, null);
	}

	/**
	 * Sends a reply message and waits for an answer to it.
	 * Additionally, the reply is done using a specific role for the sender.
	 * 
	 * @param messageToReplyTo the original message previously received
	 * @param reply the reply message.
	 * @param senderRole the role with which the reply is sent.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the reply, that is any {@link AbstractAgent.ReturnCode}
	 * different from {@link AbstractAgent.ReturnCode#SUCCESS} 
	 * (see {@link AbstractAgent#sendReplyWithRole(Message, Message, String)}). 
	 * 
	 * @see #sendReplyWithRole(Message, Message, String)
	 * @see AbstractAgent.ReturnCode
	 * @since MadKit 5
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply, String senderRole, Integer timeOutMilliSeconds){
		if(logger != null)
			logger.finest("sendReplyAndWaitForReply : sending "+reply+" as reply to "+messageToReplyTo+", and waiting reply...");
		if(sendReplyWithRole(messageToReplyTo, reply,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(reply,TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds));
	}
	
	/**
	 * Broadcasts a message and wait for answers considering a time out duration.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param message
	 * @param senderRole
	 * @param timeOutMilliSeconds
	 * @return a list of messages which are answers to the <code>message</code> which has been broadcasted.
	 * @throws KernelException if this agent has not been launched or is already terminated
	 */
	public List<Message> broadcastMessageWithRoleAndWaitForReplies(final String community, final String group, final String role, 
			Message message,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		return kernel.broadcastMessageWithRoleAndWaitForReplies(this, community, group, role, message, senderRole, timeOutMilliSeconds);
	}

	/**
	 * This method is the blocking version of nextMessage(). If there is no
	 * message in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNextMessage(long)
	 * @return the first message of received in the mailbox
	 */
	public Message waitNextMessage()
	{
		if(logger != null){
			logger.finest("Waiting next message...");
			final Message m = waitingNextMessageForEver();
			if(logger != null)
				logger.finest("... a new message has been received "+m);
			return m;
		}
		return waitingNextMessageForEver();
	}

	/**
	 * This method gets the next message of the mailbox or waits 
	 * for a new incoming message considering a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return  the first message in the mailbox, or <code>null</code> if no message
	 * has been received before the time out delay is elapsed
	 */
	final public Message waitNextMessage(final long timeOutMilliseconds)
	{
		if(logger != null){
			logger.finest("Waiting next message during "+timeOutMilliseconds+" milliseconds...");
			final Message m = waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
			if(m != null)
				logger.finest("... a new message has been received "+m);
			else
				logger.finest("... wait next message has reached time out, no message received");
			return m;
		}
		return waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * This method gets the next message of the mailbox or waits 
	 * for a new incoming message considering a certain delay.
	 * 
	 * @param timeOut the maximum time to wait.
	 * @param unit The time unit for this time out, for instance : TimeUnit.MILLISECONDS.
	 * @return the first message in the mailbox, or <code>null</code> if no message
	 * has been received before the time out is elapsed.
	 * @since MadKit 5
	 */
	final public Message waitNextMessage(final long timeOut, final TimeUnit unit)
	{
		if(logger != null)
			logger.finest("Waiting next message during "+timeOut+" "+unit);
		return waitingNextMessage(timeOut, unit);
	}

//	/**
//	 * @see madkit.kernel.AbstractAgent#nextMessage()
//	 */
//	@Override
//	public Message nextMessage() {
//		checkAliveness();
////		//no checkAliveness : this could be done in the constructor.
////		if (myThread.isInterrupted())
////			throw new KilledException(); //This is nawak if another thread call this
//		return super.nextMessage();
//	}

	/**
	 * Stops the agent's process for a while.
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	protected void pause(final int milliSeconds) {
		if(logger != null)
			logger.finest("Making a pause during "+milliSeconds+ " milliseconds");
		if(milliSeconds <0)
			return;
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			if (Thread.currentThread() == myThread) {
				throw new KilledException(e);
			}
		}
	}

	/**
	 * @param myThread the myThread to set
	 * @since MadKit 5
	 */
	final void setMyThread(final Thread thread) {
		thread.setName(getName());
		this.myThread = thread;
	}
	
	/**
	 * @see madkit.kernel.AbstractAgent#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		if (myThread != null) {
			myThread.setName(name);
		}
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @since MadKit 5
	 */
	private Message waitingNextMessageForEver() {
		try {
			return messageBox.takeFirst();
		} catch (InterruptedException e) {
			throw new KilledException(e);
		} catch (IllegalMonitorStateException e) {
			throw new KilledException(e);
		}
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @since MadKit 5
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
	 * @param m
	 * @return
	 */
	private Message waitAnswer(final Message m) {
		Message answer;
		final LinkedList<Message> receptions = new LinkedList<Message>();
		final long conversationID = m.getConversationID();
		answer = waitingNextMessageForEver();
		while(answer.getConversationID() != conversationID){
			receptions.add(answer);
			answer = waitingNextMessageForEver();
		}
		if (! receptions.isEmpty()) {
			Collections.reverse(receptions);
			for (final Message message : receptions) {
				messageBox.addFirst(message);
			}
		}
		if(logger != null)
			logger.finest("a reply has arrived "+answer);
		return answer;
	}

	/**
	 * @param theReply
	 * @param timeOutNanos
	 * @return
	 */
	private Message waitAnswer(final Message theMessageToReplyTo, Long timeOutNanos) {
		if(timeOutNanos == null)
			return waitAnswer(theMessageToReplyTo);
		Message answer;
		final LinkedList<Message> receptions = new LinkedList<Message>();
		//conversion
		final long endTime = System.nanoTime()+timeOutNanos;
		final long conversationID = theMessageToReplyTo.getConversationID();
		answer = waitingNextMessage(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && answer.getConversationID() != conversationID) {
			//			System.err.println(getName()+"\n\n\n-------------------- ID = "+answer.getID()+"\n\n\n");
			receptions.add(answer);
			answer = waitingNextMessage(endTime - System.nanoTime(),TimeUnit.NANOSECONDS);
		}
		if (! receptions.isEmpty()) {
			Collections.reverse(receptions);
			for (final Message message : receptions) {
				messageBox.addFirst(message);
			}
		}
		if(answer == null){
			if(logger != null)
				logger.finest("...Waiting for reply has reached time out, no reply received");
			return null;
		}
		if(logger != null)
			logger.finest("...a reply has arrived : "+answer);
		return answer;
	}
	
	List<Message> waitAnswers(Message message, int size, Integer timeOutMilliSeconds) {
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

//	void checkAliveness(){
//		if(isAgentThread() && Thread.interrupted()){
//			throw new KilledException(" get interrupted ");
//		}
//	}
	
//	private boolean isAgentThread(){
//		return Thread.currentThread() == myThread;
//	}
}
