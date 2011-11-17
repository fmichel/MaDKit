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
package madkit.performance;

import java.util.Arrays;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.LevelOption;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.4
 * @version 0.9
 * 
 */
public class MemoryBench extends AbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8973359349586637788L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		System.err.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		createGroup("comm", "group");
		launchAgentBucketWithRoles("madkit.kernel.AbstractAgent", 4000000, Arrays.asList("comm", "group", "role"));
		System.err.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] argss = { "--agentLogLevel", "OFF", "--" + LevelOption.madkitLogLevel, "OFF", "--orgLogLevel", "OFF",
				"--launchAgents", MemoryBench.class.getName(), ",false" };
		Madkit.main(argss);
	}

}