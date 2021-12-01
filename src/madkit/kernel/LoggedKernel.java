/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * 
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
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

import madkit.i18n.Words;

/**
 * A kernel that logs every agent actions on the kernel
 * 
 * @author Fabien Michel
 * @version 1.1
 * @since MaDKit 5.0.0.7
 *
 */
final class LoggedKernel extends MadkitKernel {

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
		if (r == SUCCESS) {
			requester.logger.finest(() -> Influence.CREATE_GROUP.successString() + getCGRString(community, group) + "distribution "
					+ (isDistributed ? "ON" : "OFF") + " with "
					+ (gatekeeper == null ? "no access control " : gatekeeper.toString() + " as gatekeeper "));
		}
		else {
			requester.handleWarning(Influence.CREATE_GROUP, () -> new OrganizationWarning(r, community, group, null));
		}
		return r;
	}

	@Override
	boolean createGroupIfAbsent(AbstractAgent requester, String community, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		requester.logger.finest(() -> "createGroupIfAbsent " + getCGRString(community, group) + "distribution " + (isDistributed ? "ON" : "OFF")
						+ " with " + (gatekeeper == null ? "no access control" : gatekeeper.toString() + " for access control"));
		return kernel.createGroup(requester, community, group, gatekeeper, isDistributed) == SUCCESS;
	}

	@Override
	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		final ReturnCode r = kernel.requestRole(requester, community, group, role, memberCard);
		if (r == SUCCESS) {
			requester.logger.finest(() -> Influence.REQUEST_ROLE.successString() + getCGRString(community, group, role) + "using " + memberCard + " as passKey");
		}
		else {
			requester.handleWarning(Influence.REQUEST_ROLE, () -> new OrganizationWarning(r, community, group, role));
		}
		return r;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveGroup(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveGroup(AbstractAgent requester, String community, String group) {
		final ReturnCode r = kernel.leaveGroup(requester, community, group);
		if (r == SUCCESS) {
			requester.logger.finest(() -> Influence.LEAVE_GROUP.successString() + getCGRString(community, group));
		}
		else {
			requester.handleWarning(Influence.LEAVE_GROUP, () -> new OrganizationWarning(r, community, group, null));
		}
		return r;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#leaveRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	ReturnCode leaveRole(AbstractAgent requester, String community, String group, String role) {
		final ReturnCode r = kernel.leaveRole(requester, community, group, role);
		if (r == SUCCESS) {
			requester.logger.finest(() -> Influence.LEAVE_ROLE.successString() + getCGRString(community, group, role));
		}
		else {
			requester.handleWarning(Influence.LEAVE_ROLE, () -> new OrganizationWarning(r, community, group, role));
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
			else {
				result = kernel.getOtherRolePlayers(requester, community, group, role);
			}
			requester.logger.finest(() -> Influence.GET_AGENTS_WITH_ROLE + getCGRString(community, group, role) + ": " + result);
			return result;
		} catch (CGRNotAvailable e) {
			requester.handleWarning(Influence.GET_AGENTS_WITH_ROLE, () -> new OrganizationWarning(e.getCode(), community, group, role));
		}
		return null;
	}

	/**
	 * @see madkit.kernel.MadkitKernel#getAgentWithRole(madkit.kernel.AbstractAgent, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	AgentAddress getAgentWithRole(AbstractAgent requester, String community, String group, String role) {
		try {
			final AgentAddress result = kernel.getAnotherRolePlayer(requester, community, group, role);
			requester.logger.finest(() -> Influence.GET_AGENT_WITH_ROLE + getCGRString(community, group, role) + ": " + result);
			return result;
		} catch (CGRNotAvailable e) {
			requester.handleWarning(Influence.GET_AGENT_WITH_ROLE, () -> new OrganizationWarning(e.getCode(), community, group, role));
		}
		return null;
	}

	@Override
	AgentAddress getAgentAddressIn(AbstractAgent agent, String community, String group, String role) {
		final AgentAddress aa = kernel.getAgentAddressIn(agent, community, group, role);
		if (aa == null && isRole(community, group, role)) {
			agent.handleWarning(Influence.GET_AGENT_ADDRESS_IN, () -> new OrganizationWarning(ReturnCode.ROLE_NOT_HANDLED, community, group, role));
		}
		return aa;
	}

	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		ReturnCode r = kernel.broadcastMessageWithRole(requester, community, group, role, messageToSend, senderRole);
		switch (r) {
		case SUCCESS:
			requester.logger.finest(() -> Influence.BROADCAST_MESSAGE + "-> " + getCGRString(community, group, role)
			+ (senderRole == null ? "" : " with role " + senderRole) + messageToSend);
			return SUCCESS;
		case NO_RECIPIENT_FOUND:
			requester.handleWarning(Influence.BROADCAST_MESSAGE, () -> new MadkitWarning(r));
			break;
		case ROLE_NOT_HANDLED:
			requester.handleWarning(Influence.BROADCAST_MESSAGE, () -> new OrganizationWarning(r, community, group, senderRole));
			break;
		default:
			requester.handleWarning(Influence.BROADCAST_MESSAGE, () -> new OrganizationWarning(r, community, group, role));
			break;
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		final ReturnCode r = kernel.sendMessage(requester, receiver, messageToSend, senderRole);
		if (r == SUCCESS) {
			requester.logger.finest(() -> Influence.SEND_MESSAGE.successString() + " " + messageToSend);
			return SUCCESS;
		}
		if (r == NOT_IN_GROUP || r == ROLE_NOT_HANDLED) {
			requester.handleWarning(Influence.SEND_MESSAGE, () -> new OrganizationWarning(r, receiver.getCommunity(), receiver.getGroup(), senderRole));
		}
		else {
			requester.handleWarning(Influence.SEND_MESSAGE, () -> new MadkitWarning(r));
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, String community, String group, String role, Message messageToSend, String senderRole) {
		ReturnCode r = kernel.sendMessage(requester, community, group, role, messageToSend, senderRole);
		if (r == SUCCESS) {
			requester.logger
					.finest(() -> (messageToSend.getReceiver().isFrom(requester.getKernelAddress()) ? Influence.SEND_MESSAGE.successString()
							: Influence.SEND_MESSAGE.toString()) + "->" + getCGRString(community, group, role) + " " + messageToSend);
			return SUCCESS;
		}

		if (r == NO_RECIPIENT_FOUND) {
			requester.handleWarning(Influence.SEND_MESSAGE, () -> new MadkitWarning(r));
		}
		else
			if (r == ROLE_NOT_HANDLED) {
				requester.handleWarning(Influence.SEND_MESSAGE, () -> new OrganizationWarning(r, community, group, senderRole));
			}
			else {
				requester.handleWarning(Influence.SEND_MESSAGE, () -> new OrganizationWarning(r, community, group, role));
			}
		return r;
	}

	@Override
	// the log is done in the kernel to not deal with the catch or specify requirement in the not logged method
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester, String community, String group, String role,
			Message message, String senderRole, Integer timeOutMilliSeconds) {
		final List<Message> result = kernel.broadcastMessageWithRoleAndWaitForReplies(requester, community, group, role, message, senderRole,
				timeOutMilliSeconds);
		requester.logger.finest(() -> Influence.BROADCAST_MESSAGE_AND_WAIT + ": received: " + result);
		return result;
	}

	@Override
	void launchAgentBucketWithRoles(AbstractAgent requester, List<AbstractAgent> bucket, int cpuCoreNb, String... CGRLocations) {
		requester.logger.finest(() -> "launchAgentBucketWithRoles : " + bucket.size() + " "
				+ (!bucket.isEmpty() ? bucket.get(0).getClass().getName() : "agents !!!") + " "
				+ (CGRLocations.length > 0 ? Arrays.deepToString(CGRLocations) : ""));
		kernel.launchAgentBucketWithRoles(requester, bucket, cpuCoreNb, CGRLocations);
		requester.logger.finest(() -> "launchAgentBucketWithRoles Done !");
	}

	/**
	 * @see madkit.kernel.MadkitKernel#launchAgent(madkit.kernel.AbstractAgent, madkit.kernel.AbstractAgent, int, boolean)
	 */
	@Override
	final ReturnCode launchAgent(AbstractAgent requester, AbstractAgent agent, int timeOutSeconds, boolean defaultGUI) {
		requester.getLogger().finest(() -> Influence.LAUNCH_AGENT + " (" + timeOutSeconds + ")" + agent.getName() + "...");
		final ReturnCode r = kernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI);
		if (r == SUCCESS || r == TIMEOUT) {
			requester.logger.finest(() -> Influence.LAUNCH_AGENT.toString() + agent + " " + r);
		}
		else {
			requester.handleWarning(Influence.LAUNCH_AGENT, () -> new MadkitWarning(agent.toString(), r));
		}
		return r;
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds) {
		requester.logger.finest(() -> Influence.KILL_AGENT + " (" + timeOutSeconds + ")" + target + "...");
		final ReturnCode r = kernel.killAgent(requester, target, timeOutSeconds);
		if (r == SUCCESS || r == TIMEOUT) {
			requester.logger.finest(() -> Influence.KILL_AGENT + target.getName() + " " + r);
		}
		else {
			requester.handleWarning(Influence.KILL_AGENT, () -> new MadkitWarning(target.toString(), r));
		}
		return r;
	}

	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		final boolean fact = kernel.isCommunity(requester, community);
		requester.logger.finest(() -> Words.COMMUNITY + " ? " + getCGRString(community) + fact);
		return fact;
	}

	@Override
	boolean isGroup(AbstractAgent requester, String community, String group) {
		final boolean fact = kernel.isGroup(requester, community, group);
		requester.logger.finest(() -> Words.GROUP + " ? " + getCGRString(community, group) + fact);
		return fact;
	}

	@Override
	boolean isRole(AbstractAgent requester, String community, String group, String role) {
		final boolean fact = kernel.isRole(requester, community, group, role);
		requester.logger.finest(() -> Words.ROLE + " ? " + getCGRString(community, group, role) + fact);
		return fact;
	}

	@Override
	MadkitKernel getMadkitKernel() {
		return kernel;
	}

	@Override
	synchronized boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.removeOverlooker(requester, o);
		requester.logger.finest(() -> o.getClass().getSimpleName() + (added ? " removed" : " not added") + o);
		return added;
	}

	@Override
	synchronized boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.addOverlooker(requester, o);
		requester.logger.finest(() -> o.getClass().getSimpleName() + (added ? " OK" : " already added") + o);
		return added;
	}

}
