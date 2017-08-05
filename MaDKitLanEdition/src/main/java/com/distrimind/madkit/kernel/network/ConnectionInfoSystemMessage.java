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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.distrimind.madkit.kernel.network.LocalNetworkAgent.PossibleAddressForDirectConnnection;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 *
 */
class ConnectionInfoSystemMessage implements SystemMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -215103842431063068L;

	private ArrayList<AbstractIP> addresses;
	private AbstractIP localAddresses;
	private int manualPortToConnect;
	private int localPortToConnect;
	private boolean canBeDirectServer;

	ConnectionInfoSystemMessage(List<PossibleAddressForDirectConnnection> inet_socket_addresses,
			InetAddress local_interface_address, int manualPortToConnect, int localPortToConnect,
			boolean canBeDirectServer, AbstractIP localAddresses) {
		this.addresses = new ArrayList<>(inet_socket_addresses.size());
		for (PossibleAddressForDirectConnnection r : inet_socket_addresses) {
			if (r.isConcernedBy(local_interface_address)) {
				if (r.getIP().getPort() < 0)
					throw new IllegalArgumentException();
				if (r.getIP().getInetAddress() == null)
					throw new IllegalArgumentException();
				this.addresses.add(r.getIP());
			}
		}

		this.manualPortToConnect = manualPortToConnect;
		if (localPortToConnect < 0)
			throw new IllegalArgumentException("localPortToConnect must be greater or equal than 0 !");
		this.localPortToConnect = localPortToConnect;
		this.canBeDirectServer = canBeDirectServer;
		this.localAddresses = localAddresses;
	}

	int getManualPortToConnect() {
		return manualPortToConnect;
	}

	boolean hasManualPortToConnect() {
		return manualPortToConnect >= 0;
	}

	int getLocalPortToConnect() {
		return localPortToConnect;
	}

	int getPortToConnect() {
		if (hasManualPortToConnect())
			return manualPortToConnect;
		else
			return localPortToConnect;
	}

	@Override
	public String toString() {
		return "ConnectionInfo[manualPortToConnect=" + manualPortToConnect + ", localPortToConnect="
				+ localPortToConnect + ", inetAddresses=" + addresses + "]";
	}

	InetSocketAddress getInetSocketAddress(InetAddress connectFrom, InetAddress perceivedDistantInetAddress) {
		if (perceivedDistantInetAddress == null) {
			return null;
		}

		if (perceivedDistantInetAddress instanceof Inet4Address)
			return getInetSocketAddress(connectFrom instanceof Inet6Address, (Inet4Address) perceivedDistantInetAddress,
					isLocalAddress(connectFrom) && isLocalAddress(perceivedDistantInetAddress));
		else if (perceivedDistantInetAddress instanceof Inet6Address)
			return getInetSocketAddress(connectFrom instanceof Inet6Address, (Inet6Address) perceivedDistantInetAddress,
					isLocalAddress(connectFrom) && isLocalAddress(perceivedDistantInetAddress));
		else {
			return null;
		}
	}

	private InetSocketAddress getInetSocketAddress(boolean connectionFromIPV6, Inet6Address perceivedDistantInetAddress,
			boolean isLocalToLocal) {
		if (isLocalToLocal && connectionFromIPV6 && this.canBeDirectServer)
			return new InetSocketAddress(perceivedDistantInetAddress, getLocalPortToConnect());

		InetSocketAddress isa = getInetSocketAddress(connectionFromIPV6, isLocalToLocal);
		if (isa == null) {
			if (connectionFromIPV6 && this.canBeDirectServer
					&& (hasManualPortToConnect() && isInternetAddress(perceivedDistantInetAddress)))
				return new InetSocketAddress(perceivedDistantInetAddress, getPortToConnect());
			else
				return null;
		} else
			return isa;
	}

	public boolean canBeDirectServer() {
		return canBeDirectServer;
	}

	private InetSocketAddress getInetSocketAddress(boolean connectionFromIPV6, Inet4Address perceivedDistantInetAddress,
			boolean isLocalToLocal) {
		if (isLocalToLocal && !connectionFromIPV6 && this.canBeDirectServer)
			return new InetSocketAddress(perceivedDistantInetAddress, getLocalPortToConnect());
		InetSocketAddress isa = getInetSocketAddress(connectionFromIPV6, isLocalToLocal);
		if (isa == null) {
			if (!connectionFromIPV6 && this.canBeDirectServer
					&& (hasManualPortToConnect() && isInternetAddress(perceivedDistantInetAddress)))
				return new InetSocketAddress(perceivedDistantInetAddress, getPortToConnect());
			else
				return null;
		} else
			return isa;
	}

	static private boolean isInternetAddress(InetAddress perceivedDistantInetAddress) {
		return !perceivedDistantInetAddress.isAnyLocalAddress() && !perceivedDistantInetAddress.isLinkLocalAddress()
				&& !perceivedDistantInetAddress.isLoopbackAddress() && !perceivedDistantInetAddress.isMulticastAddress()
				&& !perceivedDistantInetAddress.isSiteLocalAddress();
	}

	static private boolean isLocalAddress(InetAddress perceivedDistantInetAddress) {
		return (perceivedDistantInetAddress.isAnyLocalAddress() && !perceivedDistantInetAddress.isLinkLocalAddress()
				&& !perceivedDistantInetAddress.isMulticastAddress()
				&& !perceivedDistantInetAddress.isSiteLocalAddress())
				|| perceivedDistantInetAddress.isLoopbackAddress();
	}

	private InetSocketAddress getInetSocketAddress(boolean connectionFromIPV6, boolean isLocalToLocal) {
		if (connectionFromIPV6) {
			if (isLocalToLocal && localAddresses != null) {
				Inet6Address ia = localAddresses.getInet6Address();

				if (ia != null)
					return new InetSocketAddress(ia, localAddresses.getPort());
			} else {
				for (AbstractIP ip : addresses) {
					InetAddress ia = ip.getInet6Address();
					if (ia != null)
						return new InetSocketAddress(ia, ip.getPort());
				}
			}
		}
		for (AbstractIP ip : addresses) {
			if (isLocalToLocal && localAddresses != null) {
				Inet4Address ia = localAddresses.getInet4Address();
				if (ia != null)
					return new InetSocketAddress(ia, localAddresses.getPort());
			} else {
				InetAddress ia = ip.getInet4Address();
				if (ia != null)
					return new InetSocketAddress(ia, ip.getPort());
			}
		}
		return null;
	}

	@Override
	public Integrity checkDataIntegrity() {

		if (addresses == null)
			return Integrity.FAIL;
		for (AbstractIP ip : addresses) {
			Integrity i = ip.checkDataIntegrity();
			if (i != Integrity.OK)
				return i;
		}
		if (localPortToConnect < 0)
			return Integrity.FAIL;

		if (localAddresses != null)
			localAddresses.checkDataIntegrity();

		return Integrity.OK;
	}

}
