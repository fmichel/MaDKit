/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;


/**
 * This component is the default panel which is used for the frames assigned to 
 * agents that do not define their own GUI and which are launched using <code>true</code> for the <code>createFrame</code> parameter.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.2
 * @version 0.91
 * @see AbstractAgent#setupFrame(javax.swing.JFrame)
 */
public class OutputPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7020387969711622732L;
	private OutputStream out;
	final private JTextArea outField;
	private Point GUIlocation = new Point(-1,-1);
	
	
	/**
	 * returns the output stream to which log messages will
	 * be forwarded to.
	 * @return the output stream for this component.
	 */
	public OutputStream getOutputStream(){
		return out;
	}

	public OutputPanel(AbstractAgent agent)
	{
		outField = new JTextArea(5,32);
		setLayout(new BorderLayout());
		//		new OutputStreamWriter

		outField.setEditable(false);
		setPreferredSize(new Dimension(250,100));
		out = new OutputStream() {
			private void updateText(final String txt) {
				 SwingUtilities.invokeLater(new Runnable() {  
					     public void run() {
					     outField.append(txt);
					     outField.setCaretPosition(outField.getDocument().getLength());
					     }  
					   });
			}
				 @Override
			public void write(int b) throws IOException {
				updateText(String.valueOf((char) b));
			}
			@Override  
			public void write(byte[] b, int off, int len) throws IOException {  
				updateText(new String(b, off, len));  
		}  
			@Override  
			public void write(byte[] b) throws IOException {  
				write(b, 0, b.length);  
		}  

		};
		
		Handler handler = new StreamHandler(out, AgentLogger.agentFileFormatter){
			@Override
			public synchronized void publish(LogRecord record) {
				super.publish(record);
				flush();
			}
		};
		agent.getLogger().addHandler(handler);

		add("Center",new JScrollPane(outField));

		final JButton b = new JButton("clear");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outField.setText(null);
			}
		});
		add("South",b);
	}

	public void clearOutput()
	{
		outField.setText(null);
	}

	/**
	 * @param gUIlocation the gUIlocation to set
	 */
	public void setGUIPreferredlocation(Point gUIlocation) {
		GUIlocation = gUIlocation;
	}

	/**
	 * @return the gUIlocation
	 */
	public Point getGUIPreferredlocation() {
		return GUIlocation;
	}
	
	/**
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (outField != null) {
			outField.setBackground(bg);
		}
		super.setBackground(bg);
	}

	/**
	 * This one could be used to directly print in this component.
	 * 
	 * @param message the string to display
	 */
	public void print(final String message) {
		 SwingUtilities.invokeLater(new Runnable() {  
		     public void run() {
		     outField.append(message);
		     outField.setCaretPosition(outField.getDocument().getLength());
		     }  
		   });
	}
}

