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

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NoPublicConstructorAA;
import madkit.testing.util.agent.SelfLaunchAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class LaunchAgentClassTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			assertEquals(ALREADY_LAUNCHED, launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent() {
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	final AbstractAgent faulty = new AbstractAgent() {
		@SuppressWarnings("null")
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void launchFailed() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					launchAgent((String) null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void launchNotFound() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent("a"));
			}
		});
	}

	@Test
	public void NoDefaultConstructor() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent(SelfLaunchAA.class.getName()));
			}
		});
	}

	@Test
	public void NoPublicConstructor() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(launchAgent(NoPublicConstructorAA.class.getName()));
			}
		});
	}

	@Test
	public void NotPublic() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(launchAgent(NotPublicAgent.class.getName()));
			}
		});
	}

	@Test
	public void NotAnAgentClass() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent(Object.class.getName()));
			}
		});
	}

}

class NotPublicAgent extends AbstractAgent{
	
}
