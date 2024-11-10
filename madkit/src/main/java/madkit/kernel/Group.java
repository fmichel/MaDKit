
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.i18n.I18nUtilities.getCGRString;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import madkit.kernel.Agent.ReturnCode;
import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;

/**
 * @author Oliver Gutknecht
 * @author Fabien Michel since v.3
 * @version 6.1
 * @since MaDKit 1.0
 * 
 */
public final class Group {

	private final Community community;
	private final String name;
	private final ConcurrentHashMap<String, Role> roles = new ConcurrentHashMap<>();
	private final Gatekeeper gatekeeper;
	private final Logger logger;
	private final boolean isSecured;
	private final boolean distributed;

	/**
	 * @param creator
	 * @param gatekeeper
	 * @param isDistributed
	 * @param community
	 */
	Group(String group, Agent creator, Gatekeeper gatekeeper, boolean isDistributed, Community community) {
		distributed = isDistributed;
		this.gatekeeper = gatekeeper;
		isSecured = gatekeeper != null;
		name = group;
		this.community = community;
		logger = Logger.getLogger(getCGRString(community.getName(), group));
		logger.setParent(community.getLogger());
		logger.setLevel(null);
		roles.put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new ManagerRole(this, creator, isSecured));
	}

	/**
	 * @return the parentOrganization
	 */
	public Organization getOrganization() {
		return community.getOrganization();
	}

	/**
	 * @return the communityObject
	 */
	public Community getCommunity() {
		return community;
	}

	/**
	 * for distant creation
	 * 
	 * @param log
	 * @param SIM_COMMUNITY
	 * @param group
	 * @param manager
	 * @param gatekeeper
	 * @param communityObject
	 */
	Group(String group, AgentAddress manager, Community communityObject) {
		// manager = creator;
		distributed = true;
		this.community = communityObject;
		logger = communityObject.getLogger();
		if (manager instanceof GroupManagerAddress m) {
			isSecured =m.isGroupSecured();
		} else {
			isSecured = false;
		}
		gatekeeper = null;
		name = group;
		roles.put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new ManagerRole(this, manager));
	}

	boolean isSecured() {
		return isSecured;
	}

	String getName() {
		return name;
	}

	/**
	 * @param requester
	 * @param roleName
	 * @param memberCard
	 * @return
	 */
	ReturnCode requestRole(final Agent requester, final String roleName, final Object memberCard) {
		Objects.requireNonNull(roleName, ErrorMessages.R_NULL.toString());
		if (isSecured && !gatekeeper.allowAgentToTakeRole(requester.getNetworkID(), roleName, memberCard)) {
			return ACCESS_DENIED;
		}
//		if (isSecured) {
//			final MadkitKernel myKernel = getCommunityObject().getMyKernel();
//			if (gatekeeper == null) {
//				final AgentAddress manager = myKernel.getAgentWithRole(
//						communityName, groupName,
//						madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
//				final AgentAddress distantAgentWithRole = myKernel
//						.getDistantAgentWithRole(requester, madkit.agr.LocalCommunity.NAME,
//								"kernels", madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE,
//								manager.getKernelAddress());
//				MicroAgent<Boolean> ma;
//				myKernel.launchAgent(ma = new MicroAgent<Boolean>() {
//					@SuppressWarnings("unchecked")
//					protected void activate() {
//						super.activate();
//						try {
//							Message m = sendMessageAndWaitForReply(
//									distantAgentWithRole,
//									new RequestRoleSecure(myKernel
//											.getSenderAgentAddress(requester, manager,
//													null), roleName, memberCard), 10000);
//							if (m != null) {
//								setResult(((ObjectMessage<Boolean>) m).getContent());
//							}
//							else{
//								setResult(null);
//							}
//						} catch (CGRNotAvailable e) {
//							e.printStackTrace();
//						}
//					}
//				});
//				if (!ma.getResult())
//					return ACCESS_DENIED;
//			}
//			else
//				if (! gatekeeper.allowAgentToTakeRole(requester.getNetworkID(), roleName, memberCard)) {//TODO network ID
//					return ACCESS_DENIED;
//				}
//		}
		// TODO there is another RC : manager role is already handled
		return roles.computeIfAbsent(roleName, r -> new Role(this, roleName)).addMember(requester);
//		if(role.addMember(requester)) {
//			
//		}
//		synchronized (this) {
//			final Role r = getOrCreateRole(roleName);
//			if (r.addMember(requester)) {
//				// now trigger overlooker updates if needed. Note that the role always still
//				// exits here because requester is in
//				r.addToOverlookers(requester);
//				return SUCCESS;
//			}
//			return ROLE_ALREADY_HANDLED;
//		}
	}

	/**
	 * @return the gatekeeper
	 */
	Gatekeeper getGatekeeper() {
		return gatekeeper;
	}

	/**
	 * @param roleName
	 */
	void removeRole(String roleName) {
		roles.remove(roleName);
		if (logger != null)
			logger.finer(() -> "Removing" + I18nUtilities.getCGRString(community.getName(), name, roleName));
		checkEmptyness();
	}

	private void checkEmptyness() {
		if (roles.isEmpty()) {
			community.removeGroup(name);
		}
	}

	boolean leaveGroup(final Agent requester) {
		if (roles.values().parallelStream().filter(r -> r.removeMember(requester) == SUCCESS).count() > 0) {
			checkEmptyness();
			return true;
		}
		return false;
	}

	public boolean contains(Agent agent) {
		return roles.values().parallelStream().anyMatch(r -> r.contains(agent));
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
	 * @param agent
	 * @return
	 */
	AgentAddress getAgentAddressOf(Agent agent) {
		return roles.values().parallelStream().map(r -> r.getAgentAddressOf(agent)).filter(a -> a != null).findAny()
				.orElse(null);
	}

	public List<Role> getRoles() {
		return List.copyOf(roles.values());
	}

	public List<String> getRoleNames() {
		return List.copyOf(roles.keySet());
	}

	// boolean empty(){
	// return super.isEmpty() && manager.get() == null;
	// }

	/**
	 * @return the distributed
	 */
	public boolean isDistributed() {
		return distributed;
	}

	KernelAgent getKernel() {
		return community.getKernel();
	}

//	/**
//	 * @return
//	 */
//	SortedMap<String, Set<AgentAddress>> getGroupMap() {
//		final TreeMap<String, Set<AgentAddress>> export = new TreeMap<>();
//		for (final Map.Entry<String, Role> org : entrySet()) {
//			export.put(org.getKey(), org.getValue().buildAndGetAddresses());
//		}
//		return export;
//	}

//	/**
//	 * @param hashMap
//	 */
//	void importDistantOrg(final Map<String, Set<AgentAddress>> map) {
//		for (final String roleName : map.keySet()) {
//			final Set<AgentAddress> list = map.get(roleName);
//			if (list == null)
//				continue;
//			synchronized (this) {
//				getOrCreateRole(roleName).importDistantOrg(list);
//			}
//		}
//	}

//	/**
//	 * @param content
//	 */
//	void addDistantMember(AgentAddress content) {
//		final String roleName = content.getRole();
//		final Role r;
//		synchronized (this) {
//			r = getOrCreateRole(roleName);
//		}
//		r.addDistantMember(content);
//	}
//	
//	Role getOrCreateRole(final String roleName){
//		Role r = get(roleName);
//		if(r == null)
//			return createRole(roleName);
//		return r;
//	}

//	/**
//	 * @param aa
//	 */
//	void removeDistantMember(final AgentAddress aa) {
//		// boolean in = false;
//		for (final Role r : values()) {
//			aa.setRoleObject(r);// required for equals to work
//			r.removeDistantMember(aa);
//		}
//		// if (manager.get().equals(aa)){
//		// manager.set(null);
//		// in = true;
//		// }
//		// if(in){
//		// if(isEmpty()){
//		// communityObject.removeGroup(groupName);
//		// }
//		// }
//	}

//	void removeAgentsFromDistantKernel(final KernelAddress kernelAddress) {
//		if (logger != null)
//			logger.finest("Removing all agents from distant kernel "
//					+ kernelAddress + " in" + this);
//		for (final Role r : values()) {
//			r.removeAgentsFromDistantKernel(kernelAddress);
//		}
//	}

//	/**
//	 * @param oldManager the last manager
//	 * 
//	 */
//	void chooseNewManager(Agent oldManager) {
//		synchronized (this) {
//			if (!isEmpty()) {
//				for (final Role r : values()) {
//					for (Iterator<Agent> iterator = r.getPlayers()
//							.iterator(); iterator.hasNext();) {
//						Agent a = iterator.next();
//						if (a != oldManager) {
//							put(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE,
//									new ManagerRole(this, a, false));
//							return;
//						}
//					}
//				}
//			}
//		}
//	}

	public List<Role> rolesOf(Agent a) {
		return roles.values().stream().filter(r -> r.contains(a)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return I18nUtilities.getCGRString(community.getName(), name) + roles.values();
	}

	/**
	 * @return
	 * @throws CGRNotAvailable
	 */
	public Role getRole(String role) throws CGRNotAvailable {
		Role r = roles.get(role);
		if (r == null)
			throw new CGRNotAvailable(NOT_ROLE);
		return r;
	}

	public int size() {
		return roles.size();
	}

	public boolean exists() {
		return !roles.isEmpty();
	}

//	final void destroy() {
//		for (Role r : values()) {
//			r.destroy();
//		}
//		communityObject.removeGroup(groupName);
//	}

}
