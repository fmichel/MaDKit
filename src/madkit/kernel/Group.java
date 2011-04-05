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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.Utils.*;

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
	private final GroupIdentifier groupGate;
	private final Logger logger;
	private final String communityName;
	private final String groupName;
	private final Organization myCommunity;
	private final boolean distributed;

	/**
	 * @param logger 
	 * @param creator
	 * @param theIdentifier
	 * @param isDistributed 
	 */
	Group(String community,String group,AbstractAgent creator, GroupIdentifier theIdentifier,boolean isDistributed, Organization communityObject) {
		//		manager = creator;
		//		manager = null;
		distributed = isDistributed;
		myCommunity = communityObject;
		logger = myCommunity.getLogger();
		groupGate = theIdentifier;
		communityName = community;
		groupName = group;
		if(logger != null){
			logger.finer(printCGR(communityName,groupName)+"created");
		}
		//		manager = new AtomicReference<AgentAddress>(new AgentAddress(creator, new Role(community, group), myCommunity.getMyKernel().getKernelAddress()));
		put(Roles.GROUP_MANAGER_ROLE, new ManagerRole(this,creator));
	}



	/**
	 * for distant creation
	 * @param log
	 * @param community
	 * @param group
	 * @param creator
	 * @param theIdentifier
	 * @param communityObject
	 */
	Group(String community,String group,AgentAddress creator, GroupIdentifier theIdentifier,Organization communityObject) {
		//		manager = creator;
		distributed = true;
		myCommunity = communityObject;
		logger = myCommunity.getLogger();
		groupGate = theIdentifier;
		communityName = community;
		groupName = group;
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
			return NULL_STRING;
		if(groupGate != null && ! groupGate.allowAgentToTakeRole(roleName, memberCard))
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
	 * @return the myCommunity
	 */
	final Organization getMyCommunity() {
		return myCommunity;
	}

	/**
	 * @param roleName
	 */
	void removeRole(String roleName) {
		remove(roleName);
		if(logger != null)
			logger.finer("Removing"+printCGR(communityName, groupName, roleName));
		if(isEmpty()){
			myCommunity.removeGroup(groupName);
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
			export.put(org.getKey(),org.getValue().getAgentAddresses());
		}
		return export;
	}

	/**
	 * @param hashMap
	 */
	void importDistantOrg(SortedMap<String, Set<AgentAddress>> sortedMap) {
		for (String roleName : sortedMap.keySet()) {
			Set<AgentAddress> list = sortedMap.get(roleName);
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
		//				myCommunity.removeGroup(groupName);
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
						put(Roles.GROUP_MANAGER_ROLE, new ManagerRole(this,a));
						return;
					}
				}
			}
		}
	}

	//no public methods !!!
	//	/* (non-Javadoc)
	//	 * @see java.util.AbstractMap#toString()
	//	 */
	//	@Override
	//	public String toString() {
	//		// TODO Auto-generated method stub
	//		return printCGR(communityName, groupName);
	//	}

}
