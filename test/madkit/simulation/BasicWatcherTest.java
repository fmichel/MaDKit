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
import madkit.kernel.JunitMadKit;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class BasicWatcherTest extends JunitMadKit{



	@Test
	public void addingAndRemovingProbes(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup("public", "system", false,null));
				assertEquals(SUCCESS, requestRole("public", "system", "site",null));
				Watcher s = new Watcher();
				assertEquals(SUCCESS,launchAgent(s));
				ReturnCode code;
				/////////////////////////// REQUEST ROLE ////////////////////////
				Probe<AbstractAgent> a = new Probe<AbstractAgent>("public", "system", "site");
				s.addProbe(a);
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
				s.removeProbe(a);
				assertEquals(0,a.size());
				s.addProbe(a);
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
				s.removeProbe(a);
				assertEquals(0,a.size());

				s.addProbe(a);
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
