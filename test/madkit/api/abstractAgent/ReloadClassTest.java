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
package madkit.api.abstractAgent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.GroupIdentifier;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.8
 * @version 0.9
 * 
 */
public class ReloadClassTest  extends JunitMadKit{
	
	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(CLASS_NOT_FOUND, reloadAgentClass(null));
				assertEquals(CLASS_NOT_FOUND, reloadAgentClass(aa()));
				assertEquals(CLASS_NOT_FOUND, reloadAgentClass(aa()+"."+aa()));
			}
		});
	}
	
	@Test
	public void success(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, reloadAgentClass(getClass().getName()));
			}
		});
	}
}
