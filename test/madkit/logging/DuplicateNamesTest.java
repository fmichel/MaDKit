package madkit.logging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Level;

import madkit.boot.process.CreateLogFilesTest;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

import org.junit.Before;
import org.junit.Test;

public class DuplicateNamesTest extends JunitMadkit {

	private File	f;

	@Before
	public void init(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
	}

	@Test
	public void setNameTest() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 682199976324991855L;

			@Override
			protected void activate() {
				setName(COMMUNITY);
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS , launchAgent(new SetNameAgent(),true));
			}
		});
	}

	@Test
	public void setNameTestOnLogFile() {//FIXME local community does not exist
		addMadkitArgs(BooleanOption.createLogFiles.toString());
		addMadkitArgs(LevelOption.guiLogLevel.toString(),Level.ALL.toString());
		launchTest(new AbstractAgent() {

			/**
			 * 
			 */
			private static final long	serialVersionUID	= -4876624372420070372L;

			@Override
			protected void activate() {
				setName(COMMUNITY);
				setLogLevel(Level.ALL);
				assertEquals(SUCCESS , launchAgent(new SetNameAgent(),true));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		System.err.println(f);
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		assertSame(2, f.listFiles(CreateLogFilesTest.filter).length);
	}
}

class SetNameAgent extends AbstractAgent{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -522022197619143781L;
	public SetNameAgent() {
		setName(JunitMadkit.COMMUNITY);// TODO Auto-generated constructor stub
	}
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		if(logger != null)
			logger.info("test");
		setLogLevel(Level.OFF);
		if(logger != null)
			logger.info("test no display");
		setLogLevel(Level.ALL);
		if(logger != null)
			logger.info("test 2");
	}
}