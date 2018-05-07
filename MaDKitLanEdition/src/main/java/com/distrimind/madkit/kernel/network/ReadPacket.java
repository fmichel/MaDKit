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

import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.exceptions.UnknownPacketTypeException;
import com.distrimind.madkit.io.RandomOutputStream;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.MessageDigestType;

/**
 * It a reader that transfert a {@link PacketPart} to an
 * {@link RandomOutputStream}
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see WritePacket
 */
final class ReadPacket {
	static enum Validity {
		VALID, TEMPORARY_INVALID, INVALID
	}

	private AbstractMessageDigest messageDigest;
	private final byte[] digestResult;
	private final byte[] digestResultTransmitted;
	private int digestResultPos = -1;

	private final int id_packet;
	private final long length;
	private final long data_length_with_message_digest;
	private final long start_position;

	private long current_pos = 0;
	private Validity validity = Validity.VALID;

	// private final short max_buffer_size, max_random_values;

	private final RandomOutputStream output_stream;

	public ReadPacket(int max_buffer_size, short max_random_values, PacketPart _first_part,
			RandomOutputStream _output_stream, MessageDigestType messageDigestType) throws PacketException {
		if (_first_part == null)
			throw new NullPointerException("_first_part");
		if (_output_stream == null)
			throw new NullPointerException("_output_stream");
		try {
			if (messageDigestType == null) {
				messageDigest = null;
				digestResult = null;
				digestResultTransmitted = null;
			} else {
				messageDigest = messageDigestType.getMessageDigestInstance();
				messageDigest.reset();
				digestResult = new byte[messageDigest.getDigestLength()];
				digestResultTransmitted = new byte[digestResult.length];
			}
		} catch (Exception e) {
			throw new PacketException(e);
		}
		output_stream = _output_stream;

		try {
			if (!_first_part.getHead().isPacketPart())
				throw new UnknownPacketTypeException(
						"The given packet part is not a packet (type=" + _first_part.getHead().getType() + ")");

			if (!_first_part.getHead().isFirstPacketPart())
				throw new UnknownPacketTypeException(
						"The given packet part is not the first packet (type=" + _first_part.getHead().getType() + ")");
		} catch (PacketException e) {
			processTemporaryInvalid();
			throw e;
		}
		id_packet = _first_part.getHead().getID();
		start_position = _first_part.getHead().getStartPosition();
		data_length_with_message_digest = _first_part.getHead().getTotalLength();
		length = data_length_with_message_digest - (messageDigest == null ? 0 : digestResult.length);
		try {
			output_stream.ensureLength(length + start_position);
			output_stream.seek(start_position);
		} catch (IOException e) {
			processInvalid();
			throw new PacketException(e);
		}
		readNewPart(_first_part);
	}

	public final void readNewPart(PacketPart _part) throws PacketException {
		if (isFinished())
			return;
		if (_part == null)
			throw new NullPointerException("The given part is a null pointer.");
		if (!_part.isReadyToBeRead())
			throw new IllegalArgumentException("The given packet is not ready to be read, but to be sent !");
		try {
			if (!_part.getHead().isPacketPart())
				throw new UnknownPacketTypeException(
						"The given packet part is not a packet (type=" + _part.getHead().getType() + ")");

			if (current_pos != 0 && _part.getHead().isFirstPacketPart())
				throw new PacketException("The given packet part is a first part (attempted next parts).");

			if (current_pos == 0 && !_part.getHead().isFirstPacketPart())
				throw new PacketException("The given packet part is not a first part (attempted as first).");

			/*
			 * if (_part.getBytes().length>max_buffer_size+_part.getHead().getHeadSize()+
			 * max_random_values) throw new
			 * PacketException("The size of the given part ("+_part.getBytes().
			 * length+") must be lower or equal to ("+(max_buffer_size+_part.getHead().
			 * getHeadSize()+max_random_values)+")");
			 */

			if (_part.getHead().getID() != id_packet)
				throw new PacketException("The given packet has not the same id (" + _part.getHead().getID()
						+ ") than those attempted (" + id_packet + ")");
		} catch (PacketException e) {
			processTemporaryInvalid();
			throw e;
		}

		try {
			int offset = _part.getHead().getHeadSize();

			if (isValid()) {
				int attempted_lenth = _part.getHead().getDataSize();
				if (attempted_lenth < 1) {
					processTemporaryInvalid();
					throw new PacketException("Invalid packet (negative packet size).");
				}
				long waiting_data = data_length_with_message_digest - current_pos;
				if (attempted_lenth > waiting_data
						|| (attempted_lenth == waiting_data && !_part.getHead().isLastPacket())
						|| (_part.getHead().isLastPacket() && attempted_lenth != waiting_data)) {
					processTemporaryInvalid();
					throw new PacketException(
							"Invalid packet. Unexpected size, or packet coherence invalid : attempted_lenght="
									+ attempted_lenth + ", waiting_data=" + waiting_data + ", isLastPacket="
									+ _part.getHead().isLastPacket());
				}
				int currentPacketDataSize = (int) Math.max(Math.min(attempted_lenth, length - current_pos), 0);
				SubBlock subBlock=_part.getSubBlock();
				byte bytes[] = subBlock.getBytes();
				
				if (messageDigest != null)
					messageDigest.update(bytes, subBlock.getOffset(), offset);

				offset+=subBlock.getOffset();
				if (currentPacketDataSize > 0) {
					output_stream.write(bytes, offset, currentPacketDataSize);
					if (messageDigest != null)
						messageDigest.update(bytes, offset, currentPacketDataSize);
					current_pos += currentPacketDataSize;
					processValid();
				}
				if (messageDigest != null) {
					if (current_pos == length && digestResultPos < 0) {
						int dl = messageDigest.digest(digestResult, 0, digestResult.length);
						if (dl != digestResult.length)
							throw new PacketException("Invalid signature size !");
						digestResultPos = 0;
					}
					if (current_pos >= length) {
						if (digestResultPos < 0) {
							throw new PacketException("digest message not initialized !");
						}
						int currentDigestSize = attempted_lenth - currentPacketDataSize;
						if (currentDigestSize > 0) {
							System.arraycopy(bytes, offset + currentPacketDataSize, digestResultTransmitted,
									digestResultPos, currentDigestSize);
							current_pos += currentDigestSize;
							digestResultPos += currentDigestSize;
						}

						if (current_pos == data_length_with_message_digest) {
							if (digestResultPos != digestResult.length) {
								throw new PacketException("Invalid digest message !");
							}
							for (int i = 0; i < digestResult.length; i++) {
								if (digestResult[i] != digestResultTransmitted[i]) {
									throw new PacketException("Invalid data signature : pos=" + i
											+ ", signature length : " + digestResult.length + ", dataLength=" + length);
								}
							}
						}
					}
				}
				if (current_pos > data_length_with_message_digest)
					throw new PacketException("Invalid data coherence !");
			}
		} catch (PacketException e) {
			if (!isTemporaryInvalid())
				processInvalid();
			throw e;
		} catch (Exception e) {
			processInvalid();
			throw new PacketException(e);
		}
	}

	public boolean isValid() {
		return validity == Validity.VALID;
	}

	public boolean isTemporaryInvalid() {
		return validity == Validity.TEMPORARY_INVALID;
	}

	public boolean isInvalid() {
		return validity.compareTo(Validity.TEMPORARY_INVALID) >= 0;
	}

	private void processTemporaryInvalid() {
		if (validity == Validity.VALID)
			validity = Validity.TEMPORARY_INVALID;
		else
			validity = Validity.INVALID;
	}

	private void processValid() {
		if (validity == Validity.TEMPORARY_INVALID)
			validity = Validity.VALID;
	}

	private void processInvalid() {
		validity = Validity.INVALID;
	}

	public long getCurrentPosition() {
		return current_pos + start_position;
	}

	public boolean isFinished() {
		return current_pos == data_length_with_message_digest;
	}

	public final int getID() {
		return id_packet;
	}

	public long getWritedDataLength() {
		return Math.max(length - current_pos, length - start_position);
	}

	static abstract class AbstractByteTabInputStream {

		//abstract byte[] getBytesArray();
		abstract SubBlock getSubBlock();

		abstract int getRealDataSize();
	}

	static AbstractByteTabInputStream getByteTabInputStream(SubBlock subBlock, short random_values_size) {
		if (random_values_size == 0)
			return new ByteTabInputStream(subBlock);
		else
			return new ByteTabInputStreamWithRandomValues(subBlock);
	}

	static class ByteTabInputStream extends AbstractByteTabInputStream {
		private final SubBlock subBlock;

		ByteTabInputStream(SubBlock subBlock) {
			this.subBlock = subBlock;
		}

		@Override
		SubBlock getSubBlock()
		{
			return subBlock;
		}

		@Override
		int getRealDataSize() {
			return subBlock.getSize();
		}

	}

	static class ByteTabInputStreamWithRandomValues extends AbstractByteTabInputStream {
		private final SubBlock subBlock;
		private SubBlock subBlockRes = null;
		private int realSize = 0;

		ByteTabInputStreamWithRandomValues(SubBlock subBlock) {
			this.subBlock = subBlock;
		}

		@Override
		SubBlock getSubBlock() {
			if (subBlockRes == null) {
				byte tabRes[] = new byte[subBlock.getSize()];
				byte tab[] = subBlock.getBytes();
				int cursor = subBlock.getOffset();
				int shiftTabLength=subBlock.getOffset()+subBlock.getSize();
				int tabResCursor = 0;
				while (cursor < shiftTabLength) {
					byte nbrand = WritePacket.decodeLocalNumberRandomVal(tab[cursor++]);
					cursor += nbrand;
					if (cursor >= shiftTabLength)
						break;
					byte nextRandVals = tab[cursor++];
					int nextRandomValuesPos;
					if (nextRandVals == -1)
						nextRandomValuesPos = shiftTabLength;
					else
						nextRandomValuesPos = cursor + nextRandVals;
					if (cursor >= shiftTabLength)
						break;
					int size = Math.max(Math.min(nextRandomValuesPos - cursor, shiftTabLength - cursor), 0);
					if (size > 0)
						System.arraycopy(tab, cursor, tabRes, tabResCursor, size);
					cursor += size;
					tabResCursor += size;
				}

				realSize = tabResCursor;
				subBlockRes=new SubBlock(tabRes, 0, tabRes.length);
			}
			return subBlockRes;
		}

		@Override
		int getRealDataSize() {
			getSubBlock();
			return realSize;
		}
	}

}
