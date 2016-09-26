/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
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
