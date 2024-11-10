package madkit.gui.fx;

import java.util.function.Consumer;

import org.controlsfx.control.action.ActionCheck;

import javafx.event.ActionEvent;
import madkit.action.ActionData;

@ActionCheck
public class MDKFXActionCheck extends FXAction {

	/**
	 * Builds a new action using {@link FxActionInfo}
	 * 
	 * @param actionInfo
	 * @param eventHandler
	 */
	public MDKFXActionCheck(final ActionData actionInfo, Consumer<ActionEvent> eventHandler) {
		super(actionInfo, eventHandler);
	}

}
