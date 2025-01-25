/*******************************************************************************
 * MaDKit - Multiagent Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.util.NoSuchElementException;

import org.testng.annotations.Test;

import madkit.kernel.MadkitUnitTestCase;
import madkit.kernel.Watcher;
import madkit.test.agents.SimulatedAgent;
import madkit.test.agents.SimulatedAgentBis;

/**
 * The Class PropertyProbeTest.
 */
public class PropertyProbeTest extends MadkitUnitTestCase {

	/**
	 * Primitive type probing.
	 */
	@Test
	public void primitiveTypeProbing() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				threadAssertTrue(1 == fp.getPropertyValue(agent));
				PropertyProbe<Double> fp2 = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp2);
				threadAssertTrue(2 == fp2.getPropertyValue(agent));
				agent.setPrivatePrimitiveField(10);
				threadAssertTrue(10 == fp.getPropertyValue(agent));
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent() {

					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				threadAssertEquals(2, fp.size());
				threadAssertTrue(100 == fp.getPropertyValue(agent));
			}
		});
	}

	/**
	 * Multi type probing.
	 */
	@Test
	public void multiTypeProbing() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				SimulatedAgentBis agentBis;
				launchAgent(agent = new SimulatedAgent());
				launchAgent(agentBis = new SimulatedAgentBis());
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				threadAssertTrue(1 == fp.getPropertyValue(agent));
				threadAssertTrue(1 == fp.getPropertyValue(agentBis));
				PropertyProbe<Double> fp2 = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp2);
				double i = fp2.getPropertyValue(agent);
				System.err.println(i);
				threadAssertTrue(2 == fp2.getPropertyValue(agent));
				agent.setPrivatePrimitiveField(10);
				threadAssertTrue(10 == fp.getPropertyValue(agent));
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent() {
					@Override
					protected void onActivation() {
						super.onActivation();
						getLogger().info(this + "******************");
					}

					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				threadAssertEquals(2, fp.size());
				threadAssertTrue(100 == fp.getPropertyValue(agent));
			}
		});
	}

	/**
	 * Wrong type probing.
	 */
	@Test
	public void wrongTypeProbing() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(agent));
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Wrong source probing.
	 */
	@SuppressWarnings("unused")
	@Test
	public void wrongSourceProbing() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				launchAgent(new SimulatedAgent());
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					SimulatedAgent normalAA = new SimulatedAgent() {
						String privatePrimitiveField = "test";
					};
					System.err.println(fp.getPropertyValue(normalAA));
					int i = fp.getPropertyValue(normalAA);
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Wrong type setting.
	 */
	@Test
	public void wrongTypeSetting() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<Object> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					fp.setPropertyValue(agent, "a");
					noExceptionFailure();
				} catch (SimuException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * No such field probing.
	 */
	@Test
	public void noSuchFieldProbing() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				launchAgent(new SimuAgent() {
					@Override
					protected void onActivation() {
						super.onActivation();
						createSimuGroup(GROUP);
						requestSimuRole(GROUP, ROLE);
					}
				});
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(fp.getAgents().get(0)));
					noExceptionFailure();
				} catch (SimuException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Test get max.
	 */
	@Test
	public void testGetMax() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp);
				threadAssertEquals(9d, fp.getMax());
			}
		});
	}

	/**
	 * Test get min.
	 */
	@Test
	public void testGetMin() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				Watcher s = new Watcher() {
					@Override
					protected void onActivation() {
						super.onActivation();
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				threadAssertEquals(0d, fp.getMin());
			}
		});
	}

	/**
	 * Gets the average test.
	 *
	 * @return the average test
	 */
	@Test
	public void getAverageTest() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				for (int i = 0; i < 12; i++) {
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
					agent.setPrivatePrimitiveField(i * 2);
				}
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				PropertyProbe<String> fpInt = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				addProbe(fpInt);
				threadAssertEquals(5.5d, fp.getAverage());
				threadAssertEquals(11d, fpInt.getAverage());
			}
		});
	}

	/**
	 * No agentget average test.
	 */
	@Test
	public void givenEmptyProbe_whenGetAverage_thenThrowNoSuchElementException() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp);
				try {
					fp.getAverage();
					noExceptionFailure();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Given empty probe when get max then throw no such element exception.
	 */
	@Test
	public void givenEmptyProbe_whenGetMax_thenThrowNoSuchElementException() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp);
				try {
					fp.getMax();
					noExceptionFailure();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Given non number type when get average then throw class cast exception.
	 */
	@Test
	public void givenNonNumberType_whenGetAverage_thenThrowClassCastException() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				launchAgent(new SimulatedAgent());

				// Given
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "objectField");
				addProbe(fp);

				// When & Then
				try {
					fp.getAverage();
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Gets the min and get maxnot comparable.
	 *
	 */
	@Test
	public void getMinAndGetMaxnotComparable() {
		launchSimuAgentTest(new Watcher() {

			@Override
			protected void onActivation() {
				// launchDefaultAgent(this);
				threadAssertEquals(SUCCESS, launchAgent(new SimulatedAgent()));
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "objectField");
				addProbe(fp);
				try {
					System.err.println(fp.getMax());
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				try {
					System.err.println(fp.getAverage());
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				try {
					System.err.println(fp.getMin());
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});
	}

}