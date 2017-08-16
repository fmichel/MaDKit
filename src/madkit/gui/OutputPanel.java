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
import java.awt.EventQueue;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;

/**
 * A scrollable panel that prints all the agent's logging activity.
 * This component is the default panel which is used for the frames assigned to agents that do not define their own GUI
 * and which are launched using <code>true</code> for the <code>createFrame</code> parameter.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 1
 * @see AbstractAgent#setupFrame(AgentFrame)
 */
public class OutputPanel extends JPanel {

    private static final long serialVersionUID = 602152712654986449L;

    private final JTextArea textArea;

    private transient StreamHandler handler;

    /**
     * Builds the panel for the agent
     * 
     * @param agent
     */
    public OutputPanel(final AbstractAgent agent) {
	textArea = new JTextArea(5, 32);
	setLayout(new BorderLayout());

	textArea.setEditable(false);
	setPreferredSize(new Dimension(250, 100));
	setBackground(Color.WHITE);

	initHandler(agent);

	add(BorderLayout.CENTER, new JScrollPane(textArea));

	final JButton b = new JButton("clear");// TODO i18n
	b.addActionListener((evt) -> clearOutput());
	add(BorderLayout.SOUTH, b);
	setBackground(Color.WHITE);
    }

    public void writeToTextArea(String text) {
	if (EventQueue.isDispatchThread()) {
	    textArea.append(text);
	    textArea.setCaretPosition(textArea.getText().length());
	}
	else {
	    EventQueue.invokeLater(() -> writeToTextArea(text));
	}
    }


    /**
     * Remove all the contained text.
     */
    public void clearOutput() {
	textArea.setText(null);
    }

    @Override
    public void setBackground(Color bg) {
	if (textArea != null) {
	    textArea.setBackground(bg);
	}
	super.setBackground(bg);
    }

    private void initHandler(AbstractAgent agent) {
        handler = new StreamHandler() {
    
            @Override
            public synchronized void publish(LogRecord record) {
        	if (!isLoggable(record)) {
        	    return;
        	}
        	String msg;
        	try {
        	    msg = getFormatter().format(record);
        	}
        	catch(Exception ex) {
        	    // We don't want to throw an exception here, but we
        	    // report the exception to any registered ErrorManager.
        	    reportError(null, ex, ErrorManager.FORMAT_FAILURE);
        	    return;
        	}
        	writeToTextArea(msg);
    
            }
    
            @Override
            public boolean isLoggable(LogRecord record) {
        	final int levelValue = getLevel().intValue();
        	if (record.getLevel().intValue() < levelValue || levelValue == Level.OFF.intValue()) {
        	    return false;
        	}
        	final Filter filter = getFilter();
        	if (filter == null) {
        	    return true;
        	}
        	return filter.isLoggable(record);
            }
        };
        handler.setFormatter(AgentLogger.AGENT_FILE_FORMATTER);
        agent.getLogger().addHandler(getHandler());
    }

    /**
     * Returns the handler which has been created for the agent
     * 
     * @return the handler associated with this panel
     */
    public StreamHandler getHandler() {
	return handler;
    }

}
