package madkit.kernel;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.test.agents.CGRAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class GetAgentAddressInTest extends JunitMadkit {

	@Test
	public void success() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				threadAssertNotNull(getOrgnization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this));
			}
		});
	}

	@Test
	public void nullAfterLeaveRole() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrgnization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveRole(COMMUNITY, GROUP, ROLE);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrgnization().isRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void nullAfterLeaveGroup() {
		launchTestedAgent(new CGRAgent() {
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrgnization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveGroup(COMMUNITY, GROUP);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrgnization().isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTestedAgent(new CGRAgent() {
		    protected void onActivation() {
		    	super.onActivation();
				try {
					AgentAddress aa = getOrgnization().getGroup(null, GROUP).getAgentAddressOf(this);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTestedAgent(new CGRAgent() {
		    protected void onActivation() {
		    	super.onActivation();
				try {
					threadAssertNotNull(getOrgnization().getRole(COMMUNITY, null, ROLE).getAgentAddressOf(this));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

//    @Test
//    public void nullRole() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		createDefaultCGR(this);
//		try {
//		    assertNotNull(getAgentAddressIn(COMMUNITY, GROUP, null));
//		    noExceptionFailure();
//		} catch (NullPointerException e) {
//		    throw e;
//		}
//	    }
//	}, ReturnCode.AGENT_CRASH);
//    }
//
//    @Test
//    public void roleNotExist() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		createDefaultCGR(this);
//		assertNull(getAgentAddressIn(COMMUNITY, GROUP, dontExist()));
//	    }
//	});
//    }
//
//    @Test
//    public void roleNotHandled() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		createDefaultCGR(this);
//		launchAgent(new Agent(){
//		    @Override
//		    protected void activate() {
//			requestRole(COMMUNITY, GROUP, "a");
//			createGroup(COMMUNITY, "a");
//			requestRole(COMMUNITY, "a", "a");
//		    }
//		});
//		assertNull(getAgentAddressIn(COMMUNITY, GROUP, "a"));
//		assertNull(getAgentAddressIn(COMMUNITY, "a", "a"));
//	    }
//	});
//    }
//
//    @Test
//    public void groupNotExist() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		createDefaultCGR(this);
//		assertNull(getAgentAddressIn(COMMUNITY, dontExist(), dontExist()));
//	    }
//	});
//    }
//
//    @Test
//    public void communityNotExist() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		createDefaultCGR(this);
//		assertNull(getAgentAddressIn(dontExist(), dontExist(), dontExist()));
//	    }
//	});
//    }

}
