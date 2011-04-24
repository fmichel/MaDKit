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
package madkit.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import madkit.gui.GUIMessage;
import madkit.gui.MadkitActions;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
final class AgentExecutor extends ThreadPoolExecutor {

	//	private boolean started = false;
	final private Agent myAgent;

	public AgentExecutor(Agent a, ThreadFactory threadFactory) {
		super(1, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(4, false), threadFactory);
		myAgent = a;
	}

	Future<Boolean> start(final boolean gui){
		final List<Future<Boolean>> lifeCycle = new ArrayList<Future<Boolean>>(4);
		final Future<Boolean> activation = submit(new Callable<Boolean>() {
			public Boolean call() {
				myAgent.setMyThread(Thread.currentThread());
				if (! myAgent.activation(gui)) {
					myAgent.getMyLifeCycle().get(1).cancel(true);// TODO This can be null
					return false;
				}
				return true;
			}
		});
		lifeCycle.add(activation);
		lifeCycle.add(submit(new Callable<Boolean>() {
			public Boolean call() {
				return myAgent.living();
			}
		}));
		lifeCycle.add(submit(new Callable<Boolean>() {
			public Boolean call() {
				return myAgent.ending();
			}
		}));
		lifeCycle.add(submit(new Callable<Boolean>() {
			public Boolean call() {
				shutdown();
				return true;
			}
		}));
		myAgent.setMyLifeCycle(lifeCycle);
		return activation;
	}

//	@Override
//	protected void beforeExecute(Thread t, Runnable r) {
//		String lifeLog = "** entering ";
//		lifeLog += printAgentMethod()+" **";
//	}
//
//	private String printLog() {
//		switch (myAgent.state.get()) {
//		case INITIALIZING:
//			return "ACTIVATE";
//		case ACTIVATED:
//			return "LIVE";
//		case ACTIVATED:
//			return "LIVE";
//		default:
//			break;
//		}
//	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {

	}

	@Override
	protected void terminated() {
		myAgent.getAlive().set(false);
		MadkitKernel kernel = myAgent.getKernel().getMadkitKernel();
		//		setKernel(kernel);
		kernel.broadcastMessageWithRole(
				myAgent,
				Madkit.Roles.LOCAL_COMMUNITY, 
				Madkit.Roles.SYSTEM_GROUP, 
				Madkit.Roles.GUI_MANAGER_ROLE, 
				new GUIMessage(MadkitActions.AGENT_DISPOSE_GUI,myAgent), 
				null); 
		if (myAgent.getState().equals(AbstractAgent.State.TERMINATED))// TODO remove that
			throw new AssertionError("terminating twice " + myAgent);
		myAgent.state.set(AbstractAgent.State.TERMINATED);
		try {
			kernel.removeAgentFromOrganizations(myAgent);// TODO catch because of probe/activator
		} catch (Throwable e) {
			e.printStackTrace();
			kernel.kernelLog("Problem for "+this+" in TERMINATE ", Level.FINER, e);
			kernel.logSevereException(e);
		}
		//		messageBox.clear(); // TODO test speed and no need for that
		if (myAgent.logger != null) {
			myAgent.logger.finest("** TERMINATED **");
		}
		myAgent.kernel = AbstractAgent.FAKE_KERNEL;
	}
}
