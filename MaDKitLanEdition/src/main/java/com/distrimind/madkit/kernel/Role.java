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

/**
 * This class enables to encapsulate a role and a group (and its community).
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see Group
 * @see AbstractGroup
 */
public class Role {
	/**
	 * The represented group
	 */
	private final Group m_group;

	/**
	 * The represented role
	 */
	private final String m_role;

	/**
	 * The concatenation of the cummunity, the group and the group, separated by
	 * semicolons.
	 */
	private final String m_group_role;

	/**
	 * Constructing a Role assiciated with a group and a community.
	 * 
	 * @param _group
	 *            the group
	 * @param _role
	 *            the role
	 * @see Group
	 * @see AbstractGroup
	 */
	public Role(Group _group, String _role) {
		if (_role.contains(";"))
			throw new IllegalArgumentException(
					"The role given as parameter (" + _role + ") cannot contains a ';' character !");
		m_group = _group;
		m_role = _role;
		m_group_role = m_group.getCommunity() + "," + m_group.getPath() + "," + m_role;
	}

	/**
	 * Return the represented group.
	 * 
	 * @return the represented group
	 */
	public Group getGroup() {
		return m_group;
	}

	/**
	 * Return the represented role.
	 * 
	 * @return the represented role
	 */
	public String getRole() {
		return m_role;
	}

	@Override
	public String toString() {
		return m_group_role;
	}

	public boolean equals(Role _r) {
		return m_group_role.equals(m_group);
	}

	@Override
	public boolean equals(Object _r) {
		return _r == null ? false : ((_r instanceof Role) ? m_group_role.equals(m_group) : false);
	}
}
