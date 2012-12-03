/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import madkit.agr.LocalCommunity;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.20
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class GetExistingGroupsTest extends JunitMadkit {

	@Test
	public void onlyLocal() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				System.err.println(getExistingGroups(LocalCommunity.NAME));
				assertEquals(3, getExistingGroups(LocalCommunity.NAME).size());
				assertEquals(LocalCommunity.Groups.GUI, getExistingGroups(LocalCommunity.NAME).first());
			}
		});
	}
	
	@Test
	public void notFound(){
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(getExistingGroups(aa()));
			}
		});
	}

	@Test
	public void createNewAndLeave() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createGroup("aa", "g");
				assertEquals(1, getExistingGroups("aa").size());
				assertEquals("g", getExistingGroups("aa").first());
				createGroup("aa", "g2");
				assertEquals(2, getExistingGroups("aa").size());
				leaveGroup("aa", "g");
				assertEquals(1, getExistingGroups("aa").size());
				assertEquals("g2", getExistingGroups("aa").first());
				leaveGroup("aa", "g2");
				assertEquals(1, getExistingCommunities().size());
				assertEquals(LocalCommunity.NAME, getExistingCommunities().first());
			}
		});
	}

}
