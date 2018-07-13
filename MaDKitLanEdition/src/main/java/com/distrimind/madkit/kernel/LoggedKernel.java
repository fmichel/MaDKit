/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import static com.distrimind.madkit.i18n.I18nUtilities.getCGRString;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.logging.Level;

import com.distrimind.jdkrewrite.concurrent.LockerCondition;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.madkit.kernel.ConversationID.InterfacedIDs;
import com.distrimind.madkit.kernel.network.AskForConnectionMessage;
import com.distrimind.madkit.kernel.network.AskForTransferMessage;
import com.distrimind.madkit.kernel.network.Connection;
import com.distrimind.madkit.kernel.network.ConnectionIdentifier;
import com.distrimind.madkit.kernel.network.LocalLanMessage;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.jdkrewrite.concurrent.ScheduledThreadPoolExecutor;
import com.distrimind.util.IDGeneratorInt;
import com.distrimind.util.crypto.MessageDigestType;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 1.0
 *
 */
final class LoggedKernel extends MadkitKernel {

	LoggedKernel(MadkitKernel k) {
		super(k);
		loggedKernel = this;
	}


	@Override
	ReturnCode createGroup(AbstractAgent requester, Group group, Object passKey, boolean manually_created) {
		final ReturnCode r = kernel.createGroup(requester, group, passKey, manually_created);
		if (r == SUCCESS) {
			if (requester.isFinestLogOn()) {
				requester.logger.log(Level.FINEST,
						Influence.CREATE_GROUP.successString() + getCGRString(group) + "distribution "
								+ (group.isDistributed() ? "ON" : "OFF") + " with "
								+ (group.getGateKeeper() == null ? "no access control "
										: group.getGateKeeper().toString() + " as gatekeeper, with " + passKey
												+ " as pass key"));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {// do not factorize : optimizing strings and exception creation
			requester.handleException(Influence.CREATE_GROUP, new OrganizationWarning(r, group, null));
		}
		return r;
	}

	@Override
	ReturnCode requestRole(AbstractAgent requester, Group group, String role, ExternalizableAndSizable memberCard,
			boolean manual_request) {
		final ReturnCode r = kernel.requestRole(requester, group, role, memberCard, manual_request);
		if (r == SUCCESS) {
			if (requester.isFinestLogOn()) {
				requester.logger.log(Level.FINEST, Influence.REQUEST_ROLE.successString() + getCGRString(group, role)
						+ "using " + memberCard + " as passKey");
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.REQUEST_ROLE, new OrganizationWarning(r, group, role));
		}
		return r;
	}


	@Override
	ReturnCode leaveGroup(AbstractAgent requester, Group group, boolean manually_requested) {
		final ReturnCode r = kernel.leaveGroup(requester, group, manually_requested);
		if (r == SUCCESS) {
			if (requester.isFinestLogOn()) {
				requester.logger.log(Level.FINEST, Influence.LEAVE_GROUP.successString() + getCGRString(group));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.LEAVE_GROUP, new OrganizationWarning(r, group, null));
		}
		return r;
	}


	@Override
	ReturnCode leaveRole(AbstractAgent requester, Group group, String role, boolean manual_request) {
		ReturnCode r = kernel.leaveRole(requester, group, role, manual_request);
		if (r == SUCCESS) {
			if (requester.isFinestLogOn()) {
				requester.logger.log(Level.FINEST, Influence.LEAVE_ROLE.successString() + getCGRString(group, role));
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			requester.handleException(Influence.LEAVE_ROLE, new OrganizationWarning(r, group, role));
		}
		return r;
	}


	@Override
	Set<AgentAddress> getAgentsWithRole(AbstractAgent requester, AbstractGroup group, String role,
			boolean callerIncluded) {
		try {
			Set<AgentAddress> res = kernel.getAgentsWithRole(requester, group, role, callerIncluded);
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,
						Influence.GET_AGENTS_WITH_ROLE + getCGRString(group, role) + ": " + res);
			return res;
		} catch (NullPointerException e) {
			if (requester.isWarningOn()) {
				requester.handleException(Influence.GET_AGENTS_WITH_ROLE, e);
			}
			throw e;
		}
	}


	@Override
	AgentAddress getAgentWithRole(AbstractAgent requester, AbstractGroup group, String role) {
		try {
			AgentAddress aa = kernel.getAgentWithRole(requester, group, role);
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST,
						Influence.GET_AGENT_WITH_ROLE + getCGRString(group, role) + ": " + aa);
			return aa;
		} catch (NullPointerException e) {
			if (requester.isWarningOn()) {
				requester.handleException(Influence.GET_AGENTS_WITH_ROLE, e);
			}
			throw e;
		}
	}

	@Override
	AgentAddress getAgentAddressIn(AbstractAgent agent, Group group, String role) {
		final AgentAddress aa = kernel.getAgentAddressIn(agent, group, role);
		if (aa == null && agent.isWarningOn() && isCreatedRole(group, role)) {
			agent.handleException(Influence.GET_AGENT_ADDRESS_IN,
					new OrganizationWarning(ReturnCode.ROLE_NOT_HANDLED, group, role));
		}
		return aa;
	}

	@Override
	ReturnCode broadcastMessageWithRole(AbstractAgent requester, AbstractGroup group, String role,
			Message messageToSend, String senderRole, boolean sendAllRepliesInOneBlock) {

		ReturnCode r = kernel.broadcastMessageWithRole(requester, group, role, messageToSend, senderRole,
				sendAllRepliesInOneBlock);
		if (r == SUCCESS) {
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST, Influence.BROADCAST_MESSAGE + "-> " + getCGRString(group, role)
						+ (senderRole == null ? "" : " with role " + senderRole) + messageToSend);
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if (r == NO_RECIPIENT_FOUND) {
				requester.handleException(Influence.BROADCAST_MESSAGE, new MadkitWarning(r));
			} else if (r == ROLE_NOT_HANDLED) {
				requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, group, senderRole));
			} else {
				requester.handleException(Influence.BROADCAST_MESSAGE, new OrganizationWarning(r, group, role));
			}
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, Message messageToSend, String senderRole) {
		final ReturnCode r = kernel.sendMessage(requester, receiver, messageToSend, senderRole);
		if (r == SUCCESS || r == ReturnCode.TRANSFER_IN_PROGRESS) {
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST, Influence.SEND_MESSAGE.successString() + " " + messageToSend);
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if (r == NOT_IN_GROUP || r == ROLE_NOT_HANDLED) {
				requester.handleException(Influence.SEND_MESSAGE,
						new OrganizationWarning(r, receiver.getGroup(), senderRole));
			} else {
				requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
			}
		}
		return r;
	}

	@Override
	ReturnCode sendMessage(AbstractAgent requester, AbstractGroup group, String role, Message messageToSend,
			String senderRole) {
		ReturnCode r = kernel.sendMessage(requester, group, role, messageToSend, senderRole);
		if (r == SUCCESS || r == ReturnCode.TRANSFER_IN_PROGRESS) {
			if (requester.isFinestLogOn()) {
				requester.logger.log(Level.FINEST,
						(messageToSend.getReceiver().isFrom(requester.getKernelAddress())
								? Influence.SEND_MESSAGE.successString()
								: Influence.SEND_MESSAGE.toString()) + "->" + getCGRString(group, role) + " "
								+ messageToSend);
			}
			return SUCCESS;
		}
		if (requester.isWarningOn()) {
			if (r == NO_RECIPIENT_FOUND) {
				requester.handleException(Influence.SEND_MESSAGE, new MadkitWarning(r));
			} else if (r == ROLE_NOT_HANDLED) {
				requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, group, senderRole));
			} else {
				requester.handleException(Influence.SEND_MESSAGE, new OrganizationWarning(r, group, role));
			}
		}
		return r;
	}

	@Override
	// the log is done in the kernel to not deal with the catch or specify
	// requirement in the not logged method
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester, AbstractGroup group, String role,
			Message message, String senderRole, Integer timeOutMilliSeconds) throws InterruptedException {

		final List<Message> result = kernel.broadcastMessageWithRoleAndWaitForReplies(requester, group, role, message,
				senderRole, timeOutMilliSeconds);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Influence.BROADCAST_MESSAGE_AND_WAIT + ": received: " + result);
		return result;
	}

	// /**
	// * @see
	// madkit.kernel.MadkitKernel#launchAgentBucketWithRoles(madkit.kernel.AbstractAgent,
	// java.lang.String, int, java.util.Collection)
	// */
	// @Override
	// List<AbstractAgent> launchAgentBucketWithRoles(AbstractAgent requester,
	// String agentClassName, int bucketSize, String... CGRLocations) {
	// if(requester.isFinestLogOn())
	// requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles <" +
	// agentClassName + "," + bucketSize + "," + CGRLocations + ">");
	// final List<AbstractAgent> l = kernel.launchAgentBucketWithRoles(requester,
	// agentClassName, bucketSize, CGRLocations);
	// if(requester.isFinestLogOn())
	// requester.logger.log(Level.FINEST,"launchAgentBucketWithRoles done !");
	// return l;
	// }

	@Override
	void launchAgentBucketWithRoles(AbstractAgent requester, List<AbstractAgent> bucket, int cpuCoreNb,
			Role... CGRLocations) {
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"launchAgentBucketWithRoles : " + bucket.size() + " "
							+ (bucket.size() > 0 ? bucket.get(0).getClass().getName() : "agents !!!") + " "
							+ (CGRLocations.length > 0 ? Arrays.deepToString(CGRLocations) : ""));
		kernel.launchAgentBucketWithRoles(requester, bucket, cpuCoreNb, CGRLocations);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "launchAgentBucketWithRoles OK !");
	}

	/**
	 * @see com.distrimind.madkit.kernel.MadkitKernel#launchAgent(com.distrimind.madkit.kernel.AbstractAgent,
	 *      com.distrimind.madkit.kernel.AbstractAgent, int, boolean)
	 */
	@Override
	final ReturnCode launchAgent(AbstractAgent requester, AbstractAgent agent, int timeOutSeconds, boolean defaultGUI) {
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					Influence.LAUNCH_AGENT + " (" + timeOutSeconds + ")" + agent.getName() + "...");
		final ReturnCode r = kernel.launchAgent(requester, agent, timeOutSeconds, defaultGUI);
		if (r == SUCCESS || r == TIMEOUT) {
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST, Influence.LAUNCH_AGENT.toString() + agent + " " + r);
		} else if (requester.isWarningOn()) {
			requester.handleException(Influence.LAUNCH_AGENT, new MadkitWarning(agent.toString(), r));
		}
		return r;
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent requester, final AbstractAgent target, int timeOutSeconds,
			KillingType killing_type) {
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Influence.KILL_AGENT + " (" + timeOutSeconds + ")" + target + "...");
		final ReturnCode r = kernel.killAgent(requester, target, timeOutSeconds, killing_type);
		if (r == SUCCESS || r == TIMEOUT) {
			if (requester.isFinestLogOn())
				requester.logger.log(Level.FINEST, Influence.KILL_AGENT + target.getName() + " " + r);
		} else if (requester.isWarningOn()) {
			requester.handleException(Influence.KILL_AGENT, new MadkitWarning(target.toString(), r));
		}
		return r;
	}

	@Override
	protected void zombieDetected(State s, AbstractAgent target) {
		kernel.zombieDetected(s, target);
	}

	@Override
	boolean isCommunity(AbstractAgent requester, String community) {
		final boolean fact = kernel.isCommunity(requester, community);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Words.COMMUNITY + " ? " + getCGRString(community) + fact);
		return fact;
	}

	@Override
	boolean isGroup(AbstractAgent requester, Group group) {
		final boolean fact = kernel.isGroup(requester, group);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Words.GROUP + " ? " + getCGRString(group) + fact);
		return fact;
	}

	@Override
	boolean isRole(AbstractAgent requester, Group group, String role) {
		final boolean fact = kernel.isRole(requester, group, role);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Words.ROLE + " ? " + getCGRString(group, role) + fact);
		return fact;
	}

	@Override
	boolean isConcernedBy(AbstractAgent requester, AgentAddress agentAddress) {
		final boolean fact = kernel.isConcernedBy(requester, agentAddress);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, Words.AGENT_ADDRESS + " ? " + agentAddress + ":"
					+ (fact ? "" : (Words.NOT + " ")) + Words.CONCERNED);
		return fact;
	}

	@Override
	boolean isLocalAgentAddressValid(AbstractAgent requester, AgentAddress agentAddress) {
		final boolean fact = kernel.isLocalAgentAddressValid(requester, agentAddress);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					Words.AGENT_ADDRESS + " ? " + agentAddress + ":" + (fact ? "" : (Words.NOT + " ")) + Words.VALID);
		return fact;
	}

	@Override
	MadkitKernel getMadkitKernel() {
		return kernel;
	}

	// @Override //TODO think about this log
	// ReturnCode reloadClass(AbstractAgent requester, String name) throws
	// ClassNotFoundException {
	// final ReturnCode r = kernel.reloadClass(requester, name);
	// if(requester.isFinestLogOn())
	// requester.logger.log(Level.FINEST,Words.RELOAD.toString() + name);
	// if(r == SUCCESS)
	// return SUCCESS;
	// else if(requester.isWarningOn()){
	// requester.handleException(Influence.RELOAD_CLASS, new MadkitWarning(name,
	// r));
	// }
	// return r;
	// }

	@Override
	synchronized boolean removeOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.removeOverlooker(requester, o);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, o.getClass().getSimpleName() + (added ? " removed" : " not added") + o);
		return added;
	}

	@Override
	synchronized boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		final boolean added = kernel.addOverlooker(requester, o);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, o.getClass().getSimpleName() + (added ? " OK" : " already added") + o);
		return added;
	}

	/*
	 * @Override ScheduledThreadPoolExecutor
	 * killScheduledExecutorService(AbstractAgent requester, String name) {
	 * ScheduledThreadPoolExecutor rc=kernel.killScheduledExecutorService(requester,
	 * name); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"killScheduledExecutorService "+name+" : "+
	 * (rc!=null)); return rc; }
	 * 
	 * @Override ScheduledThreadPoolExecutor
	 * getScheduledExecutorService(AbstractAgent requester, String name) {
	 * ScheduledThreadPoolExecutor res=super.getScheduledExecutorService(requester,
	 * name); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"getScheduledExecutorService "+name+(res!=
	 * null?" exists.":" does not exist.")); return res; }
	 */

	@Override
	TaskID scheduleTask(AbstractAgent requester, Task<?> _task, boolean ask_for_execution_confirmation) {
		TaskID t = kernel.scheduleTask(requester, _task, ask_for_execution_confirmation);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"Scheduling task " + _task + (ask_for_execution_confirmation ? "with message confirmation" : "")
							+ " and with default task manager agent : " + (t == null ? "FAIL" : "OK"));
		return t;
	}

	/*
	 * @Override TaskID scheduleTask(AbstractAgent requester, String
	 * _task_agent_name, Task<?> _task, boolean ask_for_execution_confirmation) {
	 * TaskID t=kernel.scheduleTask(requester, _task_agent_name, _task,
	 * ask_for_execution_confirmation); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"Scheduling task "+_task+(
	 * ask_for_execution_confirmation?"with message confirmation":"")
	 * +" and with task manager agent named "+_task_agent_name+" : "+(t==null?"FAIL"
	 * :"OK")); return t; }
	 */

	/*
	 * @Override ConversationID scheduleTasks(AbstractAgent requester,
	 * Collection<Task<?>> _tasks, boolean ask_for_execution_confirmation) {
	 * ConversationID res=kernel.scheduleTasks(requester, _tasks,
	 * ask_for_execution_confirmation); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"Scheduling a collection of tasks "+res+(
	 * ask_for_execution_confirmation?"with message confirmation":"")
	 * +" and with default task manager agent : "+(res==null?"FAIL":"OK")); return
	 * res; }
	 * 
	 * @Override ConversationID scheduleTasks(AbstractAgent requester, String
	 * _task_agent_name, Collection<Task<?>> _tasks, boolean
	 * ask_for_execution_confirmation) { ConversationID
	 * res=kernel.scheduleTasks(requester, _task_agent_name, _tasks,
	 * ask_for_execution_confirmation); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"Scheduling a collection of tasks "+res+(
	 * ask_for_execution_confirmation?"with message confirmation":"")
	 * +" and with task manager agent named "+_task_agent_name+" : "+(res==null?
	 * "FAIL":"OK")); return res; }
	 */

	@Override
	boolean cancelTask(AbstractAgent requester, TaskID task_id, boolean mayInteruptTask) {
		boolean rc = kernel.cancelTask(requester, task_id, mayInteruptTask);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"Canceling task " + task_id + " with default task manager agent : " + rc);
		return rc;
	}

	/*
	 * @Override ReturnCode cancelTask(AbstractAgent requester, String
	 * _task_agent_name, ConversationID task_id) { ReturnCode rc=kernel.
	 * cancelTask(requester, _task_agent_name, task_id); if
	 * (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"Canceling task "
	 * +task_id+" with task manager agent named "+_task_agent_name+" : "+rc); return
	 * rc; }
	 */

	/*
	 * @Override AgentAddress getDefaultTaskAgent(AbstractAgent requester) {
	 * AgentAddress aa=kernel.getDefaultTaskAgent(requester); if
	 * (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"get default task agent : "+aa); return aa;
	 * }
	 */

	/*
	 * @Override ScheduledThreadPoolExecutor
	 * launchAndOrGetScheduledExecutorService(AbstractAgent requester, String name,
	 * int maximumPoolSize, int priority, long timeOutSeconds) {
	 * ScheduledThreadPoolExecutor
	 * aa=kernel.launchAndOrGetScheduledExecutorService(requester, name,
	 * maximumPoolSize, priority, timeOutSeconds); if (requester.isFinestLogOn())
	 * requester.logger.log(Level.FINEST,"get task agent "+name+" : "+aa); return
	 * aa;
	 * 
	 * }
	 */

	/*
	 * @Override ScheduledThreadPoolExecutor
	 * launchAndOrGetScheduledExecutorService(AbstractAgent requester, String
	 * _task_agent_name) { ScheduledThreadPoolExecutor
	 * rc=kernel.launchAndOrGetScheduledExecutorService(requester,
	 * _task_agent_name); if (requester.isFinestLogOn()) requester.logger.log(Level.
	 * FINEST,"Setting threads priority for Task Manager Agent"
	 * +_task_agent_name+" : "+rc); return rc; }
	 */

	@Override
	boolean isConcernedByAutoRequestRole(AbstractAgent requester, Group group, String role) {
		boolean res = kernel.isConcernedByAutoRequestRole(requester, group, role);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "isConcernedByAutoRequestRole (Agent " + requester + ", Group " + group
					+ ", Role " + role + ") : " + res);
		return res;
	}

	@Override
	void removeAllAutoRequestedGroups(AbstractAgent requester) {
		kernel.removeAllAutoRequestedGroups(requester);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "leaveAllAutoRequestedGroups (Agent " + requester + ")");
	}

	@Override
	void leaveAutoRequestedRole(AbstractAgent requester, String role) {
		kernel.leaveAutoRequestedRole(requester, role);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "leaveAutoRequestedRole (Agent " + requester + ", Role " + role + ") ");
	}

	@Override
	void leaveAutoRequestedRole(AbstractAgent requester, AbstractGroup group, String role) {
		kernel.leaveAutoRequestedRole(requester, group, role);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"leaveAutoRequestedRole (Agent " + requester + ", " + group + ", Role " + role + ") ");
	}

	@Override
	void leaveAutoRequestedGroup(AbstractAgent requester, AbstractGroup group) {
		kernel.leaveAutoRequestedGroup(requester, group);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"leaveAutoRequestedGroup (Agent " + requester + ", Group " + group + ") ");
	}

	@Override
	void autoRequesteRole(AbstractAgent requester, AbstractGroup group, String role, ExternalizableAndSizable passKey) {
		kernel.autoRequesteRole(requester, group, role, passKey);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"autoRequesteRole (Agent " + requester + ", Group " + group + ", Role " + role + ")");
	}

	@Override
	void manageDirectConnection(AbstractAgent requester, AskForConnectionMessage m) throws IllegalAccessException {
		kernel.manageDirectConnection(requester, m);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "manageDirectConnection (Agent " + requester + ", Type " + m.getType()
					+ ", IP " + m.getIP() + ", Port " + m.getIP().getPort() + ")");
	}

	@Override
	void manageTransferConnection(AbstractAgent requester, AskForTransferMessage m) throws IllegalAccessException {
		kernel.manageTransferConnection(requester, m);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"manageTransferConnection (Agent " + requester + ", Type " + m.getType() + ", kernel address 1 "
							+ m.getKernelAddress2() + ", kernel address 2 " + m.getKernelAddress2()
							+ ", InetSocketAddress 1 " + m.getInetSocketAddress1() + ", InetSocketAddress 2 "
							+ m.getInetSocketAddress2() + ")");
	}

	@Override
	BigDataTransferID sendBigData(AbstractAgent requester, AgentAddress agentAddress, RandomInputStream stream,
			long pos, long length, ExternalizableAndSizable attachedData, String senderRole, MessageDigestType messageDigestType, boolean excludeFromEncryption)
			throws IOException {
		BigDataTransferID res = kernel.sendBigData(requester, agentAddress, stream, pos, length, attachedData,
				senderRole, messageDigestType, excludeFromEncryption);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"sendBigData (Agent " + requester + ", agent address " + agentAddress + ", stream type "
							+ stream.getClass() + ", start position " + pos + ", length " + length + ", sender role "
							+ senderRole + ", messageDigestType=" + messageDigestType + ")");
		return res;
	}

	@Override
	void acceptDistantBigDataTransfer(AbstractAgent requester, BigDataPropositionMessage originalMessage) {
		kernel.acceptDistantBigDataTransfer(requester, originalMessage);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "acceptDistantBigDataTransfer (Agent " + requester + ", conversation ID "
					+ originalMessage.getConversationID() + ", ID Packet " + originalMessage.getIDPacket() + ")");

	}

	@Override
	void connectionLostForBigDataTransfer(AbstractAgent requester, ConversationID conversationID, int idPacket,
			AgentAddress sender, AgentAddress receiver, long readDataLength, long duration) {
		kernel.connectionLostForBigDataTransfer(requester, conversationID, idPacket, sender, receiver, readDataLength,
				duration);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"acceptDistantBigDataTransfer (Agent " + requester + ", conversation ID " + conversationID
							+ ", ID Packet " + idPacket + ", sender " + sender + ", receiver " + receiver
							+ ", read data length " + readDataLength + ", duration (ms) " + duration + ")");
	}

	@Override
	ReturnCode anomalyDetectedWithOneConnection(AbstractAgent requester, boolean candidateToBan,
			ConnectionIdentifier connection_identifier, String message) {
		ReturnCode rc = kernel.anomalyDetectedWithOneConnection(requester, candidateToBan, connection_identifier,
				message);
		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"anomalyDetectedWithOneConnection (Requester=" + requester + ", candidateToBan=" + true
							+ ", connectionIdentifier=" + connection_identifier + ", result=" + rc + ")");

		return rc;
	}

	@Override
	ReturnCode anomalyDetectedWithOneDistantKernel(AbstractAgent requester, boolean candidateToBan,
			KernelAddress kernelAddress, String message) {
		ReturnCode rc = kernel.anomalyDetectedWithOneDistantKernel(requester, candidateToBan, kernelAddress, message);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "anomalyDetectedWithOneDistantKernel (Requester=" + requester
					+ ", candidateToBan=" + true + ", kernelAddress=" + kernelAddress + ", result=" + rc + ")");

		return rc;
	}

	@Override
	Set<Connection> getEffectiveConnections(AbstractAgent requester) {
		Set<Connection> rc = kernel.getEffectiveConnections(requester);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"getEffectiveConnections (Requester=" + requester + ", result=" + rc + ")");

		return rc;
	}

	@Override
	Set<KernelAddress> getAvailableDistantKernels(AbstractAgent requester) {
		Set<KernelAddress> rc = kernel.getAvailableDistantKernels(requester);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"getAvailableDistantKernels (Requester=" + requester + ", result=" + rc + ")");

		return rc;
	}

	@Override
	List<Group> getAccessibleGroupsGivenByDistantPeer(AbstractAgent requester, KernelAddress kernelAddress) {
		List<Group> rc = kernel.getAccessibleGroupsGivenByDistantPeer(requester, kernelAddress);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "getAccessibleGroupsGivenByDistantPeer (Requester=" + requester
					+ ", distantKernelAddress=" + kernelAddress + ", result=" + rc + ")");

		return rc;
	}

	@Override
	List<Group> getAccessibleGroupsGivenToDistantPeer(AbstractAgent requester, KernelAddress kernelAddress) {
		List<Group> rc = kernel.getAccessibleGroupsGivenToDistantPeer(requester, kernelAddress);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "getAccessibleGroupsGivenToDistantPeer (Requester=" + requester
					+ ", distantKernelAddress=" + kernelAddress + ", result=" + rc + ")");

		return rc;
	}

	@Override
	List<PairOfIdentifiers> getEffectiveDistantLogins(AbstractAgent requester, KernelAddress kernelAddress) {
		List<PairOfIdentifiers> rc = kernel.getEffectiveDistantLogins(requester, kernelAddress);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "getEffectiveDistantLogins (Requester=" + requester
					+ ", distantKernelAddress=" + kernelAddress + ", result=" + rc + ")");

		return rc;
	}

	@Override
	ReturnCode requestHookEvents(AbstractAgent requester, AgentActionEvent hookType, boolean autoremove) {
		ReturnCode rc = kernel.requestHookEvents(requester, hookType, autoremove);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"requestHookEvents (Requester=" + requester + ", hookType=" + hookType + ")");

		return rc;
	}

	@Override
	ReturnCode releaseHookEvents(AbstractAgent requester, AgentActionEvent hookType) {
		ReturnCode rc = kernel.releaseHookEvents(requester, hookType);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"stopHookEvents (Requester=" + requester + ", hookType=" + hookType + ")");

		return rc;
	}

	@Override
	void wait(AbstractAgent requester, LockerCondition locker) throws InterruptedException {
		kernel.wait(requester, locker);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "wait (Requester=" + requester + ", locker=" + locker + ")");

	}

	@Override
	void regularWait(AbstractAgent requester, LockerCondition locker) throws InterruptedException {
		kernel.regularWait(requester, locker);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "regularWait (Requester=" + requester + ", locker=" + locker + ")");
	}

	@Override
	void sleep(AgentFakeThread requester, long millis) throws InterruptedException {
		kernel.sleep(requester, millis);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "wait (Requester=" + requester + ", millis=" + millis + ")");
	}

	@Override
	void receivingPotentialNetworkMessage(AbstractAgent requester, LocalLanMessage m) {
		kernel.receivingPotentialNetworkMessage(requester, m);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST,
					"receivingPotentialNetworkMessage (Requester=" + requester + ", message=" + m + ")");

	}

	@Override
	void waitMessageSent(AbstractAgent requester, LockerCondition locker) throws InterruptedException {
		kernel.waitMessageSent(requester, locker);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "waitMessageSent (Requester=" + requester + ", locker=" + locker + ")");
	}

	@Override
	ScheduledThreadPoolExecutor getMadkitServiceExecutor() {
		return kernel.getMadkitServiceExecutor();
	}

	@Override
	Object weakSetBlackboard(AbstractAgent requester, Group group, String name, Object data) {
		Object res = kernel.weakSetBlackboard(requester, group, name, data);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "weakSetBlackboard (Requester=" + requester + ", group=" + group
					+ ", name=" + name + ", data=" + data + ", res=" + res + ")");

		return res;

	}

	@Override
	Object setBlackboard(AbstractAgent requester, Group group, String name, Object data) {
		Object res = kernel.setBlackboard(requester, group, name, data);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "setBlackboard (Requester=" + requester + ", group=" + group + ", name="
					+ name + ", data=" + data + ", res=" + res + ")");

		return res;

	}

	@Override
	Object getBlackboard(AbstractAgent requester, Group group, String name) {
		Object res = kernel.getBlackboard(requester, group, name);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "getBlackboard (Requester=" + requester + ", group=" + group + ", name="
					+ name + ", res=" + res + ")");

		return res;
	}

	@Override
	Object removeBlackboard(AbstractAgent requester, Group group, String name) {
		Object res = kernel.removeBlackboard(requester, group, name);

		if (requester.isFinestLogOn())
			requester.logger.log(Level.FINEST, "removeBlackboard (Requester=" + requester + ", group=" + group
					+ ", name=" + name + ", res=" + res + ")");

		return res;

	}

	@Override
	boolean checkMemoryLeakAfterNetworkStopped() {
		boolean res = kernel.checkMemoryLeakAfterNetworkStopped();

		if (kernel.isFinestLogOn())
			kernel.logger.log(Level.FINEST, "checkMemoryLeakAfterNetworkStopped (res=" + res + ")");

		return res;
	}

	@Override
	int numberOfValidGeneratedID() {
		int res = kernel.numberOfValidGeneratedID();

		if (kernel.isFinestLogOn())
			kernel.logger.log(Level.FINEST, "numberOfValidGeneratedID (res=" + res + ")");
		return res;
	}

	@Override
	IDGeneratorInt getIDTransferGenerator() {
		return kernel.getIDTransferGenerator();
	}

	@Override
	<V> V take(BlockingDeque<V> toTake) throws InterruptedException {
		return kernel.take(toTake);
	}

	@Override
	List<AbstractAgent> createBucket(final String agentClass, int bucketSize, int cpuCoreNb)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return kernel.createBucket(agentClass, bucketSize, cpuCoreNb);
	}

	@Override
	Map<KernelAddress, InterfacedIDs> getGlobalInterfacedIDs() {
		return kernel.getGlobalInterfacedIDs();
	}

	@Override
	boolean isGlobalInterfacedIDsEmpty() {
		return kernel.isGlobalInterfacedIDsEmpty();
	}

	@Override
	ArrayList<AbstractAgent> getConnectedNetworkAgents() {
		return kernel.getConnectedNetworkAgents();
	}

	@Override
	protected void exit() throws InterruptedException {
		kernel.exit();
	}
}
