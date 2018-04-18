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

import java.util.HashMap;
import java.util.logging.Level;

import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.agr.LocalCommunity.Roles;
import com.distrimind.madkit.gui.AgentStatusPanel;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.CGRSynchro;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.AnomalyDetectedMessage;
import com.distrimind.madkit.kernel.network.AskForConnectionMessage;
import com.distrimind.madkit.kernel.network.AskForTransferMessage;
import com.distrimind.madkit.kernel.network.BroadcastLocalLanMessage;
import com.distrimind.madkit.kernel.network.CGRSynchros;
import com.distrimind.madkit.kernel.network.DirectLocalLanMessage;
import com.distrimind.madkit.kernel.network.LocalLanMessage;
import com.distrimind.madkit.kernel.network.MessageLocker;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.madkit.message.EnumMessage;
import com.distrimind.madkit.message.KernelMessage;

/**
 * @author Jason Mahdjoub
 * @author Fabien Michel
 * @version 1.0
 * @since MaDKitLanEdition 1.0
 *
 */
public final class NetworkAgent extends AgentFakeThread {

	// final static String
	// SCHEDULER_NAME_FOR_AGENTS_FAKE_THREAD="~~MKLE_AGENTS_FAKE_THREAD_SCHEDULER";

	// private AgentAddress kernelAgent;
	private AgentAddress NIOAgentAddress = null, LocalNetworkAffectationAgentAddress = null;
	private HashMap<ConversationID, MessageLocker> messageLockers = new HashMap<>();

	public NetworkAgent() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		setName(super.getName() + getKernelAddress());
		// setLogLevel(Level.INFO);
		requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT);
		requestRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS, LocalCommunity.Roles.NET_AGENT);

		/*
		 * kernelAgent = getAgentWithRole(Groups.NETWORK,
		 * Organization.GROUP_MANAGER_ROLE);
		 * 
		 * if(kernelAgent == null) throw new
		 * AssertionError(this+" no kernel agent to work with... Please bug report");
		 */

		// build servers
		weakSetBlackboard(LocalCommunity.Groups.NETWORK, LocalCommunity.BlackBoards.NETWORK_BLACKBOARD,
				MadkitNetworkAccess.getNetworkBlackboard());
		if (getMadkitConfig().networkProperties.network)
			launchNetwork();

	}

	/**
	 * @return true if servers are launched
	 */
	private boolean launchNetwork() {
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching network agent in " + getKernelAddress() + "...");
		// requestRole(CloudCommunity.Groups.NETWORK_AGENTS,
		// CloudCommunity.Roles.NET_AGENT);

		if (getMadkitConfig().networkProperties.upnpIGDEnabled
				|| getMadkitConfig().networkProperties.networkInterfaceScan) {
			AbstractAgent aa = MadkitNetworkAccess.getUpngIDGAgent(this);
			if (aa.getState() == State.NOT_LAUNCHED)
				launchAgent(aa);
		}
		AbstractAgent aa = MadkitNetworkAccess.getNIOAgent(this);
		launchAgent(aa);
		NIOAgentAddress = aa.getAgentAddressIn(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NIO_ROLE);
		aa = MadkitNetworkAccess.getLocalNetworkAffectationAgent(this);
		launchAgent(aa);
		LocalNetworkAffectationAgentAddress = aa.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
				LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("NetworkAgent in " + getKernelAddress() + " LAUNCHED !");
		return true;
	}

	@Override
	protected void liveByStep(Message _message) {
		handleMessage(_message);
	}

	@Override
	protected void end() {
		stopNetwork();
		removeBlackboard(LocalCommunity.Groups.NETWORK, LocalCommunity.BlackBoards.NETWORK_BLACKBOARD);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("NetworkAgent in " + getKernelAddress() + " KILLED !");
	}

	private void stopNetwork() {
		if (LocalNetworkAffectationAgentAddress != null && NIOAgentAddress != null) {
			broadcastMessage(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.TRANSFER_AGENT_ROLE,
					new StopNetworkMessage(NetworkCloseReason.NORMAL_DETECTION));
			sendMessage(LocalNetworkAffectationAgentAddress,
					new StopNetworkMessage(NetworkCloseReason.NORMAL_DETECTION));
			sendMessage(NIOAgentAddress, new StopNetworkMessage(NetworkCloseReason.NORMAL_DETECTION));
			this.broadcastMessageWithRole(LocalCommunity.Groups.NETWORK,
					LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE, new KernelMessage(KernelAction.STOP_NETWORK),
					LocalCommunity.Roles.NET_AGENT);
			LocalNetworkAffectationAgentAddress = null;
			NIOAgentAddress = null;
			if (logger != null) {
				logger.info("\n\t\t\t\t----- " + getKernelAddress() + " closing network ------\n");
			}
			// leaveGroup(CloudCommunity.Groups.NETWORK_AGENTS);
			AgentStatusPanel.updateAll();
		}
		if (this.getState().compareTo(State.ENDING) < 0)
			this.killAgent(this);
	}

	public static class StopNetworkMessage extends Message {

		private final NetworkCloseReason reason;

		StopNetworkMessage(NetworkCloseReason reason) {
			this.reason = reason;
		}

		public NetworkCloseReason getNetworkCloseReason() {
			return reason;
		}
	}

	public static enum NetworkCloseReason {
		NORMAL_DETECTION(ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED), ANOMALY_DETECTED(
				ConnectionClosedReason.CONNECTION_ANOMALY);

		private ConnectionClosedReason reason;

		private NetworkCloseReason(ConnectionClosedReason reason) {
			this.reason = reason;
		}

		public ConnectionClosedReason getConnectionClosedReason() {
			return reason;
		}

	}

	private void handleMessage(final Message m) throws ClassCastException {

		if (m.getClass() == Replies.class) {
			MessageLocker ml = messageLockers.remove(((Replies) m).getOriginalMessage().getConversationID());
			if (ml != null) {
				try {

					ml.unlock();
				} catch (Exception e) {
					if (logger != null)
						logger.severeLog("Unexpected exception", e);
				}
			}
			return;
		}
		final AgentAddress sender = m.getSender();
		if (sender == null) {// contacted by my private objects (or by the kernel ? no)
			proceedEnumMessage((EnumMessage<?>) m);
		} else if (sender.isFrom(getKernelAddress())) {// contacted locally
			switch (sender.getRole()) {
			case Roles.UPDATER:// It is a CGR update
				for (AgentAddress aa : getAgentsWithRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, false))
					sendMessage(aa, m.clone());
				break;
			case Roles.SECURITY:// It is a security problem
				if (m.getClass() == AnomalyDetectedMessage.class) {
					AnomalyDetectedMessage a = ((AnomalyDetectedMessage) m);
					if (a.getKernelAddress() != null)
						broadcastMessage(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
								LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, a, false);
					else if (a.getInetSocketAddress() != null)
						broadcastMessage(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
								LocalCommunity.Roles.SOCKET_AGENT_ROLE, a, false);
				}
				break;
			case Roles.EMMITER:// It is a message to send elsewhere
			{
				MessageLocker ml = null;
				if (m instanceof LocalLanMessage) {
					ml = ((LocalLanMessage) m).getMessageLocker();
					ml.lock();

				}

				ReturnCode rc = broadcastMessage(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, m, ml != null);
				if (rc.equals(ReturnCode.SUCCESS)) {
					if (ml != null)
						messageLockers.put(m.getConversationID(), ml);
				} else {

					if (ml != null) {
						ml.cancelLock();
					}
				}
				break;
			}
			case Roles.KERNEL:// message from the kernel

				proceedEnumMessage((EnumMessage<?>) m);
				break;
			case LocalCommunity.Roles.SOCKET_AGENT_ROLE:
				if (m instanceof CGRSynchro) {
					kernel.getMadkitKernel().injectOperation((CGRSynchro) m);
				} else if (m instanceof CGRSynchros) {
					kernel.getMadkitKernel().importDistantOrg((CGRSynchros) m);
				}
				break;
			case LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE:
				if (m instanceof DirectLocalLanMessage) {
					getMadkitKernel().injectMessage((DirectLocalLanMessage) m);
				} else if (m instanceof BroadcastLocalLanMessage) {
					getMadkitKernel().injectMessage((BroadcastLocalLanMessage) m);
				} else
					handleNotUnderstoodMessage(m);
				break;
			default:
				handleNotUnderstoodMessage(m);
				break;
			}
		}
	}

	private void handleNotUnderstoodMessage(Message m) {
		if (logger != null)
			logger.severeLog("not understood :\n" + m);
	}

	@SuppressWarnings("unused")
	private void exit() {
		this.killAgent(this);
	}

	@Override
	public void manageDirectConnection(final AskForConnectionMessage message) {
		if (message == null)
			throw new NullPointerException("message");
		sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE, message,
				LocalCommunity.Roles.NET_AGENT);
	}

	@Override
	public void manageTransferConnection(final AskForTransferMessage message) {
		if (message == null)
			throw new NullPointerException("message");
		sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE, message,
				LocalCommunity.Roles.NET_AGENT);
	}

}