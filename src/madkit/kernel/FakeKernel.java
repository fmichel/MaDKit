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
import java.util.Properties;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 1.0
 * 
 */
class FakeKernel extends MadkitKernel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109903118706339496L;

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Agent interface
	///////////////////////////////////////////////////////////////////////////
	
	

	//////////////////////////////////////////////////////////////
	////////////////////////// Organization interface
	//////////////////////////////////////////////////////////////

	String buildFailString(final AbstractAgent agent){
		return agent != null ? agent.toString() : "Agent "+AbstractAgent.State.NOT_LAUNCHED.toString()+" : must be launched to use this method";
	}
	
	@Override
	final ReturnCode createGroup(final AbstractAgent agent, final String community, final String group, final String description, final Gatekeeper gatekeeper, final boolean isDistributed) {
		throw buildKernelException(agent);
	}
	
	AgentAddress getAgentAddressIn(AbstractAgent agent, String community, String group, String role) {
		throw buildKernelException(agent);
	}
	
	@Override
	final ReturnCode requestRole(AbstractAgent agent, String community, String group, String role, Object memberCard) {
		throw buildKernelException(agent);
	}


	@Override
	final ReturnCode leaveGroup(final AbstractAgent agent, final String community, final String group) {
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode leaveRole(AbstractAgent agent, String community,String group, String role) {
		throw buildKernelException(agent);
	}

	@Override
	final AgentAddress getAgentWithRole(final AbstractAgent agent, final String community, final String group, final String role){ 
		throw buildKernelException(agent);
	}

	@Override
	final List<AgentAddress> getAgentsWithRole(AbstractAgent agent, String community, String group, String role,boolean callerIncluded){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isCommunity(AbstractAgent agent,String community){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isGroup(AbstractAgent agent, String community, String group){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isRole(AbstractAgent agent, String community, String group, String role){
		throw buildKernelException(agent);
	}
	
	@Override
	final public boolean isKernelOnline() {
		throw buildKernelException(null);
	}
	
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////
	@Override
	final ReturnCode sendMessage(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, final String senderRole) {
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode sendMessage(AbstractAgent agent, AgentAddress receiver, final Message messageToSend, final String senderRole){
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode broadcastMessageWithRole(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		throw buildKernelException(agent);
	}

	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
//	AbstractAgent launchAgent(AbstractAgent agent, String agentClass, int timeOutSeconds,  boolean defaultGUI){
//		throw buildKernelException(agent);
//	}

	@Override
	final ReturnCode launchAgent(final AbstractAgent agent, final AbstractAgent agent2, final int timeOutSeconds, final boolean defaultGUI){
		throw buildKernelException(agent);
	}

	@Override
	final List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent agent, String agentClassName,int bucketSize,Collection<String> rolesName){
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent agent,final AbstractAgent target, int timeOutSeconds){
		throw buildKernelException(agent);
	}

	/**
	 * @param agent
	 * @return 
	 * @throws KernelException 
	 */
	private KernelException buildKernelException(final AbstractAgent agent) {
		final KernelException ke = new KernelException(buildFailString(agent));
		ke.printStackTrace();
		return ke;
	}

	@Override
	final boolean removeOverlooker(final AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw buildKernelException(agent);
	}

	@Override
	final boolean addOverlooker(AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw buildKernelException(agent);
	}

	/**
	 * @return
	 */
	@Override
	final public KernelAddress getKernelAddress() {
		throw buildKernelException(null);
	}

	@Override
	final void setMadkitProperty(final AbstractAgent agent, String key, String value) {
		throw buildKernelException(agent);
	}
	
	@Override
	final public MadkitClassLoader getMadkitClassLoader() {
		throw buildKernelException(null);
	}
	
//	/**
//	 * @param abstractAgent
//	 * @return
//	 */
//	Component getGUIComponentOf(AbstractAgent agent) {
////		throw buildKernelException(agent);
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

	@Override
	final public Properties getMadkitConfig() {
		final Madkit m = Madkit.getCurrentInstance();
		if (m != null) {
			return m.getConfigOption();
		}
		return Madkit.defaultConfig;
	}

	@Override
	final List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent agent,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		throw buildKernelException(agent);
	}

	@Override
	final boolean createGroupIfAbsent(AbstractAgent agent,
			String community, String group, String group2,
			Gatekeeper gatekeeper, boolean isDistributed) {
		throw buildKernelException(agent);
	}

}
