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
package com.distrimind.madkit.io;

import java.io.IOException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class RandomByteArrayOutputStream extends RandomOutputStream {
	private byte[] bytes;
	private int current_pos;

	public RandomByteArrayOutputStream(byte[] _bytes) {
		bytes = _bytes;
		current_pos = 0;
	}

	public RandomByteArrayOutputStream() {
		bytes = new byte[0];
		current_pos = 0;
	}

	public RandomByteArrayOutputStream(int length) {
		if (length < 0)
			throw new IllegalArgumentException("length must can't be negative");
		bytes = new byte[length];
		current_pos = 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void write(int b) throws IOException {
		if (current_pos == -1)
			throw new IOException("The current RandomByteArrayOutputStream is closed !");
		ensureLength(current_pos + 1);
		bytes[current_pos++] = (byte) b;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void write(byte[] _bytes) throws IOException {
		write(_bytes, 0, _bytes.length);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void write(byte[] _bytes, int _offset, int _length) throws IOException {
		if (current_pos == -1)
			throw new IOException("The current RandomByteArrayOutputStream is closed !");
		if (_bytes == null)
			throw new NullPointerException("_bytes");
		if (_length > _bytes.length)
			throw new IllegalArgumentException("_length must be greater than _bytes.length !");
		ensureLength(current_pos + _length);
		System.arraycopy(_bytes, _offset, bytes, current_pos, _length);
		current_pos += _length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public long length() throws IOException {
		if (current_pos == -1)
			throw new IOException("The current RandomByteArrayOutputStream is closed !");
		return bytes.length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void setLength(long _length) throws IOException {
		if (current_pos == -1)
			throw new IOException("The current RandomByteArrayOutputStream is closed !");

		if (_length < 0 || _length > (long) Integer.MAX_VALUE)
			throw new IllegalArgumentException("invalid length : " + _length);

		int length = (int) _length;
		if (length == bytes.length)
			return;
		byte[] prev = bytes;
		bytes = new byte[length];
		if (prev.length != 0 && length != 0) {
			System.arraycopy(prev, 0, bytes, 0, Math.min(prev.length, length));
		}

		current_pos = Math.min(bytes.length, current_pos);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void seek(long _pos) throws IOException {
		if (current_pos == -1)
			throw new IOException("The current RandomByteArrayOutputStream is closed !");
		ensureLength(_pos + 1);
		current_pos = (int) _pos;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public long currentPosition() {
		return current_pos;
	}

	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void close() {
		current_pos = -1;
	}

}
