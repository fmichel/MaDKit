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

import static java.lang.Boolean.parseBoolean;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.TERMINATED_AGENT;
import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFrame;

import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 * 
 */
final class RootKernel extends MadkitKernel{

	RootKernel(Madkit m) {
		super(m);
	}

	final ReturnCode fakeKernelWarning(final AbstractAgent requester) {
		//		new Exception().printStackTrace();
		switch (requester.getState()) {
		case TERMINATED:
			return requester.getLogger() == null ? TERMINATED_AGENT : requester.handleException(new NotAvailableActionWarning(TERMINATED_AGENT," agent already terminated"));
		default:
			return requester.getLogger()== null ? NOT_YET_LAUNCHED : requester.handleException(new NotAvailableActionWarning(NOT_YET_LAUNCHED," agent not launched"));
		}
	}

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Agent interface
	///////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////
	////////////////////////// Organization interface
	//////////////////////////////////////////////////////////////

	ReturnCode createGroup(final AbstractAgent creator, final String community, final String group, final String description, final GroupIdentifier theIdentifier, final boolean isDistributed) {
		return fakeKernelWarning(creator);
	}


	ReturnCode requestRole(AbstractAgent requester, String community, String group, String role, Object memberCard) {
		return fakeKernelWarning(requester);
	}


	ReturnCode leaveGroup(final AbstractAgent requester, final String community, final String group) {
		return fakeKernelWarning(requester);
	}

	ReturnCode leaveRole(AbstractAgent requester, String community,String group, String role) {
		return fakeKernelWarning(requester);
	}

	AgentAddress getAgentWithRole(final AbstractAgent requester, final String community, final String group, final String role){ 
		fakeKernelWarning(requester);
		return null;
	}

	List<AgentAddress> getAgentsWithRole(AbstractAgent requester, String community, String group, String role){
		fakeKernelWarning(requester);
		return null;
	}

	boolean isCommunity(AbstractAgent requester,String community){
		fakeKernelWarning(requester);
		return false;
	}

	boolean isGroup(AbstractAgent requester, String community, String group){
		fakeKernelWarning(requester);
		return false;
	}

	boolean isRole(AbstractAgent requester, String community, String group, String role){
		fakeKernelWarning(requester);
		return false;
	}
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////
	ReturnCode sendMessage(final AbstractAgent requester, final String community, final String group, final String role, final Message messageToSend, final String senderRole) {
		return fakeKernelWarning(requester);
	}

	ReturnCode sendMessage(AbstractAgent requester, AgentAddress receiver, final Message messageToSend, final String senderRole){
		return fakeKernelWarning(requester);
	}

	ReturnCode broadcastMessageWithRole(final AbstractAgent requester, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		return fakeKernelWarning(requester);
	}

	ReturnCode sendReplyWithRole(final AbstractAgent requester, final Message messageToReplyTo,final Message reply, String senderRole) {
		return fakeKernelWarning(requester);
	}
	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////
	AbstractAgent launchAgent(AbstractAgent requester, String agentClass, int timeOutSeconds,  boolean defaultGUI){
		fakeKernelWarning(requester);
		return null;
	}

	ReturnCode launchAgent(final AbstractAgent requester, final AbstractAgent agent, final int timeOutSeconds, final boolean defaultGUI){
		return fakeKernelWarning(requester);
	}

	List<AbstractAgent> launchAgentBucketWithRoles(final AbstractAgent requester, String agentClassName,int bucketSize,Collection<String> rolesName){
		fakeKernelWarning(requester);
		return null;
	}

	ReturnCode killAgent(final AbstractAgent requester,final AbstractAgent target, int timeOutSeconds){
		return fakeKernelWarning(requester);
	}

	/**
	 * @param scheduler
	 * @param activator
	 */
	boolean removeOverlooker(final AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		fakeKernelWarning(requester);
		return false;
	}

	/**
	 * @param requester
	 * @param o
	 * @return
	 */
	boolean addOverlooker(AbstractAgent requester, Overlooker<? extends AbstractAgent> o) {
		fakeKernelWarning(requester);
		return false;
	}

	/**
	 * @return
	 */
	public KernelAddress getKernelAddress() {
		return null;
	}

	/**
	 * @param key
	 * @param value
	 */
	@Override
	void setMadkitProperty(final AbstractAgent requester, String key, String value) {
		fakeKernelWarning(requester);
	}

//	/**
//	 * @param abstractAgent
//	 * @return
//	 */
//	Component getGUIComponentOf(AbstractAgent requester) {
////		fakeKernelWarning(requester);
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
		return fakeKernelWarning(abstractAgent);
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
	List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent requester,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		// TODO Auto-generated method stub
		fakeKernelWarning(requester);
		return null;
	}

	boolean createGroupIfAbsent(AbstractAgent abstractAgent,
			String community, String group, String group2,
			GroupIdentifier theIdentifier, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, group2, theIdentifier, isDistributed) == ReturnCode.SUCCESS;
	}

	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {
		fakeKernelWarning(requester);
		return null;
	}

}
