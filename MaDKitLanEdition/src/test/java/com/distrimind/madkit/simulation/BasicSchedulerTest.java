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
package com.distrimind.madkit.simulation;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Activator;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Scheduler;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.2
 * @version 1.0
 * 
 */

public class BasicSchedulerTest extends JunitMadkit {

	@Test
	public void addingNullActivator() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				createDefaultCGR(this);
				Scheduler s = new Scheduler();
				assertEquals(SUCCESS, launchAgent(s));
				try {
					new EmptyActivator(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(GROUP, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(null, ROLE);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				createDefaultCGR(this);
				Scheduler s = new Scheduler();
				assertEquals(SUCCESS, launchAgent(s));
				try {
					new EmptyActivator(null, null);
					noExceptionFailure();
				} catch (NullPointerException ignored) {

				}
			}
		});
	}

	@Test
	public void addingAndRemovingActivators() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, requestRole(new Group("public", "system"), "site", null));
				Scheduler s = new Scheduler() {
					@Override
					public void liveCycle() throws InterruptedException {
						pause(15000);
						this.killAgent(this);

					}
				};
				assertEquals(SUCCESS, launchAgent(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator<AbstractAgent> a = new EmptyActivator(new Group("public", "system"), "site");
				s.addActivator(a);
				assertEquals(1, new Group("public", "system").getRepresentedGroups(getKernelAddress()).length);
				assertEquals(1, a.size());

				code = leaveRole(new Group("public", "system"), "site");
				assertEquals(SUCCESS, code);
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup(new Group("public", "system"), null));
				assertEquals(SUCCESS, requestRole(new Group("public", "system"), "site", null));

				assertEquals(1, a.size());

				assertEquals(SUCCESS, leaveGroup(new Group("public", "system")));
				assertEquals(0, a.size());

				// Adding and removing while group does not exist
				s.removeActivator(a);
				assertEquals(0, a.size());
				s.addActivator(a);
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup(new Group("public", "system"), null));
				assertEquals(SUCCESS, requestRole(new Group("public", "system"), "site", null));
				AbstractAgent other = new AbstractAgent() {
					@Override
					protected void activate() {
						assertEquals(SUCCESS, requestRole(new Group("public", "system"), "site", null));
					}
				};
				assertEquals(SUCCESS, launchAgent(other));

				assertEquals(2, a.size());
				s.removeActivator(a);
				assertEquals(0, a.size());

				s.addActivator(a);
				assertEquals(2, a.size());

				assertEquals(SUCCESS, leaveGroup(new Group("public", "system")));
				assertEquals(1, a.size());
				assertEquals(SUCCESS, other.leaveGroup(new Group("public", "system")));
				assertEquals(0, a.size());

				assertEquals(SUCCESS, requestRole(new Group("public", "system"), "site", null));
				assertEquals(SUCCESS, other.requestRole(new Group("public", "system"), "site", null));
				assertEquals(2, a.size());

				killAgent(s);
				assertEquals(0, a.size());
			}
		});
	}

}
