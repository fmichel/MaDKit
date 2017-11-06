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

import com.distrimind.util.Bits;

/**
 * Represent the head the a {@link PacketPart}
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see PacketPart
 */
final class PacketPartHead {
	public static final byte TYPE_PACKET = 0b001;
	public static final byte TYPE_PACKET_HEAD = 0b011;
	public static final byte TYPE_PACKET_REDOWNLOADED = 0b111;
	static final byte TYPE_PACKET_LAST = 0b1001;

	private static String getTypePacket(byte type) {
		String res = "UNKONW";
		if ((type & TYPE_PACKET) == TYPE_PACKET)
			res = "PACKET";
		if ((type & TYPE_PACKET_HEAD) == TYPE_PACKET_HEAD)
			res = res + ",HEAD";
		if ((type & TYPE_PACKET_REDOWNLOADED) == TYPE_PACKET_REDOWNLOADED)
			res = res + ",REDOWNLOADED";
		if ((type & TYPE_PACKET_LAST) == TYPE_PACKET_LAST)
			res = res + ",LAST";
		return res;
	}

	private final byte type;
	private final int id;
	private final int data_size;

	private final long start_position;
	private final long total_length;

	PacketPartHead(byte[] _part) {
		type = _part[0];
		id = Bits.getInt(_part, 1);
		data_size = Block.getShortInt(_part, 5);
		if (isFirstPacketPart()) {
			total_length = Bits.getLong(_part, 8);
			start_position = Bits.getLong(_part, 16);
		} else {
			total_length = -1;
			start_position = -1;
		}
	}

	PacketPartHead(byte type, int id, int data_size, long total_length, long start_position) {
		this.type = type;
		this.id = id;
		this.data_size = data_size;
		if (isFirstPacketPart()) {
			this.start_position = start_position;
			this.total_length = total_length;
		} else {
			this.start_position = -1;
			this.total_length = -1;
		}
	}

	@Override
	public String toString() {
		if (isFirstPacketPart())
			return "PacketPartHead[type=" + getTypePacket(type) + ", id=" + id + ", dataSize" + data_size
					+ ", startPos=" + start_position + ", totalLength=" + total_length + "]";
		else
			return "PacketPartHead[type=" + getTypePacket(type) + ", id=" + id + ", dataSize" + data_size + "]";
	}

	public int getDataSize() {
		return data_size;
	}

	public boolean isPacketPart() {
		return (type & TYPE_PACKET) == TYPE_PACKET;
	}

	public boolean isFirstPacketPart() {
		return (type & TYPE_PACKET_HEAD) == TYPE_PACKET_HEAD;
	}

	public boolean isRedownloadedPacketPart() {
		return (type & TYPE_PACKET_REDOWNLOADED) == TYPE_PACKET_REDOWNLOADED;
	}

	public boolean isLastPacket() {
		return (type & TYPE_PACKET_LAST) == TYPE_PACKET_LAST;
	}

	public int getID() {
		return id;
	}

	public byte getType() {
		return type;
	}

	public long getTotalLength() {
		return total_length;
	}

	public long getStartPosition() {
		return start_position;
	}

	public int getHeadSize() {
		return getHeadSize(isFirstPacketPart());
	}

	public static int getHeadSize(boolean first_packet) {
		if (first_packet)
			return 24;
		else
			return 8;
	}

}
