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
package madkit.boot.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NetworkOnTest extends JunitMadkit {

	@Test
	public void networkOn() {
		addMadkitArgs(BooleanOption.network.toString()
				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertTrue(isKernelOnline());
			}
		});
	}

	@Test
	public void networkOff() {
		addMadkitArgs(
				LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertFalse(isKernelOnline());
			}
		});
	}

}
