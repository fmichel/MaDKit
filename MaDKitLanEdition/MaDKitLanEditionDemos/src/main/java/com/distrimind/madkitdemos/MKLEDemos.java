package com.distrimind.madkitdemos;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;

import com.distrimind.madkitdemos.bees.BeeLauncher;
import com.distrimind.madkitdemos.marketorg.Client;
import com.distrimind.madkitdemos.pingpong.PingPong;

public class MKLEDemos {
	public static void main(String args[])
	{
		final JFrame frame=new JFrame();
		
		frame.setLayout(new FlowLayout(FlowLayout.CENTER));
		frame.setTitle("MaDKitLanEdition Demos");
		frame.setSize(new Dimension(300, 200));
		String demos[]= {"Ping pong demo", "Bees demo", "Market Demo"};
		final JList<String> list=new JList<>(demos);
		frame.add(list);
		JButton button=new JButton("Select");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				switch(list.getSelectedIndex())
				{
				case 0:
					PingPong.main(new String[0]);
					break;
				case 1:
					BeeLauncher.main(new String[0]);
					break;
				case 2:
					Client.main(new String[0]);
					break;
				}
				frame.dispose();
			}
		});
		frame.setVisible(true);
 	}
	
}
