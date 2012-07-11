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
package madkit.testing.util.agent;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class SelfLaunchAA extends DoItDuringLifeCycleAbstractAgent {

	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public SelfLaunchAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
		// TODO Auto-generated constructor stub
	}

	public SelfLaunchAA(boolean inActivate) {
		super(inActivate, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.madkit.agentLifeCycle.DoItDuringLifeCycleAgent#doIt()
	 */
	@Override
	public void doIt() {
		assertEquals(ReturnCode.ALREADY_LAUNCHED, launchAgent(this));
	}

}
