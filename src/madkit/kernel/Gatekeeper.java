/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.kernel;

/** 
 * This interface is implemented by objects which are used to verify if an agent
 * is allowed to play a certain role in a group. 
 * Objects implementing this interface could be used when creating a Group to secure
 * it.
 * 
 * @author Fabien Michel
 * @since MaDKit 3.0
 * @version 1.0
*/
//TODO put an example of use
public interface Gatekeeper {

	/**
	 * @param roleName the role the agent wants to play
	 * @param memberCard the access card provided by the agent
	 * @return <code>true</code> if the agent should be allowed to play this role in the group, or <code>false</code> otherwise
	 * associated with this {@link Gatekeeper}
	 */
	public boolean allowAgentToTakeRole(final String roleName, final Object memberCard);

}
