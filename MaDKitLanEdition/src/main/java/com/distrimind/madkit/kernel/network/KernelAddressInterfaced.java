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
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.util.AbstractDecentralizedID;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * This class represents a secured unique identifier for a distant MaDKit
 * kernel. If the original given {@link KernelAddress} is not secured, this last
 * is interfaced thanks to an encapsulated address. Then no hacker could pretend
 * usurp an identity. Uniqueness is guaranteed even when different kernels run
 * on the same JVM or over the network.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.0
 *
 */
public class KernelAddressInterfaced extends KernelAddress {
	private static final long serialVersionUID = -8597071860059314028L;

	private KernelAddress original_external_kernel_address;
	private AtomicBoolean interfaced;

	
	
	
	/**
	 * @param _original_kernel_address
	 *            the original kernel address to interface
	 * @throws NoSuchAlgorithmException 
	 *             if the used encryption algorithm does not exists
	 * @throws NoSuchProviderException if message digest provider was not found
	 */
	public KernelAddressInterfaced(KernelAddress _original_kernel_address) throws NoSuchAlgorithmException, NoSuchProviderException {
		this(_original_kernel_address, true);
	}

	/**
	 * @param _original_kernel_address
	 *            the original kernel address to eventually interface
	 * @param identical_from_original_kernel_interface
	 *            true if the original kernel address do not need to be interfaced.
	 * @throws NoSuchAlgorithmException
	 *             if the used encryption algorithm does not exists
	 * @throws NoSuchProviderException if message digest provider was not found
	 * 				
	 */
	public KernelAddressInterfaced(KernelAddress _original_kernel_address,
			boolean identical_from_original_kernel_interface) throws NoSuchAlgorithmException, NoSuchProviderException {
		super(false, false);
		if (_original_kernel_address == null)
			throw new NullPointerException("_original_kernel_address");
		original_external_kernel_address = _original_kernel_address;
		interfaced = new AtomicBoolean(!identical_from_original_kernel_interface);
		initName();
		internalSize+=original_external_kernel_address.getInternalSerializedSize()+1;
	}

	private KernelAddressInterfaced(KernelAddressInterfaced toClone) {
		super(toClone.id, false);
		original_external_kernel_address = toClone.original_external_kernel_address.clone();
		interfaced = new AtomicBoolean(toClone.interfaced.get());
		initName();
		internalSize+=original_external_kernel_address.getInternalSerializedSize()+1;
	}

	

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			internalSize=in.readShort();
			if (internalSize<128 || internalSize>513)
				throw new MessageSerializationException(Integrity.FAIL);
			byte[] tab=new byte[internalSize];
			if (internalSize!=in.read(tab))
				throw new IOException();
			try
			{
				id=AbstractDecentralizedID.instanceOf(tab);
			}
			catch(Throwable t)
			{
				throw new IOException(t);
			}
			if (id==null)
				throw new IOException();
			++internalSize;
			initName();
			
			Object o=in.readObject();
			if (!(o instanceof KernelAddress))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			original_external_kernel_address=(KernelAddress)o;
			internalSize+=original_external_kernel_address.getInternalSerializedSize();
			interfaced=new AtomicBoolean(in.readBoolean());
				
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	private void writeObject(ObjectOutputStream oos) throws IOException {
		byte[] tab=id.getBytes();
		oos.writeShort(tab.length);
		oos.write(tab);
		
		oos.writeObject(original_external_kernel_address);
		oos.writeBoolean(interfaced.get());
	}
	/**
	 * 
	 * @param identical_from_original_kernel_interface
	 *            true if the original kernel address do not need to be interfaced.
	 */
	void setInterface(boolean identical_from_original_kernel_interface) {
		if (interfaced.getAndSet(!identical_from_original_kernel_interface) == identical_from_original_kernel_interface)
			initName();
	}

	/**
	 * 
	 * @return true if the original kernel address is interfaced through a
	 *         artificial kernel address
	 */
	public boolean isInterfaced() {
		return interfaced.get();
	}

	/**
	 * 
	 * @return the original kernel address
	 */
	public KernelAddress getOriginalKernelAddress() {
		return original_external_kernel_address;
	}

	@Override
	public int hashCode() {
		return original_external_kernel_address.hashCode();
	}

	@Override
	public AbstractDecentralizedID getAbstractDecentralizedID() {
		if (interfaced.get())
			return super.getAbstractDecentralizedID();
		else
			return original_external_kernel_address.getAbstractDecentralizedID();
	}

	/*
	 * @Override public boolean equals(Object o) { if (o==null) return false; if
	 * (o==this) return true;
	 * 
	 * if (o instanceof KernelAddress) { KernelAddress
	 * ka_to_compare=(KernelAddress)o; return
	 * ka_to_compare.getAbstractDecentralizedID().equals(this.
	 * getAbstractDecentralizedID()); } else return false;
	 * 
	 * }
	 */

	@Override
	public KernelAddressInterfaced clone() {
		return new KernelAddressInterfaced(this);
	}

}
