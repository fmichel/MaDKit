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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.Gatekeeper;
import com.distrimind.madkit.kernel.KernelAddress;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the
 * agent/group/role principle by giving the possibility to the user to work with
 * a hierarchy of groups. So one group can have one or more subgroups. These
 * last groups can have also subgroups, etc. Such groups are represented by the
 * class {@link Group}. One group can represent itself, but also its subgroups
 * already handled by MadKit.
 * 
 * @author Jason Mahdjoub
 * @version 1.8
 * @since MadKitLanEdition 1.0
 * @see AbstractGroup
 * @see MultiGroup
 */
public final class Group extends AbstractGroup implements Comparable<Group> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2926497540140504602L;

	private transient GroupTree m_group;
	private transient boolean m_use_sub_groups;
	private volatile transient GroupTree[] m_sub_groups_tree = null;
	private volatile transient GroupTree[] m_global_groups_tree = null;
	private transient GroupTree[] m_parent_groups_tree = null;
	private volatile transient Group[] m_sub_groups = null;
	private volatile transient Group[] m_global_sub_groups = null;
	private transient Group[] m_parent_groups = null;

	private volatile transient Group[] m_represented_groups = null;
	private volatile transient Group[] m_global_represented_groups = null;

	/**
	 * Construct a group within a community and a path of groups. This constructor
	 * has the same effect than
	 * <code>Group(false, false, null, false, _community, _groups)</code>.
	 * 
	 * Here a typical example :
	 * 
	 * <pre>
	 * Group g = new Group("My community", "My group", "My subgroup 1", "My subgroup 2");
	 * </pre>
	 * 
	 * The created group "My subgroup 2" is contained into the group "My subgroup 1"
	 * which is contained on the group "My group", which is contained into the
	 * community "My community".
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use the function
	 * {@link #getGroupFromPath(String, String)} or this constructor as follows :
	 * 
	 * <pre>
	 * String MyPath="/My group/My subgroup 1/My subgroup 2"
	 * Group g=new Group("My community", MyPath);
	 * </pre>
	 * 
	 * This is the only way to introduce a '/' character into this constructor.
	 * 
	 * @param _community
	 *            the community
	 * @param _groups
	 *            the path of groups
	 * @throws IllegalArgumentException
	 *             if a group name is empty, or if a group name contains a ';'
	 *             character.
	 * @throws NullPointerException
	 *             if a group is null or if a comunnity is null
	 * @since MadKitGroupExtension 1.0
	 * @see #Group(boolean, boolean , Gatekeeper, String, String...)
	 */
	public Group(String _community, String... _groups) {
		this(DEFAULT_GATEKEEPER | DEFAULT_DISTRIBUTED_VALUE, false, false, null, false, _community, _groups);
	}

	/**
	 * Construct a group within a community and a path of groups. This constructor
	 * has the same effect than
	 * <code>Group(_useSubGroups, false, null, false, _community, _groups)</code>.
	 * 
	 * Here a typical example :
	 * 
	 * <pre>
	 * Group g = new Group(false, "My community", "My group", "My subgroup 1", "My subgroup 2");
	 * </pre>
	 * 
	 * The created group "My subgroup 2" is contained into the group "My subgroup 1"
	 * which is contained on the group "My group", which is contained into the
	 * community "My community". The obtained group does not represent its
	 * subgroups.
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use the function
	 * {@link #getGroupFromPath(String, String)} or this constructor as follows :
	 * 
	 * <pre>
	 * String MyPath="/My group/My subgroup 1/My subgroup 2"
	 * Group g=new Group(false, "My community", MyPath);
	 * </pre>
	 * 
	 * This is the only way to introduce a '/' character into this constructor.
	 * 
	 * @param _useSubGroups
	 *            is set to true, the current group will represent itself, but also
	 *            its subgroups. When using subgroups, an activator (for example)
	 *            can handle a set of agents which are part of this group and its
	 *            subgroups. see {@link #getRepresentedGroups(KernelAddress)} for
	 *            more information.
	 * @param _community
	 *            the community
	 * @param _groups
	 *            the path of groups
	 * @throws IllegalArgumentException
	 *             if a group name is empty, or if a group name contains a ';'
	 *             character.
	 * @throws NullPointerException
	 *             if a group is null or if a comunnity is null
	 * @since MadKitGroupExtension 1.0
	 * @see #Group(boolean, boolean , Gatekeeper, String, String...)
	 */
	public Group(boolean _useSubGroups, String _community, String... _groups) {
		this(DEFAULT_GATEKEEPER | DEFAULT_DISTRIBUTED_VALUE, _useSubGroups, false, null, false, _community, _groups);
	}

	/**
	 * Construct a group within a community and a path of groups. This constructor
	 * has the same effect than
	 * <code>Group(_useSubGroups, isDistributed, null, false, _community, _groups)</code>.
	 * 
	 * Here a typical example :
	 * 
	 * <pre>
	 * Group g = new Group(false, false, "My community", "My group", "My subgroup 1", "My subgroup 2");
	 * </pre>
	 * 
	 * The created group "My subgroup 2" is contained into the group "My subgroup 1"
	 * which is contained on the group "My group", which is contained into the
	 * community "My community". The obtained group does not represent its
	 * subgroups. He is not distributed through several instances of MadKit into a
	 * network.
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use the function
	 * {@link #getGroupFromPath(String, String)} or this constructor as follows :
	 * 
	 * <pre>
	 * String MyPath="/My group/My subgroup 1/My subgroup 2"
	 * Group g=new Group(false, false, "My community", MyPath);
	 * </pre>
	 * 
	 * This is the only way to introduce a '/' character into this constructor.
	 * 
	 * @param _useSubGroups
	 *            is set to true, the current group will represent itself, but also
	 *            its subgroups. When using subgroups, an activator (for example)
	 *            can handle a set of agents which are part of this group and its
	 *            subgroups. see {@link #getRepresentedGroups(KernelAddress)} for
	 *            more information.
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _community
	 *            the community
	 * @param _groups
	 *            the path of groups
	 * @throws IllegalArgumentException
	 *             if a group name is empty, or if a group name contains a ';'
	 *             character.
	 * @throws NullPointerException
	 *             if a group is null or if a comunnity is null
	 * @since MadKitGroupExtension 1.0
	 * @see #Group(boolean, boolean, com.distrimind.madkit.kernel.Gatekeeper,
	 *      String, String...)
	 */
	public Group(boolean _useSubGroups, boolean _isDistributed, String _community, String... _groups) {
		this(DEFAULT_GATEKEEPER, _useSubGroups, _isDistributed, null, false, _community, _groups);
	}

	/**
	 * Construct a group within a community and a path of groups. This constructor
	 * has the same effect than
	 * <code>Group(_useSubGroups, isDistributed, _theIdentifier, false, _community, _groups)</code>.
	 * 
	 * Here a typical example :
	 * 
	 * <pre>
	 * Group g = new Group(false, false, null, "My community", "My group", "My subgroup 1", "My subgroup 2");
	 * </pre>
	 * 
	 * The created group "My subgroup 2" is contained into the group "My subgroup 1"
	 * which is contained on the group "My group", which is contained into the
	 * community "My community". The obtained group does not represent its
	 * subgroups. He is not distributed through several instances of MadKit into a
	 * network. No Gatekeeper is given.
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use the function
	 * {@link #getGroupFromPath(String, String)} or this constructor as follows :
	 * 
	 * <pre>
	 * String MyPath="/My group/My subgroup 1/My subgroup 2"
	 * Group g=new Group(false, false, null, "My community", MyPath);
	 * </pre>
	 * 
	 * This is the only way to introduce a '/' character into this constructor.
	 * 
	 * @param _useSubGroups
	 *            is set to true, the current group will represent itself, but also
	 *            its subgroups. When using subgroups, an activator (for example)
	 *            can handle a set of agents which are part of this group and its
	 *            subgroups. see {@link #getRepresentedGroups(KernelAddress)} for
	 *            more information.
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _community
	 *            the community
	 * @param _groups
	 *            the path of groups
	 * @throws IllegalArgumentException
	 *             if a group name is empty, or if a group name contains a ';'
	 *             character.
	 * @throws NullPointerException
	 *             if a group is null or if a comunnity is null
	 * @since MadKitGroupExtension 1.0
	 * @see com.distrimind.madkit.kernel.Gatekeeper
	 */

	public Group(boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier, String _community,
			String... _groups) {
		this(0, _useSubGroups, _isDistributed, _theIdentifier, false, _community, _groups);
	}

	/**
	 * Construct a group within a community and a path of groups. * Here a typical
	 * example :
	 * 
	 * <pre>
	 * Group g = new Group(false, null, false, "My community", "My group", "My subgroup 1", "My subgroup 2");
	 * </pre>
	 * 
	 * The created group "My subgroup 2" is contained into the group "My subgroup 1"
	 * which is contained on the group "My group", which is contained into the
	 * community "My community". The obtained group does not represent its
	 * subgroups. He is not distributed through several instances of MadKit into a
	 * network. No Gatekeeper is given.
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use the function
	 * {@link #getGroupFromPath(String, String)} or this constructor as follows :
	 * 
	 * <pre>
	 * String MyPath="/My group/My subgroup 1/My subgroup 2"
	 * Group g=new Group(false, false, null, "My community", MyPath);
	 * </pre>
	 * 
	 * This is the only way to introduce a '/' character into this constructor.
	 * 
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _isReserved
	 *            tell if the group does not authorize other instances with the same
	 *            group
	 * @param _community
	 *            the community
	 * @param _groups
	 *            the path of groups
	 * @throws IllegalArgumentException
	 *             if a group name is empty, or if a group name contains a ';'
	 *             character.
	 * @throws NullPointerException
	 *             if a group is null or if a comunnity is null
	 * @since MadKitGroupExtension 1.0
	 * @see com.distrimind.madkit.kernel.Gatekeeper
	 */
	public Group(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community,
			String... _groups) {
		this(0, false, _isDistributed, _theIdentifier, _isReserved, _community, _groups);
	}

	private static final int DEFAULT_DISTRIBUTED_VALUE = 1;
	private static final int DEFAULT_GATEKEEPER = 2;

	private Group(int default_values, boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier,
			boolean _isReserved, String _community, String... _groups) {
		this(false, default_values, _useSubGroups, _isDistributed, _theIdentifier, _isReserved, _community, _groups);
	}

	private Group(boolean forceReserved, int default_values, boolean _useSubGroups, boolean _isDistributed,
			Gatekeeper _theIdentifier, boolean _isReserved, String _community, String... _groups) {
		if (_groups == null)
			_groups = new String[0];
		if (_groups.length == 1 && _groups[0].contains("/"))
			_groups = getGroupsStringFromPath(_groups[0]);
		m_group = getRoot(_community).getGroup(_isDistributed, _theIdentifier, _isReserved, forceReserved, _groups);

		m_use_sub_groups = _useSubGroups;
		/*
		 * if (!m_use_sub_groups) { m_represented_groups=new Group[0];
		 * //m_represented_groups[0]=this; }
		 */
		if ((default_values & DEFAULT_DISTRIBUTED_VALUE) != DEFAULT_DISTRIBUTED_VALUE) {
			if (m_group.isDistributed() && !_isDistributed)
				System.err.println("[GROUP WARNING] The current created group (" + this
						+ ") have be declared as not distributed, whereas previous declarations of the same group were distributed. So the current created group is distributed !");
			if (!m_group.isDistributed() && _isDistributed)
				System.err.println("[GROUP WARNING] The current created group (" + this
						+ ") have be declared as distributed, whereas previous declarations of the same group were not distributed. So the current created group is not distributed !");
		}
		if ((default_values & DEFAULT_GATEKEEPER) != DEFAULT_GATEKEEPER) {
			if (m_group.getGateKeeper() != _theIdentifier)
				System.err.println("[GROUP WARNING] The current created group (" + this
						+ ") have be declared with an identifier (" + _theIdentifier
						+ ") which is not the same than the one declared with the previous same declared groups. So the current created group has the previous declared MadKit identifier ("
						+ m_group.getGateKeeper() + ") !");
		}
	}

	Group(GroupTree _g) {
		this(_g, false, true);
	}

	Group(GroupTree _g, boolean _use_sub_groups, boolean increase) {
		m_group = _g;
		m_use_sub_groups = _use_sub_groups;
		if (increase)
			m_group.incrementReferences();
		/*
		 * if (!m_use_sub_groups) { m_represented_groups=new Group[1];
		 * m_represented_groups[0]=this; }
		 */

	}

	@Override
	public final void finalize() {
		m_group.decrementReferences();
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public Group clone() {
		return this;
	}

	private void readObject(ObjectInputStream ois) throws IOException {

		try {
			String com = ois.readUTF();
			String path = ois.readUTF();
			this.m_use_sub_groups = ois.readBoolean();
			boolean dist = ois.readBoolean();
			boolean isReserved = ois.readBoolean();
			this.m_parent_groups = null;
			this.m_parent_groups_tree = null;
			this.m_represented_groups = null;
			this.m_sub_groups = null;
			this.m_sub_groups_tree = null;

			m_group = getRoot(com).getGroup(dist, null, isReserved, true, getGroupsStringFromPath(path));

			if (!m_use_sub_groups) {
				m_represented_groups = new Group[0];
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		try {
			oos.writeUTF(this.getCommunity());
			oos.writeUTF(this.getPath());
			oos.writeBoolean(this.m_use_sub_groups);
			oos.writeBoolean(this.isDistributed());
			oos.writeBoolean(isReserved());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public boolean isReserved() {
		return this.m_group.isReserved();
	}

	/**
	 * Returns the parent group of this group.
	 * 
	 * @return the parent group or null if the parent group is reserved.
	 * @since MadKitGroupExtension 1.0
	 */
	public Group getParent() {
		GroupTree p = m_group.getParent();
		if (p == null)
			return null;
		else if (p.isReserved())
			return null;
		else
			return new Group(m_group.getParent());
	}

	/**
	 * Returns the parent group of this group. This parent group will represent also
	 * all its subgroups.
	 * 
	 * @return the parent group or null if the parent group is reserved.
	 * @since MadKitGroupExtension 1.0
	 */
	public Group getParentWithItsSubGroups() {
		GroupTree p = m_group.getParent();
		if (p == null)
			return null;
		else if (p.isReserved())
			return null;
		else
			return new Group(m_group.getParent(), true, true);
	}

	/**
	 * Return the community of this group.
	 * 
	 * @return the community of this group.
	 * @since MadKitGroupExtension 1.0
	 */
	public String getCommunity() {
		return m_group.getCommunity();
	}

	/**
	 * Return the name of this group.
	 * 
	 * @return the name of this group.
	 * @since MadKitGroupExtension 1.0
	 */
	public String getName() {
		return m_group.getGroupName();
	}

	/**
	 * The path of a group is used transparently by MadKitGroupExtension as being
	 * the real group name used in MadKit. If a group A is in a group B, which have
	 * no parent group, than the path of A will be '/B/A/' and the path of B will be
	 * '/B/'.
	 * 
	 * @return the path of this group.
	 * @since MadKitGroupExtension 1.0
	 */
	public String getPath() {
		return m_group.getGroupPath();
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups. This
	 * function is the same call than <code>getSubGroup(false, _groups)</code>
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ab = new Group("My community", "A", "B");
	 * Group aba = new Group("My community", "A", "B", "A");
	 * 
	 * Group subgroup = ab.getSubGroup("A");
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aba</code>
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use this function as follows :
	 * 
	 * <pre>
	 * String MySubPath="/My subgroup 1/My subgroup 2"
	 * Group g=this.getSubGroup(MySubPath);
	 * </pre>
	 * 
	 * 
	 * @param _groups
	 *            the path of the desired subgroup
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(String... _groups) {
		return this.getSubGroup(false, _groups);
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups. This
	 * function is the same call than <code>getSubGroup(false, _group)</code>.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ba = new Group("My community", "B", "A");
	 * Group aaba = new Group("My community", "A", "A", "B", "A");
	 * 
	 * Group subgroup = aa.getSubGroup(ba);
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aaba</code>
	 * 
	 * 
	 * @param _group
	 *            the subgroup that must be contained into this group.
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(Group _group) {
		return getSubGroup(false, _group);
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ba = new Group("My community", "B", "A");
	 * Group aaba = new Group("My community", "A", "A", "B", "A");
	 * 
	 * Group subgroup = aa.getSubGroup(ba);
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aaba</code>
	 * 
	 * 
	 * @param _isReserved
	 *            tell if the group does not authorize other instances with the same
	 *            group
	 * @param _group
	 *            the subgroup that must be contained into this group.
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isReserved, Group _group) {
		return getSubGroup(_isReserved, _group.getPath());
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ba = new Group("My community", "B", "A");
	 * Group aaba = new Group("My community", "A", "A", "B", "A");
	 * 
	 * Group subgroup = aa.getSubGroup(_isDistributed, _theIdentifier, ba);
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aaba</code>
	 * 
	 * 
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _group
	 *            the subgroup that must be contained into this group.
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isDistributed, Gatekeeper _theIdentifier, Group _group) {
		return getSubGroup(_isDistributed, _theIdentifier, false, _group);
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ba = new Group("My community", "B", "A");
	 * Group aaba = new Group("My community", "A", "A", "B", "A");
	 * 
	 * Group subgroup = aa.getSubGroup(_isDistributed, _theIdentifier, _isReserved, ba);
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aaba</code>
	 * 
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _isReserved
	 *            tell if the group does not authorize other instances with the same
	 *            group
	 * @param _group
	 *            the subgroup that must be contained into this group.
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, Group _group) {
		return getSubGroup(_isDistributed, _theIdentifier, _isReserved, _group.getPath());
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ab = new Group("My community", "A", "B");
	 * Group aba = new Group("My community", "A", "B", "A");
	 * 
	 * Group subgroup = ab.getSubGroup("A");
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aba</code>
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use this function as follows :
	 * 
	 * <pre>
	 * String MySubPath="/My subgroup 1/My subgroup 2"
	 * Group g=this.getSubGroup(_isReserved, MySubPath);
	 * </pre>
	 * 
	 * 
	 * @param _isReserved
	 *            tell if the group does not authorize other instances with the same
	 *            group
	 * @param _groups
	 *            the path of the desired subgroup
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isReserved, String... _groups) {
		return getSubGroup(m_group.isDistributed(), m_group.getGateKeeper(), _isReserved, _groups);
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ab = new Group("My community", "A", "B");
	 * Group aba = new Group("My community", "A", "B", "A");
	 * 
	 * Group subgroup = ab.getSubGroup("A");
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aba</code>
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use this function as follows :
	 * 
	 * <pre>
	 * String MySubPath="/My subgroup 1/My subgroup 2"
	 * Group g=this.getSubGroup(_isDistributed, _theIdentifier, MySubPath);
	 * </pre>
	 * 
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _groups
	 *            the path of the desired subgroup
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isDistributed, Gatekeeper _theIdentifier, String... _groups) {
		return getSubGroup(_isDistributed, _theIdentifier, false, _groups);
	}

	/**
	 * Returns a subgroup contained on this group, or on one of its subgroups.
	 * 
	 * Here a simple example :
	 * 
	 * <pre>
	 * Group a = new Group("My community", "A");
	 * Group aa = new Group("My community", "A", "A");
	 * Group ab = new Group("My community", "A", "B");
	 * Group aba = new Group("My community", "A", "B", "A");
	 * 
	 * Group subgroup = ab.getSubGroup("A");
	 * </pre>
	 * 
	 * On this example, the group <code>subgroup</code> is the same group than
	 * <code>aba</code>
	 * 
	 * Through the function {@link #getPath()}, you can observe the used String path
	 * (containing '/' characters) into MadKit for this group. To convert a String
	 * path to a Group class, use this function as follows :
	 * 
	 * <pre>
	 * String MySubPath="/My subgroup 1/My subgroup 2"
	 * Group g=this.getSubGroup(_isDistributed, _theIdentifier, _isReserved, MySubPath);
	 * </pre>
	 * 
	 * @param _isDistributed
	 *            tell if the group is distributed through several instances of
	 *            MadKit into a network.
	 * @param _theIdentifier
	 *            any object that implements the {@link Gatekeeper} interface. If
	 *            not <code>null</code>, this object will be used to check if an
	 *            agent can be admitted in the group. When this object is null,
	 *            there is no group access control.
	 * @param _isReserved
	 *            tell if the group does not authorize other instances with the same
	 *            group
	 * @param _groups
	 *            the path of the desired subgroup
	 * @return the subgroup
	 * @since MadKitLanEdition 1.0
	 */
	public Group getSubGroup(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved,
			String... _groups) {
		return new Group(m_group.getGroup(_isDistributed, _theIdentifier, _isReserved, _groups), false, false);
	}

	/**
	 * This function works in the same way that the function
	 * {@link #getSubGroup(String...)}. But the return subgroup will also represent
	 * its subgroups.
	 * 
	 * @param _group
	 *            the path of the desired subgroup
	 * @return the subgroup
	 * @since MadKitGroupExtension 1.0
	 * @see #getRepresentedGroups(KernelAddress)
	 */
	public Group getSubGroupWithItsSubGroups(String... _group) {
		return new Group(m_group.getGroup(m_group.isDistributed(), m_group.getGateKeeper(), false, _group), true,
				false);
	}

	/**
	 * Return the same group, which does not represent its subgroups
	 * 
	 * @return the same group, which does not represent its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see #getRepresentedGroups(KernelAddress)
	 */
	public Group getThisGroupWithoutItsSubGroups() {
		if (m_use_sub_groups) {
			return new Group(m_group, false, true);
		} else
			return this;
	}

	/**
	 * Return the same group, which represents its subgroups
	 * 
	 * @return the same group, which represents its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see #getRepresentedGroups(KernelAddress)
	 */
	public Group getThisGroupWithItsSubGroups() {
		if (!m_use_sub_groups) {
			return new Group(m_group, true, true);
		} else
			return this;
	}

	/**
	 * This function enable to return all the subgroups of this group, i.e. all the
	 * subgroups that are handled by one or more agents.
	 * 
	 * @param ka
	 *            the used kernel address
	 * @return the subgroups that are handled by one or more agents, excepted those
	 *         that are reserved
	 * @since MadKitGroupExtension 1.0
	 */
	public Group[] getSubGroups(KernelAddress ka) {
		GroupTree[] sub_groups = m_group.getSubGroups(ka);
		if (m_sub_groups_tree != sub_groups) {
			ArrayList<Group> res = new ArrayList<Group>();

			for (int i = 0; i < sub_groups.length; i++) {
				if (!sub_groups[i].isReserved())
					res.add(new Group(sub_groups[i], false, true));
			}

			synchronized (m_group.m_root) {
				m_represented_groups = null;
				m_sub_groups = new Group[res.size()];
				res.toArray(m_sub_groups);
				m_sub_groups_tree = sub_groups;
			}
		}
		return m_sub_groups;
	}

	/**
	 * This function enable to return all the subgroups of this group, i.e. all the
	 * subgroups that are handled by one or more agents, for all instantiated Madkit
	 * kernels.
	 * 
	 * @return the subgroups that are handled by one or more agents, excepted those
	 *         that are reserved
	 * @since MadKitGroupExtension 1.0
	 */
	public Group[] getSubGroups() {
		GroupTree[] sub_groups = m_group.getSubGroups();
		if (m_global_groups_tree != sub_groups) {
			ArrayList<Group> res = new ArrayList<Group>();
			for (GroupTree gt : sub_groups) {
				if (!gt.isReserved())
					res.add(new Group(gt, false, true));
			}
			synchronized (m_group.m_root) {
				m_global_represented_groups = null;
				m_global_sub_groups = new Group[res.size()];
				res.toArray(m_global_sub_groups);
				m_global_groups_tree = sub_groups;
			}
		}
		return m_global_sub_groups;
	}

	/**
	 * Return all the parent groups of this group. Note that these parent groups are
	 * not necessarily handled by one or more agents.
	 * 
	 * @return all the parent groups of this group excepted those that are reserved.
	 *         Note that these parent groups are not necessarily handled by one or
	 *         more agents.
	 * @since MadKitGroupExtension 1.0
	 */
	public Group[] getParentGroups() {
		GroupTree[] parent_groups = m_group.getParentGroups();
		if (m_parent_groups_tree != parent_groups) {
			ArrayList<Group> res = new ArrayList<>(parent_groups.length);

			for (int i = 0; i < parent_groups.length; i++) {
				if (!parent_groups[i].isReserved())
					res.add(new Group(parent_groups[i], false, true));
			}

			synchronized (m_group.m_root) {
				m_parent_groups = new Group[res.size()];
				res.toArray(m_parent_groups);
				if (m_use_sub_groups)
					m_represented_groups = null;
				m_parent_groups_tree = parent_groups;
			}
		}
		return m_parent_groups;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Group) {
			return this.equals((Group) o);
		} else
			return false;
	}

	public boolean equals(Group _g) {
		return _g == this || (this.m_group == _g.m_group && this.m_use_sub_groups == _g.m_use_sub_groups);
	}

	/**
	 * Return true if this group is distributed into a network of several MadKit
	 * kernels.
	 * 
	 * @return true if this group is distributed into a network of several MadKit
	 *         kernels.
	 * @see #Group(boolean, boolean, Gatekeeper, String, String...)
	 */
	public boolean isDistributed() {
		return m_group.isDistributed();
	}

	/**
	 * Return the gatekeeper used to control the agents handling of this group.
	 * 
	 * @return the gatekeeper used to control the agents handling of this group.
	 * @see #Group(boolean, boolean, Gatekeeper, String, String...)
	 */
	public Gatekeeper getGateKeeper() {
		return m_group.getGateKeeper();
	}

	boolean isMadKitCreated(KernelAddress ka) {
		return m_group.isMadKitCreated(ka);
	}

	boolean isAnyRoleRequested(KernelAddress ka) {
		return m_group.isAnyRoleRequested(ka);
	}

	boolean isAnyRoleRequestedRecursive(KernelAddress ka) {
		return m_group.isAnyRoleRequestedRecursive(ka);
	}

	boolean isAnyRoleRequested() {
		return m_group.isAnyRoleRequested();
	}

	void incrementMadKitReferences(KernelAddress ka) {
		m_group.incrementMadKitReferences(ka);
	}

	/*
	 * void incrementMadKitReferences(int number, KernelAddress ka) { if (number<1)
	 * throw new IllegalAccessError(); m_group.incrementMadKitReferences(number,
	 * ka); }
	 */

	void decrementMadKitReferences(KernelAddress ka) {
		m_group.decrementMadKitReferences(ka);
		// return m_group.isMadKitCreated(ka);
	}

	/*
	 * boolean decrementMadKitReferences(int number, KernelAddress ka) {
	 * m_group.decrementMadKitReferences(number, ka); return
	 * m_group.isMadKitCreated(ka); }
	 */

	void setMadKitCreated(KernelAddress ka, boolean value) {
		m_group.setMadKitCreated(ka, value);
	}

	/**
	 * This function returns the represented groups by the current instance. These
	 * groups are for the class Group a list of subgroups corresponding to this
	 * instance. Only groups that are used on MadKit, i.e. by agents, are returned.
	 * Groups that are only instantiated on the program are not returned. If the
	 * current instance does not represent its subgroups (see
	 * {@link #Group(boolean, String, String...)}), only one group can be returned
	 * at maximum. This group correspond to the current instance. Moreover, if the
	 * previous group have not been already handled by one agent, an empty list is
	 * returned.
	 * 
	 * @param ka
	 *            the used kernel address.
	 * @return the represented groups excepted those that are reserved
	 * @since MadKitGroupExtension 1.0
	 * @see AbstractGroup
	 */
	@Override
	public Group[] getRepresentedGroups(KernelAddress ka) {
		if (m_use_sub_groups) {
			synchronized (m_group.m_root) {
				Group[] sg = getSubGroups(ka);
				boolean isAnyRoleRequested;
				if (isUsedSubGroups())
					isAnyRoleRequested = this.isAnyRoleRequested(ka);
				else
					isAnyRoleRequested = this.isAnyRoleRequested(ka);
				if (m_represented_groups == null || (isAnyRoleRequested
						&& ((m_represented_groups.length != 0 && m_represented_groups[0].m_group != this.m_group)
								|| m_represented_groups.length == 0))
						|| (!isAnyRoleRequested && m_represented_groups.length != 0
								&& m_represented_groups[0].m_group == this.m_group)) {
					if (sg == null)
						sg = new Group[0];
					if (isAnyRoleRequested || (this.getPath().equals(LocalCommunity.Groups.SYSTEM.getPath())
							&& this.getCommunity().equals(LocalCommunity.Groups.SYSTEM.getCommunity()))) {
						m_represented_groups = new Group[sg.length + 1];
						m_represented_groups[0] = this.getThisGroupWithoutItsSubGroups();
						System.arraycopy(sg, 0, m_represented_groups, 1, sg.length);
					} else {
						m_represented_groups = sg;
					}
				}
				return m_represented_groups;
			}
		} else {
			synchronized (m_group.m_root) {
				if (m_represented_groups == null || m_represented_groups.length == 0) {
					if (this.isAnyRoleRequested(ka) || this.isHiddenGroup()) {
						m_represented_groups = new Group[1];
						m_represented_groups[0] = this;
					} else
						m_represented_groups = new Group[0];
				} else {
					if (!this.isAnyRoleRequested(ka)) {
						if (isHiddenGroup()) {
							m_represented_groups = new Group[1];
							m_represented_groups[0] = this;
						} else
							m_represented_groups = new Group[0];
					}
				}
				return m_represented_groups;
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Group[] getRepresentedGroups() {
		if (m_use_sub_groups) {
			synchronized (m_group.m_root) {
				Group sg[] = getSubGroups();
				if (m_represented_groups == null
						|| (this.isAnyRoleRequested() && ((m_global_represented_groups.length != 0
								&& m_global_represented_groups[0].m_group != this.m_group)
								|| m_global_represented_groups.length == 0))
						|| (!this.isAnyRoleRequested() && m_global_represented_groups.length != 0
								&& m_global_represented_groups[0].m_group == this.m_group)) {
					if (m_group.isAnyRoleRequested() || this.isHiddenGroup()) {
						m_global_represented_groups = new Group[sg.length + 1];
						m_global_represented_groups[0] = this.getThisGroupWithoutItsSubGroups();
						System.arraycopy(sg, 0, m_represented_groups, 1, sg.length);
					} else
						m_global_represented_groups = sg;
				}
				return m_global_represented_groups;
			}
		} else {
			synchronized (m_group.m_root) {
				if (m_global_represented_groups == null || m_global_represented_groups.length == 0) {
					if (m_group.isAnyRoleRequested() || this.isHiddenGroup()) {
						m_global_represented_groups = new Group[1];
						m_global_represented_groups[0] = this;
					} else
						m_global_represented_groups = new Group[0];
				}
				return m_global_represented_groups;
			}
		}
	}

	private boolean isHiddenGroup() {
		return (this.getPath().equals(LocalCommunity.Groups.SYSTEM.getPath())
				&& this.getCommunity().equals(LocalCommunity.Groups.SYSTEM.getCommunity()))
				|| this.getPath().equals(com.distrimind.madkit.agr.LocalCommunity.Groups.GUI.getPath()) && this
						.getCommunity().equals(com.distrimind.madkit.agr.LocalCommunity.Groups.GUI.getCommunity());
	}

	/**
	 * Return true if this group represents also its subgroups.
	 * 
	 * @return true if this group represents also its subgroups.
	 * @since MadKitGroupExtension 1.0
	 * @see #Group(boolean, String, String...)
	 */
	public boolean isUsedSubGroups() {
		return m_use_sub_groups;
	}

	@Override
	public int compareTo(Group _o) {
		return getPath().compareTo(_o.getPath());
	}

	/*
	 * private GroupTree getGroupTree() { return m_group; }
	 */

	@Override
	public String toString() {
		if (m_use_sub_groups)
			return "GroupAndSubGroups(" + getCommunity() + ":" + getPath() + ")";
		else
			return "Group(" + getCommunity() + ":" + getPath() + ")";
	}

	static String[] getGroupsStringFromPath(String _path) {
		String[] r = _path.split("/");
		if (r == null || r.length == 0)
			return null;
		int size = 0;
		for (int i = 0; i < r.length; i++) {
			if (r[i].length() == 0)
				r[i] = null;
			else
				size++;
		}
		String r2[];
		if (size == r.length)
			r2 = r;
		else {
			r2 = new String[size];
			int j = 0;
			for (int i = 0; i < r.length; i++) {
				if (r[i] != null) {
					r2[j] = r[i];
					j++;
				}
			}
		}
		return r2;
	}

	/**
	 * Return a group from a path.
	 * 
	 * @param _community
	 *            the community of desired group.
	 * @param _path
	 *            the path (see {@link #getPath()} to get more information)
	 *            corresponding to the desired group.
	 * @return the desired group.
	 * @since MadKitGroupExtension 1.0
	 */
	static Group getGroupFromPath(String _community, String _path) {
		return getGroupFromPath(_community, _path, false);
	}

	static Group getGroupFromPath(String _community, String _path, boolean force) {
		return new Group(force, DEFAULT_GATEKEEPER | DEFAULT_DISTRIBUTED_VALUE, false, false, null, false, _community,
				Group.getGroupsStringFromPath(_path));
	}

	static private final ArrayList<GroupChangementNotifier> m_objects_to_notify = new ArrayList<GroupChangementNotifier>(
			100);

	static void addGroupChangementNotifier(GroupChangementNotifier _gcn) {
		synchronized (m_objects_to_notify) {
			m_objects_to_notify.add(_gcn);
		}
		_gcn.potentialChangementInGroups();
	}

	static void removeGroupChangementNotifier(GroupChangementNotifier _gcn) {
		synchronized (m_objects_to_notify) {
			m_objects_to_notify.remove(_gcn);
		}
	}

	@SuppressWarnings("unchecked")
	static void notifyChangements() {
		ArrayList<GroupChangementNotifier> list = null;
		synchronized (m_objects_to_notify) {
			list = (ArrayList<GroupChangementNotifier>) m_objects_to_notify.clone();
		}
		for (GroupChangementNotifier gcn : list) {
			gcn.potentialChangementInGroups();
		}
	}

	final static class Universe extends AbstractGroup {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4697381152906077266L;

		@Override
		public Group[] getRepresentedGroups(KernelAddress _ka) {
			if (_ka == null)
				return new Group[0];
			AtomicReference<Group[]> rp = null;
			synchronized (represented_groups_universe) {
				rp = represented_groups_universe.get(_ka);
				if (rp == null) {
					rp = new AtomicReference<Group[]>(null);
					represented_groups_universe.put(_ka, rp);
				}
			}
			Group res[] = rp.get();
			if (res == null) {
				ArrayList<Group> lst = new ArrayList<>(50);
				synchronized (m_groups_root) {
					for (GroupTree gt : m_groups_root.values()) {
						for (Group g : gt.getRepresentedGroups(_ka))
							lst.add(g);
					}
				}
				res = new Group[lst.size()];
				lst.toArray(res);
				rp.set(res);
			}
			return res;
		}

		@Override
		public Group[] getRepresentedGroups() {
			Group[] res = global_represented_groups_universe;
			if (res == null) {
				synchronized (represented_groups_universe) {
					ArrayList<Group> lst = new ArrayList<>(50);
					synchronized (m_groups_root) {
						for (GroupTree gt : m_groups_root.values()) {
							for (Group g : gt.getRepresentedGroups())
								lst.add(g);
						}
					}
					res = new Group[lst.size()];
					lst.toArray(res);
					global_represented_groups_universe = res;
				}
			}
			return res;
		}

		@Override
		public AbstractGroup clone() {
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (o instanceof Universe)
				return true;
			else if (o instanceof MultiGroup)
				return o.equals(this);
			else
				return false;
		}

		@Override
		public String toString() {
			return "UniverseOfGroups";
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	static final Universe universe = new Universe();
	static final Map<KernelAddress, AtomicReference<Group[]>> represented_groups_universe = new HashMap<KernelAddress, AtomicReference<Group[]>>();
	static volatile Group[] global_represented_groups_universe = null;

	static void resetRepresentedGroupsOfUniverse(KernelAddress ka) {
		synchronized (represented_groups_universe) {
			AtomicReference<Group[]> af = represented_groups_universe.get(ka);
			if (af != null)
				af.set(null);
		}
	}

	static protected final Map<String, GroupTree> m_groups_root = new HashMap<String, GroupTree>();

	static protected GroupTree getRoot(String _community) {
		if (_community == null)
			throw new NullPointerException("_community");
		if (_community.length() == 0)
			throw new IllegalArgumentException("_community cannot be empty !");

		synchronized (m_groups_root) {
			GroupTree gt = m_groups_root.get(_community);
			if (gt == null) {
				gt = new GroupTree(_community);
				m_groups_root.put(_community, gt);
			}
			return gt;
		}
	}

	protected static KernelAddress m_first_kernel = null;

	public static KernelAddress getFirstUsedMadKitKernel() {
		return m_first_kernel;
	}

	private final static class GroupTree {
		private static final class KernelReferences {
			public int m_madkit_references = 0;
			public KernelAddress m_kernel = null;
			public LinkedList<GroupTree> m_all_sub_groups = new LinkedList<GroupTree>();
			public final AtomicReference<GroupTree[]> m_all_sub_groups_duplicated = new AtomicReference<GroupTree[]>(
					new GroupTree[0]);

			public KernelReferences(KernelAddress ka) {
				m_kernel = ka;
			}

			public boolean equals(KernelReferences kr) {
				return m_kernel.equals(kr.m_kernel);
			}

			@Override
			public boolean equals(Object o) {
				if (o instanceof KernelReferences) {
					return equals((KernelReferences) o);
				}
				return false;
			}
		}

		public boolean isReserved() {
			return isReserved;
		}

		private final ArrayList<GroupTree> m_sub_groups = new ArrayList<GroupTree>();
		protected final String m_community;
		private final String m_group;
		private final String m_path;
		private final GroupTree m_parent;
		protected final GroupTree m_root;
		private final boolean m_is_distributed;
		private final Gatekeeper m_identifier;
		private int m_references = 0;
		private HashMap<KernelAddress, KernelReferences> m_kernel_references = new HashMap<KernelAddress, KernelReferences>();
		private boolean isReserved;
		private volatile GroupTree m_global_sub_groups_duplicated[] = null;

		// private final LinkedList<GroupTree> m_all_sub_groups=new
		// LinkedList<GroupTree>();
		// private GroupTree[] m_all_sub_groups_duplicated=new GroupTree[0];

		private final LinkedList<GroupTree> m_parent_groups = new LinkedList<GroupTree>();
		private final AtomicReference<GroupTree[]> m_parent_groups_duplicated = new AtomicReference<GroupTree[]>(
				new GroupTree[0]);

		/*
		 * private final LinkedList<String> m_sub_group_paths=new LinkedList<String>();
		 * private String[] m_sub_group_paths_duplicated=new String[0];
		 * 
		 * private final LinkedList<String> m_parent_group_paths=new
		 * LinkedList<String>(); private String[] m_parent_group_paths_duplicated=new
		 * String[0];
		 */

		public GroupTree(String _community) {
			m_community = _community;
			m_group = "";
			m_path = "/";
			m_parent = null;
			m_is_distributed = true;
			m_identifier = null;
			isReserved = false;
			m_root = this;
		}

		private GroupTree(String group, GroupTree root, GroupTree _parent, boolean _isDistributed,
				Gatekeeper _theIdentifier, boolean _isReserved) {
			if (group.length() == 0)
				throw new IllegalArgumentException("There is a group whose name is empty");
			if (group.contains("/"))
				throw new IllegalArgumentException("The group named '" + group + "' cannot contains a '/' character !");
			if (group.contains(";"))
				throw new IllegalArgumentException("The group named '" + group + "' cannot contains a ';' character !");

			m_community = _parent.m_community;
			m_group = group;
			m_path = _parent.m_path + group + "/";
			m_parent = _parent;
			m_is_distributed = _isDistributed;
			m_identifier = _theIdentifier;
			isReserved = _isReserved;
			m_root = root;
		}

		public GroupTree getGroup(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved,
				String... _group) {
			return getGroup(_isDistributed, _theIdentifier, _isReserved, false, _group);
		}

		public GroupTree getGroup(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved,
				boolean forceReserved, String... _group) {
			if (_group == null || _group.length == 0)
				return this;
			if (_group.length == 1 && _group[0].contains("/"))
				_group = getGroupsStringFromPath(_group[0]);
			return getGroup(_isDistributed, _theIdentifier, 0, _isReserved, forceReserved, _group);
		}

		private GroupTree getGroup(boolean _isDistributed, Gatekeeper _theIdentifier, int i, boolean _isReserved,
				boolean forceReserved, String... _group) {
			synchronized (m_root) {
				String g = _group[i];
				if (g == null)
					throw new NullPointerException("one element of groups is null");
				if (g.length() == 0)
					throw new IllegalArgumentException("one element of groups is empty");
				for (GroupTree gt : m_sub_groups) {
					if (gt.m_group.equals(g)) {
						if (i == _group.length - 1) {
							if (!forceReserved && ((_isReserved && gt.getNbReferences() > 0) || gt.isReserved)) {
								String err = "";
								for (String s : _group)
									err += s + "/";
								if (gt.isReserved)
									throw new IllegalArgumentException("The group " + err + " is reserved !");
								else
									throw new IllegalArgumentException("The group " + err
											+ " cannot be reserved, because it have already been reserved !");
							}
							if (_isReserved)
								gt.isReserved = true;
							gt.incrementReferences();
							return gt;
						} else
							return gt.getGroup(_isDistributed, _theIdentifier, i + 1, _isReserved, forceReserved,
									_group);
					}
				}
				GroupTree gt = new GroupTree(g, m_root, this, _isDistributed, _theIdentifier,
						(i == _group.length - 1) ? _isReserved : false);
				GroupTree res;
				if (i == _group.length - 1)
					res = gt;
				else
					res = gt.getGroup(_isDistributed, _theIdentifier, i + 1, _isReserved, forceReserved, _group);
				addSubGroup(gt);
				return res;
			}
		}

		public GroupTree getParent() {
			return m_parent;
		}

		public String getCommunity() {
			return m_community;
		}

		public String getGroupName() {
			return m_group;
		}

		public String getGroupPath() {
			return m_path;
		}

		public boolean isDistributed() {
			return m_is_distributed;
		}

		public Gatekeeper getGateKeeper() {
			return m_identifier;
		}

		public GroupTree[] getSubGroups(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);
				if (kr == null)
					return new GroupTree[0];

				return kr.m_all_sub_groups_duplicated.get();
			}
		}

		public GroupTree[] getSubGroups() {
			synchronized (m_root) {
				GroupTree res[] = m_global_sub_groups_duplicated;
				if (res == null) {
					ArrayList<GroupTree> r = new ArrayList<>();
					if (isAnyRoleRequested()) {
						for (GroupTree gt : m_sub_groups) {
							if (gt.isAnyRoleRequested()) {
								r.add(gt);
								for (GroupTree gt2 : gt.getSubGroups())
									r.add(gt2);
							}
						}
					}
					res = new GroupTree[r.size()];
					r.toArray(res);
				}
				return res;
			}
		}

		public GroupTree[] getParentGroups() {
			return m_parent_groups_duplicated.get();
		}

		public void incrementMadKitReferences(KernelAddress ka) {
			boolean activate = false;
			synchronized (m_root) {
				if (Group.m_first_kernel == null)
					Group.m_first_kernel = ka;
				if (!isAnyRoleRequested())
					m_global_sub_groups_duplicated = null;
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null) {
					throw new IllegalAccessError();
					/*
					 * kr=new KernelReferences(ka); m_kernel_references.put(ka, kr);
					 * kr.m_madkit_references=1;
					 */
				}

				if (kr.m_madkit_references < 0)
					throw new IllegalAccessError("kr.m_madkit_references=" + kr.m_madkit_references);
				if (kr.m_madkit_references == 0)
					throw new IllegalAccessError("kr.m_madkit_references=" + kr.m_madkit_references);
				++kr.m_madkit_references;

				if (kr.m_madkit_references == 2) {
					activate = true;
					GroupTree p = m_parent;
					while (p != null) {
						KernelReferences krp = p.m_kernel_references.get(ka);
						if (krp == null) {
							krp = new KernelReferences(ka);
							p.m_kernel_references.put(ka, krp);
						}
						if (krp.m_madkit_references < 0)
							throw new IllegalAccessError("krp.m_madkit_references=" + krp.m_madkit_references);

						krp.m_all_sub_groups.add(this);
						p.updateDuplicatedSubGroupList(ka);
						p = p.m_parent;
					}
				}
			}
			if (activate) {
				resetRepresentedGroupsOfUniverse(ka);
				Group.notifyChangements();
			}
		}

		/*
		 * public synchronized void incrementMadKitReferences(int number, KernelAddress
		 * ka) { if (Group.m_first_kernel==null) Group.m_first_kernel=ka;
		 * 
		 * KernelReferences kr=m_kernel_references.get(ka);
		 * 
		 * if (kr==null) { kr=new KernelReferences(ka); m_kernel_references.put(ka, kr);
		 * }
		 * 
		 * 
		 * kr.m_madkit_references+=number; if (kr.m_madkit_references==number)
		 * activateGroup(ka); }
		 */
		/*
		 * public synchronized void leaveFromMadkitKernel(KernelAddress ka) {
		 * KernelReferences kr=m_kernel_references.get(ka);
		 * 
		 * if (kr==null) throw new
		 * IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !"
		 * ); kr.m_madkit_references=0; m_kernel_references.remove(kr);
		 * deactivateGroup(ka); }
		 */
		public void decrementMadKitReferences(KernelAddress ka) {

			boolean activate = false;
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null)
					throw new IllegalAccessError(
							"Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");

				--kr.m_madkit_references;
				if (kr.m_madkit_references < 0)
					throw new IllegalAccessError(
							"Problem of data integrity ! The madkit reference for this group shouldn't be lower than 0. This is a MaKitGroupExtension bug !");

				if (kr.m_madkit_references == 1) {
					//TODO check for removing this commentary
					/*if (!isAnyRoleRequestedRecursive(ka))
						m_kernel_references.remove(kr.m_kernel);*/
					activate = true;
					GroupTree p = m_parent;
					while (p != null) {
						KernelReferences krp = p.m_kernel_references.get(ka);
						if (krp == null)
							throw new IllegalAccessError(
									"Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");

						krp.m_all_sub_groups.remove(this);
						/*if (!p.isAnyMadkitCreatedRecursive(ka)) {
							p.m_kernel_references.remove(krp);
						}*/
						
						p.updateDuplicatedSubGroupList(ka);
						p = p.m_parent;
					}

				}
				if (!isAnyRoleRequested())
					m_global_sub_groups_duplicated = null;
			}
			if (activate) {
				resetRepresentedGroupsOfUniverse(ka);
				Group.notifyChangements();
			}

		}

		public void setMadKitCreated(KernelAddress ka, boolean ok) {
			synchronized (m_root) {
				if (ok) {
					if (Group.m_first_kernel == null)
						Group.m_first_kernel = ka;
					KernelReferences kr = m_kernel_references.get(ka);
					if (kr == null) {
						kr = new KernelReferences(ka);
						m_kernel_references.put(ka, kr);

					}
					kr.m_madkit_references++;
					if (kr.m_madkit_references != 1)
						throw new IllegalAccessError("kr.m_madkit_references=" + kr.m_madkit_references);
				} else {
					KernelReferences kr = m_kernel_references.get(ka);
					if (kr == null)
						throw new NullPointerException("kr");
					if (isAnyMadkitCreatedRecursive(ka))
						kr.m_madkit_references = 0;
					else {
						m_kernel_references.remove(ka);
						GroupTree p = m_parent;
						while (p != null) {
							if (!p.isAnyMadkitCreatedRecursive(ka)) {
								KernelReferences krp = p.m_kernel_references.remove(ka);
								if (krp == null)
									throw new IllegalAccessError();
							}
							p = p.m_parent;
						}
					}
				}
			}
		}

		/*
		 * public synchronized void decrementMadKitReferences(int number, KernelAddress
		 * ka) { if (number<=0) throw new
		 * IllegalArgumentException("the argument number ("
		 * +number+") should be greater than zero. This is a MaKitGroupExtension bug !"
		 * ); KernelReferences kr=m_kernel_references.get(ka);
		 * 
		 * if (kr==null) throw new
		 * IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !"
		 * );
		 * 
		 * 
		 * kr.m_madkit_references-=number; if (kr.m_madkit_references<0) throw new
		 * IllegalAccessError("Problem of data integrity ! The madkit reference for this group shouldn't be lower than 0. This is a MaKitGroupExtension bug !"
		 * ); if (kr.m_madkit_references==0) { m_kernel_references.remove(kr);
		 * deactivateGroup(ka); } }
		 */
		public boolean isMadKitCreated(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				return kr != null && kr.m_madkit_references > 0;
			}
		}

		public boolean isAnyRoleRequested(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null)
					return false;

				return kr.m_madkit_references > 1;
			}
		}

		public boolean isAnyRoleRequestedRecursive(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null)
					return false;
				if (kr.m_madkit_references > 1)
					return true;
				for (GroupTree gt : m_sub_groups)
					if (gt.isAnyRoleRequested(ka))
						return true;
				return false;
			}
		}

		public boolean isAnyMadkitCreatedRecursive(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null)
					return false;
				if (kr.m_madkit_references > 0)
					return true;
				for (GroupTree gt : m_sub_groups)
					if (gt.isAnyMadkitCreatedRecursive(ka))
						return true;
				return false;
			}
		}

		public boolean isAnyRoleRequested() {
			synchronized (m_root) {
				for (KernelReferences kr : m_kernel_references.values()) {
					if (kr.m_madkit_references > 1)
						return true;
				}
				return false;
			}
		}
		/*
		 * public void setMadKitCreatedToFalse(KernelAddress ka) { KernelReferences
		 * kr=null; for (KernelReferences k : m_kernel_references) { if
		 * (k.m_kernel.equals(ka)){ kr=k; break; } }
		 * 
		 * if (kr==null) throw new
		 * IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !"
		 * );
		 * 
		 * kr.m_madkit_references=0; }
		 */

		private void updateDuplicatedParentList() {
			synchronized (m_root) {
				GroupTree res[] = new GroupTree[m_parent_groups.size()];
				int i = 0;
				for (GroupTree gt : m_parent_groups) {
					res[i++] = gt;
				}
				m_parent_groups_duplicated.set(res);
			}
		}

		private void updateDuplicatedSubGroupList(KernelAddress ka) {
			synchronized (m_root) {
				KernelReferences kr = m_kernel_references.get(ka);

				if (kr == null)
					throw new IllegalAccessError(
							"Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");

				GroupTree res[] = new GroupTree[kr.m_all_sub_groups.size()];
				int i = 0;
				for (GroupTree gt : kr.m_all_sub_groups) {
					res[i++] = gt;
				}
				kr.m_all_sub_groups_duplicated.set(res);
			}
		}

		private void addSubGroup(GroupTree _g) {
			synchronized (m_root) {
				m_sub_groups.add(_g);
				_g.m_parent_groups.clear();
				if (m_parent != null) {
					_g.m_parent_groups.add(this);
				}
				GroupTree p = m_parent;
				while (p != null) {
					_g.m_parent_groups.add(p);
					p = p.m_parent;
				}
				_g.updateDuplicatedParentList();
			}
		}
		/*
		 * private KernelReferences getKernelReferences(KernelAddress ka) { for
		 * (KernelReferences k : m_kernel_references) { if (k.m_kernel.equals(ka)) {
		 * return k; } } return null; }
		 */
		/*
		 * private void activateGroup(KernelAddress ka) { synchronized(m_root) {
		 * KernelReferences kr=m_kernel_references.get(ka);
		 * 
		 * if (kr==null) throw new
		 * IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !"
		 * ); GroupTree p=m_parent; while (p!=null) { KernelReferences
		 * krp=p.m_kernel_references.get(ka); if (krp==null) { krp=new
		 * KernelReferences(ka); p.m_kernel_references.put(ka, krp); }
		 * krp.m_all_sub_groups.add(this); p.updateDuplicatedSubGroupList(ka);
		 * p=p.m_parent; } } resetRepresentedGroupsOfUniverse(ka);
		 * Group.notifyChangements();
		 * 
		 * } private void deactivateGroup(KernelAddress ka) { synchronized(m_root) {
		 * GroupTree p=m_parent; while (p!=null) { KernelReferences
		 * krp=p.m_kernel_references.get(ka); if (krp==null) throw new
		 * IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !"
		 * );
		 * 
		 * krp.m_all_sub_groups.remove(this); if (krp.m_madkit_references==0)
		 * p.m_kernel_references.remove(krp); p.updateDuplicatedSubGroupList(ka);
		 * p=p.m_parent; } } resetRepresentedGroupsOfUniverse(ka);
		 * Group.notifyChangements(); }
		 */

		private void removeSubGroup(GroupTree _g) {

			if (!m_sub_groups.remove(_g))
				throw new IllegalAccessError("The previous test (after this line code) should return true");
			/*
			 * m_all_sub_groups.remove(_g); updateDuplicatedSubGroupList(); GroupTree
			 * p=m_parent; while (p!=null && p.m_parent!=null) {
			 * p.m_all_sub_groups.remove(_g); p.updateDuplicatedSubGroupList(); }
			 */
			synchronized (m_root) {
				if (m_parent != null) {
					if (m_references == 0 && m_sub_groups.size() == 0)
						m_parent.removeSubGroup(this);
				} else {
					synchronized (m_groups_root) {
						if (m_sub_groups.size() == 0)
							m_groups_root.remove(this.getCommunity());
					}
				}
			}

		}

		public void incrementReferences() {
			synchronized (m_root) {
				++m_references;
			}
		}

		public int getNbReferences() {
			synchronized (m_root) {
				return m_references;
			}
		}

		public void decrementReferences() {
			synchronized (m_root) {
				if (m_parent != null) {
					--m_references;
					if (m_references == 0) {
						if (m_kernel_references.size() > 0)
							throw new IllegalAccessError(
									"The program shouldn't arrive on this line code. This is a MaKitGroupExtension bug !");
						if (m_sub_groups.size() == 0)
							m_parent.removeSubGroup(this);
					}
				} else
					--m_references;
			}
		}

		private final AtomicReference<Group> root_group = new AtomicReference<>(null);

		Group[] getRepresentedGroups(KernelAddress ka) {
			Group g = root_group.get();
			if (g == null) {
				root_group.set(g = new Group(this, true, false));
			}
			return g.getRepresentedGroups(ka);
		}

		Group[] getRepresentedGroups() {
			Group g = root_group.get();
			if (g == null) {
				root_group.set(g = new Group(this, true, false));
			}
			return g.getRepresentedGroups();
		}

	}

}