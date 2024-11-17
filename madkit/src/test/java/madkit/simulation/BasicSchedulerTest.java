
package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertEquals;

import java.util.logging.Level;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.Activator;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.test.agents.CGRAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicSchedulerTest extends JunitMadkit {

	
	@Test
	public void givenSimulationEngine_whenLaunchScheduler_works(){
    	TickBasedScheduler s = new TickBasedScheduler();
		assertEquals(SUCCESS, launchTestScheduler(s));
	}
	
	@Test
	public void addingNullActivator() {
		launchTestedAgent(new CGRAgent() {
		    @Override
			protected void onActivation() {
		    	super.onActivation();
		    	TickBasedScheduler s = new TickBasedScheduler();
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				try {
					Activator a = new EmptyActivator(null, null, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator a = new EmptyActivator(COMMUNITY, null, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator a = new EmptyActivator(COMMUNITY, GROUP, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator a = new EmptyActivator(null, GROUP, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator a = new EmptyActivator(null, null, ROLE);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		launchTestedAgent(new CGRAgent() {
		    @Override
			protected void onActivation() {
		    	super.onActivation();
				TickBasedScheduler s = new TickBasedScheduler();
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				Activator a = new EmptyActivator(null, null, null);
				s.addActivator(a);
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void addingAndRemovingActivators() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
				TickBasedScheduler s = new TickBasedScheduler() {
					@Override
					public void onLiving() {
						pause(10000);
					}
				};
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("public", "system", "site");
				s.addActivator(a);
				threadAssertEquals(1, a.size());

				code = leaveRole("public", "system", "site");
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());

				threadAssertEquals(ALREADY_GROUP, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
				
				System.err.println(a);

				threadAssertEquals(1, a.size());

				threadAssertEquals(SUCCESS, leaveGroup("public", "system"));
				threadAssertEquals(0, a.size());

				// Adding and removing while group does not exist
				s.removeActivator(a);
				threadAssertEquals(0, a.size());
				s.addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
				Agent other = new Agent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(other));

				threadAssertEquals(2, a.size());
				s.removeActivator(a);
				threadAssertEquals(0, a.size());

				s.addActivator(a);
				threadAssertEquals(2, a.size());

				threadAssertEquals(SUCCESS, leaveGroup("public", "system"));
				threadAssertEquals(1, a.size());
				threadAssertEquals(SUCCESS, other.leaveGroup("public", "system"));
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
				threadAssertEquals(SUCCESS, other.requestRole("public", "system", "site", null));
				threadAssertEquals(2, a.size());

				killAgent(s);
				threadAssertEquals(0, a.size());
			}
		});
	}
	

	@Test
	public void addAfterRequestRole() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));
				TickBasedScheduler s = new TickBasedScheduler() {
					@Override
					public void onLiving() {
						pause(10000);
					}
				};
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("public", "system", "site");
				s.addActivator(a);
				threadAssertEquals(1, a.size());

				code = leaveRole("public", "system", "site");
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());
			}
		});
	}
	

	@Test
	public void addBeforeRequestRole() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				TickBasedScheduler s = new TickBasedScheduler() {
					@Override
					public void onLiving() {
						getLogger().setLevel(Level.ALL);
						while (true) {
							pause(1000);
						}
					}
				};
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("public", "system", "site");
				s.addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));

				threadAssertEquals(1, a.size());

				killAgent(s,1);
				threadAssertEquals(0, a.size());
			}
		});
	}
	
	@Test
	public void leaveOnKill() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				TickBasedScheduler s = new TickBasedScheduler() {
					@Override
					public void onLiving() {
						pause(10000);
					}
				};
				threadAssertEquals(SUCCESS, launchTestScheduler(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("public", "system", "site");
				s.addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup("public", "system", false, null));
				threadAssertEquals(SUCCESS, requestRole("public", "system", "site", null));

				threadAssertEquals(1, a.size());

				code = leaveRole("public", "system", "site");
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());
			}
		});
	}
	

}
