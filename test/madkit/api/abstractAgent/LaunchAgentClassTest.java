/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import java.util.ArrayList;

import madkit.kernel.*;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.Madkit.Roles.*;

import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
public class LaunchAgentClassTest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			assertEquals(ALREADY_LAUNCHED,launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent(){
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	final AbstractAgent faulty = new AbstractAgent(){
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void launchFailed(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent((String) null);
			}
		},AGENT_CRASH);
	}

	@Test
	public void launchNotFound(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent("a");
			}
		},AGENT_CRASH);
	}

}
