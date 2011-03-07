/**
 * 
 */
package test.util;

import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * to keep empty madkit instances alive
 *
 */
public class OrgTestAgent extends Agent {
/**
	 * 
	 */
	private static final long serialVersionUID = 5802040757345190046L;

/* (non-Javadoc)
 * @see madkit.kernel.Agent#live()
 */
@Override
protected void activate() {
	createGroupIfAbsent("test", "test", true, null);
	requestRole("test", "test", "test");
}

protected void live() {
	// TODO Auto-generated method stub
	waitNextMessage();
}
}
