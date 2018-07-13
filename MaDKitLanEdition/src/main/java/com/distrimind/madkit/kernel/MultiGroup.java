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

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.SerializationTools;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the
 * agent/group/role principle by giving the possibility to the user to work with
 * a hierarchy of groups. So one group can have one or more subgroups. These
 * last groups can have also subgroups, etc. Such groups are represented by the
 * class {@link Group}. But the user can also work with several groups taken
 * arbitrarily on the hierarchy. Such grouping of groups is encapsulated by the
 * class {@link MultiGroup}.
 *
 * The class MultiGroup combines several type of groups chosen arbitrarily.
 * These groups can be {@link Group} class, or {@link MultiGroup} class. So a
 * MultiGroup can be composed for example of a Group which represent its
 * subgroups, and by another MutliGroup.
 * 
 * It is also possible to combine forbidden groups. For example, we want to get
 * subgroups of the Group "My group":
 * 
 * <pre>
 * MultiGroup mg = new MultiGroup(new Group(true, "My community", "My group"));
 * </pre>
 * 
 * But we want also to exclude the subgroup "One subgroup". It is possible to do
 * it like this: <code>
 * mg.addForbidenGroup(new Group(true, "My community", "My group", "One subgroup"));
 * </code> If we use {@link #getRepresentedGroups(KernelAddress)} to get the
 * represented groups by 'mg', all subgroups of "My Group" will be returned,
 * excepted "One subgroup".
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see AbstractGroup
 * @see Group
 * @see AbstractGroup#getUniverse()
 */
@SuppressWarnings({"UnusedReturnValue", "ExternalizableWithoutPublicNoArgConstructor"})
public class MultiGroup extends AbstractGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1107997740777891372L;

	transient ArrayList<AssociatedGroup> m_groups;

	private static final class RepresentedGroupsDuplicated {
		public final AtomicReference<Group[]> m_represented_groups_duplicated = new AtomicReference<>(null);
		public KernelAddress m_kernel;

		public RepresentedGroupsDuplicated(KernelAddress ka) {
			m_kernel = ka;
		}
	}

	private transient ArrayList<RepresentedGroupsDuplicated> m_represented_groups_by_kernel_duplicated = new ArrayList<>();
	private transient volatile Group m_global_represented_groups[] = null;

	@Override
	public void readExternal(ObjectInput ois) throws IOException, ClassNotFoundException {
		m_groups = new ArrayList<>();
		m_represented_groups_by_kernel_duplicated = new ArrayList<>();
		m_global_represented_groups = null;

		int notforbiden = ois.readInt();
		int forbiden = ois.readInt();
		int total=8;
		int globalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		if (notforbiden<0 || forbiden<0 || total+notforbiden*4+total+forbiden*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		for (int i = 0; i < notforbiden; i++)
		{
			Object o=SerializationTools.readExternalizableAndSizable(ois, false);
			if (!(o instanceof AbstractGroup))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			AbstractGroup ag=(AbstractGroup)o;
			total+=ag.getInternalSerializedSize();
			if (total>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			addGroup(ag);
		}
		for (int i = 0; i < forbiden; i++)
		{
			Object o=SerializationTools.readExternalizableAndSizable(ois, false);
			if (!(o instanceof AbstractGroup))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			AbstractGroup ag=(AbstractGroup)o;
			total+=ag.getInternalSerializedSize();
			if (total>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			addForbidenGroup(ag);
		}
			
	}
	@Override
	public int getInternalSerializedSize() {
		int res=8;
		for (AssociatedGroup ag : m_groups) {
			if (!ag.m_forbiden)
				res+=ag.m_group.getInternalSerializedSize();
		}
		for (AssociatedGroup ag : m_groups) {
			if (ag.m_forbiden)
				res+=ag.m_group.getInternalSerializedSize();
		}
		return res;

	}
	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {

		int forbiden = 0;
		int notforbiden = 0;
		for (AssociatedGroup ag : m_groups) {
			if (ag.m_forbiden)
				++forbiden;
			else
				++notforbiden;
		}
		oos.writeInt(notforbiden);
		oos.writeInt(forbiden);
		for (AssociatedGroup ag : m_groups) {
			if (!ag.m_forbiden)
				SerializationTools.writeExternalizableAndSizable(oos, ag.m_group, false);
		}
		for (AssociatedGroup ag : m_groups) {
			if (ag.m_forbiden)
				SerializationTools.writeExternalizableAndSizable(oos, ag.m_group, false);
		}
	}
	@SuppressWarnings("unused")
	private MultiGroup()
	{
		
	}
	// private Group[] m_represented_groups_duplicated=null;
	/**
	 * Construct a MultiGroup which combine the different groups (Group and
	 * MultiGroup) given as parameter
	 * 
	 * @param _groups
	 *            the different groups to combine.
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
	public MultiGroup(AbstractGroup... _groups) {
		m_groups = new ArrayList<>(_groups.length);

		for (AbstractGroup g : _groups) {
			addGroup(g);
		}
	}

	public MultiGroup(Collection<? extends AbstractGroup> _groups) {
		m_groups = new ArrayList<>(_groups.size());

		for (AbstractGroup g : _groups) {
			addGroup(g);
		}

	}

	private MultiGroup(ArrayList<AssociatedGroup> _groups) {
		m_groups = _groups;
	}

	@Override
	synchronized public MultiGroup clone() {
		synchronized (this) {
			ArrayList<AssociatedGroup> groups = new ArrayList<>(m_groups.size());
			for (AssociatedGroup ag : m_groups) {
				if (ag.m_group instanceof MultiGroup) {
					groups.add(new AssociatedGroup(((MultiGroup) ag.m_group).clone(), ag.m_forbiden));
				} else
					groups.add(new AssociatedGroup(ag.m_group, ag.m_forbiden));
			}
			return new MultiGroup(groups);
		}
	}

	@Override
	public String toString() {
		synchronized (this) {
			StringBuilder sb = new StringBuilder();
			sb.append("MultiGroup[");
			int s1 = m_groups.size() - 1;
			if (s1 >= 0) {
				for (int i = 0; i < s1; i++) {
					AssociatedGroup ag = m_groups.get(i);
					if (ag.m_forbiden)
						sb.append("Forbiden");
					sb.append(ag.m_group);
					sb.append(", ");
				}
				AssociatedGroup ag = m_groups.get(s1);
				if (ag.m_forbiden)
					sb.append("Forbiden");
				sb.append(ag.m_group);
			}
			sb.append("]");
			return sb.toString();
		}
	}

	enum CONTAINS {
		CONTAINS_ON_FORBIDEN, CONTAINS_ON_AUTHORIZED, NOT_CONTAINS
	}

	@SuppressWarnings({"unlikely-arg-type", "EqualsBetweenInconvertibleTypes"})
	CONTAINS contains(AbstractGroup _group) {
		for (AssociatedGroup ag : m_groups) {
			if (ag.equals(_group)) {
				if (ag.m_forbiden)
					return CONTAINS.CONTAINS_ON_FORBIDEN;
				else
					return CONTAINS.CONTAINS_ON_AUTHORIZED;
			}
		}
		return CONTAINS.NOT_CONTAINS;
	}

	/*
	 * int containsReference(AbstractGroup _group) { for (AssociatedGroup ag :
	 * m_groups) { if (ag.m_group==_group) return 1; else if (ag.m_group instanceof
	 * MultiGroup) { int v=((MultiGroup)ag.m_group).containsReference(_group); if
	 * (v>0) return v+1; } } return 0;
	 * 
	 * }
	 */

	/**
	 * Add a new group ({@link Group} or {@link MultiGroup} or Universe (see
	 * {@link AbstractGroup#getUniverse()})) which will be combined with the current
	 * MultiGroup.
	 * 
	 * @param _g
	 *            the abstract group to add.
	 * @return false if the group has been already added.
	 * @see Group
	 * @see MultiGroup
	 * @see AbstractGroup#getUniverse()
	 * @since MadKitGroupExtension 1.0
	 * 
	 */
	public boolean addGroup(AbstractGroup _g) {
		synchronized (this) {
			if (_g == AbstractGroup.getUniverse()) {
				Iterator<AssociatedGroup> it = m_groups.iterator();
				while (it.hasNext()) {
					AssociatedGroup ag = it.next();
					if (!ag.m_forbiden) {
						if (ag.m_group.equals(AbstractGroup.getUniverse())) {
							return false;
						} else {
							it.remove();
						}
					}
				}
				m_groups.add(new AssociatedGroup(AbstractGroup.getUniverse(), false));
				for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
					rgd.m_represented_groups_duplicated.set(null);
				m_global_represented_groups = null;
				return true;
			} else {
				CONTAINS c = contains(_g);
				if (c.equals(CONTAINS.NOT_CONTAINS)) {
					m_groups.add(new AssociatedGroup(_g.clone(), false));
					for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
						rgd.m_represented_groups_duplicated.set(null);
					m_global_represented_groups = null;
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * Add a new forbidden group ({@link Group} or {@link MultiGroup} or Universe
	 * (see {@link AbstractGroup#getUniverse()})) which will forbid the
	 * representation of itself onto the current MultiGroup (see the class
	 * description).
	 * 
	 * @param _g
	 *            the group to forbid.
	 * @return false if the group has been already added.
	 * @see Group
	 * @see MultiGroup
	 * @see AbstractGroup#getUniverse()
	 * @since MadKitGroupExtension 1.0
	 */
	public boolean addForbidenGroup(AbstractGroup _g) {
		synchronized (this) {
			if (_g == AbstractGroup.getUniverse()) {
				for (AssociatedGroup ag : m_groups) {
					if (ag.m_forbiden) {
						if (ag.m_group.equals(AbstractGroup.getUniverse())) {
							return false;
						}
					}
				}
				m_groups.clear();
				m_groups.add(new AssociatedGroup(AbstractGroup.getUniverse(), true));
				for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
					rgd.m_represented_groups_duplicated.set(null);
				m_global_represented_groups = null;
				return true;
			} else {
				CONTAINS c = contains(_g);
				if (c.equals(CONTAINS.CONTAINS_ON_AUTHORIZED)) {
					removeGroup(_g);
				}
				if (!c.equals(CONTAINS.CONTAINS_ON_FORBIDEN)) {
					m_groups.add(new AssociatedGroup(_g.clone(), true));
					for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
						rgd.m_represented_groups_duplicated.set(null);
					m_global_represented_groups = null;
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * Remove the group given as parameter from the current MultiGroup. This group
	 * can be forbidden or not.
	 * 
	 * @param _g
	 *            the group to remove.
	 * @return false if group was not found.
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
	@SuppressWarnings({"unlikely-arg-type", "EqualsBetweenInconvertibleTypes"})
	boolean removeGroup(AbstractGroup _g) {
		Iterator<AssociatedGroup> it = m_groups.iterator();
		while (it.hasNext()) {
			AssociatedGroup ag = it.next();
			if (ag.equals(_g)) {
				it.remove();
				for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
					rgd.m_represented_groups_duplicated.set(null);
				m_global_represented_groups = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * This method is equivalent to
	 * <code>this.include(_ag) &#38;&#38; _ag.include(this)</code>
	 * 
	 * @see #getRepresentedGroups(KernelAddress)
	 * @since MadKitGroupExtension 1.5.1
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof AbstractGroup) {
			return this.equals((AbstractGroup) o);
		} else
			return false;
	}

	/**
	 * This method is equivalent to
	 * <code>this.include(_ag) &#38;&#38; _ag.include(this)</code>
	 * 
	 * @param _ag
	 *            the abstract group to compare with
	 * @return true if this group equals to the given group as parameter
	 * @see #getRepresentedGroups(KernelAddress)
	 * @since MadKitGroupExtension 1.5.1
	 */
	public boolean equals(AbstractGroup _ag) {
		if (_ag == null)
			return false;
		if (_ag == this)
			return true;
		/*
		 * if (_ag instanceof Group.Universe) { synchronized(this) { boolean
		 * universe=false; for (AssociatedGroup ag : m_groups) { if (ag.m_forbiden) { if
		 * (!ag.m_group.isEmpty()) return false; } else if (ag.m_group.equals(_ag))
		 * universe=true;
		 * 
		 * } return universe; } } else
		 */
		return this.includes(_ag) && _ag.includes(this);
	}

	/**
	 * This function returns the represented groups by the current instance. The
	 * returned list is a combination of the represented groups of several
	 * AbstractGroups (which can be MultiGroup), excepted forbidden groups added
	 * onto the current instance.
	 * 
	 * @param ka
	 *            the used the kernel address.
	 * @return the represented groups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 * @see MultiGroup
	 */
	@Override
	synchronized public Group[] getRepresentedGroups(KernelAddress ka) {
		if (ka == null)
			throw new NullPointerException("ka");

		RepresentedGroupsDuplicated rdg = null;
		for (RepresentedGroupsDuplicated r : m_represented_groups_by_kernel_duplicated) {
			if (r.m_kernel.equals(ka)) {
				rdg = r;
				break;
			}
		}
		if (rdg == null) {
			rdg = new RepresentedGroupsDuplicated(ka);
			m_represented_groups_by_kernel_duplicated.add(rdg);
		}

		if (rdg.m_represented_groups_duplicated.get() != null) {
			for (AssociatedGroup ag : m_groups) {
				if (ag.hasRepresentedGroupsChanged(ka)) {
					rdg.m_represented_groups_duplicated.set(null);
					break;
				}
			}
		}
		if (rdg.m_represented_groups_duplicated.get() == null) {
			synchronized (this) {
				HashSet<Group> l = new HashSet<>();
				ArrayList<Group> f = new ArrayList<>(10);
				ArrayList<Group> l2;

				if (m_groups.size() > 0) {
					for (AssociatedGroup ag : m_groups) {
						if (ag.m_forbiden) {
							f.addAll(Arrays.asList(ag.m_group.getRepresentedGroups(ka)));
						} else {
							l.addAll(Arrays.asList(ag.m_group.getRepresentedGroups(ka)));
						}
					}

					l2 = new ArrayList<>(l.size());
					for (Group g : l) {
						boolean add = true;
						for (Group gf : f) {
							if (g.equals(gf)) {
								add = false;
								break;
							}
						}
						if (add)
							l2.add(g);
					}
				} else
					l2 = new ArrayList<>(0);
				Group res[] = new Group[l2.size()];
				l2.toArray(res);
				rdg.m_represented_groups_duplicated.set(res);
			}
		}

		return rdg.m_represented_groups_duplicated.get();
	}

	@Override
	synchronized public Group[] getRepresentedGroups() {
		Group[] res = m_global_represented_groups;
		if (res != null) {
			for (AssociatedGroup ag : m_groups) {
				if (ag.hasRepresentedGroupsChanged()) {
					res = m_global_represented_groups = null;
					break;
				}
			}
		}
		if (res == null) {
			synchronized (this) {
				HashSet<Group> l = new HashSet<>();
				ArrayList<Group> f = new ArrayList<>(10);
				ArrayList<Group> l2;

				if (m_groups.size() > 0) {
					for (AssociatedGroup ag : m_groups) {
						if (ag.m_forbiden) {
							Collections.addAll(f, ag.m_group.getRepresentedGroups());
						} else {
							l.addAll(Arrays.asList(ag.m_group.getRepresentedGroups()));
						}
					}

					l2 = new ArrayList<>(l.size());
					for (Group g : l) {
						boolean add = true;
						for (Group gf : f) {
							if (g.equals(gf)) {
								add = false;
								break;
							}
						}
						if (add)
							l2.add(g);
					}
				} else
					l2 = new ArrayList<>(0);
				res = new Group[l2.size()];
				l2.toArray(res);
				m_global_represented_groups = res;
			}

		}
		return res;
	}

	class AssociatedGroup implements Serializable {
		private static final long serialVersionUID = 1215548;

		final AbstractGroup m_group;
		private transient volatile Group[] m_represented_groups = null;
		private transient volatile Group[] m_global_represented_groups = null;
		final boolean m_forbiden;

		AssociatedGroup(AbstractGroup _a, boolean _forbiden) {
			m_group = _a;
			m_forbiden = _forbiden;
		}

		boolean hasRepresentedGroupsChanged(KernelAddress ka) {
			Group[] g = m_group.getRepresentedGroups(ka);
			if (g == m_represented_groups)
				return false;
			else {
				m_represented_groups = g;
				return true;
			}
		}

		boolean hasRepresentedGroupsChanged() {
			Group[] g = m_group.getRepresentedGroups();
			if (g == m_global_represented_groups)
				return false;
			else {
				m_global_represented_groups = g;
				return true;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o instanceof AssociatedGroup) {
				return m_group.equals(((AssociatedGroup) o).m_group);
			} else if (o instanceof AbstractGroup) {
				if (o instanceof Group)
					return o.equals(m_group);
				else if (m_group instanceof Group)
					return false;
				else
					return o == m_group;
			} else
				return false;
		}
	}

	
}
