/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import madkit.kernel.Madkit.Roles;


/**
 * Identifies an agent within the artificial society.
 * <p>
 * How this class works is very different from the previous versions of MadKit. 
 * More precisely, it now corresponds to a CGR location (community;group;role) 
 * where the agent is in. So, an agent may have several AgentAddress
 * as it could join many CGR locations.
 * <p>
 * Moreover, if the related agent leaves the corresponding CGR location,
 * then an AgentAddress becomes invalid and no longer permits to reach
 * this agent. For instance, a message sent using 
 * {@link AbstractAgent#sendMessage(AgentAddress, Message)}
 * will not be delivered if the agent has leaved the related CGR location.
 * 
 * @author Olivier Gutknecht 
 * @author Fabien Michel since v.3
 * @since MadKit 1.0
 * @version 5.0
 */
public class AgentAddress implements java.io.Serializable{

	private static final long serialVersionUID = -1375093777512820025L;

	//transient because they are here to speed up offline mode
	final transient private AbstractAgent agent;

	private Role roleObject;

	//these are the identifying parts over the net
	final private KernelAddress kernelAddress;

	final private int _hashCode;

	/**
	 * @param agt the agent represented by this AA
	 * @param role the object to which this AA is binded to
	 * @param ka the kernel address corresponding to the kernel in which this AA has been created
	 */
	AgentAddress(final AbstractAgent agt, final Role role, final KernelAddress ka) {
		agent = agt;
		roleObject = role;
		kernelAddress = ka;
		_hashCode = agent.hashCode();
	}

	//	/** 
	//	 * Tells if an {@link AgentAddress} belongs to the same kernel
	//	 * 
	//	 * @return <code>true</code> if this address belongs to an agent running on the same kernel, <code>false</code> otherwise
	//	 */
	//	public boolean isLocal(){//TODO just verify that
	//		return kernelAddress == roleObject.getKernelAddress();
	//	}

	/**
	 * @return
	 */
	final AbstractAgent getAgent() {
		return agent;
	}

	/**
	 * @param roleObject the roleObject to set
	 */
	final void setRoleObject(Role roleObject) {
		this.roleObject = roleObject;
	}

	/**
	 * @return the corresponding role
	 */
	Role getRoleObject() {
		return roleObject ;
	}

	/**
	 * @return the platform's kernelAddress to which this agent address comes from
	 */
	public KernelAddress getKernelAddress() {
		return kernelAddress;
	}

	/**
	 * Returns the community to which this agent address is binded to.
	 * @return the name of the community to which this agent address belongs to
	 * @since MadKit 5
	 */
	public String getCommunity() {
		return roleObject.getCommunityName();
	}

	/**
	 * Returns the group to which this agent address is binded to.
	 * @return the name of the group to which this agent address belongs to
	 * @since MadKit 5
	 */
	public String getGroup() {
		return roleObject.getGroupName();
	}

	/**
	 * Returns the role to which this agent address is binded to.
	 * @return the role name to which this agent address belongs to
	 * @since MadKit 5
	 */
	public String getRole() {
		return roleObject.getRoleName();
	}

	//	/** 
	//	 * Tells if an {@link AgentAddress} belongs to the same kernel
	//	 * 
	//	 * @return <code>true</code> if this address belongs to an agent running on the same kernel, <code>false</code> otherwise
	//	 */
	//	public boolean isLocal(){//TODO just verify that
	//		return kernelAddress == roleObject.getKernelAddress();
	//	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (getRoleObject() != null) {
			return "(" + getCommunity() + "," + getGroup() + "," +(getRole()==null ? "" : getRole() + ",") + _hashCode
			+ ")" + kernelAddress;
		}
		return "("+_hashCode+ ")" + kernelAddress;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {//TODO program the offline mode
		if(kernelAddress == null){// no network -> oofline mode
			if(o != null){ // for offline mode
				return _hashCode == o.hashCode();
			}
		}
		else{
			if(o instanceof AgentAddress){
				final AgentAddress other = (AgentAddress) o;
				return other._hashCode == _hashCode && kernelAddress.equals(other.kernelAddress); //one kerneladdress by kernel, reason of this optimization
			}
		}
		return false;
	}

	//	/** 
	//	 * Tells if an {@link AgentAddress} belongs to the same kernel
	//	 * 
	//	 * @return <code>true</code> if this address belongs to an agent running on the same kernel, <code>false</code> otherwise
	//	 */
	//	public boolean isLocal(){//TODO just verify that
	//		return kernelAddress == roleObject.getKernelAddress();
	//	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	final public int hashCode() {
		return _hashCode;
	}


}

final class CandidateAgentAddress extends AgentAddress{

	private static final long serialVersionUID = -4139216463718732678L;

	/**
	 * @param agt
	 * @param role
	 * @param ka
	 */
	CandidateAgentAddress(AbstractAgent agt, Role role, KernelAddress ka) {
		super(agt, role, ka);
	}

	/**q
	 * @see madkit.kernel.AgentAddress#getRole()
	 */
	@Override
	final public String getRole() {
		return Roles.GROUP_CANDIDATE_ROLE;
	}

}
