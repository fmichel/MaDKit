package madkit.gui;

import java.util.function.Consumer;

import org.controlsfx.control.action.Action;

import javafx.event.ActionEvent;
import madkit.action.ActionData;

/**
 * Defines an action for JavaFX. This class is a wrapper around {@link Action}
 * to provide a more convenient way to create actions.
 * 
 */
public class FXAction extends Action {

	static {
		FXManager.startFX();
	}

	/**
	 * Builds a new action using {@link ActionData} for the action information.}
	 * 
	 * @param actionInfo   the action information to use
	 * @param eventHandler the event handler for the action
	 */
	public FXAction(final ActionData actionInfo, Consumer<ActionEvent> eventHandler) {
		super(actionInfo.getShortDescription(), eventHandler);
		setLongText(actionInfo.getShortDescription());
		setText(actionInfo.getShortDescription());
		setAccelerator(actionInfo.getAccelerator());
		setGraphic(actionInfo.getGraphic());
//	    setSmallGraphic(actionInfo.getGraphicMenu());

	}
}