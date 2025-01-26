/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.message.hook;

import madkit.kernel.AgentAddress;

/**
 * Root class of messages which are sent to agents that have requested hooks to
 * the kernel
 * 
 *
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public abstract class CGREvent extends HookMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6722477889792374461L;

	CGREvent(AgentActionEvent event) {
		super(event);
	}

	/**
	 * Returns the address of the agent that did the request.
	 * 
	 * @return the name of the agent that triggers the event
	 */
	public abstract AgentAddress getSourceAgent();

	@Override
	public String toString() {
		return super.toString() + " from " + getSourceAgent();
	}

}