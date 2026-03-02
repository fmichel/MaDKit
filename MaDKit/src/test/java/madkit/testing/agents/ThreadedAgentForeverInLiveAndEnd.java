package madkit.testing.agents;

import madkit.testing.agents.behaviors.ComputeForeverInLive;
import madkit.testing.agents.behaviors.EndForeverBehavior;

public class ThreadedAgentForeverInLiveAndEnd extends ThreadedTestAgent
		implements ComputeForeverInLive, EndForeverBehavior {
}
