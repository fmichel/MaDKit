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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import madkit.i18n.ErrorMessages;
import madkit.simulation.SimulationException;

/**
 * @author Fabien Michel
 * @since MaDKit 2.1
 * @version 5.0
 * @param <A> The agent most generic type for this Overlooker
 */
@SuppressWarnings("unchecked")
abstract class Overlooker {

	private Role overlookedRole;
	private String community;
	private final String group;
	private final String role;

	/**
	 * Builds a new Activator or Probe on the given CGR location of the artificial
	 * society.
	 *
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 */
	Overlooker(final String communityName, final String groupName, final String roleName) {
//		community = Objects.requireNonNull(communityName, ErrorMessages.C_NULL.toString());
		community = communityName;
		group = Objects.requireNonNull(groupName, ErrorMessages.G_NULL.toString());
		role = Objects.requireNonNull(roleName, ErrorMessages.R_NULL.toString());
	}

	final void setOverlookedRole(final Role theRole) {
		overlookedRole = theRole;
		if (theRole != null)
			try {
				onInit();
			} catch (Throwable e) {
				throw new SimulationException("initialize problem on " + this, e);
			}
	}

	/**
	 * @return the overlookedRole
	 */
	final Role getOverlookedRole() {
		return overlookedRole;
	}

	// @SuppressWarnings("unchecked")
	// public final A getAgentNb(final int nb)
	// {
	// final List<A> l = getCurrentAgentsList();
	// return l.get(nb);
	// }

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
	 * Automatically called when first added to the organization. Default behavior
	 * is: <code>adding(getCurrentAgentsList());</code>
	 */
	void onInit() {
		adding(getCurrentAgentsList());
	}

	/**
	 * Automatically called when a list of agents joins the corresponding group and
	 * role. This method is automatically called by the MaDKit kernel when agents
	 * enter a role due to the use of
	 * {@link Agent#launchAgentBucket(String, int, String...)}. Override this method
	 * when you want to do some initialization on the agents that enter the
	 * group/role. Default implementation is:
	 *
	 * <pre>
	 *
	 * protected void adding(final List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		adding(agent);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param agents the list of agents which have been added to this group/role at
	 *               once.
	 */
	void adding(final List<Agent> agents) {
		agents.stream().forEach(this::onAdding);
	}

	/**
	 * Automatically called when an agent joins the corresponding group and role.
	 * Override this method when you want to do some initialization when an agent
	 * enters the group/role.
	 *
	 * @param agent which has been added to this group/role
	 */
	protected void onAdding(final Agent agent) {
	}

	/**
	 * Automatically called when a list of agents has leaved the corresponding group
	 * and role. This method is empty by default. Override this method when you want
	 * to do some initialization on the agents that enter the group/role. Default
	 * implementation is:
	 *
	 * <pre>
	 *
	 * protected void removing(final List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		removing(agent);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param agents the list of agents which have been removed from this group/role
	 */
	void removing(final List<Agent> agents) {
		agents.stream().forEach(this::onRemoving);
	}

	/**
	 * Automatically called when an agent leaves the corresponding group and role.
	 * Override this method when you want to do some work when an agent leaves the
	 * group/role. Note that the role is still handled by the agent when invoked.
	 *
	 * @param agent the agent which is being removed from this group/role
	 */
	protected void onRemoving(final Agent agent) {
	}

	/**
	 * Returns the number of targeted agents.
	 *
	 * @return the number of targeted agents
	 */
	public int size() {
		return getCurrentAgentsList().size();
	}

	/**
	 * Returns a snapshot at moment t of the agents handling the group/role couple
	 *
	 * @return a list view (a snapshot at moment t) of the agents that handle the
	 *         group/role couple (in proper sequence)
	 * @since MaDKit 3.0
	 */
	public <A extends Agent> List<A> getCurrentAgentsList()// TODO log if not already added !
	{
		if (overlookedRole != null) {
			return (List<A>) overlookedRole.getAgentsList();
		}
		return Collections.emptyList();
	}

	/**
	 * Returns a ListIterator over the agents which is shuffled
	 *
	 * @return a ListIterator which has been previously shuffled
	 * @since MaDKit 3.0
	 */
	public <A extends Agent> List<A> getShuffledList() {
		try {
			List<A> l = getCurrentAgentsList();
			Collections.shuffle(l);
			return l;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	/**
	 * returns a string containing the CGR location and the number of monitored
	 * agents.
	 *
	 * @return a string representation of this tool.
	 */
	@Override
	public String toString() {
		return getName() + " <" + community + "," + group + "," + role + "> A(" + size() + ")";
	}

	public String getName() {
		return getClass().getSimpleName();
	}

//
//	final void addAgent(final Agent a) {
//		onAdding(a);
//	}
//
//	final void removeAgent(final Agent a) {
//		onRemoving(a);
//	}
//
//	final void addAgents(final List<Agent> l) {
//		adding(l);
//	}

//	final void removeAgents(final List<Agent> l) {
//		removing(l);
//	}

	/**
	 * Kills all the agents which are monitored.
	 */
	public void killAgents() {
		allAgentsLeaveRole();
		getCurrentAgentsList().parallelStream().forEach(a -> a.killAgent(a, 0));
	}

	/**
	 * Makes all the agents leave the corresponding role at once.
	 */
	public void allAgentsLeaveRole() {
		if (overlookedRole != null) {
			overlookedRole.removeMembers(getCurrentAgentsList());
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