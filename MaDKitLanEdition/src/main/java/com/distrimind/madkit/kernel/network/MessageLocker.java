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

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;
import com.distrimind.madkit.exceptions.MadkitException;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.LockerCondition;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public final class MessageLocker extends LockerCondition {
	private int lock_number = 0;
	private final TransfersReturnsCodes returns_code;
	private boolean firstLockDone = false;

	MessageLocker(LocalLanMessage localLanMessage) {
		returns_code = new TransfersReturnsCodes();
		setAttachment(localLanMessage);
	}

	/*
	 * public void cancelLock() { synchronized(getLocker()) { firstLockDone=true;
	 * lock_number=0; notifyLocker(); } }
	 */

	public void lock() {
		synchronized (getLocker()) {
			++lock_number;
			firstLockDone = true;
		}
	}

	int getLockNumber() {
		synchronized (getLocker()) {
			return lock_number;
		}
	}

	boolean isFirstLockDone() {
		synchronized (getLocker()) {
			return firstLockDone;
		}
	}

	public void unlock() throws MadkitException {
		synchronized (getLocker()) {
			--lock_number;

			if (lock_number < 0)
				throw new MadkitException("unexpected exception !");
			if (lock_number == 0)
				notifyLocker();
		}
	}

	void unlock(KernelAddress ka, DataTransfertResult report) throws MadkitException {
		synchronized (getLocker()) {
			--lock_number;

			if (report != null && report.hasFinishedTransfert())
				returns_code.putResult(ka, ReturnCode.SUCCESS, report);
			else
				returns_code.putResult(ka, ReturnCode.TRANSFER_FAILED, report);

			if (lock_number < 0)
				throw new MadkitException("unexpected exception !");
			if (lock_number == 0) {
				notifyLocker();
			}
		}

	}

	public ReturnCode waitUnlock(AbstractAgent requester, boolean messageSentHasRecipient) throws InterruptedException {

		if (messageSentHasRecipient) {
			MadkitKernelAccess.waitMessageSent(requester, this);
			// requester.wait(this);
		}
		return this.isCanceled() ? ReturnCode.TRANSFER_IN_PROGRESS : returns_code.getReturnCode();
	}

	@Override
	public boolean isLocked() {
		synchronized (getLocker()) {
			return lock_number != 0 || !firstLockDone;
		}
	}

	@Override
	public MessageLocker clone() {
		return this;
	}

}
