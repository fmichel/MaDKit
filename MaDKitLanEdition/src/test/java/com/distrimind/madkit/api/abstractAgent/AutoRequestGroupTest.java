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

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 */
public class AutoRequestGroupTest extends JunitMadkit {
	@Test
	public void testAutoRequest() {
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Assert.assertFalse(hasGroup(GROUP));
				autoRequesteRole(GROUP, ROLE, null);
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				AbstractAgent aa = new AbstractAgent();
				Assert.assertEquals(ReturnCode.SUCCESS, launchAgent(aa));

				Assert.assertEquals(ReturnCode.SUCCESS, aa.requestRole(GROUP, ROLE));
				Assert.assertFalse(aa.isConcernedByAutoRequestRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				System.out.println("ici1");
				Assert.assertEquals(ReturnCode.SUCCESS, aa.leaveRole(GROUP, ROLE));

				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));

				Assert.assertEquals(ReturnCode.SUCCESS, aa.requestRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertEquals(ReturnCode.SUCCESS, aa.leaveRole(GROUP, ROLE));
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));

				Assert.assertEquals(ReturnCode.SUCCESS, aa.requestRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertEquals(ReturnCode.SUCCESS, aa.leaveGroup(GROUP));
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));

				Assert.assertEquals(ReturnCode.SUCCESS, aa.requestRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				leaveAutoRequestedGroup(GROUP);
				Assert.assertFalse(isConcernedByAutoRequestRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertEquals(ReturnCode.SUCCESS, aa.leaveGroup(GROUP));
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));

				autoRequesteRole(GROUP, ROLE, null);
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));
				aa.requestRole(GROUP, ROLE);
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));

				leaveGroup(GROUP);
				Assert.assertFalse(isConcernedByAutoRequestRole(GROUP, ROLE));
				aa.leaveGroup(GROUP);
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				aa.requestRole(GROUP, ROLE);
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));

				autoRequesteRole(GROUP, ROLE, null);
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				AbstractAgent aa2 = new AbstractAgent();
				launchAgent(aa2);
				aa2.requestRole(GROUP, ROLE);
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(aa2.hasGroup(GROUP));
				Assert.assertTrue(aa2.hasRole(GROUP, ROLE));
				autoRequesteRole(GROUP, ROLE, null);
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(aa2.hasGroup(GROUP));
				Assert.assertTrue(aa2.hasRole(GROUP, ROLE));

				aa.leaveGroup(GROUP);
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertTrue(aa2.hasGroup(GROUP));
				Assert.assertTrue(aa2.hasRole(GROUP, ROLE));
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));
				aa2.leaveGroup(GROUP);
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertFalse(aa.hasGroup(GROUP));
				Assert.assertFalse(aa.hasRole(GROUP, ROLE));
				Assert.assertFalse(aa2.hasGroup(GROUP));
				Assert.assertFalse(aa2.hasRole(GROUP, ROLE));
				Assert.assertTrue(isConcernedByAutoRequestRole(GROUP, ROLE));
				aa.requestRole(GROUP, ROLE);
				Assert.assertTrue(hasGroup(GROUP));
				Assert.assertTrue(hasRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
				leaveAllAutoRequestedGroups();
				Assert.assertFalse(hasGroup(GROUP));
				Assert.assertFalse(hasRole(GROUP, ROLE));
				Assert.assertTrue(aa.hasGroup(GROUP));
				Assert.assertTrue(aa.hasRole(GROUP, ROLE));
			}
		});
	}
}
