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
package madkit.testing.util.agent;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class SelfLaunch extends DoItDuringLifeCycleAgent{

	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public SelfLaunch(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	public SelfLaunch(boolean inActivate, boolean inLive) {
		super(inActivate, inLive, false);
	}

	public SelfLaunch(boolean inActivate) {
		super(inActivate, false, false);
	}


	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.DoItDuringLifeCycleAgent#doIt()
	 */
	@Override
	public void doIt() {
		assertEquals(ReturnCode.ALREADY_LAUNCHED,launchAgent(this));
	}

	
}


