package madkit.simulation.overlooker;

import org.testng.annotations.BeforeClass;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.simulation.activator.MethodActivator;

//FIXME
public class OverlookerTest extends JunitMadkit {

	private static MethodActivator buggy;

	@BeforeClass
	public void setUp() throws Exception {
		buggy = new MethodActivator(COMMUNITY, GROUP, ROLE, "doIt") {
		  @Override
		protected void onRemoving(Agent agent) {
		      AgentAddress address = agent.getOrgnization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(agent);
		      System.err.println("agent addreess "+address);
		      threadAssertNotNull(address);
		  }
		};
	}

//	@Test
//	public void overlookerRemovingAgentHasStillRoleAfterLeaveRole() {
//		launchTestedAgent(new Scheduler() {
//			protected void activate() {
//				NormalAA a;
//				launchAgent(a = new NormalAA() {
//				    @Override
//				    protected void activate() {
//				        super.activate();
//				        doIt();
//				    }
//				    private void doIt() {
//					getLogger().info("doingIt");
//				    }
//				});
//				addActivator(buggy);
//				assertEquals(1, buggy.getCurrentAgentsList().size());
//				assertEquals(ReturnCode.SUCCESS, a.leaveRole(COMMUNITY, GROUP, ROLE));
//				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
//				System.err.println("agent addreess "+address);
//				assertNull(address);
//			}
//		}, ReturnCode.SUCCESS);
//	}
//
//	@Test
//	public void overlookerRemovingAgentHasStillRoleAfterLeaveGroup() {
//		launchTestedAgent(new Scheduler() {
//			protected void activate() {
//				NormalAA a;
//				launchAgent(a = new NormalAA() {
//				    @Override
//				    protected void activate() {
//				        super.activate();
//				        doIt();
//				    }
//				    private void doIt() {
//					getLogger().info("doingIt");
//				    }
//				});
//				addActivator(buggy);
//				assertEquals(1, buggy.getCurrentAgentsList().size());
//				assertEquals(ReturnCode.SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
//				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
//				System.err.println("agent addreess "+address);
//				assertNull(address);
//			}
//		}, ReturnCode.SUCCESS);
//	}
//
//	
//	
//	@Test
//	public void overlookerRemovingDeadlockOnAgentAddress() {
//		launchTestedAgent(new Scheduler() {
//			protected void activate() {
//				buggy = new MethodActivator<Agent>(COMMUNITY, GROUP, ROLE, "doIt") {
//					  protected void removing(final Agent agent) {
//					     Runnable r = new Runnable() {
//
//						public void run() {
//						    System.err.println("before get agent addreess ");
//						    AgentAddress address = agent.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
//						    System.err.println("agent addreess " + address);
//						    threadAssertNotNull(address);
//						}
//					    };
//					    Thread t = new Thread(r);
//					    t.start();
//					    try {
//						t.join();
//					    }
//					    catch(InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					    }
//					  }
//					};
//				NormalAA a;
//				launchAgent(a = new NormalAA() {
//				    @Override
//				    protected void activate() {
//				        super.activate();
//				        doIt();
//				    }
//				    private void doIt() {
//					getLogger().info("doingIt");
//				    }
//				});
//				addActivator(buggy);
//				assertEquals(1, buggy.getCurrentAgentsList().size());
//				assertEquals(ReturnCode.SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
//				AgentAddress address = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
//				System.err.println("agent addreess "+address);
//				assertNull(address);
//			}
//		}, ReturnCode.SUCCESS);
//	}
}
