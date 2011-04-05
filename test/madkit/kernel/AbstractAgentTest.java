/**
 * 
 */
package madkit.kernel;

import static org.junit.Assert.*;

import java.util.logging.Level;
import madkit.kernel.AbstractAgent.ReturnCode;

import org.junit.Before;
import org.junit.Test;

/**
 * @author fab
 *
 */
public class AbstractAgentTest {
	
	AbstractAgent a,b;
	
	@Before
	public void setup(){
		a = new AbstractAgent();
		b = new AbstractAgent();
		a.kernel = new MadkitKernel(new Madkit(null));
	}
	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		assertEquals(a.hashCode(), 1);
		assertEquals(5, new AbstractAgent().hashCode());//1+kernel+booter+junit+new = 5
	}
	
	@Test
	public void testKernelNull(){
		b.setLogLevel(Level.INFO);
		b.launchAgent(new AbstractAgent());
	}
	
	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	public final void testToString() {
		System.err.println(a);
	}

	@Test
	public final void testCompareTo(){
		AbstractAgent b = new AbstractAgent();
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertTrue(a.compareTo(a) == 0);
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		AbstractAgent b = new AbstractAgent();
		assertFalse(a.equals(b));
		assertFalse(a.equals(null));
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getKernel()}.
	 */
	@Test
	public final void testGetKernel() {
		assertNotNull(a.getKernel());
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#isAlive()}.
	 */
	@Test
	public final void testIsAlive() {
		assertFalse(a.isAlive());
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getLogger()}.
	 */
	@Test
	public final void testGetLogger() {
		assertEquals(a.getLogger(),AbstractAgent.defaultLogger);
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertNotNull(a.getName());
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		assertNotNull(a.getName());
		a.setName("test");
		assertTrue(a.getName().equals("test"));
//		assertNull
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#setLogLevel(java.util.logging.Level)}.
	 */
	@Test
	public final void testSetLogLevel() {
		assertEquals(a.getLogger(),AbstractAgent.defaultLogger);
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
		a.setLogLevel(Level.INFO);
		assertNotNull(a.logger);
		a.setName("new");
		System.err.println(a.getLogger().getName());
		assertTrue(a.getLogger().getName().equals(a.getLoggingName()));//one space for no concatenation of string
		assertTrue(a.getLoggingName().endsWith("new]"));
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
	}


	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getRunState()}.
	 */
	@Test
	public final void testGetRunState() {
		assertTrue(a.getState() == AbstractAgent.State.NOT_LAUNCHED);
		assertEquals(ReturnCode.SUCCESS, a.launchAgent(a));
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getState()}.
	 */
	@Test
	public final void testGetAgentState() {
		a.getState();
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getAgentExecutor()}.
	 */
	@Test
	public final void testGetAgentExecutor() {
		assertNull(a.getMyLifeCycle());
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject1() {
		AbstractAgent b = new AbstractAgent();
		assertFalse(b.equals(a));
		assertTrue(a.equals(a));
	}


}
