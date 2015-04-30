/*
 * Copyright 1997-2015 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

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
	private OutputStream out;
	final private JTextArea outField;
	
	/**
	 * returns the output stream to which log messages will
	 * be forwarded to.
	 * @return the output stream for this component.
	 */
	public OutputStream getOutputStream(){
		return out;
	}
	
	/**
	 * Builds the panel for the agent
	 * 
	 * @param agent
	 */
	public OutputPanel(final AbstractAgent agent)
	{
		outField = new JTextArea(5,32);
		setLayout(new BorderLayout());

		outField.setEditable(false);
		setPreferredSize(new Dimension(250,100));
		
		try {
			@SuppressWarnings("resource")
			final PipedInputStream inPipe = new PipedInputStream();
			out = new PipedOutputStream(inPipe);
			new SwingWorker<Void, String>() {
				@Override
				protected Void doInBackground() throws Exception {
					Scanner s = new Scanner(inPipe);
					while (s.hasNextLine()){
						String line = s.nextLine();
						publish(line + "\n");
					}
					s.close();
					return null;
				}
				@Override
				protected void process(List<String> chunks) {
					for (String line : chunks){
						outField.append(line);
					}
				}
			}.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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

	/**
	 * Remove all the contained text.
	 */
	public void clearOutput()
	{
		outField.setText(null);
	}

	@Override
	public void setBackground(Color bg) {
		if (outField != null) {
			outField.setBackground(bg);
		}
		super.setBackground(bg);
	}

}
