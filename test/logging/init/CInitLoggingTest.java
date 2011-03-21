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
package logging.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import madkit.kernel.AbstractAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class CInitLoggingTest {
	
	@Test
	public void logLevelOFF() {
		AgentLog a = new AgentLog(Level.OFF);
		assertNull(a.getLogger());
		a.setLogLevel(Level.ALL);
		assertNotNull(a.getLogger());
	}

	@Test
	public void logLevelALL() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.getLogger());
		assertEquals(AbstractAgent.ReturnCode.NOT_YET_LAUNCHED, a.createGroup("test", "test"));
		a.setLogLevel(Level.OFF);
		assertNull(a.getLogger());
		assertEquals(AbstractAgent.ReturnCode.NOT_YET_LAUNCHED, a.createGroup("test", "test"));
	}
	
	@Test
	public void setNameAndThenLog() {
		AgentLog a = new AgentLog(Level.OFF);
		String defaultName = a.getName();
		a.setName("TEST");
		assertNull(a.getLogger());
		a.setLogLevel(Level.ALL);
		assertNotNull(a.getLogger());
		assertEquals("["+defaultName+"-TEST]",a.getLogger().getName());
	}

	@Test
	public void logAndThenSetName() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.getLogger());
		String defaultName = a.getName();
		assertEquals("["+defaultName+"]",a.getLogger().getName());
		a.setName("TEST");
		assertNotNull(a.getLogger());
		assertEquals("["+defaultName+"-TEST]",a.getLogger().getName());
	}

	@Test
	public void logOnAndOffAndOnEquality() {
		AgentLog a = new AgentLog(Level.ALL);
		assertNotNull(a.getLogger());
		Logger l = a.getLogger();
		a.setLogLevel(Level.OFF);
		assertNull(a.getLogger());
		a.setLogLevel(Level.ALL);
		assertNotNull(a.getLogger());
		assertEquals(l,a.getLogger());
	}

}

class AgentLog extends AbstractAgent{
	AgentLog(Level lvl){
		setLogLevel(lvl);
	}
}