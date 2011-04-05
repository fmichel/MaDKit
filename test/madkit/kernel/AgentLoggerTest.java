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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class AgentLoggerTest {
	
	@Test
	public void noLogger(){
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
		assertEquals(AbstractAgent.ReturnCode.NOT_YET_LAUNCHED, a.createGroup("test", "test"));
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
		assertEquals(AbstractAgent.ReturnCode.NOT_YET_LAUNCHED, a.createGroup("test", "test"));
	}
	
	@Test
	public void setNameAndThenLog() {
		AgentLog a = new AgentLog(Level.OFF);
		String defaultName = a.getName();
		a.setName("TEST");
		assertNull(a.logger);
		a.setLogLevel(Level.ALL);
		assertNotNull(a.logger);
		assertEquals("["+a.getName()+"]",a.logger.getName());
	}

	@Test
	public void logAndThenSetName() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.logger);
		String defaultName = "["+a.getClass().getSimpleName()+"-"+a.hashCode()+"]";
		assertEquals(defaultName,a.logger.getName());
		a.setName("TEST");
		assertNotNull(a.logger);
		assertEquals("[TEST]",a.logger.getName());
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
		assertEquals(l,a.logger);
	}

}

class AgentLog extends AbstractAgent{
	AgentLog(Level lvl){
		setLogLevel(lvl);
	}
}