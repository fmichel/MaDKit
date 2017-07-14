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

import static madkit.i18n.I18nUtilities.getCGRString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import madkit.i18n.ErrorMessages;

/**
 * @author Fabien Michel
 * @since MaDKit 3.0
 * @version 5.1
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
		communityName = Objects.requireNonNull(string, ErrorMessages.C_NULL.toString());
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
		final ArrayList<String> groups = new ArrayList<>();
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
		Map<String, Map<String, Set<AgentAddress>>> export = new TreeMap<>();
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
					manager = map.get(groupName).get(madkit.agr.DefaultMaDKitRoles.GROUP_MANAGER_ROLE).iterator().next();
				} catch (NullPointerException e) {//TODO a clean protocol to get the groupManager
					manager = map.get(groupName).values().iterator().next().iterator().next();
				}
				group = new Group(communityName, groupName,manager, this);
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

















