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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.NoSuchElementException;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Watcher;
import madkit.testing.agents.SimulatedAgent;
import madkit.testing.agents.SimulatedAgentBis;

/**
 * The Class PropertyProbeTest.
 */
public class PropertyProbeTest extends MadkitConcurrentTestCase {

	/**
	 * Primitive type probing.
	 */
	@Test
	public void primitiveTypeProbing() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField initial").isEqualTo(1);
				PropertyProbe<Double> fp2 = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp2);
				assertThat(fp2.getPropertyValue(agent)).as("publicPrimitiveField initial").isEqualTo(2);
				agent.setPrivatePrimitiveField(10);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField after set").isEqualTo(10);
				assertThat(launchAgent(agent = new SimulatedAgent() {

					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				})).as("launch agent override").isEqualTo(SUCCESS);
				agent.setPrivatePrimitiveField(10);
				assertThat(fp.size()).as("probe size").isEqualTo(2);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField overridden").isEqualTo(100);
				resume();
			}
		});
	}

	/**
	 * Multi type probing.
	 */
	@Test
	public void multiTypeProbing() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				SimulatedAgentBis agentBis;
				launchAgent(agent = new SimulatedAgent());
				launchAgent(agentBis = new SimulatedAgentBis());
				PropertyProbe<Integer> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField agent").isEqualTo(1);
				assertThat(fp.getPropertyValue(agentBis)).as("privatePrimitiveField agentBis").isEqualTo(1);
				PropertyProbe<Double> fp2 = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp2);
				double i = fp2.getPropertyValue(agent);
				System.err.println(i);
				assertThat(fp2.getPropertyValue(agent)).as("publicPrimitiveField").isEqualTo(2);
				agent.setPrivatePrimitiveField(10);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField after set").isEqualTo(10);
				assertThat(launchAgent(agent = new SimulatedAgent() {
					@Override
					protected void onActivation() {
						super.onActivation();
						getLogger().info(this + "******************");
					}

					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				})).as("launch agent with override").isEqualTo(SUCCESS);
				agent.setPrivatePrimitiveField(10);
				assertThat(fp.size()).as("probe size").isEqualTo(2);
				assertThat(fp.getPropertyValue(agent)).as("privatePrimitiveField overridden").isEqualTo(100);
				resume();
			}
		});
	}

	/**
	 * Wrong type probing.
	 */
	@Test
	public void wrongTypeProbing() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(agent));
					noExceptionFailure();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	/**
	 * Wrong source probing.
	 */
	@SuppressWarnings("unused")
	@Test
	public void wrongSourceProbing() {
		runSimuTest(new Watcher() {

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
				resume();
			}
		});
	}

	/**
	 * Wrong type setting.
	 */
	@Test
	public void wrongTypeSetting() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
				PropertyProbe<Object> fp = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					fp.setPropertyValue(agent, "a");
					noExceptionFailure();
				} catch (SimuException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	/**
	 * No such field probing.
	 */
	@Test
	public void noSuchFieldProbing() {
		runSimuTest(new Watcher() {
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
				resume();
			}
		});
	}

	/**
	 * Test get max.
	 */
	@Test
	public void testGetMax() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<Double> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				addProbe(fp);
				assertThat(fp.getMax()).as("max value").isEqualTo(9d);
				resume();
			}
		});
	}

	/**
	 * Test get min.
	 */
	@Test
	public void testGetMin() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<Double> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				Watcher s = new Watcher() {
					@Override
					protected void onActivation() {
						super.onActivation();
					}
				};
				assertThat(launchAgent(s)).as("launch watcher").isEqualTo(SUCCESS);
				s.addProbe(fp);
				assertThat(fp.getMin()).as("min value").isEqualTo(0d);
				resume();
			}
		});
	}

	@Test
	public void getAverageTest() {
		runSimuTest(new Watcher() {
			@Override
			protected void onActivation() {
				for (int i = 0; i < 12; i++) {
					SimulatedAgent agent;
					assertThat(launchAgent(agent = new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
					agent.publicPrimitiveField = i;
					agent.setPrivatePrimitiveField(i * 2);
				}
				PropertyProbe<String> fp = new PropertyProbe<>(GROUP, ROLE, "publicPrimitiveField");
				PropertyProbe<String> fpInt = new PropertyProbe<>(GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				addProbe(fpInt);
				assertThat(fp.getAverage()).as("average public").isEqualTo(5.5d);
				assertThat(fpInt.getAverage()).as("average private").isEqualTo(11d);
				resume();
			}
		});
	}

	/**
	 * No agentget average test.
	 */
	@Test
	public void givenEmptyProbe_whenGetAverage_thenThrowNoSuchElementException() {
		runSimuTest(new Watcher() {
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
				resume();
			}
		});
	}

	/**
	 * Given empty probe when get max then throw no such element exception.
	 */
	@Test
	public void givenEmptyProbe_whenGetMax_thenThrowNoSuchElementException() {
		runSimuTest(new Watcher() {
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
				resume();
			}
		});
	}

	/**
	 * Given non number type when get average then throw class cast exception.
	 */
	@Test
	public void givenNonNumberType_whenGetAverage_thenThrowClassCastException() {
		runSimuTest(new Watcher() {
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
				resume();
			}
		});
	}

	/**
	 * Gets the min and get maxnot comparable.
	 *
	 */
	@Test
	public void getMinAndGetMaxnotComparable() {
		runSimuTest(new Watcher() {

			@Override
			protected void onActivation() {
				// launchDefaultAgent(this);
				assertThat(launchAgent(new SimulatedAgent())).as("launch agent").isEqualTo(SUCCESS);
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
				resume();
			}
		});
	}

}
