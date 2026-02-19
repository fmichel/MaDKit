package madkit.test.agents;

import madkit.test.agents.behaviors.ComputeForeverInLive;
import madkit.test.agents.behaviors.EndForeverBehavior;

public class ThreadedAgentForeverInLiveAndEnd extends ThreadedTestAgent
		implements ComputeForeverInLive, EndForeverBehavior {
}
