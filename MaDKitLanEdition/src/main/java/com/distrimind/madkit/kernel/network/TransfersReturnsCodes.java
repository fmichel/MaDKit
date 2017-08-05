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

import java.util.HashMap;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;

/**
 * Represents a set of returns code with there data transfer reports, associated
 * to each distant (onto network) kernel address.
 * 
 * This class is used to report results of a message sent or broadcasted over
 * the network.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * 
 */
public class TransfersReturnsCodes {
	private final HashMap<KernelAddress, ReturnCode> returns_code;
	private final HashMap<KernelAddress, DataTransfertResult> data_transfert_results;
	// private final KernelAddress default_kernel_address;

	TransfersReturnsCodes() {
		returns_code = new HashMap<>();
		data_transfert_results = new HashMap<>();
	}

	void putResult(KernelAddress ka, ReturnCode returnCode, DataTransfertResult stats) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (returnCode == null)
			throw new NullPointerException("returnCode");
		if (stats == null)
			throw new NullPointerException("stats");
		returns_code.put(ka, returnCode);
		data_transfert_results.put(ka, stats);
	}

	/**
	 * Gets the return code for the LAN transfer associated to the given distant
	 * kernel address .
	 * 
	 * @param ka
	 *            the distant kernel address located into the network
	 * @return the return code of the LAN transfer.
	 */
	public ReturnCode getReturnCode(KernelAddress ka) {
		if (ka == null)
			throw new NullPointerException("ka");
		return returns_code.get(ka);
	}

	/**
	 * Gets the LAN data transfer report to the given distant kernel address.
	 * 
	 * @param ka
	 *            the distant kernel address located into the network
	 * @return the result of the LAN transfer.
	 */
	public DataTransfertResult getDataTransfertResult(KernelAddress ka) {
		if (ka == null)
			throw new NullPointerException("ka");
		return data_transfert_results.get(ka);
	}

	/*
	 * ReturnCode getReturnCode() { if (returns_code.size()!=1) throw new
	 * IllegalAccessError("The returns code list size should be equals to 1");
	 * return returns_code.values().iterator().next(); }
	 * 
	 * DataTransfertResult getDataTransfertResult() { if
	 * (data_transfert_results.size()!=1) throw new
	 * IllegalAccessError("The data transfert results list size should be equals to 1"
	 * ); return data_transfert_results.values().iterator().next(); }
	 */

	/**
	 * Return a return code associating this TransfersReturnsCodes
	 * 
	 * @return the return code.
	 * @see ReturnCode#getTransfersReturnsCodes()
	 */
	public ReturnCode getReturnCode() {
		if (returns_code.size() == 0)
			return ReturnCode.NO_RECIPIENT_FOUND;

		ReturnCode rc = null;
		for (ReturnCode r : returns_code.values()) {
			if (r.equals(ReturnCode.SUCCESS)) {
				if (rc == null)
					rc = ReturnCode.SUCCESS;
				else if (rc.equals(ReturnCode.TRANSFERS_FAILED))
					rc = ReturnCode.TRANSFERS_PARTIALLY_FAILED;
			} else {
				if (rc == null)
					rc = ReturnCode.TRANSFERS_FAILED;
				else if (rc.equals(ReturnCode.SUCCESS))
					rc = ReturnCode.TRANSFERS_PARTIALLY_FAILED;
			}
		}
		if (rc.equals(ReturnCode.TRANSFERS_FAILED) && returns_code.size() == 1)
			rc = ReturnCode.TRANSFER_FAILED;
		MadkitKernelAccess.setReturnsCode(rc, this);
		return rc;
	}

}
