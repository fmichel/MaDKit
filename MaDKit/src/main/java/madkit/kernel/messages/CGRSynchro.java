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
package madkit.kernel.messages;

import madkit.kernel.AgentAddress;
import madkit.messages.ObjectMessage;

/**
 * 
 * This class is used to synchronize the creation of groups and roles between two
 * connected kernels.
 * 
 * @since MaDKit 5.0
 *
 */
class CGRSynchro extends ObjectMessage<AgentAddress> {

	private static final long serialVersionUID = 1125125814563126121L;

	/**
	 * The Enum Code.
	 */
	public enum Code {
		CREATE_GROUP, REQUEST_ROLE, LEAVE_ROLE, LEAVE_GROUP
//		LEAVE_ORG		
	}

	private final Code code;

	/**
	 * @param code the code of the message to send to the other kernel to synchronize
	 * @param aa   the agent address of the agent that is concerned by the synchronization
	 */
	public CGRSynchro(final Code code, final AgentAddress aa) {
		super(aa);
		this.code = code;
	}

	/**
	 * 
	 * @return the code of the message
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return super.toString() + "\n\t" + getCode() + " on " + getContent();
	}

}

class RequestRoleSecure extends ObjectMessage<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1661974372588706717L;
	private final AgentAddress requester;
	private final String roleName;

	public RequestRoleSecure(AgentAddress requester, String roleName, Object key) {
		super(key);
		this.requester = requester;
		this.roleName = roleName;
	}

	/**
	 * @return the requester
	 */
	AgentAddress getRequester() {
		return requester;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}
}