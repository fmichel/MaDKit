package madkit.test.agents;

import madkit.test.agents.behaviors.ActivateCGR;
import madkit.test.agents.behaviors.EndForever;
import madkit.test.agents.behaviors.LiveForever;

/**
 * @author Fabien Michel
 *
 */
public class CGRForeverInLiveAndEnd extends ThreadedTestAgent implements ActivateCGR, EndForever, LiveForever {

}
