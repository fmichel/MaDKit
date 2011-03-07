/**
 * 
 */
package madkit.api.abstractAgent;

import madkit.kernel.*;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.Madkit.Roles.*;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class LaunchAgentTest  extends JunitMadKit{

	final AbstractAgent target = new Agent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			assertEquals(ALREADY_LAUNCHED,launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new Agent(){
		protected void activate() {
			pause(2000);
		}
	};

	final AbstractAgent faulty = new Agent(){
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(ALREADY_LAUNCHED,launchAgent(target));
				assertEquals(ALREADY_LAUNCHED,launchAgent(this));
			}
		});
	}

	@Test
	public void returnTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(LAUNCH_TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void returnAleradyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(LAUNCH_TIME_OUT,launchAgent(timeOutAgent,0));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}
	
	@Test
	public void returnAgentCrash(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(AGENT_CRASH,launchAgent(faulty,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(faulty));
			}
		});
	}

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,true));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,-1,true));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,0,false));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,0,true));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,true));
				assertEquals(INVALID_ARG,launchAgent((AbstractAgent)null,1,false));
				assertEquals(INVALID_ARG,launchAgent(target,-10));
				assertEquals(INVALID_ARG,launchAgent(target,-10,true));
			}
		});
	}

}
