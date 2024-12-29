
package madkit.kernel;

import static madkit.i18n.I18nUtilities.getCGRString;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * The `Community` class represents a community within the MaDKit kernel. It
 * manages groups and provides methods to interact with them.
 * 
 * @see madkit.kernel.Group
 * @since 1.0
 * @version 6.0
 * 
 * @author Fabien Michel
 */
public class Community {
	private final Map<String, Group> groups;
	private final Organization parentOrganization;

	private final String name;
	private final Logger logger;

	/**
	 * Constructs a `Community` with the specified name and parent organization.
	 *
	 * @param name         the name of the community
	 * @param organization the parent organization
	 */
	Community(String name, Organization organization) {
		this.name = name;
		parentOrganization = organization;
		groups = new ConcurrentHashMap<>();
		logger = Logger.getLogger(getCGRString(name));
		logger.setParent(parentOrganization.getLogger());
		logger.setLevel(null);
	}

	/**
	 * Returns the name of the community.
	 *
	 * @return the name of the community
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if the specified agent is in any group within the community.
	 *
	 * @param agent the agent to check
	 * @return true if the agent is in any group, false otherwise
	 */
	boolean isIn(Agent agent) {
		return groups.values().parallelStream().anyMatch(g -> g.contains(agent));
	}

	/**
	 * Returns the logger associated with the community.
	 *
	 * @return the logger associated with the community
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Adds a group to the community.
	 * 
	 * <p>
	 * Guarded by this in
	 * {@link KernelAgent#createGroup(Agent, String, String, Gatekeeper, boolean)}
	 * </p>
	 *
	 * @param creator       the agent creating the group
	 * @param group         the name of the group
	 * @param gatekeeper    the gatekeeper for the group
	 * @param isDistributed whether the group is distributed
	 * @return true if the group has been created, false otherwise
	 */
	boolean addGroup(final Agent creator, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		Group g = groups.get(group);
		if (g == null) {// There was no such group
			g = new Group(group, creator, gatekeeper, isDistributed, this);
			groups.put(group, g);
			if (logger != null)
				logger.fine(() -> getCGRString(name, group) + "created by " + creator.getName() + "\n");
			return true;
		}
		if (logger != null)
			logger.finer(() -> getCGRString(name, group) + "already exists: Creation aborted" + "\n");
		return false;
	}

	/**
	 * Returns the kernel agent associated with the community.
	 *
	 * @return the kernel agent associated with the community
	 */
	KernelAgent getKernel() {
		return parentOrganization.getKernel();
	}

	/**
	 * Returns the parent organization of the community.
	 *
	 * @return the parent organization of the community
	 */
	Organization getOrganization() {
		return parentOrganization;
	}

	/**
	 * Returns the group with the specified name.
	 *
	 * @param name the name of the group
	 * @return the group with the specified name
	 * @throws CGRNotAvailable if the group is not available
	 */
	public Group getGroup(final String name) throws CGRNotAvailable {
		Group g = groups.get(name);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	/**
	 * Removes the group with the specified name from the community.
	 *
	 * @param groupName the name of the group to remove
	 */
	void removeGroup(String groupName) {
		groups.remove(groupName);
		checkEmptyness();
	}

	/**
	 * Checks if the community is empty and removes it from the parent organization
	 * if it is.
	 */
	private void checkEmptyness() {
		if (groups.isEmpty()) {
			parentOrganization.removeCommunity(name);
		}
	}

	/**
	 * Returns a list of all groups in the community.
	 *
	 * @return a list of all groups in the community
	 */
	public List<Group> getGroups() {
		return List.copyOf(groups.values());
	}

	/**
	 * Removes the specified agent from all groups in the community.
	 *
	 * @param agent the agent to remove
	 */
	public void removeAgent(Agent agent) {
		groups.values().parallelStream().forEach(g -> g.leaveGroup(agent));
	}

	/**
	 * Returns the number of groups in the community.
	 *
	 * @return the number of groups in the community
	 */
	public int size() {
		return groups.size();
	}

	/**
	 * Checks if the community exists (i.e., has any groups).
	 *
	 * @return true if the community has any groups, false otherwise
	 */
	public boolean exists() {
		return !groups.isEmpty();
	}

}
