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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import madkit.i18n.ErrorMessages;
import madkit.simulation.SimuException;

/**
 * This class is the abstract class of the activators and probes. It is used to monitor a
 * group/role couple in an artificial society.
 */
@SuppressWarnings("unchecked")
abstract class Overlooker {

	private Role overlookedRole;
	private String community;
	private final String group;
	private final String role;

	/**
	 * Builds a new Activator or Probe on the given CGR location of the artificial society.
	 *
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 */
	Overlooker(String communityName, String groupName, String roleName) {
		community = communityName;
		group = Objects.requireNonNull(groupName, ErrorMessages.G_NULL.toString());
		role = Objects.requireNonNull(roleName, ErrorMessages.R_NULL.toString());
	}

	/**
	 * Sets the overlooked role.
	 *
	 * @param theRole the new overlooked role
	 */
	final void setOverlookedRole(Role theRole) {
		overlookedRole = theRole;
		if (theRole != null) {
			try {
				onInit();
			} catch (Exception e) {
				throw new SimuException("initialize problem on " + this, e);
			}
		}
	}

	/**
	 * @return the overlookedRole
	 */
	final Role getOverlookedRole() {
		return overlookedRole;
	}

	/**
	 * Gets the community to which this activator/probe is binded to.
	 *
	 * @return a string representing the community's name
	 */
	public String getCommunity() {
		return community;
	}

	/**
	 * Gets the group to which this activator/probe is binded to.
	 *
	 * @return a string representing the group's name
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Gets the role to which this activator/probe is binded to.
	 *
	 * @return a string representing the role's name
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Automatically called when first added to the organization. Default behavior is:
	 * <code>adding(getCurrentAgentsList());</code>
	 */
	void onInit() {
		adding(getAgents());
	}

	/**
	 * Automatically called when a list of agents joins the corresponding group and role. This
	 * method is automatically called by the MaDKit kernel when agents enter a role due to the
	 * use of {@link Agent#launchAgentBucket(String, int, String...)}. Override this method
	 * when you want to do some initialization on the agents that enter the group/role.
	 * Default implementation is:
	 *
	 * <pre>
	 *
	 * protected void adding(List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		adding(agent);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param agents the list of agents which have been added to this group/role at once.
	 */
	void adding(List<Agent> agents) {
		agents.forEach(this::onAdding);
	}

	/**
	 * Automatically called when an agent joins the corresponding group and role. Override
	 * this method when you want to do some initialization when an agent enters the
	 * group/role.
	 *
	 * @param agent which has been added to this group/role
	 */
	protected void onAdding(Agent agent) {
	}

	/**
	 * Automatically called when a list of agents has leaved the corresponding group and role.
	 * This method is empty by default. Override this method when you want to do some
	 * initialization on the agents that enter the group/role. Default implementation is:
	 *
	 * <pre>
	 *
	 * protected void removing(List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		removing(agent);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param agents the list of agents which have been removed from this group/role
	 */
	void removing(List<Agent> agents) {
		agents.forEach(this::onRemoving);
	}

	/**
	 * Automatically called when an agent leaves the corresponding group and role. Override
	 * this method when you want to do some work when an agent leaves the group/role. Note
	 * that the role is still handled by the agent when invoked.
	 *
	 * @param agent the agent which is being removed from this group/role
	 */
	protected void onRemoving(Agent agent) {
	}

	/**
	 * Returns the number of targeted agents.
	 *
	 * @return the number of targeted agents
	 */
	public int size() {
		return getAgents().size();
	}

	/**
	 * Checks if there is no agent in the group/role couple
	 *
	 * @return true, if no agent is handling the group/role couple
	 */
	public boolean isEmpty() {
		return getAgents().isEmpty();
	}

	/**
	 * Returns a snapshot at moment t of the agents handling the group/role couple
	 *
	 * @return a list view (a snapshot at moment t) of the agents that handle the group/role
	 *         couple (in proper sequence)
	 * @since MaDKit 3.0
	 */
	public <A extends Agent> List<A> getAgents() {
		if (overlookedRole != null) {
			return (List<A>) overlookedRole.getAgents();
		}
		return Collections.emptyList();
	}

	/**
	 * @deprecated use {@link #getAgents()} instead
	 * 
	 * @return a list of agents
	 */
	@SuppressWarnings("unused")
	@Deprecated(since = "6.0.0", forRemoval = true)
	private <A extends Agent> List<A> getCurrentAgentsList() {
		return getAgents();
	}

	/**
	 * returns a string containing the CGR location and the number of monitored agents.
	 *
	 * @return a string representation of this tool.
	 */
	@Override
	public String toString() {
		return getName() + " <" + group + "," + role + "> A(" + size() + ")";
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Kills all the agents which are monitored.
	 */
	public void killAgents() {
		allAgentsLeaveRole();
		getAgents().forEach(a -> a.killAgent(a, 0));
	}

	/**
	 * Makes all the agents leave the corresponding role at once.
	 */
	public void allAgentsLeaveRole() {
		if (overlookedRole != null) {
			overlookedRole.removeMembers(getAgents());
		}
	}

	/**
	 * @param communityName the community to set
	 */
	void setCommunity(String communityName) {
		if (community == null) {
			community = communityName;
		}
	}

}