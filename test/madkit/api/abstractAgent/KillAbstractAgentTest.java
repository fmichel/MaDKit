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
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.DoItDuringLifeCycleAbstractAgent;
import madkit.testing.util.agent.FaultyAA;
import madkit.testing.util.agent.NormalAA;
import madkit.testing.util.agent.SelfAbstractKill;
import madkit.testing.util.agent.TimeOutAA;
import madkit.testing.util.agent.UnstopableAbstractAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class KillAbstractAgentTest  extends JunitMadKit{

	@Test
	public void returnSuccess(){
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINEST");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				NormalAA naa = new NormalAA();
				assertEquals(SUCCESS,launchAgent(naa));
				assertEquals(SUCCESS,killAgent(naa));
			}
		});
	}


	@Test
	public void returnNOT_YET_LAUNCHEDAfterImmediateLaunch(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"ALL");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINEST");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT,launchAgent(to,0));
				ReturnCode r = killAgent(to);
				assertTrue(r == NOT_YET_LAUNCHED || r == SUCCESS);				
			}
		});
	}

	@Test
	public void returnAlreadyKilled(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"FINEST");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				FaultyAA f = new FaultyAA(true);
				if(logger != null)
					logger.info("activating");
				assertEquals(AGENT_CRASH,launchAgent(f));
				pause(100);
				assertEquals(ALREADY_KILLED,killAgent(f));
			}
		});
	}
	
	@Test
	public void massKill(){
		printAllStacks();
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"FINEST");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent(){
			ArrayList<AbstractAgent> list = new ArrayList<AbstractAgent>(100);
			protected void activate() {
				setLogLevel(Level.ALL);
				startTimer();
				for (int i = 0; i < 100;i++) {
					AbstractAgent t = new AbstractAgent();
					list.add(t);
					assertEquals(SUCCESS,launchAgent(t));
				}
				stopTimer("launch time ");
				startTimer();
				for (AbstractAgent a : list) {
//					killAgent(a,0);
					assertEquals(SUCCESS,killAgent(a,1));
				}
				stopTimer("kill time ");
			}
		});
	}
	
	@Test
	public void returnTimeOut(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"FINEST");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT,launchAgent(to,1));
				while(to.getState() != State.LIVING){
					pause(100);
				}
				assertEquals(TIMEOUT,killAgent(to,1));
				assertEquals(ALREADY_KILLED,killAgent(to));
				assertEquals(ALREADY_LAUNCHED,launchAgent(to));
				pause(1000);
			}
		});
	}
	
	
	@Test
	public void returnAleradyKilled(){
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
				NormalAA target = new NormalAA();
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,killAgent(target));
				assertEquals(ALREADY_KILLED,killAgent(target));
			}
		});
	}
	
	@Test
	public void noTimeoutKill(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				NormalAA target = new NormalAA();
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,killAgent(target,0));
			}
		});
	}
	
	@Test
	public void returnAgentCrash(){
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.ALL);
				FaultyAA f = new FaultyAA(true);
				assertEquals(AGENT_CRASH,launchAgent(f));
				assertEquals(ALREADY_LAUNCHED,launchAgent(f));
			}
		});
	}
	
	@Test
	public void selfKill(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.OFF);
				
				ReturnCode r = launchAgent(new SelfKillAA(true),1);
				assertTrue(r == SUCCESS || r == AGENT_CRASH);
				AbstractAgent a = new SelfKillAA(false,true);
				assertEquals(SUCCESS,launchAgent(a,1));
				assertEquals(SUCCESS,killAgent(a,1));
			}
		});
	}

	@Test
	public void selfKillInActivate(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				setLogLevel(Level.OFF);
				assertEquals(TIMEOUT,launchAgent(new SelfKillAA(true,true),0));
				assertEquals(AGENT_CRASH,launchAgent(new SelfKillAA(true,true),1));
				assertEquals(AGENT_CRASH,launchAgent(new SelfKillAA(true,true)));
			}
		});
	}
	
	@Test
	public void selfKilling(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				//the time out should not change anything because target == this
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, false, 0)));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, false, 1)));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, false, Integer.MAX_VALUE)));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, true, 0)));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, true, 1)));
				assertEquals(AGENT_CRASH,launchAgent(new SelfAbstractKill(true, true, Integer.MAX_VALUE)));
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
				ReturnCode r = killAgent(a);
				assertTrue(r == SUCCESS || r == ALREADY_KILLED);
				
				assertEquals(TIMEOUT,launchAgent(a = new FaultyAA(true, false),0));
				pause(10);
				r = killAgent(a);
				assertTrue(r == SUCCESS || r == ALREADY_KILLED);
				
				assertEquals(TIMEOUT,launchAgent(a = new FaultyAA(true, false),0));
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
				assertEquals(SUCCESS,killAgent(a,0));
				pause(10);//avoid interleaving
				assertEquals(ALREADY_KILLED,killAgent(a));
				
			}
		});
	}
	
	
	@Test
	public void selfKillInActivateAndEnd(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				SelfAbstractKill a = new SelfAbstractKill(true, true, 0);
				assertEquals(AGENT_CRASH,launchAgent(a));
				pause(1000);
				assertAgentIsTerminated(a);
			}
		});
	}
	
	@Test
	public void selfKillinActivate(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				SelfAbstractKill a = new SelfAbstractKill(true, false, 1);
				assertEquals(AGENT_CRASH,launchAgent(a));
				pause(100);
				assertEquals(ALREADY_KILLED,killAgent(a));
				assertAgentIsTerminated(a);
			}
		});
	}
	
	@Test
	public void selfKillinEndAndWaitKill(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(new SelfAbstractKill(false, true, Integer.MAX_VALUE)));
			}
		});
	}
	
	@Test
	public void cascadeKills(){
		launchTest(new AbstractAgent(){
			protected void activate() {
			Killer a = new Killer();
			Killer b = new Killer();
			a.setTarget(b);
			b.setTarget(a);
			launchAgent(a);
			launchAgent(b);
			assertEquals(SUCCESS,killAgent(a));
			assertEquals(ALREADY_KILLED,killAgent(a));
		}});
	}
	@Test
	public void returnSuccessAfterLaunchTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				TimeOutAA to = new TimeOutAA(true, true);
				assertEquals(TIMEOUT,launchAgent(to,1));
				assertEquals(SUCCESS,killAgent(to));
				assertAgentIsTerminated(to);
			}
		});
	}
	
	@Test
	public void killUnstopable(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				unstopableAgent.setLogLevel(Level.FINER);
				startTimer();
				assertEquals(TIMEOUT,launchAgent(unstopableAgent,1));
				stopTimer("launch time out ");
				assertEquals(TIMEOUT,killAgent(unstopableAgent,1));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}
	
	@Test
	public void brutalKillUnstopable(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				assertEquals(TIMEOUT,launchAgent(unstopableAgent,1));
				if(logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(SUCCESS,killAgent(unstopableAgent,0));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}
	
	@Test
	public void brutalKillUnstopableUsingSelfRef(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				assertEquals(TIMEOUT,launchAgent(unstopableAgent,2));
				assertEquals(SUCCESS,unstopableAgent.killAgent(unstopableAgent,0));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}
	
	@Test
	public void killUnstopableUsingSelfRef(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAbstractAgent();
				assertEquals(TIMEOUT,launchAgent(unstopableAgent,2));
				assertEquals(TIMEOUT,unstopableAgent.killAgent(unstopableAgent,2));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
		printAllStacks();
	}
	
}

@SuppressWarnings("serial")
class SelfKillAA extends DoItDuringLifeCycleAbstractAgent{

	public SelfKillAA() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SelfKillAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
		// TODO Auto-generated constructor stub
	}

	public SelfKillAA(boolean inActivate) {
		super(inActivate);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void doIt() {
		super.doIt();
		killAgent(this);
	}
}


@SuppressWarnings("serial")
class Killer extends AbstractAgent{
	AbstractAgent target;
	/**
	 * @param target the target to set
	 */
	final void setTarget(AbstractAgent target) {
		this.target = target;
	}
	
	@Override
	protected void end() {
		killAgent(target);
		killAgent(target);
	}
	
}
