/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.scenari.kill;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class KillingWaitingMessageThreadedAgentsTest extends JunitMadkit {

	@Test
	public void brutalKills() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			public void activate() {
				setLogLevel(Level.ALL);
				Agent a;

				a = new WaitingMessageAgent(true, false, true);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(true, true, true);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void brutalKillOnWaitInActivate() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			public void activate() {
				setLogLevel(Level.ALL);
				WaitingMessageAgent a = new WaitingMessageAgent(true, false, false);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(SUCCESS, killAgent(a, 1));
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void brutalKillOnWaitInLive() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			public void activate() {
				setLogLevel(Level.ALL);
				WaitingMessageAgent a = new WaitingMessageAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(SUCCESS, killAgent(a, 1));
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void brutalKillOnWaitInEnd() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			public void activate() {
				setLogLevel(Level.ALL);
				WaitingMessageAgent a = new WaitingMessageAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void normalKills() {// TODO more cases
		launchTest(new AbstractAgent() {
			public void activate() {
				// TODO Auto-generated method stub
				super.activate();
				Agent a;
				a = new WaitingMessageAgent(true, false, false);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(SUCCESS, killAgent(a));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(SUCCESS, killAgent(a));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(true, false, true);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(true, true, true);
				assertEquals(TIMEOUT, launchAgent(a, 1));
				assertEquals(TIMEOUT, killAgent(a, 1));
				assertAgentIsTerminated(a);
			}
		}, true);
	}
}

class WaitingMessageAgent extends DoItDuringLifeCycleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6447448286398006781L;

	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public WaitingMessageAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doIt() {
		waitNextMessage();
		waitNextMessage();
	}

}
