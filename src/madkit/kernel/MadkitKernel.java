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

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SEVERE;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static madkit.kernel.AbstractAgent.State.ACTIVATED;
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.LIVING;
import static madkit.kernel.AbstractAgent.State.NOT_LAUNCHED;
import static madkit.kernel.CGRSynchro.Code.CREATE_GROUP;
import static madkit.kernel.CGRSynchro.Code.LEAVE_GROUP;
import static madkit.kernel.CGRSynchro.Code.LEAVE_ROLE;
import static madkit.kernel.CGRSynchro.Code.REQUEST_ROLE;
import static madkit.kernel.Madkit.BooleanOption.autoConnectMadkitWebsite;
import static madkit.kernel.Madkit.BooleanOption.console;
import static madkit.kernel.Madkit.BooleanOption.loadLocalDemos;
import static madkit.kernel.Madkit.BooleanOption.network;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import madkit.action.ActionInfo;
import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.ConsoleAgent;
import madkit.gui.MASModel;
import madkit.i18n.ErrorMessages;
import madkit.i18n.Words;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.message.KernelMessage;
import madkit.message.ObjectMessage;
import madkit.message.hook.AgentLifeEvent;
import madkit.message.hook.HookMessage;
import madkit.message.hook.HookMessage.AgentActionEvent;
import madkit.message.hook.MessageEvent;
import madkit.message.hook.OrganizationEvent;

/**
 * The brand new MaDKit kernel and it is now a real Agent :)
 * 
 * @author Fabien Michel
 * @version 1.3
 * @since MaDKit 5.0
 * 
 */
class MadkitKernel extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3999398692543480834L;

	private final static ThreadGroup SYSTEM = new ThreadGroup("MK_SYSTEM") {
		public void uncaughtException(Thread t, Throwable e) {
			System.err.println("\n------------uncaught exception on " + t);
		}
	};
	// final static ThreadGroup A_LIFE = new ThreadGroup("A_LIFE"){//TODO
	// duplicate
	// public void uncaughtException(Thread t, Throwable e) {
	// System.err.println(t);
	// if(e instanceof KilledException){
	// e.printStackTrace();
	// }
	// else{
	// System.err.println("--------------internal BUG--------------------");
	// System.err.println(t);
	// e.printStackTrace();
	// };
	// }
	// };

	final static private ThreadPoolExecutor serviceExecutor = new ThreadPoolExecutor(
	// Runtime.getRuntime().availableProcessors() + 1,
			2, Integer.MAX_VALUE, 2L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
				public Thread newThread(Runnable r) {
					final Thread t = new Thread(SYSTEM, r);
					t.setPriority(Thread.MAX_PRIORITY);
					t.setName(SYSTEM.getName());
					t.setDaemon(true);
					// t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					// @Override
					// public void uncaughtException(Thread t, Throwable e) {
					// e.printStackTrace();
					// e.getCause().printStackTrace();
					// }
					// });
					return t;
				}
			});

	final private ThreadPoolExecutor lifeExecutor;

	final static ExecutorService getMadkitServiceExecutor() {
		return serviceExecutor;
	}

	// ;// = Executors.newCachedThreadPool();

	static {
		serviceExecutor.prestartAllCoreThreads();
		serviceExecutor.allowCoreThreadTimeOut(true);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				AgentLogger.resetLoggers();
			}
		});
	}

	private final ConcurrentHashMap<String, Organization> organizations;
	final private Set<Overlooker<? extends AbstractAgent>> operatingOverlookers;
	final private Madkit platform;
	final private KernelAddress kernelAddress;

	protected MadkitKernel loggedKernel;
	private volatile boolean shuttedDown = false;
	private final AgentThreadFactory normalAgentThreadFactory;
	private final AgentThreadFactory daemonAgentThreadFactory;

	private AgentAddress netAgent;
	// my private addresses for optimizing the message building
	private AgentAddress netUpdater, netEmmiter, kernelRole;
	final private Set<Agent> threadedAgents;

	/**
	 * if <code>true</code>, deactivate organizational operations
	 */
	private boolean bucketMode = false;

	private EnumMap<AgentActionEvent, Set<AbstractAgent>> hooks;

	// private AtomicInteger proceed = new AtomicInteger(0);

	/**
	 * Constructing the real one.
	 * 
	 * @param m
	 */
	MadkitKernel(Madkit m) {
		super(true);
		platform = m;
		kernel = this;
		threadedAgents = new HashSet<Agent>(20);
		setLogLevel(LevelOption.kernelLogLevel.getValue(getMadkitConfig()));
		kernelAddress = new KernelAddress();
		organizations = new ConcurrentHashMap<String, Organization>();
		operatingOverlookers = new LinkedHashSet<Overlooker<? extends AbstractAgent>>();
		loggedKernel = new LoggedKernel(this);
		normalAgentThreadFactory = new AgentThreadFactory(kernelAddress, false);
		daemonAgentThreadFactory = new AgentThreadFactory(kernelAddress, true);
		lifeExecutor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
				new ThreadFactory() {
					public Thread newThread(Runnable r) {
						final Thread t = new Thread(normalAgentThreadFactory.getThreadGroup(), r);
						// t.setPriority(Thread.MIN_PRIORITY);
						// t.setDaemon(true);
						return t;
					}
				});
		lifeExecutor.prestartAllCoreThreads();
		lifeExecutor.allowCoreThreadTimeOut(true);
	}

	MadkitKernel() {
		// for fake kernels
		super(null);
		kernel = this;
		threadedAgents = null;
		loggedKernel = this;
		platform = null;
		kernelAddress = null;
		organizations = null;
		operatingOverlookers = null;
		normalAgentThreadFactory = null;
		daemonAgentThreadFactory = null;
		lifeExecutor = null;
	}

	MadkitKernel(MadkitKernel k) {
		super(null);
		threadedAgents = null;
		platform = k.platform;
		kernelAddress = k.kernelAddress;
		organizations = k.organizations;
		operatingOverlookers = k.operatingOverlookers;
		normalAgentThreadFactory = null;
		daemonAgentThreadFactory = null;
		lifeExecutor = null;
		kernel = k;
	}

	@Override
	protected void activate() {
		if (logger != null)
			logger.setWarningLogLevel(Level.INFO);
		
		//denying all requests for this group
		createGroup(LocalCommunity.NAME, Groups.SYSTEM, false,new Gatekeeper() {
			@Override
			public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
				return false;
			}
		});

		//building the network group
		createGroup(LocalCommunity.NAME, Groups.NETWORK, false);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER, null);

		//my AAs cache
		netUpdater = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER);
		netEmmiter = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER);
		kernelRole = getAgentAddressIn(LocalCommunity.NAME, Groups.NETWORK, Roles.KERNEL);

		myThread.setPriority(Thread.NORM_PRIORITY + 1);

		if (loadLocalDemos.isActivated(getMadkitConfig())) {
			loadLocalDemos();
		}
		
		launchGuiManagerAgent();

		if (console.isActivated(getMadkitConfig())) {
			launchAgent(new ConsoleAgent());
		}
		launchNetworkAgent();
		// logCurrentOrganization(logger,Level.FINEST);
	}

	/**
	 * Starts a session considering the current MaDKit configuration
	 */
	private void startSession() {
		launchConfigAgents();
	}

	/**
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		if (autoConnectMadkitWebsite.isActivated(getMadkitConfig())) {
			addWebRepository();
		}
		startSession();
		while (!shuttedDown) {
			handleMessage(waitNextMessage());// As a daemon, a timeout is not required
		}
	}
	
	@Override
	public boolean isAlive() {
		return super.isAlive() && ! shuttedDown;
	}

	@Override
	protected void end() {
		if (LevelOption.madkitLogLevel.getValue(platform.getConfigOption()) != Level.OFF) {
			System.err.println("\n\t---------------------------------------" + "\n\t         MaDKit Kernel " + kernelAddress
					+ " \n\t        is shutting down, Bye !" + "\n\t---------------------------------------\n");
		}
	}

	final private void launchGuiManagerAgent() {
		if (logger != null)
			logger.fine("\n\t****** Launching GUI Manager ******\n");
		// if (noGUIManager.isActivated(getMadkitConfig())) {
		// if (logger != null)
		// logger.fine("** No GUI Manager: " + noGUIManager +
		// " option is true**\n");
		// } else {
		try {
			// no need to externalize : it is the only use of that string
			final Constructor<?> c = getMadkitClassLoader().loadClass("madkit.gui.GUIManagerAgent").getDeclaredConstructor(
					boolean.class);
			c.setAccessible(true);
			final AbstractAgent a = (AbstractAgent) c.newInstance(!BooleanOption.desktop.isActivated(getMadkitConfig()));
			// c.setAccessible(false); //useless
			a.setLogLevel(LevelOption.guiLogLevel.getValue(getMadkitConfig()));
			launchAgent(a);
			threadedAgents.remove(a);
			if (logger != null)
				logger.fine("\n\t****** GUI Manager launched ******\n");
		} catch (ClassNotFoundException e) {
			bugReport(e);
		} catch (SecurityException e) {
			bugReport(e);
		} catch (NoSuchMethodException e) {
			bugReport(e);
		} catch (IllegalArgumentException e) {
			bugReport(e);
		} catch (InstantiationException e) {
			bugReport(e);
		} catch (IllegalAccessException e) {
			bugReport(e);
		} catch (InvocationTargetException e) {
			bugReport(e);
		}
		// }
	}

	@SuppressWarnings("unused")
	private void loadJarFile(URL url) {
		platform.getMadkitClassLoader().addToClasspath(url);
	}

	private void copy() {
		startSession(false);
	}

	@SuppressWarnings("unused")
	private void restart() {
		copy();
		exit();
	}

	/**
	 * 
	 */
	private void addWebRepository() {
		final String repoLocation = getMadkitConfig().getProperty("madkit.repository.url");
		if (logger != null)
			logger.fine("** CONNECTING WEB REPO **" + repoLocation);
		try {
			Properties p = new Properties();
			p.load(new URL(repoLocation + "repo.properties").openStream());
			// System.err.println(p);
			for (Entry<Object, Object> object : p.entrySet()) {
				// platform.getMadkitClassLoader().addJar(new
				// URL(repoLocation+object.getKey()+".jar"));
				platform.getMadkitClassLoader().addToClasspath(
						new URL(repoLocation + object.getValue() + "/" + object.getKey() + ".agents.jar"));
			}
		} catch (final IOException e) {
			if (logger != null)
				logger.log(Level.WARNING, ErrorMessages.CANT_CONNECT + ": madkit.net " + repoLocation + "\n" + e.getMessage());
		}
	}

	/**
	 * 
	 */
	private void loadLocalDemos() {
		if (logger != null)
			logger.fine("** LOADING DEMO DIRECTORY **");
		File f = lookForMadkitDemoHome();
		if (f != null && f.isDirectory()) {
			platform.getMadkitClassLoader().loadJarsFromPath(f.getAbsolutePath());
		} else if (logger != null)
			logger.log(Level.WARNING, ErrorMessages.CANT_FIND + " demo " + Words.DIRECTORY + "\n");
	}

	private File lookForMadkitDemoHome() {
		for (URL url : getMadkitClassLoader().getURLs()) {
			if (url.getProtocol().equals("file")
					&& url.getPath().contains(platform.getConfigOption().getProperty("madkit.jar.name"))) {
				try {
					return new File(new File(url.toURI()).getParentFile(), "demos");// URI
																											// prevents
																											// error
																											// from
																											// character
																											// encoding
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private void launchMas(MASModel dm) {
		if (logger != null)
			logger.finer("** LAUNCHING SESSION " + dm.getName());
		Properties mkCfg = platform.getConfigOption();
		Properties currentConfig = new Properties();
		currentConfig.putAll(mkCfg);
		mkCfg.putAll(platform.buildConfigFromArgs(dm.getSessionArgs()));
		startSession();
		mkCfg.putAll(currentConfig);
	}
	
	
	@SuppressWarnings("unused")
	private void console() {
		launchAgent(ConsoleAgent.class.getName());
	}

	@SuppressWarnings("unused")
	private void jconsole() {
		final String jconsolePath = KernelAction.findJconsole();
		if(jconsolePath != null){
			final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
					try {
						Runtime.getRuntime().exec(jconsolePath+" "+pid.substring(0, pid.indexOf('@')));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		}
		else{
			getLogger().severe("jconsole unavailable");
		}
	}

	private void launchConfigAgents() {
		final ExecutorService startExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);// TODO
																																									// do
																																									// that
																																									// with
																																									// lifeex
		if (logger != null)
			logger.fine("** LAUNCHING CONFIG AGENTS **");
		final String agentsTolaunch = platform.getConfigOption().getProperty(Option.launchAgents.name());
		if (!agentsTolaunch.equals("null")) {
			final String[] agentsClasses = agentsTolaunch.split(";");
			for (final String classNameAndOption : agentsClasses) {
				final String[] classAndOptions = classNameAndOption.split(",");
				final String className = classAndOptions[0].trim();// TODO should test if these classes exist
				final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
				int number = 1;
				if (classAndOptions.length > 2) {
					try {
						number = Integer.parseInt(classAndOptions[2].trim());
					} catch (NumberFormatException e) {
						getLogger().severeLog(ErrorMessages.OPTION_MISUSED.toString() +Option.launchAgents.toString()+" "+ agentsTolaunch +" "+e.getClass().getName()+" !!!\n" , null);
					}
				}
				if (logger != null)
					logger.finer("Launching " + number + " instance(s) of " + className + " with GUI = " + withGUI);
				for (int i = 0; i < number; i++) {
					startExecutor.execute(new Runnable() {
						public void run() {
							if (!shuttedDown) {
									try {
										launchAgent((AbstractAgent) getMadkitClassLoader().loadClass(className).newInstance(), 1, withGUI);
									} catch (InstantiationException e) {
										cannotLaunchAgent(className, e, null);
//										getLogger().severeLog(ErrorMessages.CANT_LAUNCH.toString() + className+" "+e.getClass().getName()+" !!!\n" , null);//waiting java 7
									} catch (IllegalAccessException e) {
										cannotLaunchAgent(className, e, null);
									} catch (ClassNotFoundException e) {
										cannotLaunchAgent(className, e, null);
									} catch (Exception e) {
										cannotLaunchAgent(className, e, null);
									}
							}
						}
					});
				}
			}
			startExecutor.shutdown();
		}
	}

	@SuppressWarnings("unused")
	private void startSession(final boolean externalVM) {
		if (logger != null) {
			logger.config("starting new MaDKit session with " + Arrays.deepToString(platform.args));
		}
		if (externalVM) {
			try {
				String args = "";
				for (String s : platform.args) {
					args += s + " ";
				}
				Runtime.getRuntime().exec(//TODO not used yet
						System.getProperty("java.home")+File.separatorChar+"bin"+File.separatorChar
						+"java -cp "+System.getProperty("java.class.path")+" "+platform.getConfigOption().getProperty("madkit.main.class")+ " "+args);
			} catch (IOException e) {
				bugReport(e);
			}
		} else {
			new Madkit(platform.args);
		}
	}

	@SuppressWarnings("unused")
	private void stopNetwork() {
		// ReturnCode r = sendNetworkMessageWithRole(new Message(), kernelRole);
		if (sendNetworkMessageWithRole(new KernelMessage(KernelAction.STOP_NETWORK), kernelRole) == SUCCESS) {
			if (logger != null)
				logger.fine("\n\t****** Network stopped ******\n");
		}// TODO i18n
		else {
			if (logger != null)
				logger.fine("\n\t****** Network already down ******\n");
		}
	}

	// private ReturnCode updateNetworkStatus(boolean start){
	// return sendNetworkMessageWithRole(new
	// KernelMessage(KernelAction.LAUNCH_NETWORK,start), kernelRole);
	// }
	//
	private void handleMessage(Message m) {
		if (m instanceof KernelMessage) {
			proceedEnumMessage((KernelMessage) m);
		} else if (m instanceof HookMessage) {
			handleHookRequest((HookMessage) m);
		} else {
			if (logger != null)
				logger.warning("I received a message that I do not understand. Discarding " + m);
		}
	}

	private void handleHookRequest(HookMessage m) {
		if (hooks == null) {
			hooks = new EnumMap<AgentActionEvent, Set<AbstractAgent>>(AgentActionEvent.class);
		}
		Set<AbstractAgent> l = hooks.get(m.getCode());
		if (l == null) {
			l = new HashSet<AbstractAgent>();
			hooks.put(m.getCode(), l);
		}
		final AbstractAgent requester = m.getSender().getAgent();
		// for speeding up if there is no hook, i.e. logger == null is default
		getLogger().setLevel(Level.INFO);
		if (! l.add(requester)) {
			l.remove(requester);
			if(l.isEmpty()){
				hooks.remove(m.getCode());
				if(hooks.isEmpty())
					hooks = null;
			}
		}
	}

	private void launchNetworkAgent() {// FIXME cannot be in start session
		if (network.isActivated(getMadkitConfig())) {
			launchNetwork();
		} else {
			if (logger != null)
				logger.fine("** Networking is off: No Net Agent **\n");
		}
	}

	// /////////////////////////////////////: Kernel Part

	/**
	 * @return the loggedKernel
	 */
	final MadkitKernel getLoggedKernel() {
		return loggedKernel;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Agent interface
	// /////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Organization interface
	// ////////////////////////////////////////////////////////////

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final Gatekeeper gatekeeper,
			final boolean isDistributed) {
		if (bucketMode)
			return ReturnCode.IGNORED;
		if (group == null)
			throw new NullPointerException(ErrorMessages.G_NULL.toString());
		Organization organization = new Organization(community, this);
		// no need to remove org: never failed
		// will throw null pointer if community is null
		final Organization tmpOrg = organizations.putIfAbsent(community, organization);
		if (tmpOrg != null) {
			organization = tmpOrg;
		}
		synchronized (organization) {
			if (!organization.addGroup(creator, group, gatekeeper, isDistributed)) {
				return ALREADY_GROUP;
			}
		}
		if (isDistributed) {
			try {
				sendNetworkMessageWithRole(
						new CGRSynchro(CREATE_GROUP, getRole(community, group, madkit.agr.Organization.GROUP_MANAGER_ROLE)
								.getAgentAddressOf(creator)), netUpdater);
			} catch (CGRNotAvailable e) {
				getLogger().severeLog("Please bug report", e);
			}
		}
		if (logger != null) {
			informHooks(AgentActionEvent.CREATE_GROUP, creator.getName(), community, group, isDistributed);
		}
		return SUCCESS;
	}

	void informHooks(AgentActionEvent action, Object... parameters) {
		if (hooks != null) {
			final Set<AbstractAgent> l = hooks.get(action);
			if (l != null) {
				HookMessage hm = null;
				switch(action){
				case CREATE_GROUP:
				case REQUEST_ROLE:
				case LEAVE_GROUP:
				case LEAVE_ROLE:
					hm = new OrganizationEvent(action,parameters);
					break;
				case BROADCAST_MESSAGE:
				case SEND_MESSAGE:
					hm = new MessageEvent(action,parameters);
					break;
				case AGENT_STARTED:
				case AGENT_TERMINATED:
					hm = new AgentLifeEvent(action,parameters);
					break;
				default:
					break;
				}
				for (final AbstractAgent a : l) {
					a.receiveMessage(hm);//TODO check null
				}
			}
		}
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
		if (bucketMode)
			return ReturnCode.IGNORED;
		// final Organization org = organizations.get(community);
		// if(org == null)
		// return NOT_COMMUNITY;
		// final ReturnCode result = org.requestRole(requester, group, role,
		// memberCard);

		final Group g;
		try {
			g = getGroup(community, group);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		final ReturnCode result = g.requestRole(requester, role, memberCard);
		if (result == SUCCESS) {
			if (g.isDistributed()) {
				sendNetworkMessageWithRole(new CGRSynchro(REQUEST_ROLE, g.get(role).getAgentAddressOf(requester)), netUpdater);
			}
			if (logger != null)
				informHooks(AgentActionEvent.REQUEST_ROLE, requester.getName(), community, group, role);
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
		final List<Role> affectedRoles;
		synchronized (organizations) {
			try {
				g = getGroup(community, group);
			} catch (CGRNotAvailable e) {
				return e.getCode();
			}
			affectedRoles = g.leaveGroup(requester);
		}
		if (affectedRoles != null) {//success
			for (Role role : affectedRoles) {
				role.removeFromOverlookers(requester);
			}
			if (g.isDistributed()) {
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(requester, new Role(community, group),
						kernelAddress)), netUpdater);
			}
			if (logger != null)
				informHooks(AgentActionEvent.LEAVE_GROUP, community, group);
			return SUCCESS;
		}
		return NOT_IN_GROUP;
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
		synchronized (organizations) {
			try {
				r = getRole(community, group, role);
			} catch (CGRNotAvailable e) {
				return e.getCode();
			}
			ReturnCode rc;
			// this is apart because I need the address before the leave
			if (r.getMyGroup().isDistributed()) {
				AgentAddress leaver = r.getAgentAddressOf(requester);
				if (leaver == null)
					return ReturnCode.ROLE_NOT_HANDLED;
				rc = r.removeMember(requester);
				if (rc != SUCCESS)// TODO remove that
					throw new AssertionError("cannot remove " + requester + " from " + r.buildAndGetAddresses());
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_ROLE, leaver), netUpdater);
			}
			else{
				rc = r.removeMember(requester);
			}
			if(rc == SUCCESS){
				r.removeFromOverlookers(requester);
				if (logger != null)
					informHooks(AgentActionEvent.LEAVE_ROLE, community, group, role);
			}
			return rc;
		}
	}

	// Warning never touch this without looking at the logged kernel
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role,
			boolean callerIncluded) {
		try {
			if (callerIncluded) {
				return getRole(community, group, role).getAgentAddressesCopy();
			}
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
		try {
			AgentAddress receiver = getAnotherRolePlayer(requester, community, group, role);
			if (receiver == null) {
				return NO_RECIPIENT_FOUND;
			}
			return buildAndSendMessage(getSenderAgentAddress(requester, receiver, senderRole), receiver, message);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, final Message message, final String senderRole) {
		// check that the AA is valid : the targeted agent is still playing the
		// corresponding role or it was a candidate request
		if (!receiver.exists()) {// && !
			// receiver.getRole().equals(Roles.GROUP_CANDIDATE_ROLE)){
			return INVALID_AGENT_ADDRESS;
		}
		try {
			// get the role for the sender and then send
			return buildAndSendMessage(getSenderAgentAddress(requester, receiver, senderRole), receiver, message);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	ReturnCode broadcastMessageWithRole(final AbstractAgent requester, final String community, final String group,
			final String role, final Message messageToSend, String senderRole) {
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester, community, group, role);
			if (receivers == null)
				// the requester is the only agent in this group
				return NO_RECIPIENT_FOUND;
			messageToSend.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			// TODO consistency on senderRole
			broadcasting(receivers, messageToSend);
			if (logger != null) {
				informHooks(AgentActionEvent.BROADCAST_MESSAGE, community, group, role, messageToSend, senderRole);
			}
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	List<Message> broadcastMessageWithRoleAndWaitForReplies(final AbstractAgent requester, final String community,
			final String group, final String role, Message message, final String senderRole, final Integer timeOutMilliSeconds) {
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester, community, group, role);
			if (receivers == null)
				return null; // the requester is the only agent in this group
			message.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			broadcasting(receivers, message);
			return requester.waitAnswers(message, receivers.size(), timeOutMilliSeconds);
		} catch (CGRNotAvailable e) {
			if (requester.getKernel() != this && requester.isWarningOn()) {// is
																								// loggable
				final ReturnCode r = e.getCode();
				if (r == NO_RECIPIENT_FOUND) {
					requester.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new MadkitWarning(r));
				} else if (r == ROLE_NOT_HANDLED) {
					requester.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new OrganizationWarning(r, community, group,
							senderRole));
				} else {
					requester
							.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new OrganizationWarning(r, community, group, role));
				}
			}
			return null;
		}
	}

	private void broadcasting(final Collection<AgentAddress> receivers, Message m) {// TODO
																									// optimize
																									// without
																									// cloning
		for (final AgentAddress agentAddress : receivers) {
			if (agentAddress != null) {// TODO this should not be possible
				m = m.clone();
				m.setReceiver(agentAddress);
				sendMessage(m, agentAddress.getAgent());
			}
		}
	}

	private final ReturnCode sendMessage(Message m, AbstractAgent target) {
		if (target == null) {
			return sendNetworkMessageWithRole(new ObjectMessage<Message>(m), netEmmiter);
		}
		target.receiveMessage(m);
		return SUCCESS;
	}

	private final ReturnCode sendNetworkMessageWithRole(Message m, AgentAddress role) {
		updateNetworkAgent();
		if (netAgent != null) {
			m.setSender(role);
			m.setReceiver(netAgent);
			netAgent.getAgent().receiveMessage(m);
			return SUCCESS;
		}
		return SEVERE;
	}

	private void updateNetworkAgent() {
		if (netAgent == null || !netAgent.exists()) {// Is it still playing the
			// role ?
			netAgent = getAgentWithRole(LocalCommunity.NAME, Groups.NETWORK, madkit.agr.LocalCommunity.Roles.NET_AGENT);
		}
	}

// TODO Remove unused code found by UCDetector
// 	boolean isOnline() {
// 		getMadkitKernel().updateNetworkAgent();
// 		return getMadkitKernel().netAgent != null;
// 	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Launching and Killing
	// ////////////////////////////////////////////////////////////
	
//	List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent requester, String agentClass,
//			int bucketSize, String... cgrLocations) {
//		List<AbstractAgent> bucket = null;
//		try {
//			bucket = createBucket(agentClass, bucketSize);
//		} catch (InstantiationException e) {
//			requester.cannotLaunchAgent(agentClass, e, null);
//		} catch (IllegalAccessException e) {
//			requester.cannotLaunchAgent(agentClass, e, null);
//		} catch (ClassNotFoundException e) {
//			requester.cannotLaunchAgent(agentClass, e, null);
//		}
//
//		launchAgentBucketWithRoles(requester, bucket, cgrLocations);
//		return bucket;
//	}

	/**
	 * @param requester
	 * @param bucket
	 * @param cgrLocations
	 */
	void launchAgentBucketWithRoles(final AbstractAgent requester,
			List<AbstractAgent> bucket, String... cgrLocations) {
		AgentsJob aj = new AgentsJob() {
			@Override
			void proceedAgent(AbstractAgent a) {
				// no need to test : I created these instances
				a.state.set(ACTIVATED);
				a.setKernel(MadkitKernel.this);
				a.getAlive().set(true);
				a.logger = null;
			}
		};

		// initialization
		doMulticore(serviceExecutor, aj.getJobs(bucket));

		aj = new AgentsJob() {
			@Override
			void proceedAgent(final AbstractAgent a) {
				try {
					a.activate();
				} catch (Throwable e) {
					requester.cannotLaunchAgent(a != null ? a.getClass().getName() : "launchAgentBucketWithRoles : list contains null", e, null);
				}
			}
		};
		if (cgrLocations != null && cgrLocations.length != 0) {
			for (final String cgrLocation : cgrLocations) {
				final String[] cgr = cgrLocation.split(";");
				if (cgr.length != 3){
					throw new IllegalArgumentException(cgrLocation);
				}
				createGroup(requester, cgr[0], cgr[1], null, false);
				Group g = null;
				try {
					g = getGroup(cgr[0], cgr[1]);
				} catch (CGRNotAvailable e) {
					//not possible
					throw new AssertionError(e);
				}
				boolean roleCreated = false;
				Role r = g.get(cgr[2]);
				if (r == null) {
					r = g.createRole(cgr[2]);
					g.put(r.getRoleName(), r);
					roleCreated = true;
				}
				r.addMembers(bucket, roleCreated);
				// test vs assignement ? -> No: cannot touch the organizational
				// structure !!
			}
			synchronized (this) {
				bucketMode = true;
				doMulticore(serviceExecutor, aj.getJobs(bucket));
				bucketMode = false;
			}
		} else {
			doMulticore(serviceExecutor, aj.getJobs(bucket));
		}
	}

	final List<AbstractAgent> createBucket(final String agentClass, int bucketSize) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (shuttedDown)
			return null;
		@SuppressWarnings("unchecked")
		final Class<? extends AbstractAgent> constructor = (Class<? extends AbstractAgent>) getMadkitClassLoader().loadClass(
				agentClass);
		final int cpuCoreNb = Runtime.getRuntime().availableProcessors();
		final List<AbstractAgent> result = new ArrayList<AbstractAgent>(bucketSize);
		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
		final CompletionService<List<AbstractAgent>> ecs = new ExecutorCompletionService<List<AbstractAgent>>(serviceExecutor);
		for (int i = 0; i < cpuCoreNb; i++) {
			ecs.submit(new Callable<List<AbstractAgent>>() {
				public List<AbstractAgent> call() throws InvocationTargetException, InstantiationException, IllegalAccessException {
					final List<AbstractAgent> list = new ArrayList<AbstractAgent>(nbOfAgentsPerTask);
					for (int j = nbOfAgentsPerTask; j > 0; j--) {
						list.add(constructor.newInstance());
					}
					return list;
				}
			});
		}
		// adding the missing one when the division results as a real number
		for (int i = bucketSize - nbOfAgentsPerTask * cpuCoreNb; i > 0; i--) {
			result.add(constructor.newInstance());
		}
		for (int i = 0; i < cpuCoreNb; ++i) {
			try {
				result.addAll(ecs.take().get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void doMulticore(Executor e, ArrayList<AgentsJob> arrayList) {
		final CompletionService<Void> ecs = new ExecutorCompletionService<Void>(e);
		for (final Callable<Void> s : arrayList)
			ecs.submit(s);
		for (int i = arrayList.size(); i > 0; i--) {
			try {
				ecs.take();
			} catch (InterruptedException ignore) {
				ignore.printStackTrace();
			}
		}
	}

	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds,
			final boolean defaultGUI) {
		try {
			if (logger != null)
				logger.finest(requester + " launching " + agent + " by " + Thread.currentThread());
			// if to == 0, this is still quicker than treating the case, this also
			// holds for Integer.MAX_VALUE
			return lifeExecutor.submit(new Callable<ReturnCode>() {
				public ReturnCode call() {
					return launchingAgent(agent, defaultGUI);
				}
			}).get(timeOutSeconds, TimeUnit.SECONDS);
			// System.err.println(lifeExecutor.getCompletedTaskCount());
			// System.err.println(lifeExecutor.allowsCoreThreadTimeOut());
		} catch (InterruptedException e) {// requester has been killed or
														// something
			requester.handleInterruptedException();
			return TIMEOUT;
		} catch (ExecutionException e) {// BUG on launching agent
			bugReport("Launching task failed on " + agent, e);
			return SEVERE;
		} catch (TimeoutException e) {// launch task time out
			return TIMEOUT;
		}
	}

	private ReturnCode launchingAgent(final AbstractAgent agent, boolean defaultGUI) {
		// All this has to be done by a system thread
		// because if the job starts, it has to be done till the end
		if (!agent.state.compareAndSet(NOT_LAUNCHED, INITIALIZING) || shuttedDown) {
			return ALREADY_LAUNCHED;
		}
		if(hooks != null)
			informHooks(AgentActionEvent.AGENT_STARTED, agent.getName());
		// System.err.println("adding "+agent.getName()+" using "+Thread.currentThread()+
		// agent.getState());
		agent.setKernel(this);
		if (defaultGUI)
			agent.createGUIOnStartUp();
		Level defaultLevel = LevelOption.agentLogLevel.getValue(getMadkitConfig());
		final AgentLogger agentLogger = agent.logger;
		if (agentLogger == AgentLogger.defaultAgentLogger) {// not changed in the
																				// constructor
			if (defaultLevel == Level.OFF) {// default not changed and global is
														// OFF
				agent.logger = null;
			} else {
				agent.setLogLevel(defaultLevel);
				agent.getLogger().setWarningLogLevel(LevelOption.warningLogLevel.getValue(getMadkitConfig()));
			}
		}
		final AgentExecutor ae = agent.getAgentExecutor();
		
		//AbstractAgent
		if (ae == null) {
			ReturnCode r = AGENT_CRASH;
			final Future<Boolean> activationAttempt = lifeExecutor.submit(new Callable<Boolean>() {
				public Boolean call() {
					return agent.activation();
				}
			});
			try {
				r = activationAttempt.get() ? SUCCESS : AGENT_CRASH;
			} catch (ExecutionException e) {
				bugReport(agent + " activation task failed using " + Thread.currentThread(), e);
			} catch (InterruptedException e) {
				bugReport(agent + " activation task failed using " + Thread.currentThread(), e);
			}
			if (r != SUCCESS) {
				synchronized (agent.state) {
					agent.state.notify();
				}
			} else {
				if (agent.isAlive()) {// ! self kill -> safe to make this here
					agent.state.set(LIVING);
				}
			}
			return r;
		}
		
		//Agent
		try {
			final Agent a = (Agent) agent;
			synchronized (threadedAgents) {
				// do that even if not started for cleaning properly
				threadedAgents.add(a);
			}
			ae.setThreadFactory(a.isDaemon() ? daemonAgentThreadFactory : normalAgentThreadFactory);
			if (!shuttedDown && ae.start().get()) {
				return SUCCESS;
			}
			return AGENT_CRASH;
		} catch (InterruptedException e) {
			if (!shuttedDown) {
				// Kernel cannot be interrupted !!
				bugReport(e);
				return SEVERE;
			}
		} catch (ExecutionException e) {
			if (!shuttedDown) {
				// Kernel cannot be interrupted !!
				bugReport(e);
				return SEVERE;
			}
		} catch (CancellationException e) {
			// This is the case when the agent is killed during activation
			return AGENT_CRASH;
		}
		return TIMEOUT;
	}

	ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, final int timeOutSeconds) {
		if (target.getState().compareTo(ACTIVATED) < 0) {
			return NOT_YET_LAUNCHED;
		}
		final Future<ReturnCode> killAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call() {
				return killingAgent(target, timeOutSeconds);
			}
		});
		try {
			return killAttempt.get();
		} catch (InterruptedException e) {// requester has been killed or
														// something
			requester.handleInterruptedException();
			return TIMEOUT;
		} catch (ExecutionException e) {// BUG kill failed
			bugReport("Kill failed: " + target, e);
			return SEVERE;
			// } catch (TimeoutException e) {// kill task time out
			// return TIMEOUT;
		}
	}

	private final ReturnCode killingAgent(final AbstractAgent target, int timeOutSeconds) {
		synchronized (target.state) {
			// this has to be done by a system thread : the job must be done
			if (!target.getAlive().compareAndSet(true, false)) {
				return ALREADY_KILLED;
			}
		}
		final AgentExecutor ae = target.getAgentExecutor();
		if (ae != null) {
			return killThreadedAgent((Agent) target, ae, timeOutSeconds);
		}
		stopAbstractAgentProcess(ACTIVATED, target);
		return startEndBehavior(target, timeOutSeconds, false);
	}

	private void stopAbstractAgentProcess(State s, AbstractAgent target) {
		final ThreadGroup group = normalAgentThreadFactory.getThreadGroup();
		final Thread[] list = new Thread[group.activeCount()];
		group.enumerate(list);
		final String threadName = target.getAgentThreadName(s);
		for (final Thread t : list) {
			if (t != null && t.getName().equals(threadName)) {
				stopAgentProcess(s, target, t);
				break;
			}
		}
	}

	/**
	 * @param s
	 * @param target
	 * @param t
	 * @return <code>true</code> if a hard kill has been done
	 */
	@SuppressWarnings("deprecation")
	private boolean stopAgentProcess(State s, AbstractAgent target, Thread t) {
		synchronized (target.state) {
			if (target.getState() == s && t.getName().equals(target.getAgentThreadName(s))) {
				if (logger != null) {
					logger.finer("Hard kill on " + target + " " + t.getName());
					// // logger.finer("\n----"+target+"------");
					// // dumpThreadStack(t);
					// // logger.finer("----------\n");
				}
				t.stop(new KilledException("brutal kill"));
				// long deb = System.currentTimeMillis();
				if (logger != null)
					logger.finer("now waiting for " + s + " to end on " + target);
				try {
					target.state.wait();// TODO really, no need to do more ??
				} catch (InterruptedException e) {
					bugReport(e);
				}
				return true;
				// long end = System.currentTimeMillis();
				// if(logger != null && end - deb > 5000)
				// logger.finer("!!!!!!!!!!!!!!!!!!!!!!waiting timeout hard kill on "+target);
			}
		}
		if (logger != null)
			logger.finer(s + " already done on " + target);
		return false;
	}

//	final ReturnCode killAbstractAgent(final AbstractAgent target, int timeOutSeconds) {
//		// still activating
//		stopAbstractAgentProcess(ACTIVATED, target);
//		return startEndBehavior(target, timeOutSeconds, false);
//	}

	/**
	 * @param target
	 * @param timeOutSeconds
	 * @param asDaemon
	 * @param r
	 * @return
	 */
	final ReturnCode startEndBehavior(final AbstractAgent target, int timeOutSeconds, boolean asDaemon) {
		ReturnCode r = SUCCESS;
		final ExecutorService executor = asDaemon ? serviceExecutor : lifeExecutor;
		if (timeOutSeconds != 0) {
			final Future<Boolean> endAttempt = executor.submit(new Callable<Boolean>() {
				public Boolean call() {
					return target.ending();
				}
			});
			try {
				endAttempt.get(timeOutSeconds, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.err.println("----------------------\n\n---------------------------------------------");
			} catch (ExecutionException e) {
				bugReport("Killing task failed on " + target, e);
			} catch (TimeoutException e) {
				r = TIMEOUT;
				// searchAndDestroyAgentThread(SimulationState.ENDING+"-"+target.hashCode());
				stopAbstractAgentProcess(State.ENDING, target);
			}
		}
//		if (!(target instanceof Agent)) {
			if (target.getAgentExecutor() == null) {
			target.terminate();
		}
		return r;
	}

	private final ReturnCode killThreadedAgent(Agent target, final AgentExecutor ae, int timeOutSeconds) {
		final Future<?> end = ae.getEndProcess();
		if (timeOutSeconds == 0) {
			end.cancel(false);
		}
		ae.getLiveProcess().cancel(false);
		ae.getActivate().cancel(false);
		Thread.yield();
		target.myThread.setPriority(Thread.MIN_PRIORITY);
		ReturnCode result = SUCCESS;
		if (!stopAgentProcess(ACTIVATED, target, target.myThread)) {
			stopAgentProcess(State.LIVING, target, target.myThread);
		}
		if (timeOutSeconds != 0) {
			try {
				end.get(timeOutSeconds, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CancellationException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				bugReport("kill task failed on " + target, e);
			} catch (TimeoutException e) {
				result = TIMEOUT;
			}
		}
		stopAgentProcess(State.ENDING, target, target.myThread);
		try {
			ae.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			bugReport(e);
		}
		return result;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Organization access
	// /////////////////////////////////////////////////////////////////////////
	private final Organization getCommunity(final String community) throws CGRNotAvailable {
		Organization org = organizations.get(community);
		if (org == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		return org;
	}

	final Group getGroup(final String community, final String group) throws CGRNotAvailable {
		Group g = getCommunity(community).get(group);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	final Role getRole(final String community, final String group, final String role) throws CGRNotAvailable {
		Role r = getGroup(community, group).get(role);
		if (r == null)
			throw new CGRNotAvailable(NOT_ROLE);
		return r;
	}

	/**
	 * @param abstractAgent
	 * @param community
	 * @param group
	 * @param role
	 * @return null if nobody is found
	 * @throws CGRNotAvailable
	 *            if one of community, group or role does not exist
	 */
	final List<AgentAddress> getOtherRolePlayers(AbstractAgent abstractAgent, String community, String group, String role)
			throws CGRNotAvailable {
		// never null without throwing Ex
		final List<AgentAddress> result = getRole(community, group, role).getAgentAddressesCopy();
		Role.removeAgentAddressOf(abstractAgent, result);
		if (!result.isEmpty()) {
			return result;
		}
		return null;
	}

	final AgentAddress getAnotherRolePlayer(AbstractAgent abstractAgent, String community, String group, String role)
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
		final ReturnCode r = sendMessage(m, receiver.getAgent());
		if (logger != null && r == SUCCESS) {
			informHooks(AgentActionEvent.SEND_MESSAGE, m);
		}
		return r;
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

	private final AgentAddress getSenderAgentAddress(final AbstractAgent sender, final AgentAddress receiver, String senderRole)
			throws CGRNotAvailable {
		AgentAddress senderAA = null;
		final Role targetedRole = receiver.getRoleObject();

		//no role given
		if (senderRole == null) {
			// looking for any role in this group, starting with the receiver role
			senderAA = targetedRole.getAgentAddressInGroup(sender);
			// if still null : this SHOULD be a candidate's request to the manager or it is an error
			if (senderAA == null) {
				if (targetedRole.getRoleName().equals(madkit.agr.Organization.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				throw new CGRNotAvailable(NOT_IN_GROUP);
			}
			return senderAA;
		}

			// message sent with a particular role : check that
			// look into the senderRole role if the agent is in
		final Role senderRoleObject = targetedRole.getMyGroup().get(senderRole);
		if (senderRoleObject != null) {
			senderAA = senderRoleObject.getAgentAddressOf(sender);
		}
		if (senderAA == null) {// if still null : this SHOULD be a
			// candidate's request to the manager or it
			// is an error
			if (senderRole.equals(madkit.agr.Organization.GROUP_CANDIDATE_ROLE)
					&& targetedRole.getRoleName().equals(
							madkit.agr.Organization.GROUP_MANAGER_ROLE))
				return new CandidateAgentAddress(sender, targetedRole,
						kernelAddress);
			if (targetedRole.getAgentAddressInGroup(sender) == null)
				throw new CGRNotAvailable(NOT_IN_GROUP);
			throw new CGRNotAvailable(ROLE_NOT_HANDLED);
		}
		return senderAA;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Simulation
	// /////////////////////////////////////////////////////////////////////////

	synchronized boolean addOverlooker(@SuppressWarnings("unused") final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
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

	synchronized boolean removeOverlooker(@SuppressWarnings("unused") final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
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
			for (final String groupName : org.removeAgentFromAllGroups(theAgent)) {
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(theAgent, new Role(org.getName(), groupName),
						kernelAddress)), netUpdater);
			}
		}
	}

	@Override
	public Properties getMadkitConfig() {
		return platform.getConfigOption();
	}

	MadkitKernel getMadkitKernel() {
		return this;
	}

	boolean isCommunity(@SuppressWarnings("unused") AbstractAgent requester, String community) {
		try {
			return getCommunity(community) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	boolean isGroup(@SuppressWarnings("unused") AbstractAgent requester, String community, String group) {
		try {
			return getGroup(community, group) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	boolean isRole(@SuppressWarnings("unused") final AbstractAgent requester, final String community, String group, String role) {
		try {
			return getRole(community, group, role) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	final void importDistantOrg(final Map<String, Map<String, Map<String, Set<AgentAddress>>>> distantOrg) {
		if (logger != null) 
			logger.finer("Importing org..."+distantOrg);
		synchronized (organizations) {
			for (final String communityName : distantOrg.keySet()) {
				Organization org = new Organization(communityName, this);
				Organization previous = organizations.putIfAbsent(communityName, org);
				if (previous != null) {
					org = previous;
				}
				org.importDistantOrg(distantOrg.get(communityName));
			}
		}
	}

	@Override
	final public Map<String, Map<String, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global) {
		Map<String, Map<String, Map<String, Set<AgentAddress>>>> export = new TreeMap<String, Map<String, Map<String, Set<AgentAddress>>>>();
		synchronized (organizations) {
			for (Map.Entry<String, Organization> org : organizations.entrySet()) {
				Map<String, Map<String, Set<AgentAddress>>> currentOrg = org.getValue().getOrgMap(global);
				if (!currentOrg.isEmpty())
					export.put(org.getKey(), org.getValue().getOrgMap(global));
			}
		}
		return export;
	}

	@Override
	public MadkitClassLoader getMadkitClassLoader() {
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
		final Message toInject = m.getContent();
		final AgentAddress receiver = toInject.getReceiver();
		final AgentAddress sender = toInject.getSender();
		try {
			final Role receiverRole = kernel.getRole(receiver.getCommunity(), receiver.getGroup(), receiver.getRole());
			receiver.setRoleObject(receiverRole);
			if (receiverRole != null) {
				final AbstractAgent target = receiverRole.getAbstractAgentWithAddress(receiver);
				if (target != null) {
					// updating sender address
					sender.setRoleObject(kernel.getRole(sender.getCommunity(), sender.getGroup(), sender.getRole()));
					target.receiveMessage(toInject);
				} else if (logger != null)
					logger.finer(m + " received but the agent address is no longer valid !! Current distributed org is "
							+ getOrganizationSnapShot(false));
			}
		} catch (CGRNotAvailable e) {
			kernel.bugReport("Cannot inject " + m + "\n" + getOrganizationSnapShot(false), e);
		}
	}

	final void injectOperation(CGRSynchro m) {
		final Role r = m.getContent().getRoleObject();
		if (r == null) {
			if (logger != null)
				logger.log(Level.FINE, "distant CGR " + m.getCode() + " update failed on " + m.getContent());
			return;
		}
		final String communityName = r.getCommunityName();
		final String groupName = r.getGroupName();
		final String roleName = r.getRoleName();
		try {
			synchronized (organizations) {
				switch (m.getCode()) {
				case CREATE_GROUP:
					Organization organization = getCommunity(communityName);
					if (organization == null) {
						organization = new Organization(communityName, this);
						organizations.put(communityName, organization);
					}// TODO what about the manager
					if (organization.putIfAbsent(groupName, new Group(communityName, groupName, m.getContent(), null, organization)) == null
							&& logger != null) {
						informHooks(AgentActionEvent.CREATE_GROUP, communityName, groupName, m.getContent());
					}
					// //nerver fails : no need to remove org
					// Organization organization = new Organization(communityName,
					// this);// no
					// final Organization tmpOrg =
					// organizations.putIfAbsent(communityName, organization);
					// if (tmpOrg != null) {
					// if (isGroup(communityName, groupName)) {
					// if (logger != null)
					// logger.finer("distant group creation by " + m.getContent() +
					// " aborted : already exists locally");//TODO what about the
					// manager
					// break;
					// }
					// organization = tmpOrg;
					// }
					// organization.put(groupName, new Group(communityName,
					// groupName, m.getContent(), null, organization));
					break;
				case REQUEST_ROLE:
					getGroup(communityName, groupName).addDistantMember(m.getContent());
					if (logger != null)
						informHooks(AgentActionEvent.REQUEST_ROLE, communityName, groupName, roleName, m.getContent());
					break;
				case LEAVE_ROLE:
					getRole(communityName, groupName, roleName).removeDistantMember(m.getContent());
					if (logger != null)
						informHooks(AgentActionEvent.LEAVE_ROLE, communityName, groupName, roleName, m.getContent());
					break;
				case LEAVE_GROUP:
					getGroup(communityName, groupName).removeDistantMember(m.getContent());
					if (logger != null)
						informHooks(AgentActionEvent.LEAVE_GROUP, communityName, groupName, m.getContent());
					break;
				// case CGRSynchro.LEAVE_ORG://TODO to implement
				// break;
				default:
					bugReport(new UnsupportedOperationException("case not treated in injectOperation"));
					break;
				}
			}
		} catch (CGRNotAvailable e) {
			if (logger != null)
				logger.log(Level.FINE, "distant CGR " + m.getCode() + " update failed on " + m.getContent(), e);
		}
	}

	private void exit() {
		if (ActionInfo.javawsIsOn) {// TODO no need
																					// for that now
																					// that all exit
																					// normally
			System.exit(0);
		}
		shuttedDown = true;
		sendNetworkMessageWithRole(new KernelMessage(KernelAction.EXIT), kernelRole);
		broadcastMessageWithRole(MadkitKernel.this, LocalCommunity.NAME, Groups.GUI, madkit.agr.Organization.GROUP_MANAGER_ROLE,
				new KernelMessage(KernelAction.EXIT), null);
		// pause(10);//be sure that last executors have started
		if (logger != null)
			logger.finer("***** SHUTINGDOWN MADKIT ********\n");
		killAgents(true);
	}

	private void launchNetwork() {
		updateNetworkAgent();
		if (netAgent == null) {
			NetworkAgent na = new NetworkAgent();
			ReturnCode r = launchAgent(na);
			threadedAgents.remove(na);
			if (r == SUCCESS) {
				if (logger != null)
					logger.fine("\n\t****** Network agent launched ******\n");
			}// TODO i18n
			else {
				if (logger != null)
					logger.severe("\n\t****** Problem launching network agent ******\n");
			}
		} else {
			if (sendNetworkMessageWithRole(new KernelMessage(KernelAction.LAUNCH_NETWORK), kernelRole) == SUCCESS) {
				if (logger != null)
					logger.fine("\n\t****** Network agent up ******\n");
			} else {
				if (logger != null)
					logger.fine("\n\t****** Problem relaunching network ******\n");
			}

		}
	}

	// BAD IDEA AS IT ALLOWS CONFIG AGENT ZOMBIES TO RUN
	// /**
	// * This allows to not have dirty stack traces when ending and config agents
	// are not all launched
	// *
	// * @see madkit.kernel.AbstractAgent#terminate()
	// */
	// @Override
	// final void terminate() {
	// if (logger != null) {
	// logger.finer("** TERMINATED **");
	// }
	// }

	private void killAgents(boolean untilEmpty) {
		threadedAgents.remove(this);
		// Do not do what follows because it throws interruption on awt threads !
		// //TODO why ?
		// if(untilEmpty)
		// normalAgentThreadFactory.getThreadGroup().interrupt();
		ArrayList<Agent> l;
		synchronized (threadedAgents) {
			l = new ArrayList<Agent>(threadedAgents);
		}
		do {
			for (final Agent a : l) {
				killAgent(this, a, 0);
			}
		} while (untilEmpty && !threadedAgents.isEmpty());
	}

	boolean createGroupIfAbsent(AbstractAgent abstractAgent, String community, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, gatekeeper, isDistributed) == SUCCESS;
	}

	private void bugReport(Throwable e) {
		bugReport("", e);
	}

	private void bugReport(String m, Throwable e) {
		getMadkitKernel().getLogger().severeLog("********************** KERNEL PROBLEM, please bug report " + m, e); // Kernel
	}

	final synchronized void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		for (final Organization org : organizations.values()) {
			org.removeAgentsFromDistantKernel(kernelAddress2);
		}
	}

	synchronized ReturnCode destroyCommunity(@SuppressWarnings("unused") AbstractAgent abstractAgent, String community) {
		try {
			getCommunity(community).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	synchronized ReturnCode destroyGroup(@SuppressWarnings("unused") AbstractAgent abstractAgent, String community, String group) {
		try {
			getGroup(community, group).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	synchronized ReturnCode destroyRole(@SuppressWarnings("unused") AbstractAgent abstractAgent, String community, String group, String role) {
		try {
			getRole(community, group, role).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	void removeThreadedAgent(Agent myAgent) {
		synchronized (threadedAgents) {
			threadedAgents.remove(myAgent);
			if(logger != null)
				logger.finest(threadedAgents.toString());
		}
	}

	AgentAddress getAgentAddressIn(AbstractAgent agent, String community, String group, String role) {
		try {
			return getRole(community, group, role).getAgentAddressOf(agent);
		} catch (CGRNotAvailable e) {
			if (agent.isWarningOn()) {
				agent.setAgentStackTrace(e);
				agent.handleException(Influence.GET_AGENT_ADDRESS_IN, new OrganizationWarning(e.getCode(), community, group, role));
			}
			return null;
		}
	}

	final boolean isHooked() {
		return hooks != null;
	}

}

final class CGRNotAvailable extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -375379801933609564L;
	final private ReturnCode code;

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

	// @Override
	// public synchronized Throwable fillInStackTrace() {
	// return null;
	// }

}

abstract class AgentsJob implements Callable<Void>, Cloneable {
	private List<AbstractAgent> list;

	@Override
	public Void call() throws Exception {
		for (final AbstractAgent a : list) {
			proceedAgent(a);
		}
		return null;
	}

	final ArrayList<AgentsJob> getJobs(List<AbstractAgent> l) {
		final int cpuCoreNb = Runtime.getRuntime().availableProcessors();
		final ArrayList<AgentsJob> workers = new ArrayList<AgentsJob>(cpuCoreNb);
		int bucketSize = l.size();
		final int nbOfAgentsPerTask = bucketSize / cpuCoreNb;
		if (nbOfAgentsPerTask == 0) {
			list = l;
			workers.add(this);
			return workers;
		}
		for (int i = 0; i < cpuCoreNb; i++) {
			int firstIndex = nbOfAgentsPerTask * i;// TODO check that using junit
			workers.add(createNewAgentJobWithList(l.subList(firstIndex, firstIndex + nbOfAgentsPerTask)));
			// System.err.println("from "+firstIndex+
			// " to "+(firstIndex+nbOfAgentsPerTask));
		}
		workers.add(createNewAgentJobWithList(l.subList(cpuCoreNb * nbOfAgentsPerTask, l.size())));
		// System.err.println("from "+cpuCoreNb*nbOfAgentsPerTask+
		// " to "+l.size());
		return workers;
	}

	@SuppressWarnings("null")
	private AgentsJob createNewAgentJobWithList(List<AbstractAgent> l) {
		AgentsJob aj = null;
		try {
			aj = (AgentsJob) this.clone();
		} catch (CloneNotSupportedException e) {
			//cannot be
		}
		aj.list = l;
		return aj;
	}

	/**
	 * Is the job to do on one agent
	 * @param a
	 */
	abstract void proceedAgent(AbstractAgent a);
}
