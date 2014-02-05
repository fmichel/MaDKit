/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.networking;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.testing.util.agent.ForEverAgent;
import madkit.testing.util.agent.LeaveGroupInEndNormalAgent;

import org.junit.After;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class DistantManagerTest extends JunitMadkit {
	
	protected static final int	OTHERS	= 10;

	@After
	public void clean(){
		cleanHelperMDKs();
	}

	@Test
	public void multipleExternalConnectionTest() {
		cleanHelperMDKs();
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.networkLogLevel.toString(),"FINE"
				,LevelOption.madkitLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				Madkit m = new Madkit(
						BooleanOption.network.toString(),
						LevelOption.agentLogLevel.toString(),"ALL",
						Option.launchAgents.toString(), LeaveGroupInEndNormalAgent.class.getName()+";"+ForEverAgent.class.getName());
//						BooleanOption.createLogFiles.toString()};
				helperInstances.add(m);
				pause(2000);
				System.err.println(getOrganizationSnapShot(false));
				List<AgentAddress> l = getAgentsWithRole(COMMUNITY,GROUP,ROLE);
				System.err.println("others ="+l.size());
				assertEquals(1, l.size());
			}
		});
	}

}
