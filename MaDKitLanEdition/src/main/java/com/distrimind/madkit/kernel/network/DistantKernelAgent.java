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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.exceptions.MadkitException;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.exceptions.SelfKillException;
import com.distrimind.madkit.io.RandomByteArrayInputStream;
import com.distrimind.madkit.io.RandomByteArrayOutputStream;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.madkit.io.RandomOutputStream;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.BigDataPropositionMessage;
import com.distrimind.madkit.kernel.BigDataResultMessage;
import com.distrimind.madkit.kernel.BigDataTransferID;
import com.distrimind.madkit.kernel.CGRSynchro;
import com.distrimind.madkit.kernel.CGRSynchro.Code;
import com.distrimind.madkit.kernel.ConversationID;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.jdkrewrite.concurrent.LockerCondition;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.MultiGroup;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;
import com.distrimind.madkit.kernel.network.AbstractAgentSocket.AgentSocketKilled;
import com.distrimind.madkit.kernel.network.AbstractAgentSocket.Groups;
import com.distrimind.madkit.kernel.network.AbstractAgentSocket.ReceivedBlockData;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.message.hook.NetworkGroupsAccessEvent;
import com.distrimind.madkit.message.hook.NetworkLoginAccessEvent;
import com.distrimind.madkit.message.hook.DistantKernelAgentEventMessage;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;
import com.distrimind.util.IDGeneratorInt;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.MessageDigestType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * Represent a distant Madkit kernel
 * 
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MadkitLanEdition 1.0
 */
class DistantKernelAgent extends AgentFakeThread {

	private final ArrayList<AgentSocketData> agents_socket = new ArrayList<>();
	private final ArrayList<AgentSocketData> indirect_agents_socket = new ArrayList<>();

	protected KernelAddressInterfaced distant_kernel_address = null;
	private IDGeneratorInt packet_id_generator = new IDGeneratorInt();
	private Group distant_accepted_groups[] = new Group[0];
	private AbstractSecureRandom random;
	private boolean kernelAddressActivated = false;
	private long lastAgentsUpdate = -1;

	private ArrayList<PairOfIdentifiers> accepted_identifiers = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_accepted_identifiers = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_denied_identifiers = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_unlogged_identifiers = new ArrayList<>();
	private ArrayList<Group> localAcceptedAndRequestedGroups = new ArrayList<>();
	// private MultiGroup localGeneralAcceptedGroups=new MultiGroup();

	private StatsBandwidth stats = null;
	protected AtomicLong totalDataInQueue = new AtomicLong(0);

	private final AtomicBoolean transfertPaused = new AtomicBoolean(false);
	final AtomicReference<ExceededDataQueueSize> globalExeceededDataQueueSize = new AtomicReference<>(null);
	private HashMap<Integer, BigPacketData> packetsDataInQueue = new HashMap<>();
	NetworkBlackboard networkBlacboard = null;
	protected TaskID taskForPurgeCheck = null;
	private ArrayList<ObjectMessage<SecretMessage>> differedSecretMessages = null;

	DistantKernelAgent() {
		super();
	}

	@Override
	public void end() {
		if (distant_kernel_address != null && kernelAddressActivated)
			networkBlacboard.removeDistantKernelAddressInterfaced(distant_kernel_address);
		if (stats != null && kernelAddressActivated)
			getMadkitConfig().networkProperties.removeStatsBandwitdh(distant_kernel_address);

		purgeTotalDataQueue();

		/*
		 * if (distant_kernel_address!=null)
		 * networkBlacboard.unlockSimultaneousConnections(distant_kernel_address);
		 */

		if (distant_kernel_address != null && kernelAddressActivated) {
			MadkitKernelAccess.informHooks(this, new DistantKernelAgentEventMessage(
					AgentActionEvent.DISTANT_KERNEL_DISCONNECTED, distant_kernel_address));
			if (logger != null)
				logger.info("Distant kernel agent deconnected : " + distant_kernel_address);
		}

		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("DistantKernelAgent (" + this.distant_kernel_address + ") KILLED !");

	}

	private boolean broadcastLocalLanMessage(ArrayList<AgentSocketData> agents_socket, BroadcastLocalLanMessage message,
			AbstractGroup groups_lacking) throws MadkitException {

		for (int i = 0; i < agents_socket.size(); i++) {
			AgentSocketData asd = agents_socket.get(i);
			if (asd.isUsable()) {
				AbstractGroup concerned_groups = asd.distant_accessible_and_requested_groups.intersect(groups_lacking);
				if (!concerned_groups.isEmpty()) {
					message.getMessageLocker().lock();
					sendData(asd.getAgentAddress(), message.getBroadcastMessage(concerned_groups), false,
							message.getMessageLocker(), false, asd.getCounterSelector());

					groups_lacking = groups_lacking.minus(concerned_groups);
					if (groups_lacking.isEmpty()) {
						return false;
					}
				}
			}
		}
		return true;

	}

	private boolean removeAgentSocketData(ArrayList<AgentSocketData> agents_socket, AgentSocketKilled _message) {
		for (Iterator<AgentSocketData> it = agents_socket.iterator(); it.hasNext();) {
			AgentSocketData asd = it.next();
			if (asd.isConcernedBy(_message.getSender())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	/*
	 * static class ResendMessageToDistantKernel extends Message { private static
	 * final long serialVersionUID = -5906998698550621029L;
	 * 
	 * final Message messageToResend; ResendMessageToDistantKernel(Message
	 * messageToResend) { this.messageToResend=messageToResend; } }
	 */
	private volatile boolean concurrent = false;

	@Override
	protected void liveByStep(Message _message) {
		if (concurrent)
			throw new ConcurrentModificationException();
		concurrent = true;
		try {
			if (getState().compareTo(AbstractAgent.State.WAIT_FOR_KILL) >= 0) {
				if (logger != null)
					logger.severe("Unexpected access");
				return;
			}

			try {
				if (_message instanceof LocalLanMessage) {
					if (distant_kernel_address == null || !kernelAddressActivated || !hasUsableDistantSocketAgent()) {
						sendReplyEmpty(_message);
						return;
					}
					try {
						if (_message.getClass() == BroadcastLocalLanMessage.class) {
							BroadcastLocalLanMessage message = (BroadcastLocalLanMessage) _message;
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("BroadcastLocalLanMessage to route (distantInterfacedKernelAddress="
										+ distant_kernel_address + ") : " + _message);

							message.getMessageLocker().lock();
							if (!message.abstract_group.isEmpty()) {
								AbstractGroup groups_lacking = message.abstract_group.clone();
								updateBestAgent();

								if (broadcastLocalLanMessage(agents_socket, message, groups_lacking))
									broadcastLocalLanMessage(indirect_agents_socket, message, groups_lacking);
							}
							message.getMessageLocker().unlock();

						} else if (_message.getClass() == DirectLocalLanMessage.class) {
							DirectLocalLanMessage message = (DirectLocalLanMessage) _message;
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("DirectLocalLanMessage to route (distantInterfacedKernelAddress="
										+ distant_kernel_address + ") : " + _message);

							message.getMessageLocker().lock();
							// message.setIDPacket(packet_id_generator.getNewID());

							AgentSocketData asd = getBestAgentSocket(message.getOriginalMessage().getReceiver(), true);
							if (asd != null) {
								Message m = message.getOriginalMessage();
								if (m instanceof BigDataPropositionMessage) {
									BigDataPropositionMessage bgpm = (BigDataPropositionMessage) m;
									try {
										addBigDataInQueue(asd, bgpm);

										sendData(asd.getAgentAddress(), new DirectLanMessage(bgpm), false,
												message.getMessageLocker(), false, asd.getCounterSelector());
									} catch (PacketException e) {
										if (logger != null)
											logger.severeLog("Anomaly detected", e);
										processInvalidMessage(_message, false);
										message.getMessageLocker().unlock();
									}
								} else {
									sendData(asd.getAgentAddress(), new DirectLanMessage(m), false,
											message.getMessageLocker(), false, asd.getCounterSelector());
								}

							} else {
								message.getMessageLocker().unlock();
							}
						}
					} finally {
						sendReplyEmpty(_message);
					}
				} else if (_message.getClass() == AnomalyDetectedMessage.class) {
					AnomalyDetectedMessage m = (AnomalyDetectedMessage) _message;
					/*
					 * if (logger != null) { if (m.isCandidateToBan())
					 * logger.severe("Anomaly detected (distantInterfacedKernelAddress="
					 * +distant_kernel_address+") : "+_message); else
					 * logger.warning("Anomaly detected (distantInterfacedKernelAddress="
					 * +distant_kernel_address+") : "+_message); }
					 */

					if (m.getKernelAddress() != null && m.getKernelAddress().equals(distant_kernel_address)) {
						processInvalidProcess(null, m.getMessage(), m.isCandidateToBan());
					}
				} else if (_message.getClass() == CGRSynchro.class) {
					if (this.kernelAddressActivated) {
						CGRSynchro m = (CGRSynchro) _message;
						AgentSocketData asd = getBestAgentSocket(distant_kernel_address, m.getContent().getGroup(),
								false);
						if (asd != null) {
							if (logger != null && logger.isLoggable(Level.FINER))
								logger.finer("CGRSynchro (distantInterfacedKernelAddress=" + distant_kernel_address
										+ ") : " + _message);
							newCGRSynchroDetected(m);
							potentialChangementsInGroups();
							CGRSynchroSystemMessage message = new CGRSynchroSystemMessage(m);
							sendData(asd.getAgentAddress(), message, true, null, false, asd.getCounterSelector());
						}
					}
				} else if (_message.getClass() == SendDataFromAgentSocket.class) {
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Rooting message to send from agent socket (distantInterfacedKernelAddress="
								+ distant_kernel_address + ") : " + _message);
					SendDataFromAgentSocket m = (SendDataFromAgentSocket) _message;
					MessageLocker ml = (m.getContent() instanceof DataToBroadcast)
							? ((DataToBroadcast) m.getContent()).getMessageToBroadcast().getMessageLocker()
							: null;
					if (ml != null) {
						ml.lock();
					}
					
					
					sendData(m.getSender(), m.getContent(), m.prioritary, ml, m.last_message, getAgentSocketDataFromItsAgentAddress(m.getSender()).getCounterSelector());
				} else if (_message.getClass() == DistKernADataToUpgradeMessage.class) {
					DistKernADataToUpgradeMessage m = (DistKernADataToUpgradeMessage) _message;
					AgentAddress aa = m.dataToUpgrade.getAgentSocketSender();
					if ((aa == null || !sendMessage(aa, new DistKernADataToUpgradeMessage(m.dataToUpgrade))
							.equals(ReturnCode.SUCCESS)) && !m.dataToUpgrade.isUnlocked())
						m.dataToUpgrade.unlockMessage();
				} else if (_message.getClass() == NetworkGroupsAccessEvent.class) {
					if (distant_kernel_address == null)
						return;

					NetworkGroupsAccessEvent message = (NetworkGroupsAccessEvent) _message;

					if (message.getConcernedKernelAddress().equals(distant_kernel_address.getOriginalKernelAddress())
							&& message.concernsAuthorizedGroupsFromDistantPeer()) {
						if (logger != null && logger.isLoggable(Level.FINEST))
							logger.finest("Updating accepted groups from distant peer (distantInterfacedKernelAddress="
									+ distant_kernel_address + ") : " + message);
						AgentSocketData asd = getAgentSocketDataFromItsAgentAddress(message.getSender());
						if (asd != null) {
							asd.setDistantAccessibleGroups(message);
							updateDistantAcceptedGroups();
						} else if (logger != null)
							logger.severe("Impossible to found agent socket data from agent address "
									+ _message.getSender() + ". So impossible to set given NetworkGroupsAccessEvent.");

					}

				} else if (_message.getClass() == NetworkLoginAccessEvent.class) {
					if (distant_kernel_address == null)
						return;

					NetworkLoginAccessEvent message = (NetworkLoginAccessEvent) _message;

					if (message.getConcernedKernelAddress().equals(distant_kernel_address.getOriginalKernelAddress())) {
						if (logger != null && logger.isLoggable(Level.FINEST))
							logger.finest("Updating accepted identifiers (distantInterfacedKernelAddress="
									+ distant_kernel_address + ") : " + message);
						AgentSocketData asd = getAgentSocketDataFromItsAgentAddress(message.getSender());
						if (asd != null) {
							asd.setAcceptedIdentifiers(message);
							updateLoginData();
						} else if (logger != null)
							logger.severe("Impossible to found agent socket data from agent address "
									+ _message.getSender() + ". So impossible to set given NetworkLoginAccessEvent.");
					}

				} else if (_message.getClass() == KernelAddressValidation.class) {
					activateDistantKernelAgent(_message.getSender(),
							((KernelAddressValidation) _message).isKernelAddressInterfaceEnabled());
				} else if (_message instanceof ReceivedBlockData) {
					receiveData(_message.getSender(), ((ReceivedBlockData) _message).getContent());
				} else if (_message.getClass() == KillYou.class) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("DistantKernelAgent disabled (distantInterfacedKernelAddress="
								+ distant_kernel_address + ")");

					this.killAgent(this, KillingType.WAIT_AGENT_PURGE_ITS_MESSAGES_BOX_BEFORE_KILLING_IT);
				} else if (_message.getClass() == AbstractAgentSocket.AgentSocketKilled.class) {
					AbstractAgentSocket.AgentSocketKilled m = (AbstractAgentSocket.AgentSocketKilled) _message;

					boolean removed = removeAgentSocketData(agents_socket, m);
					if (!removed)
						removed = removeAgentSocketData(indirect_agents_socket, m);
					if (removed) {
						if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer(
									"Agent socket killed and removed from distant kernel agent list (distantInterfacedKernelAddress="
											+ distant_kernel_address + ")");

						if (agents_socket.size() == 0 && indirect_agents_socket.size() == 0) {
							for (AbstractData ad : m.shortDataNotSent) {
								if (ad instanceof AbstractPacketData) {
									BigPacketData bpd = cancelBigPacketDataInQueue(
											((AbstractPacketData) ad).getIDPacket());
									if (bpd != null) {
										MadkitKernelAccess.connectionLostForBigDataTransfer(this,
												bpd.getConversationID(), bpd.getIDPacket(), bpd.getAsker(),
												bpd.getReceiver(), bpd.getReadDataLength(), bpd.getDuration());
									}
								}
							}
							for (AbstractData ad : m.bigDataNotSent) {
								if (ad instanceof BigPacketData) {
									BigPacketData bpd = (BigPacketData) ad;
									MadkitKernelAccess.connectionLostForBigDataTransfer(this, bpd.getConversationID(),
											bpd.getIDPacket(), bpd.getAsker(), bpd.getReceiver(),
											bpd.getReadDataLength(), bpd.getDuration());
								}
							}
							/*
							 * if (this.kernelAddressActivated) MadkitKernelAccess.informHooks(this, new
							 * DistantKernelAgentEventMessage(AgentActionEvent.DISTANT_KERNEL_DISCONNECTED,
							 * this.distant_kernel_address));
							 */
							this.killAgent(this);
						} else {
							for (AbstractData ad : m.shortDataNotSent) {
								ad.reset();
							}
							for (AbstractData ad : m.bigDataNotSent) {
								ad.reset();
							}

							// TODO manage transfer connections and associate them with the new agent
							// socket, instead of actually closing them.

						}
					} else if (logger != null)
						logger.warning(
								"Agent socket killed and but not found on distant kernel agent list (distantInterfacedKernelAddress="
										+ distant_kernel_address + ")");

				} else if (_message.getClass() == ExceededDataQueueSize.class) {
					final ExceededDataQueueSize exeededDataSize = (ExceededDataQueueSize) _message;
					if (logger != null && logger.isLoggable(Level.FINEST))
						logger.finest("Exceeded data queue size (distantInterfacedKernelAddress="
								+ distant_kernel_address + ") : " + exeededDataSize);
					synchronized (networkBlacboard) {
						if (exeededDataSize.isPaused()
								&& networkBlacboard.transfertPausedForAllDistantKernelAgent.get()) {
							if (exeededDataSize.mustPurge()) {
								exeededDataSize.setReadingsToPurge(this.current_short_readings);
								globalExeceededDataQueueSize.set(exeededDataSize);
								if (this.totalDataInQueue.get() > 0) {
									if (this.current_short_readings.size() > 0) {
										if (exeededDataSize.tryPurgeNow(this)) {
											Set<AgentAddress> agents = new HashSet<>();
											for (SerializedReading sr : current_short_readings.values()) {
												agents.add(sr.getInitialAgentAddress());
											}
											if (agents.size() > 0) {
												setTansfertPaused(false, agents, true);
												final long durationBeforeTestingDDOS = 30000;
												if (taskForPurgeCheck != null)
													cancelTask(taskForPurgeCheck, true);
												taskForPurgeCheck = scheduleTask(new Task<>(new Callable<Void>() {

													@Override
													public Void call() throws Exception {
														ExceededDataQueueSize e = globalExeceededDataQueueSize.get();
														if (isAlive() && e != null && e.isPaused() && e.mustPurge()) {
															if (getMadkitConfig().networkProperties
																	.getStatsBandwith(getKernelAddress())
																	.getBytesDownloadedInRealTime(
																			NetworkProperties.DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS)
																	.getNumberOfIndentifiedBytes() == 0) {
																exeededDataSize.purgeCanceled(DistantKernelAgent.this);
																processPotentialDDOS();
															} else {
																scheduleTask(new Task<>(this, System.currentTimeMillis()
																		+ durationBeforeTestingDDOS));
															}
														}
														return null;
													}
												}, durationBeforeTestingDDOS + System.currentTimeMillis()));
											} else {
												// globalExeceededDataQueueSize.set(new
												// ExceededDataQueueSize(networkBlacboard, true));
												setTansfertPaused(true, null, true);
												exeededDataSize.purgeFinished(this);
											}
										}
									} else {
										globalExeceededDataQueueSize
												.set(new ExceededDataQueueSize(networkBlacboard, false, true));
										setTansfertPaused(true, null, true);
										exeededDataSize.purgeFinished(this);
									}
								} else {
									setTansfertPaused(true, null, true);
									exeededDataSize.purgeFinished(this);
								}
							} else {
								setTansfertPaused(true, null, true);
								globalExeceededDataQueueSize.set(exeededDataSize);
								if (exeededDataSize.hasOtherCandidates)
									exeededDataSize.candidateForPurge(this);

							}
						} else {
							if (!networkBlacboard.transfertPausedForAllDistantKernelAgent.get()) {
								if (taskForPurgeCheck != null) {
									cancelTask(taskForPurgeCheck, true);
									taskForPurgeCheck = null;
								}
								globalExeceededDataQueueSize.set(null);
								setTansfertPaused(false, null, true);
								/*
								 * if (!globalExeceededDataQueueSize.compareAndSet(null, null))
								 * setTansfertPaused(false, null, true);
								 */
							}
						}
					}
				} else if (_message.getClass() == AskForTransferMessage.class) {
					AskForTransferMessage m = (AskForTransferMessage) _message;
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("Ask for transfer message (distantInterfacedKernelAddress="
								+ distant_kernel_address + ") : " + m);

					if (m.needsInetSocketAddress()) {
						if (m.getKernelAddress1().equals(this.distant_kernel_address)
								|| m.getKernelAddress2().equals(this.distant_kernel_address)) {
							AgentSocketData asd = getBestAgentSocketCompatibleForTransfer();
							if (asd != null)
								sendReplyWithRole(m,
										new ObjectMessage<AskForTransferMessage.CandidateForTransfer>(
												m.getCandidate(asd.getGlobalAgentAddress(), this.distant_kernel_address,
														asd.distant_inet_socket_address, asd.numberOfIntermediatePeers,
														asd.getConnectionInfo())),
										LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
							else
								sendReplyEmpty(m);
						} else
							sendReplyEmpty(m);
					} else if (m.needsKernelAddress()) {
						updateBestAgent();
						AgentSocketData candidate = getAgentSocketDataForTransfer(this.agents_socket, m);
						if (candidate == null)
							candidate = getAgentSocketDataForTransfer(this.indirect_agents_socket, m);
						if (candidate != null) {
							sendReplyWithRole(m,
									new ObjectMessage<AskForTransferMessage.CandidateForTransfer>(m.getCandidate(
											candidate.getGlobalAgentAddress(), this.distant_kernel_address,
											candidate.distant_inet_socket_address, candidate.numberOfIntermediatePeers,
											candidate.getConnectionInfo())),
									LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
						} else
							sendReplyEmpty(m);
					}
				} else if (_message.getClass() == ObjectMessage.class) {
					Object o = ((ObjectMessage<?>) _message).getContent();
					if (o.getClass() == AgentSocketData.class) {
						AgentSocketData asd = (AgentSocketData) o;
						if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer("Receiving agent socket data (distantInterfacedKernelAddress="
									+ distant_kernel_address + ") : " + asd);

						if (asd.direct)
							this.agents_socket.add(asd);
						else
							this.indirect_agents_socket.add(asd);
						if (this.kernelAddressActivated)
							sendReplyWithRole(_message, new ObjectMessage<StatsBandwidth>(stats),
									LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					} else if (o instanceof KernelAddress) {
						// distant kernel address received
						setDistantKernelAddress(_message.getSender(), (KernelAddress) o);
					} else if (o.getClass() == ReceivedSerializableObject.class
							&& (((ReceivedSerializableObject) o).getContent() instanceof LanMessage)) {
						ReceivedSerializableObject originalMessage = ((ReceivedSerializableObject) o);
						LanMessage lm = (LanMessage) originalMessage.getContent();
						if (this.distant_kernel_address == null) {
							originalMessage.markDataAsRead();
							return;
						}
						try {
							MadkitKernelAccess.setSender(lm.message, lm.message.getSender());
						} catch (IllegalArgumentException e) {
							originalMessage.markDataAsRead();
							if (logger != null)
								logger.severeLog("Unexpected exception", e);
							// processInvalidSerializedObject(new AccessException("Invalid kernel address
							// "+lm.message.getSender().getKernelAddress()), lm, true);
							return;
						}

						if (lm instanceof DirectLanMessage) {
							DirectLanMessage dlm = (DirectLanMessage) lm;
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("Receiving direct lan message (distantInterfacedKernelAddress="
										+ distant_kernel_address + ") : " + dlm);

							if (dlm.message instanceof BigDataResultMessage) {
								cancelBigPacketDataInQueue(
										MadkitKernelAccess.getIDPacket((BigDataResultMessage) dlm.message));

							}
							this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT,
									new DirectLocalLanMessage(dlm.message, originalMessage),
									LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
						} else if (lm instanceof BroadcastLanMessage) {
							BroadcastLanMessage blm = (BroadcastLanMessage) lm;
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("Receiving lan message to broadcast (distantInterfacedKernelAddress="
										+ distant_kernel_address + ") : " + blm);
							this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NET_AGENT,
									new BroadcastLocalLanMessage(blm.message, originalMessage, blm.abstract_group,
											blm.role, blm.agentAddressesSender),
									LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
						}
					} else if (o.getClass() == SecretMessage.class) {
						SecretMessage sm = (SecretMessage) o;
						if (!isConcernedBy(sm.getOriginalDistantKernelAgent())) {
							if (logger != null && logger.isLoggable(Level.FINER))
								logger.finer(
										"Receiving secret message and transmitting to best agent socket (distantInterfacedKernelAddress="
												+ distant_kernel_address + ")");
							// send a secret message in order to try to consider two connections as being
							// part of the same pair of peers.

							AgentSocketData asd = getBestAgentSocket(true);
							if (asd != null && kernelAddressActivated)
								sendData(asd.getAgentAddress(), sm, true, null, false, asd.getCounterSelector());
							else {
								@SuppressWarnings("unchecked")
								ObjectMessage<SecretMessage> message = (ObjectMessage<SecretMessage>) _message;
								differSecretMessage(message);
							}
						}
					} else if (o.getClass() == AbstractAgentSocket.Groups.class) {
						AgentSocketData asd = getAgentSocketDataFromItsAgentAddress(_message.getSender());
						if (asd == null) {
							if (logger != null)
								logger.warning(
										"Receiving message (AbstractAgentSocket.Groups) from unexpected agent address : "
												+ _message.getSender());
						} else {
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("Groups update (distantInterfacedKernelAddress=" + distant_kernel_address
										+ ") from " + _message.getSender());

							asd.setAcceptedLocalGroups((Groups) o);
							updateLocalAcceptedGroups();
						}

					} else if (o.getClass() == ConnectionInfoSystemMessage.class) {
						AgentSocketData asd = getAgentSocketDataFromItsAgentAddress(_message.getSender());
						if (asd != null) {
							if (logger != null && logger.isLoggable(Level.FINEST))
								logger.finest("Receiving connection information from " + _message.getSender()
										+ " (distantInterfacedKernelAddress=" + distant_kernel_address + ") : " + o);

							asd.setConnectionInfo((ConnectionInfoSystemMessage) o);
						} else if (logger != null)
							logger.severe(
									"Impossible to found agent socket data from agent address " + _message.getSender()
											+ ". So impossible to set given ConnectionInfoSystemMessage.");
					} else if (o.getClass() == BigDataPropositionMessage.class) {
						BigDataPropositionMessage bdpm = (BigDataPropositionMessage) o;
						if (bdpm.getSender().getKernelAddress().equals(distant_kernel_address)) {
							AgentSocketData asd = getBestAgentSocket(false);
							if (asd != null) {
								if (logger != null && logger.isLoggable(Level.FINEST))
									logger.finest("Receiving big data proposition message " + _message.getSender()
											+ " (distantInterfacedKernelAddress=" + distant_kernel_address + ") : "
											+ o);

								int id = MadkitKernelAccess.getIDPacket(bdpm);
								current_big_data_readings.put(new Integer(id), new BigDataReading(bdpm));
								sendData(asd.getAgentAddress(), new ValidateBigDataProposition(id), true, null, false, asd.getCounterSelector());
							}
						}
					} else if (o.getClass() == ValidateBigDataProposition.class) {
						validatePacketDataInQueue(((ValidateBigDataProposition) o).getIDPacket());
					} else if (o.getClass() == DistantKernelAddressValidated.class) {
						activateDistantKernelAgentWithDistantConfirmation(_message.getSender());
					}

				}
			} catch (SelfKillException e) {
				throw e;
			} catch (Exception e) {
				if (logger != null)
					logger.severeLog("Unexpected exception", e);
				this.killAgent(this);
			}
		} finally {
			concurrent = false;
		}
	}

	private void differSecretMessage(ObjectMessage<SecretMessage> _message) {
		if (differedSecretMessages == null)
			differedSecretMessages = new ArrayList<>();
		differedSecretMessages.add(_message);
	}

	private void purgeDifferedSecretMessages() {
		if (differedSecretMessages != null) {
			for (Message m : differedSecretMessages)
				receiveMessage(m);
			differedSecretMessages = null;
		}
	}

	private AgentSocketData getAgentSocketDataForTransfer(ArrayList<AgentSocketData> agents_socket,
			AskForTransferMessage m) {
		for (AgentSocketData asd : agents_socket) {
			if (asd.isUsable() && (asd.distant_inet_socket_address.equals(m.getInetSocketAddress1())
					|| asd.distant_inet_socket_address.equals(m.getInetSocketAddress2()))) {
				if (asd.numberOfIntermediatePeers + 1 <= getMadkitConfig().networkProperties.gatewayDepth) {
					return asd;
				}
			}
		}
		return null;

	}

	private void addBigDataInQueue(AgentSocketData choosenSocket, BigDataPropositionMessage bgpm)
			throws PacketException {
		RandomInputStream inputStream = MadkitKernelAccess.getInputStream(bgpm);
		if (inputStream != null) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Adding big data in queue (distantInterfacedKernelAddress=" + distant_kernel_address
						+ ") : " + bgpm);

			int id = getNewPacketID();
			MadkitKernelAccess.setIDPacket(bgpm, id);
			WritePacket packet = new WritePacket(PacketPartHead.TYPE_PACKET, id,
					getMadkitConfig().networkProperties.maxBufferSize,
					getMadkitConfig().networkProperties.maxRandomPacketValues, random, inputStream,
					bgpm.getStartStreamPosition(), bgpm.getTransferLength(), true, bgpm.getMessageDigestType());
			BigPacketData packetData = new BigPacketData(choosenSocket.getAgentAddress(), packet, bgpm.getReceiver(),
					bgpm.getSender(), bgpm.getConversationID(), bgpm.getStatistics(), bgpm.bigDataExcludedFromEncryption(), choosenSocket.getCounterSelector());
			packetsDataInQueue.put(new Integer(id), packetData);
		}
	}

	private BigPacketData cancelBigPacketDataInQueue(int idTransfer) {
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Cancel big data in queue (distantInterfacedKernelAddress=" + distant_kernel_address
					+ ", idTransfer=" + idTransfer + ")");
		return packetsDataInQueue.remove(new Integer(idTransfer));
	}

	private boolean validatePacketDataInQueue(int idTransfer) {
		BigPacketData bpd = packetsDataInQueue.remove(new Integer(idTransfer));
		if (bpd == null) {
			if (logger != null)
				logger.warning("Big data in queue not valid (distantInterfacedKernelAddress=" + distant_kernel_address
						+ ", idTransfer=" + idTransfer + ")");

			return false;
		} else {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Big data in queue valid (distantInterfacedKernelAddress=" + distant_kernel_address
						+ ", idTransfer=" + idTransfer + ")");

			sendMessage(bpd.getAgentSocketSender(), new DistKernADataToUpgradeMessage(bpd));
			return true;
		}
	}

	private AgentSocketData getAgentSocketDataFromItsAgentAddress(AgentAddress aa) {
		for (AgentSocketData asd : agents_socket) {
			if (asd.isConcernedBy(aa))
				return asd;
		}
		for (AgentSocketData asd : indirect_agents_socket) {
			if (asd.isConcernedBy(aa))
				return asd;
		}
		return null;

	}

	static class ExceededDataQueueSize extends Message {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7103634976795661665L;

		private final boolean paused;
		private final boolean purge;
		private HashMap<Integer, SerializedReading> readingToPurge = null;
		protected final NetworkBlackboard networkBlacboard;
		protected final boolean hasOtherCandidates;

		ExceededDataQueueSize(NetworkBlackboard networkBlacboard, boolean hasOtherCandidates, boolean paused) {
			this(networkBlacboard, hasOtherCandidates, paused, false);
		}

		ExceededDataQueueSize(NetworkBlackboard networkBlacboard, boolean hasOtherCandidates, boolean paused,
				boolean purge) {
			this.networkBlacboard = networkBlacboard;
			this.paused = paused;
			this.purge = purge;
			this.hasOtherCandidates = hasOtherCandidates;
		}

		boolean isPaused() {
			return paused;
		}

		boolean mustPurge() {
			return purge;
		}

		@Override
		public String toString() {
			return "ExceededDataQueueSize[paused=" + paused + ", purge=" + purge + "]";
		}

		void candidateForPurge(DistantKernelAgent asker) {
			synchronized (networkBlacboard.candidatesForPurge) {
				/*
				 * if (networkBlacboard.currentCandidateForPurge==null) {
				 * networkBlacboard.currentCandidateForPurge=asker; asker.receiveMessage(new
				 * ExceededDataQueueSize(networkBlacboard, true, true)); } else { if
				 * (!networkBlacboard.candidatesForPurge.contains(asker))
				 * networkBlacboard.candidatesForPurge.add(asker); }
				 */
				if (!networkBlacboard.candidatesForPurge.contains(asker)) {
					networkBlacboard.candidatesForPurge.add(asker);
					networkBlacboard.candidatesForPurge.notifyAll();
				}

			}
		}

		boolean tryPurgeNow(DistantKernelAgent asker) throws InterruptedException {
			synchronized (networkBlacboard.candidatesForPurge) {
				if (networkBlacboard.currentCandidateForPurge == null
						|| networkBlacboard.currentCandidateForPurge == asker) {
					if (hasOtherCandidates && networkBlacboard.candidatesForPurge.size() == 0) {
						LockerCondition lc = new LockerCondition() {

							@Override
							public boolean isLocked() {
								return networkBlacboard.candidatesForPurge.size() > 0;
							}
						};
						lc.setLocker(networkBlacboard.candidatesForPurge);
						asker.wait(lc);
					}
					networkBlacboard.currentCandidateForPurge = asker;
					return true;
				} else {
					if (!networkBlacboard.candidatesForPurge.contains(asker))
						networkBlacboard.candidatesForPurge.add(asker);
					return false;
				}
			}
		}

		boolean isPurging(DistantKernelAgent asker) {
			if (isPaused() && mustPurge()) {
				synchronized (networkBlacboard.candidatesForPurge) {
					if (networkBlacboard.currentCandidateForPurge == asker) {
						return networkBlacboard.transfertPausedForAllDistantKernelAgent.get();
					}
				}
			}
			return false;
		}

		// @SuppressWarnings("synthetic-access")
		void purgeFinished(DistantKernelAgent asker) {
			synchronized (networkBlacboard.candidatesForPurge) {
				if (asker.taskForPurgeCheck != null) {
					asker.cancelTask(asker.taskForPurgeCheck, true);
					asker.taskForPurgeCheck = null;
				}
				if (networkBlacboard.transfertPausedForAllDistantKernelAgent.get())
					asker.globalExeceededDataQueueSize.set(new ExceededDataQueueSize(networkBlacboard, false, true));
				else
					asker.globalExeceededDataQueueSize.set(null);
				if (networkBlacboard.currentCandidateForPurge == asker) {
					networkBlacboard.currentCandidateForPurge = null;
					if (!networkBlacboard.candidatesForPurge.isEmpty()) {
						if (networkBlacboard.transfertPausedForAllDistantKernelAgent.get())
							networkBlacboard.currentCandidateForPurge = networkBlacboard.candidatesForPurge
									.remove(networkBlacboard.candidatesForPurge.size() - 1);
						else {
							networkBlacboard.candidatesForPurge.clear();
						}
						if (networkBlacboard.currentCandidateForPurge != null)
							networkBlacboard.currentCandidateForPurge
									.receiveMessage(new ExceededDataQueueSize(networkBlacboard, false, true, true));
					} else
						asker.setGlobalTransfersPaused(false);
				} else {
					/*
					 * if (asker.logger!=null) asker.logger.severe("Illegal access");
					 */
					networkBlacboard.candidatesForPurge.remove(asker);
					if (networkBlacboard.currentCandidateForPurge == null
							|| networkBlacboard.candidatesForPurge.isEmpty()) {
						asker.setGlobalTransfersPaused(false);
					}
				}
			}
		}

		void setOneSocketPurged(DistantKernelAgent asker, Integer id, SerializedReading sr) {
			if (isPaused() && mustPurge()) {
				if (asker.logger != null && asker.logger.isLoggable(Level.FINEST))
					asker.logger.finest("Set one socket purged (distantInterfacedKernelAddress="
							+ asker.distant_kernel_address + ", agentAddress=" + sr.getInitialAgentAddress() + ")");
				/*
				 * synchronized(networkBlacboard.candidatesForPurge) { if
				 * (networkBlacboard.transfertPausedForAllDistantKernelAgent.get())
				 * asker.sendMessageWithRole(sr.getInitialAgentAddress(), new
				 * ExceededDataQueueSize(networkBlacboard, true),
				 * LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE); }
				 */

				removeReading(id);
				if (!hasReadingsToPurge()) {
					purgeFinished(asker);
				}

			}

		}

		void purgeCanceled(DistantKernelAgent asker) {
			purgeFinished(asker);
		}

		@SuppressWarnings("unchecked")
		void setReadingsToPurge(HashMap<Integer, SerializedReading> readings) {
			readingToPurge = (HashMap<Integer, SerializedReading>) readings.clone();
		}

		void removeReading(Integer id) {
			readingToPurge.remove(id);
		}

		boolean hasReadingsToPurge() {
			return !readingToPurge.isEmpty();
		}

		boolean hasMoreThanOneReadingsToPurge() {
			return readingToPurge.size() > 1;
		}

	}

	private boolean hasUsableDistantSocketAgent(ArrayList<AgentSocketData> agents_socket) {
		for (AgentSocketData asd : agents_socket) {
			if (asd.isUsable())
				return true;
		}
		return false;
	}

	private boolean hasUsableDistantSocketAgent() {
		return hasUsableDistantSocketAgent(agents_socket);
	}

	private int getUsableDistantSocketAgentNumber(ArrayList<AgentSocketData> agents_socket) {
		int nb = 0;
		for (AgentSocketData asd : agents_socket) {
			if (asd.isUsable())
				++nb;
		}
		return nb;
	}

	private int getUsableDistantSocketAgentNumber() {
		return getUsableDistantSocketAgentNumber(agents_socket)
				+ getUsableDistantSocketAgentNumber(indirect_agents_socket);
	}

	private void activateDistantKernelAgentWithDistantConfirmation(AgentAddress sender) throws MadkitException {
		AgentSocketData asd = getAgentSocketDataFromItsAgentAddress(sender);
		if (asd != null) {
			boolean hasPreviousUsableSocketsAgents = hasUsableDistantSocketAgent();
			asd.setUsable(true);
			if (!hasPreviousUsableSocketsAgents && kernelAddressActivated) {
				activateDistantKernelAgent();
				informHooksDistantKernelAgentActivated();
			}
			else
				networkBlacboard.unlockSimultaneousConnections(distant_kernel_address);
			int nb = getUsableDistantSocketAgentNumber();
			if (nb > 1
					&& nb > getMadkitConfig().networkProperties.numberOfMaximumConnectionsBetweenTwoSameKernelsAndMachines)
				sendMessageWithRole(sender,
						new ObjectMessage<>(new TooMuchConnectionWithTheSamePeers(asd.distant_inet_socket_address)),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

		} else if (logger != null) {
			logger.severe("Unknow agent socket " + sender);
		}
	}

	private void activateDistantKernelAgent() throws MadkitException {
		ReturnCode rc = this.requestRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
				LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		if (rc.equals(ReturnCode.SUCCESS)) {
			updateDistantAcceptedGroups();
			informHooksForLoginData();
			updateLocalAcceptedGroups();
			networkBlacboard.addDistantKernelAddressInterfaced(distant_kernel_address);
			potentialChangementsInGroups();
			networkBlacboard.unlockSimultaneousConnections(distant_kernel_address);
		} else {
			if (logger != null)
				logger.severe("Unexpected return code during distant kernel agent activation : " + rc);
		}
	}

	private void informHooksDistantKernelAgentActivated() {
		MadkitKernelAccess.informHooks(this,
				new DistantKernelAgentEventMessage(AgentActionEvent.DISTANT_KERNEL_CONNECTED, distant_kernel_address));
		if (logger != null)
			logger.info("New distant madkit kernel connected (distantInterfacedKernelAddress=" + distant_kernel_address
					+ ", localKernelAddress=" + getKernelAddress() + ")");
	}

	private void activateDistantKernelAgent(AgentAddress sender, boolean currentDistantKernelAgentEnabled)
			throws MadkitException {
		if (currentDistantKernelAgentEnabled) {
			boolean previous_activated = kernelAddressActivated;
			boolean inform_hooks = false;
			if (!previous_activated) {
				// activate the current distant kernel agent
				kernelAddressActivated = true;
				stats = getMadkitConfig().networkProperties.addIfNecessaryAndGetStatsBandwitdh(distant_kernel_address);

				if (hasUsableDistantSocketAgent()) {
					activateDistantKernelAgent();
					inform_hooks = true;
				}
				purgeDifferedSecretMessages();

			}

			sendMessageWithRole(sender, new ObjectMessage<KernelAddressInterfaced>(this.distant_kernel_address),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

			sendMessageWithRole(sender, new ObjectMessage<StatsBandwidth>(stats),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

			/*
			 * synchronized(networkBlacboard.candidatesForPurge) { ExceededDataQueueSize
			 * e=this.globalExeceededDataQueueSize.get(); if (e!=null && e.isPaused()) {
			 * boolean isPirgie.isPurging(this); receiveMessage(new ObjectMessage<>(new
			 * ExceededDataQueueSize(networkBlacboard, false))); } else { receiveMessage(new
			 * ObjectMessage<>(new ExceededDataQueueSize(networkBlacboard, false, false,
			 * false))); }
			 * 
			 * }
			 */

			if (inform_hooks) {
				informHooksDistantKernelAgentActivated();
			}

		} else {
			receiveMessage(new KillYou());
			this.leaveRole(LocalCommunity.Groups.getOriginalDistantKernelAgentGroup(this.distant_kernel_address),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		}
		

	}

	static class KillYou extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7848872227308730056L;

	}

	/*
	 * An agent socket sent its concerned distant kernel address.
	 */
	private void setDistantKernelAddress(AgentAddress sender, KernelAddress distant_ka) throws MadkitException {
		try {
			if (this.distant_kernel_address == null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Setting distant kernel address (distantKernelAddress=" + distant_ka + ")");
				
				try {

					networkBlacboard.lockForSimultaneousConnections(this, distant_ka);

				} catch (InterruptedException e) {
					this.killAgent(this);
				}

				// save the distant kernel address
				this.distant_kernel_address = new KernelAddressInterfaced(distant_ka, true);

				// test if the distant kernel address has already been binded
				boolean duplicate_group = this.getAgentsWithRole(
						LocalCommunity.Groups.getOriginalDistantKernelAgentGroup(this.distant_kernel_address),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, false).size() > 0;
				// the current agent join the original distant kernel address
				this.requestRole(LocalCommunity.Groups.getOriginalDistantKernelAgentGroup(this.distant_kernel_address),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				if (duplicate_group) {
					if (logger != null && logger.isLoggable(Level.FINER))
						logger.finer("duplicate kernel address (distantKernelAddress=" + distant_ka + ")");
					// interface the received distant kernel address
					this.distant_kernel_address = new KernelAddressInterfaced(distant_ka, false);
					// the current agent join the interfaced distant kernel address
					// this.requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(this.distant_kernel_address),
					// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					this.requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(getNetworkID()),
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				} else {
					// the current agent join the interfaced distant kernel address
					// this.requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(this.distant_kernel_address),
					// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					this.requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(getNetworkID()),
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					// activate the current distant kernel agent
					activateDistantKernelAgent(sender, true);
					//networkBlacboard.unlockSimultaneousConnections(distant_ka);
				}
				// send to the concerned agent socket the result of the current agent activation
				sendMessageWithRole(sender, new KernelAddressValidation(duplicate_group),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

			} else if (logger != null)
				logger.warning("Setting distant kernel address already done (currentDistantKernelAddress="
						+ distant_kernel_address + ", receivedKernelAddress=" + distant_ka + ")");

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new MadkitException(e);
		}
	}

	private void updateDistantAcceptedGroups(ArrayList<AgentSocketData> agents_socket, MultiGroup general,
			Set<Group> groups) {
		for (AgentSocketData a : agents_socket) {
			if (a.distant_accessible_and_requested_groups2 != null) {
				for (Group g : a.distant_accessible_and_requested_groups2)
					groups.add(g);
				general.addGroup(a.distant_general_accessible_groups);
			}
		}
	}

	private void updateDistantAcceptedGroups() {
		MultiGroup general = new MultiGroup();
		Set<Group> groups = new HashSet<>();

		updateDistantAcceptedGroups(agents_socket, general, groups);
		updateDistantAcceptedGroups(indirect_agents_socket, general, groups);

		Group old[] = distant_accepted_groups;
		distant_accepted_groups = new Group[groups.size()];

		groups.toArray(distant_accepted_groups);

		if (kernelAddressActivated && hasUsableDistantSocketAgent()
				&& !((old == null || old.length == 0) && distant_accepted_groups.length == 0))
			MadkitKernelAccess.informHooks(this,
					new NetworkGroupsAccessEvent(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER, general,
							distant_accepted_groups, distant_kernel_address));
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest(
					"Distant accepted groups updated (distantInterfacedKernelAddress=" + distant_kernel_address + ")");
	}

	private void updateLoginData(ArrayList<AgentSocketData> agents_socket, Set<PairOfIdentifiers> ids,
			Set<PairOfIdentifiers> idsnewa, Set<PairOfIdentifiers> idsnewd, Set<PairOfIdentifiers> idsnewu) {
		for (AgentSocketData a : agents_socket) {
			for (PairOfIdentifiers poi : a.getAcceptedPairOfIdentifiers())
				ids.add(poi);
			for (PairOfIdentifiers poi : a.getLastAcceptedPairOfIdentifiers())
				idsnewa.add(poi);
			for (PairOfIdentifiers poi : a.getLastDeniedPairOfIdentifiers())
				idsnewd.add(poi);
			for (PairOfIdentifiers poi : a.getLastUnloggedPairOfIdentifiers())
				idsnewu.add(poi);
		}
	}

	private void updateLoginData() {
		Set<PairOfIdentifiers> ids = new HashSet<>();
		Set<PairOfIdentifiers> idsnewa = new HashSet<>();
		Set<PairOfIdentifiers> idsnewd = new HashSet<>();
		Set<PairOfIdentifiers> idsnewu = new HashSet<>();
		updateLoginData(agents_socket, ids, idsnewa, idsnewd, idsnewu);
		updateLoginData(indirect_agents_socket, ids, idsnewa, idsnewd, idsnewu);

		for (Iterator<PairOfIdentifiers> it = idsnewa.iterator(); it.hasNext();) {
			if (ids.contains(it.next()))
				it.remove();
		}
		for (Iterator<PairOfIdentifiers> it = idsnewu.iterator(); it.hasNext();) {
			if (ids.contains(it.next()))
				it.remove();
		}

		accepted_identifiers = new ArrayList<>();
		accepted_identifiers.addAll(ids);
		last_accepted_identifiers.removeAll(idsnewu);
		last_accepted_identifiers.removeAll(idsnewd);
		last_accepted_identifiers.addAll(idsnewa);
		last_denied_identifiers.addAll(idsnewd);
		last_unlogged_identifiers.removeAll(idsnewa);
		last_unlogged_identifiers.addAll(idsnewu);

		informHooksForLoginData();
		if (logger != null && logger.isLoggable(Level.FINEST))
			logger.finest("Login data updated (distantInterfacedKernelAddress=" + distant_kernel_address + ")");

	}

	private void informHooksForLoginData() {
		if (this.kernelAddressActivated && (accepted_identifiers.size() > 0 || last_accepted_identifiers.size() > 0
				|| last_denied_identifiers.size() > 0 || last_unlogged_identifiers.size() > 0)) {
			MadkitKernelAccess.informHooks(this,
					new NetworkLoginAccessEvent(this.distant_kernel_address, accepted_identifiers,
							last_accepted_identifiers, last_denied_identifiers, last_unlogged_identifiers));
			last_accepted_identifiers = new ArrayList<>();
			last_denied_identifiers = new ArrayList<>();
			last_unlogged_identifiers = new ArrayList<>();
		}
	}

	@Override
	public void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching DistantKernelAgent  (" + this.distant_kernel_address + ") ... !");

		this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);

		networkBlacboard = (NetworkBlackboard) getBlackboard(LocalCommunity.Groups.NETWORK,
				LocalCommunity.BlackBoards.NETWORK_BLACKBOARD);
		// this.requestRole(LocalCommunity.Groups.getDistantKernelAgentGroup(hashCode()),
		// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		try {
			random = getMadkitConfig().getApprovedSecureRandom();
		} catch (Exception e) {
			if (logger != null)
				logger.severeLog("Unexpected exception", e);
		}
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("DistantKernelAgent  (" + this.distant_kernel_address + ") LAUNCHED !");
	}

	private void updateBestAgent() {
		if (lastAgentsUpdate == -1 || lastAgentsUpdate
				+ getMadkitConfig().networkProperties.delayBetweenEachAgentSocketOptimization < System
						.currentTimeMillis()) {
			Collections.sort(agents_socket);
			Collections.sort(indirect_agents_socket);
			lastAgentsUpdate = System.currentTimeMillis();
		}
	}

	protected AgentSocketData getBestAgentSocket(AgentAddress _receiver, boolean testOnlyRequestedGroups) {
		return getBestAgentSocket(_receiver.getKernelAddress(), _receiver.getGroup(), testOnlyRequestedGroups);
	}

	private void potentialChangementsInGroups(ArrayList<AgentSocketData> agents_socket) throws MadkitException {
		for (int i = 0; i < agents_socket.size(); i++) {
			AgentSocketData asd = agents_socket.get(i);
			if (asd.isUsable())
				asd.potentialChangementInGroups(this);
		}
	}

	protected void potentialChangementsInGroups() throws MadkitException {
		potentialChangementsInGroups(agents_socket);
		potentialChangementsInGroups(indirect_agents_socket);
	}

	protected AgentSocketData getBestAgentSocket(KernelAddress distantKernelAddress, Group group,
			boolean testOnlyRequestedGroups) {
		updateBestAgent();

		if (!distantKernelAddress.equals(distant_kernel_address)) {
			return null;
		}

		AgentSocketData asd = getFirstValidAgentSocketData(agents_socket, distantKernelAddress, group,
				testOnlyRequestedGroups);
		if (asd == null)
			asd = getFirstValidAgentSocketData(indirect_agents_socket, distantKernelAddress, group,
					testOnlyRequestedGroups);
		return asd;
	}

	private AgentSocketData getFirstValidAgentSocketData(ArrayList<AgentSocketData> agents_socket,
			KernelAddress distantKernelAddress, Group group, boolean testOnlyRequestedGroups) {
		for (Iterator<AgentSocketData> it = agents_socket.iterator(); it.hasNext();) {
			AgentSocketData asd = it.next();
			if (asd.isUsable() && asd.contains(group, testOnlyRequestedGroups)
					&& asd.getAcceptedLocalGroups().getGroups().includes(getKernelAddress(), group))
				return asd;
		}
		return null;
	}

	private AgentSocketData getBestAgentSocket(boolean includeNotUsable) {
		updateBestAgent();

		AgentSocketData asd = getFirstValidAgentSocketData(includeNotUsable, agents_socket);
		if (asd == null)
			asd = getFirstValidAgentSocketData(includeNotUsable, indirect_agents_socket);
		return asd;
	}

	private AgentSocketData getFirstValidAgentSocketData(boolean includeNotUsable,
			ArrayList<AgentSocketData> agents_socket) {
		for (Iterator<AgentSocketData> it = agents_socket.iterator(); it.hasNext();) {
			AgentSocketData asd = it.next();
			if (includeNotUsable || asd.isUsable())
				return asd;
		}
		return null;
	}

	private AgentSocketData getFirstValidAgentSocketDataCompatibleForTransfer(
			ArrayList<AgentSocketData> agents_socket) {
		for (Iterator<AgentSocketData> it = agents_socket.iterator(); it.hasNext();) {
			AgentSocketData asd = it.next();
			if (asd.isUsable() && asd.numberOfIntermediatePeers + 1 <= getMadkitConfig().networkProperties.gatewayDepth)
				return asd;
		}
		return null;
	}

	private AgentSocketData getBestAgentSocketCompatibleForTransfer() {
		updateBestAgent();

		AgentSocketData asd = getFirstValidAgentSocketDataCompatibleForTransfer(agents_socket);
		if (asd == null)
			asd = getFirstValidAgentSocketDataCompatibleForTransfer(indirect_agents_socket);
		return asd;
	}

	static class AgentSocketData implements Comparable<AgentSocketData> {
		// final AbstractAgentSocket agent;
		private final AgentAddress global_address;
		private AgentAddress address;
		final StatsBandwidth stat;
		MultiGroup distant_accessible_and_requested_groups = new MultiGroup();
		AbstractGroup distant_general_accessible_groups = new MultiGroup();
		List<Group> distant_accessible_and_requested_groups2 = null;
		final boolean direct;
		private List<PairOfIdentifiers> accepted_identifiers = new ArrayList<>();
		private ArrayList<PairOfIdentifiers> last_accepted_identifiers = new ArrayList<>();
		private ArrayList<PairOfIdentifiers> last_denied_identifiers = new ArrayList<>();
		private ArrayList<PairOfIdentifiers> last_unlogged_identifiers = new ArrayList<>();
		private Groups myAcceptedGroups = null;
		final InetSocketAddress distant_inet_socket_address;
		final int numberOfIntermediatePeers;
		ConnectionInfoSystemMessage distantConnectionInfo = null;
		private boolean distantKernelAddressValidated;
		private final CounterSelector counterSelector;

		AgentSocketData(AbstractAgentSocket _agent_socket) {
			global_address = address = _agent_socket.getAgentAddressIn(LocalCommunity.Groups.NETWORK,
					LocalCommunity.Roles.SOCKET_AGENT_ROLE);
			stat = _agent_socket.getStatistics();
			// distant_accessible_groups=new
			// MultiGroup(_agent_socket.getDistantAcceptedGroups());
			direct = _agent_socket instanceof AgentSocket;
			distant_inet_socket_address = _agent_socket.getDistantInetSocketAddress();
			numberOfIntermediatePeers = _agent_socket.getNumberOfIntermediatePeers();
			distantKernelAddressValidated = false;
			this.counterSelector=_agent_socket.connection_protocol.getCounterSelector();
		}

		CounterSelector getCounterSelector()
		{
			return counterSelector;
		}
		
		void setUsable(boolean value) {
			distantKernelAddressValidated = value;
		}

		public boolean isUsable() {
			return distantKernelAddressValidated;
		}

		@Override
		public String toString() {
			return "AgentSocketData[global_address=" + global_address + ", direct=" + direct + "]";
		}

		@Override
		public int compareTo(AgentSocketData _o) {
			if (this.isUsable() && !_o.isUsable())
				return 1;
			else if (!this.isUsable() && _o.isUsable())
				return -1;

			TransferSpeedStat t1 = stat.getBytesUploadedInRealBytes(NetworkProperties.DEFAULT_STAT_PER_512KB_SEGMENTS);
			TransferSpeedStat t2 = _o.stat
					.getBytesUploadedInRealBytes(NetworkProperties.DEFAULT_STAT_PER_512KB_SEGMENTS);
			if (t1.isOneCycleDone()) {
				if (!t2.isOneCycleDone())
					return -1;
			} else {
				if (t2.isOneCycleDone())
					return 1;
				else
					return 0;
			}

			double v = t1.getBytesPerSecond() - t2.getBytesPerSecond();
			if (v < 0.0)
				return -1;
			if (v > 0.0)
				return 1;
			return 0;
		}

		void setConnectionInfo(ConnectionInfoSystemMessage distantConnectionInfo) {
			this.distantConnectionInfo = distantConnectionInfo;
		}

		ConnectionInfoSystemMessage getConnectionInfo() {
			return this.distantConnectionInfo;
		}

		boolean isConcernedBy(AgentAddress agentAddress) {
			if (address.representsSameAgentThan(agentAddress)) {
				address = agentAddress;
				return true;
			} else
				return false;
		}

		AgentAddress getAgentAddress() {
			return address;
		}

		AgentAddress getGlobalAgentAddress() {
			return global_address;
		}

		boolean contains(Group _group, boolean testOnlyRequestedGroups) {
			if (testOnlyRequestedGroups)
				return distant_accessible_and_requested_groups.includes(_group);
			else
				return distant_general_accessible_groups.includes(_group);
		}

		void setDistantAccessibleGroups(NetworkGroupsAccessEvent event) {
			distant_general_accessible_groups = event.getGeneralAcceptedGroups();
			distant_accessible_and_requested_groups = new MultiGroup(event.getRequestedAccessibleGroups());
			distant_accessible_and_requested_groups2 = event.getRequestedAccessibleGroups();
		}

		void setAcceptedIdentifiers(NetworkLoginAccessEvent event) {
			accepted_identifiers = event.getCurrentIdentifiers();
			last_accepted_identifiers.addAll(event.getNewAcceptedIdentifiers());
			last_denied_identifiers.addAll(event.getNewDeniedIdentifiers());
			last_unlogged_identifiers.addAll(event.getNewUnloggedIdentifiers());
		}

		List<PairOfIdentifiers> getAcceptedPairOfIdentifiers() {
			return accepted_identifiers;
		}

		ArrayList<PairOfIdentifiers> getLastDeniedPairOfIdentifiers() {
			ArrayList<PairOfIdentifiers> res = last_denied_identifiers;
			last_denied_identifiers = new ArrayList<>();
			return res;
		}

		ArrayList<PairOfIdentifiers> getLastAcceptedPairOfIdentifiers() {
			ArrayList<PairOfIdentifiers> res = last_accepted_identifiers;
			last_accepted_identifiers = new ArrayList<>();
			return res;
		}

		ArrayList<PairOfIdentifiers> getLastUnloggedPairOfIdentifiers() {
			ArrayList<PairOfIdentifiers> res = last_unlogged_identifiers;
			last_unlogged_identifiers = new ArrayList<>();
			return res;
		}

		void setAcceptedLocalGroups(Groups groups) {
			myAcceptedGroups = groups;
		}

		Groups getAcceptedLocalGroups() {
			return myAcceptedGroups;
		}

		void potentialChangementInGroups(DistantKernelAgent dka) throws MadkitException {
			AcceptedGroups ag = getAcceptedLocalGroups().potentialChangementInGroups();
			if (ag != null) {
				dka.sendData(this.global_address, ag, true, null, false, counterSelector);
			}

		}

	}

	protected void newCGRSynchroDetected(CGRSynchro cgr) {
		if (cgr.getCode() == Code.REQUEST_ROLE || cgr.getCode() == Code.LEAVE_ROLE
				|| cgr.getCode() == Code.LEAVE_GROUP) {
			ArrayList<Group> ag = computeLocalAcceptedAndRequestedGroups();
			MultiGroup mg = computeLocalGeneralAcceptedGroups();
			if (kernelAddressActivated && hasUsableDistantSocketAgent()
					&& ((cgr.getCode() == Code.REQUEST_ROLE
							&& computeMissedGroups(this.localAcceptedAndRequestedGroups, ag).size() > 0)
							|| ((cgr.getCode() == Code.LEAVE_ROLE || cgr.getCode() == Code.LEAVE_GROUP)
									&& computeMissedGroups(ag, this.localAcceptedAndRequestedGroups).size() > 0)))
				MadkitKernelAccess.informHooks(this, new NetworkGroupsAccessEvent(
						AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER, mg, ag, distant_kernel_address));
			localAcceptedAndRequestedGroups = ag;
			// localGeneralAcceptedGroups=mg;
		}
	}

	private MultiGroup computeLocalGeneralAcceptedGroups() {
		MultiGroup res = new MultiGroup();
		computeLocalGeneralAcceptedGroups(agents_socket, res);
		computeLocalGeneralAcceptedGroups(indirect_agents_socket, res);
		return res;
	}

	private void computeLocalGeneralAcceptedGroups(ArrayList<AgentSocketData> agents_socket, MultiGroup res) {
		for (AgentSocketData asd : agents_socket) {
			Groups g = asd.getAcceptedLocalGroups();
			if (g != null) {
				res.addGroup(g.getGroups());
			}
		}
	}

	private ArrayList<Group> computeLocalAcceptedAndRequestedGroups() {
		ArrayList<Group> ag = new ArrayList<>();
		computeLocalAcceptedAndRequestedGroups(agents_socket, ag);
		computeLocalAcceptedAndRequestedGroups(indirect_agents_socket, ag);
		return ag;
	}

	private void computeLocalAcceptedAndRequestedGroups(ArrayList<AgentSocketData> agents_socket, ArrayList<Group> ag) {
		for (AgentSocketData asd : agents_socket) {
			Groups g = asd.getAcceptedLocalGroups();
			if (g != null) {
				Group[] gs = g.getGroups().getRepresentedGroups(getKernelAddress());
				if (gs != null) {
					ag.ensureCapacity(gs.length);
					for (Group ggs : gs)
						ag.add(ggs);
				}
			}
		}
	}

	private ArrayList<Group> computeMissedGroups(ArrayList<Group> reference, ArrayList<Group> listToTest) {
		ArrayList<Group> res = new ArrayList<>();
		for (Group g : listToTest) {
			boolean found = false;
			for (Group g2 : reference) {
				if (g.equals(g2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				res.add(g);
			}
		}
		return res;
	}

	protected void updateLocalAcceptedGroups() throws MadkitException {

		if (kernelAddressActivated && hasUsableDistantSocketAgent()) {
			ArrayList<Group> ag = computeLocalAcceptedAndRequestedGroups();
			ArrayList<Group> newAcceptedGroups = computeMissedGroups(this.localAcceptedAndRequestedGroups, ag);
			ArrayList<Group> removedAcceptedGroups = computeMissedGroups(ag, this.localAcceptedAndRequestedGroups);

			if (newAcceptedGroups.size() > 0 || removedAcceptedGroups.size() > 0) {
				MultiGroup mg = computeLocalGeneralAcceptedGroups();
				MadkitKernelAccess.informHooks(this, new NetworkGroupsAccessEvent(
						AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER, mg, ag, distant_kernel_address));

				Map<String, Map<Group, Map<String, Set<AgentAddress>>>> agent_addresses = getOrganizationSnapShot(
						newAcceptedGroups, false);

				if (!agent_addresses.isEmpty()) {

					CGRSynchrosSystemMessage message = new CGRSynchrosSystemMessage(agent_addresses, getKernelAddress(),
							removedAcceptedGroups);
					AgentSocketData asd = getBestAgentSocket(false);
					if (asd != null) {
						sendData(asd.getAgentAddress(), message, true, null, false, asd.getCounterSelector());
					} else if (logger != null)
						logger.severe("Unexpected access (updateLocalAcceptedGroups)");
				}
			}
			localAcceptedAndRequestedGroups = ag;
		}

		// calGeneralAcceptedGroups=mg;

	}

	/*
	 * protected void sendData(AgentAddress receiver, Serializable _data) throws
	 * MadkitException { sendData(receiver, _data, false, null, false); } protected
	 * void sendData(AgentAddress receiver, Serializable _data, boolean prioritary,
	 * boolean last_message) throws MadkitException { sendData(receiver, _data,
	 * prioritary, null, last_message); } protected void sendData(AgentAddress
	 * receiver, Serializable _data, MessageLocker _local_lan_message, boolean
	 * last_message) throws MadkitException { sendData(receiver, _data, false,
	 * _local_lan_message, last_message); }
	 */
	protected void sendData(AgentAddress receiver, SystemMessage _data, boolean prioritary,
			MessageLocker _messageLocker, boolean last_message, CounterSelector counterSelector) throws NIOException {
		if (!NetworkProperties.checkSystemMessageCompatibility(_data))
			throw new NIOException("The system message of type "+_data.getClass()+" does not contain readObject and writeObject functions.");
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			try (ObjectOutputStream oos = new OOS(baos)) {
				oos.writeObject(_data);
			}
			baos.flush();

			WritePacket packet = new WritePacket(PacketPartHead.TYPE_PACKET, getNewPacketID(),
					getMadkitConfig().networkProperties.maxBufferSize,
					getMadkitConfig().networkProperties.maxRandomPacketValues, random,
					new RandomByteArrayInputStream(baos.toByteArray()));
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Sending data (distantInterfacedKernelAddress=" + distant_kernel_address + ", packetID="
						+ packet.getID() + ") : " + _data);
			
			if (!sendMessage(receiver,
					new DistKernADataToUpgradeMessage(
							new PacketData(receiver, _data, packet, _messageLocker, last_message, prioritary, _data.excludedFromEncryption(), counterSelector)))
									.equals(ReturnCode.SUCCESS))
				logger.warning("Fail sending data (distantInterfacedKernelAddress=" + distant_kernel_address
						+ ", packetID=" + packet.getID() + ") : " + _data);
		} catch (PacketException | IOException e) {
			throw new NIOException(e);
		}
	}

	private class OOS extends ObjectOutputStream {
		private NetworkProperties np=getMadkitConfig().networkProperties;
		OOS(java.io.OutputStream _out) throws IOException {
			super(_out);
			enableReplaceObject(true);
		}
		
	
		
		@Override
		protected void annotateClass(Class<?> cl) throws IOException {
			if (np.isAcceptedClassForSerializationUsingPatterns(cl.getName()))
			{
				if (np.isAcceptedClassForSerializationUsingWhiteClassList(cl))
					return;
			}
			throw new IOException("The class "+cl+" is not authorized to be serialized. See NetworkProperties class to add new classes to be authorized to be serialized.");
	    }
		@Override
		protected void annotateProxyClass(Class<?> cl) throws IOException {
			annotateClass(cl);
			for (Class<?> c : cl.getInterfaces())
			{
				if (np.isDeniedClassForSerializationUsingPatterns(c.getName()) || np.isDeniedClassForSerializationUsingBlackClassList(c))
				{
					throw new IOException("The class "+c+" is not authorized to be serialized. See NetworkProperties class to add new classes to be authorized to be serialized.");
				}
			}
	    }

		@Override
		protected Object replaceObject(Object obj) {
			if (obj instanceof KernelAddressInterfaced) {
				return ((KernelAddressInterfaced) obj).getOriginalKernelAddress();
			} else if (obj instanceof ConversationID) {
				if (obj.getClass() == BigDataTransferID.class) {
					return MadkitKernelAccess.getBigDataTransferIDInstance(
							MadkitKernelAccess.getInterfacedConversationIDToDistantPeer((ConversationID) obj,
									DistantKernelAgent.this, getKernelAddress(),
									DistantKernelAgent.this.distant_kernel_address),
							((BigDataTransferID) obj).getBytePerSecondsStat());
				} else if (obj.getClass() == TaskID.class) {
					return MadkitKernelAccess.getTaskIDInstance(MadkitKernelAccess
							.getInterfacedConversationIDToDistantPeer((ConversationID) obj, DistantKernelAgent.this,
									getKernelAddress(), DistantKernelAgent.this.distant_kernel_address));
				} else
					return MadkitKernelAccess.getInterfacedConversationIDToDistantPeer((ConversationID) obj,
							DistantKernelAgent.this, getKernelAddress(),
							DistantKernelAgent.this.distant_kernel_address);
			} else
				return obj;
		}
	}

	private class OIS extends ObjectInputStream {

		private NetworkProperties np=getMadkitConfig().networkProperties;
		public OIS(InputStream _in) throws IOException {
			super(_in);
			enableResolveObject(true);

		}
		
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException, IOException
		{
			if (np.isAcceptedClassForSerializationUsingPatterns(desc.getName()))
			{
				Class<?> c=super.resolveClass(desc);
				if (c==null)
					return null;
				if (np.isAcceptedClassForSerializationUsingWhiteClassList(c))
					return c;
			}
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(desc.getName()));
		}
		
		@Override
	    protected Class<?> resolveProxyClass(String[] interfaces)
	            throws IOException, ClassNotFoundException{
			for (String s : interfaces)
			{
				if (np.isDeniedClassForSerializationUsingPatterns(s))
					throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(s));
			}
			Class<?> c=super.resolveProxyClass(interfaces);
			if (c==null)
				return null;
			if (np.isDeniedClassForSerializationUsingBlackClassList(c))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, new ClassNotFoundException(c.getName()));
			return c;
		}
	            

		

		@Override
		protected Object resolveObject(Object obj) {
			if (obj.getClass() == KernelAddress.class) {
				KernelAddressInterfaced kai = networkBlacboard.getKernelAddressInterfaced((KernelAddress) obj);
				if (kai != null)
					return kai;
				else {
					return obj;
				}
			} else if (obj instanceof ConversationID) {
				if (obj.getClass() == BigDataTransferID.class) {
					return MadkitKernelAccess.getBigDataTransferIDInstance(
							MadkitKernelAccess.getInterfacedConversationIDFromDistantPeer((ConversationID) obj,
									DistantKernelAgent.this, getKernelAddress(),
									DistantKernelAgent.this.distant_kernel_address),
							((BigDataTransferID) obj).getBytePerSecondsStat());
				} else if (obj.getClass() == TaskID.class) {
					return MadkitKernelAccess.getTaskIDInstance(MadkitKernelAccess
							.getInterfacedConversationIDFromDistantPeer((ConversationID) obj, DistantKernelAgent.this,
									getKernelAddress(), DistantKernelAgent.this.distant_kernel_address));
				} else
					return MadkitKernelAccess.getInterfacedConversationIDFromDistantPeer((ConversationID) obj,
							DistantKernelAgent.this, getKernelAddress(),
							DistantKernelAgent.this.distant_kernel_address);
			} else
				return obj;
		}
	}

	protected int getNewPacketID() {
		synchronized (packet_id_generator) {
			return packet_id_generator.getNewID();
		}
	}

	protected void removePacketID(int id) {
		synchronized (packet_id_generator) {
			packet_id_generator.removeID(id);
		}
	}

	abstract class AbstractPacketData extends AbstractData {
		protected final WritePacket packet;
		protected ByteBuffer currentByteBuffer = null;
		protected Block currentBlock = null;

		private boolean asking_new_buffer_in_process;
		protected ByteBuffer nextByteBuffer;
		protected Block nextBlock = null;

		private RealTimeTransfertStat stat;
		private IDTransfer idTransfer = null;
		private final AtomicBoolean unlocked = new AtomicBoolean(false);
		protected final AgentAddress firstAgentSocketSender;
		private final AgentAddress agentReceiver;
		protected final AtomicBoolean isCanceled = new AtomicBoolean(false);
		protected final boolean excludedFromEncryption;
		private byte counterID=-1;
		private byte nextCounterID=-1;
		private boolean currentCounterIDReleased=true;
		private final CounterSelector counterSelector;

		protected AbstractPacketData(boolean priority, AgentAddress firstAgentSocketSender, WritePacket _packet,
				AgentAddress agentReceiver, boolean excludedFromEncryption, CounterSelector counterSelector) {
			super(priority);
			if (_packet == null)
				throw new NullPointerException("_packet");
			if (firstAgentSocketSender == null && !_packet.concernsBigData())
				throw new NullPointerException("firstAgentSocketSender");
			this.firstAgentSocketSender = firstAgentSocketSender;
			packet = _packet;
			currentByteBuffer = null;
			nextByteBuffer = null;
			currentBlock = null;
			nextBlock = null;
			asking_new_buffer_in_process = true;
			stat = null;
			this.agentReceiver = agentReceiver;
			this.excludedFromEncryption=excludedFromEncryption;
			this.counterSelector=counterSelector;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[idPacket=" + packet.getID() + ", unlocked=" + unlocked.get()
					+ ", canceled=" + isCanceled.get() + ", totalDataToSendLenght(Without connection protocol)="
					+ this.packet.getDataLengthWithHashIncluded() + ", currentByteBuffer="
					+ (currentByteBuffer == null ? null : new Integer(currentByteBuffer.capacity()))
					+ ", currentByteBufferRemaining="
					+ (currentByteBuffer == null ? null : new Integer(currentByteBuffer.remaining())) + ", dataSent="
					+ this.packet.getReadDataLengthIncludingHash() + "]";
		}

		void cancel() {
			isCanceled.set(true);
		}

		@Override
		public void reset() {
			synchronized (this) {
				if (currentByteBuffer != null) {
					currentByteBuffer.rewind();
					asking_new_buffer_in_process = false;
					DistantKernelAgent.this.receiveMessage(new DistKernADataToUpgradeMessage(this));
				}
			}
		}

		public boolean needNewByteBuffer() {
			synchronized (this) {
				return asking_new_buffer_in_process;
			}
		}

		@Override
		public ByteBuffer getByteBuffer() throws PacketException {
			synchronized (this) {
				if (currentByteBuffer == null) {
					if (!asking_new_buffer_in_process) {
						if (nextByteBuffer == null && !packet.isFinished()) {
							updateNextByteBuffer();
							asking_new_buffer_in_process = true;
						}
					}
					return null;
				}
				if (currentByteBuffer.remaining() == 0) {
					if (!currentCounterIDReleased)
					{
						currentCounterIDReleased=true;
						counterSelector.releaseCounterID(counterID);
					}
					if (asking_new_buffer_in_process) {
						return null;
					} else {
						if (stat != null)
							stat.newBytesIndentified(currentByteBuffer.capacity());

						if (nextByteBuffer == null) {
							
							currentByteBuffer = null;
							currentBlock = null;
							if (packet.isFinished()) {
								return null;
							} else {

								updateNextByteBuffer();
								return null;
							}
						} else {
							currentCounterIDReleased=false;
							counterID=nextCounterID;
							nextCounterID=-1;
							
							currentByteBuffer = nextByteBuffer;
							currentBlock = null;
							nextByteBuffer = null;
							nextBlock.setCounterSelector(counterSelector);
							nextBlock=null;
							
							if (!packet.isFinished()) {
								updateNextByteBuffer();
							}

							return currentByteBuffer;
						}

					}
				} else {
					if (!asking_new_buffer_in_process) {
						ByteBuffer bb = nextByteBuffer;
						if (bb == null && !packet.isFinished()) {
							updateNextByteBuffer();
						}
					}
					if (currentBlock!=null)
					{
						currentBlock.setCounterSelector(counterSelector);
						currentBlock=null;
					}
					return currentByteBuffer;

				}
			}
		}

		protected void setNewBlock(IDTransfer id, Block _block) throws NIOException {
			synchronized (this) {
				idTransfer = id;
				if (currentByteBuffer == null) {
					currentCounterIDReleased=false;
					counterID=_block.getCounterID();
					currentByteBuffer = ByteBuffer.wrap((currentBlock=_block).getBytes());
					if (!packet.isFinished())
						updateNextByteBuffer();
					else
						asking_new_buffer_in_process = false;
				} else if (nextByteBuffer == null) {
					nextCounterID=_block.getCounterID();
					nextByteBuffer = ByteBuffer.wrap((nextBlock=_block).getBytes());
					asking_new_buffer_in_process = false;
				} else
					throw new NIOException("Unexpected exception !");
			}
		}

		@Override
		public boolean isReady() {
			if (isCanceled.get())
				return true;
			synchronized (this) {
				if (currentByteBuffer == null)
					return false;
				if (currentByteBuffer.remaining() == 0) {
					if (asking_new_buffer_in_process) {
						return false;
					} else {
						return nextByteBuffer != null;
					}
				} else {
					return true;
				}
			}
		}

		@Override
		public boolean isFinished() throws PacketException {
			if (isCanceled.get() && isCurrentByteBufferFinished())
				return true;
			synchronized (this) {
				if (asking_new_buffer_in_process) {
					return false;
				} else if (currentByteBuffer == null || currentByteBuffer.remaining() == 0) {
					if (nextByteBuffer == null) {
						return true;
					} else
						return false;
				} else {
					return false;
				}
			}
		}

		protected void finishLastStat() {
			synchronized (this) {
				if (currentByteBuffer != null && stat != null) {
					stat.newBytesIndentified(currentByteBuffer.capacity() - currentByteBuffer.remaining());
				}
			}
		}

		@Override
		public boolean isCurrentByteBufferStarted() {
			synchronized (this) {
				return currentByteBuffer != null && currentByteBuffer.position() > 0;
			}
		}

		@Override
		public boolean isCurrentByteBufferFinished() throws PacketException {
			synchronized (this) {
				boolean res=currentByteBuffer == null || currentByteBuffer.remaining() == 0;
				if (res && !currentCounterIDReleased)
				{
					currentCounterIDReleased=true;
					counterSelector.releaseCounterID(counterID);
				}
				return res;
			}
		}

		@Override
		public DataTransferType getDataTransferType() {
			return packet.concernsBigData() ? DataTransferType.BIG_DATA : DataTransferType.SHORT_DATA;
		}

		public int getIDPacket() {
			return packet.getID();
		}

		private void updateNextByteBuffer() {
			synchronized (this) {
				asking_new_buffer_in_process = true;
				DistantKernelAgent.this.receiveMessage(new DistKernADataToUpgradeMessage(this));
			}
		}

		public IDTransfer getIDTransfer() {
			return idTransfer;
		}

		void setStat(RealTimeTransfertStat stat) {
			synchronized (this) {
				this.stat = stat;
			}
		}

		abstract boolean isSystemMessage();

		@Override
		public void unlockMessage() throws MadkitException {
			if (!currentCounterIDReleased)
			{
				currentCounterIDReleased=true;
				counterSelector.releaseCounterID(counterID);
				if (nextByteBuffer!=null)
				{
					counterSelector.releaseCounterID(nextCounterID);					
				}
			}
			unlocked.set(true);
		}

		@Override
		public boolean isUnlocked() {
			return unlocked.get();
		}

		/*@Override
		public void finalize() {
			try {
				unlockMessage();
			} catch (Exception e) {
				if (logger != null)
					logger.severeLog("Unexpected error", e);
			}
		}*/

		AgentAddress getFirstAgentSocketSender() {
			return firstAgentSocketSender;
		}

		AgentAddress getAgentSocketSender() {
			return getFirstAgentSocketSender();
			/*
			 * if (packet.concernsBigData()) { return getBestAgentSocket(agentReceiver,
			 * true).getAgentAddress(); } else return getFirstAgentSocketSender();
			 */

		}

		public long getReadDataLength() {
			return packet.getReadDataLength();
		}

		public long getReadDataLengthIncludingHash() {
			return packet.getReadDataLengthIncludingHash();
		}

		AgentAddress getReceiver() {
			return this.agentReceiver;
		}
	}

	class PacketData extends AbstractPacketData {

		private final boolean last_message;

		protected final LanMessage original_lan_message;

		private final MessageLocker messageLocker;

		private final boolean isSystemMessage;

		protected PacketData(AgentAddress first_receiver, SystemMessage lan_message, WritePacket _packet,
				MessageLocker _messageLocker, boolean _last_message, boolean pioririty, boolean excludedFromEncryption, CounterSelector counterSelector) {
			super(pioririty, first_receiver, _packet,
					(lan_message instanceof LanMessage) ? ((LanMessage) lan_message).message.getReceiver() : null, excludedFromEncryption, counterSelector);
			if (_packet.concernsBigData())
				throw new IllegalArgumentException("_packet cannot use big data !");

			last_message = _last_message;

			isSystemMessage = !(lan_message instanceof LanMessage);

			this.original_lan_message = isSystemMessage ? null : (LanMessage) lan_message;

			messageLocker = _messageLocker;
			/*
			 * if (messageLocker!=null) messageLocker.lock();
			 */
		}

		@Override
		boolean isSystemMessage() {
			return isSystemMessage;
		}

		@Override
		public boolean isLastMessage() {
			return last_message;
		}

		@Override
		public void unlockMessage() throws MadkitException {
			synchronized (this) {
				try {
					if (messageLocker != null && !isUnlocked()) {
						finishLastStat();
						long sendLength = packet.getReadDataLengthIncludingHash();
						if (currentByteBuffer != null)
							sendLength -= currentByteBuffer.remaining();
						if (nextByteBuffer != null)
							sendLength -= nextByteBuffer.remaining();
						messageLocker.unlock(distant_kernel_address, new DataTransfertResult(
								packet.getInputStream().length(), packet.getReadDataLength(), sendLength));
						
					}
					super.unlockMessage();
				} catch (IOException e) {
					throw new MadkitException(e);
				}
			}
		}

		@Override
		public void finalize() {
			removePacketID(packet.getID());
			super.finalize();
		}

	}

	class BigPacketData extends AbstractPacketData {
		private final AgentAddress asker;
		private ConversationID conversationID;
		private long timeUTC;

		protected BigPacketData(AgentAddress _firstAgentSocketSender, WritePacket _packet, AgentAddress _agentReceiver,
				AgentAddress asker, ConversationID conversationID, RealTimeTransfertStat stat, boolean excludedFromEncryption, CounterSelector counterSelector) {
			super(false, _firstAgentSocketSender, _packet, _agentReceiver, excludedFromEncryption, counterSelector);
			if (!_packet.concernsBigData())
				throw new IllegalArgumentException("_packet has to use big data !");
			if (asker == null)
				throw new NullPointerException("asker");
			if (conversationID == null)
				throw new NullPointerException("conversationID");
			this.asker = asker;
			this.conversationID = conversationID;
			setStat(stat);
			timeUTC = System.currentTimeMillis();
		}

		@Override
		boolean isSystemMessage() {
			return false;
		}

		AgentAddress getAsker() {
			return asker;
		}

		ConversationID getConversationID() {
			return conversationID;
		}

		long getDuration() {
			return System.currentTimeMillis() - timeUTC;
		}

	}

	static class DistKernADataToUpgradeMessage extends NIOMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4831211172007935978L;

		final AbstractPacketData dataToUpgrade;

		DistKernADataToUpgradeMessage(AbstractPacketData _data) {
			dataToUpgrade = _data;
			
		}
	}

	static class SendDataFromAgentSocket extends ObjectMessage<SystemMessage> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3463059956980205683L;

		final boolean last_message;
		final boolean prioritary;

		public SendDataFromAgentSocket(SystemMessage _content, boolean last_message, boolean prioritary) {
			super(_content);
			this.last_message = last_message;
			this.prioritary = prioritary;

		}

		@Override
		public String toString() {
			return "SendDataFromAgentSocket[last_message=" + last_message + ", prioritary=" + prioritary
					+ ", systemMessage=" + getContent() + "]";
		}

	}

	private final HashMap<Integer, SerializedReading> current_short_readings = new HashMap<>();
	private final HashMap<Integer, BigDataReading> current_big_data_readings = new HashMap<>();

	private class Reading {
		protected final MessageDigestType messageDigestType;
		protected ReadPacket read_packet;
		protected final RandomOutputStream output_stream;

		protected Reading(MessageDigestType messageDigestType, RandomOutputStream os) {
			if (os == null)
				throw new NullPointerException("os");
			this.messageDigestType = messageDigestType;
			read_packet = null;
			output_stream = os;

		}

		protected Reading(MessageDigestType messageDigestType, PacketPart _first_part, RandomOutputStream os)
				throws PacketException {
			if (os == null)
				throw new NullPointerException("os");
			this.messageDigestType = messageDigestType;
			output_stream = os;
			read_packet = new ReadPacket(getMadkitConfig().networkProperties.maxBufferSize,
					getMadkitConfig().networkProperties.maxRandomPacketValues, _first_part, output_stream,
					messageDigestType);
		}

		public void closeStream() throws IOException {
			output_stream.close();
		}

		public int getIdentifier() {
			return read_packet.getID();
		}

		public boolean isFinished() {
			return read_packet.isFinished();
		}

		public void readNewPart(PacketPart _part) throws PacketException {
			if (read_packet == null)
				read_packet = new ReadPacket(getMadkitConfig().networkProperties.maxBufferSize,
						getMadkitConfig().networkProperties.maxRandomPacketValues, _part, output_stream,
						messageDigestType);
			read_packet.readNewPart(_part);
		}

		public boolean isValid() {
			return read_packet == null || read_packet.isValid();
		}

		public boolean isInvalid() {
			return read_packet == null || read_packet.isInvalid();
		}

		public boolean isTemporaryInvalid() {
			return read_packet == null || read_packet.isTemporaryInvalid();
		}

		ReadPacket getReadPacket() {
			return read_packet;
		}

	}

	private class SerializedReading extends Reading {
		private long dataSize;
		private final AgentAddress initialSocketAgent;

		public SerializedReading(AgentAddress initialSocketAgent, PacketPart _part) throws PacketException {
			super(null, _part, new RandomByteArrayOutputStream());
			this.initialSocketAgent = initialSocketAgent;
			dataSize = _part.getBytes().length;
			incrementTotalDataQueue(_part.getBytes().length);
		}

		public byte[] getBytes() {
			return ((RandomByteArrayOutputStream) output_stream).getBytes();
		}

		@Override
		public void readNewPart(PacketPart _part) throws PacketException {
			dataSize += _part.getBytes().length;
			incrementTotalDataQueue(_part.getBytes().length);
			super.readNewPart(_part);
		}

		public long getDataSize() {
			return dataSize;
		}

		void freeDataSize() {
			decrementTotalDataQueue(dataSize);
			dataSize = 0;
		}

		AgentAddress getInitialAgentAddress() {
			return initialSocketAgent;
		}
	}

	private class BigDataReading extends Reading {
		private int identifier;
		private final BigDataPropositionMessage originalMessage;
		private final RealTimeTransfertStat stat;
		private int data = 0;

		public BigDataReading(BigDataPropositionMessage m) {
			super(m.getMessageDigestType(), MadkitKernelAccess.getOutputStream(m));
			identifier = MadkitKernelAccess.getIDPacket(m);
			this.originalMessage = m;
			stat = m.getStatistics();
		}

		@Override
		public int getIdentifier() {
			return identifier;
		}

		@Override
		public void readNewPart(PacketPart _part) throws PacketException {
			data += _part.getBytes().length;
			incrementTotalDataQueue(_part.getBytes().length);
			if (read_packet == null)
				read_packet = new ReadPacket(getMadkitConfig().networkProperties.maxBufferSize,
						getMadkitConfig().networkProperties.maxRandomPacketValues, _part, output_stream,
						messageDigestType);
			else
				read_packet.readNewPart(_part);

		}

		public void freeDataSize() {
			decrementTotalDataQueue(data);
			data = 0;
		}

		BigDataPropositionMessage getOriginalMessage() {
			return originalMessage;
		}

		RealTimeTransfertStat getStatistics() {
			return stat;
		}
	}

	protected void receiveData(AgentAddress agent_socket_sender, PacketPart p) {

		boolean bigData = false;

		Reading reading = current_short_readings.get(new Integer(p.getHead().getID()));
		if (reading == null) {
			reading = current_big_data_readings.get(new Integer(p.getHead().getID()));

			if (reading != null)
				bigData = true;
		}
		if (bigData) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Receiving block data for big data transfer from " + agent_socket_sender
						+ " (distantInterfacedKernelAddress=" + distant_kernel_address + ") : " + p);

			BigDataReading sr = (BigDataReading) reading;

			if (sr.isValid() || (sr.isTemporaryInvalid() && p.getHead().isRedownloadedPacketPart())) {
				try {
					sr.readNewPart(p);
					sr.getStatistics().newBytesIndentified(p.getBytes().length);
					if (sr.isInvalid()) {
						MadkitKernelAccess.dataCorrupted(sr.getOriginalMessage(),
								sr.getReadPacket().getWritedDataLength());
						current_big_data_readings.remove(new Integer(reading.getIdentifier()));
						processInvalidPacketPart(agent_socket_sender,
								new PacketException("The given packet is not valid."), p, false);
						try {
							sr.closeStream();
						} catch (Exception e2) {

						}
					} else if (sr.isFinished()) {
						sr.closeStream();
						MadkitKernelAccess.transferCompleted(sr.getOriginalMessage(),
								sr.getReadPacket().getWritedDataLength());
						current_big_data_readings.remove(new Integer(reading.getIdentifier()));
					}
				} catch (PacketException | IOException e) {
					MadkitKernelAccess.dataCorrupted(sr.getOriginalMessage(), sr.getReadPacket().getWritedDataLength());
					current_big_data_readings.remove(new Integer(reading.getIdentifier()));
					processInvalidPacketPart(agent_socket_sender, e, p, false);
					try {
						sr.closeStream();
					} catch (Exception e2) {

					}

				}
				sr.freeDataSize();
			} else if (sr.isInvalid()) {
				MadkitKernelAccess.dataCorrupted(sr.getOriginalMessage(), sr.getReadPacket().getWritedDataLength());
				current_big_data_readings.remove(new Integer(reading.getIdentifier()));
				processInvalidPacketPart(agent_socket_sender, new PacketException("The given packet is not valid."), p,
						false);
				try {
					sr.closeStream();
				} catch (Exception e2) {

				}
			}
		} else {
			if (p.getHead().getTotalLength() > getMadkitConfig().networkProperties.maxShortDataSize)
				processInvalidPacketPart(agent_socket_sender,
						new PacketException(
								"The given packet have not be traeted as a big data, and has a too big size : "
										+ p.getHead().getTotalLength()),
						p, false);
			else {
				SerializedReading sr = null;

				if (reading == null) {
					// new short data received
					try {
						if (logger != null)
							logger.finest("Receiving block data for new short data transfer from " + agent_socket_sender
									+ " (distantInterfacedKernelAddress=" + distant_kernel_address + ") : " + p);

						reading = sr = new SerializedReading(agent_socket_sender, p);

						if (sr.isInvalid()) {
							processInvalidPacketPart(agent_socket_sender,
									new PacketException("The given packet is not valid."), p, false);
							try {
								sr.closeStream();
							} catch (Exception e2) {

							}
						}
						// check too simultaneous short data sent
						boolean tooMuch = current_short_readings.size() >= this.agents_socket.size()
								+ this.indirect_agents_socket.size();
						if (!tooMuch) {
							for (SerializedReading sr2 : current_short_readings.values()) {
								if (sr2.getInitialAgentAddress().equals(agent_socket_sender)) {
									tooMuch = true;
									break;
								}
							}
						}
						if (tooMuch) {
							processTooMuchSimutaneousShortDataSent();
						} else {
							current_short_readings.put(new Integer(p.getHead().getID()), sr);

						}
					} catch (PacketException e) {
						processInvalidPacketPart(agent_socket_sender, e, p, false);
					}
				} else {
					sr = ((SerializedReading) reading);
					if (reading.isValid() || (reading.isTemporaryInvalid() && p.getHead().isRedownloadedPacketPart())) {
						try {
							if (logger != null)
								logger.finest("Receiving block data and updating short data transfer from "
										+ agent_socket_sender + " (distantInterfacedKernelAddress="
										+ distant_kernel_address + ") : " + p);

							reading.readNewPart(p);
							if (sr.isInvalid()) {
								processInvalidPacketPart(agent_socket_sender,
										new PacketException("The given packet is not valid."), p, false);
								try {
									sr.closeStream();
								} catch (Exception e2) {

								}
							}
						} catch (PacketException e) {
							processInvalidPacketPart(agent_socket_sender, e, p, false);
							sr.freeDataSize();
						}
					}
				}
				if (reading != null && sr.isFinished()) {
					if (sr.isInvalid()) {
						sr.freeDataSize();
					} else {
						try {
							sr.closeStream();
							byte[] bytes = sr.getBytes();
							try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes)) {

								try (ObjectInputStream ois = new OIS(bais)) {
									Object obj = ois.readObject();

									receiveData(agent_socket_sender, obj, sr.getDataSize());

								}
							} catch (IOException | ClassNotFoundException e) {
								sr.freeDataSize();
								processInvalidSerializedData(agent_socket_sender, e, sr.read_packet, bytes);
							}
						} catch (IOException e) {

							sr.freeDataSize();
							sendMessageWithRole(agent_socket_sender,
									new AskForConnectionMessage(ConnectionClosedReason.CONNECTION_ANOMALY, null, null,
											null, false, false),
									LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
						}
					}
					Integer id = new Integer(reading.getIdentifier());
					current_short_readings.remove(id);
					setOneSocketPurged(id, sr);

				}

			}
		}
	}

	public void receiveData(AgentAddress agent_socket_sender, Object obj, long dataSize) {
		if (obj instanceof SystemMessage) {
			SystemMessage sm = ((SystemMessage) obj);
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Receiving system message from " + agent_socket_sender
						+ " (distantInterfacedKernelAddress=" + distant_kernel_address + ") : " + sm);

			sendMessageWithRole(agent_socket_sender, new ReceivedSerializableObject(sm, dataSize),
					LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		} else
			processInvalidSerializedObject(agent_socket_sender, null, obj, true);
	}

	class ReceivedSerializableObject extends ObjectMessage<SystemMessage> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 91965364117774967L;

		private final long dataIncrement;
		private final AtomicLong dataSize = new AtomicLong(0);

		public ReceivedSerializableObject(SystemMessage _content, long dataSize) {
			super(_content);
			this.dataSize.set(dataSize);
			this.dataIncrement = dataSize;
		}

		KernelAddressInterfaced getKernelAddressInterfaced() {
			return DistantKernelAgent.this.distant_kernel_address;
		}

		void markDataAsRead() {
			if (dataSize.addAndGet(-dataIncrement) >= 0) {
				decrementTotalDataQueue(dataIncrement);
			} else {
				dataSize.set(0);
				if (logger != null)
					logger.finer("Illgal computed data size queue !");
			}
		}

		@Override
		public ReceivedSerializableObject clone() {
			incrementTotalDataQueue(dataIncrement);
			dataSize.addAndGet(dataIncrement);
			return this;
		}

	}

	private void setTansfertPaused(boolean value) {
		setTansfertPaused(value, null, false);
	}

	private void setTansfertPaused(boolean value, Set<AgentAddress> agents, boolean force) {
		if (!force) {
			ExceededDataQueueSize e = this.globalExeceededDataQueueSize.get();
			if (e != null) {
				synchronized (networkBlacboard.candidatesForPurge) {
					if (e.isPaused()) {
						if (!e.mustPurge())
							value = true;
						else
							value = false;
					}
				}
			}
		}
		/*
		 * if (e!=null && e.isPaused() && !e.mustPurge()) value=true; if (e!=null &&
		 * value && e.isPurging(this)) { value=false; }
		 */

		if (this.transfertPaused.compareAndSet(!value, value) || force) {
			if (logger != null && logger.isLoggable(Level.FINEST))
				logger.finest("Set transfer paused (distantInterfacedKernelAddress=" + distant_kernel_address
						+ ", pause=" + value + ", force=" + force + ", agents=" + agents + ")");

			if (getState().compareTo(State.ENDING) >= 0)
				return;

			if (agents == null) {
				// broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(distant_kernel_address),
				// LocalCommunity.Roles.SOCKET_AGENT_ROLE, new
				// ExceededDataQueueSize(networkBlacboard, value),
				// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(getNetworkID()),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE,
						new ExceededDataQueueSize(networkBlacboard, false, value),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
			} else {
				for (AgentAddress aa : agents)
					sendMessageWithRole(aa, new ExceededDataQueueSize(networkBlacboard, false, value),
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
			}
		}
	}

	private long getMaxSizeForUnreadShortDataFromOneDistantKernel() {
		// return
		// Math.max(getMadkitConfig().networkProperties.maxSizeForUnreadShortDataFromOneDistantKernel,
		// getMadkitConfig().networkProperties.numberOfMaximumSimultaneousNonBigDataTransfers*getMadkitConfig().networkProperties.maxShortDataSize);
		return Math.max(getMadkitConfig().networkProperties.maxSizeForUnreadShortDataFromOneDistantKernel,
				getMadkitConfig().networkProperties.numberOfMaximumConnectionsBetweenTwoSameKernelsAndMachines
						* getMadkitConfig().networkProperties.maxShortDataSize);
	}

	private long getMaxSizeForUnreadShortDataFromAllDistantKernel() {
		return getMadkitConfig().networkProperties.maxSizeForUnreadShortDataFromAllConnections;
	}

	void incrementTotalDataQueue(long value) {
		if (value < 0)
			throw new IllegalAccessError();
		if (totalDataInQueue.addAndGet(value) > getMaxSizeForUnreadShortDataFromOneDistantKernel())
			setTansfertPaused(true);
		incrementTotalDataQueueForAllDistantKernelAgent(value);

	}

	private void purgeTotalDataQueue() {
		decrementTotalDataQueueAllDistantKernelAgent(totalDataInQueue.getAndSet(0));
		new ExceededDataQueueSize(networkBlacboard, false, false).purgeCanceled(this);
	}

	void decrementTotalDataQueue(long value) {
		if (getState().compareTo(State.ENDING) >= 0)
			return;
		if (value < 0)
			throw new IllegalAccessError();
		long val = totalDataInQueue.addAndGet(-value);
		if (val < 0) {
			totalDataInQueue.addAndGet(-val);
			if (logger != null)
				logger.severe("DistantKernelAgent.totalDataInQueue cannot be negative");
		}
		/*
		 * boolean ok=true; if (val==0) { ExceededDataQueueSize
		 * e=this.globalExeceededDataQueueSize.get(); if (e!=null && e.isPaused()) {
		 * e.purgeFinished(this); setTansfertPaused(true); ok=false; } }
		 */
		if (val < getMaxSizeForUnreadShortDataFromOneDistantKernel())
			setTansfertPaused(false);

		decrementTotalDataQueueAllDistantKernelAgent(value);
	}

	private void setOneSocketPurged(Integer id, SerializedReading sr) {
		ExceededDataQueueSize e = this.globalExeceededDataQueueSize.get();
		if (e != null) {
			e.setOneSocketPurged(this, id, sr);
		}
	}

	void setGlobalTransfersPaused(boolean value) {
		synchronized (networkBlacboard.candidatesForPurge) {
			if (value && !hasToPauseGlobalTransfers())
				return;
			if (!value && hasToPauseGlobalTransfers())
				return;

			if (networkBlacboard.transfertPausedForAllDistantKernelAgent.compareAndSet(!value, value)) {
				if (logger != null && logger.isLoggable(Level.FINEST))
					logger.finest("Set global transfer paused (distantInterfacedKernelAddress=" + distant_kernel_address
							+ ", pause=" + value + ")");

				if (value) {
					networkBlacboard.currentCandidateForPurge = this;
					// set paused
					ReturnCode rc = broadcastMessageWithRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE,
							new ExceededDataQueueSize(networkBlacboard, true, false),
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					/*
					 * for (AgentAddress aa :
					 * getAgentsWithRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
					 * LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE)) { Message m=new
					 * ExceededDataQueueSize(networkBlacboard, true, false); sendMessageWithRole(aa,
					 * m, LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE); }
					 */
					// broadcastMessageWithRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
					// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, m,
					// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					Message m = new ExceededDataQueueSize(networkBlacboard, rc == ReturnCode.SUCCESS, true, true);
					this.receiveMessage(m);
				} else {
					// unset paused
					Message m = new ExceededDataQueueSize(networkBlacboard, false, false);
					broadcastMessageWithRole(LocalCommunity.Groups.DISTANT_KERNEL_AGENTS_GROUPS,
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE, m,
							LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
					this.receiveMessage(m);
					networkBlacboard.candidatesForPurge.clear();
					networkBlacboard.currentCandidateForPurge = null;
				}
			}
		}
	}

	private void incrementTotalDataQueueForAllDistantKernelAgent(long value) {
		if (value < 0)
			throw new IllegalAccessError();
		if (networkBlacboard.totalDataInQueueForAllDistantKernelAgent
				.addAndGet(value) > getMaxSizeForUnreadShortDataFromAllDistantKernel()) {
			setGlobalTransfersPaused(true);
		}
	}

	private boolean hasToPauseGlobalTransfers() {
		return networkBlacboard.totalDataInQueueForAllDistantKernelAgent
				.get() < getMaxSizeForUnreadShortDataFromAllDistantKernel() / 2;
	}

	private void decrementTotalDataQueueAllDistantKernelAgent(long value) {
		if (value < 0)
			throw new IllegalAccessError();
		long val = networkBlacboard.totalDataInQueueForAllDistantKernelAgent.addAndGet(-value);
		if (val < 0) {
			networkBlacboard.totalDataInQueueForAllDistantKernelAgent.set(0);
			if (logger != null)
				logger.severe("DistantKernelAgent.totalDataInQueueForAllDistantKernelAgent cannot be negative");
		}
		if (hasToPauseGlobalTransfers()) {
			setGlobalTransfersPaused(false);
		}
	}

	private void processInvalidSerializedData(AgentAddress source, Exception e, ReadPacket _read_packet, byte data[]) {
		processInvalidProcess(source, "Invalid serialized data from Kernel Address " + distant_kernel_address, e,
				false);
	}

	private void processInvalidMessage(Message _message, boolean candidate_to_ban) {
		processInvalidProcess(null, "Invalid message " + _message, candidate_to_ban);
	}

	private void processInvalidPacketPart(AgentAddress source, Exception e, PacketPart _part,
			boolean candidate_to_ban) {
		processInvalidProcess(source, "Invalid packet part from Kernel Address " + distant_kernel_address, e,
				candidate_to_ban);
	}

	private void processInvalidSerializedObject(AgentAddress source, Exception e, Object data,
			boolean candidate_to_ban) {
		processInvalidProcess(source, "Invalid serialized object from Kernel Address " + distant_kernel_address, e,
				candidate_to_ban);
	}

	private void processTooMuchSimutaneousShortDataSent() {
		// broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(distant_kernel_address),
		// LocalCommunity.Roles.SOCKET_AGENT_ROLE, new
		// AskForConnectionMessage(ConnectionClosedReason.CONNECTION_ANOMALY, null,
		// null, false), LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		processInvalidProcess(null, "Too much data sent from Kernel Address " + distant_kernel_address, true);
		if (logger != null)
			logger.severe(
					"Short data from " + this.distant_kernel_address + " sent too big. Killing related connections.");
	}

	protected void processPotentialDDOS() {
		// broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(distant_kernel_address),
		// LocalCommunity.Roles.SOCKET_AGENT_ROLE, new
		// AskForConnectionMessage(ConnectionClosedReason.CONNECTION_ANOMALY, null,
		// null, false), LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
		processInvalidProcess(null, "Potential DDOS from Kernel Address " + distant_kernel_address, true);
		if (logger != null)
			logger.severe(
					"Detected potential DDOS from " + this.distant_kernel_address + ". Killing related connections.");
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
		try {
			if (source != null) {
				// sendMessageWithRole(source, new AnomalyDetectedMessage(candidate_to_ban,
				// distant_kernel_address, message),
				// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				sendMessageWithRole(source, new AskForConnectionMessage(ConnectionClosedReason.CONNECTION_ANOMALY, null,
						null, null, false, false), LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
			} else if (distant_kernel_address != null) {
				// broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(distant_kernel_address),
				// LocalCommunity.Roles.SOCKET_AGENT_ROLE, new
				// AnomalyDetectedMessage(candidate_to_ban, distant_kernel_address, message),
				// LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
				broadcastMessageWithRole(LocalCommunity.Groups.getDistantKernelAgentGroup(getNetworkID()),
						LocalCommunity.Roles.SOCKET_AGENT_ROLE,
						new AnomalyDetectedMessage(candidate_to_ban, distant_kernel_address, message),
						LocalCommunity.Roles.DISTANT_KERNEL_AGENT_ROLE);
			}
		} catch (Exception e2) {
			if (logger != null)
				logger.severeLog("Unexpected exception", e2);
			else
				e2.printStackTrace();
		}
	}

}
