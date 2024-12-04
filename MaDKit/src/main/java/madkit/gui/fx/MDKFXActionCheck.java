
package madkit.gui.fx;

import java.util.function.Consumer;

import org.controlsfx.control.action.ActionCheck;

import javafx.event.ActionEvent;
import madkit.action.ActionData;

/**
 * Class representing a check action in the JavaFX GUI. That is an action that
 * make a state change from <code>false</code> to <code>true</code> when it is
 * performed.
 */
@ActionCheck
public class MDKFXActionCheck extends FXAction {

	/**
	 * Builds a new action using {@link ActionData}.
	 *
	 * @param actionInfo   the action information used to create the action
	 * @param eventHandler the event handler to be executed when the action is
	 *                     performed
	 */
	public MDKFXActionCheck(final ActionData actionInfo, Consumer<ActionEvent> eventHandler) {
		super(actionInfo, eventHandler);
	}

}
