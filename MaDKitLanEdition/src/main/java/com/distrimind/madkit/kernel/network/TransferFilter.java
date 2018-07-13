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

import java.io.Serializable;

import com.distrimind.madkit.kernel.KernelAddress;

/**
 * Used when an indirect connection is attempted. The final user must inherit
 * this class to filter asked indirect connections.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see NetworkProperties#setTransferTriggers(TransferFilter)
 * @see #newTransferConnectionPropositionToFinalPeers(NetworkProperties, KernelAddress, int, Serializable)
 * @see #newTransferConnectionPropositionToIntermediatePeers(NetworkProperties, KernelAddress, KernelAddress, int, Serializable)
 * 
 */
public interface TransferFilter {
	/**
	 * Ask to final peers if the given indirect connection can be accepted
	 * 
	 * @param properties
	 *            the used network properties
	 * @param distantKernelAddress
	 *            the distant kernel address
	 * @param numberOfIntermediatePeers
	 *            give the number of intermediate peers between the two final peers
	 *            to connect
	 * @param attachedData
	 *            the attached customized data
	 * @return true is the transferConnection can be accepted
	 */
	boolean newTransferConnectionPropositionToFinalPeers(NetworkProperties properties,
														 KernelAddress distantKernelAddress, int numberOfIntermediatePeers, Serializable attachedData);

	/**
	 * Ask to intermediate peers if the given indirect connection can be accepted
	 * 
	 * @param properties
	 *            the used network properties
	 * @param distantKernelAddress1
	 *            one distant kernel address to connect
	 * @param distantKernelAddress2
	 *            another distant kernel address to connect
	 * @param numberOfIntermediatePeers
	 *            give the number of intermediate peers between the two final peers
	 *            to connect
	 * @param attachedData
	 *            the attached customized data
	 * @return true is the transferConnection can be accepted
	 */
	boolean newTransferConnectionPropositionToIntermediatePeers(NetworkProperties properties,
																KernelAddress distantKernelAddress1, KernelAddress distantKernelAddress2, int numberOfIntermediatePeers,
																Serializable attachedData);
}
