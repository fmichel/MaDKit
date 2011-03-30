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

import static madkit.kernel.Utils.getI18N;
import static madkit.kernel.Utils.printCGR;
import static madkit.kernel.AbstractAgent.State.*;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

 * @author Olivier Gutknecht
 * @author Fabien Michel (since v.3)
 * @since MadKit 1.0
 * @version 5.0	
 */
public class Agent extends AbstractAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2333640522509416129L;
	final ExecutorService agentExecutor;
	Thread myThread;
	ArrayList<Future<Boolean>> myLifeCycle;

	//	/**
	//	 * @return the myThread
	//	 */
	//	final Thread getMyThread() {
	//		return myThread;
	//	}

	/**
	 * 
	 */
	public Agent() {
		agentExecutor = Executors.newSingleThreadScheduledExecutor(normalAgentThreadFactory);
	}

	Agent(ExecutorService es){
		agentExecutor = es;
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
	}

	/** Kills a targeted agent
	 * @see madkit.kernel.AbstractAgent#killAgent(madkit.kernel.AbstractAgent, int)
	 * @since MadKit 5
	 */
	@Override
	public ReturnCode killAgent(AbstractAgent target, int timeoutSeconds) {//TODO fins something else
		if(target == this && myThread == Thread.currentThread()){//TODO throw killedexception
			if(logger != null){
				logger.fine("Killing myself !!! ");
			}
			getAlive().set(false);
			if(timeoutSeconds == 0){
				myLifeCycle.get(2).cancel(true);
			}
			myLifeCycle.get(1).cancel(true);
			throw new KilledException();
		}
		else{
			return super.killAgent(target, timeoutSeconds);
		}
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
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply, String senderRole, Integer timeOutMilliSeconds){
		if(logger != null)
			logger.finest("sendReplyAndWaitForReply : sending "+reply+" as reply to "+messageToReplyTo+", and waiting reply...");
		if(kernel.sendReplyWithRole(this,messageToReplyTo, reply,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(reply,TimeUnit.MILLISECONDS.toNanos(timeOutMilliSeconds));
	}
	
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

	/**
	 * @see madkit.kernel.AbstractAgent#nextMessage()
	 */
	@Override
	public Message nextMessage() {
		checkAliveness();
//		//no checkAliveness : this could be done in the constructor.
//		if (myThread.isInterrupted())
//			throw new KilledException(); //This is nawak if another thread call this
		return super.nextMessage();
	}

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
			if (isAgentThread()) {
				throw new KilledException();
			}
		}
	}

	boolean activation() {
		//kernel.get().agentCounter++; //TODO useless !!
		if(logger != null){
			logger.finer("** entering ACTIVATE **");
		}
		if(! state.compareAndSet(INITIALIZING, ACTIVATED))
			throw new AssertionError("not init in activation");
//		synchronized (getAlive()) {
//			getAlive().notify();
//		}
		try {
			activate();
		} catch (KilledException e) {
//			e.printStackTrace();
			if(logger != null){
				logger.warning("-*-GET KILLED in ACTIVATE-*- : "+getI18N("terminated"));
			}
			return false;
		} catch (Exception e) {
			kernel.kernelLog("Problem for "+this+" in ACTIVATE ", Level.FINER, e);
			logSevereException(e);
			return false;
		} finally {
			if(logger != null)
				logger.finer("** exiting ACTIVATE **");
		}
		return true;
	}

//	/**
//	 * @see madkit.kernel.AbstractAgent#terminate()
//	 * @since MadKit 5
//	 */
//	@Override
//	void terminate() {
//		kernel.removeThreadedAgent(this);//TODO this should be the last call
//		super.terminate();
//	}

	/**
	 * @param myLifeCycle the myLifeCycle to set
	 * @since MadKit 5
	 */
	void setMyLifeCycle(ArrayList<Future<Boolean>> myLifeCycle) {
		this.myLifeCycle = myLifeCycle;
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
				logger.warning("-*-GET KILLED in LIVE-*- : "+getI18N("terminated"));
				//				logger.warning("my tasks "+myLifeCycle);//TODO remove that
			}
			return false;
		} catch (Exception e) {
			kernel.kernelLog("Problem for "+this+" in LIVE ", Level.FINER, e);
			logSevereException(e);
			return false;
		} finally {
			//			getRunState().set(ENDING);
			if(logger != null)
				logger.finer("** exiting LIVE **");
		}
		return true;
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
	 * @return the myLifeCycle
	 * @since MadKit 5
	 */
	final ArrayList<Future<Boolean>> getMyLifeCycle() {
		return myLifeCycle;
	}

	/**
	 * @return the agentExecutor
	 */
	@Override
	final ExecutorService getAgentExecutor() {
		return agentExecutor;
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
			throw new KilledException();
		} catch (IllegalMonitorStateException e) {
			throw new KilledException();
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
			throw new KilledException();
		} catch (IllegalMonitorStateException e) {
			throw new KilledException();
		}
	}

	/**
	 * @param m
	 * @return
	 */
	private Message waitAnswer(final Message m) {
		Message answer;
		final LinkedList<Message> receptions = new LinkedList<Message>();
		final long conversationID = m.getID();
		answer = waitingNextMessageForEver();
		while(answer.getID() != conversationID){
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
		final long conversationID = theMessageToReplyTo.getID();
		answer = waitingNextMessage(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && answer.getID() != conversationID) {
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
		final long conversationID = message.getID();
		int missing = size;
		final LinkedList<Message> receptions = new LinkedList<Message>();
		final LinkedList<Message> answers = new LinkedList<Message>();
		while(missing > 0 && System.nanoTime() < endTime){
			Message answer = waitingNextMessage(endTime - System.nanoTime(),TimeUnit.NANOSECONDS);
			if(answer == null)
				break;
			if(answer.getID() == conversationID){
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

	void checkAliveness(){
		if(isAgentThread() && Thread.interrupted()){
			throw new KilledException();
		}
	}
	
	private boolean isAgentThread(){
		return Thread.currentThread() == myThread;
	}
}
