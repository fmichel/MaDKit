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

import java.net.InetSocketAddress;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class TransferConfirmationSystemMessage extends BroadcastableSystemMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4947644670603624411L;

	private IDTransfer yourIDTransfer;
	private IDTransfer myIDTransfer;
	// private final TransferedBlockChecker transferBlockChercker;
	private final int numberOfSubBlocks;
	private final KernelAddress kernelAddressToConnect;
	private final boolean middleReached;
	private final InetSocketAddress distantInetSocketAddress;

	TransferConfirmationSystemMessage(IDTransfer idTransferDestinationUsedForBroadcast,
			KernelAddress kernelAddressDestination, KernelAddress kernelAddressToConnect, IDTransfer yourIDTransfer,
			IDTransfer myIDTransfer, int numberOfSubBlocks, boolean middleReached,
			InetSocketAddress distantInetSocketAddress) {
		super(idTransferDestinationUsedForBroadcast, kernelAddressDestination);
		if (yourIDTransfer == null)
			throw new NullPointerException("null");
		if (myIDTransfer == null)
			throw new NullPointerException("myIDTransfer");
		/*
		 * if (transferBlockChercker==null) throw new
		 * NullPointerException("transferBlockChercker");
		 */
		if (kernelAddressToConnect == null)
			throw new NullPointerException("kernelAddressToConnect");
		if (numberOfSubBlocks < 0)
			throw new IllegalArgumentException();
		this.yourIDTransfer = yourIDTransfer;
		this.myIDTransfer = myIDTransfer;
		// this.transferBlockChercker=transferBlockChercker;
		this.kernelAddressToConnect = kernelAddressToConnect;
		this.numberOfSubBlocks = numberOfSubBlocks;
		this.middleReached = middleReached;
		this.distantInetSocketAddress = distantInetSocketAddress;
	}

	public InetSocketAddress getDistantInetAddress() {
		return distantInetSocketAddress;
	}

	boolean isMiddleReached() {
		return middleReached;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[idTransferDestination=" + getIdTransferDestination()
				+ ", kernelAddressDestination=" + getIdTransferDestination() + ", yourIDTransfer=" + yourIDTransfer
				+ ", myIDTransfer=" + myIDTransfer + ", kernelAddressToConnect=" + kernelAddressToConnect
				+ ", numOfSubBlocks=" + numberOfSubBlocks + ", middle reached=" + middleReached + "]";
	}

	KernelAddress getKernelAddressToConnect() {
		return kernelAddressToConnect;
	}

	IDTransfer getYourIDTransfer() {
		return yourIDTransfer;
	}

	IDTransfer getMyIDTransfer() {
		return myIDTransfer;
	}

	/*
	 * TransferedBlockChecker getTransferBlockChercker() { return
	 * transferBlockChercker; }
	 */

	int getNumberOfSubBlocks() {
		return numberOfSubBlocks;
	}

	@Override
	public Integrity checkDataIntegrity() {
		Integrity i = super.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;

		if (yourIDTransfer == null)
			return Integrity.FAIL;
		i = yourIDTransfer.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;
		if (myIDTransfer == null)
			return Integrity.FAIL;
		i = myIDTransfer.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;
		if (numberOfSubBlocks < 0)
			return Integrity.FAIL;
		/*
		 * if (transferBlockChercker==null) return Integrity.FAIL;
		 * i=transferBlockChercker.checkDataIntegrity();
		 */
		if (i != Integrity.OK)
			return i;

		if (kernelAddressToConnect == null)
			return Integrity.FAIL;
		i = kernelAddressToConnect.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;

		return Integrity.OK;
	}

}
