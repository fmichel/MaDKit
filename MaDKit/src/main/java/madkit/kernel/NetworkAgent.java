/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
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
 *******************************************************************************/
package madkit.kernel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.agr.SystemRoles;
import madkit.messages.EnumMessage;
import madkit.messages.ObjectMessage;
import madkit.network.CGRSynchro;
import madkit.network.NetworkCommunity;

/**
 * @author Fabien Michel
 * @version 6.4
 * @since MaDKit 5
 */
final class NetworkAgent extends Agent {

	private final Map<KernelAddress, PeerConnection> peers = new ConcurrentHashMap<>();

	private PeersConnectionServer myServer;
	private MultiCastListener multicastListener;
	private boolean running = true;

	@Override
	protected void onActivation() {
//		getLogger().setLevel(getKernelConfig().getLevel("networkLogLevel"));

//		getLogger().setLevel(LevelOption.networkLogLevel.getValue(getMadkitConfig()));
		getLogger().setLevel(Level.FINE);
		requestRole(LocalCommunity.LOCAL, Groups.NETWORK, madkit.agr.LocalCommunity.Roles.NET_AGENT);
		AgentAddress kernelAgent = getAgentWithRole(LocalCommunity.LOCAL, Groups.NETWORK, SystemRoles.GROUP_MANAGER);
		if (kernelAgent == null) {
			throw new RuntimeException(this + " !!!!!!! no kernel agent !!!!!!.. Please bug report");
		}
		running = launchNetwork();
	}

	/**
	 * @return true if servers are launched
	 */
	synchronized private boolean launchNetwork() {
		if (ReturnCode.SUCCESS != createGroup(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS, true)) {
			return false;
		}
		requestRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS, NetworkCommunity.Roles.NET_AGENT);
		if (!startServers()) {
			return false;
		}
		connectToOnlinePeers();

		getLogger().finest(() -> "Now activating all connections");
		for (PeerConnection kc : peers.values()) {
			kc.activate();
		}
//		AgentStatusPanel.updateAll();
		getLogger().info(() -> "----- " + getKernelAddress() + " network started on " + myServer + " ------\n");
		return true;
	}

	/**
	 * 
	 */
	private boolean startServers() {
		try {
			myServer = PeersConnectionServer.getNewServer(this);
			myServer.activate();
			getLogger().info(() -> "----- MaDKit server activated on " + myServer + " ------\n");
			multicastListener = MultiCastListener.getNewMultiCastListener(myServer.getPort());
			multicastListener.activate(this);
			getLogger()
					.finer(() -> "----- MaDKit MulticastListener activated on " + MultiCastListener.ipAddress + " ------\n");
			return true;
		} catch (IOException e) {
			throw new RuntimeException(this + "\n\\t\\t\\t\\t---- Unable to start the Madkit kernel servers ------\\n", e);
		}
	}

	/**
	 */
	private void connectToOnlinePeers() {
		getLogger().finer("Connect to onlive peers");
		NetworkStatusMessage m;
		do {
			m = getMailbox().waitNext(100, networkMessage -> networkMessage.getCode() == NetCode.NEW_PEER_REQUEST);
			if (m != null) {
				newPeerRequest((Socket) m.getContent()[0]);
			}
		} while (m != null);
		for (Message message = nextMessage(); message != null; message = nextMessage()) {
			handleMessage(message);
		}
	}

	/**
	 * @param s
	 */
	private void newPeerRequest(Socket s) {
		getLogger().fine("Contacted by peer " + s + " -> opening kernel connection");
		PeerConnection kc;
		try {
			kc = new PeerConnection(this, s);
		} catch (IOException e) {
			getLogger().warning(() -> "I give up: Unable to contact peer on " + s + " because " + e.getMessage());
			return;
		}
		getLogger().finer(() -> "KC opened: " + kc + "\n\tsending connection INFO");
		if (sendConnectionInfo(kc)) {
			getLogger().fine(() -> "Connection info sent, now waiting reply from " + kc.getPeerSocket() + "...");
			KernelAddress dka = waitConnectionInfo(kc);
			if (dka != null) {
				addConnection(dka, kc, isAlive());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void onLive() {
		while (isAlive() && running) {
			handleMessage(waitNextMessage());
		}
	}

	@Override
	protected void onEnd() {
		stopNetwork();
	}

	synchronized private void stopNetwork() {
		if (running) {
			getLogger().info(() -> "----- Closing network " + getKernelAddress() + " ------\n");
			getLogger().finer(() -> "Closing all connections : " + peers.values());
//			try {
//				Thread.ofPlatform().start(() -> {
//					for (Map.Entry<KernelAddress, PeerConnection> entry : peers.entrySet()) {
//						peerDeconnected(entry.getKey());
//						entry.getValue().closeConnection();
//					}
//					peers.clear();
//					getLogger().finer(() -> "Closing multicast listener and kernel server");
//					if (multicastListener != null) {
//						multicastListener.stop();
//					}
//					if (myServer != null) {
//						myServer.stop();
//						myServer = null;
//					}
//				}).join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			for (Map.Entry<KernelAddress, PeerConnection> entry : peers.entrySet()) {
				peerDeconnected(entry.getKey());
				entry.getValue().closeConnection();
			}
			peers.clear();
			getLogger().finer(() -> "Closing multicast listener and kernel server");
			if (multicastListener != null) {
				multicastListener.stop();
			}
			if (myServer != null) {
				myServer.stop();
				myServer = null;
			}
			leaveGroup(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS);
			running = false;
			// AgentStatusPanel.updateAll();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleMessage(Message m) throws ClassCastException {
		AgentAddress sender = m.getSender();
		if (sender == null) {// contacted by my private objects (or by the kernel ? no)
			proceedEnumMessage((EnumMessage<?>) m);
		} else if (sender.isFrom(getKernelAddress())) {// contacted locally
			switch (sender.getRole()) {
			case Roles.UPDATER:// It is a CGR update
				broadcastUpdate(m);
				break;
			case Roles.EMMITER:// It is a message to send elsewhere
				sendDistantMessage((ObjectMessage<Message>) m);
				break;
			case Roles.KERNEL:// message from the kernel
				proceedEnumMessage((EnumMessage<?>) m);
				break;
			default:
				getLogger().severe("not understood :\n" + m);
				break;
			}
		} else {// distant message
			switch (sender.getRole()) {
			case Roles.UPDATER:
				//// It is a distant CGR update
				CGRSynchro synchro = (CGRSynchro) m;
				getLogger().finer(() -> "Injecting distant CGR " + synchro.getCode() + " on " + synchro.getContent());
				getOrganization().injectOperation((CGRSynchro) m);
				break;
			case Roles.EMMITER:// It is a distant message to inject
//				getLogger().finer(() -> "Injecting distant message " + " : " + m);
				getKernel().injectMessage((ObjectMessage<Message>) m);
				break;
			default:
				getLogger().severe("not understood :\n" + m);
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private void exit() {
		stopNetwork();
//		running = false;
	}

	/**
	 * Removes ka from peers and clean organization accordingly
	 * 
	 * @param ka
	 */
	private void peerDeconnected(KernelAddress ka) {
		if (peers.remove(ka) != null) {
			getLogger().info(() -> "----- " + getKernelAddress() + " deconnected from " + ka + "------");
			getOrganization().removeAgentsFromDistantKernel(ka);
		}
	}

	/**
	 */
	private void addConnection(KernelAddress ka, PeerConnection kc, boolean startConnection) {
		peers.put(ka, kc);
		getLogger()
				.info(() -> "----- " + getKernelAddress() + " now connected with " + kc.getPeerKernelAddress() + "------");
		if (startConnection) {
			kc.activate();
		}
	}

	/**
	 * @param packet
	 */
	private void newPeerDetected(DatagramPacket packet) {
		getLogger().fine(() -> "Contacting peer: " + packet.getAddress() + " port = " + packet.getPort()
				+ "\t-> opening PeerConnection");
		PeerConnection kc;
		try {
			kc = new PeerConnection(this, packet.getAddress(), packet.getPort());
			getLogger().finer(() -> "KC created " + kc);
		} catch (IOException e) {
			getLogger().warning(() -> "Unable to contact peer: " + packet.getAddress() + " port = " + packet.getPort()
					+ " because " + e.getMessage());
			return;
		}
		KernelAddress dka = waitConnectionInfo(kc);
		if (dka != null) {
			getLogger().finer(() -> "Now replying to " + dka);
			if (sendConnectionInfo(kc)) {
				addConnection(dka, kc, isAlive());
			}
		}
	}

	@SuppressWarnings("unused") // used by reflection
	private void connectToIp(InetAddress ipAddress) throws IOException {
		if (!ipAddress.equals(myServer.getIp())) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeLong(System.nanoTime());
			dos.close();
			newPeerDetected(new DatagramPacket(bos.toByteArray(), 8, ipAddress, 4444));
		}
	}

	/**
	 * @param kc
	 * @return
	 */
	private KernelAddress waitConnectionInfo(PeerConnection kc) {
		getLogger().finest(() -> "Waiting distant kernel address...");
		KernelAddress dka = null;
		try {
			dka = kc.waitForDistantKernelAddress();
			getLogger().finest("... Distant Kernel Address is " + dka + "Waiting distant organization info...");
			OrganizationSnapshot distantOrg = kc.waitForDistantOrg();
			getOrganization().importDistantOrg(cleanUp(distantOrg, dka));
			return dka;
		} catch (IOException | ClassNotFoundException e) {
			if (dka == null) {
				getLogger().log(Level.SEVERE, "I give up: Unable to get distant kernel address info on " + kc, e);
			} else {
				getLogger().log(Level.SEVERE, "I give up: Unable to get distant organization from " + dka, e);
			}
		}
		return null;
	}

	private boolean sendConnectionInfo(PeerConnection kc) {
		getLogger().fine(() -> "Sending info to "
				+ (kc.getPeerKernelAddress() == null ? kc.getPeerSocket() : kc.getPeerKernelAddress()));
		getLogger().finer(() -> "Local org is " + getOrganization().getOrganizationSnapShot(false) + "\n");
		try {
			kc.getOutputStream().writeObject(getKernelAddress());
			kc.getOutputStream().writeObject(getOrganization().getOrganizationSnapShot(false));
		} catch (IOException e) {
			getLogger().warning(() -> "I give up: Unable to send connection info to "
					+ (kc.getPeerKernelAddress() == null ? kc.getPeerSocket() : kc.getPeerKernelAddress()) + " because "
					+ e.getMessage());
			return false;
		}
		return true;
	}

	private OrganizationSnapshot cleanUp(OrganizationSnapshot organizationSnapshot, KernelAddress from) {
		for (Iterator<Entry<String, Map<String, Map<String, Set<AgentAddress>>>>> iterator = organizationSnapshot
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Map<String, Map<String, Set<AgentAddress>>>> org = iterator.next();
			for (Iterator<Entry<String, Map<String, Set<AgentAddress>>>> iterator2 = org.getValue().entrySet()
					.iterator(); iterator2.hasNext();) {
				Entry<String, Map<String, Set<AgentAddress>>> group = iterator2.next();
				for (Iterator<Entry<String, Set<AgentAddress>>> iterator3 = group.getValue().entrySet()
						.iterator(); iterator3.hasNext();) {
					Entry<String, Set<AgentAddress>> role = iterator3.next();
					for (Iterator<AgentAddress> iterator4 = role.getValue().iterator(); iterator4.hasNext();) {
						KernelAddress dka = iterator4.next().getKernelAddress();
						if (!from.equals(dka) && !peers.containsKey(dka)) {
							iterator4.remove();
						}
					}
					if (role.getValue().isEmpty()) {
						iterator3.remove();
					}
				}
				if (group.getValue().isEmpty()) {
					iterator2.remove();
				}
			}
			if (org.getValue().isEmpty()) {
				iterator.remove();
			}
		}
		return organizationSnapshot;
	}

	/**
	 * @param message
	 */
	private void broadcastUpdate(Message message) {
		getLogger().finer(() -> "Local CGR update\nBroadcasting " + " to " + peers.values() + message);
		// getLogger().finest("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		for (PeerConnection kc : peers.values()) {
			kc.sendMessage(message);
		}
	}

	private void sendDistantMessage(ObjectMessage<Message> m) {
		getLogger().finer(() -> "sending to " + m.getContent().getReceiver().getKernelAddress() + m);
		PeerConnection kc = peers.get(m.getContent().getReceiver().getKernelAddress());
		if (kc != null) {
			kc.sendMessage(m);
		}
	}

	@Override
	public String getName() {
		return super.getName() + getKernelAddress();// TODO precompute
	}

	@Override
	public String toString() {
		return super.toString() + " peers=" + peers.keySet();
	}

}