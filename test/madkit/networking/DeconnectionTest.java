/**
 * 
 */
package madkit.networking;

/**
 * @author fab
 *
 */
public class DeconnectionTest extends AbstractNetworkingTest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1754665831777185318L;

	/* (non-Javadoc)
	 * @see test.madkit.networking.AbstractNetworkingTest#activate()
	 */
	@Override
	public void activate() {
		pause(1000);
		launchMKNetworkInstance("test.madkit.networking.DeconnectionMKClient");
		pause(5000);
		
	}
}
