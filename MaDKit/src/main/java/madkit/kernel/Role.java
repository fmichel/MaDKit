
package madkit.kernel;

import static madkit.i18n.I18nUtilities.getCGRString;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import madkit.kernel.Agent.ReturnCode;

/**
 * Reifying the notion of Role in AGR
 * 
 * @author Fabien Michel
 * @since MaDKit 3.0
 * @version 5.0
 */
public class Role implements Serializable {

	private static final long serialVersionUID = 4447153943733812916L;

	protected final transient Set<Agent> players;
	private transient List<Agent> tmpReferenceableAgents;
	protected transient Set<AgentAddress> agentAddresses;
	protected transient boolean modified = true;
	private final transient Set<Overlooker> overlookers;
	protected final transient Group myGroup;
	private final transient Logger logger;
	private final transient KernelAddress kernelAddress;

	private final String communityName;
	private final String groupName;
	private final String name;

	/**
	 * @return the kernelAddress
	 */
	KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	Role(Group groupObject, String roleName) {
		players = new LinkedHashSet<>(50, 1f);
		tmpReferenceableAgents = new ArrayList<>();// should not be necessary but ...
		communityName = groupObject.getCommunity().getName();
		groupName = groupObject.getName();
		this.name = roleName;
		logger = groupObject.getCommunity().getLogger();
		myGroup = groupObject;
		kernelAddress = myGroup.getKernel().getKernelAddress();
		if (logger != null) {
			logger.finer(() -> toString() + " created");
		}
		overlookers = new LinkedHashSet<>();
		initializeOverlookers();
	}

	private synchronized void initializeOverlookers() {
		myGroup.getOrganization().getOperatingOverlookers().stream().filter(
				o -> o.getRole().equals(name) && o.getGroup().equals(groupName) && o.getCommunity().equals(communityName))
				.forEach(this::addOverlooker);
	}

	/**
	 * this is dirty but... This represents a Group
	 * 
	 * @param community
	 * @param group
	 */
	Role(String community, String group) {
		communityName = community;
		groupName = group;
		name = null;
		players = null;
		overlookers = null;
		myGroup = null;
		logger = null;
		kernelAddress = null;
	}

	/**
	 * @return the myGroup
	 */
	public Group getGroup() {
		return myGroup;
	}

	/**
	 * @return the parentOrganization
	 */
	public Organization getOrganization() {
		return myGroup.getOrganization();
	}

	/**
	 * @return the communityObject
	 */
	public Community getCommunity() {
		return myGroup.getCommunity();
	}

	/**
	 * @return the communityName
	 */
	final String getCommunityName() {
		return communityName;
	}

	final void addOverlooker(Overlooker o) {
		overlookers.add(o);
		o.setOverlookedRole(this);
	}

	final void removeOverlooker(Overlooker o) {
		overlookers.remove(o);
		o.setOverlookedRole(null);
	}

	/**
	 * @return the roleName
	 */
	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getCGRString(communityName, groupName, name);
	}

	/**
	 * add the agent to the role
	 * 
	 * @param name
	 * @param groupName
	 * @param communityName
	 * @param agent         the agent
	 * @return true, if the agent has been added.
	 */
	ReturnCode addMember(Agent requester) {
		synchronized (players) {
			if (!players.add(requester)) {
				return ReturnCode.ROLE_ALREADY_HANDLED;
			}
			if (logger != null) {
				logger.finest(
						() -> requester.getName() + " is now playing " + getCGRString(communityName, groupName, name));
			}
			if (agentAddresses != null) {
				agentAddresses.add(new AgentAddress(requester, this, kernelAddress));
			}
			modified = true;
			addToOverlookers(requester);
		}
		// needs to be synchronized so that adding occurs prior to getAgentList
		// So addToOverlookers(requester); has to be called in group

		return SUCCESS;
	}

	final void addMembers(List<Agent> bucket, boolean roleJustCreated) {
		synchronized (players) {
			players.addAll(bucket);// is optimized wrt size
			if (agentAddresses != null) {
				Set<AgentAddress> addresses = new HashSet<>(bucket.size() + agentAddresses.size(), 0.9f); // factor
				for (Agent a : bucket) {
					addresses.add(new AgentAddress(a, this, kernelAddress));
				}
				addresses.addAll(agentAddresses);
				agentAddresses = addresses;
			}
			modified = true;
		}
		if (roleJustCreated) {
			initializeOverlookers();
		} else {
			addToOverlookers(bucket);
		}
	}

	/**
	 * @param content
	 */
	final void addDistantMember(AgentAddress content) {
		synchronized (players) {
			content.setRoleObject(this);// required for equals to work
			buildAndGetAddresses().add(content);
		}
	}

	ReturnCode removeMember(Agent requester) {
		synchronized (players) {
			if (players.remove(requester)) {
				removeFromOverlookers(requester);
				if (agentAddresses != null) {
					removeAgentAddressOf(requester, agentAddresses).setRoleObject(null);// NOSONAR
																												// removeAgentAddressOf(requester,
																												// agentAddresses) cannot be null
																												// here
				}
				if (logger != null) {
					logger.finest(() -> requester.getName() + " has leaved role "
							+ getCGRString(communityName, groupName, name) + "\n");
				}
				modified = true;
				checkEmptyness();
				return SUCCESS;
			} else {
				if (myGroup.contains(requester)) {
					return ROLE_NOT_HANDLED;
				}
				return ReturnCode.NOT_IN_GROUP;
			}
		}
	}

	final void removeMembers(List<Agent> bucket) {
		synchronized (players) {
			removeFromOverlookers(bucket);
			players.removeAll(bucket);// is optimized
			if (agentAddresses != null) {
				for (Iterator<AgentAddress> i = agentAddresses.iterator(); i.hasNext();) {
					AgentAddress aa = i.next();
					Agent agent = aa.getAgent();
					if (agent != null && bucket.remove(agent)) {
						i.remove();
						aa.setRoleObject(null);// cost is high because of string creation...
					}
				}
			}
			modified = true;
		}
	}

	/**
	 * @param content
	 */
	void removeDistantMember(AgentAddress content) {
		if (agentAddresses != null) {
			synchronized (players) {
				removeAgentAddress(content);
				checkEmptyness();
			}
		}
	}

	final Set<AgentAddress> buildAndGetAddresses() {
		if (agentAddresses == null) {
			synchronized (players) {
				agentAddresses = players.parallelStream().map(a -> new AgentAddress(a, this, kernelAddress))
						.collect(Collectors.toSet());
			}
		}
		return agentAddresses;
	}

	private final void removeAgentAddress(AgentAddress aa) {
		if (agentAddresses.remove(aa)) {
			if (logger != null) {
				logger.finest(() -> aa + " has leaved role " + getCGRString(communityName, groupName, name) + "\n");
			}
			aa.setRoleObject(null);
		}
	}

	/**
	 * @param kernelAddress2
	 */
	void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		if (agentAddresses != null) {
			if (logger != null)
				logger.finest(() -> "Removing all agents from distant kernel " + kernelAddress2 + " in" + this);
			synchronized (players) {
				for (Iterator<AgentAddress> iterator = buildAndGetAddresses().iterator(); iterator.hasNext();) {
					AgentAddress aa = iterator.next();
					if (aa.getKernelAddress().equals(kernelAddress2)) {
						iterator.remove();
						aa.setRoleObject(null);
					}
				}
				checkEmptyness();
			}
		}
	}

	void checkEmptyness() {
		if ((players == null || players.isEmpty()) && (agentAddresses == null || agentAddresses.isEmpty())) {
			cleanAndRemove();
		}
	}

	/**
	 * 
	 */
	private void cleanAndRemove() {
		for (Overlooker o : overlookers) {
			o.setOverlookedRole(null);
		}
		myGroup.removeRole(name);
		tmpReferenceableAgents = null;
		agentAddresses = null;
	}

	/**
	 * Returns a list of agent addresses corresponding to agents having the given
	 * role
	 * 
	 * @return the list of agent addresses corresponding to agents having the given
	 *         role
	 */
	public final List<AgentAddress> getAgentAddresses() {
		return List.copyOf(buildAndGetAddresses());
	}

	/**
	 * @param requester
	 */
	static AgentAddress removeAgentAddressOf(Agent requester, Collection<AgentAddress> agentAddresses2) {
		for (Iterator<AgentAddress> iterator = agentAddresses2.iterator(); iterator.hasNext();) {
			AgentAddress aa = iterator.next();
			if (aa.getAgent() == requester) {
				iterator.remove();
				return aa;
			}
		}
		return null;
	}

	/**
	 * @param agent
	 * @return the AA of the Agent in this Role
	 */
	final AgentAddress getAgentAddressInGroup(Agent agent) {
		AgentAddress aa = getAgentAddressOf(agent);
		if (aa != null)
			return aa;
		return myGroup.getAgentAddressOf(agent);
	}

	/**
	 * This cannot be done without a copy -> ensuring that the collection can be
	 * modified during iteration
	 * 
	 * @return
	 */
	List<Agent> getAgents() {
		if (modified) {
			synchronized (players) {
				modified = false;
				tmpReferenceableAgents = new ArrayList<>(players);// new seems a little bit better: copyOf ? -> no: too slow
			}
		}
		return tmpReferenceableAgents;
	}

	Set<Agent> getAgentsSet() {
		return Collections.unmodifiableSet(players);
	}

	final void addToOverlookers(Agent a) {
		overlookers.parallelStream().forEach(o -> o.onAdding(a));
	}

	private final void addToOverlookers(List<Agent> l) {
		overlookers.parallelStream().forEach(o -> o.adding(l));
	}

	final void removeFromOverlookers(Agent a) {
		overlookers.parallelStream().forEach(o -> o.onRemoving(a));
	}

	private final void removeFromOverlookers(List<Agent> l) {
		overlookers.parallelStream().forEach(o -> o.removing(l));
	}

	/**
	 * importation when connecting to other kernel
	 * 
	 * @param list
	 */
	void importDistantOrg(Set<AgentAddress> list) {
		synchronized (players) {
			buildAndGetAddresses();
			for (AgentAddress aa : list) {
				aa.setRoleObject(this);
				agentAddresses.add(aa);
			}
		}

	}

	/**
	 * Returns the agent address of the agent in this role
	 * 
	 * @param agent the agent to check
	 * @return the agent address of the agent in this role
	 */
	public AgentAddress getAgentAddressOf(Agent agent) {
		synchronized (players) {
			return buildAndGetAddresses().stream().filter(aa -> aa.hashCode() == agent.hashCode() && aa.getAgent() != null)
					.findAny().orElse(null);
		}
	}

	final List<AgentAddress> getOtherRolePlayers(Agent agent) {
		AgentAddress agentAA = getAgentAddressOf(agent);
		synchronized (players) {
			return buildAndGetAddresses().stream().filter(aa -> !aa.equals(agentAA)).toList();
		}
	}

	/**
	 * @param aa
	 * @return the Agent corresponding to the aa agentAddress in this role, null if
	 *         it does no longer play this role
	 */
	Agent getAgentWithAddress(AgentAddress aa) {
		if (players != null) {
			int hash = aa.hashCode();
			synchronized (players) {
				for (Agent agent : players) {
					if (agent.hashCode() == hash)
						return agent;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if the agent is playing this role
	 * 
	 * @param agent the agent to check
	 * @return <code>true</code> if the agent is playing this role
	 */
	public final boolean contains(Agent agent) {
		return players.contains(agent);
	}

	/**
	 * Return the reference agent address
	 * 
	 * @param aa
	 * @return <code>null</code> if it is not contained in this role anymore
	 */
	final AgentAddress resolveDistantAddress(AgentAddress anAA) {
		return buildAndGetAddresses().parallelStream().filter(aa -> aa.equals(anAA)).findAny().orElse(null);
	}

	/**
	 * Returns the number of agents playing this role
	 * 
	 * @return the number of agents playing this role
	 */
	public int size() {
		return players.size();
	}

	/**
	 * Returns <code>true</code> if the role is not empty
	 * 
	 * @return <code>true</code> if the role is not empty.
	 */
	public boolean exists() {
		return !players.isEmpty();
	}

	/**
	 * @param agent
	 * @return
	 */
	AgentAddress getAnotherAddress(Agent agent) {
		AgentAddress agentAA = getAgentAddressOf(agent);
		if (agentAA != null) {
			return buildAndGetAddresses().parallelStream().filter(aa -> !aa.equals(agentAA)).findAny().orElse(null);
		}
		return buildAndGetAddresses().parallelStream().findAny().orElse(null);
	}

}