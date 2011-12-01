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
package madkit.simulation;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Watcher;
import madkit.testing.util.agent.SimulatedAgent;
import madkit.testing.util.agent.SimulatedAgentBis;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class PropertyProbeTest extends JunitMadKit {

	@Test
	public void primitiveTypeProbing() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SimulatedAgent agent;
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				PropertyProbe<AbstractAgent, Integer> fp = new PropertyProbe<AbstractAgent, Integer>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				s.addProbe(fp);
				assertTrue(1 == fp.getPropertyValue(agent));
				PropertyProbe<AbstractAgent, Double> fp2 = new PropertyProbe<AbstractAgent, Double>(COMMUNITY, GROUP, ROLE,
						"publicPrimitiveField");
				s.addProbe(fp2);
				assertTrue(2 == fp2.getPropertyValue(agent));
				agent.setPrivatePrimitiveField(10);
				assertTrue(10 == fp.getPropertyValue(agent));
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent() {
					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				assertEquals(2, fp.size());
				assertTrue(100 == fp.getPropertyValue(agent));
			}
		});
	}

	@Test
	public void multiTypeProbing() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SimulatedAgent agent;
				SimulatedAgentBis agentBis;
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				assertEquals(SUCCESS, launchAgent(agentBis = new SimulatedAgentBis()));
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				PropertyProbe<AbstractAgent, Integer> fp = new PropertyProbe<AbstractAgent, Integer>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				s.addProbe(fp);
				assertTrue(1 == fp.getPropertyValue(agent));
				assertTrue(1 == fp.getPropertyValue(agentBis));
				PropertyProbe<AbstractAgent, Double> fp2 = new PropertyProbe<AbstractAgent, Double>(COMMUNITY, GROUP, ROLE,
						"publicPrimitiveField");
				s.addProbe(fp2);
				double i = fp2.getPropertyValue(agent);
				System.err.println(i);
				assertTrue(2 == fp2.getPropertyValue(agent));
				agent.setPrivatePrimitiveField(10);
				assertTrue(10 == fp.getPropertyValue(agent));
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent() {
					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				assertEquals(3, fp.size());
				assertTrue(100 == fp.getPropertyValue(agent));
			}
		});
	}

	@Test
	public void wrongTypeProbing() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SimulatedAgent agent;
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				PropertyProbe<AbstractAgent, String> fp = new PropertyProbe<AbstractAgent, String>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				s.addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(agent));
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void noSuchFieldProbing() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchDefaultAgent(this);
				PropertyProbe<AbstractAgent, String> fp = new PropertyProbe<AbstractAgent, String>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				try {
					System.err.println(fp.getAllProperties());
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

}