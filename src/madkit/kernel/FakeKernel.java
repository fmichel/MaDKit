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

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 1.0
 * 
 */
final class FakeKernel extends MadkitKernel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109903118706339496L;

	FakeKernel(Madkit m) {
		super(m);
	}

	final static ReturnCode fakeKernelWarning(final AbstractAgent agent) {
			throw new KernelException(agent);
	}

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Agent interface
	///////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////
	////////////////////////// Organization interface
	//////////////////////////////////////////////////////////////

	ReturnCode createGroup(final AbstractAgent agent, final String community, final String group, final String description, final GroupIdentifier theIdentifier, final boolean isDistributed) {
		throw new KernelException(agent);
	}


	ReturnCode requestRole(AbstractAgent agent, String community, String group, String role, Object memberCard) {
		throw new KernelException(agent);
	}


	ReturnCode leaveGroup(final AbstractAgent agent, final String community, final String group) {
		throw new KernelException(agent);
	}

	ReturnCode leaveRole(AbstractAgent agent, String community,String group, String role) {
		throw new KernelException(agent);
	}

	AgentAddress getAgentWithRole(final AbstractAgent agent, final String community, final String group, final String role){ 
		throw new KernelException(agent);
	}

	List<AgentAddress> getAgentsWithRole(AbstractAgent agent, String community, String group, String role){
		throw new KernelException(agent);
	}

	boolean isCommunity(AbstractAgent agent,String community){
		throw new KernelException(agent);
	}

	boolean isGroup(AbstractAgent agent, String community, String group){
		throw new KernelException(agent);
	}

	boolean isRole(AbstractAgent agent, String community, String group, String role){
		throw new KernelException(agent);
	}
	
	@Override
	public boolean isKernelConnected() {
		throw new KernelException(null);
	}
	
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////
	ReturnCode sendMessage(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, final String senderRole) {
		throw new KernelException(agent);
	}

	ReturnCode sendMessage(AbstractAgent agent, AgentAddress receiver, final Message messageToSend, final String senderRole){
		throw new KernelException(agent);
	}

	ReturnCode broadcastMessageWithRole(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		throw new KernelException(agent);
	}

	ReturnCode sendReplyWithRole(final AbstractAgent agent, final Message messageToReplyTo,final Message reply, String senderRole) {
		throw new KernelException(agent);
	}
	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
	AbstractAgent launchAgent(AbstractAgent agent, String agentClass, int timeOutSeconds,  boolean defaultGUI){
		throw new KernelException(agent);
	}

	ReturnCode launchAgent(final AbstractAgent agent, final AbstractAgent agent2, final int timeOutSeconds, final boolean defaultGUI){
		throw new KernelException(agent);
	}

	List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent agent, String agentClassName,int bucketSize,Collection<String> rolesName){
		throw new KernelException(agent);
	}

	ReturnCode killAgent(final AbstractAgent agent,final AbstractAgent target, int timeOutSeconds){
		throw new KernelException(agent);
	}

	/**
	 * @param scheduler
	 * @param activator
	 */
	boolean removeOverlooker(final AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw new KernelException(agent);
	}

	/**
	 * @param agent
	 * @param o
	 * @return
	 */
	boolean addOverlooker(AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw new KernelException(agent);
	}

	/**
	 * @return
	 */
	public KernelAddress getKernelAddress() {
		throw new KernelException(null);
	}

	/**
	 * @param key
	 * @param value
	 */
	@Override
	void setMadkitProperty(final AbstractAgent agent, String key, String value) {
		throw new KernelException(agent);
	}

//	/**
//	 * @param abstractAgent
//	 * @return
//	 */
//	Component getGUIComponentOf(AbstractAgent agent) {
////		throw new KernelException(agent);
//		return null;
//	}
//
//	/**
//	 * @param abstractAgent
//	 * @param location
//	 */
//	void setGUILocationOf(AbstractAgent abstractAgent, Point location) {
//		fakeKernelWarning(abstractAgent);
//	}

	/**
	 * @param abstractAgent
	 * @param agentClass
	 * @throws ClassNotFoundException 
	 */
	ReturnCode reloadClass(AbstractAgent abstractAgent, String agentClass) throws ClassNotFoundException {
		throw new KernelException(null);
	}

	/**
	 * @param abstractAgent
	 * @param key
	 * @return
	 */
	String getMadkitProperty(AbstractAgent abstractAgent, String key) {//TODO this could have some wired side effects
		Madkit m = Madkit.getCurrentInstance();
		if (m != null) {
			return m.getConfigOption().getProperty(key);
		}
		else{
			return Madkit.defaultConfig.getProperty(key);
		}
	}


	@Override
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent agent,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		// TODO Auto-generated method stub
		throw new KernelException(agent);
	}

	boolean createGroupIfAbsent(AbstractAgent agent,
			String community, String group, String group2,
			GroupIdentifier theIdentifier, boolean isDistributed) {
		throw new KernelException(agent);
	}

	Class<?> getNewestClassVersion(AbstractAgent agent, String className) throws ClassNotFoundException {
		throw new KernelException(agent);
	}

}
