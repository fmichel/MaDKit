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

import java.util.logging.Level;

import madkit.kernel.JunitMadKit;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.12
 * @version 1
 * 
 */
public class TimeOutAA extends DoItDuringLifeCycleAbstractAgent{

	/**
	 * @param inActivate
	 * @param inEnd
	 */
	public TimeOutAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
		setLogLevel(Level.ALL);
	}

	public TimeOutAA(boolean inActivate) {
		this(inActivate,false);
	}

	@SuppressWarnings("null")
	@Override
	public void doIt() {
		if(logger != null)
			logger.info("waiting 1.5s");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}