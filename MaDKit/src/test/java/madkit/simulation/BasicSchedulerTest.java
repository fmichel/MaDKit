package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertEquals;

import java.util.function.Consumer;
import java.util.logging.Level;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Watcher;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.Activator;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.test.agents.CGRAgent;
import madkit.test.agents.SimulatedAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicSchedulerTest extends JunitMadkit {

	@Test
	public void givenSimulationEngine_whenLaunchScheduler_works() {
		TickBasedScheduler s = new TickBasedScheduler();
		launchSimuAgentTest(s);
	}

	@Test
	public void givenNewActivator_whenAddedBeforeAgentsJoin_thenSizeIsCorrect() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				super.onActivation();
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				launchAgent(new SimulatedAgent());
				threadAssertEquals(1, ea.size());
			}
		});
	}

	@Test
	public void givenNewActivator_whenAddedAfterAgentsJoined_thenSizeIsCorrect() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				super.onActivation();
				launchAgent(new SimulatedAgent());
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				threadAssertEquals(1, ea.size());
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				Activator a = new EmptyActivator(null, null);
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void addingAndRemovingActivators() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator(GROUP, ROLE);
				addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(ALREADY_GROUP, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));

				threadAssertEquals(1, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				// Adding and removing while group does not exist
				removeActivator(a);
				threadAssertEquals(0, a.size());
				addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				SimuAgent other = new SimuAgent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(other));

				threadAssertEquals(2, a.size());
				removeActivator(a);
				threadAssertEquals(0, a.size());
				addActivator(a);
				threadAssertEquals(2, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(1, a.size());
				threadAssertEquals(SUCCESS, other.leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				threadAssertEquals(SUCCESS, other.requestSimuRole(GROUP, ROLE));
				threadAssertEquals(2, a.size());
				killAgent(other);
				threadAssertEquals(1, a.size());
			}
		});
	}

	@Test
	public void addAfterRequestRole() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createSimuGroup("system"));
				threadAssertEquals(SUCCESS, requestSimuRole("system", "site"));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("system", "site");
				addActivator(a);
				threadAssertEquals(1, a.size());

				code = leaveSimuRole("system", "site");
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());
			}
		});
	}

}
