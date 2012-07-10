/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.15
 * @version 0.9
 * 
 */

@SuppressWarnings("serial")
public class GetAgentAddressInTest extends JunitMadkit {

	@Test
	public void success() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;
			protected void activate() {
				createDefaultCGR(this);
				assertNotNull(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;

			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;

			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(COMMUNITY, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;

			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void roleNotExist() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(COMMUNITY, GROUP, aa()));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void roleNotHandled() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;
			protected void activate() {
				createDefaultCGR(this);
				launchAgent(new AbstractAgent(){
					@Override
					protected void activate() {
						requestRole(COMMUNITY, GROUP, "a");
						createGroup(COMMUNITY, "a");
						requestRole(COMMUNITY, "a", "a");
					}
				});
				assertNull(getAgentAddressIn(COMMUNITY, GROUP, "a"));
				assertNull(getAgentAddressIn(COMMUNITY, "a", "a"));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void groupNotExist() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(COMMUNITY, aa(), aa()));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void communityNotExist() {
		launchTest(new AbstractAgent() {
			private static final long serialVersionUID = 1L;
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(aa(), aa(), aa()));
			}
		}, ReturnCode.SUCCESS);
	}

}
