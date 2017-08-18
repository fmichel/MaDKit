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

import java.util.ArrayList;
import java.util.Collection;

import com.distrimind.madkit.kernel.KernelAddress;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class LoginConfirmationMessage extends AccessMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -296815844676424978L;

	public final ArrayList<Identifier> accepted_identifiers;
	public final ArrayList<Identifier> denied_identifiers;
	public final KernelAddress kernel_address;
	private final transient short nbAnomalies;
	private final boolean checkDifferedMessages;

	public LoginConfirmationMessage(Collection<PairOfIdentifiers> _accepted_identifiers,
			Collection<PairOfIdentifiers> _denied_identifiers, KernelAddress _kernel_address, short nbAnomalies,
			boolean checkDifferedMessages) {
		if (_accepted_identifiers == null)
			throw new NullPointerException("_accepted_identifiers");
		if (_denied_identifiers == null)
			throw new NullPointerException("_denied_identifiers");
		if (_kernel_address == null)
			throw new NullPointerException("_kernel_address");
		accepted_identifiers = new ArrayList<>();
		for (PairOfIdentifiers poi : _accepted_identifiers) {
			if (poi.getLocalIdentifier() == null)
				throw new NullPointerException();
			accepted_identifiers.add(poi.getLocalIdentifier());
		}
		denied_identifiers = new ArrayList<>();
		for (PairOfIdentifiers poi : _accepted_identifiers) {
			if (poi.getLocalIdentifier() == null)
				throw new NullPointerException();
			denied_identifiers.add(poi.getLocalIdentifier());
		}
		kernel_address = _kernel_address;
		this.nbAnomalies = nbAnomalies;
		this.checkDifferedMessages = checkDifferedMessages;
	}

	@Override
	public short getNbAnomalies() {
		return nbAnomalies;
	}

	@Override
	public Integrity checkDataIntegrity() {
		if (accepted_identifiers == null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (denied_identifiers == null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (kernel_address == null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		for (Identifier id : accepted_identifiers) {
			if (id == null)
				return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		}
		for (Identifier id : denied_identifiers) {
			if (id == null)
				return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		}

		return Integrity.OK;
	}

	@Override
	public boolean checkDifferedMessages() {
		return checkDifferedMessages;
	}

}
