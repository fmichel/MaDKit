/**
 * 
 */
package madkit.networking.org;

import madkit.kernel.Madkit;

import org.junit.Test;

public class NetworkingConnectionT {

	@Test
	public void connectionTest() {
		launchMKNetworkInstance();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// launchMKNetworkInstance();

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void launchMKNetworkInstance() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] args = { "--agentLogLevel", "ALL", "--MadkitLogLevel", "ALL", "--orgLogLevel", "ALL", "--network" };
				Madkit.main(args);
			}
		}).start();

	}
}
