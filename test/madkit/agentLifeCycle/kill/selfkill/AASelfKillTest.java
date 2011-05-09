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
package madkit.agentLifeCycle.kill.selfkill;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.4
 * @version 0.9
 * 
 */
public class  AASelfKillTest extends JUnitBooterAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4538557814046610477L;

	@Override
	public void activate() {
		SelfKillAA a = new SelfKillAA();
		launchAgent(a);
		assertEquals(SUCCESS,a.selfDestruct());
		assertEquals(ALREADY_KILLED,killAgent(a));
		assertEquals(ALREADY_KILLED,a.selfDestruct());
		if(logger != null)
			logger.info("ending test");
	}
	
	@Override
	protected void end() {
		if(logger != null)
			logger.info("ending");
		super.end();
	}

}

class SelfKillAA extends AbstractAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6316054974112022575L;

	public ReturnCode selfDestruct(){
		ReturnCode r = killAgent(this);
		requestRole("test", "test", "test");
		return r;
	}

}

