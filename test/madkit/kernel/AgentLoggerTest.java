/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import org.junit.Ignore;
import org.junit.Test;

import madkit.action.LoggingAction;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
public class AgentLoggerTest extends JunitMadkit{

	@Test
	public void lazyCreationTest() {
		AbstractAgent a = new AbstractAgent();
		assertNull(a.logger);
		a.getLogger().info("testing");
		assertNotNull(a.logger);
	}

	@Test
	public void logLevelOFF() {
		AgentLog a = new AgentLog(Level.OFF);
		assertNull(a.logger);
		a.getLogger().setLevel(Level.ALL);
		assertNotNull(a.logger);
		a.getLogger().info("testing");
	}

	@Test
	public void logLevelALL() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		try {
			a.createGroup("test", "test");
			fail();
		} catch (KernelException e) {
//			e.printStackTrace();
		}
		try {
			a.createGroup("test", "test");
			fail();
		} catch (KernelException e) {
//			e.printStackTrace();
		}
	}

	@Test
	public void setNameAndThenLog() {
		AgentLog a = new AgentLog(Level.OFF);
		a.setName("TEST");
		assertNull(a.logger);
		a.getLogger().setLevel(Level.ALL);
		assertNotNull(a.logger);
		System.err.println(a.getName());
		System.err.println(a.getName());
		assertEquals("[" + a.getName() +"]", a.getLogger().getName());
	}

	@Test
	public void logAndThenSetName() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		String defaultName = "[" + a.getClass().getSimpleName() + "-" + a.hashCode() + "]";
		assertEquals(defaultName, a.getLogger().getName());
		a.setName("TEST");
		assertNotNull(a.logger);
		assertEquals(defaultName, a.getLogger().getName());
	}

	@Ignore 
	@Test
	public void logOnAndOffAndOnEquality() {//could be reactivated if I create an emptyLogger for performance
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		Logger l = a.logger;
		a.getLogger().setLevel(Level.OFF);
		assertNull(a.logger);
		a.getLogger().setLevel(Level.ALL);
		assertNotNull(a.logger);
		assertEquals(l, a.logger);
	}
	
	
	@Test
	public void severeLogTest(){
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				getLogger().severeLog("test", null);
				getLogger().severeLog("test", new Exception());
				pause(1000);
			}
		},ReturnCode.SUCCESS);
	}

	
	@Test
	public void twoDefaultFilesforOneAgentTest(){
		addMadkitArgs(BooleanOption.createLogFiles.toString());
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Madkit.Option.logDirectory.name()));
				getLogger().createLogFile();
				if(logger != null)
					getLogger().fine(getName());
				File f = new File(getMadkitProperty(Madkit.Option.logDirectory.name()));
				assertSame(2, f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File arg0, String s) {
						return s.contains(getName()) && !s.contains(".lck");
					}
				}).length);				
			}
		},ReturnCode.SUCCESS);
	}
	
	@Test
	public void warningsLoggingDefaultValuesTest() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"INFO");
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				assertFalse(getLogger().isCGRWarningsOn());
				getLogger().enableCGRWarnings();
				assertTrue(getLogger().isCGRWarningsOn());
			}
		},ReturnCode.SUCCESS);
	}
	
	@Test
	public void warningsLoggingActionTest() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"INFO");
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				assertFalse(getLogger().isCGRWarningsOn());
				Action action = getLogger().getEnableCGRWarningsAction();
//				assertFalse((boolean) action.getValue(action.SELECTED_KEY));//TODO allow reseting preferences
				action.putValue(Action.SELECTED_KEY, true);
				assertTrue(getLogger().isCGRWarningsOn());
				action.putValue(Action.SELECTED_KEY, false);
				assertFalse(getLogger().isCGRWarningsOn());
				assertFalse((boolean) action.getValue(action.SELECTED_KEY));
				action.actionPerformed(null);
				assertTrue((boolean) action.getValue(action.SELECTED_KEY));
				assertTrue(getLogger().isCGRWarningsOn());
				action.actionPerformed(null);
				assertFalse((boolean) action.getValue(action.SELECTED_KEY));
				assertFalse(getLogger().isCGRWarningsOn());
			}
		},ReturnCode.SUCCESS);
	}
	
	@Test
	public void LogLevelActionTest() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"INFO");
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				assertFalse(getLogger().isCGRWarningsOn());
				assertEquals(Level.INFO,getLogger().getLevel());
				getLogger().finest("testing");
				Action action = LoggingAction.LOG_LEVEL.getActionFor(this, Level.ALL);
				action.actionPerformed(null);
				assertEquals(Level.ALL,getLogger().getLevel());
				getLogger().finest("testing");
			}
		},ReturnCode.SUCCESS);
	}
	

}

class AgentLog extends AbstractAgent {
	AgentLog(Level lvl) {
		if (lvl != Level.OFF) {
			getLogger().setLevel(lvl);
		}
	}
}