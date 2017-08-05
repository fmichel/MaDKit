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

import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.util.Bits;

/**
 * Represent a data block, potentially encrypted
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public final class Block {
	static final int BLOCK_SIZE_LIMIT = 0xFFFF;

	private final byte[] block;
	private int transfert_type;

	public Block(PacketPart _packet_part, SubBlocksStructure _structure, int _transfert_type) throws PacketException {
		int size = _structure.block_size;
		if (size > BLOCK_SIZE_LIMIT)
			throw new PacketException(
					"This block has a size (" + size + ") greater than the size limit : " + BLOCK_SIZE_LIMIT);
		block = new byte[size];
		transfert_type = _transfert_type;
		Bits.putShort(block, 0, (short) block.length);
		Bits.putInt(block, 2, transfert_type);
		try {
			System.arraycopy(_packet_part.getBytes(), 0, block, _structure.initial_packet_offset,
					_packet_part.getBytes().length);
		} catch (Exception e) {
			throw new PacketException(e);
		}
	}

	public Block(byte _block[], SubBlocksStructure _structure, int _transfert_type) throws PacketException {
		int size = _structure.block_size;
		if (size > BLOCK_SIZE_LIMIT)
			throw new PacketException(
					"This block has a size (" + size + ") greater than the size limit : " + BLOCK_SIZE_LIMIT);
		block = _block;
		transfert_type = _transfert_type;
		Bits.putShort(block, 0, (short) block.length);
		Bits.putInt(block, 2, transfert_type);
	}

	public Block(byte _block[]) throws PacketException {
		block = _block;
		if (block.length < getHeadSize())
			throw new PacketException(
					"the size _block.length (" + block.length + ") must be greater than getHeadSize()");
		if (block.length > getMaximumBlockSize())
			throw new PacketException(
					"the size _block.length (" + block.length + ") must be lower or equal than getMaximumBlockSize()");
		int size = getBlockSize(block, 0);
		if (size > BLOCK_SIZE_LIMIT)
			throw new PacketException(
					"This block has a size (" + size + ") greater than the size limit : " + BLOCK_SIZE_LIMIT);
		if (size != _block.length)
			throw new PacketException(
					"The given block as an invalid size (readed: " + size + "; block size: " + _block.length + ")");
		transfert_type = Bits.getInt(block, 2);
	}

	public Block(int block_size, int _transfert_type) throws PacketException {
		if (block_size > BLOCK_SIZE_LIMIT)
			throw new PacketException(
					"This block has a size (" + block_size + ") greater than the size limit : " + BLOCK_SIZE_LIMIT);
		if (block_size <= getHeadSize() || block_size > getMaximumBlockSize())
			throw new PacketException(
					"block_size must be greater than getHeadSize() and lower or equal than getMaximumBlockSize()");
		block = new byte[block_size];
		Bits.putShort(block, 0, (short) block.length);
		Bits.putInt(block, 2, _transfert_type);
		transfert_type = _transfert_type;
	}

	public byte[] getBytes() {
		return block;
	}

	public static int getHeadSize() {
		return 6;
	}

	public boolean isDirect() {
		return TransferAgent.NullIDTransfer.equals(transfert_type);
	}

	public boolean isValid() {
		return transfert_type > -2;
	}

	public int getTransferID() {
		return transfert_type;
	}

	public void setTransfertID(int _id) {
		transfert_type = _id;
		Bits.putInt(block, 2, _id);
	}

	public int getBlockSize() {
		return block.length;
	}

	public static int getBlockSize(byte[] _bytes, int offset) {
		short s = Bits.getShort(_bytes, offset);
		return s & 0xFFFF;
	}

	public static int getMaximumBlockSize() {
		return 0xFFFF;
	}

	public static int getMaximumBlockContentSize() {
		return getMaximumBlockSize() - getHeadSize();
	}
}
