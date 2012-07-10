/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.aa;
import static org.junit.Assert.assertEquals;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class SelfAbstractKill extends DoItDuringLifeCycleAbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int timeOut = 0;

	public SelfAbstractKill(boolean inActivate, boolean inEnd, int noTimeOut) {
		super(inActivate, inEnd);
		timeOut = noTimeOut;
	}

	@Override
	public String getName() {
		return super.getName() + (timeOut == Integer.MAX_VALUE ? "-NoTimeOut-" : "-WithTimeOut " + timeOut);
	}

	@Override
	public void doIt() {
		if (logger != null)
			logger.info("killing myself");
		if (inActivate) {
			if (timeOut == 0) {
				assertEquals(timeOut == 0 ? SUCCESS : TIMEOUT, killAgent(this, timeOut));
			} else
				assertEquals(getState() == State.ACTIVATED ? SUCCESS : ALREADY_KILLED, killAgent(this, timeOut));
		} else
			assertEquals(timeOut == 0 ? SUCCESS : TIMEOUT, killAgent(this, timeOut));
		for (int i = 0; i < 5; i++) {
			requestRole(COMMUNITY, GROUP, aa(), null);
			System.err.println("doing in " + getState() + " " + i);
		}
	}
}
