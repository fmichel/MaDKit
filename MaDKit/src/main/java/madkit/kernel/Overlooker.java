package madkit.kernel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import madkit.i18n.ErrorMessages;
import madkit.simulation.SimuException;

/**
 * @author Fabien Michel
 * @since MaDKit 2.1
 * @version 5.0
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
			} catch (Exception e) {
				throw new SimuException("initialize problem on " + this, e);
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
		adding(getAgents());
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
		agents.forEach(this::onAdding);
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
		agents.forEach(this::onRemoving);
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
		return getAgents().size();
	}

	/**
	 * Returns a snapshot at moment t of the agents handling the group/role couple
	 *
	 * @return a list view (a snapshot at moment t) of the agents that handle the
	 *         group/role couple (in proper sequence)
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
	@Deprecated(since = "6", forRemoval = true)
	private <A extends Agent> List<A> getCurrentAgentsList() {
		return getAgents();
	}

	/**
	 * returns a string containing the CGR location and the number of monitored
	 * agents.
	 *
	 * @return a string representation of this tool.
	 */
	@Override
	public String toString() {
		return getName() + " <" + group + "," + role + "> A(" + size() + ")";
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
		getAgents().parallelStream().forEach(a -> a.killAgent(a, 0));
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