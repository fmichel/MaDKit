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
class RootKernel {

	void kernelLog(String message, Level logLvl, Throwable e){};

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
	KernelAddress getKernelAddress(AbstractAgent requester) {
		fakeKernelWarning(requester);
		return null;
	}

	/**
	 * @param networkAgent
	 * @param org
	 */
	synchronized void importDistantOrg(
			NetworkAgent networkAgent,
			HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> org) {
		fakeKernelWarning(networkAgent);
	}

	/**
	 * @param logger
	 * @param finer
	 */
	void logCurrentOrganization(AbstractAgent requester, Level finer) {
		fakeKernelWarning(requester);
	}

	/**
	 * @param networkAgent
	 * @return
	 */
	HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> getLocalOrg(
			NetworkAgent networkAgent) {
		fakeKernelWarning(networkAgent);
		return null;
	}

	/**
	 * @param networkAgent
	 * @param operation
	 * @param content
	 */
	void injectOperation(NetworkAgent networkAgent, int operation,
			AgentAddress content) {
		fakeKernelWarning(networkAgent);
	}

	/**
	 * @param networkAgent
	 * @param content
	 */
	void injectMessage(NetworkAgent networkAgent, Message content) {
		fakeKernelWarning(networkAgent);
	}

	/**
	 * @param networkAgent
	 * @param kernelAddress
	 */
	void removeAgentsFromDistantKernel(NetworkAgent networkAgent,
			KernelAddress kernelAddress) {

	}

	/**
	 * @param abstractAgent
	 */
	void disposeGUIOf(AbstractAgent abstractAgent) {//move that in madkit kernel
		fakeKernelWarning(abstractAgent);
		throw new AssertionError("This sould not be possible");
	}

	/**
	 * @param theAgent
	 */
	void removeAgentFromOrganizations(AbstractAgent theAgent) {
		fakeKernelWarning(theAgent);
		throw new AssertionError("This sould not be possible");
	}

	/**
	 * @param key
	 * @param value
	 */
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

	/**
	 * @param abstractAgent
	 * @param logger 
	 * @param newLevel
	 * @param warningLogLevel 
	 */
	void setLogLevel(AbstractAgent requester, String loggerName, Level newLevel, Level warningLogLevel) {
		if(requester.logger == AbstractAgent.defaultLogger){
			requester.logger = null;
		}
		final AgentLogger currentLogger = requester.getLogger();
		if(currentLogger != null){
			currentLogger.setWarningLogLevel(warningLogLevel);
			currentLogger.setLevel(newLevel);
		}
		if (newLevel.equals(Level.OFF)) {
			requester.logger = null;
			return;
		}
		//the logger is null or has not the right name: find if the right one was already created
		if (currentLogger == null || ! currentLogger.getName().equals(loggerName)) {
			AgentLogger newLogger = new AgentLogger(loggerName);
			if (LogManager.getLogManager().addLogger(newLogger)) {// This is a new logger
				newLogger.init(requester, currentLogger, 
						!parseBoolean(getMadkitProperty(requester, Madkit.noAgentConsoleLog)), 
						parseBoolean(getMadkitProperty(requester, Madkit.createLogFiles)) ?
								getMadkitProperty(requester, Madkit.logDirectory)
								: null,
						getMadkitProperty(requester, Madkit.agentsLogFile));
				requester.logger = newLogger;
			} else { // if it already exists : get it !
				requester.logger = (AgentLogger) Logger.getLogger(loggerName);
			}
		}
		requester.getLogger().setLevel(newLevel);
		requester.getLogger().setWarningLogLevel(warningLogLevel);
	}

//	/**
//	 * @param agent
//	 */
//	void removeThreadedAgent(Agent agent) {
//		fakeKernelWarning(agent);
//		throw new AssertionError("This sould not be possible");
//	}

	List<Message> broadcastMessageWithRoleAndWaitForReplies(Agent requester,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		// TODO Auto-generated method stub
		fakeKernelWarning(requester);
		return null;
	}

	public boolean createGroupIfAbsent(AbstractAgent abstractAgent,
			String community, String group, String group2,
			GroupIdentifier theIdentifier, boolean isDistributed) {
		return createGroup(abstractAgent, community, group, group2, theIdentifier, isDistributed) == ReturnCode.SUCCESS;
	}

	Class<?> getNewestClassVersion(AbstractAgent requester, String className) throws ClassNotFoundException {
		fakeKernelWarning(requester);
		return null;
	}

}
