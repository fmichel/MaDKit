/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.i18n.I18nUtilities.getCGRString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.i18n.ErrorMessages;

/**
 * @author Fabien Michel
 * @since MaDKit 3.0
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
		if(string == null)
			throw new NullPointerException(ErrorMessages.C_NULL.toString());
		communityName = string;
		myKernel = madkitKernel;
//		logger = madkitKernel.getLogger();
		logger = null;
		if(logger != null)
			logger.finer(getCGRString(communityName)+"created");
	}

	String getName(){
		return communityName;
	}

	/**
	 * Group adding. Guarded by this in {@link MadkitKernel#createGroup(AbstractAgent, String, String, Gatekeeper, boolean)}
	 * 
	 * @param creator
	 * @param gatekeeper 
	 * @param group 
	 * @param groupName
	 * @param gatekeeper
	 * @param isDistributed 
	 * @return true if the group has been created
	 */
	boolean addGroup(final AbstractAgent creator, String group, Gatekeeper gatekeeper, boolean isDistributed) {
		Group g = get(group);
		if(g == null){// There was no such group
			g = new Group(communityName,group,creator,gatekeeper,isDistributed,this);
			put(group,g);
			if(logger != null)
				logger.fine(getCGRString(communityName, group)+"created by "+creator.getName()+"\n");
			return true;
		}
		if(logger != null)
			logger.finer(getCGRString(communityName, group)+"already exists: Creation aborted"+"\n");
		return false;
	}

	/**
	 * @param group
	 */
	void removeGroup(final String group) {
		synchronized (this) {
			if (logger != null)
				logger.finer("Removing" + getCGRString(communityName, group));
			remove(group);
			checkEmptyness();
		}
	}

	private void checkEmptyness() {
		if(isEmpty()){
			if(logger != null)
				logger.finer("Removing"+getCGRString(communityName));
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
			if(g.leaveGroup(theAgent) != null){//at least present in one group
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
	Map<String, Map<String, Set<AgentAddress>>> getOrgMap(boolean global) {
		Map<String, Map<String, Set<AgentAddress>>> export = new TreeMap<String,Map<String,Set<AgentAddress>>>();
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
	void importDistantOrg(Map<String, Map<String, Set<AgentAddress>>> map) {
		for (String groupName : map.keySet()) {
			Group group = get(groupName);
			if(group == null){
				AgentAddress manager = null;
				try {
					manager = map.get(groupName).get(madkit.agr.Organization.GROUP_MANAGER_ROLE).iterator().next();
				} catch (NullPointerException e) {//TODO have to fix that
				}
				group = new Group(communityName, groupName,manager, null, this);//TODO have to get the groupManager
				put(groupName, group);
			}
			group.importDistantOrg(map.get(groupName));
		}		
	}

	/**
	 * @param kernelAddress2
	 */
	void removeAgentsFromDistantKernel(KernelAddress kernelAddress2) {
		for (Group group : values()) {
			if (group.isDistributed()) {
				group.removeAgentsFromDistantKernel(kernelAddress2);
			}
		}
	}
	
	
	Logger getLogger(){
		return logger;
	}

	void destroy() {
		for(final Group g : values()){
			g.destroy();
		}
		myKernel.removeCommunity(communityName);
	}

	
}

















