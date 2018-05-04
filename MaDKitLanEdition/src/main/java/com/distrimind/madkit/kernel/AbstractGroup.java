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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.MultiGroup.AssociatedGroup;
import com.distrimind.madkit.util.ExternalizableAndSizable;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the
 * agent/group/role principle by giving the possibility to the user to work with
 * a hierarchy of groups. So one group can have one or more subgroups. These
 * last groups can have also subgroups, etc. Such groups are represented by the
 * class {@link Group}. But the user can also work with several groups taken
 * arbitrarily on the hierarchy. Such grouping of groups is encapsulated by the
 * class {@link MultiGroup}.
 * 
 * The class AbstractGroup is the super class of these two classes.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see Group
 * @see MultiGroup
 * @see AbstractGroup#getUniverse()
 */
public abstract class AbstractGroup implements ExternalizableAndSizable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2233207859620713364L;

	/**
	 * This function returns the represented groups by the current instance. These
	 * groups can be a list of subgroups of one group, a list of arbitrary defined
	 * groups or list of only one group. Only groups that are used on MadKit, i.e.
	 * by agents, are returned. Groups that are only instantiated on the program are
	 * not returned.
	 * 
	 * @param ka
	 *            the used kernel address.
	 * @return the represented groups
	 * @since MadKitGroupExtension 1.0
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 */
	public abstract Group[] getRepresentedGroups(KernelAddress ka);

	/**
	 * This function returns the represented groups by the current instance, with
	 * all instantiated Madkit kernels. These groups can be a list of subgroups of
	 * one group, a list of arbitrary defined groups or list of only one group. Only
	 * groups that are used on MadKit, i.e. by agents, are returned. Groups that are
	 * only instantiated on the program are not returned.
	 * 
	 * @return the represented groups
	 * @since MadKitGroupExtension 1.0
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 */
	public abstract Group[] getRepresentedGroups();

	@Override
	public abstract AbstractGroup clone();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract String toString();

	/**
	 * This function returns the intersection between the represented groups by this
	 * group into the given KernelAddress, and those of the given abstract group in
	 * parameter.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @param _group
	 *            the group to operate with
	 * @return a list of groups which results from the request
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public ArrayList<Group> intersect(KernelAddress ka, AbstractGroup _group) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (_group == null)
			return new ArrayList<>();
		Group gs1[] = this.getRepresentedGroups(ka);
		Group gs2[] = _group.getRepresentedGroups(ka);
		ArrayList<Group> groups = new ArrayList<>(Math.min(gs1.length, gs2.length));
		if (gs1.length < gs2.length) {
			Group tmp[] = gs1;
			gs1 = gs2;
			gs2 = tmp;
		}
		for (Group g1 : gs1) {
			for (Group g2 : gs2) {
				if (g1.equals(g2)) {
					groups.add(g1);
					break;
				}
			}
		}
		groups.trimToSize();
		return groups;
	}

	/**
	 * This function returns the union between the represented groups by this group
	 * into the given KernelAddress, and those of the given abstract group in
	 * parameter.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @param _group
	 *            the group to operate with
	 * @return a list of groups which results from the request
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public HashSet<Group> union(KernelAddress ka, AbstractGroup _group) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (_group == null) {
			HashSet<Group> res = new HashSet<>();
			Collections.addAll(res, this.getRepresentedGroups(ka));
			return res;
		}
		Group gs1[] = this.getRepresentedGroups(ka);
		Group gs2[] = _group.getRepresentedGroups(ka);
		HashSet<Group> groups = new HashSet<>();

		for (Group g1 : gs1) {
			groups.add(g1);
		}
		for (Group g2 : gs2) {
			groups.add(g2);
		}
		return groups;
	}

	/**
	 * This function returns the symmetric difference between the represented groups
	 * by this group into the given KernelAddress, and those of the given abstract
	 * group in parameter.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @param _group
	 *            the group to operate with
	 * @return a list of groups which results from the request
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public HashSet<Group> symmetricDifference(KernelAddress ka, AbstractGroup _group) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (_group == null) {
			HashSet<Group> res = new HashSet<>();
			Collections.addAll(res, this.getRepresentedGroups(ka));
			return res;
		}
		ArrayList<Group> gi = this.intersect(ka, _group);
		HashSet<Group> gu = this.union(ka, _group);
		gu.removeAll(gi);
		return gu;
	}

	/**
	 * This function returns the represented groups by this group into the given
	 * KernelAddress, minus those of the given abstract group in parameter.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @param _group
	 *            the group to operate with
	 * @return a list of groups which results from the request
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public ArrayList<Group> minus(KernelAddress ka, AbstractGroup _group) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (_group == null) {
			ArrayList<Group> res = new ArrayList<>();
			Collections.addAll(res, this.getRepresentedGroups(ka));
			return res;
		}
		Group gs1[] = this.getRepresentedGroups(ka);
		Group gs2[] = _group.getRepresentedGroups(ka);
		ArrayList<Group> groups = new ArrayList<>(gs1.length);
		for (Group g1 : gs1) {
			boolean found = false;
			for (Group g2 : gs2) {
				if (g1.equals(g2)) {
					found = true;
					break;
				}
			}
			if (!found)
				groups.add(g1);
		}
		groups.trimToSize();
		return groups;
	}

	/**
	 * This function tells if the represented groups by this group into the given
	 * KernelAddress, include those of the given abstract group in parameter.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @param _group
	 *            the group to operate with
	 * @return a list of groups which results from the request
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public boolean includes(KernelAddress ka, AbstractGroup _group) {
		if (ka == null)
			throw new NullPointerException("ka");
		if (_group == null)
			return true;
		Group gs1[] = this.getRepresentedGroups(ka);
		Group gs2[] = _group.getRepresentedGroups(ka);
		if (gs1.length < gs2.length)
			return false;
		for (Group g2 : gs2) {
			boolean found = false;
			for (Group g1 : gs1) {
				if (g2.equals(g1)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/**
	 * This function tells there is no represented groups by this group into the
	 * given KernelAddress.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @return true if this group has no represented groups into the given kernel
	 *         address.
	 * @since MadKitGroupExtension 1.5
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public boolean isEmpty(KernelAddress ka) {
		if (ka == null)
			throw new NullPointerException("ka");
		return this.getRepresentedGroups(ka).length == 0;
	}

	/**
	 * Returns the complementary of the represented groups if this abstract group,
	 * considering one specific kernel address.
	 * 
	 * @param ka
	 *            the considered kernel address
	 * @return the complementary represented groups :
	 *         <code>AbstractGroup.getUniverse().minus(this).getRepresentedGroups(ka);</code>
	 * @since MadKitGroupExtension 1.5.1
	 * @see #getRepresentedGroups(KernelAddress)
	 * @see KernelAddress
	 * @see Group
	 * @see MultiGroup
	 * @throws NullPointerException
	 *             if ka is null
	 */
	public Group[] getComplementary(KernelAddress ka) {
		if (ka == null)
			throw new NullPointerException("ka");
		return getUniverse().minus(this).getRepresentedGroups(ka);
	}

	/**
	 * This function returns an AbstractGroup that intersects this group with the
	 * given abstract group in parameter.
	 * 
	 * @param _group
	 *            the group to intersect with
	 * @return the intersection result
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.5
	 */
	public AbstractGroup intersect(AbstractGroup _group) {
		if (_group == null)
			return new MultiGroup();
		if (this == _group)
			return this;
		if (this instanceof Group) {
			Group This = (Group) this;
			if (_group instanceof Group) {
				Group group = (Group) _group;
				if (group.equals(This)) {
					if (group.isUsedSubGroups() == This.isUsedSubGroups())
						return This;
					if (group.isUsedSubGroups())
						return This;
					else
						return group;
				} else {
					if (This.getCommunity().equals(group.getCommunity())) {
						if (This.getPath().startsWith(group.getPath()))
							return This;
						else if (group.getPath().startsWith(This.getPath()))
							return group;
					}
					return new MultiGroup();
				}
			} else if (_group instanceof MultiGroup) {
				return _group.intersect(this);
			} else if (_group instanceof Group.Universe) {
				return this;
			}
		} else if (this instanceof MultiGroup) {
			MultiGroup This = (MultiGroup) this;
			if (_group instanceof Group) {
				MultiGroup res = new MultiGroup();
				ArrayList<AbstractGroup> forbiden = new ArrayList<>();
				synchronized (this) {
					Group group = (Group) _group;
					ArrayList<AbstractGroup> AThis = new ArrayList<>();

					for (AssociatedGroup ag : This.m_groups) {
						if (ag.m_forbiden) {
							forbiden.add(ag.m_group);
						} else
							AThis.add(ag.m_group);
					}
					for (AbstractGroup ag : AThis) {
						AbstractGroup tmp = ag.intersect(group);
						if (!tmp.isEmpty()) {
							res.addGroup(tmp);
						}
					}
					if (res.m_groups.size() == 0)
						return new MultiGroup();
					for (AbstractGroup ag : forbiden) {
						res.addForbidenGroup(ag);
					}
					for (AssociatedGroup ag : res.m_groups) {
						if (!ag.m_forbiden)
							return res;
					}
					return new MultiGroup();
				}
			} else if (_group instanceof MultiGroup) {
				MultiGroup res = new MultiGroup();
				ArrayList<AbstractGroup> forbiden = new ArrayList<>();
				synchronized (this) {
					MultiGroup group = (MultiGroup) _group;
					ArrayList<AbstractGroup> AThis = new ArrayList<AbstractGroup>();
					MultiGroup AGroup = new MultiGroup();

					synchronized (group) {
						for (AssociatedGroup ag : This.m_groups) {
							if (ag.m_forbiden) {
								forbiden.add(ag.m_group);
							} else
								AThis.add(ag.m_group);
						}
						for (AssociatedGroup ag : group.m_groups) {
							if (ag.m_forbiden) {
								forbiden.add(ag.m_group);
							} else
								AGroup.addGroup(ag.m_group);
						}
						if (AGroup.m_groups.size() == 0)
							return new MultiGroup();
						for (AbstractGroup ag : AThis) {
							AbstractGroup tmp = ag.intersect(AGroup);
							if (!tmp.isEmpty()) {
								res.addGroup(tmp);
							}
						}
						if (res.m_groups.size() == 0)
							return new MultiGroup();
						for (AbstractGroup ag : forbiden) {
							res.addForbidenGroup(ag);
						}
						for (AssociatedGroup ag : res.m_groups) {
							if (!ag.m_forbiden)
								return res;
						}
						return new MultiGroup();
					}
				}
			} else if (_group instanceof Group.Universe) {
				return this;
			}
		} else if (this instanceof Group.Universe) {
			return _group;
		}
		return new MultiGroup();
	}

	/**
	 * This function returns an AbstractGroup that union this group with the given
	 * abstract group in parameter.
	 * 
	 * @param _group
	 *            the group to union with
	 * @return the union result
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.5
	 */
	public AbstractGroup union(AbstractGroup _group) {
		if (_group == null || _group == this)
			return this;
		if (_group instanceof Group.Universe)
			return _group;
		if (this instanceof Group.Universe)
			return this;
		MultiGroup res = new MultiGroup();
		res.addGroup(_group);
		res.addGroup(this);
		return res;
	}

	/**
	 * This function returns an AbstractGroup that is the symmetric difference
	 * between this group and the given abstract group in parameter.
	 * 
	 * @param _group
	 *            the group to operate with
	 * @return the symmetric difference result
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.5
	 */
	public AbstractGroup symmetricDifference(AbstractGroup _group) {
		if (_group == null)
			return this;
		if (_group == this || this instanceof Group.Universe || _group instanceof Group.Universe)
			return new MultiGroup();

		AbstractGroup union = this.union(_group);
		AbstractGroup intersection = this.intersect(_group);
		return union.minus(intersection);
	}

	boolean isEmptyForSure() {
		if (this instanceof Group)
			return false;
		else if (this instanceof MultiGroup) {
			synchronized (this) {
				for (AssociatedGroup ag : ((MultiGroup) this).m_groups) {
					if (ag.m_forbiden && ag.m_group instanceof Group.Universe)
						return true;
					else if (!ag.m_forbiden && !ag.m_group.isEmptyForSure()) {
						return false;
					}
				}
				return true;
			}
		} else if (this instanceof Group.Universe)
			return false;
		return false;
	}

	/**
	 * Returns true if this AbstractGroup is empty.
	 * 
	 * @return true if this AbstractGroup is empty
	 * @since MadKitGroupExtension 1.5.1
	 */
	public boolean isEmpty() {
		if (this instanceof Group.Universe)
			return false;
		if (isEmptyForSure())
			return true;
		else {
			ArrayList<Group> groups = notCompletelyExcludedGroups();
			if (groups == null)
				return true;
			else
				return groups.size() == 0;
		}
	}

	ArrayList<Group> notCompletelyExcludedGroups() {
		ArrayList<Group> res = new ArrayList<>();
		if (this instanceof MultiGroup) {
			synchronized (this) {
				MultiGroup This = (MultiGroup) this;
				boolean represent_universe = false;
				for (AssociatedGroup ag : This.m_groups) {
					if (!ag.m_forbiden) {

						ArrayList<Group> tmp = ag.m_group.notCompletelyExcludedGroups();
						if (tmp == null) {
							represent_universe = true;
							break;
						}
						for (Group g : tmp) {
							Iterator<Group> it = res.iterator();
							boolean global_add = true;
							while (it.hasNext()) {
								Group g2 = it.next();
								boolean toremove = false;
								boolean toadd = false;
								if (g.getCommunity().equals(g2.getCommunity())) {
									if (g.getPath().equals(g2.getPath())) {
										if (!g2.isUsedSubGroups() && g.isUsedSubGroups()) {
											toremove = true;
											toadd = true;
										}
									} else if (g.getPath().startsWith(g2.getPath())) {
										if (!g2.isUsedSubGroups()) {
											toadd = true;
										}
									} else if (g2.getPath().startsWith(g.getPath())) {
										if (g.isUsedSubGroups()) {
											toremove = true;
										}
										toadd = true;
									} else {
										toadd = true;
									}
								} else
									toadd = true;
								if (toremove)
									it.remove();
								global_add = global_add & toadd;
							}
							if (global_add)
								res.add(g);
						}
					}
				}

				Groups eliminated = new Groups();
				for (AssociatedGroup ag : This.m_groups) {
					if (ag.m_forbiden) {
						if (represent_universe) {
							if (ag.m_group instanceof Group.Universe)
								return new ArrayList<>();
						} else
							eliminated.mergeWithEliminated(ag.m_group.getEliminated(res));
					}
				}
				if (represent_universe)
					return null;

				res = eliminated.eliminates(res);
			}
		} else if (this instanceof Group) {
			res.add((Group) this);
		} else if (this instanceof Group.Universe)
			return null;
		return res;
	}

	Groups getEliminated(ArrayList<Group> groups) {
		if (this instanceof MultiGroup) {
			synchronized (this) {
				MultiGroup This = (MultiGroup) this;
				Groups res_gprs = new Groups();
				for (AssociatedGroup ag : This.m_groups) {
					if (!ag.m_forbiden) {
						Groups grps = ag.m_group.getEliminated(groups);
						res_gprs.mergeWithEliminated(grps);
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<Group> eliminated = (ArrayList<Group>) res_gprs.getEliminatedGroups().clone();
				for (AssociatedGroup ag : This.m_groups) {
					if (ag.m_forbiden) {
						Groups grps = ag.m_group.getSurvivors(eliminated);

						res_gprs.mergeWithSurvivors(grps);
					}
				}
				return res_gprs;
			}
		} else if (this instanceof Group) {

			Group This = (Group) this;
			Iterator<Group> it = groups.iterator();
			Groups res = new Groups();
			while (it.hasNext()) {
				Group g = it.next();
				boolean toremove = false;
				if (This.getCommunity().equals(g.getCommunity())) {
					if (This.getPath().equals(g.getPath())) {
						if (!g.isUsedSubGroups())
							toremove = true;
						else if (This.isUsedSubGroups())
							toremove = true;
					} else if (g.getPath().startsWith(This.getPath())) {

						if (This.isUsedSubGroups())
							toremove = true;
					}
				}
				if (toremove)
					res.addEliminatedGroup(g, null);
			}
			return res;
		} else if (this instanceof Group.Universe) {
			Groups res = new Groups();
			for (Group g : groups)
				res.addEliminatedGroup(g, null);
			return res;
		}
		return null;
	}

	static class Groups {
		private ArrayList<Group> groups_eliminated = new ArrayList<>();
		private ArrayList<Group> groups_generated = new ArrayList<>();

		public ArrayList<Group> getEliminatedGroups() {
			return groups_eliminated;
		}

		public ArrayList<Group> getGeneratedGroups() {
			return groups_generated;
		}

		public void addEliminatedGroup(Group _group, Group _replaced_group) {
			groups_eliminated.add(_group);

			if (_replaced_group != null) {
				addGeneratedGroup(_replaced_group);
			}
		}

		void addGeneratedGroup(Group _group) {
			Iterator<Group> it = groups_generated.iterator();
			boolean add = true;
			while (it.hasNext()) {
				Group g = it.next();

				if (g.getCommunity().equals(_group.getCommunity())) {
					if (g.getPath().equals(_group.getPath())) {
						if (!g.isUsedSubGroups() && _group.isUsedSubGroups()) {
							it.remove();
							add = true;
						} else {
							add = false;
						}
					} else if (g.getPath().startsWith(_group.getPath())) {
						if (_group.isUsedSubGroups()) {
							it.remove();
							add = true;
						} else {
							add = true;
						}
					} else if (_group.getPath().startsWith(g.getPath())) {
						if (g.isUsedSubGroups()) {
							add = false;
						} else {
							add = true;
						}
					} else
						add = true;
				} else
					add = true;
			}
			if (add)
				groups_generated.add(_group);
		}

		public void mergeWithSurvivors(Groups _groups) {
			ArrayList<Group> grps_eliminated = new ArrayList<>();
			ArrayList<Group> grps_not_eliminated = new ArrayList<>();
			ArrayList<Group> grps_generated = new ArrayList<>();
			grps_generated.addAll(_groups.groups_generated);
			grps_generated.addAll(groups_generated);

			for (Group g : _groups.groups_eliminated) {
				if (groups_eliminated.remove(g)) {
					grps_eliminated.add(g);
				} else {
					grps_not_eliminated.add(g);
					if (g.isUsedSubGroups()) {
						Iterator<Group> it = grps_generated.iterator();
						while (it.hasNext()) {
							Group g2 = it.next();
							if (g2.getCommunity().equals(g.getCommunity())) {
								if (g2.getPath().startsWith(g.getPath()))
									it.remove();
							}
						}
					}
				}
			}
			for (Group g : groups_eliminated) {
				grps_not_eliminated.add(g);
				if (g.isUsedSubGroups()) {
					Iterator<Group> it = grps_generated.iterator();
					while (it.hasNext()) {
						Group g2 = it.next();
						if (g2.getCommunity().equals(g.getCommunity())) {
							if (g2.getPath().startsWith(g.getPath()))
								it.remove();
						}
					}
				}
			}
			groups_eliminated = grps_eliminated;
			groups_generated = new ArrayList<>();
			for (Group g : grps_generated)
				addGeneratedGroup(g);
		}

		public void mergeWithEliminated(Groups _groups) {
			ArrayList<Group> generated = new ArrayList<>(_groups.groups_generated.size() + groups_generated.size());
			intersectForGenerated(groups_generated, _groups.groups_eliminated, _groups.groups_generated, generated);
			intersectForGenerated(_groups.groups_generated, groups_eliminated, groups_generated, generated);

			groups_generated = new ArrayList<>();
			for (Group g : generated)
				addGeneratedGroup(g);

			for (Group g : _groups.groups_eliminated) {
				if (!groups_eliminated.contains(g))
					groups_eliminated.add(g);
			}
		}

		public ArrayList<Group> eliminates(ArrayList<Group> groups) {
			ArrayList<Group> res = new ArrayList<Group>(groups.size());
			for (Group g : groups) {
				boolean eliminated = false;
				for (Group g2 : groups_eliminated) {
					if (g2 == g || (g.getCommunity().equals(g2.getCommunity()) && ((g.getPath().equals(g2.getPath())
							&& (g2.isUsedSubGroups() || g2.isUsedSubGroups() == g.isUsedSubGroups()))
							|| (g.getPath().startsWith(g2.getPath()) && g2.isUsedSubGroups())))) {
						eliminated = true;
						break;
					}
				}
				/*
				 * if (eliminated) { for (Group g2 : groups_generated) { if (g2==g ||
				 * (g.getCommunity().equals(g2.getCommunity()) &&
				 * ((g.getPath().equals(g2.getPath()) && (g2.isUsedSubGroups() ||
				 * g2.isUsedSubGroups()==g.isUsedSubGroups())) ||
				 * (g.getPath().startsWith(g2.getPath()) && g2.isUsedSubGroups()) ) ) ) {
				 * eliminated=false; break; } } }
				 */
				if (!eliminated)
					res.add(g);
			}
			res.addAll(groups_generated);
			return res;
		}

		static void intersectForGenerated(ArrayList<Group> groups_generated, ArrayList<Group> other_eliminated,
				ArrayList<Group> other_generated, ArrayList<Group> generated) {
			for (Group g : groups_generated) {
				boolean eliminated_on_other = false;
				for (Group g2 : other_eliminated) {
					if (g2.getCommunity().equals(g.getCommunity())) {
						if (g2.getPath().equals(g.getPath())) {
							if (g2.isUsedSubGroups() || g2.isUsedSubGroups() == g.isUsedSubGroups()) {
								eliminated_on_other = true;
								break;
							}
						} else if (g2.isUsedSubGroups()) {
							if (g.getPath().startsWith(g2.getPath())) {
								eliminated_on_other = true;
								break;
							}
						}
					}
				}
				if (eliminated_on_other) {
					for (Group g2 : other_generated) {
						if (g2.getCommunity().equals(g.getCommunity())) {
							if (g2.getPath().equals(g.getPath())) {
								if (g.isUsedSubGroups() == g2.isUsedSubGroups() || !g.isUsedSubGroups()) {
									eliminated_on_other = false;
									break;
								}
							} else if (g.getPath().startsWith(g2.getPath())) {
								if (!g2.isUsedSubGroups()) {
									eliminated_on_other = false;
									break;
								}
							}
						}
					}
				}
				if (!eliminated_on_other)
					generated.add(g);
			}

		}

		public ArrayList<Group> getNotEliminated(ArrayList<Group> authorized) {
			ArrayList<Group> res = new ArrayList<>(authorized.size());
			for (Group g : authorized) {
				boolean add = true;
				for (Group g2 : groups_eliminated) {
					if (g.getCommunity().equals(g2.getCommunity())) {
						if (g.getPath().equals(g2.getPath())) {
							if (g2.isUsedSubGroups() || g.isUsedSubGroups() == g2.isUsedSubGroups())
								add = false;
						} else if (g.getPath().startsWith(g2.getPath()) && g2.isUsedSubGroups()) {
							add = false;
						}
					}
					if (!add)
						break;
				}
				if (add)
					res.add(g);
			}
			res.addAll(groups_generated);
			return res;
		}

		private void addEliminatedGroup(Group g) {
			this.groups_eliminated.add(g);
			Iterator<Group> it = this.groups_generated.iterator();
			while (it.hasNext()) {
				Group g2 = it.next();
				if (g2.getCommunity().equals(g.getCommunity())) {
					if (g2.getPath().equals(g.getPath())
							|| (g2.getPath().startsWith(g.getPath()) && g.isUsedSubGroups()))
						it.remove();
				}
			}
		}

		public void mergeWithEliminatedSurvivors(ArrayList<Group> eliminated, Groups eliminated_survivors) {
			for (Group g : eliminated) {
				boolean add = true;
				for (Group g2 : eliminated_survivors.groups_eliminated) {
					if (g.equals(g2)) {
						add = false;
						break;
					}
				}
				if (add) {
					addEliminatedGroup(g);
				}
			}
			for (Group g : eliminated_survivors.groups_generated) {
				boolean add = true;
				Iterator<Group> it = groups_eliminated.iterator();
				while (it.hasNext()) {
					Group g2 = it.next();
					if (g2.getCommunity().equals(g.getCommunity())) {
						if (g2.getPath().equals(g.getPath())) {
							if (!g.isUsedSubGroups())
								add = false;
							else if (!g2.isUsedSubGroups()) {
								it.remove();
							}
						} else if (g2.isUsedSubGroups() && g.getPath().startsWith(g2.getPath())) {
							add = false;
						} else if (g.isUsedSubGroups() && g2.getPath().startsWith(g.getPath())) {
							it.remove();
						}
					}
				}
				if (add) {
					addEliminatedGroup(g);
				}
			}
		}
	}

	Groups getSurvivors(ArrayList<Group> groups) {
		if (this instanceof MultiGroup) {
			synchronized (this) {
				MultiGroup This = (MultiGroup) this;
				Groups res = new Groups();
				for (AssociatedGroup ag : This.m_groups) {
					if (!ag.m_forbiden) {
						Groups grps = ag.m_group.getSurvivors(groups);
						res.mergeWithSurvivors(grps);
					}
				}
				ArrayList<Group> eliminated = new ArrayList<>();

				for (Group g : groups) {
					boolean found = false;
					for (Group g2 : res.getEliminatedGroups()) {
						if (g.equals(g2)) {
							found = true;
							break;
						}
					}
					if (!found)
						eliminated.add(g);
				}
				Groups res_eliminated = new Groups();

				for (AssociatedGroup ag : This.m_groups) {
					if (ag.m_forbiden) {
						Groups grps = ag.m_group.getSurvivors(eliminated);
						res_eliminated.mergeWithSurvivors(grps);
					}
				}
				res.mergeWithEliminatedSurvivors(eliminated, res_eliminated);
				return res;
			}
		} else if (this instanceof Group) {
			Groups res = new Groups();
			Group This = (Group) this;

			for (Group g : groups) {
				Group generated = null;
				boolean toremove = false;
				if (This.getCommunity().equals(g.getCommunity())) {
					if (This.getPath().equals(g.getPath())) {
						if (This.isUsedSubGroups()) {
							toremove = true;
						} else {
							if (!g.isUsedSubGroups()) {
								toremove = true;
							}
						}
					} else if (g.getPath().startsWith(This.getPath())) {
						if (This.isUsedSubGroups())
							toremove = true;
					} else if (This.getPath().startsWith(g.getPath())) {
						if (g.isUsedSubGroups())
							generated = This;
					}
				}
				if (!toremove)
					res.addEliminatedGroup(g, generated);
			}
			return res;
		} else if (this instanceof Group.Universe) {
			return new Groups();
		}
		return null;
	}

	/**
	 * This function returns an AbstractGroup that is the result of this group minus
	 * the given abstract group in parameter.
	 * 
	 * @param _group
	 *            the group to operate with
	 * @return the subtraction result
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.5
	 */
	public AbstractGroup minus(AbstractGroup _group) {
		if (_group == null)
			return this;
		if (this == _group)
			return new MultiGroup();
		/*
		 * if (this instanceof MultiGroup) {
		 * ((MultiGroup)this).addForbidenGroup(_group); return this; } else
		 */
		{
			MultiGroup res = new MultiGroup();
			res.addGroup(this);
			res.addForbidenGroup(_group);
			return res;
		}
	}

	/**
	 * Returns the complementary of this group.
	 * 
	 * @return the complementary of this group :
	 *         <code>AbstractGroup.getUniverse().minus(this);</code>
	 * @since MadKitGroupExtension 1.5.1
	 */
	public AbstractGroup getComplementary() {
		return getUniverse().minus(this);
	}

	/**
	 * Returns true if this abstract group includes the given abstract group as
	 * parameter
	 * 
	 * @param _group
	 *            the group to test
	 * @return true if this abstract group includes the given abstract group as
	 *         parameter
	 * @since MadKitGroupExtension 1.5.1
	 */
	public boolean includes(AbstractGroup _group) {
		if (_group == null)
			return true;
		if (_group == this)
			return true;
		return _group.minus(this).isEmpty();
	}

	/**
	 * Returns a group which represents all groups of all communities.
	 * 
	 * @return a group which represents all groups of all communities.
	 */
	public static AbstractGroup getUniverse() {
		return Group.universe;
	}

	/**
	 * Returns a group that represents root group into a community
	 * 
	 * @param community
	 *            the community
	 * @return a group that represents root group into a community
	 */
	public static AbstractGroup getRootGroup(String community) {
		return new Group(community);
	}

}
