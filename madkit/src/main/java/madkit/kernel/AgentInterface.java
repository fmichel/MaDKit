package madkit.kernel;

import java.time.LocalDateTime;
import java.util.List;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.SimulationTimer;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;
import madkit.simulation.activator.DiscreteEventAgentsActivator;

/**
 * @author Fabien Michel
 *
 */
public interface AgentInterface {

	boolean isAlive();

	ReturnCode killAgent(Agent a);

	/**
	 * Returns the agent's logger. It is lazily created so that if
	 * {@link #getLogger()} is not used, there is no memory foot print at all, which
	 * could be crucial when working with thousands of abstract agents in simulation
	 * mode.
	 *
	 * @return the agent's logger.
	 * @see AgentLogger
	 * @since MaDKit 5.0.0.6
	 */
	AgentLogger getLogger();


	KernelConfig getKernelConfiguration();

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, false, null)</code>
	 *
	 * @param community the community within which the group will be created. If
	 *                  this community does not exist it will be created.
	 * @param group     the name of the new group
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	ReturnCode createGroup(String community, String group);

	/**
	 * Creates a new Group within a community. This has the same effect as
	 * <code>createGroup(community, group, isDistributed, null)</code>
	 *
	 * @param community     the community within which the group will be created. If
	 *                      this community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed
	 *                      when multiple MaDKit kernels are connected.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_GROUP}</code>: If the operation
	 *         failed because such a group already exists.</li>
	 *         <li><code>
	 *         {@link ReturnCode#IGNORED}</code>: If this method is used in activate
	 *         and this agent has been launched using
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles</li>
	 *         </ul>
	 * @see Agent#createGroup(String, String, boolean, Gatekeeper)
	 * @since MaDKit 5.0
	 */
	ReturnCode createGroup(String community, String group, boolean isDistributed);

	Organization getOrgnization();

	/**
	 * Creates a new Group within a community.
	 * <p>
	 * If this operation succeed, the agent will automatically handle the role
	 * defined by {@link DefaultMaDKitRoles#GROUP_MANAGER_ROLE}, which value is <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i>, in this
	 * created group. Especially, if the agent leaves the role of <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i>, it will also
	 * automatically leave the group and thus all the roles it has in this group.
	 * <p>
	 * Agents that want to enter the group may send messages to the <i>
	 * {@value madkit.agr.DefaultMaDKitRoles#GROUP_MANAGER_ROLE}</i> using the role
	 * defined by {@link DefaultMaDKitRoles#GROUP_CANDIDATE_ROLE}, which value is
	 * <i> {@value madkit.agr.DefaultMaDKitRoles#GROUP_CANDIDATE_ROLE}</i>.
	 *
	 * @param community     the community within which the group will be created. If
	 *                      this community does not exist it will be created.
	 * @param group         the name of the new group.
	 * @param isDistributed if <code>true</code> the new group will be distributed
	 *                      when multiple MaDKit kernels are connected.
	 * @param keyMaster     any object that implements the {@link Gatekeeper}
	 *                      interface. If not <code>null</code>, this object will be
	 *                      used to check if an agent can be admitted in the group.
	 *                      When this object is null, there is no group access
	 *                      control.
	 * @return
	 *         <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the group has been
	 *         successfully created.</li>
	 *         <li><code>
	 *         {@link ReturnCode#ALREADY_GROUP}</code>: If the operation failed
	 *         because such a group already exists.</li>
	 *         <li><code>{@link ReturnCode#IGNORED}</code>: If the agent has been
	 *         launched using a <code>launchAgentBucket</code> method such as
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul>
	 * @see Gatekeeper
	 * @see ReturnCode
	 * @since MaDKit 5.0
	 */
	ReturnCode createGroup(String community, String group, boolean isDistributed, Gatekeeper keyMaster);

	/**
	 * Requests a role within a group of a particular community. This has the same
	 * effect as <code>requestRole(community, group, role, null, false)</code>. So
	 * the passKey is <code>null</code> and the group must not be secured for this
	 * to succeed.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @see #requestRole(String, String, String, Object)
	 * @since MaDKit 5.0
	 */
	ReturnCode requestRole(String community, String group, String role);

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 *
	 * @param community the group's community.
	 * @param group     the targeted group.
	 * @param role      the desired role.
	 * @param passKey   the <code>passKey</code> to enter a secured group. It is
	 *                  generally delivered by the group's <i>group manager</i>. It
	 *                  could be <code>null</code>, which is sufficient to enter an
	 *                  unsecured group. Especially,
	 *                  {@link #requestRole(String, String, String)} uses a
	 *                  <code>null</code> <code>passKey</code>.
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
	 *         {@link Agent#launchAgentBucket(List, String...)} with non
	 *         <code>null</code> roles. This for optimization purposes.</li>
	 *         </ul>
	 * @see Agent.ReturnCode
	 * @see Gatekeeper
	 * @since MaDKit 5.0
	 */
	ReturnCode requestRole(String community, String group, String role, Object passKey);

	/**
	 * Makes this agent leaves the group of a particular community.
	 *
	 * @param community the community name
	 * @param group     the group name
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
	 * @since MaDKit 5.0
	 * @see ReturnCode
	 */
	ReturnCode leaveGroup(String community, String group);

	/**
	 * Abandons an handled role within a group of a particular community.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
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
	 * @see Agent.ReturnCode
	 * @since MaDKit 5.0
	 */
	ReturnCode leaveRole(String community, String group, String role);

	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this role
	 *         or <code>null</code> if such an agent does not exist.
	 */
	AgentAddress getAgentWithRole(String community, String group, String role);

	/**
	 * Return a string representing a unique identifier for the agent over the
	 * network.
	 *
	 * @return the agent's network identifier
	 */
	String getNetworkID();

	/**
	 * Sends a message, using an agent address, specifying explicitly the role used
	 * to send it.
	 * 
	 * @param message  the message to send
	 * @param receiver the targeted agent
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
	ReturnCode sendWithRole(Message message, AgentAddress receiver, String senderRole);

	/**
	 * Sends a message to an agent using an agent address. This has the same effect
	 * as <code>sendWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param message
	 * @param receiver
	 *
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
	ReturnCode send(Message message, AgentAddress receiver);

	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect as
	 * sendMessageWithRole(community, group, role, messageToSend,null). If several
	 * agents match, the target is chosen randomly. The sender is excluded from this
	 * search.
	 * 
	 * @param message   the message to send
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 *
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
	 */
	ReturnCode send(Message message, String community, String group, String role);

	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(community, group, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param message    the message to send
	 * @param community  the community name
	 * @param group      the group name
	 * @param role       the role name
	 * @param senderRole the agent's role with which the message has to be sent
	 *
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
	 */
	ReturnCode sendWithRole(Message message, String community, String group, String role, String senderRole);

	/**
	 * Broadcasts a message to every agent having a role in a group in a community,
	 * but not to the sender.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @param message
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
	 *         not a member of the targeted group.</li>
	 *         </ul>
	 * @see ReturnCode
	 */
	ReturnCode broadcast(String community, String group, String role, Message message);

	/**
	 * Broadcasts a message to every agent having a role in a group in a community
	 * using a specific role for the sender. The sender is excluded from the search.
	 *
	 * @param community     the community name
	 * @param group         the group name
	 * @param role          the role name
	 * @param messageToSend
	 * @param senderRole    the agent's role with which the message should be sent
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
	 */
	ReturnCode broadcastWithRole(String community, String group, String role, Message messageToSend, String senderRole);

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
	List<Message> broadcastMessageWithRoleAndWaitForReplies(String community, String group, String role,
			Message message, String senderRole, Integer timeOutMilliSeconds);
	//	catch(CGRNotAvailable e) {
	//	    final ReturnCode r = e.getCode();
	//	    if (r == NO_RECIPIENT_FOUND) {
	//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new MadkitWarning(r));
	//	    }
	//	    else if (r == ROLE_NOT_HANDLED) {
	//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new OrganizationWarning(r, community, group, senderRole));
	//	    }
	//	    else {
	//		requester.handleWarning(Influence.BROADCAST_MESSAGE_AND_WAIT, () -> new OrganizationWarning(r, community, group, role));
	//	    }
	//	    return null;
	//	}

	/**
	 * Checks if this agent address is still valid. I.e. the corresponding agent is
	 * still playing this role.
	 *
	 * @return <code>true</code> if the address still exists in the organization.
	 * @since MaDKit 5.0.4
	 */
	boolean checkAgentAddress(AgentAddress agentAddress);

	/**
	 * Sends a message by replying to a previously received message. The sender is
	 * excluded from this search.
	 *
	 * @param messageToReplyTo the previously received message.
	 * @param reply            the reply itself.
	 * @param senderRole       the agent's role with which the message should be
	 *                         sent
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
	 */
	ReturnCode replyWithRole(Message messageToReplyTo, Message reply, String senderRole);

	/**
	 * Sends a message by replying to a previously received message. This has the
	 * same effect as <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 *
	 * @param messageToReplyTo the previously received message.
	 * @param reply            the reply itself.
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
	 * @see Agent#replyWithRole(Message, Message, String)
	 */
	ReturnCode reply(Message messageToReplyTo, Message reply);

	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 *
	 * @param originalMessage the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
	Message getReplyTo(Message originalMessage);

	//////////////////////////////////// WAIT
	/**
	 * This method is the blocking version of nextMessage(). If there is no message
	 * in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNextMessage(long)
	 * @return the first received message
	 */
	Message waitNextMessage();

	/**
	 * This method gets the next message of the mailbox or waits for a new incoming
	 * message considering a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return the first message in the mailbox, or <code>null</code> if no message
	 *         has been received before the time out delay is elapsed
	 */
	Message waitNextMessage(long timeOutMilliseconds);

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query the message for which a reply is waited for
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	Message waitAnswer(Message query);

	/**
	 * Retrieves and removes the next message that is a reply to the query message,
	 * waiting for ever if necessary until a matching reply becomes available.
	 * 
	 * @param query               the message for which a reply is waited for
	 * @param timeOutMilliSeconds the maximum time to wait, in milliseconds.
	 * 
	 * @return the first reply to the query message
	 * @since MadKit 5.0.4
	 */
	Message waitAnswer(Message query, Integer timeOutMilliSeconds);

	/**
	 * This method offers a convenient way for regular object to send messages to
	 * Agents, especially threaded agents. For instance when a GUI wants to discuss
	 * with its linked agent: This allows to enqueue work to do in their life cycle
	 *
	 * @param m
	 */
	void receiveMessage(Message m);

	Mailbox getMailbox();

	/**
	 * Called on the FX application thread
	 */
	void setupGUI();

	/**
	 * Returns the {@link SimulationTime} of the current simulation. This is
	 * automatically initialized when the agent is associated with an activator for
	 * the first time. So it stays <code>null</code> if the agent is not related to
	 * a simulation
	 *
	 * @return the simulationTime of the simulation in which the agent participates
	 */
	SimulationTimer<?> getSimulationTime();

	/**
	 * Method which is used by discrete-event simulation activators for doing
	 * fine-grained simulations. By default, this method returns an event which is
	 * one second ahead of the current date of the simulation. So, this method can
	 * be overridden to fulfill the simulation requirement
	 *
	 * @return the date of the next event for this agent.
	 *
	 * @see DiscreteEventAgentsActivator
	 * @see DateBasedDiscreteEventActivator
	 */
	LocalDateTime getNextEventDate();

	/**
	 * The kernel's address on which this agent is running.
	 *
	 * @return the kernel address representing the MaDKit kernel on which the agent
	 *         is running
	 */
	KernelAddress getKernelAddress();

	/**
	 * @return
	 */
	Message nextMessage();

}