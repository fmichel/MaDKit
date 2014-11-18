/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import madkit.action.GlobalAction;
import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.ConsoleAgent;
import madkit.gui.MASModel;
import madkit.i18n.ErrorMessages;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.message.BooleanMessage;
import madkit.message.KernelMessage;
import madkit.message.ObjectMessage;
import madkit.message.hook.AgentLifeEvent;
import madkit.message.hook.HookMessage;
import madkit.message.hook.HookMessage.AgentActionEvent;
import madkit.message.hook.MessageEvent;
import madkit.message.hook.OrganizationEvent;
import madkit.util.MadkitProperties;

import org.xml.sax.SAXException;

/**
 * The brand new MaDKit kernel and it is now a real Agent :)
 * 
 * @author Fabien Michel
 * @version 1.4
 * @since MaDKit 5.0
 * 
 */
class MadkitKernel extends Agent {

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
		threadedAgents = new HashSet<>(20);
		kernelAddress = new KernelAddress();
		
		//set the log dir name and checking uniqueness
		final String logDirKey = Option.logDirectory.name();
		final MadkitProperties madkitConfig = getMadkitConfig();
		final String logBaseDir= madkitConfig.getProperty(logDirKey) + File.separator;
		String logDir = logBaseDir + Madkit.dateFormat.format(new Date())+kernelAddress;
		while (new File(logDir).exists()) {
			logDir = logBaseDir + Madkit.dateFormat.format(new Date())+kernelAddress;
		}
		madkitConfig.setProperty(logDirKey, logDir);
		
		organizations = new ConcurrentHashMap<>();
		operatingOverlookers = new LinkedHashSet<>();
		loggedKernel = new LoggedKernel(this);

		getLogger(); // Bootstrapping the agentLoggers with default logger variable for global actions
		setLogLevel(LevelOption.kernelLogLevel.getValue(madkitConfig));
		if(logger != null && BooleanOption.createLogFiles.isActivated(madkitConfig)){
			logger.createLogFile();
		}
		
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

	/**
	 * for the logged kernel
	 */
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
//		addWebRepository();
		if (logger != null)
			logger.setWarningLogLevel(Level.INFO);
		
		//denying all requests for this group
		createGroup(LocalCommunity.NAME, Groups.SYSTEM, false, new Gatekeeper() {
			@Override
			public boolean allowAgentToTakeRole(String req, String roleName, Object memberCard) {
				return false;
			}
		});
		
		createGroup(LocalCommunity.NAME,"kernels",true);//TODO

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
			GlobalAction.LOAD_LOCAL_DEMOS.actionPerformed(null);
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
		launchXMLConfigurations();
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
		while (! shuttedDown) {
			handleMessage(waitNextMessage());// As a daemon, a timeout is not required
		}
	}
	
	@Override
	public boolean isAlive() {
		return super.isAlive() && ! shuttedDown;
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
			final Constructor<?> c = MadkitClassLoader.getLoader().loadClass("madkit.gui.GUIManagerAgent").getDeclaredConstructor(
					boolean.class);
			c.setAccessible(true);
			final AbstractAgent a = (AbstractAgent) c.newInstance(!BooleanOption.desktop.isActivated(getMadkitConfig()));
			// c.setAccessible(false); //useless
			a.setLogLevel(LevelOption.guiLogLevel.getValue(getMadkitConfig()));
			launchAgent(a);
			threadedAgents.remove(a);
			if (logger != null)
				logger.fine("\n\t****** GUI Manager launched ******\n");
		} catch (ClassNotFoundException | SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			bugReport(e);
		}
		// }
	}

	@SuppressWarnings("unused")
	private void connectToIp(InetAddress ip){
		launchNetwork();
		sendNetworkMessageWithRole(new KernelMessage(KernelAction.CONNECT_TO_IP,ip), kernelRole);
	}

	private void copy() {
		startSession(false);
	}

	@SuppressWarnings("unused")
	private void restart() {
		new java.util.Timer().schedule(new TimerTask() {
		@Override
		public void run() {
			try {
				myThread.join();
			} catch (InterruptedException e) {
			}
			copy();
			}
		}, 100);
		exit();
//				copy();
	}

	/**
	 * 
	 */
	private void addWebRepository() {
		final String repoLocation = getMadkitConfig().getProperty("madkit.repository.url");
		if (logger != null)
			logger.fine("** CONNECTING WEB REPO **" + repoLocation);
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new URL(getMadkitProperty("madkit.repository.url")).openStream()));
			for(String s : in.readLine().split("<br/>")){
				MadkitClassLoader.loadUrl(new URL(s));
			}
			in.close();
		} catch (IOException e) {
			if (logger != null)
			logger.log(Level.WARNING, ErrorMessages.CANT_CONNECT + ": "+ repoLocation + "\n" + e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	private void launchMas(MASModel dm) {
		if (logger != null)
			logger.finer("** LAUNCHING SESSION " + dm.getName());
		Properties mkCfg = platform.getConfigOption();
		Properties currentConfig = new Properties();
		currentConfig.putAll(mkCfg);
		mkCfg.putAll(platform.buildConfigFromArgs(dm.getSessionArgs()));
		//TODO parse config File 
		launchConfigAgents();
		mkCfg.putAll(currentConfig);
	}
	
	@SuppressWarnings("unused")
	private void launchXml(String xmlFile, boolean inNewMadkit) {
		if (logger != null)
			logger.finer("** LAUNCHING XML CONFIG " + xmlFile);
		if (inNewMadkit) {
			new Madkit(Option.configFile.toString(),xmlFile);
		}
		else{
			MadkitProperties mkCfg = platform.getConfigOption();
			Properties currentConfig = new Properties();
			currentConfig.putAll(mkCfg);
			try {
				mkCfg.loadPropertiesFromMaDKitXML(xmlFile);
				launchXmlAgents(xmlFile);
			} catch (IOException | SAXException | ParserConfigurationException e) {
				getLogger().severeLog("",e);
			}
			mkCfg.putAll(currentConfig);
		}
	}
	
	@SuppressWarnings("unused")
	private void console() {
		launchAgent(ConsoleAgent.class.getName());
	}

	private void launchConfigAgents() {
//		final ExecutorService startExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() );// TODO
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
				try {
					final Class<?> agentClass = MadkitClassLoader.getLoader().loadClass(className);
					for (int i = 0; i < number; i++) {
						lifeExecutor.execute(new Runnable() {
							public void run() {
								if (!shuttedDown) {
									try {
										launchAgent((AbstractAgent) agentClass.newInstance(), 0, withGUI);
									} catch (Exception e) {
										cannotLaunchAgent(className, e, null);
									}
								}
							}
						});
					}
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
//			startExecutor.shutdown();
		}
	}
	
	private void launchXMLConfigurations() {
		if (logger != null)
			logger.fine("** LAUNCHING XML CONFIGS **");
		final String filesName = getMadkitProperty(Option.configFile);
		if (!filesName.equals("null")) {
			for (final String fileName : filesName.split(";")) {
				if (fileName.endsWith(".xml")) {
					lifeExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								if (logger != null)
									logger.finer("Launching xml " + fileName);
								launchXmlAgents(fileName);
							} catch (SAXException | IOException
									| ParserConfigurationException e) {
								getLogger().severeLog("xml config", e);
								e.printStackTrace();
							}
						}
					});
				}
			}
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
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					new Madkit(platform.args);
				}
			});
			t.setDaemon(false);
			t.start();
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
		} else if (m instanceof RequestRoleSecure) {
			handleRequestRoleSecure((RequestRoleSecure) m);
		} else {
			if (logger != null)
				logger.warning("I received a message that I do not understand. Discarding " + m);
		}
	}

	private void handleRequestRoleSecure(RequestRoleSecure m) {
		AgentAddress requesterAddress = m.getRequester();
		Group g = null;
		try {
			g = getGroup(requesterAddress.getCommunity(), requesterAddress.getGroup());
		} catch (CGRNotAvailable e) {
		}
		sendReply(m, new BooleanMessage(g != null && g.getGatekeeper().allowAgentToTakeRole(requesterAddress.getAgentNetworkID(), m.getRoleName(), m.getContent())));
	}

	private void handleHookRequest(HookMessage m) {
		if (hooks == null) {
			hooks = new EnumMap<>(AgentActionEvent.class);
		}
		Set<AbstractAgent> l = hooks.get(m.getContent());
		if (l == null) {
			l = new HashSet<>();
			hooks.put(m.getContent(), l);
		}
		final AbstractAgent requester = m.getSender().getAgent();
		// for speeding up if there is no hook, i.e. logger == null is default
		getLogger().setLevel(Level.INFO);
		if (! l.add(requester)) {
			l.remove(requester);
			if(l.isEmpty()){
				hooks.remove(m.getContent());
				if(hooks.isEmpty())
					hooks = null;
			}
		}
	}

	private void launchNetworkAgent() {
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

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final Gatekeeper gatekeeper, final boolean isDistributed) {
		if (group == null)
			throw new NullPointerException(ErrorMessages.G_NULL.toString());
		Organization organization = new Organization(community, this);
		// no need to remove org: never failed
		// will throw null pointer if community is null
		final Organization tmpOrg = organizations.putIfAbsent(community,
				organization);
		if (tmpOrg != null) {
			organization = tmpOrg;
		}
		synchronized (organization) {
			if (!organization.addGroup(creator, group, gatekeeper, isDistributed)) {
				return ALREADY_GROUP;
			}
			try {//TODO bof...
				if (isDistributed) {
					sendNetworkMessageWithRole(
							new CGRSynchro(CREATE_GROUP, 
									getRole(community, group, madkit.agr.Organization.GROUP_MANAGER_ROLE).getAgentAddressOf(creator)), 
									netUpdater);
				}
				if (hooks != null) {
					informHooks(AgentActionEvent.CREATE_GROUP, 
							getRole(community, group, madkit.agr.Organization.GROUP_MANAGER_ROLE).getAgentAddressOf(creator));
				}
			} catch (CGRNotAvailable e) {
				getLogger().severeLog("Please bug report", e);
			}
		}
		return SUCCESS;
	}

		void informHooks(AgentActionEvent action, Object parameter) {
		if (hooks != null) {
			final Set<AbstractAgent> l = hooks.get(action);
			if (l != null) {
				HookMessage hm = null;
				switch(action){
				case CREATE_GROUP:
				case REQUEST_ROLE:
				case LEAVE_GROUP:
				case LEAVE_ROLE:
					hm = new OrganizationEvent(action,(AgentAddress) parameter);
					break;
				case BROADCAST_MESSAGE:
				case SEND_MESSAGE:
					hm = new MessageEvent(action,(Message) parameter);
					break;
				case AGENT_STARTED:
				case AGENT_TERMINATED:
					hm = new AgentLifeEvent(action,(AbstractAgent) parameter);
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
	 * @param community
	 * @param memberCard
	 * @param roleName
	 * @param groupName
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
		if (result == SUCCESS) {
			if (g.isDistributed()) {
				sendNetworkMessageWithRole(new CGRSynchro(REQUEST_ROLE, new AgentAddress(requester, g.get(role), kernelAddress)), netUpdater);
			}
			if (hooks != null)
				informHooks(AgentActionEvent.REQUEST_ROLE, new AgentAddress(requester, g.get(role), kernelAddress));
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
			for (final Role role : affectedRoles) {
				role.removeFromOverlookers(requester);
			}
			if (g.isDistributed()) {
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_GROUP, new AgentAddress(requester, new Role(community, group),
						kernelAddress)), netUpdater);
			}
			if (hooks != null)//should not be factorized to avoid useless object creation
				informHooks(AgentActionEvent.LEAVE_GROUP, new AgentAddress(requester, new Role(community, group), kernelAddress));
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
				sendNetworkMessageWithRole(new CGRSynchro(LEAVE_ROLE, new AgentAddress(requester, r, kernelAddress)), netUpdater);
			}
			else{
				rc = r.removeMember(requester);
			}
			if(rc == SUCCESS){
				r.removeFromOverlookers(requester);
				if (hooks != null){
					informHooks(AgentActionEvent.LEAVE_ROLE, new AgentAddress(requester, r, kernelAddress));
				}
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

	AgentAddress getDistantAgentWithRole(AbstractAgent abstractAgent,
			String community, String group, String role, KernelAddress from) {
		try {
			List<AgentAddress> l = getOtherRolePlayers(abstractAgent, community, group, role);
			if(l != null){
				for (AgentAddress agentAddress : l) {
					if(agentAddress.getKernelAddress().equals(from))
						return agentAddress;
				}
			}
		} catch (CGRNotAvailable e) {
		}
		return null;
	}

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
//		if (! receiver.exists()) {// && !
		final AgentAddress target = resolveAddress(receiver);
		if (target == null && ! (receiver instanceof CandidateAgentAddress)) {
			return INVALID_AGENT_ADDRESS;
		}
		try {
			// get the role for the sender and then send
			return buildAndSendMessage(getSenderAgentAddress(requester, target, senderRole), target, message);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	final AgentAddress resolveAddress(AgentAddress receiver) {
		final Role roleObject = receiver.getRoleObject();
		if(roleObject != null){
			if(roleObject.players == null){//has been traveling
				try {
					return getRole(roleObject.getCommunityName(), roleObject.getGroupName(),roleObject.getRoleName()).resolveAgentAddress(receiver);
				} catch (CGRNotAvailable e) {
					return null;
				}
			}
			return receiver;
		}
		return null;
	}

	ReturnCode broadcastMessageWithRole(final AbstractAgent requester, final String community, final String group,
			final String role, final Message messageToSend, String senderRole) {
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester, community, group, role);
			if (receivers == null)
				// the requester is the only agent in this group
				return NO_RECIPIENT_FOUND;
			final AgentAddress senderAgentAddress = getSenderAgentAddress(requester, receivers.get(0), senderRole);
			messageToSend.setSender(senderAgentAddress);
			// TODO consistency on senderRole
			broadcasting(receivers, messageToSend);
			if (hooks != null) {
				messageToSend.setReceiver(receivers.get(0));
				informHooks(AgentActionEvent.BROADCAST_MESSAGE, messageToSend);
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
			m.getConversationID().setOrigin(kernelAddress);
			return sendNetworkMessageWithRole(new ObjectMessage<>(m), netEmmiter);
		}
		target.receiveMessage(m);
		return SUCCESS;
	}

	final ReturnCode sendNetworkMessageWithRole(Message m, AgentAddress role) {
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
		if (netAgent == null || ! checkAgentAddress(netAgent)) {// Is it still playing the
			// role ?
			netAgent = getAgentWithRole(LocalCommunity.NAME, Groups.NETWORK, madkit.agr.LocalCommunity.Roles.NET_AGENT);
		}
	}

	// ////////////////////////////////////////////////////////////
	// //////////////////////// Launching and Killing
	// ////////////////////////////////////////////////////////////
	
	/**
	 * @param requester
	 * @param bucket
	 * @param cpuCoreNb the number of parallel tasks to use. 
	 * Beware that if cpuCoreNb is greater than 1, the agents' {@link #activate()} methods
	 * will be called simultaneously so that one has to be careful if shared resources are
	 * accessed 
	 * @param cgrLocations
	 */
	void launchAgentBucketWithRoles(final AbstractAgent requester, List<AbstractAgent> bucket, int cpuCoreNb, String... cgrLocations) {
		if (cgrLocations != null && cgrLocations.length != 0) {
			AgentsJob init = new AgentsJob() {
				@Override
				void proceedAgent(final AbstractAgent a) {
					// no need to test : I created these instances :this is not true for the list case //TODO
					a.state.set(INITIALIZING);
					a.setKernel(MadkitKernel.this);
					a.getAlive().set(true);
					a.logger = null;
				}
			};
			doMulticore(init.getJobs(bucket, cpuCoreNb));
			synchronized (this) {
				for (final String cgrLocation : cgrLocations) {
					final String[] cgr = cgrLocation.split(",");
					if (cgr.length != 3) {
						throw new IllegalArgumentException("\"" + cgrLocation + "\" is incorrect. As of MDK 5.0.2, correct format is \"C,G,R\" ");
					}
					createGroup(requester, cgr[0], cgr[1], null, false);
					Group g = null;
					try {
						g = getGroup(cgr[0], cgr[1]);
					} catch (CGRNotAvailable e) {
						// not possible
						throw new AssertionError(e);
					}
					boolean roleCreated = false;
					Role r = g.get(cgr[2]);
					if (r == null) {
						r = g.createRole(cgr[2]);
						roleCreated = true;
					}
					r.addMembers(bucket, roleCreated);
					// test vs assignement ? -> No: cannot touch the organizational
					// structure !!
				}
				init = new AgentsJob() {
					@Override
					void proceedAgent(final AbstractAgent a) {
						try {
							a.activate();
							a.state.set(ACTIVATED);
						} catch (Throwable e) {
							requester.cannotLaunchAgent(a != null ? a.getClass().getName() : "launchAgentBucketWithRoles : list contains null", e, null);
						}
					}
				};
				doMulticore(init.getJobs(bucket, cpuCoreNb));
			}
		}
		else {
			AgentsJob aj = new AgentsJob() {
				@Override
				void proceedAgent(final AbstractAgent a) {
					// no need to test : I created these instances :this is not true for the list case //TODO
					a.state.set(ACTIVATED);
					a.setKernel(MadkitKernel.this);
					a.getAlive().set(true);
					a.logger = null;
					try {
						a.activate();
					} catch (Throwable e) {
						requester.cannotLaunchAgent("launchAgentBucketWithRoles : "+a.getClass().getName(), e, null);
					}
				}
			};
			doMulticore(aj.getJobs(bucket, cpuCoreNb));
		}
	}

	/**
	 * Creates all the instances for launching
	 * 
	 * @param agentClass
	 * @param bucketSize
	 * @param cpuCoreNb
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	final List<AbstractAgent> createBucket(final String agentClass, int bucketSize, int cpuCoreNb) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		final Class<? extends AbstractAgent> constructor = (Class<? extends AbstractAgent>) MadkitClassLoader.getLoader().loadClass(agentClass);
		cpuCoreNb = cpuCoreNb > 0 ? cpuCoreNb : 1;
		final List<AbstractAgent> result = new ArrayList<>(bucketSize);
		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
		final CompletionService<List<AbstractAgent>> ecs = new ExecutorCompletionService<>(serviceExecutor);
		for (int i = 0; i < cpuCoreNb; i++) {
			ecs.submit(new Callable<List<AbstractAgent>>() {
				public List<AbstractAgent> call() throws InvocationTargetException, InstantiationException, IllegalAccessException {
					final List<AbstractAgent> list = new ArrayList<>(nbOfAgentsPerTask);
					for (int j = nbOfAgentsPerTask; j > 0; j--) {
						list.add(constructor.newInstance());
					}
					return list;
				}
			});
		}
		// adding the missing ones when the division results as a real number
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

	private void doMulticore(ArrayList<AgentsJob> arrayList) {
		try {
			serviceExecutor.invokeAll(arrayList);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds,
			final boolean defaultGUI) {
		try {
			if (logger != null)
				logger.finest(requester + " launching " + agent + " by " + Thread.currentThread());
			// if to == 0, this is still quicker than treating the case, this also
			// holds for Integer.MAX_VALUE
			final ReturnCode returnCode = lifeExecutor.submit(new Callable<ReturnCode>() {
				public ReturnCode call() {
					return launchingAgent(agent, defaultGUI);
				}
			}).get(timeOutSeconds, TimeUnit.SECONDS);
			if(returnCode == AGENT_CRASH || returnCode == ALREADY_LAUNCHED){
				requester.getLogger().severeLog(Influence.LAUNCH_AGENT.failedString(), new MadkitWarning(agent.toString(),returnCode));
			}
			return returnCode;
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
		// System.err.println("adding "+agent.getName()+" using "+Thread.currentThread()+
		// agent.getState());
		agent.setKernel(this);
		informHooks(AgentActionEvent.AGENT_STARTED, agent);
		if (defaultGUI)
			agent.createGUIOnStartUp();
		Level defaultLevel = LevelOption.agentLogLevel.getValue(getMadkitConfig());
		if ( agent.logger == AgentLogger.defaultAgentLogger) {// not changed in the
																				// constructor
			if (defaultLevel == Level.OFF) {// default not changed and global is
														// OFF
				agent.logger = null;
			} else {
				agent.setLogLevel(defaultLevel);
				agent.getLogger().setWarningLogLevel(LevelOption.warningLogLevel.getValue(getMadkitConfig()));
			}
		}
//		final AgentExecutor ae = agent.getAgentExecutor();
		
		//AbstractAgent
		if (! (agent instanceof Agent)) {
			ReturnCode r = AGENT_CRASH;
			final Future<ReturnCode> activationAttempt = lifeExecutor.submit(new Callable<ReturnCode>() {
				public ReturnCode call() {
					return agent.activation();
				}
			});
			try {
				r = activationAttempt.get();
			} catch (ExecutionException e) {
				bugReport(agent + " activation task failed using " + Thread.currentThread(), e);
			} catch (InterruptedException e) {
				bugReport(agent + " activation task failed using " + Thread.currentThread(), e);
			}
			if (r != SUCCESS) {
				synchronized (agent.state) {
					agent.state.notify();
				}
				startEndBehavior(agent, 0, false);
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
			final AgentExecutor ae = a.getAgentExecutor();
			synchronized (threadedAgents) {
				// do that even if not started for cleaning properly
				threadedAgents.add(a);
			}
			ae.setThreadFactory(a.isDaemon() ? daemonAgentThreadFactory : normalAgentThreadFactory);
			if (!shuttedDown) {
				return ae.start().get();
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
		if (target instanceof Agent && ((Agent) target).myThread != null) {
			//extends Agent and not launched in bucket mode
			return killThreadedAgent((Agent) target, timeOutSeconds);
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
		if (! (target instanceof Agent && ((Agent) target).myThread != null) ) {
			target.terminate();
		}
		return r;
	}

	private final ReturnCode killThreadedAgent(Agent target, int timeOutSeconds) {
		final AgentExecutor ae = target.getAgentExecutor();
		final Future<?> end = ae .getEndProcess();
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
			} catch (InterruptedException  | CancellationException e) {
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
//		System.err.println("HHHHHHHHHHHHHHHHHHHH "+community);
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
		if (r == SUCCESS && hooks != null) {
			informHooks(AgentActionEvent.SEND_MESSAGE, m);
		}
		return r;
	}

	final AgentAddress getSenderAgentAddress(final AbstractAgent sender, final AgentAddress receiver, String senderRole)
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
			} catch (CGRNotAvailable e) {//the role does not exist yet
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
	
	@Override
	public String getServerInfo(){
		if (netAgent != null) {
			return netAgent.getAgent().getServerInfo();
		}
		return "";
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
	public MadkitProperties getMadkitConfig() {
		return platform.getConfigOption();
	}

	MadkitKernel getMadkitKernel() {
		return this;
	}

	@Override
	public TreeSet<String> getExistingCommunities() {
		return new TreeSet<>(organizations.keySet());
	}
	
	@Override
	public TreeSet<String> getExistingGroups(String community) {
		try {
			return new TreeSet<>(getCommunity(community).keySet());
		} catch (CGRNotAvailable e) {
			return null;
		}
	}
	
	@Override
	public TreeSet<String> getExistingRoles(String community, String group) {
		try {
			return new TreeSet<>(getGroup(community,group).keySet());
		} catch (CGRNotAvailable e) {
			return null;
		}
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
		Map<String, Map<String, Map<String, Set<AgentAddress>>>> export = new TreeMap<>();
		synchronized (organizations) {
			for (Map.Entry<String, Organization> org : organizations.entrySet()) {
				Map<String, Map<String, Set<AgentAddress>>> currentOrg = org.getValue().getOrgMap(global);
				if (!currentOrg.isEmpty())
					export.put(org.getKey(), org.getValue().getOrgMap(global));
			}
		}
		return export;
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
					receiver.setAgent(target);
					try {
						sender.setRoleObject(kernel.getRole(sender.getCommunity(), sender.getGroup(), sender.getRole()));
					} catch (CGRNotAvailable e) {
						sender.setRoleObject(null);
					}
					target.receiveMessage(toInject);
					if (hooks != null) {
						informHooks(AgentActionEvent.SEND_MESSAGE, toInject);
					}
				} else if (logger != null)
					logger.finer(m + " received but the agent address is no longer valid !! Current distributed org is "
							+ getOrganizationSnapShot(false));
			}
		} catch (CGRNotAvailable e) {
			kernel.bugReport("Cannot inject " + m + "\n" + getOrganizationSnapShot(false), e);
		}
	}

	final void injectOperation(CGRSynchro m) {
		final AgentAddress agentAddress = m.getContent();
		final String communityName = agentAddress.getCommunity();
		final String groupName = agentAddress.getGroup();
		final String roleName = agentAddress.getRole();
		synchronized (organizations) {
			switch (m.getCode()) {
			case CREATE_GROUP:
				Organization organization = null;
				try {
					organization = getCommunity(communityName);
				} catch (CGRNotAvailable e) {
					organization = new Organization(communityName, this);
					organizations.put(communityName, organization);
				}
				if (organization.putIfAbsent(groupName, new Group(communityName, groupName, agentAddress, organization)) == null) {
					informHooks(AgentActionEvent.CREATE_GROUP, agentAddress);
				}
				break;
			case REQUEST_ROLE:
				try {
					getGroup(communityName, groupName).addDistantMember(agentAddress);
					informHooks(AgentActionEvent.REQUEST_ROLE, agentAddress);
				} catch (CGRNotAvailable e) {
					logInjectOperationFailure(m, agentAddress, e);
				}
				break;
			case LEAVE_ROLE:
				try {
					getRole(communityName, groupName, roleName).removeDistantMember(agentAddress);
					informHooks(AgentActionEvent.LEAVE_ROLE, agentAddress);
				} catch (CGRNotAvailable e) {
					logInjectOperationFailure(m, agentAddress, e);
				}
				break;
			case LEAVE_GROUP:
				try {
					getGroup(communityName, groupName).removeDistantMember(agentAddress);
					informHooks(AgentActionEvent.LEAVE_GROUP, agentAddress);
				} catch (CGRNotAvailable e) {
					logInjectOperationFailure(m, agentAddress, e);
				}
				break;
			// case CGRSynchro.LEAVE_ORG://TODO to implement
			// break;
			default:
				bugReport(new UnsupportedOperationException("case not treated in injectOperation"));
				break;
			}
		}
	}

	/**
	 * @param m
	 * @param agentAddress
	 * @param e
	 */
	private void logInjectOperationFailure(CGRSynchro m, final AgentAddress agentAddress, CGRNotAvailable e) {
		getLogger().log(Level.FINE, "distant CGR " + m.getCode() + " update failed on " + agentAddress, e);
	}

	@Override
	void terminate() {
//		AgentLogger.closeLoggersFrom(kernelAddress);
		super.terminate();
		if (LevelOption.madkitLogLevel.getValue(getMadkitConfig()) != Level.OFF) {
			System.out.println("\n\t---------------------------------------" 
					+ "\n\t         MaDKit Kernel " + kernelAddress
					+ " \n\t        is shutting down, Bye !" 
					+ "\n\t---------------------------------------\n");
		}
	}

	private void exit() {
		shuttedDown = true;
		sendNetworkMessageWithRole(new KernelMessage(KernelAction.EXIT), kernelRole);
		broadcastMessageWithRole(MadkitKernel.this, LocalCommunity.NAME, Groups.GUI, madkit.agr.Organization.GROUP_MANAGER_ROLE,
				new KernelMessage(KernelAction.EXIT), null);
		while (getAgentWithRole(LocalCommunity.NAME, Groups.GUI, madkit.agr.Organization.GROUP_MANAGER_ROLE) != null) {
			pause(10);
		}
		// pause(10);//be sure that last executors have started
		if (logger != null)
			logger.finer("***** SHUTINGDOWN MADKIT ********\n");
		killAgents(true);
		killAgent(this);
	}

	private void launchNetwork() {
		updateNetworkAgent();
		if (netAgent == null) {
			final NetworkAgent na = new NetworkAgent();
			final ReturnCode r = launchAgent(na);
			threadedAgents.remove(na);
			if (r == SUCCESS) {
//				requestRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, Roles.KERNEL);
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
			l = new ArrayList<>(threadedAgents);
		}
		do {
			for (final Agent a : l) {
				killAgent(this, a, 10);
			}
			pause(10);
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

	/**
	 * 
	 * @param abstractAgent
	 * @param community
	 * @return the group names the agent is in, or <code>null</code> if this
	 * community does not exist
	 */
	final TreeSet<String> getGroupsOf(AbstractAgent abstractAgent, String community) {
		final TreeSet<String> groups = new TreeSet<>();
		try {
			for (final Group g : getCommunity(community).values()) {
				if(g.isIn(abstractAgent)){
					groups.add(g.getName());
				}
			}
		} catch (CGRNotAvailable e) {
			return null;
		}
		return groups;
	}

	final TreeSet<String> getRolesOf(AbstractAgent abstractAgent, String community, String group) {
		final TreeSet<String> roles = new TreeSet<>();
		try {
			for (final Role r : getGroup(community,group).values()) {
				if(r.contains(abstractAgent)){
					roles.add(r.getRoleName());
				}
			}
		} catch (CGRNotAvailable e) {
			return null;
		}
		return roles;
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
	
	@Override
	public String toString() {
		return super.toString()+" "+getCode();
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

	/**
	 * 
	 * Creates n tasks
	 * 
	 * @param l
	 * @param cpuCoreNb
	 * @return
	 */
	final ArrayList<AgentsJob> getJobs(List<AbstractAgent> l, int cpuCoreNb) {
		final ArrayList<AgentsJob> workers = new ArrayList<>(cpuCoreNb);
		int bucketSize = l.size();
		final int nbOfAgentsPerTask = bucketSize / cpuCoreNb;
		if (nbOfAgentsPerTask == 0) {
			list = l;
			workers.add(this);
			return workers;
		}
		for (int i = 0; i < cpuCoreNb; i++) {
			int firstIndex = nbOfAgentsPerTask * i;
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
