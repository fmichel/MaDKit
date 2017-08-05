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
package com.distrimind.madkit.api.abstractAgent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Enumeration;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.20
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class GetExistingGroupsTest extends JunitMadkit {

	@Test
	public void onlyLocal() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				System.err.println(getExistingGroups(LocalCommunity.NAME));
				assertEquals(4, getSize(getExistingGroups(LocalCommunity.NAME)));
				Assert.assertTrue(contains(getExistingGroups(LocalCommunity.NAME), LocalCommunity.Groups.GUI));
			}
		});
	}

	@Test
	public void notFound() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNull(getExistingGroups(aa()));
			}
		});
	}

	@Test
	public void createNewAndLeave() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				createGroup(new Group("aa", "g"));
				assertEquals(1, getSize(getExistingGroups("aa")));
				assertEquals(new Group("aa", "g"), getExistingGroups("aa").nextElement());
				createGroup(new Group("aa", "g2"));
				assertEquals(2, getSize(getExistingGroups("aa")));
				leaveGroup(new Group("aa", "g"));
				assertEquals(1, getSize(getExistingGroups("aa")));
				assertEquals(new Group("aa", "g2"), getExistingGroups("aa").nextElement());
				leaveGroup(new Group("aa", "g2"));
				assertEquals(1, getExistingCommunities().size());
				assertEquals(LocalCommunity.NAME, getExistingCommunities().first());
			}
		});
	}

	static int getSize(Enumeration<Group> e) {
		int size = 0;
		for (; e.hasMoreElements(); e.nextElement())
			++size;
		return size;
	}

	static boolean contains(Enumeration<Group> e, Group group) {
		for (; e.hasMoreElements();)
			if (e.nextElement().equals(group))
				return true;
		return false;
	}

}
