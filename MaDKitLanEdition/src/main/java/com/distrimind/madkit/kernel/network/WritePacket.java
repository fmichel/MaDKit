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
import java.util.Random;

import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.exceptions.UnknownPacketTypeException;
import com.distrimind.madkit.io.RandomFileInputStream;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.util.Bits;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.MessageDigestType;

import gnu.vm.jgnu.security.DigestException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see ReadPacket
 */
public final class WritePacket {
	private AbstractMessageDigest messageDigest;
	private final byte[] digestResult;
	private int digestResultPos = -1;
	protected final RandomInputStream input_stream;
	private final int id_packet;
	private final long start_position;
	private volatile long current_pos = 0;
	protected final short max_buffer_size;
	private final long data_length;
	private final long data_length_with_message_digest;
	private final boolean redownloaded;
	private boolean finished = false;
	private final boolean transfert_as_big_data;
	private final short random_values_size;
	private final Random random;

	public WritePacket(int _type, int _id_packet, short _max_buffer_size, short random_values_size, Random rand,
			RandomInputStream _input_stream) throws PacketException, IOException {
		this(_type, _id_packet, _max_buffer_size, random_values_size, rand, _input_stream, 0, _input_stream.length());
	}

	public WritePacket(int _type, int _id_packet, short _max_buffer_size, short random_values_size, Random rand,
			RandomInputStream _input_stream, MessageDigestType messageDigestType) throws PacketException, IOException {
		this(_type, _id_packet, _max_buffer_size, random_values_size, rand, _input_stream, 0, _input_stream.length(),
				_input_stream instanceof RandomFileInputStream, messageDigestType);
	}

	public WritePacket(int _type, int _id_packet, short _max_buffer_size, short random_values_size, Random rand,
			RandomInputStream _input_stream, long _data_length) throws PacketException {
		this(_type, _id_packet, _max_buffer_size, random_values_size, rand, _input_stream, 0, _data_length);
	}

	public WritePacket(int _type, int _id_packet, short _max_buffer_size, short random_values_size, Random rand,
			RandomInputStream _input_stream, long _start_position, long length) throws PacketException {
		this(_type, _id_packet, _max_buffer_size, random_values_size, rand, _input_stream, _start_position, length,
				_input_stream instanceof RandomFileInputStream, null);
	}

	public WritePacket(int _type, int _id_packet, short _max_buffer_size, short random_values_size, Random rand,
			RandomInputStream _input_stream, long _start_position, long length, boolean _transfert_as_big_data,
			MessageDigestType messageDigestType) throws PacketException {
		if ((_type & PacketPartHead.TYPE_PACKET) != PacketPartHead.TYPE_PACKET)
			throw new UnknownPacketTypeException("The given type is not a packet type (" + _type + ")");
		try {
			if (messageDigestType == null) {
				messageDigest = null;
				digestResult = null;
			} else {
				messageDigest = messageDigestType.getMessageDigestInstance();
				messageDigest.reset();
				digestResult = new byte[messageDigest.getDigestLength()];
			}
		} catch (Exception e) {
			throw new PacketException(e);
		}

		redownloaded = (_type & PacketPartHead.TYPE_PACKET_REDOWNLOADED) == PacketPartHead.TYPE_PACKET_REDOWNLOADED;

		input_stream = _input_stream;
		id_packet = _id_packet;
		start_position = current_pos = _start_position;
		max_buffer_size = _max_buffer_size;
		this.random_values_size = getRandomValueSize(max_buffer_size, random_values_size);

		if (this.random_values_size == 0)
			random = null;
		else {
			if (rand == null)
				throw new NullPointerException("rand");
			random = rand;
		}
		try {
			data_length = length;
			data_length_with_message_digest = messageDigest == null ? data_length
					: data_length + messageDigest.getDigestLength();
			if (_input_stream.length() - _start_position < data_length)
				throw new PacketException("The given data length (" + data_length + ") cannot be greater than "
						+ (_input_stream.length() - _start_position));
			_input_stream.seek(_start_position);
		} catch (IOException e) {
			throw new PacketException(e);
		}

		transfert_as_big_data = _transfert_as_big_data;
	}

	static short getRandomValueSize(short max_buffer_size, short random_values_size) {
		if (random_values_size > getMiniRandomValueSize()) {
			if (random_values_size < getMaximumGlobalRandomValues(max_buffer_size))
				return random_values_size;
			else
				return getMaximumGlobalRandomValues(max_buffer_size);
		} else
			return 0;
	}

	static protected short getMiniRandomValueSize() {
		return 3;
	}

	static protected byte getMaximumLocalRandomValues() {
		return 32;
	}

	static protected byte getMaximumLocalRandomValuesBitsNumber() {
		return 5;
	}

	static byte encodeLocalNumberRandomVal(byte val, Random rand) {
		return (byte) ((rand
				.nextInt(1 << (8 - getMaximumLocalRandomValuesBitsNumber())) << getMaximumLocalRandomValuesBitsNumber())
				| (int) val);
	}

	static byte decodeLocalNumberRandomVal(byte val) {
		return (byte) ((val & 255) & ((1 << getMaximumLocalRandomValuesBitsNumber()) - 1));
	}

	static protected short getMaximumGlobalRandomValues(short _max_buffer_size) {
		return (short) (_max_buffer_size / 2);
	}

	public long getDataLengthWithHashIncluded() {
		return data_length_with_message_digest;
	}

	public long getDataLength() {
		return data_length;
	}

	RandomInputStream getInputStream() {
		return input_stream;
	}

	public boolean concernsBigData() {
		return transfert_as_big_data;
	}

	private final PacketPartHead setHeadPart(boolean last_packet, AbstractByteTabOutputStream tab) {
		byte type = PacketPartHead.TYPE_PACKET;
		if (current_pos == start_position) {
			if (redownloaded)
				type |= PacketPartHead.TYPE_PACKET_REDOWNLOADED;
			else
				type |= PacketPartHead.TYPE_PACKET_HEAD;
		}
		if (last_packet)
			type |= PacketPartHead.TYPE_PACKET_LAST;
		tab.writeData(type);
		tab.writeInt(id_packet);
		tab.writeShort(tab.getRealDataSizeWithoutPacketHeadSize());
		if (current_pos == start_position) {
			tab.writeLong(data_length_with_message_digest);
			tab.writeLong(start_position);
		}
		return new PacketPartHead(type, id_packet, tab.getRealDataSizeWithoutPacketHeadSize(), data_length,
				start_position);
	}

	public final int getID() {
		return id_packet;
	}

	public boolean isFinished() {
		return finished;
	}

	public long getReadDataLengthIncludingHash() {
		return current_pos - start_position;
	}

	public long getReadDataLength() {
		return Math.min(current_pos - start_position, data_length);
	}

	public final PacketPart getNextPart() throws PacketException {
		try {
			if (finished)
				return null;
			boolean first_packet = (current_pos == start_position);
			int headSize = PacketPartHead.getHeadSize(first_packet);
			AbstractByteTabOutputStream res = getByteTabOutputStream(
					data_length + start_position <= current_pos ? null : messageDigest, max_buffer_size, headSize,
					data_length_with_message_digest - (current_pos - start_position), random_values_size, random);
			boolean last_packet = (current_pos
					+ res.getRealDataSizeWithoutPacketHeadSize()) == (data_length_with_message_digest + start_position);
			PacketPartHead pph = setHeadPart(last_packet, res);

			int currentPacketDataSize = (int) Math.max(
					Math.min(res.getRealDataSizeWithoutPacketHeadSize(), data_length - (current_pos - start_position)),
					0);

			// byte[] res=new byte[PacketPartHead.getHeadSize(first_packet)+size];
			if (currentPacketDataSize > 0) {
				// int offset=PacketPartHead.getHeadSize(first_packet);
				int readed_data = res.writeData(input_stream, currentPacketDataSize);
				if (readed_data != currentPacketDataSize)
					throw new IllegalAccessError("Illegal writed data quantity : writed=" + readed_data + ", expected="
							+ currentPacketDataSize);
				// int readed_data=input_stream.read(res, offset, size);

				current_pos += readed_data;

			}
			if (messageDigest != null) {
				if (current_pos == data_length + start_position && digestResultPos < 0) {
					int dl = messageDigest.digest(digestResult, 0, digestResult.length);
					if (dl != digestResult.length)
						throw new IllegalAccessError("Invalid signature size !");
					digestResultPos = 0;
					res.disableMessageDigest();
				}

				if (current_pos >= data_length + start_position) {
					if (digestResultPos < 0)
						throw new IllegalAccessError();
					int currentDigestSize = res.getRealDataSizeWithoutPacketHeadSize() - currentPacketDataSize;
					if (currentDigestSize > 0) {
						int readed_data = res.writeData(digestResult, digestResultPos, currentDigestSize);
						if (readed_data != currentDigestSize)
							throw new IllegalAccessError("Illegal writed hash data quantity : writed=" + readed_data
									+ ", expected=" + currentDigestSize);
						digestResultPos += readed_data;
						current_pos += readed_data;
					}

				}
			}
			if (current_pos == data_length_with_message_digest + start_position) {
				finished = true;
			}
			res.finilizeTab();
			int totalDataLength = res.getWritedData() - headSize;
			if (totalDataLength != res.getRealDataSizeWithoutPacketHeadSize())
				throw new IllegalAccessError("The length returned by the input stream (" + totalDataLength
						+ ") does not corresponds to the effective contained data ("
						+ res.getRealDataSizeWithoutPacketHeadSize() + ").");

			/*
			 * if (current_pos<data_length_with_message_digest+start_position &&
			 * readed_data!=res.getRealDataSizeWithoutPacketHeadSize()) { throw new
			 * PacketException("The length returned by the input stream ("
			 * +readed_data+") does not corresponds to the effective contained data ("+res.
			 * getRealDataSizeWithoutPacketHeadSize()+")."); }
			 */
			if (current_pos > data_length_with_message_digest + start_position) {
				finished = true;
				throw new IllegalAccessError(
						"The length returned by the input stream does not corresponds to the effective contained data.");
			}

			return new PacketPart(res.getBytesArray(), pph);
		} catch (IOException | DigestException | IllegalAccessError e) {
			throw new PacketException(e);
		}
	}

	protected static abstract class AbstractByteTabOutputStream {
		protected AbstractMessageDigest messageDigest;

		protected AbstractByteTabOutputStream(AbstractMessageDigest messageDigest) {
			this.messageDigest = messageDigest;
		}

		abstract boolean writeData(byte d);

		abstract int writeData(byte[] d, int offset, int size);

		abstract int writeData(RandomInputStream is, int size) throws IOException;

		abstract void finilizeTab();

		abstract byte[] getBytesArray();

		abstract int getRealDataSize();

		abstract short getRealDataSizeWithoutPacketHeadSize();

		boolean writeInt(int _value) {
			byte b[] = new byte[4];
			Bits.putInt(b, 0, _value);
			return writeData(b, 0, 4) == 4;
		}

		boolean writeShort(short _value) {
			byte b[] = new byte[2];
			Bits.putShort(b, 0, _value);
			return writeData(b, 0, 2) == 2;
		}

		boolean writeLong(long _value) {
			byte b[] = new byte[8];
			Bits.putLong(b, 0, _value);
			return writeData(b, 0, 8) == 8;
		}

		void disableMessageDigest() {
			messageDigest = null;
		}

		abstract int getWritedData();

	}

	protected static AbstractByteTabOutputStream getByteTabOutputStream(AbstractMessageDigest messageDigest,
			short max_buffer_size, int packet_head_size, long _data_remaining, short random_values_size, Random rand) {
		if (random_values_size == 0)
			return new ByteTabOutputStream(messageDigest, max_buffer_size, packet_head_size, _data_remaining);
		else
			return new ByteTabOutputStreamWithRandomValues(messageDigest, max_buffer_size, packet_head_size,
					_data_remaining, random_values_size, rand);
	}

	protected static class ByteTabOutputStream extends AbstractByteTabOutputStream {
		private final byte[] tab;
		private int cursor;
		private final short realDataSize_WithoutHead;

		ByteTabOutputStream(AbstractMessageDigest messageDigest, short max_buffer_size, int packet_head_size,
				long _data_remaining) {
			super(messageDigest);
			/*
			 * realDataSize_WithoutHead=(short)Math.min(_data_remaining, max_buffer_size);
			 * int size=(int)(realDataSize_WithoutHead+packet_head_size); tab=new
			 * byte[size];
			 */
			realDataSize_WithoutHead = (short) Math.min(_data_remaining, max_buffer_size);
			int size = packet_head_size + ((int) realDataSize_WithoutHead);
			tab = new byte[size];
			cursor = 0;
		}

		@Override
		int getWritedData() {
			return cursor;
		}

		@Override
		boolean writeData(byte _d) {
			if (cursor >= tab.length)
				return false;
			tab[cursor++] = _d;
			if (messageDigest != null)
				messageDigest.update(_d);
			return true;
		}

		@Override
		int writeData(byte[] _d, int _offset, int size) {
			System.arraycopy(_d, _offset, tab, cursor, size);
			if (messageDigest != null)
				messageDigest.update(_d, _offset, size);
			cursor += size;
			return size;
		}

		@Override
		int writeData(RandomInputStream _is, int _size) throws IOException {
			int rl = _is.read(tab, cursor, _size);
			if (messageDigest != null)
				messageDigest.update(tab, cursor, _size);
			cursor += rl;
			return rl;
		}

		@Override
		void finilizeTab() {
		}

		@Override
		byte[] getBytesArray() {
			return tab;
		}

		@Override
		int getRealDataSize() {
			return tab.length;
		}

		@Override
		short getRealDataSizeWithoutPacketHeadSize() {
			return realDataSize_WithoutHead;
		}
	}

	protected static class ByteTabOutputStreamWithRandomValues extends AbstractByteTabOutputStream {
		private final Random random;
		private final byte[] tab;
		private final short random_values_size;
		private short random_values_size_remaining;
		private int data_size;
		private int cursor;
		private int nextRandValuePos;
		private final short realDataSize_WithoutHead;
		private int randamValuesWrited = 0;

		ByteTabOutputStreamWithRandomValues(AbstractMessageDigest messageDigest, short max_buffer_size,
				int packet_head_size, long _data_remaining, short max_random_values_size, Random rand) {
			super(messageDigest);

			this.random = rand;
			short min = getMiniRandomValueSize();
			if (max_random_values_size >= min)
				this.random_values_size = (short) (min + rand.nextInt(
						Math.min(getMaximumGlobalRandomValues(max_buffer_size), max_random_values_size) - min + 1));
			else
				this.random_values_size = 0;
			random_values_size_remaining = this.random_values_size;
			/*
			 * int size=(int)(Math.min(_data_remaining, max_buffer_size)+packet_head_size);
			 * tab=new byte[(int)Math.max(Math.min(size, max_buffer_size),
			 * Math.min(size+this.random_values_size, max_buffer_size))];
			 * realDataSize_WithoutHead=(short)(tab.length-this.random_values_size-
			 * packet_head_size); data_size=(short)(tab.length-this.random_values_size);
			 */
			int size = (int) (Math.min(_data_remaining, max_buffer_size));
			tab = new byte[size + packet_head_size + this.random_values_size];
			realDataSize_WithoutHead = (short) (size);
			data_size = tab.length - this.random_values_size;
			cursor = 0;
			nextRandValuePos = 0;
		}

		@Override
		int getWritedData() {
			return cursor - randamValuesWrited;
		}

		@Override
		boolean writeData(byte d) {
			byte t[] = new byte[1];
			t[0] = d;
			return writeData(t, 0, 1) == 1;
		}

		@Override
		int writeData(byte[] d, int offset, int size) {
			if (size <= 0)
				return 0;
			int total = 0;
			while (size > 0) {
				int length = Math.min(nextRandValuePos - cursor, size);
				if (length > 0) {
					System.arraycopy(d, offset, tab, cursor, length);
					if (messageDigest != null)
						messageDigest.update(tab, cursor, length);
					offset += length;
					cursor += length;
					size -= length;
					total += length;
					writeRandomValues();
				} else if (cursor >= tab.length)
					return total;
				else
					writeRandomValues();
			}
			return total;
		}

		@Override
		int writeData(RandomInputStream is, int size) throws IOException {
			if (size <= 0)
				return 0;
			int total = 0;
			while (size > 0) {
				int length = Math.min(nextRandValuePos - cursor, size);

				if (length > 0) {
					int readLength = is.read(tab, cursor, length);
					if (messageDigest != null)
						messageDigest.update(tab, cursor, length);

					cursor += readLength;
					total += readLength;
					size -= readLength;
					if (readLength != length)
						return total;
					writeRandomValues();
				} else if (cursor >= tab.length)
					return total;
				else
					writeRandomValues();
			}
			return total;
		}

		private void writeRandomValues() {
			if (cursor == nextRandValuePos) {
				random_values_size_remaining = (short) Math.min(tab.length - cursor, random_values_size_remaining);
				if (random_values_size_remaining < getMiniRandomValueSize()) {
					random_values_size_remaining = 0;
					nextRandValuePos = tab.length;
					return;
				}

				short nbrandmax = (short) Math.min(random_values_size_remaining - getMiniRandomValueSize() + 1,
						getMaximumLocalRandomValues() - 1);
				byte nbrand = (byte) (random.nextInt(nbrandmax) + 1);
				byte tabrand[] = new byte[nbrand];
				random.nextBytes(tabrand);
				byte nextRand = -1;
				if (random_values_size_remaining - getMiniRandomValueSize() * 2 + 1 - nbrand >= 0)
					nextRand = (byte) (random.nextInt(64) + 64);
				tab[cursor++] = encodeLocalNumberRandomVal(nbrand, random);
				for (int i = 0; i < tabrand.length; i++)
					tab[cursor++] = tabrand[i];
				tab[cursor++] = nextRand;
				randamValuesWrited += 2 + tabrand.length;
				if (nextRand == -1)
					nextRandValuePos = tab.length;
				else
					nextRandValuePos = cursor + nextRand;
				random_values_size_remaining -= (nbrand + 2);
			}
		}

		@Override
		void finilizeTab() {
			byte b[] = new byte[tab.length - cursor];
			random.nextBytes(b);
			System.arraycopy(b, 0, tab, cursor, b.length);
		}

		@Override
		byte[] getBytesArray() {
			return tab;
		}

		@Override
		int getRealDataSize() {
			return data_size;
		}

		@Override
		short getRealDataSizeWithoutPacketHeadSize() {
			return this.realDataSize_WithoutHead;
		}

	}

}
