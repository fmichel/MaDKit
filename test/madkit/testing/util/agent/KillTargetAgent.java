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

import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class KillTargetAgent extends DoItDuringLifeCycleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5186267329069400681L;
	Agent toKill;
	/**
	 * @param a
	 */
	public KillTargetAgent(Agent a) {
		this(a,false,false,false);
	}

	public KillTargetAgent(Agent a,boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		toKill = a;
	}

	public KillTargetAgent(Agent a,boolean inActivate, boolean inLive) {
		this(a,inActivate, inLive,false);
	}

	public KillTargetAgent(Agent a,boolean inActivate) {
		this(a,inActivate,false,false);
	}

	@Override
	public void live() {
		killAgent(toKill, 2);
	}
}
