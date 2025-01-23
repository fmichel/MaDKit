package madkit.gui;

import java.util.function.Consumer;

import org.controlsfx.control.action.Action;

import javafx.event.ActionEvent;
import madkit.action.ActionData;
import madkit.action.ActionWithData;

/**
 * Defines an action for JavaFX. This class is a wrapper around {@link Action}
 * to provide a more convenient way to create actions.
 * 
 */
public class ActionFromEnum extends ActionWithData {

	/**
	 * Builds a new action using {@link ActionData} for the action information.
	 * 
	 * @param actionInfo   the action information to use
	 * @param eventHandler the event handler for the action
	 */
	public ActionFromEnum(ActionData actionInfo, Consumer<ActionEvent> eventHandler) {
		super(actionInfo.getShortDescription(), eventHandler);
		setActionData(actionInfo);
	}
}