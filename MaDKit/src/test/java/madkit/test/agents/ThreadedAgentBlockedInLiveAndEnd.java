package madkit.test.agents;

import madkit.test.agents.behaviors.BlockedEndBehavior;
import madkit.test.agents.behaviors.BlockedLiveBehavior;

public class ThreadedAgentBlockedInLiveAndEnd extends ThreadedTestAgent
		implements BlockedLiveBehavior, BlockedEndBehavior {
}