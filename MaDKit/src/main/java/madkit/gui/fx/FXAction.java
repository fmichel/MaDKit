package madkit.gui.fx;

import java.util.function.Consumer;

import org.controlsfx.control.action.Action;

import javafx.event.ActionEvent;
import madkit.action.ActionData;

public class FXAction extends Action {

	static {
		FXManager.startFX();
	}

	/**
	 * Builds a new action using {@link ActionData} for the action information.}
	 * 
	 * @param actionInfo
	 * @param eventHandler
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