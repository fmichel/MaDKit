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

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.Iterator;
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
 * @since MadKit 1.0
 *
 */
final class Group extends ConcurrentHashMap<String,Role> {

	private static final long serialVersionUID = 498214902172237862L;
	//	private AbstractAgent manager;
	//	private final AtomicReference<AgentAddress> manager;
	private final Gatekeeper gatekeeper;
	private final Logger logger;
	private final String communityName;
	private final String groupName;
	private final Organization communityObject;

	private final boolean distributed;

	/**
	 * @param logger 
	 * @param creator
	 * @param gatekeeper
	 * @param isDistributed 
	 * @param organization 
	 */
	Group(String community,String group,AbstractAgent creator, Gatekeeper gatekeeper,boolean isDistributed, Organization organization) {
		distributed = isDistributed;
		this.gatekeeper = gatekeeper;
		communityName = community;
		groupName = group;
		communityObject = organization;
		logger = communityObject.getLogger();
		//		manager = new AtomicReference<AgentAddress>(new AgentAddress(creator, new Role(community, group), communityObject.getMyKernel().getKernelAddress()));
		put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this,creator));
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
	 * @param creator
	 * @param gatekeeper
	 * @param communityObject
	 */
	Group(String community,String group,AgentAddress creator, Gatekeeper gatekeeper,Organization communityObject) {
		//		manager = creator;
		distributed = true;
		this.communityObject = communityObject;
		logger = communityObject.getLogger();
		this.gatekeeper = gatekeeper;
		communityName = community;
		groupName = group;
		put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this,creator));
		//		manager = new AtomicReference<AgentAddress>(creator);
	}

	String getName(){
		return groupName;
	}

	/**
	 * @param requester
	 * @param roleName
	 * @param memberCard
	 * @return
	 */
	ReturnCode requestRole(AbstractAgent requester, String roleName, Object memberCard) {
		if(roleName == null)
			throw new NullPointerException(ErrorMessages.R_NULL.toString());
		if(gatekeeper != null && ! gatekeeper.allowAgentToTakeRole(roleName, memberCard))
			return ACCESS_DENIED;
		Role theRole = get(roleName);
		if(theRole == null){
			theRole = createRole(roleName);
			put(roleName,theRole);
			theRole.addMember(requester);
			return SUCCESS;
		}
		if (theRole.addMember(requester)) {
			return SUCCESS;
		}
		else
			return ROLE_ALREADY_HANDLED;
	}

	Role createRole(final String roleName) {
		return new Role(this, roleName);
	}

	/**
	 * @param roleName
	 */
	void removeRole(String roleName) {
		remove(roleName);
		if(logger != null)
			logger.finer("Removing"+I18nUtilities.getCGRString(communityName, groupName, roleName));
		checkEmptyness();
	}



	private void checkEmptyness() {
		if(isEmpty()){
			communityObject.removeGroup(groupName);
		}
	}

	/**
	 * @param requester
	 * @return
	 */
	ReturnCode leaveGroup(final AbstractAgent requester) {
		boolean in = false;
		for(final Role r : values()){
			if(r.removeMember(requester) == SUCCESS){
				in = true;
			}
		}
		return in ? SUCCESS : NOT_IN_GROUP;
	}
	
	boolean isIn(AbstractAgent agent){
		for (Role r : values()) {
			if(r.contains(agent))
				return true;
		}
		return false;
	}

//	/**
//	 * @param roleName
//	 * @return
//	 * @throws getAgentWithRoleWarning 
//	 */
//	List<AgentAddress> getRolePlayers(final String roleName) throws CGRException {
//		try {
//			return get(roleName).getAgentAddresses();
//		} catch (NullPointerException e) {
//			throw new CGRException(NOT_ROLE,	printCGR(communityName,groupName, roleName),e);
//		}
//	}

	/**
	 * @param abstractAgent
	 * @return
	 */
	AgentAddress getAgentAddressOf(AbstractAgent abstractAgent) {
		for (final Role r : values()) {
			final AgentAddress aa = r.getAgentAddressOf(abstractAgent);
			if(aa != null)
				return aa;
		}
		return null;
	}

	//	boolean empty(){
	//		return super.isEmpty() && manager.get() == null;
	//	}

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
		TreeMap<String, Set<AgentAddress>> export = new TreeMap<String,Set<AgentAddress>>();
		for (Map.Entry<String, Role> org : entrySet()) {
			export.put(org.getKey(),org.getValue().buildAndGetAddresses());
		}
		return export;
	}

	/**
	 * @param hashMap
	 */
	void importDistantOrg(Map<String, Set<AgentAddress>> map) {
		for (String roleName : map.keySet()) {
			Set<AgentAddress> list = map.get(roleName);
			if(list == null)
				continue;
			Role role = get(roleName);
			if(role == null){
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
		//		boolean in = false;
		for (final Role r : values()) {
			r.removeDistantMember(aa);
		}
		//		if (manager.get().equals(aa)){
		//			manager.set(null);
		//			in = true;
		//		}
		//		if(in){
		//			if(isEmpty()){
		//				communityObject.removeGroup(groupName);
		//			}
		//		}
	}

	void removeAgentsFromDistantKernel(final KernelAddress kernelAddress) {
		if(logger != null)
			logger.finest("Removing all agents from distant kernel "+kernelAddress+" in"+this);
		for (final Role r : values()) {
			r.removeAgentsFromDistantKernel(kernelAddress);
		}
	}

	/**
	 * @param oldManager the last manager
	 * 
	 */
	void chooseNewManager(AbstractAgent oldManager) {
		if(! isEmpty()){
			for (final Role r : values()) {
				for (Iterator<AbstractAgent> iterator = r.getPlayers().iterator(); iterator.hasNext();) {
					AbstractAgent a = iterator.next();
					if(a != oldManager){
						put(madkit.agr.Organization.GROUP_MANAGER_ROLE, new ManagerRole(this,a));
						return;
					}
				}
			}
		}
	}

		@Override
		public String toString() {
			return I18nUtilities.getCGRString(communityName, groupName)+values();
		}



		final void destroy() {
			for(Role r : values()){
				r.destroy();
			}
			communityObject.removeGroup(groupName);
		}

}
