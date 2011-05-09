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
package madkit.api.abstractAgent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.GroupIdentifier;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class isCGRTest  extends JunitMadKit{
	
	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(null));
				assertFalse(isGroup(null,null));
				assertFalse(isRole(null,null,null));
			}
		});
	}
	
	@Test
	public void existTrue(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertTrue(isRole(COMMUNITY,GROUP,Madkit.Roles.GROUP_MANAGER_ROLE));
			}
		});
	}
	
	@Test
	public void notExist(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertFalse(isCommunity(aa()));
				assertFalse(isGroup(COMMUNITY,aa()));
				assertFalse(isRole(COMMUNITY,GROUP,aa()));
			}
		});
	}
	
}
