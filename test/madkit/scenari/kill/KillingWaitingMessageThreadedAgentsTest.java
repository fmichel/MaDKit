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
package madkit.scenari.kill;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class  KillingWaitingMessageThreadedAgentsTest extends JunitMadKit{

	@Test
	public void brutalKills() {//TODO brutal kill with to < 0
		launchTest(new AbstractAgent(){
			public void activate() {
				Agent a;
				a = new WaitingMessageAgent(true,false,false);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(SUCCESS, killAgent(a,-1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false,true,false);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(SUCCESS, killAgent(a,-1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false,false,true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(SUCCESS, killAgent(a,-1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(true,false,true);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(SUCCESS, killAgent(a,-1));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(true,true,true);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(SUCCESS, killAgent(a,-1));
				assertAgentIsTerminated(a);
			}});
	}



	@Test
	public void normalKills() {//TODO more cases
		launchTest(new AbstractAgent(){
			public void activate() {
				// TODO Auto-generated method stub
				super.activate();
				Agent a;
				a = new WaitingMessageAgent(true,false,false);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(SUCCESS, killAgent(a));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false,true,false);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(SUCCESS, killAgent(a));
				assertAgentIsTerminated(a);

				a = new WaitingMessageAgent(false,false,true);
				assertEquals(SUCCESS, launchAgent(a));
				pause(100);
				assertEquals(TIMEOUT, killAgent(a,1));
				assertEquals(State.ENDING, a.getState());

				a = new WaitingMessageAgent(true,false,true);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(TIMEOUT, killAgent(a,1));
				assertEquals(State.ENDING, a.getState());

				a = new WaitingMessageAgent(true,true,true);
				assertEquals(TIMEOUT, launchAgent(a,1));
				assertEquals(TIMEOUT, killAgent(a,1));
				assertEquals(State.ENDING, a.getState());
			}});
	}
}


class WaitingMessageAgent extends DoItDuringLifeCycleAgent{

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
	public void doIt() {		waitNextMessage();	}

}

