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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;


/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public abstract class MKAbstractAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9062133992028327542L;

	public MKAbstractAction(int keyEvent, ImageIcon bigIcon, ImageIcon smallIcon, String[] codes) {
		putValue(Action.NAME, codes[0]);
		putValue(Action.SHORT_DESCRIPTION, codes.length > 1 ? codes[1] : codes[0]);
		putValue(Action.LONG_DESCRIPTION, codes.length > 2 ? codes[2] : getValue(Action.SHORT_DESCRIPTION));
		if (bigIcon != null) {
			putValue(AbstractAction.LARGE_ICON_KEY, bigIcon);
				putValue(AbstractAction.SMALL_ICON, bigIcon);
		}
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyEvent, KeyEvent.CTRL_MASK));
		putValue(Action.MNEMONIC_KEY, keyEvent);
		putValue(Action.ACTION_COMMAND_KEY, codes[0]);
		putValue(Action.SELECTED_KEY, false);
	}
	
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
	
	public static <E extends Enum<E>> String enumToMethodName(E mka){
		final String[] tab = mka.name().split("_");
		String methodName = tab[0].toLowerCase();
		for (int i = 1; i < tab.length; i++) {
			String s = tab[i];
			methodName += s.charAt(0) + s.substring(1).toLowerCase();
		}
		return methodName;
	}

}
