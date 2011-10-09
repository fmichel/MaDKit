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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
final class AgentExecutor extends ThreadPoolExecutor {

	//	private boolean started = false;
	final private Agent myAgent;
	private Future<Boolean> activate;
	private Future<?> live;
	private Future<?> end;

//	public AgentExecutor(Agent a, ThreadFactory threadFactory) {
//		super(1, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(4, false), threadFactory);
//		myAgent = a;
////		myAgent.setAgentExecutor(this);
//		setThreadFactory(threadFactory);
//	}
	
	public AgentExecutor(Agent a) {
		super(1, 1, 0, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(4, false));
		myAgent = a;
		activate = new FutureTask<Boolean>(new Callable<Boolean>() {
			public Boolean call() {
				myAgent.myThread = Thread.currentThread();
				if (! myAgent.activation()) {
//					myAgent.getAlive().set(false);
					live.cancel(false);
					return false;
				}
				return true;
			}
		});
		live = new FutureTask<Object>(new Runnable() {
			public void run() {
				if(myAgent.getAlive().get()){//TODO
					myAgent.living();
				}
			}},null);
		end = new FutureTask<Object>(new Runnable() {
			public void run() {
					myAgent.ending();
			}},null);
	}
	
	Future<Boolean> start(){//TODO transform to futuretask and execute
		execute((Runnable) activate);
		execute((Runnable) live);
		execute((Runnable) end);
		execute(new Runnable() {
			@Override
			public void run() {
//				System.err.println(activate.isDone());
				shutdown();
			}
		});
//		activate = submit(new Callable<Boolean>() {
//			public Boolean call() {
//				if (! myAgent.activation()) {
//					myAgent.getAlive().set(false);// TODO This can be null : out of bound
//					live.cancel(false);
//					return false;
//				}
//				return true;
//			}
//		});
//		live = submit(new Runnable() {
//			@Override
//			public void run() {
//				if(myAgent.getAlive().get()){//TODO
//					myAgent.living();
//				}
//				
//			}});
//		end = submit(new Runnable() {
//			public void run() {
//				myAgent.ending();
//			}
//		});
//		submit(new Runnable() {
//			@Override
//			public void run() {
//				shutdown();
//			}
//		});
		return activate;
	}

//	@Override
//	protected void afterExecute(Runnable r, Throwable t) {
////		if(t != null){
////			myAgent.getAlive().set(false);
////			if(t instanceof KilledException){
////				if(myAgent.logger != null){
////					myAgent.logger.finer( "-*-GET KILLED in "+methodName()+"-*- : "+t.getMessage());
////				}
////			}
////			else{
////				myAgent.kernel.logSevereException(t);
////				myAgent.kernel.getMadkitKernel().kernelLog("Problem for "+this+" in "+methodName(), Level.FINER, t);
////			}
////		}
//		if(! isTerminating() && myAgent.logger != null){
//			myAgent.logger.finer("** exiting "+methodName()+" **");
//		}
//	}
//
//	String methodName(){
//		switch (myAgent.getState()) {
//		case ACTIVATED:
//			return "ACTIVATE";
//		case LIVING:
//			return "LIVE";
//		default:
//			return "END";
//		}
//	}

	@Override
	protected void terminated() {
		//this is always done, even if the AE has not been started !
		if (myAgent.getKernel() != AbstractAgent.FAKE_KERNEL) {
			MadkitKernel k = myAgent.getKernel().getMadkitKernel();
			myAgent.terminate();
			k.removeThreadedAgent(myAgent);
		}
	}
	
	Future<?> getEndProcess() {
		return end;
	}
	Future<?> getLiveProcess() {
		return live;
	}

	/**
	 * @return the activate
	 */
	Future<Boolean> getActivate() {
		return activate;
	}
}

