/**
 * 
 */
package madkit.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Scheduler;
import test.util.JUnitBooterAgent;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

/**
 * @author fab
 *
 */
public class BasicSchedulerTest extends JUnitBooterAgent{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2877345828231387457L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		/////////////////////////// REQUEST ROLE ////////////////////////
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));

		
		Scheduler s = new Scheduler(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 3584722159226480940L;

			@Override
			public void live() {
				pause(10000);
			}
		};
		assertEquals(SUCCESS,launchAgent(s));
		Activator<AbstractAgent> a = new Activator<AbstractAgent>("public", "system", "site");
		s.addActivator(a);
		assertEquals(1,a.size());

		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		
		assertEquals(0,a.size());
		
		assertEquals(ALREADY_GROUP,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));

		assertEquals(1,a.size());

		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));		
		assertEquals(0,a.size());

		// Adding and removing while group goes not exist
		s.removeActivator(a);
		assertEquals(0,a.size());
		s.addActivator(a);
		assertEquals(0,a.size());

		AbstractAgent other = launchAgent("madkit.kernel.AbstractAgent");
		assertNotNull(other);
		
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));
		assertEquals(SUCCESS,other.requestRole("public", "system", "site",null));
		assertEquals(2,a.size());

		s.removeActivator(a);
		assertEquals(0,a.size());

		s.addActivator(a);
		assertEquals(2,a.size());

		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));		
		assertEquals(1,a.size());
		assertEquals(SUCCESS,other.leaveGroup("public", "system"));		
		assertEquals(0,a.size());

		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));
		assertEquals(SUCCESS,other.requestRole("public", "system", "site",null));
		assertEquals(2,a.size());
		
		killAgent(s);
		assertEquals(0,a.size());
}

}
