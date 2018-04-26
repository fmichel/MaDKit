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
package com.distrimind.madkit.message.hook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.hook.HookMessage;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;

/**
 * Notification message about accessible logins into the network, concerning one
 * specific kernel address.
 * 
 * 
 * @author Jason Mahdjoub
 * 
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * 
 * @see AgentActionEvent#LOGGED_IDENTIFIERS_UPDATE
 */
public class NetworkLoginAccessEvent extends HookMessage {


	private final KernelAddress concernedKernelAddress;
	private final List<PairOfIdentifiers> currentIdentifiers;
	private final List<PairOfIdentifiers> newAcceptedIdentifiers;
	private final List<PairOfIdentifiers> newDeniedIdentifiers;
	private final List<PairOfIdentifiers> newUnloggedIdentifiers;

	public NetworkLoginAccessEvent(KernelAddress _concerned_kernel_address_interfaced,
			ArrayList<PairOfIdentifiers> allCurrentIdentifiers, ArrayList<PairOfIdentifiers> newAcceptedIdentifiers,
			ArrayList<PairOfIdentifiers> newDeniedIdentifiers, ArrayList<PairOfIdentifiers> newUnloggedIdentifiers) {
		super(AgentActionEvent.LOGGED_IDENTIFIERS_UPDATE);
		if (_concerned_kernel_address_interfaced == null)
			throw new NullPointerException("_concerned_kernel_address_interfaced");
		this.concernedKernelAddress = _concerned_kernel_address_interfaced;
		if (allCurrentIdentifiers == null)
			throw new NullPointerException("allCurrentIdentifiers");
		this.currentIdentifiers = Collections.unmodifiableList(allCurrentIdentifiers);
		if (newAcceptedIdentifiers == null)
			this.newAcceptedIdentifiers = Collections.unmodifiableList(new ArrayList<PairOfIdentifiers>());
		else
			this.newAcceptedIdentifiers = Collections.unmodifiableList(newAcceptedIdentifiers);
		if (newDeniedIdentifiers == null)
			this.newDeniedIdentifiers = Collections.unmodifiableList(new ArrayList<PairOfIdentifiers>());
		else
			this.newDeniedIdentifiers = Collections.unmodifiableList(newDeniedIdentifiers);
		if (newUnloggedIdentifiers == null)
			this.newUnloggedIdentifiers = Collections.unmodifiableList(new ArrayList<PairOfIdentifiers>());
		else
			this.newUnloggedIdentifiers = Collections.unmodifiableList(newUnloggedIdentifiers);
	}

	@Override
	public String toString() {
		return "NetworkLoginAccessEvent[concernedKernelAddress=" + concernedKernelAddress + ", newAcceptedIdentifiers="
				+ newAcceptedIdentifiers + ", newDeniedIdentifiers=" + newDeniedIdentifiers
				+ ", newUnloggedIdentifiers=" + newUnloggedIdentifiers + "]";
	}

	public List<PairOfIdentifiers> getCurrentIdentifiers() {
		return currentIdentifiers;
	}

	public List<PairOfIdentifiers> getNewAcceptedIdentifiers() {
		return newAcceptedIdentifiers;
	}

	public List<PairOfIdentifiers> getNewDeniedIdentifiers() {
		return newDeniedIdentifiers;
	}

	public List<PairOfIdentifiers> getNewUnloggedIdentifiers() {
		return newUnloggedIdentifiers;
	}

	public KernelAddress getConcernedKernelAddress() {
		return concernedKernelAddress;
	}
}
