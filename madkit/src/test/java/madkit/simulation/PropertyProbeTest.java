package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import java.util.NoSuchElementException;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Watcher;
import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.probe.PropertyProbe;
import madkit.test.agents.CGRAgent;
import madkit.test.agents.SimulatedAgent;
import madkit.test.agents.SimulatedAgentBis;


/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class PropertyProbeTest extends JunitMadkit {

	@Test
	public void primitiveTypeProbing() {
		launchTestedAgent(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<Integer> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				threadAssertTrue(1 == fp.getPropertyValue(agent));
				PropertyProbe<Double> fp2 = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
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

	@Test
	public void multiTypeProbing() {
		launchTestedAgent(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				SimulatedAgentBis agentBis;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				threadAssertEquals(SUCCESS, launchAgent(agentBis = new SimulatedAgentBis()));
				PropertyProbe<Integer> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				threadAssertTrue(1 == fp.getPropertyValue(agent));
				threadAssertTrue(1 == fp.getPropertyValue(agentBis));
				PropertyProbe<Double> fp2 = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
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
						getLogger().info(this+"******************");
					}
					@Override
					public void setPrivatePrimitiveField(int privatePrimitiveField) {
						super.setPrivatePrimitiveField(100);
					}
				}));
				agent.setPrivatePrimitiveField(10);
				threadAssertEquals(3, fp.size());
				threadAssertTrue(100 == fp.getPropertyValue(agent));
			}
		});
	}

	@Test
	public void wrongTypeProbing() {
		launchTestedAgent(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(agent));
					noExceptionFailure();
				} catch (ClassCastException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@SuppressWarnings("unused")
	@Test
	public void wrongSourceProbing() {
		launchTestedAgent(new Watcher() {

			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, launchAgent(new SimulatedAgent()));
				PropertyProbe<Integer> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE,
						"privatePrimitiveField");
				addProbe(fp);
				try {
					CGRAgent normalAA = new CGRAgent() {

						String	privatePrimitiveField	= "test";
					};
					System.err.println(fp.getPropertyValue(normalAA));
					int i = fp.getPropertyValue(normalAA);
					noExceptionFailure();
				} catch (ClassCastException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void wrongTypeSetting() {
		launchTestedAgent(new Watcher() {

			@Override
			protected void onActivation() {
				SimulatedAgent agent;
				threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
				PropertyProbe<Object> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				addProbe(fp);
				try {
					fp.setPropertyValue(agent, "a");
					noExceptionFailure();
				} catch (SimulationException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void noSuchFieldProbing() {
		launchTestedAgent(new Agent() {

			@Override
			protected void onActivation() {
				launchAgent(new CGRAgent());
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				try {
					System.err.println(fp.getPropertyValue(fp.getCurrentAgentsList().get(0)));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void testGetMax() {
		launchTestedAgent(new Agent() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				threadAssertEquals(9d, fp.getMax());
			}
		});
	}

	@Test
	public void testGetMin() {
		launchTestedAgent(new Agent() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 10; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
				}
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				threadAssertEquals(0d, fp.getMin());
			}
		});
	}

	@Test
	public void getAverageTest() {
		launchTestedAgent(new Agent() {

			@Override
			protected void onActivation() {
				for (int i = 0; i < 12; i++) {
					// launchDefaultAgent(this);
					SimulatedAgent agent;
					threadAssertEquals(SUCCESS, launchAgent(agent = new SimulatedAgent()));
					agent.publicPrimitiveField = i;
					agent.setPrivatePrimitiveField(i*2);
				}
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
				PropertyProbe<String> fpInt = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "privatePrimitiveField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				s.addProbe(fpInt);
				threadAssertEquals(5.5d, fp.getAverage());
				threadAssertEquals(11d, fpInt.getAverage());
			}
		});
	}

	@Test
	public void noAgentgetAverageTest() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "publicPrimitiveField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
				try {
					fp.getAverage();
					noExceptionFailure();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	@Test
	public void getMinAndGetMaxnotComparable() {
		launchTestedAgent(new GenericTestAgent() {

			@Override
			protected void onActivation() {
				// launchDefaultAgent(this);
				threadAssertEquals(SUCCESS, launchAgent(new SimulatedAgent()));
				PropertyProbe<String> fp = new PropertyProbe<>(COMMUNITY, GROUP, ROLE, "objectField");
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				s.addProbe(fp);
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