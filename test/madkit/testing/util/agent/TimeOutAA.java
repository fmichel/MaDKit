/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.util.logging.Level;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.12
 * @version 1
 * 
 */
public class TimeOutAA extends DoItDuringLifeCycleAbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param inActivate
	 * @param inEnd
	 */
	public TimeOutAA(boolean inActivate, boolean inEnd) {
		super(inActivate, inEnd);
		setLogLevel(Level.ALL);
	}

	public TimeOutAA(boolean inActivate) {
		this(inActivate, false);
	}

	@Override
	public void doIt() {
		if (logger != null)
			logger.info("waiting 1.5s");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}