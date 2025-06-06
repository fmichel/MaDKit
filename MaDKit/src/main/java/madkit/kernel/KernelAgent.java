/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.Agent.ReturnCode.TIMEOUT;

import javafx.application.Platform;
import javafx.stage.Window;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.SystemRoles;
import madkit.gui.FXExecutor;
import madkit.i18n.ErrorMessages;
import madkit.internal.FXInstance;
import madkit.messages.KernelMessage;
import madkit.random.Randomness;

/**
 * @since MaDKit 6.0
 *
 */
class KernelAgent extends Agent implements DaemonAgent {

	/** The kernel address. */
	final KernelAddress kernelAddress;

	/** The Constant deadKernel. */
	static final DeadKernel deadKernel = new DeadKernel();

	private AgentAddress netAgent;
	// my private addresses for optimizing the message building
	private AgentAddress netUpdater, netEmmiter, kernelRole;
	/////////////////////////////////// CGR
	private final Set<Overlooker> operatingOverlookers;
	private final Organization org;

	final Madkit madkit;

	final List<Agent> threadedAgents;

	private final AgentsExecutors agentExecutors;

	private boolean exitRequested = false;

	private RandomGenerator randomGenerator;

	private static Set<KernelAgent> kernerls = new HashSet<>();

	/**
	 * @return the madkit
	 */
	Madkit getMadkit() {
		return madkit;
	}

	/**
	 * @param madkit
	 */
	KernelAgent(Madkit madkit) {
		kernelAddress = new KernelAddress();
		agentExecutors = new AgentsExecutors(kernelAddress);
		operatingOverlookers = new LinkedHashSet<>();
		org = new Organization(this);
		this.madkit = madkit;
		logger = new AgentLogger(this);
		logger.setLevel(getKernelConfig().getLevel("kernelLogLevel"));
		threadedAgents = Collections.synchronizedList(new ArrayList<>());
		kernerls.add(this);
	}

	/**
	 * Instantiates a new kernel agent.
	 */
	KernelAgent() {// for alternative kernels
		kernelAddress = null;
		agentExecutors = null;
		operatingOverlookers = null;
		org = null;
		madkit = null;
		threadedAgents = null;
	}

	/**
	 * Gets the agent executor.
	 *
	 * @param a the a
	 * @return the agent executor
	 */
	Executor getAgentExecutor(Agent a) {
		return agentExecutors.getAgentExecutor(a);
	}

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
//			getLogger().setLevel(Level.ALL);
		if (GraphicsEnvironment.isHeadless()) {
			getKernelConfig().setProperty(MDKCommandLine.HEADLESS_MODE, true);
		}
		onCreateRandomGenerator();
		FXInstance.setHeadlessMode(getKernelConfig().getBoolean(MDKCommandLine.HEADLESS_MODE));
		FXInstance.startFX(getLogger());
		createGroup(LocalCommunity.NAME, Groups.SYSTEM, false, (_, _, _) -> {
			return false;
		});
		createGroup(LocalCommunity.NAME, "kernels", true);

		// // building the network group
		// createGroup(LocalCommunity.NAME, Groups.NETWORK, false);
		// requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL, null);
		// requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER, null);
		// requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER, null);

		launchConfigAgents();

		// my AAs cache
		// netUpdater = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER);
		// netEmmiter = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER);
		// kernelRole = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL);

		// myThread.setPriority(Thread.NORM_PRIORITY + 1);

		// if (loadLocalDemos.isActivated(getMadkitConfig())) {
		// GlobalAction.LOAD_LOCAL_DEMOS.actionPerformed(null);
		// }
		//
		// launchGuiManagerAgent();
		//
		// if (console.isActivated(getMadkitConfig())) {
		// launchAgent(new ConsoleAgent());
		// }
		// launchNetworkAgent();
		// logCurrentOrganization(logger,Level.FINEST);

	}

	/**
	 * On create random generator.
	 */
	void onCreateRandomGenerator() {
		int seedIndex = getKernelConfig().getInt("seed");
		seedIndex = seedIndex == Integer.MIN_VALUE ? new SecureRandom().nextInt(1_000) : seedIndex;
		long seed = 0xFEDCBA0987654321L + seedIndex;
		randomGenerator = Randomness.getBestRandomGeneratorFactory().create(seed);
		getLogger()
				.fine(" PRNG < " + randomGenerator.getClass().getSimpleName() + " ; seed index ->  " + seedIndex + " >");
	}

	/**
	 * 
	 */
	private void garbageDeadThreadedAgents() {
		synchronized (threadedAgents) {
			threadedAgents.removeIf(a -> a.kernel == deadKernel);
		}
	}

	private void handleMessage(Message message) {
		if (message instanceof KernelMessage m) {
			proceedEnumMessage(m);
			// } else if (m instanceof HookMessage) {
			// handleHookRequest((HookMessage) m);
			// } else if (m instanceof RequestRoleSecure) {
			// handleRequestRoleSecure((RequestRoleSecure) m);
		} else {
			if (message != null) {
				logger.warning(() -> "I received a message that I do not understand. Discarding " + message);
			}
		}
	}

	// @SuppressWarnings("unused")
	// private void console() {
	// launchAgent(ConsoleAgent.class.getName(), 0);
	// }

	/**
	 * main loop of the kernel. As a daemon, a timeout is not required
	 */
	@Override
	protected void onLive() {
		while (!exitRequested) {
			handleMessage(waitNextMessage(1000));
			garbageDeadThreadedAgents();
			if (threadedAgents.isEmpty() && FXExecutor.isStarted()
					&& FXAgentStage.getAgentsWithStage(kernelAddress).isEmpty()) {
				logIfLoggerNotNull(Level.FINE,
						() -> "No more activity within kernel " + getKernelAddress() + " -> Quitting");
				return;
			}
		}
	}

	/**
	 * Exit.
	 */
	void exit() {
		getLogger().fine(() -> "***** SHUTINGDOWN MADKIT ********\n");
		exitRequested = true;
		Collection<Agent> c = FXAgentStage.getAgentsWithStage(getKernelAddress());
		getLogger().finer(() -> "***** KILLING agent with stages " + c);
		c.forEach(a -> killAgent(a, 1));
		garbageDeadThreadedAgents();
		getLogger().finer(() -> "Killing agents -> " + threadedAgents);
		while (!threadedAgents.isEmpty()) {
			synchronized (threadedAgents) {
				threadedAgents.parallelStream().forEach(a -> killAgent(a, 1));
				garbageDeadThreadedAgents();
			}
		}
		kernerls.remove(this);
	}

	/**
	 * On end.
	 */
	@Override
	protected void onEnd() {
		kernerls.remove(this);
		if (kernerls.isEmpty()) {
			if (Window.getWindows().isEmpty()) {
				getLogger().finer(() -> "Shutdown down JavaFX platform");
				Platform.exit();
			}
		}
	}

	/**
	 * Launch agent.
	 *
	 * @param agent          the agent
	 * @param timeOutSeconds the time out seconds
	 * @return the return code
	 */
	@Override
	public ReturnCode launchAgent(Agent agent, int timeOutSeconds) {
		Objects.requireNonNull(agent);
		if (agent.kernel != null) {
			throw new IllegalArgumentException(agent + " ALREADY_LAUNCHED");
		}
		if (!exitRequested) {
			agent.kernel = this;
			CompletableFuture<ReturnCode> activationPromise = new CompletableFuture<>();
			agent.startAgentLifeCycle(activationPromise);
			try {
				return activationPromise.get(timeOutSeconds, TimeUnit.SECONDS);
			} catch (ExecutionException e) {
				return AGENT_CRASH;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AgentInterruptedException();
			} catch (TimeoutException e) {
				return TIMEOUT;
			}
		}
		return AGENT_CRASH;
	}

	/**
	 * Kill agent.
	 *
	 * @param a       the a
	 * @param seconds the seconds
	 * @return the return code
	 */
	@Override
	protected ReturnCode killAgent(Agent a, int seconds) {
		if (a.isThreaded()) {
			hardKillAgent(a, seconds);
		} else {
			CompletableFuture<Void> killing = CompletableFuture.runAsync(a::killed, a.getExecutor());
			try {
				killing.get(seconds, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException e) {
				logIfLoggerNotNull(Level.FINE, () -> a + " KILLED");
			} catch (TimeoutException e) {
				hardKillAgent(a, 1);
			}
		}
		return SUCCESS;
	}

	private void hardKillAgent(Agent a, int seconds) {
		Thread t = agentExecutors.getAgentThread(a);
		if (t == null) {
			return;
		}
		try {
			tryInterruption(a, seconds, t);
			if (a.kernel != deadKernel) {
				tryInterruption(a, seconds, t);
			}
		} catch (InterruptedException e) {
			logIfLoggerNotNull(Level.FINE, () -> a + " KILLED");
			Thread.currentThread().interrupt();
		}
		synchronized (a.alive) {
			if (a.kernel != deadKernel) {
				a.getLogger().finer(() -> "**** HARD KILLED ****");
				a.terminate();
			}
		}
	}

	/**
	 * @param a
	 * @param seconds
	 * @param t
	 * @throws InterruptedException
	 */
	private void tryInterruption(Agent a, int seconds, Thread t) throws InterruptedException {
		getLogger().fine(() -> "INTERRUPTING " + a);
		t.interrupt();
		synchronized (a.alive) {
			a.alive.wait(((long) seconds) * 1000);
		}
	}

	/**
	 * Gets the kernel config.
	 *
	 * @return the kernel config
	 */
	@Override
	public KernelConfig getKernelConfig() {
		return madkit.getConfig();
	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Organization interface
	// ////////////////////////////////////////////////////////////

	/**
	 * Creates the group.
	 *
	 * @param creator       the creator
	 * @param community     the community
	 * @param group         the group
	 * @param gatekeeper    the gatekeeper
	 * @param isDistributed the is distributed
	 * @return the return code
	 */
	ReturnCode createGroup(Agent creator, String community, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		Objects.requireNonNull(group, ErrorMessages.G_NULL.toString());
		return org.createGroup(creator, community, group, gatekeeper, isDistributed);
//		// no need to remove org: never failed
//		// will throw null pointer if community is null
//		Org organization = organizations.computeIfAbsent(community, o -> new Org(this));
//		synchronized (organization) {
//			if (!organization.addGroup(creator, group, gatekeeper, isDistributed)) {
//				return ALREADY_GROUP;
//			}
		////			try {// TODO bof...
////				if (isDistributed) {
////					sendNetworkMessageWithRole(new CGRSynchro(Code.CREATE_GROUP,
////							getRole(community, group, madkit.agr.SystemRoles.GROUP_MANAGER_ROLE)
////									.getAgentAddressOf(creator)),
////							netUpdater);
////				}
////				if (hooks != null) {
////					informHooks(AgentActionEvent.CREATE_GROUP,
////							getRole(community, group, madkit.agr.SystemRoles.GROUP_MANAGER_ROLE)
////									.getAgentAddressOf(creator));
////				}
////			} catch (CGRNotAvailable e) {
////				getLogger().severeLog("Please bug report", e);
////			}
//		}
//		return SUCCESS;
	}

	/**
	 * @param requester
	 * @param community
	 * @param memberCard
	 * @param roleName
	 * @param groupName
	 * @throws RequestRoleException
	 */

	ReturnCode requestRole(Agent requester, String community, String group, String role, Object memberCard) {
		try {
			ReturnCode result = org.requestRole(requester, community, group, role, memberCard);
//			System.err.println("\n"+result+"  "+requester+" "+community+","+group+","+role);
			return result;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
//	if (result == SUCCESS) {
//	    if (g.isDistributed()) {
//		sendNetworkMessageWithRole(new CGRSynchro(REQUEST_ROLE, new AgentAddress(requester, g.get(role), kernelAddress)), netUpdater);
//	    }
//	    if (hooks != null)
//		informHooks(AgentActionEvent.REQUEST_ROLE, new AgentAddress(requester, g.get(role), kernelAddress));
//	}
	}

	/**
	 * @param Agent
	 * @param communityName
	 * @param group
	 * @return
	 */

	ReturnCode leaveGroup(Agent requester, String community, String group) {
		try {
			Group g = getOrganization().getGroup(community, group);
			if (g.leaveGroup(requester)) {
//    			if (g.isDistributed()) {
//    				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(requester, new Role(community, group), kernelAddress)), netUpdater);
//    			}
//    			if (hooks != null)// should not be factorized to avoid useless object creation
//    				informHooks(AgentActionEvent.LEAVE_GROUP, new AgentAddress(requester, new Role(community, group), kernelAddress));
				return SUCCESS;
			} else {
				return NOT_IN_GROUP;
			}
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	// ////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Organization access
	/**
	 * Gets the organization.
	 *
	 * @return the organization
	 */
	// /////////////////////////////////////////////////////////////////////////
	@Override
	public Organization getOrganization() {
		return org;
	}

	/**
	 * Gets the operating overlookers.
	 *
	 * @return the operating overlookers
	 */
	Set<Overlooker> getOperatingOverlookers() {
		return operatingOverlookers;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Messaging
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Send message.
	 *
	 * @param requester  the requester
	 * @param receiver   the receiver
	 * @param message    the message
	 * @param senderRole the sender role
	 * @return the return code
	 */
	ReturnCode sendMessage(Agent requester, AgentAddress receiver, Message message, String senderRole) {
		// check that the AA is valid : the targeted agent is still playing the
		// corresponding role or it was a candidate request
		AgentAddress target = getActualAddress(receiver);
		if (target == null && !(receiver instanceof CandidateAgentAddress)) {
			return INVALID_AGENT_ADDRESS;
		}
		try {
			// get the role for the sender and then send
			return buildAndSendMessage(getSenderAgentAddress(requester, target, senderRole), target, message);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	/**
	 * Broadcast message with role.
	 *
	 * @param requester     the requester
	 * @param receivers     the receivers
	 * @param messageToSend the message to send
	 * @param senderRole    the sender role
	 * @return the return code
	 */
	ReturnCode broadcastMessageWithRole(Agent requester, List<AgentAddress> receivers, Message messageToSend,
			String senderRole) {
		try {
			AgentAddress senderAgentAddress = getSenderAgentAddress(requester, receivers.get(0), senderRole);
			messageToSend.setSender(senderAgentAddress);
			broadcasting(receivers, messageToSend);
//			if (hooks != null) {
//				messageToSend.setReceiver(receivers.get(0));
//    		informHooks(AgentActionEvent.BROADCAST_MESSAGE, messageToSend.clone());
//			}
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	private void broadcasting(Collection<AgentAddress> receivers, Message m) {
		receivers.stream().forEach(agentAddress -> {
			Message cm = m.clone();
			cm.setReceiver(agentAddress);
			deliverMessage(cm, agentAddress.getAgent());
		});
	}

	/**
	 * Builds the and send message.
	 *
	 * @param sender   the sender
	 * @param receiver the receiver
	 * @param m        the m
	 * @return the return code
	 */
	ReturnCode buildAndSendMessage(AgentAddress sender, AgentAddress receiver, Message m) {
		m.setSender(sender);
		m.setReceiver(receiver);
		ReturnCode r = deliverMessage(m, receiver.getAgent());
//	if (r == SUCCESS && hooks != null) {
//	    informHooks(AgentActionEvent.SEND_MESSAGE, m);
//	}
		return r;
	}

	private ReturnCode deliverMessage(Message m, Agent target) {
		if (target == null) {
//			m.getConversationID().setOrigin(kernelAddress);
//			return sendNetworkMessageWithRole(new ObjectMessage<>(m), netEmmiter);
		}
		target.receiveMessage(m);
		return SUCCESS;
	}

	/**
	 * Gets the actual address.
	 *
	 * @param receiver the receiver
	 * @return the actual address
	 */
	final AgentAddress getActualAddress(AgentAddress receiver) {
		Role roleObject = receiver.getRoleObject();
		if (roleObject != null) {
			if (roleObject.players == null) {// has been traveling
				return roleObject.resolveDistantAddress(receiver);
			}
			return receiver;
		}
		return null;
	}

	/**
	 * Gets the sender agent address.
	 *
	 * @param sender     the sender
	 * @param receiver   the receiver
	 * @param senderRole the sender role
	 * @return the sender agent address
	 * @throws CGRNotAvailable the CGR not available
	 */
	final AgentAddress getSenderAgentAddress(Agent sender, AgentAddress receiver, String senderRole)
			throws CGRNotAvailable {
		AgentAddress senderAA = null;
		Role targetedRole = receiver.getRoleObject();
		// no role given
		if (senderRole == null) {
			// looking for any role in this group, starting with the receiver role
			senderAA = targetedRole.getAgentAddressInGroup(sender);
			// if still null : this SHOULD be a candidate's request to the manager or an
			// error
			if (senderAA == null) {
				if (targetedRole.getName().equals(SystemRoles.GROUP_MANAGER)) {
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				}
				throw new CGRNotAvailable(NOT_IN_GROUP);
			}
			return senderAA;
		}

		// sent with a particular role : check that
		// look into the senderRole role if the agent is in
		Role senderRoleObject;
		try {
			senderRoleObject = targetedRole.getGroup().getRole(senderRole);
			senderAA = senderRoleObject.getAgentAddressOf(sender);
		} catch (CGRNotAvailable e) {
			// candidate's request to the manager or it is an error
			if (senderRole.equals(SystemRoles.GROUP_CANDIDATE)
					&& targetedRole.getName().equals(SystemRoles.GROUP_MANAGER)) {
				return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
			}
		}
		if (senderAA == null) {// if still null :
			if (targetedRole.getAgentAddressInGroup(sender) == null) {
				throw new CGRNotAvailable(NOT_IN_GROUP);
			}
			throw new CGRNotAvailable(ROLE_NOT_HANDLED);
		}
		return senderAA;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// STARTUP
	// /////////////////////////////////////////////////////////////////////////

	private void launchConfigAgents() {
		for (String classNameAndOption : madkit.getConfig().getList(String.class, "agents")) {
			if (classNameAndOption.equals("null")) {
				return;
			}
			String[] classAndOptions = classNameAndOption.split(",");
			String className = classAndOptions[0].trim();// TODO should test if these classes exist
			int number = 1;
			if (classAndOptions.length > 1) {
				try {
					number = Integer.parseInt(classAndOptions[1].trim());
				} catch (NumberFormatException e) {
//				getLogger().severeLog(
//					ErrorMessages.OPTION_MISUSED.toString() + Option.launchAgents.toString() + " " + agentsTolaunch + " " + e.getClass().getName() + " !!!\n", null);
				}
			}
			if (logger != null) {
				logger.finer("Launching " + number + " instance(s) of " + className);
			}
			try {
				Class<?> agentClass = MadkitClassLoader.getLoader().loadClass(className);
				for (int i = 0; i < number; i++) {
					// if (! shuttedDown) {
					Agent newInstance = (Agent) agentClass.getConstructor().newInstance();
					launchAgent(newInstance, 0);
					threadedAgents.add(newInstance);
					// }
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}

		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Internal functioning
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Removes the community.
	 *
	 * @param community the community
	 */
	void removeCommunity(String community) {
		org.removeCommunity(community);
	}

	/**
	 * @param agent
	 * @param community
	 * @param group
	 * @param role
	 * @return
	 */
	ReturnCode leaveRole(Agent agent, String community, String group, String role) {
		try {
			ReturnCode rc = getOrganization().getRole(community, group, role).removeMember(agent);
			if (rc == SUCCESS) {
//				if (hooks != null) {
//					informHooks(AgentActionEvent.LEAVE_ROLE, new AgentAddress(requester, r, kernelAddress));
//				}
			}
			return rc;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

//	@SuppressWarnings("unused")
//	private void console() {
//		launchAgent(ConsoleAgent.class.getName(), 0);
//	}

	private void copy() {
		getMadkit().startNewSession();
	}

	@SuppressWarnings("unused")
	private void restart() {
		copy();
		exit();
	}

	/**
	 * Gets the prng.
	 *
	 * @return the prng
	 */
	public RandomGenerator getPRNG() {
		return randomGenerator;
	}

}

final class DeadKernel extends KernelAgent {

}