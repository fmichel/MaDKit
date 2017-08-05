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
public class RandomByteArrayInputStream extends RandomInputStream {
	private byte[] bytes;
	private int current_pos;

	public RandomByteArrayInputStream(byte[] _bytes) {
		if (_bytes == null)
			throw new NullPointerException("_bytes");
		bytes = _bytes;
		current_pos = 0;
	}

	private int getFreeSpace() {
		return bytes.length - current_pos;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public int read() throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");
		if (current_pos >= bytes.length)
			return -1;
		else {
			return bytes[current_pos++];
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public int read(byte[] _bytes) throws IOException {
		return read(_bytes, 0, _bytes.length);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public int read(byte[] _bytes, int _offset, int _length) throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");
		int readed = Math.min(Math.min(_length, getFreeSpace()), _bytes.length - _offset);
		System.arraycopy(bytes, current_pos, _bytes, _offset, readed);
		current_pos += readed;
		return readed;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public long length() throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");

		return bytes.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seek(long _pos) throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");
		if (_pos >= (long) bytes.length || _pos < 0)
			throw new IOException("The given position (" + _pos + ") is invalid. Attempted a position between 0 and "
					+ bytes.length + " excluded.");
		current_pos = (int) _pos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long _nb) throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");
		long skipped = Math.min(getFreeSpace(), _nb);
		current_pos += skipped;
		return skipped;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long currentPosition() throws IOException {
		if (bytes == null)
			throw new IOException("The current RandomByteArrayInputStream is closed !");
		return current_pos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		bytes = null;
		current_pos = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() {
		return getFreeSpace();
	}

	public byte[] getBytes() {
		return bytes;
	}

}
