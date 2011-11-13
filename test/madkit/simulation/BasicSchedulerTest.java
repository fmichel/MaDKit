/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Activator;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Scheduler;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class BasicSchedulerTest extends JunitMadKit{


	@Test
	public void addingNullActivator(){
			launchTest(new AbstractAgent(){
				protected void activate() {
					createDefaultCGR(this);
					Scheduler s = new Scheduler();
					assertEquals(SUCCESS,launchAgent(s));
					try {
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(null, null, null);
						s.addActivator(a);
						noExceptionFailure();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					try {
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(COMMUNITY, null, null);
						s.addActivator(a);
						noExceptionFailure();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					try {
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(COMMUNITY, GROUP, null);
						s.addActivator(a);
						noExceptionFailure();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					try {
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(null, GROUP, null);
						s.addActivator(a);
						noExceptionFailure();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					try {
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(null, null, ROLE);
						s.addActivator(a);
						noExceptionFailure();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			});
	}


	@Test
	public void addingNullActivatorExceptionPrint(){
			launchTest(new AbstractAgent(){
				protected void activate() {
					createDefaultCGR(this);
					Scheduler s = new Scheduler();
					assertEquals(SUCCESS,launchAgent(s));
						Activator<AbstractAgent> a = new Activator<AbstractAgent>(null, null, null);
						s.addActivator(a);
					}
			},ReturnCode.AGENT_CRASH);
	}


	@Test
	public void addingAndRemovingActivators(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup("public", "system", false,null));
				assertEquals(SUCCESS, requestRole("public", "system", "site",null));
				Scheduler s = new Scheduler(){
					public void live() {
						pause(10000);
					}
				};
				assertEquals(SUCCESS,launchAgent(s));
				ReturnCode code;
				/////////////////////////// REQUEST ROLE ////////////////////////
				Activator<AbstractAgent> a = new Activator<AbstractAgent>("public", "system", "site");
				s.addActivator(a);
				assertEquals(1,a.size());

				code = leaveRole("public", "system", "site");
				assertEquals(SUCCESS,code);
				assertEquals(0,a.size());
				
				assertEquals(ALREADY_GROUP,createGroup("public", "system", false,null));
				assertEquals(SUCCESS,requestRole("public", "system", "site",null));

				assertEquals(1,a.size());

				assertEquals(SUCCESS,leaveGroup("public", "system"));		
				assertEquals(0,a.size());

				// Adding and removing while group does not exist
				s.removeActivator(a);
				assertEquals(0,a.size());
				s.addActivator(a);
				assertEquals(0,a.size());

				assertEquals(SUCCESS,createGroup("public", "system", false,null));
				assertEquals(SUCCESS,requestRole("public", "system", "site",null));
				AbstractAgent other = new AbstractAgent(){
					protected void activate() {
						assertEquals(SUCCESS,requestRole("public", "system", "site",null));
					}
				};
				assertEquals(SUCCESS, launchAgent(other));

				assertEquals(2,a.size());
				s.removeActivator(a);
				assertEquals(0,a.size());

				s.addActivator(a);
				assertEquals(2,a.size());

				assertEquals(SUCCESS, leaveGroup("public", "system"));		
				assertEquals(1,a.size());
				assertEquals(SUCCESS,other.leaveGroup("public", "system"));		
				assertEquals(0,a.size());

				assertEquals(SUCCESS,createGroup("public", "system", false,null));
				assertEquals(SUCCESS,requestRole("public", "system", "site",null));
				assertEquals(SUCCESS,other.requestRole("public", "system", "site",null));
				assertEquals(2,a.size());

				killAgent(s);
				assertEquals(0,a.size());
			}});
	}

}
