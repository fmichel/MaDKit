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

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class DoubleIP extends AbstractIP {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6293701888066104446L;

	private Inet4Address inet4Address;
	private Inet6Address inet6Address;

	protected DoubleIP() {
		super(-1);
		this.inet4Address = null;
		this.inet6Address = null;
	}

	public DoubleIP(int port, Inet4Address inet4Address, Inet6Address inet6Address) {
		super(port);
		if (inet4Address == null)
			throw new NullPointerException("inet4Address");
		if (inet6Address == null)
			throw new NullPointerException("inet6Address");
		this.inet4Address = inet4Address;
		this.inet6Address = inet6Address;
	}

	public DoubleIP(int port, Inet4Address inet4Address) {
		super(port);
		if (inet4Address == null)
			throw new NullPointerException("inet4Address");
		this.inet4Address = inet4Address;
		this.inet6Address = null;
	}

	public DoubleIP(int port, Inet6Address inet6Address) {
		super(port);
		if (inet6Address == null)
			throw new NullPointerException("inet6Address");
		this.inet4Address = null;
		this.inet6Address = inet6Address;
	}

	public DoubleIP(int port, InetAddress inetAddress) {
		super(port);
		if (inetAddress == null)
			throw new NullPointerException("inetAddress");
		if (inetAddress instanceof Inet4Address) {
			this.inet4Address = (Inet4Address) inetAddress;
			this.inet6Address = null;
		} else if (inetAddress instanceof Inet6Address) {
			this.inet4Address = null;
			this.inet6Address = (Inet6Address) inetAddress;
		} else
			throw new IllegalArgumentException("inetAddress must be an ipv4 or an ivp6 address");
	}

	public DoubleIP(InetSocketAddress inetAddress) {
		this(inetAddress.getPort(), inetAddress.getAddress());
	}

	@Override
	public Inet6Address getInet6Address() {
		return inet6Address;
	}

	@Override
	public Inet4Address getInet4Address() {
		return inet4Address;
	}

	@Override
	public InetAddress[] getInetAddresses() {
		int size = 0;
		if (inet4Address != null)
			size++;
		if (inet6Address != null)
			size++;
		InetAddress[] res = new InetAddress[size];
		size = 0;
		if (inet4Address != null)
			res[size++] = inet4Address;
		if (inet6Address != null)
			res[size++] = inet6Address;
		return res;
	}

	@Override
	public Inet6Address[] getInet6Addresses() {
		if (inet6Address != null) {
			Inet6Address[] res = new Inet6Address[1];
			res[0] = inet6Address;
			return res;
		} else
			return new Inet6Address[0];
	}

	@Override
	public Inet4Address[] getInet4Addresses() {
		if (inet4Address != null) {
			Inet4Address[] res = new Inet4Address[1];
			res[0] = inet4Address;
			return res;
		} else
			return new Inet4Address[0];
	}

	@Override
	public Integrity checkDataIntegrity() {
		if (getPort() < 0)
			return Integrity.FAIL;
		if (inet4Address == null && inet6Address == null)
			return Integrity.FAIL;
		return Integrity.OK;
	}
}
