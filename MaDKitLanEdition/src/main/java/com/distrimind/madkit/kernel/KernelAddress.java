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
package com.distrimind.madkit.kernel;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

import com.distrimind.madkit.kernel.network.KernelAddressInterfaced;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.util.AbstractDecentralizedID;
import com.distrimind.util.RenforcedDecentralizedIDGenerator;
import com.distrimind.util.SecuredDecentralizedID;
import com.distrimind.util.crypto.SecureRandomType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * This class represents a unique identifier for MaDKit kernel. Uniqueness is
 * guaranteed even when different kernels run on the same JVM or over the
 * network.
 * 
 * @author Oliver Gutknecht
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 5.6
 * @since MaDKit 1.0
 * @since MaDKitLanEdition 1.0
 *
 */
public class KernelAddress implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8558944358948665656L;

	protected final AbstractDecentralizedID id;

	private transient String name;


	/**
	 * Avoid the default public visibility for denying usage.
	 * 
	 * @param isSecured if the kernel address must be secured
	 * @throws NoSuchAlgorithmException
	 *             if the used encryption algorithm does not exists
	 * @throws NoSuchProviderException if the provider use to hash kernel address was not found
	 */
	protected KernelAddress(boolean isSecured) throws NoSuchAlgorithmException, NoSuchProviderException {
		this(isSecured, true);
	}

	protected KernelAddress(boolean isSecured, boolean initName) throws NoSuchAlgorithmException, NoSuchProviderException {

		RenforcedDecentralizedIDGenerator generatedid = new RenforcedDecentralizedIDGenerator();
		if (isSecured) {
			id = new SecuredDecentralizedID(generatedid, SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED.getInstance(null));
		} else
			id = generatedid;

		if (initName)
			initName();
		else
			name = null;
	}

	protected void initName() {
		name = getKernelName();
	}

	protected KernelAddress(AbstractDecentralizedID id, boolean initName) {
		if (id == null)
			throw new NullPointerException("id");
		this.id = id;

		if (initName)
			initName();
		else
			name = null;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			in.defaultReadObject();
			if (!(this instanceof KernelAddressInterfaced))
				initName();
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private String getKernelName() {
		return "@" + Madkit.getVersion().getShortProgramName() + "-" + getNetworkID();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof KernelAddress) {
			KernelAddress ka_to_compare = (KernelAddress) o;
			return ka_to_compare.getAbstractDecentralizedID().equals(this.getAbstractDecentralizedID());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String getNetworkID() {
		return getHexString(getAbstractDecentralizedID().getBytes());
	}

	private static String getHexString(byte[] bytes) {
		return Base64.encodeBase64URLSafeString(bytes);
	}

	public AbstractDecentralizedID getAbstractDecentralizedID() {
		return id;
	}

	/**
	 * Returns a simplified string representation for this platform address
	 * 
	 * @return a string representation for this platform address
	 */
	@Override
	public String toString() {
		return name;
	}

	public Integrity checkDataIntegrity() {
		try {
			if (id == null)
				return Integrity.FAIL;
			if (id.getBytes() == null)
				return Integrity.FAIL;
			if (!id.equals(id))
				return Integrity.FAIL;
			if (getAbstractDecentralizedID() == null)
				return Integrity.FAIL;
			if (getAbstractDecentralizedID().getBytes() == null)
				return Integrity.FAIL;
			if (!getAbstractDecentralizedID().equals(getAbstractDecentralizedID()))
				return Integrity.FAIL;
			return Integrity.OK;
		} catch (Exception e) {
			return Integrity.FAIL;
		}
	}

	@Override
	public KernelAddress clone() {
		return this;
	}

}
