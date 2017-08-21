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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.database.IPBanStat;
import com.distrimind.madkit.database.IPBanned;
import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.exceptions.MadkitException;
import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.exceptions.OverflowException;
import com.distrimind.madkit.exceptions.RouterException;
import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.i18n.AgentSocketMessage;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.AgentNetworkID;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.MultiGroup;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;
import com.distrimind.madkit.kernel.network.AskForTransferMessage.InitiateTransferConnection;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.AbstractPacketData;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.AgentSocketData;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.DistKernADataToUpgradeMessage;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.ReceivedSerializableObject;
import com.distrimind.madkit.kernel.network.LocalNetworkAgent.PossibleInetAddressesUsedForDirectConnectionChanged;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.ExceededDataQueueSize;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.TransferAgent.DirectConnectionFailed;
import com.distrimind.madkit.kernel.network.TransferAgent.DirectConnectionSuceeded;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.kernel.network.TransferAgent.InterfacedIDTransfer;
import com.distrimind.madkit.kernel.network.TransferAgent.TryDirectConnection;
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionFinished;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.madkit.kernel.network.connection.ErrorConnection;
import com.distrimind.madkit.kernel.network.connection.access.AccessAbordedMessage;
import com.distrimind.madkit.kernel.network.connection.access.AccessAskInitiliazation;
import com.distrimind.madkit.kernel.network.connection.access.AccessErrorMessage;
import com.distrimind.madkit.kernel.network.connection.access.AccessMessage;
import com.distrimind.madkit.kernel.network.connection.access.AccessMessagesList;
import com.distrimind.madkit.kernel.network.connection.access.AccessGroupsNotifier;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocol;
import com.distrimind.madkit.kernel.network.connection.access.DoNotSendMessage;
import com.distrimind.madkit.kernel.network.connection.access.Identifier;
import com.distrimind.madkit.kernel.network.connection.access.LoginEventsTrigger;
import com.distrimind.madkit.kernel.network.connection.access.NewLocalLoginAddedMessage;
import com.distrimind.madkit.kernel.network.connection.access.NewLocalLoginRemovedMessage;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.hook.NetworkGroupsAccessEvent;
import com.distrimind.madkit.message.hook.NetworkLoginAccessEvent;
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.message.hook.NetworkEventMessage;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;
import com.distrimind.madkit.message.hook.IPBannedEvent;
import com.distrimind.madkit.message.hook.NetworkAnomalyEvent;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.IDGeneratorInt;
import com.distrimind.util.Timer;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SecureRandomType;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
abstract class AbstractAgentSocket extends AgentFakeThread implements AccessGroupsNotifier {

	enum State {
		CONNECTION_IN_PROGRESS, CONNECTED_INITIALIZING_ACCESS, CONNECTED, DISCONNECTION_IN_PROGRESS, DISCONNECTION;
	}

	protected final AbstractIP distantIP;
	protected final InetSocketAddress distant_inet_address;
	protected final InetSocketAddress local_interface_address;
	// protected final ArrayList<ConnectionProtocol> connection_protocols;
	protected ConnectionProtocol<?> connection_protocol;
	protected State state = State.CONNECTION_IN_PROGRESS;
	protected ConnectionClosedReason connection_closed_reason = null;

	protected int max_buffer_size;
	protected int max_block_size;

	protected final boolean this_ask_connection;
	protected AbstractAccessProtocol access_protocol;
	// private ConnectionState
	// global_connection_state=ConnectionState.NOT_CONNECTED;
	protected final AgentAddress nio_agent_address;
	protected final SocketChannel socket;
	/*
	 * private byte buffer[]=null; private int cursor_buffer=0;
	 */
	private DataSocketSynchronizer dataSynchronizer = new DataSocketSynchronizer();
	final AtomicBoolean distantKernelAddressValidated = new AtomicBoolean(false);
	private boolean distant_kernel_agent_activated = false;
	private AgentAddress distant_socket_agent_address = null;
	protected volatile boolean waitingPongMessage = false;

	private DataSocketSynchronizer.SocketAgentInterface dataSynchronized = new DataSocketSynchronizer.SocketAgentInterface() {

		@Override
		public void receivedBlock(Block block) {
			try {
				try {
					InterfacedIDTransfer idt = routesData(block);
					if (idt == null)
						receiveData(block);
					else {
						block.setTransfertID(idt.getLocalID().getID());
						if (idt.getTransferToAgentAddress() != null) {

							if (idt.getTransferBlockChecker() == null) {
								receiveIndirectData(block, idt);
							} else {
								SubBlockInfo sbi = idt.getTransferBlockChecker()
										.recursiveCheckSubBlock(new SubBlock(block));

								if (sbi.isValid()) {
									receiveDataToResend(block, idt);
								} else {
									processInvalidBlock(
											new RouterException("Invalid block with transfer block checker "
													+ idt.getTransferBlockChecker() + " !"),
											block, sbi.isCandidateToBan());
								}
							}
						} else {
							throw new RouterException("Unexpected exception");
						}
					}
				} catch (RouterException e) {
					processInvalidBlock(e, block, false);
				}
			} catch (Exception e) {
				processInvalidBlock(e, block, false);
			}

		}

		@Override
		public boolean processInvalidBlock(Exception _e, Block _block, boolean _candidate_to_ban) {
			return AbstractAgentSocket.this.processInvalidBlock(_e, _block, _candidate_to_ban);
		}

		@Override
		public boolean isBannedOrExpulsed() {
			return AbstractAgentSocket.this.isBannedOrExpulsed();
		}
	};
	AgentAddress agent_for_distant_kernel_aa;
	private boolean need_random;
	private final AbstractSecureRandom random;
	final AtomicReference<HashMap<AgentAddress, SecretMessage>> currentSecretMessages = new AtomicReference<>(null);
	private final AtomicBoolean exceededDataQueueSize = new AtomicBoolean(false);
	protected KernelAddressInterfaced distantInterfacedKernelAddress = null;
	protected final AtomicLong dataToTransferInQueue = new AtomicLong(0);
	private ConnectionInfoSystemMessage distantConnectionInfo = null;
	protected final TransferIDs transfer_ids = new TransferIDs();
	protected final TransferIDs transfer_ids_to_finalize = new TransferIDs();
	private TaskID taskTransferNodeChecker = null;
	private final AtomicReference<StatsBandwidth> stats = new AtomicReference<>(null);
	private boolean isBanned = false;
	protected volatile long lastDistKernADataToUpgradeMessageSentUTC = Long.MIN_VALUE;
	protected volatile long lastReceivedDataUTC = Long.MIN_VALUE;

	public AbstractAgentSocket(AbstractIP distantIP, AgentAddress agent_for_distant_kernel_aa, SocketChannel _socket,
			AgentAddress _nio_agent_address, InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, boolean _this_ask_connection) {
		super();

		if (distantIP == null)
			throw new NullPointerException("distantIP");
		if (_socket == null)
			throw new NullPointerException("_socket");
		if (_nio_agent_address == null)
			throw new NullPointerException("_nio_agent_address");
		if (_distant_inet_address == null)
			throw new NullPointerException("_distant_inet_address");
		if (_local_interface_address == null)
			throw new NullPointerException("_local_interface_address");
		distant_inet_address = _distant_inet_address;
		local_interface_address = _local_interface_address;
		this_ask_connection = _this_ask_connection;
		nio_agent_address = _nio_agent_address;
		socket = _socket;
		this.agent_for_distant_kernel_aa = agent_for_distant_kernel_aa;
		this.distantIP = distantIP;
		AbstractSecureRandom r = null;
		try {
			r = SecureRandomType.DEFAULT.getInstance();
		} catch (Exception e) {
			if (logger != null)
				logger.severeLog("Unexpected exception", e);
		}
		random = r;
	}

	abstract IDTransfer getTransfertType();

	public StatsBandwidth getStatistics() {
		if (stats.get() == null)
			stats.set(getMadkitConfig().networkProperties.addIfNecessaryAndGetStatsBandwitdh(
					new ConnectionIdentifier(getTransfertType(), distant_inet_address, local_interface_address)));
		return stats.get();
	}

	public int getMaxBlockSize() {
		return max_block_size;
	}

	public InetSocketAddress getDistantInetSocketAddressRoot() {
		return distant_inet_address;
	}

	protected void addTaskTransferCheckerIfNecessary() {
		if (taskTransferNodeChecker == null) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Adding transfer ckecker task (distant_inet_address=" + distant_inet_address
						+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");

			taskTransferNodeChecker = this.scheduleTask(new Task<>(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					receiveMessage(new CheckDeadTransferNodes());
					return null;
				}
			}, System.currentTimeMillis() + getMadkitConfig().networkProperties.connectionTimeOut,
					getMadkitConfig().networkProperties.connectionTimeOut));
		}
	}

	protected void removeTaskTransferCheckerIfNecessary() {
		if (taskTransferNodeChecker != null) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Removing transfer ckecker task (distant_inet_address=" + distant_inet_address
						+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");

			if (!transfer_ids.hasDataToCheck() && !transfer_ids_to_finalize.hasDataToCheck()) {
				cancelTask(taskTransferNodeChecker, false);
				taskTransferNodeChecker = null;
			}
		}
	}

	protected long getDelayBeforeTransferNodeBecomesObsolete() {
		return getMadkitConfig().networkProperties.connectionTimeOut * 3;
	}

	@Override
	protected void activate() {
		try {
			setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
			if (logger != null && logger.isLoggable(Level.FINE))
				logger.fine("Starting " + toString() + " (" + this.distant_inet_address + ")... !");
			if (logger != null)
				logger.info("Starting connection (distant_inet_address=" + distant_inet_address + ", local_interface="
						+ local_interface_address + ")");

			connection_protocol = getMadkitConfig().networkProperties.getConnectionProtocolInstance(
					distant_inet_address, local_interface_address, getMadkitConfig().getDatabaseWrapper(),
					!this_ask_connection, this instanceof IndirectAgentSocket);

			if (connection_protocol == null)
				throw new IllegalArgumentException(
						"The properties must have at least one connection protocol comptatible !");

			max_buffer_size = getMadkitConfig().networkProperties.maxBufferSize;
			if (max_buffer_size <= 0)
				throw new IllegalArgumentException("The buffer size must be greater than 0");
			short max = SubBlocksStructure.getAbsoluteMaximumBufferSize(connection_protocol);
			if (max <= 0)
				throw new IllegalArgumentException(
						String.format(AgentSocketMessage.TO_MUCH_BYTE_CONNEC_PROTOCOL.toString(),
								new Integer(SubBlocksStructure.getAbsoluteMaximumBlockSize(connection_protocol, 1)),
								new Integer(0xFFFF)));
			if (max_buffer_size > max)
				throw new IllegalArgumentException(
						String.format(AgentSocketMessage.BUFFER_SIZE_TO_BIG_CONSIDERING_CONNEC_PROTOCOL.toString(),
								new Integer(max), new Integer(max_buffer_size)));
			max_block_size = SubBlocksStructure.getAbsoluteMaximumBlockSize(connection_protocol, max_buffer_size);
			if (max_block_size > 0xFFFF)
				throw new NIOException(new UnexpectedException(AgentSocketMessage.UNEXPECTED_EXCEPTION.toString()));
			// defining the block size and the packet offset
			boolean nr = false;

			for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext();) {
				ConnectionProtocol<?> cp = it.next();
				if (cp.isCrypted()) {
					nr = true;
					break;
				}
			}
			need_random = nr;
			LoginEventsTrigger lt = new LoginEventsTrigger() {

				@Override
				public void removingIdentifiers(Collection<Identifier> _identifiers) {
					AbstractAgentSocket.this.receiveMessage(new ObjectMessage<NewLocalLoginRemovedMessage>(
							new NewLocalLoginRemovedMessage(new ArrayList<>(_identifiers))));
				}

				@Override
				public void removingIdentifier(Identifier _identifier) {
					ArrayList<Identifier> identifiers = new ArrayList<>();
					identifiers.add(_identifier);
					AbstractAgentSocket.this.receiveMessage(
							new ObjectMessage<NewLocalLoginRemovedMessage>(new NewLocalLoginRemovedMessage(identifiers)));
				}

				@Override
				public void addingIdentifiers(Collection<Identifier> _identifiers) {
					AbstractAgentSocket.this.receiveMessage(new ObjectMessage<NewLocalLoginAddedMessage>(
							new NewLocalLoginAddedMessage(new ArrayList<>(_identifiers))));

				}

				@Override
				public void addingIdentifier(Identifier _identifier) {
					ArrayList<Identifier> identifiers = new ArrayList<>();
					identifiers.add(_identifier);
					AbstractAgentSocket.this.receiveMessage(new ObjectMessage<NewLocalLoginAddedMessage>(
							new NewLocalLoginAddedMessage(new ArrayList<>(identifiers))));
				}
			};

			my_accepted_groups = new Groups();
			my_accepted_logins = new Logins();

			access_protocol = getMadkitConfig().networkProperties.getAccessProtocolProperties(distant_inet_address,local_interface_address).getAccessProtocolInstance(distant_inet_address, local_interface_address, lt, getMadkitConfig());
			
			if (!this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE)
					.equals(ReturnCode.SUCCESS) && logger != null)
				logger.severe("Cannot request group " + LocalCommunity.Groups.NETWORK + " and role "
						+ LocalCommunity.Roles.SOCKET_AGENT_ROLE);

			if (!this.requestRole(LocalCommunity.Groups.getAgentSocketGroup(getAgentID()),
					LocalCommunity.Roles.MASTER_SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS) && logger != null)
				logger.severe("Cannot request group " + LocalCommunity.Groups.getAgentSocketGroup(getAgentID())
						+ " and role " + LocalCommunity.Roles.SOCKET_AGENT_ROLE);

			if (!requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(agent_for_distant_kernel_aa),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS) && logger != null)
				logger.severe("Cannot request group "
						+ LocalCommunity.Groups.getDistantKernelAgentGroup(agent_for_distant_kernel_aa) + " and role "
						+ LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			access_protocol.setKernelAddress(getKernelAddress());

			sendMessageWithRole(this.agent_for_distant_kernel_aa,
					new ObjectMessage<AgentSocketData>(new AgentSocketData(this)),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			if (this.distantConnectionInfo != null)
				AbstractAgentSocket.this.sendMessageWithRole(agent_for_distant_kernel_aa,
						new ObjectMessage<>(this.distantConnectionInfo), LocalCommunity.Roles.SOCKET_AGENT_ROLE);

			// this.launchAgent(watcher);
			initiateConnectionIfNecessaryOnActivation();
			if (logger != null && logger.isLoggable(Level.FINE))
				logger.fine(" launched ! (distant_inet_address=" + this.distant_inet_address + ", this_ask_connection="
						+ this_ask_connection + ", connectionProtocolsNumber="
						+ (1 + connection_protocol.sizeOfSubConnectionProtocols()) + ") ");

		} catch (Exception e) {
			if (logger != null)
				logger.severeLog(
						"Start of " + this.getClass().getName() + " (" + this.distant_inet_address + ") FAILED !", e);
			startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
		}
	}

	protected void initiateConnectionIfNecessaryOnActivation() throws ConnectionException {
		if (this_ask_connection) {
			ConnectionMessage cm = null;
			for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext();) {

				ConnectionProtocol<?> cp = it.next();
				if (!cp.hasFinished()) {
					cm = cp.setAndGetNextMessage(new AskConnection(true));

					if (cm == null)
						throw new NullPointerException(
								"The first returned message by the asker's connection protocol must be an AskMessage.");
					if (!(cm instanceof AskConnection))
						throw new ConnectionException(
								"The first returned message by the asker's connection protocol must be an AskMessage.");
					break;
				}
			}
			if (cm != null) {
				checkTransferBlockCheckerChangments();
				sendData((AskConnection) cm, true, false);
			}
		}
	}

	@Override
	protected void end() {
		// MadkitKernelAccess.removeGroupChangementNotifier(my_accepted_groups);
		cancelTaskTransferNodeChecker();
		getMadkitConfig().networkProperties.removeStatsBandwitdh(
				new ConnectionIdentifier(getTransfertType(), distant_inet_address, local_interface_address));
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine(this.getClass().getName() + " (" + this.distant_inet_address + ") killed !");

	}

	protected void startDeconnectionProcess(ConnectionClosedReason reason) {
		this.startDeconnectionProcess(reason, null);
	}

	protected void startDeconnectionProcess(ConnectionClosedReason reason, ConnectionFinished last_message) {
		if (reason == ConnectionClosedReason.CONNECTION_ANOMALY)
			isBanned = true;
		if (state.compareTo(State.DISCONNECTION_IN_PROGRESS) >= 0)
			return;
		if (logger != null)
			logger.info("Starting deconnection process (distant_inet_address=" + distant_inet_address
					+ ", local_interface=" + local_interface_address + ", distantInterfacedKernelAddress="
					+ distantInterfacedKernelAddress + ", reason=" + reason + ")");
		state = State.DISCONNECTION_IN_PROGRESS;
		connection_closed_reason = reason;
		transfer_ids.closeAllTransferID(false);
		transfer_ids_to_finalize.closeAllTransferID(false);
		if (last_message == null) {
			try {

				if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
					if (reason.equals(ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED))
						sendData(new ConnectionFinished(this.distant_inet_address, reason), true, true);
					else {
						closeConnectionProtocols(reason);
						sendMessageWithRole(
								this.nio_agent_address, new AskForConnectionMessage(reason, distantIP,
										getDistantInetSocketAddressRoot(), local_interface_address, true, false),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					}
				} else {
					if (reason.equals(ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED))
						sendData(new ConnectionFinished(this.distant_inet_address, reason), true, false);
					else {
						closeConnectionProtocols(reason);
						sendMessageWithRole(this.nio_agent_address,
								new AskForConnectionMessage(reason, distantIP, getDistantInetSocketAddressRoot(),
										local_interface_address, true, false, getTransfertType(),
										getAgentAddressIn(LocalCommunity.Groups.NETWORK,
												LocalCommunity.Roles.SOCKET_AGENT_ROLE)),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					}
				}

			} catch (ConnectionException e) {
				if (logger != null)
					logger.severeLog("Deconnection problem", e);
			}
		} else
			sendData(last_message, true, true);
	}

	public AgentNetworkID getSocketID() {
		return getNetworkID();
	}

	private void closeConnectionProtocols(ConnectionClosedReason reason) throws ConnectionException {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Closing all protocols (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ", reason=" + reason
					+ ")");
		for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext();) {
			ConnectionProtocol<?> cp = it.next();
			if (!cp.isConnectionFinishedButClosed())
				cp.setConnectionClosed(reason);
		}

	}

	private ArrayList<AbstractData> getFileteredData(Collection<AbstractData> data, boolean checkTransferIds) {
		ArrayList<AbstractData> res = new ArrayList<>(data.size());
		for (AbstractData ad : data) {
			if (ad.getIDTransfer().equals(getTransfertType())
					|| (checkTransferIds && this.transfer_ids.getLocal(ad.getIDTransfer()) != null))
				res.add(ad);
		}
		return res;
	}

	private void cancelTaskTransferNodeChecker() {
		if (taskTransferNodeChecker != null) {
			taskTransferNodeChecker.cancelTask(false);
			taskTransferNodeChecker = null;
		}

	}

	protected void deconnected(ConnectionClosedReason reason, Collection<AbstractData> _data_not_sent,
			ArrayList<AbstractData> bigDataNotSent, Collection<AbstractData> dataToTransferNotSent) {

		try {
			cancelTaskTransferNodeChecker();
			connection_closed_reason = reason;
			for (AgentAddress aa : getAgentsWithRole(LocalCommunity.Groups.getAgentSocketGroup(getAgentID()),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE))
				sendMessageWithRole(aa,
						new ConnectionClosed(this.getSocketID(),
								reason == ConnectionClosedReason.CONNECTION_ANOMALY ? reason
										: ConnectionClosedReason.CONNECTION_LOST,
								_data_not_sent, bigDataNotSent, dataToTransferNotSent),
						LocalCommunity.Roles.MASTER_SOCKET_AGENT_ROLE);
			transfer_ids.closeAllTransferID(false);
			transfer_ids_to_finalize.closeAllTransferID(false);

			if (state.compareTo(State.DISCONNECTION_IN_PROGRESS) <= 0
					&& reason == ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED) {
				closeConnectionProtocols(reason);
			}
		} catch (ConnectionException e) {
			if (logger != null)
				logger.severeLog("Deconnection problem", e);

		}

		state = State.DISCONNECTION;
		try {
			_data_not_sent = getFileteredData(_data_not_sent, false);
			bigDataNotSent = getFileteredData(bigDataNotSent, false);
			dataToTransferNotSent = getFileteredData(dataToTransferNotSent, true);
			for (AbstractData ad : _data_not_sent)
				ad.unlockMessage();
			for (AbstractData ad : bigDataNotSent)
				ad.unlockMessage();
			for (AbstractData ad : dataToTransferNotSent)
				ad.unlockMessage();

			if (this.agent_for_distant_kernel_aa != null)
				this.sendMessageWithRole(agent_for_distant_kernel_aa,
						new AgentSocketKilled(_data_not_sent, bigDataNotSent, dataToTransferNotSent),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			this.sendMessageWithRole(nio_agent_address,
					new AgentSocketKilled(_data_not_sent, bigDataNotSent, dataToTransferNotSent),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			if (logger != null)
				logger.info("Deconnection OK (distant_inet_address=" + distant_inet_address + ", local_interface="
						+ local_interface_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
						+ ", reason=" + reason + ")");
			if (agent_for_distant_kernel_aa != null && distantInterfacedKernelAddress != null)
				MadkitKernelAccess
						.informHooks(this,
								new NetworkEventMessage(connection_closed_reason,
										new Connection(new ConnectionIdentifier(getTransfertType(),
												distant_inet_address, local_interface_address),
												distantInterfacedKernelAddress)));
			this.killAgent(this);
		} catch (SelfKillException e) {
			throw e;
		} catch (MadkitException e) {
			if (logger != null)
				logger.severeLog("", e);
			this.killAgent(this);
		}
	}

	class AgentSocketKilled extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3421377471500152881L;

		protected final Collection<AbstractData> shortDataNotSent;
		protected final ArrayList<AbstractData> bigDataNotSent;
		protected final Collection<AbstractData> dataToTransferNotSent;

		AgentSocketKilled(Collection<AbstractData> _data_not_sent, ArrayList<AbstractData> bigDataNotSent,
				Collection<AbstractData> dataToTransferNotSent) {
			this.shortDataNotSent = _data_not_sent;
			this.bigDataNotSent = bigDataNotSent;
			this.dataToTransferNotSent = dataToTransferNotSent;
		}
	}

	@Override
	protected void liveByStep(Message _message) {
		/*
		 * if (state.compareTo(State.DISCONNECTION_IN_PROGRESS)>=0 &&
		 * !(_message.getClass()==DistKernADataToUpgradeMessage.class &&
		 * ((DistKernADataToUpgradeMessage) _message).dataToUpgrade.isLastMessage()) &&
		 * _message.getClass()!=ConnectionClosed.class) return;
		 */

		if (_message.getClass() == DistKernADataToUpgradeMessage.class) {

			AbstractPacketData d = ((DistKernADataToUpgradeMessage) _message).dataToUpgrade;
			boolean sendMessage = d.getReadDataLengthIncludingHash() == 0;
			// d.setStat(getBytesPerSecondsStat());
			if (d.needNewByteBuffer()) {

				try {
					d.setNewBlock(getTransfertType(), getBlock(d.packet, getTransfertType().getID()));
					/*
					 * if (d.needNewByteBuffer()) d.setNewBlock(getTransfertType(),
					 * getBlock(d.packet, getTransfertType().getID()));
					 */
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Data buffer updated (distant_inet_address=" + distant_inet_address
								+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
								+ ", totalDataLe) : " + d);
				} catch (NIOException e) {
					if (logger != null)
						logger.severeLog("Impossible to send packet " + d.getIDPacket(), e);
					d.cancel();
				}
				if (!sendMessage) {
					synchronized (this.nio_agent_address) {
						this.nio_agent_address.notifyAll();
					}
				}
			} else if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Sending data buffer (distant_inet_address=" + distant_inet_address
						+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + d);
			if (sendMessage) {
				if (sendMessageWithRole(nio_agent_address, new DataToSendMessage(d, getSocketID()),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS)) {
					lastDistKernADataToUpgradeMessageSentUTC = System.currentTimeMillis();
				}
			}

		} else if (_message.getClass() == SendPingMessage.class) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Sending ping message (distant_inet_address=" + distant_inet_address
						+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
			waitingPongMessage = true;
			sendData(new PingMessage(), true, false);
		} else if (_message instanceof DataReceivedMessage) {
			receiveData(((DataReceivedMessage) _message).received_data);
		} else if (_message instanceof ConnectionClosed) {
			ConnectionClosed cc = (ConnectionClosed) _message;
			deconnected(cc.reason, cc.data_not_sent, cc.bigDataNotSent, cc.dataToTransferNotSent);
		} else if (_message.getClass() == FailedCreateIndirectAgentSocket.class) {
			((FailedCreateIndirectAgentSocket) _message).executeTask();
		} else if (_message.getClass() == AskForConnectionMessage.class) {
			AskForConnectionMessage cc = (AskForConnectionMessage) _message;
			if (cc.type.equals(ConnectionStatusMessage.Type.DISCONNECT)) {
				if (cc.connection_closed_reason.equals(ConnectionClosedReason.CONNECTION_ANOMALY)) {
					if (!processExternalAnomaly(null, cc.getCandidateToBan()))
						startDeconnectionProcess(cc.connection_closed_reason);
				} else
					startDeconnectionProcess(cc.connection_closed_reason);
			}
		} else if (_message instanceof ReceivedIndirectData) {
			ReceivedIndirectData m = ((ReceivedIndirectData) _message);
			if (m.block.getTransferID() != getTransfertType().getID()) {

				InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(m.getSender(), m.block.getTransferID());
				if (idt == null || idt.getTransferToAgentAddress() == null) {
					processInvalidBlockToTransfer(m.block);
				} else {

					sendMessageWithRole(idt.getTransferToAgentAddress(), m, LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					// receiveIndirectData(m.block, idt);
				}
			} else {
				receiveData(m.block);
			}
		} else if (_message.getClass() == ResendData.class) {
			ResendData rd = ((ResendData) _message);
			InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(_message.getSender(),
					rd.block_data.getIDTransfer());

			if (idt == null) {
				processInvalidBlockToTransfer(rd.block_data.getBlock());
			} else {
				if (idt.getTransferToAgentAddress() != null) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest(
								"Resend indirect data (middle local node, distant_inet_address=" + distant_inet_address
										+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
					sendMessageWithRole(idt.getTransferToAgentAddress(), rd, LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest(
								"Resend indirect data (end local node, distant_inet_address=" + distant_inet_address
										+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
					// rd.block_data.getBlock().setTransfertID(idt.getDistantID().getID());
					sendMessageWithRole(nio_agent_address, new DataToSendMessage(rd.block_data, getSocketID()),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				}
			}
		} else if (_message instanceof ReceivedSerializableObject) {
			ReceivedSerializableObject m = (ReceivedSerializableObject) _message;
			receiveData(m.getContent(), m);
		} else if (_message.getClass() == KernelAddressValidation.class) {
			if (((KernelAddressValidation) _message).isKernelAddressInterfaceEnabled()) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer(
							"Duplicate kernel address detected, trying to authentifiate (middle local node, distant_inet_address="
									+ distant_inet_address + ", distantInterfacedKernelAddress="
									+ distantInterfacedKernelAddress + ", distant_kernel_address="
									+ distant_kernel_address + ")");

				// the received distant kernel address is already used by another connection.
				// Try to know if it these connections are part of the same peer

				// send a secret message to the distant sockets that share the same kernel
				// address.
				HashMap<AgentAddress, SecretMessage> secretMessages = new HashMap<>();
				for (AgentAddress aa : this.getAgentsWithRole(
						LocalCommunity.Groups.getOriginalDistantKernelAgentGroup(distant_kernel_address),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE)) {
					if (!aa.representsSameAgentThan(agent_for_distant_kernel_aa)) {
						SecretMessage sm = new SecretMessage(random, distant_socket_agent_address,
								agent_for_distant_kernel_aa);
						secretMessages.put(aa, sm);
						sendMessageWithRole(aa, new ObjectMessage<SecretMessage>(sm),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					}
				}
				this.currentSecretMessages.set(secretMessages);

				// add a task that will considerer after an elapsed time that the received
				// distant kernel address is not part of a same already connected peer.
				scheduleTask(new Task<>(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						if (isAlive()) {
							if (AbstractAgentSocket.this.getState().compareTo(AbstractAgent.State.ENDING) < 0)
								validateKernelAddress(AbstractAgentSocket.this.agent_for_distant_kernel_aa);
						}
						return null;
					}
				}, System.currentTimeMillis()
						+ getMadkitConfig().networkProperties.maxDurationOfDistantKernelAddressCheck));
				((NetworkBlackboard) getBlackboard(LocalCommunity.Groups.NETWORK,
						LocalCommunity.BlackBoards.NETWORK_BLACKBOARD))
								.unlockSimultaneousConnections(distant_kernel_address);
			} else {
				// The received distant kernel address is unique into this peer.
				// The kernel address is validated.
				// the kernel address is not interfaced
				currentSecretMessages.set(null);
			}
		} else if (_message.getClass() == ExceededDataQueueSize.class) {

			boolean paused = ((ExceededDataQueueSize) _message).isPaused();

			if (exceededDataQueueSize.getAndSet(paused) != paused) {
				if (logger != null) {
					if (paused) {
						if (logger.isLoggable(Level.FINEST))
							logger.finest("Exceeding data queue size (" + this.distant_inet_address + ", "
									+ distant_inet_address + ") : " + true);
					} else if (logger.isLoggable(Level.FINEST))
						logger.finest("Exceeding data queue size (" + this.distant_inet_address + ", "
								+ distant_inet_address + ") : " + false);
				}
			}
		} else if (_message.getClass() == PossibleInetAddressesUsedForDirectConnectionChanged.class) {
			PossibleInetAddressesUsedForDirectConnectionChanged m = (PossibleInetAddressesUsedForDirectConnectionChanged) _message;
			if (m.isConcernedBy(local_interface_address.getAddress())) {
				if (logger != null && logger.isLoggable(Level.FINEST))
					logger.finest("Possible inet address change (distant_inet_address=" + distant_inet_address
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");

				sendConnectionInfoSystemMessage();
			}
		} else if (_message.getClass() == CheckDeadTransferNodes.class) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Checking dead transfer nodes (distant_inet_address=" + distant_inet_address
						+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");

			transfer_ids.removeObsoleteData();
			transfer_ids_to_finalize.removeObsoleteData();
			this.removeTaskTransferCheckerIfNecessary();
		} else if (_message.getClass() == AnomalyDetectedMessage.class) {
			AnomalyDetectedMessage m = (AnomalyDetectedMessage) _message;
			if ((m.getInetSocketAddress() != null && m.getInetSocketAddress().equals(distant_inet_address))
					|| (m.getKernelAddress() != null && m.getKernelAddress().equals(distantInterfacedKernelAddress))
					|| m.getKernelAddress() == null) {
				processExternalAnomaly(m.getMessage(), m.isCandidateToBan());
			}
		} else if (_message.getClass() == ObjectMessage.class) {
			Object o = ((ObjectMessage<?>) _message).getContent();
			if (o.getClass() == NewLocalLoginAddedMessage.class) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Local login added (" + this.distant_inet_address + ", " + distant_inet_address + ")");

				receiveData(o, null);
			} else if (o.getClass() == SecretMessage.class) {
				if (logger != null && logger.isLoggable(Level.FINEST))
					logger.finest("Send secret message (distant_inet_address=" + distant_inet_address
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
				SecretMessage sm = (SecretMessage) o;
				sm.removeAgentSocketAddress();
				sendData(sm, true, false);
			} else if (o.getClass() == StatsBandwidth.class) {
				if (logger != null && logger.isLoggable(Level.FINEST))
					logger.finest("Updating statistics for distant kernel address (distant_inet_address="
							+ distant_inet_address + ", distantInterfacedKernelAddress="
							+ distantInterfacedKernelAddress + ")");
				getStatistics().putStateForDistantKernelAddress((StatsBandwidth) o);
			} else if (o.getClass() == KernelAddressInterfaced.class) {
				// requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup((KernelAddressInterfaced)o),
				// LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				leaveRole(LocalCommunity.Groups.getDistantKernelAgentGroup(agent_for_distant_kernel_aa),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);

				if (!requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(_message.getSender()),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS) && logger != null)
					logger.severe("Cannot request group "
							+ LocalCommunity.Groups.getDistantKernelAgentGroup(_message.getSender()) + " and role "
							+ LocalCommunity.Roles.SOCKET_AGENT_ROLE);

				// Collection<AgentAddress>
				// c=getAgentsWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup((KernelAddressInterfaced)o),
				// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				Collection<AgentAddress> c = getAgentsWithRole(
						LocalCommunity.Groups.getDistantKernelAgentGroup(_message.getSender()),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

				AgentAddress aa = null;
				if (c.size() == 1)
					aa = c.iterator().next();
				else if (logger != null) {
					if (c.size() == 0)
						logger.severe("Distant kernel agent not found : " + o);
					else
						logger.severe("Distant kernel agent duplicated : " + o);
				}
				if (aa != null) {
					distant_kernel_agent_activated = true;
					agent_for_distant_kernel_aa = aa;
					distantInterfacedKernelAddress = (KernelAddressInterfaced) o;
					sendData(new DistantKernelAddressValidated(), true, false);
					if (this.distantKernelAddressValidated.get()) {
						sendMessageWithRole(this.agent_for_distant_kernel_aa,
								new ObjectMessage<>(new DistantKernelAddressValidated()),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						informHooksForConnectionEstablished();
					}
				} else {
					this.startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
				}
			} else if (o.getClass() == InitiateTransferConnection.class) {
				InitiateTransferConnection candidate = (InitiateTransferConnection) o;

				initiateTransferConnection(_message.getSender(), candidate);
			} else if (o.getClass() == DataToBroadcast.class) {
				DataToBroadcast m = (DataToBroadcast) o;
				if (logger != null)
					logger.finest("broadcasting undirect data (distant_inet_address=" + distant_inet_address
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : "
							+ m.getTransferID());

				/*
				 * if(m.getMessageToBroadcast().getIdTransferDestination().equals(
				 * getTransfertType())) { receiveData(m, null); } else {
				 */
				receiveBroadcastData(_message.getSender(), m, false);
				// }
			} else if (o.getClass() == TransferConfirmationSystemMessage.class) {
				TransferConfirmationSystemMessage t = (TransferConfirmationSystemMessage) o;
				broadcastPropositionAnwser(t);
			} else if (o.getClass() == TransferImpossibleSystemMessageFromMiddlePeer.class) {
				TransferImpossibleSystemMessageFromMiddlePeer t = (TransferImpossibleSystemMessageFromMiddlePeer) o;
				broadcastPropositionImpossibleAnwser(t);
			} else if (o.getClass() == TransferClosedSystemMessage.class) {
				TransferClosedSystemMessage t = (TransferClosedSystemMessage) o;
				if (logger != null && logger.isLoggable(Level.FINEST))
					logger.finest("closing undirect connection (distant_inet_address=" + distant_inet_address
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : "
							+ t.getIdTransfer());
				t = new TransferClosedSystemMessage(getTransfertType(), t.getKernelAddressDestination(),
						t.getIdTransfer(), t.isLastPass());

				if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
					InterfacedIDTransfer idt = transfer_ids.getLocal(t.getIdTransfer());
					if (idt != null) {
						idt = transfer_ids.getDistant(idt.getDistantID());
						AgentAddress aa = idt.getTransferToAgentAddress();
						if (aa == null)
							processInvalidTransferConnectionProcotol("Unexpected exception");
						else
							receiveTransferClosedSystemMessage(aa, t, getKernelAddress(), true, true);
					}
				} else {
					InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(
							((IndirectAgentSocket) this).getParentAgentSocketAddress(), t.getIdTransfer());
					if (idt != null) {
						AgentAddress aa = idt.getTransferToAgentAddress();
						if (aa == null)
							processInvalidTransferConnectionProcotol("Unexpected exception 2");
						else
							receiveTransferClosedSystemMessage(aa, t, getKernelAddress(), true, true);
					}
				}
			} else if (o.getClass() == TryDirectConnection.class) {
				sendData((TryDirectConnection) o, true, false);
			} else if (o.getClass() == DirectConnectionFailed.class) {
				sendData((DirectConnectionFailed) o, true, false);
			} else if (o.getClass() == DirectConnectionSuceeded.class) {
				sendData((DirectConnectionSuceeded) o, true, false);
			} else if (o.getClass() == TooMuchConnectionWithTheSamePeers.class) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer(
							"Too much connections with the same peers (distant_inet_address=" + distant_inet_address
									+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
				sendData((TooMuchConnectionWithTheSamePeers) o, true, true);
			} else if (o instanceof Runnable) {
				((Runnable) o).run();
			}
		}
	}

	private void sendConnectionInfoSystemMessage() {
		ArrayList<InetAddress> ias = new ArrayList<>();
		MultipleIP mip = null;
		try {
			for (Enumeration<InetAddress> it = NetworkInterface.getByInetAddress(local_interface_address.getAddress())
					.getInetAddresses(); it.hasMoreElements();) {
				InetAddress ia = it.nextElement();
				if (getMadkitConfig().networkProperties.needsServerSocket(new InetSocketAddress(ia,
						getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections))) {
					ias.add(ia);
				}
			}
		} catch (Exception e) {
			logger.severeLog("Unexpected exception", e);
		}
		if (!ias.isEmpty())
			mip = new MultipleIP(getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections, ias);
		ConnectionInfoSystemMessage ci = new ConnectionInfoSystemMessage(
				getMadkitConfig().networkProperties.getPossibleAddressesForDirectConnection(),
				this.local_interface_address.getAddress(),
				getMadkitConfig().networkProperties.portsToBindForManualDirectConnections,
				getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections,
				this.getMadkitConfig().networkProperties.needsServerSocket(local_interface_address), mip);
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Sending connection information to distant peer (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + ci);

		sendData(ci, true, false);
	}

	private void initiateTransferConnection(AgentAddress transferAgentAddress, InitiateTransferConnection candidate) {
		if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
			// putInterfacedIDTransferToFinalize(null, new
			// InterfacedIDTransfer(candidate.getIdTransfer(), candidate.getAgentAddress(),
			// candidate.getKernelAddress()));
			transfer_ids_to_finalize.putLocal(new InterfacedIDTransfer(candidate.getIdTransfer(),
					candidate.getAgentAddress(), candidate.getKernelAddress()));
			// putInterfacedIDTransferToFinalize(candidate.getAgentAddress(), new
			// InterfacedIDTransfer(candidate.getIdTransfer(), null,
			// distant_kernel_address));
			// this.transfer_ids_to_finalize.putLocal(candidate.getIdTransfer());

			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Initiate transfer connection (end local node, distant_inet_address="
						+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
						+ ") : " + candidate);

		} else {
			putInterfacedIDTransferToFinalize(((IndirectAgentSocket) this).getParentAgentSocketAddress(),
					new InterfacedIDTransfer(candidate.getIdTransfer(), candidate.getAgentAddress(),
							candidate.getKernelAddress()));
			putInterfacedIDTransferToFinalize(candidate.getAgentAddress(),
					new InterfacedIDTransfer(candidate.getIdTransfer(),
							((IndirectAgentSocket) this).getParentAgentSocketAddress(), distant_kernel_address));
			// this.transfer_ids_to_finalize.putMiddle(((IndirectAgentSocket)this).getParentAgentSocketAddress(),
			// candidate.getIdTransfer());
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Initiate transfer connection (intermediate local node, distant_inet_address="
						+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
						+ ") : " + candidate);
		}

		this.transfer_ids_to_finalize.putTransferAgentAddress(candidate.getIdTransfer(), transferAgentAddress);
		broadcastDataTowardEachIntermediatePeer(new TransferPropositionSystemMessage(getTransfertType(),
				candidate.getIdTransfer(), candidate.getKernelAddress(), this.distant_kernel_address,
				candidate.getNumberOfIntermediatePeers(), candidate.getOriginalMessage().getAttachedData(),
				candidate.isYouAskConnection()), true);
	}

	protected abstract int getNumberOfIntermediatePeers();

	protected InterfacedIDTransfer getInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender,
			int idTransfer) {
		if (sender != null) {
			return transfer_ids.getLocal(idTransfer);
		} else
			return transfer_ids.getDistant(idTransfer);
	}

	protected final InterfacedIDTransfer getValidatedInterfacedIDTransfer(AgentAddress sender, IDTransfer idTransfer) {
		return getInterfacedIDTransfer(transfer_ids, sender, idTransfer.getID());
	}

	protected final InterfacedIDTransfer getValidatedInterfacedIDTransfer(AgentAddress sender, int idTransfer) {
		return getInterfacedIDTransfer(transfer_ids, sender, idTransfer);
	}

	protected final InterfacedIDTransfer getInterfacedIDTransferToFinalize(AgentAddress sender, IDTransfer idTransfer) {
		return getInterfacedIDTransfer(transfer_ids_to_finalize, sender, idTransfer.getID());
	}

	protected final InterfacedIDTransfer getInterfacedIDTransferToFinalize(AgentAddress sender, int idTransfer) {
		return getInterfacedIDTransfer(transfer_ids_to_finalize, sender, idTransfer);
	}

	protected void putInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender, InterfacedIDTransfer idt) {
		if (sender != null)
			transfer_ids.putLocal(idt);
		else
			transfer_ids.putDistant(idt);
	}

	protected final void putValidatedInterfacedIDTransfer(AgentAddress sender, InterfacedIDTransfer idt) {
		putInterfacedIDTransfer(transfer_ids, sender, idt);
	}

	protected final void putInterfacedIDTransferToFinalize(AgentAddress sender, InterfacedIDTransfer idt) {
		putInterfacedIDTransfer(transfer_ids_to_finalize, sender, idt);
	}

	protected void validateInterfacedIDTransfer(AgentAddress sender, IDTransfer id, boolean forceLocalID) {
		InterfacedIDTransfer idt = null;
		if (sender != null || forceLocalID) {
			idt = this.transfer_ids_to_finalize.removeLocal(id);

			this.transfer_ids.putLocal(idt);
			if (idt.getDistantID() != null)
				this.transfer_ids.putDistant(this.transfer_ids_to_finalize.removeDistant(idt.getDistantID()));
		} else {
			idt = this.transfer_ids_to_finalize.removeDistant(id);
			this.transfer_ids.putDistant(idt);
			InterfacedIDTransfer idtLocal = this.transfer_ids_to_finalize.removeLocal(idt.getLocalID());
			if (idtLocal != null)
				this.transfer_ids.putLocal(idtLocal);
		}
		TransferPropositionSystemMessage tp = this.transfer_ids_to_finalize
				.removeTransferPropositionSystemMessage(idt.getLocalID());
		if (tp != null)
			this.transfer_ids.putTransferPropositionSystemMessage(idt.getLocalID(), tp);
		AgentAddress aa = this.transfer_ids_to_finalize.removeTransferAgentAddress(idt.getLocalID());
		if (aa != null)
			this.transfer_ids.putTransferAgentAddress(idt.getLocalID(), aa);
	}

	protected final InterfacedIDTransfer removeValidatedInterfacedIDTransfer(AgentAddress sender, IDTransfer id) {
		return removeValidatedInterfacedIDTransfer(sender, id, false);
	}

	protected final InterfacedIDTransfer removeValidatedInterfacedIDTransfer(AgentAddress sender, IDTransfer id,
			boolean forceLocal) {
		return removeInterfacedIDTransfer(transfer_ids, sender, id, forceLocal);
	}

	protected final InterfacedIDTransfer removeInterfacedIDTransferToFinalize(AgentAddress sender, IDTransfer id) {
		return removeInterfacedIDTransferToFinalize(sender, id, false);
	}

	protected final InterfacedIDTransfer removeInterfacedIDTransferToFinalize(AgentAddress sender, IDTransfer id,
			boolean forceLocal) {
		return removeInterfacedIDTransfer(transfer_ids_to_finalize, sender, id, forceLocal);
	}

	protected InterfacedIDTransfer removeInterfacedIDTransfer(TransferIDs transfer_ids, AgentAddress sender,
			IDTransfer id, boolean forceLocal) {
		InterfacedIDTransfer idt = null;
		if (sender != null || forceLocal) {
			idt = transfer_ids.removeLocal(id);
			if (idt.getDistantID() != null)
				transfer_ids.removeDistant(idt.getDistantID());
		} else {
			idt = transfer_ids.removeDistant(id);
			transfer_ids.removeLocal(idt.getLocalID());
		}
		transfer_ids.removeTransferPropositionSystemMessage(idt.getLocalID());
		transfer_ids.removeTransferAgentAddress(idt.getLocalID());
		getMadkitConfig().networkProperties.removeStatsBandwitdh(idt.getLocalID().getID());
		getStatistics().removeTransferAgentStats(idt.getLocalID());

		return idt;
	}

	private void receiveBroadcastData(AgentAddress sender, DataToBroadcast d, boolean justReceivedFromNetwork) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest(
					"Receiving broadcast data (sender=" + sender + ", distant_inet_address=" + distant_inet_address
							+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + d);

		if (d.getMessageToBroadcast().getClass() == TransferPropositionSystemMessage.class) {
			TransferPropositionSystemMessage t = (TransferPropositionSystemMessage) d.getMessageToBroadcast();

			if (t.getIdTransferDestination().equals(this.getTransfertType())
					&& getKernelAddress().equals(t.getKernelAddressDestination())) {
				receiveTransferProposition(sender, t);
			} else {
				if (justReceivedFromNetwork) {
					TransferFilter tf = getMadkitConfig().networkProperties.getTransferTriggers();
					if (tf == null || tf.newTransferConnectionPropositionToIntermediatePeers(
							getMadkitConfig().networkProperties, t.getKernelAddressDestination(),
							t.getKernelAddressToConnect(), t.getNumberOfIntermediatePeers(),
							t.getAttachedDataForConnection())) {
						InterfacedIDTransfer idDest = getValidatedInterfacedIDTransfer(sender,
								t.getIdTransferDestination());
						if (idDest == null) {
							processInvalidTransferConnectionProcotol(
									"Received a message to broadcast trough each transfer node, but impossible to found the correspondantant TransferID "
											+ t.getIdTransferDestination() + " in the recieving router agent "
											+ this.toString());
						} else {

							try {
								IDTransfer id = IDTransfer
										.generateIDTransfer(MadkitKernelAccess.getIDTransferGenerator(this));
								InterfacedIDTransfer interfacedIDTransfer = new InterfacedIDTransfer(id,
										idDest.getTransferToAgentAddress(), t.getKernelAddressDestination());
								interfacedIDTransfer.setDistantID(t.getIdTransfer());
								putInterfacedIDTransferToFinalize(sender, interfacedIDTransfer);

								interfacedIDTransfer = new InterfacedIDTransfer(id, null,
										t.getKernelAddressToConnect());
								interfacedIDTransfer.setDistantID(t.getIdTransfer());

								putInterfacedIDTransferToFinalize(idDest.getTransferToAgentAddress(),
										interfacedIDTransfer);

								TransferPropositionSystemMessage t2 = new TransferPropositionSystemMessage(
										idDest.getLocalID(), interfacedIDTransfer.getLocalID(),
										t.getKernelAddressToConnect(), t.getKernelAddressDestination(),
										t.getNumberOfIntermediatePeers(), t.getAttachedDataForConnection(),
										t.isYouAskConnection());
								broadcastDataTowardEachIntermediatePeer(sender, t2, t.getIdTransferDestination(), d);
							} catch (OverflowException e) {
								TransferImpossibleSystemMessage t2 = new TransferImpossibleSystemMessage(
										idDest.getLocalID(), d.getSender(), t.getIdTransfer());
								t2.setMessageLocker(t.getMessageLocker());
								broadcastDataTowardEachIntermediatePeer(t2, true);
								if (logger != null)
									logger.severeLog("Too much transfer connections", e);
							}
						}
					}
				} else {

					InterfacedIDTransfer idLocal = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());
					if (idLocal == null) {
						processInvalidTransferConnectionProcotol(
								"Received a message to broadcase through eacg transfer node, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the moddile router agent "
										+ this.toString());
					} else {
						InterfacedIDTransfer interfacedIDTransfer = new InterfacedIDTransfer(t.getIdTransfer(),
								idLocal.getTransferToAgentAddress(), t.getKernelAddressDestination());// TODO check
																										// transfer to
																										// agent

						putInterfacedIDTransferToFinalize(sender, interfacedIDTransfer);
						broadcastDataTowardEachIntermediatePeer(sender, t, t.getIdTransferDestination(), d);
					}
				}
			}
		} else if (d.getMessageToBroadcast().getClass() == TransferImpossibleSystemMessage.class) {

			TransferImpossibleSystemMessage t = (TransferImpossibleSystemMessage) d.getMessageToBroadcast();

			if ((t.getIdTransferDestination() == getTransfertType()
					|| t.getIdTransferDestination().equals(this.getTransfertType()))
					&& getKernelAddress().equals(t.getKernelAddressDestination())) {
				receiveTransferImpossibleAnwser(sender, t);
			} else {
				if (justReceivedFromNetwork) {
					InterfacedIDTransfer idDist = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());
					if (idDist == null) {
						processInvalidTransferConnectionProcotol(
								"Received a message to broadcast trough each transfer node, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the recieving router agent "
										+ this.toString());
					} else {
						InterfacedIDTransfer idRemoved = removeInterfacedIDTransferToFinalize(sender,
								t.getYourIDTransfer(), true);

						if (idRemoved == null) {
							processInvalidTransferConnectionProcotol(
									"Received TransferImpossibleSystemMessage, but impossible to found the correspondant TransferID "
											+ t.getYourIDTransfer() + " in the recieving router agent "
											+ this.toString());
						} else {
							TransferImpossibleSystemMessage ti = new TransferImpossibleSystemMessage(
									idDist.getLocalID(), t.getKernelAddressDestination(), t.getYourIDTransfer());
							ti.setMessageLocker(t.getMessageLocker());
							broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
						}
					}
				} else {

					InterfacedIDTransfer idRemoved = removeInterfacedIDTransferToFinalize(sender, t.getYourIDTransfer(),
							true);
					if (idRemoved == null) {
						processInvalidTransferConnectionProcotol(
								"Received TransferImpossibleSystemMessage, but impossible to found the correspondant TransferID "
										+ t.getYourIDTransfer() + " in the sender router agent " + this.toString());
					} else {
						TransferImpossibleSystemMessage ti = new TransferImpossibleSystemMessage(
								t.getIdTransferDestination(), t.getKernelAddressDestination(),
								getTransfertType().equals(TransferAgent.NullIDTransfer) ? idRemoved.getLocalID()
										: idRemoved.getDistantID());
						ti.setMessageLocker(t.getMessageLocker());
						broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
					}
				}
			}
		} else if (d.getMessageToBroadcast().getClass() == TransferImpossibleSystemMessageFromMiddlePeer.class) {
			TransferImpossibleSystemMessageFromMiddlePeer t = (TransferImpossibleSystemMessageFromMiddlePeer) d
					.getMessageToBroadcast();

			if ((t.getIdTransferDestination() == getTransfertType()
					|| t.getIdTransferDestination().equals(this.getTransfertType()))
					&& getKernelAddress().equals(t.getKernelAddressDestination())) {
				receiveTransferImpossibleAnwser(sender, t);
			} else {
				if (justReceivedFromNetwork) {
					InterfacedIDTransfer idDist = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());
					if (idDist == null) {
						processInvalidTransferConnectionProcotol(
								"Received a message to broadcast trough each transfer node, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the recieving router agent "
										+ this.toString());
					} else {
						InterfacedIDTransfer idRemoved = removeValidatedInterfacedIDTransfer(sender,
								t.getYourIDTransfer(), true);
						if (idRemoved == null)
							idRemoved = removeInterfacedIDTransferToFinalize(sender, t.getYourIDTransfer(), true);
						if (idRemoved != null) {
							TransferImpossibleSystemMessageFromMiddlePeer ti = new TransferImpossibleSystemMessageFromMiddlePeer(
									idDist.getLocalID(), t.getKernelAddressDestination(), t.getYourIDTransfer(),
									t.getYourIDTransfer());
							ti.setMessageLocker(t.getMessageLocker());
							broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
							// sendMessageWithRole(idDist.getTransferToAgentAddress(), new
							// ObjectMessage<DataToBroadcast>(new DataToBroadcast(ti, d.getSender(),
							// d.isPrioritary(), ti.getIdTransferDestination())),
							// LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						} else
							processInvalidTransferConnectionProcotol(
									"Received TransferImpossibleSystemMessage, but impossible to found the correspondant TransferID "
											+ t.getYourIDTransfer() + " in the sender router agent " + this.toString());
					}
				} else {
					InterfacedIDTransfer idLocal = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());

					InterfacedIDTransfer idRemoved = removeValidatedInterfacedIDTransfer(sender, t.getYourIDTransfer(),
							true);
					if (idRemoved == null)
						idRemoved = removeInterfacedIDTransferToFinalize(sender, t.getYourIDTransfer(), true);
					if (idRemoved == null) {
						processInvalidTransferConnectionProcotol(
								"Received TransferImpossibleSystemMessage, but impossible to found the correspondant TransferID "
										+ t.getYourIDTransfer() + " in the sender router agent " + this.toString());
					} else {
						TransferImpossibleSystemMessageFromMiddlePeer ti = new TransferImpossibleSystemMessageFromMiddlePeer(
								idLocal.getLocalID(), t.getKernelAddressDestination(), idRemoved.getDistantID(),
								idRemoved.getLocalID());
						ti.setMessageLocker(t.getMessageLocker());
						broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
						// broadcastDataTowardEachIntermediatePeer(ti, d.isPrioritary());
					}
				}
			}
		} else if (d.getMessageToBroadcast().getClass() == TransferConfirmationSystemMessage.class) {
			TransferConfirmationSystemMessage t = (TransferConfirmationSystemMessage) d.getMessageToBroadcast();

			if ((t.getIdTransferDestination() == getTransfertType()
					|| t.getIdTransferDestination().equals(this.getTransfertType()))
					&& getKernelAddress().equals(t.getKernelAddressDestination())) {
				receiveTransferConfirmationAnwser(sender, t);
			} else {
				if (justReceivedFromNetwork) {
					InterfacedIDTransfer idDist = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());
					if (idDist == null) {
						processInvalidTransferConnectionProcotol(
								"Received a message to broadcast trough each transfer node, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the recieving router agent "
										+ this.toString());
					} else {

						if (t.isMiddleReached()) {
							validateInterfacedIDTransfer(sender, t.getMyIDTransfer(), false);

							InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(sender, t.getMyIDTransfer());
							if (!t.getKernelAddressDestination().equals(getKernelAddress()))
								idt.setTransferBlockChecker(new ConnectionProtocol.NullBlockChecker(
										t.getNumberOfSubBlocks(), false, (short) 0));

							TransferConfirmationSystemMessage ti = new TransferConfirmationSystemMessage(
									idDist.getLocalID(), t.getKernelAddressDestination(), t.getKernelAddressToConnect(),
									t.getYourIDTransfer(), t.getYourIDTransfer(), t.getNumberOfSubBlocks(), true,
									t.getDistantInetAddress());
							ti.setMessageLocker(t.getMessageLocker());
							broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
						} else {
							InterfacedIDTransfer idt = new InterfacedIDTransfer(t.getYourIDTransfer(),
									idDist.getTransferToAgentAddress(), t.getKernelAddressDestination());
							idt.setDistantID(t.getMyIDTransfer());

							idt.setTransferBlockChecker(new ConnectionProtocol.NullBlockChecker(
									t.getNumberOfSubBlocks(), false, (short) 0));
							putInterfacedIDTransferToFinalize(sender, idt);
							getInterfacedIDTransferToFinalize(idDist.getTransferToAgentAddress(), idt.getLocalID())
									.setDistantID(t.getMyIDTransfer());

							TransferConfirmationSystemMessage ti = new TransferConfirmationSystemMessage(
									idDist.getLocalID(), t.getKernelAddressDestination(), t.getKernelAddressToConnect(),
									t.getYourIDTransfer(), t.getYourIDTransfer(), t.getNumberOfSubBlocks(), false,
									t.getDistantInetAddress());
							ti.setMessageLocker(t.getMessageLocker());

							broadcastDataTowardEachIntermediatePeer(sender, ti, t.getIdTransferDestination(), d);
						}
						getStatistics().putTransferAgentStats(t.getYourIDTransfer(), getMadkitConfig().networkProperties
								.addIfNecessaryAndGetStatsBandwitdh(idDist.getLocalID().getID()));

					}

				} else {
					InterfacedIDTransfer idLocal = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());
					if (idLocal == null) {
						removeInterfacedIDTransferToFinalize(sender, t.getYourIDTransfer());
						processInvalidTransferConnectionProcotol(
								"Received TransferConfirmationSystemMessage to transfer for internal use 1, but impossible to found the correspondant TransferID "
										+ t.getYourIDTransfer() + " in the moddile router agent " + this.toString());
					} else {
						if (t.isMiddleReached()) {
							validateInterfacedIDTransfer(sender, t.getYourIDTransfer(), false);

							if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
								InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(sender,
										t.getYourIDTransfer());
								TransferConfirmationSystemMessage ti = new TransferConfirmationSystemMessage(
										t.getIdTransferDestination(), t.getKernelAddressDestination(),
										t.getKernelAddressToConnect(), idt.getDistantID(), idt.getLocalID(),
										t.getNumberOfSubBlocks(), true, t.getDistantInetAddress());
								ti.setMessageLocker(t.getMessageLocker());
								t = ti;
							}
						} else {
							if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
								InterfacedIDTransfer idt = getInterfacedIDTransferToFinalize(sender,
										t.getYourIDTransfer());
								TransferConfirmationSystemMessage ti = new TransferConfirmationSystemMessage(
										t.getIdTransferDestination(), t.getKernelAddressDestination(),
										t.getKernelAddressToConnect(), idt.getDistantID(), idt.getLocalID(),
										t.getNumberOfSubBlocks(), false, t.getDistantInetAddress());
								ti.setMessageLocker(t.getMessageLocker());
								t = ti;
							} else {
								InterfacedIDTransfer idt = new InterfacedIDTransfer(t.getYourIDTransfer(),
										idLocal.getTransferToAgentAddress(), t.getKernelAddressDestination());
								putInterfacedIDTransferToFinalize(sender, idt);
							}
						}
						broadcastDataTowardEachIntermediatePeer(sender, t, t.getIdTransferDestination(), d);
					}
				}
			}
		} else if (d.getMessageToBroadcast().getClass() == TransferBlockCheckerSystemMessage.class) {
			TransferBlockCheckerSystemMessage t = (TransferBlockCheckerSystemMessage) d.getMessageToBroadcast();

			if (!t.getIdTransferDestination().equals(this.getTransfertType())
					|| !getKernelAddress().equals(t.getKernelAddressDestination())) {

				if (justReceivedFromNetwork) {
					InterfacedIDTransfer idDist = getValidatedInterfacedIDTransfer(sender,
							t.getIdTransferDestination());

					InterfacedIDTransfer idLocal = null;
					if (idDist == null || (idLocal = this.transfer_ids.getLocal(idDist.getLocalID())) == null) {
						processInvalidTransferConnectionProcotol(
								"Received a message to broadcast trough each transfer node, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the recieving router agent "
										+ this.toString());
					} else {
						if (!t.getKernelAddressDestination().equals(getKernelAddress())) {
							idDist.setTransferBlockChecker(t.getTransferBlockChercker());
							idLocal.setTransferBlockChecker(t.getTransferBlockChercker());
							TransferBlockCheckerSystemMessage tn = new TransferBlockCheckerSystemMessage(
									idLocal.getLocalID(), t.getKernelAddressDestination(),
									t.getTransferBlockChercker());
							tn.setMessageLocker(t.getMessageLocker());

							broadcastDataTowardEachIntermediatePeer(sender, tn, t.getIdTransferDestination(), d);
						}
					}
				} else {
					ReturnCode rc = broadcastDataTowardEachIntermediatePeer(sender, t, t.getIdTransferDestination(), d);
					if (!rc.equals(ReturnCode.SUCCESS) && !rc.equals(ReturnCode.TRANSFER_IN_PROGRESS))
						processInvalidTransferConnectionProcotol(
								"Received TransferBlockCheckerSystemMessage, but impossible to found the correspondant TransferID "
										+ t.getIdTransferDestination() + " in the moddile router agent "
										+ this.toString());

				}
			}
		} else if (d.getMessageToBroadcast().getClass() == TransferClosedSystemMessage.class) {
			TransferClosedSystemMessage t = (TransferClosedSystemMessage) d.getMessageToBroadcast();

			if (t.getIdTransfer().equals(this.getTransfertType())
					&& getKernelAddress().equals(t.getKernelAddressDestination())
					&& !t.getIdTransfer().equals(TransferAgent.NullIDTransfer)) {
				transfer_ids.closeAllTransferID(!t.isLastPass());
				transfer_ids_to_finalize.closeAllTransferID(!t.isLastPass());
				if (!t.isLastPass()) {
					TransferClosedSystemMessage t2 = new TransferClosedSystemMessage(getTransfertType(),
							distant_kernel_address, getTransfertType(), true);
					t2.setMessageLocker(new MessageLocker(null));
					broadcastDataTowardEachIntermediatePeer(t2, true);
				}

				startDeconnectionProcess(ConnectionClosedReason.CONNECTION_LOST);
			} else {
				receiveTransferClosedSystemMessage(sender, t, d.getSender(), d.isPrioritary(), false);
			}
		}
	}

	protected void receiveTransferClosedSystemMessage(final AgentAddress sender, TransferClosedSystemMessage t,
			KernelAddress kaSender, boolean isPrioritary, boolean fromTransferAgent) {
		IDTransfer id = null;
		{

			InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(sender, t.getIdTransferDestination());

			if (idt == null && !t.getIdTransferDestination().equals(TransferAgent.NullIDTransfer)) {
				processInvalidTransferConnectionProcotol(
						"Received a message to broadcast trough each transfer node (1), but impossible to found the correspondant TransferID "
								+ t.getIdTransferDestination() + " in the recieving router agent " + this.toString());
				return;
			}
			id = idt == null ? t.getIdTransferDestination() : idt.getLocalID();
		}

		InterfacedIDTransfer idtToClose = getValidatedInterfacedIDTransfer(sender, t.getIdTransfer());
		AgentAddress transferAA = null;
		if (idtToClose == null) {
			idtToClose = getInterfacedIDTransferToFinalize(sender, t.getIdTransfer());
			if (idtToClose != null) {
				if (t.isLastPass()) {
					transferAA = transfer_ids_to_finalize.removeTransferAgentAddress(idtToClose.getLocalID());
				} else
					transferAA = transfer_ids_to_finalize.getTransferAgentAddress(idtToClose.getLocalID());
			}
		} else {
			if (t.isLastPass())
				transferAA = transfer_ids.removeTransferAgentAddress(idtToClose.getLocalID());
			else
				transferAA = transfer_ids.getTransferAgentAddress(idtToClose.getLocalID());
		}

		boolean destinationReached = t.getIdTransferDestination().equals(getTransfertType())
				&& t.getKernelAddressDestination().equals(getKernelAddress());
		if (idtToClose == null && !destinationReached) {
			// processInvalidTransferConnectionProcotol("Received a message to broadcast
			// trough each transfer node (2), but impossible to found the correspondant
			// Indirect TransferID "+t.getIdTransfer()+" in the recieving router agent
			// "+this.toString());
			return;
		}
		if (transferAA == null || fromTransferAgent) {
			if (destinationReached) {
				if (idtToClose != null && idtToClose.getLocalID().getID() != getTransfertType().getID()) {
					TransferClosedSystemMessage t2 = new TransferClosedSystemMessage(idtToClose.getLocalID(),
							t.getKernelAddressDestination(), idtToClose.getLocalID(), t.isLastPass());
					sendMessageWithRole(idtToClose.getTransferToAgentAddress(),
							new ObjectMessage<>(
									new DataToBroadcast(t2, kaSender, isPrioritary, idtToClose.getLocalID())),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else
					return;
			} else {
				if (sender == null) {
					TransferClosedSystemMessage t2 = new TransferClosedSystemMessage(id,
							t.getKernelAddressDestination(), idtToClose.getLocalID(), t.isLastPass());
					broadcastDataTowardEachIntermediatePeer(sender, t2, t.getIdTransferDestination(), kaSender,
							isPrioritary);
				} else {
					broadcastDataTowardEachIntermediatePeer(sender, t, t.getIdTransferDestination(), kaSender,
							isPrioritary);
				}
			}
		} else {
			TransferClosedSystemMessage t2 = new TransferClosedSystemMessage(id, getKernelAddress(),
					idtToClose.getLocalID(), t.isLastPass());
			sendMessageWithRole(transferAA, new ObjectMessage<>(t2), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}
		if (t.isLastPass()) {
			final IDTransfer idfinal = t.getIdTransfer();
			scheduleTask(new Task<>(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (isAlive()) {
						receiveMessage(new ObjectMessage<>(new Runnable() {

							@Override
							public void run() {
								InterfacedIDTransfer idt = removeValidatedInterfacedIDTransfer(sender, idfinal);
								if (idt == null)
									removeInterfacedIDTransferToFinalize(sender, idfinal);
							}
						}));
					}
					return null;
				}
			}, getMadkitConfig().networkProperties.connectionTimeOut));

		}

	}

	private void receiveTransferProposition(AgentAddress sender, TransferPropositionSystemMessage p) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Receiving receiveTransferProposition (sender=" + sender + ", distant_inet_address="
					+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
					+ ") : " + p);

		TransferFilter tf = getMadkitConfig().networkProperties.getTransferTriggers();
		p.addIntermediateTestResult(tf == null || tf.newTransferConnectionPropositionToFinalPeers(
				getMadkitConfig().networkProperties, p.getKernelAddressToConnect(), p.getNumberOfIntermediatePeers(),
				p.getAttachedDataForConnection()));
		if (p.getFinalTestResult() && getMadkitConfig().networkProperties
				.isConnectionPossible(this.distant_inet_address, this.local_interface_address, true, true, true)) {
			try {
				InterfacedIDTransfer idDist = new InterfacedIDTransfer(
						sender == null ? IDTransfer.generateIDTransfer(MadkitKernelAccess.getIDTransferGenerator(this))
								: p.getIdTransfer(),
						null, getKernelAddress());
				idDist.setDistantID(p.getIdTransfer());
				putInterfacedIDTransferToFinalize(sender, idDist);

				this.transfer_ids_to_finalize.putTransferPropositionSystemMessage(idDist, p);

				TransferConfirmationSystemMessage tcsm = new TransferConfirmationSystemMessage(getTransfertType(),
						this.distant_kernel_address, getKernelAddress(), idDist.getDistantID(), idDist.getLocalID(),
						getMadkitConfig().networkProperties.getConnectionProtocolProperties(distant_inet_address,
								local_interface_address, true, true).getNumberOfSubConnectionProtocols(),
						false, null);
				tcsm.setMessageLocker(p.getMessageLocker());
				broadcastDataTowardEachIntermediatePeer(tcsm, true);
			} catch (OverflowException | NIOException e) {
				TransferImpossibleSystemMessage ti = new TransferImpossibleSystemMessage(getTransfertType(),
						distant_kernel_address, p.getIdTransfer());
				ti.setMessageLocker(p.getMessageLocker());
				broadcastDataTowardEachIntermediatePeer(ti, true);

				if (logger != null)
					logger.severeLog("Too much transfer connections", e);
			}

		} else {
			TransferImpossibleSystemMessage ti = new TransferImpossibleSystemMessage(getTransfertType(),
					distant_kernel_address, p.getIdTransfer());
			ti.setMessageLocker(p.getMessageLocker());
			broadcastDataTowardEachIntermediatePeer(ti, true);
		}
	}

	private void receiveTransferImpossibleAnwser(AgentAddress sender, TransferImpossibleSystemMessage ti) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Receiving transfer impossible as anwser (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + ti);
		AgentAddress aat = transfer_ids_to_finalize.getTransferAgentAddress(ti.getYourIDTransfer());
		removeInterfacedIDTransferToFinalize(sender, ti.getYourIDTransfer());

		if (aat == null) {
			processInvalidTransferConnectionProcotol(
					"Received TransferImpossibleSystemMessage, but impossible to found the correspondant TransferID "
							+ ti.getYourIDTransfer() + " in the peer agent " + this.toString());
		} else {
			sendMessageWithRole(aat, new ObjectMessage<>(ti), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}
	}

	private void receiveTransferImpossibleAnwser(AgentAddress sender,
			TransferImpossibleSystemMessageFromMiddlePeer ti) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Receiving transfer impossible as anwser from middle peer (distant_inet_address="
					+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
					+ ") : " + ti);
		AgentAddress aat = transfer_ids_to_finalize.getTransferAgentAddress(ti.getYourIDTransfer());

		removeInterfacedIDTransferToFinalize(sender, ti.getYourIDTransfer());

		if (aat == null) {
			processInvalidTransferConnectionProcotol(
					"Received TransferImpossibleSystemMessageFromMiddlePeer, but impossible to found the correspondant TransferID "
							+ ti.getYourIDTransfer() + " in the peer agent " + this.toString());
		} else {
			sendMessageWithRole(aat, new ObjectMessage<>(ti), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}
	}

	class FailedCreateIndirectAgentSocket extends Message {
		/**
		 * 
		 */
		private static final long serialVersionUID = -743854093128636515L;

		private final Exception e;
		private final InterfacedIDTransfer idLocal;
		private final InterfacedIDTransfer idDist;
		private final IndirectAgentSocket indirectAgentSocket;
		private final DistantKernelAgent dka;
		private final TransferConfirmationSystemMessage ti;
		private final TransferPropositionSystemMessage p;

		FailedCreateIndirectAgentSocket(Exception e, InterfacedIDTransfer idLocal, InterfacedIDTransfer idDist,
				IndirectAgentSocket indirectAgentSocket, DistantKernelAgent dka, TransferConfirmationSystemMessage ti,
				TransferPropositionSystemMessage p) {
			this.e = e;
			this.idLocal = idLocal;
			this.idDist = idDist;
			this.indirectAgentSocket = indirectAgentSocket;
			this.dka = dka;
			this.ti = ti;
			this.p = p;
		}

		void executeTask() {
			if (logger != null)
				logger.severeLog("Start of " + this.getClass().getName() + " (" + distant_inet_address + ") FAILED !",
						e);

			transfer_ids.removeLocal(idLocal.getLocalID());
			transfer_ids.removeDistant(idDist.getDistantID());
			getStatistics().removeTransferAgentStats(idLocal.getLocalID());
			getMadkitConfig().networkProperties.removeStatsBandwitdh(idLocal.getLocalID().getID());
			killAgent(indirectAgentSocket);
			killAgent(dka);
			TransferClosedSystemMessage tc = new TransferClosedSystemMessage(getTransfertType(),
					p.getKernelAddressToConnect(), idLocal.getLocalID(), true);
			tc.setMessageLocker(ti.getMessageLocker());
			broadcastDataTowardEachIntermediatePeer(tc, true);

		}
	}

	private void receiveTransferConfirmationAnwser(AgentAddress sender, final TransferConfirmationSystemMessage ti) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Receiving transfer confirmation as anwser (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + ti);
		AgentAddress aat = transfer_ids_to_finalize.getTransferAgentAddress(ti.getYourIDTransfer());
		if (aat == null) {
			// here we are into a final peer

			validateInterfacedIDTransfer(sender, ti.getMyIDTransfer(), false);

			final InterfacedIDTransfer idtDist = getValidatedInterfacedIDTransfer(sender, ti.getMyIDTransfer());
			final TransferPropositionSystemMessage p = transfer_ids
					.removeTransferPropositionSystemMessage(ti.getYourIDTransfer());

			if (idtDist != null) {
				final DistantKernelAgent dka = new DistantKernelAgent();
				this.launchAgent(dka);
				AgentAddress dkaaa = dka.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

				if (ti.getDistantInetAddress() == null) {
					TransferClosedSystemMessage tc = new TransferClosedSystemMessage(getTransfertType(),
							p.getKernelAddressToConnect(), idtDist.getLocalID(), true);
					tc.setMessageLocker(ti.getMessageLocker());
					removeValidatedInterfacedIDTransfer(sender, ti.getMyIDTransfer());
					broadcastDataTowardEachIntermediatePeer(tc, true);
					return;
				}

				final IndirectAgentSocket indirectAgentSocket = new IndirectAgentSocket(this.distantIP, dkaaa,
						this.socket, this.nio_agent_address, ti.getDistantInetAddress(), this.local_interface_address,
						p.isYouAskConnection(), idtDist.getLocalID(), p.getNumberOfIntermediatePeers() + 1,
						this.getStatistics(),
						getAgentAddressIn(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE),
						distant_kernel_address, p.getKernelAddressToConnect(), getDistantInetSocketAddressRoot(),
						getSocketID());
				launchAgent(indirectAgentSocket);
				if (indirectAgentSocket.getState().equals(com.distrimind.madkit.kernel.AbstractAgent.State.LIVING)
						&& dka.getState().equals(com.distrimind.madkit.kernel.AbstractAgent.State.LIVING)) {
					AgentAddress iasaa = indirectAgentSocket.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					final InterfacedIDTransfer idtLocal = new InterfacedIDTransfer(idtDist.getLocalID(), sender,
							ti.getKernelAddressToConnect());
					idtLocal.setDistantID(idtDist.getLocalID());
					idtDist.setTransferToAgentAddress(iasaa);
					putValidatedInterfacedIDTransfer(iasaa, idtLocal);

					this.scheduleTask(new Task<>(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							try {
								indirectAgentSocket.initiateConnectionIfNecessary();
							} catch (Exception e) {
								receiveMessage(new FailedCreateIndirectAgentSocket(e, idtLocal, idtDist,
										indirectAgentSocket, dka, ti, p));
							}
							return null;
						}
					}));
				} else {
					killAgent(indirectAgentSocket);
					killAgent(dka);
					TransferClosedSystemMessage tc = new TransferClosedSystemMessage(getTransfertType(),
							p.getKernelAddressToConnect(), idtDist.getLocalID(), true);
					tc.setMessageLocker(ti.getMessageLocker());
					removeValidatedInterfacedIDTransfer(sender, ti.getMyIDTransfer());
					broadcastDataTowardEachIntermediatePeer(tc, true);
				}
			}

		} else {
			// here we are into the junction between two peers
			if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {

				InterfacedIDTransfer idLocal = transfer_ids_to_finalize.getLocal(ti.getYourIDTransfer());
				idLocal.setDistantID(ti.getMyIDTransfer());
				InterfacedIDTransfer idDist = new InterfacedIDTransfer(idLocal.getLocalID(),
						idLocal.getTransferToAgentAddress(), idLocal.getTransferToKernelAddress());
				idLocal.setTransferToAgentAddress(null);
				idDist.setTransferBlockChecker(
						new ConnectionProtocol.NullBlockChecker(ti.getNumberOfSubBlocks(), false, (short) 0));
				idDist.setDistantID(ti.getMyIDTransfer());
				transfer_ids_to_finalize.putDistant(idDist);

				getStatistics().putTransferAgentStats(idDist.getLocalID(), getMadkitConfig().networkProperties
						.addIfNecessaryAndGetStatsBandwitdh(idDist.getLocalID().getID()));
			}

			sendMessageWithRole(aat, new ObjectMessage<>(ti), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}
	}

	private void broadcastPropositionAnwser(TransferConfirmationSystemMessage t) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Broadcasting proposition confirmation answer (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + t);

		t.setIdTransferDestination(getTransfertType());
		validateInterfacedIDTransfer(null, t.getMyIDTransfer(), true);

		if (getTransfertType().equals(TransferAgent.NullIDTransfer)) {
			InterfacedIDTransfer idDist = getValidatedInterfacedIDTransfer(null, t.getYourIDTransfer());
			if (idDist == null)
				processInvalidTransferConnectionProcotol(
						"Broadcasting TransferConfirmationSystemMessage, but impossible to found the correspondant TransferID "
								+ t.getMyIDTransfer() + " in the peer agent " + this.toString());
			else {
				AgentAddress aa = transfer_ids.getTransferAgentAddress(idDist.getLocalID());
				if (aa == null) {
					processInvalidTransferConnectionProcotol(
							"Broadcasting TransferConfirmationSystemMessage, but impossible to found the correspondantant TransferID "
									+ t.getMyIDTransfer() + " and its transfer agent address");
				} else {
					broadcastDataTowardEachIntermediatePeer(t, true);
				}
			}

		} else {

			InterfacedIDTransfer idMiddle = getValidatedInterfacedIDTransfer(
					((IndirectAgentSocket) this).getParentAgentSocketAddress(), t.getMyIDTransfer());
			if (idMiddle == null) {
				processInvalidTransferConnectionProcotol(
						"Broadcasting TransferConfirmationSystemMessage, but impossible to found the correspondant TransferID "
								+ t.getMyIDTransfer() + " in the middle peer agent " + this.toString());
			} else {
				AgentAddress aa = transfer_ids.getTransferAgentAddress(idMiddle);
				if (aa == null) {
					processInvalidTransferConnectionProcotol(
							"Broadcasting TransferConfirmationSystemMessage, but impossible to found the correspondant TransferID "
									+ t.getMyIDTransfer() + " and its transfer agent address");
				} else {
					/*
					 * transfer_ids.putMiddle(((IndirectAgentSocket)this).
					 * getParentAgentSocketAddress(), idMiddle);
					 * transfer_ids.putTransferAgentAddress(idMiddle.getLocalID(), aa);
					 * 
					 * InterfacedIDTransfer idt=new InterfacedIDTransfer(idMiddle.getLocalID(),
					 * ((IndirectAgentSocket)this).getParentAgentSocketAddress(),this.
					 * distantInterfacedKernelAddress);
					 * transfer_ids.putMiddle(idMiddle.getTransferToAgentAddress(), idt);
					 */
					broadcastDataTowardEachIntermediatePeer(t, true);
				}
			}

		}
	}

	private void broadcastPropositionImpossibleAnwser(TransferImpossibleSystemMessageFromMiddlePeer t) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Broadcasting proposition impossible as answer (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ") : " + t);

		t.setIdTransferDestination(getTransfertType());
		InterfacedIDTransfer idLocal = transfer_ids_to_finalize.removeLocal(t.getMyIDTransfer());
		if (idLocal != null) {
			transfer_ids_to_finalize.removeTransferAgentAddress(idLocal);
			t.setYourIDTransfer(idLocal.getDistantID());
			broadcastDataTowardEachIntermediatePeer(t, true);
		}
	}

	InetSocketAddress getDistantInetSocketAddress() {
		return distant_inet_address;
	}

	boolean isTransferReadPaused() {
		return exceededDataQueueSize.get() || dataToTransferInQueue
				.get() > getMadkitConfig().networkProperties.numberOfCachedBytesToTransferBeforeBlockingSocket;
	}

	Group[] getDistantAcceptedAndRequestedGroups() {
		return distant_accepted_and_requested_groups;
	}

	AbstractGroup getDistantGeneralAcceptedGroups() {
		return distant_general_accepted_groups;
	}

	private void receiveData(byte[] _bytes) {
		lastReceivedDataUTC = System.currentTimeMillis();
		dataSynchronizer.receiveData(_bytes, dataSynchronized);
	}

	private Timer timer_read = null;
	private int dataRead = 0;

	protected ReturnCode receiveData(Block _block) {
		try {
			if (_block.getTransferID() != getTransfertType().getID()) {
				processInvalidBlockToTransfer(_block);
			}
			if (timer_read == null)
				timer_read = new Timer(true);
			else
				getStatistics().newDataReceived(new Integer(_block.getTransferID()), dataRead,
						timer_read.getDeltaMili());
			dataRead = _block.getBlockSize();
			getStatistics().newDataReceived(new Integer(_block.getTransferID()), _block.getBlockSize());
			_block.setTransfertID(TransferAgent.NullIDTransfer.getID());
			PacketPart p = getPacketPart(_block);
			ReturnCode rc = sendMessageWithRole(this.agent_for_distant_kernel_aa, new ReceivedBlockData(p),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			if (logger != null && !rc.equals(ReturnCode.SUCCESS))
				logger.severeLog("Block impossible to transfert to " + this.agent_for_distant_kernel_aa);
			return rc;
		} catch (NIOException e) {
			if (logger != null)
				logger.severeLog("", e);
			return ReturnCode.TRANSFER_FAILED;
		}
	}

	protected ReturnCode receiveDataToResend(Block block, InterfacedIDTransfer idt) {
		ReturnCode rc = sendMessageWithRole(idt.getTransferToAgentAddress(),
				new ResendData(new BlockDataToTransfer(block, idt.getLocalID())),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		if (!rc.equals(ReturnCode.SUCCESS) && logger != null)
			logger.severeLog("Indirect data impossible to resend to " + idt.getTransferToAgentAddress());
		return rc;
	}

	protected ReturnCode receiveIndirectData(Block block, InterfacedIDTransfer idt) {
		ReturnCode rc = sendMessageWithRole(idt.getTransferToAgentAddress(), new ReceivedIndirectData(block),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		if (!rc.equals(ReturnCode.SUCCESS) && logger != null)
			logger.severeLog("Indirect data impossible to transfert to " + idt.getTransferToAgentAddress());
		return rc;
	}

	static class ReceivedBlockData extends ObjectMessage<PacketPart> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3172797115031982660L;

		public ReceivedBlockData(PacketPart _content) {
			super(_content);
		}

	}

	private void receiveData(Object obj, ReceivedSerializableObject originalMessage) {
		try {
			if (obj == null) {
				processInvalidSerializedObject(null, null, "null reference");
				return;
			}
			if (state.compareTo(State.CONNECTED_INITIALIZING_ACCESS) < 0
					&& !((obj instanceof ConnectionMessage) || (obj instanceof ConnectionInfoSystemMessage)
							|| (obj instanceof PingMessage) || (obj instanceof PongMessage))) {
				processInvalidSerializedObject(
						new ConnectionException("Attempting to transmit a message of type " + obj.getClass()
								+ " with a connection not initialized !"),
						obj, "message " + obj.getClass() + " not autorized during connection process", true);
				return;
			} else if (state.compareTo(State.CONNECTED) < 0 && !((obj instanceof AccessMessage)
					|| (obj instanceof ConnectionMessage) || (obj instanceof ConnectionInfoSystemMessage)
					|| (obj instanceof PingMessage) || (obj instanceof PongMessage))) {
				processInvalidSerializedObject(
						new ConnectionException("Attempting to transmit a message of type " + obj.getClass()
								+ " with a access not initialized !"),
						obj, "message " + obj.getClass() + " not autorized during access/login process", true);
				return;
			}

			if (originalMessage != null && !(obj instanceof LanMessage))
				originalMessage.markDataAsRead();

			if (obj instanceof SystemMessage) {
				Integrity integrity = ((SystemMessage) obj).checkDataIntegrity();
				if (!integrity.equals(Integrity.OK)) {
					processInvalidSerializedObject(null, obj,
							"intigrity test for message " + obj.getClass() + " FAILED",
							integrity.equals(Integrity.FAIL_AND_CANDIDATE_TO_BAN));
					// startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);

					return;
				}

				if (obj instanceof ConnectionMessage) {
					boolean sendAskConnectionMessage = false;
					// boolean found=false;
					for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext();) {

						ConnectionProtocol<?> cp = it.next();

						if (!cp.isConnectionEstablished() && !cp.isConnectionFinishedButClosed()) {
							// found=true;
							try {
								ConnectionClosedReason connection_closed_reason = null;
								ConnectionMessage cm = null;
								if (sendAskConnectionMessage) {
									sendAskConnectionMessage = false;
									if (this_ask_connection) {
										cm = cp.setAndGetNextMessage(new AskConnection(true));
										if (cm == null)
											throw new NullPointerException(
													"The first returned message by the asker's connection protocol must be an AskMessage.");
										if (!(cm instanceof AskConnection))
											throw new ConnectionException(
													"The first returned message by the asker's connection protocol must be an AskMessage.");
									} else
										break;

								} else
									cm = cp.setAndGetNextMessage((ConnectionMessage) obj);

								boolean send_data = true;
								if (cm != null) {

									if (cm instanceof ConnectionFinished) {
										connection_closed_reason = ((ConnectionFinished) cm)
												.getConnectionClosedReason();

										if (connection_closed_reason != null) {

											ConnectionFinished cf = null;
											if (obj instanceof ConnectionFinished) {
												cf = (ConnectionFinished) obj;
												if (cf.getState().equals(
														ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED))
													cm = null;
												else
													send_data = false;
											}

											if (cf != null) {
												for (Iterator<ConnectionProtocol<?>> it2 = connection_protocol
														.reverseIterator(it); it2.hasNext();) {
													ConnectionProtocol<?> cp2 = it.next();
													cp2.setAndGetNextMessage(cf);
												}
											} else {
												for (Iterator<ConnectionProtocol<?>> it2 = connection_protocol
														.reverseIterator(it); it2.hasNext();) {
													ConnectionProtocol<?> cp2 = it.next();
													cp2.setConnectionClosed(connection_closed_reason);
												}
											}
										}

									} else if (cm instanceof ErrorConnection) {
										ErrorConnection ec = (ErrorConnection) cm;
										processInvalidConnectionMessage(null, (ConnectionMessage) obj, ec);
										ec.candidate_to_ban = false;
									}
								}
								if (cm != null) {
									if (connection_closed_reason != null) {
										startDeconnectionProcess(connection_closed_reason,
												send_data ? (ConnectionFinished) cm : null);
									} else if (send_data) {
										checkTransferBlockCheckerChangments();
										sendData(cm, true, false);
									}
								}
								State oldState = state;
								updateState();
								if (oldState == State.CONNECTION_IN_PROGRESS
										&& state == State.CONNECTED_INITIALIZING_ACCESS) {
									if (logger != null && logger.isLoggable(Level.FINER))
										logger.finer(
												"Connection protocols successfully initialized (distant_inet_address="
														+ distant_inet_address + ", distantInterfacedKernelAddress="
														+ distantInterfacedKernelAddress + ")");
									if (state == State.CONNECTED_INITIALIZING_ACCESS) {
										if (logger != null && logger.isLoggable(Level.FINER))
											logger.finer("Initializing access protocol !");
										AccessMessage am = access_protocol
												.setAndGetNextMessage(new AccessAskInitiliazation());
										if (am != null) {
											checkTransferBlockCheckerChangments();
											if (am instanceof AccessMessagesList)
											{
												for (AccessMessage am2 : ((AccessMessagesList) am).getMessages())
													sendData(am2, true, false);	
											}
											else
												sendData(am, true, false);
										}
									}
								}

							} catch (ConnectionException ce) {
								if (logger != null)
									logger.severeLog("", ce);

							}
							if (cp.isConnectionEstablished()) {
								sendAskConnectionMessage = true;
							} else
								break;
						}
					}
					if (sendAskConnectionMessage) {
						checkTransferBlockCheckerChangments();
						sendConnectionInfoSystemMessage();
					}
				} else if (obj.getClass() == ConnectionInfoSystemMessage.class) {
					this.distantConnectionInfo = (ConnectionInfoSystemMessage) obj;
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Receiving connection information messsage (distant_inet_address="
								+ distant_inet_address + ", distantInterfacedKernelAddress="
								+ distantInterfacedKernelAddress + ") : " + this.distantConnectionInfo);
					AbstractAgentSocket.this.sendMessageWithRole(agent_for_distant_kernel_aa,
							new ObjectMessage<>(this.distantConnectionInfo), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj instanceof AccessMessage) {
					try {
						ConnectionClosedReason con_close_reason = null;
						if (obj instanceof AccessAbordedMessage) {
							con_close_reason = ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED;
						}

						AccessMessage tsm = access_protocol.setAndGetNextMessage((AccessMessage) obj);
						AccessMessage messagesToSend[]=(tsm instanceof AccessMessagesList)?((AccessMessagesList)tsm).getMessages():new AccessMessage[] {tsm};
						for (AccessMessage toSend : messagesToSend)
						{
							if (toSend != null) {
								if (toSend instanceof DoNotSendMessage) {
									toSend = null;
								}
							}
							if (toSend != null) {
								if (toSend instanceof AccessErrorMessage) {
									AccessErrorMessage aem = (AccessErrorMessage) toSend;
									processInvalidAccessMessage(null, (AccessMessage) obj, aem);
									aem.candidate_to_ban = false;
									con_close_reason = ConnectionClosedReason.CONNECTION_ANOMALY;
								}
							}
							if (toSend != null) {
								short nbAnomalies = toSend.getNbAnomalies();
								if (nbAnomalies > 0) {
									if (processInvalidProcess("Too much anomalies during identification protocol",
											nbAnomalies)) {
										toSend = null;
									}
								}
							}
							if (toSend != null && con_close_reason == null && !(toSend instanceof DoNotSendMessage)) {
								AccessMessage am = toSend;
								while (am != null) {
									sendData(am, true, false);
									if (am.checkDifferedMessages()) {
										am = access_protocol.manageDifferedAccessMessage();
									} else
										am = null;
								}
							}
							if (toSend != null && toSend instanceof AccessAbordedMessage) {
								con_close_reason = ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED;
							}
							if (con_close_reason != null)
								startDeconnectionProcess(con_close_reason);
							else if (access_protocol.isNotifyAccessGroupChangements()) {
								notifyNewAccessChangements();
							}
							State oldState = state;
							updateState();
						
							if (logger != null && logger.isLoggable(Level.FINER)
									&& oldState == State.CONNECTED_INITIALIZING_ACCESS && state == State.CONNECTED)
								logger.finer("Access protocol successfully finished (distant_inet_address="
										+ distant_inet_address + ", distantInterfacedKernelAddress="
										+ distantInterfacedKernelAddress + ")");
						}
					} catch (Exception e) {
						if (logger != null)
							logger.severeLog("", e);
					}
				} else if (obj.getClass() == AcceptedGroups.class) {
					AcceptedGroups agm = ((AcceptedGroups) obj);
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Receiving distant accepted groups messsage (distant_inet_address="
								+ distant_inet_address + ", distantInterfacedKernelAddress="
								+ distantInterfacedKernelAddress + ") : " + agm);

					distant_accepted_and_requested_groups = agm.accepted_groups_and_requested;
					distant_general_accepted_groups = agm.accepted_groups;
					if (distant_kernel_address == null) {
						if (agm.kernelAddress.equals(getKernelAddress())) {
							processSameDistantKernelAddressWithLocal(agm.kernelAddress, true);
							return;
						} else {
							// receiving distant kernel address
							distant_kernel_address = agm.kernelAddress;
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("Receiving distant kernel address (distant_inet_address="
										+ distant_inet_address + ", distantInterfacedKernelAddress="
										+ distantInterfacedKernelAddress + ") : " + distant_kernel_address);

							// generate secret messages
							this.requestRole(
									LocalCommunity.Groups.getOriginalDistantKernelAgentGroup(distant_kernel_address),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
							distant_socket_agent_address = agm.distant_agent_socket_address;
							// send distant kernel address to DistantKernelAddressAgent
							sendMessageWithRole(agent_for_distant_kernel_aa,
									new ObjectMessage<KernelAddress>(distant_kernel_address),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
							// send to DistantKernelAddressAgent logins and access authorized
							sendMessageWithRole(agent_for_distant_kernel_aa,
									new NetworkLoginAccessEvent(distant_kernel_address, my_accepted_logins.identifiers,
											my_accepted_logins.identifiers, null, null),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						}
					}

					// distant_accepted_multi_groups=new MultiGroup(distant_accepted_groups);

					sendMessageWithRole(this.agent_for_distant_kernel_aa,
							new NetworkGroupsAccessEvent(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER,
									distant_general_accepted_groups, distant_accepted_and_requested_groups,
									distant_kernel_address),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == TooMuchConnectionWithTheSamePeers.class) {
					startDeconnectionProcess(ConnectionClosedReason.CONNECTION_LOST);
				} else if (obj.getClass() == SecretMessage.class) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Receiving secret message (distant_inet_address=" + distant_inet_address
								+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");

					SecretMessage sm = (SecretMessage) obj;
					if (sm.getAgentSocketAddress() == null)// if the received secret message must be transfered or
															// tested
					{
						// the received secret message must be tested
						HashMap<AgentAddress, SecretMessage> secretMessages = currentSecretMessages.get();
						if (secretMessages != null) {
							for (Map.Entry<AgentAddress, SecretMessage> entry : secretMessages.entrySet()) {
								if (sm.equals(entry.getValue())) {
									validateKernelAddress(entry.getKey());
									break;
								}
							}
						}
					} else if (sm.getAgentSocketAddress().isFrom(this.getKernelAddress()))
						sendMessageWithRole(sm.getAgentSocketAddress(), new ObjectMessage<SecretMessage>(sm),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					else if (logger != null)
						logger.warning("Unexpected secret message " + sm);

				} else if (obj.getClass() == DistantKernelAddressValidated.class) {

					this.distantKernelAddressValidated.set(true);
					if (distant_kernel_agent_activated) {
						informHooksForConnectionEstablished();
						sendMessageWithRole(this.agent_for_distant_kernel_aa,
								new ObjectMessage<>((DistantKernelAddressValidated) obj),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					}
				} else if (obj.getClass() == PingMessage.class) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Receiving ping message and sending pong message (distant_inet_address="
								+ distant_inet_address + ", distantInterfacedKernelAddress="
								+ distantInterfacedKernelAddress + ")");
					sendData(new PongMessage(), true, false);
				} else if (obj.getClass() == PongMessage.class) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Receiving pong message (distant_inet_address=" + distant_inet_address
								+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ")");
					waitingPongMessage = false;
					sendMessageWithRole(nio_agent_address, new PongMessageReceived(getSocketID()),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				}
				/*
				 * else if (obj.getClass()== TransferPropositionSystemMessage.class) {
				 * TransferPropositionSystemMessage tpsm=(TransferPropositionSystemMessage)obj;
				 * if (logger != null && logger.isLoggable(Level.FINEST))
				 * logger.finest("Receiving transfer proposition (distant_inet_address="
				 * +distant_inet_address+", distantInterfacedKernelAddress="
				 * +distantInterfacedKernelAddress+") : "+tpsm);
				 * receiveTransferProposition(null, tpsm); } else if (obj.getClass()==
				 * TransferConfirmationSystemMessage.class) { TransferConfirmationSystemMessage
				 * tcsm=(TransferConfirmationSystemMessage)obj; if (logger != null &&
				 * logger.isLoggable(Level.FINEST))
				 * logger.finest("Receiving transfer confirmation (distant_inet_address="
				 * +distant_inet_address+", distantInterfacedKernelAddress="
				 * +distantInterfacedKernelAddress+") : "+tcsm);
				 * receiveTransferConfirmationAnwser(tcsm); }
				 */
				else if (obj.getClass() == TryDirectConnection.class) {
					TryDirectConnection tdc = (TryDirectConnection) obj;
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Receiving request to try direct connection (distant_inet_address="
								+ distant_inet_address + ", distantInterfacedKernelAddress="
								+ distantInterfacedKernelAddress + ") : " + tdc);

					AskForConnectionMessage ask = new AskForConnectionMessage(ConnectionStatusMessage.Type.CONNECT,
							new DoubleIP(tdc.getInetSocketAddress()));
					ask.chooseIP(true);
					ask.setJoinedPiece(tdc,
							getAgentAddressIn(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE));
					broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_ROLE,
							ask, LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == DirectConnectionFailed.class) {
					broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.TRANSFER_AGENT_ROLE,
							new ObjectMessage<>((DirectConnectionFailed) obj), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == DirectConnectionSuceeded.class) {
					broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.TRANSFER_AGENT_ROLE,
							new ObjectMessage<>((DirectConnectionSuceeded) obj),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == CGRSynchroSystemMessage.class) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Receiving CGRSynchro message (distantInterfacedKernelAddress="
								+ distant_kernel_address + ") : " + obj);
					CGRSynchroSystemMessage cgr = ((CGRSynchroSystemMessage) obj);

					sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT,
							cgr.getCGRSynchro(), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == CGRSynchrosSystemMessage.class) {
					sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT,
							((CGRSynchrosSystemMessage) obj).getCGRSynchros(distant_kernel_address),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj.getClass() == ValidateBigDataProposition.class) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Receiving big data proposition validation (distant_inet_address="
								+ distant_inet_address + ", distantInterfacedKernelAddress="
								+ distantInterfacedKernelAddress + ") : " + obj);

					sendMessageWithRole(this.agent_for_distant_kernel_aa,
							new ObjectMessage<>((ValidateBigDataProposition) obj),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				} else if (obj instanceof LanMessage && originalMessage != null) {
					if (obj.getClass() == DirectLanMessage.class) {
						DirectLanMessage dlm = (DirectLanMessage) obj;
						if (logger != null && logger.isLoggable(Level.FINEST))
							logger.finest("Receiving direct lan message (distant_inet_address=" + distant_inet_address
									+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
									+ ", conversationID=" + dlm.message.getConversationID() + ")");

						if (dlm.message.getReceiver().getKernelAddress().equals(getKernelAddress())
								&& my_accepted_groups.acceptGroup(dlm.message.getReceiver())) {
							this.sendMessageWithRole(this.agent_for_distant_kernel_aa,
									new ObjectMessage<ReceivedSerializableObject>(originalMessage),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						}
					} else if (obj.getClass() == BroadcastLanMessage.class) {
						BroadcastLanMessage blm = (BroadcastLanMessage) obj;
						if (logger != null && logger.isLoggable(Level.FINEST))
							logger.finest(
									"Receiving broadcast lan message (distant_inet_address=" + distant_inet_address
											+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
											+ ", conversationID=" + blm.message.getConversationID() + ")");

						AbstractGroup bgroups = my_accepted_groups.getAcceptedGroups(blm.abstract_group);
						if (!bgroups.isEmpty()) {
							blm.setAccetedGroups(bgroups);
							this.sendMessageWithRole(this.agent_for_distant_kernel_aa,
									new ObjectMessage<ReceivedSerializableObject>(originalMessage),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						}
					} else {
						processInvalidSerializedObject(
								new NIOException("Unknow type message " + obj.getClass().getCanonicalName()), obj,
								null);
						originalMessage.markDataAsRead();
					}
				} else if (obj.getClass() == DataToBroadcast.class) {
					DataToBroadcast d = (DataToBroadcast) obj;
					receiveBroadcastData(null, d, true);
				} else {
					processInvalidSerializedObject(
							new NIOException("Unknow type message " + obj.getClass().getCanonicalName()), obj, null);
				}

			} else {
				processInvalidSerializedObject(
						new NIOException("Unknow type message " + obj.getClass().getCanonicalName()), obj, null);
			}
		} catch (SelfKillException e) {
			throw e;
		} catch (Exception e) {
			if (logger != null)
				logger.severeLog("Unexpected exception", e);
		}
	}

	private void informHooksForConnectionEstablished() {
		if (logger != null)
			logger.info("Connection established (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ", local_interface="
					+ local_interface_address + ")");

		MadkitKernelAccess.informHooks(this,
				new NetworkEventMessage(AgentActionEvent.CONNEXION_ESTABLISHED, new Connection(
						new ConnectionIdentifier(getTransfertType(), distant_inet_address, local_interface_address),
						distantInterfacedKernelAddress)));
	}

	protected abstract void checkTransferBlockCheckerChangments() throws ConnectionException;

	void validateKernelAddress(AgentAddress concernedDistantKernelAgent) {
		if (currentSecretMessages.getAndSet(null) != null) {
			if (concernedDistantKernelAgent == this.agent_for_distant_kernel_aa) {
				// the default DistantKernelAgent can be activated, and the distant kernel
				// address must be interfaced
				AbstractAgentSocket.this.sendMessageWithRole(agent_for_distant_kernel_aa,
						new KernelAddressValidation(true), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			} else {
				// leaveRole(LocalCommunity.Groups.getDistantKernelAgentGroup(agent_for_distant_kernel_aa.getAgentNetworkID()),
				// LocalCommunity.Roles.SOCKET_AGENT_ROLE);

				AgentAddress aa = this.agent_for_distant_kernel_aa;
				if (isLocalAgentAddressValid(concernedDistantKernelAgent))
					agent_for_distant_kernel_aa = concernedDistantKernelAgent;
				else // the agent have been deleted during the connection process
					agent_for_distant_kernel_aa = null;

				if (agent_for_distant_kernel_aa == null) {
					// create a new distant kernel agent
					DistantKernelAgent agent = new DistantKernelAgent();
					launchAgent(agent);
					agent_for_distant_kernel_aa = agent.getAgentAddressIn(
							LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					sendMessageWithRole(this.agent_for_distant_kernel_aa,
							new ObjectMessage<KernelAddress>(distant_kernel_address),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				}
				if (!requestRole(
						LocalCommunity.Groups
								.getDistantKernelAgentGroup(agent_for_distant_kernel_aa.getAgentNetworkID()),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE).equals(ReturnCode.SUCCESS) && logger != null)
					logger.severe("Cannot request group "
							+ LocalCommunity.Groups
									.getDistantKernelAgentGroup(agent_for_distant_kernel_aa.getAgentNetworkID())
							+ " and role " + LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				// send data to distant kernel agent
				sendMessageWithRole(this.agent_for_distant_kernel_aa,
						new ObjectMessage<AgentSocketData>(new AgentSocketData(this)),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				sendMessageWithRole(this.agent_for_distant_kernel_aa,
						new NetworkGroupsAccessEvent(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER,
								distant_general_accepted_groups, distant_accepted_and_requested_groups,
								distant_kernel_address),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				sendMessageWithRole(
						this.agent_for_distant_kernel_aa, new NetworkLoginAccessEvent(distant_kernel_address,
								my_accepted_logins.identifiers, my_accepted_logins.identifiers, null, null),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				sendMessageWithRole(agent_for_distant_kernel_aa, new KernelAddressValidation(true),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				// validate kernel address
				this.sendMessageWithRole(aa, new KernelAddressValidation(false),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);

				if (my_accepted_groups != null)
					my_accepted_groups.notifyDistantKernelAgent();

				/*
				 * if (this.distantConnectionInfo!=null)
				 * AbstractAgentSocket.this.sendMessageWithRole(agent_for_distant_kernel_aa, new
				 * ObjectMessage<>(this.distantConnectionInfo),
				 * LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				 */
			}
		}
	}

	private void updateState() {
		if (!state.equals(State.DISCONNECTION_IN_PROGRESS) && !state.equals(State.DISCONNECTION)) {
			if (isConnectionEstablished()) {
				if (access_protocol.isAccessFinalized()) {
					state = State.CONNECTED;
				} else {
					state = State.CONNECTED_INITIALIZING_ACCESS;
				}
			} else
				state = State.CONNECTION_IN_PROGRESS;
		}
	}

	@Override
	public void notifyNewAccessChangements() {
		my_accepted_groups.updateGroups(access_protocol.getGroupsAccess());
		my_accepted_logins.updateData();
	}

	private Groups my_accepted_groups = null;
	private Logins my_accepted_logins = null;
	// private MultiGroup distant_accepted_multi_groups=null;
	private Group[] distant_accepted_and_requested_groups = null;
	private AbstractGroup distant_general_accepted_groups = null;
	KernelAddress distant_kernel_address = null;
	// KernelAddressInterfaced distant_kernel_address_interfaced=null;

	class Groups /* implements GroupChangementNotifier */
	{
		private volatile MultiGroup groups;
		// private boolean auto_requested=false;
		private volatile Group[] represented_groups = null;

		private AgentAddress distant_agent_address = null;
		private boolean kernelAddressSent = false;

		protected Groups() {
			this(null);
		}

		protected Groups(MultiGroup _groups) {
			if (_groups == null)
				groups = new MultiGroup();
			else
				updateGroups(_groups);
			// MadkitKernelAccess.addGroupChangementNotifier(this);
		}

		boolean acceptGroup(AgentAddress add) {
			return add.getKernelAddress().equals(AbstractAgentSocket.this.getKernelAddress())
					&& add.getGroup().isDistributed() && groups.includes(getKernelAddress(), add.getGroup());
		}

		AbstractGroup getAcceptedGroups(AbstractGroup group) {

			MultiGroup mg = new MultiGroup();
			for (Group g : groups.intersect(getKernelAddress(), group)) {
				if (g.isDistributed())
					mg.addGroup(g);
			}
			return mg;
		}

		public MultiGroup getGroups() {
			return groups;
		}

		protected void updateGroups(MultiGroup _groups) {
			if (_groups != null) {
				// MultiGroup old_groups=groups;
				groups = _groups;
				/*
				 * if (auto_requested) { if
				 * (!AbstractAgentSocket.this.replaceAutoRequestedGroup(old_groups, _groups)) {
				 * if (logger!=null) logger.
				 * severe("Problem of group replacement in function 'AbstractAgentSocket.Groups.updateGroups(MultiGroup)' !"
				 * ); } //watcher.addProbe(new Probe<>(_group, _role)) } else
				 * AbstractAgentSocket.this.autoRequestRole(groups,
				 * MadKitLanExtension.LAN_TRANSMITER_ROLE);
				 */

				// auto_requested=true;
				boolean changements = isThereDetectedChangements();
				if (changements)
					notifyGroupChangements();
				if (changements || distant_agent_address != agent_for_distant_kernel_aa) {
					notifyDistantKernelAgent();
				}

			}
		}

		protected void notifyDistantKernelAgent() {
			distant_agent_address = agent_for_distant_kernel_aa;
			if (agent_for_distant_kernel_aa != null)
				sendMessageWithRole(agent_for_distant_kernel_aa, new ObjectMessage<Groups>(this),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}

		private boolean isThereDetectedChangements() {
			synchronized (this) {
				Group[] rp = groups.getRepresentedGroups(AbstractAgentSocket.this.getKernelAddress());

				boolean res = !kernelAddressSent;

				if (!res) {
					if (represented_groups == null) {
						if (rp.length > 0)
							res = true;
					} else {
						if (represented_groups.length != rp.length)
							res = true;
						else {
							for (Group g1 : rp) {
								boolean found = false;
								for (Group g2 : represented_groups) {
									if (g2.equals(g1)) {
										found = true;
										break;
									}
								}
								if (!found) {
									res = true;
									break;
								}
							}
						}
					}
				}
				represented_groups = rp;
				return res;
			}
		}

		public AcceptedGroups potentialChangementInGroups() {
			if (getState().compareTo(AbstractAgent.State.ACTIVATED) >= 0 && isThereDetectedChangements())
				return getGroupChangements();
			return null;

		}

		private void notifyGroupChangements() {
			AcceptedGroups ag = getGroupChangements();
			if (ag != null)
				AbstractAgentSocket.this.sendData(ag, true, false);
		}

		private AcceptedGroups getGroupChangements() {
			if (represented_groups != null && access_protocol.isAccessFinalized() && !groups.isEmpty()) {
				kernelAddressSent = true;
				try {
					// AbstractAgentSocket.this.sendData(new AcceptedGroups(groups,
					// represented_groups, getKernelAddress(),
					// AbstractAgentSocket.this.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
					// LocalCommunity.Roles.SOCKET_AGENT_ROLE)), true, false);
					return new AcceptedGroups(groups, represented_groups, getKernelAddress(), AbstractAgentSocket.this
							.getAgentAddressIn(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.SOCKET_AGENT_ROLE));
				} catch (Exception e) {
					if (logger != null)
						logger.severeLog("Invalid accepted groups or no accepted groups by access data : ", e);
					startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
				}
				// notifyDistantKernelAgent();
			}
			return null;
		}

	}

	private class Logins {
		ArrayList<PairOfIdentifiers> identifiers = new ArrayList<>();

		Logins() {
		}

		void updateData() {
			if (access_protocol != null) {

				ArrayList<PairOfIdentifiers> accepted = access_protocol.getLastAcceptedIdentifiers();
				ArrayList<PairOfIdentifiers> denied = access_protocol.getLastDeniedIdentifiers();
				ArrayList<PairOfIdentifiers> unlogged = access_protocol.getLastUnloggedIdentifiers();
				identifiers = access_protocol.getAllAcceptedIdentifiers();

				if (distant_kernel_address != null) {
					sendMessageWithRole(AbstractAgentSocket.this.agent_for_distant_kernel_aa,
							new NetworkLoginAccessEvent(distant_kernel_address, identifiers, accepted, denied,
									unlogged),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				}
			}
		}
	}

	private boolean isConnectionEstablished() {
		for (ConnectionProtocol<?> cp : connection_protocol) {
			if (!cp.isConnectionEstablished())
				return false;
		}
		return true;
	}

	/*
	 * protected class TransfertPacket { public final AgentAddress address; public
	 * final int id_transfert; public final boolean message_to_resend; public final
	 * BytesPerSecondsStat stat=new BytesPerSecondsStat(max_buffer_size,
	 * max_buffer_size/4);
	 * 
	 * public TransfertPacket(AgentAddress _address, int _id_transfert) {
	 * address=_address; id_transfert=_id_transfert; if
	 * (getKernelAddress().equals(address.getKernelAddress()))
	 * message_to_resend=false; else message_to_resend=true; }
	 * 
	 * }
	 * 
	 * HashMap<Integer, TransfertPacket> indirect_agent_socket_addresses=new
	 * HashMap<>();
	 */

	protected InterfacedIDTransfer routesData(Block _block) throws RouterException, BlockParserException {
		boolean valid = _block.isValid();
		if (valid) {
			if (_block.isDirect() || getTransfertType().equals(_block.getTransferID())) {
				return null;
			} else {
				InterfacedIDTransfer idt = transfer_ids.getDistant(_block.getTransferID());

				if (idt == null)
					throw new RouterException("Unknow transfert ID : " + _block.getTransferID());

				return idt;
			}
		} else
			throw new BlockParserException();
	}

	protected IDGeneratorInt packet_id_generator = new IDGeneratorInt();

	/*
	 * protected void sendData(Serializable _data) throws NIOException {
	 * sendData(_data, false, null, false); } protected void sendData(Serializable
	 * _data, boolean prioritary, boolean last_message) throws NIOException {
	 * sendData(_data, prioritary, null, last_message); } protected void
	 * sendData(Serializable _data, MessageLocker _local_lan_message, boolean
	 * last_message) throws NIOException { sendData(_data, false,
	 * _local_lan_message, last_message); } protected void sendData(Serializable
	 * _data, boolean prioritary, MessageLocker _local_lan_message, boolean
	 * last_message) throws NIOException { try (ByteArrayOutputStream baos=new
	 * ByteArrayOutputStream();ObjectOutputStream oos=new ObjectOutputStream(baos);)
	 * { oos.writeObject(_data); WritePacket packet=new
	 * WritePacket(PacketPartHead.TYPE_PACKET, packet_id_generator.getNewID(),
	 * connection_protocol.getProperties().maxBufferSize, new
	 * ByteArrayInputStream(baos.toByteArray())); sendMessage(nio_agent_address, new
	 * DataToSendMessage(new PacketData(packet, getTransfertType(),
	 * _local_lan_message, getNextTimeBeforeSendingNewPacket(), last_message),
	 * socket, prioritary)); } catch(PacketException | IOException |
	 * InputStreamException e) { throw new NIOException(e); } }
	 */
	protected ReturnCode broadcastDataTowardEachIntermediatePeer(BroadcastableSystemMessage _data, boolean prioritary) {
		return broadcastDataTowardEachIntermediatePeer(
				new DataToBroadcast(_data, getKernelAddress(), prioritary, _data.getIdTransferDestination()),
				prioritary);
	}

	protected final ReturnCode broadcastDataTowardEachIntermediatePeer(AgentAddress sender,
			BroadcastableSystemMessage _data, IDTransfer distantIDDestination, DataToBroadcast d) {
		return broadcastDataTowardEachIntermediatePeer(sender, _data, distantIDDestination, d.getSender(),
				d.isPrioritary());
	}

	protected ReturnCode broadcastDataTowardEachIntermediatePeer(AgentAddress sender, BroadcastableSystemMessage _data,
			IDTransfer distantIDDestination, KernelAddress kaServer, boolean isPrioritary) {
		if (sender == null) {
			InterfacedIDTransfer idt = getValidatedInterfacedIDTransfer(sender, distantIDDestination);
			if (idt == null)
				idt = getInterfacedIDTransferToFinalize(sender, distantIDDestination);
			if (idt == null) {
				if (logger != null)
					logger.warning("Impossible to route data : " + _data);
				return ReturnCode.NO_RECIPIENT_FOUND;
			}

			return sendMessageWithRole(idt.getTransferToAgentAddress(),
					new ObjectMessage<DataToBroadcast>(
							new DataToBroadcast(_data, kaServer, isPrioritary, _data.getIdTransferDestination())),
					LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		} else
			return broadcastDataTowardEachIntermediatePeer(_data, isPrioritary);
	}

	protected ReturnCode broadcastDataTowardEachIntermediatePeer(DataToBroadcast _data, boolean prioritary) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Broadcasting indirect data toward each intermediate peer (distant_inet_address="
					+ distant_inet_address + ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress
					+ ", prioritary=" + prioritary + ") : " + _data);
		return sendData(_data, prioritary, false);
	}

	protected ReturnCode sendData(SystemMessage _data, boolean prioritary, boolean last_message) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Ask for data routing (distant_inet_address=" + distant_inet_address
					+ ", distantInterfacedKernelAddress=" + distantInterfacedKernelAddress + ", prioritary="
					+ prioritary + ", last_message=" + last_message + ")");
		return sendMessageWithRole(this.agent_for_distant_kernel_aa,
				new DistantKernelAgent.SendDataFromAgentSocket(_data, last_message, prioritary),
				LocalCommunity.Roles.SOCKET_AGENT_ROLE);
	}

	class DataToSendMessageFromAgentSocket extends ObjectMessage<SystemMessage> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5947895434314589098L;

		final boolean last_message;
		final boolean prioritary;

		public DataToSendMessageFromAgentSocket(SystemMessage _content, boolean last_message, boolean prioritary) {
			super(_content);
			this.last_message = last_message;
			this.prioritary = prioritary;
		}
	}

	private PacketPart getPacketPart(Block _block) throws NIOException {
		try {
			return connection_protocol.getPacketPart(_block, getMadkitConfig().networkProperties);
		} catch (NIOException e) {
			if (e.isInvalid()) {
				processInvalidBlock(null, _block, e.isCandidateForBan());
				return null;
			} else
				throw e;
		}
	}

	private boolean processInvalidSerializedObject(Exception e, Object data, String message) {
		return processInvalidSerializedObject(e, data, message, false);
	}

	private boolean processInvalidSerializedObject(Exception e, Object data, String message, boolean candidate_to_ban) {
		return processInvalidProcess("Invalid serialized object from Kernel Address " + distantInterfacedKernelAddress
				+ " and InetSocketAddress " + distant_inet_address + " : " + message, e, candidate_to_ban);
	}

	private boolean processSameDistantKernelAddressWithLocal(KernelAddress ka, boolean candidate_to_ban) {
		return processInvalidProcess(
				"The received distant kernel address is the same than the local kernel address : " + ka,
				candidate_to_ban);
	}

	protected boolean processInvalidBlock(Exception e, Block _block, boolean candidate_to_ban) {
		return processInvalidProcess("Invalid block from Kernel Address " + distantInterfacedKernelAddress
				+ " and InetSocketAddress " + distant_inet_address, e, candidate_to_ban);
	}

	private boolean processInvalidAccessMessage(Exception e, AccessMessage received, AccessErrorMessage returned) {
		return processInvalidProcess("Invalid access message from Kernel Address " + distantInterfacedKernelAddress
				+ " and InetSocketAddress " + distant_inet_address, e, returned.candidate_to_ban);
	}

	private boolean processExternalAnomaly(String message, boolean candidate_to_ban) {
		return processInvalidProcess(message, candidate_to_ban);
	}

	private boolean processInvalidConnectionMessage(Exception e, ConnectionMessage received, ErrorConnection returned) {
		return processInvalidProcess("Invalid connection message from Kernel Address " + distantInterfacedKernelAddress
				+ " and InetSocketAddress " + distant_inet_address, e, returned.candidate_to_ban);
	}

	private boolean processInvalidTransferConnectionProcotol(String message) {
		return processInvalidProcess(message, false);
	}

	private boolean processInvalidBlockToTransfer(Block _block) {
		return processInvalidProcess("Invalid block to transfer. Unkonw TransferID " + _block.getTransferID(), false);
	}

	private boolean processInvalidProcess(String message, boolean candidate_to_ban) {
		return processInvalidProcess(message, null, candidate_to_ban);
	}

	boolean processInvalidProcess(String message, short nbAnomalies) {
		try {
			MadkitKernelAccess.informHooks(this, new NetworkAnomalyEvent(distantInterfacedKernelAddress,
					distant_inet_address.getAddress(), false, message));
			if (getMadkitConfig().getDatabaseWrapper() != null) {
				try {
					IPBanned.Record r = ((IPBanStat) getMadkitConfig().getDatabaseWrapper()
							.getTableInstance(IPBanStat.class)).processExpulsion(nbAnomalies,
									distant_inet_address.getAddress(), false,
									getMadkitConfig().networkProperties.expulsionDuration,
									getMadkitConfig().networkProperties.nbMaxAnomaliesBeforeTrigeringExpulsion,
									getMadkitConfig().networkProperties.nbMaxExpulsions,
									getMadkitConfig().networkProperties.banishmentDuration,
									getMadkitConfig().networkProperties.nbMaxAnomaliesBeforeTrigeringBanishment,
									getMadkitConfig().networkProperties.nbMaxBanishments,
									getMadkitConfig().networkProperties.expulsionStatisticDuration,
									getMadkitConfig().networkProperties.banishmentStatisticDuration,
									getMadkitConfig().networkProperties.getWhiteInetAddressesList());
					if ((r != null && r.expiration_time > System.currentTimeMillis())) {
						MadkitKernelAccess.informHooks(this,
								new IPBannedEvent(distant_inet_address.getAddress(), r.expiration_time));
						startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
						return true;
					}
				} catch (DatabaseException e2) {
					if (logger != null)
						logger.severeLog("Database exception", e2);
				}
			} else if (nbAnomalies >= getMadkitConfig().networkProperties.nbMaxAnomaliesBeforeTrigeringExpulsion) {
				startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
				return true;
			}
		} catch (Exception e) {
			if (logger != null)
				logger.severeLog("Unexpected exception", e);
			else
				e.printStackTrace();
		}
		return false;
	}

	boolean processInvalidProcess(String message, Exception e, boolean candidate_to_ban) {
		if (logger != null) {
			if (e == null)
				logger.severeLog(message == null ? "Invalid process" : message);
			else
				logger.severeLog(message == null ? "Invalid process" : message, e);
		}
		MadkitKernelAccess.informHooks(this, new NetworkAnomalyEvent(distantInterfacedKernelAddress,
				distant_inet_address.getAddress(), candidate_to_ban, message));
		try {
			if (getMadkitConfig().getDatabaseWrapper() != null) {
				IPBanned.Record r = ((IPBanStat) getMadkitConfig().getDatabaseWrapper()
						.getTableInstance(IPBanStat.class)).processExpulsion(distant_inet_address.getAddress(),
								candidate_to_ban, getMadkitConfig().networkProperties.expulsionDuration,
								getMadkitConfig().networkProperties.nbMaxAnomaliesBeforeTrigeringExpulsion,
								getMadkitConfig().networkProperties.nbMaxExpulsions,
								getMadkitConfig().networkProperties.banishmentDuration,
								getMadkitConfig().networkProperties.nbMaxAnomaliesBeforeTrigeringBanishment,
								getMadkitConfig().networkProperties.nbMaxBanishments,
								getMadkitConfig().networkProperties.expulsionStatisticDuration,
								getMadkitConfig().networkProperties.banishmentStatisticDuration,
								getMadkitConfig().networkProperties.getWhiteInetAddressesList());
				if ((r != null && r.expiration_time > System.currentTimeMillis()) || candidate_to_ban) {
					if (r != null && r.expiration_time > System.currentTimeMillis()) {
						MadkitKernelAccess.informHooks(this,
								new IPBannedEvent(distant_inet_address.getAddress(), r.expiration_time));
					}
					startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
					return true;
				}
			} else if (candidate_to_ban) {
				startDeconnectionProcess(ConnectionClosedReason.CONNECTION_ANOMALY);
				return true;
			}
		} catch (DatabaseException e2) {
			if (logger != null)
				logger.severeLog("Database exception", e2);
		}

		return false;
	}

	public boolean isBannedOrExpulsed() {
		return isBanned;
		/*
		 * try { return
		 * ((IPBanStat)getMadkitConfig().getDatabaseWrapper().getTableInstance(IPBanStat
		 * .class)).isBannedOrExpulsed(distant_inet_address.getAddress()); } catch
		 * (DatabaseException e) { if (logger!=null)
		 * logger.severeLog("Database exception", e); return false; }
		 */
	}

	protected Block getBlock(WritePacket _packet, int _transfert_type) throws NIOException {
		return connection_protocol.getBlock(_packet, _transfert_type, need_random ? random : null);
	}

	protected abstract class BlockData extends AbstractData {
		private final IDTransfer id_transfert;
		private final ByteBuffer buffer;
		private Block block;

		BlockData(boolean priority, Block _block, IDTransfer id) {
			super(priority);
			block = _block;
			buffer = ByteBuffer.wrap(_block.getBytes());
			id_transfert = id;
		}

		Block getBlock() {
			return block;
		}

		@Override
		public void unlockMessage() {
		}

		@Override
		public boolean isUnlocked() {
			return true;
		}

		@Override
		public ByteBuffer getByteBuffer() {
			return buffer;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public boolean isFinished() {
			return buffer.remaining() == 0;
		}

		@Override
		public boolean isCurrentByteBufferFinished() {
			return buffer.remaining() == 0;
		}

		@Override
		DataTransferType getDataTransferType() {
			return DataTransferType.SHORT_DATA;
		}

		@Override
		IDTransfer getIDTransfer() {
			return id_transfert;
		}

		@Override
		boolean isCurrentByteBufferStarted() {
			return buffer.position() > 0;
		}

	}

	protected class BlockDataToTransfer extends BlockData {

		// private final long beginTime;
		private boolean is_locked = true;

		BlockDataToTransfer(Block _block, IDTransfer _id_transfert) {
			super(false, _block, _id_transfert);
			// beginTime=System.currentTimeMillis();
			dataToTransferInQueue.addAndGet(getBlock().getBlockSize());
			// sendMessageWithRole(nio_agent_address, new LockReadMessage(socket),
			// LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		}

		@Override
		public void unlockMessage() {
			if (is_locked) {
				is_locked = false;
				dataToTransferInQueue.addAndGet(-getBlock().getBlockSize());
				/*
				 * sendMessageWithRole(nio_agent_address,new UnlockReadMessage(socket),
				 * LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				 * id_transfert.stat_transfer.newBytesIndentified(this.getByteBuffer().capacity(
				 * ), System.currentTimeMillis()-beginTime);
				 */
			}
		}

		@Override
		public boolean isUnlocked() {
			return is_locked;
		}

		@Override
		DataTransferType getDataTransferType() {
			return DataTransferType.DATA_TO_TRANSFER;
		}

		@Override
		public void reset() {
			// TODO manage connection closed
		}

	}

	protected class TransferIDs {
		private final HashMap<Integer, HashMap<AgentAddress, InterfacedIDTransfer>> middle_transfer_ids = new HashMap<>();
		private final HashMap<Integer, InterfacedIDTransfer> local_transfer_ids = new HashMap<>();
		private final HashMap<Integer, InterfacedIDTransfer> distant_transfer_ids = new HashMap<>();
		private final HashMap<Integer, AgentAddress> transfer_agents = new HashMap<>();
		private final HashMap<Integer, TransferPropositionSystemMessage> propositions_in_progress = new HashMap<>();

		protected boolean hasDataToCheck() {
			return middle_transfer_ids.size() > 0 || local_transfer_ids.size() > 0 || distant_transfer_ids.size() > 0;
		}

		protected void removeObsoleteData() {
			removeObsoleteData(local_transfer_ids, getDelayBeforeTransferNodeBecomesObsolete());
			removeObsoleteData(distant_transfer_ids, getDelayBeforeTransferNodeBecomesObsolete());
			removeObsoleteMiddleData(middle_transfer_ids, getDelayBeforeTransferNodeBecomesObsolete());
		}

		private void removeObsoleteData(HashMap<Integer, InterfacedIDTransfer> data, long timeOut) {
			timeOut = System.currentTimeMillis() - timeOut;
			for (Iterator<Map.Entry<Integer, InterfacedIDTransfer>> it = data.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Integer, InterfacedIDTransfer> e = it.next();
				if (e.getValue().getLastAccessUTC() < timeOut) {
					AgentAddress aa = transfer_agents.remove(new Integer(e.getValue().getLocalID().getID()));

					if (!getTransfertType().equals(e.getValue().getLocalID()) && aa != null && checkAgentAddress(aa)) {
						TransferClosedSystemMessage t = new TransferClosedSystemMessage(e.getValue().getLocalID(),
								e.getValue().getTransferToKernelAddress(), e.getValue().getLocalID(), true);
						sendMessageWithRole(aa,
								new ObjectMessage<DataToBroadcast>(new DataToBroadcast(t,
										e.getValue().getTransferToKernelAddress(), true, e.getValue().getLocalID())),
								LocalCommunity.Roles.SOCKET_AGENT_ROLE);
					}

					propositions_in_progress.remove(new Integer(e.getValue().getLocalID().getID()));
					it.remove();
				}
			}
		}

		private void removeObsoleteMiddleData(HashMap<Integer, HashMap<AgentAddress, InterfacedIDTransfer>> data,
				long timeOut) {
			timeOut = System.currentTimeMillis() - timeOut;
			for (Iterator<Map.Entry<Integer, HashMap<AgentAddress, InterfacedIDTransfer>>> it = data.entrySet()
					.iterator(); it.hasNext();) {
				HashMap<AgentAddress, InterfacedIDTransfer> hm = it.next().getValue();
				boolean remove = false;
				for (InterfacedIDTransfer idt : hm.values()) {
					if (idt.getLastAccessUTC() < timeOut) {
						remove = true;
						AgentAddress aa = transfer_agents.remove(new Integer(idt.getLocalID().getID()));

						if (!getTransfertType().equals(idt.getLocalID()) && aa != null && checkAgentAddress(aa)) {
							TransferClosedSystemMessage t = new TransferClosedSystemMessage(idt.getLocalID(),
									idt.getTransferToKernelAddress(), idt.getLocalID(), true);
							sendMessageWithRole(aa,
									new ObjectMessage<DataToBroadcast>(new DataToBroadcast(t,
											idt.getTransferToKernelAddress(), true, idt.getLocalID())),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						}
						propositions_in_progress.remove(new Integer(idt.getLocalID().getID()));
						break;
					}
				}
				if (remove)
					it.remove();
			}
		}

		/*
		 * protected void closeTransferID(AbstractAgentSocket asker, IDTransfer idLocal)
		 * { AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();
		 * 
		 * InterfacedIDTransfer idt=local_transfer_ids.remove(idLocal); if (idt!=null) {
		 * distant_transfer_ids.remove(idt.getDistantID());
		 * 
		 * propositions_in_progress.remove(new Integer(idLocal.getID())); AgentAddress
		 * aa=transfer_agents.remove(new Integer(idLocal.getID())); if (aa==null) {
		 * AgentAddress aa2=idt.getTransferToAgentAddress(); if (aa2!=null)
		 * asker.sendMessageWithRole(aa2, new ObjectMessage<>(new
		 * TransferClosedSystemMessage(idt.getDistantID(), asker.getKernelAddress(),
		 * idt.getLocalID())), LocalCommunity.Roles.SOCKET_AGENT_ROLE);
		 * HashMap<AgentAddress,InterfacedIDTransfer> hm=middle_transfer_ids.remove(new
		 * Integer(idLocal.getID())); if (hm!=null) {
		 * idt=hm.get(asker.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
		 * LocalCommunity.Roles.SOCKET_AGENT_ROLE)); if (idt!=null &&
		 * idt.getTransferToAgentAddress()!=null)
		 * asker.sendMessageWithRole(idt.getTransferToAgentAddress(), new
		 * ObjectMessage<>(new TransferClosedSystemMessage(idt.getLocalID(),
		 * asker.getKernelAddress(), idt.getLocalID())),
		 * LocalCommunity.Roles.SOCKET_AGENT_ROLE); } } else {
		 * asker.sendMessageWithRole(aa, new ObjectMessage<>(new
		 * TransferClosedSystemMessage(null, asker.getKernelAddress(), idLocal)),
		 * LocalCommunity.Roles.SOCKET_AGENT_ROLE); } }
		 * 
		 * }
		 */

		protected void closeAllTransferID(boolean returnsToAsker) {
			AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();

			for (Map.Entry<Integer, InterfacedIDTransfer> e : distant_transfer_ids.entrySet()) {
				getMadkitConfig().networkProperties.removeStatsBandwitdh(e.getValue().getLocalID().getID());
				AgentAddress aa = transfer_agents.get(e.getValue().getLocalID().getID());
				if (aa == null) {
					if (e.getValue().getTransferToAgentAddress() != null)
						broadcastDataTowardEachIntermediatePeer(null,
								new TransferClosedSystemMessage(e.getValue().getLocalID(),
										e.getValue().getTransferToKernelAddress(), e.getValue().getLocalID(), true),
								e.getValue().getDistantID(), AbstractAgentSocket.this.getKernelAddress(), true);
				} else {
					sendMessageWithRole(aa,
							new ObjectMessage<>(new TransferClosedSystemMessage(null,
									AbstractAgentSocket.this.getKernelAddress(), e.getValue().getLocalID(), true)),
							LocalCommunity.Roles.SOCKET_AGENT_ROLE);
				}
				if (returnsToAsker) {
					TransferClosedSystemMessage t = new TransferClosedSystemMessage(e.getValue().getLocalID(),
							AbstractAgentSocket.this.distant_kernel_address, e.getValue().getLocalID(), true);
					t.setMessageLocker(new MessageLocker(null));
					AbstractAgentSocket.this.broadcastDataTowardEachIntermediatePeer(t, true);
				}
			}

			for (Map.Entry<Integer, HashMap<AgentAddress, InterfacedIDTransfer>> e : middle_transfer_ids.entrySet()) {

				if (e.getValue().size() > 0) {
					InterfacedIDTransfer idt = getMiddle(e.getKey().intValue(),
							((IndirectAgentSocket) AbstractAgentSocket.this).getParentAgentSocketAddress());
					if (idt != null) {
						AgentAddress aa = transfer_agents.get(idt.getLocalID().getID());

						if (aa == null) {
							if (idt.getTransferToAgentAddress() != null) {
								AbstractAgentSocket.this.broadcastDataTowardEachIntermediatePeer(
										((IndirectAgentSocket) AbstractAgentSocket.this).getParentAgentSocketAddress(),
										new TransferClosedSystemMessage(idt.getLocalID(),
												idt.getTransferToKernelAddress(), idt.getLocalID(), true),
										idt.getLocalID(), AbstractAgentSocket.this.getKernelAddress(), true);
							}
						} else {
							AbstractAgentSocket.this.sendMessageWithRole(aa,
									new ObjectMessage<>(new TransferClosedSystemMessage(null,
											AbstractAgentSocket.this.getKernelAddress(), idt.getLocalID(), true)),
									LocalCommunity.Roles.SOCKET_AGENT_ROLE);
						}
						if (returnsToAsker && idt.getTransferToAgentAddress() != null) {
							InterfacedIDTransfer idt2 = getMiddle(idt.getLocalID(), idt.getTransferToAgentAddress());
							KernelAddress ka = AbstractAgentSocket.this.getTransfertType()
									.equals(TransferAgent.NullIDTransfer) ? AbstractAgentSocket.this.getKernelAddress()
											: ((IndirectAgentSocket) AbstractAgentSocket.this)
													.getDistantKernelAddressRequester();
							TransferClosedSystemMessage t = new TransferClosedSystemMessage(getTransfertType(), ka,
									idt2.getLocalID(), true);
							t.setMessageLocker(new MessageLocker(null));
							broadcastDataTowardEachIntermediatePeer(t, true);
						}
					}
				}
			}

			middle_transfer_ids.clear();
			transfer_agents.clear();
			local_transfer_ids.clear();
			distant_transfer_ids.clear();
			propositions_in_progress.clear();

		}

		protected void putTransferPropositionSystemMessage(InterfacedIDTransfer id,
				TransferPropositionSystemMessage proposition) {
			putTransferPropositionSystemMessage(id.getLocalID(), proposition);
		}

		protected void putTransferPropositionSystemMessage(IDTransfer id,
				TransferPropositionSystemMessage proposition) {
			putTransferPropositionSystemMessage(id.getID(), proposition);
		}

		protected void putTransferPropositionSystemMessage(int id, TransferPropositionSystemMessage proposition) {
			if (proposition == null)
				throw new NullPointerException("null");
			propositions_in_progress.put(new Integer(id), proposition);
		}

		protected TransferPropositionSystemMessage getTransferPropositionSystemMessage(InterfacedIDTransfer id) {
			return getTransferPropositionSystemMessage(id.getLocalID());
		}

		protected TransferPropositionSystemMessage getTransferPropositionSystemMessage(IDTransfer id) {
			return getTransferPropositionSystemMessage(id.getID());
		}

		protected TransferPropositionSystemMessage getTransferPropositionSystemMessage(int id) {
			return propositions_in_progress.get(new Integer(id));
		}

		protected TransferPropositionSystemMessage removeTransferPropositionSystemMessage(InterfacedIDTransfer id) {
			return removeTransferPropositionSystemMessage(id.getLocalID());
		}

		protected TransferPropositionSystemMessage removeTransferPropositionSystemMessage(IDTransfer id) {
			return removeTransferPropositionSystemMessage(id.getID());
		}

		protected TransferPropositionSystemMessage removeTransferPropositionSystemMessage(int id) {
			return propositions_in_progress.remove(new Integer(id));
		}

		protected void putTransferAgentAddress(InterfacedIDTransfer id, AgentAddress transferAgentAddress) {
			putTransferAgentAddress(id.getLocalID(), transferAgentAddress);
		}

		protected void putTransferAgentAddress(IDTransfer id, AgentAddress transferAgentAddress) {
			putTransferAgentAddress(id.getID(), transferAgentAddress);
		}

		protected void putTransferAgentAddress(int id, AgentAddress transferAgentAddress) {
			if (transferAgentAddress == null)
				throw new NullPointerException("null");
			transfer_agents.put(new Integer(id), transferAgentAddress);
		}

		protected AgentAddress getTransferAgentAddress(InterfacedIDTransfer id) {
			return getTransferAgentAddress(id.getLocalID());
		}

		protected AgentAddress getTransferAgentAddress(IDTransfer id) {
			return getTransferAgentAddress(id.getID());
		}

		protected AgentAddress getTransferAgentAddress(int id) {
			return transfer_agents.get(new Integer(id));
		}

		protected AgentAddress removeTransferAgentAddress(InterfacedIDTransfer id) {
			return removeTransferAgentAddress(id.getLocalID());
		}

		protected AgentAddress removeTransferAgentAddress(IDTransfer id) {
			return removeTransferAgentAddress(id.getID());
		}

		protected AgentAddress removeTransferAgentAddress(int id) {
			return transfer_agents.remove(new Integer(id));
		}

		protected HashMap<AgentAddress, InterfacedIDTransfer> getMiddle(int id) {
			return middle_transfer_ids.get(new Integer(id));
		}

		protected InterfacedIDTransfer getMiddle(IDTransfer id, AgentAddress comingFrom) {
			return getMiddle(id.getID(), comingFrom);
		}

		protected InterfacedIDTransfer getMiddle(int id, AgentAddress comingFrom) {
			HashMap<AgentAddress, InterfacedIDTransfer> hm = middle_transfer_ids.get(new Integer(id));
			if (hm == null)
				return null;
			else
				return hm.get(comingFrom);
		}

		protected InterfacedIDTransfer removeMiddleGoingTo(int id, AgentAddress goingTo) {
			HashMap<AgentAddress, InterfacedIDTransfer> hm = middle_transfer_ids.get(new Integer(id));
			if (hm == null)
				return null;
			else {
				for (Iterator<InterfacedIDTransfer> it = hm.values().iterator(); it.hasNext();) {
					InterfacedIDTransfer idt = it.next();
					if (idt.getTransferToAgentAddress().equals(goingTo)) {
						it.remove();
						if (hm.size() == 0)
							middle_transfer_ids.remove(new Integer(id));
						AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();
						return idt;
					}

				}
				return null;
			}

		}

		protected InterfacedIDTransfer removeMiddleGoingTo(IDTransfer id, AgentAddress goingTo) {
			return removeMiddleGoingTo(id.getID(), goingTo);
		}

		protected InterfacedIDTransfer getMiddleGoingTo(int id, AgentAddress goingTo) {
			HashMap<AgentAddress, InterfacedIDTransfer> hm = middle_transfer_ids.get(new Integer(id));
			if (hm == null)
				return null;
			else {
				for (InterfacedIDTransfer idt : hm.values()) {
					if (idt.getTransferToAgentAddress().equals(goingTo))
						return idt;
				}
				return null;
			}
		}

		protected InterfacedIDTransfer getMiddleGoingTo(IDTransfer id, AgentAddress goingTo) {
			return getMiddleGoingTo(id.getID(), goingTo);
		}

		protected void putMiddle(int id, HashMap<AgentAddress, InterfacedIDTransfer> middle) {
			if (middle == null)
				throw new NullPointerException("middle");
			middle_transfer_ids.put(new Integer(id), middle);
		}

		protected void putMiddle(AgentAddress comingFrom, InterfacedIDTransfer idTransfer) {
			if (idTransfer == null)
				throw new NullPointerException("idTransfer");
			HashMap<AgentAddress, InterfacedIDTransfer> hm = middle_transfer_ids
					.get(new Integer(idTransfer.getLocalID().getID()));
			if (hm == null) {
				hm = new HashMap<>();
				middle_transfer_ids.put(new Integer(idTransfer.getLocalID().getID()), hm);
			}
			hm.put(comingFrom, idTransfer);
			AbstractAgentSocket.this.addTaskTransferCheckerIfNecessary();
		}

		protected HashMap<AgentAddress, InterfacedIDTransfer> removeMiddle(IDTransfer id) {
			return removeMiddle(id.getID());
		}

		protected HashMap<AgentAddress, InterfacedIDTransfer> removeMiddle(int id) {
			return middle_transfer_ids.remove(new Integer(id));
		}

		protected HashMap<AgentAddress, InterfacedIDTransfer> removeMiddleFromDistantID(int id) {
			for (Iterator<Map.Entry<Integer, HashMap<AgentAddress, InterfacedIDTransfer>>> it = middle_transfer_ids
					.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Integer, HashMap<AgentAddress, InterfacedIDTransfer>> e = it.next();
				for (InterfacedIDTransfer idt : e.getValue().values()) {
					if (idt.getDistantID() != null && idt.getDistantID().equals(id)) {
						try {
							it.remove();
							return e.getValue();
						} finally {
							AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();
						}
					}
				}
			}
			return null;
		}

		protected InterfacedIDTransfer getLocal(int id) {
			return local_transfer_ids.get(new Integer(id));
		}

		protected InterfacedIDTransfer getLocal(IDTransfer id) {
			return getLocal(id.getID());
		}

		protected void putLocal(InterfacedIDTransfer idTransfer) {
			if (idTransfer == null)
				throw new NullPointerException("idTransfer");
			local_transfer_ids.put(new Integer(idTransfer.getLocalID().getID()), idTransfer);
			AbstractAgentSocket.this.addTaskTransferCheckerIfNecessary();
		}

		protected InterfacedIDTransfer removeLocal(int id) {
			return local_transfer_ids.remove(new Integer(id));
		}

		protected InterfacedIDTransfer removeLocal(IDTransfer id) {
			return removeLocal(id.getID());
		}

		protected InterfacedIDTransfer removeLocalFromDistantID(int id) {
			for (Iterator<InterfacedIDTransfer> it = local_transfer_ids.values().iterator(); it.hasNext();) {
				InterfacedIDTransfer idt = it.next();
				if (idt.getDistantID() != null && idt.getDistantID().getID() == id) {
					it.remove();
					AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();
					return idt;
				}
			}
			return null;
		}

		protected InterfacedIDTransfer removeLocalFromDistantID(IDTransfer id) {
			return removeLocalFromDistantID(id.getID());
		}

		protected InterfacedIDTransfer getDistant(IDTransfer id) {
			return getDistant(id.getID());
		}

		protected InterfacedIDTransfer getDistant(int id) {
			return distant_transfer_ids.get(new Integer(id));
		}

		protected void putDistant(InterfacedIDTransfer idTransfer) {
			if (idTransfer == null)
				throw new NullPointerException("idTransfer");
			distant_transfer_ids.put(new Integer(idTransfer.getDistantID().getID()), idTransfer);
			AbstractAgentSocket.this.addTaskTransferCheckerIfNecessary();
		}

		protected InterfacedIDTransfer removeDistant(IDTransfer id) {
			return removeDistant(id.getID());
		}

		protected InterfacedIDTransfer removeDistant(int id) {
			return distant_transfer_ids.remove(new Integer(id));
		}

		protected InterfacedIDTransfer removeDistantFromLocalID(int id) {
			for (Iterator<InterfacedIDTransfer> it = distant_transfer_ids.values().iterator(); it.hasNext();) {
				InterfacedIDTransfer idt = it.next();
				if (idt.getDistantID() != null && idt.getDistantID().getID() == id) {
					it.remove();
					AbstractAgentSocket.this.removeTaskTransferCheckerIfNecessary();
					return idt;
				}
			}
			return null;
		}

		protected InterfacedIDTransfer removeDistantFromLocalID(IDTransfer id) {
			return removeDistantFromLocalID(id.getID());
		}

	}

	protected static class CheckDeadTransferNodes extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4796209575192698417L;

	}

}
