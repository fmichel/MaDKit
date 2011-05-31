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

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIME_OUT;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.testing.util.agent.SelfLaunch;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.5
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAgentTest  extends JunitMadKit{

	final AbstractAgent target = new Agent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			assertEquals(ALREADY_LAUNCHED,launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new Agent(){
		protected void activate() {
			pause(2000);
		}
	};

	final AbstractAgent faulty = new Agent(){
		@SuppressWarnings("null")
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(ALREADY_LAUNCHED,launchAgent(target));
				assertEquals(ALREADY_LAUNCHED,launchAgent(this));
			}
		});
	}

	@Test
	public void returnTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void returnAleradyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,0));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void returnAgentCrash(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(AGENT_CRASH,launchAgent(faulty,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(faulty));
			}
		});
	}

	@Test
	public void selfLaunching(){
		addMadkitArgs("--kernelLogLevel","ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
		SelfLaunch a = new SelfLaunch(true);
		assertEquals(SUCCESS,launchAgent(a,1));
		a = new SelfLaunch(false,true);
		assertEquals(SUCCESS,launchAgent(a,1));		
		a = new SelfLaunch(false,false,true);
		assertEquals(SUCCESS,launchAgent(a,1));		
			}
		});
	}

	@Test
	public void nullArgs(){
		addMadkitArgs("--kernelLogLevel","ALL");
	launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(new AbstractAgent(),-1,true));
//				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,true));
//				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,0,false));
//				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,0,true));
//				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,true));
//				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,false));
//				assertEquals(INVALID_ARG,launchAgent(target,-10));
//				assertEquals(INVALID_ARG,launchAgent(target,-10,true));
			}
		});
	}
	
}
