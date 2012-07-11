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

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Oliver Gutknecht
 * @author Fabien Michel since v.3
 * @version 5.0
 * @since MaDKit 1.0
 * 
 */
final class Group extends ConcurrentHashMap<String, Role> {

	private static final long	serialVersionUID	= 498214902172237862L;
	// private AbstractAgent manager;
	// private final AtomicReference<AgentAddress> manager;
	private final Gatekeeper	gatekeeper;
	private final Logger			logger;
	private final String			communityName;
	private final String			groupName;
	private final Organization	communityObject;

	private final boolean		distributed;

	/**
	 * @param creator
	 * @param gatekeeper
	 * @param isDistributed
	 * @param organization
	 */
	Group(String community, String group, AbstractAgent creator, Gatekeeper gatekeeper, boolean isDistributed,
			Organization organization) {
		distributed = isDistributed;
		this.gatekeeper = gatekeeper;
		communityName = community;
		groupName = group;
		communityObject = organization;
		logger = communityObject.getLogger();
		put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this, creator));
	}

	/**
	 * @return the communityObject
	 */
	final Organization getCommunityObject() {
		return communityObject;
	}

	/**
	 * for distant creation
	 * 
	 * @param log
	 * @param community
	 * @param group
	 * @param manager
	 * @param gatekeeper
	 * @param communityObject
	 */
	Group(String community, String group, AgentAddress manager, Gatekeeper gatekeeper, Organization communityObject) {
		// manager = creator;
		distributed = true;
		this.communityObject = communityObject;
		logger = communityObject.getLogger();
		this.gatekeeper = gatekeeper;
		communityName = community;
		groupName = group;
		if (manager != null) {
			put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this,
					manager));
		}
	}

	String getName() {
		return groupName;
	}

	/**
	 * @param requester
	 * @param roleName
	 * @param memberCard
	 * @return
	 */
	ReturnCode requestRole(AbstractAgent requester, String roleName, Object memberCard) {
		if (roleName == null)
			throw new NullPointerException(ErrorMessages.R_NULL.toString());
		if (gatekeeper != null && !gatekeeper.allowAgentToTakeRole(roleName, memberCard))
			return ACCESS_DENIED;
		ReturnCode result = SUCCESS;
		Role theRole;
		synchronized (this) {
			theRole = get(roleName);
			if (theRole == null) {
				theRole = createRole(roleName);
				put(roleName, theRole);
				theRole.addMember(requester);
//				return SUCCESS;
			}
			else {
				// TODO there is another RC : manager role is already handled
				result = theRole.addMember(requester) ? SUCCESS : ROLE_ALREADY_HANDLED;
			}
		}
		// now trigger overlooker updates if needed. 
		if (result == SUCCESS) {
			//Note that the role always still exits here because requester is in
			theRole.addToOverlookers(requester);
		}
		return result;
	}

	Role createRole(final String roleName) {
		return new Role(this, roleName);
	}

	/**
	 * @param roleName
	 */
	void removeRole(String roleName) {
		synchronized (this) {
			remove(roleName);
			if (logger != null)
				logger.finer("Removing" + I18nUtilities.getCGRString(communityName, groupName, roleName));
			checkEmptyness();
		}
	}

	private void checkEmptyness() {
		if (isEmpty()) {
			communityObject.removeGroup(groupName);
		}
	}

	/**
	 * @param requester
	 * @return a list of affected roles, or <code>null</code> if none of them are affected 
	 */
	List<Role> leaveGroup(final AbstractAgent requester) {
		List<Role> affectedRoles = null;
		synchronized (this) {
			for (final Role r : values()) {
				if (r.removeMember(requester) == SUCCESS) {
					if(affectedRoles == null)
						affectedRoles = new ArrayList<Role>();
					affectedRoles.add(r);
				}
			}
		}
		return affectedRoles;
	}

	boolean isIn(AbstractAgent agent) {
		for (Role r : values()) {
			if (r.contains(agent))
				return true;
		}
		return false;
	}

	// /**
	// * @param roleName
	// * @return
	// * @throws getAgentWithRoleWarning
	// */
	// List<AgentAddress> getRolePlayers(final String roleName) throws
	// CGRException {
	// try {
	// return get(roleName).getAgentAddresses();
	// } catch (NullPointerException e) {
	// throw new CGRException(NOT_ROLE, printCGR(communityName,groupName,
	// roleName),e);
	// }
	// }

	/**
	 * @param abstractAgent
	 * @return
	 */
	AgentAddress getAgentAddressOf(AbstractAgent abstractAgent) {
		for (final Role r : values()) {
			final AgentAddress aa = r.getAgentAddressOf(abstractAgent);
			if (aa != null)
				return aa;
		}
		return null;
	}

	// boolean empty(){
	// return super.isEmpty() && manager.get() == null;
	// }

	/**
	 * @return the distributed
	 */
	boolean isDistributed() {
		return distributed;
	}

	/**
	 * @return
	 */
	SortedMap<String, Set<AgentAddress>> getGroupMap() {
		TreeMap<String, Set<AgentAddress>> export = new TreeMap<String, Set<AgentAddress>>();
		for (Map.Entry<String, Role> org : entrySet()) {
			export.put(org.getKey(), org.getValue().buildAndGetAddresses());
		}
		return export;
	}

	/**
	 * @param hashMap
	 */
	void importDistantOrg(Map<String, Set<AgentAddress>> map) {
		for (String roleName : map.keySet()) {
			Set<AgentAddress> list = map.get(roleName);
			if (list == null)
				continue;
			Role role = get(roleName);
			if (role == null) {
				role = createRole(roleName);
				put(roleName, role);
			}
			role.importDistantOrg(list);
		}
	}

	/**
	 * @param content
	 */
	void addDistantMember(AgentAddress content) {
		final String roleName = content.getRole();
		Role r;
		synchronized (this) {
			r = get(roleName);
			if (r == null) {
				r = createRole(roleName);
				put(roleName, r);
			}
		}
		r.addDistantMember(content);
	}

	/**
	 * @param aa
	 */
	void removeDistantMember(final AgentAddress aa) {
		// boolean in = false;
		for (final Role r : values()) {
			aa.setRoleObject(r);
			r.removeDistantMember(aa);
		}
		// if (manager.get().equals(aa)){
		// manager.set(null);
		// in = true;
		// }
		// if(in){
		// if(isEmpty()){
		// communityObject.removeGroup(groupName);
		// }
		// }
	}

	void removeAgentsFromDistantKernel(final KernelAddress kernelAddress) {
		if (logger != null)
			logger.finest("Removing all agents from distant kernel " + kernelAddress + " in" + this);
		for (final Role r : values()) {
			r.removeAgentsFromDistantKernel(kernelAddress);
		}
	}

	/**
	 * @param oldManager the last manager
	 * 
	 */
	void chooseNewManager(AbstractAgent oldManager) {
		synchronized (this) {
			if (!isEmpty()) {
				for (final Role r : values()) {
					for (Iterator<AbstractAgent> iterator = r.getPlayers().iterator(); iterator.hasNext();) {
						AbstractAgent a = iterator.next();
						if (a != oldManager) {
							put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this, a));
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return I18nUtilities.getCGRString(communityName, groupName) + values();
	}

	final void destroy() {
		for (Role r : values()) {
			r.destroy();
		}
		communityObject.removeGroup(groupName);
	}

}
