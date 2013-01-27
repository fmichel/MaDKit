/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.kernel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class OptionTesting extends JunitMadkit {

	@Test
	public void correctness() {
		mkArgs = new ArrayList<>(Arrays.asList(BooleanOption.autoConnectMadkitWebsite.toString(),
		// "--"+Madkit.logDirectory,getBinTestDir(),
		// "--"+Madkit.agentLogLevel,"ALL",
				LevelOption.kernelLogLevel.toString(), "INFO"));
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals("INFO", getMadkitProperty(LevelOption.kernelLogLevel.name()));
				assertEquals("true", getMadkitProperty(BooleanOption.autoConnectMadkitWebsite.name()));
			}
		});
	}

}
