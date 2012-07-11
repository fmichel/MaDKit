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
package madkit.simulation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;

import org.junit.Before;
import org.junit.Test;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class ProbeTest {

	Probe<TestAgent> a;
	TestAgent agt;

	@Before
	public void setUp() throws Exception {
		a = new Probe<TestAgent>("t", "t", "t");
		agt = new TestAgent() {
			boolean bool2 = false;
		};
	}

	@Test
	public void testActivator() {
		a = new Probe<TestAgent>(null, null, null);
	}

	@Test
	public void testFindFieldOnInheritedPublic() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "bool");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPublic() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "bool2");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnNotExist() {
		try {
			Field m = a.findFieldOn(agt.getClass(), "notExist");
			fail("ex not thrown");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindFieldOnProtected() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "value");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

	@Test
	public void testFindFieldOnPrivate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field m = a.findFieldOn(agt.getClass(), "something");
		assertNotNull(m);
		System.err.println(m.get(agt));
	}

}

@SuppressWarnings("all")
class TestAgent extends AbstractAgent {
	public boolean bool = false;
	protected int value = 2;
	private double something = 3.0;
}
