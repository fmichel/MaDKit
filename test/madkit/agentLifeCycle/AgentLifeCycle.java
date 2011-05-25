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
package madkit.agentLifeCycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import java.util.ArrayList;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.testing.util.agent.FaultyAgent;
import madkit.testing.util.agent.KillTargetAgent;
import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.5
 * @version 0.9
 * 
 */
public class  AgentLifeCycle extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1174211473070578640L;
	Agent a;

	public void activate() {

//				noMadkitConsoleLog();
				emptyAgent();
				faultyAgent();
				selfLaunching();
//				selfKilling();
//				selfKillingNoTimeOut();
//				immediateKill();
//				immediateKillWithTimeOut();
//				killTimeOut();
//				randomLaunchAndKill();
//			cascadeKills();
//		noAgentConsoleLog();
//		randomTestingOnLaunch();
//				autoAgentLogFile();
//				madkitLogFileOn();
//	
//		randomTestingOnLaunchAndKill();
	}


	private void cascadeKills(){
		if(logger != null){
			logger.info("\n******************* STARTING CASCADE KILLS *******************\n");
		}
			a = (Agent) launchAgent("madkit.agentLifeCycle.NormalLife", (int)(Math.random()*3));
			assertNotNull(a);
			KillTargetAgent ka = new KillTargetAgent(a);
			assertEquals(SUCCESS,launchAgent(ka,1));
			pause(100);
			killAgent(ka, 0);
			pause(100);
			assertAgentIsTerminated(ka);
			pause(3000);
			assertAgentIsTerminated(a);
	}
	/**
	 * 
	 */
	private void randomTestingOnLaunch() {
		RandomT.killingOn = false;
		randomTesting();
	}
		private void randomTestingOnLaunchAndKill() {
			RandomT.killingOn = true;
		randomTesting();
	}


		private void randomTesting() {
			if(logger != null){
				logger.info("******************* STARTING RANDOM TESTING *******************\n");
			}
			ArrayList<AbstractAgent> agents = new ArrayList<AbstractAgent>();
			for (int i = 0; i < 500; i++) {
				agents.add(new RandomT());
			}
			RandomT.agents=agents;
			assertEquals(SUCCESS,launchAgent(agents.get(0),1));
			boolean notFinished = true;
			while(notFinished){
				if(logger != null){
					logger.info("waiting for the end of the test");
				}
				pause(3000);
				notFinished = false;
				for (AbstractAgent randomTest : agents) {
					try {
						if(randomTest.getState() != State.TERMINATED && randomTest.getState() != State.NOT_LAUNCHED){
							notFinished = true;
							if(logger != null){
								logger.info("Waiting termination of "+randomTest.getName()+" state is "+randomTest.getState());
							}
						}
						
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}


//	private void selfKillingNoTimeOut() {
//		SelfKillAgent.immediateKill=true;
//		SelfKillAgent a = new SelfKillAgent(true);
//		assertEquals(AGENT_CRASH,launchAgent(a,1));
//		pause(100);
//		assertAgentIsTerminated(a);
//		assertFalse(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(false,true);
//		assertEquals(SUCCESS,launchAgent(a,1));		
//		pause(100);
//		assertAgentIsTerminated(a);
//		assertFalse(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(false,false,true);
//		assertEquals(SUCCESS,launchAgent(a,1));		
//		pause(100);
//		assertAgentIsTerminated(a);
//		assertTrue(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(true,false,true);
//		assertEquals(AGENT_CRASH,launchAgent(a,1));		
//		pause(100);
//		assertAgentIsTerminated(a);
//		assertFalse(((LifeCycleTestAgent)a).endDone);
//	}




	/**
	 * 
	 */
	private void immediateKillWithTimeOut() {
		if(logger != null){
			logger.info("******************* STARTING IMMEDIATE AGENT_KILL WITH TIME OUT *******************\n");
		}
		a = (Agent) launchAgent("madkit.agentLifeCycle.NormalLife",0);
		assertNotNull(a);
		assertEquals(SUCCESS, killAgent(a,1));
		ReturnCode res = killAgent(a,2);
		assertTrue(ALREADY_KILLED == res);
		pause(1500);
		assertAgentIsTerminated(a);
		assertTrue(((LifeCycleTestAgent)a).endDone);
	}

	private void immediateKill() {
		if(logger != null){
			logger.info("******************* STARTING IMMEDIATE AGENT_KILL WITH NO TIME OUT *******************\n");
		}
		a = (Agent) launchAgent("madkit.agentLifeCycle.NormalLife",0);
		assertNotNull(a);
		pause(1000);
		assertEquals(SUCCESS, killAgent(a,0));
		assertAgentIsTerminated(a);
		assertFalse(((LifeCycleTestAgent)a).endDone);
		pause(100);
		Agent b = (Agent) launchAgent("madkit.kernel.Agent",10);
		killAgent(b,0);
		assertAgentIsTerminated(b);
	}

	private void selfKilling(){
		if(logger != null){
			logger.info("******************* STARTING SELF KILLING *******************\n");
		}
		SelfKill.immediateKill=false;
		SelfKill a = new SelfKill(true);
		assertEquals(AGENT_CRASH,launchAgent(a,1));
		pause(100);
		assertAgentIsTerminated(a);
//		assertTrue(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(false,true);
//		assertEquals(SUCCESS,launchAgent(a,1));		
//		pause(1000);
//		assertAgentIsTerminated(a);
//		assertTrue(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(false,false,true);
//		assertEquals(SUCCESS,launchAgent(a,1));		
//		pause(1000);
//		assertAgentIsTerminated(a);
//		assertTrue(((LifeCycleTestAgent)a).endDone);
//		a = new SelfKillAgent(true,false,true);
//		assertEquals(AGENT_CRASH,launchAgent(a,1));		
//		pause(1000);
//		assertAgentIsTerminated(a);
//		assertTrue(((LifeCycleTestAgent)a).endDone);
	}

	private void emptyAgent() {
		if(logger != null){
			logger.info("******************* STARTING EMPTY AGENT *******************\n");
		}
		a = (Agent) launchAgent("madkit.kernel.Agent");
		assertEquals(ALREADY_LAUNCHED,launchAgent(a));
		assertEquals(ALREADY_LAUNCHED,launchAgent(a,1));
		assertNotNull(a);
		pause(100);
		assertAgentIsTerminated(a);

		a = (Agent) launchAgent("madkit.kernel.Agent",0);
		assertEquals(ALREADY_LAUNCHED,launchAgent(a));
		assertEquals(ALREADY_LAUNCHED,launchAgent(a,1));
		assertNotNull(a);
		pause(100);
		assertAgentIsTerminated(a);

		a = (Agent) launchAgent("madkit.kernel.Agent",1);
		assertEquals(ALREADY_LAUNCHED,launchAgent(a));
		assertEquals(ALREADY_LAUNCHED,launchAgent(a,1));
		assertNotNull(a);
		pause(100);
		assertAgentIsTerminated(a);

	}

	private void selfLaunching(){
		SelfLaunch a = new SelfLaunch(true);
		assertEquals(SUCCESS,launchAgent(a,1));
		a = new SelfLaunch(false,true);
		assertEquals(SUCCESS,launchAgent(a,1));		
		a = new SelfLaunch(false,false,true);
		assertEquals(SUCCESS,launchAgent(a,1));		
	}

	private void faultyAgent() {
		a = new FaultyAgent(true);
		assertEquals(AGENT_CRASH,launchAgent(a,1));
		assertEquals(ALREADY_LAUNCHED,launchAgent(a,1));
		assertNotNull(a);
		pause(100);
		assertAgentIsTerminated(a);

		FaultyAgent b = new FaultyAgent(false,true);
		assertEquals(SUCCESS,launchAgent(b,1));
		assertEquals(ALREADY_LAUNCHED,launchAgent(b,1));
		assertNotNull(b);
		pause(100);
		assertAgentIsTerminated(b);

		b = new FaultyAgent(false,false,true);
		assertEquals(SUCCESS,launchAgent(b,1));
		assertEquals(ALREADY_LAUNCHED,launchAgent(b,1));
		assertNotNull(b);
		pause(100);
		assertAgentIsTerminated(b);

		b = new FaultyAgent(false,true,true);
		assertEquals(SUCCESS,launchAgent(b,1));
		assertEquals(ALREADY_LAUNCHED,launchAgent(b,1));
		assertNotNull(b);
		pause(100);
		assertAgentIsTerminated(b);

		b = new FaultyAgent(true,true,true);
		assertEquals(AGENT_CRASH,launchAgent(b,1));
		assertEquals(ALREADY_LAUNCHED,launchAgent(b,1));
		assertNotNull(b);
		pause(100);
		assertAgentIsTerminated(b);

	}


	public void killTimeOut(){
		if(logger != null){
			logger.info("******************* STARTING AGENT_KILL TIMEOUT *******************\n");
		}
		a = (Agent) launchAgent("madkit.agentLifeCycle.NormalLife",2);
		assertNotNull(a);
		pause(1000);
		killAgent(a,1);
		assertAgentIsTerminated(a);
		assertTrue(((LifeCycleTestAgent)a).endDone);
	}

}

class LifeCycleTestAgent extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3862837874328598334L;
	public boolean endDone = false;

	@Override
	public void end() {
		if (! endDone) {
			endDone = true;
		}
		else{
			fail("end done twice !!!");
		}
	}

}