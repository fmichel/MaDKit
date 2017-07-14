/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;
import madkit.message.ConversationFilter;
import madkit.message.MessageFilter;

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
 * @version 5.0.2
 */
public class Agent extends AbstractAgent{

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
    * Changes the priority of the agent's thread. 
    * This should be used only starting from the {@link Agent#activate()}
    * to have a concrete effect.
    * Default priority is set to {@link Thread#NORM_PRIORITY} - 1 
    * to ensure swing responsiveness.
    * 
    * @param newPriority priority to set this thread to
    * @exception  IllegalArgumentException  If the priority is not in the
    *               range <code>Thread.MIN_PRIORITY</code> to
    *               <code>Thread.MAX_PRIORITY</code>.
    * @exception  SecurityException  if the current thread cannot modify this thread.
    * @see        Thread
    * @since MadKit 5.0.1
    */
	//do not give any access to the thread because its name is normalized
	public void setThreadPriority(int newPriority){
		if(myThread != null){
			myThread.setPriority(newPriority);
		}
	}

   /**
    * Returns this thread's priority.
    *
    * @return  this thread's priority for this agent.
    * @see     Thread
    * @since MadKit 5.0.1
    */
	public int getThreadPriority(){
		if(myThread != null){
			return myThread.getPriority();
		}
		return Thread.NORM_PRIORITY;
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
		terminate(); //the ae's terminate method will not be executed because it will be on fake kernel
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
			} catch (Throwable e) {
				synchronized (state) {
					logLifeException(e);
					alive.set(false);
				}
			}
			if(! alive.get()){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}//answer the kill
			}
		} catch (ThreadDeath e) {
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
		getLogger().setLevel(Level.INFO);
		getLogger().talk("\n\tHi Human and hello World !!\n\n I am an instance of the madkit.kernel.Agent class\n As such, I am a MaDKit threaded Agent\n and thus have an autonomous activity!");
		pause(5000);
		getLogger().talk("\n\n And in fact, I am the simplest agent ever\n because I simply do nothing at all :)\n\n");
		pause(4000);
		int i = (int) (Math.random()*3000+4500);
		getLogger().info("I will quit in "+i+" milliseconds... Bye !");
		pause(i);
	}

	@Override
	public ReturnCode killAgent(AbstractAgent target, int timeOutSeconds) {
		//if this is a self kill done by the agent itself, not an object which has access to the agent
		if(target == this && myThread == Thread.currentThread() && alive.compareAndSet(true, false)){
			throw new SelfKillException(timeOutSeconds);
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
			logger.finest(() -> "sendMessageAndWaitForReply : sending "+messageToSend+" to "+receiver+", and waiting reply...");
		if(getKernel().sendMessage(this, receiver, messageToSend,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
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
			logger.finest(() -> "sendMessageAndWaitForReply : sending "+messageToSend+" to any "+I18nUtilities.getCGRString(community, group, role)+
					(timeOutMilliSeconds == null ? "":", and waiting reply for "+TimeUnit.MILLISECONDS.toSeconds(timeOutMilliSeconds)+" s..."));
		if(getKernel().sendMessage(this,community,group,role, messageToSend,senderRole) != SUCCESS){
			return null;
		}
		return waitAnswer(messageToSend, timeOutMilliSeconds);
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
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply, int timeOutMilliSeconds){
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
		if(sendReplyWithRole(messageToReplyTo, reply,senderRole) != SUCCESS){
			return null;
		}
		if(logger != null)
			logger.finest(() -> "sendReplyAndWaitForReply : sending "+reply+" as reply to "+messageToReplyTo+", and waiting reply...");
		return waitAnswer(reply,timeOutMilliSeconds);
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
	 * @return the first received message
	 */
	public Message waitNextMessage()
	{
		if(logger != null){
			logger.finest(() -> "waitNextMessage...");
			final Message m = waitingNextMessageForEver();
			logger.finest(() -> "..."+Words.NEW_MSG+": "+m);
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
	public Message waitNextMessage(final long timeOutMilliseconds)
	{
		if(logger != null){
			logger.finest(() -> "Waiting next message during "+timeOutMilliseconds+" milliseconds...");
			final Message m = waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
			if(m != null)
				logger.finest(() -> "waitNextMessage->"+Words.NEW_MSG+": "+m);
			else
				logger.finest(() -> "waitNextMessage time out !");
			return m;
		}
		return waitingNextMessage(timeOutMilliseconds, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Retrieves and removes the next message that complies
	 * with the filter, waiting for ever if necessary
	 * until a matching message becomes available.
	 * 
	 * @param filter
	 * 
	 * @return 	the first received message that matches the filter
	 */
	public Message waitNextMessage(final MessageFilter filter) {
		final List<Message> receptions = new ArrayList<>();
		Message m = waitingNextMessageForEver();
		while (!filter.accept(m)) {
			receptions.add(m);
			m = waitingNextMessageForEver();
		}
		addAllToMessageBox(receptions);
		if (logger != null) {
			final Message answerFinal = m;
			logger.finest(() -> "a match has arrived " + answerFinal);
		}
		return m;
	}

	/**
	 * This method gets the next message of the mailbox or waits 
	 * for a new incoming acceptable message up to a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * @param filter
	 * 
	 * @return a message that matches or <code>null</code> otherwise.
	 */
	public Message waitNextMessage(final Integer timeOutMilliseconds, final MessageFilter filter)
	{
		if(timeOutMilliseconds == null){
			return waitNextMessage(filter);
		}
		// conversion
		final long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(timeOutMilliseconds);
		final List<Message> receptions = new ArrayList<>();
		final long endTime = System.nanoTime() + timeOutNanos;
		Message answer = waitingNextMessage(timeOutNanos, TimeUnit.NANOSECONDS);
		while (answer != null && ! filter.accept(answer)) {
			receptions.add(answer);
			answer = waitingNextMessage(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		addAllToMessageBox(receptions);
//		if (!receptions.isEmpty()) {
//			synchronized (messageBox) {
//				messageBox.addAll(receptions);
//			}
//		}
		if(logger != null){
			final Message answerFinal = answer;
			logger.finest(() -> (answerFinal == null) ? "...Waiting time out, no compliant message received" : "...a match has arrived : " + answerFinal);
		}
		return answer;
	}

	/**
	 * Stops the agent's process for a while.
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	protected void pause(final int milliSeconds) {
		if (milliSeconds > 0) {
			if (logger != null)
				logger.finest(() -> Words.PAUSE + " " + milliSeconds + " ms.");
			try {
				Thread.sleep(milliSeconds);
			} catch (InterruptedException e) {
				handleInterruptedException();
			}
		}
	}

	/**
	 * @return message
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
	 * Retrieves and removes the next message that is a reply
	 * to the query message, waiting for ever if necessary
	 * until a matching reply becomes available.
	 * 
	 * @param query the message for which a reply is waited for 
	 * 
	 * @return 	the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final Message query) {
		return waitNextMessage(new ConversationFilter(query));
	}

	/**
	 * Retrieves and removes the next message that is a reply
	 * to the query message, waiting for ever if necessary
	 * until a matching reply becomes available.
	 * 
	 * @param query the message for which a reply is waited for 
	 * @param timeOutMilliSeconds the maximum time to wait, in milliseconds.
	 * 
	 * @return 	the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	public Message waitAnswer(final Message query, final Integer timeOutMilliSeconds) {
		return waitNextMessage(timeOutMilliSeconds, new ConversationFilter(query));
	}
	
	

}
