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
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * Reifying the notion of Role in AGR
 * 
 * @author Fabien Michel
 * @since MaDKit 3.0
 * @version 5.0
 */
class Role implements Serializable {// TODO test with arraylist

    private static final long serialVersionUID = 4447153943733812916L;

    protected final transient List<AbstractAgent> players;// TODO test copyonarraylist and linkedhashset
    private transient List<AbstractAgent> tmpReferenceableAgents;
    protected transient Set<AgentAddress> agentAddresses;
    protected transient boolean modified = true;
    private final transient Set<Overlooker<? extends AbstractAgent>> overlookers;
    protected final transient Group myGroup;
    private final transient Logger logger;
    private final transient KernelAddress kernelAddress;

    private final String communityName;
    private final String groupName;
    private final String roleName;

    /**
     * @return the kernelAddress
     */
    KernelAddress getKernelAddress() {
	return kernelAddress;
    }

    Role(final Group groupObject, final String roleName) {
	players = new ArrayList<>();
	tmpReferenceableAgents = new ArrayList<>();// should not be necessary but ...
	communityName = groupObject.getCommunityObject().getName();
	groupName = groupObject.getName();
	this.roleName = roleName;
	final MadkitKernel k = groupObject.getCommunityObject().getMyKernel();
	logger = groupObject.getCommunityObject().getLogger();
	myGroup = groupObject;
	kernelAddress = k.getKernelAddress();
	if (logger != null) {
	    logger.finer(() -> toString() + " created");
	}
	overlookers = new LinkedHashSet<>();
	initializeOverlookers();
    }

    // @Override
    // public boolean equals(Object obj) { //override should not be required
    // if(this == obj)
    // return true;
    // Role other = (Role) obj;
    // return communityName.equals(other.communityName) &&
    // groupName.equals(other.groupName) &&
    // roleName.equals(other.roleName);
    // }

    private synchronized void initializeOverlookers() {
	for (final Overlooker<? extends AbstractAgent> o : myGroup.getCommunityObject().getMyKernel().getOperatingOverlookers()) {
	    if (o.getRole().equals(roleName) && o.getGroup().equals(groupName) && o.getCommunity().equals(communityName))
		addOverlooker(o);
	}
    }

    /**
     * this is dirty but... This represents a Group
     * 
     * @param community
     * @param group
     */
    Role(final String community, final String group) {
	communityName = community;
	groupName = group;
	roleName = null;
	players = null;
	overlookers = null;
	myGroup = null;
	logger = null;
	kernelAddress = null;
    }

    /**
     * @return the players
     */
    List<AbstractAgent> getPlayers() {
	return players;
    }

    /**
     * @return the myGroup
     */
    Group getMyGroup() {
	return myGroup;
    }

    /**
     * @return the communityName
     */
    final String getCommunityName() {
	return communityName;
    }

    /**
     * @return the groupName
     */
    final String getGroupName() {
	return groupName;
    }

    final void addOverlooker(final Overlooker<? extends AbstractAgent> o) {
	overlookers.add(o);
	o.setOverlookedRole(this);
    }

    final void removeOverlooker(final Overlooker<? extends AbstractAgent> o) {
	overlookers.remove(o);
	o.setOverlookedRole(null);
    }

    /**
     * @return the roleName
     */
    final String getRoleName() {
	return roleName;
    }

    @Override
    public String toString() {
	return getCGRString(communityName, groupName, roleName);
    }

    /**
     * add the agent to the role
     * 
     * @param roleName
     * @param groupName
     * @param communityName
     * @param agent
     *            the agent
     * @return
     * @return true, if the agent has been added.
     */
    boolean addMember(final AbstractAgent requester) {
	synchronized (players) {
	    if (players.contains(requester)) {// TODO looks like I should use linkedhashset
		return false;
	    }
	    players.add(requester);
	    if (logger != null) {
		logger.finest(requester.getName() + " is now playing " + getCGRString(communityName, groupName, roleName));
	    }
	    // System.err.println(requester.getName() + " is now playing " + getCGRString(communityName, groupName, roleName));
	    // System.err.println(this+" current players---\n"+players+"\n\n");
	    if (agentAddresses != null) {
		agentAddresses.add(new AgentAddress(requester, this, kernelAddress));
	    }
	    modified = true;
	}
	// needs to be synchronized so that adding occurs prior to getAgentList
	// So addToOverlookers(requester); has to be called in group

	return true;
    }

    final void addMembers(final List<AbstractAgent> bucket, final boolean roleJustCreated) {
	// System.err.println("add members "+bucket.size());
	synchronized (players) {
	    players.addAll(bucket);// is optimized wrt size
	    if (agentAddresses != null) {
		final Set<AgentAddress> addresses = new HashSet<>(bucket.size() + agentAddresses.size(), 0.9f);// TODO try load factor
		for (final AbstractAgent a : bucket) {
		    addresses.add(new AgentAddress(a, this, kernelAddress));
		}
		addresses.addAll(agentAddresses);// TODO test vs assignment : this because knowing the size
		agentAddresses = addresses;
	    }
	    modified = true;
	}
	if (roleJustCreated) {
	    initializeOverlookers();
	}
	else {
	    addToOverlookers(bucket);
	}
    }

    /**
     * @param content
     */
    final void addDistantMember(final AgentAddress content) {
	synchronized (players) {
	    content.setRoleObject(this);// required for equals to work
	    buildAndGetAddresses().add(content);
	}
    }

    ReturnCode removeMember(final AbstractAgent requester) {
	int index;
	synchronized (players) {
	    index = players.indexOf(requester);
	    if (index < 0) {
		if (myGroup.isIn(requester)) {
		    return ROLE_NOT_HANDLED;
		}
		return ReturnCode.NOT_IN_GROUP;
	    }
	}
	removeFromOverlookers(requester);
	synchronized (players) {
	    players.remove(index);
	    if (agentAddresses != null) {
		removeAgentAddressOf(requester, agentAddresses).setRoleObject(null);
	    }
	    if (logger != null) {
		logger.finest(requester.getName() + " has leaved role " + getCGRString(communityName, groupName, roleName) + "\n");
	    }
	    modified = true;
	}
	checkEmptyness();
	return SUCCESS;
    }

    final void removeMembers(final List<AbstractAgent> bucket) {
	synchronized (players) {
	    removeFromOverlookers(bucket);
	    players.removeAll(bucket);// is optimized
	    if (agentAddresses != null) {
		for (Iterator<AgentAddress> i = agentAddresses.iterator(); i.hasNext();) {
		    AgentAddress aa = i.next();
		    AbstractAgent agent = aa.getAgent();
		    if (agent != null && bucket.remove(agent)) {
			i.remove();
			aa.setRoleObject(null);// cost is high because of string creation...
		    }
		}
	    }
	    modified = true;
	}
    }

    /**
     * @param content
     */
    void removeDistantMember(final AgentAddress content) {
	if (agentAddresses != null) {
	    synchronized (players) {
		removeAgentAddress(content);
		checkEmptyness();
	    }
	}
    }

    final Set<AgentAddress> buildAndGetAddresses() {// TODO lambda
	if (agentAddresses == null) {
	    agentAddresses = new HashSet<>(players.size(), 0.8f);
	    synchronized (players) {
		for (final AbstractAgent a : players) {
		    agentAddresses.add(new AgentAddress(a, this, kernelAddress));
		}
	    }
	}
	return agentAddresses;
    }

    private final void removeAgentAddress(AgentAddress aa) {
	if (agentAddresses.remove(aa)) {
	    if (logger != null) {
		logger.finest(aa + " has leaved role " + getCGRString(communityName, groupName, roleName) + "\n");
	    }
	    aa.setRoleObject(null);
	}
    }

    /**
     * @param kernelAddress2
     */
    void removeAgentsFromDistantKernel(final KernelAddress kernelAddress2) {
	if (agentAddresses != null) {
	    if (logger != null)
		logger.finest(() -> "Removing all agents from distant kernel " + kernelAddress2 + " in" + this);
	    synchronized (players) {
		for (Iterator<AgentAddress> iterator = buildAndGetAddresses().iterator(); iterator.hasNext();) {// TODO return if no agent addresses
		    AgentAddress aa = iterator.next();
		    if (aa.getKernelAddress().equals(kernelAddress2)) {
			iterator.remove();
			aa.setRoleObject(null);
		    }
		}
		checkEmptyness();
	    }
	}
    }

    void checkEmptyness() {
	if ((players == null || players.isEmpty()) && (agentAddresses == null || agentAddresses.isEmpty())) {
	    cleanAndRemove();
	}
    }

    /**
     * 
     */
    private void cleanAndRemove() {
	for (final Overlooker<? extends AbstractAgent> o : overlookers) {
	    o.setOverlookedRole(null);
	}
	myGroup.removeRole(roleName);
	// overlookers = null;
	tmpReferenceableAgents = null;
	// players = null;
	agentAddresses = null;
    }

    final void destroy() {
	if (agentAddresses != null) {
	    for (AgentAddress aa : agentAddresses) {
		aa.setRoleObject(null);// TODO optimize
	    }
	}
	cleanAndRemove();
    }

    final List<AgentAddress> getAgentAddressesCopy() {
	synchronized (players) {
	    return new ArrayList<>(buildAndGetAddresses());
	}
    }

    /**
     * @param requester
     */
    static AgentAddress removeAgentAddressOf(final AbstractAgent requester, final Collection<AgentAddress> agentAddresses2) {
	// if(requester == null)
	// throw new AssertionError("Wrong use ^^");
	for (final Iterator<AgentAddress> iterator = agentAddresses2.iterator(); iterator.hasNext();) {
	    try {
		final AgentAddress aa = iterator.next();
		if (aa.getAgent() == requester) {// TODO test speed with hashcode test
		    iterator.remove();
		    return aa;
		}
	    }
	    catch(NullPointerException e) {
		e.printStackTrace();
	    }
	}
	return null;
    }

    /**
     * @param abstractAgent
     * @return the AA of the abstractAgent in this Role
     */
    final AgentAddress getAgentAddressInGroup(final AbstractAgent abstractAgent) {
	final AgentAddress aa = getAgentAddressOf(abstractAgent);
	if (aa != null)
	    return aa;
	return myGroup.getAgentAddressOf(abstractAgent);
    }

    final List<AbstractAgent> getAgentsList() {
	if (modified) {
	    synchronized (players) {
		modified = false;
		// long startTime = System.nanoTime();
		tmpReferenceableAgents = new ArrayList<>(players);// new seems a little bit better
		// tmpReferenceableAgents = (ArrayList<AbstractAgent>)referenceableAgents.clone();
		// long estimatedTime = System.nanoTime() - startTime; System.err.println(estimatedTime);
	    }
	}
	return tmpReferenceableAgents;
    }

    final void addToOverlookers(AbstractAgent a) {
	overlookers.parallelStream().forEach(o -> o.addAgent(a));
    }

    private final void addToOverlookers(List<AbstractAgent> l) {
	overlookers.parallelStream().forEach(o -> o.addAgents(l));
    }

    final void removeFromOverlookers(AbstractAgent a) {
	overlookers.parallelStream().forEach(o -> o.removeAgent(a));
    }

    private final void removeFromOverlookers(List<AbstractAgent> l) {
	overlookers.parallelStream().forEach(o -> o.removeAgents(l));
    }

    /**
     * importation when connecting to other kernel
     * 
     * @param list
     */
    void importDistantOrg(Set<AgentAddress> list) {
	synchronized (players) {
	    buildAndGetAddresses();
	    for (final AgentAddress aa : list) {
		aa.setRoleObject(this);
		agentAddresses.add(aa);
	    }
	}

    }

    AgentAddress getAgentAddressOf(final AbstractAgent a) {
	final int hash = a.hashCode();
	// final KernelAddress ka = a.getKernelAddress();
	synchronized (players) {
	    for (final AgentAddress aa : buildAndGetAddresses()) {// TODO when offline second part is useless
		if (aa.hashCode() == hash && aa.getAgent() != null)// && ka.equals(aa.getKernelAddress()))
		    return aa;
	    }
	}
	return null;
    }

    /**
     * @param aa
     * @return the AbstractAgent corresponding to the aa agentAddress in this role, null if it does no longer play this role
     */
    AbstractAgent getAbstractAgentWithAddress(AgentAddress aa) {
	if (players != null) {
	    final int hash = aa.hashCode();
	    synchronized (players) {
		for (final AbstractAgent agent : players) {
		    if (agent.hashCode() == hash)
			return agent;
		}
	    }
	}
	return null;
    }

    final boolean contains(AbstractAgent agent) {
	return players.contains(agent);
    }

    /**
     * Return the reference agent address
     * 
     * @param aa
     * @return <code>null</code> if it is not contained in this role anymore
     */
    final AgentAddress resolveAgentAddress(AgentAddress anAA) {
	for (final AgentAddress aa : buildAndGetAddresses()) {
	    if (aa.equals(anAA)) {
		return aa;
	    }
	}
	return null;
    }

}