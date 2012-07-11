/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
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
 * agents that do not define their own GUI and which are launched using <code>true</code>
 * for the <code>createFrame</code> parameter.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.92
 * @see AbstractAgent#setupFrame(javax.swing.JFrame)
 */
public class OutputPanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 602152712654986449L;
	final private OutputStream out;
	final private JTextArea outField;
	
	/**
	 * returns the output stream to which log messages will
	 * be forwarded to.
	 * @return the output stream for this component.
	 */
	public OutputStream getOutputStream(){
		return out;
	}

	public OutputPanel(final AbstractAgent agent)
	{
		outField = new JTextArea(5,32);
		setLayout(new BorderLayout());

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
			@Override  //TODO check utility
			public void write(byte[] b, int off, int len) throws IOException {  
				updateText(new String(b, off, len));  
		}  
			@Override  
			public void write(byte[] b) throws IOException {  
				write(b, 0, b.length);  
		}  

		};

		final StreamHandler handler = new StreamHandler(out, AgentLogger.AGENT_FILE_FORMATTER){
			@Override
			public synchronized void publish(LogRecord record) {
				super.publish(record);
				flush();
			}
		};
		agent.getLogger().addHandler(handler);

		add(BorderLayout.CENTER,new JScrollPane(outField));

		final JButton b = new JButton("clear");//TODO i18n
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearOutput();
			}
		});
		add(BorderLayout.SOUTH,b);
	}

	public void clearOutput()
	{
		outField.setText(null);
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

//	/**
//	 * This one could be used to directly print in this component.
//	 * 
//	 * @param message the string to display
//	 */
//	public void print(final String message) {//TODO remove ? useless ?
//		 SwingUtilities.invokeLater(new Runnable() {  
//		     public void run() {
//		     outField.append(message);
//		     outField.setCaretPosition(outField.getDocument().getLength());
//		     }  
//		   });
//	}
}

