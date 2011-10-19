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
import static madkit.kernel.AbstractAgent.ReturnCode.NETWORK_DOWN;
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
import static madkit.kernel.Madkit.BooleanOption.loadLocalDemos;
import static madkit.kernel.Madkit.BooleanOption.network;
import static madkit.kernel.Madkit.BooleanOption.noGUIManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.GUIMessage;
import madkit.gui.actions.MadkitAction;
import madkit.i18n.ErrorMessages;
import madkit.i18n.Words;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.messages.KernelMessage;
import madkit.messages.ObjectMessage;

/**
 * The brand new MadKit kernel and it is now a real Agent :)
 * 
 * @author Fabien Michel
 * @version 1.0
 * @since MadKit 5.0
 * 
 */
class MadkitKernel extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5870076996141195039L;

	final static ThreadGroup SYSTEM = new ThreadGroup("MK_SYSTEM"){
		public void uncaughtException(Thread t, Throwable e) {
			System.err.println(t);
			if(e instanceof KilledException){
				e.printStackTrace();
			}
			else{
				System.err.println("--------------internal BUG--------------------");
				System.err.println(t);
				e.printStackTrace();
			};
		}
	};
	//	final static ThreadGroup A_LIFE = new ThreadGroup("A_LIFE"){//TODO duplicate
	//		public void uncaughtException(Thread t, Throwable e) {
	//			System.err.println(t);
	//			if(e instanceof KilledException){
	//				e.printStackTrace();
	//			}
	//			else{
	//				System.err.println("--------------internal BUG--------------------");
	//				System.err.println(t);
	//				e.printStackTrace();
	//			};
	//		}
	//	};

	final static private ThreadPoolExecutor serviceExecutor = new ThreadPoolExecutor(
			//			Runtime.getRuntime().availableProcessors() + 1, 
			2, Integer.MAX_VALUE, 
			2L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(), new ThreadFactory() {
				public Thread newThread(Runnable r) {
					final Thread t = new Thread(SYSTEM, r);
					t.setPriority(Thread.MAX_PRIORITY);
					t.setName(SYSTEM.getName());
					t.setDaemon(true);
					//					t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					//						@Override
					//						public void uncaughtException(Thread t, Throwable e) {
					//							e.printStackTrace();
					//							e.getCause().printStackTrace();
					//						}
					//					});
					return t;
				}
			});

	final private ThreadPoolExecutor lifeExecutor;

	final static ExecutorService getMadkitServiceExecutor() {
		return serviceExecutor;
	}

	// ;// = Executors.newCachedThreadPool();
	final private static Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();
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
		serviceExecutor.prestartAllCoreThreads();
		serviceExecutor.allowCoreThreadTimeOut(true);
		Runtime.getRuntime().addShutdownHook(new Thread(){
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
	private volatile boolean  shuttedDown = false;
	final AgentThreadFactory normalAgentThreadFactory;
	final AgentThreadFactory daemonAgentThreadFactory;

	private AgentAddress netAgent;
	// my private addresses for optimizing the message building
	private AgentAddress netUpdater, netEmmiter, kernelRole;
	final private Set<Agent> threadedAgents;

	/**
	 * if <code>true</code>, deactivate organizational operations
	 */
	private boolean bucketMode = false;

	/**
	 * Constructing the real one.
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
		lifeExecutor = new ThreadPoolExecutor(
				2, Integer.MAX_VALUE, 
				1L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ThreadFactory() {
					public Thread newThread(Runnable r) {
						final Thread t = new Thread(normalAgentThreadFactory.getThreadGroup(), r);
						//						t.setPriority(Thread.MIN_PRIORITY);
						//						t.setDaemon(true);
						return t;
					}
				});
		lifeExecutor.prestartAllCoreThreads();
		lifeExecutor.allowCoreThreadTimeOut(true);
		// launchingAgent(this, this, false);
	}

	MadkitKernel(){
		//for fake kernels
		super(null);
		kernel = this;
		threadedAgents = null;
		loggedKernel = this;
		platform = null;
		logger = null;
		kernelAddress = null;
		organizations = null;
		operatingOverlookers = null;
		normalAgentThreadFactory = null;
		daemonAgentThreadFactory = null;
		lifeExecutor = null;
	}

	MadkitKernel(MadkitKernel k) {
		super(null);
		logger = null;
		threadedAgents = null;
		platform = k.platform;
		kernelAddress = k.kernelAddress;
		organizations = k.organizations;
		operatingOverlookers = k.operatingOverlookers;
		normalAgentThreadFactory = null;
		daemonAgentThreadFactory = null;
		lifeExecutor = null;
		kernel = k;
		//		normalAgentThreadFactory = k.normalAgentThreadFactory;//no need
		//		daemonAgentThreadFactory = k.daemonAgentThreadFactory;//TODO no need
	}

	@Override
	protected void activate() {
		if(logger != null)
			logger.setWarningLogLevel(Level.INFO);
		createGroup(LocalCommunity.NAME, Groups.SYSTEM, false);
		createGroup(LocalCommunity.NAME, Groups.NETWORK, false);
		requestRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER, null);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER, null);

		myThread.setPriority(Thread.NORM_PRIORITY+1);
		// black magic here
		try {
			netUpdater = getRole(LocalCommunity.NAME, Groups.NETWORK, Roles.UPDATER).getAgentAddressOf(this);
			netEmmiter = getRole(LocalCommunity.NAME, Groups.NETWORK, Roles.EMMITER).getAgentAddressOf(this);
			kernelRole = getRole(LocalCommunity.NAME, Groups.SYSTEM, madkit.agr.LocalCommunity.Roles.KERNEL).getAgentAddressOf(this);
		} catch (CGRNotAvailable e) {
			throw new AssertionError("Kernel Agent initialization problem");
		}

		//		platform.logSessionConfig(platform.getConfigOption(), Level.FINER);
		if (loadLocalDemos.isActivated(getMadkitConfig())) {
			loadLocalDemos();
		}
		if (autoConnectMadkitWebsite.isActivated(getMadkitConfig())) {
			addWebRepository();
		}
		launchGuiManagerAgent();
		startSession();
		//		Message m = nextMessage();// In activate only MadKit can feed my mailbox
		//		while (m != null) {
		//			handleMessage(m);
		//			m = waitNextMessage(100);
		//		}
		// logCurrentOrganization(logger,Level.FINEST);
		// try {
		// platform.getMadkitClassLoader().addJar(new
		// URL("http://www.madkit.net/demonstration/repo/market.jar"));
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * Starts a session considering the current MadKit configuration
	 */
	private void startSession() {
		launchNetworkAgent();
		launchConfigAgents();
	}

	/**
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while (! shuttedDown) {
			System.err.println(logger);
			handleMessage(waitNextMessage());
		}
	}

	@Override
	protected void end() {
		if(LevelOption.madkitLogLevel.getValue(platform.getConfigOption()) != Level.OFF){
			System.err.println(
					"\n\t-----------------------------------------------------"+
					"\n\t   MadKit Kernel "+kernelAddress+" is shutting down, Bye !"+
					"\n\t-----------------------------------------------------\n");			
		}
	}

	final private void launchGuiManagerAgent() {
		if (noGUIManager.isActivated(getMadkitConfig())) {
			if (logger != null)
				logger.fine("** No GUI Manager: " + noGUIManager + " option is true**\n");
		} else {
			AbstractAgent a = null;
			try {
				final Constructor<?> c = getMadkitClassLoader().loadClass(getMadkitProperty("booterAgent")).getDeclaredConstructor(boolean.class);
				c.setAccessible(true);
				a = (AbstractAgent) c.newInstance(! BooleanOption.desktop.isActivated(getMadkitConfig()));
				c.setAccessible(false);
//			Agent a = new GUIManagerAgent(! BooleanOption.desktop.isActivated(getMadkitConfig()));
				a.setLogLevel(LevelOption.guiLogLevel.getValue(getMadkitConfig()));
				launchAgent(a);
				threadedAgents.remove(a);
				if (logger != null)
					logger.fine("\n\t****** GUI Manager launched ******\n");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	final private void handleKernelMessage(KernelMessage km) {
		Method operation = null;
		final Object[] arguments = km.getContent();
		switch (km.getCode()) {
		case AGENT_LAUNCH_AGENT:// TODO semantic
			operation = checkValidity("launchAgent",arguments);
			break;
		case MADKIT_KILL_AGENT:// TODO log errors
			operation = checkValidity("killAgent",arguments);
			break;
		case MADKIT_KILL_AGENTS:// TODO semantic
			killAgents(false);
			return;
		case LOAD_LOCAL_DEMOS:// TODO semantic
			loadLocalDemos();
			sendReply(km, new Message());
			return;
		case MADKIT_LAUNCH_SESSION:// TODO semantic
			launchSession((String[]) arguments);
			return;
		case CONNECT_WEB_REPO:
			addWebRepository();
			sendReply(km, new Message());
			return;
		case MADKIT_LOAD_JAR_FILE:// TODO semantic
			//			System.err.println((URL) km.getContent()[0]);
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
			shuttedDown = true;
			restartSession(500);
		case MADKIT_EXIT_ACTION:
			shutdown();
			return;
		default:
			if (logger != null) logger.warning("I received a kernel message that I do not understand. Discarding " + km);
			return;
		}
		doOperation(operation, arguments);
	}

	/**
	 * 
	 */
	private void addWebRepository() {
		final String repoLocation = getMadkitProperty("madkit.repository.url");
		try {
			Properties p = new Properties();
			p.load(new URL(repoLocation+ "repo.properties").openStream());
			//				System.err.println(p);
			for (Entry<Object, Object> object : p.entrySet()) {
				// platform.getMadkitClassLoader().addJar(new
				// URL(repoLocation+object.getKey()+".jar"));
				platform.getMadkitClassLoader().addJar(new URL(repoLocation + object.getValue() + "/" + object.getKey() + ".jar"));
			}
		} catch (final IOException e) {
			if(logger != null)
				logger.log(Level.WARNING,ErrorMessages.CANT_CONNECT+": madkit.net "+repoLocation+"\n");
		}
	}

	/**
	 * 
	 */
	private void loadLocalDemos() {
		File f = lookForMadkitDemoHome();
		if(f != null && f.isDirectory()){
			if(logger != null)
				logger.fine("** LOADING DEMO DIRECTORY **");
			platform.getMadkitClassLoader().loadJarsFromPath(f.getAbsolutePath());
		}
		else if(logger != null)
			logger.log(Level.WARNING,ErrorMessages.CANT_FIND+" demo "+Words.DIRECTORY+"\n");
	}

	private File lookForMadkitDemoHome() {
		for(URL url : getMadkitClassLoader().getURLs()){
			if(url.getProtocol().equals("file") && url.getPath().contains(platform.getConfigOption().getProperty("madkit.jar.name"))){
				try {
					return new File(new File(url.toURI()).getParentFile(),"demos");//URI prevents error from character encoding
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void launchSession(String[] arguments) {
		if(logger != null)
			logger.finer("** LAUNCHING SESSION "+arguments);
		Properties mkCfg = platform.getConfigOption();
		Properties currentConfig = new Properties();
		currentConfig.putAll(mkCfg);
		mkCfg.putAll(platform.buildConfigFromArgs(arguments));
		startSession();
		mkCfg.putAll(currentConfig);
	}

	private void launchConfigAgents(){
		final ExecutorService startExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
		if(logger != null)
			logger.fine("** LAUNCHING CONFIG AGENTS **");
		final String agentsTolaunch = platform.getConfigOption().getProperty(Option.launchAgents.name());
		if(! agentsTolaunch.equals("null")){
			final String[] agentsClasses = agentsTolaunch.split(";");
			for (final String classNameAndOption : agentsClasses) {
				final String[] classAndOptions = classNameAndOption.split(",");
				final String className = classAndOptions[0].trim();//TODO should test if these classes exist
				final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
				final int number;
				if (classAndOptions.length > 2) {
					number = Integer.parseInt(classAndOptions[2].trim());
				}
				else{
					number = 1;
				}
				if (logger != null)
					logger.finer("Launching " + number + " instance(s) of " + className + " with GUI = " + withGUI);
				for (int i = 0; i < number; i++) {
					startExecutor.execute(new Runnable() {
						public void run() {//TODO log failures here ?
							if (! shuttedDown) {
								launchAgent(className, 1, withGUI);
							}
						}
					});
				}
			}
			startExecutor.shutdown();
		}
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
//		ReturnCode r = sendNetworkMessageWithRole(new Message(), kernelRole);
		if (updateNetworkStatus(false) == SUCCESS) {
			if (logger != null) logger.fine("\n\t****** Network stopped ******\n");
		}// TODO i18n
		else {
			if (logger != null) logger.fine("\n\t****** Network already down ******\n");
		}
	}

	private void startNetwork() {
		updateNetworkAgent();
		if (netAgent == null) {
			NetworkAgent na = new NetworkAgent();
			ReturnCode r = launchAgent(na);
			threadedAgents.remove(na);
			if (r == SUCCESS) {
				if (logger != null) logger.fine("\n\t****** Network agent launched ******\n");
			}// TODO i18n
			else {
				if (logger != null) logger.severe("\n\t****** Problem launching network agent ******\n");
			}
		} else {
			if(updateNetworkStatus(true) == SUCCESS){
				if (logger != null) logger.fine("\n\t****** Network agent up ******\n");
			}
			else{
				if (logger != null) logger.fine("\n\t****** Problem relaunching network agent  ******\n");
			}
					
		}
	}
	
	private ReturnCode updateNetworkStatus(boolean start){
		return sendNetworkMessageWithRole(new KernelMessage(start ? MadkitAction.MADKIT_LAUNCH_NETWORK : MadkitAction.MADKIT_STOP_NETWORK), kernelRole);
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

	private void handleMessage(Message m) {
		if (m instanceof KernelMessage) {
			handleKernelMessage((KernelMessage) m);
		} else {
			if (logger != null) logger.warning("I received a message that I do not understand. Discarding " + m);
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
	@SuppressWarnings("unused")// used by reflection
	private Method launchAgent(Object[] content) {
		return checkValidity("launchAgent", content);
	}

	private Method checkValidity(String method, Object[] content) {
		Class<?>[] parameters = new Class<?>[content.length];
		for (int i = 0; i < content.length; i++) {
			parameters[i] = content[i].getClass();
			//			System.err.println(parameters[i].getName());
			if(AbstractAgent.class.isAssignableFrom(parameters[i])){
				parameters[i] = AbstractAgent.class;
			}
			final Class<?> primitive = primitiveTypes.get(parameters[i].getName());
			//			System.err.println(primitive);
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

	//	private AbstractAgent launchPlatformAgent(String mkProperty, String userMessage) {
	//		final String agentClassName = getMadkitProperty(mkProperty);
	//		if (logger != null) {
	//			logger.fine("** Launching " + userMessage + ": " + agentClassName + " **");
	//		}
	//		AbstractAgent targetAgent = launchAgent(agentClassName);
	//		if (targetAgent == null) {
	//			if (logger != null) {
	//				logger.warning("Problem building " + userMessage + " " + agentClassName + " -> Using MK default " + userMessage
	//						+ " : " + Madkit.defaultConfig.get(mkProperty));
	//			}
	//			return launchAgent(Madkit.defaultConfig.getProperty(mkProperty));
	//		}
	//		return targetAgent;
	//	}

	private void launchNetworkAgent() {
		if (network.isActivated(getMadkitConfig())) {
			startNetwork();
		} else {
			if(logger != null)
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

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final String description,
			final Gatekeeper gatekeeper, final boolean isDistributed) {
		if(bucketMode)
			return SUCCESS;
		// no need to remove org: never failed
		//will throw null pointer if community is null
		Organization organization = new Organization(community, this);
		if(group == null)
			throw new NullPointerException("group's name is null");
		final Organization tmpOrg = organizations.putIfAbsent(community, organization);
		if (tmpOrg != null) {
			organization = tmpOrg;
		}
		if (!organization.addGroup(creator, group,gatekeeper,isDistributed)) {
			return ALREADY_GROUP;
		}
		if (isDistributed) {
			try {
				sendNetworkMessageWithRole(new CGRSynchro(CREATE_GROUP, getRole(community, group, madkit.agr.Organization.GROUP_MANAGER_ROLE)
						.getAgentAddressOf(creator)), netUpdater);
			} catch (CGRNotAvailable e) {
				getLogger().severeLog("Please bug report", e);
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
		if(bucketMode)
			return SUCCESS;
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
		if(bucketMode)
			return SUCCESS;
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
		if(bucketMode)
			return SUCCESS;
		final Role r;
		try {
			r = getRole(community, group, role);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		//this is apart because I need the address before the leave
		if (r.getMyGroup().isDistributed()) {
			AgentAddress leaver = r.getAgentAddressOf(requester);
			if (leaver == null)
				return ReturnCode.ROLE_NOT_HANDLED;
			if (r.removeMember(requester) != SUCCESS)
				throw new AssertionError("cannot remove " + requester + " from " + r.buildAndGetAddresses());
			sendNetworkMessageWithRole(new CGRSynchro(LEAVE_ROLE, leaver), netUpdater);
			return SUCCESS;
		}
		return r.removeMember(requester);
	}

	// Warning never touch this without looking at the logged kernel
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role, boolean callerIncluded) {
		try {
			if (callerIncluded) {
				return getRole(community, group, role).getAgentAddressesCopy();
			}
			else{
				return getOtherRolePlayers(requester, community, group, role);
			}
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
			return INVALID_AA;
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
				return NO_RECIPIENT_FOUND; // the requester is the only agent in
			// this group
			messageToSend.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			broadcasting(receivers, messageToSend);
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
			if (requester.getKernel() != this && requester.isWarningOn()) {//is loggable
				final ReturnCode r = e.getCode();
				if (r == NO_RECIPIENT_FOUND) {
					requester.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new MadkitWarning(r));
				} else if (r == ROLE_NOT_HANDLED) {
					requester.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new OrganizationWarning(r, community, group,
							senderRole));
				} else {
					requester.handleException(Influence.BROADCAST_MESSAGE_AND_WAIT, new OrganizationWarning(r, community, group, role));
				}
			}
			return null;
		}
	}

	void broadcasting(final Collection<AgentAddress> receivers, Message m) {//TODO optimize without cloning
		for (final AgentAddress agentAddress : receivers) {
			if (agentAddress != null) {// TODO this should not be possible
				m = m.clone();
				m.setReceiver(agentAddress);
				sendMessage(m, agentAddress.getAgent());
			}
		}
	}

	final ReturnCode sendMessage(Message m, AbstractAgent target) {
		if (target == null) {
			return sendNetworkMessageWithRole(new ObjectMessage<Message>(m), netEmmiter);
		} else {
			target.receiveMessage(m);
		}
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
		return NETWORK_DOWN;
	}

	private void updateNetworkAgent() {
		if (netAgent == null || ! netAgent.exists()) {// Is it still playing the
			// role ?
			netAgent = getAgentWithRole(LocalCommunity.NAME, Groups.NETWORK, madkit.agr.LocalCommunity.Roles.NET_AGENT);
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
		if(shuttedDown)
			return null;
		bucketMode  = CGRLocations != null;
		Class<? extends AbstractAgent> agentClass = null;
		try {//TODO put that in the cl
			agentClass = (Class<? extends AbstractAgent>) platform.getMadkitClassLoader().loadClass(agentClassName);
		} catch (ClassCastException e) {
			requester.getLogger().severe("Cannot launch " + agentClassName + " because it is not an agent class");
			return null;
		} catch (ClassNotFoundException e) {
			requester.getLogger().severe("Cannot launch " + agentClassName + " because the class has not been found");
			return null;
		}
		ArrayList<AbstractAgent> bucket = null;
		try {
			bucket = createBucket(agentClass, bucketSize);
		} catch (InterruptedException e) {//forward the interruption
			requester.handleInterruptedException();
			return null;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				// test vs assignement ? -> No: cannot touch the organizational
				// structure !!
			}
		}
		//		for (final AbstractAgent a : bucket) {
		//			a.activation();
		//		}
		bucketMode = false;
		return bucket;
	}

	private ArrayList<AbstractAgent> createBucket(final Class<? extends AbstractAgent> agentClass, int bucketSize) throws InterruptedException, InstantiationException, IllegalAccessException {
		final int cpuCoreNb = Runtime.getRuntime().availableProcessors();
		final ArrayList<AbstractAgent> result = new ArrayList<AbstractAgent>(bucketSize);
		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
		// System.err.println("nb of ag per task "+nbOfAgentsPerTask);
		CompletionService<ArrayList<AbstractAgent>> ecs = new ExecutorCompletionService<ArrayList<AbstractAgent>>(serviceExecutor);
		final ArrayList<Callable<ArrayList<AbstractAgent>>> workers = new ArrayList<Callable<ArrayList<AbstractAgent>>>(cpuCoreNb);
		for (int i = 0; i < cpuCoreNb; i++) {
			workers.add(new Callable<ArrayList<AbstractAgent>>() {

				public ArrayList<AbstractAgent> call() throws InstantiationException, IllegalAccessException{
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
				// adding the missing one when the division results as a real number
				for (int i = bucketSize - nbOfAgentsPerTask * cpuCoreNb; i > 0; i--) {
					result.add(initAbstractAgent(agentClass));
				}
				for (int i = 0; i < n; ++i) {
					try {
						result.addAll(ecs.take().get());
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				// System.err.println(result.size());
				return result;
	}

	private AbstractAgent initAbstractAgent(final Class<? extends AbstractAgent> agentClass) throws InstantiationException, IllegalAccessException {
		final AbstractAgent a = agentClass.newInstance();
		a.state.set(ACTIVATED); // no need to test : I created these
		// instances
		a.setKernel(this);
		a.getAlive().set(true);
		a.logger = null;
		return a;
	}

	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds,
			final boolean defaultGUI) {
		try {
			if(logger != null)
				logger.finest(requester+" launching "+agent+ " by "+Thread.currentThread());
			// if to == 0, this is still quicker than treating the case, this also holds for Integer.MAX_VALUE
			return lifeExecutor.submit(new Callable<ReturnCode>() {
				public ReturnCode call() {
					return launchingAgent(agent, defaultGUI);
				}
			}).get(timeOutSeconds, TimeUnit.SECONDS);
			//			System.err.println(lifeExecutor.getCompletedTaskCount());
			//			System.err.println(lifeExecutor.allowsCoreThreadTimeOut());
		} catch (InterruptedException e) {// requester has been killed or something
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
		//because if the job starts, it has to be done till the end
		if (! agent.state.compareAndSet(NOT_LAUNCHED, INITIALIZING) || shuttedDown) {
			return ALREADY_LAUNCHED;
		}
		//		System.err.println("adding "+agent.getName()+" using "+Thread.currentThread()+ agent.getState());
		agent.setKernel(this);
		if(defaultGUI)
			agent.activateGUI();
		Level defaultLevel = LevelOption.agentLogLevel.getValue(getMadkitConfig());
		final AgentLogger agentLogger = agent.logger;
		if(agentLogger == AbstractAgent.defaultLogger){//not changed in the constructor
			if(defaultLevel == Level.OFF) {//default not changed and global is OFF
				agent.logger = null;
			}
			else{
				agent.setLogLevel(defaultLevel);
				agent.getLogger().setWarningLogLevel(LevelOption.warningLogLevel.getValue(getMadkitConfig()));
			}
		}
		final AgentExecutor ae = agent.getAgentExecutor();
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
				bugReport(agent+" activation task failed using " + Thread.currentThread(), e);
			} catch (InterruptedException e) {
				bugReport(agent+" activation task failed using " + Thread.currentThread(), e);
			}
			if(r != SUCCESS){
				synchronized (agent.state) {
					agent.state.notify();
				}
			}
			else{
				if (agent.isAlive()) {// ! self kill -> safe to make this here
					agent.state.set(LIVING);
				}
			}
			return r;
		}
		else{
			try {
				final Agent a = (Agent) agent;
				synchronized (threadedAgents) {
					//do that even if not started for cleaning properly
					threadedAgents.add(a);
				}
				ae.setThreadFactory(a.isDaemon() ? daemonAgentThreadFactory : normalAgentThreadFactory);
				if (! shuttedDown && ae.start().get()){
					return SUCCESS;
				}
				else{
					return AGENT_CRASH;
				}
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
			}  catch (CancellationException e) {
				//This is the case when the agent is killed during activation
				return AGENT_CRASH;
			}
			return TIMEOUT;
		}
	}

	ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, final int timeOutSeconds) {
		if (target.getState().compareTo(ACTIVATED) < 0) {
			return NOT_YET_LAUNCHED;
		}
		final Future<ReturnCode> killAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call() {
				return killingAgent(target,timeOutSeconds);
			}
		});
		try {
			return killAttempt.get();
		} catch (InterruptedException e) {// requester has been killed or something
			requester.handleInterruptedException();
			return TIMEOUT;
		} catch (ExecutionException e) {// BUG kill failed
			bugReport("Killing task failed on " + target, e);
			return SEVERE;
			//		} catch (TimeoutException e) {// kill task time out
			//			return TIMEOUT;
		}
	}

	final ReturnCode killingAgent(final AbstractAgent target, int timeOutSeconds) {// TODO avec
		synchronized (target.state) {
			// this has to be done by a system thread : the job must be done
			if (!target.getAlive().compareAndSet(true, false)) {
				return ALREADY_KILLED;
			}
		}
		final AgentExecutor ae = target.getAgentExecutor();
		if (ae != null) {
			return killThreadedAgent((Agent)target, ae,timeOutSeconds);
		}
		else{
			stopAbstractAgentProcess(ACTIVATED, target);
			return startEndBehavior(target, timeOutSeconds,false);
		}
	}

	private void stopAbstractAgentProcess(State s, AbstractAgent target){
		final ThreadGroup group = normalAgentThreadFactory.getThreadGroup();
		final Thread[] list = new Thread[group.activeCount()];
		group.enumerate(list);
		final String threadName = target.getAgentThreadName(s);
		for(final Thread t : list){
			if(t.getName().equals(threadName)){
				stopAgentProcess(s, target, t);
				break;
			}
		}
	}

	static void dumpThreadStack(Thread t){
		for (StackTraceElement ste : t.getStackTrace()) {
			System.err.println(ste);
		}
	}

	/**
	 * @param s
	 * @param target
	 * @param t
	 * @return <code>true</code> if a hard kill has been done
	 */
	@SuppressWarnings("deprecation")
	private boolean stopAgentProcess(State s, AbstractAgent target, Thread t){
		//soft kill	
		//		synchronized (target.state) {
		//			if(target.getState() == s){
		//				t.interrupt();
		//			Thread.yield();
		////			pause(5);
		//			}
		//		}
		//hard kill
		synchronized (target.state) {
			if(target.getState() == s && t.getName().equals(target.getAgentThreadName(s))){
				if(logger != null){
					logger.finer("Hard kill on "+target+" "+t.getName());
					//					//					logger.finer("\n----"+target+"------");
					//					//					dumpThreadStack(t);
					//					//					logger.finer("----------\n");
				}
				t.stop(new KilledException("brutal kill"));
				//				long deb = System.currentTimeMillis();
				if(logger != null)
					logger.finer("now waiting for "+s+" to end on "+target);
				try {
					target.state.wait();//TODO really, no need to do more ??
				} catch (InterruptedException e) {
					bugReport(e);
				}
				return true;
				//				long end = System.currentTimeMillis();
				//				if(logger != null && end - deb > 5000)
				//					logger.finer("!!!!!!!!!!!!!!!!!!!!!!waiting timeout hard kill on "+target);
			}
		}
		if(logger != null)
			logger.finer(s+" already done on "+target);
		return false;
	}
	//				
	//				
	//				
	//				
	//				while (true) {
	//					synchronized (target) {
	//						else{
	//							break;//thread is actually quitting : state has changed
	//						}
	//					}
	//					try {
	//						Thread.sleep(100);
	//					} catch (InterruptedException e) {
	//						// TODO Auto-generated catch block
	//						e.printStackTrace();
	//					}
	//				}
	//				System.err.println(t.getState());
	//				synchronized (target.state) {
	//					try {
	//						if (target.state.get() == s) {
	//							target.state.wait();
	//						}
	//					} catch (InterruptedException e) {
	//						// TODO Auto-generated catch block
	//						e.printStackTrace();
	//					}
	//				}
	//			}
	//		}
	//	}

	final ReturnCode killAbstractAgent(final AbstractAgent target, int timeOutSeconds){
		//still activating
		stopAbstractAgentProcess(ACTIVATED, target);
		return startEndBehavior(target, timeOutSeconds,false);
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
		if(timeOutSeconds != 0){
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
				//				searchAndDestroyAgentThread(State.ENDING+"-"+target.hashCode());
				stopAbstractAgentProcess(State.ENDING, target);
			}
		}
		if(! (target instanceof Agent)){
			target.terminate();
		}
		return r;
	}

	final ReturnCode killThreadedAgent(Agent target, final AgentExecutor ae, int timeOutSeconds) {
		final Future<?> end = ae.getEndProcess();
		if(timeOutSeconds == 0){
			end.cancel(false);
		}
		ae.getLiveProcess().cancel(false);
		ae.getActivate().cancel(false);
		Thread.yield();
		target.myThread.setPriority(Thread.MIN_PRIORITY);
		ReturnCode result = SUCCESS;
		if(! stopAgentProcess(ACTIVATED, target, target.myThread)){
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
				bugReport("kill task failed on "+target,e);
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
	Organization getCommunity(final String community) throws CGRNotAvailable {
		Organization org = organizations.get(community);
		if (org == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		return org;
	}

	Group getGroup(final String community, final String group) throws CGRNotAvailable {
		Group g = getCommunity(community).get(group);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	Role getRole(final String community, final String group, final String role) throws CGRNotAvailable {
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
	List<AgentAddress> getOtherRolePlayers(AbstractAgent abstractAgent, String community, String group, String role)
			throws CGRNotAvailable {
		// never null without throwing Ex
		final List<AgentAddress> result = getRole(community, group, role).getAgentAddressesCopy();
		Role.removeAgentAddressOf(abstractAgent, result);
		if (!result.isEmpty()) {
			return result;
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
		return sendMessage(m,receiver.getAgent());
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
				if (targetedRole.getRoleName().equals(madkit.agr.Organization.GROUP_MANAGER_ROLE))
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
				if (senderRole.equals(madkit.agr.Organization.GROUP_CANDIDATE_ROLE) && targetedRole.getRoleName().equals(madkit.agr.Organization.GROUP_MANAGER_ROLE))
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

	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		platform.getConfigOption().setProperty(key, value);// TODO update agent logging
		// on or off
	}

	@Override
	public Properties getMadkitConfig(){
		return platform.getConfigOption();
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
		//		if (name == null)
		//			throw new ClassNotFoundException(ReturnCode.CLASS_NOT_FOUND + " " + name);
		if (!name.contains("madkit.kernel") && !name.contains("madkit.gui") && !name.contains("madkit.messages")
				&& !name.contains("madkit.simulation") && platform.getMadkitClassLoader().reloadClass(name))
			return SUCCESS;
		return SEVERE;// TODO not the right code here
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

	synchronized void importDistantOrg(Map<String, Map<String, Map<String, Set<AgentAddress>>>> distantOrg) {
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
	public Map<String, Map<String, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global) {
		Map<String, Map<String, Map<String, Set<AgentAddress>>>> export = new TreeMap<String, Map<String, Map<String, Set<AgentAddress>>>>();
		for (Map.Entry<String, Organization> org : organizations.entrySet()) {
			Map<String, Map<String, Set<AgentAddress>>> currentOrg = org.getValue().getOrgMap(global);
			if (!currentOrg.isEmpty())
				export.put(org.getKey(), org.getValue().getOrgMap(global));
		}
		return export;
	}

	@Override
	public URLClassLoader getMadkitClassLoader() {
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
					//updating sender address
					sender.setRoleObject(kernel.getRole(sender.getCommunity(), sender.getGroup(), sender.getRole()));
					target.receiveMessage(toInject);
				}
				else if (logger != null)
					logger.finer(m+" received but the agent address is no longer valid !! Current distributed org is "
							+ getOrganizationSnapShot(false));
			}
		} catch (CGRNotAvailable e) {
			kernel.bugReport("Cannot inject "+m+"\n"+getOrganizationSnapShot(false),e);
		}
	}

	final void injectOperation(CGRSynchro m) {
		final Role r = m.getContent().getRoleObject();
		if(r == null){
			if(logger != null)
				logger.log(Level.FINE, "distant CGR " + m.getCode() + " update failed on " + m.getContent());
			return;
		}
		final String communityName = r.getCommunityName();
		final String groupName = r.getGroupName();
		final String roleName = r.getRoleName();
		try {
			switch (m.getCode()) {
			case CREATE_GROUP:
				//nerver fails : no need to remove org
				Organization organization = new Organization(communityName, this);// no
				final Organization tmpOrg = organizations.putIfAbsent(communityName, organization);
				if (tmpOrg != null) {
					if (isGroup(communityName, groupName)) {
						if (logger != null)
							logger.finer("distant group creation by " + m.getContent() + " aborted : already exists locally");//TODO what about the manager
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
				bugReport("case not treated in injectOperation", new KernelException(""));
				break;
			}
		} catch (CGRNotAvailable e) {
			if(logger != null)
				logger.log(Level.FINE, "distant CGR " + m.getCode() + " update failed on " + m.getContent(), e);
		}
	}

	void shutdown() {
		if(System.getProperty("javawebstart.version") != null){
			System.exit(0);
		}
		shuttedDown = true;
		sendNetworkMessageWithRole(new KernelMessage(MadkitAction.MADKIT_EXIT_ACTION), kernelRole);
		broadcastMessageWithRole(MadkitKernel.this, LocalCommunity.NAME,
				Groups.SYSTEM, madkit.agr.LocalCommunity.Roles.GUI_MANAGER, new GUIMessage(MadkitAction.MADKIT_EXIT_ACTION, MadkitKernel.this), null);
		//		pause(10);//be sure that last executors have started
		if (logger != null)
			logger.finer("***** SHUTINGDOWN MADKIT ********\n");
		killAgents(true);
	}
	
	
	
	// BAD IDEA AS IT ALLOWS CONFIG AGENT ZOMBIES TO RUN 
//	/**
//	 * This allows to not have dirty stack traces when ending and config agents are not all launched
//	 * 
//	 * @see madkit.kernel.AbstractAgent#terminate()
//	 */
//	@Override
//	final void terminate() {
//		if (logger != null) {
//			logger.finer("** TERMINATED **");
//		}
//	}

	private void killAgents(boolean untilEmpty) {
		threadedAgents.remove(this);
		//Do not do what follows because it throws interruption on awt threads ! //TODO why ?
//		if(untilEmpty)
//			normalAgentThreadFactory.getThreadGroup().interrupt();
		ArrayList<Agent> l;
		synchronized (threadedAgents) {
			l = new ArrayList<Agent>(threadedAgents);
		}
		do {
			for (final Agent a : l) {
				killAgent(this, a, 0);
			}
		} while (untilEmpty && ! threadedAgents.isEmpty());
	}

	boolean createGroupIfAbsent(AbstractAgent abstractAgent, String community, String group, String group2,
			Gatekeeper gatekeeper, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, group, gatekeeper, isDistributed) == SUCCESS;
	}

	void bugReport(Throwable e) {
		bugReport("", e);
	}

	void bugReport(String m, Throwable e) {
		kernel.getLogger().severeLog("********************** KERNEL PROBLEM, please bug report "+m, e); // Kernel
	}

	final synchronized void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		for (final Organization org : organizations.values()) {
			org.removeAgentsFromDistantKernel(kernelAddress2);
		}
	}

	//	void removeThreadedAgent(Agent myAgent) {
	//		synchronized (threadedAgents) {
	//			threadedAgents.remove(myAgent);
	//		}
	//	}

	synchronized ReturnCode destroyCommunity(AbstractAgent abstractAgent, String community) {
		try {
			getCommunity(community).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	synchronized ReturnCode destroyGroup(AbstractAgent abstractAgent, String community, String group) {
		try {
			getGroup(community,group).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	synchronized ReturnCode destroyRole(AbstractAgent abstractAgent, String community, String group, String role) {
		try {
			getRole(community,group,role).destroy();
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}

	void removeThreadedAgent(Agent myAgent) {
		synchronized (threadedAgents) {
			threadedAgents.remove(myAgent);
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

	//	@Override
	//	public synchronized Throwable fillInStackTrace() {
	//		return null;
	//	}

}