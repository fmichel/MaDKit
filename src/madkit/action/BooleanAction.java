package madkit.action;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import javax.swing.Action;

public class BooleanAction extends MDKAbstractAction {

    private static final long serialVersionUID = -8640665654876640983L;

    private static final Preferences BOOLEAN_PREFERENCES = Preferences.userRoot().node(BooleanAction.class.getName());

    private static AtomicInteger instancesCount = new AtomicInteger(0);
    private final String keyId;

    public BooleanAction(ActionInfo actionInfo) {
	super(actionInfo);
	keyId = getValue(ACTION_COMMAND_KEY).toString() + instancesCount.incrementAndGet();
	addPropertyChangeListener(evt -> {
	    if (evt.getPropertyName().equals("SwingSelectedKey")) {
		final boolean isSelected = (boolean) getValue(SELECTED_KEY);
		BOOLEAN_PREFERENCES.putBoolean(keyId, isSelected);
		onUpdate(isSelected);
	    }
	});
	restoreSelectedKeyPreference();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e == null) { // programmatically triggered
	    putValue(Action.SELECTED_KEY, !(boolean) getValue(Action.SELECTED_KEY));
	}
    }

    public void restoreSelectedKeyPreference() {
	putValue(SELECTED_KEY, BOOLEAN_PREFERENCES.getBoolean(keyId, false));
    }

    /**
     * Method automatically triggered when the action is selected or unselected
     * 
     * @param isSelected
     */
    public void onUpdate(boolean isSelected) {
	// Doing nothing is default behavior
    }
    
    @Override
    public String toString() {
        return "action: "+getValue(ACTION_COMMAND_KEY)+": "+getValue(SELECTED_KEY);
    }

}
