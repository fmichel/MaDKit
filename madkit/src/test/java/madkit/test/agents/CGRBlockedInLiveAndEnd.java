package madkit.test.agents;

import madkit.test.agents.behaviors.ActivateCGR;
import madkit.test.agents.behaviors.BlockedEnd;
import madkit.test.agents.behaviors.BlockedLive;

/**
 * @author Fabien Michel
 *
 */
public class CGRBlockedInLiveAndEnd extends ThreadedTestAgent implements ActivateCGR, BlockedLive, BlockedEnd {
}