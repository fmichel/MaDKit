/**
 * 
 */
package madkit.api.abstractAgent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.GroupIdentifier;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class isCGRTest  extends JunitMadKit{
	
	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(null));
				assertFalse(isGroup(null,null));
				assertFalse(isRole(null,null,null));
			}
		});
	}
	
	@Test
	public void existTrue(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertTrue(isRole(COMMUNITY,GROUP,Madkit.Roles.GROUP_MANAGER_ROLE));
			}
		});
	}
	
	@Test
	public void notExist(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertFalse(isCommunity(aa()));
				assertFalse(isGroup(COMMUNITY,aa()));
				assertFalse(isRole(COMMUNITY,GROUP,aa()));
			}
		});
	}
	
}
