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

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_ARG;
import static madkit.kernel.AbstractAgent.ReturnCode.LAUNCH_TIME_OUT;
import static madkit.kernel.AbstractAgent.ReturnCode.NETWORK_DOWN;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_STRING;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SEVERE;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.ACTIVATED;
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.NOT_LAUNCHED;
import static madkit.kernel.CGRSynchro.Code.CREATE_GROUP;
import static madkit.kernel.CGRSynchro.Code.LEAVE_GROUP;
import static madkit.kernel.CGRSynchro.Code.LEAVE_ROLE;
import static madkit.kernel.CGRSynchro.Code.REQUEST_ROLE;
import static madkit.kernel.Madkit.Roles.GUI_MANAGER_ROLE;
import static madkit.kernel.Madkit.Roles.KERNEL_ROLE;
import static madkit.kernel.Madkit.Roles.LOCAL_COMMUNITY;
import static madkit.kernel.Madkit.Roles.NETWORK_GROUP;
import static madkit.kernel.Madkit.Roles.NETWORK_ROLE;
import static madkit.kernel.Madkit.Roles.SYSTEM_GROUP;

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import madkit.gui.GUIManagerAgent;
import madkit.gui.GUIMessage;
import madkit.gui.MadkitActions;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;
import madkit.messages.ObjectMessage;

/**
 * The brand new MadKit kernel and it is now a real Agent :)
 * 
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 * 
 */
class MadkitKernel extends Agent {

	final static ExecutorService serviceExecutor = Executors.newCachedThreadPool();
	final static Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();
	static {
		primitiveTypes.put("java.lang.Integer", int.class);
		primitiveTypes.put("java.lang.Boolean", boolean.class);
		primitiveTypes.put("java.lang.Byte", byte.class);
		primitiveTypes.put("java.lang.Character", char.class);
		primitiveTypes.put("java.lang.Float", float.class);
		primitiveTypes.put("java.lang.Void", void.class);
		primitiveTypes.put("java.lang.Short", short.class);
		primitiveTypes.put("java.lang.Double", double.class);
		primitiveTypes.put("java.lang.Long", long.class);
	}

	static {
		final ThreadPoolExecutor t = (ThreadPoolExecutor) serviceExecutor;
		t.setCorePoolSize(Runtime.getRuntime().availableProcessors() + 1);
		t.setThreadFactory(new ThreadFactory() {

			public Thread newThread(Runnable r) {
				final Thread t = new Thread(r);
				t.setPriority(Thread.MAX_PRIORITY);
				t.setName("MK_EXECUTOR");
				t.setDaemon(true);
				return t;
			}
		});
	}

	private final ConcurrentHashMap<String, Organization> organizations;
	final private Set<Overlooker<? extends AbstractAgent>> operatingOverlookers;
	final private Madkit platform;
	final private KernelAddress kernelAddress;

	// private MadKitGUIsManager guiManager;
	protected LoggedKernel loggedKernel;
	private boolean shuttedDown = false;
	private Level defaultAgentLogLevel;
	private Level defaultWarningLogLvl;
	final AgentThreadFactory normalAgentThreadFactory;
	final AgentThreadFactory daemonAgentThreadFactory;

	private AgentAddress netAgent;
	// my private addresses for optimizing the message building
	private AgentAddress netUpdater, netEmmiter, kernelRole;
	private Set<AbstractAgent> threadedAgents;

	MadkitKernel(Madkit m) {
		super(true);
		platform = m;
		if (m != null) {
//			setLogLevel(Level.ALL);
			threadedAgents = new HashSet<AbstractAgent>(20);
			kernelAddress = platform.getPlatformID();
			setDefaultAgentLogLevel(Level.parse(platform.getConfigOption().getProperty(Madkit.agentLogLevel)),
					Level.parse(platform.getConfigOption().getProperty(Madkit.warningLogLevel)));
			organizations = new ConcurrentHashMap<String, Organization>();
			operatingOverlookers = new LinkedHashSet<Overlooker<? extends AbstractAgent>>();
			normalAgentThreadFactory = new AgentThreadFactory("MKRA" + kernelAddress, false);
			daemonAgentThreadFactory = new AgentThreadFactory("MKDA" + kernelAddress, true);
			loggedKernel = new LoggedKernel(this);
			// launchingAgent(this, this, false);
		} else {
			kernelAddress = null;
			organizations = null;
			operatingOverlookers = null;
			normalAgentThreadFactory = null;
			daemonAgentThreadFactory = null;
		}
	}

	MadkitKernel(MadkitKernel k) {
		organizations = k.organizations;
		kernelAddress = k.kernelAddress;
		operatingOverlookers = k.operatingOverlookers;
		platform = k.platform;
		normalAgentThreadFactory = k.normalAgentThreadFactory;
		daemonAgentThreadFactory = k.daemonAgentThreadFactory;
	}

	@Override
	protected void activate() {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				System.err.println(this+"bye");
			}
		});
		createGroup(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, false);
		createGroup(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, false);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, Roles.KERNEL_ROLE, null);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "emmiter", null);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "updater", null);

		// black magic here
		myThread.setPriority(Thread.MAX_PRIORITY - 3);
		try {
			netUpdater = getRole(LOCAL_COMMUNITY, NETWORK_GROUP, "updater").getAgentAddressOf(this);
			netEmmiter = getRole(LOCAL_COMMUNITY, NETWORK_GROUP, "emmiter").getAgentAddressOf(this);
			kernelRole = getRole(LOCAL_COMMUNITY, SYSTEM_GROUP, KERNEL_ROLE).getAgentAddressOf(this);
		} catch (CGRNotAvailable e) {
			throw new AssertionError("Kernel Agent initialization problem");
		}

		launchGuiManagerAgent();
		launchNetworkAgent();
		Message m = nextMessage();// In activate only MadKit can feed my mailbox
		while (m != null) {
			handleMessage(m);
			m = waitNextMessage(100);
		}
		// logCurrentOrganization(logger,Level.FINEST);
//		try {
//			platform.getMadkitClassLoader().addJar(new URL("http://www.madkit.net/demonstration/repo/market.jar"));
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	final private void launchGuiManagerAgent() { 
		if (Boolean.parseBoolean(platform.getConfigOption().getProperty(Madkit.noGUIManager))) {
			if (logger != null)
				logger.fine("** No GUI Manager: " + Madkit.noGUIManager + " option is true**\n");
		} else {
			launchAgent(new GUIManagerAgent(! Boolean.parseBoolean(platform.getConfigOption().getProperty("desktop"))));
			if(logger != null)
				logger.fine("\n\t****** GUI Manager launched ******\n");
		}
	}

	final private void handleKernelMessage(KernelMessage km) {
		Method operation = null;
		final Object[] arguments = km.getContent();
		switch (km.getCode()) {
		case AGENT_LAUNCH_AGENT:// TODO semantic
			operation = launchAgent(arguments);
			break;
		case MADKIT_KILL_AGENTS:// TODO semantic
			killThreadedAgents();
			return;
		case CONNECT_WEB_REPO:
			try {
//				URL url = new URL("http://www.madkit.net/MadKit-"+getMadkitProperty("version")+"/repo.properties");
				String repoLocation = "http://www.madkit.net/repository/MadKit-5.0.0.9/";
				URL url = new URL(repoLocation+"repo.properties");
				Properties p = new Properties();
				p.load(url.openStream());
				System.err.println(p);
				for (Entry<Object, Object> object : p.entrySet()) {
//					platform.getMadkitClassLoader().addJar(new URL(repoLocation+object.getKey()+".jar"));
					platform.getMadkitClassLoader().addJar(new URL(repoLocation+object.getValue()+"/"+object.getKey()+".jar"));
				}
				sendReply(km, new Message());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		case MADKIT_LOAD_JAR_FILE:// TODO semantic
			System.err.println((URL) km.getContent()[0]);
			platform.getMadkitClassLoader().addJar((URL) km.getContent()[0]);
			sendReply(km, new Message());
			return;
		case MADKIT_LAUNCH_NETWORK:// TODO semantic
			startNetwork();
			return;
		case MADKIT_STOP_NETWORK:// TODO semantic
			stopNetwork();
			return;
		case MADKIT_CLONE:// TODO semantic
			startSession((Boolean) km.getContent()[0]);
			return;
		case MADKIT_RESTART:
			restartSession(100);
		case MADKIT_EXIT_ACTION:
			shutdownn();
			return;
		default:
			getLogger().warning("I received a kernel message that I do not understand. Discarding " + km);
			return;
		}
		doOperation(operation, arguments);
	}

	private void killThreadedAgents() {
		threadedAgents.remove(this);
		for(AbstractAgent a : threadedAgents){
			if (! a.getName().contains("GUIManagerAgent")) {
				killAgent(a,0);
			}
		}
//		Thread[] agents = new Thread[normalAgentThreadFactory.getThreadGroup().activeCount()];
//		normalAgentThreadFactory.getThreadGroup().enumerate(agents);
//		for (Thread thread : agents) {
//				thread.interrupt();
//			}
//		}
	}

	private void startSession(boolean externalVM) {
		if (logger != null)
			logger.info("starting MadKit session");
		if (externalVM) {
			try {
				Runtime.getRuntime().exec(platform.cmdLine);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			new Madkit(platform.args);
		}
	}

	private void stopNetwork() {
		ReturnCode r = sendNetworkMessageWithRole(new Message(), kernelRole);
		if (r == SUCCESS) {
			getLogger().fine("\n\t****** Network agent stopped ******\n");
		}// TODO i18n
		else {
			getLogger().fine("\n\t****** Network already down ******\n");
		}
	}

	private void startNetwork() {//TODO never use getLogger
		updateNetworkAgent();
		if (netAgent == null) {
			ReturnCode r = launchAgent(new NetworkAgent());
			if (r == SUCCESS) {
				getLogger().fine("\n\t****** Network agent launched ******\n");
			}// TODO i18n
			else {
				getLogger().severe("\n\t****** Problem launching network agent ******\n");
			}
		} else {
			getLogger().fine("\n\t****** Network agent already up ******\n");
		}
	}

	private void restartSession(final int time) {
		new Thread() {
			public void run() {
				pause(time);
				// for (Object s : new TreeSet(System.getProperties().keySet())) {
				// System.err.println(s+" = "+System.getProperty((String) s));
				// }
				new Madkit(platform.args);
				// try {
				// Process p = Runtime.getRuntime().exec(platform.cmdLine);
				// pause(10000);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}.start();
	}

	/**
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while (true) {
			handleMessage(waitNextMessage());
		}
	}

	private void handleMessage(Message m) {
		if (m instanceof KernelMessage) {
			handleKernelMessage((KernelMessage) m);
		} else {
			getLogger().warning("I received a message that I do not understand. Discarding " + m);
		}
	}

	/**
	 * @param operation
	 * @param arguments
	 */
	private void doOperation(Method operation, Object[] arguments) {
		try {// TODO log failures
			operation.invoke(this, arguments);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param content
	 * @return
	 */
	private Method launchAgent(Object[] content) {
		return checkValidity("launchAgent", content);
	}

	private Method checkValidity(String method, Object[] content) {
		Class<?>[] parameters = new Class<?>[content.length];
		for (int i = 0; i < content.length; i++) {
			parameters[i] = content[i].getClass();
			final Class<?> primitive = primitiveTypes.get(parameters[i].getName());
			if (primitive != null)
				parameters[i] = primitive;
		}
		try {// TODO log failures
			return getClass().getMethod(method, parameters);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	private AbstractAgent launchPlatformAgent(String mkProperty, String userMessage) {
		final String agentClassName = getMadkitProperty(mkProperty);
		if (logger != null) {
			logger.fine("** Launching " + userMessage + ": " + agentClassName + " **");
		}
		AbstractAgent targetAgent = launchAgent(agentClassName);
		if (targetAgent == null) {
			if (logger != null) {
				logger.warning("Problem building " + userMessage + " " + agentClassName + " -> Using MK default " + userMessage
						+ " : " + Madkit.defaultConfig.get(mkProperty));
			}
			return launchAgent(Madkit.defaultConfig.getProperty(mkProperty));
		}
		return targetAgent;
	}

	private void launchNetworkAgent() {
		if (Boolean.parseBoolean(getMadkitProperty(Madkit.network))) {
			startNetwork();
		} else {
			getLogger().fine("** Networking is off: No Net Agent **\n");
		}
	}

	// /////////////////////////////////////: Kernel Part

	/**
	 * @return the loggedKernel
	 */
	final LoggedKernel getLoggedKernel() {
		return loggedKernel;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Agent interface
	// /////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Organization interface
	// ////////////////////////////////////////////////////////////

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final String description,
			final GroupIdentifier theIdentifier, final boolean isDistributed) {
		if (community == null || group == null) {
			return NULL_STRING;
		}
		// no need to remove org: never failed
		Organization organization = new Organization(community, this);
		final Organization tmpOrg = organizations.putIfAbsent(community, organization);
		if (tmpOrg != null) {
			organization = tmpOrg;
		}
		if (!organization.createGroup(creator, group, theIdentifier, isDistributed)) {
			return ALREADY_GROUP;
		}
		if (isDistributed) {
			try {
				sendNetworkMessageWithRole(new CGRSynchro(CREATE_GROUP, getRole(community, group, Roles.GROUP_MANAGER_ROLE)
						.getAgentAddressOf(creator)), netUpdater);
			} catch (CGRNotAvailable e) {
				e.printStackTrace();
				kernelLog("Please bug report", Level.SEVERE, e);
			}
		}
		return SUCCESS;
	}

	/**
	 * @param requester
	 * @param roleName
	 * @param groupName
	 * @param community
	 * @param memberCard
	 * @throws RequestRoleException
	 */

	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		final Group g;
		try {
			g = getGroup(community, group);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		final ReturnCode result = g.requestRole(requester, role, memberCard);
		if (g.isDistributed() && result == SUCCESS) {
			sendNetworkMessageWithRole(new CGRSynchro(REQUEST_ROLE, g.get(role).getAgentAddressOf(requester)), netUpdater);
		}
		return result;
	}

	/**
	 * @param abstractAgent
	 * @param communityName
	 * @param group
	 * @return
	 */

	ReturnCode leaveGroup(final AbstractAgent requester, final String community, final String group) {
		final Group g;
		try {
			g = getGroup(community, group);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		final ReturnCode result = g.leaveGroup(requester);
		if (g.isDistributed() && result == SUCCESS) {
			sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(requester, new Role(community, group),
					kernelAddress)), netUpdater);
		}
		return result;
	}

	/**
	 * @param abstractAgent
	 * @param community
	 * @param group
	 * @param role
	 * @return
	 */

	ReturnCode leaveRole(AbstractAgent requester, String community, String group, String role) {
		final Role r;
		try {
			r = getRole(community, group, role);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		if (r.getMyGroup().isDistributed()) {
			AgentAddress leaver = r.getAgentAddressOf(requester);
			if (leaver == null)
				return ReturnCode.ROLE_NOT_HANDLED;
			if (r.removeMember(requester) != SUCCESS)
				throw new AssertionError("cannot remove " + requester + " from " + r.getAgentAddresses());
			return sendNetworkMessageWithRole(new CGRSynchro(LEAVE_ROLE, leaver), netUpdater);
		}
		return r.removeMember(requester);
		// final Role r;
		// try {
		// // g = getGroup(community, group);
		// r = getRole(community,group,role);
		// } catch (CGRNotAvailable e) {
		// return e.getCode();
		// }
		// Group g = r.getMyGroup();
		// //must do that before remove, in case the group disappears
		// boolean distributed = netAgent != null && g.isDistributed();
		// final ReturnCode result = r.removeMember(requester);
		// if(distributed && result == SUCCESS) {
		// sendNetworkMessage(new CGRSynchro(CGRSynchro.LEAVE_ROLE,
		// r.getAgentAddressOf(requester)));
		// }
		// return result;
	}

	// Warning never touch this without looking at the logged kernel
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role) {
		try {
			return getOtherRolePlayers(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role) {
		try {
			return getAnotherRolePlayer(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Messaging interface
	// ////////////////////////////////////////////////////////////

	ReturnCode sendMessage(final AbstractAgent requester, final String community, final String group, final String role,
			final Message message, final String senderRole) {
		if (message == null) {
			return INVALID_ARG;
		}
		AgentAddress receiver, sender;
		try {
			receiver = getAnotherRolePlayer(requester, community, group, role);
			if (receiver == null) {
				return NO_RECIPIENT_FOUND;
			}
			sender = getSenderAgentAddress(requester, receiver, senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		return buildAndSendMessage(sender, receiver, message);
	}

	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, final Message message, final String senderRole) {
		if (receiver == null || message == null) {
			return INVALID_ARG;
		}
		// check that the AA is valid : the targeted agent is still playing the
		// corresponding role or it was a candidate request
		if (!receiver.exists()) {// && !
			// receiver.getRole().equals(Roles.GROUP_CANDIDATE_ROLE)){
			return INVALID_AA;
		}
		// get the role for the sender
		AgentAddress sender;
		try {
			sender = getSenderAgentAddress(requester, receiver, senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		return buildAndSendMessage(sender, receiver, message);
	}

	ReturnCode sendReplyWithRole(final AbstractAgent requester, final Message messageToReplyTo, final Message reply,//TODO the reply should not be the same
			String senderRole) {
		if (messageToReplyTo == null || reply == null) {
			return INVALID_ARG;
		}
		reply.setID(messageToReplyTo.getConversationID());
		return sendMessage(requester, messageToReplyTo.getSender(), reply, senderRole);
	}

	ReturnCode broadcastMessageWithRole(final AbstractAgent requester, final String community, final String group,
			final String role, final Message messageToSend, String senderRole) {
		if (messageToSend == null)
			return INVALID_ARG;
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester, community, group, role);
			if (receivers == null)
				return NO_RECIPIENT_FOUND; // the requester is the only agent in
			// this group
			messageToSend.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			broadcasting(receivers, messageToSend);
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	List<Message> broadcastMessageWithRoleAndWaitForReplies(final AbstractAgent abstractAgent, final String community,
			final String group, final String role, Message message, final String senderRole, final Integer timeOutMilliSeconds) {
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(abstractAgent, community, group, role);
			if (message == null || receivers == null)
				return null; // the requester is the only agent in this group
			message.setSender(getSenderAgentAddress(abstractAgent, receivers.get(0), senderRole));
			broadcasting(receivers, message);
			return abstractAgent.waitAnswers(message, receivers.size(), timeOutMilliSeconds);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	void broadcasting(final Collection<AgentAddress> receivers, Message m) {
		for (final AgentAddress agentAddress : receivers) {
			if (agentAddress != null) {// TODO this should not be possible
				m = m.clone();
				m.setReceiver(agentAddress);
				sendMessage(m);
			}
		}
	}

	ReturnCode sendMessage(Message m) {
		final AbstractAgent target = m.getReceiver().getAgent();
		if (target == null) {
			return sendNetworkMessageWithRole(new ObjectMessage<Message>(m), netEmmiter);
		} else {
			target.receiveMessage(m);
		}
		return SUCCESS;
	}

	ReturnCode sendNetworkMessageWithRole(Message m, AgentAddress role) {
		updateNetworkAgent();
		if (netAgent != null) {
			m.setSender(role);
			m.setReceiver(netAgent);
			netAgent.getAgent().receiveMessage(m);
			return SUCCESS;
		}
		return NETWORK_DOWN;
	}

	private void updateNetworkAgent() {
		if (netAgent == null || !netAgent.exists()) {// Is it still playing the
			// role ?
			netAgent = getAgentWithRole(LOCAL_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE);
		}
	}

	boolean isOnline() {
		getMadkitKernel().updateNetworkAgent();
		return getMadkitKernel().netAgent != null;
	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Launching and Killing
	// ////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	synchronized List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent requester, String agentClassName,
			int bucketSize, Collection<String> CGRLocations) {
		Class<? extends AbstractAgent> agentClass = null;
		try {
			agentClass = (Class<? extends AbstractAgent>) platform.getMadkitClassLoader().loadClass(agentClassName);
		} catch (ClassCastException e) {
			if (requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch " + agentClassName + " because it is not an agent class");
			return null;
		} catch (ClassNotFoundException e) {
			if (requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch " + agentClassName + " because the class has not been found");
			return null;
		}
		final ArrayList<AbstractAgent> bucket = createBucket(agentClass, bucketSize);
		if (CGRLocations != null) {
			for (final String cgrLocation : CGRLocations) {
				final String[] cgr = cgrLocation.split(";");
				if (cgr.length != 3)
					return null;// TODO logging
				createGroup(requester, cgr[0], cgr[1], null, null, false);
				Group g = null;
				try {
					g = getGroup(cgr[0], cgr[1]);
				} catch (CGRNotAvailable e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
				boolean roleCreated = false;
				Role r = g.get(cgr[2]);
				if (r == null) {
					r = g.createRole(cgr[2]);
					g.put(r.getRoleName(), r);
					roleCreated = true;
				}
				r.addMembers(bucket, roleCreated);
				// test vs assignement ? -> No: cannot touch the organizational structure !!
			}
		}
		for (final AbstractAgent a : bucket) {
			a.activation(false);
		}
		return bucket;
	}

	private ArrayList<AbstractAgent> createBucket(final Class<? extends AbstractAgent> agentClass, int bucketSize) {
		final int cpuCoreNb = ((ThreadPoolExecutor) serviceExecutor).getCorePoolSize();
		final ArrayList<AbstractAgent> result = new ArrayList<AbstractAgent>(bucketSize);
		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
		// System.err.println("nb of ag per task "+nbOfAgentsPerTask);
		CompletionService<ArrayList<AbstractAgent>> ecs = new ExecutorCompletionService<ArrayList<AbstractAgent>>(serviceExecutor);
		final ArrayList<Callable<ArrayList<AbstractAgent>>> workers = new ArrayList<Callable<ArrayList<AbstractAgent>>>(cpuCoreNb);
		for (int i = 0; i < cpuCoreNb; i++) {
			workers.add(new Callable<ArrayList<AbstractAgent>>() {

				public ArrayList<AbstractAgent> call() throws Exception {
					final ArrayList<AbstractAgent> list = new ArrayList<AbstractAgent>(nbOfAgentsPerTask);
					for (int i = nbOfAgentsPerTask; i > 0; i--) {
						list.add(initAbstractAgent(agentClass));
					}
					return list;
				}
			});
		}
		for (final Callable<ArrayList<AbstractAgent>> w : workers)
			ecs.submit(w);
		int n = workers.size();
		for (int i = 0; i < n; ++i) {
			try {
				result.addAll(ecs.take().get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		for (int i = bucketSize - nbOfAgentsPerTask * cpuCoreNb; i > 0; i--) {
			// System.err.println("adding aone");
			result.add(initAbstractAgent(agentClass));
		}
		// System.err.println(result.size());
		return result;
	}

	private AbstractAgent initAbstractAgent(final Class<? extends AbstractAgent> agentClass) {
		final AbstractAgent a;
		try {
			a = agentClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		a.state.set(INITIALIZING); // no need to test : I created these
		// instances
		a.setKernel(this);
		a.getAlive().set(true);
		a.logger = null;
		return a;
	}

	@SuppressWarnings("unchecked")
	AbstractAgent launchAgent(AbstractAgent requester, final String agentClass, int timeOutSeconds, boolean defaultGUI) {
		Class<? extends AbstractAgent> aClass = null;
		try {
			aClass = (Class<? extends AbstractAgent>) platform.getMadkitClassLoader().loadClass(agentClass);
		} catch (ClassCastException e) {
			if (requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch " + agentClass + " because it is not an agent class");
			return null;
		} catch (ClassNotFoundException e) {
			if (requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch " + agentClass + " because the class has not been found");
			return null;
		}
		try {
			final AbstractAgent agent = aClass.newInstance();
			if (launchAgent(requester, agent, timeOutSeconds, defaultGUI) == AGENT_CRASH) {
				return null; // TODO when time out ?
			}
			return agent;
		} catch (InstantiationException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "Cannot launch " + agentClass + " because it has no default constructor",
							"Launch failed", JOptionPane.WARNING_MESSAGE);
				}
			});
			if (requester.logger != null)
				requester.logger.warning("Cannot launch " + agentClass + " because it has no default constructor");
		} catch (Throwable t) {
			if (requester.logger != null)
				requester.logger.warning("Cannot launch " + agentClass + " because " + t);
		}
		return null;
	}

	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds,
			final boolean defaultGUI) {
		if (agent == null || timeOutSeconds < 0)
			return INVALID_ARG;
		final Future<ReturnCode> launchAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call() {
				return launchingAgent(agent, defaultGUI);
			}
		});
		try {
			// if to == 0, this is still quicker than treating the case
			// this is holds for Integer.MAX_VALUE
			return launchAttempt.get(timeOutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {// requester has been killed or
			// something
			throw new KilledException(e);
		} catch (ExecutionException e) {// target has crashed !
			kernelLog("Launch failed on " + agent, Level.FINE, e);
			if (e.getCause() instanceof AssertionError)// convenient for Junit
				throw new AssertionError(e);
			return AGENT_CRASH;
		} catch (TimeoutException e) {// launch task time out
			return LAUNCH_TIME_OUT;
		}

	}

	ReturnCode launchingAgent(final AbstractAgent agent, boolean defaultGUI) {
		// this has to be done by a system thread
		if (!agent.state.compareAndSet(NOT_LAUNCHED, INITIALIZING) || shuttedDown) {
			return ALREADY_LAUNCHED;
		}
		final ArrayList<Future<Boolean>> lifeCycle = agent.getMyLifeCycle();
		agent.setKernel(this);

		if (defaultAgentLogLevel == Level.OFF && agent.logger == AbstractAgent.defaultLogger) {
			agent.logger = null;
		} else if (defaultAgentLogLevel != Level.OFF && agent.logger == AbstractAgent.defaultLogger) {
			agent.setLogLevel(defaultAgentLogLevel);
			agent.getLogger().setWarningLogLevel(defaultWarningLogLvl);
		}
		if (!agent.getAlive().compareAndSet(false, true)) {// TODO remove that
			throw new AssertionError("already alive in launch");
		}
		if (lifeCycle == null) {
			return agent.activation(defaultGUI) ? SUCCESS : AGENT_CRASH;
		}
		try {
			boolean success = startAgentLifeCycle((Agent) agent, defaultGUI).get();
			if(success)
				threadedAgents.add(agent);
			return success ? SUCCESS : AGENT_CRASH;
		} catch (InterruptedException e) {
			if (!shuttedDown) {
				e.printStackTrace();
				// Kernel cannot be interrupted !!
				kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); // Kernel
				return SEVERE;
			}
		} catch (ExecutionException e) {
			if (!shuttedDown) {
				e.printStackTrace();
				kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); // Kernel
				// Kernel cannot be interrupted !!
				return SEVERE;
			}
		}
		return LAUNCH_TIME_OUT;
	}

	Future<Boolean> startAgentLifeCycle(final Agent agent, final boolean gui) {
		final ArrayList<Future<Boolean>> lifeCycle = new ArrayList<Future<Boolean>>(4);
		final ExecutorService agentExecutor;
		if (agent.getMyLifeCycle().isEmpty()) {
			agentExecutor = Executors.newSingleThreadScheduledExecutor(normalAgentThreadFactory);
		} else {
			agentExecutor = Executors.newSingleThreadScheduledExecutor(daemonAgentThreadFactory);
		}
		final Future<Boolean> activation = agentExecutor.submit(new Callable<Boolean>() {

			public Boolean call() {
				final Thread currentThread = Thread.currentThread();
				// if (! currentThread.isDaemon()) {
				// activeThreadedAgents.add(agent);
				// }
				agent.setMyThread(currentThread);
				if (!agent.activation(gui)) {
					agent.getMyLifeCycle().get(1).cancel(true);// no
					// activation
					// no
					// living
					return false;
				}
				return true;
			}
		});
		lifeCycle.add(activation);
		lifeCycle.add(agentExecutor.submit(new Callable<Boolean>() {
			public Boolean call() {
				return agent.living();
			}
		}));
		lifeCycle.add(agentExecutor.submit(new Callable<Boolean>() {
			public Boolean call() {
				return agent.ending();
			}
		}));
		lifeCycle.add(agentExecutor.submit(new Callable<Boolean>() {
			public Boolean call() {
				agent.terminate();
				agentExecutor.shutdown();
				return true;
			}
		}));
		agent.setMyLifeCycle(lifeCycle);
		return activation;
	}

	ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, final int timeOutSeconds) {
		if (target == null || timeOutSeconds < 0)
			return INVALID_ARG;
		if (target.getState().compareTo(ACTIVATED) < 0) {
			return NOT_YET_LAUNCHED;
		}
		final Future<ReturnCode> killAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call() {
				return killingAgent(target);
			}
		});
		try {
			return killAttempt.get(timeOutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {// requester has been killed or
			// something
			throw new KilledException(e);
		} catch (ExecutionException e) {// target has crashed in end !
			kernelLog("kill failed on " + target, Level.FINE, e);
			if (e.getCause() instanceof AssertionError)
				throw new AssertionError(e);
			return AGENT_CRASH;
		} catch (TimeoutException e) {// kill task time out
			return LAUNCH_TIME_OUT;
		}
	}

	final ReturnCode killingAgent(final AbstractAgent target) {//TODO avec timeout
		// this has to be done by a system thread
		if (!target.getAlive().compareAndSet(true, false)) {
			return ALREADY_KILLED;
		}
		final ArrayList<Future<Boolean>> lifeCycle = target.getMyLifeCycle();
		if (lifeCycle != null) {
			killThreadedAgent((Agent) target);
			return SUCCESS;
		}
		target.ending();
		target.terminate();
		return SUCCESS;
	}

	private void killThreadedAgent(final Agent target) {
		target.myThread.setPriority(Thread.MIN_PRIORITY);
		final ArrayList<Future<Boolean>> lifeCycle = target.getMyLifeCycle();
		lifeCycle.get(1).cancel(true);
		lifeCycle.get(0).cancel(true);
		try {
			// JOptionPane.showMessageDialog(null, "coucou");
			lifeCycle.get(2).get(); // waiting that end ends with to
		} catch (CancellationException e) {
			kernelLog("wired", Level.SEVERE, e);
		} catch (InterruptedException e) {
			kernelLog("wired", Level.SEVERE, e);
		} catch (ExecutionException e) {// agent crashed in end
			kernelLog("agent crashed in ", Level.SEVERE, e);
		}
		try {
			lifeCycle.get(3).get();
		} catch (InterruptedException e) {
			kernelLog("wired bug report", Level.SEVERE, e);
		} catch (ExecutionException e) {
			kernelLog("wired bug report", Level.SEVERE, e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Organization access
	// /////////////////////////////////////////////////////////////////////////
	Organization getCommunity(final String community) throws CGRNotAvailable {
		if (community == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		Organization org = organizations.get(community);
		if (org == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		return org;
	}

	Group getGroup(final String community, final String group) throws CGRNotAvailable {
		Organization o = getCommunity(community);
		if (group == null)
			throw new CGRNotAvailable(NOT_GROUP);
		Group g = o.get(group);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	Role getRole(final String community, final String group, final String role) throws CGRNotAvailable {
		Group g = getGroup(community, group);// get group before for warning
		// coherency
		if (role == null)
			throw new CGRNotAvailable(NOT_ROLE);
		Role r = g.get(role);
		if (r == null)
			throw new CGRNotAvailable(NOT_ROLE);
		return r;
	}

	// List<AgentAddress> getRolePlayers(String community,String group, String
	// role) throws CGRNotAvailable{
	// return getRole(community, group, role).getAgentAddresses();
	// }

	/**
	 * @param abstractAgent
	 * @param community
	 * @param group
	 * @param role
	 * @return null if nobody is found
	 * @throws CGRNotAvailable
	 *            if one of community, group or role does not exist
	 */
	List<AgentAddress> getOtherRolePlayers(AbstractAgent abstractAgent, String community, String group, String role)
	throws CGRNotAvailable {
		// never null without throwing Ex
		final Set<AgentAddress> result = getRole(community, group, role).getAgentAddressesCopy();
		Role.removeAgentAddressOf(abstractAgent, result);
		if (!result.isEmpty()) {
			return new ArrayList<AgentAddress>(result);
		}
		return null;
	}

	AgentAddress getAnotherRolePlayer(AbstractAgent abstractAgent, String community, String group, String role)
	throws CGRNotAvailable {
		List<AgentAddress> others = getOtherRolePlayers(abstractAgent, community, group, role);
		if (others != null) {
			return others.get((int) (Math.random() * others.size()));
		}
		return null;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Messaging
	// /////////////////////////////////////////////////////////////////////////

	private ReturnCode buildAndSendMessage(final AgentAddress sender, final AgentAddress receiver, final Message m) {
		m.setSender(sender);
		m.setReceiver(receiver);
		return sendMessage(m);
		// final AbstractAgent target = receiver.getAgent();
		// if(target == null){
		// if(netAgent != null)
		// netAgent.receiveMessage(new MessageConveyor(m));
		// else
		// return NETWORK_DOWN;
		// }
		// else{
		// target.receiveMessage(m);
		// }
		// return SUCCESS;
	}

	final AgentAddress getSenderAgentAddress(final AbstractAgent sender, final AgentAddress receiver, String senderRole)
	throws CGRNotAvailable {
		AgentAddress senderAA = null;
		final Role targetedRole = receiver.getRoleObject();
		if (senderRole == null) {// looking for any role in this group, starting
			// with the receiver role
			senderAA = targetedRole.getAgentAddressInGroup(sender);
			// if still null : this SHOULD be a candidate's request to the
			// manager or it is an error
			if (senderAA == null) {
				if (targetedRole.getRoleName().equals(Roles.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				else
					throw new CGRNotAvailable(NOT_IN_GROUP);
			}
			return senderAA;
		}
		// the sender explicitly wants to send the message with a particular
		// role : check that
		else {
			// look into the senderRole role if the agent is in
			final Role senderRoleObject = targetedRole.getMyGroup().get(senderRole);
			if (senderRoleObject != null) {
				senderAA = senderRoleObject.getAgentAddressOf(sender);
			}
			if (senderAA == null) {// if still null : this SHOULD be a
				// candidate's request to the manager or it
				// is an error
				if (senderRole.equals(Roles.GROUP_CANDIDATE_ROLE) && targetedRole.getRoleName().equals(Roles.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				if (targetedRole.getAgentAddressInGroup(sender) == null)
					throw new CGRNotAvailable(NOT_IN_GROUP);
				else
					throw new CGRNotAvailable(ROLE_NOT_HANDLED);
			}
			return senderAA;
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Simulation
	// /////////////////////////////////////////////////////////////////////////

	boolean addOverlooker(final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		if (operatingOverlookers.add(o)) {
			try {
				getRole(o.getCommunity(), o.getGroup(), o.getRole()).addOverlooker(o);
			} catch (CGRNotAvailable e) {
			}
			return true;
		}
		return false;
	}

	/**
	 * @param scheduler
	 * @param activator
	 */

	boolean removeOverlooker(final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final Role r = o.getOverlookedRole();
		if (r != null) {
			r.removeOverlooker(o);
		}
		return operatingOverlookers.remove(o);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Internal functioning
	// /////////////////////////////////////////////////////////////////////////

	void removeCommunity(String community) {
		organizations.remove(community);
	}

	//
	// void removeThreadedAgent(Agent a){
	// activeThreadedAgents.remove(a);
	// // if(activeThreadedAgents.isEmpty()){
	// // kernelAgent.receiveMessage(new
	// KernelMessage(OperationCode.SHUTDOWN_NOW,(Object[])null));
	// // }
	// }

	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {
		return platform.getMadkitClassLoader().loadClass(className);
	}

	@Override
	public KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	/**
	 * @return
	 */
	Set<Overlooker<? extends AbstractAgent>> getOperatingOverlookers() {
		return operatingOverlookers;
	}

	/**
	 * @param abstractAgent
	 */

	void removeAgentFromOrganizations(AbstractAgent theAgent) {
		for (final Organization org : organizations.values()) {
			final ArrayList<String> groups = org.removeAgentFromAllGroups(theAgent);
			for (final String groupName : groups) {
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(theAgent, new Role(org.getName(), groupName),
						kernelAddress)), netUpdater);
			}
		}
	}

	String getMadkitProperty(AbstractAgent abstractAgent, String key) {
		return platform.getConfigOption().getProperty(key);
	}

	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		platform.checkAndValidateOption(key, value);// TODO update agent logging
		// on or off
	}

	MadkitKernel getMadkitKernel() {
		return this;
	}

	/**
	 * Asks MasKit to reload the class byte code so that new instances, created
	 * using {@link Class#newInstance()} on a class object obtained with
	 * {@link #getNewestClassVersion(AbstractAgent, String)}, will reflect
	 * compilation changes during run time.
	 * 
	 * @param requester
	 * @param name
	 *           The fully qualified class name of the class
	 * @throws ClassNotFoundException
	 */

	ReturnCode reloadClass(AbstractAgent requester, String name) throws ClassNotFoundException {
		if (name == null)
			throw new ClassNotFoundException(ReturnCode.CLASS_NOT_FOUND.getMessage() + " " + name);
		if (!name.contains("madkit.kernel") && !name.contains("madkit.gui") && !name.contains("madkit.messages")
				&& !name.contains("madkit.simulation") && platform.getMadkitClassLoader().reloadClass(name))
			return SUCCESS;
		return ReturnCode.CLASS_NOT_FOUND;// TODO not the right code here
	}

	boolean isCommunity(AbstractAgent requester, String community) {
		try {
			return getCommunity(community) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	boolean isGroup(AbstractAgent requester, String community, String group) {
		try {
			return getGroup(community, group) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		try {
			return getRole(community, group, role) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * @param l
	 */
	void setDefaultAgentLogLevel(Level agentLoglevel, Level warningLogLevel) {
		defaultAgentLogLevel = agentLoglevel;
		defaultWarningLogLvl = warningLogLevel;
	}

	synchronized void importDistantOrg(SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> distantOrg) {
		for (String communityName : distantOrg.keySet()) {
			Organization org = new Organization(communityName, this);
			Organization previous = organizations.putIfAbsent(communityName, org);
			if (previous != null) {
				org = previous;
			}
			org.importDistantOrg(distantOrg.get(communityName));
		}
	}

	@Override
	public SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global) {
		SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> export = new TreeMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>>();
		for (Map.Entry<String, Organization> org : organizations.entrySet()) {
			SortedMap<String, SortedMap<String, Set<AgentAddress>>> currentOrg = org.getValue().getOrgMap(global);
			if (!currentOrg.isEmpty())
				export.put(org.getKey(), org.getValue().getOrgMap(global));
		}
		return export;
	}

	@Override
	public URLClassLoader getMadkitClassLoader(){
		return platform.getMadkitClassLoader();
	}

	// void logCurrentOrganization(Logger requester, Level lvl){
	// if(requester != null){
	// String message = "Current organization is\n";
	// if(organizations.isEmpty()){
	// message+="\n ------------ EMPTY !! ------------\n";
	// }
	// for(final Map.Entry<String, Organization> org :
	// organizations.entrySet()){
	// message+="\n\n--"+org.getKey()+"----------------------";
	// for(final Map.Entry<String, Group> group : org.getValue().entrySet()){
	// // final AgentAddress manager = group.getValue().getManager().get();
	// message+="\n|--"+group.getKey()+"--";// managed by
	// ["+manager.getAgent()+"] "+manager+" --\n";
	// for(final Map.Entry<String, Role> role: group.getValue().entrySet()){
	// message+="\n||--"+role.getKey()+"--";
	// message+="\n|||--players- "+role.getValue().getPlayers();
	// message+="\n|||--addresses- = "+role.getValue().getAgentAddresses();
	// }
	// }
	// message+="\n-----------------------------";
	// }
	// requester.log(lvl, message+"\n");
	// }
	// }

	final void injectMessage(final ObjectMessage<Message> m) {
		Message toInject = m.getContent();
		final AgentAddress receiver = toInject.getReceiver();
		final AgentAddress sender = toInject.getSender();
		try {
			Role receiverRole = getRole(receiver.getCommunity(), receiver.getGroup(), receiver.getRole());
			receiver.setRoleObject(receiverRole);
			Role senderRole = getRole(sender.getCommunity(), sender.getGroup(), sender.getRole());
			sender.setRoleObject(senderRole);
			AbstractAgent target = null;
			if (receiverRole != null) {
				target = receiverRole.getAbstractAgentWithAddress(receiver);
				if (target != null) {
					target.receiveMessage(toInject);
				}
			}
			if (target == null && logger != null)
				logger.finer("message received but the agent address is no longer valid !! Current distributed org is "
						+ getOrganizationSnapShot(false));
		} catch (CGRNotAvailable e) {
			bugReport(e);
		}
	}

	final void injectOperation(CGRSynchro m) {
		final Role r = m.getContent().getRoleObject();
		final String communityName = r.getCommunityName();
		final String groupName = r.getGroupName();
		final String roleName = r.getRoleName();
		if (logger != null)
			logger.finer("distant CGR " + m.getCode() + " on " + m.getContent());
		try {
			switch (m.getCode()) {
			case CREATE_GROUP:
				Organization organization = new Organization(communityName, this);// no
				// need
				// to
				// remove
				// org
				// never
				// failed
				// if
				// not
				// present
				final Organization tmpOrg = organizations.putIfAbsent(communityName, organization);
				if (tmpOrg != null) {
					if (isGroup(communityName, groupName)) {
						if (logger != null)
							logger.finer("distant group creation by " + m.getContent() + " aborted : already exists locally");
						break;
					}
					organization = tmpOrg;
				}
				organization.put(groupName, new Group(communityName, groupName, m.getContent(), null, organization));
				break;
			case REQUEST_ROLE:
				getGroup(communityName, groupName).addDistantMember(m.getContent());
				break;
			case LEAVE_ROLE:
				getRole(communityName, groupName, roleName).removeDistantMember(m.getContent());
				break;
			case LEAVE_GROUP:
				getGroup(communityName, groupName).removeDistantMember(m.getContent());
				break;
				// case CGRSynchro.LEAVE_ORG://TODO to implement
				// break;
			default:
				break;
			}
		} catch (CGRNotAvailable e) {
			kernelLog("distant CGR " + m.getCode() + " update failed on " + m.getContent(), Level.FINE, e);
			e.printStackTrace();
		}
	}

	// /**
	// * @param lk
	// */
	// void setLoggedKernel(LoggedKernel lk) {
	// loggedKernel = lk;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#kernelLog(java.lang.String)
	 */

	void kernelLog(String message, Level logLvl, Throwable e) {
		platform.kernelLog(message, logLvl, e);
	}
	
	synchronized void shutdownn(){
		shuttedDown = true;
		threadedAgents.remove(this);
		for (AbstractAgent a : threadedAgents) {
			killAgent(a,0);
		}
		shutdown();
	}

	synchronized void shutdown() {
		if (logger != null)
			logger.finer("***** SHUTINGDOWN MADKIT ********\n");
		shuttedDown = true;
		// Thread t = new Thread(new Runnable() {
		// public void run() {
		if (logger != null) {
			if (logger.getLevel().intValue() <= Level.FINER.intValue()) {
				normalAgentThreadFactory.getThreadGroup().list();
			}
		}
		MadkitKernel.this.getMadkitKernel().broadcastMessageWithRoleAndWaitForReplies(MadkitKernel.this, LOCAL_COMMUNITY,
				SYSTEM_GROUP, GUI_MANAGER_ROLE, new GUIMessage(MadkitActions.MADKIT_EXIT_ACTION, MadkitKernel.this), null, 5000);// TODO
		// if
		// it
		// takes too
		// long
		normalAgentThreadFactory.getThreadGroup().interrupt();
		if (normalAgentThreadFactory.getThreadGroup().activeCount() != 0) {
			pause(1000);
			normalAgentThreadFactory.getThreadGroup().interrupt();
		}
		if (logger != null) {
			if (logger.getLevel().intValue() <= Level.FINER.intValue()) {
				System.err.println("---------remaining---------");
				normalAgentThreadFactory.getThreadGroup().list();
			}
		}
		if (logger != null)
			logger.talk(platform.printFareWellString());
//		normalAgentThreadFactory.getThreadGroup().stop();
		LogManager.getLogManager().reset();
		// }
		// });
		// t.start();
	}

	boolean createGroupIfAbsent(AbstractAgent abstractAgent, String community, String group, String group2,
			GroupIdentifier theIdentifier, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, group, theIdentifier, isDistributed) == SUCCESS;
	}

	void bugReport(Throwable e) {
		e.printStackTrace();
		kernelLog("**** Please bug report", Level.SEVERE, e);
	}

	final synchronized void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		for (Organization org : organizations.values()) {
			org.removeAgentsFromDistantKernel(kernelAddress2);
		}
	}

}

final class CGRNotAvailable extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -375379801933609564L;
	final ReturnCode code;

	/**
	 * @return the code
	 */
	final ReturnCode getCode() {
		return code;
	}

	/**
	 * @param notCommunity
	 */
	CGRNotAvailable(ReturnCode code) {
		this.code = code;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}

}