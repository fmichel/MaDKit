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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
public class HostIP extends AbstractIP {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5630622556544306131L;

	private String host;
	private final transient Random random = new Random(System.currentTimeMillis());

	protected HostIP() {
		super(-1);
		host = null;
	}

	public HostIP(int port, URL url) throws URISyntaxException {
		super(port);
		if (url == null)
			throw new NullPointerException("url");
		this.host = url.toURI().getHost();

	}

	public HostIP(int port, URI uri) {
		super(port);
		if (uri == null)
			throw new NullPointerException("uri");
		this.host = uri.getHost();
	}

	public HostIP(int port, String host) {
		super(port);
		if (host == null)
			throw new NullPointerException("host");
		this.host = host;
	}

	@Override
	public int hashCode() {
		return host.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return this.toString().equals(o.toString());
	}

	@Override
	public Inet6Address getInet6Address() {
		try {
			InetAddress ias[] = InetAddress.getAllByName(host);
			ArrayList<Inet6Address> res = new ArrayList<>(ias == null ? 0 : ias.length);
			for (InetAddress ia : ias) {
				if (ia instanceof Inet6Address)
					res.add((Inet6Address) ia);
			}
			if (res.isEmpty())
				return null;
			else {
				synchronized (random) {
					return res.get(random.nextInt(res.size()));
				}
			}
		} catch (UnknownHostException e) {
		}
		return null;
	}

	@Override
	public Inet4Address getInet4Address() {
		try {
			InetAddress ias[] = InetAddress.getAllByName(host);
			ArrayList<Inet4Address> res = new ArrayList<>(ias == null ? 0 : ias.length);
			for (InetAddress ia : ias) {
				if (ia instanceof Inet4Address)
					res.add((Inet4Address) ia);
			}
			if (res.isEmpty())
				return null;
			else {
				synchronized (random) {
					return res.get(random.nextInt(res.size()));
				}
			}
		} catch (UnknownHostException e) {
		}
		return null;
	}

	@Override
	public InetAddress[] getInetAddresses() {
		try {
			return InetAddress.getAllByName(host);
		} catch (UnknownHostException e) {
			return new InetAddress[0];
		}

	}

	@Override
	public Inet6Address[] getInet6Addresses() {
		try {

			InetAddress ias[] = InetAddress.getAllByName(host);
			int size = 0;
			for (InetAddress ia : ias)
				if (ia instanceof Inet6Address)
					++size;

			Inet6Address res[] = new Inet6Address[size];
			size = 0;
			for (InetAddress ia : ias)
				if (ia instanceof Inet6Address)
					res[size++] = (Inet6Address) ia;
			return res;
		} catch (UnknownHostException e) {
			return new Inet6Address[0];
		}
	}

	@Override
	public Inet4Address[] getInet4Addresses() {
		try {

			InetAddress ias[] = InetAddress.getAllByName(host);
			int size = 0;
			for (InetAddress ia : ias)
				if (ia instanceof Inet4Address)
					++size;

			Inet4Address res[] = new Inet4Address[size];
			size = 0;
			for (InetAddress ia : ias)
				if (ia instanceof Inet4Address)
					res[size++] = (Inet4Address) ia;
			return res;
		} catch (UnknownHostException e) {
			return new Inet4Address[0];
		}
	}

	@Override
	public Integrity checkDataIntegrity() {
		if (getPort() < 0)
			return Integrity.FAIL;
		if (getInetAddress() == null)
			return Integrity.FAIL;
		return Integrity.OK;
	}

	@Override
	public boolean excludedFromEncryption() {
		return false;
	}

}
