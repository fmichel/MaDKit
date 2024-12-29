package madkit.marketorg;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import madkit.gui.AgentDefaultGUI;
import madkit.gui.AgentLogArea;
import madkit.kernel.Agent;

/**
 * This class provides a default GUI for the agents in the market organization.
 * It displays an image of the agent and a log area for the agent's log records. 
 */
public class MarketAgentGUI extends AgentDefaultGUI {

	private static final Image BROKER = new Image(
			MarketAgentGUI.class.getResourceAsStream("/marketorg/images/broker.png"));
	private static final Image CLIENT = new Image(
			MarketAgentGUI.class.getResourceAsStream("/marketorg/images/client.png"));
	private static final Image BUS = new Image(MarketAgentGUI.class.getResourceAsStream("/marketorg/images/bus.png"));
	private static final Image BOAT = new Image(MarketAgentGUI.class.getResourceAsStream("/marketorg/images/boat.png"));
	private static final Image PLANE = new Image(
			MarketAgentGUI.class.getResourceAsStream("/marketorg/images/plane.png"));
	private static final Image TRAIN = new Image(
			MarketAgentGUI.class.getResourceAsStream("/marketorg/images/train.png"));

	private static int nbOfClientsOnScreen = -1;
	private static int nbOfBrokersOnScreen = -1;
	private static int nbOfProvidersOnScreen = -1;

	/**
	 * Constructs a default GUI for the specified agent. Override this constructor
	 * to customize the initialization of the GUI.
	 *
	 * @param agent the agent for which the GUI is created
	 * @param yPositioning the y position of the GUI on the screen 
	 */
	public MarketAgentGUI(Agent agent, int yPositioning) {
		super(agent);
		positionGUI(agent, yPositioning);
	}

	@Override
	protected Node createCenterNode() {
		VBox vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		vb.getChildren().add(getImageView());
		vb.getChildren().add(new AgentLogArea(getAgent()));
		return vb;
	}

	/**
	 * Positions the GUI on the screen based on the type of agent.
	 * @param agent the agent
	 * @param yPositioning the y position of the GUI on the screen
	 */
	private void positionGUI(Agent agent, int yPositioning) {
		int totalWidth = Screen.getScreens().stream().mapToInt(s -> (int) s.getBounds().getWidth()).sum();
		if (agent instanceof Client) {
			nbOfClientsOnScreen++;
			if (nbOfClientsOnScreen * 550 > totalWidth) {
				nbOfClientsOnScreen = 0;
			}
			getStage().setX(nbOfClientsOnScreen * 550);
		} else if (agent instanceof Broker) {
			nbOfBrokersOnScreen++;
			if (nbOfBrokersOnScreen * 700 > totalWidth) {
				nbOfBrokersOnScreen = 0;
			}
			getStage().setX(nbOfBrokersOnScreen * 700);
		} else if (agent instanceof Provider) {
			nbOfProvidersOnScreen++;
			if (nbOfProvidersOnScreen * 550 > totalWidth) {
				nbOfProvidersOnScreen = 0;
			}
			getStage().setX(nbOfProvidersOnScreen * 550);
		}
		getStage().setY(yPositioning);
	}

	protected ImageView getImageView() {
		String name = getAgent().getName().toLowerCase();
		if (name.contains("broker")) {
			return new ImageView(BROKER);
		} else if (name.contains("train")) {
			return new ImageView(TRAIN);
		} else if (name.contains("boat")) {
			return new ImageView(BOAT);
		} else if (name.contains("plane")) {
			return new ImageView(PLANE);
		} else if (name.contains("bus")) {
			return new ImageView(BUS);
		}
		return new ImageView(CLIENT);
	}

}
