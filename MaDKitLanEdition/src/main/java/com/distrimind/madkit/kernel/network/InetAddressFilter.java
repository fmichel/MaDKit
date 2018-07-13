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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import com.distrimind.util.properties.MultiFormatProperties;

/**
 * This class represent a network through an IP and a mask;
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class InetAddressFilter extends MultiFormatProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1812478492531750238L;

	/**
	 * The sub network IP. If set to null, then the filter wrap all IP addresses.
	 */
	private InetAddress ip = null;

	/**
	 * The network prefix length which enables to determine the subnetwork mask
	 */
	private short networkPrefixLength = 0;

	/**
	 * Define the concerned port. A negative value means that all ports are
	 * concerned.
	 */
	private int concernedPort = -1;

	protected InetAddressFilter() {
		super(null);
	}

	public InetAddressFilter(InetAddress ip, short networkPrefixLength, int concernedPort) {
		super(null);
		this.ip = ip;
		this.networkPrefixLength = networkPrefixLength;
		this.concernedPort = concernedPort;
	}

	public InetAddress getIp() {
		return ip;
	}

	public short getNetworkPrefixLength() {
		return networkPrefixLength;
	}

	public int getConcernedPort() {
		return concernedPort;
	}

	@Override
	public String toString() {
		return ip.toString() + "/" + networkPrefixLength + ":" + concernedPort;
	}

	public static InetAddressFilter parse(String _string) throws IllegalArgumentException {
		String split[] = _string.split("[/:]");
		if (split.length == 3) {
			try {
				InetAddressFilter res = new InetAddressFilter();
				res.ip = InetAddress.getByName(split[0]);
				res.networkPrefixLength = Short.parseShort(split[1]);
				res.concernedPort = Integer.parseInt(split[2]);
				return res;
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid string format : " + _string, e);
			}
		} else
			throw new IllegalArgumentException("Invalid string format : " + _string);
	}

	public boolean isConcernedBy(InetAddress _distant_inet_address, int _local_port) {
		if (_distant_inet_address == null)
			return false;
		return (ip == null || ip.isAnyLocalAddress()
				|| isSameLocalNetwork(ip.getAddress(), _distant_inet_address.getAddress(), networkPrefixLength))
				&& (concernedPort < 0 || concernedPort == _local_port);
	}

	public static boolean isSameLocalNetwork(byte addr1[], byte addr2[], short network_prefix_length) {
		int length = network_prefix_length / 8;
		for (int i = 0; i < length; i++) {
			if (addr1[i] != addr2[i])
				return false;
		}

		int mod = network_prefix_length % 8;
		if (mod != 0) {
			int b1 = ((int) addr1[length]) & 0xff;
			int b2 = ((int) addr2[length]) & 0xff;
			int filter = (1 << (8 - mod)) - 1;
			return (b1 | filter) == (b2 | filter);
		} else
			return true;

	}

	public static boolean isSameLocalNetwork(InetAddress ia1, InetAddress ia2) throws SocketException {
		if (ia1.equals(ia2))
			return true;
		NetworkInterface ni1 = NetworkInterface.getByInetAddress(ia1);
		NetworkInterface ni2 = NetworkInterface.getByInetAddress(ia2);

		if (ni1.equals(ni2)) {
			InterfaceAddress interA1 = null;
			InterfaceAddress interA2 = null;
			for (InterfaceAddress interA : ni1.getInterfaceAddresses()) {
				if (interA.getAddress().equals(ia1)) {
					interA1 = interA;
					break;
				}
			}
			for (InterfaceAddress interA : ni2.getInterfaceAddresses()) {
				if (interA.getAddress().equals(ia2)) {
					interA2 = interA;
					break;
				}
			}
			if (interA1 != null && InetAddressFilter.isSameLocalNetwork(ia1.getAddress(), ia2.getAddress(),
					interA1.getNetworkPrefixLength())) {
				return true;
			}

			return interA2 != null && InetAddressFilter.isSameLocalNetwork(ia1.getAddress(), ia2.getAddress(),
					interA2.getNetworkPrefixLength());
		}
		return false;

	}

}
