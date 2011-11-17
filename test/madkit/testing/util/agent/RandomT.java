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

import java.util.ArrayList;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.KernelException;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class RandomT extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6420300053860559183L;
	public static ArrayList<AbstractAgent> agents;
	public static boolean killingOn = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	public void live() {
		setLogLevel(Level.OFF);
		getLogger().setWarningLogLevel(Level.FINE);
		for (int i = 0; i < 100; i++) {
			if (logger != null) {
				logger.fine("living");
			}
			pause((int) (Math.random() * 100));
			ReturnCode res = launchAgent(agents.get((int) (Math.random() * agents.size())), Math.random() > .5 ? 0 : 1);
			// launchAgent(agents.get((int)
			// (Math.random()*agents.size())),Math.random()>.5?0:1);
			if (logger != null)
				logger.fine("launching result is : " + res);
			killSomebody();
		}
	}

	private void killSomebody() {
		ReturnCode res;
		if (killingOn) {
			AbstractAgent a = agents.get((int) (Math.random() * agents.size()));
			res = killAgent(a, Math.random() > .5 ? 0 : 1);
			if (logger != null)
				logger.fine("kill on " + a.getName() + " result is : " + res);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.madkit.agentLifeCycle.LifeCycleTestAgent#end()
	 */
	@Override
	public void end() {
		super.end();
		for (int i = 0; i < 10; i++) {
			if (logger != null) {
				logger.fine("dying");
			}
			try {
				pause((int) (Math.random() * 100));
				ReturnCode res = launchAgent(agents.get((int) (Math.random() * agents.size())), Math.random() > .5 ? 0 : 1);
				// launchAgent(agents.get((int)
				// (Math.random()*agents.size())),Math.random()>.5?0:1);
				if (logger != null)
					logger.fine("launching result is : " + res);
				killSomebody();
			} catch (KernelException e) {
				System.err.println("kernel ex : " + getState() + " alive " + isAlive());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
