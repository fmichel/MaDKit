/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package com.distrimind.madkitdemos.pingpong;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.distrimind.madkit.gui.OutputPanel;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.Madkit;
import com.distrimind.madkit.message.ObjectMessage;

/**
 * @author Fabien Michel, Olivier Gutknecht
 * @version 5.1
 */
public class PingPong extends Agent {

	private AgentAddress currentPartner = null;
	private ObjectMessage<Color> ball = null;
	private JPanel myPanel;
	private JFrame myFrame;

	@Override
	public void activate() {
		requestRole(new Group("ping-pong", "room"), "player", null);
	}

	@Override
	public void liveCycle() throws InterruptedException {
		searching();
		playing();
	}

	/**
	 * @throws InterruptedException
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void searching() throws InterruptedException {
		currentPartner = null; // searching a new partner
		changeGUIColor(Color.WHITE);
		ball = null;
		while (currentPartner == null) {
			ball = (ObjectMessage<Color>) waitNextMessage(1000);
			if (ball != null) {
				currentPartner = ball.getSender();
			} else {
				currentPartner = getAgentWithRole(new Group("ping-pong", "room"), "player");
			}
		}
		getLogger().info("I found someone to play with : " + currentPartner + " !!! ");
	}

	/**
	 * @throws InterruptedException
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void playing() throws InterruptedException {
		Color ballColor;
		if (ball == null) {
			ballColor = getRandomColor();
			ball = (ObjectMessage<Color>) sendMessageAndWaitForReply(currentPartner, new ObjectMessage<>(ballColor),
					1300);
			if (ball == null) { // nobody replied !
				getLogger().info(currentPartner + " did not reply to me :( !! ");
				currentPartner = null;
				return;
			}
		} else {
			ballColor = ball.getContent();
		}

		changeGUIColor(ballColor);
		ObjectMessage<Color> ballMessage = new ObjectMessage<>(ballColor);

		for (int i = 0; i < 10; i++) {// if ball == null partner is gone !!
			ball = (ObjectMessage<Color>) sendReplyAndWaitForReply(ball, ballMessage, 1300);
			if (ball == null) {
				getLogger().info(currentPartner + " is gone :( !! ");
				break;
			}

			getLogger().info(" Playing :) with " + currentPartner + " ball nb is " + i);

			pause((int) (Math.random() * 1000));
		}
		purgeMailbox(); // purge mailBox from old playing attempts
	}

	@Override
	public void setupFrame(JFrame frame) {
		myPanel = new OutputPanel(this);
		frame.add(myPanel);
		myFrame = frame;
	}

	/**
	 * 
	 */
	private void changeGUIColor(Color c) {
		if (myPanel != null)
			myPanel.setBackground(c);
	}

	public void setFrameLocation(int x, int y) {
		if (myFrame != null) {
			myFrame.setLocation(x, y);
		}
	}

	private Color getRandomColor() {
		return new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	public static void main(String[] args) {
		String[] argss = { "--network", "--launchAgents", PingPong.class.getName(), ",true" };
		Madkit.main(argss);
	}

}