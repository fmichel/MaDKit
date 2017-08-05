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

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.distrimind.madkit.exceptions.PacketException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class DataSocketSynchronizer {
	private byte buffer[] = null;
	private int cursor_buffer = 0;
	private final AtomicBoolean dataInProgress = new AtomicBoolean(false);

	void receiveData(byte[] _bytes, SocketAgentInterface socketAgentInterface) {
		if (!dataInProgress.compareAndSet(false, true))
			throw new ConcurrentModificationException();

		try {
			if (_bytes == null)
				return;
			if (buffer != null && buffer.length < Block.getHeadSize()) {
				byte[] b = new byte[buffer.length + _bytes.length];
				System.arraycopy(buffer, 0, b, 0, buffer.length);
				System.arraycopy(_bytes, 0, b, buffer.length, _bytes.length);
				if (b.length < Block.getHeadSize()) {
					buffer = b;
					return;
				} else {
					_bytes = b;
					buffer = null;
				}
			}

			int cursor_array = 0;

			while (cursor_array < _bytes.length) {

				if (buffer == null) {
					int l = _bytes.length - cursor_array;
					if (l >= Block.getHeadSize()) {
						int size = Block.getBlockSize(_bytes, cursor_array);
						if (size < Block.getHeadSize()) {
							socketAgentInterface
									.processInvalidBlock(
											new PacketException("the block size (" + size
													+ ") must be greater than getHeadSize(). data_size=" + _bytes.length
													+ " cursor_array=" + cursor_array + " bufferLength=null"),
											null, false);
							return;
						}
						buffer = new byte[size];
						cursor_buffer = 0;
					} else {
						if (l > 0) {
							buffer = new byte[l];
							cursor_buffer = 0;
							System.arraycopy(_bytes, cursor_array, buffer, 0, l);
						}
						return;
					}

				}
				int length = Math.min(buffer.length - cursor_buffer, _bytes.length - cursor_array);
				System.arraycopy(_bytes, cursor_array, buffer, cursor_buffer, length);
				cursor_buffer += length;
				cursor_array += length;
				if (cursor_buffer == buffer.length) {
					try {
						Block block = new Block(buffer);
						socketAgentInterface.receivedBlock(block);
					} catch (PacketException e) {
						socketAgentInterface.processInvalidBlock(e, null, false);
					} finally {
						buffer = null;
					}
					if (socketAgentInterface.isBannedOrExpulsed())
						return;
				}
			}
		} finally {
			dataInProgress.set(false);
		}
	}

	static interface SocketAgentInterface {
		public void receivedBlock(Block _block);

		public boolean processInvalidBlock(Exception _e, Block _block, boolean _candidate_to_ban);

		public boolean isBannedOrExpulsed();

	}

}
