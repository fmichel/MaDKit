package com.distrimind.madkit.kernel.network;

import java.util.Collection;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testng.Assert;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Madkit;

@RunWith(Parameterized.class)
public class AutomaticLocalConnectionTest extends JunitMadkit {
	final NetworkEventListener eventListener1;
	final NetworkEventListener eventListener2;

	@Parameters
	public static Collection<Object[]> data() {
		return NetworkEventListener.getNetworkEventListenersForPeerToPeerConnectionsWithRandomProperties(true, false,
				true, true, true, null, 2, 1, 2);
	}

	public AutomaticLocalConnectionTest(NetworkEventListener eventListener1, NetworkEventListener eventListener2) {
		this.eventListener1 = eventListener1;
		this.eventListener2 = eventListener2;
		this.eventListener1.durationBeforeCancelingTransferConnection = 90000;
		this.eventListener2.durationBeforeCancelingTransferConnection = 120000;
	}

	private static final long timeOut = 120000;

	@Test
	public void testAutomaticLocalConnection() {
		cleanHelperMDKs();
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() throws InterruptedException {
				launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, null, eventListener2);
				pause(2000);
				launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, null, eventListener1);
				pause(1400);
				for (Madkit m : getHelperInstances(2)) {
					checkConnectedKernelsNb(this, m, 1, timeOut);
				}
				for (Madkit m : getHelperInstances(2)) {
					checkConnectedIntancesNb(this, m, 3, timeOut);
				}
				sleep(400);
				cleanHelperMDKs(this);
				Assert.assertEquals(getHelperInstances(0).size(), 0);

			}
		});
	}

	/*
	 * @Test public void testSameKernelAddress() { cleanHelperMDKs(); launchTest(new
	 * AbstractAgent() {
	 * 
	 * @Override protected void activate() throws InterruptedException {
	 * launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, null,
	 * eventListener2); pause(2000);
	 * 
	 * KernelAddress
	 * ka1=AutomaticLocalConnectionTest.this.getKernelAddress(getHelperInstances(1).
	 * get(0)); try(ByteArrayOutputStream baos=new ByteArrayOutputStream()) {
	 * try(ObjectOutputStream oos=new ObjectOutputStream(baos)) {
	 * oos.writeObject(ka1); } try(ByteArrayInputStream bais=new
	 * ByteArrayInputStream(baos.toByteArray())) { try(ObjectInputStream ois=new
	 * ObjectInputStream(bais)) { ka1=(KernelAddress)ois.readObject(); } } }
	 * catch(Exception e) { e.printStackTrace(); }
	 * 
	 * launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, null,
	 * eventListener1, ka1); pause(300); for (Madkit m : getHelperInstances(2)) {
	 * checkConnectedKernelsNb(this, m, 0, timeOut); } for (Madkit m :
	 * getHelperInstances(2)) { checkConnectedIntancesNb(this, m, 0, timeOut); }
	 * sleep(400); cleanHelperMDKs(this);
	 * Assert.assertEquals(getHelperInstances(0).size(), 0);
	 * 
	 * } }); }
	 */

}
