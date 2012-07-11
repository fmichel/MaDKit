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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;

/**
 * The super class of all MaDKit threaded agents, v 5. 
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
 * @since MaDKit 1.0
 * @version 5.11
 */
public class Agent extends AbstractAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8564494100061187968L;
	Thread myThread;

	final private AgentExecutor agentExecutor;
	final private boolean isDaemon;

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
	 * pretty dirty solution for fake and terminated kernel
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
	@Override
	final AgentExecutor getAgentExecutor() {
		return agentExecutor;
	}
	
	/**
	 * @param e
	 */
	final void suicide(SelfKillException e) {
		getAgentExecutor().getLiveProcess().cancel(false);
		getAgentExecutor().getEndProcess().cancel(false);
		super.suicide(e);
		terminate(); //the ae's terminate will not be executed because it will be on fake kernel
	}


	final boolean living() {
		try {
			state.set(State.LIVING);
			setMyThread(Thread.currentThread());
			logMethod(true);
			try {
				live();
			} catch (SelfKillException e) {
				suicide(e);
			} catch (Exception e) {
				synchronized (state) {//TODO factoriser
					alive.set(false);
					logLifeException(e);
				}
			}
			if(! alive.get()){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}//answer the kill
			}
		} catch (KilledException e) {
			logLifeException(e);
		}
		logMethod(false);
		return true;
	}

	/**
	 * This method corresponds to the second behavior which is called by the MaDKit kernel 
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
		if(logger != null)
			logger.talk("\n\tHi human and hello World !!\n\n I am an instance of the madkit.kernel.Agent class\n As such, I am a MaDKit threaded Agent\n and thus have an autonomous activity!");
		pause(5000);
		if(logger != null)
		logger.talk("\n\n And in fact, I am the simpliest agent ever\n because I simply do nothing at all :)\n\n");
		pause(4000);
		int i = (int) (Math.random()*3000+4500);
		if(logger != null)
		logger.info("I will quit in "+i+" milliseconds... Bye !");
		pause(i);
	}

	@Override
	public ReturnCode killAgent(AbstractAgent target, int timeOutSeconds) {
		//if this is a self kill done by the agent itself, not an object which has access to the agent
		if(target == this && myThread == Thread.currentThread() && alive.compareAndSet(true, false)){
			throw new SelfKillException(""+timeOutSeconds);
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
	 * @since MaDKit 5
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend){ // NO_UCD
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend, String senderRole, Integer timeOutMilliSeconds ){
		//no need to checkAliveness : this is done in noLogSendingMessage
		if(logger != null)
			logger.finest("sendMessageAndWaitForReply : sending "+messageToSend+" to "+receiver+", and waiting reply...");
		if(getKernel().sendMessage(this, receiver, messageToSend,senderRole) != SUCCESS){
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
	 */
	public Message sendMessageWithRoleAndWaitForReply(final String community, final String group, final String role, 
			Message messageToSend,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		if(logger != null)
			logger.finest("sendMessageAndWaitForReply : sending "+messageToSend+" to any "+I18nUtilities.getCGRString(community, group, role)+
					(timeOutMilliSeconds == null ? "":", and waiting reply for "+TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds)+" s..."));
		if(getKernel().sendMessage(this,community,group,role, messageToSend,senderRole) != SUCCESS){
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * @since MaDKit 5
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
	 * Broadcasts a message and wait for answers considering a timeout duration.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param message
	 * @param senderRole
	 * @param timeOutMilliSeconds
	 * @return a list of messages which are answers to the <code>message</code> which has been broadcasted.
	 */
	public List<Message> broadcastMessageWithRoleAndWaitForReplies(final String community, final String group, final String role, 
			Message message,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		return getKernel().broadcastMessageWithRoleAndWaitForReplies(this, community, group, role, message, senderRole, timeOutMilliSeconds);
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
			logger.finest("waitNextMessage...");
			final Message m = waitingNextMessageForEver();
			if(logger != null)
				logger.finest("..."+Words.NEW_MSG+": "+m);
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
				logger.finest("..."+Words.NEW_MSG+": "+m);
			else
				logger.finest("...time out !");
			return m;
		}
		return waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
	}

// TODO Remove unused code found by UCDetector
// 	/**
// 	 * This method gets the next message of the mailbox or waits 
// 	 * for a new incoming message considering a certain delay.
// 	 * 
// 	 * @param timeOut the maximum time to wait.
// 	 * @param unit The time unit for this time out, for instance : TimeUnit.MILLISECONDS.
// 	 * @return the first message in the mailbox, or <code>null</code> if no message
// 	 * has been received before the time out is elapsed.
// 	 * @since MaDKit 5
// 	 */
// 	final public Message waitNextMessage(final long timeOut, final TimeUnit unit)
// 	{
// 		if(logger != null)
// 			logger.finest("Waiting next message during "+timeOut+" "+unit);
// 		return waitingNextMessage(timeOut, unit);
// 	}

	/**
	 * Stops the agent's process for a while.
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	protected void pause(final int milliSeconds) {
		if(logger != null)
			logger.finest(Words.PAUSE+" "+milliSeconds+ " ms.");
		if(milliSeconds <0)
			return;
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			handleInterruptedException();
		}
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @since MaDKit 5
	 */
	private Message waitingNextMessageForEver() {
		try {
			return messageBox.take();
		} catch (InterruptedException e) {
			handleInterruptedException();
			//		} catch (IllegalMonitorStateException e) {
			//			throw e;
		}
		return null;
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @since MaDKit 5
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
	 * @param m
	 * @return
	 */
	private Message waitAnswer(final Message m) {
		Message answer;
		final List<Message> receptions = new ArrayList<Message>(messageBox.size());
		final long conversationID = m.getConversationID();
		answer = waitingNextMessageForEver();
		while(answer.getConversationID() != conversationID){
			receptions.add(answer);
			answer = waitingNextMessageForEver();
		}
		if (!receptions.isEmpty()) {
			synchronized (messageBox) {
					messageBox.addAll(receptions);
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
		final List<Message> receptions = new ArrayList<Message>(messageBox.size());
		//conversion
		final long endTime = System.nanoTime()+timeOutNanos;
		final long conversationID = theMessageToReplyTo.getConversationID();
		answer = waitingNextMessage(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && answer.getConversationID() != conversationID) {
			receptions.add(answer);
			answer = waitingNextMessage(endTime - System.nanoTime(),TimeUnit.NANOSECONDS);
		}
		if (!receptions.isEmpty()) {
			synchronized (messageBox) {
					messageBox.addAll(receptions);
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

}
