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
package madkit.boot.process;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.Option;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LaunchAgentArg extends AbstractAgent{

	public static void main(String[] args) {
		String[] argss = {Option.launchAgents.toString(),Agent.class.getName()};
		Madkit.main(argss);
		String[] argsss = {Option.launchAgents.toString(),Agent.class.getName()+",true,2"};
		Madkit.main(argsss);
	}
}
