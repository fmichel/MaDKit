/*******************************************************************************
 * Copyright (c) 2024, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.reflection;

import static org.testng.Assert.assertNotNull;

import java.lang.invoke.MethodHandle;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;
import madkit.kernel.Message;

/**
 *
 * 
 * @since 6.0
 */
public class MethodHandleFinderTest {

	MethodHandle mh;
	Agent agent = new GenericTestAgent();

	@Test
	void given_NoArgs_when_findMethodHandle_then_notNull() throws Throwable {
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "privateMethod");
		assertNotNull(mh);
		mh.invoke(agent);
	}

	@Test
	void given_NoArgs_when_findMethodHandleFromParam_then_notNullIsReturned() throws Throwable {
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethod");
		assertNotNull(mh);
		mh.invoke(agent);
	}

//	@Test
//	void given_NullArgs_when_findMethodHandleFromParam_then_notNullIsReturned() throws Throwable {
//		MethodHandle mh = MethodHandleFinder.findMethodHandleFromParam(GenericTestAgent.class, "privateMethod",(Object) null);
//		assertNotNull(mh);
//		GenericTestAgent name = new GenericTestAgent();
//		mh.invoke(name);
//	}

	@Test
	public void getClassMethodOnNoArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandle(Agent.class, "getLogger");// public
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(Agent.class, "activate");// protected
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(Agent.class, "getKernel");// private
		mh.invoke(agent);
	}

	@Test
	public void getClassInheritedMethodOnPublicNoArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "getLogger");// public
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "activate");// protected
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "getKernel");// private
		mh.invoke(agent);
	}

	@Test
	public void getMethodOnArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandleFromArgs(Agent.class, "receiveMessage", new Message());
		mh.invoke(agent, new Message());
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethod");// private
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethodWithArgs", "test",
				new Object());// private
		mh.invoke(agent, "test", new Object());
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test",
				Integer.valueOf(1));// private
		mh.invoke(agent, "test", 1);
		mh.invoke(agent, "test", Integer.valueOf(1));
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test",
				1);// private
		mh.invoke(agent, "test", 1);
		mh.invoke(agent, "test", Integer.valueOf(1));
	}

	@Test
	public void getMethodOnPrimitiveArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandleFromArgs(GenericTestAgent.class, "privateMethodWithPrimitiveArgs", "test",
				1);
		mh.invoke(agent, "test", 1);
		mh.invoke(agent, "test", Integer.valueOf(1));
	}

	@Test
	public void getMethodFromTypesNoArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "getLogger");// public
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "isThreaded");// protected
		mh.invoke(agent);
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "privateMethod");// private
		mh.invoke(agent);
	}

	@Test
	public void getMethodFromTypesArg() throws Throwable {
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "receiveMessage", Message.class);
		mh.invoke(agent, new Message());
		mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "privateMethodWithArgs", String.class,
				Object.class);
		mh.invoke(agent, "test", new Object());
		MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "privateMethodWithArgs", String.class, Integer.class);
		mh.invoke(agent, "test", 2);
		mh.invoke(agent, "test", (Integer) 2);
	}

	@Test
	public void getMethodFromPrimitiveTypesArg() throws Throwable {
		MethodHandle mh = MethodHandleFinder.findMethodHandle(GenericTestAgent.class, "privateMethodWithArgs",
				String.class, int.class);
		assertNotNull(mh);
	}

}
