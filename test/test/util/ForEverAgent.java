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
public class ForEverAgent extends Agent {
/**
	 * 
	 */
	private static final long serialVersionUID = -5262817771855473702L;

/* (non-Javadoc)
 * @see madkit.kernel.Agent#live()
 */
@Override

protected void live() {
	waitNextMessage();
}
}
