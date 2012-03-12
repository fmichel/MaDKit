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
package madkit.boot.process;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.action.GUIManagerAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class WrongArgTest extends JunitMadKit {

	@Test
	public void agentDoesNotExist() {
		addMadkitArgs(Option.launchAgents.toString(),"fake.fake",LevelOption.kernelLogLevel.toString(),Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void wrongLaunchParameters() {
		addMadkitArgs(Option.launchAgents.toString(),AbstractAgent.class.getName()+",fd,h",LevelOption.kernelLogLevel.toString(),Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void notAnAgentClass() {
		addMadkitArgs(Option.launchAgents.toString(),Object.class.getName(),LevelOption.kernelLogLevel.toString(),Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void agentCannotBeInitialized() {
		addMadkitArgs(Option.launchAgents.toString(),GUIManagerAction.class.getName(),LevelOption.kernelLogLevel.toString(),Level.OFF.toString());
		launchTest(new AbstractAgent());
	}

	@Test
	public void newBooleanValue() {
		addMadkitArgs("--test",LevelOption.madkitLogLevel.toString(),Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("true", getMadkitProperty("test"));
			}
		});
	}

	@Test
	public void defaultLogLevels() {
		mkArgs = null;
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("OFF", getMadkitProperty(LevelOption.kernelLogLevel.name()));
				assertEquals("OFF", getMadkitProperty(LevelOption.guiLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.networkLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.madkitLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.agentLogLevel.name()));
				assertEquals("FINE", getMadkitProperty(LevelOption.warningLogLevel.name()));
				assertEquals("INFO", getMadkitProperty(LevelOption.madkitLogLevel.name()));
			}
		});
	}
}
