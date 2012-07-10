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

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class isCGRTest extends JunitMadkit {

	@Test
	public void nullCommunity() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				try {
					assertFalse(isCommunity(null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void nullGroup() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					assertFalse(isGroup(COMMUNITY, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void nullRole() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					assertFalse(isRole(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void existTrue() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertTrue(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
			}
		});
	}

	@Test
	public void notExist() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(aa()));
				assertFalse(isGroup(COMMUNITY, aa()));
				assertFalse(isRole(COMMUNITY, GROUP, aa()));
			}
		});
	}

}
