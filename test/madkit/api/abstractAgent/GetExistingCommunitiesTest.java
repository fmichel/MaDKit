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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;
import madkit.agr.LocalCommunity;
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
public class GetExistingCommunitiesTest extends JunitMadkit {

	@Test
	public void onlyLocal() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(1, getExistingCommunities().size());
				assertEquals(LocalCommunity.NAME, getExistingCommunities().first());
			}
		});
	}

	@Test
	public void atLeastOneCommunity(){
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(getExistingCommunities());
			}
		});
	}

	@Test
	public void createNewAndLeave() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createGroup("aa", "g");
				assertEquals(2, getExistingCommunities().size());
				assertEquals("aa", getExistingCommunities().first());
				leaveGroup("aa", "g");
				assertEquals(1, getExistingCommunities().size());
				assertEquals(LocalCommunity.NAME, getExistingCommunities().first());
			}
		});
	}

}
