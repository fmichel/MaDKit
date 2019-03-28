package madkit.simulation.overlooker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Scheduler;
import madkit.simulation.activator.GenericBehaviorActivator;
import madkit.testing.util.agent.NormalAA;

public class OverlookerTest extends JunitMadkit {

	private static GenericBehaviorActivator<AbstractAgent> buggy;

	@Before
	public void setUp() throws Exception {
		buggy = new GenericBehaviorActivator<AbstractAgent>(COMMUNITY, GROUP, ROLE, "doIt") {
		  protected void removing(AbstractAgent agent) {
		      AgentAddress address = agent.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
		      System.err.println("agent addreess "+address);
		      assertNotNull(address);
		  }
		};
	}

	@Test
	public void overlookerRemovingAgentHasStillRoleAfterLeaveRole() {
		launchTest(new Scheduler() {
			protected void activate() {
				NormalAA a;
				launchAgent(a = new NormalAA() {
				    @Override
				    protected void activate() {
				        super.activate();
				        doIt();
				    }
				    private void doIt() {
					getLogger().info("doingIt");
				    }
				});
				addActivator(buggy);
				assertEquals(1, buggy.getCurrentAgentsList().size());
				assertEquals(ReturnCode.SUCCESS, a.leaveRole(COMMUNITY, GROUP, ROLE));
				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				System.err.println("agent addreess "+address);
				assertNull(address);
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void overlookerRemovingAgentHasStillRoleAfterLeaveGroup() {
		launchTest(new Scheduler() {
			protected void activate() {
				NormalAA a;
				launchAgent(a = new NormalAA() {
				    @Override
				    protected void activate() {
				        super.activate();
				        doIt();
				    }
				    private void doIt() {
					getLogger().info("doingIt");
				    }
				});
				addActivator(buggy);
				assertEquals(1, buggy.getCurrentAgentsList().size());
				assertEquals(ReturnCode.SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				System.err.println("agent addreess "+address);
				assertNull(address);
			}
		}, ReturnCode.SUCCESS);
	}

	
	
	@Test
	public void overlookerRemovingDeadlockOnAgentAddress() {
		launchTest(new Scheduler() {
			protected void activate() {
				buggy = new GenericBehaviorActivator<AbstractAgent>(COMMUNITY, GROUP, ROLE, "doIt") {
					  protected void removing(final AbstractAgent agent) {
					     Runnable r = new Runnable() {

						public void run() {
						    System.err.println("before get agent addreess ");
						    AgentAddress address = agent.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
						    System.err.println("agent addreess " + address);
						    assertNotNull(address);
						}
					    };
					    Thread t = new Thread(r);
					    t.start();
					    try {
						t.join();
					    }
					    catch(InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					    }
					  }
					};
				NormalAA a;
				launchAgent(a = new NormalAA() {
				    @Override
				    protected void activate() {
				        super.activate();
				        doIt();
				    }
				    private void doIt() {
					getLogger().info("doingIt");
				    }
				});
				addActivator(buggy);
				assertEquals(1, buggy.getCurrentAgentsList().size());
				assertEquals(ReturnCode.SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				System.err.println("agent addreess "+address);
				assertNull(address);
			}
		}, ReturnCode.SUCCESS);
	}
}
