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

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_RIGHT;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JSlider;

import madkit.i18n.I18nUtilities;
import madkit.kernel.Scheduler;
import madkit.message.SchedulingMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum SchedulingAction {

	RUN(VK_O),
	STEP(VK_P),
	SPEED_UP(VK_RIGHT),
	SPEED_DOWN(VK_LEFT),
	SHUTDOWN(VK_DOLLAR);
	
	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(SchedulingAction.class.getSimpleName());

	private ActionInfo actionInfo;
	final private int keyEvent;

	@Override
	public String toString() {
		return ActionInfo.enumToMethodName(this);
	}

	private SchedulingAction(int keyEvent){
		this.keyEvent = keyEvent;
	}
	
	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent, messages);
		return actionInfo;
	}

	public Action getActionFor(final Scheduler agent, final Object... info){
		switch (this) {
		case RUN:
		case STEP:
		case SHUTDOWN:
		return new MKAbstractAction(getActionInfo()){
			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				agent.receiveMessage(new SchedulingMessage(SchedulingAction.this,info));//TODO work with AA but this is probably worthless	
			}
		};
		case SPEED_DOWN:
		case SPEED_UP:
			return new MKAbstractAction(getActionInfo()) {
				public void actionPerformed(ActionEvent e) {
					JSlider s = agent.getSpeedSlider();
					s.setValue(s.getValue() + (SchedulingAction.this == SPEED_UP ? -50 : 50));
				}
			};
		default:
			throw new AssertionError(this);
		}
	}
	
//	public Action getAction(final Scheduler agent){
//		switch (this) {
//		case RUN:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.RUNNING));
//				}
//			};
//		case STEP:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.STEP));
//				}
//			};
//		case SPEED_DOWN:
//		case SPEED_UP:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					JSlider s = agent.getSpeedSlider();
//					s.setValue(s.getValue() + (SchedulingAction.this == SPEED_UP ? -50 : 50));
//				}
//			};
//		default:
//			throw new AssertionError(this);
//		}
//	}
}