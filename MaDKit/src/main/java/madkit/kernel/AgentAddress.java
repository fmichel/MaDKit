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

import madkit.agr.SystemRoles;

/**
 * This class represents an address an agent can have within the artificial society. It
 * represents the agent's position in the artificial society.
 * <p>
 * More precisely, it corresponds to a CGR location (community;group;role) where the agent
 * is in. So, an agent may have several AgentAddress as it could join many CGR locations.
 * <p>
 * Moreover, if the related agent leaves the corresponding CGR location, then an
 * AgentAddress becomes invalid and no longer permits to reach this agent. For instance, a
 * message sent using {@link Agent#send(Message, AgentAddress)} will not be delivered if
 * the agent has leaved the related CGR location.
 *
 * @since MaDKit 1.0
 * @version 6.0.1
 */
public class AgentAddress implements java.io.Serializable {

	private static final long serialVersionUID = -5109274890965282440L;

	private transient Agent agent;

	// these are the identifying parts over the net
	private final KernelAddress kernelAddress;
	private final int hashCode;
	private Role roleObject;

	/**
	 * This is necessary to keep info in agent addresses that do not exist anymore
	 */
	private String cgr;

	/**
	 * Constructs an AgentAddress with the specified agent, role, and kernel address.
	 *
	 * @param agt  the agent represented by this AgentAddress
	 * @param role the role object to which this AgentAddress is bound
	 * @param ka   the kernel address corresponding to the kernel in which this AgentAddress
	 *             has been created
	 */
	AgentAddress(Agent agt, Role role, KernelAddress ka) {
		agent = agt;
		roleObject = role;
		kernelAddress = ka;// could be computed from agent but this is serious optimization for mass launching
		hashCode = agt.hashCode();
	}

	/**
	 * Returns the agent associated with this AgentAddress.
	 *
	 * @return the agent associated with this AgentAddress
	 */
	final Agent getAgent() {
		return agent;
	}

	/**
	 * Sets the role object for this AgentAddress.
	 *
	 * @param newRole the new role object to set
	 */
	final void setRoleObject(Role newRole) {
		if (newRole == null && cgr == null) {
			cgr = roleObject.getCommunityName() + ";;" + roleObject.getGroup().getName() + ";;" + roleObject.getName();
		}
		roleObject = newRole;
	}

	/**
	 * Returns the role object associated with this AgentAddress.
	 *
	 * @return the role object associated with this AgentAddress
	 */
	Role getRoleObject() {
		return roleObject;
	}

	/**
	 * Returns the kernel address associated with this AgentAddress.
	 *
	 * @return the kernel address associated with this AgentAddress
	 */
	public KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	/**
	 * Returns the community to which this AgentAddress is bound.
	 *
	 * @return the name of the community to which this AgentAddress belongs
	 * @since MaDKit 5
	 */
	public String getCommunity() {
		return roleObject != null ? roleObject.getCommunityName() : cgr.split(";;")[0];
	}

	/**
	 * Returns the group to which this AgentAddress is bound.
	 *
	 * @return the name of the group to which this AgentAddress belongs
	 * @since MaDKit 5
	 */
	public String getGroup() {
		return roleObject != null ? roleObject.getGroup().getName() : cgr.split(";;")[1];
	}

	/**
	 * Returns the role to which this AgentAddress is bound.
	 *
	 * @return the role name to which this AgentAddress belongs
	 * @since MaDKit 5
	 */
	public String getRole() {
		return roleObject != null ? roleObject.getName() : cgr.split(";;")[2];
	}

	/**
	 * Returns a string representing this address. This string contains the ID of the owner
	 * agent, the CGR location of this address and the {@link KernelAddress} to which this
	 * address belongs.
	 *
	 * @return a description of this address.
	 */
	@Override
	public String toString() {
		return hashCode + "@(" + getCommunity() + "," + getGroup() + "," + getRole() + ")" + kernelAddress;
	}

	/**
	 * Tells if another address is the same. If <code>true</code>, this means that both
	 * addresses refer to the same agent considering the same position in the artificial
	 * society.
	 *
	 * @param agentAddress the address to compare.
	 * @throws ClassCastException On purpose, if the address is compared to an object with
	 *                            another type which is considered as a programming error.
	 */
	@Override
	public boolean equals(Object agentAddress) throws ClassCastException {// NOSONAR
		if (this == agentAddress) {
			return true;
		}
		if (agentAddress == null || agentAddress.hashCode() != hashCode) {
			return false;
		}
		AgentAddress aa = (AgentAddress) agentAddress;
		return kernelAddress.equals(aa.kernelAddress) && getRole().equals(aa.getRole())
				&& getGroup().equals(aa.getGroup()) && getCommunity().equals(aa.getCommunity());
	}

	/**
	 * Checks if this AgentAddress is valid. Returns <code>true</code> if the corresponding
	 * agent is still playing the associated role.
	 *
	 * @return <code>true</code> if this AgentAddress is valid, <code>false</code> otherwise
	 */
	public boolean isValid() {
		return roleObject != null;
	}

	/**
	 * The hash code of an agent address. It is the same as the underlying agent's. See
	 * {@link Agent#hashCode()}
	 */
	@Override
	public final int hashCode() {
		return hashCode;
	}

	/**
	 * Tells if the address is from a specific kernel. If <code>true</code>, This means that
	 * the agent to which this address belongs to is located on the tested kernel. So, it is
	 * just a shortcut for *
	 *
	 * <pre>
	 * return getKernelAddress().equals(kernel);
	 * </pre>
	 *
	 * for which this address has been created was running on the local kernel.
	 *
	 * @param kernel the kernel address against which this address should be tested.
	 * @return <code>true</code> if this address belongs to the corresponding kernel.
	 * @since MaDKit 5.0.4
	 */
	boolean isFrom(final KernelAddress kernel) {
		return kernelAddress.equals(kernel);
	}

	/**
	 * Return a string representing a unique identifier of the binded agent over the network.
	 *
	 * @return the agent's network identifier
	 */
	public final String getAgentNetworkID() {
		return hashCode + "@" + kernelAddress.getNetworkID();
	}

	/**
	 * Return a string representing a shorter version of the unique identifier of the binded
	 * agent over the network. As a simplified version, this string may not be unique.
	 *
	 * @return a simplified version of the binded agent's network identifier
	 */
	public final String getSimpleAgentNetworkID() {
		return hashCode + "@" + kernelAddress;
	}

	/**
	 * Sets the agent for this AgentAddress.
	 *
	 * @param a the agent to set
	 */
	final void setAgent(final Agent a) {
		this.agent = a;
	}

}

final class CandidateAgentAddress extends AgentAddress {

	private static final long serialVersionUID = -4139216463718732678L;

	/**
	 * Constructs a CandidateAgentAddress with the specified agent, role, and kernel address.
	 *
	 * @param agt  the agent represented by this CandidateAgentAddress
	 * @param role the role object to which this CandidateAgentAddress is bound
	 * @param ka   the kernel address corresponding to the kernel in which this
	 *             CandidateAgentAddress has been created
	 */
	CandidateAgentAddress(Agent agt, Role role, KernelAddress ka) {
		super(agt, role, ka);
	}

	/**
	 * Returns the role associated with this CandidateAgentAddress.
	 *
	 * @return the role name associated with this CandidateAgentAddress
	 * @see madkit.kernel.AgentAddress#getRole()
	 */
	@Override
	public final String getRole() {
		return SystemRoles.GROUP_CANDIDATE;
	}

}

final class GroupManagerAddress extends AgentAddress {

	private static final long serialVersionUID = -5757376397376189866L;
	private final boolean securedGroup;

	/**
	 * Constructs a GroupManagerAddress with the specified agent, role, kernel address, and
	 * security status.
	 *
	 * @param agt          the agent represented by this GroupManagerAddress
	 * @param role         the role object to which this GroupManagerAddress is bound
	 * @param ka           the kernel address corresponding to the kernel in which this
	 *                     GroupManagerAddress has been created
	 * @param securedGroup the security status of the group
	 */
	GroupManagerAddress(Agent agt, Role role, KernelAddress ka, boolean securedGroup) {
		super(agt, role, ka);
		this.securedGroup = securedGroup;
	}

	/**
	 * Checks if the group is secured.
	 *
	 * @return <code>true</code> if the group is secured, <code>false</code> otherwise
	 */
	boolean isGroupSecured() {
		return securedGroup;
	}

}