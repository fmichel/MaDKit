
package madkit.kernel;

/**
 * This interface is implemented by objects which are used to verify if an agent
 * is allowed to play a certain role in a group. Objects implementing this
 * interface could be used when creating a Group to secure it.
 * 
 * @author Fabien Michel
 * @since MaDKit 3.0
 * @version 2.0
 */
@FunctionalInterface
public interface Gatekeeper {

	/**
	 * @param agentNetworkID a string identifying uniquely an agent, even over
	 *                       multiple connected kernels
	 * @param roleName       the role the agent wants to play
	 * @param memberCard     the access card provided by the agent
	 * @return <code>true</code> if the agent should be allowed to play this role in
	 *         the group, or <code>false</code> otherwise associated with this
	 *         {@link Gatekeeper}
	 */
	public boolean allowAgentToTakeRole(final String agentNetworkID, final String roleName, final Object memberCard);

}
