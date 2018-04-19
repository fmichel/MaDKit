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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.kernel.network.connection.PointToPointTransferedBlockChecker;
import com.distrimind.madkit.util.OOSUtils;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
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
	private int numberOfSubBlocks;
	private KernelAddress kernelAddressToConnect;
	private boolean middleReached;
	private InetSocketAddress distantInetSocketAddress;
	private PointToPointTransferedBlockChecker pointToPointBlockChecker;

	@Override
	public int getInternalSerializedSize() {
		
		return super.getInternalSerializedSize()+yourIDTransfer.getInternalSerializedSize()+myIDTransfer.getInternalSerializedSize()+5+kernelAddressToConnect.getInternalSerializedSize()+OOSUtils.getInternalSize(distantInetSocketAddress, 0)+(pointToPointBlockChecker==null?1:pointToPointBlockChecker.getInternalSerializedSize());
	}


	@Override
	public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		super.readAndCheckObject(in);
		Object o=in.readObject();
		if (!(o instanceof IDTransfer))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		yourIDTransfer=(IDTransfer)o;
		o=in.readObject();
		if (!(o instanceof IDTransfer))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		myIDTransfer=(IDTransfer)o;
		numberOfSubBlocks=in.readInt();
		o=in.readObject();
		if (!(o instanceof KernelAddress))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		kernelAddressToConnect=(KernelAddress)o;
		middleReached=in.readBoolean();
		distantInetSocketAddress=OOSUtils.readInetSocketAddress(in, true);
		if (in.readBoolean())
		{
			o=in.readObject();
			if (!(o instanceof PointToPointTransferedBlockChecker))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			pointToPointBlockChecker=(PointToPointTransferedBlockChecker)o;
			
		}
		else
			pointToPointBlockChecker=null;
		if (numberOfSubBlocks < 0)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		
	}

	@Override
	public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
		super.writeAndCheckObject(oos);
		oos.writeObject(yourIDTransfer);
		oos.writeObject(myIDTransfer);
		oos.writeInt(numberOfSubBlocks);
		oos.writeObject(kernelAddressToConnect);
		oos.writeBoolean(middleReached);
		OOSUtils.writeInetSocketAddress(oos, distantInetSocketAddress, true);
		if (pointToPointBlockChecker==null)
			oos.writeBoolean(false);
		else
		{
			oos.writeBoolean(true);
			oos.writeObject(pointToPointBlockChecker);
		}
		
	}
	
	
	TransferConfirmationSystemMessage(IDTransfer idTransferDestinationUsedForBroadcast,
			KernelAddress kernelAddressDestination, KernelAddress kernelAddressToConnect, IDTransfer yourIDTransfer,
			IDTransfer myIDTransfer, int numberOfSubBlocks, boolean middleReached,
			InetSocketAddress distantInetSocketAddress, PointToPointTransferedBlockChecker pointToPointBlockChecker) {
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
		this.pointToPointBlockChecker=pointToPointBlockChecker;
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



	public PointToPointTransferedBlockChecker getPointToPointBlockChecker() {
		return pointToPointBlockChecker;
	}

	@Override
	public boolean excludedFromEncryption() {
		return false;
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}

}
