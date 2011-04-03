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

import static java.lang.Boolean.parseBoolean;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import madkit.gui.GUIMessage;
import madkit.gui.GUIsManagerAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;
import madkit.messages.ObjectMessage;
import static madkit.kernel.Madkit.Roles.*;

/**
 * The brand new madkit kernel and it is now an real Agent :)
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
class MadkitKernel extends Agent{

	final static ExecutorService serviceExecutor = Executors.newCachedThreadPool();
	final static Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();
	static{
		primitiveTypes.put("java.lang.Integer",int.class);
		primitiveTypes.put("java.lang.Boolean",boolean.class);
		primitiveTypes.put("java.lang.Byte",byte.class);
		primitiveTypes.put("java.lang.Character",char.class);
		primitiveTypes.put("java.lang.Float",float.class);
		primitiveTypes.put("java.lang.Void",void.class);
		primitiveTypes.put("java.lang.Short",short.class);
		primitiveTypes.put("java.lang.Double",double.class);
		primitiveTypes.put("java.lang.Long",long.class);
	}

	static{
		final ThreadPoolExecutor t = (ThreadPoolExecutor) serviceExecutor;
		t.setCorePoolSize(Runtime.getRuntime().availableProcessors()+1);
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

	final private ConcurrentHashMap<String, Organization> organizations;
	final private Set<Overlooker<? extends AbstractAgent>> operatingOverlookers;
	final private Madkit platform;
	final private KernelAddress kernelAddress;
	

	private AgentAddress netAgent;
	//	private MadKitGUIsManager guiManager;
	private Level defaultAgentLogLevel;
	private Level defaultWarningLogLvl;

	private LoggedKernel loggedKernel;
	private boolean shuttedDown = false;

	MadkitKernel(Madkit m) {
		super(true);
		platform = m;
		if (m != null) {
			kernelAddress = platform.getPlatformID();
			setDefaultAgentLogLevel(
					Level.parse(platform.getConfigOption().getProperty(
							Madkit.agentLogLevel)),
							Level.parse(platform.getConfigOption().getProperty(
									Madkit.warningLogLevel)));
			organizations = new ConcurrentHashMap<String, Organization>();
			operatingOverlookers = new LinkedHashSet<Overlooker<? extends AbstractAgent>>();
			setLogLevel(Level.ALL,Level.INFO);
			//			launchingAgent(this, this, false);
		}
		else{
			kernelAddress = null;
			organizations = null;
			operatingOverlookers = null;
		}
	}

	@Override
	protected void activate() {
		myThread.setPriority(Thread.MAX_PRIORITY-2);
		createGroup(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, true);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, Roles.KERNEL_ROLE, null);
		launchBooterAgent();
		launchNetworkAgent();
		Message m = nextMessage();//In activate only MadKit can feed my mailbox
		while (m != null) {
			handleMessage(m);
			m = waitNextMessage(100);
		}
		//		logCurrentOrganization(logger,Level.FINEST);
	}

	private void handleKernelMessage(KernelMessage km) {
		Method operation = null;
		final Object[] arguments = km.getContent();
		switch (km.getCode()) {
		case LAUNCH_AGENT:
			operation = launchAgent(arguments);
			break;
		case SHUTDOWN_NOW:
			shutdown();
			return;
		default:
			break;
		}
		doOperation(operation,arguments);
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
		if(m instanceof KernelMessage){
			handleKernelMessage((KernelMessage) m);
		}
		else{
			if(logger != null)
				logger.warning("I received a message that I do not understang. Discarding "+m);
		}
	}

	/**
	 * @param operation
	 * @param arguments
	 */
	private void doOperation(Method operation, Object[] arguments) {
		try {//TODO log failures
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

	private Method checkValidity(String method, Object[] content){
		Class<?>[] parameters = new Class<?>[content.length];
		for(int i = 0;i < content.length;i++){
			parameters[i] = content[i].getClass();
			final Class<?> primitive = primitiveTypes.get(parameters[i].getName());
			if(primitive != null)
				parameters[i] = primitive;
		}
		try {//TODO log failures
			return getClass().getMethod(method, parameters);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void launchBooterAgent(){ //TODO put that into MadKit
		AbstractAgent booter=null;
		if(! getMadkitProperty(Madkit.booterAgentKey).toLowerCase().equals("null"))
			booter=launchPlatformAgent(Madkit.booterAgentKey, "Boot Agent");
		else if(logger != null){
			logger.fine("** Booter agent off: --booterAgent property is null**\n");
			return;
		}
		//		if(booter != null && booter instanceof MadKitGUIsManager){
		//			if(logger != null)
		//				logger.fine("** Setting "+booter.getName()+" as AgentsGUIManager **\n");
		//			operatingKernel.setGuiManager((MadKitGUIsManager) booter);
		//		}
	}

	private AbstractAgent launchPlatformAgent(String mkProperty,String userMessage){
		final String agentClassName=getMadkitProperty(mkProperty);
		if(logger != null){
			logger.fine("** Launching "+userMessage+": "+agentClassName+" **");
		}
		AbstractAgent targetAgent = launchAgent(agentClassName);
		if(targetAgent == null){
			if(logger != null){
				logger.warning("Problem building "+userMessage+" "+agentClassName+" -> Using MK default "+userMessage+" : "+Madkit.defaultConfig.get(mkProperty));
			}
			return launchAgent(Madkit.defaultConfig.getProperty(mkProperty));
		}
		return targetAgent;
	}

	private void launchNetworkAgent() {
		if(! Boolean.parseBoolean(getMadkitProperty(Madkit.network))){
			if(logger != null){
				logger.fine("** Networking is off: No Net Agent **\n");
			}
		}
		else{
			launchPlatformAgent("networkAgent",  "Net Agent");
		}
	}

	///////////////////////////////////////: Kernel Part

	/**
	 * @return the loggedKernel
	 */
	LoggedKernel getLoggedKernel() {
		return loggedKernel;
	}


	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Agent interface
	///////////////////////////////////////////////////////////////////////////


	//////////////////////////////////////////////////////////////
	////////////////////////// Organization interface
	//////////////////////////////////////////////////////////////

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final String description, final GroupIdentifier theIdentifier, final boolean isDistributed) {
		if(community == null || group == null){
			return NULL_STRING;
		}
		Organization organization = new Organization(community,this);//TODO optimize , no need to remove org never failed if not present
		final Organization tmpOrg = organizations.putIfAbsent(community, organization);
		if(tmpOrg != null){
			organization = tmpOrg;
		}
		if(! organization.createGroup(creator, group, theIdentifier,isDistributed)){
			return ALREADY_GROUP;
		}
		if(isDistributed){
			sendNetworkMessage(new CGRSynchroMessage(
					CGRSynchroMessage.CREATE_GROUP, 
					new AgentAddress(creator, new Role(community,group), kernelAddress)));
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
		if(g.isDistributed() && result == SUCCESS) {
			sendNetworkMessage(
					new CGRSynchroMessage(
							CGRSynchroMessage.REQUEST_ROLE, 
							g.get(role).getAgentAddressOf(requester)));
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
		if(g.isDistributed() && result == SUCCESS){
			sendNetworkMessage(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_GROUP, new AgentAddress(requester, new Role(community, group), kernelAddress)));
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

	ReturnCode leaveRole(AbstractAgent requester, String community,String group, String role) {
		final Role r;
		try {
			//			g = getGroup(community, group);
			r = getRole(community,group,role);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		Group g = r.getMyGroup();
		//must do that before remove, in case the group disappears
		boolean distributed = netAgent != null && g.isDistributed();
		final ReturnCode result = r.removeMember(requester);
		if(distributed && result == SUCCESS) {
			sendNetworkMessage(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_ROLE, r.getAgentAddressOf(requester)));
		}
		return result;
	}


	/**
	 * @see madkit.kernel.RootKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */

	//Warning never touch this without looking at the logged kernel
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role){
		try {
			return getOtherRolePlayers(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}


	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role){
		try {
			return getAnotherRolePlayer(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////

	ReturnCode sendMessage(final AbstractAgent requester, final String community, final String group, final String role, final Message message, final String senderRole) {
		if(message == null){
			return INVALID_ARG;
		}
		AgentAddress receiver,sender;
		try {
			receiver = getAnotherRolePlayer(requester,community, group, role);
			if(receiver == null){
				return NO_RECIPIENT_FOUND;
			}
			sender = getSenderAgentAddress(requester,receiver,senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		return buildAndSendMessage(sender,receiver,message);
	}


	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, final Message message, final String senderRole){
		if(receiver == null || message == null){
			return INVALID_ARG;
		}
		//get the role for the sender
		AgentAddress sender;
		try {
			sender = getSenderAgentAddress(requester,receiver,senderRole);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		// check that the AA is valid : the targeted agent is still playing the corresponding role or it was a candidate request
		if(! receiver.getRoleObject().isPlayingRole(receiver) && ! receiver.getRole().equals(Roles.GROUP_CANDIDATE_ROLE)){
			return INVALID_AA;
		}
		return buildAndSendMessage(sender,receiver,message);
	}


	ReturnCode sendReplyWithRole(final AbstractAgent requester, final Message messageToReplyTo, final Message reply, String senderRole) {
		if(messageToReplyTo == null || reply == null){
			return INVALID_ARG;
		}
		reply.setID(messageToReplyTo.getConversationID());
		return sendMessage(requester,messageToReplyTo.getSender(),reply,senderRole);
	}


	ReturnCode broadcastMessageWithRole(final AbstractAgent requester, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		if(messageToSend == null)
			return INVALID_ARG;
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester,community, group, role);
			if(receivers == null)
				return NO_RECIPIENT_FOUND; // the requester is the only agent in this group
			messageToSend.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			broadcasting(receivers, messageToSend);
			return SUCCESS;
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
	}


	List<Message> broadcastMessageWithRoleAndWaitForReplies(final AbstractAgent abstractAgent, 
			final String community, final String group, final String role, 
			Message message,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(abstractAgent,community, group, role);
			if(message == null || receivers == null)
				return null; // the requester is the only agent in this group
			message.setSender(getSenderAgentAddress(abstractAgent, receivers.get(0), senderRole));
			broadcasting(receivers, message);
			return abstractAgent.waitAnswers(message,receivers.size(),timeOutMilliSeconds);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}



	void broadcasting(final List<AgentAddress> receivers, Message m){
		for (final AgentAddress agentAddress : receivers) {
			if (agentAddress != null) {//TODO this should not be possible
				m = m.clone();
				m.setReceiver(agentAddress);
				sendMessage(m);
			}
		}
	}

	ReturnCode sendMessage(Message m) {
		final AbstractAgent target = m.getReceiver().getAgent();
		if(target == null){
			return sendNetworkMessage(new MessageConveyor(m));
		} else{
			target.receiveMessage(m);
		}
		return SUCCESS;
	}

	ReturnCode sendNetworkMessage(Message m){
		if(netAgent == null || ! netAgent.exists()){//Is it still playing the role ?
			netAgent = getAgentWithRole(NetworkAgent.NETWORK_COMMUNITY, NetworkAgent.NETWORK_GROUP, NetworkAgent.NETWORK_ROLE);
		}
		if(netAgent != null){
			netAgent.getAgent().receiveMessage(m);
			return SUCCESS;
		}
		return NETWORK_DOWN;
	}

	//	private boolean updateNetAgent(){//TODO 
	//		if(netAgent == null){
	//			netAgent = getAgentWithRole(requester, community, group, role);
	//		}
	//	}

	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")

	synchronized List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent requester, String agentClassName,int bucketSize,Collection<String> CGRLocations){
		Class<? extends AbstractAgent> agentClass = null;
		try {
			agentClass = (Class<? extends AbstractAgent>) platform.getMadkitClassLoader().loadClass(agentClassName);
		} catch (ClassCastException e) {
			if(requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch "+agentClassName+" because it is not an agent class");
			return null;
		} catch (ClassNotFoundException e) {
			if(requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch "+agentClassName+" because the class has not been found");
			return null;
		}
		final ArrayList<AbstractAgent> bucket = createBucket(agentClass, bucketSize);
		if (CGRLocations != null) {
			for (final String cgrLocation : CGRLocations) {
				final String[] cgr = cgrLocation.split(";");
				if (cgr.length != 3)
					return null;//TODO logging
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
				r.addMembers(bucket, roleCreated);//test vs assignement ? -> No cannot touch the organizational structure !!
			}
		}
		for(final AbstractAgent a : bucket){
			a.activation(false);
		}
		return bucket;
	}

	private ArrayList<AbstractAgent> createBucket(final Class<? extends AbstractAgent> agentClass, int bucketSize){
		final int cpuCoreNb = ((ThreadPoolExecutor) serviceExecutor).getCorePoolSize();
		final ArrayList<AbstractAgent> result = new ArrayList<AbstractAgent>(bucketSize);
		final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
		//		System.err.println("nb of ag per task "+nbOfAgentsPerTask);
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
		for(int i=bucketSize-nbOfAgentsPerTask*cpuCoreNb;i>0;i--){
			//			System.err.println("adding aone");
			result.add(initAbstractAgent(agentClass));
		}
		//		System.err.println(result.size());
		return result;
	}

	private AbstractAgent initAbstractAgent(final Class<? extends AbstractAgent> agentClass){
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
		a.state.set(INITIALIZING); // no need to test : I created these instances
		a.setKernel(this);
		a.getAlive().set(true);
		a.logger = null;
		return a;
	}



	AbstractAgent launchAgent(AbstractAgent requester, final String agentClass, int timeOutSeconds,  boolean defaultGUI){
		Class<? extends AbstractAgent> aClass = null;
		try {
			aClass = (Class<? extends AbstractAgent>) platform.getMadkitClassLoader().loadClass(agentClass);
		} catch (ClassCastException e) {
			if(requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch "+agentClass+" because it is not an agent class");
			return null;
		} catch (ClassNotFoundException e) {
			if(requester.getLogger() != null)
				requester.getLogger().severe("Cannot launch "+agentClass+" because the class has not been found");
			return null;
		}
		try {
			final AbstractAgent agent = aClass.newInstance();
			if (launchAgent(requester, agent, timeOutSeconds,defaultGUI) == AGENT_CRASH) {
				return null; //TODO when time out ?
			}
			return agent;
		} catch (InstantiationException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "Cannot launch "
							+ agentClass
							+ " because it has no default constructor",
							"Launch failed", JOptionPane.WARNING_MESSAGE);
				}
			});
			if(requester.getLogger() != null)
				requester.getLogger().warning("Cannot launch "+agentClass+" because it has no default constructor");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}


	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds, final boolean defaultGUI){
		if(agent == null || timeOutSeconds < 0)
			return INVALID_ARG;
		final Future<ReturnCode> launchAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call(){
				return launchingAgent(requester, agent, defaultGUI);
			}
		});
		try {
			//		   if to == 0, this is still quicker than treating the case
			//			this is holds for Integer.MAX_VALUE
			return launchAttempt.get(timeOutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {// requester has been killed or something
			throw new KilledException(e);
		} catch (ExecutionException e) {// target has crashed !
			kernelLog("Launch failed on "+agent, Level.FINE, e);
			if(e.getCause() instanceof AssertionError)//convenient for Junit
				throw new AssertionError(e);
			return AGENT_CRASH;
		} catch (TimeoutException e) {// launch task time out
			return LAUNCH_TIME_OUT;
		}

	}

	ReturnCode launchingAgent(AbstractAgent requester,final AbstractAgent agent, boolean defaultGUI) {
		if(! agent.state.compareAndSet(NOT_LAUNCHED, INITIALIZING) || shuttedDown ){// this has to be done by a system thread
			return ALREADY_LAUNCHED;			
		}
		final ExecutorService agentExecutor = agent.getAgentExecutor();
		agent.setKernel(this);

		if (agent.getLogger() == AbstractAgent.defaultLogger) {
			if(defaultAgentLogLevel == Level.OFF){
				agent.logger = null;
			}
			else{
				agent.setLogLevel(defaultAgentLogLevel, defaultWarningLogLvl);
			}
		}
		if(! agent.getAlive().compareAndSet(false, true)){//TODO remove that
			throw new AssertionError("already alive in launch");
		}
		if(agentExecutor == null){
			return agent.activation(defaultGUI) ? SUCCESS : AGENT_CRASH;
		}
		try {
			return startAgentLifeCycle((Agent)agent,agentExecutor,defaultGUI).get() ? SUCCESS : AGENT_CRASH;
		} catch (InterruptedException e) {
			if (! shuttedDown) {
				kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); //Kernel cannot be interrupted !!
				return SEVERE;			
			}
		} catch (ExecutionException e) {
			if (! shuttedDown) {
				kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); //Kernel cannot be interrupted !!
				return SEVERE;			
			}
		}
		return LAUNCH_TIME_OUT;
	}

	Future<Boolean> startAgentLifeCycle(final Agent agent, final ExecutorService agentExecutor,final boolean gui) {
		final ArrayList<Future<Boolean>> lifeCycle = new ArrayList<Future<Boolean>>(4);
		final Future<Boolean> activation = agentExecutor.submit(new Callable<Boolean>() {

			public Boolean call(){
				final Thread currentThread = Thread.currentThread();
				//				if (! currentThread.isDaemon()) {
				//					activeThreadedAgents.add(agent);
				//				}
				agent.setMyThread(currentThread);
				if(! agent.activation(gui)){
					agent.getMyLifeCycle().get(1).cancel(true);//no activation no living
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


	ReturnCode killAgent(final AbstractAgent requester,final AbstractAgent target, final int timeOutSeconds){
		if(target == null || timeOutSeconds < 0)
			return INVALID_ARG;
		if (target.getState().compareTo(ACTIVATED) < 0) {
			return NOT_YET_LAUNCHED;
		}
		final Future<ReturnCode> killAttempt = serviceExecutor.submit(new Callable<ReturnCode>() {
			public ReturnCode call(){
				return killingAgent(target);
			}
		});
		try {
			return killAttempt.get(timeOutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {// requester has been killed or something
			throw new KilledException(e);
		} catch (ExecutionException e) {// target has crashed in end !
			kernelLog("kill failed on "+target, Level.FINE, e);
			if(e.getCause() instanceof AssertionError)
				throw new AssertionError(e);
			return AGENT_CRASH;
		} catch (TimeoutException e) {// kill task time out
			return LAUNCH_TIME_OUT;
		}
	}

	final ReturnCode killingAgent(final AbstractAgent target){
		if(! target.getAlive().compareAndSet(true,false)){// this has to be done by a system thread
			return ALREADY_KILLED;
		}
		final ExecutorService agentExecutor = target.getAgentExecutor();
		if(agentExecutor != null){
			killThreadedAgent((Agent) target, agentExecutor);
			return SUCCESS;
		}
		target.ending();
		target.terminate();
		return SUCCESS;
	}

	private void killThreadedAgent(final Agent target, ExecutorService agentExecutor){
		final ArrayList<Future<Boolean>> lifeCycle = target.getMyLifeCycle();
		lifeCycle.get(1).cancel(true);
		lifeCycle.get(0).cancel(true);
		try {
			lifeCycle.get(2).get(); //waiting that end ends with to
		} catch (CancellationException e) {
			kernelLog("wired", Level.SEVERE, e);
		} catch (InterruptedException e) {
			kernelLog("wired", Level.SEVERE, e);
		} catch (ExecutionException e) {//agent crashed in end
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

	/**
	 * @see madkit.kernel.RootKernel#setLogLevel(madkit.kernel.AbstractAgent, java.lang.String, madkit.kernel.AgentLogger, java.util.logging.Level, java.util.logging.Level)
	 */

	void setLogLevel(AbstractAgent requester, String loggerName, Level newLevel, Level warningLogLevel) {
		if(requester.logger == AbstractAgent.defaultLogger){
			requester.logger = null;
		}
		final AgentLogger currentLogger = requester.getLogger();
		if(currentLogger != null){
			currentLogger.setWarningLogLevel(warningLogLevel);
			currentLogger.setLevel(newLevel);
		}
		if (newLevel.equals(Level.OFF)) {
			requester.logger = null;
			return;
		}
		//the logger is null or has not the right name: find if the right one was already created
		if (currentLogger == null || ! currentLogger.getName().equals(loggerName)) {
			AgentLogger newLogger = new AgentLogger(loggerName);
			if (LogManager.getLogManager().addLogger(newLogger)) {// This is a new logger
				newLogger.init(requester, currentLogger, 
						!parseBoolean(getMadkitProperty(requester, Madkit.noAgentConsoleLog)), 
						parseBoolean(getMadkitProperty(requester, Madkit.createLogFiles)) ?
								getMadkitProperty(requester, Madkit.logDirectory)
								: null,
								getMadkitProperty(requester, Madkit.agentsLogFile));
				requester.logger = newLogger;
			} else { // if it already exists : get it !
				requester.logger = (AgentLogger) Logger.getLogger(loggerName);
			}
		}
		requester.getLogger().setLevel(newLevel);
		requester.getLogger().setWarningLogLevel(warningLogLevel);
		requester.setKernel(loggedKernel);
	}



	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Organization access
	///////////////////////////////////////////////////////////////////////////
	private Organization getCommunity(final String community) throws CGRNotAvailable{
		if(community == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		Organization org = organizations.get(community);
		if(org == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		return org;
	}

	private Group getGroup(final String community, final String group) throws CGRNotAvailable{
		Organization o = getCommunity(community);
		if(group == null)
			throw new CGRNotAvailable(NOT_GROUP);
		Group g = o.get(group);
		if(g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	private Role getRole(final String community, final String group, final String role) throws CGRNotAvailable{
		Group g = getGroup(community,group);// get group before for warning coherency
		if(role == null)
			throw new CGRNotAvailable(NOT_ROLE);
		Role r = g.get(role);
		if(r == null)
			throw new CGRNotAvailable(NOT_ROLE);
		return r;
	}

	//	List<AgentAddress> getRolePlayers(String community,String group, String role) throws CGRNotAvailable{
	//		return getRole(community, group, role).getAgentAddresses();
	//	}

	/**
	 * @param abstractAgent
	 * @param community
	 * @param group
	 * @param role
	 * @return null if nobody is found
	 * @throws CGRNotAvailable if one of community, group or role does not exist
	 */
	List<AgentAddress> getOtherRolePlayers(AbstractAgent abstractAgent, String community, String group, String role) throws CGRNotAvailable {
		//never null without throwing Ex
		final List<AgentAddress> result = new ArrayList<AgentAddress>(getRole(community, group, role).getAgentAddresses());
		Role.removeAgentAddressOf(abstractAgent, result);
		if (! result.isEmpty()) {
			return result;
		}
		return null;
		//		final java.util.List <AgentAddress> rolePlayers = getRolePlayers(community, group, role);
		//		List<AgentAddress> result = null;
		//		if(rolePlayers != null){
		//			result = new ArrayList<AgentAddress>(rolePlayers);
		//			Role.removeAgentAddressOf(abstractAgent, result);
		//			if (! result.isEmpty()) {
		//				return result;
		//			}
		//		}
		//		return null;
	}

	AgentAddress getAnotherRolePlayer(AbstractAgent abstractAgent,String community, String group, String role) throws CGRNotAvailable {
		final java.util.List <AgentAddress> rolePlayers = getOtherRolePlayers(abstractAgent,community,group, role);
		if(rolePlayers != null){
			return rolePlayers.get((int) (Math.random()*rolePlayers.size()));
		} 
		return null;
	}

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Messaging
	///////////////////////////////////////////////////////////////////////////

	private ReturnCode buildAndSendMessage(final AgentAddress sender,final AgentAddress receiver, final Message m){
		m.setSender(sender);
		m.setReceiver(receiver);
		return sendMessage(m);
		//		final AbstractAgent target = receiver.getAgent();
		//		if(target == null){
		//			if(netAgent != null)
		//				netAgent.receiveMessage(new MessageConveyor(m));
		//			else
		//				return NETWORK_DOWN;
		//		} 
		//		else{
		//			target.receiveMessage(m);
		//		}
		//		return SUCCESS;
	}

	final AgentAddress getSenderAgentAddress(final AbstractAgent sender, final AgentAddress receiver, String senderRole) throws CGRNotAvailable{
		AgentAddress senderAA = null;
		final Role targetedRole = receiver.getRoleObject();
		if(senderRole == null){//looking for any role in this group, starting with the receiver role
			senderAA = targetedRole.getAgentAddressInGroup(sender);
			//if still null : this SHOULD be a candidate's request to the manager or it is an error
			if(senderAA == null){
				if(targetedRole.getRoleName().equals(Roles.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				else
					throw new CGRNotAvailable(NOT_IN_GROUP);
			}
			return senderAA;
		}
		//the sender explicitly wants to send the message with a particular role : check that 
		else{
			//look into the senderRole role if the agent is in
			final Role senderRoleObject = targetedRole.getMyGroup().get(senderRole);
			if (senderRoleObject != null) {
				senderAA = senderRoleObject.getAgentAddressOf(sender);
			}
			if(senderAA == null){//if still null : this SHOULD be a candidate's request to the manager or it is an error
				if(senderRole.equals(Roles.GROUP_CANDIDATE_ROLE) && targetedRole.getRoleName().equals(Roles.GROUP_MANAGER_ROLE))
					return new CandidateAgentAddress(sender, targetedRole, kernelAddress);
				if(targetedRole.getAgentAddressInGroup(sender) == null)
					throw new CGRNotAvailable(NOT_IN_GROUP);
				else
					throw new CGRNotAvailable(ROLE_NOT_HANDLED);
			}
			return senderAA;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Simulation
	///////////////////////////////////////////////////////////////////////////


	boolean addOverlooker(final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		if(operatingOverlookers.add(o)){
			try {
				getRole(o.getCommunity(),o.getGroup(),o.getRole()).addOverlooker(o);
			}
			catch (CGRNotAvailable e) {
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
		if(r != null){
			r.removeOverlooker(o);
		}
		return operatingOverlookers.remove(o);
	}


	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Internal functioning
	///////////////////////////////////////////////////////////////////////////

	void removeCommunity(String community){
		organizations.remove(community);
	}

	//	
	//	void removeThreadedAgent(Agent a){
	//		activeThreadedAgents.remove(a);
	//		//		if(activeThreadedAgents.isEmpty()){
	//		//			kernelAgent.receiveMessage(new KernelMessage(OperationCode.SHUTDOWN_NOW,(Object[])null));
	//		//		}
	//	}


	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {
		return platform.getMadkitClassLoader().loadClass(className);
	}


	@Override
	public KernelAddress getKernelAddress() {
		return getMadkitKernel().kernelAddress;
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
			if(netAgent != null){
				for(final String groupName : groups){
					sendNetworkMessage(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_GROUP, new AgentAddress(theAgent, new Role(org.getName(), groupName), kernelAddress)));
				}
			}
		}
	}




	String getMadkitProperty(AbstractAgent abstractAgent, String key) {
		return platform.getConfigOption().getProperty(key);
	}


	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		platform.checkAndValidateOption(key, value);//TODO update agent logging on or off
	}

	MadkitKernel getMadkitKernel(){
		return this;
	}


	/**
	 * Asks MasKit to reload the class byte code so that new instances, 
	 * created using {@link Class#newInstance()} on a class object obtained with
	 * {@link #getNewestClassVersion(AbstractAgent, String)}, will reflect compilation changes
	 * during run time. 
	 * 
	 * @param requester
	 * @param name The fully qualified class name of the class
	 * @throws ClassNotFoundException 
	 */

	ReturnCode reloadClass(AbstractAgent requester, String name) throws ClassNotFoundException {
		if(name == null)
			throw new ClassNotFoundException(ReturnCode.CLASS_NOT_FOUND.getMessage()+" "+name);
		if(	! name.contains("madkit.kernel")
				&& ! name.contains("madkit.gui")
				&& ! name.contains("madkit.messages")
				&& ! name.contains("madkit.simulation")
				&& platform.getMadkitClassLoader().reloadClass(name))
			return SUCCESS;
		return ReturnCode.CLASS_NOT_FOUND;//TODO not the right code here
	}

	/**
	 * @see madkit.kernel.RootKernel#isCommunity(madkit.kernel.AbstractAgent, java.lang.String)
	 */

	boolean isCommunity(AbstractAgent requester, String community) {
		try {
			return getCommunity(community) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#isGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */

	boolean isGroup(AbstractAgent requester, String community, String group) {
		try {
			return getGroup(community,group) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#isGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */

	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		try {
			return getRole(community,group,role) != null;
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


	synchronized void importDistantOrg(NetworkAgent networkAgent, SortedMap<String, SortedMap<String, SortedMap<String, List<AgentAddress>>>> distantOrg) {
		for (String communityName : distantOrg.keySet()) {
			Organization org = new Organization(communityName, this);
			Organization previous = organizations.putIfAbsent(communityName, org);
			if(previous != null){
				org = previous;
			}
//			Organization org = organizations.get(communityName);
//			if(org == null){
//				organizations.put(communityName, org);
//			}
			org.importDistantOrg(distantOrg.get(communityName));
		}
	}


	synchronized SortedMap<String, SortedMap<String, SortedMap<String, List<AgentAddress>>>> getOrganizationSnapShot(AbstractAgent requester, boolean global) {
		SortedMap<String,SortedMap<String,SortedMap<String,List<AgentAddress>>>> export = new TreeMap<String, SortedMap<String,SortedMap<String,List<AgentAddress>>>>();
		for (Map.Entry<String, Organization> org : organizations.entrySet()) {
			export.put(org.getKey(),org.getValue().getOrgMap(true));
		}
		return export;
	}

	//	void logCurrentOrganization(Logger requester, Level lvl){
	//		if(requester != null){
	//			String message = "Current organization is\n";
	//			if(organizations.isEmpty()){
	//				message+="\n ------------ EMPTY !! ------------\n";
	//			}
	//			for(final Map.Entry<String, Organization> org : organizations.entrySet()){
	//				message+="\n\n--"+org.getKey()+"----------------------";
	//				for(final Map.Entry<String, Group> group : org.getValue().entrySet()){
	//					//					final AgentAddress manager = group.getValue().getManager().get();
	//					message+="\n|--"+group.getKey()+"--";// managed by ["+manager.getAgent()+"] "+manager+" --\n";
	//					for(final Map.Entry<String, Role> role: group.getValue().entrySet()){
	//						message+="\n||--"+role.getKey()+"--";
	//						message+="\n|||--players- "+role.getValue().getPlayers();
	//						message+="\n|||--addresses- = "+role.getValue().getAgentAddresses();
	//					}
	//				}
	//				message+="\n-----------------------------";
	//			}
	//			requester.log(lvl, message+"\n");
	//		}
	//	}


	void injectMessage(NetworkAgent networkAgent,final Message m){
		//		System.err.println("\n\n ----------------------dadadz------------"+m);
		final AgentAddress receiver = m.getReceiver();
		final AgentAddress sender = m.getSender();
		Role receiverRole = receiver.getRoleObject();
		Role senderRole = sender.getRoleObject(); //TODO to optimize
		try {
			receiverRole = organizations.get(receiverRole.getCommunityName()).get(receiverRole.getGroupName()).get(receiverRole.getRoleName());
			receiver.setRoleObject(receiverRole);
			senderRole = organizations.get(senderRole.getCommunityName()).get(senderRole.getGroupName()).get(senderRole.getRoleName());
			sender.setRoleObject(senderRole);
			receiverRole.getAbstractAgentWithAddress(receiver).receiveMessage(m);
		} catch (NullPointerException e) {
			//			if(logger != null){
			//				logger.finer("message received but the agentaddress is no longer valid !!");
			//				logCurrentOrganization(logger,Level.FINEST);
		}
	}


	void injectOperation(NetworkAgent networkAgent, int operation, AgentAddress content) {
		final Role r = content.getRoleObject();
		final String communityName = r.getCommunityName();
		final String groupName = r.getGroupName();
		final String roleName = r.getRoleName();
		switch (operation) {
		case CGRSynchroMessage.CREATE_GROUP:
			Organization organization = new Organization(communityName,this);//TODO optimize , no need to remove org never failed if not present
			final Organization tmpOrg = organizations.putIfAbsent(communityName, organization);
			if(tmpOrg != null){
				organization = tmpOrg;
			}
			if(organization.get(groupName) != null){
				//				if(orgLogger != null)
				//					orgLogger.warning("distant group creation by "+content+" aborted : already exists locally");
			}
			else{
				//				if(orgLogger != null)
				//					orgLogger.finer("distant group creation by "+content+" : group is "+printCGR(communityName, groupName));
				organization.put(groupName,new Group(communityName,groupName,content,null,organization));
			}

			break;
		case CGRSynchroMessage.REQUEST_ROLE:
			final Group g = organizations.get(communityName).get(groupName);
			if(g == null){
				//				if(orgLogger != null)
				//					orgLogger.warning("distant request role "+printCGR(communityName, groupName, roleName)+" by "+content+" aborted : group "+groupName+" does not exists locally");
			} 
			else {
				//				if(orgLogger != null)
				//					orgLogger.finer("distant request role "+printCGR(communityName, groupName, roleName)+" by "+content+" successed");
				g.addDistantMember(content);
			}
			break;
		case CGRSynchroMessage.LEAVE_ROLE:
			try {
				getRole(communityName, groupName, roleName).removeDistantMember(content);
			} catch (CGRNotAvailable e) {
			}
			//			if(localRole == null){
			//				if(orgLogger != null)
			//					orgLogger.warning("distant leave role "+printCGR(communityName, groupName, roleName)+" by "+content+" aborted : group "+groupName+" does not exists locally");
			//			} 
			//			else {
			//				if(orgLogger != null)
			//					orgLogger.finer("distant leave role "+printCGR(communityName, groupName, roleName)+" by "+content+" successed");
			//				localRole.removeDistantMember(content);
			//			}
			break;
		case CGRSynchroMessage.LEAVE_GROUP:
			final Group g1 = organizations.get(communityName).get(groupName);
			if(g1 == null){
				//				if(orgLogger != null)
				//					orgLogger.warning("distant leave group "+printCGR(communityName, groupName, roleName)+" by "+content+" aborted : group "+groupName+" does not exists locally");
			} 
			else {
				//				if(orgLogger != null)
				//					orgLogger.finer("distant leave group "+printCGR(communityName, groupName, roleName)+" by "+content+" successed");
				g1.removeDistantMember(content);
			}
			break;
		case CGRSynchroMessage.LEAVE_ORG://TODO to implement
			break;
		default:
			break;
		}

	}

	/**
	 * @param lk
	 */
	void setLoggedKernel(LoggedKernel lk) {
		loggedKernel = lk;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.RootKernel#kernelLog(java.lang.String)
	 */

	void kernelLog(String message, Level logLvl, Throwable e) {
		platform.kernelLog(message, logLvl, e);
	}

	synchronized void shutdown() {
		shuttedDown = true;
		//		Thread t = new Thread(new Runnable() {
		//			public void run() {
		if(logger != null){
			logger.talk(platform.printFareWellString());
		}
		AbstractAgent.normalAgentThreadFactory.getThreadGroup().interrupt();
		pause(100);
		AbstractAgent.normalAgentThreadFactory.getThreadGroup().interrupt();
		MadkitKernel.this.kernel.getMadkitKernel().broadcastMessageWithRoleAndWaitForReplies(
				MadkitKernel.this,
				LOCAL_COMMUNITY, 
				SYSTEM_GROUP, 
				GUI_MANAGER_ROLE, 
				new GUIMessage(GUIMessage.GuiCode.SHUTDOWN,MadkitKernel.this), 
				null, 
				10000);//TODO if it takes too long
		//		if(logger != null)
		//			logger.fine("Shutting down now !!");
		LogManager.getLogManager().reset();
		//			}
		//		});
		//		t.start();
	}

	boolean createGroupIfAbsent(AbstractAgent abstractAgent,
			String community, String group, String group2,
			GroupIdentifier theIdentifier, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, group, theIdentifier, isDistributed) == SUCCESS;
	}

}

final class CGRNotAvailable extends Exception{

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

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}

}