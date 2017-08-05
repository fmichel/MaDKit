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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.distrimind.util.Bits;
import com.distrimind.util.sizeof.ObjectSizer;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class DatagramData {
	private ByteBuffer data;

	DatagramData(DatagramLocalNetworkPresenceMessage message) throws IOException {
		if (message == null)
			throw new NullPointerException("message");
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			message.writeTo(baos);
			byte[] b = baos.toByteArray();
			int size = b.length;
			byte res[] = new byte[ObjectSizer.sizeOf(size) + size];
			Bits.putInt(res, 0, size);
			System.arraycopy(b, 0, res, ObjectSizer.sizeOf(size), size);
			data = ByteBuffer.wrap(res);
		}
	}

	DatagramData(byte[] data) {
		if (data == null)
			throw new NullPointerException("data");
		this.data = ByteBuffer.wrap(data);
	}

	DatagramData() {
		this.data = ByteBuffer.allocate(DatagramLocalNetworkPresenceMessage.getMaxDatagramMessageLength());
	}

	void put(byte[] data, int offset, int length) {
		if (this.data.remaining() < length) {
			ByteBuffer nd = ByteBuffer.allocate(this.data.position() + length);
			nd.put(this.data.array(), 0, this.data.position());
			this.data = nd;
		}
		this.data.put(data, offset, length);
	}

	ByteBuffer getByteBuffer() {
		return data;
	}

	DatagramData getNextDatagramData() {
		ByteBuffer next = getUnusedReceivedData();
		if (next != null) {
			DatagramData res = new DatagramData();
			res.put(next.array(), 0, next.capacity());
			return res;
		} else
			return null;
	}

	ByteBuffer getUnusedReceivedData() {
		if (isComplete() && isValid()) {
			ByteBuffer next = null;
			int length = data.getInt(0);
			length += ObjectSizer.sizeOf(length);
			int nLength = data.position() - length;
			if (nLength > 0) {
				next = ByteBuffer.allocate(nLength);
				next.put(data.array(), length, nLength);
			}
			return next;
		} else
			return null;

	}

	boolean isComplete() {
		int sizeInt = 0;
		sizeInt = ObjectSizer.sizeOf(sizeInt);
		if (data.position() < sizeInt)
			return false;
		else
			return data.position() >= data.getInt(0) + sizeInt;
	}

	DatagramLocalNetworkPresenceMessage getDatagramLocalNetworkPresenceMessage() throws IOException {
		if (isComplete() && isValid()) {
			int size = 0;
			return DatagramLocalNetworkPresenceMessage.readFrom(data.array(), ObjectSizer.sizeOf(size), data.getInt(0));
		} else
			throw new IOException("Invalid or incomplete buffer !");
	}

	boolean isValid() {
		int sizeInt = 0;
		sizeInt = ObjectSizer.sizeOf(sizeInt);

		return data.position() < sizeInt
				|| data.getInt(0) <= DatagramLocalNetworkPresenceMessage.getMaxDatagramMessageLength();
	}
}
