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

import java.io.Serializable;

/**
 * Represents a unique identifier for an agent over the network
 * 
 * @author Jason Mahdjoub
 * @since MadKitLanEdition 1.0
 * @version 1.0
 *
 */
public final class AgentNetworkID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3963170349383064158L;

	private final KernelAddress kernelAddress;
	private final long id;

	AgentNetworkID(KernelAddress _ka, long _id) {
		kernelAddress = _ka;
		id = _id;
	}

	@Override
	public String toString() {
		return id + kernelAddress.toString();
	}

	@Override
	public int hashCode() {
		return (int) id + kernelAddress.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof AgentNetworkID) {
			AgentNetworkID nid = (AgentNetworkID) o;
			return id == nid.id && kernelAddress.equals(nid.kernelAddress);
		}
		return false;
	}

	public boolean equals(AgentNetworkID nid) {
		if (nid == null)
			return true;
		return id == nid.id && kernelAddress.equals(nid.kernelAddress);
	}

	/**
	 * Return a string representing a shorter version of the unique identifier of
	 * the binded agent over the network. As a simplified version, this string may
	 * not be unique.
	 * 
	 * @return a simplified version of the binded agent's network identifier
	 * @see AbstractAgent#getNetworkID()
	 */
	final public String getSimpleString() {
		return id + "@" + kernelAddress;
	}

}
