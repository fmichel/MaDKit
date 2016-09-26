/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import java.util.concurrent.ThreadFactory;

/**
 * @author Fabien Michel
 * @version 0.92
 * @since MaDKit 5.0
 * 
 */
final class AgentThreadFactory extends Object implements ThreadFactory {

	final private static int MKRA_PRIORITY = Thread.NORM_PRIORITY-1;
	final private static int MKDA_PRIORITY = Thread.MAX_PRIORITY;
	final private boolean		daemonThreads;
	final private ThreadGroup	group;

	AgentThreadFactory(final KernelAddress kernelAddress, final boolean daemonThreadFactory) {
		daemonThreads = daemonThreadFactory;
		group = new ThreadGroup(daemonThreads ? "DAEMON" : "LIFE" + kernelAddress) {

			public void uncaughtException(Thread t, Throwable e) {
				if (e instanceof ThreadDeath) {
					e.printStackTrace();
					throw new AssertionError("ThreadDeath uncaught");
				}
				System.err.println("--------------internal BUG--------------------");
				System.err.println("\n-----------------uncaught exception on " + t);// TODO
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

	@Override
	public Thread newThread(final Runnable r) {
		final Thread t = new Thread(group, r);
		// System.err.println("\n\n\n new thread "+t);
		t.setDaemon(daemonThreads);
//		t.setPriority(Thread.NORM_PRIORITY - 1);
		return t;
	}

	ThreadGroup getThreadGroup() {
		return group;
	}

}
