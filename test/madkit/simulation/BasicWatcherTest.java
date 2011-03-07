/**
 * 
 */
package madkit.simulation;

import static madkit.kernel.AbstractAgent.ReturnCode.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class BasicWatcherTest extends JUnitBooterAgent{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6063836696265082516L;

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

		
		Watcher w = new Watcher();
		assertEquals(SUCCESS,launchAgent(w));
		Probe<AbstractAgent> a = new Probe<AbstractAgent>("public", "system", "site");
		w.addProbe(a);
		assertEquals(1,a.size());

		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		
		assertEquals(0,a.size());
		
		assertEquals(false,testAgent.createGroupIfAbsent("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));

		assertEquals(1,a.size());

		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));		
		assertEquals(0,a.size());

		// Adding and removing while group goes not exist
		w.removeProbe(a);
		assertEquals(0,a.size());
		w.addProbe(a);
		assertEquals(0,a.size());

		AbstractAgent other = launchAgent("madkit.kernel.AbstractAgent");
		assertNotNull(other);
		
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));
		assertEquals(SUCCESS,other.requestRole("public", "system", "site",null));
		assertEquals(2,a.size());

		w.removeProbe(a);
		assertEquals(0,a.size());

		w.addProbe(a);
		assertEquals(2,a.size());

		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));		
		assertEquals(1,a.size());
		assertEquals(SUCCESS,other.leaveGroup("public", "system"));		
		assertEquals(0,a.size());

		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "site",null));
		assertEquals(SUCCESS,other.requestRole("public", "system", "site",null));
		assertEquals(2,a.size());
		
		killAgent(w);
		assertEquals(0,a.size());
}

}
