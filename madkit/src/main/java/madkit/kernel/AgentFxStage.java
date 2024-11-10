package madkit.kernel;

import java.util.concurrent.CompletableFuture;

import javafx.stage.Stage;
import madkit.gui.fx.FXManager;

public class AgentFxStage extends Stage {

	public AgentFxStage(final Agent agent) {
		this(agent,true);
	}
	
	public AgentFxStage(final Agent agent, boolean autoCloseOnAgentEnd) {
		setTitle(agent.getName());
		setOnCloseRequest(ae -> {
			try {
				agent.killAgent(agent,2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		if(autoCloseOnAgentEnd) {
			CompletableFuture.runAsync(() -> {
				try {
					synchronized (agent.alive) {
						agent.alive.wait();
					}
					FXManager.runLater(() -> close());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			});
		}
	}
}