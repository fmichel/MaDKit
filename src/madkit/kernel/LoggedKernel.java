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

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.CLASS_NOT_FOUND;
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
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_MSG;
import static madkit.kernel.AbstractAgent.ReturnCode.NULL_STRING;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SEVERE;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Utils.getI18N;
import static madkit.kernel.Utils.printCGR;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Michel
 * @version 0.91
 * @since MadKit 5.0.0.7
 *
 */
final class LoggedKernel extends MadkitKernel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7327012859170685445L;

	LoggedKernel(MadkitKernel k) {
		super(k);
		loggedKernel = this;
		setKernel(k);
	}

	/**
	 * @see madkit.kernel.MadkitKernel#createGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, madkit.kernel.GroupIdentifier, boolean)
	 */
	@Override
	ReturnCode createGroup(AbstractAgent requester, String community, String group, String description, GroupIdentifier theIdentifier, boolean isDistributed) {
		logMessage(requester, "createGroup" + printCGR(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		switch (kernel.createGroup(requester, community, group, group, theIdentifier, isDistributed)) {
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
	boolean createGroupIfAbsent(AbstractAgent requester,String community, String group, String desc, GroupIdentifier theIdentifier, boolean isDistributed) {
		logMessage(requester, "createGroupIfAbsent" + printCGR(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		return kernel.createGroup(requester, community, group, desc,theIdentifier, isDistributed) == SUCCESS;
	}
	

	/**
	 * @see madkit.kernel.MadkitKernel#requestRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		logMessage(requester, "requestRole" + printCGR(community, group, role) + "using " + memberCard + " as passKey");
		switch (kernel.requestRole(requester, community, group, role, memberCard)) {
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
		switch (kernel.leaveGroup(requester, community, group)) {
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
		switch (kernel.leaveRole(requester, community, group, role)) {
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
			return requester.handleException(new RequestRoleWarning(SEVERE, "result not handled "+kernel.leaveRole(requester, community, group, role)));
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role) {
		logMessage(requester, "getAgentsWithRole" + printCGR(community, group, role));
		try {
			return kernel.getOtherRolePlayers(requester, community, group, role);
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
			return kernel.getAnotherRolePlayer(requester, community, group, role);
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

	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		logMessage(requester, "broadcastMessage to <" + community + "," + group + "," + role + ">");
		switch (kernel.broadcastMessageWithRole(requester, community, group, role, messageToSend, senderRole)) {
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

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		logMessage(requester, "sendMessage to " + receiver);
		switch (kernel.sendMessage(requester, receiver, messageToSend, senderRole)) {
		case SUCCESS:
			return SUCCESS;
		case INVALID_ARG:
			return requester.handleException(new sendMessageWarning(INVALID_ARG, messageToSend == null ? " Cannot send a null message" : "receiver is null"));
		case ROLE_NOT_HANDLED:
			return requester.handleException(new sendMessageWarning(ROLE_NOT_HANDLED, printCGR(receiver.getCommunity(), receiver.getGroup(), senderRole)));
		case NOT_IN_GROUP:
			return requester.handleException(new sendMessageWarning(NOT_IN_GROUP, printCGR(receiver.getCommunity(), receiver.getGroup())));
		case INVALID_AA:
			if (receiver.isLocal()) {
				return requester.handleException(new sendMessageWarning(
						INVALID_AA));//, printCGR(receiver.getCommunity(), receiver.getGroup(), receiver.getRole())));
			}
		case NETWORK_DOWN:
			return requester.handleException(new sendMessageWarning(NETWORK_DOWN));//, printCGR(receiver.getCommunity(), receiver.getGroup(), receiver.getRole())));
		default:
			return requester.handleException(new sendMessageWarning(SEVERE, "result not handled"));
		}
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		logMessage(requester, "sendMessage to " + printCGR(community,group,role));
		switch (kernel.sendMessage(requester, community, group, role, messageToSend, senderRole)) {
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

	@Override
	ReturnCode sendReplyWithRole(AbstractAgent requester, Message messageToReplyTo, Message reply, String senderRole) {
		logMessage(requester, "sending " + reply + " as reply to " + messageToReplyTo);
		// TODO Auto-generated method stub
		switch (kernel.sendReplyWithRole(requester, messageToReplyTo, reply, senderRole)) {
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
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		logMessage(requester, "broadcastMessageWithRoleAndWaitForReplies");
		return kernel.broadcastMessageWithRoleAndWaitForReplies(requester, community, group, role, message, senderRole, timeOutMilliSeconds);//TODO logging
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgentBucketWithRoles(madkit.kernel.AbstractAgent, java.lang.String, int, java.util.Collection)
	 */
	@Override
	List<AbstractAgent> launchAgentBucketWithRoles(AbstractAgent requester, String agentClassName, int bucketSize, Collection<String> CGRLocations) {
		logMessage(requester, "launchAgentBucketWithRoles  <" + agentClassName + "," + bucketSize + "," + CGRLocations + ">");
		final List<AbstractAgent> l = kernel.launchAgentBucketWithRoles(requester, agentClassName, bucketSize, CGRLocations);
		logMessage(requester, "launchAgentBucketWithRoles  done !");
		return l;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, java.lang.String, int, boolean)
	 */
	@Override
	AbstractAgent launchAgent(AbstractAgent requester, String agentClass, int timeOutSeconds, boolean defaultGUI) {
		logMessage(requester, getI18N("launchA") + agentClass);
		final AbstractAgent a = kernel.launchAgent(requester, agentClass, timeOutSeconds, defaultGUI);
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
		switch (kernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI)) {
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

	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		if(kernel.isCommunity(requester, community)){
			logMessage(requester, "isCommunity ? "+printCGR(community)+" YES");
			return true;
		}
		logMessage(requester, "isCommunity ? "+printCGR(community)+" NO");
		return false;
	}

	@Override
	boolean isGroup(AbstractAgent requester, String community, String group) {
		if(kernel.isGroup(requester, community, group)){
			logMessage(requester, "isGroup ? "+printCGR(community, group)+" YES");
			return true;
		}
		logMessage(requester, "isGroup ? "+printCGR(community, group)+" NO");
		return false;
	}

	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		if(kernel.isRole(requester, community, group, role)){
			logMessage(requester, "isRole ? "+printCGR(community, group, role)+" YES");
			return true;
		}
		logMessage(requester, "isRole ? "+printCGR(community, group, role)+" NO");
		return false;
	}
	
	@Override
	MadkitKernel getMadkitKernel() {
		return kernel;
	}
	
	@Override
	final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds) {
//		kernelLog("Killing " + target.getName() + " and waiting its termination for " + timeOutSeconds + " s...", Level.FINER, null);
		logMessage(requester, "Killing " + target.getName() + " and waiting its termination for " + timeOutSeconds + " s...");
		switch (kernel.killAgent(requester, target, timeOutSeconds)) {
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

	@Override //TODO think about this log
	ReturnCode reloadClass(AbstractAgent requester, String name) throws ClassNotFoundException {
		logMessage(requester, getI18N("reload") + name);
		if(kernel.reloadClass(requester, name) == SUCCESS)
			return SUCCESS;
		return requester.handleException(new killedAgentWarning(CLASS_NOT_FOUND, "Cannot find a class file for reloading <"+name+">"));
//		switch(kernel.reloadClass(requester, name)){//TODO if else if ok
//		case SUCCESS:
//			return SUCCESS;
//		case CLASS_NOT_FOUND://TODO exception for this
//			return requester.handleException(new killedAgentWarning(CLASS_NOT_FOUND, "Cannot reload "+name));			
//		default:
//			return requester.handleException(new killedAgentWarning(SEVERE, "result not handled"));
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.FakeKernel#removeOverlooker(madkit.kernel.AbstractAgent, madkit.kernel.Overlooker)
	 */
	@Override
	boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		boolean added = kernel.removeOverlooker(requester, o);
		logMessage(requester, (o instanceof Activator<?> ?"Activator":"Probe")+"added:"+o);
		return added;
	}

	@Override
	boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		return kernel.addOverlooker(requester, o);
	}

	final void logMessage(final AbstractAgent requester, final String m) {
		if (requester.logger != null)
			requester.logger.finest(m);
	}

	@Override
	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {//TODO log 
		return kernel.getNewestClassVersion(requester, className);
	}

}
