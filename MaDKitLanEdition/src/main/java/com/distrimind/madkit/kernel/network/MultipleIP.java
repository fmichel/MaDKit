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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
public class MultipleIP extends AbstractIP {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8772565720786798887L;

	private ArrayList<Inet4Address> inet4Adresses;
	private ArrayList<Inet6Address> inet6Adresses;
	private transient Random random;

	protected MultipleIP() {
		super(-1);
		random = new Random(System.currentTimeMillis());
		this.inet4Adresses = new ArrayList<>();
		this.inet6Adresses = new ArrayList<>();
	}
	@Override
	public int getInternalSerializedSize() {
		int res=super.getInternalSerializedSize()+8;
		for (InetAddress ia : inet4Adresses)
			res+=SerializationTools.getInternalSize(ia, 0);
		for (InetAddress ia : inet6Adresses)
			res+=SerializationTools.getInternalSize(ia, 0);
		return res;
	}
	
	@Override
	public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		super.readAndCheckObject(in);
		random = new Random(System.currentTimeMillis());
		int globalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		int totalSize=4;
		int size=in.readInt();
		if (size<0 || totalSize+size*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		this.inet4Adresses = new ArrayList<>(size);
		for (int i=0;i<size;i++)
		{
			InetAddress o=SerializationTools.readInetAddress(in, false);
			if (!(o instanceof Inet4Address))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);	
			Inet4Address ia=(Inet4Address)o;
			totalSize+=SerializationTools.getInternalSize(ia,0);
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			inet4Adresses.add(ia);
		}
		size=in.readInt();
		totalSize+=4;
		if (size<0 || totalSize+size*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		this.inet6Adresses = new ArrayList<>(size);
		if (inet4Adresses.isEmpty() && inet6Adresses.isEmpty())
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		for (int i=0;i<size;i++)
		{
			InetAddress o=SerializationTools.readInetAddress(in, false);
			if (!(o instanceof Inet6Address))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);	
			Inet6Address ia=(Inet6Address)o;
			totalSize+=SerializationTools.getInternalSize(ia,0);
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			inet6Adresses.add(ia);
		}
	}

	@Override
	public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
		
		super.writeAndCheckObject(oos);
		if (inet4Adresses.isEmpty() && inet6Adresses.isEmpty())
			throw new IOException();
		oos.writeInt(inet4Adresses.size());
		for (InetAddress ia : inet4Adresses)
			SerializationTools.writeInetAddress(oos, ia, false);
		oos.writeInt(inet6Adresses.size());
		for (InetAddress ia : inet6Adresses)
			SerializationTools.writeInetAddress(oos, ia, false);
		
	}
	

	public MultipleIP(int port, Collection<Inet4Address> inet4Adresses, Collection<Inet6Address> inet6Adresses) {
		super(port);
		random = new Random(System.currentTimeMillis());
		this.inet4Adresses = new ArrayList<>();
		this.inet6Adresses = new ArrayList<>();
		for (Inet4Address ia : inet4Adresses) {
			if (ia != null)
				this.inet4Adresses.add(ia);
		}
		for (Inet6Address ia : inet6Adresses) {
			if (ia != null)
				this.inet6Adresses.add(ia);
		}
	}

	public MultipleIP(int port, Collection<?> inetAdresses) {
		super(port);
		random = new Random(System.currentTimeMillis());
		this.inet4Adresses = new ArrayList<>();
		this.inet6Adresses = new ArrayList<>();
		for (Object ia : inetAdresses) {
			if (ia != null) {
				if (ia instanceof Inet4Address)
					this.inet4Adresses.add((Inet4Address) ia);
				else if (ia instanceof Inet6Address)
					this.inet6Adresses.add((Inet6Address) ia);
				else if (ia instanceof DoubleIP) {
					DoubleIP di = (DoubleIP) ia;
					if (di.getInet4Address() != null)
						this.inet4Adresses.add(di.getInet4Address());
					if (di.getInet6Address() != null)
						this.inet6Adresses.add(di.getInet6Address());
				}
			}
		}
	}

	public MultipleIP(int port, InetAddress... inetAdresses) {
		super(port);
		random = new Random(System.currentTimeMillis());
		this.inet4Adresses = new ArrayList<>();
		this.inet6Adresses = new ArrayList<>();
		for (InetAddress ia : inetAdresses) {
			if (ia != null) {
				if (ia instanceof Inet4Address)
					this.inet4Adresses.add((Inet4Address) ia);
				else if (ia instanceof Inet6Address)
					this.inet6Adresses.add((Inet6Address) ia);
			}
		}
	}

	public MultipleIP(int port, DoubleIP... doubleIPS) {
		super(port);
		random = new Random(System.currentTimeMillis());
		this.inet4Adresses = new ArrayList<>();
		this.inet6Adresses = new ArrayList<>();
		for (DoubleIP di : doubleIPS) {
			if (di != null) {
				if (di.getInet4Address() != null)
					this.inet4Adresses.add(di.getInet4Address());
				if (di.getInet6Address() != null)
					this.inet6Adresses.add(di.getInet6Address());
			}
		}
	}

	@Override
	public Inet6Address getInet6Address() {
		synchronized (random) {
			if (inet6Adresses.isEmpty())
				return null;
			return inet6Adresses.get(random.nextInt(inet6Adresses.size()));
		}
	}

	@Override
	public Inet4Address getInet4Address() {
		synchronized (random) {
			if (inet4Adresses.isEmpty())
				return null;
			return inet4Adresses.get(random.nextInt(inet4Adresses.size()));
		}
	}

	@Override
	public InetAddress[] getInetAddresses() {
		InetAddress[] res = new InetAddress[inet4Adresses.size() + inet6Adresses.size()];
		int index = 0;
		for (Inet4Address ia : inet4Adresses)
			res[index++] = ia;
		for (Inet6Address ia : inet6Adresses)
			res[index++] = ia;
		return res;
	}

	@Override
	public Inet6Address[] getInet6Addresses() {
		Inet6Address res[] = new Inet6Address[inet6Adresses.size()];
		int index = 0;
		for (Inet6Address ia : inet6Adresses)
			res[index++] = ia;
		return res;
	}

	@Override
	public Inet4Address[] getInet4Addresses() {
		Inet4Address res[] = new Inet4Address[inet4Adresses.size()];
		int index = 0;
		for (Inet4Address ia : inet4Adresses)
			res[index++] = ia;
		return res;
	}


	@Override
	public boolean excludedFromEncryption() {
		return false;
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}
}
