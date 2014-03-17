/*
 * Copyright 2014 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.message;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;


/**
 * A filter that accepts message based on the sender's role.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 *
 */
public class SenderRoleFilter implements MessageFilter {
	
	
	final private String community, group, role;

	/**
	 * a new filter that acts according to the sender's CGR location.
	 * 
	 * @param community 
	 * @param group 
	 * @param role the role that the sender must have
	 */
	public SenderRoleFilter(final String community, final String group, final String role) {
		this.community = community;
		this.group = group;
		this.role = role;
	}

	@Override
	public boolean accept(final Message m) {
		final AgentAddress sender = m.getSender();
		return sender.getCommunity().equals(community) && sender.getGroup().equals(group) && sender.getRole().equals(role);
	}

}
