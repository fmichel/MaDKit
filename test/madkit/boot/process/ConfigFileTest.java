/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.boot.process;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class ConfigFileTest extends JunitMadkit {

	@Test
	public void configFile() {
		addMadkitArgs(Madkit.Option.configFile.toString(),"madkit/boot/process/test.prop",Madkit.LevelOption.madkitLogLevel.toString(),Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("ok",getMadkitProperty("test"));
			}
		});
	}
	
		@Test
		public void multiConfigFile() {
			addMadkitArgs(Madkit.Option.configFile.toString(),"madkit/boot/process/test.prop;madkit/boot/process/test2.prop",Madkit.LevelOption.madkitLogLevel.toString(),Level.ALL.toString());
			launchTest(new AbstractAgent() {
				@Override
				protected void activate() {
					assertEquals("false",getMadkitProperty("test"));
					assertEquals("ok",getMadkitProperty("test2"));
				}
			});
		}

		@Test
		public void multiConfigOptions() {
			addMadkitArgs(
					Madkit.Option.configFile.toString(),"madkit/boot/process/test.prop",
					Madkit.Option.configFile.toString(),"madkit/boot/process/test2.prop",
					Madkit.BooleanOption.network.toString(),"true",
					Madkit.LevelOption.madkitLogLevel.toString(),Level.ALL.toString());
			launchTest(new AbstractAgent() {
				@Override
				protected void activate() {
					assertEquals("false",getMadkitProperty("test"));
					assertEquals("ok",getMadkitProperty("test2"));
				}
			});
		}
}
