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

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Message;

/**
 * Message used to inform the system that an anomaly has been detected with a
 * connection or a distant Madkit kernel
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see AbstractAgent#anomalyDetectedWithOneConnection(boolean, ConnectionIdentifier, String)
 * 
 */
public class AnomalyDetectedMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5396999073321069707L;

	private final boolean candidateToBan;
	private final InetSocketAddress inetSocketAddress;
	private final KernelAddress kernelAddress;
	private final String message;

	public AnomalyDetectedMessage(boolean candidateToBan, ConnectionIdentifier connectionIdentifier, String message) {
		this(candidateToBan, connectionIdentifier.getDistantInetSocketAddress(), message);
	}

	public AnomalyDetectedMessage(boolean candidateToBan, InetSocketAddress inetSocketAddress, String message) {
		if (inetSocketAddress == null)
			throw new NullPointerException("inetSocketAddress");
		this.candidateToBan = candidateToBan;
		this.inetSocketAddress = inetSocketAddress;
		this.kernelAddress = null;
		this.message = message;
	}

	public AnomalyDetectedMessage(boolean candidateToBan, KernelAddress kernelAddress, String message) {
		this.candidateToBan = candidateToBan;
		this.kernelAddress = kernelAddress;
		this.inetSocketAddress = null;
		this.message = message;
	}

	@Override
	public String toString() {
		return "AnomalyDetectedMessage[candidateToBan=" + candidateToBan + ", inetSocketAddress=" + inetSocketAddress
				+ ", kernelAddress=" + kernelAddress + ", message=" + message + "]";
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	public KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	public boolean isCandidateToBan() {
		return candidateToBan;
	}

	public String getMessage() {
		return message;
	}

}