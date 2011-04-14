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

import java.util.concurrent.atomic.AtomicInteger;

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
 * @author Fabien Michel
 * @since MadKit 1.0
 * @version 5.1
 */
public class AgentAddress implements java.io.Serializable{

	private final static AtomicInteger addressCount = new AtomicInteger();
	
	private static final long serialVersionUID = -5109274890965282440L;

	//not transmitted 
	final transient private AbstractAgent agent;

	//these are the identifying parts over the net
	final private KernelAddress kernelAddress;
	final private int _hashCode;
	final private int agentCode;

	private Role roleObject;
	private transient String cgr;//This is necessary to keep info in agent addresses that do not exist anymore


	/**
	 * @param agt the agent represented by this AA
	 * @param role the object to which this AA is binded to
	 * @param ka the kernel address corresponding to the kernel in which this AA has been created
	 */
	AgentAddress(final AbstractAgent agt, final Role role, final KernelAddress ka) {
		agent = agt;
		agentCode = agt.hashCode();
		roleObject = role;
		kernelAddress = ka;//could have been computed from agt but this is serious optimization for mass launching
		_hashCode = addressCount.getAndIncrement();
	}

	/**
	 * @return
	 */
	final AbstractAgent getAgent() {
		return agent;
	}

	/**
	 * @param roleObject the roleObject to set
	 */
	final void setRoleObject(Role newRole) {
		if (newRole == null) {
			cgr=roleObject.getCommunityName()+";;"+roleObject.getGroupName()+";;"+roleObject.getRoleName();
		}
		roleObject = newRole;
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
		if (roleObject != null) {
			return roleObject.getCommunityName();
		}
		return cgr.split(";;")[0];
	}

	/**
	 * Returns the group to which this agent address is binded to.
	 * @return the name of the group to which this agent address belongs to
	 * @since MadKit 5
	 */
	public String getGroup() {
		if (roleObject != null) {
			return roleObject.getGroupName();
		}
		return cgr.split(";;")[1];
	}

	/**
	 * Returns the role to which this agent address is binded to.
	 * @return the role name to which this agent address belongs to
	 * @since MadKit 5
	 */
	public String getRole() {
		if (roleObject != null) {
			return roleObject.getRoleName();
		}
		return cgr.split(";;")[2];
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
		if(roleObject == null)
			return Utils.getI18N("unregisteredAA");
//		return agentCode+"@("+getCommunity()+"."+getGroup()+"."+getRole()+(isLocal() ? ")" : ")@"+getKernelAddress());
		return agentCode+"@("+getCommunity()+","+getGroup()+","+getRole()+")"+getKernelAddress();
	}

	final int getAgentCode() {
		return agentCode;
	}

	/**
	 * Tells if another address is the same.
	 * If <code>true</code>, this means that both addresses are
	 * identical.
	 * 
	 * @param agentAddress the address to compare.
	 * @throws ClassCastException On purpose, 
	 * if the address is compared with another object type.
	 */
	@Override
	public boolean equals(final Object agentAddress) throws ClassCastException{//TODO program the offline mode
		if(this == agentAddress)
			return true;
		if(agentAddress == null || !(agentAddress.hashCode() == _hashCode))
			return false;
		AgentAddress otherAA = (AgentAddress) agentAddress;
		return kernelAddress.equals(otherAA.getKernelAddress());
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

	/**
	 * Tells if this agent address is still valid. I.e. the corresponding agent is 
	 * still playing this role.
	 * 
	 * @return <code>true</code> if the address still exists in the organization.
	 * @since MadKit 5.0.0.9
	 */
	public boolean exists() {
		return roleObject != null;
	}

	/**
	 * Tells if the address belongs to a local agent. This does not imply that the address 
	 * is still valid and exists in the artificial society. This only tells if the agent
	 * for which this address has been created was running on the local kernel.
	 *  
	 * @return <code>true</code> if this address corresponds to an agent which is running
	 * on the same kernel.
	 * @since MadKit 1
	 * @see #exists()
	 */
	public boolean isLocal() {
		return agent != null;///*exists() && */kernelAddress.equals(roleObject.getKernelAddress());//will crash if used inside the network agent
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
	
	@Override
	public boolean exists() {
		return true;
	}

}
