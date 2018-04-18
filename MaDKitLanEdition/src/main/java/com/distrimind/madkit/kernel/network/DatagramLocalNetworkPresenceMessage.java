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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.MessageDigestType;
import com.distrimind.util.sizeof.ObjectSizer;
import com.distrimind.util.version.Version;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * 
 */
class DatagramLocalNetworkPresenceMessage extends Message {

	final long onlineTime;
	final int programBuildNumber;
	final int madkitBuildNumber;

	final byte[] programName;
	final byte[] inetAddress;
	final byte[] kernelAddress;
	private final int hashCode;
	private static final int maxProgramNameLength = 100;
	private static final int maxKernelAddressLengthLength = 1024;

	@Override
	public String toString() {
		InetAddress ia = null;
		try {
			ia = InetAddress.getByAddress(inetAddress);
		} catch (Exception e) {

		}
		return "DatagramLocalNetworkPresenceMessage[onlineTime=" + onlineTime + ", programBuildNumber="
				+ programBuildNumber + ", madkitBuildNumber" + madkitBuildNumber + ", programName"
				+ new String(programName) + ", inetAddress=" + ia;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	int computeHashCode() {
		return Arrays.hashCode(inetAddress) + Arrays.hashCode(kernelAddress) + (int) onlineTime;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		else if (o == this)
			return true;
		else if (o instanceof DatagramLocalNetworkPresenceMessage) {
			DatagramLocalNetworkPresenceMessage d = (DatagramLocalNetworkPresenceMessage) o;
			return onlineTime == d.onlineTime && Arrays.equals(inetAddress, d.inetAddress)
					&& Arrays.equals(kernelAddress, d.kernelAddress);
		}
		return false;
	}

	DatagramLocalNetworkPresenceMessage(long onlineTime, Version programVersion, Version madkitVersion,
			InetAddress inetAddress, KernelAddress kernelAddress) throws NoSuchAlgorithmException, NoSuchProviderException {
		this.onlineTime = onlineTime;
		if (programVersion == null)
			this.programBuildNumber = 0;
		else
			this.programBuildNumber = programVersion.getBuildNumber();
		if (madkitVersion == null)
			throw new NullPointerException("madkitVersion");
		this.madkitBuildNumber = madkitVersion.getBuildNumber();
		if (programVersion == null)
			programName = getProgramNameInBytes(MadkitProperties.defaultProjectCodeName);
		else
			programName = getProgramNameInBytes(programVersion.getShortProgramName());

		this.inetAddress = inetAddress == null ? null : inetAddress.getAddress();
		if (this.inetAddress != null && this.inetAddress.length != 16 && this.inetAddress.length != 4)
			throw new IllegalArgumentException("inetAddress");

		this.kernelAddress = digestMessage(kernelAddress.getAbstractDecentralizedID().getBytes());
		this.hashCode = computeHashCode();
	}

	private static byte[] digestMessage(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException {
		AbstractMessageDigest mda = MessageDigestType.BC_FIPS_SHA3_256.getMessageDigestInstance();
		return mda.digest(bytes);
	}

	private DatagramLocalNetworkPresenceMessage(long onlineTime, int programBuildNumber, int madkitBuildNumber,
			byte[] programName, byte[] inetAddress, byte[] kernelAddress) {
		this.onlineTime = onlineTime;
		this.programBuildNumber = programBuildNumber;
		this.madkitBuildNumber = madkitBuildNumber;
		this.programName = programName;
		this.inetAddress = inetAddress;
		this.kernelAddress = kernelAddress;
		this.hashCode = computeHashCode();
	}

	void writeTo(OutputStream os) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(os)) {
			dos.writeLong(onlineTime);
			dos.writeInt(programBuildNumber);
			dos.writeInt(madkitBuildNumber);
			if (inetAddress == null) {
				dos.writeShort(0);
			} else {
				dos.writeShort(inetAddress.length);
				dos.write(inetAddress);
			}
			dos.writeInt(kernelAddress.length);
			dos.write(kernelAddress);
			dos.writeInt(programName.length);
			dos.write(programName);
		}
	}

	static byte[] getProgramNameInBytes(String programName) {
		byte[] b = programName.getBytes();
		byte[] res = null;
		if (b.length > maxProgramNameLength) {
			res = new byte[maxProgramNameLength];
			System.arraycopy(b, 0, res, 0, maxProgramNameLength);
		} else
			res = b;
		return res;
	}

	static DatagramLocalNetworkPresenceMessage readFrom(byte data[], int offset, int length) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, length)) {
			return readFrom(bais);
		}
	}

	static DatagramLocalNetworkPresenceMessage readFrom(InputStream is) throws IOException {
		try (DataInputStream dis = new DataInputStream(is)) {
			long onlineTime = dis.readLong();
			int programBuildNumber = dis.readInt();
			int madkitBuildNumber = dis.readInt();
			int length = dis.readShort();
			byte inetAddress[] = null;
			byte kernelAddress[] = null;
			if (length != 0) {
				inetAddress = new byte[length];
				dis.read(inetAddress, 0, length);
				try {
					InetAddress.getByAddress(inetAddress);
				} catch (UnknownHostException e) {
					throw new IOException(e);
				}
			}
			length = dis.readInt();
			if (length > maxKernelAddressLengthLength)
				throw new IOException("Kernel Address length too big");
			kernelAddress = new byte[length];
			dis.read(kernelAddress, 0, length);
			length = dis.readInt();
			if (length > maxProgramNameLength)
				throw new IOException("Program name length too big !");
			byte programName[] = new byte[length];
			dis.read(programName, 0, length);
			return new DatagramLocalNetworkPresenceMessage(onlineTime, programBuildNumber, madkitBuildNumber,
					programName, inetAddress, kernelAddress);
		}
	}

	boolean isCompatibleWith(long localOnlineTime, Version localProgramVersionMinimum,
			Version localMadkitVersionMinimum, KernelAddress kernelAddress) throws NoSuchAlgorithmException, NoSuchProviderException {
		/*
		 * if (localOnlineTime>=onlineTime) { return false; }
		 */

		return isCompatibleWith(localProgramVersionMinimum, localMadkitVersionMinimum, kernelAddress);
	}

	boolean isCompatibleWith(Version localProgramVersionMinimum, Version localMadkitVersionMinimum,
			KernelAddress kernelAddress) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (localMadkitVersionMinimum == null)
			throw new NullPointerException("localMadkitVersionMinimum");

		if (kernelAddress == null)
			throw new NullPointerException("kernelAddress");

		if (localMadkitVersionMinimum.getBuildNumber() > madkitBuildNumber)
			return false;

		if (localProgramVersionMinimum != null && localProgramVersionMinimum.getBuildNumber() > programBuildNumber)
			return false;
		byte[] lpn = null;
		if (localProgramVersionMinimum == null)
			lpn = getProgramNameInBytes(MadkitProperties.defaultProjectCodeName);
		else
			lpn = getProgramNameInBytes(localProgramVersionMinimum.getShortProgramName());

		if (lpn.length != programName.length)
			return false;

		for (int i = 0; i < lpn.length; i++)
			if (lpn[i] != programName[i])
				return false;

		byte[] ka = digestMessage(kernelAddress.getAbstractDecentralizedID().getBytes());
		if (Arrays.equals(ka, this.kernelAddress))
			return false;

		return true;
	}

	InetAddress getConcernedInetAddress() throws UnknownHostException {
		return inetAddress == null ? null : InetAddress.getByAddress(inetAddress);
	}

	public long getOnlineTime() {
		return onlineTime;
	}

	static int getMaxDatagramMessageLength() {
		long lv = 0;
		int iv = 0;
		short is = 0;
		return ObjectSizer.sizeOf(lv) + ObjectSizer.sizeOf(iv) * 2 + ObjectSizer.sizeOf(new byte[maxProgramNameLength])
				+ 16 + is;
	}
}
