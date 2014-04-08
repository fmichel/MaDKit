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
public class NormalLife extends DoItDuringLifeCycleAgent {

	public NormalLife(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
	}

	public NormalLife(boolean inActivate, boolean inLive) {
		super(inActivate, inLive);
	}

	public NormalLife(boolean inActivate) {
		super(inActivate);
	}

	public NormalLife() {
		super();
//		KernelAction.LAUNCH_AGENT.getAction(this,"t");
	}

	@Override
	public void doIt() {
		for (int i = 0; i < 100; i++) {
			System.err.println(getName() + " " + getState());
			pause((int) (Math.random() * 1000));
		}
	}

public static void main(String[] args) {
	AbstractAgent.executeThisAgent(args);
}

}
