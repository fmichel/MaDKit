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

import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIME_OUT;
import static madkit.i18n.I18nMadkitClass.*;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import madkit.i18n.Words;

/**
 * @author Fabien Michel
 * @version 0.92
 * @since MadKit 5.0.0.7
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
		setKernel(k);
	}

	/**
	 * @see madkit.kernel.MadkitKernel#createGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, madkit.kernel.GroupIdentifier, boolean)
	 */
	@Override
	ReturnCode createGroup(AbstractAgent requester, String community, String group, String description, GroupIdentifier theIdentifier, boolean isDistributed) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"createGroup" + getCGRString(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		ReturnCode r = kernel.createGroup(requester, community, group, group, theIdentifier, isDistributed);
		return r == SUCCESS ? SUCCESS : requester.handleException(Influence.CREATE_GROUP, new OrganizationWarning(r, community, group, null));
	}
	
	@Override
	boolean createGroupIfAbsent(AbstractAgent requester,String community, String group, String desc, GroupIdentifier theIdentifier, boolean isDistributed) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"createGroupIfAbsent" + getCGRString(community, group) + "distribution " + (isDistributed ? "ON" : "OFF") + " with "
				+ (theIdentifier == null ? "no access control" : theIdentifier.toString() + " for access control"));
		return kernel.createGroup(requester, community, group, desc,theIdentifier, isDistributed) == SUCCESS;
	}
	

	/**
	 * @see madkit.kernel.MadkitKernel#requestRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"requestRole" + getCGRString(community, group, role) + "using " + memberCard + " as passKey");
		ReturnCode r = kernel.requestRole(requester, community, group, role, memberCard);
		return r == SUCCESS ? SUCCESS : requester.handleException(Influence.REQUEST_ROLE, new OrganizationWarning(r, community, group, role));
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveGroup(AbstractAgent requester, String community, String group) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"leaveGroup" + getCGRString(community, group));
		ReturnCode r = kernel.leaveGroup(requester, community, group);
		return r == SUCCESS ? SUCCESS : requester.handleException(Influence.LEAVE_GROUP, new OrganizationWarning(r, community, group, null));
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveRole(AbstractAgent requester, String community, String group, String role) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"leaveRole" + getCGRString(community, group, role));
		ReturnCode r = kernel.leaveRole(requester, community, group, role);
		return r == SUCCESS ? SUCCESS : requester.handleException(Influence.LEAVE_ROLE, new OrganizationWarning(r, community, group, role));
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentsWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role, boolean callerIncluded) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"getAgentsWithRole" + getCGRString(community, group, role));
		try {
			if (callerIncluded) {
				return kernel.getRole(community, group, role).getAgentAddressesCopy();
			}
			else{
				return kernel.getOtherRolePlayers(requester, community, group, role);
			}
		} catch (CGRNotAvailable e) {
			requester.handleException(Influence.GET_AGENTS_WITH_ROLE, new OrganizationWarning(e.getCode(), community, group, role));
			return null;
		}
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"getAgentWithRole" + getCGRString(community, group, role));
		try {
			return kernel.getAnotherRolePlayer(requester, community, group, role);
		} catch (CGRNotAvailable e) {
			requester.handleException(Influence.GET_AGENT_WITH_ROLE, new OrganizationWarning(e.getCode(), community, group, role));
			return null;
		}
	}

	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"broadcastMessage to <" + community + "," + group + "," + role + ">");
		ReturnCode r = kernel.broadcastMessageWithRole(requester, community, group, role, messageToSend, senderRole);
		if(r == SUCCESS)
			return SUCCESS;
		if(r == NO_RECIPIENT_FOUND)
			return requester.handleException(Influence.BROADCAST_MESSAGE, new MadkitWarning(r));
		if(r == ROLE_NOT_HANDLED)
			return requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, community, group, senderRole));
		return requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, community, group, role));
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"sendMessage "+messageToSend+" to " + receiver);
		ReturnCode r = kernel.sendMessage(requester, receiver, messageToSend, senderRole);
		if(r == SUCCESS)
			return SUCCESS;
		if(r == NOT_IN_GROUP || r == ROLE_NOT_HANDLED)
			return requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, receiver.getCommunity(), receiver.getGroup(), senderRole));
		return requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"sendMessage "+messageToSend+" to" + getCGRString(community,group,role));
		ReturnCode r = kernel.sendMessage(requester, community, group, role, messageToSend, senderRole);
		if(r == SUCCESS)
			return SUCCESS;
		if(r == NO_RECIPIENT_FOUND)
			return requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
		if(r == ROLE_NOT_HANDLED)
			return requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, community, group, senderRole));
		return requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, community, group, role));
	}

	@Override
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester,//TODO log
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"broadcastMessageWithRoleAndWaitForReplies");
		return kernel.broadcastMessageWithRoleAndWaitForReplies(requester, community, group, role, message, senderRole, timeOutMilliSeconds);//TODO logging
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgentBucketWithRoles(madkit.kernel.AbstractAgent, java.lang.String, int, java.util.Collection)
	 */
	@Override
	List<AbstractAgent> launchAgentBucketWithRoles(AbstractAgent requester, String agentClassName, int bucketSize, Collection<String> CGRLocations) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles  <" + agentClassName + "," + bucketSize + "," + CGRLocations + ">");
		final List<AbstractAgent> l = kernel.launchAgentBucketWithRoles(requester, agentClassName, bucketSize, CGRLocations);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles  done !");
		return l;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, madkit.kernel.AbstractAgent, int, boolean)
	 */
	@Override
	ReturnCode launchAgent(AbstractAgent requester, AbstractAgent agent, int timeOutSeconds, boolean defaultGUI) {
		ReturnCode r = kernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI);
		if(r == SUCCESS || r == TIME_OUT){
			if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Influence.LAUNCH_AGENT +" "+ agent+" "+r);
			return r;
		}
		return requester.handleException(Influence.LAUNCH_AGENT, new MadkitWarning(agent.toString(),r));
	}

	@Override
		final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds) {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Influence.KILL_AGENT +" "+ target.getName());
			ReturnCode r = kernel.killAgent(requester, target, timeOutSeconds);
			if(r == SUCCESS || r == TIME_OUT){
				if(requester.isFinestLogOn())
					requester.logger.log(Level.FINEST, Influence.KILL_AGENT + " " + target.getName() + r);
				return r;
			}
			return requester.handleException(Influence.KILL_AGENT, new MadkitWarning(target.toString(),r));
		}

	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		if(kernel.isCommunity(requester, community)){
			if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isCommunity ? "+getCGRString(community)+" YES");
			return true;
		}
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isCommunity ? "+getCGRString(community)+" NO");
		return false;
	}

	@Override
	boolean isGroup(AbstractAgent requester, String community, String group) {
		if(kernel.isGroup(requester, community, group)){
			if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isGroup ? "+getCGRString(community, group)+" YES");
			return true;
		}
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isGroup ? "+getCGRString(community, group)+" NO");
		return false;
	}

	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		if(kernel.isRole(requester, community, group, role)){
			if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isRole ? "+getCGRString(community, group, role)+" YES");
			return true;
		}
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,"isRole ? "+getCGRString(community, group, role)+" NO");
		return false;
	}
	
	@Override
	MadkitKernel getMadkitKernel() {
		return kernel;
	}
	
	@Override //TODO think about this log
	ReturnCode reloadClass(AbstractAgent requester, String name) throws ClassNotFoundException {
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,Words.RELOAD.toString() + name);
		ReturnCode r = kernel.reloadClass(requester, name);
		if(r == SUCCESS)
			return SUCCESS;
		return requester.handleException(new MadkitWarning(Influence.RELOAD_CLASS+" "+name, r));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.FakeKernel#removeOverlooker(madkit.kernel.AbstractAgent, madkit.kernel.Overlooker)
	 */
	@Override
	boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		boolean added = kernel.removeOverlooker(requester, o);
		if(requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,(o instanceof Activator<?> ?"Activator":"Probe")+"added:"+o);
		return added;
	}

	@Override
	boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		return kernel.addOverlooker(requester, o);
	}

	final void logMessage(final AbstractAgent requester, final String m) {//TODO optimize according to level 
		if (requester.logger != null)
			requester.logger.log(Level.FINEST,m);
	}

	@Override
	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {//TODO log 
		return kernel.getNewestClassVersion(requester, className);
	}

}
