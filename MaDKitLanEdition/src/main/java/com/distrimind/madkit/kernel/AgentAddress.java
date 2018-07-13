/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.madkit.util.SerializationTools;

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
 * {@link AbstractAgent#sendMessage(AgentAddress, Message)} will not be
 * delivered if the agent has leaved the related CGR location.
 * 
 * @author Olivier Gutknecht
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * 
 * @since MaDKitLanEdition 1.0
 * @version 5.2
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class AgentAddress implements ExternalizableAndSizable, Cloneable {

	private static final long serialVersionUID = -5109274890965282440L;

	// not transmitted
	transient private AbstractAgent agent;

	// these are the identifying parts over the net
	private KernelAddress kernelAddress;
	private long agentID;
	private InternalRole roleObject;

	private String cgr;// This is necessary to keep info in agent addresses that do not exist anymore
	private boolean manually_requested;
	AgentAddress()
	{
		
	}
	/**
	 * @param agt
	 *            the agent represented by this AA
	 * @param role
	 *            the object to which this AA is binded to
	 * @param ka
	 *            the kernel address corresponding to the kernel in which this AA
	 *            has been created
	 */
	AgentAddress(final AbstractAgent agt, final InternalRole role, final KernelAddress ka, boolean manually_requested) {
		agent = agt;
		roleObject = role;
		kernelAddress = ka;// could have been computed from agt but this is serious optimization for mass
							// launching//TODO unsure
		agentID = agt.getAgentID();
		this.manually_requested = manually_requested;
	}

	final AbstractAgent getAgent() {
		return agent;
	}

	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		SerializationTools.writeExternalizableAndSizable(oos, kernelAddress, false);
		oos.writeLong(agentID);
		SerializationTools.writeExternalizableAndSizable(oos, roleObject, true);
		SerializationTools.writeString(oos, cgr, Group.MAX_CGR_LENGTH, true);
		oos.writeBoolean(manually_requested);
	}
	@Override
	public int getInternalSerializedSize() {
		
		return kernelAddress.getInternalSerializedSize()+13+(roleObject==null?0:roleObject.getInternalSerializedSize())+(cgr==null?0:cgr.length()*2);
	}
	@Override
	public void readExternal(ObjectInput ois) throws IOException, ClassNotFoundException {
		
		Object o=SerializationTools.readExternalizableAndSizable(ois, false);
		if (o instanceof KernelAddress)
		{
			kernelAddress=(KernelAddress)o;
			agentID=ois.readLong();
			o=SerializationTools.readExternalizableAndSizable(ois, true);
			if (o!=null && !(o instanceof InternalRole))
			{
				throw new MessageSerializationException(Integrity.FAIL);
			}
			roleObject=(InternalRole)o;
			cgr=SerializationTools.readString(ois, Group.MAX_CGR_LENGTH, true);
			manually_requested=ois.readBoolean();
		}
		else
		{
			throw new MessageSerializationException(Integrity.FAIL);
		}
		
	}
	
	
	/*
	 * public AgentAddress getOriginalAgentAddesss(KernelAddressInterfaced kai) { if
	 * (!kernelAddress.equals(kai)) throw new
	 * IllegalArgumentException("The given kernel address interfaced is incompatible with this agent address !"
	 * ); if (kai.isInterfaced()) return new AgentAddress(agent, roleObject,
	 * kai.getOriginalKernelAddress(), manually_requested); else return this; }
	 * 
	 * public AgentAddress interfaceDistantAgentAddress(KernelAddressInterfaced kai)
	 * { if (!kernelAddress.equals(kai.getOriginalKernelAddress())) throw new
	 * IllegalArgumentException("The given kernel address interfaced is incompatible with this agent address !"
	 * ); if (kai.isInterfaced()) return new AgentAddress(agent, roleObject, kai,
	 * manually_requested); else return this; }
	 */

	/**
	 * @param newRole
	 *            the roleObject to set
	 */
	final void setRoleObject(InternalRole newRole) {
		if (newRole == null && cgr == null) {
			cgr = roleObject.getGroup().getCommunity() + ";;" + roleObject.getGroup().getPath() + ";;"
					+ roleObject.getRoleName();
		}
		roleObject = newRole;
	}

	/**
	 * @return the corresponding role
	 */
	InternalRole getRoleObject() {
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
		return roleObject != null ? roleObject.getGroup().getCommunity() : cgr.split(";;")[0];
	}

	/**
	 * Returns the group to which this agent address is binded to.
	 * 
	 * @return the name of the group to which this agent address belongs to
	 * @since MadKitLanEdition 1.0
	 * @see Group
	 */
	public Group getGroup() {
		return roleObject != null ? roleObject.getGroup()
				: Group.getGroupFromPath(getCommunity(), cgr.split(";;")[1], true);
	}

	/**
	 * Returns the role to which this agent address is binded to.
	 * 
	 * @return the role name to which this agent address belongs to
	 * @since MaDKit 5
	 */
	public String getRole() {
		return roleObject != null ? roleObject.getRoleName() : cgr.split(";;")[2];
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
		return agentID + "@(" + getGroup() + "," + getRole() + ")" + kernelAddress;
	}

	public long getAgentID() {
		return agentID;
	}

	/**
	 * Tells if another address is the same. If <code>true</code>, this means that
	 * both addresses refer to the same agent considering the same position in the
	 * artificial society.
	 * 
	 * @param agentAddress
	 *            the address to compare.
	 * @throws ClassCastException
	 *             On purpose, if the address is compared to an object with another
	 *             type which is considered as a programming error.
	 */
	@Override
	public boolean equals(final Object agentAddress) throws ClassCastException {
		if (this == agentAddress)
			return true;
		if (!(agentAddress instanceof AgentAddress))
			return false;

		final AgentAddress aa = (AgentAddress) agentAddress;
		if (aa.getAgentID() != getAgentID())
			return false;
		return kernelAddress.equals(aa.kernelAddress) && getRole().equals(aa.getRole())
				&& getGroup().equals(aa.getGroup()) && getCommunity().equals(aa.getCommunity());
	}

	/**
	 * Tells if another address represents the same agent. If <code>true</code>,
	 * this means that both addresses refer to the same agent. We do not consider
	 * here the agent's position in the artificial society.
	 * 
	 * @param agentAddress
	 *            the address to compare.
	 * @return true if the given address represents the same agent with the current
	 *         address.
	 */
	public boolean representsSameAgentThan(AgentAddress agentAddress) {
		if (this == agentAddress)
			return true;
		if (agentAddress == null || agentAddress.getAgentID() != getAgentID())
			return false;
		return kernelAddress.equals(agentAddress.kernelAddress);
	}

	/**
	 * The hash code of an agent address. It is the same as the underlying agent's.
	 * See {@link AbstractAgent#hashCode()}
	 */
	@Override
	final public int hashCode() {
		return (int) agentID;
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
	 * @param kernel
	 *            the kernel address against which this address should be tested.
	 * @return <code>true</code> if this address belongs to the corresponding
	 *         kernel.
	 * 
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
	final public AgentNetworkID getAgentNetworkID() {
		return new AgentNetworkID(kernelAddress, getAgentID());
	}

	final void setAgent(final AbstractAgent a) {
		this.agent = a;
	}

	/**
	 * Tells if this agent address was automatically requested or manually requested
	 * 
	 * @return true if this agent address was manually requested
	 * @see AbstractAgent#autoRequestRole(AbstractGroup, String, ExternalizableAndSizable)
	 */
	public boolean isManuallyRequested() {
		return manually_requested;
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public AgentAddress clone() {
		return this;
	}

	

	
}

@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
final class CandidateAgentAddress extends AgentAddress {

	private static final long serialVersionUID = -4139216463718732678L;


	CandidateAgentAddress(AbstractAgent agt, InternalRole role, KernelAddress ka, boolean manually_requested) {
		super(agt, role, ka, manually_requested);
	}
	
	@SuppressWarnings("unused")
	CandidateAgentAddress()
	{
		
	}

	/**
	 * q
	 * 
	 * @see com.distrimind.madkit.kernel.AgentAddress#getRole()
	 */
	@Override
	final public String getRole() {
		return Organization.GROUP_CANDIDATE_ROLE;
	}

}

@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
final class GroupManagerAddress extends AgentAddress {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5757376397376189866L;
	private boolean securedGroup;
	@SuppressWarnings("unused")
	GroupManagerAddress()
	{
		
	}
	GroupManagerAddress(AbstractAgent agt, InternalRole role, KernelAddress ka, boolean manually_requested,
			boolean securedGroup) {
		super(agt, role, ka, manually_requested);
		this.securedGroup = securedGroup;
	}

	boolean isGroupSecured() {
		return securedGroup;
	}
	@Override
	public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException
	{
		super.readExternal(in);
		securedGroup=in.readBoolean();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeBoolean(securedGroup);
	}
	

}