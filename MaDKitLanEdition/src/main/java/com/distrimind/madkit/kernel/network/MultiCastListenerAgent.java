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

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.logging.Level;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.Message;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 1.0
 *
 */
final class MultiCastListenerAgent extends AgentFakeThread {

	private final NetworkInterface networkInterface;
	private final InetAddress networkInterfaceAddress;
	private InetAddress groupIPAddress;
	private long localOnlineTime;
	private NetworkBlackboard networkBlackboard;
	static final long durationBeforeRemovingMulticastMessages = 2000;

	MultiCastListenerAgent(NetworkInterface networkInterface, InetAddress networkInterfaceAddress) {
		this.networkInterface = networkInterface;
		this.networkInterfaceAddress = networkInterfaceAddress;
		this.groupIPAddress = null;
	}

	private static InetAddress getDefaultGroupIpAddress(InetAddress networkInterfaceAddress) {
		if (networkInterfaceAddress == null)
			throw new NullPointerException();
		if (networkInterfaceAddress instanceof Inet4Address) {
			return NetworkProperties.defaultIPV4MulticastGroupForPeerDiscovery;
		} else if (networkInterfaceAddress instanceof Inet6Address) {
			return NetworkProperties.defaultIPV6MulticastGroupForPeerDiscovery;
		} else
			return null;
	}

	@Override
	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.networkLogLevel);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching multicast network listener on network interface address " + networkInterfaceAddress
					+ "...");

		this.requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.MULTICAST_LISTENER_ROLE);
		networkBlackboard = (NetworkBlackboard) getBlackboard(LocalCommunity.Groups.NETWORK,
				LocalCommunity.BlackBoards.NETWORK_BLACKBOARD);

		if (networkInterfaceAddress instanceof Inet4Address) {
			groupIPAddress = getMadkitConfig().networkProperties.IPV4MulticastGroupForPeerDiscovery;
		} else if (networkInterfaceAddress instanceof Inet6Address) {
			groupIPAddress = getMadkitConfig().networkProperties.IPV6MulticastGroupForPeerDiscovery;
		}
		if (groupIPAddress == null)
			groupIPAddress = getDefaultGroupIpAddress(networkInterfaceAddress);
		localOnlineTime = System.currentTimeMillis();

		sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NIO_ROLE,
				new MulticastListenerConnectionMessage(networkInterface, networkInterfaceAddress,
						getMadkitConfig().networkProperties.portForMulticastDiffusionMessage, groupIPAddress),
				LocalCommunity.Roles.MULTICAST_LISTENER_ROLE);
		try {
			sendMessageWithRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.NIO_ROLE,
					new DatagramLocalNetworkPresenceMessage(localOnlineTime, getMadkitConfig().projectVersion,
							getMadkitConfig().madkitVersion, this.networkInterfaceAddress, getKernelAddress()),
					LocalCommunity.Roles.MULTICAST_LISTENER_ROLE);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException e) {
			if (logger != null)
				logger.severeLog("Impossible to digest kernel address", e);
		}
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Multicast network listener on network interface address " + networkInterfaceAddress
					+ " LAUNCHED !");
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message instanceof DatagramLocalNetworkPresenceMessage) {
			DatagramLocalNetworkPresenceMessage m = (DatagramLocalNetworkPresenceMessage) _message;
			try {
				if (m.isCompatibleWith(localOnlineTime, getMadkitConfig().projectVersion,
						getMadkitConfig().madkitVersion, getKernelAddress())) {

					try {
						InetAddress ia = m.getConcernedInetAddress();
						NetworkInterface ni = null;
						try {
							ni = NetworkInterface.getByInetAddress(ia);
						} catch (Exception e) {
							ni = null;
						}
						if (ni != null) {
							if (!getMadkitConfig().networkProperties.needsServerSocket(new InetSocketAddress(
									getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections)))
								ni = null;
						}
						if (ni == null) {
							if (networkBlackboard.addMessage(this, m)) {
								if (logger != null && logger.isLoggable(Level.FINER))
									logger.finer("Compatible message received : " + _message);

								AskForConnectionMessage am = new AskForConnectionMessage(
										ConnectionStatusMessage.Type.CONNECT,
										new DoubleIP(
												getMadkitConfig().networkProperties.portsToBindForAutomaticLocalConnections,
												ia));
								sendMessageWithRole(LocalCommunity.Groups.NETWORK,
										LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE, am,
										LocalCommunity.Roles.MULTICAST_LISTENER_ROLE);
							}
						} else if (logger != null && logger.isLoggable(Level.FINER))
							logger.finer("message received corrupted : " + _message);

					} catch (UnknownHostException e) {
						if (logger != null)
							logger.severeLog("Impossible to read ip address", e);
					}
				} else if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Incompatible message received : " + _message);
			} catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException e) {
				if (logger != null)
					logger.severeLog("Impossible to digest kernel address", e);
			}
			/*
			 * catch(IllegalAccessException e) { if (logger!=null)
			 * logger.severeLog("Impossible to ask connection", e); }
			 */
		} else if (_message instanceof MulticastListenerDeconnectionMessage) {
			this.killAgent(this);
		}

	}

	@Override
	public void end() {
		if (logger != null)
			logger.fine(
					"Multicast network listener on network interface address " + networkInterfaceAddress + " KILLED !");
	}

}
