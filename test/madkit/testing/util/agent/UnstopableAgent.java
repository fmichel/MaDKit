/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;


/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
public class UnstopableAgent extends DoItDuringLifeCycleAgent {

	public UnstopableAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
	}

	public UnstopableAgent(boolean inActivate, boolean inLive) {
		super(inActivate, inLive);
	}

	public UnstopableAgent(boolean inActivate) {
		super(inActivate);
	}

	public UnstopableAgent() {
		super(true, true, true);
	}

	public void doIt() {
		super.doIt();
		int i = 0;
		while (true) {
			i++;
			if (i % 10000000 == 0) {
				if (logger != null)
					logger.info(getState() + " " + i);
			}
		}
	}

	public static void main(String[] args) {
		executeThisAgent(args);
//		String[] argss = { LevelOption.agentLogLevel.toString(), "ALL", LevelOption.kernelLogLevel.toString(), "ALL",
//				"--launchAgents", UnstopableAgent.class.getName(), ",true" };
//		Madkit.main(argss);
	}

}