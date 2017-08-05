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

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;

/**
 * This message is sent to agents that requested kernel's hook related to
 * distant kernel events.
 * 
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 1.0
 * @see AgentActionEvent
 * 
 */
public class DistantKernelAgentEventMessage extends HookMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2073618739610287169L;

	private final KernelAddress kernelAddress;

	public DistantKernelAgentEventMessage(AgentActionEvent _hookType, KernelAddress kernelAddress) {
		super(_hookType);
		if (_hookType == null)
			throw new NullPointerException("_hookType");
		if (_hookType != AgentActionEvent.DISTANT_KERNEL_CONNECTED
				&& _hookType != AgentActionEvent.DISTANT_KERNEL_DISCONNECTED)
			throw new IllegalArgumentException("_hookType");
		if (kernelAddress == null)
			throw new NullPointerException("kernelAddress");
		this.kernelAddress = kernelAddress;
	}

	public KernelAddress getDistantKernelAddress() {
		return kernelAddress;
	}
}
