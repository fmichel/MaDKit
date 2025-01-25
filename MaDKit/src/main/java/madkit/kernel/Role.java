/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

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
 * Reifying the notion of Role in AGR. A Role is a set of agents playing a specific
 * function in a group.
 * <p>
 * Especially, a role maintains a list of agents playing it, and the corresponding agent
 * addresses.
 * 
 * @version 6.0.1
 */
public class Role implements Serializable {

	private static final long serialVersionUID = 4447153943733812916L;

	/** The players. */
	protected final transient Set<Agent> players;
	private transient List<Agent> tmpReferenceableAgents;

	/** The agent addresses. */
	protected transient Set<AgentAddress> agentAddresses;

	/** The modified. */
	protected transient boolean modified = true;
	private final transient Set<Overlooker> overlookers;

	/** The my group. */
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

	/**
	 * Instantiates a new role.
	 *
	 * @param groupObject the group object
	 * @param roleName    the role name
	 */
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
	 * Returns a list of agent addresses corresponding to agents having the given role
	 * 
	 * @return the list of agent addresses corresponding to agents having the given role
	 */
	public final List<AgentAddress> getAgentAddresses() {
		return List.copyOf(buildAndGetAddresses());
	}

	/**
	 * Returns the agent address of the agent in this role
	 * 
	 * @param agent the agent to check
	 * @return the agent address of the agent in this role, or <code>null</code> if the agent
	 *         is not in this role
	 */
	public AgentAddress getAgentAddressOf(Agent agent) {
		synchronized (players) {
			return buildAndGetAddresses().stream().filter(aa -> aa.hashCode() == agent.hashCode() && aa.getAgent() != null)
					.findAny().orElse(null);
		}
	}

	/**
	 * Returns the organization which this role belongs to.
	 * 
	 * @return the parent organization of this role
	 */
	public Organization getOrganization() {
		return myGroup.getOrganization();
	}

	/**
	 * Returns the community which this role belongs to.
	 * 
	 * @return the community which this role belongs to.
	 */
	public Community getCommunity() {
		return myGroup.getCommunity();
	}

	/**
	 * Returns the group which this role belongs to.
	 * 
	 * @return the group which this role belongs to.
	 */
	public Group getGroup() {
		return myGroup;
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
	 * Returns true if the agent is playing this role
	 * 
	 * @param agent the agent to check
	 * @return <code>true</code> if the agent is playing this role
	 */
	public final boolean contains(Agent agent) {
		return players.contains(agent);
	}

	/**
	 * Returns the role name.
	 * 
	 * @return the role name.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns a string representation that includes the community, group, and role names.
	 *
	 * @return the string representation of the role.
	 */
	@Override
	public String toString() {
		return getCGRString(communityName, groupName, name);
	}

	/**
	 * Returns the community name.
	 * 
	 * @return the community name.
	 */
	final String getCommunityName() {
		return communityName;
	}

	/**
	 * Adds the overlooker.
	 *
	 * @param o the o
	 */
	final void addOverlooker(Overlooker o) {
		overlookers.add(o);
		o.setOverlookedRole(this);
	}

	/**
	 * Removes the overlooker.
	 *
	 * @param o the o
	 */
	final void removeOverlooker(Overlooker o) {
		overlookers.remove(o);
		o.setOverlookedRole(null);
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

	/**
	 * Adds the members.
	 *
	 * @param bucket          the bucket
	 * @param roleJustCreated the role just created
	 */
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

	/**
	 * Removes the member.
	 *
	 * @param requester the requester
	 * @return the return code
	 */
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

	/**
	 * Removes the members.
	 *
	 * @param bucket the bucket
	 */
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

	/**
	 * Builds the and get addresses.
	 *
	 * @return the sets the
	 */
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
			if (logger != null) {
				logger.finest(() -> "Removing all agents from distant kernel " + kernelAddress2 + " in" + this);
			}
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

	/**
	 * Check emptyness.
	 */
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
		if (aa != null) {
			return aa;
		}
		return myGroup.getAnyAgentAddressOf(agent);
	}

	/**
	 * This cannot be done without a copy -> ensuring that the collection can be modified
	 * during iteration
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

	/**
	 * Gets the agents set.
	 *
	 * @return the agents set
	 */
	Set<Agent> getAgentsSet() {
		return Collections.unmodifiableSet(players);
	}

	/**
	 * Adds the to overlookers.
	 *
	 * @param a the a
	 */
	final void addToOverlookers(Agent a) {
		overlookers.parallelStream().forEach(o -> o.onAdding(a));
	}

	private final void addToOverlookers(List<Agent> l) {
		overlookers.parallelStream().forEach(o -> o.adding(l));
	}

	/**
	 * Removes the from overlookers.
	 *
	 * @param a the a
	 */
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
	 * Gets the other role players.
	 *
	 * @param agent the agent
	 * @return the other role players
	 */
	final List<AgentAddress> getOtherRolePlayers(Agent agent) {
		AgentAddress agentAA = getAgentAddressOf(agent);
		synchronized (players) {
			return buildAndGetAddresses().stream().filter(aa -> !aa.equals(agentAA)).toList();
		}
	}

	/**
	 * @param aa
	 * @return the Agent corresponding to the aa agentAddress in this role, null if it does no
	 *         longer play this role
	 */
	Agent getAgentWithAddress(AgentAddress aa) {
		if (players != null) {
			int hash = aa.hashCode();
			synchronized (players) {
				for (Agent agent : players) {
					if (agent.hashCode() == hash) {
						return agent;
					}
				}
			}
		}
		return null;
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