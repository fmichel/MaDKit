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
package com.distrimind.madkit.kernel.network.connection.access;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.util.AbstractDecentralizedID;
import com.distrimind.util.RenforcedDecentralizedIDGenerator;
import com.distrimind.util.SecuredDecentralizedID;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SecureRandomType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * This identifier is related to a machine, or more precisely to an specific
 * instance of the program.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * @see Identifier
 * @see CloudIdentifier
 */
public abstract class HostIdentifier implements ExternalizableAndSizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1532662660365201854L;
	protected static final AbstractSecureRandom random;

	static {
		AbstractSecureRandom rand = null;
		try {
			rand = SecureRandomType.BC_FIPS_APPROVED.getSingleton(null);
		} catch (Exception e) {
			System.err.println("Unexpected error :");
			e.printStackTrace();
			System.exit(-1);
		}
		random = rand;
	}

	@Override
	public abstract boolean equals(Object _object);

	@Override
	public abstract int hashCode();

	/**
	 * Generates a unique host identifier
	 * 
	 * @return a unique host identifier
	 * @throws NoSuchAlgorithmException
	 *             if the used encryption algorithm was not found
	 * @throws NoSuchProviderException  if a problem occurs
	 */
	public static HostIdentifier generateDefaultHostIdentifier() throws NoSuchAlgorithmException, NoSuchProviderException {
		return new DefaultHostIdentifier();
	}

	public HostIdentifier getDefaultHostIdentifier(byte[] bytes) {
		return getDefaultHostIdentifier(bytes, 0, bytes.length);
	}

	public static HostIdentifier getDefaultHostIdentifier(byte[] bytes, int off, int len) {
		return new DefaultHostIdentifier(bytes, off, len);
	}

	private static class DefaultHostIdentifier extends HostIdentifier {
		private static final long serialVersionUID = -1122797789837718737L;
		private SecuredDecentralizedID id;

		public DefaultHostIdentifier() throws NoSuchAlgorithmException, NoSuchProviderException {
			synchronized (random) {
				id = new SecuredDecentralizedID(new RenforcedDecentralizedIDGenerator(), random);
			}
		}

		DefaultHostIdentifier(byte[] bytes, int off, int len) {
			id = (SecuredDecentralizedID) AbstractDecentralizedID.instanceOf(bytes, off, len);
		}

		@Override
		public boolean equals(Object _host_identifier) {
			if (_host_identifier == null)
				return false;
			if (_host_identifier == this)
				return true;
			if (_host_identifier instanceof DefaultHostIdentifier) {
				DefaultHostIdentifier dhi = (DefaultHostIdentifier) _host_identifier;
				return id.equals(dhi.id);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
		@Override
		public int getInternalSerializedSize() {
			return SerializationTools.getInternalSize(id, 0);
		}

		@Override
		public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
		{
			AbstractDecentralizedID a=SerializationTools.readDecentralizedID(in, false);
			if (!(a instanceof SecuredDecentralizedID))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			id=(SecuredDecentralizedID)a;
		}
		@Override
		public void writeExternal(final ObjectOutput oos) throws IOException
		{
			
			SerializationTools.writeDecentralizedID(oos, id, false);
		}
		
	}

}
