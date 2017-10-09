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
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.util.Bits;
import com.distrimind.util.sizeof.ObjectSizer;
import com.distrimind.util.version.Version;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class DatagramDataTest {
	private static final String programName = "programName";
	private static final InetAddress inet4, inet6;
	private static final KernelAddress kernelAddress;
	private static final KernelAddress kernelAddressReceiver;
	static {
		KernelAddress ka1 = null;
		KernelAddress ka2 = null;
		try {
			ka1 = JunitMadkit.getKernelAddressInstance();
			ka2 = JunitMadkit.getKernelAddressInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		kernelAddress = ka1;
		kernelAddressReceiver = ka2;
	}
	static {
		InetAddress i4 = null, i6 = null;
		try {
			i4 = InetAddress.getByName("192.168.0.1");
			i6 = InetAddress.getByName("2001:db8:0:85a3:0:0:ac1f:8001");
		} catch (Exception e) {
			e.printStackTrace();
		}
		inet4 = i4;
		inet6 = i6;
	}

	@Test
	public void testOneMessageDatagramData() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		Assert.assertNotNull(inet4);
		Assert.assertNotNull(inet6);
		testValidOneMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet4, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
		testValidOneMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet6, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
		Version v1 = getVersionBase(programName);
		v1.incrementBuildNumber();
		testValidOneMessageDatagramData(new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), v1,
				getVersionBase(programName), inet4, kernelAddress), getVersionBase(programName),
				getVersionBase(programName));
		testValidOneMessageDatagramData(new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), v1,
				getVersionBase(programName), inet6, kernelAddress), getVersionBase(programName),
				getVersionBase(programName));
		Version v2 = getVersionBase(programName);
		v2.incrementBuildNumber();
		testValidOneMessageDatagramData(new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(),
				getVersionBase(programName), v2, inet4, kernelAddress), getVersionBase(programName),
				getVersionBase(programName));
		testValidOneMessageDatagramData(new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(),
				getVersionBase(programName), v2, inet6, kernelAddress), getVersionBase(programName),
				getVersionBase(programName));
	}

	private void testValidOneMessageDatagramData(DatagramLocalNetworkPresenceMessage message, Version programVersion,
			Version madkitVersion) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		DatagramData d = new DatagramData(message);

		DatagramData d2 = new DatagramData();
		Assert.assertFalse(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		d2.put(d.getByteBuffer().array(), 0, d.getByteBuffer().array().length);
		Assert.assertTrue(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		DatagramLocalNetworkPresenceMessage m = d2.getDatagramLocalNetworkPresenceMessage();
		Assert.assertTrue(m.isCompatibleWith(0, programVersion, madkitVersion, kernelAddressReceiver));
		Assert.assertFalse(m.isCompatibleWith(0, programVersion, madkitVersion, kernelAddress));
		Assert.assertNull(d2.getUnusedReceivedData());
		Assert.assertNull(d2.getNextDatagramData());
		programVersion.incrementBuildNumber();
		programVersion.incrementBuildNumber();
		Assert.assertFalse(m.isCompatibleWith(0, programVersion, madkitVersion, kernelAddressReceiver));
		programVersion = getVersionBase(programName);
		madkitVersion.incrementBuildNumber();
		madkitVersion.incrementBuildNumber();
		Assert.assertFalse(m.isCompatibleWith(0, programVersion, madkitVersion, kernelAddressReceiver));

	}

	@Test
	public void testInvalidOneMessageDatagramData() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		Assert.assertNotNull(inet4);
		Assert.assertNotNull(inet6);
		testInvalidOneMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet4, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
		testInvalidOneMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet6, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
	}

	private void testInvalidOneMessageDatagramData(DatagramLocalNetworkPresenceMessage message, Version programVersion,
			Version madkitVersion) throws IOException {
		DatagramData d = new DatagramData(message);

		DatagramData d2 = new DatagramData();
		Assert.assertFalse(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		d2.getByteBuffer().putInt(10000);
		d2.put(d.getByteBuffer().array(), 0, d.getByteBuffer().array().length);
		Assert.assertFalse(d2.isValid());
		Assert.assertFalse(d2.isComplete());
	}

	@Test
	public void testMultipleMessageDatagramData() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		Assert.assertNotNull(inet4);
		Assert.assertNotNull(inet6);
		testMultipleMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet4, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
		testMultipleMessageDatagramData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet6, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
	}

	private void testMultipleMessageDatagramData(DatagramLocalNetworkPresenceMessage message, Version programVersion,
			Version madkitVersion) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		DatagramData d = new DatagramData(message);

		DatagramData d2 = new DatagramData();
		Assert.assertFalse(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		d2.put(d.getByteBuffer().array(), 0, d.getByteBuffer().array().length);
		d2.put(d.getByteBuffer().array(), 0, d.getByteBuffer().array().length);
		Assert.assertTrue(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		Assert.assertTrue(d2.getDatagramLocalNetworkPresenceMessage().isCompatibleWith(0, programVersion, madkitVersion,
				kernelAddressReceiver));
		Assert.assertFalse(d2.getDatagramLocalNetworkPresenceMessage().isCompatibleWith(0, programVersion,
				madkitVersion, kernelAddress));
		d2 = d2.getNextDatagramData();
		Assert.assertNotNull(d2);
		Assert.assertTrue(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		Assert.assertTrue(d2.getDatagramLocalNetworkPresenceMessage().isCompatibleWith(0, programVersion, madkitVersion,
				kernelAddressReceiver));
	}

	@Test
	public void testOneMessageDatagramDataAndRandomData() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		Assert.assertNotNull(inet4);
		Assert.assertNotNull(inet6);
		testOneMessageDatagramDataAndRandomData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet4, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
		testOneMessageDatagramDataAndRandomData(
				new DatagramLocalNetworkPresenceMessage(System.currentTimeMillis(), getVersionBase(programName),
						getVersionBase(programName), inet6, kernelAddress),
				getVersionBase(programName), getVersionBase(programName));
	}

	private void testOneMessageDatagramDataAndRandomData(DatagramLocalNetworkPresenceMessage message,
			Version programVersion, Version madkitVersion) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		DatagramData d = new DatagramData(message);

		DatagramData d2 = new DatagramData();
		Assert.assertFalse(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		byte[] data = d.getByteBuffer().array();
		Assert.assertEquals(data.length, Bits.getInt(data, 0) + ObjectSizer.sizeOf((int) 0));
		d2.put(data, 0, data.length);
		Assert.assertEquals(data.length, Bits.getInt(d2.getByteBuffer().array(), 0) + ObjectSizer.sizeOf((int) 0));
		Assert.assertEquals(data.length, d2.getByteBuffer().getInt(0) + ObjectSizer.sizeOf((int) 0));
		Random rand = new Random(System.currentTimeMillis());
		byte randData[] = new byte[rand.nextInt(500) + 600];
		rand.nextBytes(randData);
		d2.put(randData, 0, randData.length);
		Assert.assertEquals(data.length, Bits.getInt(d2.getByteBuffer().array(), 0) + ObjectSizer.sizeOf((int) 0));
		Assert.assertEquals(data.length, d2.getByteBuffer().getInt(0) + ObjectSizer.sizeOf((int) 0));
		Assert.assertTrue(d2.isComplete());
		Assert.assertTrue(d2.isValid());
		Assert.assertTrue(d2.getDatagramLocalNetworkPresenceMessage().isCompatibleWith(0, programVersion, madkitVersion,
				kernelAddressReceiver));
		Assert.assertEquals(randData.length, d2.getByteBuffer().position()
				- (Bits.getInt(d2.getByteBuffer().array(), 0) + ObjectSizer.sizeOf((int) 0)));
		Assert.assertEquals(randData.length,
				d2.getByteBuffer().position() - (d2.getByteBuffer().getInt(0) + ObjectSizer.sizeOf((int) 0)));
		ByteBuffer randDataReceived = d2.getUnusedReceivedData();
		Assert.assertNotNull(randDataReceived);
		Assert.assertArrayEquals(randData, randDataReceived.array());
	}

	Version getVersionBase(String programeName) {
		Version res = new Version(programName + "long", programName, 1, 0, 0, Version.Type.Stable, 0,
				new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
		res.setBuildNumber(0);
		return res;
	}

}
