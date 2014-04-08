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

import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
public abstract class DoItDuringLifeCycleAgent extends Agent {

	private boolean inActivate = false, inLive = false, inEnd = false;

	public DoItDuringLifeCycleAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		this.inActivate = inActivate;
		this.inLive = inLive;
		this.inEnd = inEnd;
		setName(getName());
	}
	
	
	@SuppressWarnings("unused")
	private void privateMethod() {
	}

	public DoItDuringLifeCycleAgent(boolean inActivate, boolean inLive) {
		this(inActivate, inLive, false);
	}

	public DoItDuringLifeCycleAgent(boolean inActivate) {
		this(inActivate, false, false);
	}

	public DoItDuringLifeCycleAgent() {
		this(false, true, false);
	}

	@Override
	public String getName() {
		return super.getName() + (inActivate ? "-inActivate-" : "") + (inLive ? "-inLive-" : "") + (inEnd ? "-inEnd-" : "")
				+ hashCode();
	}

	public void activate() {
		if (inActivate) {
			if (logger != null)
				logger.info("Doing It in activate!!");
			doIt();
		}
	}

	public void live() {
		if (inLive) {
			if (logger != null)
				logger.info("Doing It in live!!");
			doIt();
		}
	}

	public void end() {
		super.end();
		if (inEnd) {
			if (logger != null)
				logger.info("Doing It in end!!");
			doIt();
		}
	}

	public void doIt() {
		if (logger != null)
			logger.info("\n\n\tDo it JOB DONE !!!!!! in " + getState() + "\n\n");
	}
}
