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

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.LAUNCH_TIME_OUT;
import static madkit.kernel.AbstractAgent.ReturnCode.NETWORK_DOWN;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_MSG;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SEVERE;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.ACTIVATED;
import static madkit.kernel.AbstractAgent.State.INITIALIZING;
import static madkit.kernel.AbstractAgent.State.NOT_LAUNCHED;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import madkit.gui.DefaultGUIsManagerAgent;
import madkit.gui.GUIsManagerAgent;
import madkit.gui.MadKitGUIsManager;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;

/**
 * The brand new madkit kernel
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
final class MadkitKernel extends RootKernel{

	final static ExecutorService serviceExecutor = Executors.newCachedThreadPool();
	static{
		final ThreadPoolExecutor t = (ThreadPoolExecutor) serviceExecutor;
		t.setCorePoolSize(Runtime.getRuntime().availableProcessors()+1);
		t.setThreadFactory(new ThreadFactory() {
			@Override
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
	final private List<AbstractAgent> activeThreadedAgents;
	final private Set<Overlooker<? extends AbstractAgent>> operatingOverlookers;

	final private Madkit platform;
	private KernelAddress kernelAddress;
	private AbstractAgent netAgent;
//	private MadKitGUIsManager guiManager;
	private Level defaultAgentLogLevel;
	private Level defaultWarningLogLvl;

	private LoggedKernel loggedKernel;
	private KernelAgent kernelAgent;
	private boolean shuttedDown = false;

	/**
	 * 
	 */
	MadkitKernel(Madkit m) {
		platform = m;
		kernelAddress = platform.getPlatformID();
		setDefaultAgentLogLevel(Level.parse(platform.getConfigOption().getProperty(Madkit.agentLogLevel)),
				Level.parse(platform.getConfigOption().getProperty(Madkit.warningLogLevel)));
		organizations = new ConcurrentHashMap<String, Organization>();
		operatingOverlookers = new LinkedHashSet<Overlooker<? extends AbstractAgent>>();
		activeThreadedAgents = Collections.synchronizedList(new ArrayList<AbstractAgent>());//TODO useless
	}

	/**
	 * @return the kernelAgent
	 */
	final KernelAgent getKernelAgent() {
		return kernelAgent;
	}

	/**
	 * @param kernelAgent the kernelAgent to set
	 */
	final void setKernelAgent(KernelAgent kernelAgent) {
		this.kernelAgent = kernelAgent;
	}

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
	@Override
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
			networkUpdate(new CGRSynchroMessage(
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
	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		final Group g;
		try {
			g = getGroup(community, group);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		final ReturnCode result = g.requestRole(requester, role, memberCard);
		if(g.isDistributed() && result == SUCCESS) {
			networkUpdate(
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
	@Override
	ReturnCode leaveGroup(final AbstractAgent requester, final String community, final String group) {
		final Group g;
		try {
			g = getGroup(community, group);
		} catch (CGRNotAvailable e) {
			return e.getCode();
		}
		final ReturnCode result = g.leaveGroup(requester);;
		if(g.isDistributed() && result == SUCCESS){
			networkUpdate(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_GROUP, new AgentAddress(requester, new Role(community, group), kernelAddress)));
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
	@Override
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
			networkUpdate(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_ROLE, r.getAgentAddressOf(requester)));
		}
		return result;
	}


	/**
	 * @see madkit.kernel.RootKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	//Warning never touch this without looking at the logged kernel
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role){
		try {
			return getOtherRolePlayers(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			return null;
		}
	}

	@Override
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
	@Override
	final ReturnCode sendMessage(final AbstractAgent requester, final String community, final String group, final String role, final Message message, final String senderRole) {
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

	@Override
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

	@Override
	final ReturnCode sendReplyWithRole(final AbstractAgent requester, final Message messageToReplyTo, final Message reply, String senderRole) {
		if(messageToReplyTo == null || reply == null){
			return INVALID_ARG;
		}
		reply.setID(messageToReplyTo.getID());
		return sendMessage(requester,messageToReplyTo.getSender(),reply,senderRole);
	}

	@Override
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
	
	@Override
	List<Message> broadcastMessageWithRoleAndWaitForReplies(final Agent requester,  final String community, final String group, final String role, 
			Message message,
			final String senderRole, 
			final Integer timeOutMilliSeconds){
		try {
			final List<AgentAddress> receivers = getOtherRolePlayers(requester,community, group, role);
			if(message == null || receivers == null)
				return null; // the requester is the only agent in this group
			message.setSender(getSenderAgentAddress(requester, receivers.get(0), senderRole));
			broadcasting(receivers, message);
			return requester.waitAnswers(message,receivers.size(),timeOutMilliSeconds);
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
			if(netAgent != null)
				netAgent.receiveMessage(new MessageConveyor(m));
			else
				return NETWORK_DOWN;
		} else{
			target.receiveMessage(m);
		}
		return SUCCESS;
	}
	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
	@Override
	synchronized List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent requester, String agentClassName,int bucketSize,Collection<String> CGRLocations){
		final Class<? extends AbstractAgent> agentClass = getPlatform().loadClass(requester, agentClassName);
		if(agentClass == null)
			return null;
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
			a.activation();
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
				@Override
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


	@Override
	AbstractAgent launchAgent(AbstractAgent requester, String agentClass, int timeOutSeconds,  boolean defaultGUI){
		final Class<? extends AbstractAgent> aClass = getPlatform().loadClass(requester, agentClass);
		if(aClass == null)
			return null;
		//		Future<AbstractAgent> createInstance = serviceExecutor.submit(new Callable<AbstractAgent>() {
		//			public AbstractAgent call() throws Exception {
		//				return (AbstractAgent) aClass.newInstance();
		//			};
		//		});
		//		AbstractAgent agent;
		//		try {
		//			agent = createInstance.get();
		//		} catch (InterruptedException e) {// requester has been killed or something
		//			throw new KilledException(e);
		//		} catch (ExecutionException e) {// target has crashed !
		//			kernelLog("Launch failed on "+agentClass, Level.FINE, e);
		//			if(e.getCause() instanceof AssertionError)
		//				throw new AssertionError(e);
		//			return null;
		//		}
		//		if (launchAgent(requester, agent, timeOutSeconds,defaultGUI) == AGENT_CRASH) {
		//			return null; //TODO when time out ?
		//		}
		//		return agent;
		try {
			final AbstractAgent agent = aClass.newInstance();
			if (launchAgent(requester, agent, timeOutSeconds,defaultGUI) == AGENT_CRASH) {
				return null; //TODO when time out ?
			}
			return agent;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
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

	private ReturnCode launchingAgent(AbstractAgent requester,final AbstractAgent agent, boolean defaultGUI) {
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

		if (defaultGUI){
			GUIsManagerAgent guiManagerAgent = getGUIManager();
			if(guiManagerAgent != null)
				guiManagerAgent.setupGUIOf(agent);
		}

		if(! agent.getAlive().compareAndSet(false, true)){//TODO remove that
			throw new AssertionError("already alive in launch");
		}
		if(agentExecutor == null){
			return agent.activation() ? SUCCESS : AGENT_CRASH;
		}

		//		Future<Boolean> activation = null;
		//			activation = startAgentLifeCycle((Agent)agent,agentExecutor);

		//		synchronized (agent.getAlive()) {
		//			try {
		//				agent.getAlive().wait();
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//				Utils.logSevereException(null, e, "*********please bug report !!");
		//			}
		//		}
		try {
			return startAgentLifeCycle((Agent)agent,agentExecutor).get() ? SUCCESS : AGENT_CRASH;
		} catch (InterruptedException e) {
			kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); //Kernel cannot be interrupted !!
			return SEVERE;			
		} catch (ExecutionException e) {
			kernelLog("KERNEL PROBLEM, please bug report", Level.SEVERE, e); //Kernel cannot be interrupted !!
			return SEVERE;			
		}
	}

	Future<Boolean> startAgentLifeCycle(final Agent agent, final ExecutorService agentExecutor) {
		final ArrayList<Future<Boolean>> lifeCycle = new ArrayList<Future<Boolean>>(4);
		final Future<Boolean> activation = agentExecutor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call(){
				final Thread currentThread = Thread.currentThread();
				if (! currentThread.isDaemon()) {
					activeThreadedAgents.add(agent);
				}
				agent.setMyThread(currentThread);
				if(! agent.activation()){
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

	@Override
	final ReturnCode killAgent(final AbstractAgent requester,final AbstractAgent target, final int timeOutSeconds){
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
	@Override
	void setLogLevel(AbstractAgent requester, String loggerName, Level newLevel, Level warningLogLevel) {
		super.setLogLevel(requester, loggerName, newLevel, warningLogLevel);
		if(requester.getLogger() != null){
			requester.setKernel(loggedKernel);
		}
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

	ReturnCode buildAndSendMessage(final AgentAddress sender,final AgentAddress receiver, final Message m){
		m.setSender(sender);
		m.setReceiver(receiver);
		final AbstractAgent target = receiver.getAgent();
		if(target == null){
			if(netAgent != null)
				netAgent.receiveMessage(new MessageConveyor(m));
			else
				return NETWORK_DOWN;
		} 
		else{
			target.receiveMessage(m);
		}
		return SUCCESS;
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

	@Override
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
	@Override
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
	/**
	 * @param cgrSynchroMessage
	 */
	void networkUpdate(CGRSynchroMessage cgrSynchroMessage) {
		if(netAgent != null)
			netAgent.receiveMessage(cgrSynchroMessage);
	}

	/**
	 * @return the platform
	 */
	final Madkit getPlatform() {
		return platform;
	}

	void removeCommunity(String community){
		organizations.remove(community);
	}

	@Override
	void removeThreadedAgent(Agent a){
		activeThreadedAgents.remove(a);
		//		if(activeThreadedAgents.isEmpty()){
		//			kernelAgent.receiveMessage(new KernelMessage(OperationCode.SHUTDOWN_NOW,(Object[])null));
		//		}
	}

	@Override
	KernelAddress getKernelAddress(AbstractAgent requester) {
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
	@Override
	void disposeGUIOf(AbstractAgent agent) {
		GUIsManagerAgent guiManager = getGUIManager();
		if(guiManager != null)
			guiManager.disposeGUIOf(agent);
	}

	/**
	 * @param abstractAgent
	 */
	@Override
	void removeAgentFromOrganizations(AbstractAgent theAgent) {
		for (final Organization org : organizations.values()) {
			final ArrayList<String> groups = org.removeAgentFromAllGroups(theAgent);
			if(netAgent != null){
				for(final String groupName : groups){
					netAgent.receiveMessage(new CGRSynchroMessage(CGRSynchroMessage.LEAVE_GROUP, new AgentAddress(theAgent, new Role(org.getName(), groupName), kernelAddress)));
				}
			}
		}
	}

	@Override
	String getMadkitProperty(AbstractAgent abstractAgent, String key) {
		return getPlatform().getConfigOption().getProperty(key);
	}

	@Override
	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		platform.checkAndValidateOption(key, value);//TODO update agent logging on or off
	}

	private GUIsManagerAgent getGUIManager() {
		try {
			return (GUIsManagerAgent) getRole(Madkit.Roles.LOCAL_COMMUNITY,
					Madkit.Roles.SYSTEM_GROUP,
					Madkit.Roles.GUI_MANAGER_ROLE).getAgentAddresses().get(0).getAgent();
		} catch (CGRNotAvailable e) {
			// no gui manager //TODO log this
		} catch (ClassCastException e) {
			// TODO: handle exception
		}		
		return null;
	}

	/**
	 * @param abstractAgent
	 * @param agentClass
	 */
	@Override
	ReturnCode reloadClass(AbstractAgent abstractAgent, String agentClass) {
		try {
			getPlatform().reloadClass(agentClass);
		} catch (ClassNotFoundException e) {
			return ReturnCode.CLASS_NOT_FOUND;
		}
		return SUCCESS;
	}

	/**
	 * @see madkit.kernel.RootKernel#isCommunity(madkit.kernel.AbstractAgent, java.lang.String)
	 */
	@Override
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
	@Override
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
	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		try {
			return getRole(community,group,role) != null;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * @param a
	 */
	void setNetAgent(AbstractAgent a) {
		netAgent = a;
	}

//	/**
//	 * @param booter
//	 */
//	void setGuiManager(MadKitGUIsManager booter) {
//		guiManager = booter;		
//	}

	/**
	 * @param l
	 */
	void setDefaultAgentLogLevel(Level agentLoglevel, Level warningLogLevel) {
		defaultAgentLogLevel = agentLoglevel;
		defaultWarningLogLvl = warningLogLevel;
	}

	@Override
	synchronized void importDistantOrg(NetworkAgent networkAgent, HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> distantOrg) {
		for (String communityName : distantOrg.keySet()) {
			Organization org = organizations.get(communityName);
			if(org == null){
				org = new Organization(communityName, this);
				organizations.put(communityName, org);
			}
			org.importDistantOrg(distantOrg.get(communityName));
		}
	}

	@Override
	synchronized HashMap<String,HashMap<String,HashMap<String,List<AgentAddress>>>> getLocalOrg(NetworkAgent networkAgent) {
		HashMap<String,HashMap<String,HashMap<String,List<AgentAddress>>>> export = new HashMap<String, HashMap<String,HashMap<String,List<AgentAddress>>>>();
		for (Map.Entry<String, Organization> org : organizations.entrySet()) {
			export.put(org.getKey(),org.getValue().getLocalOrg());
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

	@Override
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

	@Override
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
	@Override
	final void kernelLog(String message, Level logLvl, Throwable e) {
		platform.kernelLog(message, logLvl, e);
	}

	synchronized void shutdown() {
		shuttedDown = true;
		AbstractAgent.normalAgentThreadFactory.getThreadGroup().interrupt();
		LogManager.getLogManager().reset();
		platform.printFareWellString();
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