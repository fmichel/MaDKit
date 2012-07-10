/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class AgentLoggerTest extends JunitMadkit{

	@Test
	public void noLogger() {
		AbstractAgent a = new AbstractAgent();
		a.logger.info("testing");
		assertSame(AgentLogger.defaultAgentLogger, a.logger);
	}

	@Test
	public void logLevelOFF() {
		AgentLog a = new AgentLog(Level.OFF);
		assertNull(a.logger);
		a.setLogLevel(Level.ALL);
		assertNotNull(a.logger);
		a.logger.info("testing");
	}

	@Test
	public void logLevelALL() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		try {
			a.createGroup("test", "test");
			fail();
		} catch (KernelException e) {
			e.printStackTrace();
		}
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
		try {
			a.createGroup("test", "test");
			fail();
		} catch (KernelException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void setNameAndThenLog() {
		AgentLog a = new AgentLog(Level.OFF);
		a.setName("TEST");
		assertNull(a.logger);
		a.setLogLevel(Level.ALL);
		assertNotNull(a.logger);
		assertEquals("[" + a.getName() + "]", a.logger.getName());
	}

	@Test
	public void logAndThenSetName() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		String defaultName = "[" + a.getClass().getSimpleName() + "-" + a.hashCode() + "]";
		assertEquals(defaultName, a.logger.getName());
		a.setName("TEST");
		assertNotNull(a.logger);
		assertEquals("[TEST]", a.logger.getName());
	}

	@Test
	public void logOnAndOffAndOnEquality() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		Logger l = a.logger;
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
		a.setLogLevel(Level.ALL);
		assertNotNull(a.logger);
		assertEquals(l, a.logger);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void severeLogTest(){
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				getLogger().severeLog("test", null);
				getLogger().severeLog("test", new Exception());
				pause(1000);
			}
		},ReturnCode.SUCCESS, true);
	}

	@SuppressWarnings("serial")
	@Test
	public void onlyOneFileTest(){
		addMadkitArgs(BooleanOption.createLogFiles.toString());
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Madkit.Option.logDirectory.name()));
				getLogger().createLogFile();
				if(logger != null)
					logger.fine(getName());
				File f = new File(getMadkitProperty(Madkit.Option.logDirectory.name()));
				assertSame(1, f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File arg0, String s) {
						return s.contains(getName()) && !s.contains(".lck");
					}
				}).length);				
			}
		},ReturnCode.SUCCESS, true);
		
	}

}

class AgentLog extends AbstractAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AgentLog(Level lvl) {
		setLogLevel(lvl);
	}
}