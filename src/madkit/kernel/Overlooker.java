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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Fabien Michel
 * @since MadKit 2.1
 * @version 5.0
 * @param <A> The agent most generic type for this Overlooker
 * 
 */
@SuppressWarnings("unchecked")
abstract class Overlooker <A extends AbstractAgent>
{ 
	private Role overlookedRole;
	final private String community;
	final private String group;
	final private String role;

	/**
	 * Builds a new Activator or Probe on the given CGR location of the
	 * artificial society.
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 */
	Overlooker(final String communityName, final String groupName, final String roleName)
	{
		community=communityName;
		group=groupName;
		role=roleName;
	}

	synchronized final void setOverlookedRole(final Role theRole)
	{
		overlookedRole = theRole;
		if(theRole != null)
			try {
				initialize();
			} catch (Throwable e) {
				System.err.println("\n-----MADKIT WARNING : problem during initialize()-----\n-----Problem on "+this.getClass().getSimpleName()+" on <"+community+";"+group+";"+role+">-----\n-----Method call is at:");
				e.printStackTrace(); //TODO find another way
			}
	}

	/**
	 * @return the overlookedRole
	 */
	final Role getOverlookedRole() {
		return overlookedRole;
	}


	//	@SuppressWarnings("unchecked")
	//	final public A getAgentNb(final int nb)
	//	{
	//		final List<A> l = getCurrentAgentsList();
	//		return l.get(nb);
	//	}
	
		final public String getCommunity()  {	return community;   }

	//	@SuppressWarnings("unchecked")
		//	final public A getAgentNb(final int nb)
		//	{
		//		final List<A> l = getCurrentAgentsList();
		//		return l.get(nb);
		//	}
		
			final public String getGroup()  {	return group;   }

	//	@SuppressWarnings("unchecked")
			//	final public A getAgentNb(final int nb)
			//	{
			//		final List<A> l = getCurrentAgentsList();
			//		return l.get(nb);
			//	}
			
				final public String getRole()   {	return role;    }

	/**
	 * Called by the MadKit kernel when the Activator or Probe is
	 * first added. Default behavior is: <code>adding(getCurrentAgentsList());</code>
	 */
	public void initialize(){
		adding(getCurrentAgentsList());
	}
	
	/**
	 * Called when a list of agents have joined the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * @param agents the list of agents which have been added to this group/role
	 */
	protected void adding(final List<A> agents){
		for(A agent : agents){
			adding(agent);
		}
	}

	/**
	 * Called when an agent has joined the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization when an agent enters the group/role.
	 * @param theAgent which has been added to this group/role
	 */
	protected void adding(final A theAgent){}

	/**
	 * Called when a list of agents have leaved the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * @param agents the list of agents which have been removed from this group/role
	 */
	protected void removing(final List<A> agents){
		for(A agent : agents){
			removing(agent);
		}
	}

	/**
	 * Called when an agent has leaved the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some work when an agent leaves the group/role.
	 * @param theAgent which has been removed from this group/role
	 */
	protected void removing(final A theAgent){}

	//	final private void nullRoleErrorMessage(final NullPointerException e,final String using) {
	//		System.err.println("\n-----WARNING : probes and activators should not be used before being added-----\n-----Problem on "+this.getClass().getSimpleName()+" on <"+community+";"+group+";"+role+"> using "+using+"-----\n-----Method call is at:");
	//		e.printStackTrace();
	//	}
	
		/** 
		 * Returns the number of the agents handling the group/role couple
		 * @return the number of the agents that handle the group/role couple
		 */
		final public int size() {
				return getCurrentAgentsList().size();
		}

	/** 
		 * Returns a snapshot at moment t of the agents handling the group/role couple
		 * @return a list view (a snapshot at moment t) of the agents that handle the group/role couple (in proper sequence)
		 * @since MadKit 3.0
		 */
		final public List<A> getCurrentAgentsList()//TODO log if not already added !
		{
			if (overlookedRole != null) {
				return (List<A>) overlookedRole.getAgentsList();
			}
			else{
				return Collections.emptyList();
			}
		}

	/** 
	 * Returns a ListIterator over the agents which is shuffled
	 * @return a ListIterator which has been previously shuffled
	 * @since MadKit 3.0
	 */
	final public List<A> getShuffledList()
	{
		try {
			List<A> l = getCurrentAgentsList();
			Collections.shuffle(l);
			return l;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		try {
			return getClass().getSimpleName() + " on <" + community + ";" + group + ";" + role + "> "+ size() + " agents)";
		} catch (NullPointerException e) {
			return getClass().getSimpleName() + " on <" + community + ";" + group + ";" + role + "> not currently added";
		}
	}
	
	final void addAgent(AbstractAgent a) {
		adding((A) a);
	}
	
	final void removeAgent(AbstractAgent a) {
		removing((A) a);
	}
	
	final void addAgents(List<AbstractAgent> l) {
		adding((List<A>) l);
	}
	
	final void removeAgents(List<AbstractAgent> l) {
		removing((List<A>) l);
	}
	
	public final void killAgents(){
		List<A> l = new ArrayList<A>(getCurrentAgentsList());
		allAgentsLeaveRole();
		for (A agent : l) {
			System.err.println("killing ALL "+agent);
			agent.killAgent(agent,0);
		}
	}
	
	public void allAgentsLeaveRole(){
		if(overlookedRole != null){
			overlookedRole.removeMembers((List<AbstractAgent>) getCurrentAgentsList());
		}
	}
	
	final public ExecutorService getMadkitServiceExecutor() {
		return MadkitKernel.getMadkitServiceExecutor();
	}


}