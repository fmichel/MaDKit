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

/**
 * Represents a pair of identifiers, each equivalent between one peer and one
 * other. Each local and distant identifiers has the same cloud identifier, but
 * not the same host identifier
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class PairOfIdentifiers {
	private final Identifier localIdentifier;
	private final Identifier distantIdentifier;

	PairOfIdentifiers(Identifier _localIdentifier, Identifier _distant_identifier) {
		if (_localIdentifier == null)
			throw new NullPointerException("_localIdentifier");
		if (_distant_identifier == null)
			throw new NullPointerException("_distant_identifier");

		if (_localIdentifier.equalsHostIdentifier(_distant_identifier)) {
			throw new IllegalArgumentException(
					"_localIdentifier and _distant_identifier cannot have the same host identifiers !");
		}

		if (_localIdentifier.equalsCloudIdentifier(_distant_identifier)) {
			localIdentifier = _localIdentifier;
			distantIdentifier = _distant_identifier;
		}

		else {
			throw new IllegalArgumentException(
					"_localIdentifier and _distant_identifier must have the same cloud identifier");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof PairOfIdentifiers)
			return this.equals((PairOfIdentifiers) o);
		return false;
	}

	/**
	 * 
	 * @return the identifier of the local peer
	 */
	public Identifier getLocalIdentifier() {
		return localIdentifier;
	}

	/**
	 * 
	 * @return the identifier of the distant peer
	 */
	public Identifier getDistantIdentifier() {
		return distantIdentifier;
	}

	public boolean equals(PairOfIdentifiers _identifier) {
		return localIdentifier.equals(_identifier.localIdentifier)
				&& distantIdentifier.equals(_identifier.distantIdentifier);
	}
}
