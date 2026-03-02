package madkit.testing.agents;

import madkit.testing.agents.behaviors.BlockedEndBehavior;
import madkit.testing.agents.behaviors.BlockedLiveBehavior;

public class ThreadedAgentBlockedInLiveAndEnd extends ThreadedTestAgent
		implements BlockedLiveBehavior, BlockedEndBehavior {
}
