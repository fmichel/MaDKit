/**
 * 
 */
package madkit.kernel;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import madkit.kernel.AbstractAgent.ReturnCode;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author fab
 *
 */
public class AgentLoggerTest {

	AbstractAgent a;
	private String agentLogFileName = "AbstractAgent-1";
	private AgentLogger logger;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		a = new AbstractAgent();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		new File(agentLogFileName ).deleteOnExit();
	}
	
	@AfterClass
	public static void after(){
		LogManager.getLogManager().reset();
	}
	
	@Test public void testNoLogger(){
		assertNull(a.getLogger());
		a.setLogLevel(Level.OFF);
		assertNull(a.getLogger());
	}
	
	@Test public void testNoKernelChangeName() throws SecurityException, IOException{
		assertNull(a.getLogger());
		a.setName("TEST");
		assertNull(a.getLogger());
		a.setLogLevel(Level.INFO);
		assertNotNull(a.getLogger());
		a.getLogger().info(ReturnCode.ALREADY_GROUP.getMessage());
		a.setName("NEW_NAME");
		assertEquals(a.getLogger().getName(), "[NEW_NAME] ");
//		a.getLogger().addHandler(new FileHandler(a.getName()));//adding an arbitrary handler
		a.getLogger().info("I have a new name");
//		a.setLogLevel(Level.OFF);
//		assertNull(a.getLogger());
//		a.setName("OO");
//		assertNull(a.getLogger());
		a.setLogLevel(Level.ALL);//TODO should use previous file
		a.getLogger().finest("finest");
	}
	
	@Test
	public void testNoNameLogger(){
		a.setLogLevel(Level.FINE);
		System.err.println(a.getLogger().getName());
		a.getLogger().info(a.getState().toString());
		assertTrue(a.getLogger().getName().equals("["+a.getClass().getSimpleName()+"-"+a.hashCode()+"] "));
		Exception e = new NotAvailableActionWarning(ReturnCode.NOT_YET_LAUNCHED, "agent not launched");
		e.fillInStackTrace();
		a.getLogger().log(Level.WARNING, "testing log", e);
		a.getLogger().log(Level.SEVERE, "testing log", e);
//		Utils.logWarningException(a.getLogger(), e, "testing log");
	}

	@Test
	public final void testCSAgentLogger() {
//		logger = new AgentLogger(a);
//		logger.init(a, true, "null", null);
//		logger.info("test");
//		logger.setLevel(Level.ALL);
//		logger.fine("fine");
	}

	@Test
	public final void testCSAndAutoLogDirLogger() {
//		a.setLogLevel(Level.INFO);
//		AgentLogger logger = new AgentLogger(a);
//		logger.init(a, true, "bin/", null);
//		logger.info("notExist");
//		logger.setLevel(Level.ALL);
//		logger.fine("fine");
//		logger.close();
	}
	
	@Test
	public final void testInitFromOtherLogger(){
//		AgentLogger logger = new AgentLogger(a);
//		logger.init(a, true, "bin/", null);
//		logger.setLevel(Level.ALL);
//		logger.info("notExist");
//		a.setName("Test");
//		AgentLogger logger2 = new AgentLogger(a);
//		logger2.init(a, logger, "bin/");
//		logger2.setLevel(Level.ALL);
//		logger2.fine("notExist");
		
	}

	/**
	 * Test method for {@link java.util.logging.Logger#log(java.util.logging.LogRecord)}.
	 */
	@Test
	public final void testLogLogRecord() {
		fail("Not yet implemented"); // TODO
	}

}
