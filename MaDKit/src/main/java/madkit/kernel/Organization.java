package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.i18n.ErrorMessages;
import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.SimuAgent;

/**
 * This class is responsible for managing the organization of the artificial
 * society. It is responsible for creating and removing communities, groups and
 * roles.
 * 
 *
 */
public final class Organization {

	private final Map<String, Community> communities;
	private final Set<Overlooker> registeredOverlookers;
	private final Logger logger;
	private final KernelAgent kernel;

	/**
	 * @return the registeredOverlookers
	 */
	Set<Overlooker> getOperatingOverlookers() {
		return registeredOverlookers;
	}

	/**
	 * @return the logger
	 */
	Logger getLogger() {
		return logger;
	}

	/**
	 * @param communities
	 * @param registeredOverlookers
	 */
	Organization(KernelAgent kernel) {
		super();
		this.kernel = kernel;
		communities = new ConcurrentHashMap<>();
		registeredOverlookers = new LinkedHashSet<>();
		logger = Logger.getLogger("[ORG]");
		logger.setUseParentHandlers(false);
	}

	ReturnCode createGroup(Agent creator, String communityName, String group, Gatekeeper gatekeeper,
			boolean isDistributed) {
		Objects.requireNonNull(group, ErrorMessages.G_NULL.toString());
		// no need to remove org: never failed
		// will throw null pointer if community is null
		Community community = communities.computeIfAbsent(communityName, c -> new Community(communityName, this));
		synchronized (community) {
			if (!community.addGroup(creator, group, gatekeeper, isDistributed)) {
				return ALREADY_GROUP;
			}
//			try {// TODO bof...
//				if (isDistributed) {
//					sendNetworkMessageWithRole(new CGRSynchro(Code.CREATE_GROUP,
//							getRole(community, group, madkit.agr.SystemRoles.GROUP_MANAGER)
//									.getAgentAddressOf(creator)),
//							netUpdater);
//				}
//				if (hooks != null) {
//					informHooks(AgentActionEvent.CREATE_GROUP,
//							getRole(community, group, madkit.agr.SystemRoles.GROUP_MANAGER)
//									.getAgentAddressOf(creator));
//				}
//			} catch (CGRNotAvailable e) {
//				getLogger().severeLog("Please bug report", e);
//			}
		}
		return SUCCESS;
	}

	ReturnCode requestRole(Agent requester, String community, String group, String role, Object memberCard)
			throws CGRNotAvailable {
		return getGroup(community, group).requestRole(requester, role, memberCard);
	}

	/**
	 * Returns <code>true</code> if the group exists in the organization. A group
	 * exists if it has at least one agent playing a role in it.
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @return <code>true</code> if the group exists
	 */
	public boolean isGroup(String community, String group) {
		try {
			getGroup(community, group);
			return true;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if the role exists in the organization. A role
	 * exists if it has at least one agent playing it.
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return <code>true</code> if the role exists
	 */
	public boolean isRole(String community, String group, String role) {
		try {
			getRole(community, group, role);
			return true;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * Returns the role object corresponding to the given community, group and role
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return the role object
	 * @throws CGRNotAvailable
	 */
	public Role getRole(String community, String group, String role) throws CGRNotAvailable {
		return getGroup(community, group).getRole(role);
	}

	/**
	 * @return the kernel
	 */
	KernelAgent getKernel() {
		return kernel;
	}

	final Community getCommunity(String community) throws CGRNotAvailable {
		Community c = communities.get(community);
		if (c == null)
			throw new CGRNotAvailable(NOT_COMMUNITY);
		return c;
	}

	/**
	 * Returns the group object corresponding to the given community and group names
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @return the group object corresponding to the given community and group names
	 * @throws CGRNotAvailable
	 */
	public Group getGroup(String community, String group) throws CGRNotAvailable {
		Group g = getCommunity(community).getGroup(group);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	/**
	 * Returns <code>true</code> if the community exists in the organization. A
	 * community exists if it has at least one agent playing a role in a group.
	 * 
	 * @param community the community name
	 * @return <code>true</code> if the community exists
	 */
	public boolean isCommunity(String community) {
		try {
			getCommunity(community);
			return true;
		} catch (CGRNotAvailable e) {
			return false;
		}
	}

	/**
	 * @param community
	 */
	public void removeCommunity(String community) {
		communities.remove(community);
	}

	/**
	 * @param agent
	 */
	void removeAgent(Agent agent) {
		communities.values().parallelStream().forEach(c -> c.removeAgent(agent));
	}

	/**
	 * Returns a list of all the communities in the organization
	 * 
	 * @return the list of communities in the organization
	 */
	public List<Community> getCommunities() {
		return List.copyOf(communities.values());
	}

	/**
	 * Returns a {@link List} containing all other agents having this position in
	 * the organization. The caller is excluded from the search.
	 *
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return a list of agent addresses corresponding to agents having the given
	 *         role
	 */
	public List<AgentAddress> getAgentsWithRole(String community, String group, String role) {
		try {
			return getRole(community, group, role).getAgentAddresses();
		} catch (CGRNotAvailable e) {
			return Collections.emptyList();
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// //////////////////////// Simulation
	// /////////////////////////////////////////////////////////////////////////

	synchronized boolean addOverlooker(SimuAgent owner, Overlooker o) {
		o.setCommunity(owner.getCommunity());
		if (registeredOverlookers.add(o)) {
			try {
				getRole(o.getCommunity(), o.getGroup(), o.getRole()).addOverlooker(o);
			} catch (CGRNotAvailable e) {// the role does not exist yet
			}
			return true;
		}
		return false;
	}

	/**
	 * @param scheduler
	 * @param activator
	 */

	synchronized boolean removeOverlooker(Overlooker o) {
		Role r = o.getOverlookedRole();
		if (r != null) {
			r.removeOverlooker(o);
		}
		return registeredOverlookers.remove(o);
	}

}
