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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.parseBoolean;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.Utils.*;

/**
 * @author Fabien Michel
 * @since MadKit 3.0
 * @version 5.0
 */
final class Organization extends ConcurrentHashMap <String, Group>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1547623313555380703L;
	private final Logger logger;
	private final String communityName;
	private final transient MadkitKernel myKernel;

	/**
	 * @return the myKernel
	 */
	final MadkitKernel getMyKernel() {
		return myKernel;
	}

	/**
	 * @param setLogging
	 * @param string 
	 */
	Organization(final String string, final MadkitKernel madkitKernel) {
		communityName = string;
		myKernel = madkitKernel;
//		Madkit platform = myKernel.getPlatform();
//		logger = platform.setLogging("[*"+platform.getPlatformID()+" "+communityName+"_OrgLogger*]",
//				! parseBoolean(platform.getConfigOption().getProperty(Madkit.noOrgConsoleLog)),
//				Level.parse(platform.getConfigOption().getProperty(Madkit.orgLogLevel)),
//				Arrays.asList(platform.getMadkitLogFileHandler()),
//				AgentLogger.agentFormatter);
		logger = null;
		if(logger != null)
			logger.finer(printCGR(communityName)+"created");
	}

	String getName(){
		return communityName;
	}

	/**
	 * @param creator
	 * @param groupName
	 * @param theIdentifier
	 * @param isDistributed 
	 * @return true if the group has been created
	 */
	boolean createGroup(final AbstractAgent creator, final String groupName, final GroupIdentifier theIdentifier, final boolean isDistributed) {
		
		final Group g = new Group(communityName,groupName,creator,theIdentifier,isDistributed, this);
//		g.getManager().compareAndSet(null, new AgentAddress(creator, null, getMyKernel().getKernelAddress()));
		if (putIfAbsent(groupName,g) == null) {// There was no such group
			if(logger != null)
				logger.fine(printCGR(communityName, groupName)+"created by "+creator.getName()+"\n");
			return true;
		}
		if(logger != null)
			logger.finer(printCGR(communityName, groupName)+"already exists: Creation aborted"+"\n");
		return false;
	}

	/**
	 * @param group
	 */
	void removeGroup(final String group) {
		if(logger != null)
			logger.finer("Removing"+printCGR(communityName, group));
		remove(group);
		if(isEmpty()){
			if(logger != null)
				logger.finer("Removing"+printCGR(communityName));
			myKernel.removeCommunity(communityName);
		}
	}

	/**
	 * @param theAgent
	 * @return all the groups that are distributed and that contained the agent
	 */
	ArrayList<String> removeAgentFromAllGroups(final AbstractAgent theAgent) {
		final ArrayList<String> groups = new ArrayList<String>();
		for (final Iterator<Map.Entry<String, Group>> e = this.entrySet().iterator();e.hasNext();) {
			final Map.Entry<String, Group> entry = e.next();
			final Group g = entry.getValue();
			if(g.leaveGroup(theAgent) == SUCCESS){
				if (g.isDistributed()) {
					groups.add(entry.getKey());
				}
				if(g.isEmpty())
					e.remove();
			}
		}
		return groups;

	}

	/**
	 * @param b
	 * @return
	 */
	SortedMap<String, SortedMap<String, List<AgentAddress>>> getOrgMap(boolean global) {
		SortedMap<String,SortedMap<String,List<AgentAddress>>> export = new TreeMap<String,SortedMap<String,List<AgentAddress>>>();
		for (Map.Entry<String, Group> org : entrySet()) {
			if (global || org.getValue().isDistributed()) {
				export.put(org.getKey(), org.getValue().getGroupMap());
			}
		}
		return export;
	}

	/**
	 * @param hashMap
	 */
	void importDistantOrg(SortedMap<String, SortedMap<String, List<AgentAddress>>> sortedMap) {
		for (String groupName : sortedMap.keySet()) {
			Group group = get(groupName);
			if(group == null){
				group = new Group(communityName, groupName,(AgentAddress)null, null, this);//TODO have to get the groupManager
				put(groupName, group);
			}
			group.importDistantOrg(sortedMap.get(groupName));
		}		
	}

	/**
	 * @param kernelAddress2
	 */
	void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		for (Group group : values()) {
			group.removeAgentsFromDistantKernel(kernelAddress2);
		}
	}
	
	
	Logger getLogger(){
		return logger;
	}
	

}

















