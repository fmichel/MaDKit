/*
 * Copyright 2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
import madkit.kernel.Madkit.BooleanOption;

public class WorkingAgent extends DoItDuringLifeCycleAgent {

	public WorkingAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
	}

	public WorkingAgent() {
		super(true, true, true);
	}

	public void doIt() {
		for (int i = 0; i < 100000000; i++) {
			@SuppressWarnings("unused")
			double d = Math.random() * 2;
			d *= Math.PI * 100;
			// if(i % 10000000 == 0)
			// if(logger != null)
			// logger.info("yo");
		}
		super.doIt();
	}

	public static void main(String[] args) {
		String[] myArgs = {BooleanOption.desktop.toString()};
		AbstractAgent.executeThisAgent(myArgs);
	}
}