
package madkit.kernel;

import madkit.agr.DefaultMaDKitRoles;

/**
 * Identifies an agent within the artificial society.
 * <p>
 * How this class works is very different from the previous versions of MaDKit.
 * More precisely, it now corresponds to a CGR location (community;group;role)
 * where the agent is in. So, an agent may have several AgentAddress as it could
 * join many CGR locations.
 * <p>
 * Moreover, if the related agent leaves the corresponding CGR location, then an
 * AgentAddress becomes invalid and no longer permits to reach this agent. For
 * instance, a message sent using
 * {@link Agent#send(Message, AgentAddress)} will not be delivered if the
 * agent has leaved the related CGR location.
 * 
 * @author Olivier Gutknecht
 * @author Fabien Michel
 * @since MaDKit 1.0
 * @version 5.1
 */
public class AgentAddress implements java.io.Serializable {

	private static final long serialVersionUID = -5109274890965282440L;

	// not transmitted
	private transient Agent agent;

	// these are the identifying parts over the net
	private final KernelAddress kernelAddress;
	private final int hashCode;
	private Role roleObject;

	private String cgr;// This is necessary to keep info in agent addresses that do not exist anymore

	/**
	 * @param agt  the agent represented by this AA
	 * @param role the object to which this AA is binded to
	 * @param ka   the kernel address corresponding to the kernel in which this AA
	 *             has been created
	 */
	AgentAddress(final Agent agt, final Role role, final KernelAddress ka) {
		agent = agt;
		roleObject = role;
		kernelAddress = ka;// could have been computed from agent but this is serious optimization for mass
							// launching//TODO unsure
		hashCode = agt.hashCode();
	}

	final Agent getAgent() {
		return agent;
	}

	/**
	 * @param roleObject the roleObject to set
	 */
	final void setRoleObject(Role newRole) {
		if (newRole == null && cgr == null) {
			cgr = roleObject.getCommunityName() + ";;" + roleObject.getGroup().getName() + ";;" + roleObject.getName();
		}
		roleObject = newRole;
	}

	/**
	 * @return the corresponding role
	 */
	Role getRoleObject() {
		return roleObject;
	}

	/**
	 * @return the platform's kernelAddress to which this agent address comes from
	 */
	public KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	/**
	 * Returns the community to which this agent address is binded to.
	 * 
	 * @return the name of the community to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getCommunity() {
		return roleObject != null ? roleObject.getCommunityName() : cgr.split(";;")[0];
	}

	/**
	 * Returns the group to which this agent address is binded to.
	 * 
	 * @return the name of the group to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getGroup() {
		return roleObject != null ? roleObject.getGroup().getName() : cgr.split(";;")[1];
	}

	/**
	 * Returns the role to which this agent address is binded to.
	 * 
	 * @return the role name to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getRole() {
		return roleObject != null ? roleObject.getName() : cgr.split(";;")[2];
	}

	/**
	 * Returns a string representing this address. This string contains the ID of
	 * the owner agent, the CGR location of this address and the
	 * {@link KernelAddress} to which this address belongs to.
	 * 
	 * @return a description of this address.
	 */
	@Override
	public String toString() {
		return hashCode + "@(" + getCommunity() + "," + getGroup() + "," + getRole() + ")" + kernelAddress;
	}

	/**
	 * Tells if another address is the same. If <code>true</code>, this means that
	 * both addresses refer to the same agent considering the same position in the
	 * artificial society.
	 * 
	 * @param agentAddress the address to compare.
	 * @throws ClassCastException On purpose, if the address is compared to an
	 *                            object with another type which is considered as a
	 *                            programming error.
	 */
	@Override
	public boolean equals(final Object agentAddress) throws ClassCastException {
		if (this == agentAddress)
			return true;
		if (agentAddress == null || agentAddress.hashCode() != hashCode)
			return false;
		final AgentAddress aa = (AgentAddress) agentAddress;
		return kernelAddress.equals(aa.kernelAddress) && getRole().equals(aa.getRole())
				&& getGroup().equals(aa.getGroup()) && getCommunity().equals(aa.getCommunity());
	}

	public boolean isValid() {
		return roleObject != null;
	}

	/**
	 * The hash code of an agent address. It is the same as the underlying agent's.
	 * See {@link Agent#hashCode()}
	 */
	@Override
	public final int hashCode() {
		return hashCode;
	}

	/**
	 * Tells if the address is from a specific kernel. If <code>true</code>, This
	 * means that the agent to which this address belongs to is located on the
	 * tested kernel. So, it is just a shortcut for *
	 * 
	 * <pre>
	 * return getKernelAddress().equals(kernel);
	 * </pre>
	 * 
	 * for which this address has been created was running on the local kernel.
	 * 
	 * @param kernel the kernel address against which this address should be tested.
	 * @return <code>true</code> if this address belongs to the corresponding
	 *         kernel.
	 * @since MaDKit 5.0.4
	 */
	public boolean isFrom(final KernelAddress kernel) {
		return kernelAddress.equals(kernel);
	}

	/**
	 * Return a string representing a unique identifier of the binded agent over the
	 * network.
	 * 
	 * @return the agent's network identifier
	 */
	public final String getAgentNetworkID() {
		return hashCode + "@" + kernelAddress.getNetworkID();
	}

	/**
	 * Return a string representing a shorter version of the unique identifier of
	 * the binded agent over the network. As a simplified version, this string may
	 * not be unique.
	 * 
	 * @return a simplified version of the binded agent's network identifier
	 * @see Agent#getSimpleNetworkID()
	 */
	public final String getSimpleAgentNetworkID() {
		return hashCode + "@" + kernelAddress;
	}

	final void setAgent(final Agent a) {
		this.agent = a;
	}

}

final class CandidateAgentAddress extends AgentAddress {

	private static final long serialVersionUID = -4139216463718732678L;

	/**
	 * @param agt
	 * @param role
	 * @param ka
	 */
	CandidateAgentAddress(Agent agt, Role role, KernelAddress ka) {
		super(agt, role, ka);
	}

	/**
	 * q
	 * 
	 * @see madkit.kernel.AgentAddress#getRole()
	 */
	@Override
	public final String getRole() {
		return DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE;
	}

}

final class GroupManagerAddress extends AgentAddress {

	private static final long serialVersionUID = -5757376397376189866L;
	private final boolean securedGroup;

	GroupManagerAddress(Agent agt, Role role, KernelAddress ka, boolean securedGroup) {
		super(agt, role, ka);
		this.securedGroup = securedGroup;
	}

	boolean isGroupSecured() {
		return securedGroup;
	}

}