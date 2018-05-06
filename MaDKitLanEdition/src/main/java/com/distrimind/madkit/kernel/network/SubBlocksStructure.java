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

import java.security.SecureRandom;
import java.util.Iterator;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class SubBlocksStructure {
	final int sub_block_sizes[];
	// final int sub_block_sizes_for_parent[];
	final int sub_block_offsets[];
	public final int initial_packet_offset;
	public final int initial_packet_size;
	final boolean need_random = false;
	final int block_content_size;
	final int block_size;

	public SubBlocksStructure(PacketPart _packet, ConnectionProtocol<?> connection_protocol) throws NIOException {
		int size = _packet.getBytes().length;
		int inipacketsize = -1;
		int offsets[] = new int[connection_protocol.sizeOfSubConnectionProtocols() + 1];
		sub_block_sizes = new int[offsets.length];
		// sub_block_sizes_for_parent=new int[offsets.length];
		int i = offsets.length - 1;
		for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext(); i--) {
			ConnectionProtocol<?> cp = it.next();
			SubBlockParser sbp = cp.getParser();

			try {
				/*
				 * if (sbp.getSizeBlockModulus()<1) throw new BlockParserException("The parser "
				 * +sbp+" returns a block modulus lower than 1: "+sbp.getSizeBlockModulus());
				 */

				int headSize = sbp.getSizeHead();
				if (headSize < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns a head size lower than 0: " + sbp.getSizeHead());

				int outputSize = sbp.getBodyOutputSizeForEncryption(size);
				if (outputSize < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns an ouput size lower than 0: " + outputSize);

				/*
				 * int mod=size%sbp.getSizeBlockModulus(); if (mod!=0)
				 * size=size+(sbp.getSizeBlockModulus()-size%sbp.getSizeBlockModulus());
				 */
				if (inipacketsize == -1)
					inipacketsize = size;
				/*
				 * else { sub_block_sizes_for_parent[i+1]=size; }
				 */
				size = outputSize + headSize;

				// size+=sbp.getSizeHead();

				offsets[i] = headSize;
				sub_block_sizes[i] = size;
			} catch (BlockParserException e) {
				throw new NIOException(e);
			}
		}

		// sub_block_sizes_for_parent[0]=sub_block_sizes[0]+Block.getHeadSize();
		initial_packet_size = inipacketsize;
		block_content_size = size;

		sub_block_offsets = new int[offsets.length];
		int offset = Block.getHeadSize();
		sub_block_offsets[0] = offset;
		for (i = 1; i < offsets.length; i++) {
			offset += offsets[i - 1];
			sub_block_offsets[i] = offset;
		}
		initial_packet_offset = offset + offsets[offsets.length - 1];
		block_size = block_content_size + Block.getHeadSize();
	}

	public SubBlocksStructure(Block _block, ConnectionProtocol<?> connection_protocol) throws NIOException {
		int size = block_content_size = (block_size = _block.getBlockSize()) - Block.getHeadSize();
		if (size <= 0)
			throw new NIOException("Invalid block (too little size)");
		int offset = Block.getHeadSize();
		sub_block_sizes = new int[connection_protocol.sizeOfSubConnectionProtocols() + 1];
		sub_block_offsets = new int[sub_block_sizes.length];
		// sub_block_sizes_for_parent=new int[sub_block_sizes.length];
		// sub_block_sizes_for_parent[0]=_block.getBlockSize();
		int i = 0;
		for (Iterator<ConnectionProtocol<?>> it = connection_protocol.iterator(); it.hasNext(); i++) {
			ConnectionProtocol<?> cp = it.next();
			SubBlockParser sbp = cp.getParser();
			int sizeHead = -1;
			/*
			 * if (i<sub_block_sizes_for_parent.length-1)
			 * sub_block_sizes_for_parent[i+1]=size;
			 */
			try {
				/*
				 * if (sbp.getSizeBlockModulus()<1) throw new BlockParserException("The parser "
				 * +sbp+" returns a block modulus lower than 1: "+sbp.getSizeBlockModulus());
				 */
				sizeHead = sbp.getSizeHead();
				if (sizeHead < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns a head size lower than 0: " + sbp.getSizeHead());

				/*
				 * int s=size-sbp.getSizeHead(); s=s-s%sbp.getSizeBlockModulus();
				 * size=s+sbp.getSizeHead();
				 */
				if (size <= 0 || offset > _block.getBlockSize() || _block.getBlockSize() - offset < size)
					throw new BlockParserException("Invalid block");
			} catch (BlockParserException e) {
				throw new NIOException(e);
			}

			sub_block_sizes[i] = size;
			sub_block_offsets[i] = offset;

			try {
				size = sbp.getBodyOutputSizeForDecryption(size - sizeHead);
				offset += sizeHead;
				if (size <= 0)
					throw new BlockParserException("Invalid block");
				if (offset > _block.getBlockSize())
					throw new BlockParserException("Invalid block");
				if (_block.getBlockSize() - offset < size)
					throw new BlockParserException("Invalid block");

			} catch (BlockParserException e) {
				throw new NIOException(e);
			}
		}

		initial_packet_offset = offset;
		initial_packet_size = size;
	}

	public SubBlock getSubBlockForParent(SubBlock _block, int sub_block_index, SecureRandom rand)
			throws BlockParserException {
		if (sub_block_index < 0 || sub_block_index >= sub_block_sizes.length)
			throw new BlockParserException(new ArrayIndexOutOfBoundsException("sub_block_index is invalid"));
		if (_block.getBytes().length != block_size)
			throw new BlockParserException("Invalid block size");
		if (_block.getOffset() != sub_block_offsets[sub_block_index])
			throw new BlockParserException("Invalid block offset (found=" + _block.getOffset() + ", expected="
					+ sub_block_offsets[sub_block_index] + ")");
		if (_block.getSize() != sub_block_sizes[sub_block_index])
			throw new BlockParserException("Invalid sub block size (found=" + _block.getSize() + ", expected="
					+ sub_block_sizes[sub_block_index] + ")");

		return _block;
	}

	public SubBlock getSubBlockForChild(SubBlock _block, int sub_block_index) throws BlockParserException {
		if (sub_block_index < 0 || sub_block_index >= sub_block_sizes.length)
			throw new BlockParserException(new ArrayIndexOutOfBoundsException("sub_block_index is invalid"));

		if (_block.getBytes().length != block_size)
			throw new BlockParserException("Invalid block");
		if (sub_block_index == sub_block_sizes.length - 1) {
			if (_block.getOffset() != initial_packet_offset || _block.getSize() > initial_packet_size)
				throw new BlockParserException("Invalid block");
			return _block;
		} else {
			++sub_block_index;
			if (_block.getOffset() != sub_block_offsets[sub_block_index]
					|| _block.getSize() > sub_block_sizes[sub_block_index])
				throw new BlockParserException("Invalid block");
			return _block;
			/*
			 * if (_block.getSize()==sub_block_sizes[sub_block_index]) return _block; else
			 * return new SubBlock(_block.getBytes(), sub_block_offsets[sub_block_index],
			 * sub_block_sizes[sub_block_index]);
			 */
		}
	}

	public static int getAbsoluteMaximumBufferSize(ConnectionProtocol<?> connection_protocol) throws NIOException {
		int size = Block.BLOCK_SIZE_LIMIT - Block.getHeadSize() - PacketPartHead.getHeadSize(true);
		for (Iterator<ConnectionProtocol<?>> it = connection_protocol.iterator(); it.hasNext();) {
			ConnectionProtocol<?> cp = it.next();
			SubBlockParser sbp = cp.getParser();
			try {
				/*
				 * if (sbp.getSizeBlockModulus()<1) throw new BlockParserException("The parser "
				 * +sbp+" returns a block modulus lower than 1: "+sbp.getSizeBlockModulus());
				 */
				if (sbp.getMaximumSizeHead() < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns a head size lower than 0: " + sbp.getMaximumSizeHead());
				size -= sbp.getMaximumSizeHead();

				// size=size-size%sbp.getSizeBlockModulus();
			} catch (BlockParserException e) {
				throw new NIOException(e);
			}
		}
		return Math.min(size, Block.BLOCK_SIZE_LIMIT);
	}

	public static int getAbsoluteMaximumBlockSize(ConnectionProtocol<?> connection_protocol, int max_buffer_size, short random_values_size)
			throws NIOException {
		int size = PacketPartHead.getMaxOutputSize(max_buffer_size, PacketPartHead.getHeadSize(true), random_values_size);

		for (Iterator<ConnectionProtocol<?>> it = connection_protocol.reverseIterator(); it.hasNext();) {
			ConnectionProtocol<?> cp = it.next();
			SubBlockParser sbp = cp.getParser();

			try {
				/*
				 * if (sbp.getSizeBlockModulus()<1) throw new BlockParserException("The parser "
				 * +sbp+" returns a block modulus lower than 1: "+sbp.getSizeBlockModulus());
				 */
				if (sbp.getMaximumSizeHead() < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns a head size lower than 0: " + sbp.getMaximumSizeHead());
				
				int outputSize = sbp.getMaximumBodyOutputSizeForEncryption(size);
				if (outputSize < 0)
					throw new BlockParserException(
							"The parser " + sbp + " returns an ouput size lower than 0: " + outputSize);
				size = outputSize + sbp.getMaximumSizeHead();

				/*
				 * int mod=size%sbp.getSizeBlockModulus(); if (mod!=0)
				 * size=size+(sbp.getSizeBlockModulus()-size%sbp.getSizeBlockModulus());
				 * 
				 * size+=sbp.getSizeHead();
				 */
			} catch (BlockParserException e) {
				throw new NIOException(e);
			}
		}
		return size + Block.getHeadSize();
	}
}
