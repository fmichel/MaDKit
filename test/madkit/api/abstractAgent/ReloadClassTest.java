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
package madkit.api.abstractAgent;

import static org.junit.Assert.fail;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.MadkitClassLoader;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.8
 * @version 0.9
 * 
 */

public class ReloadClassTest extends JunitMadkit {

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					try {
						MadkitClassLoader.reloadClass(null);
						noExceptionFailure();
					} catch (ClassNotFoundException e) {
						fail("wrong exception");
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void classNotFound() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					MadkitClassLoader.reloadClass(aa());
					noExceptionFailure();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					MadkitClassLoader.reloadClass(aa() + "." + aa());
					noExceptionFailure();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void success() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					MadkitClassLoader.reloadClass(getClass().getName());
				} catch (ClassNotFoundException e) {
					fail("exception thrown");
					e.printStackTrace();
				}
			}
		});
	}
}
