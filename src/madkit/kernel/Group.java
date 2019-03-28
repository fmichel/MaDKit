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

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.message.ObjectMessage;

/**
 * @author Oliver Gutknecht
 * @author Fabien Michel since v.3
 * @version 5.1
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
	private final boolean		isSecured;

	private final boolean		distributed;

	/**
	 * @param creator
	 * @param gatekeeper
	 * @param isDistributed
	 * @param organization
	 */
	Group(String community, String group, AbstractAgent creator,
			Gatekeeper gatekeeper, boolean isDistributed, Organization organization) {
		distributed = isDistributed;
		this.gatekeeper = gatekeeper;
		isSecured = gatekeeper != null;
		communityName = community;
		groupName = group;
		communityObject = organization;
		logger = communityObject.getLogger();
		put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new ManagerRole(this,
				creator, isSecured));
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
	Group(String community, String group, AgentAddress manager, Organization communityObject) {
		// manager = creator;
		distributed = true;
		this.communityObject = communityObject;
		logger = communityObject.getLogger();
		if (manager instanceof GroupManagerAddress) {
			isSecured = ((GroupManagerAddress) manager).isGroupSecured();
		}
		else{
			isSecured = false;
		}
		gatekeeper = null;
		communityName = community;
		groupName = group;
		put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new ManagerRole(this, manager));
	}

	boolean isSecured() {
		return isSecured;
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
	ReturnCode requestRole(final AbstractAgent requester, final String roleName, final Object memberCard) {
		Objects.requireNonNull(roleName, ErrorMessages.R_NULL.toString());
		if (isSecured) {
			final MadkitKernel myKernel = getCommunityObject().getMyKernel();
			if (gatekeeper == null) {
				final AgentAddress manager = myKernel.getAgentWithRole(
						communityName, groupName,
						madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
				final AgentAddress distantAgentWithRole = myKernel
						.getDistantAgentWithRole(requester, madkit.agr.LocalCommunity.NAME,
								"kernels", madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE,
								manager.getKernelAddress());
				MicroAgent<Boolean> ma;
				myKernel.launchAgent(ma = new MicroAgent<Boolean>() {
					@SuppressWarnings("unchecked")
					protected void activate() {
						super.activate();
						try {
							Message m = sendMessageAndWaitForReply(
									distantAgentWithRole,
									new RequestRoleSecure(myKernel
											.getSenderAgentAddress(requester, manager,
													null), roleName, memberCard), 10000);
							if (m != null) {
								setResult(((ObjectMessage<Boolean>) m).getContent());
							}
							else{
								setResult(null);
							}
						} catch (CGRNotAvailable e) {
							e.printStackTrace();
						}
					}
				});
				if (!ma.getResult())
					return ACCESS_DENIED;
			}
			else
				if (! gatekeeper.allowAgentToTakeRole(requester.getNetworkID(), roleName, memberCard)) {//TODO network ID
					return ACCESS_DENIED;
				}
		}
		// TODO there is another RC : manager role is already handled
		synchronized (this) {
			final Role r = getOrCreateRole(roleName);
			if(r.addMember(requester)){
				// now trigger overlooker updates if needed. Note that the role always still exits here because requester is in
				r.addToOverlookers(requester);
				return SUCCESS;
			}
			return ROLE_ALREADY_HANDLED;
		}
	}

	
	/**
	 * @return the gatekeeper
	 */
	Gatekeeper getGatekeeper() {
		return gatekeeper;
	}

	Role createRole(final String roleName) {
		final Role r = new Role(this, roleName);
		put(roleName,r);
		return r;
	}

	/**
	 * @param roleName
	 */
	void removeRole(String roleName) {
		synchronized (this) {
			remove(roleName);
			if (logger != null)
				logger.finer("Removing"
						+ I18nUtilities.getCGRString(communityName, groupName,
								roleName));
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
//			values().parallelStream().filter(r -> r.removeMember(requester) == SUCCESS);//TODO lambda
			for (final Role r : values()) {
				if (r.removeMember(requester) == SUCCESS) {
					if (affectedRoles == null)
						affectedRoles = new ArrayList<>(10);
					affectedRoles.add(r);
				}
			}
		}
		return affectedRoles;
	}

	boolean isIn(AbstractAgent agent) {
//		return values().parallelStream().anyMatch(r -> r.contains(agent));//TODO lambda
		for (final Role r : values()) {
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
		final TreeMap<String, Set<AgentAddress>> export = new TreeMap<>();
		for (final Map.Entry<String, Role> org : entrySet()) {
			export.put(org.getKey(), org.getValue().buildAndGetAddresses());
		}
		return export;
	}

	/**
	 * @param hashMap
	 */
	void importDistantOrg(final Map<String, Set<AgentAddress>> map) {
		for (final String roleName : map.keySet()) {
			final Set<AgentAddress> list = map.get(roleName);
			if (list == null)
				continue;
			synchronized (this) {
				getOrCreateRole(roleName).importDistantOrg(list);
			}
		}
	}

	/**
	 * @param content
	 */
	void addDistantMember(AgentAddress content) {
		final String roleName = content.getRole();
		final Role r;
		synchronized (this) {
			r = getOrCreateRole(roleName);
		}
		r.addDistantMember(content);
	}
	
	Role getOrCreateRole(final String roleName){
		Role r = get(roleName);
		if(r == null)
			return createRole(roleName);
		return r;
	}

	/**
	 * @param aa
	 */
	void removeDistantMember(final AgentAddress aa) {
		// boolean in = false;
		for (final Role r : values()) {
			aa.setRoleObject(r);// required for equals to work
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
			logger.finest("Removing all agents from distant kernel "
					+ kernelAddress + " in" + this);
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
					for (Iterator<AbstractAgent> iterator = r.getPlayers()
							.iterator(); iterator.hasNext();) {
						AbstractAgent a = iterator.next();
						if (a != oldManager) {
							put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE,
									new ManagerRole(this, a, false));
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
