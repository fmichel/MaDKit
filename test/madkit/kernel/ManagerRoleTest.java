package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.agr.Organization;

import org.junit.Test;

public class ManagerRoleTest extends JunitMadkit {

	@Test
	public void oneManager() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				launchAgent(new AbstractAgent(){
					/**
					 * 
					 */
					private static final long	serialVersionUID	= 1L;

					@Override
					protected void activate() {
						createDefaultCGR(this);
					}
				});
				try {
					System.err.println("\nplayers="+getKernel().getRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE).players+"\n");
					assertEquals(1, getKernel().getRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE).players.size());
					assertEquals(this, getKernel().getRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE).players.get(0));
				} catch (CGRNotAvailable e) {
					e.printStackTrace();
				}
			}
		});
	}


}
