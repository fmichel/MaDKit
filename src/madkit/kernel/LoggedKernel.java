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

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import madkit.kernel.AbstractAgent.ReturnCode;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.Utils.getI18N;
import static madkit.kernel.Utils.printCGR;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0.0.7
 *
 */
// TODO logger from agent should never be null
final class LoggedKernel extends RootKernel {

	final private MadkitKernel madkitKernel;

	LoggedKernel(MadkitKernel k) {
		madkitKernel = k;
	}

	@Override
	void setLogLevel(AbstractAgent requester, String loggerName, Level newLevel, Level warningLogLevel) {
		logMessage(requester, "Changing log level from " + (requester.getLogger() == null ? "OFF" : requester.getLogger().getLevel()) + " to " + newLevel);
		super.setLogLevel(requester, loggerName, newLevel, warningLogLevel);
		if (requester.getLogger() == null) {
			requester.setKernel(madkitKernel);
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#createGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, madkit.kernel.GroupIdentifier, boolean)
	 */
	@Override
	ReturnCode createGroup(AbstractAgent requester, String community, String group, String description, GroupIdentifier theIdentifier, boolean isDistributed) {
		logMessage(requester, "createGroup" + printCGR(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		switch (madkitKernel.createGroup(requester, community, group, group, theIdentifier, isDistributed)) {
		case SUCCESS:
			return SUCCESS;
		case ALREADY_GROUP:
			return requester.handleException(new CreateGroupWarning(ALREADY_GROUP, printCGR(community, group)));
		case NULL_STRING:
			return requester.handleException(new RequestRoleWarning(NULL_STRING, printCGR(community)));
		default:
			return requester.handleException(new RequestRoleWarning(SEVERE, "result not handled"));
		}
	}
	
	@Override
	public boolean createGroupIfAbsent(AbstractAgent requester,String community, String group, String desc, GroupIdentifier theIdentifier, boolean isDistributed) {
		logMessage(requester, "createGroupIfAbsent" + printCGR(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		return madkitKernel.createGroup(requester, community, group, desc,theIdentifier, isDistributed) == SUCCESS;
	}
	

	/**
	 * @see madkit.kernel.MadkitKernel#requestRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		logMessage(requester, "requestRole" + printCGR(community, group, role) + "using " + memberCard + " as passKey");
		switch (madkitKernel.requestRole(requester, community, group, role, memberCard)) {
		case SUCCESS:
			return SUCCESS;
		case NOT_COMMUNITY:
			return requester.handleException(new RequestRoleWarning(NOT_COMMUNITY, printCGR(community)));
		case ROLE_ALREADY_HANDLED:
			return requester.handleException(new RequestRoleWarning(ROLE_ALREADY_HANDLED, printCGR(community, group, role)));
		case NOT_GROUP:
			return requester.handleException(new RequestRoleWarning(NOT_GROUP, printCGR(community, group)));
		case ACCESS_DENIED:
			return requester.handleException(new RequestRoleWarning(ACCESS_DENIED, printCGR(community, group, role)));
		case NULL_STRING:
			return requester.handleException(new RequestRoleWarning(NULL_STRING, printCGR(community, group, role)));
		default:
			return requester.handleException(new RequestRoleWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveGroup(AbstractAgent requester, String community, String group) {
		logMessage(requester, "leaveGroup" + printCGR(community, group));
		switch (madkitKernel.leaveGroup(requester, community, group)) {
		case SUCCESS:
			return SUCCESS;
		case NOT_COMMUNITY:
			return requester.handleException(new LeaveGroupWarning(NOT_COMMUNITY, printCGR(community)));
		case NOT_GROUP:
			return requester.handleException(new LeaveGroupWarning(NOT_GROUP, printCGR(community, group)));
		case NOT_IN_GROUP:
			return requester.handleException(new LeaveGroupWarning(NOT_IN_GROUP, printCGR(community, group)));
		default:
			return requester.handleException(new LeaveGroupWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveRole(AbstractAgent requester, String community, String group, String role) {
		logMessage(requester, "leaveRole" + printCGR(community, group, role));
		switch (madkitKernel.leaveRole(requester, community, group, role)) {
		case SUCCESS:
			return SUCCESS;
		case NOT_COMMUNITY:
			return requester.handleException(new RequestRoleWarning(NOT_COMMUNITY, printCGR(community)));
		case NOT_ROLE:
		case ROLE_NOT_HANDLED:
			return requester.handleException(new RequestRoleWarning(ROLE_NOT_HANDLED, printCGR(community, group, role)));
		case NOT_GROUP:
			return requester.handleException(new RequestRoleWarning(NOT_GROUP, printCGR(community, group)));
		default:
			return requester.handleException(new RequestRoleWarning(SEVERE, "result not handled "+madkitKernel.leaveRole(requester, community, group, role)));
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role) {
		logMessage(requester, "getAgentsWithRole" + printCGR(community, group, role));
		try {
			return madkitKernel.getOtherRolePlayers(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			switch (e.getCode()) {
			case NOT_COMMUNITY:
				requester.handleException(new getAgentsWithRoleWarning(NOT_COMMUNITY, printCGR(community)));
				return null;
			case NOT_GROUP:
				requester.handleException(new getAgentsWithRoleWarning(NOT_GROUP, printCGR(community, group)));
				return null;
			case NOT_ROLE:
				requester.handleException(new getAgentsWithRoleWarning(NOT_ROLE, printCGR(community, group, role)));
				return null;
			default:
				requester.handleException(new getAgentsWithRoleWarning(SEVERE, "result not handled"));
				return null;
			}
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role) {
		logMessage(requester, "getAgentWithRole" + printCGR(community, group, role));
		try {
			return madkitKernel.getAnotherRolePlayer(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			switch (e.getCode()) {
			case NOT_COMMUNITY:
				requester.handleException(new getAgentWithRoleWarning(NOT_COMMUNITY, printCGR(community)));
				return null;
			case NOT_GROUP:
				requester.handleException(new getAgentWithRoleWarning(NOT_GROUP, printCGR(community, group)));
				return null;
			case NOT_ROLE:
				requester.handleException(new getAgentWithRoleWarning(NOT_ROLE, printCGR(community, group, role)));
				return null;
			default:
				requester.handleException(new getAgentWithRoleWarning(SEVERE, "result not handled"));
				return null;
			}
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#broadcastMessageWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, madkit.kernel.Message, java.lang.String)
	 */
	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		logMessage(requester, "broadcastMessage to <" + community + "," + group + "," + role + ">");
		switch (madkitKernel.broadcastMessageWithRole(requester, community, group, role, messageToSend, senderRole)) {
		case SUCCESS:
			return SUCCESS;
		case INVALID_ARG:
			return requester.handleException(new sendMessageWarning(INVALID_ARG, " Cannot broadcast a null message"));
		case ROLE_NOT_HANDLED:
			return requester.handleException(new sendMessageWarning(ROLE_NOT_HANDLED, printCGR(community, group, senderRole)));
		case NOT_COMMUNITY:
			return requester.handleException(new sendMessageWarning(NOT_COMMUNITY, printCGR(community)));
		case NOT_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_GROUP, printCGR(community, group)));
		case NOT_ROLE:
			return requester.handleException(new sendMessageWarning(NOT_ROLE, printCGR(community, group, role)));
		case NO_RECIPIENT_FOUND:
			return requester.handleException(new sendMessageWarning(NO_RECIPIENT_FOUND, "nobody found in "+printCGR(community,group,role)));
		case NOT_IN_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_IN_GROUP, printCGR(community, group)));
		default:
			return requester.handleException(new sendMessageWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#sendMessage(madkit.kernel.AbstractAgent, madkit.kernel.AgentAddress, madkit.kernel.Message, java.lang.String)
	 */
	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		logMessage(requester, "sendMessage to " + receiver);
		switch (madkitKernel.sendMessage(requester, receiver, messageToSend, senderRole)) {
		case SUCCESS:
			return SUCCESS;
		case INVALID_ARG:
			return requester.handleException(new sendMessageWarning(INVALID_ARG, messageToSend == null ? " Cannot send a null message" : "receiver is null"));
		case ROLE_NOT_HANDLED:
			return requester.handleException(new sendMessageWarning(ROLE_NOT_HANDLED, printCGR(receiver.getCommunity(), receiver.getGroup(), senderRole)));
		case NOT_IN_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_IN_GROUP, printCGR(receiver.getCommunity(), receiver.getGroup())));
		case INVALID_AA:
			return requester.handleException(new sendMessageWarning(INVALID_AA, printCGR(receiver.getCommunity(), receiver.getGroup(), receiver.getRole())));
		default:
			return requester.handleException(new sendMessageWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#sendMessage(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, madkit.kernel.Message, java.lang.String)
	 */
	@Override
	ReturnCode sendMessage(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		logMessage(requester, "sendMessage to " + printCGR(community,group,role));
		switch (madkitKernel.sendMessage(requester, community, group, role, messageToSend, senderRole)) {
		case SUCCESS:
			return SUCCESS;
		case INVALID_ARG:
			return requester.handleException(new sendMessageWarning(INVALID_ARG, messageToSend == null ? " Cannot send a null message" : "receiver is null"));
		case NOT_COMMUNITY:
			return requester.handleException(new sendMessageWarning(NOT_COMMUNITY, printCGR(community)));
		case NOT_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_GROUP, printCGR(community, group)));
		case NOT_ROLE:
			return requester.handleException(new sendMessageWarning(NOT_ROLE, printCGR(community, group,role)));
		case ROLE_NOT_HANDLED:
			return requester.handleException(new sendMessageWarning(ROLE_NOT_HANDLED, printCGR(community,group,senderRole)));
		case NOT_IN_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_IN_GROUP, printCGR(community,group)));
		case NO_RECIPIENT_FOUND:
			return requester.handleException(new sendMessageWarning(NO_RECIPIENT_FOUND, "nobody found in "+printCGR(community,group,role)));
		default:
			return requester.handleException(new sendMessageWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#sendReplyWithRole(madkit.kernel.AbstractAgent, madkit.kernel.Message, madkit.kernel.Message, java.lang.String)
	 */
	@Override
	ReturnCode sendReplyWithRole(AbstractAgent requester, Message messageToReplyTo, Message reply, String senderRole) {
		logMessage(requester, "sending " + reply + " as reply to " + messageToReplyTo);
		// TODO Auto-generated method stub
		switch (madkitKernel.sendReplyWithRole(requester, messageToReplyTo, reply, senderRole)) {
		case SUCCESS:
			return SUCCESS;
		case INVALID_ARG:
			return requester.handleException(new sendMessageWarning(NULL_MSG, messageToReplyTo != null ? " Cannot reply with a null message" : "Cannot reply to a null message"));
		case NULL_MSG:
			return requester.handleException(new sendMessageWarning(NULL_MSG, messageToReplyTo != null ? " Cannot reply with a null message" : "Cannot reply to a null message"));
		case ROLE_NOT_HANDLED:
			return requester.handleException(new sendMessageWarning(ROLE_NOT_HANDLED, printCGR(reply.getReceiver().getCommunity(), reply.getReceiver().getGroup(), senderRole)));
		case NULL_AA:
			return requester.handleException(new sendMessageWarning(NULL_AA, "agent address not valid"));
		case INVALID_AA:
			return requester.handleException(new sendMessageWarning(INVALID_AA, " null sender address from original message : this seems to not be a previously received message " + messageToReplyTo));
		case NOT_IN_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_IN_GROUP, printCGR(
					messageToReplyTo.getReceiver().getCommunity(), 
					messageToReplyTo.getReceiver().getGroup())));
		default:
			return requester.handleException(new sendMessageWarning(SEVERE, "result not handled"));
		}
	}

	@Override
	List<Message> broadcastMessageWithRoleAndWaitForReplies(Agent requester,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		return madkitKernel.broadcastMessageWithRoleAndWaitForReplies(requester, community, group, role, message, senderRole, timeOutMilliSeconds);//TODO logging
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgentBucketWithRoles(madkit.kernel.AbstractAgent, java.lang.String, int, java.util.Collection)
	 */
	@Override
	List<AbstractAgent> launchAgentBucketWithRoles(AbstractAgent requester, String agentClassName, int bucketSize, Collection<String> CGRLocations) {
		logMessage(requester, "launchAgentBucketWithRoles  <" + agentClassName + "," + bucketSize + "," + CGRLocations + ">");
		final List<AbstractAgent> l = madkitKernel.launchAgentBucketWithRoles(requester, agentClassName, bucketSize, CGRLocations);
		logMessage(requester, "launchAgentBucketWithRoles  done !");
		return l;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, java.lang.String, int, boolean)
	 */
	@Override
	AbstractAgent launchAgent(AbstractAgent requester, String agentClass, int timeOutSeconds, boolean defaultGUI) {
		logMessage(requester, getI18N("launchA") + agentClass);
		final AbstractAgent a = madkitKernel.launchAgent(requester, agentClass, timeOutSeconds, defaultGUI);
		if (a == null) {
			logMessage(requester, getI18N("launchA") + agentClass + " time out or crash");
			return null;
		}
		logMessage(requester, getI18N("launchA") + agentClass + " OK");
		return a;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, madkit.kernel.AbstractAgent, int, boolean)
	 */
	@Override
	ReturnCode launchAgent(AbstractAgent requester, AbstractAgent agent, int timeOutSeconds, boolean defaultGUI) {
		logMessage(requester, getI18N("launchA") + agent);
		switch (madkitKernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI)) {
		case INVALID_ARG:
			requester.handleException(new LaunchAgentWarning(INVALID_ARG, " launch failed "+INVALID_ARG));
			logMessage(requester, getI18N("launchA") + agent + " not done : null arg");
			return INVALID_ARG;
		case SUCCESS:
			logMessage(requester, getI18N("launchA") + agent.getName() + " Done");
			return SUCCESS;
		case LAUNCH_TIME_OUT:
			logMessage(requester, getI18N("launchA") + agent.getName() + " Time out !");
			return LAUNCH_TIME_OUT;
		case AGENT_CRASH:
			logMessage(requester, getI18N("launchA") + agent.getName() + " not done : agent crashed");
			return requester.handleException(new LaunchAgentWarning(AGENT_CRASH, " not done : agent crashed"));
		case ALREADY_LAUNCHED:
			logMessage(requester, getI18N("launchA") + agent.getName() + " not done : already launched");
			return requester.handleException(new LaunchAgentWarning(ALREADY_LAUNCHED, " not done : agent crashed"));
		default:
			return requester.handleException(new LaunchAgentWarning(SEVERE, "result not handled"));
		}
	}

	/**
	 * @see madkit.kernel.RootKernel#isCommunity(madkit.kernel.AbstractAgent, java.lang.String)
	 */
	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		if(madkitKernel.isCommunity(requester, community)){
			logMessage(requester, "isCommunity ? "+printCGR(community)+" YES");
			return true;
		}
		logMessage(requester, "isCommunity ? "+printCGR(community)+" NO");
		return false;
	}

	/**
	 * @see madkit.kernel.RootKernel#isGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */
	@Override
	boolean isGroup(AbstractAgent requester, String community, String group) {
		if(madkitKernel.isGroup(requester, community, group)){
			logMessage(requester, "isGroup ? "+printCGR(community, group)+" YES");
			return true;
		}
		logMessage(requester, "isGroup ? "+printCGR(community, group)+" NO");
		return false;
	}

	/**
	 * @see madkit.kernel.RootKernel#isRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		if(madkitKernel.isRole(requester, community, group, role)){
			logMessage(requester, "isRole ? "+printCGR(community, group, role)+" YES");
			return true;
		}
		logMessage(requester, "isRole ? "+printCGR(community, group, role)+" NO");
		return false;
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds) {
		logMessage(requester, "Killing " + target.getName() + " and waiting its termination for " + timeOutSeconds + " s...");
		switch (madkitKernel.killAgent(requester, target, timeOutSeconds)) {
		case SUCCESS:
			logMessage(requester, "... Done: [" + target.getName() + "] succesfully killed !");
			return SUCCESS;
		case ALREADY_KILLED:
			return requester.handleException(new killedAgentWarning(ALREADY_KILLED, "kill failed on [" + target.getName() + "]: Has been already killed !"));
		case NOT_YET_LAUNCHED:
			return requester.handleException(new killedAgentWarning(NOT_YET_LAUNCHED, "kill failed on [" + target.getName() + "]: Has not been launched yet !"));
		case LAUNCH_TIME_OUT:
			return requester.handleException(new killedAgentWarning(LAUNCH_TIME_OUT, "kill time out on [" + target.getName() + "] !"));
		default:
			return requester.handleException(new killedAgentWarning(SEVERE, "result not handled"));
		}
	}

	@Override
	ReturnCode reloadClass(AbstractAgent requester, String agentClass) {
		logMessage(requester, getI18N("reload") + agentClass);
		switch(madkitKernel.reloadClass(requester, agentClass)){
		case SUCCESS:
			return SUCCESS;
		case CLASS_NOT_FOUND://TODO exception for this
			return requester.handleException(new killedAgentWarning(CLASS_NOT_FOUND, " Class not found "+agentClass));			
		default:
			return requester.handleException(new killedAgentWarning(SEVERE, "result not handled"));
		}
	}

	@Override
	String getMadkitProperty(AbstractAgent abstractAgent, String key) {
		return madkitKernel.getMadkitProperty(abstractAgent, key);
	}

	@Override
	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		madkitKernel.setMadkitProperty(requester, key, value);// TODO update agent logging on or off
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#removeOverlooker(madkit.kernel.AbstractAgent, madkit.kernel.Overlooker)
	 */
	@Override
	boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		boolean added = madkitKernel.removeOverlooker(requester, o);
		logMessage(requester, (o instanceof Activator<?> ?"Activator":"Probe")+"added:"+o);
		return added;
	}

	/**
	 * @see madkit.kernel.RootKernel#addOverlooker(madkit.kernel.AbstractAgent, madkit.kernel.Overlooker)
	 */
	@Override
	boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		// TODO Auto-generated method stub
		return madkitKernel.addOverlooker(requester, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#getKernelAddress(madkit.kernel.AbstractAgent)
	 */
	@Override
	KernelAddress getKernelAddress(AbstractAgent requester) {
		// TODO Auto-generated method stub
		return madkitKernel.getKernelAddress(requester);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#importDistantOrg(madkit.kernel.NetworkAgent, java.util.HashMap)
	 */
	@Override
	void importDistantOrg(NetworkAgent networkAgent, HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> org) {
		madkitKernel.importDistantOrg(networkAgent, org);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#logCurrentOrganization(madkit.kernel.AbstractAgent, java.util.logging.Level)
	 */
	@Override
	void logCurrentOrganization(AbstractAgent requester, Level finer) {
		madkitKernel.logCurrentOrganization(requester, finer);
	}

	/**
	 * @see madkit.kernel.RootKernel#getLocalOrg(madkit.kernel.NetworkAgent)
	 */
	@Override
	HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> getLocalOrg(NetworkAgent networkAgent) {
		return madkitKernel.getLocalOrg(networkAgent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#injectOperation(madkit.kernel.NetworkAgent, int, madkit.kernel.AgentAddress)
	 */
	@Override
	void injectOperation(NetworkAgent networkAgent, int operation, AgentAddress content) {
		// TODO Auto-generated method stub
		madkitKernel.injectOperation(networkAgent, operation, content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#injectMessage(madkit.kernel.NetworkAgent, madkit.kernel.Message)
	 */
	@Override
	void injectMessage(NetworkAgent networkAgent, Message content) {
		if(networkAgent.getLogger() != null)
			networkAgent.getLogger().finest("Injecting message = "+content);
		madkitKernel.injectMessage(networkAgent,content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#removeAgentsFromDistantKernel(madkit.kernel.NetworkAgent, madkit.kernel.KernelAddress)
	 */
	@Override
	void removeAgentsFromDistantKernel(NetworkAgent networkAgent, KernelAddress kernelAddress) {
		// TODO Auto-generated method stub
		madkitKernel.removeAgentsFromDistantKernel(networkAgent, kernelAddress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#disposeGUIOf(madkit.kernel.AbstractAgent)
	 */
	@Override
	void disposeGUIOf(AbstractAgent abstractAgent) {
		// TODO Auto-generated method stub
		madkitKernel.disposeGUIOf(abstractAgent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.RootKernel#removeAgentFromOrganizations(madkit.kernel.AbstractAgent)
	 */
	@Override
	void removeAgentFromOrganizations(AbstractAgent theAgent) {
		// TODO Auto-generated method stub
		madkitKernel.removeAgentFromOrganizations(theAgent);
	}

	@Override
	void removeThreadedAgent(Agent a) {
		madkitKernel.removeThreadedAgent(a);
	}

	boolean logMessage(final AbstractAgent requester, final String m) {
		// just in case the user sets logger to null manually :( but protected is so convenient...
		final Logger logger = requester.getLogger();
		if (logger == null)
			return false;
		else
			logger.finest(m);
		return true;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.RootKernel#kernelLog(java.lang.String)
	 */
	@Override
	final void kernelLog(String message, Level logLvl, Throwable e) {
		madkitKernel.kernelLog(message,logLvl,e);
	}

}
