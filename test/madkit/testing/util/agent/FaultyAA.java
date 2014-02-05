/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;

import java.util.logging.Level;

import madkit.kernel.Madkit.LevelOption;
/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class FaultyAA extends DoItDuringLifeCycleAbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param inActivate
	 * @param inEnd
	 */
	public FaultyAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
	}

	public FaultyAA(boolean inActivate) {
		super(inActivate);
	}

	public FaultyAA() {
		super();
	}
	
	@Override
	public void activate() {
		createGroup(COMMUNITY, GROUP,true);
		requestRole(COMMUNITY, GROUP, ROLE);
		super.activate();
	}

	@SuppressWarnings("null")
	@Override
	public void doIt() {
		if (logger != null)
			logger.info("crashing myself");
		Object o = null;
		o.toString();
	}

	public static void main(String[] args) {
		String[] myArgs = {LevelOption.agentLogLevel.toString(),Level.ALL.toString()};
		executeThisAgent(myArgs);
	}


}