/**
 * 
 */
package madkit.organization;

import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class BasicRequestOrgTest extends JUnitBooterAgent{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2249689565781155735L;
	AbstractAgent other = new AbstractAgent();
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		launchAgent(other);
		/////////////////////////// REQUEST ROLE ////////////////////////
		code = testAgent.requestRole("public", "system", "test",null);
		assertEquals(NOT_COMMUNITY,code);
		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		
		//////////////////////// REQUESTS ///////////////////
		AgentAddress aa = other.getAgentWithRole("public", "system", "site");
		assertNotNull(aa);
		if(logger != null)
			logger.info("aa = "+aa);
		
		aa = testAgent.getAgentWithRole("public", "system", "site");// it is alone so a null is returned
		assertNull(aa);
		
		aa = testAgent.getAgentWithRole("publicc", "system", "site");// it is alone so a null is returned
		assertNull(aa);
		
		aa = testAgent.getAgentWithRole("public", "systemm", "site");// it is alone so a null is returned
		assertNull(aa);
		
		aa = testAgent.getAgentWithRole("public", "system", "sitee");// it is alone so a null is returned
		assertNull(aa);
		
		
	}

}
