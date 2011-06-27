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
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.SelfLaunchAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAbstractAgentTest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			assertEquals(ALREADY_LAUNCHED,launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent(){
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	final AbstractAgent faulty = new AbstractAgent(){
		@SuppressWarnings("null")
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyNoGUI(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
//				assertNull(target.getGUIComponent());
				assertEquals(ALREADY_LAUNCHED,launchAgent(target));
			}
		});
	}

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				try {
					assertEquals(SUCCESS,launchAgent((AbstractAgent)null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					assertEquals(e.getMessage(),"agent");
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS,launchAgent((AbstractAgent)null,true));
					noExceptionFailure();
				} catch (NullPointerException e) {
					assertEquals(e.getMessage(),"agent");
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS,launchAgent((AbstractAgent)null,1));
					noExceptionFailure();
				} catch (NullPointerException e) {
					assertEquals(e.getMessage(),"agent");
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS,launchAgent((AbstractAgent)null,1,true));
					noExceptionFailure();
				} catch (NullPointerException e) {
					assertEquals(e.getMessage(),"agent");
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void returnSuccessAndAlreadyLaunchWithGUI(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target,true));
				assertEquals(ALREADY_LAUNCHED,launchAgent(target,true));
//				assertNotNull(target.getGUIComponent());
			}
		});
	}

	@Test
	public void returnTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIMEOUT,launchAgent(timeOutAgent,1));
				assertEquals(TIMEOUT,launchAgent(new AbstractAgent(),0));
				assertEquals(TIMEOUT,launchAgent(new AbstractAgent(),-1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void massLaunch(){
		addMadkitArgs("--"+LevelOption.agentLogLevel,"OFF");
		launchTest(new AbstractAgent(){
			int number = 1000;
			protected void activate() {
				assertEquals(SUCCESS, createGroup(JunitMadKit.COMMUNITY,JunitMadKit.GROUP));
				startTimer();
				for (int i = 0; i < number;i++) {
					assertEquals(SUCCESS,launchAgent(new AbstractAgent()));
				}
				stopTimer("launch time ");
				startTimer();
				for (int i = 0; i < number;i++) {
					ReturnCode r = launchAgent(new AbstractAgent(),0);
					assertTrue(TIMEOUT == r || SUCCESS == r);
				}
				stopTimer("launch time ");
			}
		});
	}

	@Test
	public void massLaunchWithGUI(){
		addMadkitArgs("--"+LevelOption.agentLogLevel,"OFF");
		launchTest(new AbstractAgent(){
			int number = 50;
			protected void activate() {
				assertEquals(SUCCESS, createGroup(JunitMadKit.COMMUNITY,JunitMadKit.GROUP));
				startTimer();
				for (int i = 0; i < number;i++) {
					assertEquals(SUCCESS,launchAgent(new AbstractAgent(),true));
				}
				stopTimer("launch time ");
				startTimer();
				for (int i = 0; i < number;i++) {
					ReturnCode r = launchAgent(new AbstractAgent(),0,true);
					assertTrue(TIMEOUT == r || SUCCESS == r);
				}
				stopTimer("launch time ");
			}
		});
	}

	@Test
	public void returnAleradyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(ALREADY_LAUNCHED,launchAgent(target));
				
				ReturnCode r = launchAgent(timeOutAgent,0,true);
				assertTrue(TIMEOUT == r || SUCCESS == r);
				r = launchAgent(timeOutAgent,true);
				assertTrue(ALREADY_LAUNCHED == r || SUCCESS == r);
			}
		});
	}
	
	@Test
	public void returnAgentCrash(){
		addMadkitArgs("--kernelLogLevel","ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(AGENT_CRASH,launchAgent(faulty));
				assertEquals(ALREADY_LAUNCHED,launchAgent(faulty));
			}
		});
	}
	
	@Test
	public void SelfLaunching(){
		addMadkitArgs("--kernelLogLevel","ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
		SelfLaunchAA a = new SelfLaunchAA(true);
		assertEquals(SUCCESS,launchAgent(a,1));
		a = new SelfLaunchAA(false,true);
		assertEquals(SUCCESS,launchAgent(a,1));		
			}
		});
	}

	

}
