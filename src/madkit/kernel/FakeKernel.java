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
//		if (agent != null) {
//			final AgentExecutor ae = agent.getAgentExecutor();
//			if(ae != null){
//				ae.shutdownNow();
//			}
//		}
		return agent != null ? agent.toString() : "";
	}
	
	@Override
	final ReturnCode createGroup(final AbstractAgent agent, final String community, final String group, final String description, final Gatekeeper gatekeeper, final boolean isDistributed) {
		throw new KernelException(buildFailString(agent));
	}
	
	@Override
	final ReturnCode requestRole(AbstractAgent agent, String community, String group, String role, Object memberCard) {
		throw new KernelException(buildFailString(agent));
	}


	@Override
	final ReturnCode leaveGroup(final AbstractAgent agent, final String community, final String group) {
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final ReturnCode leaveRole(AbstractAgent agent, String community,String group, String role) {
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final AgentAddress getAgentWithRole(final AbstractAgent agent, final String community, final String group, final String role){ 
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final List<AgentAddress> getAgentsWithRole(AbstractAgent agent, String community, String group, String role,boolean callerIncluded){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final boolean isCommunity(AbstractAgent agent,String community){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final boolean isGroup(AbstractAgent agent, String community, String group){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final boolean isRole(AbstractAgent agent, String community, String group, String role){
		throw new KernelException(buildFailString(agent));
	}
	
	@Override
	final public boolean isKernelOnline() {
		throw new KernelException(buildFailString(null));
	}
	
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////
	@Override
	final ReturnCode sendMessage(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, final String senderRole) {
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final ReturnCode sendMessage(AbstractAgent agent, AgentAddress receiver, final Message messageToSend, final String senderRole){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final ReturnCode broadcastMessageWithRole(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		throw new KernelException(buildFailString(agent));
	}

	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
//	AbstractAgent launchAgent(AbstractAgent agent, String agentClass, int timeOutSeconds,  boolean defaultGUI){
//		throw new KernelException(buildFailString(agent));
//	}

	@Override
	final ReturnCode launchAgent(final AbstractAgent agent, final AbstractAgent agent2, final int timeOutSeconds, final boolean defaultGUI){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent agent, String agentClassName,int bucketSize,Collection<String> rolesName){
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent agent,final AbstractAgent target, int timeOutSeconds){
		throw new KernelException(buildFailString(agent));
	}

	/**
	 * @param scheduler
	 * @param activator
	 */
	@Override
	final boolean removeOverlooker(final AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw new KernelException(buildFailString(agent));
	}

	/**
	 * @param agent
	 * @param o
	 * @return
	 */
	@Override
	final boolean addOverlooker(AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw new KernelException(buildFailString(agent));
	}

	/**
	 * @return
	 */
	@Override
	final public KernelAddress getKernelAddress() {
		throw new KernelException(buildFailString(null));
	}

	/**
	 * @param key
	 * @param value
	 */
	@Override
	final void setMadkitProperty(final AbstractAgent agent, String key, String value) {
		throw new KernelException(buildFailString(agent));
	}
	
	@Override
	final public java.net.URLClassLoader getMadkitClassLoader() {
		throw new KernelException(buildFailString(null));
	}
	
//	/**
//	 * @param abstractAgent
//	 * @return
//	 */
//	Component getGUIComponentOf(AbstractAgent agent) {
////		throw new KernelException(buildFailString(agent));
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
	@Override
	final ReturnCode reloadClass(AbstractAgent abstractAgent, String agentClass) throws ClassNotFoundException {
		throw new KernelException(buildFailString(null));
	}
	
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
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final boolean createGroupIfAbsent(AbstractAgent agent,
			String community, String group, String group2,
			Gatekeeper gatekeeper, boolean isDistributed) {
		throw new KernelException(buildFailString(agent));
	}

	@Override
	final Class<?> getNewestClassVersion(AbstractAgent agent, String className) throws ClassNotFoundException {
		throw new KernelException(buildFailString(agent));
	}

}
