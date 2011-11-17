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

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class FieldProbeTest extends JunitMadKit {

	@Test
	public void primitiveTypeProbing() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				SimulatedAgent agent;
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				FieldProbe<AbstractAgent, Integer> fp = new FieldProbe<AbstractAgent, Integer>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				s.addProbe(fp);
				assertTrue(1 == fp.getFieldValueFor(agent));
				FieldProbe<AbstractAgent, Double> fp2 = new FieldProbe<AbstractAgent, Double>(COMMUNITY, GROUP, ROLE,
						"publicPrimitiveField");
				s.addProbe(fp2);
				double i = fp2.getFieldValueFor(agent);
				assertTrue(2 == fp2.getFieldValueFor(agent));
				agent.setPrivatePrimitiveField(10);
				assertTrue(10 == fp.getFieldValueFor(agent));
				assertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent() {
					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				assertEquals(2, fp.size());
				assertTrue(100 == fp.getFieldValueFor(agent));
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
				FieldProbe<AbstractAgent, String> fp = new FieldProbe<AbstractAgent, String>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				s.addProbe(fp);
				try {
					System.err.println(fp.getFieldValueFor(agent));
					noExceptionFailure();
				} catch (ClassCastException e) {
				}
			}
		});
	}

}