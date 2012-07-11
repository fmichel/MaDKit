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

import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public abstract class DoItDuringLifeCycleAbstractAgent extends AbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean inActivate = false, inEnd = false;

	public DoItDuringLifeCycleAbstractAgent(boolean inActivate, boolean inEnd) {
		this.inActivate = inActivate;
		this.inEnd = inEnd;
		setName(getClass().getSimpleName() + (inActivate ? "-inActivate-" : "") + (inEnd ? "-inEnd-" : "") + hashCode());
	}

	public DoItDuringLifeCycleAbstractAgent(boolean inActivate) {
		this(inActivate, false);
	}

	public DoItDuringLifeCycleAbstractAgent() {
		this(true, false);
	}

	public void activate() {
		if (inActivate) {
			doIt();
		}
	}

	public void end() {
		super.end();
		if (inEnd) {
			doIt();
		}
	}

	public void doIt() {
		if (logger != null)
			logger.info("I am in " + getState());
	}
}
