/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the [CeCILL|CeCILL-B|CeCILL-C] license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the [CeCILL|CeCILL-B|CeCILL-C]
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
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
