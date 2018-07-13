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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.madkit.util.MultiFormatPropertiesObjectParser;
import com.distrimind.util.properties.MultiFormatProperties;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public abstract class AbstractIP extends MultiFormatProperties implements SystemMessage, ExternalizableAndSizable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5670994991069850019L;

	private int port;

	public abstract Inet6Address getInet6Address();

	public abstract Inet4Address getInet4Address();

	public abstract InetAddress[] getInetAddresses();

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		port=in.readInt();
		if (port<0)
			throw new MessageSerializationException(Integrity.FAIL);
	}

	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		oos.writeInt(port);
	}
	@Override
	public int getInternalSerializedSize() {
		return 4;
	}
	
	public InetAddressFilter[] getInetAddressFilters() {
		InetAddress[] ias = getInetAddresses();
		InetAddressFilter[] res = new InetAddressFilter[ias.length];
		for (int i = 0; i < ias.length; i++) {
			res[i] = new InetAddressFilter(ias[i], (short) 0, port);
		}
		return res;
	}

	public abstract Inet6Address[] getInet6Addresses();

	public InetAddressFilter[] getInet6AddressFilters() {
		InetAddress[] ias = getInet6Addresses();
		InetAddressFilter[] res = new InetAddressFilter[ias.length];
		for (int i = 0; i < ias.length; i++) {
			res[i] = new InetAddressFilter(ias[i], (short) 0, port);
		}
		return res;
	}

	public abstract Inet4Address[] getInet4Addresses();

	public InetAddressFilter[] getInet4AddressFilters() {
		InetAddress[] ias = getInet4Addresses();
		InetAddressFilter[] res = new InetAddressFilter[ias.length];
		for (int i = 0; i < ias.length; i++) {
			res[i] = new InetAddressFilter(ias[i], (short) 0, port);
		}
		return res;
	}

	protected AbstractIP(int port) {
		super(new MultiFormatPropertiesObjectParser());
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public InetAddress getInetAddress() {
		InetAddress res = getInet6Address();
		if (res == null)
			return getInet4Address();
		else
			return res;

	}

	@Override
	public String toString() {
		InetAddress ia = getInetAddress();
		if (ia == null)
			return "null:" + port;
		else
			return ia.toString() + ":" + port;
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public AbstractIP clone() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof AbstractIP) {
			if (port != ((AbstractIP) o).port)
				return false;
			InetAddress[] ias = getInetAddresses();
			InetAddress[] oias = ((AbstractIP) o).getInetAddresses();
			if (ias == oias)
				return true;
			if (ias == null || oias == null)
				return false;
			if (ias.length != oias.length)
				return false;
			for (InetAddress ia1 : ias) {
				boolean found = false;
				for (InetAddress ia2 : oias) {
					if (ia1.equals(ia2)) {
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
			return true;
		} else
			return false;
	}

}
