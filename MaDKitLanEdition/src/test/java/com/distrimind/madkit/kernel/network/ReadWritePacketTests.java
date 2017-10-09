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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.io.RandomByteArrayInputStream;
import com.distrimind.madkit.io.RandomByteArrayOutputStream;
import com.distrimind.madkit.io.RandomFileInputStream;
import com.distrimind.madkit.io.RandomFileOutputStream;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.madkit.io.RandomOutputStream;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.unsecured.UnsecuredConnectionProtocolProperties;
import com.distrimind.util.crypto.MessageDigestType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
@RunWith(Parameterized.class)
public class ReadWritePacketTests extends JunitMadkit {
	public static int testsNumber = 300;
	final Random rand = new Random(System.currentTimeMillis());
	private final byte[] originalData;
	private byte[] data;
	private File fileInput;
	private File fileOutput;
	private final int idPacket = Math.abs(rand.nextInt());
	private final ConnectionProtocol<?> connectionProtocol;
	private final DataSocketSynchronizer synchronizer;
	private final SAI socketAgentInterface;

	static class SAI implements DataSocketSynchronizer.SocketAgentInterface {
		private boolean fail = false;
		private boolean received = false;
		private Block originalBlock = null;

		@Override
		public void receivedBlock(Block _block) {
			Assert.assertArrayEquals(originalBlock.getBytes(), _block.getBytes());
			received = true;
		}

		@Override
		public boolean processInvalidBlock(Exception _e, Block _block, boolean _candidate_to_ban) {
			_e.printStackTrace();
			fail = true;
			Assert.fail();
			return true;
		}

		@Override
		public boolean isBannedOrExpulsed() {
			return fail;
		}

		void setOriginalBlock(Block originalBlock) {
			this.originalBlock = originalBlock;
			this.received = false;
		}

		boolean isReceived() {
			return received;
		}

	}

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> res = new ArrayList<>(testsNumber);
		for (int i = 0; i < testsNumber; i++) {
			Object o[] = new Object[1];
			o[0] = getData();

			res.add(o);
		}
		return res;
	}

	public ReadWritePacketTests(byte[] data) throws NIOException, UnknownHostException {
		this.originalData = data;
		connectionProtocol = new UnsecuredConnectionProtocolProperties().getConnectionProtocolInstance(
				new InetSocketAddress(InetAddress.getByName("254.168.45.1"), 10),
				new InetSocketAddress(InetAddress.getByName("192.168.0.1"), 10), null, new MadkitProperties(), new NetworkProperties(), false,
				false);
		synchronizer = new DataSocketSynchronizer();
		socketAgentInterface = new SAI();

	}

	@Before
	public void createFile() throws IOException {
		data = originalData.clone();
		fileInput = new File(System.getProperty("java.io.tmpdir"), "TEST_RANDOM_FILE_INPUT_STREAM");
		fileOutput = new File(System.getProperty("java.io.tmpdir"), "TEST_RANDOM_FILE_OUTPUT_STREAM");
		try (FileOutputStream fos = new FileOutputStream(fileInput)) {
			fos.write(data);
		}
	}

	@After
	public void deleteFile() {
		fileInput.delete();
	}

	/*
	 * @Test public void testByteArrayInputStream() throws IOException {
	 * testInputStream(true, fileInput); }
	 * 
	 * @Test public void testFileInputStream() throws IOException { if
	 * (originalData.length<10000) testInputStream(false, fileInput); }
	 */

	@Test
	public void testByteArrayOutputInputStream() throws IOException {
		testOutputInputStream(true);
	}

	@Test
	public void testFileOutputStream() throws IOException {
		if (originalData.length < 10000)
			testOutputInputStream(false);
	}

	public void testInputStream(boolean byteArray, byte[] dataIn, File fileInput) throws IOException {
		try (RandomInputStream input = byteArray ? new RandomByteArrayInputStream(dataIn)
				: new RandomFileInputStream(fileInput)) {
			Assert.assertEquals(data.length, input.length());
			if (byteArray)
				Assert.assertEquals(data.length, input.available());
			for (int i = 0; i < data.length; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());

			}
			Assert.assertEquals(data.length, input.length());
			Assert.assertEquals(0, input.available());
			Assert.assertEquals(-1, input.read());
			Assert.assertEquals(data.length, input.currentPosition());
		}

		try (RandomInputStream input = byteArray ? new RandomByteArrayInputStream(dataIn)
				: new RandomFileInputStream(fileInput)) {
			byte[] data2 = new byte[(int) input.length()];
			input.read(data2);
			Assert.assertEquals(data.length, data2.length);
			for (int i = 0; i < data.length; i++)
				Assert.assertEquals(data[i], data2[i]);
			Assert.assertEquals(data.length, input.length());
			Assert.assertEquals(0, input.available());
			Assert.assertEquals(-1, input.read());
			Assert.assertEquals(data.length, input.currentPosition());
		}

		if (byteArray) {
			try (RandomByteArrayInputStream input = new RandomByteArrayInputStream(dataIn)) {
				byte[] data2 = input.getBytes();
				input.read(data2);
				Assert.assertEquals(data.length, data2.length);
				for (int i = 0; i < data.length; i++)
					Assert.assertEquals(data[i], data2[i]);
				Assert.assertEquals(data.length, input.length());
				Assert.assertEquals(0, input.available());
				Assert.assertEquals(-1, input.read());
				Assert.assertEquals(data.length, input.currentPosition());
			}
		}
		try (RandomInputStream input = byteArray ? new RandomByteArrayInputStream(dataIn)
				: new RandomFileInputStream(fileInput)) {
			for (int i = 0; i < 10; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());
			}
			input.mark(100);
			for (int i = 10; i < 20; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());
			}
			input.reset();
			for (int i = 10; i < data.length; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());
			}
		}
		try (RandomInputStream input = byteArray ? new RandomByteArrayInputStream(dataIn)
				: new RandomFileInputStream(fileInput)) {
			for (int i = 0; i < 10; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());
			}
			input.mark(10);
			for (int i = 10; i < 21; i++) {
				Assert.assertEquals(i, input.currentPosition());
				Assert.assertEquals(data[i], (byte) input.read());
				Assert.assertEquals(i + 1, input.currentPosition());
			}
			try {
				input.reset();
				noExceptionFailure();
			} catch (IOException e) {

			}
		}
	}

	public void testOutputInputStream(boolean byteArray) throws IOException {
		if (fileOutput.exists())
			fileOutput.delete();
		byte[] dataout = null;
		try (RandomOutputStream output = byteArray ? new RandomByteArrayOutputStream()
				: new RandomFileOutputStream(fileOutput)) {
			Assert.assertEquals(0, output.length());
			output.setLength(data.length);
			Assert.assertEquals(0, output.currentPosition());
			Assert.assertEquals(data.length, output.length());

			for (int i = 0; i < data.length; i++) {
				Assert.assertEquals(i, output.currentPosition());
				output.write(data[i]);
				Assert.assertEquals(i + 1, output.currentPosition());

			}
			Assert.assertEquals(data.length, output.length());
			Assert.assertEquals(data.length, output.currentPosition());
			if (output instanceof RandomByteArrayOutputStream)
				dataout = ((RandomByteArrayOutputStream) output).getBytes();
		}
		testInputStream(byteArray, dataout, fileOutput);
		fileOutput.delete();
		dataout = null;
		try (RandomOutputStream output = byteArray ? new RandomByteArrayOutputStream()
				: new RandomFileOutputStream(fileOutput)) {
			Assert.assertEquals(0, output.length());
			output.setLength(data.length);
			Assert.assertEquals(data.length, output.length());
			output.write(data);
			Assert.assertEquals(data.length, output.length());
			Assert.assertEquals(data.length, output.currentPosition());
			if (output instanceof RandomByteArrayOutputStream)
				dataout = ((RandomByteArrayOutputStream) output).getBytes();
		}
		testInputStream(byteArray, dataout, fileOutput);
		fileOutput.delete();

	}

	@Test
	public void testReadWritePacket() throws PacketException, NoSuchAlgorithmException, NIOException, NoSuchProviderException {
		testReadWritePacket(null);
		testReadWritePacket(MessageDigestType.BC_FIPS_SHA3_512);
	}

	public void testReadWritePacket(MessageDigestType messageDigestType)
			throws PacketException, NoSuchAlgorithmException, NIOException, NoSuchProviderException {
		for (int i = 0; i < 100; i++) {
			byte val = (byte) rand.nextInt(1 << WritePacket.getMaximumLocalRandomValuesBitsNumber());
			Assert.assertEquals(val,
					WritePacket.decodeLocalNumberRandomVal(WritePacket.encodeLocalNumberRandomVal(val, rand)));
		}
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 0, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 0, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 10, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 10, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 0, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 0, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 10, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 10, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 0, 0, data.length, true, messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 0, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 10, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 10, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 0, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 0, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 50, (short) 10, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) 200, (short) 10, 10, data.length - 50, true,
				messageDigestType);

		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 0, data.length, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 10, data.length - 50, false,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 10, data.length - 50,
				false, messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 10, data.length - 50,
				false, messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 0, data.length, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 0, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 10, data.length - 50, true,
				messageDigestType);
		testReadWritePacket(PacketPartHead.TYPE_PACKET, (short) Short.MAX_VALUE, (short) 10, 10, data.length - 50, true,
				messageDigestType);

	}

	private void testReadWritePacket(int _type, short _max_buffer_size, short random_values_size, long _start_position,
			long length, boolean _transfert_as_big_data, MessageDigestType messageDigestType)
			throws PacketException, NoSuchAlgorithmException, NIOException, NoSuchProviderException {
		WritePacket output = new WritePacket(_type, idPacket, _max_buffer_size, random_values_size, rand,
				new RandomByteArrayInputStream(data), _start_position, length, _transfert_as_big_data,
				messageDigestType);
		Assert.assertEquals(new Boolean(_transfert_as_big_data), new Boolean(output.concernsBigData()));
		Assert.assertEquals(length, output.getDataLength());
		int messageDigestSize = messageDigestType == null ? 0
				: messageDigestType.getMessageDigestInstance().getDigestLength();
		Assert.assertEquals(length + messageDigestSize, output.getDataLengthWithHashIncluded());
		Assert.assertEquals(idPacket, output.getID());
		Assert.assertEquals(0, output.getReadDataLengthIncludingHash());
		Assert.assertFalse(output.isFinished());
		ReadPacket read = null;
		RandomByteArrayOutputStream outputStream = new RandomByteArrayOutputStream();
		do {
			PacketPart pp = output.getNextPart();
			Assert.assertTrue(pp.getHead().isPacketPart());
			Assert.assertFalse(pp.isReadyToBeRead());
			Assert.assertTrue(pp.isReadyToBeSent());
			Assert.assertEquals(pp.getHead().getID(), idPacket);
			Assert.assertTrue(pp.getHead().isPacketPart());
			if (read == null) {
				Assert.assertTrue(pp.getHead().isFirstPacketPart());
				Assert.assertEquals(length, pp.getHead().getTotalLength());
				Assert.assertEquals(_start_position, pp.getHead().getStartPosition());

			}

			byte bpp[] = testDataSynchronizer(pp);

			if (!output.isFinished())
				Assert.assertTrue(bpp.length >= _max_buffer_size);
			pp = new PacketPart(bpp, _max_buffer_size, random_values_size);
			Assert.assertTrue(pp.isReadyToBeRead());
			Assert.assertFalse(pp.isReadyToBeSent());
			Assert.assertTrue(pp.getHead().isPacketPart());
			if (read == null) {
				Assert.assertTrue(pp.getHead().isFirstPacketPart());

				Assert.assertEquals(length + messageDigestSize, pp.getHead().getTotalLength());
				Assert.assertEquals(_start_position, pp.getHead().getStartPosition());

				read = new ReadPacket(_max_buffer_size, random_values_size, pp, outputStream, messageDigestType);
				// Assert.assertEquals(read.getCurrentPosition(), 0);
				Assert.assertEquals(idPacket, read.getID());
				if (output.isFinished())
					Assert.assertTrue(read.isFinished());
				else
					Assert.assertFalse(read.isFinished());
			} else {
				Assert.assertFalse(pp.getHead().isFirstPacketPart());
				read.readNewPart(pp);
				if (output.isFinished())
					Assert.assertTrue(read.isFinished());
				else
					Assert.assertFalse(read.isFinished());
			}
			Assert.assertFalse(read.isTemporaryInvalid());
			Assert.assertFalse(read.isInvalid());
			Assert.assertTrue(read.isValid());

		} while (!output.isFinished());
		Assert.assertEquals(length + messageDigestSize, output.getReadDataLengthIncludingHash());
		Assert.assertEquals(length, output.getReadDataLength());
		Assert.assertTrue(read.isFinished());
		byte res[] = outputStream.getBytes();
		Assert.assertEquals(_start_position + length, res.length);
		for (int i = 0; i < length; i++)
			Assert.assertEquals(data[(int) _start_position + i], res[(int) _start_position + i]);
	}

	private byte[] testDataSynchronizer(PacketPart pp) throws NIOException, PacketException {

		SubBlocksStructure sbs = new SubBlocksStructure(pp, connectionProtocol);
		byte[] ppb = pp.getBytes();
		byte[] b = new byte[ppb.length + Block.getHeadSize()];
		System.arraycopy(ppb, 0, b, Block.getHeadSize(), ppb.length);
		final Block block = new Block(b, sbs, -1);
		socketAgentInterface.setOriginalBlock(block);
		int index = 0;
		while (index < b.length) {
			int nb = rand.nextInt(b.length - index) + 1;
			byte tmpb[] = new byte[nb];
			System.arraycopy(b, index, tmpb, 0, nb);
			index += nb;
			synchronizer.receiveData(tmpb, socketAgentInterface);
		}
		Assert.assertTrue(socketAgentInterface.isReceived());
		return ppb;
	}

	public static byte[] getData() {
		Random rand = new Random(System.currentTimeMillis());
		byte[] res = new byte[100 + rand.nextInt(1000000)];
		for (int i = 0; i < res.length; i++)
			res[i] = (byte) rand.nextInt();
		return res;
	}
}
