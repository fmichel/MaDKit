/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package com.distrimind.madkit.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKitLanEdition 1.0
 * @version 6.1
 * @param <A>
 *            The agent most generic type for this Overlooker
 * 
 */
@SuppressWarnings("unchecked")
abstract class Overlooker<A extends AbstractAgent> {

	final private AbstractGroup groups;
	final private String role;
	final AtomicReference<MadkitKernel> madkit_kernel = new AtomicReference<>(null);
	private final AtomicReference<Group[]> represented_groups = new AtomicReference<>(null);

	private final AtomicReference<ArrayList<OLR>> overlookedRoles = new AtomicReference<ArrayList<OLR>>(
			new ArrayList<OLR>());
	private final ArrayList<Group> overlookedRoles_to_add = new ArrayList<>();
	private final ArrayList<Group> overlookedRoles_to_remove = new ArrayList<>();
	private final AtomicReference<ArrayList<A>> referenced_agents = new AtomicReference<>(null);

	private final AtomicBoolean unique_references = new AtomicBoolean(true);
	private final GroupChangementNotifier group_changement_notifier;

	private class OLR {
		public final InternalRole overlookedRole;
		public final Group group;
		public final AtomicReference<List<AbstractAgent>> agents = new AtomicReference<>(null);
		public final AtomicReference<List<A>> filtred_agents = new AtomicReference<>(null);

		public OLR(InternalRole _overlookedRole, Group g) {
			overlookedRole = _overlookedRole;
			group = g;
		}

		public List<A> updateAndGet() {
			List<AbstractAgent> laa = overlookedRole.getAgentsList();
			if (agents.get() != laa) {
				filtred_agents.set(convertList(laa));
				agents.set(laa);
			}
			return filtred_agents.get();
		}

		private List<A> convertList(List<AbstractAgent> laa) {

			if (laa != null) {
				ArrayList<A> res = new ArrayList<>(laa.size());
				for (AbstractAgent aa : laa) {
					try {
						res.add((A) aa);
					} catch (ClassCastException e) {

					}
				}
				return res;
			} else
				return null;

		}
	}

	/**
	 * Tells if the function {@link Overlooker#getCurrentAgentsList()} must returns
	 * unique references.
	 * 
	 * @param value
	 * @see Overlooker#getCurrentAgentsList()
	 */
	public void setUniqueReferences(boolean value) {
		unique_references.set(true);
		referenced_agents.set(null);
	}

	/**
	 * Tells
	 * 
	 * @return true if the function {@link Overlooker#getCurrentAgentsList()}
	 *         returns unique references.
	 * @see Overlooker#getCurrentAgentsList()
	 */
	public boolean isUniqueReferences() {
		return unique_references.get();
	}

	/**
	 * Builds a new Activator or Probe on the given CGR location of the artificial
	 * society.
	 * 
	 * @param _groups
	 * @param roleName
	 */
	Overlooker(AbstractGroup _groups, final String _roleName)// TODO nullPointerEx here ?
	{
		this(_groups, _roleName, true);
	}

	/**
	 * Builds a new Activator or Probe on the given CGR location of the artificial
	 * society.
	 * 
	 * @param _groups
	 * @param roleName
	 * @param unique
	 *            Tells if the function {@link Overlooker#getCurrentAgentsList()}
	 *            must returns unique references.
	 */
	Overlooker(AbstractGroup _groups, final String _roleName, final boolean unique)// TODO nullPointerEx here ?
	{
		if (_groups == null)
			throw new NullPointerException("_groups");
		if (_roleName == null)
			throw new NullPointerException("_roleName");
		if (_roleName.equals(""))
			throw new IllegalArgumentException("_roleName");
		groups = _groups;
		role = _roleName;
		unique_references.set(unique);
		group_changement_notifier = new GroupChangementNotifier() {

			@Override
			public void potentialChangementInGroups() {
				Overlooker.this.potentialChangementInGroups();
			}
		};
	}

	void addToKernel(MadkitKernel _madkit_kernel) {
		if (!madkit_kernel.weakCompareAndSet(null, _madkit_kernel)) {
			_madkit_kernel
					.bugReport(new IllegalArgumentException("Attempting to add an Overlooker to several kernels."));
		}
		Group.addGroupChangementNotifier(group_changement_notifier);
		potentialChangementInGroups();
	}

	final void removeFromKernel() throws IllegalAccessException {
		if (madkit_kernel.get() == null) {
			throw new IllegalAccessException("Attempting to remove the Overlooker " + this
					+ " from a kernel, where it has not been added to a kernel.");
		} else {
			Group.removeGroupChangementNotifier(group_changement_notifier);
			synchronized (this) {
				if (!madkit_kernel.compareAndSet(null, null)) {
					cleanGroupToAddAndRemove();
					for (OLR olr : overlookedRoles.get()) {
						putGroupToRemove(olr.group);
					}
					updateOverlookedRoles();
					represented_groups.set(null);
					madkit_kernel.set(null);
					referenced_agents.set(null);
				}
			}
		}
	}

	boolean isConcernedBy(Group group, String role) {
		return role.equals(this.role) && groups.includes(group);
	}

	private void updateOverlookedRoles() {
		MadkitKernel mkk = this.madkit_kernel.get();
		ArrayList<OLR> overlookedRoles = (ArrayList<OLR>) this.overlookedRoles.get().clone();
		boolean changes = false;

		// removing roles
		for (Group g : overlookedRoles_to_remove) {
			OLR olr = null;
			Iterator<OLR> it = overlookedRoles.iterator();
			while (it.hasNext()) {
				OLR o = it.next();
				if (o.group.equals(g)) {
					olr = o;
					break;
				}
			}
			if (olr != null) {
				List<A> l = olr.filtred_agents.get();
				if (l != null)
					removing(l);
				olr.overlookedRole.removeOverlooker(this);
				it.remove();
				changes = true;
			}
		}

		// adding roles
		for (Group g : overlookedRoles_to_add) {
			try {
				InternalRole ir = mkk.getRole(g, this.role);
				ir.addOverlooker(this);
				overlookedRoles.add(new OLR(ir, g));
				addAgents(ir.getAgentsList());
				changes = true;
			} catch (CGRNotAvailable e) {
			}
		}

		cleanGroupToAddAndRemove();
		if (changes) {
			this.overlookedRoles.set(overlookedRoles);
			this.referenced_agents.set(null);
		}
	}

	void potentialChangementInGroups() {
		if (madkit_kernel.get() != null) {
			Group[] gps = groups.getRepresentedGroups(madkit_kernel.get().getKernelAddress());
			Group[] rpg = represented_groups.get();
			if (rpg != gps) {
				synchronized (this) {
					if (madkit_kernel.get() != null) {
						if (rpg == null) {
							for (Group g : gps)
								putGroupToAdd(g);
						} else {
							compareGroupsTab(rpg, gps);
						}
						represented_groups.set(gps);
						updateOverlookedRoles();
					}
				}
			}

		}
	}

	void InternalRoleInitialized(InternalRole ir) {
		synchronized (this) {
			if (madkit_kernel.get() != null) {
				putGroupToAdd(ir.getGroup());
			}
		}
	}

	private void compareGroupsTab(Group[] old_group, Group[] new_group) {
		for (Group og : old_group) {
			boolean found = false;
			for (Group ng : new_group) {
				if (og.equals(ng)) {
					found = true;
					break;
				}
			}
			if (!found) {
				putGroupToRemove(og);
			}
		}
		for (Group ng : new_group) {
			boolean found = false;
			for (Group og : old_group) {
				if (og.equals(ng)) {
					found = true;
					break;
				}
			}
			if (!found) {
				putGroupToAdd(ng);
			}
		}

	}

	private void putGroupToAdd(Group g) {
		if (!overlookedRoles_to_add.contains(g))
			overlookedRoles_to_add.add(g);
		overlookedRoles_to_remove.remove(g);
	}

	private void putGroupToRemove(Group g) {
		overlookedRoles_to_add.remove(g);
		if (!overlookedRoles_to_remove.contains(g))
			overlookedRoles_to_remove.add(g);
	}

	private void cleanGroupToAddAndRemove() {
		overlookedRoles_to_add.clear();
		overlookedRoles_to_remove.clear();
	}

	// @SuppressWarnings("unchecked")
	// final public A getAgentNb(final int nb)
	// {
	// final List<A> l = getCurrentAgentsList();
	// return l.get(nb);
	// }

	/**
	 * Gets the group(s) to which this activator/probe is binded to.
	 * 
	 * @return a string representing the group's name
	 */
	public AbstractGroup getGroups() {
		return groups;
	}

	/**
	 * Gets the role to which this activator/probe is binded to.
	 * 
	 * @return a string representing the role's name
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Called by the MaDKit kernel when the Activator or Probe is first added.
	 * Default behavior is empty.
	 */
	public void initialize() {

	}

	/**
	 * Called when a list of agents joins the corresponding group and role. This
	 * method is automatically called by the MaDKit kernel when agents enter a role
	 * due to the use of
	 * {@link AbstractAgent#launchAgentBucket(String, int, String...)}. Override
	 * this method when you want to do some initialization on the agents that enter
	 * the group/role. Default implementation is:
	 * 
	 * <pre>
	 * protected void adding(final List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		adding(agent);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param agents
	 *            the list of agents which have been added to this group/role at
	 *            once.
	 */
	protected void adding(final List<A> agents) {
		for (final A agent : agents) {
			adding(agent);
		}
	}

	/**
	 * This method is automatically called when an agent joins the corresponding
	 * group and role. This method is empty by default. Override this method when
	 * you want to do some initialization when an agent enters the group/role.
	 * 
	 * @param agent
	 *            which has been added to this group/role
	 */
	protected void adding(final A agent) {
	}

	/**
	 * This method is automatically called when a list of agents has leaved the
	 * corresponding group and role. This method is empty by default. Override this
	 * method when you want to do some initialization on the agents that enter the
	 * group/role. Default implementation is:
	 * 
	 * <pre>
	 * protected void removing(final List&lt;A&gt; agents) {
	 * 	for (A agent : agents) {
	 * 		removing(agent);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param agents
	 *            the list of agents which have been removed from this group/role
	 */
	protected void removing(final List<A> agents) {
		for (final A agent : agents) {
			removing(agent);
		}
	}

	/**
	 * This method is automatically called when an agent leaves the corresponding
	 * group and role. This method is empty by default. Override this method when
	 * you want to do some work when an agent leaves the group/role.
	 * 
	 * @param agent
	 *            which has been removed from this group/role
	 */
	protected void removing(final A agent) {
	}

	// final private void nullRoleErrorMessage(final NullPointerException e,final
	// String using) {
	// System.err.println("\n-----WARNING : probes and activators should not be used
	// before being added-----\n-----Problem on "+this.getClass().getSimpleName()+"
	// on <"+community+";"+group+";"+role+"> using "+using+"-----\n-----Method call
	// is at:");
	// e.printStackTrace();
	// }

	/**
	 * Returns the number of the agents handling the group/role couple
	 * 
	 * @return the number of the agents that handle the group/role couple
	 */
	public int size() {
		return getCurrentAgentsList().size();
	}

	/**
	 * Returns a snapshot at moment t of the agents handling the group/role couple.
	 * 
	 * @return a list view (a snapshot at moment t) of the agents that handle the
	 *         group/role couple (in proper sequence)
	 * @since MaDKitLanEdition 1.0
	 * @see Overlooker#setUniqueReferences(boolean)
	 */
	public List<A> getCurrentAgentsList()// TODO log if not already added !
	{
		ArrayList<OLR> olr = overlookedRoles.get();

		ArrayList<A> referenced_agents = this.referenced_agents.get();

		// detect changes
		boolean changes = (referenced_agents == null);

		if (!changes) {
			for (OLR o : olr) {
				if (o.agents.get() != o.overlookedRole.getAgentsList()) {
					changes = true;
					break;
				}
			}
		}
		if (changes) {
			Collection<A> ra = null;
			boolean unique = unique_references.get();
			if (unique) {
				ra = new HashSet<>();
			} else {
				ra = referenced_agents = new ArrayList<>();
			}
			for (OLR o : olr) {
				List<A> l = o.updateAndGet();
				if (l != null)
					ra.addAll(l);
			}
			if (unique) {
				referenced_agents = new ArrayList<>(ra.size());
				referenced_agents.addAll(ra);
			}

			this.referenced_agents.set(referenced_agents);
		}
		return referenced_agents;
	}

	/**
	 * Returns a ListIterator over the agents which is shuffled
	 * 
	 * @return a ListIterator which has been previously shuffled
	 * @since MaDKit 3.0
	 */
	public List<A> getShuffledList() {
		try {
			List<A> l = getCurrentAgentsList();
			Collections.shuffle(l);
			return l;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	/**
	 * returns a string containing the CGR location and the number of monitored
	 * agents.
	 * 
	 * @return a string representation of this tool.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " <" + groups + "," + role + "> " + size() + " agents";
	}

	final void addAgent(final AbstractAgent a) {
		try {
			adding((A) a);
		} catch (ClassCastException e) {
		}
	}

	final void removeAgent(final AbstractAgent a) {
		try {
			removing((A) a);
		} catch (ClassCastException e) {
		}
	}

	final void addAgents(final List<AbstractAgent> l) {
		List<A> laa = convertList(l);
		if (laa == null)
			return;
		if (laa.size() > 0)
			adding(laa);
	}

	final void removeAgents(final List<AbstractAgent> l) {
		List<A> laa = convertList(l);
		if (laa == null)
			return;
		if (laa.size() > 0)
			removing(laa);
	}

	private List<A> convertList(List<AbstractAgent> laa) {
		if (laa == null)
			return null;
		ArrayList<A> res = new ArrayList<>(laa.size());
		for (AbstractAgent aa : laa) {
			try {
				res.add((A) aa);
			} catch (ClassCastException e) {

			}
		}
		return res;
	}

	/**
	 * Kills all the agents which are monitored.
	 */
	public void killAgents() {
		final List<A> l = new ArrayList<>(getCurrentAgentsList());
		for (OLR olr : overlookedRoles.get()) {
			List<A> laaa = convertList(olr.overlookedRole.getAgentsList());
			for (A a : laaa)
				a.leaveAllAutoRequestedGroups();
			olr.overlookedRole.removeMembers((List<AbstractAgent>) laaa, true);
			l.addAll(laaa);
		}
		for (final A agent : l) {
			agent.killAgent(agent, 0);
		}
	}

	/**
	 * Makes all the agents leave the corresponding role at once.
	 */
	public void allAgentsLeaveRole() {
		for (OLR olr : overlookedRoles.get())
			olr.overlookedRole.removeMembers(olr.overlookedRole.getAgentsList(), true);
	}

}