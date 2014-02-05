/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.HashSet;

import madkit.agr.Organization;
import madkit.kernel.AbstractAgent.ReturnCode;


/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0.0.2
 *
 */
final class ManagerRole extends Role{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1919401829672949296L;

	ManagerRole(final Group groupObject, AbstractAgent requester, boolean securedGroup) {
		super(groupObject, Organization.GROUP_MANAGER_ROLE);
		synchronized (players) {
			players.add(requester);
			agentAddresses = new HashSet<>(1,1);
			agentAddresses.add(new GroupManagerAddress(requester, this, getKernelAddress(), securedGroup));
//			System.err.println(requester.getName() + " is now playing " + getCGRString(communityName, groupName, roleName));
//			System.err.println(this+" current players---\n"+players+"\n\n");
			modified = true;
		}
	}
	
	ManagerRole(final Group groupObject, AgentAddress creator) {
		super(groupObject, Organization.GROUP_MANAGER_ROLE);
		synchronized (players) {
			agentAddresses = new HashSet<>(1,1);
			agentAddresses.add(creator);
			creator.setRoleObject(this);//required for equals to work
		}
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.Role#addMember(madkit.kernel.AbstractAgent)
	 */
	@Override
	boolean addMember(AbstractAgent requester) {// manager is never changed from outside
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
	
//	@Override
//	void checkEmptyness() {
//		if(buildAndGetAddresses().isEmpty()){
//			myGroup.chooseNewManager(oldManager);
//		}
//		super.checkEmptyness();
//	}
	
	

}
