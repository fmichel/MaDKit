/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.util.concurrent.ThreadFactory;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 *
 */
final class AgentThreadFactory extends Object implements ThreadFactory {

	final private static int MKRA_PRIORITY = Thread.NORM_PRIORITY-1;
	final private static int MKDA_PRIORITY = Thread.MAX_PRIORITY;
	final private boolean daemonThreads;
    final private ThreadGroup group;
//	static private int nbthread=0;
    
    AgentThreadFactory(final KernelAddress kernelAddress, final boolean daemonThreadFactory) {
    	daemonThreads = daemonThreadFactory;
    	group = new ThreadGroup(daemonThreads ? "DAEMON" : "LIFE"+kernelAddress){
			public void uncaughtException(Thread t, Throwable e) {
				if(e instanceof KilledException){
					e.printStackTrace();
					throw new AssertionError("killedException uncaught");
				}
				System.err.println("--------------internal BUG--------------------");
				System.err.println("\n-----------------uncaught exception on "+t);//TODO
				e.printStackTrace();
			}
		};

    	if (daemonThreadFactory) {
			group.setMaxPriority(MKDA_PRIORITY);
		}
    	else{
			group.setMaxPriority(MKRA_PRIORITY);    		
    	}
    }
	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(group, r);
//        System.err.println("\n\n\n new thread "+t);
        t.setDaemon(daemonThreads);
//        t.setPriority(t.getThreadGroup().getMaxPriority());
        return t;
    }
	
	ThreadGroup getThreadGroup(){
		return group;
	}
	
}
