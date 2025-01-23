package madkit.action;

import java.util.function.Consumer;

import org.controlsfx.control.action.Action;

import javafx.event.ActionEvent;

/**
 * An Action which properties could be set using an {@link ActionData}.
 */
public class ActionWithData extends Action {

	/**
	 * Builds a new action using the given event handler.
	 * 
	 * @param text         the text to display
	 * @param eventHandler the event handler for the action
	 */
	public ActionWithData(String text, Consumer<ActionEvent> eventHandler) {
		super(text, eventHandler);
	}

	/**
	 * Sets the action data for this action.
	 * 
	 * @param actionInfo the action information to use
	 */
	public void setActionData(ActionData actionInfo) {
		setLongText(actionInfo.getShortDescription());
		setAccelerator(actionInfo.getAccelerator());
		setGraphic(actionInfo.getGraphic());
	}

}
