/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */

package madkit.kernel;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;
import static madkit.kernel.AbstractAgent.ReturnCode.*;


/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0.0.2
 *
 */
final class ManagerRole extends Role {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1919401829672949296L;

	/**
	 * @param log
	 * @param community
	 * @param group
	 * @param role
	 * @param groupObject
	 * @param ka
	 * @param overlookers2
	 */
	ManagerRole(final Group groupObject, AbstractAgent creator) {
		super(groupObject, Roles.GROUP_MANAGER_ROLE);
		super.addMember(creator);
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.Role#addMember(madkit.kernel.AbstractAgent)
	 */
	@Override
	boolean addMember(AbstractAgent requester) {// manage is never changed from outside
		return false;
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.Role#removeMember(madkit.kernel.AbstractAgent)
	 */
	@Override
	ReturnCode removeMember(final AbstractAgent requester) {
		if(super.removeMember(requester) == SUCCESS){
			myGroup.chooseNewManager(requester);
			return SUCCESS;
		}
		return ROLE_NOT_HANDLED;
	}
	
	
	

}
