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
package com.distrimind.madkit.kernel.network;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentNetworkID;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.kernel.network.TransferAgent.InterfacedIDTransfer;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.madkit.kernel.network.connection.PointToPointTransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.message.ObjectMessage;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class IndirectAgentSocket extends AbstractAgentSocket {
	private final IDTransfer transfer_id;
	// protected final TransferSpeedStat stat;
	// private long time_between_each_packet;
	private final AgentAddress agentSocketRequester;
	private final KernelAddress distant_kernel_address_requester;
	private final int numberOfIntermediatePeers;
	private StatsBandwidth statRequester;
	private KernelAddress kernelAddressDestinationForSystemBroadcast;
	private final InetSocketAddress distantInetSocketAddressRoot;
	private final AgentNetworkID socketIDRoot;
	private TaskID pingTaskID;

	IndirectAgentSocket(AbstractIP distantIP, AgentAddress agent_for_distant_kernel_aa, SocketChannel _socket,
			AgentAddress _nio_agent_address, InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean _this_ask_connection, IDTransfer _transfer_id,
			int numberOfIntermediatePeers, StatsBandwidth statRequester, AgentAddress agentSocketRequester,
			KernelAddress distant_kernel_address_requester, KernelAddress kernelAddressDestinationForSystemBroadcast,
			InetSocketAddress distantInetSocketAddressRoot, AgentNetworkID socketIDRoot) {
		super(distantIP, agent_for_distant_kernel_aa, _socket, _nio_agent_address, _distant_inet_address,
				_local_interface_address, _this_ask_connection);
		if (_transfer_id == null)
			throw new NullPointerException("_transfer_id");
		if (_transfer_id.equals(TransferAgent.NullIDTransfer))
			throw new IllegalArgumentException("The given IDTransfer cannot be a NullIDTransfer !");
		if (kernelAddressDestinationForSystemBroadcast == null)
			throw new NullPointerException("kernelAddressDestinationForSystemBroadcast");
		transfer_id = _transfer_id;
		this.numberOfIntermediatePeers = numberOfIntermediatePeers;
		// stat=new
		// TransferSpeedStat(this.getMadkitConfig().networkProperties.maxBufferSize*3,
		// (this.getMadkitConfig().networkProperties.maxBufferSize*3)/20);
		if (statRequester == null)
			throw new NullPointerException("statRequester");
		this.statRequester = statRequester;
		statRequester.putTransferAgentStats(transfer_id, super.getStatistics());
		this.agentSocketRequester = agentSocketRequester;
		this.kernelAddressDestinationForSystemBroadcast = kernelAddressDestinationForSystemBroadcast;
		this.distant_kernel_address_requester = distant_kernel_address_requester;
		this.distantInetSocketAddressRoot = distantInetSocketAddressRoot;
		this.socketIDRoot = socketIDRoot;
	}

	@Override
	public AgentNetworkID getSocketID() {
		return socketIDRoot;
	}

	@Override
	public InetSocketAddress getDistantInetSocketAddressRoot() {
		return distantInetSocketAddressRoot;
	}

	@Override
	protected void initiateConnectionIfNecessaryOnActivation() {

	}

	@Override
	public void activate() {
		super.activate();
		if (!this.requestRole(LocalCommunity.Groups.getAgentSocketGroup(agentSocketRequester.hashCode()),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS) && logger != null)
			logger.severe(
					"Cannot request group " + LocalCommunity.Groups.getAgentSocketGroup(agentSocketRequester.hashCode())
							+ " and role " + LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		pingTaskID = scheduleTask(new Task<>(new Callable<Void>() {

			@Override
			public Void call() {
				if (!isAlive())
					return null;
				long threshold = System.currentTimeMillis() - getMadkitConfig().networkProperties.connectionTimeOut;
				if (lastReceivedDataUTC < threshold) {
					lastReceivedDataUTC = System.currentTimeMillis();

					if (waitingPongMessage) {
						startDeconnectionProcess(ConnectionClosedReason.CONNECTION_LOST);
					} else {
						receiveMessage(new SendPingMessage());
					}
				}
				return null;
			}
		}, System.currentTimeMillis() + getMadkitConfig().networkProperties.connectionTimeOut,
				getMadkitConfig().networkProperties.connectionTimeOut));
	}

	public KernelAddress getDistantKernelAddressRequester() {
		return distant_kernel_address_requester;
	}

	@Override
	public StatsBandwidth getStatistics() {
		return statRequester;
	}

	void initiateConnectionIfNecessary() throws ConnectionException {
		super.initiateConnectionIfNecessaryOnActivation();
	}

	@Override
	public IDTransfer getTransfertType() {
		return transfer_id;
	}

	/*
	 * @Override protected TransferSpeedStat getBytesPerSecondsStat() { return stat;
	 * }
	 */

	@Override
	protected ReturnCode broadcastDataTowardEachIntermediatePeer(DataToBroadcast _data, boolean prioritary) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Broadcasting indirect data toward each intermediate peer (distant_inet_address="
					+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
					+ ", prioritary=" + prioritary + ") : " + _data);
		return sendMessageWithRole(agentSocketRequester, new ObjectMessage<>(
						new DataToBroadcast(_data.getMessageToBroadcast(), _data.getSender(), prioritary, getTransfertType())),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);
	}

	@Override
	protected ReturnCode broadcastDataTowardEachIntermediatePeer(BroadcastableSystemMessage _data, boolean prioritary) {
		if (_data == null)
			throw new NullPointerException("_data");
		DataToBroadcast d = new DataToBroadcast(_data, getKernelAddress(), prioritary, getTransfertType());
		ReturnCode rc = sendMessageWithRole(agentSocketRequester, new ObjectMessage<>(d),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);

		if (rc != ReturnCode.SUCCESS || _data.getMessageLocker() == null) {
			if (logger != null && rc != ReturnCode.SUCCESS)
				logger.warning("Impossible to broadcast indirect data : " + _data);
			return rc;
		}
		try {

			return _data.getMessageLocker().waitUnlock(this, true);
		} catch (InterruptedException e) {
			return ReturnCode.TRANSFER_IN_PROGRESS;
		}
	}

	@Override
	protected ReturnCode broadcastDataTowardEachIntermediatePeer(AgentAddress sender, BroadcastableSystemMessage _data,
			IDTransfer distantIDDestination, KernelAddress kaServer, boolean isPrioritary) {
		if (sender == null) {
			throw new IllegalAccessError();
		}
		InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(sender, distantIDDestination);
		if (idt == null)
			idt = getInterfacedIDTransferToFinalize(sender, distantIDDestination);
		if (idt == null) {
			return ReturnCode.NO_RECIPIENT_FOUND;
		}
		return sendMessageWithRole(idt.getTransferToAgentAddress(),
				new ObjectMessage<>(
						new DataToBroadcast(_data, kaServer, isPrioritary, _data.getIdTransferDestination())),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);
	}

	@Override
	protected int getNumberOfIntermediatePeers() {
		return numberOfIntermediatePeers;
	}

	AgentAddress getParentAgentSocketAddress() {
		return agentSocketRequester;
	}

	private void cancelPingTaskID() {
		if (pingTaskID != null) {
			pingTaskID.cancelTask(false);
			pingTaskID = null;
		}

	}

	@Override
	protected void end() {
		statRequester.removeTransferAgentStats(getTransfertType());
		cancelPingTaskID();
		super.end();

	}

	@Override
	protected void deconnected(ConnectionClosedReason reason, Collection<AbstractData> _data_not_sent,
			ArrayList<AbstractData> bigDataNotSent, Collection<AbstractData> dataToTransferNotSent) {
		cancelPingTaskID();
		super.deconnected(reason, _data_not_sent, bigDataNotSent, dataToTransferNotSent);
	}

	@Override
	protected void checkTransferBlockCheckerChangments() throws ConnectionException {
		if (connection_protocol.isTransferBlockCheckerChanged()) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Update and broacast transfer block checker");

			TransferedBlockChecker tbc=this.connection_protocol.getTransferedBlockChecker();
			if (tbc.isCompletelyInoperant() && getMadkitConfig().networkProperties.canUsePointToPointTransferedBlockChecker)
			{
				tbc=new PointToPointTransferedBlockChecker();
				connection_protocol.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbc);
			}
			else
				connection_protocol.setPointToPointTransferedBlockChecker(null);
			
			TransferBlockCheckerSystemMessage tbcm = new TransferBlockCheckerSystemMessage(getTransfertType(),
					this.kernelAddressDestinationForSystemBroadcast,
					tbc);
			tbcm.setMessageLocker(new MessageLocker(null));
			
			broadcastDataTowardEachIntermediatePeer(tbcm, true);
			try {
				sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected InterfacedIDTransfer getInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender,
			int idTransfer) {
		if (sender == null)
			throw new NullPointerException("sender");
		return transfer_ids.getMiddle(idTransfer, sender);
	}

	@Override
	protected void putInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender, InterfacedIDTransfer idt) {
		if (sender == null)
			throw new NullPointerException("sender");
		transfer_ids.putMiddle(sender, idt);
	}

	@Override
	protected void validateInterfacedIDTransfer(AgentAddress sender, IDTransfer id, boolean forceLocalID) {
		HashMap<AgentAddress, InterfacedIDTransfer> middle = this.transfer_ids_to_finalize.removeMiddle(id);
		this.transfer_ids.putMiddle(id.getID(), middle);
		TransferPropositionSystemMessage tp = this.transfer_ids_to_finalize.removeTransferPropositionSystemMessage(id);
		if (tp != null)
			this.transfer_ids.putTransferPropositionSystemMessage(id, tp);
		AgentAddress aa = this.transfer_ids_to_finalize.removeTransferAgentAddress(id);
		if (aa != null)
			this.transfer_ids.putTransferAgentAddress(id, aa);
	}

	@Override
	protected InterfacedIDTransfer removeInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender,
			IDTransfer id, boolean forceLocal) {
		transfer_ids.removeTransferPropositionSystemMessage(id);
		transfer_ids.removeTransferAgentAddress(id);
		return transfer_ids.removeMiddle(id).values().iterator().next();
	}

}
