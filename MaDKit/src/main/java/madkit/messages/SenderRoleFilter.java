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
package madkit.messages;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
 * A filter that accepts message based on the sender's role.
 * 
 * @since MaDKit 5.0.4
 * @version 6.0
 *
 */
public class SenderRoleFilter implements MessageFilter {

	private final String community;
	private final String group;
	private final String role;

	/**
	 * a new filter that acts according to the sender's CGR location.
	 * 
	 * @param community
	 * @param group
	 * @param role      the role that the sender must have
	 */
	public SenderRoleFilter(String community, String group, String role) {
		this.community = community;
		this.group = group;
		this.role = role;
	}

	/**
	 * Tests whether or not the specified message should be accepted. The message is accepted
	 * if the sender's community, group, and role match the specified values.
	 *
	 * @param m the message to be tested
	 * @return true if the message is accepted, false otherwise
	 */
	@Override
	public boolean accept(Message m) {
		AgentAddress sender = m.getSender();
		return sender.getCommunity().equals(community) && sender.getGroup().equals(group)
				&& sender.getRole().equals(role);
	}

}