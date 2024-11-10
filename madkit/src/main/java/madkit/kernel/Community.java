package madkit.kernel;

import static madkit.i18n.I18nUtilities.getCGRString;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Fabien Michel
 *
 */
public class Community {
	private final Map<String, Group> groups;
	private final Organization parentOrganization;

	private final String name;
	private final Logger logger;

	Community(String name, Organization organization) {
		this.name = name;
		parentOrganization = organization;
		groups = new ConcurrentHashMap<>();
		logger = Logger.getLogger(getCGRString(name));
		logger.setParent(parentOrganization.getLogger());
		logger.setLevel(null);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	boolean isIn(Agent agent) {
		return groups.values().parallelStream().anyMatch(g -> g.contains(agent));
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

//	Map<String, Group> getGroups() {
//		return groups;
//	}

	/**
	 * Group adding. Guarded by this in
	 * {@link KernelAgent#createGroup(Agent, String, String, Gatekeeper, boolean)}
	 * 
	 * @param creator
	 * @param gatekeeper
	 * @param group
	 * @param groupName
	 * @param gatekeeper
	 * @param isDistributed
	 * @return true if the group has been created
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
	 * @return the kernel
	 */
	KernelAgent getKernel() {
		return parentOrganization.getKernel();
	}

	/**
	 * @return the parentOrganization
	 */
	Organization getOrganization() {
		return parentOrganization;
	}

	public Group getGroup(final String name) throws CGRNotAvailable {
		Group g = groups.get(name);
		if (g == null)
			throw new CGRNotAvailable(NOT_GROUP);
		return g;
	}

	/**
	 * @param groupName
	 */
	void removeGroup(String groupName) {
		groups.remove(groupName);
		checkEmptyness();
	}

	private void checkEmptyness() {
		if (groups.isEmpty()) {
			parentOrganization.removeCommunity(name);
		}
	}

	public List<Group> getGroups() {
		return List.copyOf(groups.values());
	}

	/**
	 * @param agent
	 * @return
	 */
	public void removeAgent(Agent agent) {
		groups.values().parallelStream().forEach(g -> g.leaveGroup(agent));
	}

	public int size() {
		return groups.size();
	}

	public boolean exists() {
		return !groups.isEmpty();
	}

}
