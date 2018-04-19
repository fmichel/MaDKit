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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.exceptions.OverflowException;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.NetworkAgent.StopNetworkMessage;
import com.distrimind.madkit.kernel.Replies;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;
import com.distrimind.madkit.kernel.network.AskForTransferMessage.CandidateForTransfer;
import com.distrimind.madkit.kernel.network.AskForTransferMessage.InitiateTransferConnection;
import com.distrimind.madkit.kernel.network.connection.PointToPointTransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.message.hook.TransferEventMessage;
import com.distrimind.madkit.message.hook.TransferEventMessage.TransferEventType;
import com.distrimind.madkit.util.OOSUtils;
import com.distrimind.madkit.util.SerializableAndSizable;
import com.distrimind.util.IDGeneratorInt;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
class TransferAgent extends AgentFakeThread {
	protected final AskForTransferMessage originalAskMessage;
	private CandidateForTransfer candidate1 = null, candidate2 = null;
	private boolean directConnection2Tried = false;
	private TransferConfirmationSystemMessage confirmation1 = null, confirmation2 = null;
	private int numberOfIntermediatePeers = 0;
	private State state = State.TRANSFER_NOT_ACTIVE;
	protected IDTransfer idTransfer = null;
	// private boolean oneconnectionalreadydone=false;
	protected AtomicReference<TaskID> timeElapsedTask = new AtomicReference<TaskID>(null);
	protected final AtomicLong timeElapsed = new AtomicLong(-1);
	private boolean stopNetwork = false;

	private enum State {
		TRANSFER_NOT_ACTIVE, TRANSFER_CONNEXION_IN_PROGRESS, TRANSFER_CLOSING, TRANSFER_ACTIVE;
	}

	TransferAgent(AskForTransferMessage message) {
		super();
		if (message == null)
			throw new NullPointerException("message");
		this.originalAskMessage = message;
	}

	@Override
	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching agent for " + originalAskMessage + "...");
		this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.TRANSFER_AGENT_ROLE);

		askForTransferCandidate();
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Agent for " + originalAskMessage + " LAUNCHED !");
	}

	private void askForTransferCandidate() {
		broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE,
				originalAskMessage, true, LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message == null)
			return;

		cancelTimeElapsedTask();

		if (_message.getClass() == Replies.class) {
			Replies replies = (Replies) _message;
			if (replies.getConversationID().equals(originalAskMessage.getConversationID())) {

				boolean tooMuchCandidates = false;
				for (Message m : replies.getReplies()) {
					@SuppressWarnings("unchecked")
					CandidateForTransfer candidate = ((ObjectMessage<CandidateForTransfer>) m).getContent();
					if (candidate.isConcernedBy(originalAskMessage)) {
						if (candidate2 != null) {
							tooMuchCandidates = true;
						} else if (candidate1 != null) {
							if (candidate1.getKernelAddress().equals(candidate.getKernelAddress())) {
								tooMuchCandidates = true;
							} else {
								candidate2 = candidate;
								numberOfIntermediatePeers += candidate.getNumberOfIntermediatePeers();
							}
						} else {
							candidate1 = candidate;
							numberOfIntermediatePeers += candidate.getNumberOfIntermediatePeers();
						}
					}
				}
				if (tooMuchCandidates) {
					resetTransfer();
					MadkitKernelAccess.informHooks(this, new TransferEventMessage(idTransfer.getID(),
							originalAskMessage, TransferEventType.CONNECTION_IMPOSSIBLE_BECAUSE_TOO_MUCH_CANDIDATES));
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Too much candidate for transfer connection : " + originalAskMessage);

					this.killAgent(this);
				} else if (candidate1 == null || candidate2 == null) {
					MadkitKernelAccess.informHooks(this,
							new TransferEventMessage(idTransfer == null ? NullIDTransfer.getID() : idTransfer.getID(),
									originalAskMessage,
									TransferEventType.CONNECTION_IMPOSSIBLE_BECAUSE_NOT_ENOUGH_CANDIDATES));
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Not enougth candidates for transfer connection : " + originalAskMessage);
					resetTransfer();
					this.killAgent(this);
				} else {
					state = State.TRANSFER_CONNEXION_IN_PROGRESS;
					if (logger.isLoggable(Level.FINEST))
						logger.fine(state.toString());
					try {
						idTransfer = IDTransfer.generateIDTransfer(MadkitKernelAccess.getIDTransferGenerator(this));

						InetSocketAddress isa = null;
						if (originalAskMessage.getType()
								.equals(AskForTransferMessage.Type.TRY_DIRECT_CONNECTION_FIRST_OR_TRANSFER)) {
							isa = candidate1.getInetSocketAddress(candidate2.getInetAddress().getAddress());
							if (isa == null) {
								isa = candidate2.getInetSocketAddress(candidate1.getInetAddress().getAddress());
								directConnection2Tried = true;
							}
						}

						if (isa == null) {
							initiateTransferConnection();
						} else {
							if (logger != null && logger.isLoggable(Level.FINER))
								logger.finer("Try direct connection first : " + originalAskMessage);

							if (directConnection2Tried) {
								sendMessageWithRole(candidate1.getAgentAddress(),
										new ObjectMessage<>(new TryDirectConnection(idTransfer, isa)),
										LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
							} else {
								sendMessageWithRole(candidate2.getAgentAddress(),
										new ObjectMessage<>(new TryDirectConnection(idTransfer, isa)),
										LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
							}
						}
						launchTimeElapsedTask();
					} catch (OverflowException e) {
						if (logger != null)
							logger.severeLog("Impossible to establish a transfer connection", e);
						MadkitKernelAccess.informHooks(this, new TransferEventMessage(idTransfer.getID(),
								originalAskMessage, TransferEventType.CONNECTION_IMPOSSIBLE));
						this.killAgent(this);
					}
				}
			}
		} else if (_message.getClass() == AskForTransferMessage.class) {
			AskForTransferMessage m = (AskForTransferMessage) _message;
			if (m.getType().equals(AskForTransferMessage.Type.DISCONNECT) && originalAskMessage.isConcernedBy(m)) {
				stopTransfer(false, false, null);
			}
		} else if (_message instanceof StopNetworkMessage) {
			stopNetwork = true;
		} else if (_message.getClass() == ObjectMessage.class) {
			ObjectMessage<?> om = (ObjectMessage<?>) _message;
			if (om.getContent() == null)
				return;
			if (om.getContent().getClass() == TransferConfirmationSystemMessage.class) {

				TransferConfirmationSystemMessage t = (TransferConfirmationSystemMessage) om.getContent();
				boolean ok = true;
				if (_message.getSender().equals(candidate1.getAgentAddress())) {
					confirmation1 = t;
				} else if (_message.getSender().equals(candidate2.getAgentAddress())) {
					confirmation2 = t;
				} else {
					ok = false;
					processInvalidProcess(null, "Invalid transfer confirmation message", false);
				}
				if (ok) {
					if (confirmation1 != null && confirmation2 != null) {
						getMadkitConfig().networkProperties.addIfNecessaryAndGetStatsBandwitdh(this.idTransfer.getID());

						sendMessageWithRole(candidate1.getAgentAddress(),
								new ObjectMessage<>(new TransferConfirmationSystemMessage(null,
										candidate1.getKernelAddress(), candidate2.getKernelAddress(),
										confirmation1.getMyIDTransfer(), idTransfer,
										confirmation1.getNumberOfSubBlocks(), true, candidate2.getInetAddress(), null)),
								LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
						sendMessageWithRole(candidate2.getAgentAddress(),
								new ObjectMessage<>(new TransferConfirmationSystemMessage(null,
										candidate2.getKernelAddress(), candidate1.getKernelAddress(),
										confirmation2.getMyIDTransfer(), idTransfer,
										confirmation2.getNumberOfSubBlocks(), true, candidate1.getInetAddress(), null)),
								LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
						// oneconnectionalreadydone=true;
						state = State.TRANSFER_ACTIVE;
						MadkitKernelAccess.informHooks(this, new TransferEventMessage(idTransfer.getID(),
								originalAskMessage, TransferEventType.TRANSFER_EFFECTIVE));
						if (logger != null)
							logger.info("Transfer " + idTransfer + " active : " + originalAskMessage);

					}
				}
			} else if (om.getContent().getClass() == TransferImpossibleSystemMessage.class) {
				TransferImpossibleSystemMessage t = (TransferImpossibleSystemMessage) om.getContent();
				if (t.getYourIDTransfer().equals(idTransfer)) {
					boolean ok = true;
					if (_message.getSender().equals(candidate1.getAgentAddress())) {
						sendMessageWithRole(candidate2.getAgentAddress(),
								new ObjectMessage<>(new TransferImpossibleSystemMessageFromMiddlePeer(null,
										candidate2.getKernelAddress(), null, idTransfer)),
								LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
					} else if (_message.getSender().equals(candidate2.getAgentAddress())) {
						sendMessageWithRole(candidate1.getAgentAddress(),
								new ObjectMessage<>(new TransferImpossibleSystemMessageFromMiddlePeer(null,
										candidate1.getKernelAddress(), null, idTransfer)),
								LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
					} else {
						ok = false;
						processInvalidProcess(null, "Invalid transfer impossible message", false);
					}
					if (ok) {
						resetTransfer();
						MadkitKernelAccess.informHooks(this, new TransferEventMessage(idTransfer.getID(),
								originalAskMessage, TransferEventType.CONNECTION_IMPOSSIBLE));
						if (logger != null)
							logger.info("Transfer impossible : " + originalAskMessage);

						this.killAgent(this);
					}
				}
			} else if (om.getContent().getClass() == TransferClosedSystemMessage.class) {
				TransferClosedSystemMessage t = (TransferClosedSystemMessage) om.getContent();
				if (t.getIdTransfer().equals(idTransfer)) {
					boolean ok = false;
					if (_message.getSender().equals(candidate1.getAgentAddress())) {
						// sendMessageWithRole(candidate2.getAgentAddress(), new ObjectMessage<>(new
						// TransferClosedSystemMessage(t.getIdTransferDestination(),
						// candidate2.getKernelAddress(), idTransfer, t.isLastPass())),
						// LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
						ok = true;
					} else if (_message.getSender().equals(candidate2.getAgentAddress())) {
						// sendMessageWithRole(candidate1.getAgentAddress(), new ObjectMessage<>(new
						// TransferClosedSystemMessage(t.getIdTransferDestination(),
						// candidate1.getKernelAddress(), idTransfer, t.isLastPass())),
						// LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
						ok = true;
					}
					if (ok) {
						// stopTransfer(oneconnectionalreadydone);//TODO check if reconnection is
						// possible
						stopTransfer(false, t.isLastPass(), _message.getSender());
					} else {

						processInvalidProcess(null, "Invalid transfer closed message", false);
					}
				}
			} else if (om.getContent().getClass() == DirectConnectionFailed.class) {
				DirectConnectionFailed d = (DirectConnectionFailed) om.getContent();
				if (d.getIDTransfer().equals(idTransfer) && state == State.TRANSFER_CONNEXION_IN_PROGRESS) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Direct connection failed : " + originalAskMessage);

					if (directConnection2Tried) {
						initiateTransferConnection();
					} else {
						InetSocketAddress isa = candidate2
								.getInetSocketAddress(candidate1.getInetAddress().getAddress());
						directConnection2Tried = true;

						if (isa == null) {
							initiateTransferConnection();
						} else {
							sendMessageWithRole(candidate1.getAgentAddress(),
									new ObjectMessage<>(new TryDirectConnection(idTransfer, isa)),
									LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
						}
						launchTimeElapsedTask();
					}
				}
			} else if (om.getContent().getClass() == DirectConnectionSuceeded.class) {
				DirectConnectionSuceeded d = (DirectConnectionSuceeded) om.getContent();
				if (d.getIDTransfer().equals(idTransfer) && state == State.TRANSFER_CONNEXION_IN_PROGRESS) {
					MadkitKernelAccess.informHooks(this, new TransferEventMessage(idTransfer.getID(),
							originalAskMessage, TransferEventType.DIRECT_CONNECTION_EFFECTIVE));
					resetTransfer();
					if (logger != null)
						logger.info("Direct connection established : " + originalAskMessage);

					this.killAgent(this);
				}
			}

		}

	}

	private void cancelTimeElapsedTask() {
		TaskID c = timeElapsedTask.getAndSet(null);
		if (c != null) {
			timeElapsed.set(-1);
			cancelTask(c, false);
		}
	}

	private void launchTimeElapsedTask() {
		long d = System.currentTimeMillis()
				+ getMadkitConfig().networkProperties.durationBeforeCancelingTransferConnection;
		timeElapsed.set(d);
		timeElapsedTask.set(scheduleTask(new Task<>(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (timeElapsedTask.getAndSet(null) != null) {
					long duration = timeElapsed.getAndSet(-1);
					if (duration != -1 && duration >= System.currentTimeMillis()) {
						MadkitKernelAccess.informHooks(TransferAgent.this, new TransferEventMessage(idTransfer.getID(),
								originalAskMessage, TransferEventType.CONNECTION_IMPOSSIBLE_BECAUSE_TIME_ELAPSED));
						resetTransfer();
						TransferAgent.this.killAgent(TransferAgent.this);
					}
				}
				return null;
			}
		}, d)));
	}

	private void processInvalidProcess(AgentAddress source, String message, boolean candidate_to_ban) {
		processInvalidProcess(source, message, null, candidate_to_ban);
	}

	private void processInvalidProcess(AgentAddress source, String message, Exception e, boolean candidate_to_ban) {
		if (logger != null) {
			if (e == null)
				logger.severeLog(message == null ? "Invalid process" : message);
			else
				logger.severeLog(message == null ? "Invalid process" : message, e);
		}

		if (source == null) {
			sendMessageWithRole(source,
					new AnomalyDetectedMessage(candidate_to_ban, candidate1.getKernelAddress(), message),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		} else {
			sendMessageWithRole(candidate1.getAgentAddress(),
					new AnomalyDetectedMessage(candidate_to_ban, candidate1.getKernelAddress(), message),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
			sendMessageWithRole(candidate2.getAgentAddress(),
					new AnomalyDetectedMessage(candidate_to_ban, candidate2.getKernelAddress(), message),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		}
	}

	@Override
	public void end() {
		cancelTimeElapsedTask();
		stopTransfer(false, true, null);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Agent for " + originalAskMessage + " KILLED !");
	}

	private void initiateTransferConnection() {
		if (logger != null)
			logger.finer("Initiate transfer connection : " + originalAskMessage);
		
		sendMessageWithRole(candidate2.getAgentAddress(),
				new ObjectMessage<InitiateTransferConnection>(
						originalAskMessage.getIntiateConnectionMessage(candidate1.getAgentAddress(),
								candidate1.getKernelAddress(), idTransfer, numberOfIntermediatePeers, false)),
				LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
		sendMessageWithRole(candidate1.getAgentAddress(),
				new ObjectMessage<InitiateTransferConnection>(
						originalAskMessage.getIntiateConnectionMessage(candidate2.getAgentAddress(),
								candidate2.getKernelAddress(), idTransfer, numberOfIntermediatePeers, true)),
				LocalCommunity.Roles.TRANSFER_AGENT_ROLE);

	}

	protected void resetTransfer() {
		if (idTransfer != null)
			getMadkitConfig().networkProperties.removeStatsBandwitdh(this.idTransfer.getID());
		candidate1 = null;
		candidate2 = null;
		numberOfIntermediatePeers = 0;
		idTransfer = null;
		directConnection2Tried = false;
		// oneconnectionalreadydone=false;
		state = State.TRANSFER_NOT_ACTIVE;
	}

	private boolean isTransferActiveOrConnectionInProgress() {
		return state == State.TRANSFER_ACTIVE || state == State.TRANSFER_CONNEXION_IN_PROGRESS
				|| state == State.TRANSFER_CLOSING;
	}

	private void stopTransfer(boolean tryReconnection, boolean lastPass, AgentAddress askedFrom) {
		if (isTransferActiveOrConnectionInProgress()) {
			tryReconnection &= !stopNetwork;
			if (!lastPass || state == State.TRANSFER_ACTIVE) {
				if (!candidate1.getAgentAddress().equals(askedFrom)) {
					sendMessageWithRole(
							candidate1.getAgentAddress(), new ObjectMessage<>(new TransferClosedSystemMessage(null,
									candidate1.getKernelAddress(), idTransfer, lastPass)),
							LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
				}
				if (!candidate2.getAgentAddress().equals(askedFrom)) {
					sendMessageWithRole(
							candidate2.getAgentAddress(), new ObjectMessage<>(new TransferClosedSystemMessage(null,
									candidate2.getKernelAddress(), idTransfer, lastPass)),
							LocalCommunity.Roles.TRANSFER_AGENT_ROLE);
				}
			}
			IDTransfer idTransfer = this.idTransfer;
			AskForTransferMessage originalAskMessage = this.originalAskMessage;
			state = State.TRANSFER_CLOSING;
			if (lastPass) {
				resetTransfer();

				MadkitKernelAccess.informHooks(this,
						new TransferEventMessage(idTransfer.getID(), originalAskMessage,
								tryReconnection ? TransferEventType.TRANSFER_DISCONNECTED_BUT_TRYING_RECONNECTION
										: TransferEventType.TRANSFER_DISCONNECTED));
				if (logger != null)
					logger.info(originalAskMessage + " STOPED !");
				if (getState().compareTo(AbstractAgent.State.ENDING) < 0) {
					if (tryReconnection) {
						askForTransferCandidate();
					} else {
						this.killAgent(this);
					}
				}
			}

		}
	}

	final static class IDTransfer implements SerializableAndSizable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6820580619502727187L;

		private final transient IDGeneratorInt generator_id_transfert;
		private final int id;

		private IDTransfer() {
			generator_id_transfert = null;
			id = -1;
		}

		boolean isGenerated() {
			return generator_id_transfert != null;
		}

		public static IDTransfer generateIDTransfer(IDGeneratorInt generator_id_transfert) throws OverflowException {
			return new IDTransfer(generator_id_transfert);
		}

		private IDTransfer(IDGeneratorInt generator_id_transfert) throws OverflowException {

			this.generator_id_transfert = generator_id_transfert;
			this.id = getNewIDTransfer();
		}

		@Override
		public void finalize() {
			if (generator_id_transfert != null)
				removeIDTransfer(id);
		}

		public int getID() {
			return id;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public String toString() {
			return "IDTransfer[" + id + "]";
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o == this)
				return true;
			if (o.getClass() == IDTransfer.class || o.getClass() == Integer.class)
				return id == o.hashCode();
			return false;
		}

		public boolean equals(int id) {
			return this.id == id;
		}

		int numberOfValidGeneratedID() {
			synchronized (generator_id_transfert) {
				return generator_id_transfert.getNumberOfMemorizedIds();
			}
		}

		private int getNewIDTransfer() throws OverflowException {
			synchronized (generator_id_transfert) {
				try {
					int res = generator_id_transfert.getNewID();
					if (res == -1) {
						res = generator_id_transfert.getNewID();
					}
					return res;
				} catch (OutOfMemoryError e) {
					throw new OverflowException("Too much ID transfers has been generated !", e);
				}

			}
		}

		private boolean removeIDTransfer(int _id) {
			synchronized (generator_id_transfert) {
				return generator_id_transfert.removeID(_id);
			}
		}

		@Override
		public int getInternalSerializedSize() {
			return 4;
		}

	}

	public static final IDTransfer NullIDTransfer = new IDTransfer();

	static class InterfacedIDTransfer implements Cloneable {
		private final IDTransfer local_id;
		private IDTransfer distant_id;
		private AgentAddress transferToAgentAddress;
		private final KernelAddress transferToKernelAddress;
		private TransferedBlockChecker transferBlockChecker;
		private PointToPointTransferedBlockChecker lastPointToPointTransferedBlockChecker;
		private long lastAccessUTC;

		InterfacedIDTransfer(IDTransfer local_id, AgentAddress transferToAgentAddress,
				KernelAddress transferToKernelAddress) {
			this.local_id = local_id;
			distant_id = null;
			this.transferToAgentAddress = transferToAgentAddress;
			this.transferToKernelAddress = transferToKernelAddress;
			this.transferBlockChecker = null;
			lastAccessUTC = System.currentTimeMillis();
		}

		private InterfacedIDTransfer(InterfacedIDTransfer id) {
			this.local_id = id.local_id;
			this.distant_id = id.distant_id;
			this.transferToAgentAddress = id.transferToAgentAddress;
			this.transferToKernelAddress = id.transferToKernelAddress;
			this.transferBlockChecker = id.transferBlockChecker;
			this.lastAccessUTC = id.lastAccessUTC;
		}

		@Override
		public String toString() {
			return "InterfacedIDTransfer[localID=" + local_id + ", distantID=" + distant_id + ", kernelAddress="
					+ transferToKernelAddress + "]";
		}

		@Override
		public InterfacedIDTransfer clone() {
			return new InterfacedIDTransfer(this);
		}

		KernelAddress getTransferToKernelAddress() {
			lastAccessUTC = System.currentTimeMillis();
			return transferToKernelAddress;
		}

		IDTransfer getDistantID() {
			lastAccessUTC = System.currentTimeMillis();
			return distant_id;
		}

		void setDistantID(IDTransfer _distant_id) {
			lastAccessUTC = System.currentTimeMillis();
			distant_id = _distant_id;
		}

		IDTransfer getLocalID() {
			lastAccessUTC = System.currentTimeMillis();
			return local_id;
		}

		AgentAddress getTransferToAgentAddress() {
			lastAccessUTC = System.currentTimeMillis();
			return transferToAgentAddress;
		}

		void setTransferToAgentAddress(AgentAddress aa) {
			lastAccessUTC = System.currentTimeMillis();
			this.transferToAgentAddress = aa;
		}

		TransferedBlockChecker getTransferBlockChecker() {
			lastAccessUTC = System.currentTimeMillis();
			return transferBlockChecker;
		}

		void setTransferBlockChecker(TransferedBlockChecker _transferBlockChecker) {
			lastAccessUTC = System.currentTimeMillis();
			transferBlockChecker = _transferBlockChecker;
		}
		void setLastPointToPointTransferedBlockChecker(PointToPointTransferedBlockChecker lastPointToPointTransferedBlockChecker) {
			lastAccessUTC = System.currentTimeMillis();
			this.lastPointToPointTransferedBlockChecker = lastPointToPointTransferedBlockChecker;
		}
		
		PointToPointTransferedBlockChecker getLastPointToPointTransferedBlockChecker()
		{
			return lastPointToPointTransferedBlockChecker;
		}
		
		

		long getLastAccessUTC() {
			return lastAccessUTC;
		}

	}

	static class TryDirectConnection extends DirectConnection {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6680419937761208277L;

		private InetSocketAddress inetSocketAddress;

		protected TryDirectConnection(IDTransfer idTransfer, InetSocketAddress inetSocketAddress) {
			super(idTransfer);
			if (inetSocketAddress == null)
				throw new NullPointerException("inetSocketAddress");
			if (inetSocketAddress.getAddress() == null)
				throw new NullPointerException("inetSocketAddress.getAddress()");
			if (inetSocketAddress.getPort() < 0)
				throw new IllegalArgumentException();
			this.inetSocketAddress = inetSocketAddress;
		}

		InetSocketAddress getInetSocketAddress() {
			return inetSocketAddress;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[idTransfer=" + idTransfer + ", inetSocketAddress=" + inetSocketAddress
					+ "]";
		}

		
		
		@Override
		public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			super.readAndCheckObject(in);
			inetSocketAddress=OOSUtils.readInetSocketAddress(in, false);
		}

		@Override
		public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
			super.writeAndCheckObject(oos);
			OOSUtils.writeInetSocketAddress(oos, inetSocketAddress, false);
			if (inetSocketAddress.getPort() < 0)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			
		}
	}

	static class DirectConnection implements SystemMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9091617361664811235L;
		protected IDTransfer idTransfer;

		DirectConnection(IDTransfer idTransfer) {
			if (idTransfer == null)
				throw new NullPointerException("idTransfer");
			this.idTransfer = idTransfer;
		}

		IDTransfer getIDTransfer() {
			return idTransfer;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[idTransfer=" + idTransfer + "]";
		}

		

		@Override
		public boolean excludedFromEncryption() {
			return false;
		}

		@Override
		public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			Object o=in.readObject();
			if (!(o instanceof IDTransfer))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			idTransfer=(IDTransfer)o;
			
		}

		@Override
		public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
			oos.writeObject(idTransfer);

			
		}
	}

	static class DirectConnectionFailed extends DirectConnection {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6123269171761664791L;

		DirectConnectionFailed(IDTransfer _idTransfer) {
			super(_idTransfer);
		}

	}

	static class DirectConnectionSuceeded extends DirectConnection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2423853120927358753L;

		DirectConnectionSuceeded(IDTransfer _idTransfer) {
			super(_idTransfer);
		}
	}

}
