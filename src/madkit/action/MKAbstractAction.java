/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.action;

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * This class provides an easy way of building new actions
 * initialized with {@link ActionInfo} objects.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public abstract class MKAbstractAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1414495456612340010L;

	public MKAbstractAction(ActionInfo actionInfo) {
		putValue(Action.NAME, actionInfo.getName());
		putValue(Action.SHORT_DESCRIPTION, actionInfo.getShortDescription());
		putValue(Action.LONG_DESCRIPTION, actionInfo.getLongDescription());
		if (actionInfo.getBigIcon() != null) {
			putValue(AbstractAction.LARGE_ICON_KEY, actionInfo.getBigIcon());
				putValue(AbstractAction.SMALL_ICON, actionInfo.getSmallIcon());
		}
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(actionInfo.getKeyEvent(), KeyEvent.CTRL_MASK));//TODO facto
		putValue(Action.MNEMONIC_KEY, actionInfo.getKeyEvent());
		putValue(Action.ACTION_COMMAND_KEY, actionInfo.getName());
		putValue(Action.SELECTED_KEY, false);
	}

}
