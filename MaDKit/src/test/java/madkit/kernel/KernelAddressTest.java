package madkit.kernel;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class KernelAddressTest {

	@Test
	public void givenNewKernelAddress_whenCreated_thenNetworkIDIsNotNull() {
		KernelAddress ka = new KernelAddress();
		assertNotNull(ka.getNetworkID());
	}

	@Test
	public void givenTwoKernelAddresses_whenDifferentNetworkIDOrLocalID_thenEqualsReturnsFalse() {
		KernelAddress ka1 = new KernelAddress();
		KernelAddress ka2 = new KernelAddress();
		assertNotEquals(ka1, ka2);
	}

}
