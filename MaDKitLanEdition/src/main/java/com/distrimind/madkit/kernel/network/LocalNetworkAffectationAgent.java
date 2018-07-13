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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.NetworkAgent;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.NetworkInterfaceInformationMessage;
import com.distrimind.madkit.kernel.network.BindInetSocketAddressMessage.Type;
import com.distrimind.madkit.kernel.network.UpnpIGDAgent.AskForNetworkInterfacesMessage;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
@SuppressWarnings("unused")
class LocalNetworkAffectationAgent extends AgentFakeThread {

	// private ArrayList<LocalNetworkAgent> local_networks_agents=new ArrayList<>();
	// private ArrayList<NetworkInterfaceAgent> local_network_interfaces_agents=new
	// ArrayList<>();
	private ArrayList<LocalNetworkAgent> local_network_agents = new ArrayList<>();

	private ArrayList<InetSocketAddress> port_binds = new ArrayList<>();
	private ArrayList<AskForConnectionMessage> askedConnections = new ArrayList<>();
	private boolean networkStoped = false;

	@SuppressWarnings("unused")
	LocalNetworkAffectationAgent() {

	}

	@Override
	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching LocalNetworkAffectationAgent ...");
		this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		this.requestRole(LocalCommunity.Groups.LOCAL_NETWORKS, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		this.requestRole(LocalCommunity.Groups.NETWORK_INTERFACES, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		// his.requestRole(LocalCommunity.Groups.LOCAL_NETWORKS,
		// LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);

		if (getMadkitConfig().networkProperties.networkInterfaceScan)
			this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
					new AskForNetworkInterfacesMessage(
							getMadkitConfig().networkProperties.delayBetweenEachNetworkInterfaceScan),
					LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);

		if (getMadkitConfig().networkProperties.localInetAddressesToBind != null) {
			for (InetAddress isa : getMadkitConfig().networkProperties.localInetAddressesToBind) {
				receiveMessage(new BindInetSocketAddressMessage(Type.BIND, new InetSocketAddress(isa,
						getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections)));
			}
		} else
			receiveMessage(new BindInetSocketAddressMessage(Type.BIND, new InetSocketAddress(
					getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections)));

		if (getMadkitConfig().networkProperties.connectionToAttempt != null) {
			boolean first=true;
			long timeUTC=0;
			for (final AbstractIP isa : getMadkitConfig().networkProperties.connectionToAttempt) {
				if (isa != null)
				{
					if (first || getMadkitConfig().networkProperties.delayInMsBetweenEachConnectionAsk==0)
					{
						first=false;
						receiveMessage(new AskForConnectionMessage(ConnectionStatusMessage.Type.CONNECT, isa));
						timeUTC=System.currentTimeMillis();
					}
					else
					{
						System.out.println("ici1");
						timeUTC+=getMadkitConfig().networkProperties.delayInMsBetweenEachConnectionAsk;
						scheduleTask(new Task<>(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								System.out.println("ici2");
								if (isAlive())
									receiveMessage(new AskForConnectionMessage(ConnectionStatusMessage.Type.CONNECT, isa));
								return null;
							}
						}, timeUTC));
					}
				}
			}
		}

		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("LocalNetworkAffectationAgent LAUNCHED !");

	}

	@Override
	protected void end() {
		if (!networkStoped && getMadkitConfig().networkProperties.networkInterfaceScan) {
			Set<AgentAddress> aa = getAgentsWithRole(LocalCommunity.Groups.NETWORK,
					LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE);
			if (aa.size() > 0)
				this.sendMessageWithRole(aa.iterator().next(), new AskForNetworkInterfacesMessage(0),
						LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
		}
		if (logger != null)
			logger.fine("LocalNetworkAffectationAgent KILLED !");

	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message instanceof NetworkInterfaceInformationMessage) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Received network interface information : " + _message);

			NetworkInterfaceInformationMessage m = (NetworkInterfaceInformationMessage) _message;
			this.broadcastMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NIO_ROLE, m,
					LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);

			ArrayList<LocalNetworkAgent> lnas = LocalNetworkAgent.putAndRemoveNetworkInterfaces(
					this.local_network_agents, m.getNewConnectedInterfaces(), m.getNewDisconnectedInterfaces());
			boolean lnas_activated[] = new boolean[lnas.size()];
			for (int i = 0; i < lnas.size(); i++) {
				lnas_activated[i] = true;
			}

			for (int i = 0; i < lnas.size(); i++) {
				LocalNetworkAgent lna = lnas.get(i);
				if (lna.getState().equals(State.NOT_LAUNCHED)) {
					if (!launchAgent(lna).equals(ReturnCode.SUCCESS) && logger != null)
						logger.severe("Impossible to launch local network agent " + lna);

					AgentAddress aa = lna.getAgentAddressIn(LocalCommunity.Groups.LOCAL_NETWORKS,
							LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
					// update port binds
					for (InetSocketAddress isa : port_binds)
						sendMessageWithRole(aa, new BindInetSocketAddressMessage(Type.BIND, isa),
								LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
					// process memorized connections
					for (AskForConnectionMessage afcm : askedConnections) {
						sendMessageWithRole(aa, afcm.clone(), LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
					}
				}
				/*
				 * else local_network_agents.remove(lna);
				 */
				if (lnas_activated[i]) {
					for (int j = i + 1; j < lnas.size(); j++) {
						LocalNetworkAgent lna2 = lnas.get(j);
						try {
							if (lna.isSameAs(lna2))
								lnas_activated[i] = false;
						} catch (Exception e) {
							if (logger != null)
								logger.severeLog("Unexpected exception", e);
						}
					}
				}
			}
			for (int i = 0; i < lnas.size(); i++) {
				LocalNetworkAgent lna = lnas.get(i);
				AgentAddress aa = lna.getAgentAddressIn(LocalCommunity.Groups.LOCAL_NETWORKS,
						LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
				sendMessageWithRole(aa, new LocalNetworkAgent.ActivateAskConnection(lnas_activated[i]),
						LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
			}

			local_network_agents = lnas;
		} else if (_message instanceof BindInetSocketAddressMessage) {
			BindInetSocketAddressMessage bind = (BindInetSocketAddressMessage) _message;
			if (bind.getInetSocketAddress().getAddress() != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Received message : " + _message);

				if (bind.getType().equals(BindInetSocketAddressMessage.Type.BIND)) {
					if (!port_binds.contains(bind.getInetSocketAddress()))
						port_binds.add(bind.getInetSocketAddress());
				} else if (bind.getType().equals(BindInetSocketAddressMessage.Type.DISCONNECT)) {
					port_binds.remove(bind.getInetSocketAddress());
				}
				for (AgentAddress aa : getAgentsWithRole(LocalCommunity.Groups.LOCAL_NETWORKS,
						LocalCommunity.Roles.LOCAL_NETWORK_ROLE))
					sendMessage(aa, bind.clone());

			}
		} else if (_message instanceof NetworkAgent.StopNetworkMessage) {
			networkStoped = true;
			this.broadcastMessage(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_ROLE, _message,
					false);
			this.sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE,
					_message, LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE);
			this.killAgent(this);
		} else if (_message.getClass() == AskForConnectionMessage.class) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Receiving message : " + _message);

			addAskedConnection((AskForConnectionMessage) _message);
			for (AgentAddress aa : getAgentsWithRole(LocalCommunity.Groups.LOCAL_NETWORKS,
					LocalCommunity.Roles.LOCAL_NETWORK_ROLE)) {
				this.sendMessage(aa, _message.clone());
			}
		} else if (_message.getClass() == AskForTransferMessage.class) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Received message : " + _message);
			AskForTransferMessage m = (AskForTransferMessage) _message;
			if (m.getType() == AskForTransferMessage.Type.DISCONNECT) {
				for (AgentAddress aa : getAgentsWithRole(LocalCommunity.Groups.NETWORK,
						LocalCommunity.Roles.TRANSFER_AGENT_ROLE))
					sendMessage(aa, m.clone());
			} else {
				launchAgent(new TransferAgent((AskForTransferMessage) _message));
			}
		}

	}

	private void addAskedConnection(AskForConnectionMessage m) {

		for (Iterator<AskForConnectionMessage> it = askedConnections.iterator(); it.hasNext();) {
			AskForConnectionMessage afcm = it.next();
			if (afcm.getIP().equals(m.getIP())) {
				it.remove();
			}
		}
		if (m.getType().equals(ConnectionStatusMessage.Type.CONNECT))
			askedConnections.add(m);
		if (logger != null && logger.isLoggable(Level.FINER))
			logger.finer("Connection asked : " + m);

	}

}
