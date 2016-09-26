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

import madkit.agr.Organization;


/**
 * Identifies an agent within the artificial society.
 * <p>
 * How this class works is very different from the previous versions of MaDKit. 
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
 * @since MaDKit 1.0
 * @version 5.1
 */
public class AgentAddress implements java.io.Serializable{

	private static final long serialVersionUID = -5109274890965282440L;

	//not transmitted 
	transient private AbstractAgent agent;

	//these are the identifying parts over the net
	final private KernelAddress kernelAddress;
	final private int _hashCode;
	private Role roleObject;

	private String cgr;//This is necessary to keep info in agent addresses that do not exist anymore


	/**
	 * @param agt the agent represented by this AA
	 * @param role the object to which this AA is binded to
	 * @param ka the kernel address corresponding to the kernel in which this AA has been created
	 */
	AgentAddress(final AbstractAgent agt, final Role role, final KernelAddress ka) {
		agent = agt;
		roleObject = role;
		kernelAddress = ka;//could have been computed from agt but this is serious optimization for mass launching//TODO unsure
		_hashCode = agt.hashCode();
	}

	final AbstractAgent getAgent() {
		return agent;
	}

	/**
	 * @param roleObject the roleObject to set
	 */
	final void setRoleObject(Role newRole) {
		if (newRole == null && cgr == null) {
				cgr = roleObject.getCommunityName() + ";;" + roleObject.getGroupName() + ";;" + roleObject.getRoleName();
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
	 * @since MaDKit 5
	 */
	public String getCommunity() {
		return roleObject != null ? 
				roleObject.getCommunityName() : 
					cgr.split(";;")[0];
	}

	/**
	 * Returns the group to which this agent address is binded to.
	 * @return the name of the group to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getGroup() {
		return roleObject != null ? roleObject.getGroupName() : cgr.split(";;")[1];
	}

	/**
	 * Returns the role to which this agent address is binded to.
	 * @return the role name to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getRole() {
		return roleObject != null ? roleObject.getRoleName() : cgr.split(";;")[2];
	}

	/**
	 * Returns a string representing this address.
	 * This string contains the ID of the owner agent, 
	 * the CGR location of this address and the {@link KernelAddress}
	 * to which this address belongs to.
	 * 
	 *  @return a description of this address.
	 */
	@Override
	public String toString() {
		return _hashCode+"@("+getCommunity()+","+getGroup()+","+getRole()+")"+kernelAddress;
	}
	
	/**
	 * Tells if another address is the same.
	 * If <code>true</code>, this means that both addresses refer to
	 * the same agent considering the same position in the artificial society.
	 * 
	 * @param agentAddress the address to compare.
	 * @throws ClassCastException On purpose, 
	 * if the address is compared to an object with another type 
	 * which is considered as a programming error.
	 */
	@Override
	public boolean equals(final Object agentAddress) throws ClassCastException{
		if(this == agentAddress)
			return true;
		if(agentAddress == null || agentAddress.hashCode() != _hashCode)
			return false;
		final AgentAddress aa = (AgentAddress) agentAddress;
		return kernelAddress.equals(aa.kernelAddress) && getRole().equals(aa.getRole()) && getGroup().equals(aa.getGroup()) && getCommunity().equals(aa.getCommunity());
	}

	/**
	 * The hash code of an agent address.
	 * It is the same as
	 * the underlying agent's. See {@link AbstractAgent#hashCode()}
	 */
	@Override
	final public int hashCode() {
		return _hashCode;
	}

	/**
	 * Tells if the address is from a specific kernel. If <code>true</code>,
	 * This means that the agent to which 
	 * this address belongs to is located on the tested kernel. So, it is just a  
	 * shortcut for 
	 * 	 * <pre>
		* return getKernelAddress().equals(kernel);
	 * </pre>

	 * for which this address has been created was running on the local kernel.
	 *  
	 * @param kernel the kernel address against which this address should be tested.
	 * @return <code>true</code> if this address belongs to the corresponding kernel.
	 * 
	 * @since MaDKit 5.0.4
	 */
	public boolean isFrom(final KernelAddress kernel) {
		return kernelAddress.equals(kernel);
	}
	
	/**
	 * Return a string representing a unique identifier of the binded agent
	 * over the network.
	 * 
	 * @return the agent's network identifier
	 */
	final public String getAgentNetworkID(){
		return _hashCode+"@"+kernelAddress.getNetworkID();
	}

	/**
	 * Return a string representing a shorter version of the 
	 * unique identifier of the binded agent over the network.
	 * As a simplified version, this string may not be unique.
	 * 
	 * @return a simplified version of the binded agent's network identifier
	 * @see AbstractAgent#getSimpleNetworkID()
	 */
	final public String getSimpleAgentNetworkID(){
		return _hashCode +"@"+ kernelAddress;
	}

	final void setAgent(final AbstractAgent a) {
		this.agent = a;
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
		return Organization.GROUP_CANDIDATE_ROLE;
	}
	
}

final class GroupManagerAddress extends AgentAddress{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5757376397376189866L;
	private final boolean securedGroup;

	GroupManagerAddress(AbstractAgent agt, Role role, KernelAddress ka, boolean securedGroup) {
		super(agt, role, ka);
		this.securedGroup = securedGroup;
	}
	
	boolean isGroupSecured(){
		return securedGroup;
	}
	
}