/*******************************************************************************
 * Copyright (c) 1997, 2021, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.Agent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.Agent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.Agent.ReturnCode.TIMEOUT;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import madkit.agr.DefaultMaDKitRoles;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.ConsoleAgent;
import madkit.gui.fx.FXManager;
import madkit.i18n.ErrorMessages;
import madkit.messages.KernelMessage;

/**
 * 
 * 
 * @author Fabien Michel
 * @since MaDKit 6.0
 * @version 1.0
 *
 */
class KernelAgent extends Agent implements DaemonAgent{

	final KernelAddress kernelAddress;

	final static DeadKernel deadKernel = new DeadKernel();

	private AgentAddress netAgent;
	// my private addresses for optimizing the message building
	private AgentAddress netUpdater, netEmmiter, kernelRole;
	/////////////////////////////////// CGR
	private final Set<Overlooker> operatingOverlookers;
//	private final ConcurrentHashMap<String, Organization> organizations;
	private final Organization org;

	final Madkit madkit;

	final List<Agent> threadedAgents;

	private final AgentsExecutors agentExecutors;

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
//		organizations = new ConcurrentHashMap<>();
		operatingOverlookers = new LinkedHashSet<>();
		org = new Organization(this);
		this.madkit = madkit;
		logger = new AgentLogger(this);
		logger.setLevel(getKernelConfig().getLevel("kernelLogLevel"));
		threadedAgents = Collections.synchronizedList(new ArrayList<>());
//		logger.setLevel(Level.ALL);
	}

	KernelAgent() {// for alternative kernels
		kernelAddress = null;
		agentExecutors = null;
		operatingOverlookers = null;
		org = null;
		madkit = null;
		threadedAgents = null;
	}

	Executor getAgentExecutor(Agent a) {
		return agentExecutors.getAgentExecutor(a);
	}

	private void handleMessage(Message message) {
		if (message instanceof KernelMessage m) {
			proceedEnumMessage(m);
//		} else if (m instanceof HookMessage) {
//			handleHookRequest((HookMessage) m);
//		} else if (m instanceof RequestRoleSecure) {
//			handleRequestRoleSecure((RequestRoleSecure) m);
		} else {
			if (logger != null)
				logger.warning(() -> "I received a message that I do not understand. Discarding " + message);
		}
	}

	@Override
	protected void onLiving() {
//		getLogger().info(threadFactory.toString());
		while (true) {
			handleMessage(waitNextMessage());// As a daemon, a timeout is not required
		}
//		while (true) {
//			waitNextMessage(3000);
//			getLogger().info("alive");
//			getLogger().info("agents thread -------------------> " + threadFactory.toString());
////			getLogger().info("daemons thread -------------------> " + daemonThreadFactory.toString());
//		}
	}

	@Override
	protected void onActivation() {
//		getLogger().setLevel(Level.ALL);
		FXManager.setHeadlessMode(getKernelConfig().getBoolean("headless") || GraphicsEnvironment.isHeadless());
		FXManager.startFX();
//		GraphicsEnvironment.getLocalGraphicsEnvironment();
//		getLogger().info(() -> "headless graphics environment-> "+GraphicsEnvironment.isHeadless());
		createGroup(LocalCommunity.NAME, Groups.SYSTEM, false, (req, ro, card) -> {
			return false;
		});
		createGroup(LocalCommunity.NAME, "kernels", true);// TODO

		// building the network group
		createGroup(LocalCommunity.NAME, Groups.NETWORK, false);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER, null);

		launchConfigAgents();
		
		// my AAs cache
//		netUpdater = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER);
//		netEmmiter = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER);
//		kernelRole = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL);

//		myThread.setPriority(Thread.NORM_PRIORITY + 1);

//		if (loadLocalDemos.isActivated(getMadkitConfig())) {
//		    GlobalAction.LOAD_LOCAL_DEMOS.actionPerformed(null);
//		}
//
//		launchGuiManagerAgent();
//
//		if (console.isActivated(getMadkitConfig())) {
//		    launchAgent(new ConsoleAgent());
//		}
//		launchNetworkAgent();
		// logCurrentOrganization(logger,Level.FINEST);

//		javax.swing.Action b = GlobalAction.JCONSOLE.getSwingAction();
//		b.actionPerformed(null);
//		Object o = null;		o.toString();
	}

	@Override
	protected ReturnCode launchAgent(final Agent agent, final int timeOutSeconds) {
		Objects.requireNonNull(agent);
		if (agent.kernel != null) {
			throw new IllegalArgumentException(agent+" ALREADY_LAUNCHED");
		}
		agent.kernel = this;
		CompletableFuture<ReturnCode> activationPromise = new CompletableFuture<>();
		agent.startAgentLifeCycle(activationPromise);
		try {
			return activationPromise.get(timeOutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return AGENT_CRASH;
		} catch (TimeoutException e) {
			return TIMEOUT;
		}
	}

	@Override
	protected ReturnCode killAgent(Agent a, int seconds) {
		if (a.isThreaded()) {
			hardKillAgent(a, seconds);
		} else {
			CompletableFuture<Void> killing = CompletableFuture.runAsync(() -> {
				a.killed();
			}, a.getExecutor());
			try {
				killing.get(seconds, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				hardKillAgent(a, 1);
			}
		}
		return SUCCESS;
	}

	private void hardKillAgent(Agent a, int seconds) {
		Thread t = agentExecutors.getAgentThread(a);
		if (t == null)// FIXME may not scale
			return;
		try {
			tryInterruption(a, seconds, t);
			if (a.kernel != deadKernel) {
				tryInterruption(a, seconds, t);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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

	@Override
	public KernelConfig getKernelConfig() {
		return madkit.getConfig();
	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Organization interface
	// ////////////////////////////////////////////////////////////

	ReturnCode createGroup(final Agent creator, final String community, final String group, final Gatekeeper gatekeeper,
			final boolean isDistributed) {
		Objects.requireNonNull(group, ErrorMessages.G_NULL.toString());
		return org.createGroup(creator, community, group, gatekeeper, isDistributed);
//		// no need to remove org: never failed
//		// will throw null pointer if community is null
//		final Org organization = organizations.computeIfAbsent(community, o -> new Org(this));
//		synchronized (organization) {
//			if (!organization.addGroup(creator, group, gatekeeper, isDistributed)) {
//				return ALREADY_GROUP;
//			}
////			try {// TODO bof...
////				if (isDistributed) {
////					sendNetworkMessageWithRole(new CGRSynchro(Code.CREATE_GROUP,
////							getRole(community, group, madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE)
////									.getAgentAddressOf(creator)),
////							netUpdater);
////				}
////				if (hooks != null) {
////					informHooks(AgentActionEvent.CREATE_GROUP,
////							getRole(community, group, madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE)
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

	ReturnCode leaveGroup(final Agent requester, final String community, final String group) {
		try {
			Group g = getOrgnization().getGroup(community, group);
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
	// /////////////////////////////////////////////////////////////////////////
	@Override
	public Organization getOrgnization() {
		return org;
	}

//    final Role getRole(final String community, final String group, final String role) throws CGRNotAvailable {
//	Role r = getGroup(community, group).get(role);
//	if (r == null)
//	    throw new CGRNotAvailable(NOT_ROLE);
//	return r;
//    }

	Set<Overlooker> getOperatingOverlookers() {
		return operatingOverlookers;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Messaging
	// /////////////////////////////////////////////////////////////////////////

	ReturnCode sendMessage(Agent requester, AgentAddress receiver, final Message message, final String senderRole) {
		// check that the AA is valid : the targeted agent is still playing the
		// corresponding role or it was a candidate request
		// if (! receiver.exists()) {// && !
		final AgentAddress target = getActualAddress(receiver);
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

	ReturnCode broadcastMessageWithRole(final Agent requester, final List<AgentAddress> receivers,
			final Message messageToSend, String senderRole) {
		try {
			final AgentAddress senderAgentAddress = getSenderAgentAddress(requester, receivers.get(0), senderRole);
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

	private void broadcasting(final Collection<AgentAddress> receivers, final Message m) {
		receivers.parallelStream().forEach(agentAddress -> {
			final Message cm = m.clone();
			cm.setReceiver(agentAddress);
			deliverMessage(cm, agentAddress.getAgent());
		});
	}

	ReturnCode buildAndSendMessage(final AgentAddress sender, final AgentAddress receiver, final Message m) {
		m.setSender(sender);
		m.setReceiver(receiver);
		final ReturnCode r = deliverMessage(m, receiver.getAgent());
//	if (r == SUCCESS && hooks != null) {
//	    informHooks(AgentActionEvent.SEND_MESSAGE, m);
//	}
		return r;
	}

	private final ReturnCode deliverMessage(Message m, Agent target) {
		if (target == null) {
//			m.getConversationID().setOrigin(kernelAddress);
//			return sendNetworkMessageWithRole(new ObjectMessage<>(m), netEmmiter);
		}
		target.receiveMessage(m);
		return SUCCESS;
	}

	final AgentAddress getActualAddress(AgentAddress receiver) {
		final Role roleObject = receiver.getRoleObject();
		if (roleObject != null) {
			if (roleObject.players == null) {// has been traveling
				return roleObject.resolveDistantAddress(receiver);
			}
			return receiver;
		}
		return null;
	}

	final AgentAddress getSenderAgentAddress(final Agent sender, final AgentAddress receiver, String senderRole)
			throws CGRNotAvailable {
		AgentAddress senderAA = null;
		final Role targetedRole = receiver.getRoleObject();
		// no role given
		if (senderRole == null) {
			// looking for any role in this group, starting with the receiver role
			senderAA = targetedRole.getAgentAddressInGroup(sender);
			// if still null : this SHOULD be a candidate's request to the manager or an
			// error
			if (senderAA == null) {
				if (targetedRole.getName().equals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
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
			if (senderRole.equals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE)
					&& targetedRole.getName().equals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE))
				return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
		}
		if (senderAA == null) {// if still null :
			if (targetedRole.getAgentAddressInGroup(sender) == null)
				throw new CGRNotAvailable(NOT_IN_GROUP);
			throw new CGRNotAvailable(ROLE_NOT_HANDLED);
		}
		return senderAA;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// STARTUP
	// /////////////////////////////////////////////////////////////////////////

	private void launchConfigAgents() {
		for (String classNameAndOption : madkit.getConfig().getList(String.class, "agents")) {
			if (classNameAndOption.equals("null"))
				return;
			final String[] classAndOptions = classNameAndOption.split(",");
			final String className = classAndOptions[0].trim();// TODO should test if these classes exist
			int number = 1;
			if (classAndOptions.length > 1) {
				try {
					number = Integer.parseInt(classAndOptions[1].trim());
				} catch (NumberFormatException e) {
//				getLogger().severeLog(
//					ErrorMessages.OPTION_MISUSED.toString() + Option.launchAgents.toString() + " " + agentsTolaunch + " " + e.getClass().getName() + " !!!\n", null);
				}
			}
			if (logger != null)
				logger.finer("Launching " + number + " instance(s) of " + className);
			try {
				final Class<?> agentClass = MadkitClassLoader.getLoader().loadClass(className);
				for (int i = 0; i < number; i++) {
					// if (! shuttedDown) {
					launchAgent((Agent) agentClass.getConstructor().newInstance(), 0);
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
			ReturnCode rc = getOrgnization().getRole(community, group, role).removeMember(agent);
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

	@SuppressWarnings("unused")
	private void console() {
		launchAgent(ConsoleAgent.class.getName(), 0);
	}

	private void exit() {
		threadedAgents.parallelStream().forEach(a -> killAgent(a, 2));
		getLogger().fine(() -> "***** SHUTINGDOWN MY KERNEL ********\n");
//		System.exit(0);//FIXME
	}

	private void copy() {
		startSession(true);
	}

	private void startSession(final boolean externalVM) {
//		String[] args = getKernelConfiguration().get(String[].class, "CMD_LINE");
//		Class<?> launcherClass = getMadkit().launcherClass;
		
		String[] args = getMadkit().getLauncherArgs();
		Class<?> launcherClass = getMadkit().getOneFileLauncherClass();
		
		if (logger != null)
			logger.config(() -> "starting new MaDKit session with " + Arrays.deepToString(args));// +
																																// Arrays.deepToString(getKernelConfiguration().get(String[].class,
		if (externalVM) {
			try {
				String command = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar
						+ "java --add-modules ALL-MODULE-PATH -p " + System.getProperty("jdk.module.path") + " -m "
						+ launcherClass.getModule().getName() + '/' + launcherClass.getName();
				for (int i = 0; i < args.length; i++) {
					command += " " + args[i];
				}
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			final Thread t = new Thread(() -> {
				try {
					Method c = getMadkit().getLauncherClassMainMethod();
					c.invoke(null, (Object) getMadkit().startingArgs);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				new Madkit(getMadkit().startingArgs);
				
//				try {
//					if(getMadkit().oneFileLauncher != null) {
//						Method m = launcherClass.getMethod("main", String[].class);
//						m.invoke(null, (Object) args);
//					} else {
//						Constructor<? extends Madkit> cons = (Constructor<? extends Madkit>) launcherClass.getConstructor(String[].class);
//						cons.newInstance((Object) args);
//					}
//				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
//						| IllegalArgumentException | InvocationTargetException e) {
//					e.printStackTrace();
//				}
			});
			t.setDaemon(false);
			t.start();
		}
	}

	@SuppressWarnings("unused")
	private void restart() {
		startSession(false);
		pause(500);
		exit();
	}

}

final class DeadKernel extends KernelAgent {

}