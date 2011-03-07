/**
 * 
 */
package madkit.kernel;

import static org.junit.Assert.*;

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
public class UtilsTest {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#getI18N(java.lang.String)}.
	 */
	@Test
	public final void testGetI18N() {
		assertEquals(ReturnCode.ALREADY_GROUP.getMessage(), " already exists");
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#createFileHandler(java.lang.String, java.util.logging.Logger)}.
	 */
	@Test
	public final void testCreateFileHandler() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#logWarningException(java.util.logging.Logger, java.lang.Exception, java.lang.String, java.util.logging.Level)}.
	 */
	@Test
	public final void testLogWarningExceptionLoggerExceptionStringLevel() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#logWarningException(java.util.logging.Logger, java.lang.Exception, java.lang.String)}.
	 */
	@Test
	public final void testLogWarningExceptionLoggerExceptionString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#logSevereException(java.util.logging.Logger, java.lang.Throwable, java.lang.String)}.
	 */
	@Test
	public final void testLogSevereException() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#logException(java.util.logging.Logger, java.lang.Exception, java.lang.String, java.util.logging.Level, java.util.logging.Level)}.
	 */
	@Test
	public final void testLogException() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#printCGR(java.lang.String)}.
	 */
	@Test
	public final void testPrintCGRString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#printCGR(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testPrintCGRStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Utils#printCGR(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testPrintCGRStringStringString() {
		fail("Not yet implemented"); // TODO
	}

}
