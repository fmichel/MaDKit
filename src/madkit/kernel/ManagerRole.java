/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */

package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.HashSet;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent.ReturnCode;


/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0.0.2
 *
 */
final class ManagerRole extends Role{

	private static final long serialVersionUID = 1919401829672949296L;

	ManagerRole(final Group groupObject, AbstractAgent requester, boolean securedGroup) {
		super(groupObject, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		synchronized (players) {
			players.add(requester);
			agentAddresses = new HashSet<>(1,1);
			agentAddresses.add(new GroupManagerAddress(requester, this, getKernelAddress(), securedGroup));
			modified = true;
		}
	}
	
	ManagerRole(final Group groupObject, AgentAddress creator) {
		super(groupObject, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		synchronized (players) {
			agentAddresses = new HashSet<>(1,1);
			agentAddresses.add(creator);
			creator.setRoleObject(this);//required for equals to work
		}
	}
	
	@Override
	boolean addMember(AbstractAgent requester) {// manager is never changed from outside
		return false;
	}
	
	@Override
	ReturnCode removeMember(final AbstractAgent requester) {
		if(super.removeMember(requester) == SUCCESS){
			myGroup.chooseNewManager(requester);
			return SUCCESS;
		}
		return ROLE_NOT_HANDLED;
	}
	
}
