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

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.7
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class GetAgentsWithRoleTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(SUCCESS, createGroup(GROUP));
		}
	};

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					getAgentsWithRole(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, createGroup(GROUP));
					assertNull(getAgentsWithRole(null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {

					assertNull(getAgentsWithRole(GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(new Group(null, G), ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void returnNull() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(GROUP, ROLE));
				assertEquals(0, getAgentsWithRole(new Group(aa(), G), ROLE).size());
				assertEquals(0, getAgentsWithRole(new Group(C, aa()), ROLE).size());
				assertEquals(0, getAgentsWithRole(GROUP, aa()).size());
				assertEquals(1, getAgentsWithRole(GROUP, ROLE).size());
				assertEquals(SUCCESS, target.leaveRole(GROUP, ROLE));
				assertEquals(0, getAgentsWithRole(GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(0, getAgentsWithRole(GROUP, ROLE).size());
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(GROUP, ROLE));
				assertNotNull(getAgentsWithRole(GROUP, ROLE));
				assertEquals(1, getAgentsWithRole(GROUP, ROLE).size());
				launchAgent(new AbstractAgent() {
					protected void activate() {
						assertEquals(SUCCESS, requestRole(GROUP, ROLE));
					}
				});
				assertNotNull(getAgentsWithRole(GROUP, ROLE));
				assertEquals(2, getAgentsWithRole(GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void returnNotNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(target);
				assertNotNull(getAgentsWithRole(GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(1, getAgentsWithRole(GROUP, Organization.GROUP_MANAGER_ROLE).size());
			}
		});
	}

	@Test
	public void returnNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(0, getAgentsWithRole(GROUP, Organization.GROUP_MANAGER_ROLE).size());
			}
		});
	}
}