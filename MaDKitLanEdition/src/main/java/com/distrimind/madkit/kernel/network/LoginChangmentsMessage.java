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

import java.util.ArrayList;


import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.connection.access.PairOfIdentifiers;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;

/**
 * It is sent to agents that listen to
 * {@link AgentActionEvent#LOGGED_IDENTIFIERS_UPDATE}
 * 
 * @author Jason Mahdjoub
 *
 * @version 1.0
 * @since MadKitLanExtension 1.0
 */
public class LoginChangmentsMessage extends Message {


	public final ArrayList<PairOfIdentifiers> new_accepted_identifiers;
	public final ArrayList<PairOfIdentifiers> new_removed_identifiers;
	public final KernelAddress concerned_kernel_address;
	final KernelAddressInterfaced concerned_kernel_address_interfaced;
	final ConnectionProperties connection_properties;

	@SuppressWarnings("unused")
	LoginChangmentsMessage(ArrayList<PairOfIdentifiers> _new_accepted_identifiers,
						   ArrayList<PairOfIdentifiers> _new_denied_identifiers, KernelAddress _concerned_kernel_address,
						   KernelAddressInterfaced _concerned_kernel_address_interfaced, ConnectionProperties _connection_properties) {
		new_accepted_identifiers = _new_accepted_identifiers;
		new_removed_identifiers = _new_denied_identifiers;
		concerned_kernel_address = _concerned_kernel_address;
		concerned_kernel_address_interfaced = _concerned_kernel_address_interfaced;
		connection_properties = _connection_properties;
	}
}
