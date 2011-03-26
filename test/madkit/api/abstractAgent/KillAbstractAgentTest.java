/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.LAUNCH_TIME_OUT;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.*;

import java.util.ArrayList;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.SelfAbstractKill;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class KillAbstractAgentTest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent(){
		protected void activate() {
			pause(1500);
		}
		protected void end() {
			pause(1500);
		};
	};

	final AbstractAgent faulty = new AbstractAgent(){
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,killAgent(target));
			}
		});
	}

	@Test
	public void returnSuccessAfterLaunchTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(LAUNCH_TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(SUCCESS,killAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void returnNOT_YET_LAUNCHEDAfterImmediateLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(NOT_YET_LAUNCHED,killAgent(timeOutAgent));
				assertEquals(LAUNCH_TIME_OUT,launchAgent(timeOutAgent,0));
				pause(10);
				assertEquals(SUCCESS,killAgent(timeOutAgent));
				assertEquals(ALREADY_KILLED,killAgent(timeOutAgent));				
			}
		});
	}

	@Test
	public void returnAlreadyKilled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(AGENT_CRASH,launchAgent(faulty));
				assertEquals(ALREADY_KILLED,killAgent(faulty));
			}
		});
	}
	
	@Test
	public void massKill(){
		addMadkitArgs("--"+Madkit.agentLogLevel,"OFF");
		launchTest(new AbstractAgent(){
			ArrayList<AbstractAgent> list = new ArrayList<AbstractAgent>(100);
			protected void activate() {
				startTimer();
				for (int i = 0; i < 100000;i++) {
					AbstractAgent t = new AbstractAgent();
					list.add(t);
					assertEquals(SUCCESS,launchAgent(t));
				}
				stopTimer("launch time ");
				startTimer();
				for (AbstractAgent a : list) {
					assertEquals(SUCCESS,killAgent(a));
				}
				stopTimer("kill time ");
			}
		});
	}
	
	@Test
	public void returnTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(LAUNCH_TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(LAUNCH_TIME_OUT,killAgent(timeOutAgent,1));
				assertEquals(ALREADY_KILLED,killAgent(timeOutAgent));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void returnAleradyKilled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,killAgent(target));
				assertEquals(ALREADY_KILLED,killAgent(target));
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
	public void selfKilling(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				//the time out should not change anything because target == this
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, false, 0)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, false, 1)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, false, Integer.MAX_VALUE)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, true, 0)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, true, 1)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, true, Integer.MAX_VALUE)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, true, 0)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, true, 1)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, true, Integer.MAX_VALUE)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, false, 0)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, false, 1)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, false, Integer.MAX_VALUE)));
			}
		});
	}
	
	@Test
	public void killFaulty(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent a;
				assertEquals(AGENT_CRASH,launchAgent(a = new FaultyAA(true, false)));
				assertEquals(ALREADY_KILLED,killAgent(a));
				
				assertEquals(LAUNCH_TIME_OUT,launchAgent(a = new FaultyAA(true, false),0));
				pause(10);
				ReturnCode r = killAgent(a);
				assertTrue(r == SUCCESS || r == ALREADY_KILLED);
				
				assertEquals(LAUNCH_TIME_OUT,launchAgent(a = new FaultyAA(true, false),0));
				pause(200);
				assertEquals(ALREADY_KILLED,killAgent(a));
				
				//in end
				assertEquals(SUCCESS,launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS,killAgent(a));

				assertEquals(SUCCESS,launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS,killAgent(a));
				
				assertEquals(SUCCESS,launchAgent(a = new FaultyAA(false, true)));
				assertEquals(SUCCESS,killAgent(a));
				
				assertEquals(SUCCESS,launchAgent(a = new FaultyAA(false, true)));
				assertEquals(LAUNCH_TIME_OUT,killAgent(a,0));
				pause(10);//avoid interleaving
				assertEquals(ALREADY_KILLED,killAgent(a));
				
			}
		});
	}
	
	
	@Test
	public void testAlone(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, true, 0)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(true, false, 1)));
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, true, Integer.MAX_VALUE)));
			}
		});
	}
}
