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

import static madkit.i18n.I18nUtilities.getCGRString;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import madkit.i18n.Words;

/**
 * @author Fabien Michel
 * @version 0.92
 * @since MaDKit 5.0.0.7
 *
 */
final class LoggedKernel extends MadkitKernel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8002356811073647875L;

	LoggedKernel(MadkitKernel k) {
		super(k);
		loggedKernel = this;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#createGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, madkit.kernel.Gatekeeper, boolean)
	 */
	@Override
	ReturnCode createGroup(AbstractAgent requester, String community, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		final ReturnCode r = kernel.createGroup(requester, community, group, gatekeeper, isDistributed);
		if(r == SUCCESS){
			if(requester.isFinestLogOn()){
				requester.logger.log(Level.FINEST,
						Influence.CREATE_GROUP.successString()+ getCGRString(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
						+ (gatekeeper == null ? "no access control " : gatekeeper.toString() + " as gatekeeper "));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {//do not factorize : optimizing strings and exception creation
			requester.handleException(Influence.CREATE_GROUP, new OrganizationWarning(r, community, group, null));
		}
		return r;
	}
	

	@Override
	boolean createGroupIfAbsent(AbstractAgent requester,String community, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"createGroupIfAbsent" + getCGRString(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
					+ (gatekeeper == null ? "no access control" : gatekeeper.toString() + " for access control"));
		return kernel.createGroup(requester, community, group, gatekeeper,isDistributed) == SUCCESS;
	}


	/**
	 * @see madkit.kernel.MadkitKernel#requestRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		final ReturnCode r = kernel.requestRole(requester, community, group, role, memberCard);
		if(r == SUCCESS){
			if(requester.isFinestLogOn()){
				requester.logger.log(Level.FINEST,Influence.REQUEST_ROLE.successString() + getCGRString(community, group, role) + "using " + memberCard + " as passKey");
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.REQUEST_ROLE, new OrganizationWarning(r, community, group, role));
		}
		return r;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveGroup(AbstractAgent requester, String community, String group) {
		final ReturnCode r = kernel.leaveGroup(requester, community, group);
		if(r == SUCCESS){
			if(requester.isFinestLogOn()){
				requester.logger.log(Level.FINEST,"leaveGroup" + getCGRString(community, group));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.LEAVE_GROUP, new OrganizationWarning(r, community, group, null));
		}
		return r;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveRole(AbstractAgent requester, String community, String group, String role) {
		ReturnCode r = kernel.leaveRole(requester, community, group, role);
		if(r == SUCCESS){
			if(requester.isFinestLogOn()){
				requester.logger.log(Level.FINEST,"leaveRole" + getCGRString(community, group, role));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.LEAVE_ROLE, new OrganizationWarning(r, community, group, role));
		}
		return r;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role, boolean callerIncluded) {
		try {
			final List<AgentAddress> result;
			if (callerIncluded) {
				result = kernel.getRole(community, group, role).getAgentAddressesCopy();
			}
			else{
				result = kernel.getOtherRolePlayers(requester, community, group, role);
			}
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.GET_AGENTS_WITH_ROLE + getCGRString(community, group, role)+": "+result);
			return result;
		} catch (CGRNotAvailable e) {
			if (requester.isWarningOn()) {
				requester.handleException(Influence.GET_AGENTS_WITH_ROLE,
						new OrganizationWarning(e.getCode(), community, group, role));
			}
			return null;
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role) {
		try {
			final AgentAddress result = kernel.getAnotherRolePlayer(requester, community, group, role);
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.GET_AGENT_WITH_ROLE + getCGRString(community, group, role)+": "+result);
			return result;
		} catch (CGRNotAvailable e) {
			if (requester.isWarningOn()) {
				requester.handleException(Influence.GET_AGENT_WITH_ROLE, new OrganizationWarning(e.getCode(), community, group, role));
			}
			return null;
		}
	}
	
	@Override
	AgentAddress getAgentAddressIn(AbstractAgent agent, String community, String group, String role) {
		final AgentAddress aa = kernel.getAgentAddressIn(agent, community, group, role);
		if(aa == null && agent.isWarningOn() && isRole(community, group, role)){
			agent.handleException(Influence.GET_AGENT_ADDRESS_IN, new OrganizationWarning(ReturnCode.ROLE_NOT_HANDLED, community, group, role));
		}
		return aa;
	}

	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		ReturnCode r = kernel.broadcastMessageWithRole(requester, community, group, role, messageToSend, senderRole);
		if(r == SUCCESS){
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.BROADCAST_MESSAGE + "-> "
						+ getCGRString(community, group, role)+ (senderRole == null ? "" : " with role "+senderRole)
						+ messageToSend
						);
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if(r == NO_RECIPIENT_FOUND){
				requester.handleException(Influence.BROADCAST_MESSAGE, new MadkitWarning(r));
			}
			else if(r == ROLE_NOT_HANDLED){
				requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, community, group, senderRole));
			}
			else{
				requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, community, group, role));
			}
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		final ReturnCode r = kernel.sendMessage(requester, receiver, messageToSend, senderRole);
		if(r == SUCCESS){
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.SEND_MESSAGE.successString()+" "+messageToSend);
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if(r == NOT_IN_GROUP || r == ROLE_NOT_HANDLED){
				requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, receiver.getCommunity(), receiver.getGroup(), senderRole));
			}
			else{
				requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
			}
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		ReturnCode r = kernel.sendMessage(requester, community, group, role, messageToSend, senderRole);
		if(r == SUCCESS){
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.SEND_MESSAGE.successString() +"->" 
						+ getCGRString(community,group,role)+" "+messageToSend);
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if(r == NO_RECIPIENT_FOUND){
				requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
			}
			else if(r == ROLE_NOT_HANDLED){
				requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, community, group, senderRole));
			}
			else{
				requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, community, group, role));
			}
		}
		return r;
	}

	@Override
	//the log is done in the kernel to not deal with the catch or specify requirement in the not logged method
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		final List<Message> result = kernel.broadcastMessageWithRoleAndWaitForReplies(requester, community, group, role, message, senderRole, timeOutMilliSeconds);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Influence.BROADCAST_MESSAGE_AND_WAIT+": received: "+result);
		return result;
	}

//	/**
//	 * @see madkit.kernel.MadkitKernel#launchAgentBucketWithRoles(madkit.kernel.AbstractAgent, java.lang.String, int, java.util.Collection)
//	 */
//	@Override
//	List<AbstractAgent> launchAgentBucketWithRoles(AbstractAgent requester, String agentClassName, int bucketSize, String... CGRLocations) {
//		if(requester.isFinestLogOn())
//			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles  <" + agentClassName + "," + bucketSize + "," + CGRLocations + ">");
//		final List<AbstractAgent> l = kernel.launchAgentBucketWithRoles(requester, agentClassName, bucketSize, CGRLocations);
//		if(requester.isFinestLogOn())
//			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles  done !");
//		return l;
//	}
	
	@Override
	void launchAgentBucketWithRoles(AbstractAgent requester, List<AbstractAgent> bucket,
			String... CGRLocations) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles : "+bucket.size()+ " : "+Arrays.deepToString(CGRLocations));
		kernel.launchAgentBucketWithRoles(requester, bucket, CGRLocations);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles  done !");
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, madkit.kernel.AbstractAgent, int, boolean)
	 */
	@Override
	final ReturnCode launchAgent(AbstractAgent requester, AbstractAgent agent, int timeOutSeconds, boolean defaultGUI) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Influence.LAUNCH_AGENT+" ("+timeOutSeconds+")"+ agent.getLoggingName()+"...");
		final ReturnCode r = kernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI);
		if(r == SUCCESS || r == TIMEOUT){
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,Influence.LAUNCH_AGENT.toString()+ agent+" "+r);
		}
		else if (requester.isWarningOn()) {
			requester.handleException(Influence.LAUNCH_AGENT, new MadkitWarning(agent.toString(),r));
		}
		return r;
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Influence.KILL_AGENT+" ("+timeOutSeconds+")"+ target +"...");
		final ReturnCode r = kernel.killAgent(requester, target, timeOutSeconds);
		if(r == SUCCESS || r == TIMEOUT){
			if(requester.isFinestLogOn())
				requester.logger.log(Level.FINEST, Influence.KILL_AGENT+ target.getLoggingName()+" "+r);
		}
		else if (requester.isWarningOn()) {
				requester.handleException(Influence.KILL_AGENT, new MadkitWarning(target.toString(),r));
		}
		return r;
	}

	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		final boolean fact = kernel.isCommunity(requester, community);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Words.COMMUNITY+" ? "+getCGRString(community)+fact);
		return fact;
	}

	@Override
	boolean isGroup(AbstractAgent requester, String community, String group) {
		final boolean fact = kernel.isGroup(requester, community, group); 
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Words.GROUP+" ? "+getCGRString(community, group)+fact);
		return fact;
	}

	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		final boolean fact = kernel.isRole(requester, community, group, role); 
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Words.ROLE+" ? "+getCGRString(community, group, role)+fact);
		return fact;
	}

	@Override
	MadkitKernel getMadkitKernel() {
		return kernel;
	}

//	@Override //TODO think about this log
//	ReturnCode reloadClass(AbstractAgent requester, String name) throws ClassNotFoundException {
//		final ReturnCode r = kernel.reloadClass(requester, name);
//		if(requester.isFinestLogOn())
//			requester.logger.log(Level.FINEST,Words.RELOAD.toString() + name);
//		if(r == SUCCESS)
//			return SUCCESS;
//		else if(requester.isWarningOn()){
//				requester.handleException(Influence.RELOAD_CLASS, new MadkitWarning(name, r));
//		}
//		return r;
//	}

	@Override
	synchronized boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.removeOverlooker(requester, o);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,o.getClass().getSimpleName()+(added ? " removed":" not added")+o);
		return added;
	}

	@Override
	synchronized boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.addOverlooker(requester, o);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,o.getClass().getSimpleName()+(added ? " OK":" already added")+o);
		return added;
	}

}
